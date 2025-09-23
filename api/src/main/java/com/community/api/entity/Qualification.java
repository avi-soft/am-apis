package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "is_subjects_required", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean is_subjects_required;

    @Column(name = "is_stream_required", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean is_stream_required;

    @Column(name = "overlapping")
    private Long overlap;

    @Column(name = "sort_order", nullable = false)
    private Long sort_order;

    @Column(name = "archived",columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean archived;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "qualification_stream",
            joinColumns = @JoinColumn(name = "qualification_id"),
            inverseJoinColumns = @JoinColumn(name = "stream_id")
    )
    private List<CustomStream> streams = new ArrayList<>();

}