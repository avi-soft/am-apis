package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "district_distribution")
@Getter
@Setter
public class DistrictDistribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "state_distribution_id", nullable = false)
    private StateDistribution stateDistribution;

    @ManyToOne
    @JoinColumn(name = "district_id", nullable = false)
    private Districts district;

    @Column(name = "is_gender_wise")
    private Boolean isGenderWise;

    @Column(name = "male_vacancy")
    private Integer maleVacancy;

    @Column(name = "female_vacancy")
    private Integer femaleVacancy;

    @Column(name = "total_vacancy")
    private Integer totalVacancy;

    @OneToMany(mappedBy = "districtDistribution", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DistrictCategoryDistribution> categoryDistributions = new ArrayList<>();
}