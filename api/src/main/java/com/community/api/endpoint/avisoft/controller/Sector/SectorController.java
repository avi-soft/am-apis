package com.community.api.endpoint.avisoft.controller.Sector;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.AddSectorDto;
import com.community.api.dto.CompressedProductWrapper;
import com.community.api.dto.SectorDTO;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomSector;
import com.community.api.services.GenderService;
import com.community.api.services.ReserveCategoryAgeService;
import com.community.api.services.ReserveCategoryService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.SectorService;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.StaticDataService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.community.api.component.Constant.request;

@RestController
public class SectorController {

    private final ExceptionHandlingService exceptionHandlingService;
    private final SectorService sectorService;
    private final StaticDataService staticDataService;
    @Autowired
    EntityManager entityManager;
    @Autowired
    ReserveCategoryService reserveCategoryService;
    @Autowired
    GenderService genderService;
    @Autowired
    JwtUtil jwtTokenUtil;
    @Autowired
    RoleService roleService;
    @Autowired
    ReserveCategoryAgeService reserveCategoryAgeService;
    @Autowired
    SharedUtilityService sharedUtilityService;
    @Autowired
    CatalogService catalogService;

    @Autowired
    public SectorController(ExceptionHandlingService exceptionHandlingService, SectorService sectorService, StaticDataService staticDataService) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.sectorService = sectorService;
        this.staticDataService = staticDataService;
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @PostMapping("/add-sector")
    public ResponseEntity<?> addSubject(@RequestBody AddSectorDto addSectorDto, @RequestHeader(value = "Authorization") String authHeader) {
        try {
            if (!staticDataService.validiateAuthorization(authHeader)) {
                return ResponseService.generateErrorResponse("NOT AUTHORIZED TO ADD A SECTOR", HttpStatus.FORBIDDEN);
            }

            sectorService.validateAddSubjectDto(addSectorDto);
            sectorService.addSector(addSectorDto);

            return ResponseService.generateSuccessResponse("SUCCESSFULLY ADDED", addSectorDto, HttpStatus.OK);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @PutMapping("/edit-sector/{sectorId}")
    public ResponseEntity<?> editSector(@PathVariable Long sectorId, @RequestBody AddSectorDto addSectorDto, @RequestHeader(value = "Authorization") String authHeader) {
        try {
            if (!staticDataService.validiateAuthorization(authHeader)) {
                return ResponseService.generateErrorResponse("NOT AUTHORIZED TO Edit A SECTOR", HttpStatus.FORBIDDEN);
            }
            sectorService.validateAddSubjectDto(addSectorDto);
            sectorService.editSector(sectorId, addSectorDto);
            Map<String, Object> details = new HashMap<>();
            details.put("sector_id", sectorId);
            details.put("sector_name", addSectorDto.getSectorName());
            details.put("sector_description", addSectorDto.getSectorDescription());
            return ResponseService.generateSuccessResponse("SUCCESSFULLY EDITED", details, HttpStatus.OK);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all-sector")
    public ResponseEntity<?> getAllSubject(@RequestParam(defaultValue = "false", required = false) Boolean archived) {
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
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @DeleteMapping("/{sectorId}/manage")
    public ResponseEntity<?> manageSector(@PathVariable Long sectorId, @RequestParam(required = false, defaultValue = "true") Boolean archive) {
        try {
            if (sectorId == null)
                return ResponseService.generateErrorResponse("Sector id is required", HttpStatus.BAD_REQUEST);
            return ResponseService.generateSuccessResponse("Sector status altered", sectorService.manageSector(sectorId, archive), HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-products-by-sectors")
    public ResponseEntity<?> getProductsByAdvertisementId(
            @RequestParam(value = "sectors", required = false) String sectors,
            @RequestParam(value = "limit", required = false, defaultValue = "30") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "name-only", required = false, defaultValue = "true") Boolean nameOnly,
            @RequestParam(value = "categoryId", required = false) List<Long> categoryId) {

        try {
            CustomCustomer customCustomer = null;
            String role = Constant.roleUser;

            if (authHeader != null) {
                String jwtToken = authHeader.substring(7);
                Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
                role = roleService.findRoleName(roleId);
                Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
                if (roleId == 5) {
                    customCustomer = entityManager.find(CustomCustomer.class, tokenUserId);
                }
            }
            if (categoryId != null) {
                for (Long id : categoryId) {
                    Category category = catalogService.findCategoryById(id);
                    if (category == null)
                        return ResponseService.generateErrorResponse("Category with id " + id + " not found", HttpStatus.BAD_REQUEST);
                }
            }
            List<Long> longList = new ArrayList<>();
            if (sectors != null) {
                longList = Arrays.stream(sectors.split(","))
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
            }

            // Ensure categoryId is not null
            if (categoryId == null) {
                categoryId = new ArrayList<>();
            }

            List<Object[]> rows = sectorService.getCompressedProductsBySector(longList, offset, limit, categoryId);
            BigInteger count = sectorService.getCompressedProductsBySectorCount(longList, categoryId);

            List<SectorDTO> adv = new ArrayList<>();
            for (Object[] row : rows) {
                SectorDTO dto = new SectorDTO();
                dto.setSectorId(((BigInteger) row[0]).longValue());
                dto.setSectorDescription((String) row[1]);
                dto.setSectorName((String) row[2]);
                adv.add(dto);

                if (!nameOnly) {
                    String productIdsStr = row[3].toString();
                    String[] productIdStrings = productIdsStr.split(",");

                    for (String idStr : productIdStrings) {
                        try {
                            Long id = Long.parseLong(idStr.trim());
                            CustomProduct product = entityManager.find(CustomProduct.class, id);
                            if (product != null) {
                                CompressedProductWrapper compressedProductWrapper = new CompressedProductWrapper();
                                compressedProductWrapper.wrapDetails(product, request, reserveCategoryService,
                                        reserveCategoryAgeService, genderService, customCustomer, sharedUtilityService);
                                dto.getProducts().add(compressedProductWrapper);
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid product ID: " + idStr);
                        }
                    }
                }
            }

            int totalItems = count.intValue();
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            int fromIndex = offset * limit;
            int toIndex = Math.min(fromIndex + limit, totalItems);

            if (fromIndex >= totalItems && offset != 0) {
                return ResponseService.generateErrorResponse("Page index out of range", HttpStatus.BAD_REQUEST);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("sectors", adv);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", offset);

            return ResponseService.generateSuccessResponse("SECTORS RETRIEVED SUCCESSFULLY", response, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            e.printStackTrace();
            return ResponseService.generateErrorResponse("Failed to retrieve sectors: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-products-by-sector-id")
    public ResponseEntity<?> getProductsByAdvertisementId(@RequestParam(value = "sectorId", required = true) Long sectorId
            , @RequestParam(value = "limit", required = false, defaultValue = "30") Integer limit
            , @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset
            , @RequestHeader(value = "Authorization", required = false) String authHeader
            , @RequestParam(value = "categoryId", required = false) List<Long> categoryId) {
        try {
            CustomCustomer customCustomer = null;
            String role = Constant.roleUser;
            if (authHeader != null) {
                String jwtToken = authHeader.substring(7);
                Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
                role = roleService.findRoleName(roleId);
                Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
                if (roleId == 5)
                    customCustomer = entityManager.find(CustomCustomer.class, tokenUserId);
            }
            if (categoryId != null) {
                for (Long id : categoryId) {
                    Category category = catalogService.findCategoryById(id);
                    if (category == null)
                        return ResponseService.generateErrorResponse("Category with id " + id + " not found", HttpStatus.BAD_REQUEST);
                }
            }
            CustomSector customSector = entityManager.find(CustomSector.class, sectorId);
            if (customSector == null)
                return ResponseService.generateErrorResponse("Sector not found", HttpStatus.BAD_REQUEST);
            BigInteger count = sectorService.getCompressedProductsCount(sectorId, categoryId);
            SectorDTO sectorDTO = new SectorDTO();
            sectorDTO.setSectorName(customSector.getSectorName());
            sectorDTO.setSectorDescription(customSector.getSectorDescription());
            sectorDTO.setSectorId(customSector.getSectorId());
            List<BigInteger> productIds = sectorService.getCompressedProductBySector(sectorId, offset, limit, categoryId);
            List<CompressedProductWrapper> products = new ArrayList<>();
            for (BigInteger productId : productIds) {
                CustomProduct product = entityManager.find(CustomProduct.class, productId.longValue());
                if (product != null) {
                    CompressedProductWrapper wrapper = new CompressedProductWrapper();
                    wrapper.wrapDetails(product, request, reserveCategoryService, reserveCategoryAgeService, genderService, customCustomer, sharedUtilityService);
                    products.add(wrapper);
                }
            }

            int totalItems = products.size();
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            int fromIndex = offset * limit;
            int toIndex = Math.min(fromIndex + limit, totalItems);

            products = products.subList(fromIndex, toIndex);
            sectorDTO.setProducts(products);

            if (fromIndex >= totalItems && offset != 0) {
                return ResponseService.generateErrorResponse("Page index out of range", HttpStatus.BAD_REQUEST);
            }
            // Construct paginated response
            Map<String, Object> response = new HashMap<>();
            response.put("sectors", sectorDTO);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", offset);
            return ResponseService.generateSuccessResponse("SECTORS RETRIEVED SUCCESSFULLY", response, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse("Failed to retrieve sectors by product id: " + exception.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
