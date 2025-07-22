package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BankAccountDTO {

    private Long accountId;

    @NotNull(message = "Customer ID is required")
    private Long userId;
/*
    @NotBlank(message = "Customer name is required")
    @Pattern(regexp = "^[A-Za-z ]{2,50}$", message = "name must contain only alphabets and spaces (2-50 characters)")
    private String name;*/

    @NotNull(message = "Customer role is required")
    private Integer role;

    @NotBlank(message = "Account number is required")
    @Size(min = 10, max = 20, message = "Account number must be between 10 and 20 characters")
    @Pattern(
            regexp = "^(?!0+$)[0-9]{10,20}$",
            message = "Account number must contain only digits, be 10 to 20 digits long, and cannot be all zeros"
    )
    private String accountNumber;



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
