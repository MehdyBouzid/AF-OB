package com.afob.backend.prestation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class PrestationExceptionHandler {

    @ExceptionHandler(PrestationValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handlePrestationValidation(PrestationValidationException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(PrestationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handlePrestationNotFound(PrestationNotFoundException ex) {
        return Map.of("message", ex.getMessage());
    }
}
