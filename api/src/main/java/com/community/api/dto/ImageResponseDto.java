package com.community.api.dto;

import com.community.api.entity.RandomImageType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageResponseDto {
    private Long id;
    private String file_name;
    private String file_type;
    private String image_size;
    protected Boolean archived;
    private RandomImageType randomImageType;
    private String fileUrl;
}
