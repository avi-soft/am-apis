package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "other_distribution")
@Getter
@Setter
public class OtherDistribution implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_vacancy")
    Long totalVacancy;

    @Column(name = "other_distribution_value", columnDefinition = "text")
    String otherDistributionValue;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @JsonIgnore
    @Column(name = "additional_comments", columnDefinition = "text")
    @JsonProperty("additional_comments")
    private String additionalComments;
}