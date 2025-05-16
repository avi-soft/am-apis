package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomWorkQuality;
import com.community.api.services.exception.ExceptionHandlingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
@Slf4j
public class WorkQualityService {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<CustomWorkQuality> getAllWorkQuality() throws Exception {
        try {
            List<CustomWorkQuality> workQualityList = entityManager.createQuery(Constant.GET_ALL_WORK_QUALITY, CustomWorkQuality.class).getResultList();

            if(!workQualityList.isEmpty()){
                return workQualityList;
            } else {
                throw new IllegalArgumentException("No Work Quality found");
            }
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some Exception Caught: " + exception.getMessage());
        }
    }

    public CustomWorkQuality getWorkQualityByWorkQualityId(Long workQualityId) throws Exception {
        try {

            Query query = entityManager.createQuery(Constant.GET_TICKET_TYPE_BY_WORK_QUALITY_ID, CustomWorkQuality.class);
            query.setParameter("workQualityId", workQualityId);
            List<CustomWorkQuality> workQuality = query.getResultList();

            if (!workQuality.isEmpty()) {
                return workQuality.get(0);
            } else {
                throw new IllegalArgumentException("No Work Quality found with this work quality id");
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some Exception Caught: " + exception.getMessage());
        }
    }

}
