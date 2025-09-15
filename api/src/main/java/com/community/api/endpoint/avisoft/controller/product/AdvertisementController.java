package com.community.api.endpoint.avisoft.controller.product;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.AddAdvertisementDto;
import com.community.api.dto.AdvertisementCompressedDTO;
import com.community.api.dto.AdvertisementProductWrapper;
import com.community.api.dto.AdvertisementWrapper;
import com.community.api.dto.CompressedProductWrapper;
import com.community.api.dto.CustomAdvertisementProductWrapper;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.entity.Advertisement;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.Role;
import com.community.api.services.AdvertisementService;
import com.community.api.services.GenderService;
import com.community.api.services.ProductService;
import com.community.api.services.ReserveCategoryAgeService;
import com.community.api.services.ReserveCategoryService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.CategoryImpl;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.community.api.component.Constant.SOME_EXCEPTION_OCCURRED;
import static com.community.api.component.Constant.request;
import static com.community.api.services.ProductService.stripTime;

@RestController
@RequestMapping(value = "/advertisement", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
public class AdvertisementController {

    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Autowired
    AdvertisementService advertisementService;

    @Autowired
    ProductService productService;

    @Autowired
    CatalogService catalogService;

    @Autowired
    RoleService roleService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    SharedUtilityService sharedUtilityService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    JwtUtil jwtTokenUtil;

    @Autowired
    private ReserveCategoryService reserveCategoryService;
    @Autowired
    private ReserveCategoryAgeService reserveCategoryAgeService;
    @Autowired
    private GenderService genderService;

    @PostMapping("/add/{categoryIdString}")
    @Authorize(value = {Constant.roleAdmin, Constant.roleSuperAdmin, Constant.roleServiceProvider})
    public ResponseEntity<?> addAdvertisement(@RequestBody AddAdvertisementDto addAdvertisementDto,
                                              @PathVariable String categoryIdString,
                                              @RequestHeader(value = "Authorization") String authHeader) {
        try {
            Long categoryId = Long.parseLong(categoryIdString);
            Category category = advertisementService.validateSubCategory(categoryId);
            advertisementService.validateAdvertisement(addAdvertisementDto);

            Role role = productService.getRoleByToken(authHeader);
            Long creatorUserId = productService.getUserIdByToken(authHeader);

            Advertisement advertisement = advertisementService.saveAdvertisement(addAdvertisementDto, creatorUserId, role, (CategoryImpl) category);

            AdvertisementWrapper wrapper = new AdvertisementWrapper();
            wrapper.wrapDetails(advertisement, null);

            return ResponseService.generateSuccessResponse("Advertisement Created Successfully", wrapper, HttpStatus.OK);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            exceptionHandlingService.handleException(dataIntegrityViolationException);
            return ResponseService.generateErrorResponse(dataIntegrityViolationException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{advertisementId}")
    @Authorize(value = {Constant.roleAdmin, Constant.roleSuperAdmin, Constant.roleAdminServiceProvider, Constant.roleServiceProvider})
    public ResponseEntity<?> updateAdvertisement(@RequestBody AddAdvertisementDto addAdvertisementDto, @PathVariable Long advertisementId, @RequestHeader(value = "Authorization") String authHeader) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            String roleName = roleService.findRoleName(roleId);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);

            Advertisement advertisement = advertisementService.updateAdvertisement(addAdvertisementDto, advertisementId);
            if (roleName.equals(Constant.roleServiceProvider) && (!advertisement.getUserId().equals(tokenUserId))) {
                return ResponseService.generateErrorResponse("Not Authorized to update advertisement", HttpStatus.FORBIDDEN);
            }
            if (advertisement.getArchived() != 'Y') {
                List<CustomProductWrapper> products = new ArrayList<>();

                List<CustomProduct> customProducts = productService.getAllProductsByAdvertisementId(advertisement);
                for (CustomProduct customProduct : customProducts) {

                    if (customProduct != null && (((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date()))) {
                        CustomProductWrapper wrapper = new CustomProductWrapper();
                        wrapper.wrapDetails(customProduct, null, reserveCategoryService, reserveCategoryAgeService, genderService, null, sharedUtilityService);
                        products.add(wrapper);
                    }
                }
            }
            AdvertisementWrapper wrapper = new AdvertisementWrapper();
            wrapper.wrapDetails(advertisement, null);

            return ResponseService.generateSuccessResponse("Advertisement Updated Successfully", wrapper, HttpStatus.OK);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            exceptionHandlingService.handleException(dataIntegrityViolationException);
            return ResponseService.generateErrorResponse(dataIntegrityViolationException.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/get-advertisement-by-id/{advertisementId}")
    public ResponseEntity<?> retrieveAdvertisementById(HttpServletRequest request, @PathVariable("advertisementId") String advertisementIdPath, @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            CustomCustomer customCustomer = null;
            String role = Constant.roleUser;
            if (authHeader != null) {
                String jwtToken = authHeader.substring(7);
                Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
                Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
                role = roleService.findRoleName(roleId);
                if (roleId == 5)
                    customCustomer = entityManager.find(CustomCustomer.class, tokenUserId);
            }
            Long advertisementId = Long.parseLong(advertisementIdPath);
            if (advertisementId <= 0) {
                return ResponseService.generateErrorResponse("ADVERTISEMENT ID CANNOT BE <= 0", HttpStatus.BAD_REQUEST);
            }

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Advertisement advertisement = entityManager.find(Advertisement.class, advertisementId);

            if (advertisement == null) {
                return ResponseService.generateErrorResponse("Advertisement Not Found", HttpStatus.BAD_REQUEST);
            }

            if (advertisement.getArchived() != 'Y') {
                List<CustomProductWrapper> products = new ArrayList<>();

                List<CustomProduct> customProducts = productService.getAllProductsByAdvertisementId(advertisement);
                for (CustomProduct customProduct : customProducts) {
                    if (role.equalsIgnoreCase(Constant.roleUser)) {
                        if (customProduct != null &&
                                ((Status) customProduct).getArchived() != 'Y' &&
                                customProduct.getDefaultSku().getActiveEndDate().after(new Date()) &&
                                !customProduct.getGoLiveDate().after(new Date()) &&
                                !customProduct.getProductState().getProductState().equalsIgnoreCase(Constant.PRODUCT_STATE_DRAFT) &&
                                customProduct.getIsApproved()) {
                            CustomProductWrapper wrapper = new CustomProductWrapper();
                            wrapper.wrapDetails(customProduct, null, reserveCategoryService, reserveCategoryAgeService, genderService, customCustomer, sharedUtilityService);
                            products.add(wrapper);
                        }
                    } else {
                        if (customProduct != null && (((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date()))
                                && !customProduct.getProductState().getProductState().equalsIgnoreCase(Constant.PRODUCT_STATE_DRAFT) && customProduct.getGoLiveDate().before(new Date())) {
                            CustomProductWrapper wrapper = new CustomProductWrapper();
                            wrapper.wrapDetails(customProduct, null, reserveCategoryService, reserveCategoryAgeService, genderService, customCustomer, sharedUtilityService);
                            products.add(wrapper);
                        }
                    }

                }
                AdvertisementWrapper wrapper = new AdvertisementWrapper();
                wrapper.wrapDetails(advertisement, products, null);

                return ResponseService.generateSuccessResponse("ADVERTISEMENT FOUND", wrapper, HttpStatus.OK);

            } else {
                return ResponseService.generateErrorResponse("ADVERTISEMENT IS ARCHIVED", HttpStatus.OK);
            }

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleAdmin, Constant.roleSuperAdmin, Constant.roleAdminServiceProvider, Constant.roleServiceProvider})
    @GetMapping("/get-filter-advertisement")
    public ResponseEntity<?> getFilterAdvertisements(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "creatorId", required = false) Long creatorId,
            @RequestParam(value = "category", required = false) List<Long> categories,
            @RequestParam(value = "subCategory", required = false) List<Long> subCategories,
            @RequestParam(value = "all", required = false, defaultValue = "false") Boolean all,
            @RequestParam(value = "preview", required = false, defaultValue = "false") Boolean preview,
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(required = false, defaultValue = "DESC") String sortOrder,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "30") int limit, @RequestHeader(value = "Authorization") String authHeader) {

        try {

            if (offset < 0) {
                throw new IllegalArgumentException("Offset for pagination cannot be a negative number");
            }
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit for pagination cannot be a negative number or 0");
            }
            List<Advertisement> advertisements = advertisementService.filterAdvertisements(title, categories, subCategories, creatorId, all);
            if (preview) {
                String jwtToken = authHeader.substring(7);
                Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
                String roleName = roleService.findRoleName(roleId);
                Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
                if (roleName.equals(Constant.roleServiceProvider)) {
                    if (!creatorId.equals(tokenUserId))
                        return ResponseService.generateErrorResponse("Forbidden Access", HttpStatus.FORBIDDEN);
                    if (id != null) {
                        Query query = entityManager.createNativeQuery("SELECT Count(advertisement_id) from advertisement where creator_user_id = :id and advertisement_id = :aid");
                        query.setParameter("id", tokenUserId);
                        query.setParameter("aid", id);
                        BigInteger count = (BigInteger) query.getSingleResult();
                        if (count.intValue() == 0)
                            return ResponseService.generateErrorResponse("Forbidden Access", HttpStatus.FORBIDDEN);
                    }
                }
            }
            if (advertisements.isEmpty()) {
                return ResponseService.generateSuccessResponse("NO ADVERTISEMENT FOUND WITH THE GIVEN CRITERIA", advertisements, HttpStatus.OK);
            }
            List<AdvertisementWrapper> responses = new ArrayList<>();
            if (id != null) {
                Advertisement advertisement = entityManager.find(Advertisement.class, id);
                if (advertisement != null) {
                    AdvertisementWrapper wrapper = new AdvertisementWrapper();
                    if (advertisement.getArchived().equals('Y'))
                        return ResponseService.generateErrorResponse("Advertisement not found", HttpStatus.NOT_FOUND);
                    wrapper.wrapDetails(advertisement, null, null);
                    responses.add(wrapper);
                    Map<String, Object> response = new HashMap<>();
                    response.put("advertisements", responses);
                    response.put("totalItems", 1);
                    response.put("totalPages", 1);
                    response.put("currentPage", 1);
                    return ResponseService.generateSuccessResponse("ADVERTISEMENT RETRIEVED SUCCESSFULLY", response, HttpStatus.OK);
                } else
                    return ResponseService.generateErrorResponse("Advertisement not found", HttpStatus.NOT_FOUND);
            }

            if ("ASC".equalsIgnoreCase(sortOrder)) {
                advertisements.sort(
                        Comparator.comparing(
                                Advertisement::getModifiedDate,
                                Comparator.nullsFirst(Comparator.naturalOrder())
                        )
                );
            } else {
                advertisements.sort(
                        Comparator.comparing(
                                Advertisement::getModifiedDate,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
                );
            }
            for (Advertisement advertisement : advertisements) {
                if (advertisement == null) {
                    return ResponseService.generateErrorResponse("Advertisement Not Found", HttpStatus.BAD_REQUEST);
                }
                AdvertisementWrapper wrapper = new AdvertisementWrapper();
                wrapper.wrapDetails(advertisement, null, null);
                responses.add(wrapper);
            }

            // Manual Pagination
            int totalItems = responses.size();
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            int fromIndex = offset * limit;
            int toIndex = Math.min(fromIndex + limit, totalItems);
            if (fromIndex >= totalItems) {
                return ResponseService.generateErrorResponse("No more advertisements available", HttpStatus.BAD_REQUEST);
            }

            List<AdvertisementWrapper> paginatedResponses = responses.subList(fromIndex, toIndex);

            // Construct paginated response
            Map<String, Object> response = new HashMap<>();
            response.put("advertisements", paginatedResponses);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", offset);

            return ResponseService.generateSuccessResponse("ADVERTISEMENT RETRIEVED SUCCESSFULLY", response, HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get-all-advertisement-by-categoryId")
    public ResponseEntity<?> getFilterAdvertisements(
            @RequestParam(value = "category", required = false) String categories,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "30") int limit,
            @RequestParam(defaultValue = "false", required = false) Boolean ext) {

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

            if(categories == null || categories.isEmpty()) {
                throw new IllegalArgumentException("Category is null (Mandatory)");
            }
            List<Long> longList = Arrays.stream(categories.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if (ext) {
                List<Object[]> rows = advertisementService.getAdvCompressed(longList, offset, limit);

                BigInteger count = advertisementService.getAdvCompressedCount(longList);
                List<AdvertisementCompressedDTO> adv = new ArrayList<>();
                for (Object[] row : rows) {
                    AdvertisementCompressedDTO dto = new AdvertisementCompressedDTO();
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
                                CompressedProductWrapper compressedProductWrapper = new CompressedProductWrapper();
                                compressedProductWrapper.wrapDetails(product, request, reserveCategoryService, reserveCategoryAgeService, genderService, customCustomer, sharedUtilityService);
                                dto.getProductList().add(compressedProductWrapper);
                            }
                        } catch (NumberFormatException e) {
                            // Skip invalid ID strings
                            System.err.println("Invalid product ID: " + idStr);
                        }
                    }
                    /* activeCategories.add(dto);*/
                }

                int totalItems = adv.size();
                int totalPages = (int) Math.ceil((double) totalItems / limit);
                int fromIndex = offset * limit;
                int toIndex = Math.min(fromIndex + limit, totalItems);

                if (fromIndex >= totalItems && offset != 0) {
                    return ResponseService.generateErrorResponse("Page index out of range", HttpStatus.BAD_REQUEST);
                }

                // Construct paginated response
                Map<String, Object> response = new HashMap<>();
                response.put("advertisements", adv.subList(fromIndex, toIndex));
                response.put("totalItems", totalItems);
                response.put("totalPages", totalPages);
                response.put("currentPage", offset);
                return ResponseService.generateSuccessResponse("ADVERTISEMENT RETRIEVED SUCCESSFULLY", response, HttpStatus.OK);
            }
            List<Advertisement> advertisements = advertisementService.filterAdvertisements(null, longList, null, null, false);
            if (advertisements.isEmpty()) {
                return ResponseService.generateSuccessResponse("NO ADVERTISEMENT FOUND WITH THE GIVEN CRITERIA", advertisements, HttpStatus.OK);
            }

            List<AdvertisementProductWrapper> responses = new ArrayList<>();
            for (Advertisement advertisement : advertisements) {
                if (advertisement == null) {
                    return ResponseService.generateErrorResponse("Advertisement Not Found", HttpStatus.BAD_REQUEST);
                }

                if (advertisement.getArchived() != 'Y' &&
                        ((advertisement.getNotificationEndDate() == null) ||
                                (advertisement.getNotificationEndDate().after(new Date())))) {

                    List<CustomAdvertisementProductWrapper> products = new ArrayList<>();
                    List<CustomProduct> customProducts = productService.getAllProductsByAdvertisementId(advertisement);
                    List<CustomProduct> activeProducts = new ArrayList<>();
                    if (Constant.roleUser.equalsIgnoreCase(role)) {
                        Date today = stripTime(new Date());
                        List<CustomProduct> filteredProducts = customProducts.stream()
                                .filter(customProduct -> customProduct != null)
                                .filter(customProduct -> ((Status) customProduct).getArchived() != 'Y')
                                .filter(customProduct -> customProduct.getIsApproved())
                                .filter(customProduct -> !customProduct.getProductState().getProductState().equalsIgnoreCase(Constant.PRODUCT_STATE_DRAFT))
                                .filter(customProduct -> !customProduct.getGoLiveDate().after(new Date()))
                                .filter(customProduct -> {
                                    // If active end date is null, product is considered active indefinitely
                                    if (customProduct.getActiveEndDate() == null) {
                                        return true;
                                    }

                                    Date activeEndDate = stripTime(customProduct.getActiveEndDate());
                                    // Product is active if end date is today or in the future
                                    return !activeEndDate.before(today);
                                })
                                // Sorting
                                .sorted((p1, p2) -> {
                                    if (p1.getModifiedDate() == null && p2.getModifiedDate() == null) return 0;
                                    if (p1.getModifiedDate() == null) return 1;  // nulls go last
                                    if (p2.getModifiedDate() == null) return -1; // nulls go last
                                    return p2.getModifiedDate().compareTo(p1.getModifiedDate()); // DESC
                                })
                                .collect(Collectors.toList());

                        activeProducts = filteredProducts;
                    } else {

                        // Filter products: Keep only active products
                        activeProducts = customProducts.stream()
                                .filter(customProduct -> customProduct != null &&
                                        (((Status) customProduct).getArchived() != 'Y' &&
                                                !customProduct.getProductState().getProductState().equalsIgnoreCase(Constant.PRODUCT_STATE_DRAFT) &&
                                                customProduct.getDefaultSku().getActiveEndDate().after(new Date()))
                                        && customProduct.getGoLiveDate().before(new Date()))
                                .sorted((p1, p2) -> {
                                    if (p1.getModifiedDate() == null && p2.getModifiedDate() == null) return 0;
                                    if (p1.getModifiedDate() == null) return 1;  // nulls go last
                                    if (p2.getModifiedDate() == null) return -1; // nulls go last
                                    return p2.getModifiedDate().compareTo(p1.getModifiedDate()); // DESC
                                })
                                .collect(Collectors.toList());

                    }
// Clear products list before adding new ones to prevent duplicates
                    products.clear();
                    if (activeProducts.isEmpty()) {
                        continue; // Skip this advertisement
                    }

                    // Wrap active products
                    for (CustomProduct customProduct : activeProducts) {
                        CustomAdvertisementProductWrapper wrapper = new CustomAdvertisementProductWrapper();
                        if (authHeader == null) {
                            wrapper.wrapDetails(customProduct, null, reserveCategoryService, reserveCategoryAgeService, genderService, null, sharedUtilityService);
                        } else
                            wrapper.wrapDetails(customProduct, null, reserveCategoryService, reserveCategoryAgeService, genderService, customCustomer, sharedUtilityService);
                        products.add(wrapper);
                    }

                    // Wrap advertisement with valid products
                    AdvertisementProductWrapper wrapper = new AdvertisementProductWrapper();
                    wrapper.wrapDetails(advertisement, products, null);
                    responses.add(wrapper);
                }
            }

            // Manual Pagination
            int totalItems = responses.size();
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            int fromIndex = offset * limit;
            int toIndex = Math.min(fromIndex + limit, totalItems);

            if (fromIndex >= totalItems && offset != 0) {
                return ResponseService.generateErrorResponse("Page index out of range", HttpStatus.BAD_REQUEST);
            }

            List<AdvertisementProductWrapper> paginatedResponses = responses.subList(fromIndex, toIndex);

            // Construct paginated response
            Map<String, Object> response = new HashMap<>();
            response.put("advertisements", paginatedResponses);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", offset);

            return ResponseService.generateSuccessResponse("ADVERTISEMENT RETRIEVED SUCCESSFULLY", response, HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin, Constant.roleAdmin, Constant.roleServiceProvider})
    @DeleteMapping("/delete/{advertisementId}")
    @Transactional
    public ResponseEntity<?> deleteProduct(@PathVariable("advertisementId") String advertisementIdPath,
                                           @RequestHeader(value = "Authorization") String authHeader) {
        try {

            Long advertisementId = Long.parseLong(advertisementIdPath);

            jdbcTemplate.execute("CALL update_advertisement_product_counts()");

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Advertisement advertisement = entityManager.find(Advertisement.class, advertisementId);
            if (advertisement == null)
                return ResponseService.generateErrorResponse("Advertisement does not exist", HttpStatus.NOT_FOUND);

            if (authHeader != null) {
                String jwtToken = authHeader.substring(7);
                Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
                String role = roleService.findRoleName(roleId);
                Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
                if (roleId == 4) {
                    System.out.println(advertisement.getUserId());
                    System.out.println(tokenUserId);
                    if (!advertisement.getUserId().equals(tokenUserId)) {
                        return ResponseService.generateErrorResponse("Not authorized to delete the advertisement", HttpStatus.FORBIDDEN);
                    }
                }
            }
            if (advertisement.getProductCount() != 0) {
                return ResponseService.generateErrorResponse("Cannot delete live advertisement", HttpStatus.FORBIDDEN);                               // Find the Custom Product
            }

            if (advertisement == null) {
                return ResponseService.generateErrorResponse("Advertisement Not Found", HttpStatus.NOT_FOUND);
            }

            if (advertisement.getArchived() == 'Y') {
                return ResponseService.generateErrorResponse("Advertisement is Already Archived", HttpStatus.NOT_FOUND);
            }
            advertisement.setArchived('Y');

            String formattedDate = dateFormat.format(new Date());
            Date modifiedDate = dateFormat.parse(formattedDate); // Convert formatted date string back to Date

            advertisement.setModifiedDate(modifiedDate);

            Role role = productService.getRoleByToken(authHeader);
            Long modifierUserId = productService.getUserIdByToken(authHeader);
            advertisement.setModifierId(modifierUserId);
            advertisement.setModifierRole(role);
            advertisement.setArchived('Y');
            jdbcTemplate.execute("CALL archive_skus_and_products_for_advertisement(" + advertisementId.toString() + ")");
            entityManager.merge(advertisement);

            return ResponseService.generateSuccessResponse("ADVERTISEMENT DELETED SUCCESSFULLY", advertisement, HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-new-all-advertisement-by-categoryId")
    public ResponseEntity<?> getFilterAdvertisementNew(
            @RequestParam(value = "summarize", required = false) Boolean summarise,
            @RequestParam(value = "category", required = false) String categories,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "30") int limit) {

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

            if(categories == null || categories.isEmpty()) {
                throw new IllegalArgumentException("Category is null (Mandatory)");
            }
            List<Long> longList = Arrays.stream(categories.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Advertisement> advertisements = advertisementService.filterAdvertisements(null, longList, null, null, false);
            if (advertisements.isEmpty()) {
                return ResponseService.generateSuccessResponse("NO ADVERTISEMENT FOUND WITH THE GIVEN CRITERIA", advertisements, HttpStatus.OK);
            }

            List<AdvertisementProductWrapper> responses = new ArrayList<>();
            for (Advertisement advertisement : advertisements) {
                if (advertisement == null) {
                    return ResponseService.generateErrorResponse("Advertisement Not Found", HttpStatus.BAD_REQUEST);
                }

                if (advertisement.getArchived() != 'Y' &&
                        ((advertisement.getNotificationEndDate() == null) ||
                                (advertisement.getNotificationEndDate().after(new Date())))) {

                    List<CustomAdvertisementProductWrapper> products = new ArrayList<>();
                    List<CustomProduct> customProducts = productService.getAllProductsByAdvertisementId(advertisement);
                    List<CustomProduct> activeProducts = new ArrayList<>();
                    if (Constant.roleUser.equalsIgnoreCase(role)) {
                        Date today = stripTime(new Date());
                        List<CustomProduct> filteredProducts = customProducts.stream()
                                .filter(customProduct -> customProduct != null)
                                .filter(customProduct -> ((Status) customProduct).getArchived() != 'Y')
                                .filter(customProduct -> customProduct.getIsApproved())
                                .filter(customProduct -> !customProduct.getProductState().getProductState().equalsIgnoreCase(Constant.PRODUCT_STATE_DRAFT))
                                .filter(customProduct -> !customProduct.getGoLiveDate().after(new Date()))
                                .filter(customProduct -> {
                                    // If active end date is null, product is considered active indefinitely
                                    if (customProduct.getActiveEndDate() == null) {
                                        return true;
                                    }

                                    Date activeEndDate = stripTime(customProduct.getActiveEndDate());
                                    // Product is active if end date is today or in the future
                                    return !activeEndDate.before(today);
                                })
                                // Sorting
                                .sorted((p1, p2) -> {
                                    if (p1.getCreatedDate() == null && p2.getCreatedDate() == null) return 0;
                                    if (p1.getCreatedDate() == null) return 1;
                                    if (p2.getCreatedDate() == null) return -1;
                                    return p2.getCreatedDate().compareTo(p1.getCreatedDate()); // DESC order
                                })
                                .collect(Collectors.toList());

                        activeProducts = filteredProducts;
                    } else {

                        // Filter products: Keep only active products
                        activeProducts = customProducts.stream()
                                .filter(customProduct -> customProduct != null &&
                                        (((Status) customProduct).getArchived() != 'Y' &&
                                                !customProduct.getProductState().getProductState().equalsIgnoreCase(Constant.PRODUCT_STATE_DRAFT) &&
                                                customProduct.getDefaultSku().getActiveEndDate().after(new Date()))
                                        && customProduct.getGoLiveDate().before(new Date()))
                                .collect(Collectors.toList());

                    }
// Clear products list before adding new ones to prevent duplicates
                    products.clear();
                    if (activeProducts.isEmpty()) {
                        continue; // Skip this advertisement
                    }

                    // Wrap active products
                    for (CustomProduct customProduct : activeProducts) {
                        CustomAdvertisementProductWrapper wrapper = new CustomAdvertisementProductWrapper();
                        if (authHeader == null) {
                            wrapper.wrapDetailsSimplified(customProduct, null, reserveCategoryService, reserveCategoryAgeService, genderService, null, sharedUtilityService);
                        } else
                            wrapper.wrapDetailsSimplified(customProduct, null, reserveCategoryService, reserveCategoryAgeService, genderService, customCustomer, sharedUtilityService);
                        products.add(wrapper);
                    }

                    // Wrap advertisement with valid products
                    AdvertisementProductWrapper wrapper = new AdvertisementProductWrapper();
                    wrapper.wrapDetailsNew(advertisement, products, null);
                    responses.add(wrapper);
                }
            }

            // Manual Pagination
            int totalItems = responses.size();
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            int fromIndex = offset * limit;
            int toIndex = Math.min(fromIndex + limit, totalItems);

            if (fromIndex >= totalItems && offset != 0) {
                return ResponseService.generateErrorResponse("Page index out of range", HttpStatus.BAD_REQUEST);
            }

            List<AdvertisementProductWrapper> paginatedResponses = responses.subList(fromIndex, toIndex);

            // Construct paginated response
            Map<String, Object> response = new HashMap<>();
            response.put("advertisements", paginatedResponses);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", offset);

            return ResponseService.generateSuccessResponse("ADVERTISEMENT RETRIEVED SUCCESSFULLY", response, HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
