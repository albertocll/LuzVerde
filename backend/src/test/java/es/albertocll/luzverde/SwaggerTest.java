package es.albertocll.luzverde;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Sin SWAGGER_ENABLED, como en producción, no se publica la documentación de la API. */
class SwaggerTest extends TestBase {

    @Test
    void swaggerEstaApagadoPorDefecto() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isNotFound());
        mvc.perform(get("/swagger-ui.html")).andExpect(status().isNotFound());
    }
}
