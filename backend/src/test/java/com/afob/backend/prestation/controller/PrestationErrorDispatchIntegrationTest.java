package com.afob.backend.prestation.controller;

import com.afob.backend.auth.entity.Intervenant;
import com.afob.backend.auth.repository.IntervenantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Teste le vrai serveur embarqué (filtres Spring Security réels + mécanisme /error de Spring Boot),
 * pas un MockMvc : c'est ce chemin, invisible en @WebMvcTest, qui avait déjà transformé un échec de
 * validation en 401 au lieu de 400 sur /auth/login avant que /error soit ajouté à permitAll() dans
 * SecurityConfig. Ce test vérifie que les créations de Prestation/FormuleTarifaire sont protégées
 * de la même façon.
 *
 * Utilise un intervenant jetable créé et supprimé par ce test (base "afob_test" du profil "test"),
 * jamais la base de développement, et sans dépendre de données métier préexistantes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PrestationErrorDispatchIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private IntervenantRepository intervenantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private static final String MOT_DE_PASSE = "MotDePasseTest123";

    private Long intervenantId;
    private String email;

    @BeforeEach
    void creerUnIntervenantJetable() {
        email = "test-integration-" + UUID.randomUUID() + "@afob.com";

        Intervenant intervenant = new Intervenant();
        intervenant.setPrenom("Test");
        intervenant.setEmail(email);
        intervenant.setPasswordHash(passwordEncoder.encode(MOT_DE_PASSE));

        intervenantId = intervenantRepository.save(intervenant).getId();
    }

    @AfterEach
    void supprimerLIntervenantJetable() {
        intervenantRepository.deleteById(intervenantId);
    }

    @Test
    void creerPrestation_typeTarificationInvalide_renvoie400EtPasUneErreurDAuthentification() throws Exception {
        String base = "http://localhost:" + port;
        String csrfToken = recupererJetonCsrf(base);
        String sessionCookie = seConnecter(base, csrfToken);

        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder(URI.create(base + "/prestations"))
                        .header("Content-Type", "application/json")
                        .header("Cookie", "XSRF-TOKEN=" + csrfToken + "; JSESSIONID=" + sessionCookie)
                        .header("X-XSRF-TOKEN", csrfToken)
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {"nom":"Test intégration","description":"Test intégration","typeTarification":"GRATUIT"}
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "Un typeTarification invalide doit renvoyer 400, pas 401 (voir SecurityConfig : /error doit rester en permitAll)");
    }

    @Test
    void creerFormuleTarifaire_corpsJsonInvalide_renvoie400EtPasUneErreurDAuthentification() throws Exception {
        String base = "http://localhost:" + port;
        String csrfToken = recupererJetonCsrf(base);
        String sessionCookie = seConnecter(base, csrfToken);

        // prix non numérique : échec de désérialisation Jackson avant tout accès à la base,
        // l'identifiant de prestation utilisé n'a donc pas besoin d'exister.
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder(URI.create(base + "/prestations/999999/formules-tarifaires"))
                        .header("Content-Type", "application/json")
                        .header("Cookie", "XSRF-TOKEN=" + csrfToken + "; JSESSIONID=" + sessionCookie)
                        .header("X-XSRF-TOKEN", csrfToken)
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {"libelle":"Test intégration","prix":"abc"}
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "Un corps JSON invalide doit renvoyer 400, pas 401 (voir SecurityConfig : /error doit rester en permitAll)");
    }

    private String recupererJetonCsrf(String base) throws Exception {
        HttpResponse<Void> csrfResponse = httpClient.send(
                HttpRequest.newBuilder(URI.create(base + "/auth/csrf")).GET().build(),
                HttpResponse.BodyHandlers.discarding());
        return extraireValeurCookie(csrfResponse, "XSRF-TOKEN");
    }

    private String seConnecter(String base, String csrfToken) throws Exception {
        HttpResponse<String> loginResponse = httpClient.send(
                HttpRequest.newBuilder(URI.create(base + "/auth/login"))
                        .header("Content-Type", "application/json")
                        .header("Cookie", "XSRF-TOKEN=" + csrfToken)
                        .header("X-XSRF-TOKEN", csrfToken)
                        .POST(HttpRequest.BodyPublishers.ofString(
                                "{\"email\":\"" + email + "\",\"password\":\"" + MOT_DE_PASSE + "\"}"))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, loginResponse.statusCode(), "La connexion de l'intervenant de test doit réussir");
        return extraireValeurCookie(loginResponse, "JSESSIONID");
    }

    private String extraireValeurCookie(HttpResponse<?> response, String cookieName) {
        return response.headers().allValues("Set-Cookie").stream()
                .flatMap(header -> java.util.Arrays.stream(header.split(";")))
                .map(String::trim)
                .filter(part -> part.startsWith(cookieName + "="))
                .map(part -> part.substring((cookieName + "=").length()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cookie " + cookieName + " absent de la réponse"));
    }
}
