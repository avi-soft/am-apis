package com.community.api.endpoint.avisoft.controller.Earnings;

import com.community.api.annotation.Authorize;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.PaymentDetailsDTO;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.Earnings;
import com.community.api.entity.Role;
import com.community.api.services.PaymentService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import jakarta.jws.soap.SOAPBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    @Autowired
    public EarningsController(EntityManager entityManager,
                              JwtUtil jwtTokenUtil,
                              RoleService roleService,
                              PaymentService paymentService) {
        this.entityManager = entityManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.roleService = roleService;
        this.paymentService = paymentService;
    }

    @GetMapping("filter")
    public ResponseEntity<?> getFilteredEarnings(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestParam(required = false, defaultValue = "false") Boolean settled,
            @RequestParam(required = true) List<Long> spIds,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String from) throws Exception {


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
            if (spIds != null && !spIds.isEmpty()) {
                return ResponseService.generateErrorResponse("Invalid action", HttpStatus.BAD_REQUEST);
            } else {
                spIds = new ArrayList<>();
                spIds.add(tokenUserId);
            }
        }

        List<BigInteger> earnings = new ArrayList<>();
        if (spIds != null && !spIds.isEmpty()) {
            generalizedQuery.append(" AND provider_id IN (");
            generalizedQuery.append(spIds.stream().map(id -> "?").collect(Collectors.joining(",")));
            generalizedQuery.append(")");
            params.addAll(spIds);
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
        Map<String,Object> resultMap=new HashMap<>();
        double[]balances=paymentService.balances(spIds.get(0));
        resultMap.put("last_month_payable",balances[0]);
        resultMap.put("this_month_payable",balances[1]);
        resultMap.put("balance_amount",balances[0]+balances[1]);
        resultMap.put("payments",result);
        return new ResponseService().generateSuccessResponse("Payments", resultMap, HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    @GetMapping("get-all")
    public ResponseEntity<?> getFilteredEarnings(@RequestHeader(value = "Authorization")String authHeader,@RequestParam(required = false) Long spId) {
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
                return ResponseService.generateSuccessResponse("Result", response, HttpStatus.OK);
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
            String address = provider.getBusiness_name() + " " +
                    (provider.getSpAddresses().isEmpty() ? "N/A" :
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
}

