package com.afob.backend.prestation.controller;
import com.afob.backend.prestation.dto.PrestationDto;
import com.afob.backend.prestation.entity.Prestation;
import com.afob.backend.prestation.service.PrestationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/prestations")
public class PrestationController {
    private final PrestationService prestationService;

    public PrestationController(PrestationService prestationService){
       this.prestationService=prestationService;
    }
   @GetMapping("/{id}")
    public PrestationDto  getById(@PathVariable Long id) {
    Prestation prestation=  prestationService.findById(id);
     return new PrestationDto(
        prestation.getId(),
        prestation.getNom(),
        prestation.getDescription(),
        prestation.getActive(),
        prestation.getTypeTarification(),
        prestation.getPrixMinimum()
    );
}
}
