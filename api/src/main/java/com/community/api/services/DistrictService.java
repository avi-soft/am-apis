package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.Districts;
import com.community.api.entity.StateCode;
import com.community.api.services.exception.ExceptionHandlingImplement;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class DistrictService {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private SharedUtilityService sharedUtilityService;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    public List<Districts> findDistrictsByStateCode(String state_code,Boolean archived) {
        TypedQuery<Districts> query = entityManager.createQuery(Constant.DISTRICT_QUERY, Districts.class);
        query.setParameter("archived",archived);
        query.setParameter("state_code", state_code);
        return query.getResultList();
    }

    public List<Districts> findAllDistricts(Boolean archived) {
        TypedQuery<Districts> query = entityManager.createQuery(Constant.DISTRICT_ALL_QUERY, Districts.class);
        query.setParameter("archived",archived);
        return query.getResultList();
    }

    public String findDistrictById(int district_id) {
        return entityManager.createQuery(Constant.FIND_DISTRICT, String.class)
                .setParameter("district_id", district_id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public String findStateById(int state_id) {

        return entityManager.createQuery(Constant.FIND_STATE, String.class)
                .setParameter("state_id", state_id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public List<StateCode> findStateList(Boolean archived) {
        TypedQuery<StateCode> query = entityManager.createQuery(Constant.GET_STATES_LIST, StateCode.class);
        query.setParameter("archived",archived);
        return query.getResultList();
    }

    public StateCode getStateByStateId(int stateId) throws Exception {
        try {
            Query query = entityManager.createQuery(Constant.GET_STATE_BY_STATE_ID, StateCode.class);
            query.setParameter("stateId", stateId);

            List<StateCode> stateCode = query.getResultList();
            if (stateCode.size() == 0 || stateCode == null) {
                throw new IllegalArgumentException("STATE NOT FOUND");
            }
            return stateCode.get(0);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandling.handleException(numberFormatException);
            throw new NumberFormatException("Number format exception: " + numberFormatException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception("Some Exception Occurred: " + exception.getMessage());
        }
    }

    public StateCode getStateByStateName(String state) throws Exception {
        try {
            Query query = entityManager.createQuery(Constant.GET_STATE_BY_STATE_NAME, StateCode.class);
            query.setParameter("state", state);

            List<StateCode> stateCode = query.getResultList();
            if (stateCode.size() == 0 || stateCode == null) {
                throw new IllegalArgumentException("STATE NOT FOUND");
            }
            return stateCode.get(0);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandling.handleException(numberFormatException);
            throw new NumberFormatException("Number format exception: " + numberFormatException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception("Some Exception Occurred: " + exception.getMessage());
        }
    }

    public Districts findDistrictByName(String district) throws Exception {
        try {
            Query query = entityManager.createQuery(Constant.FIND_DISTRICT_BY_NAME, Districts.class);
            query.setParameter("district", district);
            System.out.println("District:" + district);

            List<Districts> districts = query.getResultList();
            if (districts.size() == 0 || districts == null) {
                System.out.println(district);
                throw new IllegalArgumentException("DISTRICTS NOT FOUND");
            }
            return districts.get(0);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandling.handleException(numberFormatException);
            throw new NumberFormatException("Number format exception: " + numberFormatException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception("Some Exception Occurred: " + exception.getMessage());
        }
    }
    public Integer getCount()
    {
        String query ="SELECT MAX(state_id) AS max_state_id FROM custom_state_codes";
        Query nquery=entityManager.createNativeQuery(query);
        return (Integer)nquery.getSingleResult();
    }
    public Integer getCountDistrict()
    {
        String query ="SELECT MAX(district_id) AS max_district_id FROM custom_districts";
        Query nquery=entityManager.createNativeQuery(query);
        return (Integer)nquery.getSingleResult();
    }
    @Transactional
    public StateCode addState(StateCode stateCode) throws IllegalArgumentException, Exception {
        try {
            Query query = entityManager.createQuery(Constant.GET_STATE_BY_STATE_NAME, StateCode.class);
            query.setParameter("state", stateCode.getState_name());
            List<StateCode> stateCodes = query.getResultList();
            if(stateCode.getState_id()!=null)
                throw new IllegalArgumentException("Cannot give state id when adding");
            stateCode.setState_id(getCount()+1);
            if(!stateCodes.isEmpty())
                throw new IllegalArgumentException("State already exists");
            if(stateCode.getState_name()==null)
                throw new IllegalArgumentException("State name is required");
            if(stateCode.getState_code()==null)
                throw new IllegalArgumentException("State code is required");
        /*    if (!sharedUtilityService.isAlphabetic(stateCode.getState_name()))
                throw new IllegalArgumentException("State name should contain only alphabets");*/
            if (!sharedUtilityService.isAlphabetic(stateCode.getState_code()))
                throw new IllegalArgumentException("State code should contain only alphabets");
            if(stateCode.getArchived()!=null)
            {
                throw new IllegalArgumentException("Cannot provide archive status when adding a state");
            }
            stateCode.setArchived(false);
            stateCode.setIsState(true);
            entityManager.persist(stateCode);
            return stateCode;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
    @Transactional
    public Districts addDistrict(Districts district,Integer stateId) throws IllegalArgumentException, Exception {
        try {
            if(district.getState_code()==null)
                throw new IllegalArgumentException("State code is required");
            StateCode stateCode=entityManager.find(StateCode.class,stateId);
            if (stateCode==null)
                throw new IllegalArgumentException("Invalid state id ,State not found");
            Query query = entityManager.createQuery(Constant.GET_DISTRICT_BY_DISTRICT_NAME, Districts.class);
            if(!stateCode.getState_code().equals(district.getState_code()))
                throw new IllegalArgumentException("State code "+district.getState_code()+" does not belong to state "+stateCode.getState_name());
            query.setParameter("state", stateCode.getState_code());
            query.setParameter("district",district.getDistrict_name());
            List<Districts> districts = query.getResultList();
            if(district.getDistrict_id()!=null)
                throw new IllegalArgumentException("Cannot give district id when adding");
            district.setDistrict_id(getCountDistrict()+1);
            if(!districts.isEmpty())
                throw new IllegalArgumentException("District already exists in this state");
            if(district.getDistrict_name()==null)
                throw new IllegalArgumentException("District name is required");

            /*if (!sharedUtilityService.isAlphabetic(district.getDistrict_name()))
                throw new IllegalArgumentException("District name should contain only alphabets");*/
            if (!sharedUtilityService.isAlphabetic(district.getState_code()))
                throw new IllegalArgumentException("District code should contain only alphabets");
            if(district.getArchived()!=null)
            {
                throw new IllegalArgumentException("Cannot provide archive status when adding a district");
            }
            district.setArchived(false);
            entityManager.persist(district);
            return district;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
    @Transactional
    public StateCode editState(Integer stateId,StateCode stateCode) throws IllegalArgumentException, Exception {
        try {
            StateCode state=entityManager.find(StateCode.class,stateId);

            if(stateCode.getState_id()!=null)
                throw new IllegalArgumentException("Cannot give state id when editing");
            Query query = entityManager.createQuery(Constant.GET_STATE_BY_STATE_NAME, StateCode.class);
            query.setParameter("state", stateCode.getState_name());
            List<StateCode> stateCodes = query.getResultList();
            if(!stateCodes.isEmpty()&&stateCodes.get(0).getState_id()!=state.getState_id())
                throw new IllegalArgumentException("State with this name already exists");
            query = entityManager.createQuery(Constant.GET_STATE_BY_STATE_CODE, StateCode.class);
            query.setParameter("code", stateCode.getState_code());
            stateCodes = query.getResultList();
            if(!stateCodes.isEmpty()&&stateCodes.get(0).getState_id()!=state.getState_id())
                throw new IllegalArgumentException("State with this state code already exists");
           /* if (!sharedUtilityService.isAlphabetic(stateCode.getState_name()))
                throw new IllegalArgumentException("State name should contain only alphabets");*/
            if (!sharedUtilityService.isAlphabetic(stateCode.getState_code()))
                throw new IllegalArgumentException("State code should contain only alphabets");
            if(stateCode.getArchived()!=null)
            {
                throw new IllegalArgumentException("Cannot provide archive status when updating a state");
            }
            state.setState_code(stateCode.getState_code());
            state.setState_name(stateCode.getState_name());
            state.setArchived(stateCode.getArchived());
            /*state.setArchived(false);*/
            entityManager.merge(state);
            return state;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
    @Transactional
    public Districts editDistrict(Integer districtId,Districts district) throws IllegalArgumentException, Exception {
        try {
            Districts districtToEdit=entityManager.find(Districts.class,districtId);

            if(district.getDistrict_id()!=null)
                throw new IllegalArgumentException("Cannot give district id when editing");
            Query query = entityManager.createQuery(Constant.GET_DISTRICT_BY_DISTRICT_NAME, Districts.class);
            query.setParameter("state", district.getState_code());
            query.setParameter("district",district.getDistrict_name());
            List<Districts> districts = query.getResultList();
            if(!districts.isEmpty()&&districtToEdit.getDistrict_id()!=districts.get(0).getDistrict_id())
                throw new IllegalArgumentException("District already exists in state "+districts.get(0).getDistrict_name());
           /* if (!sharedUtilityService.isAlphabetic(district.getDistrict_name()))
                throw new IllegalArgumentException("District name should contain only alphabets");*/
            if (!sharedUtilityService.isAlphabetic(district.getState_code()))
                throw new IllegalArgumentException("State code should contain only alphabets");
            if(district.getArchived()!=null)
            {
                throw new IllegalArgumentException("Cannot provide archive status when updating a state");
            }
            districtToEdit.setState_code(district.getState_code());
            districtToEdit.setDistrict_name(district.getDistrict_name());
            districtToEdit.setArchived(false);
            entityManager.merge(districtToEdit);
            return districtToEdit;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
    @Transactional
    public StateCode manageState(Integer stateId,Boolean archive) throws IllegalArgumentException, Exception {
        try {
            StateCode state = entityManager.find(StateCode.class, stateId);
            if(archive) {
                if (state.getArchived())
                    throw new IllegalArgumentException("State already archived");
                else {
                    state.setArchived(true);
                    Query query =entityManager.createQuery("SELECT d from Districts d where d.state_code = :code",Districts.class);
                    query.setParameter("code",state.getState_code());
                    List<Districts>districts=query.getResultList();
                    for(Districts district:districts)
                    {
                        district.setArchived(true);
                        entityManager.merge(district);
                    }
                    entityManager.merge(state);
                }
            }
            else
            {
                if (!state.getArchived())
                    throw new IllegalArgumentException("State already unarchived");
                else {
                    state.setArchived(false);
                    Query query =entityManager.createQuery("SELECT d from Districts d where d.state_code = :code",Districts.class);
                    query.setParameter("code",state.getState_code());
                    List<Districts>districts=query.getResultList();
                    for(Districts district:districts)
                    {
                        district.setArchived(false);
                        entityManager.merge(district);
                    }
                    entityManager.merge(state);
                }
            }
            return state;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
    @Transactional
    public Districts manageDistrict(Integer districtId,Boolean archive) throws IllegalArgumentException, Exception {
        try {
            Districts district = entityManager.find(Districts.class, districtId);
            if(archive) {
                if (district.getArchived())
                    throw new IllegalArgumentException("District already archived");
                else {
                    district.setArchived(true);
                    entityManager.merge(district);
                }
            }
            else
            {
                if (!district.getArchived())
                    throw new IllegalArgumentException("District already unarchived");
                else {
                    district.setArchived(false);
                    entityManager.merge(district);
                }
            }
            return district;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

}
