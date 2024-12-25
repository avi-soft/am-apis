package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gender_wise_distribution")
@Getter
@Setter
public class GenderWiseDistribution
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "is_gender_wise")
    Boolean isGenderWise;

    @Column(name = "male_vacancy")
    Long maleVacancy;

    @Column(name = "female_vacancy")
    Long femaleVacancy;

    @Column(name = "total_vacancy")
    Long totalVacancy;

    @OneToMany(mappedBy = "genderWiseDistribution", cascade = CascadeType.ALL)
    private List<CategoryDistribution> categoryDistributions = new ArrayList<>();

    @JsonIgnore
    @OneToOne(mappedBy = "genderWiseDistribution")
    private Post post;
}
