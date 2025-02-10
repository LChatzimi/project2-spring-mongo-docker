package com.spring.mongo.demo.service;

import com.spring.mongo.demo.model.*;
import com.spring.mongo.demo.repository.CrimeReportRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CrimeReportDataInitializer {

    private final CrimeReportRepository crimeReportRepository;

    private final Logger LOGGER = Logger.getLogger(getClass().getName());


    public void initData(String locationPattern) {
        //find crime report size > 0 return
        if (crimeReportRepository.count() > 0) {
            return;
        }
        System.out.println("start time: " + LocalDateTime.now());
        DateTimeFormatter formatter =  DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
        List<CrimeReport> crimeReports = new ArrayList<>();
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(locationPattern);
            for (Resource resource : resources) {
                try (Reader reader = new InputStreamReader(resource.getInputStream());
                     CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {
                    for (CSVRecord row : csvParser) {

                        CrimeReport crimeReport = CrimeReport.builder()
                                .drNo(row.get(0)) // DR_NO
                                .dateReported(LocalDateTime.parse(row.get(1), formatter).toLocalDate()) // Date Rptd
                                .dateOccurred(LocalDateTime.parse(row.get(2), formatter).toLocalDate()) // DATE OCC
                                .timeOccurred(row.get(3)) // TIME OCC
                                .areaInfo(AreaInfo.builder()
                                        .areaId(Integer.parseInt(row.get(4))) // AREA
                                        .areaName(row.get(5)) // AREA NAME
                                        .build())
                                .reportDistrictNo(Integer.parseInt(row.get(6))) // Rpt Dist No
                                .part1or2(Integer.parseInt(row.get(7))) // Part 1/2
                                .crimeCodes(Stream.of(CrimeCodes.builder()
                                                .crimeCode(row.get(8)) // Crm Cd
                                                .crimeDescription(row.get(9)) // Crm Cd Desc
                                                .rank(1)
                                                .build(),
                                        CrimeCodes.builder()
                                                .crimeCode(row.get(21)) // Crm Cd
                                                .crimeDescription("") // Crm Cd Desc
                                                .rank(2)
                                                .build(),
                                        CrimeCodes.builder()
                                                .crimeCode(row.get(22)) // Crm Cd
                                                .crimeDescription("") // Crm Cd Desc
                                                .rank(3)
                                                .build(),
                                        CrimeCodes.builder()
                                                .crimeCode(row.get(23)) // Crm Cd
                                                .crimeDescription("") // Crm Cd Desc
                                                .rank(4)
                                                .build()
                                ).collect(Collectors.toSet()))
                                .mocodes(Stream.of(row.get(10)).collect(Collectors.toList())) // MO Codes
                                .victimInfo(VictimInfo.builder()
                                        .victimAge(Integer.parseInt(row.get(11)))// Vict Age
                                        .victimSex(row.get(12)) // Vict Sex
                                        .victimDescent(row.get(13)) // Vict Descent
                                        .build())
                                .premiseInfo(PremiseInfo.builder()
                                        .premiseCode(row.get(14)) // Premis Cd
                                        .premiseDescription(row.get(15)) // Premis Desc
                                        .build())
                                .weaponInfo(WeaponInfo.builder()
                                        .weaponUsedCode(row.get(16)) // Weapon Used Cd
                                        .weaponDescription(row.get(17)) // Weapon Desc
                                        .build())
                                .status(row.get(18)) // Status
                                .statusDescription(row.get(19)) // Status Desc
                                .location(row.get(24)) // LOCATION
                                .crossStreet(row.get(25)) // Cross Street
                                .latitude(Float.parseFloat(row.get(26))) // LAT
                                .longitude(Float.parseFloat(row.get(27))) // LON
                                .build();
                        if (Math.random() < (1.0 / 3)) {
                            crimeReport.setUpvotes(generateUpvotes());
                        }
                        crimeReports.add(crimeReport);

                        if (row.getRecordNumber() % 1000 == 0) {
                            LOGGER.info("row: " + row.getRecordNumber());
                            crimeReportRepository.saveAll(crimeReports);
                            crimeReports.clear();
                        }

                    }
                }
            }
            crimeReportRepository.saveAll(crimeReports);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("end time: " + LocalDateTime.now());
    }

    private List<Upvote> generateUpvotes() {
        List<Upvote> upvotes = new ArrayList<>();
        for (int i = 0; i < Math.random() * 1000 ; i++) {
            int officerNumber = (int) (Math.random() * 50000);
            String batchNumber = String.format("%09d", officerNumber);
            Upvote upvote = Upvote.builder()
                    .officerName("Officer " + officerNumber)
                    .officerEmail("Officer"+officerNumber+"@mail.com")
                    .badgeNumber(batchNumber)
                    .creationDate(LocalDateTime.now().toLocalTime())
                    .build();

            upvotes.add(upvote);
        }
        return upvotes;
    }
}
