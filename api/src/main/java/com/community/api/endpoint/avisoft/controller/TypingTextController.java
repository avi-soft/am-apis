package com.community.api.endpoint.avisoft.controller;

import com.community.api.entity.TypingText;
import com.community.api.services.ResponseService;
import com.community.api.services.TypingTextService;
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
@RequestMapping("/typing-text")
public class TypingTextController {
    private final TypingTextService typingTextService;
    private final ExceptionHandlingImplement exceptionHandling;

    public TypingTextController(TypingTextService typingTextService, ExceptionHandlingImplement exceptionHandling) {
        this.typingTextService = typingTextService;
        this.exceptionHandling = exceptionHandling;
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllRandomImages(@RequestParam(required = false, defaultValue = "false") Boolean archived) {
        try {
            List<TypingText> randomTypingTexts = typingTextService.getAllRandomTypingTexts(archived);
            if (randomTypingTexts.isEmpty()) {
                return ResponseService.generateSuccessResponse(archived ? "archived Typing Text list is empty" : "Unarchived Typing Text list is empty", randomTypingTexts, HttpStatus.OK);
            }
            return ResponseService.generateSuccessResponse(archived ? "Archived Typing Text list is found" : "Unarchived Typing Text list is found", randomTypingTexts, HttpStatus.OK);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/add-all")
    public ResponseEntity<?> addAllRandomImages(@RequestBody List<TypingText> typingTexts) {
        try {
            List<?> randomTypingTexts = typingTextService.addAllRandomTypingTexts(typingTexts);
            return ResponseService.generateSuccessResponse("The Typing Texts are added successfully", randomTypingTexts, HttpStatus.CREATED);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/manage/{typingTextId}")
    public ResponseEntity<?> deleteTypingTexts(@PathVariable Long typingTextId, @RequestParam(defaultValue = "false") Boolean archive) {
        try {
            TypingText typingTextToDelete = typingTextService.archiveOrUnarchiveTypingText(typingTextId, archive);
            String message = archive ? "Typing text is archived successfully" : "Typing text is unarchived successfully";
            return ResponseService.generateSuccessResponse(message, typingTextToDelete, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{typingTextId}")
    public ResponseEntity<?> updateTypingText(@RequestBody TypingText typingText, @PathVariable Long typingTextId) {
        try {
            TypingText typingTextToUpdate = typingTextService.updateTypingText(typingText, typingTextId);
            return ResponseService.generateSuccessResponse("Typing text is updated successfully", typingTextToUpdate, HttpStatus.OK);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
