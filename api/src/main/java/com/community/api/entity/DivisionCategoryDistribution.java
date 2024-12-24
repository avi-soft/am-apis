package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
        import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "division_category_distribution")
@Getter
@Setter
public class DivisionCategoryDistribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL,optional = true)
    @JoinColumn(name = "division_distribution_id", nullable = false)
    private DivisionDistribution divisionDistribution;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CustomReserveCategory category;

    @Column(name = "vacancy_count")
    private Integer vacancyCount;
}

