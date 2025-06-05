package com.community.api.services;

import com.community.api.dto.CommunicationRequest;
import com.community.api.endpoint.avisoft.controller.ServiceProviderActionController;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.ServiceProviderTestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.List;
@Service
public class StatusChangeEmailService
{

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ServiceProviderActionController serviceProviderActionController;

    public void sendStatusChangeEmails(List<ServiceProviderEntity> serviceProviders, String action, String authHeader) {
        for (ServiceProviderEntity serviceProvider : serviceProviders) {
            try {
                CommunicationRequest communicationRequest = new CommunicationRequest();
                communicationRequest.setSubject("Service Provider Status Update");
                communicationRequest.setUserIds(String.valueOf(serviceProvider.getService_provider_id()));
                communicationRequest.setModes("1"); // Email mode
                System.out.println(action);
                String statusMessage = getStatusChangeMessage(action);
                System.out.println(statusMessage);
                String newStatus = getStatusDisplayName(serviceProvider);

                communicationRequest.setContentText(
                        "Dear " + (serviceProvider.getFirst_name() != null ? serviceProvider.getFirst_name() : "Service Provider") +
                                (serviceProvider.getLast_name() != null ? " " + serviceProvider.getLast_name() : "") + ",\n\n" +
                                "We would like to inform you that your service provider status has been updated.\n\n" +
                                "Your current status: " + newStatus + "\n\n" +
                                statusMessage + "\n\n" +
                                "If you have any questions, please contact our support team.\n\n" +
                                "Best regards,\n" +
                                "System Administrator"
                );

                // Assuming you have this method to send communications asynchronously
                communicateWithCustomersAsync(communicationRequest, serviceProvider.getRole(), authHeader);

            } catch (Exception e) {
                // Log the error but don't fail the entire operation
                System.err.println("Failed to send email notification to service provider " +
                        serviceProvider.getService_provider_id() + ": " + e.getMessage());
            }
        }
    }

    // Helper method to get status display name
    public String getStatusDisplayName(ServiceProviderEntity serviceProvider) {
        if (serviceProvider.getServiceProviderStatus() != null) {
            return serviceProvider.getServiceProviderStatus().getTest_status_name();
        }

        if (serviceProvider.getApproved() != null && serviceProvider.getApproved()) {
            return "Approved";
        } else if (serviceProvider.getRejected() != null && serviceProvider.getRejected()) {
            return "Rejected";
        } else if (serviceProvider.getIsArchived() != null && serviceProvider.getIsArchived()) {
            return "Suspended";
        } else if (serviceProvider.getServiceProviderStatus().getTest_status_id().equals(2L))
        {
            return "Test Completed";
        }
        else {
            ServiceProviderTestStatus status= entityManager.find(ServiceProviderTestStatus.class, serviceProvider.getLastStatusId());
            return status.getTest_status_name();
        }
    }

    // Helper method to get appropriate status change message
    private String getStatusChangeMessage(String action) {
        System.out.println("inside status message");
        switch (action.toLowerCase()) {
            case "approve":
                return "Congratulations! Your service provider account has been approved. You can now access all service provider features and start offering your services.";
            case "reject":
                return "Unfortunately, your service provider application has been rejected.You can contact support for more information regarding this or resubmit your application.";
            case "suspend":
                return "Your service provider account has been suspended. Please contact support for assistance in resolving this matter.";
            case "activate":
                return "Good news! Your service provider account has been reactivated. You can now resume using all service provider features.";
            case "testcomplete":
                System.out.println("hey");
                return "You have successfully submitted the skill test";
            default:
                return "Your service provider status has been updated.";
        }
    }
    @Async
    public void communicateWithCustomersAsync(CommunicationRequest communicationRequest, Integer roleToBeId, String authHeader) {
        try {
            ResponseEntity<?> response=serviceProviderActionController.communicateWithCustomersDummy(communicationRequest, roleToBeId, authHeader,true);
            System.out.println(response);
        } catch (Exception e) {
        }
    }
}
