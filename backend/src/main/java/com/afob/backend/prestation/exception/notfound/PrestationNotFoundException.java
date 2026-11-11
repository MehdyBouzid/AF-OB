package com.afob.backend.prestation.exception.notfound;

public class PrestationNotFoundException extends RuntimeException {
    public PrestationNotFoundException(String message) {
        super(message);
    }
}
