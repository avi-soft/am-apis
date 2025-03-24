package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountDTO {

    @NotNull(message = "Customer ID is required")
    private Long cid;

    @NotBlank(message = "Customer name is required")
    @Pattern(regexp = "^[A-Za-z ]{2,50}$", message = "Customer name must contain only alphabets and spaces (2-50 characters)")
    private String customerName;

    @NotNull(message = "Customer role is required")
    private Integer customerRole;

    @NotBlank(message = "Account number is required")
    @Size(min = 10, max = 20, message = "Account number must be between 10 and 20 characters")
    @Pattern(regexp = "^[0-9]{10,20}$", message = "Account number must contain only digits")
    private String accountNumber;

    @NotBlank(message = "Re-entered account number is required")
    @Size(min = 10, max = 20, message = "Re-entered account number must be between 10 and 20 characters")
    @Pattern(regexp = "^[0-9]{10,20}$", message = "Re-entered account number must contain only digits")
    private String reEnterAccountNumber;

    @NotBlank(message = "Account holder name is required")
    @Pattern(regexp = "^[A-Za-z ]{2,50}$", message = "Account holder name must contain only alphabets and spaces (2-50 characters)")
    private String accountHolder;

    @NotBlank(message = "IFSC code is required")
    @Pattern(regexp = "^[A-Za-z]{4}[a-zA-Z0-9]{7}$", message = "Invalid IFSC code format")
    private String ifscCode;

    @NotBlank(message = "Bank name is required")
    @Pattern(regexp = "^[A-Za-z0-9 .,&-]{2,100}$", message = "Bank name can contain alphabets, numbers, spaces, and special characters like . , & - (2-100 characters)")
    private String bankName;

    @NotBlank(message = "Branch name is required")
    @Pattern(regexp = "^[A-Za-z0-9 .,&-]{2,100}$", message = "Branch name can contain alphabets, numbers, spaces, and special characters like . , & - (2-100 characters)")
    private String branchName;

    @NotBlank(message = "Account type is required")
    @Pattern(regexp = "^(Savings|Current|Salary|Fixed Deposit|Recurring Deposit)$",
            message = "Account type must be one of: Savings, Current, Salary, Fixed Deposit, Recurring Deposit")
    private String accountType;

    @NotBlank(message = "UPI ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9\\.\\-_]{2,256}@[a-zA-Z]{2,64}$",
            message = "Invalid UPI ID format")
    private String upiId;
}
