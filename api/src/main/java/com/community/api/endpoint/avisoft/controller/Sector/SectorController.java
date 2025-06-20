package com.community.api.endpoint.avisoft.controller.Sector;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.dto.AddSectorDto;
import com.community.api.dto.AdvertisementCompressedDTO;
import com.community.api.dto.CompressedProductWrapper;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomSector;
import com.community.api.services.ResponseService;
import com.community.api.services.SectorService;
import com.community.api.services.StaticDataService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.community.api.component.Constant.request;

@RestController
public class SectorController {

    private final ExceptionHandlingService exceptionHandlingService;
    private final SectorService sectorService;
    private final StaticDataService staticDataService;

    @Autowired
    public SectorController(ExceptionHandlingService exceptionHandlingService, SectorService sectorService, StaticDataService staticDataService) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.sectorService = sectorService;
        this.staticDataService = staticDataService;
    }

    @PostMapping("/add-sector")
    public ResponseEntity<?> addSubject(@RequestBody AddSectorDto addSectorDto, @RequestHeader(value = "Authorization") String authHeader) {
        try{
            if(!staticDataService.validiateAuthorization(authHeader)) {
                return ResponseService.generateErrorResponse("NOT AUTHORIZED TO ADD A SECTOR", HttpStatus.UNAUTHORIZED);
            }

            sectorService.validateAddSubjectDto(addSectorDto);
            sectorService.saveSector(addSectorDto);

            return ResponseService.generateSuccessResponse("SUCCESSFULLY ADDED", addSectorDto, HttpStatus.OK);
        }  catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/edit-sector/{sectorId}")
    public ResponseEntity<?> editSector(@PathVariable Long sectorId,@RequestBody AddSectorDto addSectorDto, @RequestHeader(value = "Authorization") String authHeader) {
        try{
            if(!staticDataService.validiateAuthorization(authHeader)) {
                return ResponseService.generateErrorResponse("NOT AUTHORIZED TO Edit A SECTOR", HttpStatus.UNAUTHORIZED);
            }
            sectorService.validateAddSubjectDto(addSectorDto);
            sectorService.edit(sectorId,addSectorDto);
            Map<String,Object>details=new HashMap<>();
            details.put("sector_id",sectorId);
            details.put("sector_name",addSectorDto.getSectorName());
            details.put("sector_description",addSectorDto.getSectorDescription());
            return ResponseService.generateSuccessResponse("SUCCESSFULLY EDITED", details, HttpStatus.OK);
        }  catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/get-all-sector")
    public ResponseEntity<?> getAllSubject(@RequestParam(defaultValue = "false",required = false) Boolean archived) {
        try {
            List<CustomSector> subjectList = sectorService.getAllSector(archived);
            if (subjectList.isEmpty()) {
                return ResponseService.generateErrorResponse("NO SECTOR FOUND", HttpStatus.OK);
            }
            return ResponseService.generateSuccessResponse("SECTOR FOUND", subjectList, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-sector-by-sector-id/{sectorId}")
    public ResponseEntity<?> getSubjectById(@PathVariable Long sectorId) {
        try {
            CustomSector sector = sectorService.getSectorBySectorId(sectorId);
            if (sector == null) {
                return ResponseService.generateErrorResponse("NO SECTOR FOUND", HttpStatus.OK);
            }
            return ResponseService.generateSuccessResponse("SECTORS FOUND", sector, HttpStatus.OK);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{sectorId}/manage")
    public ResponseEntity<?> manageSector(@PathVariable Long sectorId, @RequestParam (required = false,defaultValue = "true")Boolean archive) {
        try {
            if (sectorId==null)
                return ResponseService.generateErrorResponse("Sector id is required",HttpStatus.BAD_REQUEST);
            return ResponseService.generateSuccessResponse("Sector status altered",sectorService.manageSector(sectorId,archive), HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
   /* @Autowired
    EntityManager entityManager;
    @GetMapping("/get-products-by-sector-id/{sectorId}")
    public ResponseEntity<?>getProductsByAdvertisementId(@PathVariable Long sectorId)
    {
        CustomSector customSector=entityManager.find(CustomSector.class,sectorId);
        if(customSector==null)
            return ResponseService.generateErrorResponse("Sector not found",HttpStatus.BAD_REQUEST);
        List<Object[]> rows =sectorService.getCompressedProductsBySector(longList,offset,limit);
        System.out.println("res"+rows);
        BigInteger count=advertisementService.getAdvCompressedCount(longList);
        List<AdvertisementCompressedDTO>adv=new ArrayList<>();
        for (Object[] row : rows) {
            AdvertisementCompressedDTO dto=new AdvertisementCompressedDTO();
            dto.setAdvertisement_id(((BigInteger) row[0]).longValue());
            dto.setAdvertisementDesc((String) row[1]);
            dto.setAdvertisementTitle((String) row[2]);
            adv.add(dto);
            String productIdsStr = row[3].toString(); // row[3] contains comma-separated product IDs
            String[] productIdStrings = productIdsStr.split(",");

            for (String idStr : productIdStrings) {
                try {
                    Long id = Long.parseLong(idStr.trim());
                    CustomProduct product = entityManager.find(CustomProduct.class, id);
                    if (product != null) {
                        CompressedProductWrapper compressedProductWrapper=new CompressedProductWrapper();
                        compressedProductWrapper.wrapDetails(product,request,reserveCategoryService,reserveCategoryAgeService,genderService,customCustomer,sharedUtilityService);
                        dto.getProductList().add(compressedProductWrapper);
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid ID strings
                    System.err.println("Invalid product ID: " + idStr);
                }
            }
            *//* activeCategories.add(dto);*//*
        }
        int totalItems = count.intValue();
        int totalPages = (int) Math.ceil((double) totalItems / limit);
        int fromIndex = offset * limit;
        int toIndex = Math.min(fromIndex + limit, totalItems);

        if (fromIndex >= totalItems && offset != 0) {
            return ResponseService.generateErrorResponse("Page index out of range", HttpStatus.BAD_REQUEST);
        }
        // Construct paginated response
        Map<String, Object> response = new HashMap<>();
        response.put("advertisements", adv);
        response.put("totalItems", totalItems);
        response.put("totalPages", totalPages);
        response.put("currentPage", offset);
        return ResponseService.generateSuccessResponse("ADVERTISEMENT RETRIEVED SUCCESSFULLY", response, HttpStatus.OK);
    }
    }*/
}
