package com.community.api.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class CommunicationRequest {
    private Object customerIds; // Can be List<Long> or String (comma-separated)
    private Object modes;       // Can be List<Integer> or String (comma-separated)
    private String contentText;
    private String subject;
    private List<MultipartFile> files;
}