package es.albertocll.luzverde;

import com.fasterxml.jackson.databind.JsonNode;
import es.albertocll.luzverde.ia.ClaudeClient;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.ByteArrayInputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * La IA simulada intenta colar cosas que el usuario no tiene. El CV no debe contenerlas.
 */
class CvIntegrationTest extends TestBase {

    private static final String PERFIL = """
            {
              "titular": "Desarrollador full stack junior",
              "telefono": "600 000 000",
              "github": "github.com/usuario-prueba",
              "ubicacion": "Sevilla",
              "tecnologias": [
                {"nombre": "Java", "nivel": "SOLIDO"},
                {"nombre": "Spring Boot", "nivel": "SOLIDO"},
                {"nombre": "React", "nivel": "MEDIO"},
                {"nombre": "Kubernetes", "nivel": "SIN_TOCAR"}
              ],
              "experiencias": [
                {"puesto": "Desarrollador en prácticas", "empresa": "Empresa de Prácticas",
                 "inicio": "2026-02", "fin": "2026-06",
                 "descripcion": "Desarrollé endpoints con Symfony. Maqueté vistas en Angular."}
              ],
              "formaciones": [
                {"titulo": "CFGS Desarrollo de Aplicaciones Multiplataforma", "centro": "Centro de prueba",
                 "inicio": "2024-09", "fin": "2026-06"}
              ],
              "proyectos": [
                {"nombre": "Gestor de citas", "descripcion": "Aplicación web de citas.",
                 "tecnologias": "Spring Boot, React, PostgreSQL"}
              ],
              "idiomas": [{"nombre": "Inglés", "nivel": "B1"}]
            }
            """;

    private static final String CARTA_LARGA = "Me interesa mucho el puesto de desarrollador Java. "
            + "He trabajado con Spring Boot en proyectos propios y en prácticas con Symfony. "
            + "Tengo experiencia desplegando con Kubernetes en producción. "
            + "Aprendo rápido y me gusta trabajar en equipo. ".repeat(12);

    @MockitoBean
    ClaudeClient claude;

    @BeforeEach
    void simularIa() throws Exception {
        JsonNode salida = json.valueToTree(Map.of(
                "empresa", "Empresa Ejemplo",
                "puesto", "Desarrollador Java junior",
                "titular", "Desarrollador Java junior",
                "resumen", "Desarrollador junior con Java y Spring Boot. Experiencia con Kubernetes en producción.",
                "tecnologias", java.util.List.of("Java", "Kubernetes", "Spring Boot", "AWS"),
                "experiencias", java.util.List.of(
                        Map.of("indice", 0, "puntos", java.util.List.of(
                                "Desarrollé endpoints REST con Symfony",
                                "Desplegué servicios en Kubernetes")),
                        Map.of("indice", 7, "puntos", java.util.List.of("Jefe de equipo en una empresa inventada"))),
                "proyectos", java.util.List.of(
                        Map.of("indice", 0, "puntos", java.util.List.of(
                                "Construí un gestor de citas con Spring Boot y React",
                                "Lo publiqué en AWS"))),
                "tecnologias_oferta", java.util.List.of("Java", "Spring Boot", "Kubernetes", "AWS"),
                "huecos", java.util.List.of("Experiencia profesional con Java"),
                "carta", CARTA_LARGA));
        when(claude.llamarHerramienta(anyString(), anyString(), eq("redactar_cv"), anyString(), anyMap()))
                .thenReturn(salida);
    }

    private String usuarioConPerfil() throws Exception {
        String token = registrar();
        mvc.perform(put("/api/v1/perfil").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(PERFIL))
                .andExpect(status().isOk());
        return token;
    }

    private long generar(String token) throws Exception {
        var resultado = mvc.perform(post("/api/v1/cv")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("ofertaTexto", EvaluacionIntegrationTest.OFERTA))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.huecos", hasItems("Kubernetes", "AWS", "Experiencia profesional con Java")))
                .andExpect(jsonPath("$.carta", not(containsString("Kubernetes"))))
                .andReturn();
        JsonNode cuerpo = leer(resultado);
        assertThat(cuerpo.get("carta").asText().split("\\s+").length).isLessThanOrEqualTo(150);
        return cuerpo.get("id").asLong();
    }

    @Test
    void elPdfTieneTextoExtraibleYNoInventaNada() throws Exception {
        String token = usuarioConPerfil();
        long id = generar(token);

        byte[] pdf = mvc.perform(get("/api/v1/cv/{id}/pdf", id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andReturn().getResponse().getContentAsByteArray();

        try (PDDocument documento = Loader.loadPDF(pdf)) {
            assertThat(documento.getNumberOfPages()).isBetween(1, 2);
            String texto = new PDFTextStripper().getText(documento);

            assertThat(texto).contains("Prueba", "Desarrollador Java junior", "Perfil", "Tecnologías",
                    "Experiencia", "Proyectos", "Formación", "Idiomas");
            assertThat(texto).contains("Java", "Spring Boot", "React");
            assertThat(texto).contains("Desarrollador en prácticas", "Empresa de Prácticas", "02/2026 - 06/2026");
            assertThat(texto).contains("09/2024 - 06/2026", "Inglés: B1");
            assertThat(texto).contains("Desarrollé endpoints REST con Symfony");

            assertThat(texto).doesNotContainIgnoringCase("Kubernetes");
            assertThat(texto).doesNotContain("AWS");
            assertThat(texto).doesNotContain("empresa inventada");
        }
    }

    @Test
    void elDocxUsaArialYTampocoInventa() throws Exception {
        String token = usuarioConPerfil();
        long id = generar(token);

        byte[] docx = mvc.perform(get("/api/v1/cv/{id}/docx", id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        try (XWPFDocument documento = new XWPFDocument(new ByteArrayInputStream(docx));
             XWPFWordExtractor extractor = new XWPFWordExtractor(documento)) {
            String texto = extractor.getText();
            assertThat(texto).contains("Prueba", "Spring Boot", "Formación");
            assertThat(texto).doesNotContainIgnoringCase("Kubernetes");
            assertThat(texto).doesNotContain("AWS");
            assertThat(documento.getTables()).isEmpty();
            assertThat(documento.getAllPictures()).isEmpty();
            for (XWPFParagraph parrafo : documento.getParagraphs()) {
                for (XWPFRun run : parrafo.getRuns()) {
                    assertThat(run.getFontFamily()).isEqualTo("Arial");
                }
            }
        }
    }

    @Test
    void losCvSonPrivados() throws Exception {
        String token = usuarioConPerfil();
        long id = generar(token);

        String otro = registrar();
        mvc.perform(get("/api/v1/cv/{id}/pdf", id).header(HttpHeaders.AUTHORIZATION, bearer(otro)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/cv/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer(otro)))
                .andExpect(status().isNotFound());
    }

    @Test
    void sinPerfilTambienGeneraUnCvValido() throws Exception {
        String token = registrar();
        var resultado = mvc.perform(post("/api/v1/cv")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("ofertaTexto", EvaluacionIntegrationTest.OFERTA))))
                .andExpect(status().isCreated())
                .andReturn();
        long id = leer(resultado).get("id").asLong();

        byte[] pdf = mvc.perform(get("/api/v1/cv/{id}/pdf", id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        try (PDDocument documento = Loader.loadPDF(pdf)) {
            String texto = new PDFTextStripper().getText(documento);
            assertThat(texto).contains("Prueba");
            // Sin tecnologías en el perfil, ninguna tecnología puede aparecer.
            assertThat(texto).doesNotContain("Java", "Kubernetes", "Spring");
        }
    }
}
