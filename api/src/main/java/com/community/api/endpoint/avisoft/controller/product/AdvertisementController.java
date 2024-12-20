package com.community.api.endpoint.avisoft.controller.product;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.dto.AddAdvertisementDto;
import com.community.api.dto.AddProductDto;
import com.community.api.dto.AdvertisementWrapper;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.entity.Advertisement;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.Role;
import com.community.api.services.AdvertisementService;
import com.community.api.services.ProductService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.CategoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = "/advertisement", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
public class AdvertisementController {

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Autowired
    AdvertisementService advertisementService;

    @Autowired
    ProductService productService;

    @Transactional
    @PostMapping("/add/{categoryId}")
    @Authorize(value = {Constant.roleAdmin, Constant.roleSuperAdmin})
    public ResponseEntity<?> addAdvertisement(@RequestBody AddAdvertisementDto addAdvertisementDto,
                                              @PathVariable Long categoryId,
                                              @RequestHeader(value = "Authorization") String authHeader) {
        try {

            Category category = productService.validateCategory(categoryId);

            advertisementService.validateAdvertisement(addAdvertisementDto);

            Role role = productService.getRoleByToken(authHeader);
            Long creatorUserId = productService.getUserIdByToken(authHeader);

            Advertisement advertisement = advertisementService.saveAdvertisement(addAdvertisementDto, creatorUserId, role, (CategoryImpl) category);

            AdvertisementWrapper wrapper = new AdvertisementWrapper();
            wrapper.wrapDetails(advertisement, null);

            return ResponseService.generateSuccessResponse("Advertisement Created Successfully", wrapper, HttpStatus.OK);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-filter-advertisement")
    public ResponseEntity<?> getFilterAdvertisements(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) List<Long> categories) {

        try {

            List<Advertisement> advertisements = advertisementService.filterAdvertisements(title, categories);

            if (advertisements.isEmpty()) {
                return ResponseService.generateSuccessResponse("NO ADVERTISEMENT FOUND WITH THE GIVEN CRITERIA", advertisements, HttpStatus.OK);
            }

            List<AdvertisementWrapper> responses = new ArrayList<>();
            for (Advertisement advertisement : advertisements) {

                if (advertisement != null) {

                    if (advertisement.getArchived() != 'Y') {

                        AdvertisementWrapper wrapper = new AdvertisementWrapper();
                        wrapper.wrapDetails(advertisement, null);

                        responses.add(wrapper);
                    }
                }
            }

            return ResponseService.generateSuccessResponse("ADVERTISEMENT RETRIEVED SUCCESSFULLY", responses, HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
