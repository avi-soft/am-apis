package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "qualification_eligibility")
@Getter
@Setter
public class QualificationEligibility implements Serializable
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

    @Column(name = "is_appearing")
    @JsonProperty("isAppearing")
    private Boolean isAppearing;

    @JsonIgnore
    @Column(name = "additional_comments")
    @JsonProperty("additionalComments")
    private String additionalComments;

    private String qualificationIdRunningField;
    private String subjectIdRunningField;
    private String streamIdRunningField;
    private String reserveCatIdRunningField;

    @ElementCollection
    @CollectionTable(name = "highest_qualification_subject_names_in_product", joinColumns = @JoinColumn(name = "qualification_detail_id"))
    @Column(name = "subject_name")
    private List<String> highestQualificationSubjectNames;

}
