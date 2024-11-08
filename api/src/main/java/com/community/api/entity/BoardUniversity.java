package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "board_university")
public class BoardUniversity
{
    @Id
    private Long id;

    @Column(name="board_university_name",nullable = false)
    private String board_university_name;

    @Column(name = "board_university_location", nullable = false)
    private String board_university_location;

    @Column(nullable = false)
    private String board_university_code;

    @Column(nullable = false)
    private String board_university_type;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", updatable = false)
    private String created_date;

    @Column(name = "modified_date")
    private String modified_date;

    @Column(name = "created_by")
    private String created_by;

    @Column(name = "modified_by")
    private String modified_by;
}
