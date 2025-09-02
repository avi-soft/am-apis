package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "board_university_type")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BoardUniversityType {
    @Id
    @Column(name = "board_university_id")
    Long boardUniversityId;
    @Column(name= "board_university_type",nullable = false)
    private String board_university_type;
}
