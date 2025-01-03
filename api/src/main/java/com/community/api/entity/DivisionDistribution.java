package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "division_distribution")
@Getter
@Setter
public class DivisionDistribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "zone_distribution_id", nullable = false)
    private ZoneDistribution zoneDistribution;

    @ManyToOne
    @JoinColumn(name = "division_id", nullable = false)
    private ZoneDivisions divisions;

    @Column(name = "is_gender_wise")
    private Boolean isGenderWise;

    @Column(name = "male_vacancy")
    private Integer maleVacancy;

    @Column(name = "female_vacancy")
    private Integer femaleVacancy;

    @Column(name = "total_vacancy")
    private Integer totalVacancy;

    @OneToMany(mappedBy = "divisionDistribution", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DivisionCategoryDistribution> categoryDistributions = new ArrayList<>();
}