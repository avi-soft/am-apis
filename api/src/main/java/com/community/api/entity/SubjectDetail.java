package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Data
@NoArgsConstructor
@Table(name = "subject_details")
public class SubjectDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subject_detail_id;

    @Column(name = "subject_marks_obtained")
    private String subject_marks_obtained;

    @Column(name = "subject_total_marks")
    private String subject_total_marks;

    @Column(name="subject_equivalent_percentage")
    private Double subject_equivalent_percentage;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "qualification_detail_id", nullable = false)
    private QualificationDetails qualificationDetails;

    @ManyToOne
    @JoinColumn(name = "custom_subject_id", nullable = false)
    private CustomSubject customSubject;
}
