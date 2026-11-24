package es.albertocll.luzverde;

import es.albertocll.luzverde.candidatura.CandidaturaRepository;
import es.albertocll.luzverde.cv.Cv;
import es.albertocll.luzverde.cv.CvRepository;
import es.albertocll.luzverde.evaluacion.Evaluacion;
import es.albertocll.luzverde.evaluacion.EvaluacionRepository;
import es.albertocll.luzverde.evaluacion.Semaforo;
import es.albertocll.luzverde.ia.UsoIa;
import es.albertocll.luzverde.ia.UsoIaRepository;
import es.albertocll.luzverde.perfil.PerfilRepository;
import es.albertocll.luzverde.usuario.Usuario;
import es.albertocll.luzverde.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CuentaIntegrationTest extends TestBase {

    @Autowired UsuarioRepository usuarios;
    @Autowired PerfilRepository perfiles;
    @Autowired CandidaturaRepository candidaturas;
    @Autowired EvaluacionRepository evaluaciones;
    @Autowired CvRepository cvs;
    @Autowired UsoIaRepository usosIa;

    @Test
    void borrarLaCuentaBorraTodosSusDatos() throws Exception {
        String email = emailUnico();
        var registro = mvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "contrasena-segura", "nombre": "Prueba"}
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn();
        String token = leer(registro).get("token").asText();
        long id = leer(registro).get("usuarioId").asLong();
        String otroToken = registrar();

        mvc.perform(put("/perfil")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ubicacion": "Sevilla", "tecnologias": [{"nombre": "Java", "nivel": "SOLIDO"}],
                                 "idiomas": [{"nombre": "Inglés", "nivel": "B1"}]}
                                """))
                .andExpect(status().isOk());
        for (String t : new String[]{token, otroToken}) {
            mvc.perform(post("/candidaturas")
                            .header(HttpHeaders.AUTHORIZATION, bearer(t))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"empresa": "Empresa", "puesto": "Java junior", "estado": "ENVIADA",
                                     "fecha": "2026-11-20"}
                                    """))
                    .andExpect(status().isCreated());
        }
        Usuario usuario = usuarios.getReferenceById(id);
        Evaluacion evaluacion = new Evaluacion(usuario, "Oferta de prueba");
        evaluacion.setSemaforo(Semaforo.VERDE);
        evaluaciones.save(evaluacion);
        Cv cv = new Cv(usuario);
        cv.setPdf(new byte[]{1});
        cv.setDocx(new byte[]{1});
        cvs.save(cv);
        usosIa.save(new UsoIa(id, "EVALUACION"));

        mvc.perform(delete("/cuenta").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        assertThat(usuarios.existsById(id)).isFalse();
        assertThat(perfiles.findByUsuarioId(id)).isEmpty();
        assertThat(candidaturas.findByUsuarioIdOrderByFechaDescIdDesc(id)).isEmpty();
        assertThat(evaluaciones.findTop50ByUsuarioIdOrderByCreadoEnDesc(id)).isEmpty();
        assertThat(cvs.findTop50ByUsuarioIdOrderByCreadoEnDesc(id)).isEmpty();
        assertThat(usosIa.countByUsuarioIdAndCreadoEnGreaterThanEqual(id, Instant.EPOCH)).isZero();

        // El otro usuario conserva sus datos.
        mvc.perform(get("/candidaturas").header(HttpHeaders.AUTHORIZATION, bearer(otroToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // El token de la cuenta borrada ya no sirve y el correo no puede entrar.
        mvc.perform(get("/perfil").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "contrasena-segura"}
                                """.formatted(email)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sinTokenNoSePuedeBorrar() throws Exception {
        mvc.perform(delete("/cuenta")).andExpect(status().isUnauthorized());
    }
}
