package com.afob.backend.prestation.service;

import org.springframework.stereotype.Service;
import com.afob.backend.prestation.entity.FormuleTarifaire;
import com.afob.backend.prestation.entity.Prestation;
import com.afob.backend.prestation.entity.TypeTarification;
import com.afob.backend.prestation.repository.FormuleTarifaireRepository;
import com.afob.backend.prestation.repository.PrestationRepository;
import com.afob.backend.prestation.exception.notfound.PrestationNotFoundException;
import com.afob.backend.prestation.exception.validation.FormuleTarifaireValidationException;
import com.afob.backend.prestation.dto.FormuleTarifaireDto;

@Service
public class FormuleTarifaireService {

  private final FormuleTarifaireRepository formuleTarifaireRepository;
  private final PrestationRepository prestationRepository;

  public FormuleTarifaireService(FormuleTarifaireRepository formuleTarifaireRepository,
      PrestationRepository prestationRepository) {
    this.formuleTarifaireRepository = formuleTarifaireRepository;
    this.prestationRepository = prestationRepository;
  }

  public FormuleTarifaire save(Long prestationId, FormuleTarifaireDto dto) {
    if (dto.libelle() == null || dto.libelle().isBlank()) {
      throw new FormuleTarifaireValidationException("Le libellé de la formule tarifaire est obligatoire");
    }

    if (dto.prix() == null) {
      throw new FormuleTarifaireValidationException("Le prix de la formule tarifaire est obligatoire");
    }

    Prestation prestation = prestationRepository.findById(prestationId)
        .orElseThrow(() -> new PrestationNotFoundException("Prestation introuvable"));

    if (prestation.getTypeTarification() != TypeTarification.FIXE) {
      throw new FormuleTarifaireValidationException(
          "Une formule tarifaire ne peut être associée qu'à une prestation FIXE");
    }

    FormuleTarifaire formuleTarifaire = new FormuleTarifaire();
    formuleTarifaire.setLibelle(dto.libelle());
    formuleTarifaire.setPrix(dto.prix());
    formuleTarifaire.setPrestation(prestation);

    return formuleTarifaireRepository.save(formuleTarifaire);
  }
}
