package com.afob.backend.auth.service;
import com.afob.backend.auth.exception.IntervenantNotFoundException;
import com.afob.backend.auth.entity.Intervenant;
import com.afob.backend.auth.repository.IntervenantRepository;
import org.springframework.stereotype.Service;

@Service
public class IntervenantService {

    private final IntervenantRepository intervenantRepository;

    public IntervenantService(IntervenantRepository intervenantRepository) {
        this.intervenantRepository = intervenantRepository;
    }

    public Intervenant findByEmail(String email) {
        return intervenantRepository.findByEmail(email)
                .orElseThrow(() -> new IntervenantNotFoundException("Intervenant introuvable"));
    }

}
