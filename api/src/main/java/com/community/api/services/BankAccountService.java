package com.community.api.services;

import com.community.api.component.JwtUtil;
import com.community.api.dto.BankAccountDTO;
import com.community.api.entity.BankDetails;
import com.community.api.entity.CustomCustomer;
import com.mchange.util.AlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.social.NotAuthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * The type Bank account service.
 */
@Service
public class BankAccountService {

    /**
     * The Entity manager.
     */
    @Autowired
    EntityManager entityManager;

    @Autowired
    JwtUtil jwtTokenUtil;

    @Autowired
    private javax.validation.Validator validator;

    public Map<String, String> validateBankAccountDTO(BankAccountDTO bankAccountDTO) {
        Set<ConstraintViolation<BankAccountDTO>> violations = validator.validate(bankAccountDTO);
        Map<String, String> errors = new HashMap<>();

        for (ConstraintViolation<BankAccountDTO> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();  // Gets the field name
            String message = violation.getMessage();                   // Gets the validation message
            errors.put(fieldName, message);
        }
        return errors;
    }
    public String getErrorMessage(BankAccountDTO bankAccountDTO) {
        Map<String, String> response = validateBankAccountDTO(bankAccountDTO);
        if (!response.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder();

            for (Map.Entry<String, String> entry : response.entrySet()) {
                String message = entry.getValue();
                errorMessage.append(message).append(", ");
            }
            // Remove the trailing comma and space
            if (errorMessage.length() > 0) {
                errorMessage.setLength(errorMessage.length() - 2);
            }
            // Return or print the final error message
            return errorMessage.toString();
        }
        else return "";
    }
    @Transactional
    public String addBankAccount(String authHeader,BankAccountDTO bankAccountDTO) throws AlreadyExistsException {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            if (doesAccountExist(bankAccountDTO.getAccountNumber(), null,tokenUserId)) {
                throw  new AlreadyExistsException( "Account already exists.");
            }

       /*     if (!bankAccountDTO.getAccountNumber().equals(bankAccountDTO.getReEnterAccountNumber())) {
                return "Account numbers do not match.";
            }*/

            BankDetails bankDetails = new BankDetails();
            bankDetails.setAccountNumber(bankAccountDTO.getAccountNumber());
            bankDetails.setUserId(bankAccountDTO.getUserId());
            bankDetails.setRole(roleId);
            bankDetails.setIfscCode(bankAccountDTO.getIfscCode());
            bankDetails.setBankName(bankAccountDTO.getBankName());
            bankDetails.setBranchName(bankAccountDTO.getBranchName());
            bankDetails.setAccountType(bankAccountDTO.getAccountType());
            bankDetails.setUpiId(bankAccountDTO.getUpiId());
            bankDetails.setAccountHolder(bankAccountDTO.getAccountHolder());
            entityManager.persist(bankDetails);
            Long generatedId = bankDetails.getId();

            return "Bank account added successfully! ID: " + generatedId;
        } catch (Exception e) {
            return "Error adding bank account: " + e.getMessage();
        }
    }


    /**
     * Delete bank account string.
     *
     * @param accountId the account id
     * @return the string
     */
    @Transactional
    public String deleteBankAccount(String authHeader,Long accountId) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            BankDetails bankDetails = entityManager.find(BankDetails.class, accountId);
            if (bankDetails != null) {
                entityManager.remove(bankDetails); // Delete the bank account
                return "Bank account deleted successfully!";
            } else if(!tokenUserId.equals(bankDetails.getUserId()))
            {
                return "Unauthorized";
            }else {
                return "Bank account not found!";
            }
        } catch (Exception e) {
            return "Error deleting bank account: " + e.getMessage();
        }
    }

   /* *//**
     * Gets bank account by id.
     *
     * @param accountId the account id
     * @return the bank account by id
     */
    @Transactional

    public Optional<BankDetails> getBankAccountById(Long accountId) {
        try {
            if (accountId == null) {
                return Optional.empty();
            }

            BankDetails bankDetails = entityManager.find(BankDetails.class, accountId);
            if (bankDetails == null) {
                return Optional.empty();
            }
            if (bankDetails != null) {

                return Optional.of(bankDetails);
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Update bank account string.
     *
     * @param accountId      the account id
     * @param bankAccountDTO the bank account dto
     * @return the string
     */
    @Transactional
    public String updateBankAccount(String authHeader, Long accountId, BankAccountDTO bankAccountDTO)
            throws AlreadyExistsException, NotAuthorizedException {
        try {
            // Validate authorization
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);

            // Verify account exists and belongs to user
            BankDetails existingAccount = entityManager.find(BankDetails.class, accountId);
            if (existingAccount == null) {
                return "Account update failed. Account not found.";
            }
            if (Objects.equals(existingAccount.getRole(), roleId) &&!existingAccount.getUserId().equals(tokenUserId)) {
                throw new NotAuthorizedException("NA", "Forbidden");
            }

            // Validate account number uniqueness (excluding current account)
            if (doesAccountExist(bankAccountDTO.getAccountNumber(), accountId, tokenUserId)) {
                throw new AlreadyExistsException("Account number already exists.");
            }

            // Update fields from DTO to entity
            existingAccount.setRole(bankAccountDTO.getRole());
            existingAccount.setAccountNumber(bankAccountDTO.getAccountNumber());
            existingAccount.setAccountHolder(bankAccountDTO.getAccountHolder());
            existingAccount.setIfscCode(bankAccountDTO.getIfscCode());
            existingAccount.setBankName(bankAccountDTO.getBankName());
            existingAccount.setBranchName(bankAccountDTO.getBranchName());
            existingAccount.setAccountType(bankAccountDTO.getAccountType());
            existingAccount.setUpiId(bankAccountDTO.getUpiId());

            entityManager.merge(existingAccount);

            return "Bank account updated successfully!";
        } catch (AlreadyExistsException | NotAuthorizedException e) {
            throw e; // Re-throw specific exceptions
        } catch (Exception e) {
            e.printStackTrace();
            return "Account update failed: " + e.getMessage();
        }
    }

    public BankAccountDTO convertToDTO(BankDetails bankDetails) {
        BankAccountDTO dto = new BankAccountDTO();
        dto.setUpiId(bankDetails.getUpiId());
        dto.setAccountHolder(bankDetails.getAccountHolder());
        dto.setUserId(bankDetails.getId());
        dto.setAccountNumber(bankDetails.getAccountNumber());
        dto.setIfscCode(bankDetails.getIfscCode());
        dto.setBankName(bankDetails.getBankName());
        dto.setBranchName(bankDetails.getBranchName());
        dto.setAccountType(bankDetails.getAccountType());
        return dto;
    }

    /**
     * Check if the account number already exists, excluding the current account being updated.
     *
     * @param accountNumber the account number to check
     * @param accountId the ID of the account being updated (null for new accounts)
     * @return true if the account number exists, false otherwise
     */
    public boolean doesAccountExist(String accountNumber, Long accountId,Long id) {
        try {
            List<BankDetails> duplicateAccounts = entityManager.createQuery(
                            "SELECT b FROM BankDetails b WHERE b.accountNumber = :accountNumber and b.userId =:id", BankDetails.class)
                    .setParameter("accountNumber", accountNumber)
                    .setParameter("id", id)
                    .getResultList();

            if (accountId != null) {
                duplicateAccounts.removeIf(bankDetails -> bankDetails.getId().equals(accountId));
            }

            return !duplicateAccounts.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Gets bank accounts by customer id.
     *
     * @param customerId the customer id
     * @return the bank accounts by customer id
     */
    @Transactional

    public List<BankAccountDTO> getBankAccountsByCustomerId(Long customerId,Integer roleId) {
        try {
            List<BankAccountDTO> bankAccountDTOList=new ArrayList<>();
            List<BigInteger> bankIds = entityManager.createNativeQuery(
                            "SELECT b.id FROM bank_details b WHERE b.user_id = :customerId AND b.role = :roleId")
                    .setParameter("customerId", customerId)
                    .setParameter("roleId", roleId)
                    .getResultList();
            for (BigInteger bankId : bankIds) {
                BankDetails bankDetails = entityManager.find(BankDetails.class, bankId.longValue());
               bankAccountDTOList = new ArrayList<>();
                BankAccountDTO bankAccountDTO = new BankAccountDTO();
                bankAccountDTO.setRole(bankDetails.getRole());
                bankAccountDTO.setUserId(bankDetails.getUserId());
                bankAccountDTO.setAccountId(bankDetails.getId());
                bankAccountDTO.setAccountHolder(bankDetails.getAccountHolder());
                bankAccountDTO.setUserId(bankDetails.getUserId());
                bankAccountDTO.setUpiId(bankDetails.getUpiId());
                bankAccountDTO.setAccountNumber(bankDetails.getAccountNumber());
                bankAccountDTO.setIfscCode(bankDetails.getIfscCode());
                bankAccountDTO.setBankName(bankDetails.getBankName());
                bankAccountDTO.setBranchName(bankDetails.getBranchName());
                bankAccountDTO.setAccountType(bankDetails.getAccountType());
                bankAccountDTOList.add(bankAccountDTO);

            }
            return bankAccountDTOList;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
