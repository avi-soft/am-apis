package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.CascadeType;
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

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "state_code_id", nullable = false)
    private StateCode stateCode;

    @Column(name = "is_district_distribution", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDistrictDistribution;

    @Column(name = "is_gender_wise")
    private Boolean isGenderWise;

    @Column(name = "male_vacancy")
    private Integer maleVacancy;

    @Column(name = "female_vacancy")
    private Integer femaleVacancy;

    @Column(name = "total_vacancies_in_state", nullable = false)
    private Integer totalVacanciesInState;

    @OneToMany(mappedBy = "stateDistribution", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DistrictDistribution> districtDistributions = new ArrayList<>();

    @OneToMany(mappedBy = "stateDistribution", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CategoryDistribution> categoryDistributions = new ArrayList<>();


}
