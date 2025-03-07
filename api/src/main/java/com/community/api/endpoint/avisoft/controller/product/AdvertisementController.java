package com.community.api.endpoint.avisoft.controller.product;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.AddAdvertisementDto;
import com.community.api.dto.AdvertisementProductWrapper;
import com.community.api.dto.AdvertisementWrapper;
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
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.community.api.component.Constant.SOME_EXCEPTION_OCCURRED;
import static com.community.api.component.Constant.request;
import static com.community.api.services.ServiceProvider.ServiceProviderServiceImpl.getLongList;
import static elemental2.core.JsRegExp.input;

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
    @Authorize(value = {Constant.roleAdmin, Constant.roleSuperAdmin,Constant.roleAdminServiceProvider, Constant.roleServiceProvider})
    public ResponseEntity<?> updateAdvertisement(@RequestBody AddAdvertisementDto addAdvertisementDto,@PathVariable Long advertisementId) {
        try {
            Advertisement advertisement=advertisementService.updateAdvertisement(addAdvertisementDto,advertisementId);

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

    @GetMapping("/get-advertisement-by-id/{advertisementId}")
    public ResponseEntity<?> retrieveAdvertisementById(HttpServletRequest request, @PathVariable("advertisementId") String advertisementIdPath) {

        try {
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

                    if (customProduct != null && (((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date()))) {
                        CustomProductWrapper wrapper = new CustomProductWrapper();
                        wrapper.wrapDetails(customProduct);
                        products.add(wrapper);
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

    @GetMapping("/get-filter-advertisement")
    public ResponseEntity<?> getFilterAdvertisements(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) List<Long> categories,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        try {
            if(offset<0)
            {
                throw new IllegalArgumentException("Offset for pagination cannot be a negative number");
            }
            if(limit<=0)
            {
                throw new IllegalArgumentException("Limit for pagination cannot be a negative number or 0");
            }
            List<Advertisement> advertisements = advertisementService.filterAdvertisements(title, categories);

            if (advertisements.isEmpty()) {
                return ResponseService.generateSuccessResponse("NO ADVERTISEMENT FOUND WITH THE GIVEN CRITERIA", advertisements, HttpStatus.OK);
            }

            List<AdvertisementWrapper> responses = new ArrayList<>();

            for (Advertisement advertisement : advertisements) {
                if (advertisement == null) {
                    return ResponseService.generateErrorResponse("Advertisement Not Found", HttpStatus.BAD_REQUEST);
                }

                if (advertisement.getArchived() != 'Y' &&
                        ((advertisement.getNotificationEndDate() == null) ||
                                (advertisement.getNotificationEndDate().after(new Date())))) {

                    AdvertisementWrapper wrapper = new AdvertisementWrapper();
                    wrapper.wrapDetails(advertisement, null, null);
                    responses.add(wrapper);
                }
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

            return ResponseService.generateSuccessResponse("ADVERTISEMENT RETRIEVED SUCCESSFULLY",response, HttpStatus.OK);

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
            @RequestParam(defaultValue = "10") int limit) {

        try {
            CustomCustomer customCustomer = null;
            if (authHeader != null) {
                String jwtToken = authHeader.substring(7);
                Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
                Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
                if (roleId == 5)
                    customCustomer = entityManager.find(CustomCustomer.class, tokenUserId);
            }
            List<Long> longList = Arrays.stream(categories.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Advertisement> advertisements = advertisementService.filterAdvertisements(null, longList);
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

                    // Filter products: Keep only active products
                    List<CustomProduct> activeProducts = customProducts.stream()
                            .filter(customProduct -> customProduct != null &&
                                    (((Status) customProduct).getArchived() != 'Y' &&
                                            customProduct.getDefaultSku().getActiveEndDate().after(new Date())))
                            .collect(Collectors.toList());

                    // **Skip this advertisement if all products are expired**
                    if (activeProducts.isEmpty()) {
                        continue; // Skip this advertisement
                    }

                    // Wrap active products
                    for (CustomProduct customProduct : activeProducts) {
                        CustomAdvertisementProductWrapper wrapper = new CustomAdvertisementProductWrapper();
                        if (authHeader == null)
                            wrapper.wrapDetails(customProduct, null);
                        else
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

    @DeleteMapping("/delete/{advertisementId}")
    @Transactional
    public ResponseEntity<?> deleteProduct(@PathVariable("advertisementId") String advertisementIdPath,
                                           @RequestHeader(value = "Authorization") String authHeader) {
        try {

            Long advertisementId = Long.parseLong(advertisementIdPath);

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Advertisement advertisement = entityManager.find(Advertisement.class, advertisementId); // Find the Custom Product

            if (advertisement == null) {
                return ResponseService.generateErrorResponse("Advertisement Not Found", HttpStatus.NOT_FOUND);
            }

            if(advertisement.getArchived() == 'Y') {
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
            entityManager.merge(advertisement);

            return ResponseService.generateSuccessResponse("ADVERTISEMENT DELETED SUCCESSFULLY", "DELETED", HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
