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
import javax.persistence.Transient;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Data
@NoArgsConstructor
@Table(name = "subject_details")
public class SubjectDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subject_detail_id;

    @NotNull(message = "Marks obtained for subject cannot be null")
    @Min(value = 0, message = "Marks obtained for subject cannot be negative")
    @Column(name = "subject_marks_obtained", nullable = false)
    private Double subject_marks_obtained;

    @NotNull(message = "Total marks for subject cannot be null")
    @Min(value = 1, message = "Total marks for subject must be greater than zero")
    @Column(name = "subject_total_marks", nullable = false)
    private Double subject_total_marks;

    @NotNull(message = "Equivalent percentage for subject cannot be null")
    @Column(name="subject_equivalent_percentage")
    private Double subject_equivalent_percentage;

//    @Transient
//    public Double getPercentage() {
//        return totalMarks != 0 ? (marksObtained * 100.0) / totalMarks : null;
//    }

    @AssertTrue(message = "Marks obtained for subject cannot exceed total marks")
    public boolean isMarksValid() {
        return subject_total_marks == null || subject_marks_obtained == null || subject_marks_obtained <= subject_total_marks;
    }

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "qualification_detail_id", nullable = false)
    private QualificationDetails qualificationDetails;

    @ManyToOne
    @JoinColumn(name = "custom_subject_id", nullable = false)
    private CustomSubject customSubject;
}
