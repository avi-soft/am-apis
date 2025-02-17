package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "black_listed_tokens")
public class BlackListedTokens {
    @GeneratedValue
    @Id
    Long blackListId;
    private LocalDate blackListDate;
    @Column(columnDefinition = "TEXT DEFAULT NULL")
    private String blackListToken;
    private Long userId;
    private Integer roleId;
}
