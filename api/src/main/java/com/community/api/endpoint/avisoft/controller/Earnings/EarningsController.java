package com.community.api.endpoint.avisoft.controller.Earnings;

import com.community.api.annotation.Authorize;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.PaymentDetailsDTO;
import com.community.api.dto.TransactionDTO;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.Earnings;
import com.community.api.entity.Role;
import com.community.api.entity.Transaction;
import com.community.api.services.PaymentService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.SharedUtilityService;
import jakarta.jws.soap.SOAPBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/payments")
public class EarningsController {
    private final EntityManager entityManager;
    private final JwtUtil jwtTokenUtil;
    private final RoleService roleService;
    private final PaymentService paymentService;
    private final SharedUtilityService sharedUtilityService;

    @Autowired
    public EarningsController(EntityManager entityManager,
                              JwtUtil jwtTokenUtil,
                              RoleService roleService,
                              PaymentService paymentService,
                              SharedUtilityService sharedUtilityService) {
        this.entityManager = entityManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.roleService = roleService;
        this.paymentService = paymentService;
        this.sharedUtilityService=sharedUtilityService;
    }
    @Authorize(value = {Constant.roleAdmin,Constant.roleSuperAdmin,Constant.roleServiceProvider,Constant.roleAdminServiceProvider})
    @GetMapping("filter")
    public ResponseEntity<?> getFilteredEarnings(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestParam(required = false, defaultValue = "true") Boolean settled,
            @RequestParam(required = false) Long spId,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String from,
            @RequestParam(required = false,defaultValue ="0") Integer page,
            @RequestParam(required = false,defaultValue = "30")Integer limit) throws Exception {


        String jwtToken = authHeader.substring(7);
        Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);

        // Role check
        Role role=roleService.getRoleByRoleId(roleId);
        if(role.getRole_name().equals(Constant.roleUser))
            return ResponseService.generateErrorResponse("Forbidden",HttpStatus.FORBIDDEN);

        Long tokenUserId = jwtTokenUtil.extractId(jwtToken);


            if (from!=null&&!from.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                throw new IllegalArgumentException("Date must be in YYYY-MM-DD format");
            }
        if (to!=null&&!to.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new IllegalArgumentException("Date must be in YYYY-MM-DD format");
        }

        StringBuilder generalizedQuery = new StringBuilder("SELECT id FROM earnings WHERE 1=1");
        List<Object> params = new ArrayList<>();
        List<Earnings> result = new ArrayList<>();

        // Service Provider role-specific validation
        if (role.getRole_name().equals(Constant.roleServiceProvider)) {
            if (spId!=null) {
                return ResponseService.generateErrorResponse("Invalid action", HttpStatus.BAD_REQUEST);
            } else {
                spId=tokenUserId;
            }
        }
        else if(role.getRole_name().equals(Constant.roleAdmin)||role.getRole_name().equals(Constant.roleSuperAdmin))
        {
            if(spId==null)
                return ResponseService.generateErrorResponse("SP Id is required",HttpStatus.BAD_REQUEST);
        }

