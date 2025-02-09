package com.spring.mongo.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Upvote {
    private String officerName;
    private String officerEmail;
    private String badgeNumber;
    private LocalTime creationDate;
}
