package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.*;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateQualificationDto
{
    private Long id;

    @Size(max = 255, message = "Institution name should not exceed 255 characters")
    @Pattern(regexp = "^[^\\d]*$", message = "Institution name cannot contain numeric values")
    private String institution_name;

    //    @Min(value = 1900, message = "Year of passing should not be before 1900")
//    @Max(value = 9999, message = "Year of passing should be a valid 4-digit year")
    private Long year_of_passing;

    private Long board_university_id;

    private Long examination_role_number;

    private Long examination_registration_number;

    @Size(max = 255, message = "Subject name should not exceed 255 characters")
    @Pattern(regexp = "^[^\\d]*$", message = "Subject name cannot contain numeric values")
    private String subject_name;

    @Size(max = 255, message = "Stream should not exceed 255 characters")
    @Pattern(regexp = "^[^\\d]*$", message = "Stream cannot contain numeric values")
    private String stream;

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