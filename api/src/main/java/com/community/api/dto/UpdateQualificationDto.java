package com.community.api.dto;

import com.community.api.entity.SubjectDetail;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.*;
import java.util.ArrayList;
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

    private List<Long> subject_ids;

    private Long stream_id;

    @Min(value = 0, message = "Overall cumulative Percentage must not be less than 0")
    @Max(value = 100, message = "Overall cumulative Percentage must not be greater than 100")
    private Double cumulative_percentage_value;

    private String total_marks;

    private String marks_obtained;

    private Integer qualification_id;
    @Size(max = 255, message = "Subject name should not exceed 255 characters")
    @Pattern(regexp = "^[^\\d]*$", message = "Subject name cannot contain numeric values")
    private String subject_name;

    private Long examination_role_number;

    private Long examination_registration_number;

    private String total_marks_type;
    private String subject_marks_type;
    private List<SubjectDetail> subject_details = new ArrayList<>();

}