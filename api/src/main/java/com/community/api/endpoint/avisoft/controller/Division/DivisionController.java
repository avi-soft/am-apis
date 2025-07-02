package com.community.api.endpoint.avisoft.controller.Division;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.dto.DivisionRequest;
import com.community.api.dto.DivisionResponse;
import com.community.api.entity.Districts;
import com.community.api.entity.StateCode;
import com.community.api.entity.Zone;
import com.community.api.entity.ZoneDivisions;
import com.community.api.services.DistrictService;
import com.community.api.services.ResponseService;
import com.community.api.services.SharedUtilityService;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @PostMapping("/add")
    public ResponseEntity<?> addDivision(@Valid @RequestBody DivisionRequest request) {
        try {
            // 1. Validate zone exists and is active
            Zone zone = entityManager.createQuery(
                            "SELECT z FROM Zone z WHERE z.zoneId = :zoneId AND z.archived = false",
                            Zone.class)
                    .setParameter("zoneId", request.getZoneId())
                    .getSingleResult();

            // 2. Check for existing division name in the SAME ZONE using native query
            Query duplicateCheck = entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM zone_divisions zd " +
                            "JOIN custom_state_codes d ON zd.division_id = d.state_id " +
                            "WHERE zd.zone_id = :zoneId " +
                            "AND LOWER(d.state_name) = LOWER(:name)");

            duplicateCheck.setParameter("zoneId", request.getZoneId());
            duplicateCheck.setParameter("name", request.getDivisionName());

            int duplicateCount = ((Number)duplicateCheck.getSingleResult()).intValue();

            if (duplicateCount > 0) {
                return ResponseService.generateErrorResponse(
                        "Division with this name already exists in the specified zone",
                        HttpStatus.CONFLICT);
            }
/*

            // 3. Check for division name uniqueness GLOBALLY (case-insensitive)
            boolean duplicateGlobally = entityManager.createQuery(
                            "SELECT COUNT(d) > 0 FROM StateCode d " +
                                    "WHERE LOWER(d.state_name) = LOWER(:name)",
                            Boolean.class)
                    .setParameter("name", request.getDivisionName())
                    .getSingleResult();

            if (duplicateGlobally) {
                return ResponseService.generateErrorResponse(
                        "Division name must be globally unique",
                        HttpStatus.CONFLICT);
            }
*/

            // 4. Check for division code uniqueness (case-insensitive)
            boolean codeExists = entityManager.createQuery(
                            "SELECT COUNT(d) > 0 FROM StateCode d " +
                                    "WHERE LOWER(d.state_code) = LOWER(:code)",
                            Boolean.class)
                    .setParameter("code", request.getDivisionCode())
                    .getSingleResult();

            if (codeExists) {
                return ResponseService.generateErrorResponse(
                        "Division code must be unique",
                        HttpStatus.CONFLICT);
            }

            // 5. Create and persist new division
            StateCode division = new StateCode();
            division.setState_name(request.getDivisionName());
            division.setState_id(getCount()+1);
            division.setState_code(request.getDivisionCode().toUpperCase());
            division.setIsState(false);
            division.setArchived(false);
            entityManager.persist(division);

            // 6. Get next zonedivisionid
            Query maxIdQuery = entityManager.createNativeQuery(
                    "SELECT MAX(zonedivisionid) FROM zone_divisions");
            BigInteger maxId = (BigInteger) maxIdQuery.getSingleResult();
            int newZoneDivisionId = (maxId != null) ? maxId.intValue() + 1 : 1;

            // 7. Create zone-division linkage using native query
            Query linkageQuery = entityManager.createNativeQuery(
                    "INSERT INTO zone_divisions (zonedivisionid, zone_id, division_id) " +
                            "VALUES (:zoneDivId, :zoneId, :divId)");

            linkageQuery.setParameter("zoneDivId", newZoneDivisionId);
            linkageQuery.setParameter("zoneId", request.getZoneId());
            linkageQuery.setParameter("divId", division.getState_id());
            linkageQuery.executeUpdate();

            request.setZoneName(zone.getZoneName());
            return ResponseService.generateSuccessResponse(
                    "Division created and linked to zone successfully",
                    request,
                    HttpStatus.CREATED);

        } catch (NoResultException e) {
            return ResponseService.generateErrorResponse(
                    "Specified zone not found or is archived",
                    HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseService.generateErrorResponse(
                    "Division creation failed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public Integer getCount()
    {
        String query ="SELECT MAX(state_id) AS max_state_id FROM custom_state_codes";
        Query nquery=entityManager.createNativeQuery(query);
        return (Integer)nquery.getSingleResult();
    }

    @Transactional
    @PutMapping("/{divisionId}/edit")
    public ResponseEntity<?> editDivision(
            @PathVariable Integer divisionId,
            @RequestBody Map<String, String> requestBody,
            @RequestParam(required = false) List<Integer> zoneIds) {  // Changed to List<Integer>

        try {
            // 1. Validate division exists and is active
            StateCode division = entityManager.createQuery(
                            "SELECT d FROM StateCode d WHERE d.state_id = :divisionId AND d.archived = false",
                            StateCode.class)
                    .setParameter("divisionId", divisionId)
                    .getSingleResult();


            // 2. Get current zone linkages
            List<Integer> currentZoneIds = entityManager.createNativeQuery(
                            "SELECT zone_id FROM zone_divisions WHERE division_id = :divisionId")
                    .setParameter("divisionId", divisionId)
                    .getResultList();

            // Get current zone linkage
            Query currentZoneQuery = entityManager.createNativeQuery(
                    "SELECT zone_id FROM zone_divisions " +
                            "WHERE division_id = :divisionId");
            currentZoneQuery.setParameter("divisionId", divisionId);
            /*Integer currentZoneId = (Integer) currentZoneQuery.getSingleResult();
            Zone currentZone = entityManager.find(Zone.class, currentZoneId);*/
            Integer currentZoneId=null;
           zoneIds = currentZoneQuery.getResultList();

            Zone currentZone = null;
            if (!zoneIds.isEmpty()) {
                currentZone = entityManager.find(Zone.class, zoneIds.get(0));
                currentZoneId = zoneIds.get(0);
            }


            List<Zone> currentZones = entityManager.createQuery(
                            "SELECT z FROM Zone z WHERE z.zoneId IN :zoneIds", Zone.class)
                    .setParameter("zoneIds", currentZoneIds)
                    .getResultList();

            // 3. Process division name update if provided
            if (requestBody.containsKey("division_name")) {
                String newName = requestBody.get("division_name").trim();
                if (newName.isEmpty()) {
                    return ResponseService.generateErrorResponse("Division name cannot be empty", HttpStatus.BAD_REQUEST);
                }

                // Check for duplicate name in any of the current or new zones
                List<Integer> targetZoneIds = zoneIds != null ? zoneIds : currentZoneIds;
                if (targetZoneIds != null && !targetZoneIds.isEmpty()) {
                    Query nameCheck = entityManager.createNativeQuery(
                            "SELECT COUNT(*) FROM zone_divisions zd " +
                                    "JOIN custom_state_codes d ON zd.division_id = d.state_id " +
                                    "WHERE zd.zone_id IN :zoneIds AND LOWER(d.state_name) = LOWER(:name) AND d.state_id != :divisionId");
                    nameCheck.setParameter("zoneIds", targetZoneIds);
                    nameCheck.setParameter("name", newName);
                    nameCheck.setParameter("divisionId", divisionId);

                    if (((Number)nameCheck.getSingleResult()).intValue() > 0) {
                        return ResponseService.generateErrorResponse(
                                "Division with this name already exists in one of the target zones",
                                HttpStatus.CONFLICT);
                    }
                }
                division.setState_name(newName);
            }

            // 4. Process division code update if provided
            if (requestBody.containsKey("division_code")) {
                String newCode = requestBody.get("division_code").trim().toUpperCase();
                if (!newCode.matches("^[A-Z]+$")) {
                    return ResponseService.generateErrorResponse(
                            "Division code should contain only alphabets",
                            HttpStatus.BAD_REQUEST);
                }

                Query codeCheck = entityManager.createQuery(
                        "SELECT COUNT(d) FROM StateCode d WHERE d.state_code = :code AND d.state_id != :divisionId",
                        Long.class);
                codeCheck.setParameter("code", newCode);
                codeCheck.setParameter("divisionId", divisionId);

                if ((Long)codeCheck.getSingleResult()> 0L) {
                    return ResponseService.generateErrorResponse(
                            "Division code must be unique across all zones",
                            HttpStatus.CONFLICT);
                }
                division.setState_code(newCode);
            }

            // 5. Process zone updates if zoneIds parameter provided
            if (zoneIds != null) {
                // Remove existing zone associations
                Query deleteQuery = entityManager.createNativeQuery(
                        "DELETE FROM zone_divisions WHERE division_id = :divisionId");
                deleteQuery.setParameter("divisionId", divisionId);
                deleteQuery.executeUpdate();

                // Add new zone associations
                if (!zoneIds.isEmpty()) {
                    // Validate all new zones exist and are active
                    List<Zone> newZones = entityManager.createQuery(
                                    "SELECT z FROM Zone z WHERE z.zoneId IN :zoneIds AND z.archived = false", Zone.class)
                            .setParameter("zoneIds", zoneIds)
                            .getResultList();

                    if (newZones.size() != zoneIds.size()) {
                        return ResponseService.generateErrorResponse(
                                "One or more zones not found or are archived",
                                HttpStatus.NOT_FOUND);
                    }

                    // Create new associations
                    int sequence = 1;
                    for (Zone zone : newZones) {
                        Query insertQuery = entityManager.createNativeQuery(
                                "INSERT INTO zone_divisions (zonedivisionid, zone_id, division_id) " +
                                        "VALUES (:id, :zoneId, :divId)");
                        insertQuery.setParameter("id", sequence++);
                        insertQuery.setParameter("zoneId", zone.getZoneId());
                        insertQuery.setParameter("divId", divisionId);
                        insertQuery.executeUpdate();
                    }
                }
            }

            // 6. Save all changes
            entityManager.merge(division);

            // 7. Get updated zone information for response
            List<Zone> updatedZones = Collections.emptyList();
            if (zoneIds != null || !currentZoneIds.isEmpty()) {
                List<Integer> finalZoneIds = zoneIds != null ? zoneIds : currentZoneIds;
                updatedZones = entityManager.createQuery(
                                "SELECT z FROM Zone z WHERE z.zoneId IN :zoneIds", Zone.class)
                        .setParameter("zoneIds", finalZoneIds)
                        .getResultList();
            }

            // 8. Build response
            Map<String, Object> response = new HashMap<>();
            response.put("divisionId", division.getState_id());
            response.put("divisionName", division.getState_name());
            response.put("divisionCode", division.getState_code());
            response.put("zoneIds", updatedZones.stream().map(Zone::getZoneId).collect(Collectors.toList()));
            response.put("zoneNames", updatedZones.stream().map(Zone::getZoneName).collect(Collectors.toList()));

            return ResponseService.generateSuccessResponse(
                    zoneIds != null ? "Division and zone linkages updated successfully" : "Division updated successfully",
                    response,
                    HttpStatus.OK);

        } catch (NoResultException e) {
            return ResponseService.generateErrorResponse("Division not found or is archived", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseService.generateErrorResponse(
                    "Failed to update division: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private ResponseEntity<?> buildSuccessResponse(StateCode division, Zone zone, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("divisionId", division.getState_id());
        response.put("divisionName", division.getState_name());
        response.put("zoneId", zone != null ? zone.getZoneId() : null);
        response.put("zoneName", zone != null ? zone.getZoneName() : null);

        return ResponseService.generateSuccessResponse(
                message,
                response,
                HttpStatus.OK);
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
    public ResponseEntity<?> getAllDivisionsWithZones(
            @RequestParam(required = false, defaultValue = "false") Boolean archived) {
        try {
            // 1. Get all divisions
            List<StateCode> divisions = entityManager.createQuery(
                            "SELECT s FROM StateCode s WHERE s.archived = :archived", StateCode.class)
                    .setParameter("archived", archived)
                    .getResultList();

            // 2. Create response DTOs with zone information
            List<DivisionResponse> responseList = new ArrayList<>();

            for (StateCode division : divisions) {
                DivisionResponse response = new DivisionResponse();
                response.setDivisionId(division.getState_id());
                response.setDivisionName(division.getState_name());
                response.setDivisionCode(division.getState_code());
                response.setArchived(division.getArchived());

                // Get zone information for each division
                Query zoneQuery = entityManager.createNativeQuery(
                        "SELECT z.zone_id, z.zone_name FROM zone_divisions zd " +
                                "JOIN custom_zones z ON zd.zone_id = z.zone_id " +
                                "WHERE zd.division_id = :divisionId");
                zoneQuery.setParameter("divisionId", division.getState_id());

                // Handle multiple zone associations (if applicable)
                List<Object[]> zoneResults = zoneQuery.getResultList();
                if (!zoneResults.isEmpty()) {
                    // Take the first zone association (or implement logic for multiple zones)
                    Object[] firstZone = zoneResults.get(0);
                    response.setZoneId((Integer) firstZone[0]);
                    response.setZoneName((String) firstZone[1]);
                } else {
                    response.setZoneId(null);
                    response.setZoneName(null);
                }

                responseList.add(response);
            }

            return ResponseService.generateSuccessResponse(
                    "List of divisions with zone information",
                    responseList,
                    HttpStatus.OK);

        } catch (Exception e) {
            return ResponseService.generateErrorResponse(
                    "Failed to retrieve divisions: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
