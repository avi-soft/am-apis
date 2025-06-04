package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomApplicationScope;
import com.community.api.entity.CustomGender;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class GenderService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    SharedUtilityService sharedUtilityService;

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    public List<CustomGender> getAllGender(Boolean archived) {
        try {
            List<CustomGender> customGenderList = entityManager.createQuery(Constant.GET_ALL_GENDER, CustomGender.class).setParameter("archived",archived).getResultList();
            return customGenderList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }

    public CustomGender getGenderById(Long genderId) throws Exception {
        try {
            Query query = entityManager.createQuery(
                    "SELECT g FROM CustomGender g WHERE g.genderId = :genderId", CustomGender.class);
            query.setParameter("genderId", genderId);

            List<CustomGender> genders = query.getResultList();
            if (genders.isEmpty() || genders == null) {
                throw new IllegalArgumentException("GENDER NOT FOUND");
            }
            return genders.get(0);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Number format exception: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            throw new Exception("Some Exception Occurred: " + e.getMessage());
        }
    }



    public CustomGender getGenderByGenderId(Long genderId) {
        try {

            Query query = entityManager.createQuery(Constant.GET_GENDER_BY_GENDER_ID, CustomGender.class);
            query.setParameter("genderId", genderId);
            List<CustomGender> customGender = query.getResultList();

            if (!customGender.isEmpty()) {
                return customGender.get(0);
            } else {
                return null;
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }
    public CustomGender getGenderByName(String genderName)
    {
        try {
            genderName = genderName.toUpperCase();
            Query query = entityManager.createQuery(Constant.GET_GENDER_BY_GENDER_NAME, CustomGender.class);
            query.setParameter("genderName", genderName);
            List<CustomGender> customGender = query.getResultList();

            if (!customGender.isEmpty()) {
                return customGender.get(0);
            } else {
                return null;
            }
        }
            catch (NoResultException exception) {
                exceptionHandlingService.handleException(exception);
                return null;
            }
        catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
        }
    public Long getMaxGenderId() {
        String query = "SELECT MAX(gender_id) AS max_gender_id FROM custom_gender";
        Query nquery = entityManager.createNativeQuery(query);
        return ((Number)nquery.getSingleResult()).longValue();
    }

    @Transactional
    public CustomGender addGender(CustomGender customGender) throws IllegalArgumentException, Exception {
        try {
            Query query = entityManager.createQuery(
                    "SELECT g FROM CustomGender g WHERE g.genderName = :genderName", CustomGender.class);
            query.setParameter("genderName", customGender.getGenderName());
            List<CustomGender> genders = query.getResultList();

            if(customGender.getGenderId() != null)
                throw new IllegalArgumentException("Cannot give gender id when adding");

            customGender.setGenderId(getMaxGenderId() + 1);

            if(!genders.isEmpty())
                throw new IllegalArgumentException("Gender already exists");

            if(customGender.getGenderName() == null)
                throw new IllegalArgumentException("Gender name is required");

            if(customGender.getGenderSymbol() == null)
                throw new IllegalArgumentException("Gender symbol is required");

            if (!sharedUtilityService.isAlphabetic(customGender.getGenderName()))
                throw new IllegalArgumentException("Gender name should contain only alphabets");

            if(customGender.getArchived() != null) {
                throw new IllegalArgumentException("Cannot provide archive status when adding a gender");
            }

            customGender.setArchived(false);
            entityManager.persist(customGender);
            return customGender;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Transactional
    public CustomGender editGender(Long genderId, CustomGender customGender)
            throws IllegalArgumentException, Exception {
        try {
            CustomGender gender = entityManager.find(CustomGender.class, genderId);

            if(customGender.getGenderId() != null)
                throw new IllegalArgumentException("Cannot give gender id when editing");

            Query query = entityManager.createQuery(
                    "SELECT g FROM CustomGender g WHERE g.genderName = :genderName", CustomGender.class);
            query.setParameter("genderName", customGender.getGenderName());
            List<CustomGender> genders = query.getResultList();

            if(!genders.isEmpty() && genders.get(0).getGenderId() != gender.getGenderId())
                throw new IllegalArgumentException("Gender with this name already exists");

            if (!sharedUtilityService.isAlphabetic(customGender.getGenderName()))
                throw new IllegalArgumentException("Gender name should contain only alphabets");

            if(customGender.getArchived() != null) {
                throw new IllegalArgumentException("Cannot provide archive status when updating a gender");
            }

            gender.setGenderName(customGender.getGenderName());
            gender.setGenderSymbol(customGender.getGenderSymbol());
            entityManager.merge(gender);
            return gender;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Transactional
    public CustomGender manageGender(Long genderId, Boolean archive)
            throws IllegalArgumentException, Exception {
        try {
            CustomGender gender = entityManager.find(CustomGender.class, genderId);

            if(archive) {
                if (gender.getArchived())
                    throw new IllegalArgumentException("Gender already archived");
                else {
                    gender.setArchived(true);
                    entityManager.merge(gender);
                }
            } else {
                if (!gender.getArchived())
                    throw new IllegalArgumentException("Gender already unarchived");
                else {
                    gender.setArchived(false);
                    entityManager.merge(gender);
                }
            }
            return gender;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
    }

