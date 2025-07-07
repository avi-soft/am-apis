package com.community.api.entity;

import com.community.api.enums.LogicalOperator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "qualification_eligibility_group")
@Getter
@Setter
public class QualificationEligibilityGroup implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;
    
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "post_id")
    private Post post;
    
    private String groupName;
    
    @Enumerated(EnumType.STRING)
    private LogicalOperator logicalOperator = LogicalOperator.AND;
    
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<QualificationEligibility> qualificationEligibilities = new ArrayList<>();
    
    private LocalDateTime createdAt;
}