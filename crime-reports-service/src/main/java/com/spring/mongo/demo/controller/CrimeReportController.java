package com.spring.mongo.demo.controller;

import com.spring.mongo.demo.model.Upvote;
import com.spring.mongo.demo.service.CrimeReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import org.bson.Document;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/crime-reports")
public class CrimeReportController {

    @Autowired
    private CrimeReportService crimeReportService;


    @GetMapping("/query1")
    public List<Document> getTotalReportsByCrimeCode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return crimeReportService.getTotalReportsByCrimeCode(startDate, endDate);
    }


    @GetMapping("/query2")
    public List<Document> getDailyReportsByCrimeCode(
            @RequestParam String crimeCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return crimeReportService.getDailyReportsByCrimeCode(crimeCode, startDate, endDate);
    }


    @GetMapping("/query3")
    public List<Document> getTopCrimesPerArea(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return crimeReportService.getTopCrimesByAreaForDay(date);
    }

    @GetMapping("/query4")
    public List<Document> getTopCrimesPerArea(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return crimeReportService.getTwoLeastCommonCrimes(startDate, endDate);
    }

    @GetMapping("/query5")
    public List<Document> getWeaponsUsedInSameCrimeAcrossMultipleAreas() {
        return crimeReportService.getWeaponsUsedInSameCrimeAcrossMultipleAreas();
    }

    @GetMapping("/query6")
    public List<Document> getCrimeReportsByLocation(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return crimeReportService.getTopUpvotedReportsForDay(date);
    }

    @GetMapping("/query7")
    public List<Document> getTop50ActiveOfficers() {
        return crimeReportService.getTop50ActiveOfficers();
    }

    @GetMapping("/query8")
    public List<Document> getTopFiftyOfficersByUpvotedAreas() {
        return crimeReportService.getTopFiftyOfficersByUpvotedAreas();
    }

    @GetMapping("/query9")
    public List<Document> getOfficersWithMultipleBadgeNumbers() {
        return crimeReportService.getOfficersWithMultipleBadgeNumbers();
    }

    @GetMapping("/query10")
    public List<Document> getAreasAndReportsByOfficer(
            @RequestParam String officerName) {
        return crimeReportService.getAreasAndReportsByOfficer(officerName);
    }

    @PostMapping("/create")
    public String createCrimeReport(@RequestBody Document crimeReport) {
        return crimeReportService.createCrimeReport(crimeReport);
    }



    @PostMapping("/{drNo}/upvote")
    public String upvoteCrimeReport(@PathVariable String drNo, @RequestBody Upvote upvote) {
        return crimeReportService.upvoteCrimeReport(drNo, upvote);
    }
}
