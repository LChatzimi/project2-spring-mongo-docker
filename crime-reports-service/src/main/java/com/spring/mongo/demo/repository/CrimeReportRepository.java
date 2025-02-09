package com.spring.mongo.demo.repository;

import com.spring.mongo.demo.model.CrimeReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CrimeReportRepository extends MongoRepository<CrimeReport, String> {
    Optional<CrimeReport> findByDrNo(String drNo);
}
