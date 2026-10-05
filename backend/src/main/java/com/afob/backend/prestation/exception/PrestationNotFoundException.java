package com.afob.backend.prestation.exception;

public class PrestationNotFoundException extends RuntimeException {
    public PrestationNotFoundException(String message) {
        super(message);
    }
}
