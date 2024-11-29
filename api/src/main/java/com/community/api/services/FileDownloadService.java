package com.community.api.services;
import com.community.api.entity.CustomMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class FileDownloadService {

    private final WebClient webClient;

    public FileDownloadService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://example.com").build();
    }
    public MultipartFile downloadFileToMultipart(String fileUrl) {
        // Use WebClient to download the file synchronously
        byte[] fileBytes = webClient.get()
                .uri(fileUrl)  // the file URL passed in
                .retrieve()  // initiate the request
                .bodyToMono(byte[].class)  // retrieve the file as a byte array
                .block();  // block to wait for the result synchronously

        // Create a MultipartFile from the downloaded bytes
        return new CustomMultipartFile("downloadedFile", fileBytes);
    }
}
