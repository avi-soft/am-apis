package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomJobGroup;
import com.community.api.entity.Qualification;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.utils.DocumentType;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static com.community.api.component.Constant.FIND_ALL_QUALIFICATIONS_QUERY;

@Service
public class QualificationService {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandlingService;
    @Autowired
    private QualificationService qualificationService;
    @Autowired
    private ResponseService responseService;

    public List<Qualification> getAllQualifications(Boolean archived) {
        TypedQuery<Qualification> query = entityManager.createQuery(Constant.FIND_ALL_QUALIFICATIONS_QUERY, Qualification.class);
        query.setParameter("archived", archived);
        List<Qualification> qualifications = query.getResultList();
        return qualifications;
    }

    //    @todo:- Need to work on add qualification function so that entries should be inserted in document table also make sure to add one exam text in description
    @Transactional
    public Qualification addQualification(@RequestBody Qualification qualification) throws Exception {
        Qualification qualificationToBeSaved = new Qualification();
        int id = findCount() + 1;
        if (qualification.getQualification_name() == null || qualification.getQualification_name().trim().isEmpty()) {
            throw new IllegalArgumentException("Qualification name cannot be empty or consist only of whitespace");
        }
        if (qualification.getQualification_description() == null || qualification.getQualification_description().trim().isEmpty()) {
            throw new IllegalArgumentException("Qualification description cannot be empty or consist only of whitespace");
        }
        /*if (!qualification.getQualification_name().matches("^[a-zA-Z ]+$")) {
            throw new IllegalArgumentException("Qualification name cannot contain numeric values or special characters");
        }*/
        if (!(qualification.getQualification_description() instanceof String)) {
            throw new IllegalArgumentException("Qualification description must be a string");
        }
        if (qualification.getIs_stream_required() == null) {
            throw new IllegalArgumentException("You have to give whether stream required or not for qualification");
        }
        if (qualification.getIs_subjects_required() == null) {
            throw new IllegalArgumentException("You have to give whether subject required or not for qualification");
        }
        String description = qualification.getQualification_description();
        if (description.isEmpty()) {
            throw new IllegalArgumentException("Qualification description cannot be empty");
        }


        List<Qualification> qualifications = qualificationService.getAllQualifications(false);
        for (Qualification existingQualification : qualifications) {
            if (existingQualification.getQualification_name().equalsIgnoreCase(qualification.getQualification_name())) {
                throw new IllegalArgumentException("Qualification with the same name already exists");
            }
        }
        qualificationToBeSaved.setQualification_id(id);
        qualificationToBeSaved.setQualification_name(qualification.getQualification_name());
        qualificationToBeSaved.setQualification_description(qualification.getQualification_description());
        Long maxSortOrder = getSecondMaxSortOrder();
        qualificationToBeSaved.setSort_order(maxSortOrder + 1);
        qualificationToBeSaved.setIs_stream_required(qualification.getIs_stream_required());
        qualificationToBeSaved.setArchived(false);
        qualificationToBeSaved.setIs_subjects_required(qualification.getIs_subjects_required());
        entityManager.persist(qualificationToBeSaved);
        return qualificationToBeSaved;
    }

    @Transactional
    public Qualification edit(Integer qualificationId, @RequestBody Qualification qualification) throws Exception {
        Qualification qualificationToBeSaved = entityManager.find(Qualification.class, qualificationId);
        if (qualificationToBeSaved == null)
            throw new IllegalArgumentException("Qualification not found");
        if (qualification.getQualification_name() == null || qualification.getQualification_name().trim().isEmpty()) {
            throw new IllegalArgumentException("Qualification name cannot be empty or consist only of whitespace");
        }
        if (qualification.getQualification_description() == null || qualification.getQualification_description().trim().isEmpty()) {
            throw new IllegalArgumentException("Qualification description cannot be empty or consist only of whitespace");
        }
        if (!(qualification.getQualification_description() instanceof String)) {
            throw new IllegalArgumentException("Qualification description must be a string");
        }
        if (qualification.getIs_stream_required() == null) {
            throw new IllegalArgumentException("You have to give whether stream required or not for qualification");
        }
        if (qualification.getIs_subjects_required() == null) {
            throw new IllegalArgumentException("You have to give whether subject required or not for qualification");
        }
        String description = qualification.getQualification_description();
        if (description.isEmpty()) {
            throw new IllegalArgumentException("Qualification description cannot be empty");
        }


        List<Qualification> qualifications = qualificationService.getAllQualifications(false);
        for (Qualification existingQualification : qualifications) {
            if (existingQualification.getQualification_name().equalsIgnoreCase(qualification.getQualification_name()) && !existingQualification.getQualification_id().equals(qualificationId)) {
                throw new IllegalArgumentException("Qualification with the same name already exists");
            }
        }
        qualificationToBeSaved.setQualification_name(qualification.getQualification_name());
        qualificationToBeSaved.setQualification_description(qualification.getQualification_description());
        Long maxSortOrder = getSecondMaxSortOrder();
        qualificationToBeSaved.setSort_order(maxSortOrder + 1);
        qualificationToBeSaved.setIs_stream_required(qualification.getIs_stream_required());
        qualificationToBeSaved.setIs_subjects_required(qualification.getIs_subjects_required());
        entityManager.merge(qualificationToBeSaved);
        return qualificationToBeSaved;
    }

    public Long getSecondMaxSortOrder() {
        String query = "SELECT q.sort_order FROM Qualification q ORDER BY q.sort_order DESC";
        List<Long> sortOrders = entityManager.createQuery(query, Long.class)
                .setMaxResults(2)
                .getResultList();
        if (sortOrders.size() == 2) {
            return sortOrders.get(1); // second max
        } else if (sortOrders.size() == 1) {
            return sortOrders.get(0); // only one exists, use it
        } else {
            return 0L; // none exists
        }
    }

    //need to be change here
    public int findCount() throws Exception {
        try {
            String queryString = Constant.GET_QUALIFICATIONS_COUNT;
            TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
            return query.getSingleResult().intValue();
        } catch (NoResultException e) {
            exceptionHandlingService.handleException(e);
            throw new NoResultException("No qualification is found");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOMETHING WENT WRONG: " + exception.getMessage());
        }
    }


    public Qualification getQualificationByQualificationId(Integer qualificationId) throws Exception {
        try {

            Query query = entityManager.createQuery(Constant.GET_QUALIFICATION_BY_ID, Qualification.class);
            query.setParameter("qualificationId", qualificationId);
            List<Qualification> qualification = query.getResultList();

            if (!qualification.isEmpty()) {
                return qualification.get(0);
            } else {
                return null;
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOMETHING WENT WRONG: " + exception.getMessage());
        }
    }
}
