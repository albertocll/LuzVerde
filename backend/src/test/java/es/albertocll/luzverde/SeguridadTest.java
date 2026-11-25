package es.albertocll.luzverde;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Base64;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de seguridad de la API: tokens manipulados, fuerza bruta, asignación masiva,
 * fugas de información y cabeceras.
 */
class SeguridadTest extends TestBase {

    @Test
    void tokenConFirmaAlteradaEsRechazado() throws Exception {
        String token = registrar();
        String[] partes = token.split("\\.");
        // Cambiamos el usuario dentro del token sin volver a firmarlo.
        String cuerpoFalso = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"1\",\"email\":\"otro@ejemplo.com\"}".getBytes());
        String manipulado = partes[0] + "." + cuerpoFalso + "." + partes[2];

        mvc.perform(get("/api/v1/perfil").header(HttpHeaders.AUTHORIZATION, bearer(manipulado)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSinFirmaAlgNoneEsRechazado() throws Exception {
        String cabecera = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"none\"}".getBytes());
        String cuerpo = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"sub\":\"1\"}".getBytes());

        mvc.perform(get("/api/v1/perfil").header(HttpHeaders.AUTHORIZATION, bearer(cabecera + "." + cuerpo + ".")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void fuerzaBrutaContraUnaCuentaSeBloquea() throws Exception {
        String email = emailUnico();
        mvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "contrasena-buena", "nombre": "Víctima"}
                                """.formatted(email)))
                .andExpect(status().isAccepted());

        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "%s", "password": "intento-%d"}
                                    """.formatted(email, i)))
                    .andExpect(status().isUnauthorized());
        }

        // Aunque ahora acierte, sigue bloqueado durante la ventana.
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "contrasena-buena"}
                                """.formatted(email)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void lasRespuestasNoExponenLaContrasenaNiSuHash() throws Exception {
        String email = emailUnico();
        mvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "contrasena-segura", "nombre": "Ana"}
                                """.formatted(email)))
                .andExpect(status().isAccepted())
                .andExpect(content().string(not(containsString("contrasena-segura"))))
                .andExpect(content().string(not(containsString("$2a$"))));

        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "contrasena-segura"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("contrasena-segura"))))
                .andExpect(content().string(not(containsString("$2a$"))))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void asignacionMasivaNoPermiteElegirIdNiDueno() throws Exception {
        String ana = registrar();
        String luis = registrar();

        mvc.perform(post("/api/v1/candidaturas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(luis))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": 1, "usuarioId": 1, "usuario": {"id": 1},
                                 "empresa": "X", "puesto": "Y", "estado": "ENVIADA", "fecha": "2026-11-01"}
                                """))
                .andExpect(status().isCreated());

        // Ana no ve la candidatura aunque Luis haya intentado asignarla a otro usuario.
        mvc.perform(get("/api/v1/candidaturas").header(HttpHeaders.AUTHORIZATION, bearer(ana)))
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/v1/candidaturas").header(HttpHeaders.AUTHORIZATION, bearer(luis)))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void entradasMaliciosasNoRompenNadaNiFiltranTrazas() throws Exception {
        String token = registrar();

        mvc.perform(get("/api/v1/candidaturas").param("estado", "ENVIADA' OR '1'='1")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString("Exception"))));

        mvc.perform(post("/api/v1/candidaturas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString("Exception"))))
                .andExpect(content().string(not(containsString("at es.albertocll"))));

        // El texto se guarda tal cual: sin inyección SQL ni ejecución.
        mvc.perform(post("/api/v1/candidaturas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empresa": "Robert'); DROP TABLE candidaturas;--", "puesto": "<script>alert(1)</script>",
                                 "estado": "PENDIENTE", "fecha": "2026-11-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.empresa").value("Robert'); DROP TABLE candidaturas;--"));

        mvc.perform(get("/api/v1/candidaturas").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void cabecerasDeSeguridad() throws Exception {
        mvc.perform(get("/api/v1/candidaturas").header(HttpHeaders.AUTHORIZATION, bearer(registrar())))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")));
    }

    @Test
    void soloElHealthDeActuatorEsPublico() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());

        mvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
        mvc.perform(get("/actuator/beans")).andExpect(status().isUnauthorized());
    }
}
