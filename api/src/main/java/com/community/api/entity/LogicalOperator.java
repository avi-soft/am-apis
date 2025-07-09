package com.community.api.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "logical_operator")
@Getter
@Setter
public class LogicalOperator implements Serializable {
    
    @Id
    @Column(name = "logical_operator_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logicalOperatorId;
    
    @Column(name = "operator_name", nullable = false, unique = true)
    private String operatorName;
}