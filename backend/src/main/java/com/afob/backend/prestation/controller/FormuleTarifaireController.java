package com.afob.backend.prestation.controller;

import com.afob.backend.prestation.dto.FormuleTarifaireDto;
import com.afob.backend.prestation.entity.FormuleTarifaire;
import com.afob.backend.prestation.service.FormuleTarifaireService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/prestations/{prestationId}/formules-tarifaires")
public class FormuleTarifaireController {

    private final FormuleTarifaireService formuleTarifaireService;

    public FormuleTarifaireController(FormuleTarifaireService formuleTarifaireService) {
        this.formuleTarifaireService = formuleTarifaireService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FormuleTarifaireDto create(@PathVariable Long prestationId, @RequestBody FormuleTarifaireDto dto) {
        FormuleTarifaire formuleTarifaireSauvegardee = formuleTarifaireService.save(prestationId, dto);

        return new FormuleTarifaireDto(
                formuleTarifaireSauvegardee.getId(),
                formuleTarifaireSauvegardee.getPrix(),
                formuleTarifaireSauvegardee.getLibelle());
    }

}
