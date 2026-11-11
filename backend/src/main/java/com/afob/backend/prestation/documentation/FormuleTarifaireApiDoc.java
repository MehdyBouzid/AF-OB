package com.afob.backend.prestation.documentation;

import com.afob.backend.config.OpenApiConfig;
import com.afob.backend.prestation.dto.FormuleTarifaireDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Documentation OpenAPI des formules tarifaires. Implémentée par FormuleTarifaireController, qui porte
 * les annotations Spring MVC et la logique ; ce fichier ne porte que les annotations Swagger.
 */
@Tag(name = "Formules tarifaires")
public interface FormuleTarifaireApiDoc {

    @Operation(operationId = "createFormuleTarifaire",
            summary = "Créer une formule tarifaire pour une prestation",
            description = "Ajoute une formule tarifaire (libellé + prix) au catalogue commun, associée à la prestation désignée par prestationId.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Formule tarifaire créée et associée à la prestation"),
            @ApiResponse(responseCode = "400", description = "Libellé ou prix manquant/invalide",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"message\": \"Le prix de la formule tarifaire est obligatoire\"}"))),
            @ApiResponse(responseCode = "401", description = "Session absente ou expirée",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"message\": \"Non authentifié\"}"))),
            @ApiResponse(responseCode = "403", description = "Session valide mais jeton CSRF manquant ou invalide (en-tête X-XSRF-TOKEN)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"timestamp\": \"2026-01-01T10:00:00.000+00:00\", \"status\": 403, \"error\": \"Forbidden\", \"path\": \"/prestations/1/formules-tarifaires\"}"))),
            @ApiResponse(responseCode = "404", description = "Aucune prestation avec cet identifiant",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"message\": \"Prestation introuvable\"}")))
    })
    @SecurityRequirements({
            @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME),
            @SecurityRequirement(name = OpenApiConfig.CSRF_HEADER_SCHEME)
    })
    FormuleTarifaireDto create(Long prestationId, FormuleTarifaireDto dto);
}
