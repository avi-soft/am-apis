package com.community.api.dto;

import com.community.api.entity.CustomSubject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.*;
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

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date of passing must be in the format YYYY-MM-DD.")
    private String date_of_passing;

    private Long board_university_id;

    private Long examination_role_number;

    private Long examination_registration_number;

    private List<Long> subject_ids;

    private Long stream_id;

    @Pattern(regexp = "^(100|[1-9]?[0-9](\\\\.\\\\d*)?)$|^[A-Za-z]+$", message = "Grade or percentage value must be either a number  (up to 100) or a valid grade")
    @Size(max = 10, message = "Grade or percentage value should not exceed 10 characters")
    private String grade_or_percentage_value;

    @Min(value = 1, message = "Total marks must be greater than zero")
    private Long total_marks;

    @Min(value = 0, message = "Marks obtained cannot be negative")
    private Long marks_obtained;

    private Integer qualification_id;

    @AssertTrue(message = "Total marks cannot be less than marks obtained")
    private boolean isMarksTotalValid() {
        return total_marks >= marks_obtained;
    }

//    @AssertTrue(message = "Year of passing must be less than or equal to the current year")
//    private boolean isYearOfPassingValid() {
//        return year_of_passing <= Year.now().getValue();
//    }
}