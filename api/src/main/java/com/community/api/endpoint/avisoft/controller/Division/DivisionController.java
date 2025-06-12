package com.community.api.endpoint.avisoft.controller.Division;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.entity.Districts;
import com.community.api.entity.StateCode;
import com.community.api.entity.Zone;
import com.community.api.services.DistrictService;
import com.community.api.services.ResponseService;
import com.community.api.services.SharedUtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("division")
public class DivisionController {

    @Autowired
    EntityManager entityManager;
    @Autowired
    DistrictService districtService;
    @Autowired
    SharedUtilityService sharedUtilityService;

    /*@Authorize(value ={Constant.roleSuperAdmin})
    @RequestMapping(value = "add", method = RequestMethod.POST)
    public ResponseEntity<?> addDivision(@RequestBody Map<String,String> divisionDto) {
        try {
            if (divisionDto.get("division_name") == null)
                throw new IllegalArgumentException("Division name is required");
            Query query = entityManager.createQuery(Constant.GET_STATE_BY_STATE_NAME, StateCode.class);
            query.setParameter("state", divisionDto.get("division_name"));
            List<StateCode> stateCodes = query.getResultList();
            StateCode division = new StateCode();
            division.setIsState(false);
            division.setState_id(districtService.getCount() + 1);
            Long zoneId=Long.parseLong(divisionDto.get("zoneId"));
            Zone zone=entityManager.find(Zone.class,zoneId);
            if(zone==null)
                return ResponseService.generateErrorResponse("Zone does not exist",HttpStatus.NOT_FOUND);
      *//*      if (!stateCodes.isEmpty())
                throw new IllegalArgumentException("Division already exists");*//*

            Query queryTofindLinkage=entityManager.createNativeQuery("SELECT COUNT(*) FROM zone_divisions where zone_id = :zoneId and division_id = :divId");
            queryTofindLinkage.setParameter("zoneId",zone.getZoneId());
            queryTofindLinkage.setParameter("divId",stateCodes.get(0).getState_id());
            if(!queryTofindLinkage.getResultList().isEmpty())
            {
                throw new IllegalArgumentException("Division already exists in this zone");
            }
            Query queryTofindMaxDiv=entityManager.createNativeQuery("SELECT MAX(zonedivisionid) FROM zone_divisions where zonedivisionid <990");
            Long maxId= (Long) queryTofindMaxDiv.getSingleResult();
            queryTofindLinkage.setParameter("zoneId",zone.getZoneId());
            queryTofindLinkage.setParameter("divId",stateCodes.get(0).getState_id());
            if (divisionDto.get("division_code") == null)
                throw new IllegalArgumentException("Division code is required");
            String divName = divisionDto.get("division_name");
            String divCode = divisionDto.get("division_code");
            if (!sharedUtilityService.isAlphabetic(divCode))
                throw new IllegalArgumentException("Division code should contain only alphabets");
            division.setState_name(divName);
            division.setState_code(divCode);
            division.setArchived(false);
            entityManager.persist(division);
            Query queryToAddLinkage = entityManager.createNativeQuery(
                    "INSERT INTO zone_divisions (zonedivisionid, zone_id, division_id) VALUES (:zdid, :zid, :did)"
            );
            queryToAddLinkage.setParameter("zdid", maxId + 1);
            queryToAddLinkage.setParameter("zid", zoneId);
            queryToAddLinkage.setParameter("did", division.getState_id());

            int updateCount = queryToAddLinkage.executeUpdate();

            if (updateCount == 1) {
                return ResponseService.generateSuccessResponse("Division added successfully", divisionDto, HttpStatus.OK);
            } else {
                return ResponseService.generateErrorResponse("Cannot add division", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Cannot add division",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

    @Transactional
    @RequestMapping(value = "add", method = RequestMethod.POST)
    public ResponseEntity<?> addDivision(@RequestBody Map<String, String> divisionDto) {
        String divisionName = divisionDto.get("division_name");
        String divisionCode = divisionDto.get("division_code");
        String zoneIdStr = divisionDto.get("zone_id");

        if (divisionName == null || divisionName.trim().isEmpty())
            throw new IllegalArgumentException("Division name is required");
        if (divisionCode == null || divisionCode.trim().isEmpty())
            throw new IllegalArgumentException("Division code is required");
        if (!sharedUtilityService.isAlphabetic(divisionCode))
            throw new IllegalArgumentException("Division code should contain only alphabets");
        if (zoneIdStr == null)
            throw new IllegalArgumentException("Zone ID is required");

        Integer zoneId = Integer.parseInt(zoneIdStr);
        Zone zone = entityManager.find(Zone.class, zoneId);
        if (zone == null)
            return ResponseService.generateErrorResponse("Zone does not exist", HttpStatus.NOT_FOUND);

        StateCode newDivision = new StateCode();
        newDivision.setIsState(false);
        newDivision.setState_id(districtService.getCount() + 1);
        newDivision.setState_name(divisionName);
        newDivision.setState_code(divisionCode);
        newDivision.setArchived(false);

        entityManager.persist(newDivision);

        Query linkageCheck = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM zone_divisions z " +
                        "JOIN custom_state_codes s ON z.division_id = s.state_id " +
                        "WHERE z.zone_id = :zid AND s.state_name = :dn"
        );
        System.out.println(newDivision.getState_id());
        linkageCheck.setParameter("zid", zoneId);
        linkageCheck.setParameter("dn", newDivision.getState_name());
        Number count = (Number) linkageCheck.getSingleResult();

        if (count.intValue() > 0) {
            return ResponseService.generateErrorResponse("Division already linked to this zone", HttpStatus.BAD_REQUEST);
        }

        Query maxIdQuery = entityManager.createNativeQuery(
                "SELECT MAX(zonedivisionid) FROM zone_divisions WHERE zonedivisionid < 990"
        );
            BigInteger maxId = (BigInteger) maxIdQuery.getSingleResult();

        Query insertLinkage = entityManager.createNativeQuery(
                "INSERT INTO zone_divisions (zonedivisionid, zone_id, division_id) VALUES (:zdid, :zid, :did)"
        );
        insertLinkage.setParameter("zdid", maxId.intValue() + 1);
        insertLinkage.setParameter("zid", zoneId);
        insertLinkage.setParameter("did", newDivision.getState_id());

        int result = insertLinkage.executeUpdate();
        if (result == 1) {
            return ResponseService.generateSuccessResponse("Division added successfully", divisionDto, HttpStatus.OK);
        } else {
            throw new IllegalStateException("Failed to link division to zone");
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @Transactional
    @RequestMapping(value = "{divisionId}/edit", method = RequestMethod.POST)
    public ResponseEntity<?> editDivision(@PathVariable("divisionId") Integer divisionId,
                                          @RequestBody Map<String, String> divisionDto) {
        try {
            String newDivName = divisionDto.get("division_name");
            String newDivCode = divisionDto.get("division_code");
            String zoneIdStr = divisionDto.get("zone_id");

            if (newDivName == null || newDivName.trim().isEmpty())
                throw new IllegalArgumentException("Division name is required");
            if (newDivCode == null || newDivCode.trim().isEmpty())
                throw new IllegalArgumentException("Division code is required");
            if (!sharedUtilityService.isAlphabetic(newDivCode))
                throw new IllegalArgumentException("Division code should contain only alphabets");

            StateCode division = entityManager.find(StateCode.class, divisionId);
            if (division == null )
                return ResponseService.generateErrorResponse("Division not found", HttpStatus.NOT_FOUND);

            // Update name and code
            division.setState_name(newDivName);
            division.setState_code(newDivCode);
            entityManager.merge(division);

            // Update zone mapping if zone_id is provided
            if (zoneIdStr != null && !zoneIdStr.trim().isEmpty()) {
                Integer newZoneId = Integer.parseInt(zoneIdStr);
                Zone zone = entityManager.find(Zone.class, newZoneId);
                if (zone == null)
                    return ResponseService.generateErrorResponse("Zone does not exist", HttpStatus.NOT_FOUND);

                // Check if the mapping already exists for this name in the same zone
                Query linkageCheck = entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM zone_divisions z " +
                                "JOIN custom_state_codes s ON z.division_id = s.state_id " +
                                "WHERE z.zone_id = :zid AND s.state_name = :dn"
                );
                linkageCheck.setParameter("zid", newZoneId);
                linkageCheck.setParameter("dn", newDivName);
                Number count = (Number) linkageCheck.getSingleResult();

                if (count.intValue() == 0) {
                    // Delete old mappings
                    Query deleteOld = entityManager.createNativeQuery(
                            "DELETE FROM zone_divisions WHERE division_id = :did");
                    deleteOld.setParameter("did", divisionId);
                    deleteOld.executeUpdate();

                    // Get max zonedivisionid
                    Query maxIdQuery = entityManager.createNativeQuery(
                            "SELECT MAX(zonedivisionid) FROM zone_divisions WHERE zonedivisionid < 990");
                    BigInteger maxId = (BigInteger) maxIdQuery.getSingleResult();
                    if (maxId == null) maxId = BigInteger.ZERO;

                    // Insert new mapping
                    Query insertNew = entityManager.createNativeQuery(
                            "INSERT INTO zone_divisions (zonedivisionid, zone_id, division_id) VALUES (:zdid, :zid, :did)");
                    insertNew.setParameter("zdid", maxId.intValue() + 1);
                    insertNew.setParameter("zid", newZoneId);
                    insertNew.setParameter("did", divisionId);
                    insertNew.executeUpdate();
                }
            }

            return ResponseService.generateSuccessResponse("Division updated successfully", divisionDto, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Cannot update division", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value ={Constant.roleSuperAdmin})
    @Transactional
    @RequestMapping(value = "/{divisionId}/manage", method = RequestMethod.PUT)
    public ResponseEntity<?> manage(@PathVariable Integer divisionId,@RequestParam(defaultValue = "true") Boolean archive) {
        try {
            StateCode state =districtService.getStateByStateId(divisionId);
            if(state==null)
                return ResponseService.generateErrorResponse("Division not found",HttpStatus.BAD_REQUEST);
            return ResponseService.generateSuccessResponse("Division archive status alterd successfully in master data",districtService.manageState(divisionId,archive),HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse("Cannot archive Division : "+e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return ResponseService.generateErrorResponse("Cannot archive Division : "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/all-divisions")
    public ResponseEntity<?> getAll(@RequestParam(required = false,defaultValue = "false") Boolean archived) {
        try {

            Query query = entityManager.createQuery(
                    "SELECT s FROM StateCode s WHERE s.archived = :archived", StateCode.class
            );
            query.setParameter("archived", archived);
            return ResponseService.generateSuccessResponse("List of divisions",query.getResultList(),HttpStatus.OK);
        } catch (Exception exception) {
            return ResponseService.generateErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
