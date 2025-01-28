package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

    @Entity
    @Table(name = "zone_distribution")
    @Getter
    @Setter
    public class ZoneDistribution {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @JsonIgnore
        @ManyToOne
        @JoinColumn(name = "post_id", nullable = false)
        private Post post;

        @ManyToOne
        @JoinColumn(name = "zone_id", nullable = false)
        private Zone zone;

        @Column(name = "is_division_distribution", columnDefinition = "BOOLEAN DEFAULT FALSE")
        private Boolean isDivisionDistribution;

        // State level gender distribution (used when isDistrictDistribution is false)
        @Column(name = "is_gender_wise")
        private Boolean isGenderWise;

        @Column(name = "male_vacancy")
        private Integer maleVacancy;

        @Column(name = "female_vacancy")
        private Integer femaleVacancy;

        @Column(name = "total_vacancies_in_zone", nullable = false)
        private Integer totalVacanciesInZone;

        // District level distributions
        @OneToMany(mappedBy = "zoneDistribution", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<DivisionDistribution> divisionDistributions = new ArrayList<>();

        // Category distributions
        @OneToMany(mappedBy = "zoneDistribution", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
        private List<CategoryDistribution> categoryDistributions = new ArrayList<>();
}

