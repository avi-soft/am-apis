package com.community.api.endpoint.avisoft.controller.Admin;

import com.community.api.entity.Zone;
import com.community.api.services.ResponseService;
import com.community.api.services.ZoneDivisionService;
import io.swagger.models.auth.In;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.List;

@Controller
@RequestMapping("/zone")
public class ZoneDivisionController {

    @Autowired
    private ZoneDivisionService zoneDivisionService;
    @Autowired
    private EntityManager entityManager;
    @GetMapping("/divisions/{zoneId}")
    public ResponseEntity<?> getDivisionByZone(@PathVariable Integer zoneId) {
        try {
            return ResponseService.generateSuccessResponse("Divisions : ", zoneDivisionService.getDivisionsByZoneId(zoneId), HttpStatus.OK);
        } catch (NoResultException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping("/all-zones")
    public ResponseEntity<?> getDivisionByZone(@RequestParam(required = false,defaultValue = "false") Boolean archived) {
        try {
            return ResponseService.generateSuccessResponse("All Zones:", zoneDivisionService.getAllZones(archived), HttpStatus.OK);
        } catch (Exception exception) {
            return ResponseService.generateErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/findByDivision/{divisionId}")
    public ResponseEntity<?> getLInkedZone(@PathVariable Integer divisionId)
    {
        try{
            return ResponseService.generateSuccessResponse("Linked Zone : ",zoneDivisionService.findDivisionsLinkedZone(divisionId),HttpStatus.OK);
        } catch (NotFoundException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (NoResultException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.OK);
        }
    }

    @Transactional
    @PostMapping("/add")
    public ResponseEntity<?> addResponse(@RequestBody Zone zone) {
        try {
            // Validate zone name
            if (zone.getZoneName() == null || zone.getZoneName().trim().isEmpty()) {
                return ResponseService.generateErrorResponse("Zone name is required", HttpStatus.BAD_REQUEST);
            }

            // Case-insensitive duplicate check using CriteriaBuilder
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Zone> query = cb.createQuery(Zone.class);
            Root<Zone> root = query.from(Zone.class);

            // Case-insensitive comparison using LOWER()
            Predicate namePredicate = cb.equal(
                    cb.lower(root.get("zoneName")),
                    zone.getZoneName().toLowerCase()
            );
            query.select(root).where(namePredicate);

            List<Zone> existingZones = entityManager.createQuery(query).getResultList();

            if (!existingZones.isEmpty()) {
                return ResponseService.generateErrorResponse("Zone with this name already exists", HttpStatus.CONFLICT);
            }

            // Additional validation
            if (zone.getZoneName().length() > 100) {
                return ResponseService.generateErrorResponse("Zone name must be 100 characters or less", HttpStatus.BAD_REQUEST);
            }

            // Set default values if needed
            if (zone.getArchived() == null) {
                zone.setArchived(false);
            }

            // Save the new zone
            entityManager.persist(zone);
            return ResponseService.generateSuccessResponse("Zone added successfully", zone, HttpStatus.CREATED);

        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Failed to add zone: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @PutMapping("/{zoneId}")  // Changed to PUT as it's more semantically correct for updates
    public ResponseEntity<?> editZone(
            @PathVariable Integer zoneId,
            @RequestBody  Zone zoneUpdateRequest) {  // Added @Valid for validation

        try {
            // 1. Find existing zone
            Zone existingZone = entityManager.find(Zone.class, zoneId);
            if (existingZone == null) {
                return ResponseService.generateErrorResponse("Zone not found", HttpStatus.NOT_FOUND);
            }

            // 2. Validate input
            if (zoneUpdateRequest.getZoneName() == null || zoneUpdateRequest.getZoneName().trim().isEmpty()) {
                return ResponseService.generateErrorResponse("Zone name is required", HttpStatus.BAD_REQUEST);
            }

            // 3. Case-insensitive duplicate check (excluding current zone)
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Zone> query = cb.createQuery(Zone.class);
            Root<Zone> root = query.from(Zone.class);

            Predicate namePredicate = cb.equal(
                    cb.lower(root.get("zoneName")),
                    zoneUpdateRequest.getZoneName().toLowerCase()
            );
            Predicate notCurrentZone = cb.notEqual(root.get("zoneId"), zoneId);

            query.select(root).where(cb.and(namePredicate, notCurrentZone));

            List<Zone> conflictingZones = entityManager.createQuery(query).getResultList();

            if (!conflictingZones.isEmpty()) {
                return ResponseService.generateErrorResponse(
                        "Zone with this name already exists",
                        HttpStatus.CONFLICT);
            }

            // 4. Additional validations
            if (zoneUpdateRequest.getZoneName().length() > 100) {
                return ResponseService.generateErrorResponse(
                        "Zone name must be 100 characters or less",
                        HttpStatus.BAD_REQUEST);
            }

            // 5. Update only allowed fields
            existingZone.setZoneName(zoneUpdateRequest.getZoneName());

         /*   // Add other updatable fields as needed
            if (zoneUpdateRequest.ge() != null) {
                existingZone.setDescription(zoneUpdateRequest.getDescription());
            }*/

            // 6. Save changes
            entityManager.merge(existingZone);

            return ResponseService.generateSuccessResponse(
                    "Zone updated successfully",
                    existingZone,
                    HttpStatus.OK);

        } catch (Exception e) {
            return ResponseService.generateErrorResponse(
                    "Failed to update zone: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @PutMapping("/{zoneId}/manage")
    public ResponseEntity<?>edit(@PathVariable Integer zoneId,@RequestParam(required = false,defaultValue = "true")Boolean archived)
    {
        try
        {
            Zone zoneToBeEdited=entityManager.find(Zone.class,zoneId);
            if(zoneToBeEdited==null)
                return ResponseService.generateErrorResponse("Zone not found",HttpStatus.NOT_FOUND);
            if(archived)
            {
                if(zoneToBeEdited.getArchived())
                    return ResponseService.generateErrorResponse("Zone already archived",HttpStatus.BAD_REQUEST);
            }
            else
            {
                if(!zoneToBeEdited.getArchived())
                    return ResponseService.generateErrorResponse("Zone already unarchived",HttpStatus.BAD_REQUEST);
            }
            zoneToBeEdited.setArchived(archived);
            entityManager.merge(zoneToBeEdited);
            return ResponseService.generateSuccessResponse("Zone status altered successfully",zoneToBeEdited,HttpStatus.OK);
        }
        catch (Exception exception)
        {
            return ResponseService.generateErrorResponse("Cannot alter zone status",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
