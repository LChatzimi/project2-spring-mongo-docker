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


    @PostMapping("/{drNo}/upvote")
    public String upvoteCrimeReport(@PathVariable String drNo, @RequestBody Upvote upvote) {
        return crimeReportService.upvoteCrimeReport(drNo, upvote);
    }
}
