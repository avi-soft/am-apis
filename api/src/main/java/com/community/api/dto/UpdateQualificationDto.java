package com.community.api.dto;

import com.community.api.entity.CustomSubject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.*;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateQualificationDto
{
    private Long id;

    @Size(max = 255, message = "Institution name should not exceed 255 characters")
    @Pattern(regexp = "^[^\\d]*$", message = "Institution name cannot contain numeric values")
    private String institution_name;

    @Temporal(TemporalType.TIMESTAMP)
    private Date date_of_passing;

    private Long board_university_id;

    private Long examination_role_number;

    private Long examination_registration_number;

    private List<Long> subject_ids;

    private Long stream_id;

    @Pattern(regexp = "^(100|[1-9]?[0-9](\\\\.\\\\d*)?)$|^[A-Za-z]+$", message = "Grade or percentage value must be either a number  (up to 100) or a valid grade")
    @Size(max = 10, message = "Grade or percentage value should not exceed 10 characters")
    private String grade_or_percentage_value;

    @Min(value = 1, message = "Total marks must be greater than zero")
    private Double total_marks;

    @Min(value = 0, message = "Marks obtained cannot be negative")
    private Double marks_obtained;

    private Integer qualification_id;

    @AssertTrue(message = "Total marks cannot be less than marks obtained")
    private boolean isMarksTotalValid() {
        return total_marks >= marks_obtained;
    }

}