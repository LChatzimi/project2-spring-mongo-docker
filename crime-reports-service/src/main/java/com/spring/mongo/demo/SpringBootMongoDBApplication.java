package com.spring.mongo.demo;

import com.spring.mongo.demo.service.CrimeReportDataInitializer;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.TimeZone;


@SpringBootApplication
@EnableMongoRepositories
public class SpringBootMongoDBApplication {


	public static void main(String[] args) {
		SpringApplication.run(SpringBootMongoDBApplication.class, args);
	}

	
	@Bean
	CommandLineRunner runner(CrimeReportDataInitializer crimeReportDataInitializer) {
		return args -> crimeReportDataInitializer.initData("classpath:Crime_Data_from_2020_to_Present_20250130.csv");
	}

	@PostConstruct
	public void init(){
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

}
