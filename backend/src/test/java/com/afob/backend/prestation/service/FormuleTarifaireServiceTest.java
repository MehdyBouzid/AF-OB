package com.afob.backend.prestation.service;

import com.afob.backend.prestation.dto.FormuleTarifaireDto;
import com.afob.backend.prestation.entity.FormuleTarifaire;
import com.afob.backend.prestation.entity.Prestation;
import com.afob.backend.prestation.entity.TypeTarification;
import com.afob.backend.prestation.exception.notfound.PrestationNotFoundException;
import com.afob.backend.prestation.exception.validation.FormuleTarifaireValidationException;
import com.afob.backend.prestation.repository.FormuleTarifaireRepository;
import com.afob.backend.prestation.repository.PrestationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormuleTarifaireServiceTest {

    @Mock
    private FormuleTarifaireRepository formuleTarifaireRepository;

    @Mock
    private PrestationRepository prestationRepository;

    @InjectMocks
    private FormuleTarifaireService formuleTarifaireService;

    private Prestation prestation(Long id, TypeTarification typeTarification) {
        Prestation prestation = new Prestation();
        ReflectionTestUtils.setField(prestation, "id", id);
        prestation.setTypeTarification(typeTarification);
        return prestation;
    }

    @Test
    void save_prestationIntrouvable_levePrestationNotFoundEtNEnregistreRien() {
        when(prestationRepository.findById(999L)).thenReturn(Optional.empty());
        FormuleTarifaireDto dto = new FormuleTarifaireDto(null, 70, "Canapé 2-4 places");

        assertThrows(PrestationNotFoundException.class, () -> formuleTarifaireService.save(999L, dto));

        verify(formuleTarifaireRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void save_libelleAbsentOuVide_leveUneErreurDeValidationSansRienEnregistrer(String libelle) {
        FormuleTarifaireDto dto = new FormuleTarifaireDto(null, 70, libelle);

        assertThrows(FormuleTarifaireValidationException.class, () -> formuleTarifaireService.save(1L, dto));

        verify(prestationRepository, never()).findById(any());
        verify(formuleTarifaireRepository, never()).save(any());
    }

    @Test
    void save_prixAbsent_leveUneErreurDeValidationSansRienEnregistrer() {
        FormuleTarifaireDto dto = new FormuleTarifaireDto(null, null, "Canapé 2-4 places");

        assertThrows(FormuleTarifaireValidationException.class, () -> formuleTarifaireService.save(1L, dto));

        verify(prestationRepository, never()).findById(any());
        verify(formuleTarifaireRepository, never()).save(any());
    }

    @Test
    void save_prestationFixe_associeLaPrestationTrouveeEtEnregistre() {
        Prestation prestationFixe = prestation(1L, TypeTarification.FIXE);
        when(prestationRepository.findById(1L)).thenReturn(Optional.of(prestationFixe));
        when(formuleTarifaireRepository.save(any(FormuleTarifaire.class))).thenAnswer(inv -> inv.getArgument(0));
        FormuleTarifaireDto dto = new FormuleTarifaireDto(null, 70, "Canapé 2-4 places");

        FormuleTarifaire resultat = formuleTarifaireService.save(1L, dto);

        ArgumentCaptor<FormuleTarifaire> captor = ArgumentCaptor.forClass(FormuleTarifaire.class);
        verify(formuleTarifaireRepository).save(captor.capture());
        assertEquals("Canapé 2-4 places", captor.getValue().getLibelle());
        assertEquals(70, captor.getValue().getPrix());
        assertSame(prestationFixe, captor.getValue().getPrestation());
        assertSame(captor.getValue(), resultat);
    }

    // BE-7 (suite) : règle validée dans project.md — les formules tarifaires sont réservées aux
    // prestations FIXE. FormuleTarifaireService.save() ne vérifie pas encore typeTarification :
    // ce test doit échouer (RED) jusqu'à l'ajout de ce contrôle dans le Service.
    @Test
    void save_prestationDevis_leveUneErreurDeValidationSansRienEnregistrer() {
        Prestation prestationDevis = prestation(1L, TypeTarification.DEVIS);
        when(prestationRepository.findById(1L)).thenReturn(Optional.of(prestationDevis));
        FormuleTarifaireDto dto = new FormuleTarifaireDto(null, 70, "Canapé 2-4 places");

        assertThrows(FormuleTarifaireValidationException.class, () -> formuleTarifaireService.save(1L, dto));

        verify(formuleTarifaireRepository, never()).save(any());
    }
}
