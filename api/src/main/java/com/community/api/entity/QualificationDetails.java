package com.community.api.entity;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.util.ArrayList;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "qualification_details")
@JsonIgnoreProperties(ignoreUnknown = true)
public class QualificationDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Institution name is required")
    @Size(max = 255, message = "Institution name should not exceed 255 characters")
    @Pattern(regexp = "^[^\\d]*$", message = "Institution name cannot contain numeric values")
    @Column(name = "institution_name", nullable = false)
    private String institution_name;

    @NotNull(message = "Date of passing is required")
    @Column(name = "date_of_passing", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date_of_passing;

    @NotNull(message = "board or university id is required")
    @Column(name = "board_university_id", nullable = false)
    private Long board_university_id;

//    @NotNull(message = "Examination Role Number is required")
    @Column(name = "examination_role_number",nullable = true)
    private Long examination_role_number;

//    @NotNull(message = "Examination Registration Number is required")
    @Column(name = "examination_registration_number",nullable = true)
    private Long examination_registration_number;

    @Column(name = "stream_id")
    private Long stream_id;

    @Column(name= "subject_marks_type")
    private String subject_marks_type;

    @NotBlank(message = "Grade or percentage value is required")
    @Pattern(regexp = "^(100|[1-9]?[0-9](\\\\.\\\\d*)?)$|^[A-Za-z]+$", message = "Grade or percentage value must be either a number  (up to 100) or a valid grade")
    @Size(max = 10, message = "Grade or percentage value should not exceed 10 characters")
    @Column(name = "grade_or_percentage_value", nullable = false)
    private String grade_or_percentage_value;

    @Min(value = 1, message = "Total marks must be greater than zero")
    @Column(name = "total_marks", nullable = false)
    private Double total_marks;

    @Min(value = 0, message = "Marks obtained cannot be negative")
    @Column(name = "marks_obtained", nullable = false)
    private Double marks_obtained;

    @NotNull(message = "Qualification id is required")
    @Column(name = "qualification_id", nullable = false)
    private Integer qualification_id;

    @NotNull(message = "You have to select whether you are adding total marks in actual marks, CGPA or Grade")
    @Column(name = "total_marks_type", nullable = false)
    private String total_marks_type;

    @Min(value = 0, message = "Percentage must not be less than 0")
    @Max(value = 100, message = "Percentage must not be greater than 100")
    @Column(name = "total_percentage" , nullable = false)
    private Double total_percentage;


    @AssertTrue(message = "Total marks cannot be less than marks obtained")
    private Boolean isMarksTotalValid() {
        return total_marks >= marks_obtained;
    }

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
    @JsonManagedReference
    private List<SubjectDetail> subjectDetails = new ArrayList<>();

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "service_provider_id")
    private ServiceProviderEntity service_provider;
}
