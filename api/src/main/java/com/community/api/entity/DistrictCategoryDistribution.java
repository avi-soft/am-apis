package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "district_category_distribution")
@Getter
@Setter
public class DistrictCategoryDistribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL,optional = true)
    @JoinColumn(name = "district_distribution_id", nullable = false)
    private DistrictDistribution districtDistribution;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CustomReserveCategory category;

    @Column(name = "vacancy_count")
    private Integer vacancyCount;
}

