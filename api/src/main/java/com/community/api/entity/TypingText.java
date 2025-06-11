package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.CodePointLength;

import javax.persistence.*;

@Entity
@Table(name="typing_text")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypingText
{
    @Id
    private Long id;

    @Column(name = "text",nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name="archived",columnDefinition = "BOOLEAN DEFAULT FALSE")
    @JsonProperty("archived")
    protected Boolean archived=true;
}
