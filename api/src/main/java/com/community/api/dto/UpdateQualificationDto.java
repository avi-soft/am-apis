package com.community.api.dto;

import com.community.api.entity.SubjectDetail;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateQualificationDto
{
    private Long id;

    private Long institution_id;

    private String date_of_passing;

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

    private String other_stream;

    private String other_board_university;

    private String other_qualification;
    private Boolean is_grade;

    private String grade_value;
    private Long course_duration_in_months;
    private List<String> highest_qualification_subject_names;
    List<String> otherSubjects=new ArrayList<>();

    private Boolean is_division;

    private String division_value;

    private String total_marks_type;
    private String subject_marks_type;
    private List<SubjectDetail> subject_details = new ArrayList<>();

}