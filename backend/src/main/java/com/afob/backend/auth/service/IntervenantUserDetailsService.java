package com.afob.backend.auth.service;

import com.afob.backend.auth.entity.Intervenant;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class IntervenantUserDetailsService implements UserDetailsService {

    private final IntervenantService intervenantService;

    public IntervenantUserDetailsService(IntervenantService intervenantService) {
        this.intervenantService = intervenantService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Intervenant intervenant = intervenantService.findByEmail(email);

        return User.withUsername(intervenant.getEmail())
                .password(intervenant.getPasswordHash())
                .authorities("USER")
                .build();
    }
}