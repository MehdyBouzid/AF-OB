package com.afob.backend.auth.exception;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class IntervenantNotFoundException extends UsernameNotFoundException {
    public IntervenantNotFoundException(String message) {
        super(message);
    }
}
