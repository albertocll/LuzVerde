package es.albertocll.luzverde;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends TestBase {

    private static String cuerpoRegistro(String email, String password) {
        return """
                {"email": "%s", "password": "%s", "nombre": "Ana"}
                """.formatted(email, password);
    }

    @Test
    void registroDevuelveToken() throws Exception {
        mvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(emailUnico(), "contrasena-segura")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    void registroConEmailRepetidoDa409() throws Exception {
        String email = emailUnico();
        mvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(email, "contrasena-segura")))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(email.toUpperCase(), "otra-contrasena")))
                .andExpect(status().isConflict());
    }

    @Test
    void registroConDatosMalosDa400() throws Exception {
        mvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro("esto-no-es-un-email", "corta")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email", notNullValue()))
                .andExpect(jsonPath("$.campos.password", notNullValue()));
    }

    @Test
    void loginCorrectoEIncorrecto() throws Exception {
        String email = emailUnico();
        mvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(email, "contrasena-segura")))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "contrasena-segura"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));

        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "mala"}
                                """.formatted(email)))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "nadie@ejemplo.com", "password": "contrasena-segura"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sinTokenOConTokenFalsoDa401() throws Exception {
        mvc.perform(get("/api/v1/candidaturas"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/v1/candidaturas").header(HttpHeaders.AUTHORIZATION, "Bearer token-inventado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void swaggerEsPublico() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Luz Verde API"));
    }
}
