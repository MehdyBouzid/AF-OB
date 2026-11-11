package com.afob.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String SESSION_COOKIE_SCHEME = "sessionCookie";
    public static final String CSRF_HEADER_SCHEME = "csrfHeader";

    @Bean
    public OpenAPI afobOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AF-OB / Washafo — API Backend")
                        .version("v1")
                        .description("""
                                API du backend AF-OB (gestion de rendez-vous et de prestations).

                                ### Tester un endpoint protégé depuis Swagger UI
                                L'authentification repose sur une session serveur (cookie HttpOnly) protégée par CSRF — pas sur un jeton Bearer/OAuth.
                                Procédure, étape par étape :
                                1. Exécuter `GET /auth/csrf` (bouton "Try it out" → "Execute").
                                2. Récupérer la valeur du cookie `XSRF-TOKEN` déposé par cette réponse (voir "Où trouver XSRF-TOKEN" ci-dessous).
                                3. Ouvrir le bouton **Authorize** (en haut de la page) et renseigner cette valeur dans le champ `csrfHeader`. Dans Authorize, laissez `sessionCookie` vide : après une connexion réussie, le navigateur gère automatiquement l'envoi du cookie de session `JSESSIONID`.
                                4. Exécuter `POST /auth/login` avec vos identifiants. **Important** : l'en-tête `X-XSRF-TOKEN` est déjà nécessaire sur cet appel de login lui-même (pas seulement sur les suivants), donc il faut avoir fait l'étape 3 avant. En cas de succès, le cookie de session `JSESSIONID` (HttpOnly) est déposé par le navigateur.
                                5. Exécuter les endpoints protégés suivants normalement : le cookie de session est renvoyé automatiquement par le navigateur, et le jeton CSRF renseigné à l'étape 3 reste valable (il n'est pas lié à la session, pas besoin de refaire `GET /auth/csrf` après le login).

                                ### Où trouver XSRF-TOKEN
                                Dans les DevTools du navigateur : **Application** (Chrome) ou **Storage** (Firefox) → **Cookies** → ligne `XSRF-TOKEN`.
                                Copier la **valeur** du cookie (la longue chaîne), pas son nom.

                                ### 401 vs 403 — ce que renvoie réellement Washafo
                                - **401** `{"message":"Non authentifié"}` : pas de session valide, ou identifiants invalides sur `POST /auth/login`.
                                - **403** (format d'erreur par défaut de Spring Boot) : jeton CSRF manquant ou invalide sur un `POST`/`PUT`/`DELETE` — y compris `POST /auth/login` lui-même : il n'y a pas de cas particulier, le jeton CSRF est vérifié avant toute autre logique, authentifié ou non.
                                """))
                .tags(List.of(
                        new Tag().name("Authentification").description("Connexion, session et jeton CSRF"),
                        new Tag().name("Prestations").description("Gestion du catalogue de prestations"),
                        new Tag().name("Formules tarifaires").description("Gestion des formules tarifaires associées à une prestation FIXE")
                ))
                .components(new Components()
                        .addSecuritySchemes(SESSION_COOKIE_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("Cookie de session créé automatiquement après un POST /auth/login réussi. "
                                        + "Il est HttpOnly : votre code JavaScript ne peut pas le lire, et vous n'avez normalement "
                                        + "RIEN à saisir manuellement ici dans Authorize. Le navigateur le renvoie lui-même sur les "
                                        + "requêtes suivantes vers la même origine, dans les limites des règles de cookies applicables "
                                        + "(SameSite, HTTPS, etc.). Ce schéma documente seulement le fait qu'une session est requise ; "
                                        + "pour tester un endpoint protégé, suivez la procédure décrite dans la description générale de l'API."))
                        .addSecuritySchemes(CSRF_HEADER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-XSRF-TOKEN")
                                .description("Valeur du cookie XSRF-TOKEN obtenu via GET /auth/csrf, à renvoyer sur tout POST/PUT/DELETE")));
    }

    /**
     * Répéter l'annotation de sécurité sur une opération produit plusieurs entrées dans "security" (sémantique OU).
     * Les opérations protégées par session ET CSRF ont besoin des deux en même temps : on fusionne donc
     * les deux entrées séparées (sessionCookie seul, csrfHeader seul) en une seule entrée combinée.
     */
    @Bean
    public GlobalOperationCustomizer mergeSessionAndCsrfSecurityRequirements() {
        return (operation, handlerMethod) -> {
            List<SecurityRequirement> security = operation.getSecurity();
            if (security == null) {
                return operation;
            }
            boolean hasSession = security.stream().anyMatch(r -> r.containsKey(SESSION_COOKIE_SCHEME));
            boolean hasCsrf = security.stream().anyMatch(r -> r.containsKey(CSRF_HEADER_SCHEME));
            if (hasSession && hasCsrf) {
                SecurityRequirement merged = new SecurityRequirement()
                        .addList(SESSION_COOKIE_SCHEME)
                        .addList(CSRF_HEADER_SCHEME);
                operation.setSecurity(List.of(merged));
            }
            return operation;
        };
    }
}
