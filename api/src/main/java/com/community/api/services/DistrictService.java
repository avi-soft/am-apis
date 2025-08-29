package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.Districts;
import com.community.api.entity.StateCode;
import com.community.api.services.exception.ExceptionHandlingImplement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

@Slf4j
@Service
public class DistrictService {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private SharedUtilityService sharedUtilityService;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    public List<Districts> findDistrictsByStateCode(String state_code, Boolean archived) throws Exception {
        try {
            TypedQuery<Districts> query = entityManager.createQuery(Constant.DISTRICT_QUERY, Districts.class);
            query.setParameter("archived", archived);
            query.setParameter("state_code", state_code);
            return query.getResultList();
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public List<Districts> findALLDistrictsByStateCode(String state_code) {
        TypedQuery<Districts> query = entityManager.createQuery(Constant.DISTRICT_QUERY_ALL, Districts.class);
        query.setParameter("state_code", state_code);
        return query.getResultList();
    }

    public List<Districts> findAllDistricts(Boolean archived) throws Exception {
        try {
            TypedQuery<Districts> query = entityManager.createQuery(Constant.DISTRICT_ALL_QUERY, Districts.class);
            query.setParameter("archived", archived);
            return query.getResultList();
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception("Error Fetching District List.");
        }
    }

    public String findDistrictById(int district_id) throws Exception {
        try {
            return entityManager.createQuery(Constant.FIND_DISTRICT, String.class)
                    .setParameter("district_id", district_id)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception("Some exception occurred while fetching district by id.");
        }
    }

    public Integer findDistrictIdByName(String district_name) throws Exception {
        try {
            return entityManager.createQuery(Constant.FIND_DISTRICT_ID_BY_NAME, Integer.class)
                    .setParameter("district_name", district_name)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception("Some exception occurred while fetching district by name.");
        }
    }

    public String findStateById(int state_id) throws Exception {
        try {
            return entityManager.createQuery(Constant.FIND_STATE, String.class)
                    .setParameter("state_id", state_id)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception("Some exception occurred while fetching state by id.");
        }
    }

    public Integer findStateIdByName(String state_name) throws Exception {
        try {
            return entityManager.createQuery(Constant.FIND_STATE_ID_BY_NAME, Integer.class)
                    .setParameter("state_name", state_name)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception("Some exception occurred while fetching state by name.");
        }
    }


    public List<StateCode> findStateList(Boolean archived) throws Exception {
        try {
            TypedQuery<StateCode> query = entityManager.createQuery(Constant.GET_STATES_LIST, StateCode.class);
            query.setParameter("archived", archived);
            return query.getResultList();
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception("Some exception occurred while fetching state list.");
        }
    }

    public List<StateCode> findAllStateList() throws Exception {
        try {
            TypedQuery<StateCode> query = entityManager.createQuery(Constant.GET_STATES_LIST_ALL, StateCode.class);
            return query.getResultList();
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception("Some exception occurred while fetching state list.");
        }
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
                log.info("state is: {}", state);
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

    public Integer getCount() throws Exception {
        try {
            String query = "SELECT MAX(state_id) AS max_state_id FROM custom_state_codes";
            Query nquery = entityManager.createNativeQuery(query);
            return (Integer) nquery.getSingleResult();
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception("Some Exception Occurred while fetching state code count.");
        }
    }

    public Integer getCountDistrict() throws Exception {
        try {
            String query = "SELECT MAX(district_id) AS max_district_id FROM custom_districts";
            Query nquery = entityManager.createNativeQuery(query);
            return (Integer) nquery.getSingleResult();
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception("Some Exception Occurred while fetching district code count.");
        }
    }

    @Transactional
    public StateCode addState(StateCode stateCode) throws IllegalArgumentException, Exception {
        try {
            if (stateCode.getState_id() != null)
                throw new IllegalArgumentException("Cannot give state id when adding");
            stateCode.setState_id(getCount() + 1);
            if (stateCode.getState_name() == null || stateCode.getState_name().trim().isEmpty())
                throw new IllegalArgumentException("State name cannot be null or empty");
            if (stateCode.getState_code() == null || stateCode.getState_code().trim().isEmpty())
                throw new IllegalArgumentException("State code cannot be null or empty");
            if (!sharedUtilityService.isAlphabetic(stateCode.getState_name()))
                throw new IllegalArgumentException("State name should contain only alphabets");
            if (!sharedUtilityService.isAlphabetic(stateCode.getState_code()))
                throw new IllegalArgumentException("State code should contain only alphabets");
            List<StateCode> existingState = findAllStateList();
            for (StateCode stateCode1 : existingState) {
                if (stateCode1.getState_name().equalsIgnoreCase(stateCode.getState_name())) {
                    throw new IllegalArgumentException("State with this state name already exists");
                }
                if (stateCode1.getState_code().equalsIgnoreCase(stateCode.getState_code())) {
                    throw new IllegalArgumentException("State with this state code already exists");
                }
            }
            if (stateCode.getArchived() != null) {
                throw new IllegalArgumentException("Cannot provide archive status when adding a state");
            }
            stateCode.setArchived(false);
            stateCode.setIsState(true);
            entityManager.persist(stateCode);
            return stateCode;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    @Transactional
    public Districts addDistrict(Districts district, Integer stateId) throws IllegalArgumentException, Exception {
        try {
            if (district.getState_code() == null || district.getState_code().trim().isEmpty())
                throw new IllegalArgumentException("State code cannot be null or empty");
            StateCode stateCode = entityManager.find(StateCode.class, stateId);
            if (stateCode == null)
                throw new IllegalArgumentException("Invalid state id ,State not found");
            if (!stateCode.getState_code().equals(district.getState_code()))
                throw new IllegalArgumentException("State code " + district.getState_code() + " does not belong to state " + stateCode.getState_name());
            if (district.getDistrict_id() != null)
                throw new IllegalArgumentException("Cannot give district id when adding");
            district.setDistrict_id(getCountDistrict() + 1);

            if (district.getDistrict_name() == null || district.getDistrict_name().trim().isEmpty())
                throw new IllegalArgumentException("District name cannot be null or empty");

            if (!sharedUtilityService.isAlphabetic(district.getDistrict_name()))
                throw new IllegalArgumentException("District name should contain only alphabets");
            if (!sharedUtilityService.isAlphabetic(district.getState_code()))
                throw new IllegalArgumentException("State code should contain only alphabets");
            List<Districts> existingDistricts = findALLDistrictsByStateCode(stateCode.getState_code());
            for (Districts districts : existingDistricts) {
                if (districts.getDistrict_name().equalsIgnoreCase(district.getDistrict_name())) {
                    throw new IllegalArgumentException("District with this district name already exists in this state");
                }
            }
            if (district.getArchived() != null) {
                throw new IllegalArgumentException("Cannot provide archive status when adding a district");
            }
            district.setArchived(false);
            entityManager.persist(district);
            return district;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    @Transactional
    public StateCode editState(Integer stateId, StateCode stateCode) throws IllegalArgumentException, Exception {
        try {
            StateCode state = entityManager.find(StateCode.class, stateId);

            if (stateCode.getState_id() != null)
                throw new IllegalArgumentException("Cannot give state id when editing");
            if (stateCode.getState_name() != null) {
                if (stateCode.getState_name().trim().isEmpty()) {
                    throw new IllegalArgumentException("State name cannot be empty");
                }

                if (!sharedUtilityService.isAlphabetic(stateCode.getState_name())) {
                    throw new IllegalArgumentException("State name should contain only alphabets");
                }

                List<StateCode> existingState = findAllStateList();
                for (StateCode stateCode1 : existingState) {
                    if (stateCode1.getState_name().equalsIgnoreCase(stateCode.getState_name()) && !stateCode1.getState_id().equals(stateId)) {
                        throw new IllegalArgumentException("State with this state name already exists");
                    }
                }
                state.setState_name(stateCode.getState_name());
            }

            if (stateCode.getState_code() != null) {
                if (stateCode.getState_code().trim().isEmpty()) {
                    throw new IllegalArgumentException("State code cannot be empty");
                }
                if (!sharedUtilityService.isAlphabetic(stateCode.getState_code())) {
                    throw new IllegalArgumentException("State code should contain only alphabets");
                }
                List<StateCode> existingStateCodes = findStateList(false);
                for (StateCode stateCode1 : existingStateCodes) {
                    if (stateCode1.getState_code().equalsIgnoreCase(stateCode.getState_code()) && !stateCode1.getState_id().equals(stateId)) {
                        throw new IllegalArgumentException("State with this state code already exists");
                    }
                }
                state.setState_code(stateCode.getState_code());
            }
            if (stateCode.getArchived() != null) {
                throw new IllegalArgumentException("Cannot provide archive status when updating a state");
            }
            /*state.setArchived(false);*/
            entityManager.merge(state);
            return state;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    @Transactional
    public Districts editDistrict(Integer districtId, Districts district) throws IllegalArgumentException, Exception {
        try {
            Districts districtToEdit = entityManager.find(Districts.class, districtId);

            if (district.getDistrict_id() != null)
                throw new IllegalArgumentException("Cannot give district id when editing");
            String stateCode = null;
            if (district.getState_code() != null) {
                if (district.getState_code().trim().isEmpty())
                    throw new IllegalArgumentException("State code cannot be null or empty");
                if (!sharedUtilityService.isAlphabetic(district.getState_code()))
                    throw new IllegalArgumentException("State code should contain only alphabets");

                districtToEdit.setState_code(district.getState_code());
                stateCode = district.getState_code();
            } else {
                stateCode = districtToEdit.getState_code();
            }
            if (district.getDistrict_name() != null) {
                if (district.getDistrict_name().trim().isEmpty())
                    throw new IllegalArgumentException("District name cannot be null or empty");
                if (!sharedUtilityService.isAlphabetic(district.getDistrict_name()))
                    throw new IllegalArgumentException("District name should contain only alphabets");
                List<Districts> existingDistricts = findALLDistrictsByStateCode(stateCode);
                for (Districts districts : existingDistricts) {
                    if (districts.getDistrict_name().equalsIgnoreCase(district.getDistrict_name()) && !districts.getDistrict_id().equals(districtId)) {
                        throw new IllegalArgumentException("District with this district name already exists in this state");
                    }
                }
                districtToEdit.setDistrict_name(district.getDistrict_name());
            }

            if (district.getArchived() != null) {
                throw new IllegalArgumentException("Cannot provide archive status when updating a state");
            }
            entityManager.merge(districtToEdit);
            return districtToEdit;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    @Transactional
    public StateCode manageState(Integer stateId, Boolean archive) throws IllegalArgumentException, Exception {
        try {
            StateCode state = entityManager.find(StateCode.class, stateId);
            if (archive) {
                if (state.getArchived())
                    throw new IllegalArgumentException("State already archived");
                else {
                    state.setArchived(true);
                    Query query = entityManager.createQuery("SELECT d from Districts d where d.state_code = :code", Districts.class);
                    query.setParameter("code", state.getState_code());
                    List<Districts> districts = query.getResultList();
                    for (Districts district : districts) {
                        district.setArchived(true);
                        entityManager.merge(district);
                    }
                    entityManager.merge(state);
                }
            } else {
                if (!state.getArchived())
                    throw new IllegalArgumentException("State already unarchived");
                else {
                    state.setArchived(false);
                    Query query = entityManager.createQuery("SELECT d from Districts d where d.state_code = :code", Districts.class);
                    query.setParameter("code", state.getState_code());
                    List<Districts> districts = query.getResultList();
                    for (Districts district : districts) {
                        district.setArchived(false);
                        entityManager.merge(district);
                    }
                    entityManager.merge(state);
                }
            }
            return state;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    @Transactional
    public Districts manageDistrict(Integer districtId, Boolean archive) throws IllegalArgumentException, Exception {
        try {
            Districts district = entityManager.find(Districts.class, districtId);
            if (archive) {
                if (district.getArchived())
                    throw new IllegalArgumentException("District already archived");
                else {
                    district.setArchived(true);
                    entityManager.merge(district);
                }
            } else {
                if (!district.getArchived())
                    throw new IllegalArgumentException("District already unarchived");
                else {
                    district.setArchived(false);
                    entityManager.merge(district);
                }
            }
            return district;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

}
