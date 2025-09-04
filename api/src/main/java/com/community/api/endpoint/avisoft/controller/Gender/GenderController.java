package com.community.api.endpoint.avisoft.controller.Gender;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.entity.CustomGender;
import com.community.api.services.GenderService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingService;
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

import java.util.List;

@RestController
public class GenderController {

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Autowired
    GenderService genderService;

    @GetMapping("/get-all-gender")
    public ResponseEntity<?> getAllGender(@RequestParam(defaultValue = "false", required = false) Boolean archived) {
        try {
            List<CustomGender> customGenderList = genderService.getAllGender(archived);
            if (customGenderList.isEmpty()) {
                return ResponseService.generateErrorResponse("NO GENDER FOUND", HttpStatus.OK);
            }
            return ResponseService.generateSuccessResponse("GENDER FOUND", customGenderList, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            exceptionHandlingService.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/get-gender-by-gender-id/{genderId}")
    public ResponseEntity<?> getGenderByGenderId(@PathVariable Long genderId) {
        try {
            CustomGender customGender = genderService.getGenderByGenderId(genderId);
            if (customGender == null) {
                return ResponseService.generateSuccessResponse("NO GENDER FOUND", new Object(), HttpStatus.OK);
            }
            return ResponseService.generateSuccessResponse("GENDER FOUND", customGender, HttpStatus.OK);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @RequestMapping(value = "gender/add", method = RequestMethod.POST)
    public ResponseEntity<?> addGender(@RequestBody CustomGender customGender) {
        try {
            return ResponseService.generateSuccessResponse("Gender added successfully",
                    genderService.addGender(customGender), HttpStatus.OK);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse("Cannot add gender: " + illegalArgumentException.getMessage(),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse("Cannot add gender: " + exception.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @RequestMapping(value = "gender/{genderId}/edit", method = RequestMethod.PUT)
    public ResponseEntity<?> editGender(@PathVariable Long genderId, @RequestBody CustomGender customGender) {
        try {
            CustomGender gender = genderService.getGenderById(genderId);
            if (gender == null)
                return ResponseService.generateErrorResponse("Gender not found", HttpStatus.BAD_REQUEST);
            return ResponseService.generateSuccessResponse("Gender updated successfully",
                    genderService.editGender(genderId, customGender), HttpStatus.OK);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse("Cannot edit gender: " + illegalArgumentException.getMessage(),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse("Cannot edit gender: " + exception.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @RequestMapping(value = "gender/{genderId}/manage", method = RequestMethod.DELETE)
    public ResponseEntity<?> manageGender(@PathVariable Long genderId,
                                          @RequestParam(defaultValue = "true") Boolean archive) {
        try {
            CustomGender gender = genderService.getGenderById(genderId);
            if (gender == null)
                return ResponseService.generateErrorResponse("Gender not found", HttpStatus.BAD_REQUEST);
            return ResponseService.generateSuccessResponse("Gender archive status altered successfully",
                    genderService.manageGender(genderId, archive), HttpStatus.OK);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse("Cannot archive gender: " + illegalArgumentException.getMessage(),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse("Cannot archive gender: " + exception.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
