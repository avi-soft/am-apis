package com.community.api.endpoint.avisoft.controller;

import com.community.api.entity.FileType;
import com.community.api.services.FileTypeService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/file-type")
public class FileTypeController {
    private final FileTypeService fileTypeService;
    private final ExceptionHandlingImplement exceptionHandling;

    public FileTypeController(FileTypeService fileTypeService, ExceptionHandlingImplement exceptionHandling) {
        this.fileTypeService = fileTypeService;
        this.exceptionHandling = exceptionHandling;
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllRandomImages(@RequestParam(required = false, defaultValue = "false") Boolean archived) {
        try {
            List<FileType> randomFileTypes = fileTypeService.getAllRandomFileTypes(archived);
            if (randomFileTypes.isEmpty()) {
                return ResponseService.generateSuccessResponse(archived ? "archived File type list is empty" : "Unarchived File type list is empty", randomFileTypes, HttpStatus.OK);
            }
            return ResponseService.generateSuccessResponse(archived ? "Archived File type list is found" : "Unarchived file type list is found", randomFileTypes, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/add-all")
    public ResponseEntity<?> addAllRandomImages(@RequestBody List<FileType> fileTypes) {
        try {
            List<?> randomFileTypes = fileTypeService.addAllRandomFileTypes(fileTypes);
            return ResponseService.generateSuccessResponse("The File Types are added successfully", randomFileTypes, HttpStatus.OK);
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/manage/{fileTypeId}")
    public ResponseEntity<?> deleteFileTypes(@PathVariable Integer fileTypeId, @RequestParam(defaultValue = "false") Boolean archive) {
        try {
            FileType fileTypeToDelete = fileTypeService.archiveOrUnarchiveFileType(fileTypeId, archive);
            String message = archive ? "File Type is archived successfully" : "File Type is unarchived successfully";
            return ResponseService.generateSuccessResponse(message, fileTypeToDelete, HttpStatus.OK);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{fileTypeId}")
    public ResponseEntity<?> updateFileType(@RequestBody FileType fileType, @PathVariable Integer fileTypeId) {
        try {
            FileType fileTypeToUpdate = fileTypeService.updateFileType(fileType, fileTypeId);
            return ResponseService.generateSuccessResponse("File Type is updated successfully", fileTypeToUpdate, HttpStatus.OK);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
