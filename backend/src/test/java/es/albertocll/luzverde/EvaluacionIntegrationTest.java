package es.albertocll.luzverde;

import com.fasterxml.jackson.databind.JsonNode;
import es.albertocll.luzverde.ia.ClaudeClient;
import es.albertocll.luzverde.ia.IaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * La API de Claude está simulada: no se gasta ningún crédito.
 */
class EvaluacionIntegrationTest extends TestBase {

    static final String OFERTA = """
            Empresa Ejemplo busca desarrollador Java junior en Sevilla, modalidad híbrida.
            Requisitos: Java, Spring Boot, SQL. Valorable: Kubernetes. Salario 22.000 a 25.000 euros.
            """;

    @MockitoBean
    ClaudeClient claude;

    @BeforeEach
    void simularIa() throws Exception {
        JsonNode salida = json.readTree("""
                {
                  "empresa": "Empresa Ejemplo",
                  "puesto": "Desarrollador Java junior",
                  "semaforo": "VERDE",
                  "resumen": "Cumples los requisitos principales y el salario supera tu mínimo.",
                  "pros": ["Java a nivel sólido", "Spring Boot en un proyecto propio"],
                  "huecos": ["Kubernetes"],
                  "banderas_rojas": [],
                  "accion_recomendada": "Aplica esta semana y menciona tu proyecto con Spring Boot."
                }
                """);
        when(claude.llamarHerramienta(anyString(), anyString(), eq("registrar_evaluacion"), anyString(), anyMap()))
                .thenReturn(salida);
    }

    private String cuerpo(String texto) throws Exception {
        return json.writeValueAsString(java.util.Map.of("ofertaTexto", texto));
    }

    @Test
    void devuelveSemaforoProsHuecosYAccion() throws Exception {
        String token = registrar();

        mvc.perform(post("/api/v1/evaluaciones")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(OFERTA)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.semaforo").value("VERDE"))
                .andExpect(jsonPath("$.empresa").value("Empresa Ejemplo"))
                .andExpect(jsonPath("$.pros", hasSize(2)))
                .andExpect(jsonPath("$.huecos[0]").value("Kubernetes"))
                .andExpect(jsonPath("$.banderasRojas", hasSize(0)))
                .andExpect(jsonPath("$.accionRecomendada", containsString("Aplica")))
                .andExpect(jsonPath("$.usosRestantesHoy").value(9));

        mvc.perform(get("/api/v1/evaluaciones").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].semaforo").value("VERDE"));
    }

    @Test
    void enviaElPerfilDelUsuarioALaIa() throws Exception {
        String token = registrar();
        mvc.perform(put("/api/v1/perfil").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"salarioMinimo": 20000, "tecnologias": [{"nombre": "Spring Boot", "nivel": "SOLIDO"}]}
                                """))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/evaluaciones")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(OFERTA)))
                .andExpect(status().isCreated());

        verify(claude).llamarHerramienta(anyString(), contains("Spring Boot"), eq("registrar_evaluacion"),
                anyString(), anyMap());
    }

    @Test
    void limiteDeDiezAlDia() throws Exception {
        String token = registrar();
        for (int i = 0; i < 10; i++) {
            mvc.perform(post("/api/v1/evaluaciones")
                            .header(HttpHeaders.AUTHORIZATION, bearer(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cuerpo(OFERTA)))
                    .andExpect(status().isCreated());
        }

        mvc.perform(post("/api/v1/evaluaciones")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(OFERTA)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.detail", containsString("10")))
                .andExpect(jsonPath("$.code").value("AI_DAILY_LIMIT"));

        // Otro usuario tiene su propio contador.
        mvc.perform(post("/api/v1/evaluaciones")
                        .header(HttpHeaders.AUTHORIZATION, bearer(registrar()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(OFERTA)))
                .andExpect(status().isCreated());
    }

    @Test
    void ofertaDemasiadoCortaNoLlamaALaIa() throws Exception {
        mvc.perform(post("/api/v1/evaluaciones")
                        .header(HttpHeaders.AUTHORIZATION, bearer(registrar()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("Busco programador")))
                .andExpect(status().isBadRequest());

        verify(claude, never()).llamarHerramienta(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void semaforoInventadoDa502() throws Exception {
        when(claude.llamarHerramienta(anyString(), anyString(), eq("registrar_evaluacion"), anyString(), anyMap()))
                .thenReturn(json.readTree("{\"semaforo\": \"MORADO\"}"));

        mvc.perform(post("/api/v1/evaluaciones")
                        .header(HttpHeaders.AUTHORIZATION, bearer(registrar()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(OFERTA)))
                .andExpect(status().isBadGateway());
    }

    @Test
    void falloDeLaIaDa502() throws Exception {
        when(claude.llamarHerramienta(anyString(), anyString(), eq("registrar_evaluacion"), anyString(), anyMap()))
                .thenThrow(new IaException("caída"));

        mvc.perform(post("/api/v1/evaluaciones")
                        .header(HttpHeaders.AUTHORIZATION, bearer(registrar()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(OFERTA)))
                .andExpect(status().isBadGateway());
    }

    @Test
    void lasEvaluacionesSonPrivadas() throws Exception {
        String ana = registrar();
        var resultado = mvc.perform(post("/api/v1/evaluaciones")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ana))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(OFERTA)))
                .andExpect(status().isCreated())
                .andReturn();
        long id = leer(resultado).get("id").asLong();

        mvc.perform(get("/api/v1/evaluaciones/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer(registrar())))
                .andExpect(status().isNotFound());
    }
}
