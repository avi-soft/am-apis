package com.community.api.endpoint.avisoft.controller.Bank;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.BankAccountDTO;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.BankDetails;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.Role;
import com.community.api.services.BankAccountService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.mchange.util.AlreadyExistsException;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.social.NotAuthorizedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The type Bank account controller.
 */
@RestController
@RequestMapping("/bank-account")
public class BankAccountController {

    /**
     * The Bank account service.
     */
    @Autowired
    BankAccountService bankAccountService;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    /**
     * The Entity manager.
     */
    @Autowired
    EntityManager entityManager;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private RoleService roleService;

    /**
     * Add bank account response entity.
     * the customer id
     *
     * @param bankAccountDTO the bank account dto
     * @return the response entity
     */
    @Transactional
    @PostMapping("/add")
    public ResponseEntity<?> addBankAccount(
            @Valid @RequestBody BankAccountDTO bankAccountDTO,
            BindingResult bindingResult,
            @RequestHeader(value = "Authorization") String authHeader) {

        try {
            Map<String,String> errorMessages= new HashMap<>();
            Long customerId = bankAccountDTO.getUserId();
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            if (Objects.equals(roleId, bankAccountDTO.getRole()) && !Objects.equals(tokenUserId, bankAccountDTO.getUserId()))
                return ResponseService.generateSuccessResponse("Forbidden","forbidden", HttpStatus.FORBIDDEN);

            if (customerId == null) {
                return ResponseService.generateSuccessResponse("Customer Id not specified","customerId", HttpStatus.BAD_REQUEST);
            }

            if (bindingResult.hasErrors()) {
                bindingResult.getFieldErrors().forEach(error ->
                        errorMessages.put(error.getField(), error.getDefaultMessage())
                );
            }

            Map<String,String> bankErrors = bankAccountService.validateBankAccountDTO(bankAccountDTO);
            errorMessages.putAll(bankErrors);

            if (bankAccountService.isAccountDuplicate(bankAccountDTO.getAccountNumber())) {
                errorMessages.put("accountNumber", "Account number already exists.");
            }
            if (bankAccountService.isUpiDuplicate(bankAccountDTO.getUpiId())) {
                errorMessages.put("upiId", "UPI ID already exists.");
            }
            if (bankAccountService.doesAccountExist(bankAccountDTO.getAccountNumber(), null, jwtTokenUtil.extractId(authHeader.substring(7)))) {
                errorMessages.put("accountNumber", "Account already exists.");
            }

            if (bankAccountDTO.getIfscCode() == null || bankAccountDTO.getIfscCode().isBlank()) {
                errorMessages.put("ifscCode", "IFSC Code is required.");
            }

// If any error is present, return them
            if (!errorMessages.isEmpty()) {
                String message = String.join(", ", errorMessages.values());
                return ResponseService.generateSuccessResponse(message, errorMessages.keySet(), HttpStatus.BAD_REQUEST);
            }
            // Create and persist the bank details
            BankDetails bankDetails = new BankDetails();
            bankDetails.setAccountNumber(bankAccountDTO.getAccountNumber());
            bankDetails.setUserId(bankAccountDTO.getUserId());
            bankDetails.setRole(bankAccountDTO.getRole());
            bankDetails.setIfscCode(bankAccountDTO.getIfscCode());
            bankDetails.setBankName(bankAccountDTO.getBankName());
            bankDetails.setBranchName(bankAccountDTO.getBranchName());
            bankDetails.setAccountType(bankAccountDTO.getAccountType());
            bankDetails.setUpiId(bankAccountDTO.getUpiId());
            bankDetails.setAccountHolder(bankAccountDTO.getAccountHolder());
            entityManager.persist(bankDetails);
            Long generatedId = bankDetails.getId();

           /* if (result.contains("Account number already exists.")) {
                return ResponseService.generateErrorResponse(result, HttpStatus.BAD_REQUEST);
            }*/
            BankAccountDTO responseDTO = new BankAccountDTO(
                    generatedId,
                    bankAccountDTO.getUserId(),
                    bankAccountDTO.getRole(),
                    bankAccountDTO.getAccountNumber(),
                    bankAccountDTO.getAccountHolder(),
                    bankAccountDTO.getIfscCode(),
                    bankAccountDTO.getBankName(),
                    bankAccountDTO.getBranchName(),
                    bankAccountDTO.getAccountType(),
                    bankAccountDTO.getUpiId()
            );


            return ResponseService.generateSuccessResponse("Bank account added successfully!", responseDTO, HttpStatus.OK);
        }  catch (ValidationException v) {
            return ResponseService.generateSuccessResponse("Failed Validation" + v.getMessage(),"validationException", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateSuccessResponse(e.getMessage(),"generalException", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Gets bank accounts by customer id.
     *
     * @param customerId the customer id
     * @return the bank accounts by customer id
     */
    @GetMapping("/get/{customerId}")
    public ResponseEntity<?> getBankAccountsByCustomerId(@PathVariable Long customerId, @RequestHeader(value = "Authorization") String authHeader, @RequestParam Integer role) {
        try {
            String jwtToken = authHeader.substring(7);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Role roleToCheck = roleService.getRoleByRoleId(role);

            if (role == null)
                return ResponseService.generateErrorResponse("Invalid role", HttpStatus.NOT_FOUND);
            if (roleToCheck.getRole_name().equals(Constant.roleUser)) {
                Customer customer = entityManager.find(CustomCustomer.class, customerId);
                if (customer == null) {
                    return ResponseService.generateErrorResponse("User not found for this Id", HttpStatus.NOT_FOUND);
                }
                if (roleId == 4)
                    return ResponseService.generateErrorResponse("Unauthorized", HttpStatus.FORBIDDEN);
            }
            if (roleToCheck.getRole_name().equals(Constant.roleServiceProvider)) {
                ServiceProviderEntity customer = entityManager.find(ServiceProviderEntity.class, customerId);
                if (customer == null) {
                    return ResponseService.generateErrorResponse("User not found for this Id", HttpStatus.NOT_FOUND);
                }
                if (roleId == 5)
                    return ResponseService.generateErrorResponse("Unauthorized", HttpStatus.FORBIDDEN);
            }
            if (roleId.equals(role) && !tokenUserId.equals(customerId))
                return ResponseService.generateErrorResponse("Unauthorized", HttpStatus.FORBIDDEN);
            if (customerId == null) {
                return ResponseService.generateErrorResponse("User Id not specified", HttpStatus.BAD_REQUEST);
            }
            List<BankAccountDTO> bankAccounts = bankAccountService.getBankAccountsByCustomerId(customerId, role);
            if (bankAccounts.isEmpty()) {
                return ResponseService.generateErrorResponse("No bank accounts found for this user", HttpStatus.OK);
            }
            return ResponseService.generateSuccessResponse("Bank accounts fetched successfully!", bankAccounts, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    /**
     * Update bank account response entity.
     *
     * @param accountId      the account id
     * @param bankAccountDTO the bank account dto
     * @return the response entity
     */
    @Transactional
    @PutMapping("/update/{accountId}")
    public ResponseEntity<?> updateBankAccount(
            @PathVariable Long accountId,
            @Valid @RequestBody BankAccountDTO bankAccountDTO, BindingResult bindingResult,
            @RequestHeader(value = "Authorization") String authHeader)
           {
        try {
            // Validate path variable
            Map<String,String> errorMessages = new HashMap<>();
            if (accountId == null) {
                return ResponseService.generateSuccessResponse("Account ID is required","accountId", HttpStatus.BAD_REQUEST);
            }
            // Check if account exists
            Optional<BankDetails> existingAccount = bankAccountService.getBankAccountById(accountId);
            if (existingAccount.isEmpty()) {
                return ResponseService.generateSuccessResponse("Bank account not found", "accountId",HttpStatus.NOT_FOUND);
            }
            if (bindingResult.hasErrors()) {
                bindingResult.getFieldErrors().forEach(error ->
                        errorMessages.put(error.getField(), error.getDefaultMessage())
                );
            }
            Map<String,String> bankErrors = bankAccountService.validateBankAccountDTO(bankAccountDTO);
            errorMessages.putAll(bankErrors);
            BankDetails existingAccount1 = entityManager.find(BankDetails.class, accountId);
            // Update account
            if (bankAccountService.hasDuplicateAccountForUpdate(existingAccount1, bankAccountDTO, accountId) ){
                errorMessages.put("accountNumber", "Account number already exists.");
            }
            if (bankAccountService.hasDuplicateUpiForUpdate(existingAccount1, bankAccountDTO, accountId)) {
                errorMessages.put("upiId", "UPI ID already exists.");
            }
//            if (bankAccountService.doesAccountExist(bankAccountDTO.getAccountNumber(), null, jwtTokenUtil.extractId(authHeader.substring(7)))) {
//                errorMessages.put("accountNumber", "Account already exists.");
//            }
            if (!errorMessages.isEmpty()) {
                String message = String.join(", ", errorMessages.values());
                return ResponseService.generateSuccessResponse(message, errorMessages.keySet(), HttpStatus.BAD_REQUEST);
            }
            // Update fields from DTO to entity
            existingAccount1.setRole(bankAccountDTO.getRole());
            existingAccount1.setAccountNumber(bankAccountDTO.getAccountNumber());
            existingAccount1.setAccountHolder(bankAccountDTO.getAccountHolder());
            existingAccount1.setIfscCode(bankAccountDTO.getIfscCode());
            existingAccount1.setBankName(bankAccountDTO.getBankName());
            existingAccount1.setBranchName(bankAccountDTO.getBranchName());
            existingAccount1.setAccountType(bankAccountDTO.getAccountType());
            existingAccount1.setUpiId(bankAccountDTO.getUpiId());

            entityManager.merge(existingAccount1);

            // Return updated account details
            Optional<BankDetails> updatedAccount = bankAccountService.getBankAccountById(accountId);
            BankAccountDTO updatedBankAccountDTO = bankAccountService.convertToDTO(updatedAccount.get());

            return ResponseService.generateSuccessResponse("Bank account updated successfully!",
                    updatedBankAccountDTO, HttpStatus.OK);

        } catch (NotAuthorizedException e) {
            return ResponseService.generateSuccessResponse(e.getMessage(), "notAuthorizedException",HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateSuccessResponse("Failed to update bank account","generalException",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Delete bank account response entity.
     *
     * @param accountId the account id
     * @return the response entity
     */
    @DeleteMapping("/delete/{accountId}")
    public ResponseEntity<?> deleteBankAccount(@PathVariable Long accountId, @RequestHeader(value = "Authorization") String authHeader) {
        try {
            if (accountId == null) {
                return ResponseService.generateErrorResponse("Account ID is required", HttpStatus.BAD_REQUEST);
            }


            Optional<BankDetails> existingAccount = bankAccountService.getBankAccountById(accountId);
            if (!existingAccount.isPresent()) {
                return ResponseService.generateErrorResponse("Bank account not found for this Id", HttpStatus.NOT_FOUND);
            }

            String result = bankAccountService.deleteBankAccount(authHeader, accountId);
            if (result.equals("Account deletion failed")) {
                return ResponseService.generateErrorResponse("Failed to delete bank account", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return ResponseService.generateSuccessResponse("Bank account deleted successfully!", null, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }
}
