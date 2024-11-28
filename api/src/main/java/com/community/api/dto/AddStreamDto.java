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
public class AddStreamDto {

    @NotNull
    @NotBlank
    @JsonProperty("stream_name")
    @Pattern(regexp = "^[a-zA-Z -]*$", message = "Stream name must contains only Alphabets and hyphen.")
    private String streamName;

    @NotBlank
    @JsonProperty("stream_description")
    @Size(max = 255)
    @Pattern(regexp = "^[a-zA-Z0-9 ,.!?';:()&-]*$", message = "Stream description must contains only Alphabets and Digits.")
    private String streamDescription;

}