package com.afob.backend.prestation.documentation;

import com.afob.backend.config.OpenApiConfig;
import com.afob.backend.prestation.dto.PrestationDto;
import com.afob.backend.prestation.dto.PrestationRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Documentation OpenAPI des prestations. Implémentée par PrestationController, qui porte
 * les annotations Spring MVC et la logique ; ce fichier ne porte que les annotations Swagger.
 */
@Tag(name = "Prestations")
public interface PrestationApiDoc {

    @Operation(operationId = "getPrestationById", summary = "Consulter une prestation par son identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prestation trouvée"),
            @ApiResponse(responseCode = "401", description = "Session absente ou expirée",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"message\": \"Non authentifié\"}"))),
            @ApiResponse(responseCode = "404", description = "Aucune prestation avec cet identifiant",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"message\": \"Prestation introuvable\"}")))
    })
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
    PrestationDto getById(Long id);

    @Operation(operationId = "createPrestation",
            summary = "Créer une prestation",
            description = "Crée une prestation (FIXE ou DEVIS) ; elle est active par défaut. Pour une prestation FIXE, prixMinimum est ignoré (le prix est porté par les formules tarifaires).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Prestation créée"),
            @ApiResponse(responseCode = "400", description = "Nom, description ou type de tarification manquant/invalide",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"message\": \"Le type de tarification est obligatoire\"}"))),
            @ApiResponse(responseCode = "401", description = "Session absente ou expirée",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"message\": \"Non authentifié\"}"))),
            @ApiResponse(responseCode = "403", description = "Session valide mais jeton CSRF manquant ou invalide (en-tête X-XSRF-TOKEN)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"timestamp\": \"2026-01-01T10:00:00.000+00:00\", \"status\": 403, \"error\": \"Forbidden\", \"path\": \"/prestations\"}")))
    })
    @SecurityRequirements({
            @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME),
            @SecurityRequirement(name = OpenApiConfig.CSRF_HEADER_SCHEME)
    })
    PrestationDto CreatePrestationDto(PrestationRequestDto dto);

    @Operation(operationId = "updatePrestation",
            summary = "Modifier une prestation existante",
            description = "Remplace nom, description, type de tarification et prixMinimum. Le passage à FIXE remet systématiquement prixMinimum à null.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prestation modifiée"),
            @ApiResponse(responseCode = "400", description = "Nom, description ou type de tarification manquant/invalide",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"message\": \"Le nom est obligatoire\"}"))),
            @ApiResponse(responseCode = "401", description = "Session absente ou expirée",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"message\": \"Non authentifié\"}"))),
            @ApiResponse(responseCode = "403", description = "Session valide mais jeton CSRF manquant ou invalide (en-tête X-XSRF-TOKEN)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"timestamp\": \"2026-01-01T10:00:00.000+00:00\", \"status\": 403, \"error\": \"Forbidden\", \"path\": \"/prestations/1\"}"))),
            @ApiResponse(responseCode = "404", description = "Aucune prestation avec cet identifiant",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"message\": \"Prestation introuvable\"}")))
    })
    @SecurityRequirements({
            @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME),
            @SecurityRequirement(name = OpenApiConfig.CSRF_HEADER_SCHEME)
    })
    PrestationDto updatePrestation(Long id, PrestationRequestDto nouvelleDonnee);
}
