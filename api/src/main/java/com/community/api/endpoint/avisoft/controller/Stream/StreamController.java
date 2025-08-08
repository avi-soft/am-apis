package com.community.api.endpoint.avisoft.controller.Stream;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.AddStreamDto;
import com.community.api.entity.CustomStream;
import com.community.api.entity.Role;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.StreamService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.util.List;



@RestController
public class StreamController {

    private final ExceptionHandlingService exceptionHandlingService;
    private final StreamService streamService;
    private final RoleService roleService;
    private final JwtUtil jwtTokenUtil;

    @Autowired
    EntityManager entityManager;
    @Autowired
    public StreamController(ExceptionHandlingService exceptionHandlingService, StreamService streamService, RoleService roleService, JwtUtil jwtTokenUtil) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.streamService = streamService;
        this.roleService = roleService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/add-stream")
    public ResponseEntity<?> addStream(@Valid @RequestBody AddStreamDto addStreamDto, @RequestHeader(value = "Authorization") String authHeader) {
        try{
            if(!streamService.validiateAuthorization(authHeader)) {
                return ResponseService.generateErrorResponse("NOT AUTHORIZED TO ADD A STREAM", HttpStatus.FORBIDDEN);
            }

            streamService.validateAddStreamDto(addStreamDto);
            String jwtToken = authHeader.substring(7);

            Long creatorId = jwtTokenUtil.extractId(jwtToken);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Role creatorRole = roleService.getRoleByRoleId(roleId);

            CustomStream customStream = streamService.saveStream(addStreamDto, creatorId, creatorRole);

            if(customStream == null) {
                return ResponseService.generateErrorResponse("SOMETHING WENT WRONG", HttpStatus.BAD_REQUEST);
            }
            return ResponseService.generateSuccessResponse("SUCCESSFULLY ADDED", customStream, HttpStatus.OK);
        } catch (MethodArgumentNotValidException methodArgumentNotValidException) {
            exceptionHandlingService.handleException(methodArgumentNotValidException);
            return ResponseService.generateErrorResponse(  "Method Argument Not Valid Exception Caught: " + methodArgumentNotValidException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(  illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all-stream")
    public ResponseEntity<?> getAllStream(@RequestParam(required = false,defaultValue = "false")Boolean archived) {
        try {
            List<CustomStream> applicationScopeList = streamService.getAllStream(archived);
            if (applicationScopeList.isEmpty()) {
                return ResponseService.generateErrorResponse("NO STREAM FOUND", HttpStatus.OK);
            }
            return ResponseService.generateSuccessResponse("STREAMS FOUND", applicationScopeList, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-stream-by-id/{streamIdString}")
    public ResponseEntity<?> getStreamByStreamId(@PathVariable String streamIdString) {
        try {
            Long streamId = Long.parseLong(streamIdString);
            CustomStream stream = streamService.getStreamByStreamId(streamId);
            if (stream == null) {
                return ResponseService.generateErrorResponse("NO STREAM FOUND", HttpStatus.OK);
            }
            return ResponseService.generateSuccessResponse("STREAM FOUND", stream, HttpStatus.OK);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse("Invalid StreamId: " + numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

 /*   @DeleteMapping("/remove-stream-by-id/{streamIdString}")
    public ResponseEntity<?> removeStreamByStreamId(@PathVariable String streamIdString) {
        try {
            Long streamId = Long.parseLong(streamIdString);
            CustomStream stream = streamService.getStreamByStreamId(streamId);
            if (stream == null) {
                return ResponseService.generateErrorResponse("NO STREAM FOUND", HttpStatus.NOT_FOUND);
            }
            streamService.removeStreamById(stream);
            return ResponseService.generateSuccessResponse("STREAM SUCCESSFULLY ARCHIVED", stream, HttpStatus.OK);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse("Invalid StreamId: " + numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

    @GetMapping("/get-streams-by-qualification-id/{qualificationId}")
    public ResponseEntity<?> getStreamsByQualification(@PathVariable Integer qualificationId) {
        try {
            List<CustomStream> applicationScopeList =streamService.getStreamByQualificationId(qualificationId);

            if (applicationScopeList.isEmpty()) {
                return ResponseService.generateSuccessResponse("LIST OF STREAMS IS EMPTY IN QUALIFICATION WITH ID "+ qualificationId,applicationScopeList, HttpStatus.OK);
            }
            return ResponseService.generateSuccessResponse("STREAMS FOUND IN QUALIFICATION WITH ID "+ qualificationId, applicationScopeList, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Authorize(value = {Constant.roleSuperAdmin})
    @RequestMapping(value = "stream/{streamId}/manage", method = RequestMethod.DELETE)
    public ResponseEntity<?> manageStream(
            @PathVariable Long streamId,
            @RequestParam(defaultValue = "true") Boolean archive) {
        try {
            CustomStream stream = entityManager.find(CustomStream.class, streamId);
            if (stream == null) {
                return ResponseService.generateErrorResponse("Stream not found", HttpStatus.BAD_REQUEST);
            }

            return ResponseService.generateSuccessResponse(
                    "Stream archive status updated successfully",
                    streamService.manageStream(streamId, archive),
                    HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse("Cannot update stream: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Cannot update stream: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Authorize(value = {Constant.roleSuperAdmin})
    @RequestMapping(value = "stream/{streamId}/edit", method = RequestMethod.PUT)
    public ResponseEntity<?> editStream(
            @PathVariable Long streamId,
            @RequestBody CustomStream stream,
            @RequestParam(required = false) List<Integer>qualificationIds) {
        try {
            CustomStream existingStream = entityManager.find(CustomStream.class, streamId);
            if (existingStream == null) {
                return ResponseService.generateErrorResponse("Stream not found", HttpStatus.BAD_REQUEST);
            }

            return ResponseService.generateSuccessResponse(
                    "Stream updated successfully",
                    streamService.editStream(streamId,qualificationIds,stream),
                    HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse("Cannot edit stream: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Cannot edit stream: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
