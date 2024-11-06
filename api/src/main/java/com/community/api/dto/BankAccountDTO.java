package com.community.api.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountDTO {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Account number is required")
    @Size(min = 10, max = 20, message = "Account number must be between 10 and 20 characters")
    private String accountNumber;

    @NotBlank(message = "Re-entered account number is required")
    @Size(min = 10, max = 20, message = "Re-entered account number must be between 10 and 20 characters")
    private String reEnterAccountNumber;

    @NotBlank(message = "IFSC code is required")
    @Pattern(regexp = "^[A-Za-z]{4}[a-zA-Z0-9]{7}$", message = "Invalid IFSC code")
    private String ifscCode;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Branch name is required")
    private String branchName;

    @NotBlank(message = "Account type is required")
    private String accountType;

}
