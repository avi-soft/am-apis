package com.community.api.entity;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.utils.CustomDateDeserializer;
import com.community.api.utils.Document;
import com.community.api.utils.ServiceProviderDocument;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.util.Iterator;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "qualification_details")
@JsonIgnoreProperties(ignoreUnknown = true)
public class QualificationDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long qualification_detail_id;

    @NotNull(message = "Institution is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @NotNull(message = "Date of passing is required")
    @Column(name = "date_of_passing", nullable = false)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date date_of_passing;

    @Column(name = "examination_role_number",nullable = true)
    private Long examination_role_number;

    @Column(name = "course_duration_in_months",nullable = true)
    private Long course_duration_in_months;

    //    @NotNull(message = "Examination Registration Number is required")
    @Column(name = "examination_registration_number",nullable = true)
    private Long examination_registration_number;

    @NotNull(message = "board or university id is required")
    @Column(name = "board_university_id", nullable = false)
    private Long board_university_id;

    @Column(name = "stream_id")
    private Long stream_id;

    @NotNull(message = "total marks cannot be null")
    @Column(name = "total_marks", nullable = false)
    private String total_marks;

    @NotNull(message = "marks obtained cannot be null")
    @Column(name = "marks_obtained", nullable = false)
    private String marks_obtained;

    @Column(name = "is_grade",columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean is_grade;

    @Column(name = "grade_value")
    private String grade_value;

    @Column(name = "is_division", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean is_division;

    @Column(name = "division_value")
    private String division_value;

    @NotNull(message = "Qualification id is required")
    @Column(name = "qualification_id", nullable = false)
    private Integer qualification_id;

    @NotNull(message = "You have to select whether you are adding total marks in actual marks, CGPA or Grade")
    @Column(name = "total_marks_type", nullable = false)
    private String total_marks_type;

    @Min(value = 0, message = "Overall cumulative Percentage must not be less than 0")
    @Max(value = 100, message = "Overall cumulative Percentage must not be greater than 100")
    @Column(name = "cumulative_percentage_value" , nullable = false)
    private Double cumulative_percentage_value;

    @Size(max = 255, message = "Subject name should not exceed 255 characters")
    @Pattern(regexp = "^[^\\d]*$", message = "Subject name cannot contain numeric values")
    @Column(name = "subject_name")
    private String subject_name;

    @Column(name = "other_stream", columnDefinition = "text")
    private String other_stream;

    @Column(name = "other_board_university", columnDefinition = "text")
    private String other_board_university;

    @Column(name = "other_qualification", columnDefinition = "text")
    private String other_qualification;

    @Column(name = "other_institution", columnDefinition = "text")
    private String other_institution;

    @ElementCollection
    @CollectionTable(name = "highest_qualification_subject_names", joinColumns = @JoinColumn(name = "qualification_detail_id"))
    @Column(name = "subject_name")
    private List<String> highest_qualification_subject_names;

    @JsonBackReference("qualificationDetailsList-customer")
    @ManyToOne
    @JoinColumn(name = "custom_customer_id")
    private CustomCustomer custom_customer;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "qualification_subject_ids", joinColumns = @JoinColumn(name = "qualification_detail_id"))
    @Column(name = "subject_id")
    @Fetch(FetchMode.SUBSELECT)
    private List<Long> subject_ids;

    @OneToMany(mappedBy = "qualificationDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("subject-details")
    private List<SubjectDetail> subject_details = new ArrayList<>();

    @JsonBackReference("qualificationDetailsList-service-provider")
    @ManyToOne
    @JoinColumn(name = "service_provider_id")
    private ServiceProviderEntity service_provider;

    @OneToOne(mappedBy = "qualificationDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    private Document qualificationDocument;

    @OneToOne(mappedBy = "qualificationDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    private ServiceProviderDocument serviceProviderDocument;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)

    @JoinTable(
            name = "qualification_detail_other_item",
            joinColumns = @JoinColumn(name = "qualification_detail_id"),
            inverseJoinColumns = @JoinColumn(name = "other_item_id")
    )
    private List<OtherItem> otherItems = new ArrayList<>();

    @JsonSetter("institution_id")
    public void setInstitutionById(Long institutionId) {
        // Temporarily store institutionId (to be processed in the service layer)
        this.institution = new Institution();
        this.institution.setInstitution_id(institutionId);
    }
    @ElementCollection
    @CollectionTable(name = "other_subject_names", joinColumns = @JoinColumn(name = "qualification_detail_id"))
    @Column(name = "other_subject_name")
    List<String> otherSubjects=new ArrayList<>();

}
