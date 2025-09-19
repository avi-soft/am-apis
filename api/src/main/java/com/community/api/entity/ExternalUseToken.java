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
@Table(name = "external_use_tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExternalUseToken {
    @Id
    Long spId;

    @Column(columnDefinition = "TEXT")
    String token;
}
