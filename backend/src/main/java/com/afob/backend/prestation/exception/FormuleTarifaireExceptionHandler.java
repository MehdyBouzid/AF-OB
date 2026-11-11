package com.afob.backend.prestation.exception;

import com.afob.backend.prestation.exception.notfound.FormuleTarifaireNotFoundException;
import com.afob.backend.prestation.exception.validation.FormuleTarifaireValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class FormuleTarifaireExceptionHandler {

    @ExceptionHandler(FormuleTarifaireValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleFormuleTarifaireValidation(FormuleTarifaireValidationException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(FormuleTarifaireNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleFormuleTarifaireNotFound(FormuleTarifaireNotFoundException ex) {
        return Map.of("message", ex.getMessage());
    }
}
