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
            if (zone.getZoneName() == null || zone.getZoneName().trim().isEmpty()) {
                return ResponseService.generateErrorResponse("Zone name is required", HttpStatus.BAD_REQUEST);
            }

            // Check for existing zone name using CriteriaBuilder
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Zone> query = cb.createQuery(Zone.class);
            Root<Zone> root = query.from(Zone.class);
            query.select(root).where(cb.equal(root.get("zoneName"), zone.getZoneName()));

            List<Zone> existingZones = entityManager.createQuery(query).getResultList();

            if (!existingZones.isEmpty()) {
                return ResponseService.generateErrorResponse("Cannot add duplicate Zone", HttpStatus.BAD_REQUEST);
            }

            // Save the new zone
            entityManager.persist(zone);
            return ResponseService.generateSuccessResponse("Zone added successfully", zone, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace(); // For debugging; replace with logger in prod
            return ResponseService.generateErrorResponse("Cannot add zone: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @PostMapping("/{zoneId}/edit")
    public ResponseEntity<?> edit(@PathVariable Integer zoneId, @RequestBody Zone zone) {
        try {
            Zone zoneToBeEdited = entityManager.find(Zone.class, zoneId);
            if (zoneToBeEdited == null) {
                return ResponseService.generateErrorResponse("Zone not found", HttpStatus.NOT_FOUND);
            }

            if (zone.getZoneName() == null || zone.getZoneName().trim().isEmpty()) {
                return ResponseService.generateErrorResponse("Zone name is required", HttpStatus.BAD_REQUEST);
            }

            // Check if another zone with the same name exists
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Zone> query = cb.createQuery(Zone.class);
            Root<Zone> root = query.from(Zone.class);
            query.select(root).where(cb.equal(root.get("zoneName"), zone.getZoneName()));
            List<Zone> existingZones = entityManager.createQuery(query).getResultList();

            if (!existingZones.isEmpty() && !existingZones.get(0).getZoneId().equals(zoneId)) {
                return ResponseService.generateErrorResponse("Cannot edit: Zone with this name already exists", HttpStatus.BAD_REQUEST);
            }

            // Proceed with update
            zoneToBeEdited.setZoneName(zone.getZoneName());
            entityManager.merge(zoneToBeEdited);

            return ResponseService.generateSuccessResponse("Zone edited successfully", zoneToBeEdited, HttpStatus.OK);
        } catch (Exception exception) {
            exception.printStackTrace(); // optional
            return ResponseService.generateErrorResponse("Cannot edit zone", HttpStatus.INTERNAL_SERVER_ERROR);
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
