package com.community.api.services;

import com.community.api.dto.BankAccountDTO;
import com.community.api.entity.BankDetails;
import com.community.api.entity.CustomCustomer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;

@Service
public class BankAccountService {

    @Autowired
    EntityManager entityManager;

    public String addBankAccount(CustomCustomer customCustomer, BankAccountDTO bankAccountDTO) {
            try{
                if (!bankAccountDTO.getAccountNumber().equals(bankAccountDTO.getReEnterAccountNumber())) {
                    return "Account numbers do not match.";
                }
                BankDetails bankDetails = new BankDetails();
                bankDetails.setCustomerName(bankAccountDTO.getCustomerName());
                bankDetails.setAccountNumber(bankAccountDTO.getAccountNumber());
                bankDetails.setCustomer(customCustomer);
                bankDetails.setIfscCode(bankAccountDTO.getIfscCode());
                bankDetails.setBankName(bankAccountDTO.getBankName());
                bankDetails.setBranchName(bankAccountDTO.getBranchName());
                bankDetails.setAccountType(bankAccountDTO.getAccountType());

                entityManager.persist(bankDetails);

                return "Bank account added successfully!";
            }catch (Exception e){
                return "Error adding bank account: " + e.getMessage();
            }
    }
}
