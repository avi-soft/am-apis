package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "custom_sector")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomSector implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "sector_id")
    protected Long sectorId;

    @Column(name = "sector_name")
    protected String sectorName;

    @Column(name = "sector_description")
    protected String sectorDescription;

    @Column(name = "archived",columnDefinition = "BOOLEAN DEFAULT FALSE")
    protected Boolean archived = false;
}
