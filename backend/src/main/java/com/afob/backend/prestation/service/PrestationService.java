package com.afob.backend.prestation.service;

import com.afob.backend.prestation.exception.PrestationNotFoundException;
import com.afob.backend.prestation.exception.PrestationValidationException;
import com.afob.backend.prestation.entity.Prestation;
import com.afob.backend.prestation.entity.TypeTarification;
import com.afob.backend.prestation.dto.PrestationRequestDto;

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

  public Prestation save(Prestation prestation) {
    prestation.setActive(true);
    prestation.getNom();
    prestation.getDescription();
    TypeTarification getTypeTarification = prestation.getTypeTarification();

    if (prestation.getNom() == null || prestation.getNom().isBlank()
        || prestation.getDescription() == null || prestation.getDescription().isBlank()) {
      throw new PrestationValidationException("le champs est vide ");
    }
    ;

    if (getTypeTarification == null) {
      throw new PrestationValidationException("Le type de tarification est obligatoire");
    }
    if (getTypeTarification == TypeTarification.FIXE) {
      prestation.setPrixMinimum(null);
    }

    return prestationRepository.save(prestation);
  }

  public Prestation updatePrestation(Long id, PrestationRequestDto nouvelleDonnee) {
    Prestation prestationExistante = findById(id);

    if (nouvelleDonnee.nom() == null || nouvelleDonnee.nom().isBlank()) {
      throw new PrestationValidationException("Le nom est obligatoire");
    }
    if (nouvelleDonnee.description() == null || nouvelleDonnee.description().isBlank()) {
      throw new PrestationValidationException("La description est obligatoire");
    }
    if (nouvelleDonnee.typeTarification() == null) {
      throw new PrestationValidationException("Le type de tarification est obligatoire");
    }
    if (nouvelleDonnee.typeTarification() == TypeTarification.FIXE) {
      prestationExistante.setPrixMinimum(null);
    } else {
      prestationExistante.setPrixMinimum(nouvelleDonnee.prixMinimum());
    }

    prestationExistante.setNom(nouvelleDonnee.nom());
    prestationExistante.setDescription(nouvelleDonnee.description());
    prestationExistante.setTypeTarification(nouvelleDonnee.typeTarification());

    return prestationRepository.save(prestationExistante);

  }

}
