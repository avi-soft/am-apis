package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddSubjectDto {

    @NotNull
    @NotBlank
    @JsonProperty("subject_name")
    @Pattern(regexp = "^[a-zA-Z -]*$", message = "Subject name must contains only Alphabets and hyphen.")
    private String subjectName;

    @NotBlank
    @JsonProperty("subject_description")
    @Size(max = 255)
    @Pattern(regexp = "^[a-zA-Z0-9 ,.!?';:()&-]*$", message = "Subject description must contains only Alphabets and Digits.")
    private String subjectDescription;

}