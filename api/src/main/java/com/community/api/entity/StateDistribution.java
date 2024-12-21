package com.community.api.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "state_distribution")
@Getter
@Setter
public class StateDistribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "state_code_id", nullable = false)
    private StateCode stateCode;

    @ManyToMany
    @JoinTable(
        name = "state_district_distribution",
        joinColumns = @JoinColumn(name = "state_distribution_id"),
        inverseJoinColumns = @JoinColumn(name = "district_id")
    )
    private List<Districts> districts = new ArrayList<>();

    @Column(name = "is_gender_wise", nullable = false)
    private Boolean isGenderWise = false;

    @Column(name = "vacancies_male")
    private Integer vacanciesMale;

    @Column(name = "vacancies_female")
    private Integer vacanciesFemale;

    @Column(name = "total_vacancies_in_state", nullable = false)
    private Integer totalVacanciesInState;

//     Category-wise distribution
    @OneToMany(mappedBy = "stateDistribution", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoryDistribution> categoryDistributions = new ArrayList<>();


}
