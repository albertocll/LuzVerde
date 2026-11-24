package es.albertocll.luzverde;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Con SWAGGER_ENABLED=true, como en Docker Compose local, la documentación es pública. */
@TestPropertySource(properties = "SWAGGER_ENABLED=true")
class SwaggerActivadoTest extends TestBase {

    @Test
    void swaggerEsPublicoSiSeActiva() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Luz Verde API"));
    }
}
