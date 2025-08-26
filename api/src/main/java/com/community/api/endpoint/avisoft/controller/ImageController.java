package com.community.api.endpoint.avisoft.controller;

import com.community.api.dto.ImageResponseDto;
import com.community.api.entity.Image;
import com.community.api.services.ImageService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
        } catch (IOException ioException) {
            exceptionHandlingImplement.handleException(ioException);
            return ResponseService.generateErrorResponse(ioException.getMessage(),HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingImplement.handleException(exception);
            return ResponseService.generateErrorResponse(exception.getMessage(),HttpStatus.BAD_REQUEST);
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
        try {
            List<ImageResponseDto> randomImages= imageService.getAllRandomImagesDtos(randomImageTypeIds,archived);
            if(randomImages.isEmpty())
            {
                return ResponseService.generateSuccessResponse(archived?"Archived Image list is empty":"Unarchived image list is empty",randomImages,HttpStatus.OK);
            }
            return ResponseService.generateSuccessResponse(archived?"Archived Image list is found":"Unarchived image list is found",randomImages,HttpStatus.OK);
        } catch (IllegalArgumentException illegalArgumentException)
        {
            exceptionHandlingImplement.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception exception)
        {
            exceptionHandlingImplement.handleException(exception);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
   }

    @DeleteMapping("/manage/{randomImageId}")
    public ResponseEntity<?>  manageRandomImages(@PathVariable Long randomImageId,@RequestParam(defaultValue = "false") Boolean archive)
    {
        try {
            Image imageToDelete = imageService.archiveOrUnArchiveImage(randomImageId, archive);
            String message = archive ? "Random Image is archived successfully" : "Random Image is unarchived successfully";
            return ResponseService.generateSuccessResponse(message, imageToDelete,HttpStatus.OK);
        }
        catch (IllegalArgumentException illegalArgumentException)
        {
            exceptionHandlingImplement.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception exception)
        {
            exceptionHandlingImplement.handleException(exception);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
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
        }catch (Exception exception)
        {
            exceptionHandlingImplement.handleException(exception);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
