package com.community.api.entity;
import javax.persistence.Entity;

import com.community.api.enums.LogicalOperator;
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
@Table(name = "qualification_reserve_category_relationship")
@Getter
@Setter
public class QualificationReserveCategoryRelationship implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "qualification_eligibility_id")
    private QualificationEligibility qualificationEligibility;
    
    private Boolean isMandatory = false;
}