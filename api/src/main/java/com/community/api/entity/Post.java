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
import java.util.List;

@Entity
@Table(name = "post_details")
@Getter
@Setter
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long postId;

    @Column(name = "post_name",nullable = false)
    String postName;

    @Column(name = "post_total_vacancies",nullable = false)
    Long postTotalVacancies;

    @Column(name = "post_code")
    String postCode;

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

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "product_id")
    private CustomProduct product;

    @OneToOne
    @JsonProperty("reserve_category_age")
    @JoinColumn(name = "product_reserve_category_id")
    private CustomProductReserveCategoryBornBeforeAfterRef ageRequirement;
    @JsonIgnore
    private Long refId;
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<CustomProductGenderPhysicalRequirementRef> physicalRequirements = new ArrayList<>();

}
