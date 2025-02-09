package com.spring.mongo.demo.service;

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
     *  1. Find the total number of reports per ‘Crm Cd” that occurred within a specified time range and sort them in a descending order.
     * @param startDate the start date of the time range
     * @param endDate the end date of the time range
     * @return a list of documents containing the crime code and the total number of reports
     */
    public List<Document> getTotalReportsByCrimeCode(LocalDate startDate, LocalDate endDate) {
        // Ensure UTC time zone consistency
        Date start = toDate(startDate.atStartOfDay());
        Date end = toDate(endDate.atTime(23, 59, 59));

        UnwindOperation unwindCrimeCodes = Aggregation.unwind("crimeCodes");

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("dateOccurred").gte(start).lte(end)
                        .and("crimeCodes.crimeCode").ne("")  // Exclude empty and null crime codes
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
