package com.community.api.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class CommunicationRequest {
    private List<Long> customerIds;
    private List<Integer> modes;
    private String contentText;
    private String subject;
    private List<MultipartFile> files;
}