package com.community.api.entity;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date of passing must be in the format YYYY-MM-DD.")
    @Column(name = "date_of_passing", nullable = false)
    private String date_of_passing;

    @NotNull(message = "board or university id is required")
    @Column(name = "board_university_id", nullable = false)
    private Long board_university_id;

//    @NotNull(message = "Examination Role Number is required")
    @Column(name = "examination_role_number",nullable = true)
    private Long examination_role_number;

//    @NotNull(message = "Examination Registration Number is required")
    @Column(name = "examination_registration_number",nullable = true)
    private Long examination_registration_number;


    @NotNull(message = "stream id is required")
    @Column(name = "stream_id", nullable = false)
    private Long stream_id;

    @NotBlank(message = "Grade or percentage value is required")
    @Pattern(regexp = "^(100|[1-9]?[0-9](\\\\.\\\\d*)?)$|^[A-Za-z]+$", message = "Grade or percentage value must be either a number  (up to 100) or a valid grade")
    @Size(max = 10, message = "Grade or percentage value should not exceed 10 characters")
    @Column(name = "grade_or_percentage_value", nullable = false)
    private String grade_or_percentage_value;

    @Min(value = 1, message = "Total marks must be greater than zero")
    @Column(name = "total_marks", nullable = false)
    private Long total_marks;

    @Min(value = 0, message = "Marks obtained cannot be negative")
    @Column(name = "marks_obtained", nullable = false)
    private Long marks_obtained;

    @NotNull(message = "Qualification id is required")
    @Column(name = "qualification_id", nullable = false)
    private Integer qualification_id;

    @AssertTrue(message = "Total marks cannot be less than marks obtained")
    private Boolean isMarksTotalValid() {
        return total_marks >= marks_obtained;
    }

    //    @AssertTrue(message = "Year of passing must be less than or equal to the current year")
//    private boolean isYearOfPassingValid() {
//        return year_of_passing <= Year.now().getValue();
//    }
    @JsonBackReference("qualificationDetailsList-customer")
    @ManyToOne
    @JoinColumn(name = "custom_customer_id")
    private CustomCustomer custom_customer;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "qualification_subject_ids", joinColumns = @JoinColumn(name = "qualification_detail_id"))
    @Column(name = "subject_id")
    @Fetch(FetchMode.SUBSELECT)
    private List<Long> subject_ids;


    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "service_provider_id")
    private ServiceProviderEntity service_provider;
}
