package com.afob.backend.prestation.service;

import com.afob.backend.prestation.dto.PrestationRequestDto;
import com.afob.backend.prestation.entity.Prestation;
import com.afob.backend.prestation.entity.TypeTarification;
import com.afob.backend.prestation.exception.PrestationValidationException;
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

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrestationServiceTest {

    @Mock
    private PrestationRepository prestationRepository;

    @InjectMocks
    private PrestationService prestationService;

    private Prestation prestation(String nom, String description, TypeTarification type, BigDecimal prixMinimum) {
        Prestation prestation = new Prestation();
        prestation.setNom(nom);
        prestation.setDescription(description);
        prestation.setTypeTarification(type);
        prestation.setPrixMinimum(prixMinimum);
        return prestation;
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void save_nomAbsentOuVide_leveUneErreurDeValidation(String nom) {
        Prestation prestation = prestation(nom, "Description", TypeTarification.DEVIS, null);

        assertThrows(PrestationValidationException.class, () -> prestationService.save(prestation));

        verify(prestationRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void save_descriptionAbsenteOuVide_leveUneErreurDeValidation(String description) {
        Prestation prestation = prestation("Nettoyage vitres", description, TypeTarification.DEVIS, null);

        assertThrows(PrestationValidationException.class, () -> prestationService.save(prestation));

        verify(prestationRepository, never()).save(any());
    }

    @Test
    void save_typeTarificationAbsent_leveUneErreurDeValidation() {
        Prestation prestation = prestation("Nettoyage vitres", "Description", null, null);

        assertThrows(PrestationValidationException.class, () -> prestationService.save(prestation));

        verify(prestationRepository, never()).save(any());
    }

    @Test
    void save_prestationValide_estActiveParDefaut() {
        Prestation prestation = prestation("Canapé", "Nettoyage canapé", TypeTarification.FIXE, null);
        prestation.setActive(false);
        when(prestationRepository.save(any(Prestation.class))).thenAnswer(inv -> inv.getArgument(0));

        prestationService.save(prestation);

        ArgumentCaptor<Prestation> captor = ArgumentCaptor.forClass(Prestation.class);
        verify(prestationRepository).save(captor.capture());
        assertTrue(captor.getValue().isActive());
    }

    @Test
    void save_prestationValide_retourneLaPrestationEnregistree() {
        Prestation prestation = prestation("Canapé", "Nettoyage canapé", TypeTarification.FIXE, null);
        Prestation enregistree = prestation("Canapé", "Nettoyage canapé", TypeTarification.FIXE, null);
        when(prestationRepository.save(prestation)).thenReturn(enregistree);

        assertSame(enregistree, prestationService.save(prestation));
    }

    @Test
    void save_fixeSansPrixMinimum_estAccepte() {
        Prestation prestation = prestation("Matelas", "Nettoyage matelas", TypeTarification.FIXE, null);
        when(prestationRepository.save(any(Prestation.class))).thenAnswer(inv -> inv.getArgument(0));

        Prestation resultat = prestationService.save(prestation);

        assertNull(resultat.getPrixMinimum());
    }

    @Test
    void save_devisSansPrixMinimum_estAccepte() {
        Prestation prestation = prestation("Vitres", "Nettoyage vitres", TypeTarification.DEVIS, null);
        when(prestationRepository.save(any(Prestation.class))).thenAnswer(inv -> inv.getArgument(0));

        Prestation resultat = prestationService.save(prestation);

        assertNull(resultat.getPrixMinimum());
    }

    @Test
    void save_devisAvecPrixMinimum_leConserve() {
        Prestation prestation = prestation("Intérieur véhicule", "Nettoyage intérieur", TypeTarification.DEVIS,
                new BigDecimal("60.00"));
        when(prestationRepository.save(any(Prestation.class))).thenAnswer(inv -> inv.getArgument(0));

        Prestation resultat = prestationService.save(prestation);

        assertEquals(new BigDecimal("60.00"), resultat.getPrixMinimum());
    }

    // Règle validée : prixMinimum est null pour une prestation FIXE (le prix est porté par les formules).
    // save() ne refuse pas la valeur envoyée, il la remet à null.
    @Test
    void save_fixeAvecPrixMinimum_leMetANull() {
        Prestation prestation = prestation("Canapé", "Nettoyage canapé", TypeTarification.FIXE,
                new BigDecimal("70.00"));
        when(prestationRepository.save(any(Prestation.class))).thenAnswer(inv -> inv.getArgument(0));

        prestationService.save(prestation);

        ArgumentCaptor<Prestation> captor = ArgumentCaptor.forClass(Prestation.class);
        verify(prestationRepository).save(captor.capture());
        assertNull(captor.getValue().getPrixMinimum());
    }

    // BE-6 : passer une prestation de DEVIS à FIXE remet prixMinimum à null (règle validée, comme pour save())
    // et conserve l'identité de la prestation (même id, c'est une modification et non une création).
    @Test
    void updatePrestation_devisVersFixe_metPrixMinimumANullEtConserveLId() {
        Prestation existante = prestation("Intérieur véhicule", "Nettoyage intérieur", TypeTarification.DEVIS,
                new BigDecimal("70.00"));
        ReflectionTestUtils.setField(existante, "id", 1L);
        // la demande de modification renvoie encore l'ancien prix : le Service doit quand même le remettre à null
        PrestationRequestDto nouvelleDonnee = new PrestationRequestDto("Intérieur véhicule", "Nettoyage intérieur",
                TypeTarification.FIXE, new BigDecimal("70.00"));
        when(prestationRepository.findById(1L)).thenReturn(Optional.of(existante));
        when(prestationRepository.save(any(Prestation.class))).thenAnswer(inv -> inv.getArgument(0));

        Prestation resultat = prestationService.updatePrestation(1L, nouvelleDonnee);

        ArgumentCaptor<Prestation> captor = ArgumentCaptor.forClass(Prestation.class);
        verify(prestationRepository).save(captor.capture());
        Prestation enregistree = captor.getValue();
        assertEquals(TypeTarification.FIXE, enregistree.getTypeTarification());
        assertNull(enregistree.getPrixMinimum());
        assertEquals(1L, enregistree.getId());
        assertSame(enregistree, resultat);
    }
}
