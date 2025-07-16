package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "qualification_group")
@Getter
@Setter
public class QualificationGroup implements Serializable {

    @Id
    @Column(name = "qualification_group_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long qualificationGroupId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "group_order")
    private Integer groupOrder;

    @OneToMany(mappedBy = "qualificationGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QualificationEligibility> qualificationEligibilityInGroup = new ArrayList<>();

    @Column(name = "additional_comments", columnDefinition = "text")
    private String additionalComments;
}