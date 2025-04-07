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
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.ValidationException;
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
    the customer id
     * @param bankAccountDTO the bank account dto
     * @return the response entity
     */
    @PostMapping("/add")
    public ResponseEntity<?> addBankAccount(
            @RequestBody BankAccountDTO bankAccountDTO,
    @RequestHeader(value = "Authorization")String authHeader) {

        try {
            Long customerId=bankAccountDTO.getUserId();
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            if(Objects.equals(roleId, bankAccountDTO.getRole()) &&!Objects.equals(tokenUserId, bankAccountDTO.getUserId()))
                return ResponseService.generateErrorResponse("Forbidden",HttpStatus.FORBIDDEN);
            String errorMessage= bankAccountService.getErrorMessage(bankAccountDTO);
            if(!errorMessage.isEmpty())
                return ResponseService.generateErrorResponse(errorMessage,HttpStatus.BAD_REQUEST);
            if (customerId == null) {
                return ResponseService.generateErrorResponse("Customer Id not specified", HttpStatus.BAD_REQUEST);
            }
            if(bankAccountDTO.getRole()==5) {
                Customer customer = customerService.readCustomerById(customerId);
                if (customer == null) {
                    return ResponseService.generateErrorResponse("Customer not found for this Id", HttpStatus.NOT_FOUND);
                }
            }
            else
            {
                ServiceProviderEntity customer=entityManager.find(ServiceProviderEntity.class,customerId);
                if (customer == null) {
                    return ResponseService.generateErrorResponse("Customer not found for this Id", HttpStatus.NOT_FOUND);
                }
            }
          /*  if(bankAccountService.doesAccountExist(bankAccountDTO.getAccountNumber(),null,bankAccountDTO.getUserId()))
                return ResponseService.generateErrorResponse("Bank account exists",HttpStatus.BAD_REQUEST);*/
            String result = bankAccountService.addBankAccount(authHeader,bankAccountDTO);

            if (result.contains("Account numbers do not match.")) {
                return ResponseService.generateErrorResponse(result, HttpStatus.BAD_REQUEST);
            }
           /* if (result.contains("Account number already exists.")) {
                return ResponseService.generateErrorResponse(result, HttpStatus.BAD_REQUEST);
            }*/
            String[] resultParts = result.split("ID: ");
            System.out.println(result);
            Long generatedId = Long.parseLong(resultParts[1].trim());
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
        }catch (AlreadyExistsException e)
        {
            return ResponseService.generateErrorResponse("Bank Account Already exists",HttpStatus.BAD_REQUEST);
        }
        catch (ValidationException v)
        {
            return ResponseService.generateErrorResponse("Failed Validation"+v.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    /**
     * Gets bank accounts by customer id.
     *
     * @param customerId the customer id
     * @return the bank accounts by customer id
     */
    @GetMapping("/get/{customerId}")
    public ResponseEntity<?> getBankAccountsByCustomerId(@PathVariable Long customerId,@RequestHeader(value = "Authorization")String authHeader,@RequestParam Integer role) {
        try{
            String jwtToken = authHeader.substring(7);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Integer roleId=jwtTokenUtil.extractRoleId(jwtToken);
            Role roleToCheck=roleService.getRoleByRoleId(role);

            if(role==null)
                return ResponseService.generateErrorResponse("Invalid role",HttpStatus.NOT_FOUND);
            if(roleToCheck.getRole_name().equals(Constant.roleUser)) {
                Customer customer = entityManager.find(CustomCustomer.class,customerId);
                if (customer == null) {
                    return ResponseService.generateErrorResponse("User not found for this Id", HttpStatus.NOT_FOUND);
                }
                if(roleId==4)
                    return ResponseService.generateErrorResponse("Unauthorized",HttpStatus.UNAUTHORIZED);
            }
            if(roleToCheck.getRole_name().equals(Constant.roleServiceProvider)) {
                ServiceProviderEntity customer = entityManager.find(ServiceProviderEntity.class,customerId);
                if (customer == null) {
                    return ResponseService.generateErrorResponse("User not found for this Id", HttpStatus.NOT_FOUND);
                }
                if(roleId==5)
                    return ResponseService.generateErrorResponse("Unauthorized",HttpStatus.UNAUTHORIZED);
            }
            if(roleId.equals(role)&&!tokenUserId.equals(customerId))
                return ResponseService.generateErrorResponse("Unauthorized",HttpStatus.UNAUTHORIZED);
            if (customerId == null) {
                return ResponseService.generateErrorResponse("User Id not specified", HttpStatus.BAD_REQUEST);
            }
            List<BankAccountDTO> bankAccounts = bankAccountService.getBankAccountsByCustomerId(customerId,role);
            if (bankAccounts.isEmpty()) {
                return ResponseService.generateErrorResponse("No bank accounts found for this user", HttpStatus.OK);
            }
            return ResponseService.generateSuccessResponse("Bank accounts fetched successfully!", bankAccounts, HttpStatus.OK);

        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        }    }

    /**
     * Update bank account response entity.
     *
     * @param accountId      the account id
     * @param bankAccountDTO the bank account dto
     * @return the response entity
     */
    @PutMapping("/update/{accountId}")
    public ResponseEntity<?> updateBankAccount(
            @PathVariable Long accountId,
            @RequestBody BankAccountDTO bankAccountDTO,
            @RequestHeader(value = "Authorization") String authHeader) {
        try {
            // Validate path variable
            if (accountId == null) {
                return ResponseService.generateErrorResponse("Account ID is required", HttpStatus.BAD_REQUEST);
            }
            String errorMessage= bankAccountService.getErrorMessage(bankAccountDTO);
            if(!errorMessage.isEmpty())
                return ResponseService.generateErrorResponse(errorMessage,HttpStatus.BAD_REQUEST);
            // Validate DTO (handled by @Valid and DTO annotations)

            // Check if account exists
            Optional<BankDetails> existingAccount = bankAccountService.getBankAccountById(accountId);
            if (existingAccount.isEmpty()) {
                return ResponseService.generateErrorResponse("Bank account not found", HttpStatus.NOT_FOUND);
            }

            // Update account
            String result = bankAccountService.updateBankAccount(authHeader, accountId, bankAccountDTO);

            // Return updated account details
            Optional<BankDetails> updatedAccount = bankAccountService.getBankAccountById(accountId);
            BankAccountDTO updatedBankAccountDTO = bankAccountService.convertToDTO(updatedAccount.get());

            return ResponseService.generateSuccessResponse("Bank account updated successfully!",
                    updatedBankAccountDTO, HttpStatus.OK);

        } catch (AlreadyExistsException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.CONFLICT);
        } catch (NotAuthorizedException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Failed to update bank account",
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
    public ResponseEntity<?> deleteBankAccount(@PathVariable Long accountId,@RequestHeader(value = "Authorization")String authHeader) {
        try {
            if (accountId == null) {
                return ResponseService.generateErrorResponse("Account ID is required", HttpStatus.BAD_REQUEST);
            }


            Optional<BankDetails> existingAccount = bankAccountService.getBankAccountById(accountId);
            if (!existingAccount.isPresent()) {
                return ResponseService.generateErrorResponse("Bank account not found for this Id", HttpStatus.NOT_FOUND);
            }

            String result = bankAccountService.deleteBankAccount(authHeader,accountId);
            if (result.equals("Account deletion failed")) {
                return ResponseService.generateErrorResponse("Failed to delete bank account", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return ResponseService.generateSuccessResponse("Bank account deleted successfully!", null, HttpStatus.OK);

        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }
}
