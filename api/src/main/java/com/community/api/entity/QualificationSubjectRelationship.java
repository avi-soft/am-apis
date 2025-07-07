package com.community.api.entity;

import javax.persistence.Entity;

import com.community.api.enums.LogicalOperator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
@Entity
@Table(name = "qualification_subject_relationship")
@Getter
@Setter
public class QualificationSubjectRelationship implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "qualification_eligibility_id")
    private QualificationEligibility qualificationEligibility;
    
    @Enumerated(EnumType.STRING)
    private LogicalOperator logicalOperator = LogicalOperator.AND;
    
    private Boolean isMandatory = true;
}