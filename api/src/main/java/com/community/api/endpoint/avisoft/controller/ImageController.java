package com.community.api.endpoint.avisoft.controller;

import com.community.api.entity.Image;
import com.community.api.entity.TypingText;
import com.community.api.services.ImageService;
import com.community.api.services.InstitutionService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/image")
public class ImageController
{
    @Autowired
    ImageService imageService;
    @Autowired
    EntityManager entityManager;

    @Autowired
    ExceptionHandlingImplement exceptionHandlingImplement;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("random_image_type") Integer randomImageTypeId) {
        try {
            Image savedImage = imageService.saveImage(file, randomImageTypeId);
            return ResponseService.generateSuccessResponse("Image is saved",savedImage,HttpStatus.OK);
        } catch (IOException e) {
            return ResponseService.generateErrorResponse(e.getMessage(),HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    /*@PostMapping("/upload-all")
    public ResponseEntity<?> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        try {
            // Call the updated service method to save multiple images
            List<Image> savedImages = imageService.saveImages(files);

            // Return a success response with the list of saved images
            return ResponseService.generateSuccessResponse("Images are saved", savedImages, HttpStatus.OK);
        } catch (IOException e) {
            // Handle IO exception and return error response
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Handle general exceptions and return error response
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }*/


    @GetMapping("/get-all")
    public ResponseEntity<?> getAllRandomImages(@RequestParam(required = false) List<Integer> randomImageTypeIds,@RequestParam(required = false,defaultValue = "false")Boolean archived)
    {
       List<Image> randomImages= imageService.getAllRandomImages(randomImageTypeIds,archived);
       if(randomImages.isEmpty())
       {
           return ResponseService.generateSuccessResponse(archived?"Archived Image list is empty":"Unarchived image list is empty",randomImages,HttpStatus.OK);
       }
       return ResponseService.generateSuccessResponse(archived?"Archived Image list is found":"Unarchived image list is found",randomImages,HttpStatus.OK);
    }

    @DeleteMapping("/manage/{randomImageId}")
    public ResponseEntity<?>  manageRandomImages(@PathVariable Long randomImageId,@RequestParam(defaultValue = "false") Boolean archive)
    {
        try {
            Image imageToDelete = imageService.archiveOrUnArchiveImage(randomImageId, archive);
            String message = archive ? "Random Image is archived successfully" : "Random Image is unarchived successfully";
            return ResponseService.generateSuccessResponse(message, imageToDelete,HttpStatus.OK);
        }
        catch (IllegalArgumentException e)
        {
            exceptionHandlingImplement.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e)
        {
            exceptionHandlingImplement.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update/{randomImageId}")
    public ResponseEntity<?> updateImageTypeInRandomImage(@RequestParam(required = false) Integer randomImageTypeId,@PathVariable Long randomImageId)
    {
        try
        {
            Image imageToUpdate= imageService.updateImageTypeInRandomImage(randomImageTypeId,randomImageId);
            return ResponseService.generateSuccessResponse("Random image information is updated successfully", imageToUpdate,HttpStatus.OK);
        }
        catch (IllegalArgumentException illegalArgumentException)
        {
            exceptionHandlingImplement.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse( illegalArgumentException.getMessage(),HttpStatus.BAD_REQUEST);
        }catch (Exception e)
        {
            exceptionHandlingImplement.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.BAD_REQUEST);
        }
    }
}
