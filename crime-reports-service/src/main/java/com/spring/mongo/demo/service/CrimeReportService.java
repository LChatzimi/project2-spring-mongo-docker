package com.spring.mongo.demo.service;

import com.mongodb.BasicDBObject;
import com.spring.mongo.demo.model.CrimeReport;
import com.spring.mongo.demo.model.Upvote;
import com.spring.mongo.demo.repository.CrimeReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

import org.bson.Document;

@Service
public class CrimeReportService {

    @Autowired
    private CrimeReportRepository crimeReportRepository;

    private final MongoTemplate mongoTemplate;

    public CrimeReportService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     *  Query 1
     */
    public List<Document> getTotalReportsByCrimeCode(LocalDate startDate, LocalDate endDate) {
        Date start = toDate(startDate.atStartOfDay());
        Date end = toDate(endDate.atTime(23, 59, 59));

        UnwindOperation unwindCrimeCodes = Aggregation.unwind("crimeCodes");

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("dateOccurred").gte(start).lte(end)
                        .and("crimeCodes.crimeCode").ne("")
        );

        GroupOperation groupOperation = Aggregation.group("crimeCodes.crimeCode")
                .count().as("totalReports");

        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "totalReports");

        Aggregation aggregation = Aggregation.newAggregation(
                unwindCrimeCodes,
                matchOperation,
                groupOperation,
                sortOperation
        );

        return mongoTemplate.aggregate(aggregation, "crime_reports", Document.class).getMappedResults();
    }


    /**
     *  Query 2
     */
    public List<Document> getDailyReportsByCrimeCode(String crimeCode, LocalDate startDate, LocalDate endDate) {
        Date start = toDate(startDate.atStartOfDay());
        Date end = toDate(endDate.atTime(23, 59, 59));

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("crimeCodes.crimeCode").is(crimeCode)
                        .and("dateOccurred").gte(start).lte(end)
        );

        ProjectionOperation projectDate = Aggregation.project()
                .and(DateOperators.dateOf("dateOccurred").toString("%Y-%m-%d")).as("reportDate");

        GroupOperation groupByDate = Aggregation.group("reportDate")
                .count().as("totalReports");

        SortOperation sortByDate = Aggregation.sort(Sort.by(Sort.Direction.ASC, "_id"));

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                projectDate,
                groupByDate,
                sortByDate
        );

        return mongoTemplate.aggregate(aggregation, "crime_reports", Document.class).getMappedResults();
    }

    /**
     *  Query 3
     */
    public List<Document> getTopCrimesByAreaForDay(LocalDate date) {
        Date startOfDay = toDate(date.atStartOfDay());
        Date endOfDay = toDate(date.atTime(23, 59, 59));

        MatchOperation dateMatch = Aggregation.match(
                Criteria.where("dateOccurred")
                        .gte(startOfDay)
                        .lte(endOfDay)
        );

        UnwindOperation unwindCrimeCodes = Aggregation.unwind("crimeCodes");

        MatchOperation crimeCodeMatch = Aggregation.match(
                Criteria.where("crimeCodes.crimeCode").ne("")
        );

        GroupOperation groupByAreaAndCrime = Aggregation.group(
                Fields.from(
                        Fields.field("areaName", "$areaInfo.areaName"),
                        Fields.field("crimeCode", "$crimeCodes.crimeCode")
                )
        ).count().as("totalReports");

        SortOperation sortByTotalReports = Aggregation.sort(Sort.Direction.DESC, "totalReports");

        GroupOperation groupByArea = Aggregation.group("_id.areaName")
                .push(
                        new BasicDBObject("crime", "$_id.crimeCode")
                                .append("count", "$totalReports")
                ).as("topCrimes");

        ProjectionOperation projectTop3Crimes = Aggregation.project()
                .and("topCrimes").slice(3).as("topCrimes");



        Aggregation aggregation = Aggregation.newAggregation(
                dateMatch,
                unwindCrimeCodes,
                crimeCodeMatch,
                groupByAreaAndCrime,
                sortByTotalReports,
                groupByArea,
                projectTop3Crimes
        );

        return mongoTemplate.aggregate(aggregation, "crime_reports", Document.class).getMappedResults();
    }

    /**
     *  Query 4
     */
    public List<Document> getTwoLeastCommonCrimes(LocalDate startDate, LocalDate endDate) {
        Date start = toDate(startDate.atStartOfDay());
        Date end = toDate(endDate.atTime(23, 59, 59));

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("dateOccurred").gte(start).lte(end)
        );

        UnwindOperation unwindCrimeCodes = Aggregation.unwind("crimeCodes");

        GroupOperation groupOperation = Aggregation.group("crimeCodes.crimeDescription")
                .count().as("totalReports");

        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "totalReports");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                unwindCrimeCodes,
                groupOperation,
                sortOperation,
                Aggregation.limit(2)
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "crime_reports", Document.class);
        return results.getMappedResults();
    }

    /**
     *  Query 5
     */
    public List<Document> getWeaponsUsedInSameCrimeAcrossMultipleAreas() {
        UnwindOperation unwindCrimeCodes = Aggregation.unwind("crimeCodes");

        MatchOperation initialMatch = Aggregation.match(
                Criteria.where("crimeCodes.crimeCode").ne("")
                        .and("weaponInfo.weaponDescription").ne("")
        );

        GroupOperation groupByCrimeAndWeapon = Aggregation.group(
                Fields.from(Fields.field("crimeCode", "crimeCodes.crimeCode"),
                        Fields.field("weaponUsed", "weaponInfo.weaponDescription"))
        ).addToSet("areaInfo.areaName").as("areas");

        MatchOperation areasGreaterThanOne = Aggregation.match(
                Criteria.where("areas.1").exists(true)
        );

        GroupOperation groupByCrimeCode = Aggregation.group("_id.crimeCode")
                .addToSet("_id.weaponUsed").as("weapons");

        ProjectionOperation projectFields = Aggregation.project()
                .and("_id").as("crimeCode")
                .and("weapons").as("weapons")
                .andExclude("_id");

        Aggregation aggregation = Aggregation.newAggregation(
                unwindCrimeCodes,
                initialMatch,
                groupByCrimeAndWeapon,
                areasGreaterThanOne,
                groupByCrimeCode,
                projectFields
        );

        return mongoTemplate.aggregate(aggregation, "crime_reports", Document.class).getMappedResults();
    }

    /**
     *  Query 6
     */
    public List<Document> getTopUpvotedReportsForDay(LocalDate date) {
        Date startOfDay = toDate(date.atStartOfDay());
        Date endOfDay = toDate(date.atTime(23, 59, 59));

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("dateOccurred").gte(startOfDay).lte(endOfDay)
        );

        ProjectionOperation projectOperation = Aggregation.project("drNo", "dateOccurred")
                .and(
                        ArrayOperators.Size.lengthOfArray(
                                ConditionalOperators.ifNull("upvotes").then(Collections.emptyList())
                        )
                ).as("totalUpvotes");

        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "totalUpvotes");

        LimitOperation limitOperation = Aggregation.limit(50);

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                projectOperation,
                sortOperation,
                limitOperation
        );

        return mongoTemplate.aggregate(aggregation, "crime_reports", Document.class).getMappedResults();
    }

    /**
     *  Query 7
     */
    public List<Document> getTop50ActiveOfficers() {
        UnwindOperation unwindUpvotes = Aggregation.unwind("upvotes");

        GroupOperation groupOperation = Aggregation.group(
                Fields.from(
                        Fields.field("officerName", "upvotes.officerName"),
                        Fields.field("officerEmail", "upvotes.officerEmail"),
                        Fields.field("badgeNumber", "upvotes.badgeNumber")
                )
        ).count().as("totalUpvotes");

        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "totalUpvotes");

        LimitOperation limitOperation = Aggregation.limit(50);

        ProjectionOperation projectOperation = Aggregation.project()
                .and("_id.officerName").as("officerName")
                .and("_id.officerEmail").as("officerEmail")
                .and("_id.badgeNumber").as("badgeNumber")
                .andInclude("totalUpvotes");

        Aggregation aggregation = Aggregation.newAggregation(
                unwindUpvotes,
                groupOperation,
                sortOperation,
                limitOperation,
                projectOperation
        );

        return mongoTemplate.aggregate(aggregation, "crime_reports", Document.class).getMappedResults();
    }


    /**
     *  Query 8
     */
    public List<Document> getTopFiftyOfficersByUpvotedAreas() {
        UnwindOperation unwindUpvotes = Aggregation.unwind("upvotes");

        GroupOperation groupByOfficer = Aggregation.group(
                Fields.fields(
                        "upvotes.officerName",
                        "upvotes.officerEmail",
                        "upvotes.badgeNumber"
                )
        ).addToSet("areaInfo.areaName").as("areas");

        ProjectionOperation projectFields = Aggregation.project()
                .and("_id.upvotes.officerName").as("officerName")
                .and("_id.upvotes.officerEmail").as("officerEmail")
                .and("_id.upvotes.badgeNumber").as("badgeNumber")
                .and(ArrayOperators.Size.lengthOfArray("$areas")).as("totalAreas");

        SortOperation sortByTotalAreas = Aggregation.sort(Sort.Direction.DESC, "totalAreas");

        LimitOperation limitToFifty = Aggregation.limit(50);

        Aggregation aggregation = Aggregation.newAggregation(
                unwindUpvotes,
                groupByOfficer,
                projectFields,
                sortByTotalAreas,
                limitToFifty
        );

        return mongoTemplate.aggregate(aggregation, "crime_reports", Document.class).getMappedResults();
    }


    /**
     *  Query 9
     */
    public List<Document> getOfficersWithMultipleBadgeNumbers() {
        UnwindOperation unwindUpvotes = Aggregation.unwind("upvotes");

        GroupOperation groupByOfficerEmail = Aggregation.group("upvotes.officerEmail")
                .addToSet("upvotes.badgeNumber").as("badgeNumbers")
                .addToSet("drNo").as("reports");

        MatchOperation matchMultipleBadgeNumbers = Aggregation.match(
                Criteria.where("badgeNumbers.1").exists(true)
        );

        ProjectionOperation projectFields = Aggregation.project()
                .and("_id").as("officerEmail")
                .andInclude("badgeNumbers", "reports");

        Aggregation aggregation = Aggregation.newAggregation(
                unwindUpvotes,
                groupByOfficerEmail,
                matchMultipleBadgeNumbers,
                projectFields
        ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());

        return mongoTemplate.aggregate(aggregation, "crime_reports", Document.class).getMappedResults();
    }


    /**
     *  Query 10
     */
    public List<Document> getAreasAndReportsByOfficer(String officerName) {
        UnwindOperation unwindUpvotes = Aggregation.unwind("upvotes");

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("upvotes.officerName").is(officerName)
        );

        GroupOperation groupOperation = Aggregation.group("upvotes.officerName")
                .addToSet("areaInfo.areaName").as("areas")
                .addToSet("drNo").as("reports");

        ProjectionOperation projectOperation = Aggregation.project()
                .and("$_id").as("officerName")
                .andInclude("areas", "reports");

        Aggregation aggregation = Aggregation.newAggregation(
                unwindUpvotes,
                matchOperation,
                groupOperation,
                projectOperation
        );

        return mongoTemplate.aggregate(aggregation, "crime_reports", Document.class).getMappedResults();
    }

    /**
     *  Create a new crime report
     */
    public String createCrimeReport(Document crimeReport) {

        CrimeReport newCrimeReport = new CrimeReport();
        newCrimeReport.setDrNo(crimeReport.getString("drNo"));
        String dateReported = crimeReport.getString("dateReported");
        if (dateReported != null) {
            newCrimeReport.setDateReported(LocalDate.parse(dateReported));
        }
        String dateOccurred = crimeReport.getString("dateOccurred");
        if (dateOccurred != null) {
            newCrimeReport.setDateOccurred(LocalDate.parse(dateOccurred));
        }
        newCrimeReport.setTimeOccurred(crimeReport.getString("timeOccurred"));
        newCrimeReport.setReportDistrictNo(crimeReport.getInteger("reportDistrictNo"));
        newCrimeReport.setPart1or2(crimeReport.getInteger("part1or2"));
        newCrimeReport.setStatus(crimeReport.getString("status"));
        newCrimeReport.setStatusDescription(crimeReport.getString("statusDescription"));
        newCrimeReport.setLocation(crimeReport.getString("location"));
        newCrimeReport.setCrossStreet(crimeReport.getString("crossStreet"));
        newCrimeReport.setLatitude(crimeReport.getDouble("latitude").floatValue());
        newCrimeReport.setLongitude(crimeReport.getDouble("longitude").floatValue());
        newCrimeReport.setAreaInfo(newCrimeReport.getAreaInfo());
        newCrimeReport.setPremiseInfo(newCrimeReport.getPremiseInfo());
        newCrimeReport.setCrimeCodes(newCrimeReport.getCrimeCodes());
        newCrimeReport.setVictimInfo(newCrimeReport.getVictimInfo());
        newCrimeReport.setWeaponInfo(newCrimeReport.getWeaponInfo());
        newCrimeReport.setMocodes(newCrimeReport.getMocodes());
        newCrimeReport.setUpvotes(newCrimeReport.getUpvotes());

        crimeReportRepository.save(newCrimeReport);
        return "Crime report successfully created.";
    }




    private Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.of("UTC")).toInstant());
    }




    public String upvoteCrimeReport(String drNo, Upvote newUpvote) {
        Optional<CrimeReport> optionalReport = crimeReportRepository.findByDrNo(drNo);
        
        if (optionalReport.isPresent()) {
            CrimeReport report = optionalReport.get();

            if (report.getUpvotes() == null) {
                report.setUpvotes(new ArrayList<>());
            }
            boolean alreadyUpvoted =  report.getUpvotes().stream()
                .anyMatch(upvote -> upvote.getOfficerEmail().equals(newUpvote.getOfficerEmail()) &&
                                    upvote.getBadgeNumber().equals(newUpvote.getBadgeNumber()));

            if (alreadyUpvoted) {
                return "Officer has already upvoted this report.";
            }

            newUpvote.setCreationDate(LocalTime.now());
            report.getUpvotes().add(newUpvote);
            crimeReportRepository.save(report);

            return "Upvote successfully added.";
        } else {
            return "Crime report not found.";
        }
    }
}
