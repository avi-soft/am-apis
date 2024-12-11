package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
@Entity
@Table(name = "Qualification")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Qualification
{
    @Id
    private Integer qualification_id;

    @Column(name = "qualification_name", nullable = false)
    private String qualification_name;

    @Column(name = "qualification_description", nullable = false)
    private String qualification_description;

    @Column(name = "is_subjects_required")
    private Boolean is_subjects_required;

    @Column(name = "is_stream_required")
    private Boolean is_stream_required;
}