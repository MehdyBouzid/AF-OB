package com.afob.backend.prestation.controller;

import com.afob.backend.prestation.dto.PrestationDto;
import com.afob.backend.prestation.dto.PrestationRequestDto;
import com.afob.backend.prestation.entity.Prestation;
import com.afob.backend.prestation.service.PrestationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/prestations")
public class PrestationController {
    private final PrestationService prestationService;

    public PrestationController(PrestationService prestationService) {
        this.prestationService = prestationService;
    }

    @GetMapping("/{id}")
    public PrestationDto getById(@PathVariable Long id) {
        Prestation prestation = prestationService.findById(id);
        return new PrestationDto(
                prestation.getId(),
                prestation.getNom(),
                prestation.getDescription(),
                prestation.isActive(),
                prestation.getTypeTarification(),
                prestation.getPrixMinimum());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrestationDto CreatePrestationDto(@RequestBody PrestationRequestDto dto) {

        Prestation prestation = new Prestation();

        prestation.setNom(dto.nom());
        prestation.setDescription(dto.description());
        prestation.setTypeTarification(dto.typeTarification());
        prestation.setPrixMinimum(dto.prixMinimum());

        Prestation prestationSauvegardee = prestationService.save(prestation);

        return new PrestationDto(
                prestationSauvegardee.getId(),
                prestationSauvegardee.getNom(),
                prestationSauvegardee.getDescription(),
                prestationSauvegardee.isActive(),
                prestationSauvegardee.getTypeTarification(),
                prestationSauvegardee.getPrixMinimum());
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PrestationDto updatePrestation(
            @PathVariable Long id,
            @RequestBody PrestationRequestDto nouvelleDonnee) {

        Prestation prestationUpdate = prestationService.updatePrestation(id, nouvelleDonnee);

        return new PrestationDto(
                prestationUpdate.getId(),
                prestationUpdate.getNom(),
                prestationUpdate.getDescription(),
                prestationUpdate.isActive(),
                prestationUpdate.getTypeTarification(),
                prestationUpdate.getPrixMinimum());

    }
}
