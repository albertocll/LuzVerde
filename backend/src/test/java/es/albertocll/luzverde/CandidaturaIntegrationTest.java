package es.albertocll.luzverde;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CandidaturaIntegrationTest extends TestBase {

    private static String candidatura(String empresa, String estado, String fecha) {
        return """
                {
                  "empresa": "%s",
                  "puesto": "Desarrollador Java junior",
                  "enlace": "https://ejemplo.com/oferta/1",
                  "estado": "%s",
                  "fecha": "%s",
                  "notas": "Contactar con RR. HH."
                }
                """.formatted(empresa, estado, fecha);
    }

    private long crear(String token, String cuerpo) throws Exception {
        var resultado = mvc.perform(post("/api/v1/candidaturas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andReturn();
        return leer(resultado).get("id").asLong();
    }

    @Test
    void cicloCompletoCrud() throws Exception {
        String token = registrar();
        long id = crear(token, candidatura("Acme", "PENDIENTE", "2026-11-20"));

        mvc.perform(get("/api/v1/candidaturas/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empresa").value("Acme"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.fecha").value("2026-11-20"));

        mvc.perform(put("/api/v1/candidaturas/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(candidatura("Acme", "ENTREVISTA", "2026-11-22")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENTREVISTA"));

        mvc.perform(delete("/api/v1/candidaturas/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/candidaturas/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void filtraPorEstado() throws Exception {
        String token = registrar();
        crear(token, candidatura("Uno", "ENVIADA", "2026-11-01"));
        crear(token, candidatura("Dos", "ENVIADA", "2026-11-05"));
        crear(token, candidatura("Tres", "DESCARTADA", "2026-11-03"));

        mvc.perform(get("/api/v1/candidaturas").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].empresa").value("Dos"));

        mvc.perform(get("/api/v1/candidaturas").param("estado", "ENVIADA")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(get("/api/v1/candidaturas").param("estado", "NO_EXISTE")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cadaUsuarioSoloVeLoSuyo() throws Exception {
        String ana = registrar();
        String luis = registrar();
        long idDeAna = crear(ana, candidatura("Privada", "ENVIADA", "2026-11-10"));

        mvc.perform(get("/api/v1/candidaturas").header(HttpHeaders.AUTHORIZATION, bearer(luis)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mvc.perform(get("/api/v1/candidaturas/{id}", idDeAna).header(HttpHeaders.AUTHORIZATION, bearer(luis)))
                .andExpect(status().isNotFound());

        mvc.perform(put("/api/v1/candidaturas/{id}", idDeAna)
                        .header(HttpHeaders.AUTHORIZATION, bearer(luis))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(candidatura("Hackeada", "DESCARTADA", "2026-11-10")))
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/v1/candidaturas/{id}", idDeAna).header(HttpHeaders.AUTHORIZATION, bearer(luis)))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/candidaturas/{id}", idDeAna).header(HttpHeaders.AUTHORIZATION, bearer(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empresa").value("Privada"));
    }

    @Test
    void validaCamposObligatorios() throws Exception {
        String token = registrar();
        mvc.perform(post("/api/v1/candidaturas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empresa": "", "puesto": "Dev"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.empresa").exists())
                .andExpect(jsonPath("$.campos.estado").exists())
                .andExpect(jsonPath("$.campos.fecha").exists());
    }
}
