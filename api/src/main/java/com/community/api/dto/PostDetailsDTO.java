package com.community.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PostDetailsDTO {
    private Long postId;
    private String postName;
    private String postCode;
}
