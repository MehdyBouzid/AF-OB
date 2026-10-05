package com.afob.backend.prestation.service;

import com.afob.backend.prestation.exception.PrestationNotFoundException;
import com.afob.backend.prestation.entity.Prestation;
import org.springframework.stereotype.Service;
import com.afob.backend.prestation.repository.PrestationRepository;

@Service
public class PrestationService {

    private final PrestationRepository prestationRepository;

     public PrestationService(PrestationRepository prestationRepository) {
        this.prestationRepository = prestationRepository;
    }
      public Prestation findById(Long id) {
        return prestationRepository.findById(id)
            .orElseThrow(() -> new PrestationNotFoundException("Prestation introuvable"));
     }

}
