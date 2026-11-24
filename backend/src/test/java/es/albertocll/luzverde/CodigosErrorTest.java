package es.albertocll.luzverde;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Cada error lleva un code fijo. La app traduce por él, así que no puede cambiar sin avisar. */
class CodigosErrorTest extends TestBase {

    private static String registro(String email) {
        return """
                {"email": "%s", "password": "contrasena-segura", "nombre": "Prueba"}
                """.formatted(email);
    }

    @Test
    void emailRepetido() throws Exception {
        String email = emailUnico();
        mvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON).content(registro(email)))
                .andExpect(status().isCreated());
        mvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON).content(registro(email)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_TAKEN"));
    }

    @Test
    void credencialesInvalidas() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "no-es-esta"}
                                """.formatted(emailUnico())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void datosNoValidos() throws Exception {
        mvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "no-es-un-email", "password": "x", "nombre": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.campos.email").exists());
    }

    @Test
    void peticionIlegible() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void recursoQueNoExiste() throws Exception {
        String token = registrar();
        mvc.perform(get("/candidaturas/999999").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void rutaQueNoExiste() throws Exception {
        String token = registrar();
        mvc.perform(get("/no-existe").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
