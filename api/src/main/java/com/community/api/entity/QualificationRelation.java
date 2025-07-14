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
import java.io.Serializable;

@Entity
@Table(name = "qualification_relation")
@Getter
@Setter
public class QualificationRelation implements Serializable {
    
    @Id
    @Column(name = "qualification_relation_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long qualificationRelationId;
    
    @ManyToOne
    @JoinColumn(name = "logical_operator_id", nullable = false)
    private LogicalOperator logicalOperator;
    
    @ManyToOne
    @JoinColumn(name = "qualification_id")
    private Qualification qualification;

}