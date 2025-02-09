package com.spring.mongo.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "crime_reports")
public class CrimeReport implements Serializable {

    @Id
    private String id;

    private String drNo;
    private LocalDate dateReported;
    private LocalDate dateOccurred;
    private String timeOccurred;
    private Integer reportDistrictNo;
    private Integer part1or2;
    private String status;
    private String statusDescription;
    private String location;
    private String crossStreet;

    private Float latitude;
    private Float longitude;
    private AreaInfo areaInfo;

    private PremiseInfo premiseInfo;

    private Set<CrimeCodes> crimeCodes;

    private VictimInfo victimInfo;

    private WeaponInfo weaponInfo;

    private List<String> mocodes;


    private List<Upvote> upvotes;
}
