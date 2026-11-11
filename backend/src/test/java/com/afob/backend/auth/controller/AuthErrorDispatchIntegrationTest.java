package com.afob.backend.auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Teste le vrai serveur embarqué (filtres Spring Security réels + mécanisme /error de Spring Boot)
 * avec un client HTTP standard (java.net.http), pas un MockMvc : c'est ce chemin, invisible en
 * @WebMvcTest, qui transformait un échec de validation en 401 au lieu de 400 avant que /error
 * soit ajouté à permitAll() dans SecurityConfig.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthErrorDispatchIntegrationTest {

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void login_motDePasseTropCourt_renvoie400EtPasUneErreurDAuthentification() throws Exception {
        String base = "http://localhost:" + port;

        HttpResponse<Void> csrfResponse = httpClient.send(
                HttpRequest.newBuilder(URI.create(base + "/auth/csrf")).GET().build(),
                HttpResponse.BodyHandlers.discarding());
        String csrfToken = extractCookieValue(csrfResponse, "XSRF-TOKEN");

        HttpResponse<String> loginResponse = httpClient.send(
                HttpRequest.newBuilder(URI.create(base + "/auth/login"))
                        .header("Content-Type", "application/json")
                        .header("Cookie", "XSRF-TOKEN=" + csrfToken)
                        .header("X-XSRF-TOKEN", csrfToken)
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {"email":"intervenant@afob.com","password":"court"}
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(400, loginResponse.statusCode(),
                "Une erreur de validation sur /auth/login doit renvoyer 400, pas 401 (voir SecurityConfig : /error doit rester en permitAll)");
        assertFalse(loginResponse.body().contains("Non authentifié"),
                "Le corps ne doit pas être celui de l'entrée d'authentification (401), mais celui de l'erreur de validation (400)");
    }

    private String extractCookieValue(HttpResponse<?> response, String cookieName) {
        return response.headers().allValues("Set-Cookie").stream()
                .flatMap(header -> java.util.Arrays.stream(header.split(";")))
                .map(String::trim)
                .filter(part -> part.startsWith(cookieName + "="))
                .map(part -> part.substring((cookieName + "=").length()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cookie " + cookieName + " absent de la réponse"));
    }
}
