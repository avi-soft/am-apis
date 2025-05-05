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
import javax.validation.constraints.NotNull;

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

    @Column(name = "subject_grade")
    private String subject_grade;

    @Column(name="subject_equivalent_percentage")
    private Double subject_equivalent_percentage;

    @NotNull(message = "You have to select whether you are adding subject marks in actual marks, CGPA or Grade")
    @Column(name= "subject_marks_type",nullable = false)
    private String subject_marks_type;

    @JsonBackReference("subject-details")
    @ManyToOne
    @JoinColumn(name = "qualification_detail_id")
    private QualificationDetails qualificationDetails;

    @ManyToOne
    @JoinColumn(name = "custom_subject_id", nullable = false)
    private CustomSubject customSubject;
}
