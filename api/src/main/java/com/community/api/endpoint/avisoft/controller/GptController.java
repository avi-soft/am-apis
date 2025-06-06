package com.community.api.endpoint.avisoft.controller;

import com.community.api.dto.ProductUpdateRequest;
import com.community.api.services.gptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.Map;

@Controller
public class GptController {
    @Autowired
    private gptService chatGPTService;

    @PostMapping("/generate-update-summary")
    public ResponseEntity<?> generateUpdateSummary(@RequestBody ProductUpdateRequest request) {
        try {
            String summary = chatGPTService.getUpdateSummary(request.getOld(), request.getLatest());
            return ResponseEntity.ok(Map.of("updateSummary", summary));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate update summary"));
        }
    }
}
