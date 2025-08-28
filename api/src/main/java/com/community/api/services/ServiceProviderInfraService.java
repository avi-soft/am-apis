package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.ServiceProviderInfra;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class ServiceProviderInfraService {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private ResponseService responseService;

    @Transactional
    public ResponseEntity<?> addInfra(@RequestBody ServiceProviderInfra serviceProviderInfra) {
        try {
            if (serviceProviderInfra.getInfra_name() == null || serviceProviderInfra.getInfra_name().isEmpty() || serviceProviderInfra.getInfra_name().trim().isEmpty())
                return responseService.generateErrorResponse("Infra name cannot be empty", HttpStatus.BAD_REQUEST);
            int count = (int) findCount();
            serviceProviderInfra.setInfra_id(++count);
            entityManager.persist(serviceProviderInfra);
            return responseService.generateSuccessResponse("Infra added successfully", serviceProviderInfra, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return responseService.generateErrorResponse("Error saving skill : " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public long findCount() throws Exception {
        try {
            String queryString = Constant.GET_INFRA_COUNT;
            TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
            return query.getSingleResult();
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception("Error Finding count of infra list.");
        }
    }

    public List<ServiceProviderInfra> findAllInfraList() throws Exception {
        try {
            TypedQuery<ServiceProviderInfra> query = entityManager.createQuery(Constant.GET_INFRA_LIST, ServiceProviderInfra.class);
            return query.getResultList();
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception("Error Fetching infra list.");
        }
    }
}
