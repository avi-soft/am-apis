package com.community.api.endpoint.avisoft.controller;

import com.community.api.dto.CommunicationRequest;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.*;
import com.community.api.services.EmailService;
import com.community.api.services.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/service-provider-actions")
public class ServiceProviderActionController {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EmailService emailService; // You'll need to implement this

//    @Autowired
//    private WhatsappService whatsappService; // You'll need to implement this

    @PostMapping("/communicate/{serviceProviderId}")
    @Transactional
    public ResponseEntity<?> communicateWithCustomers(
            @RequestBody CommunicationRequest request,
            @PathVariable Long serviceProviderId) {
        try {
            // Validate service provider
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (serviceProvider == null) {
                return ResponseService.generateErrorResponse("Service Provider not found", HttpStatus.NOT_FOUND);
            }

            // Create communication content
            CommunicationContent content = new CommunicationContent();
            content.setServiceProvider(serviceProvider);
            content.setContentText(request.getContentText());
            content.setSubject(request.getSubject());
            
            // Handle file attachments if any
            if (request.getFiles() != null && !request.getFiles().isEmpty()) {
                List<ContentFile> contentFiles = processFiles(request.getFiles(), content);
                content.setContentFiles(contentFiles);
            }
            
            entityManager.persist(content);

            // Process each customer
            List<ActionLog> actionLogs = new ArrayList<>();
            for (Long customerId : request.getCustomerIds()) {
                CustomCustomer customer = entityManager.find(CustomCustomer.class, customerId);
                if (customer == null) continue;

                ActionLog actionLog = new ActionLog();
                actionLog.setServiceProvider(serviceProvider);
                actionLog.setCustomCustomer(customer);
                actionLog.setContent(content);
                actionLog.setCustomModes(request.getModes().stream()
                    .map(modeId -> entityManager.find(CustomMode.class, modeId))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));

                // Send communications based on selected modes
                for (CustomMode mode : actionLog.getCustomModes()) {
                    try {
                        switch (mode.getCustomModeName().toLowerCase()) {
                            case "email":
                                emailService.sendEmail(customer.getEmailAddress(), content);
                                break;
//                            case "whatsapp":
//                                whatsappService.sendMessage(customer.getPhone(), content);
//                                break;
                        }
                        actionLog.setDeliveryStatus("SUCCESS");
                    } catch (Exception e) {
                        actionLog.setDeliveryStatus("FAILED: " + e.getMessage());
                    }
                }
                
                entityManager.persist(actionLog);
                actionLogs.add(actionLog);
            }

            entityManager.flush();
            return ResponseService.generateSuccessResponse(
                "Communication sent successfully", 
                actionLogs.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList()), 
                HttpStatus.OK
            );

        } catch (Exception e) {
            return ResponseService.generateErrorResponse(
                "Failed to process communication: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private List<ContentFile> processFiles(List<MultipartFile> files, CommunicationContent content) {
        return files.stream().map(file -> {
            ContentFile contentFile = new ContentFile();
            contentFile.setFileName(file.getOriginalFilename());
            contentFile.setSize(file.getSize());
            contentFile.setCommunicationContent(content);
            // Set file path after saving file to your storage
            contentFile.setFilePath(saveFile(file));
            // Set appropriate file types
            contentFile.setFileTypes(determineFileTypes(file.getContentType()));
            return contentFile;
        }).collect(Collectors.toList());
    }

    private String saveFile(MultipartFile file) {
        // Implement file saving logic
        return "path/to/saved/file";
    }

    private List<FileType> determineFileTypes(String contentType) {
        // Implement file type determination logic
        return new ArrayList<>();
    }

    private Map<String, Object> convertToDTO(ActionLog actionLog) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("actionLogId", actionLog.getActionLogId());
        dto.put("customerId", actionLog.getCustomCustomer().getId());
        dto.put("deliveryStatus", actionLog.getDeliveryStatus());
        dto.put("modes", actionLog.getCustomModes().stream()
            .map(CustomMode::getCustomModeName)
            .collect(Collectors.toList()));
        return dto;
    }
}