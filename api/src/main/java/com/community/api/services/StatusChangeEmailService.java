package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.CommunicationRequest;
import com.community.api.endpoint.avisoft.controller.ServiceProviderActionController;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
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
    private JwtUtil jwtUtil;
    @Autowired
    private ServiceProviderActionController serviceProviderActionController;

    public void sendStatusChangeEmails(List<ServiceProviderEntity> serviceProviders, String action, String authHeader) {
        for (ServiceProviderEntity serviceProvider : serviceProviders) {
            try {
                String jwtToken = authHeader.substring(7);
                Integer role = jwtUtil.extractRoleId(jwtToken);
                CommunicationRequest communicationRequest = new CommunicationRequest();
                communicationRequest.setSubject("Account Status Update");
                communicationRequest.setUserIds(String.valueOf(serviceProvider.getService_provider_id()));
                communicationRequest.setModes("1"); // Email mode

                // Determine recipient type based on role
                String recipientType = (serviceProvider.getRole()== 2)
                        ? "Admin"
                        : "Service Provider";

                String statusMessage = getStatusChangeMessage(action);
                String newStatus = getStatusDisplayName(serviceProvider);

                communicationRequest.setContentText(
                        "Dear " + (serviceProvider.getFirst_name() != null ? serviceProvider.getFirst_name() : recipientType) +
                                (serviceProvider.getLast_name() != null ? " " + serviceProvider.getLast_name() : "") + ",\n\n" +
                                "We would like to inform you that your " + recipientType.toLowerCase() + " account status has been updated.\n\n" +
                                "Your current status: " + newStatus + "\n\n" +
                                statusMessage + "\n\n" +
                                "If you have any questions, please contact our support team.\n\n" +
                                "Best regards,\n" +
                                "System Administrator"
                );

                // Send communication asynchronously
                communicateWithCustomersAsync(communicationRequest, serviceProvider.getRole(), authHeader);

            } catch (Exception e) {
                // Improved error logging
                System.out.println(e);
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
        switch (action.toLowerCase()) {
            case "approve":
                return "Congratulations! Your service provider account has been approved. You can now access all service provider features and start offering your services.";
            case "reject":
                return "Unfortunately, your service provider application has been rejected.You can contact support for more information regarding this or resubmit your profile after required correction.";
            case "suspend":
                return "Your service provider account has been suspended. Please contact support for assistance in resolving this matter.";
            case "activate":
                return "Good news! Your service provider account has been reactivated. You can now resume using all service provider features.";
            case "testcomplete":
                return "You have successfully submitted the skill test";
            default:
                return "Your service provider status has been updated.";
        }
    }
    @Async
    public void communicateWithCustomersAsync(CommunicationRequest communicationRequest, Integer roleToBeId, String authHeader) {
        try {
            ResponseEntity<?> response=serviceProviderActionController.communicateWithCustomersDummy(communicationRequest, roleToBeId, authHeader,true);
        } catch (Exception e) {
        }
    }

    public void sendCustomerStatusChangeEmails(List<CustomCustomer> customers, String action, String authHeader) {
        for (CustomCustomer customer : customers) {
            try {
                String jwtToken = authHeader.substring(7);
                Integer role = jwtUtil.extractRoleId(jwtToken);
                CommunicationRequest communicationRequest = new CommunicationRequest();
                communicationRequest.setSubject("Account Status Update");
                communicationRequest.setUserIds(String.valueOf(customer.getId()));
                communicationRequest.setModes("1"); // Email mode

                // Determine recipient type based on role
                String recipientType = "Customer";

                String statusMessage = getCustomerStatusChangeMessage(action);
                String newStatus = getCustomerStatusDisplayName(customer);

                communicationRequest.setContentText(
                        "Dear " + (customer.getFirstName() != null ? customer.getFirstName() : recipientType) +
                                (customer.getLastName() != null ? " " + customer.getLastName() : "") + ",\n\n" +
                                "We would like to inform you that your " + recipientType.toLowerCase() + " account status has been updated.\n\n" +
                                "Your current status: " + newStatus + "\n\n" +
                                statusMessage + "\n\n" +
                                "If you have any questions, please contact our support team.\n\n" +
                                "Best regards,\n" +
                                "System Administrator"
                );

                communicateWithCustomersAsync(communicationRequest, Constant.CUSTOMER_ROLE_ID, authHeader);

            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public String getCustomerStatusDisplayName(CustomCustomer customer) {
        if (customer.getArchived() != null && customer.getArchived()) {
            return "Suspended";
        } else {
            return "Active";
        }
    }

    private String getCustomerStatusChangeMessage(String action) {
        switch (action.toLowerCase()) {
            case "suspend":
                return "Your customer account has been suspended. Please contact support for assistance in resolving this matter.";
            case "activate":
                return "Good news! Your customer account has been reactivated. You can now resume using all platform features.";
            default:
                return "Your customer account status has been updated.";
        }
    }
}
