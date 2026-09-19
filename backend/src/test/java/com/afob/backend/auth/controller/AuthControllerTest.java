package com.afob.backend.auth.controller;

import com.afob.backend.auth.service.AuthenticationService;
import com.afob.backend.auth.service.IntervenantUserDetailsService;
import com.afob.backend.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private IntervenantUserDetailsService intervenantUserDetailsService;

    @Test
    void routeProtegee_sansAuthentification_renvoie401() throws Exception {
        mockMvc.perform(get("/prestations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_identifiantsValides_renvoie200() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken(
                "test@afob.com", null, List.of(new SimpleGrantedAuthority("USER"))
        );
        when(authenticationService.authenticate("test@afob.com", "motdepasse")).thenReturn(auth);

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@afob.com\",\"password\":\"motdepasse\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void login_mauvaisMotDePasse_renvoie401() throws Exception {
        when(authenticationService.authenticate("test@afob.com", "mauvaismdp"))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@afob.com\",\"password\":\"mauvaismdp\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_motDePasseTropCourt_renvoie400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@afob.com\",\"password\":\"court\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_sansTokenCsrf_renvoie403() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@afob.com\",\"password\":\"motdepasse\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_identifiantsValides_sauvegardeContexteSecurite() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken(
                "test@afob.com", null, List.of(new SimpleGrantedAuthority("USER"))
        );
        when(authenticationService.authenticate("test@afob.com", "motdepasse")).thenReturn(auth);

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@afob.com\",\"password\":\"motdepasse\"}"))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        notNullValue()
                ));
    }
}
