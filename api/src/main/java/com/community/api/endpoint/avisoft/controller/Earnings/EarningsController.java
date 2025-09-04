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

import static antlr.build.ANTLR.root;

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
    @Transactional
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
                return ResponseService.generateErrorResponse("Forbidden Access", HttpStatus.FORBIDDEN);
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
        if(Boolean.FALSE.equals(settled))
        {
            generalizedQuery.append(" Order by date ASC");
        }
        else
        {
            generalizedQuery.append(" Order by date DESC");
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
        resultMap.put("total_number_of_items",result.size());
        resultMap.put("total_number_of_pages",result.size()/limit);
        resultMap.put("current_page",page);
        resultMap.put("last_month_payable", balances[0]);
        resultMap.put("this_month_payable", balances[1]);
        resultMap.put("surplus", balances[2]); // Optional: include it explicitly
        resultMap.put("balance_amount", balances[0] + balances[1] + balances[2]);
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
                    return ResponseService.generateErrorResponse("Forbidden Access", HttpStatus.FORBIDDEN);
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
                Map<String,Object> resultMap=new HashMap<>();
                resultMap.put("total_number_of_items",response.size());
                resultMap.put("total_number_of_pages",response.size()/limit);
                resultMap.put("current_page",page);
                resultMap.put("result",response.subList(fromIndex, toIndex));
                return ResponseService.generateSuccessResponse("Result",resultMap,HttpStatus.OK);
            } else {
                ServiceProviderEntity provider = entityManager.find(ServiceProviderEntity.class, spId);
                if (provider == null) {
                    return ResponseService.generateErrorResponse("User not found", HttpStatus.NOT_FOUND);
                }
                PaymentDetailsDTO dto = createPaymentDetailsDTO(provider, spId);
                Map<String,Object> resultMap=new HashMap<>();
                resultMap.put("total_number_of_items",1);
                resultMap.put("total_number_of_pages",1);
                resultMap.put("current_page",0);
                resultMap.put("result",dto);
                return ResponseService.generateSuccessResponse("Result",resultMap,HttpStatus.OK);
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
            cq.select(transaction).orderBy(cb.desc(transaction.get("txnId")));
            // Execute query
            List<Transaction> transactions = entityManager.createQuery(cq).getResultList();

            int fromIndex = Math.min((page) * limit,transactions.size());
            int toIndex = Math.min(fromIndex + limit, transactions.size());
            Map<String,Object> resultMap=new HashMap<>();
            resultMap.put("total_number_of_items",transactions.size());
            resultMap.put("total_number_of_pages",transactions.size()/limit);
            resultMap.put("current_page",page);
            resultMap.put("result",transactions.subList(fromIndex, toIndex));
            return ResponseService.generateSuccessResponse("Result",resultMap,HttpStatus.OK);

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
    //working if no surplus is included
   /* @Authorize(value = {Constant.roleAdmin, Constant.roleSuperAdmin})
    @Transactional
    @PostMapping("/settle-amount")
    public ResponseEntity<?> settleEarnings(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestBody TransactionDTO transactionDTO) {

        if (transactionDTO.getUserId() == null)
            return ResponseService.generateErrorResponse("User id is required", HttpStatus.BAD_REQUEST);

        if (transactionDTO.getAmountToSettle() == null || transactionDTO.getAmountToSettle() <= 0)
            return ResponseService.generateErrorResponse("Amount to settle must be greater than 0", HttpStatus.BAD_REQUEST);

        ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, transactionDTO.getUserId());
        if (serviceProvider == null)
            return ResponseService.generateErrorResponse("User not found", HttpStatus.NOT_FOUND);

        double[] balances = paymentService.balances(serviceProvider.getService_provider_id());
        double availableBalance = balances[0] + balances[1];
        double amountToSettle = transactionDTO.getAmountToSettle();

        if (amountToSettle > availableBalance) {
            return ResponseService.generateErrorResponse("Amount to settle cannot exceed available balance", HttpStatus.BAD_REQUEST);
        }

        // Fetch carry over entry if exists
        Earnings earningWithCarryOver = null;
        Query carryOverQuery = entityManager.createQuery(
                "SELECT e.id FROM Earnings e WHERE e.carryOver < 0 AND e.providerId = :id", Long.class);
        carryOverQuery.setParameter("id", transactionDTO.getUserId());
        List<Long> carryOverIds = carryOverQuery.getResultList();

        if (!carryOverIds.isEmpty()) {
            earningWithCarryOver = entityManager.find(Earnings.class, carryOverIds.get(0));
        }

        // Fetch all unsettled earnings in oldest-first order
        Query earningsQuery = entityManager.createQuery(
                "SELECT e FROM Earnings e WHERE e.providerId = :id AND e.settled = false ORDER BY e.date ASC", Earnings.class);
        earningsQuery.setParameter("id", transactionDTO.getUserId());
        List<Earnings> earningsList = earningsQuery.getResultList();

        double remainingAmount = amountToSettle;
        double totalSettled = 0.0;

        for (Earnings earning : earningsList) {
            if (remainingAmount <= 0) break;

            double pending = earning.getPending();

            if (pending <= remainingAmount) {
                // Full settlement
                earning.setPaid(earning.getPaid() + pending);
                earning.setPending(0.0);
                earning.setSettled(true);
                earning.setPaymentDone(true);
                remainingAmount -= pending;
                totalSettled += pending;
            } else {
                // Partial settlement
                earning.setPaid(earning.getPaid() + remainingAmount);
                earning.setPending(pending - remainingAmount);
                earning.setSettled(false);
                earning.setPaymentDone(false);
                totalSettled += remainingAmount;
                remainingAmount = 0.0;
            }

            entityManager.merge(earning);
        }

        // Handle carry over
        if (earningWithCarryOver != null && (balances[0] + balances[1] >= (-earningWithCarryOver.getCarryOver()))) {
            earningWithCarryOver.setCarryOver(0.0);
            entityManager.merge(earningWithCarryOver);
        }

        // Store transaction record
        Transaction transaction = new Transaction();
        transaction.setCurrentMonthPayable(balances[1]);
        transaction.setLastMonthPayable(balances[0]);
        transaction.setSettledAmount(totalSettled);
        transaction.setBalance(availableBalance - totalSettled);
        transaction.setSettlementRemarks(transactionDTO.getSettlementRemarks());
        transaction.setRole(serviceProvider.getRole());
        transaction.setUserId(serviceProvider.getService_provider_id());
        transaction.setDate(new Date());

        entityManager.persist(transaction);

        return ResponseService.generateSuccessResponse("Transaction settled successfully", transaction, HttpStatus.OK);
    }*/
    //l3
   /* @Authorize(value = {Constant.roleAdmin, Constant.roleSuperAdmin})
    @Transactional
    @PostMapping("/settle-amount")
    public ResponseEntity<?> settleEarnings(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestBody TransactionDTO transactionDTO) {

        if (transactionDTO.getUserId() == null)
            return ResponseService.generateErrorResponse("User id is required", HttpStatus.BAD_REQUEST);

        if (transactionDTO.getAmountToSettle() == null || transactionDTO.getAmountToSettle() <= 0)
            return ResponseService.generateErrorResponse("Amount to settle must be greater than 0", HttpStatus.BAD_REQUEST);

        ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, transactionDTO.getUserId());
        if (serviceProvider == null)
            return ResponseService.generateErrorResponse("User not found", HttpStatus.NOT_FOUND);

        double[] balances = paymentService.balances(serviceProvider.getService_provider_id());
        double availableBalance = balances[0] + balances[1];
        double requestedSettleAmount = transactionDTO.getAmountToSettle();

        // Fetch carry over entry if exists
        Earnings earningWithCarryOver = null;
        Query carryOverQuery = entityManager.createQuery(
                "SELECT e.id FROM Earnings e WHERE e.carryOver < 0 AND e.providerId = :id", Long.class);
        carryOverQuery.setParameter("id", transactionDTO.getUserId());
        List<Long> carryOverIds = carryOverQuery.getResultList();
        if (!carryOverIds.isEmpty()) {
            earningWithCarryOver = entityManager.find(Earnings.class, carryOverIds.get(0));
        }

        // Fetch all unsettled earnings in oldest-first order
        Query earningsQuery = entityManager.createQuery(
                "SELECT e FROM Earnings e WHERE e.providerId = :id AND e.settled = false ORDER BY e.date ASC", Earnings.class);
        earningsQuery.setParameter("id", transactionDTO.getUserId());
        List<Earnings> earningsList = earningsQuery.getResultList();

        double remainingAmount = requestedSettleAmount;
        double totalSettled = 0.0;
        Earnings lastEarningUpdated = null;

        for (Earnings earning : earningsList) {
            if (remainingAmount <= 0) break;

            double pending = earning.getPending();

            if (pending <= remainingAmount) {
                // Full settlement
                earning.setPaid(earning.getPaid() + pending);
                earning.setPending(0.0);
                earning.setSettled(true);
                earning.setPaymentDone(true);
                remainingAmount -= pending;
                totalSettled += pending;
            } else {
                // Partial settlement
                earning.setPaid(earning.getPaid() + remainingAmount);
                earning.setPending(pending - remainingAmount);
                earning.setSettled(false);
                earning.setPaymentDone(false);
                totalSettled += remainingAmount;
                remainingAmount = 0.0;
            }

            entityManager.merge(earning);
            lastEarningUpdated = earning;
        }

        //@Note- Handle carry over if there's still remaining amount after all earnings are settled
        if (remainingAmount > 0 && lastEarningUpdated != null) {
            lastEarningUpdated.setCarryOver(-remainingAmount);
            entityManager.merge(lastEarningUpdated);
        }

        // Clean up existing carryOver if it was covered by available balance
        if (earningWithCarryOver != null && (availableBalance >= -earningWithCarryOver.getCarryOver())) {
            earningWithCarryOver.setCarryOver(0.0);
            entityManager.merge(earningWithCarryOver);
        }

        // Record the transaction
        Transaction transaction = new Transaction();
        transaction.setCurrentMonthPayable(balances[1]);
        transaction.setLastMonthPayable(balances[0]);
        transaction.setSettledAmount(requestedSettleAmount);
        transaction.setBalance(availableBalance - requestedSettleAmount);
        transaction.setSettlementRemarks(transactionDTO.getSettlementRemarks());
        transaction.setRole(serviceProvider.getRole());
        transaction.setUserId(serviceProvider.getService_provider_id());
        transaction.setDate(new Date());

        entityManager.persist(transaction);

        return ResponseService.generateSuccessResponse("Transaction settled successfully", transaction, HttpStatus.OK);
    }*/
    //v2
   /* @Authorize(value = {Constant.roleAdmin, Constant.roleSuperAdmin})
    @Transactional
    @PostMapping("/settle-amount")
    public ResponseEntity<?> settleEarnings(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestBody TransactionDTO transactionDTO) {

        if (transactionDTO.getUserId() == null)
            return ResponseService.generateErrorResponse("User id is required", HttpStatus.BAD_REQUEST);

        if (transactionDTO.getAmountToSettle() == null || transactionDTO.getAmountToSettle() <= 0)
            return ResponseService.generateErrorResponse("Amount to settle must be greater than 0", HttpStatus.BAD_REQUEST);

        ServiceProviderEntity provider = entityManager.find(ServiceProviderEntity.class, transactionDTO.getUserId());
        if (provider == null)
            return ResponseService.generateErrorResponse("User not found", HttpStatus.NOT_FOUND);

        double[] balances = paymentService.balances(provider.getService_provider_id());
        double loanBalance = provider.getSurplus() != null ? provider.getSurplus() : 0.0; // rename from surplus to loanBalance

        // balances[0] = last month pending
        // balances[1] = this month pending
        // balances[2] = previously stored loan balance (for compatibility, but we will rely on provider.loanBalance now)
        double availableBalance = balances[0] + balances[1] + loanBalance; // loanBalance is negative or zero

        double requestedSettleAmount = transactionDTO.getAmountToSettle();

        // Fetch all unsettled earnings oldest first
        Query earningsQuery = entityManager.createQuery(
                "SELECT e FROM Earnings e WHERE e.providerId = :id AND e.settled = false ORDER BY e.date ASC", Earnings.class);
        earningsQuery.setParameter("id", transactionDTO.getUserId());
        List<Earnings> earningsList = earningsQuery.getResultList();

        double remainingAmount = requestedSettleAmount;

        for (Earnings earning : earningsList) {
            if (remainingAmount <= 0) break;

            double pending = earning.getPending();

            // Settle as much as possible from remainingAmount
            double paidNow = Math.min(pending, remainingAmount);
            earning.setPaid(earning.getPaid() + paidNow);
            earning.setPending(pending - paidNow);
            remainingAmount -= paidNow;

            if (earning.getPending() > 0) {
                // Pending not zero, check if loan balance (negative) can cover the rest of this earning
                if (loanBalance < 0) {
                    double amountToCover = earning.getPending();
                    double loanAbs = Math.abs(loanBalance);

                    if (loanAbs >= amountToCover) {
                        // Loan fully covers remaining pending
                        earning.setPending(0.0);
                        earning.setSettled(true);
                        earning.setPaymentDone(true);

                        // Reduce negative loan balance by amount covered
                        loanBalance += amountToCover; // e.g. loanBalance = -50 + 50 = 0

                    } else {
                        // Loan partially covers pending
                        earning.setPending(amountToCover - loanAbs);
                        earning.setSettled(false);
                        earning.setPaymentDone(false);

                        // Loan fully used
                        loanBalance = 0.0;
                    }
                } else {
                    // No negative loan balance to cover, earning remains unsettled
                    earning.setSettled(false);
                    earning.setPaymentDone(false);
                }
            } else {
                // Fully settled earning
                earning.setSettled(true);
                earning.setPaymentDone(true);
            }

            entityManager.merge(earning);
        }

        // Calculate total unpaid at start (excluding loanBalance, which is accounted here)
        double totalUnpaidAtStart = balances[0] + balances[1];

        // Amount directly applied to earnings from requested settle amount
        double amountAppliedToEarnings = requestedSettleAmount - remainingAmount;

        // Adjust loanBalance based on settle amount vs unpaid
        if (requestedSettleAmount > totalUnpaidAtStart) {
            // Overpayment beyond unpaid earnings increases negative loan (more debt)
            double overpayment = requestedSettleAmount - totalUnpaidAtStart;
            loanBalance -= overpayment; // e.g. loanBalance = -1000 - 1000 = -2000 (more debt)
        } else if (requestedSettleAmount < totalUnpaidAtStart) {
            // Settled less than unpaid, loanBalance stays as-is (no positive loan)
            // Do nothing here; loanBalance cannot go positive
        }

        // If loanBalance got positive due to adjustments, reset it to zero because positive loan is invalid
        if (loanBalance > 0) {
            loanBalance = 0.0;
        }

        // Persist updated loan balance
        provider.setSurplus(loanBalance);
        entityManager.merge(provider);

        // Create and persist Transaction
        Transaction transaction = new Transaction();
        transaction.setLastMonthPayable(balances[0]);
        transaction.setCurrentMonthPayable(balances[1]);
        // transaction.setSurplus(loanBalance); // If you want to keep this for records

        transaction.setSettledAmount(requestedSettleAmount);
        transaction.setBalance(balances[0] + balances[1] + loanBalance - requestedSettleAmount);
        transaction.setSettlementRemarks(transactionDTO.getSettlementRemarks());
        transaction.setRole(provider.getRole());
        transaction.setUserId(provider.getService_provider_id());
        transaction.setDate(new Date());

        entityManager.persist(transaction);

        return ResponseService.generateSuccessResponse("Transaction settled successfully", transaction, HttpStatus.OK);
    }*/

    @Authorize(value = {Constant.roleAdmin, Constant.roleSuperAdmin})
    @Transactional
    @PostMapping("/settle-amount")
    public ResponseEntity<?> settleEarnings(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestBody TransactionDTO transactionDTO) {

        if (transactionDTO.getUserId() == null)
            return ResponseService.generateErrorResponse("User id is required", HttpStatus.BAD_REQUEST);

        if (transactionDTO.getAmountToSettle() == null || transactionDTO.getAmountToSettle() <= 0)
            return ResponseService.generateErrorResponse("Amount to settle must be greater than 0", HttpStatus.BAD_REQUEST);

        ServiceProviderEntity provider = entityManager.find(ServiceProviderEntity.class, transactionDTO.getUserId());
        if (provider == null)
            return ResponseService.generateErrorResponse("User not found", HttpStatus.NOT_FOUND);

        double[] balances = paymentService.balances(provider.getService_provider_id());
        double requestedSettleAmount = transactionDTO.getAmountToSettle();

        // Fetch unsettled earnings ordered oldest first
        Query earningsQuery = entityManager.createQuery(
                "SELECT e FROM Earnings e WHERE e.providerId = :id AND e.settled = false ORDER BY e.date ASC", Earnings.class);
        earningsQuery.setParameter("id", transactionDTO.getUserId());
        List<Earnings> earningsList = earningsQuery.getResultList();

        double remainingAmount = requestedSettleAmount;
        double currentSurplus = provider.getSurplus() != null ? provider.getSurplus() : 0.0;

        for (Earnings earning : earningsList) {
            if (remainingAmount <= 0) break;

            double pending = earning.getPending();
            double paidNow = Math.min(pending, remainingAmount);

            // Pay from the requested amount first
            earning.setPaid(earning.getPaid() + paidNow);
            earning.setPending(pending - paidNow);
            remainingAmount -= paidNow;

            // If earning still has pending, try to cover from negative surplus (loan)
            if (earning.getPending() > 0 && currentSurplus < 0) {
                double amountToCover = earning.getPending();
                double surplusAbs = Math.abs(currentSurplus);

                if (surplusAbs >= amountToCover) {
                    // Surplus fully covers remaining pending
                    earning.setPending(0.0);
                    earning.setSettled(true);
                    earning.setPaymentDone(true);

                    currentSurplus += amountToCover; // Reduce negative surplus (loan repaid)
                } else {
                    // Surplus partially covers remaining pending
                    earning.setPending(amountToCover - surplusAbs);
                    earning.setSettled(false);
                    earning.setPaymentDone(false);

                    currentSurplus = 0.0; // Surplus fully used
                }
            } else if (earning.getPending() == 0) {
                // Fully settled earning
                earning.setSettled(true);
                earning.setPaymentDone(true);
            } else {
                // Pending > 0 and surplus >= 0
                earning.setSettled(false);
                earning.setPaymentDone(false);
            }

            entityManager.merge(earning);
        }

        // Calculate total unpaid from balances (without surplus)
        double totalUnpaidAtStart = balances[0] + balances[1];

        // Adjust surplus: if settled more than unpaid, surplus (loan) increases (more negative)
        if (requestedSettleAmount > totalUnpaidAtStart) {
            double overpayment = requestedSettleAmount - totalUnpaidAtStart;
            currentSurplus -= overpayment;
        } else if (requestedSettleAmount < totalUnpaidAtStart) {
            // If less settled than unpaid, loan increases by unused unpaid amount
            double unusedAmount = totalUnpaidAtStart - requestedSettleAmount;
            currentSurplus += unusedAmount;
            // But ensure surplus never becomes positive
            if (currentSurplus > 0) currentSurplus = 0;
        }

        // Update provider surplus (loan)
        provider.setSurplus(currentSurplus);
        entityManager.merge(provider);

        // Calculate final balance after settlement and surplus adjustments
        double finalBalance = balances[0] + balances[1] + currentSurplus;
        if (finalBalance < 0) finalBalance = 0; // balance can’t be negative

        // Create and persist transaction
        Transaction transaction = new Transaction();
        transaction.setLastMonthPayable(balances[0]);
        transaction.setCurrentMonthPayable(balances[1]);
        // transaction.setSurplus(currentSurplus); // Uncomment if Transaction has surplus field

        transaction.setSettledAmount(requestedSettleAmount);
        transaction.setBalance(finalBalance);
        transaction.setSettlementRemarks(transactionDTO.getSettlementRemarks());
        transaction.setRole(provider.getRole());
        transaction.setUserId(provider.getService_provider_id());
        transaction.setDate(new Date());

        entityManager.persist(transaction);

        return ResponseService.generateSuccessResponse("Transaction settled successfully", transaction, HttpStatus.OK);
    }






}


