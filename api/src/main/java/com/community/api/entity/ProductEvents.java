package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "product_update_events")
public class ProductEvents {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    Long eventId;

    LocalDateTime lastUpdate;

    String summaryOfUpdate;

    Long productId;
}
