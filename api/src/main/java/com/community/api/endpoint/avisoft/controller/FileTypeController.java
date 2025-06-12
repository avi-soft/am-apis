package com.community.api.endpoint.avisoft.controller;

import com.community.api.entity.Image;
import com.community.api.entity.FileType;
import com.community.api.services.ResponseService;
import com.community.api.services.FileTypeService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import io.swagger.models.auth.In;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/file-type")
public class FileTypeController
{
    private final FileTypeService fileTypeService;
    private final ExceptionHandlingImplement exceptionHandling;

    public FileTypeController(FileTypeService fileTypeService,ExceptionHandlingImplement exceptionHandling) {
        this.fileTypeService = fileTypeService;
        this.exceptionHandling=exceptionHandling;
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllRandomImages()
    {
        List<FileType> randomFileTypes= fileTypeService.getAllRandomFileTypes();
        if(randomFileTypes.isEmpty())
        {
            return ResponseService.generateSuccessResponse("File Type list is empty",randomFileTypes, HttpStatus.OK);
        }
        return ResponseService.generateSuccessResponse("File Type list is found",randomFileTypes,HttpStatus.OK);
    }

    @PostMapping("/add-all")
    public ResponseEntity<?> addAllRandomImages(@RequestBody List<FileType> fileTypes)
    {
        try
        {
            List<?> randomFileTypes= fileTypeService.addAllRandomFileTypes(fileTypes);
            return ResponseService.generateSuccessResponse("The File Types are added successfully",randomFileTypes, HttpStatus.CREATED);
        }
        catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong",HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/manage/{fileTypeId}")
    public ResponseEntity<?>  deleteFileTypes(@PathVariable Integer fileTypeId, @RequestParam(defaultValue = "false") Boolean archive)
    {
        try {
            FileType fileTypeToDelete = fileTypeService.archiveOrUnarchiveFileType(fileTypeId, archive);
            String message = archive ? "File Type is archived successfully" : "File Type is unarchived successfully";
            return ResponseService.generateSuccessResponse(message, fileTypeToDelete,HttpStatus.OK);
        }
        catch (IllegalArgumentException e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update/{fileTypeId}")
    public ResponseEntity<?> updateFileType(@RequestBody FileType fileType,@PathVariable Integer fileTypeId)
    {
        try
        {
            FileType fileTypeToUpdate= fileTypeService.updateFileType(fileType,fileTypeId);
            return ResponseService.generateSuccessResponse("File Type is updated successfully", fileTypeToUpdate,HttpStatus.OK);
        }
        catch (IllegalArgumentException illegalArgumentException)
        {
            exceptionHandling.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse( illegalArgumentException.getMessage(),HttpStatus.BAD_REQUEST);
        }catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.BAD_REQUEST);
        }
    }
}
