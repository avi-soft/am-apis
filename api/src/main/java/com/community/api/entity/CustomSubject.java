package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "custom_subject")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id")
    @JsonProperty("subject_id")
    protected Long subjectId;

    @NotNull
    @Column(name = "archived")
    @JsonProperty("archived")
    protected Character archived = 'N';

    @NotBlank
    @Column(name = "subject_name")
    @JsonProperty("subject_name")
    @Pattern(regexp = "^[a-zA-Z -]*$", message = "Subject name must contains only Alphabets and hyphen.")
    protected String subjectName;

    @NotBlank
    @Column(name = "subject_description")
    @JsonProperty("subject_description")
    @Size(max = 255)
    @Pattern(regexp = "^[a-zA-Z0-9 ,.!?';:()&-]*$", message = "Subject description must contains only Alphabets and Digits.")
    protected String subjectDescription;

    @Column(name = "created_at")
    @JsonProperty("created_at")
    protected Date createdDate;

    @Column(name = "created_by")
    @JsonProperty("creator_user_id")
    protected Long creatorUserId;

    @ManyToOne
    @JoinColumn(name = "creator_role")
    @JsonProperty("creator_role")
    protected Role creatorRole;

    @JsonIgnore
    @ManyToMany(mappedBy = "subjects")
    private List<CustomStream> streams = new ArrayList<>();

}
