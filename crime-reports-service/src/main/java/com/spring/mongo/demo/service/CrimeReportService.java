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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
