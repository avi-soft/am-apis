package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "custom_districts")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Districts {
    @Id
    private Integer district_id;
    private String district_name;
    private String state_code;
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean archived;
}
