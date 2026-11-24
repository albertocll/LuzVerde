package es.albertocll.luzverde;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PerfilIntegrationTest extends TestBase {

    private static final String PERFIL = """
            {
              "salarioMinimo": 20000,
              "modalidad": "HIBRIDO",
              "ubicacion": "Sevilla",
              "tecnologias": [
                {"nombre": "Java", "nivel": "SOLIDO"},
                {"nombre": "React", "nivel": "MEDIO"},
                {"nombre": "Kubernetes", "nivel": "SIN_TOCAR"}
              ],
              "experiencias": [
                {"puesto": "Prácticas full stack", "empresa": "Empresa de prueba",
                 "inicio": "2026-02", "fin": "2026-06", "descripcion": "Symfony y Angular"}
              ],
              "formaciones": [
                {"titulo": "CFGS DAM", "centro": "Centro de prueba", "inicio": "2024-09", "fin": "2026-06"}
              ],
              "proyectos": [
                {"nombre": "Proyecto demo", "descripcion": "API REST", "enlace": "https://ejemplo.com",
                 "tecnologias": "Spring Boot, React"}
              ],
              "idiomas": [
                {"nombre": "Inglés", "nivel": "B1"}
              ]
            }
            """;

    @Test
    void perfilNuevoEstaVacio() throws Exception {
        String token = registrar();
        mvc.perform(get("/api/v1/perfil").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salarioMinimo").doesNotExist())
                .andExpect(jsonPath("$.tecnologias", hasSize(0)));
    }

    @Test
    void guardaYDevuelveElPerfil() throws Exception {
        String token = registrar();
        mvc.perform(put("/api/v1/perfil")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PERFIL))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/perfil").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salarioMinimo").value(20000))
                .andExpect(jsonPath("$.modalidad").value("HIBRIDO"))
                .andExpect(jsonPath("$.tecnologias", hasSize(3)))
                .andExpect(jsonPath("$.tecnologias[0].nombre").value("Java"))
                .andExpect(jsonPath("$.tecnologias[2].nivel").value("SIN_TOCAR"))
                .andExpect(jsonPath("$.experiencias[0].inicio").value("2026-02"))
                .andExpect(jsonPath("$.formaciones[0].titulo").value("CFGS DAM"))
                .andExpect(jsonPath("$.proyectos[0].nombre").value("Proyecto demo"))
                .andExpect(jsonPath("$.idiomas[0].nivel").value("B1"));
    }

    @Test
    void segundoPutSustituyeLasListas() throws Exception {
        String token = registrar();
        mvc.perform(put("/api/v1/perfil").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(PERFIL))
                .andExpect(status().isOk());

        mvc.perform(put("/api/v1/perfil").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"salarioMinimo": 22000, "tecnologias": [{"nombre": "Docker", "nivel": "MEDIO"}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salarioMinimo").value(22000))
                .andExpect(jsonPath("$.tecnologias", hasSize(1)))
                .andExpect(jsonPath("$.experiencias", hasSize(0)));
    }

    @Test
    void elPerfilEsPrivado() throws Exception {
        String ana = registrar();
        String luis = registrar();
        mvc.perform(put("/api/v1/perfil").header(HttpHeaders.AUTHORIZATION, bearer(ana))
                        .contentType(MediaType.APPLICATION_JSON).content(PERFIL))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/perfil").header(HttpHeaders.AUTHORIZATION, bearer(luis)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tecnologias", hasSize(0)));
    }

    @Test
    void rechazaDatosMalos() throws Exception {
        String token = registrar();
        mvc.perform(put("/api/v1/perfil").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"salarioMinimo": -5,
                                 "experiencias": [{"puesto": "Dev", "empresa": "X", "inicio": "junio 2025"}]}
                                """))
                .andExpect(status().isBadRequest());

        mvc.perform(put("/api/v1/perfil").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tecnologias": [{"nombre": "Java", "nivel": "EXPERTO"}]}
                                """))
                .andExpect(status().isBadRequest());
    }
}
