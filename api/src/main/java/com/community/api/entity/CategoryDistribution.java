package com.community.api.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "category_distribution")
@Getter
@Setter
public class CategoryDistribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "state_distribution_id", nullable = false)
    private StateDistribution stateDistribution;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CustomReserveCategory category;

    @Column(name = "vacancies", nullable = false)
    private Integer vacancies;
}
