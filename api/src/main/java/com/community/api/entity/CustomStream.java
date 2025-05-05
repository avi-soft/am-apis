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
import javax.persistence.JoinTable;
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
@Table(name = "custom_stream")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomStream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stream_id")
    @JsonProperty("stream_id")
    protected Long streamId;

    @NotNull
    @Column(name = "archived")
    @JsonProperty("archived")
    protected Character archived = 'N';

    @NotBlank
    @Column(name = "stream_name")
    @JsonProperty("stream_name")
    @Pattern(regexp = "^[a-zA-Z -]*$", message = "Stream name must contains only Alphabets and hyphen.")
    protected String streamName;

    @NotBlank
    @Column(name = "stream_description")
    @JsonProperty("stream_description")
    @Size(max = 255)
    @Pattern(regexp = "^[a-zA-Z0-9 ,.!?';:()&-]*$", message = "Stream description must contains only Alphabets and Digits.")
    protected String streamDescription;

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

    @ManyToMany(mappedBy = "streams")
    @JsonIgnore
    private List<Qualification> qualifications = new ArrayList<>();

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "stream_subject",
            joinColumns = @JoinColumn(name = "stream_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private List<CustomSubject> subjects = new ArrayList<>();

}
