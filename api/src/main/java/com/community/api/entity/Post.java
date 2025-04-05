package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.JoinColumn;
import javax.persistence.CascadeType;
import javax.persistence.ManyToMany;
import javax.persistence.JoinTable;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "post_details")
@Getter
@Setter
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(name = "post_name",nullable = false)
    private String postName;

    @Column(name = "post_total_vacancies",nullable = false)
    private Long postTotalVacancies;

    @Column(name = "post_code")
    private String postCode;

    @ManyToMany
    @JoinTable(
            name = "post_vacancy_distribution_type",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "vacancy_distribution_type_id")
    )
    private List<VacancyDistributionType> vacancyDistributionTypes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<StateDistribution> stateDistributions = new ArrayList<>();

    @OneToMany(mappedBy = "post",cascade = CascadeType.ALL)
    private List<ZoneDistribution> zoneDistributions= new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "gender_wise_distribution_id", referencedColumnName = "id")
    private GenderWiseDistribution genderWiseDistribution;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QualificationEligibility>qualificationEligibility;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "product_id")
    private CustomProduct product;

    @JsonProperty("reserve_category_age")
    @ManyToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    @JoinTable(
            name = "post_age_requirement",  // The name of the mapping table
            joinColumns = @JoinColumn(name = "post_id"),  // Foreign key to the Product entity
            inverseJoinColumns = @JoinColumn(name = "age_requirement_id")  // Foreign key to CustomProductReserveCategoryBornBeforeAfterRef
    )
    private List<CustomProductReserveCategoryBornBeforeAfterRef> ageRequirement;
    @JsonIgnore
    private Long refId;
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL/*,fetch = FetchType.EAGER*/)
    private List<CustomProductGenderPhysicalRequirementRef> physicalRequirements = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<OtherDistribution> otherDistributions = new ArrayList<>();
}
