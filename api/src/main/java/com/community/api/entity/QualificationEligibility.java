package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "qualification_eligibility")
@Getter
@Setter
public class QualificationEligibility
{
    @Id
    @Column(name = "qualification_eligibility_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long qualificationEligibilityId;

    @ManyToMany
    @JoinTable(
            name = "qualification_eligibility_qualifications",
            joinColumns = @JoinColumn(name = "qualification_eligibility_id"),
            inverseJoinColumns = @JoinColumn(name = "qualification_id")
    )
    private List<Qualification> qualifications;

    @ManyToMany
    @JoinTable(
            name = "qualification_eligibility_subjects",
            joinColumns = @JoinColumn(name = "qualification_eligibility_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private List<CustomSubject> customSubjects;

    @ManyToMany
    @JoinTable(
            name = "qualification_eligibility_streams",
            joinColumns = @JoinColumn(name = "qualification_eligibility_id"),
            inverseJoinColumns = @JoinColumn(name = "stream_id")
    )
    private List<CustomStream> customStreams;

    @ManyToOne
    @JoinColumn(name = "reserve_category_id")
    private CustomReserveCategory customReserveCategory;

    @Column(name = "percentage")
    private Long percentage;

    @Column(name = "cgpa")
    private Double cgpa;

    @Column(name = "is_percentage",columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isPercentage;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

}