        List<BigInteger> earnings = new ArrayList<>();
        if (spId != null) {
            generalizedQuery.append(" AND provider_id = ?");
            params.add(spId);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        if (from != null && !from.isEmpty()) {
            Date fromDate;
            try {
                fromDate= sdf.parse(from);
            }catch (Exception e)
            {
                return ResponseService.generateErrorResponse("Invalid from date",HttpStatus.BAD_REQUEST);
            }
            generalizedQuery.append(" AND date >= ?");
            params.add(fromDate);
        }
        if (to != null && !to.isEmpty()) {
            Date toDate;
            try {
                toDate = sdf.parse(to);
            }
            catch (Exception e)
            {
                return ResponseService.generateErrorResponse("Invalid to date",HttpStatus.BAD_REQUEST);
            }
            generalizedQuery.append(" AND date <= ?");
            params.add(toDate);
        }
        // Optional: Handle 'settled' filter if your table has a 'settled' column
        if (settled != null) {
            generalizedQuery.append(" AND settled = ?");
            params.add(settled);
        }
        // Create the query and set parameters properly
        Query query = entityManager.createNativeQuery(generalizedQuery.toString());
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i)); // Native queries use 1-based indexing
        }

        earnings = query.getResultList();

        // Fetch full Earnings entities based on the IDs fetched
        for (BigInteger id : earnings) {
            Earnings earning = entityManager.find(Earnings.class, id.longValue());
            if (earning != null) {
                result.add(earning);
            }
        }
        int fromIndex = Math.min((page) * limit,result.size());
        int toIndex = Math.min(fromIndex + limit, result.size());
        Map<String,Object> resultMap=new HashMap<>();
        double[]balances=paymentService.balances(spId);
        resultMap.put("last_month_payable",balances[0]);
        resultMap.put("this_month_payable",balances[1]);
        resultMap.put("balance_amount",balances[0]+balances[1]);
        resultMap.put("payments", result.subList(fromIndex, toIndex));
        return new ResponseService().generateSuccessResponse("Payments", resultMap, HttpStatus.OK);
    }

    @Authorize(value = {Constant.roleAdmin,Constant.roleSuperAdmin,Constant.roleServiceProvider,Constant.roleAdminServiceProvider})
    @Transactional(readOnly = true)
    @GetMapping("get-all")
    public ResponseEntity<?> getFilteredEarnings(@RequestHeader(value = "Authorization")String authHeader,@RequestParam(required = false) Long spId,@RequestParam(required = false,defaultValue ="0") Integer page,
                                                 @RequestParam(required = false,defaultValue = "30")Integer limit) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId=jwtTokenUtil.extractId(jwtToken);
            Role role=roleService.getRoleByRoleId(roleId);
            if(role.getRole_name().equals(Constant.roleUser))
                return ResponseService.generateErrorResponse("Forbidden",HttpStatus.FORBIDDEN);
            if(role.getRole_name().equals(Constant.roleServiceProvider))
            {
                if(spId!=null)
                    return ResponseService.generateErrorResponse("Invalid request",HttpStatus.BAD_REQUEST);
                else
                    spId=tokenUserId;
            }
            if (spId == null) {
                List<PaymentDetailsDTO> response = new ArrayList<>();

                // Corrected query - returns Long values
                List<Long> providerIds = entityManager.createQuery(
                                "SELECT DISTINCT e.providerId FROM Earnings e", Long.class)
                        .getResultList();

                for (Long id : providerIds) {
                    ServiceProviderEntity provider = entityManager.find(ServiceProviderEntity.class, id);
                    if (provider != null) {
                        PaymentDetailsDTO dto = createPaymentDetailsDTO(provider, id);
                        response.add(dto);
                    }
                }
                int fromIndex = Math.min((page) * limit,response.size());
                int toIndex = Math.min(fromIndex + limit, response.size());
                return ResponseService.generateSuccessResponse("Result", response.subList(fromIndex, toIndex),HttpStatus.OK);
            } else {
                ServiceProviderEntity provider = entityManager.find(ServiceProviderEntity.class, spId);
                if (provider == null) {
                    return ResponseService.generateErrorResponse("User not found", HttpStatus.NOT_FOUND);
                }
                PaymentDetailsDTO dto = createPaymentDetailsDTO(provider, spId);
                return ResponseService.generateSuccessResponse("Result", dto, HttpStatus.OK);
            }
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error processing request: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private PaymentDetailsDTO createPaymentDetailsDTO(ServiceProviderEntity provider, Long providerId) {
        PaymentDetailsDTO dto = new PaymentDetailsDTO();
        dto.setUserId(provider.getService_provider_id());
        dto.setName(provider.getFirst_name() + " " + provider.getLast_name());

        try {
            String address = (provider.getBusiness_name() == null ? "N/A" : provider.getBusiness_name()) + "," +
                    (provider.getSpAddresses() == null ? "N/A" :
                            provider.getSpAddresses().get(0).getAddress_line() + "," +
                                    provider.getSpAddresses().get(0).getCity() + "," +
                                    provider.getSpAddresses().get(0).getState());
            dto.setAddress(address);
        } catch (Exception e) {
            dto.setAddress("N/A");
        }

        double[] balances = paymentService.balances(providerId);
        dto.setLastMonthPayable(balances[0]);
        dto.setThisMonthPayable(balances[1]); // Fixed the duplicate setter
        dto.setTotalBalance(balances[0] + balances[1]);
        return dto;
    }
    @Authorize(value = {Constant.roleAdmin,Constant.roleSuperAdmin,Constant.roleServiceProvider,Constant.roleAdminServiceProvider})
    @Transactional(readOnly = true)
    @GetMapping("get-transactions-history")
    public ResponseEntity<?> getPaymentHistory(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestParam(required = false) Long spId,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String from,@RequestParam(required = false,defaultValue ="0") Integer page,
            @RequestParam(required = false,defaultValue = "30")Integer limit
            ) {

        try {
            // Validate and extract JWT token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseService.generateErrorResponse("Invalid authorization header", HttpStatus.BAD_REQUEST);
            }
            String jwtToken = authHeader.substring(7);

            // Extract user info from token
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);

            // Validate date formats
            if (from != null && !from.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                return ResponseService.generateErrorResponse("From date must be in YYYY-MM-DD format", HttpStatus.BAD_REQUEST);
            }
            if (to != null && !to.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                return ResponseService.generateErrorResponse("To date must be in YYYY-MM-DD format", HttpStatus.BAD_REQUEST);
            }

            // Authorization check
            if (role.getRole_name().equals(Constant.roleUser)) {
                return ResponseService.generateErrorResponse("Forbidden", HttpStatus.FORBIDDEN);
            }

            // Validate service provider access
            if (role.getRole_name().equals(Constant.roleServiceProvider)) {
                if (spId != null && !spId.equals(tokenUserId)) {
                    return ResponseService.generateErrorResponse("Service providers can only view their own transactions",
                            HttpStatus.FORBIDDEN);
                }
                spId = tokenUserId; // Force SP to only see their own transactions
            }

            // Parse dates
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date fromDate = null;
            Date toDate = null;

            try {
                if (from != null) fromDate = sdf.parse(from);
                if (to != null) toDate = sdf.parse(to);
            } catch (ParseException e) {
                return ResponseService.generateErrorResponse("Invalid date format", HttpStatus.BAD_REQUEST);
            }

            // Build query using JPA Criteria API for type safety
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Transaction> cq = cb.createQuery(Transaction.class);
            Root<Transaction> transaction = cq.from(Transaction.class);

            List<Predicate> predicates = new ArrayList<>();

            // Add SP filter if specified
            if (spId != null) {
                predicates.add(cb.equal(transaction.get("userId"), spId));
            }

            // Add date range filters
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(transaction.get("date"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(transaction.get("date"), toDate));
            }

            cq.where(predicates.toArray(new Predicate[0]));
            cq.distinct(true);

            // Execute query
            List<Transaction> transactions = entityManager.createQuery(cq).getResultList();
            int fromIndex = Math.min((page) * limit,transactions.size());
            int toIndex = Math.min(fromIndex + limit, transactions.size());
            return ResponseService.generateSuccessResponse("Transactions retrieved successfully",
                    transactions.subList(fromIndex,toIndex), HttpStatus.OK);

        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error processing request",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Authorize(value = {Constant.roleAdmin,Constant.roleSuperAdmin})
    @Transactional
    @PutMapping("manage/{txnId}")
    public ResponseEntity<?> alterStatus(@PathVariable Long txnId,@RequestParam Boolean settle)
    {
        Earnings earnings=entityManager.find(Earnings.class,txnId);
        if(earnings==null)
            return ResponseService.generateErrorResponse("Invalid txn id provided",HttpStatus.BAD_REQUEST);
        if(earnings.isSettled()==settle) {
            if(settle)
                return ResponseService.generateErrorResponse("Transaction has already been settled", HttpStatus.BAD_REQUEST);
            else
                return ResponseService.generateErrorResponse("Transaction has already been unsettled", HttpStatus.BAD_REQUEST);
        }
        earnings.setSettled(settle);
        entityManager.merge(earnings);
        return ResponseService.generateSuccessResponse("Transaction status altered",earnings,HttpStatus.OK);
    }

    @Authorize(value = {Constant.roleAdmin,Constant.roleSuperAdmin})
    @Transactional
    @PostMapping("/settle-amount")
    public ResponseEntity<?> getFilteredEarnings(@RequestHeader(value = "Authorization")String authHeader, @RequestBody TransactionDTO transactionDTO) {
            List<Long> txnIds = new ArrayList<>();
            Earnings earningWithCarryOver=null;
            Query queryForCarryOver = entityManager.createQuery("Select e.id from Earnings e where e.carryOver < 0 and e.providerId =:id", Long.class);
            queryForCarryOver.setParameter("id",transactionDTO.getUserId());
            List<Long> ids = queryForCarryOver.getResultList();
            if (!ids.isEmpty()) {
                Long id = ids.get(0);
                earningWithCarryOver = entityManager.find(Earnings.class, id);
            } else {
                earningWithCarryOver = null; // No result found, avoid exception
            }
            if (transactionDTO.getUserId() == null)
                return ResponseService.generateErrorResponse("User id is required", HttpStatus.BAD_REQUEST);
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, transactionDTO.getUserId());
            if (serviceProvider == null)
                return ResponseService.generateErrorResponse("User not found", HttpStatus.NOT_FOUND);
            double[] balances = paymentService.balances(serviceProvider.getService_provider_id());
            if (transactionDTO.getAmountToSettle() == null || transactionDTO.getAmountToSettle() == 0)
                return ResponseService.generateErrorResponse("Amount to settle is needed", HttpStatus.BAD_REQUEST);
            else if (transactionDTO.getAmountToSettle() < 0)
                return ResponseService.generateErrorResponse("Amount to settle cannot be negative", HttpStatus.BAD_REQUEST);
          /*if(transactionDTO.getAmountToSettle()>(balances[0]+balances[1]))
              return ResponseService.generateErrorResponse("Amount to settle cannot be more than balance",HttpStatus.BAD_REQUEST);*/
          /*if(transactionDTO.getTxnIds()==null||transactionDTO.getTxnIds().isEmpty())
              return ResponseService.generateErrorResponse("Transaction ids are required",HttpStatus.BAD_REQUEST);*/
            double checkAmt = 0.0;
            if (transactionDTO.getTxnIds() != null && !transactionDTO.getTxnIds().isEmpty()) {
                for (Long txnId : transactionDTO.getTxnIds()) {
                    Earnings earnings = entityManager.find(Earnings.class, txnId);
                    if (earnings == null)
                        return ResponseService.generateErrorResponse("Invalid txn id provided", HttpStatus.BAD_REQUEST);
                    if (!earnings.getProviderId().equals(transactionDTO.getUserId()))
                        return ResponseService.generateErrorResponse("Payment with id : " + txnId + " does not belong to the selected user", HttpStatus.BAD_REQUEST);
                    if (earnings.isSettled())
                        return ResponseService.generateErrorResponse("Payment with Id : " + txnId + " is already settled", HttpStatus.BAD_REQUEST);
                    checkAmt += earnings.getPending();
                    txnIds = transactionDTO.getTxnIds();
                }
            }

            else {
                Query query = entityManager.createQuery("Select id from Earnings WHERE providerId = :userId AND settled = false", Long.class);
                query.setParameter("userId", transactionDTO.getUserId());
                txnIds = query.getResultList();
                if(txnIds.isEmpty())
                {
                    query = entityManager.createQuery("Select MAX(id) from Earnings WHERE providerId = :userId AND settled = true", Long.class);
                    query.setParameter("userId", transactionDTO.getUserId());
                    txnIds.add((Long) query.getResultList().get(0));
                }
            }
          /*if(transactionDTO.getAmountToSettle()>checkAmt)
              return ResponseService.generateErrorResponse("Amount cannot be settled for selected transactions",HttpStatus.BAD_REQUEST);*/
                Double amt = 0.0;
                Collections.sort(txnIds);

                if(transactionDTO.getAmountToSettle()>balances[0]+balances[1])
                {
                    for (Long txnId : txnIds) {
                    Earnings earnings = entityManager.find(Earnings.class, txnId);
                        amt += earnings.getPending();
                        earnings.setPaid(earnings.getPaid()+earnings.getPending());
                        earnings.setPending(0.0);
                        earnings.setPaymentDone(true);
                        earnings.setSettled(true);
                        entityManager.merge(earnings);
                    }
                    Earnings earnings = entityManager.find(Earnings.class, txnIds.get(txnIds.size()-1));
                    if(earningWithCarryOver!=null) {
                        earningWithCarryOver.setCarryOver(0.0);
                        if(amt - transactionDTO.getAmountToSettle()+earningWithCarryOver.getCarryOver()<0)
                            earnings.setCarryOver(amt - transactionDTO.getAmountToSettle()+earningWithCarryOver.getCarryOver());
                        entityManager.merge(earningWithCarryOver);
                        entityManager.merge(earnings);
                    }
                    else
                    {
                        earnings.setCarryOver(amt - transactionDTO.getAmountToSettle());
                        entityManager.merge(earnings);
                    }
                    Transaction transaction = new Transaction();
                    transaction.setCurrentMonthPayable(balances[1]);
                    transaction.setLastMonthPayable(balances[0]);
                    transaction.setSettledAmount(transactionDTO.getAmountToSettle());
                    transaction.setBalance(balances[0] + balances[1] - transactionDTO.getAmountToSettle());
                    transaction.setSettlementRemarks(transactionDTO.getSettlementRemarks());
                    transaction.setRole(serviceProvider.getRole());
                    transaction.setUserId(serviceProvider.getService_provider_id());
                    transaction.setDate(new Date());
                    entityManager.persist(transaction);
                    if(earningWithCarryOver!=null&&balances[0]+balances[1]>=(-earningWithCarryOver.getCarryOver())) {
                        earningWithCarryOver.setCarryOver(0.0);
                        entityManager.merge(earningWithCarryOver);
                    }
                    return ResponseService.generateSuccessResponse("Transaction Done", transaction, HttpStatus.OK);
                }
                else
                {
                for (Long txnId : txnIds) {
                    Earnings earnings = entityManager.find(Earnings.class, txnId);
                    if (earnings.getPending() + amt < transactionDTO.getAmountToSettle()) {
                        amt = amt + earnings.getPending();
                        earnings.setPaid(earnings.getPending());
                        earnings.setPending(0.0);
                        earnings.setPaymentDone(true);
                        earnings.setSettled(true);
                        entityManager.merge(earnings);
                    } else if (amt == transactionDTO.getAmountToSettle()) {
                        Transaction transaction = new Transaction();
                        transaction.setCurrentMonthPayable(balances[1]);
                        transaction.setLastMonthPayable(balances[0]);
                        transaction.setSettledAmount(amt);
                        transaction.setBalance(balances[0] + balances[1] - amt);
                        transaction.setSettlementRemarks(transactionDTO.getSettlementRemarks());
                        transaction.setRole(serviceProvider.getRole());
                        transaction.setUserId(serviceProvider.getService_provider_id());
                        transaction.setDate(new Date());
                        entityManager.persist(transaction);
                        if(earningWithCarryOver!=null&&earningWithCarryOver!=null&&balances[0]+balances[1]>=(-earningWithCarryOver.getCarryOver())) {
                            earningWithCarryOver.setCarryOver(0.0);
                            entityManager.merge(earningWithCarryOver);
                        }
                        return ResponseService.generateSuccessResponse("Transaction Done", transaction, HttpStatus.OK);
                    } else if (earnings.getPending() + amt > transactionDTO.getAmountToSettle()) {
                        if(earningWithCarryOver!=null) {
                            earnings.setPending(earnings.getPending() - (transactionDTO.getAmountToSettle() - amt) + earningWithCarryOver.getCarryOver());
                        }
                        else
                            earnings.setPending(earnings.getPending() - (transactionDTO.getAmountToSettle() - amt));
                        earnings.setPaid(transactionDTO.getAmountToSettle() - amt);
                        amt = transactionDTO.getAmountToSettle();
                        earnings.setPaymentDone(false);
                        earnings.setSettled(false);
                        Transaction transaction = new Transaction();
                        transaction.setCurrentMonthPayable(balances[1]);
                        transaction.setLastMonthPayable(balances[0]);
                        transaction.setSettledAmount(amt);
                        transaction.setBalance(balances[0] + balances[1] - amt);
                        transaction.setSettlementRemarks(transactionDTO.getSettlementRemarks());
                        transaction.setRole(serviceProvider.getRole());
                        transaction.setUserId(serviceProvider.getService_provider_id());
                        transaction.setDate(new Date());
                        entityManager.persist(transaction);
                        entityManager.merge(earnings);
                        if(earningWithCarryOver!=null&&earningWithCarryOver!=null&&balances[0]+balances[1]>=(-earningWithCarryOver.getCarryOver())) {
                            earningWithCarryOver.setCarryOver(0.0);
                            entityManager.merge(earningWithCarryOver);
                        }
                        return ResponseService.generateSuccessResponse("Transaction Done", transaction, HttpStatus.OK);
                    }
                }
                }
            return null;
        }
    }


