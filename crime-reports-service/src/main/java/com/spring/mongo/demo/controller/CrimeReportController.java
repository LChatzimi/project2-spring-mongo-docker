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

    @PostMapping("/{drNo}/upvote")
    public String upvoteCrimeReport(@PathVariable String drNo, @RequestBody Upvote upvote) {
        return crimeReportService.upvoteCrimeReport(drNo, upvote);
    }
}
