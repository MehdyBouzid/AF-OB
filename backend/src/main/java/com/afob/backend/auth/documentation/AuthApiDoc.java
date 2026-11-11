package com.afob.backend.auth.documentation;

import com.afob.backend.auth.dto.LoginRequestDto;
import com.afob.backend.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;

/**
 * Documentation OpenAPI de l'authentification. Implémentée par AuthController, qui porte
 * les annotations Spring MVC et la logique ; ce fichier ne porte que les annotations Swagger.
 */
@Tag(name = "Authentification")
public interface AuthApiDoc {

    @Operation(
            operationId = "loginIntervenant",
            summary = "Connexion d'un intervenant",
            description = """
                    Authentifie un intervenant par email et mot de passe et ouvre une session serveur (cookie HttpOnly).

                    Étape obligatoire avant cet appel : GET /auth/csrf, puis transmettre le jeton reçu (cookie XSRF-TOKEN)
                    dans l'en-tête X-XSRF-TOKEN de CETTE requête de login elle-même (le login est un POST, donc protégé
                    par CSRF comme n'importe quelle autre écriture).

                    Réponse en cas de succès : 200, corps vide. Le cookie de session (JSESSIONID, HttpOnly) est déposé ;
                    il sera envoyé automatiquement par le navigateur sur les requêtes authentifiées suivantes.
                    Le cookie XSRF-TOKEN n'est pas lié à la session : il reste valable après la connexion, pas besoin
                    de le régénérer pour les appels authentifiés qui suivent.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentification réussie, session ouverte. Corps de réponse vide."),
            @ApiResponse(responseCode = "400",
                    description = "Corps de requête invalide (email absent/mal formé, mot de passe trop court). "
                            + "Format d'erreur par défaut de Spring Boot (page /error) : aucun détail de validation n'est inclus "
                            + "(spring.web.error.include-message=never).",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object",
                                    example = "{\"timestamp\": \"2026-01-01T10:00:00.000Z\", \"status\": 400, \"error\": \"Bad Request\", \"path\": \"/auth/login\"}"))),
            @ApiResponse(responseCode = "401",
                    description = "Email ou mot de passe incorrect.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"message\": \"Non authentifié\"}"))),
            @ApiResponse(responseCode = "403",
                    description = "Jeton CSRF manquant ou invalide (en-tête X-XSRF-TOKEN) — vérifié avant toute authentification, "
                            + "même si la requête est anonyme : pas de cas particulier pour le login.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object",
                                    example = "{\"timestamp\": \"2026-01-01T10:00:00.000Z\", \"status\": 403, \"error\": \"Forbidden\", \"path\": \"/auth/login\"}")))
    })
    @SecurityRequirement(name = OpenApiConfig.CSRF_HEADER_SCHEME)
    ResponseEntity<Void> login(
            LoginRequestDto request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    );

    @Operation(
            operationId = "getCsrfToken",
            summary = "Obtenir un jeton CSRF",
            description = """
                    Première étape obligatoire avant tout POST/PUT/DELETE (y compris /auth/login).
                    Ne renvoie aucun corps : le jeton est déposé via l'en-tête Set-Cookie (cookie XSRF-TOKEN, lisible en JavaScript).
                    À transmettre ensuite dans l'en-tête X-XSRF-TOKEN de chaque requête protégée.
                    """
    )
    @ApiResponse(responseCode = "200", description = "Jeton CSRF déposé dans le cookie XSRF-TOKEN. Corps de réponse vide.")
    ResponseEntity<Void> csrf(@Parameter(hidden = true) CsrfToken csrfToken);
}
