package es.albertocll.luzverde;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class TestBase {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;

    protected static String emailUnico() {
        return "test-" + UUID.randomUUID() + "@ejemplo.com";
    }

    /** Registra un usuario nuevo y devuelve su token. */
    protected String registrar() throws Exception {
        String cuerpo = """
                {"email": "%s", "password": "contrasena-segura", "nombre": "Prueba"}
                """.formatted(emailUnico());
        MvcResult resultado = mvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andReturn();
        return leer(resultado).get("token").asText();
    }

    protected JsonNode leer(MvcResult resultado) throws Exception {
        return json.readTree(resultado.getResponse().getContentAsString());
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }
}
