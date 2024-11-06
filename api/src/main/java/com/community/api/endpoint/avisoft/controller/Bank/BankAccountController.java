package com.community.api.endpoint.avisoft.controller.Bank;
import com.community.api.dto.BankAccountDTO;
import com.community.api.entity.CustomCustomer;
import com.community.api.services.BankAccountService;
import com.community.api.services.ResponseService;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.validation.Valid;

@RestController
@RequestMapping("/bank-account")
public class BankAccountController {

    @Autowired
    BankAccountService bankAccountService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    private CustomerService customerService;

    @PostMapping("/add/{customerId}")
    public ResponseEntity<?> addBankAccount(
            @PathVariable Long customerId,
            @RequestBody BankAccountDTO bankAccountDTO) {

        Long id = Long.valueOf(customerId);

        if(id==null)
            return ResponseService.generateErrorResponse("Customer Id not specified", HttpStatus.BAD_REQUEST);

        Customer customer = customerService.readCustomerById(customerId);
        if (customer == null) {
            return ResponseService.generateErrorResponse("Customer not found for this Id", HttpStatus.NOT_FOUND);
        }
        CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
        String result = bankAccountService.addBankAccount(customCustomer,bankAccountDTO);

        if (result.equals("Account numbers do not match.")) {
            return ResponseService.generateErrorResponse(result, HttpStatus.BAD_REQUEST);

        }
        return ResponseService.generateSuccessResponse("Bank account added successfully!", bankAccountDTO,HttpStatus.OK);


    }
}
