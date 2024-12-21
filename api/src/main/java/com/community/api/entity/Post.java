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
@Table(name = "post_details")
@Getter
@Setter
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long postId;

    @Column(name = "post_name",nullable = false)
    String postName;

    @Column(name = "total_vacancies",nullable = false)
    String totalVacancies;

    @Column(name = "post_code")
    String postCode;

    @ManyToMany
    @JoinTable(
            name = "post_vacancy_distribution_type",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "vacancy_distribution_type_id")
    )
    private List<VacancyDistributionType> vacancyDistributionTypes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StateDistribution> stateDistributions = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private CustomProduct product;

}
