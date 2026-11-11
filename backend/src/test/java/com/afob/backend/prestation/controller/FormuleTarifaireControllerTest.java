package com.afob.backend.prestation.controller;

import com.afob.backend.auth.service.IntervenantUserDetailsService;
import com.afob.backend.config.SecurityConfig;
import com.afob.backend.prestation.dto.FormuleTarifaireDto;
import com.afob.backend.prestation.entity.FormuleTarifaire;
import com.afob.backend.prestation.exception.notfound.PrestationNotFoundException;
import com.afob.backend.prestation.exception.validation.FormuleTarifaireValidationException;
import com.afob.backend.prestation.service.FormuleTarifaireService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FormuleTarifaireController.class)
@Import(SecurityConfig.class)
class FormuleTarifaireControllerTest {

    private static final String CORPS_VALIDE = """
            {"prix":70,"libelle":"Canapé 2-4 places"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FormuleTarifaireService formuleTarifaireService;

    // Requis par SecurityConfig (AuthenticationManager)
    @MockitoBean
    private IntervenantUserDetailsService intervenantUserDetailsService;

    @Test
    void creer_sansAuthentification_renvoie401() throws Exception {
        mockMvc.perform(post("/prestations/1/formules-tarifaires")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPS_VALIDE))
                .andExpect(status().isUnauthorized());

        verify(formuleTarifaireService, never()).save(any(), any());
    }

    @Test
    void creer_authentifieSansTokenCsrf_renvoie403() throws Exception {
        mockMvc.perform(post("/prestations/1/formules-tarifaires")
                        .with(user("patron@afob.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPS_VALIDE))
                .andExpect(status().isForbidden());

        verify(formuleTarifaireService, never()).save(any(), any());
    }

    @Test
    void creer_donneesValides_renvoie201AvecLeDtoEtTransmetLePrestationIdAuService() throws Exception {
        FormuleTarifaire enregistree = new FormuleTarifaire();
        ReflectionTestUtils.setField(enregistree, "id", 5L);
        enregistree.setLibelle("Canapé 2-4 places");
        enregistree.setPrix(70);
        when(formuleTarifaireService.save(eq(1L), any())).thenReturn(enregistree);

        mockMvc.perform(post("/prestations/1/formules-tarifaires")
                        .with(user("patron@afob.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPS_VALIDE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.libelle").value("Canapé 2-4 places"))
                .andExpect(jsonPath("$.prix").value(70));

        ArgumentCaptor<Long> prestationIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<FormuleTarifaireDto> dtoCaptor = ArgumentCaptor.forClass(FormuleTarifaireDto.class);
        verify(formuleTarifaireService).save(prestationIdCaptor.capture(), dtoCaptor.capture());
        assertEquals(1L, prestationIdCaptor.getValue());
        assertEquals("Canapé 2-4 places", dtoCaptor.getValue().libelle());
        assertEquals(70, dtoCaptor.getValue().prix());
    }

    @Test
    void creer_prestationIntrouvable_renvoie404AvecMessage() throws Exception {
        when(formuleTarifaireService.save(eq(999L), any()))
                .thenThrow(new PrestationNotFoundException("Prestation introuvable"));

        mockMvc.perform(post("/prestations/999/formules-tarifaires")
                        .with(user("patron@afob.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPS_VALIDE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Prestation introuvable"));
    }

    @Test
    void creer_libelleManquant_renvoie400AvecMessage() throws Exception {
        when(formuleTarifaireService.save(eq(1L), any()))
                .thenThrow(new FormuleTarifaireValidationException("Le libellé de la formule tarifaire est obligatoire"));

        mockMvc.perform(post("/prestations/1/formules-tarifaires")
                        .with(user("patron@afob.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"prix":70}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Le libellé de la formule tarifaire est obligatoire"));
    }

    @Test
    void creer_prixManquant_renvoie400AvecMessage() throws Exception {
        when(formuleTarifaireService.save(eq(1L), any()))
                .thenThrow(new FormuleTarifaireValidationException("Le prix de la formule tarifaire est obligatoire"));

        mockMvc.perform(post("/prestations/1/formules-tarifaires")
                        .with(user("patron@afob.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"libelle":"Canapé 2-4 places"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Le prix de la formule tarifaire est obligatoire"));
    }
}
