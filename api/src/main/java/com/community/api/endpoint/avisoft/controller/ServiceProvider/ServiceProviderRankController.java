package com.community.api.endpoint.avisoft.controller.ServiceProvider;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.ServiceProviderRankDto;
import com.community.api.entity.Role;
import com.community.api.entity.ServiceProviderRank;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.ServiceProvider.ServiceProviderRankService;
import com.community.api.services.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.social.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.List;
import java.util.Map;

import static com.community.api.component.Constant.FIND_ALL_SERVICE_PROVIDER_TEST_RANK_QUERY;

@RestController
@RequestMapping("/service-provider-rank")
public class ServiceProviderRankController {

    private EntityManager entityManager;
    private ResponseService responseService;
    protected ExceptionHandlingImplement exceptionHandling;
    private ServiceProviderRankService serviceProviderRankService;
    private RoleService roleService;
    private JwtUtil jwtTokenUtil;

    public ServiceProviderRankController(EntityManager entityManager, ResponseService responseService, ExceptionHandlingImplement exceptionHandling, ServiceProviderRankService serviceProviderRankService, RoleService roleService, JwtUtil jwtTokenUtil) {
        this.responseService = responseService;
        this.entityManager = entityManager;
        this.exceptionHandling = exceptionHandling;
        this.serviceProviderRankService = serviceProviderRankService;
        this.roleService = roleService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @GetMapping("/get-all-service-provider-rank")

    public ResponseEntity<?> getAllServiceProviderRank() {
        try
        {
            TypedQuery<ServiceProviderRank> query = entityManager.createQuery(FIND_ALL_SERVICE_PROVIDER_TEST_RANK_QUERY, ServiceProviderRank.class);
            List<ServiceProviderRank> serviceProviderTestRankList = query.getResultList();
            if (serviceProviderTestRankList.isEmpty()) {
                return responseService.generateResponse(HttpStatus.OK, "Service Provider Test Rank List is Empty", serviceProviderTestRankList);
            }
            return responseService.generateResponse(HttpStatus.OK, "Service Provider Test Rank List Retrieved Successfully", serviceProviderTestRankList);
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong",HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/get-score-card/{serviceProviderId}")
    public ResponseEntity<?> getScoreCardToServiceProvider(@PathVariable Long serviceProviderId) {
        try {
            Map<String,Integer> scoreCard = serviceProviderRankService.getScoreCard(serviceProviderId);
            return ResponseService.generateSuccessResponse("score card is retrieved successfully for service provider with ID: " + serviceProviderId,scoreCard , HttpStatus.OK);
        }
        catch (IllegalArgumentException e)
        {
            return ResponseService.generateErrorResponse(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong",HttpStatus.BAD_REQUEST);
        }
    }


    @Authorize(value = {Constant.roleAdmin, Constant.roleSuperAdmin})
    @GetMapping("get-by-id/{serviceProviderRankId}")
    public ResponseEntity<?> getServiceProvider(@PathVariable Long serviceProviderRankId) {
        try {

            ServiceProviderRank serviceProviderRank = serviceProviderRankService.getServiceProviderRankByRankId(serviceProviderRankId);

            return ResponseService.generateSuccessResponse("Service Provider Rank Found Successfully", serviceProviderRank , HttpStatus.OK);
        } catch (ResourceNotFoundException resourceNotFoundException) {
            exceptionHandling.handleException(resourceNotFoundException);
            return ResponseService.generateErrorResponse("Resource Not Found: " + resourceNotFoundException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse("Bad Request: " + illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Something went wrong: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("update/{serviceProviderRankId}")
    @Authorize(value = {Constant.roleSuperAdmin})
    public ResponseEntity<?> updateServiceProvider(@PathVariable Long serviceProviderRankId, @RequestBody ServiceProviderRankDto serviceProviderRankDto, @RequestHeader(value = "Authorization") String authHeader) {
        try {

            if(serviceProviderRankDto == null) {
                throw new IllegalArgumentException("Nothing to update");
            }
            ServiceProviderRank serviceProviderRank = serviceProviderRankService.getServiceProviderRankByRankId(serviceProviderRankId);
            serviceProviderRankService.updateServiceProviderRank(serviceProviderRankDto, serviceProviderRank);

            return ResponseService.generateSuccessResponse("Service Provider Rank updated Successfully", serviceProviderRank , HttpStatus.OK);
        } catch (ResourceNotFoundException resourceNotFoundException) {
            exceptionHandling.handleException(resourceNotFoundException);
            return ResponseService.generateErrorResponse("Resource Not Found: " + resourceNotFoundException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse("Bad Request: " + illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Something went wrong: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}


