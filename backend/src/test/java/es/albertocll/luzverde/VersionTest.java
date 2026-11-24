package es.albertocll.luzverde;

import es.albertocll.luzverde.version.VersionController;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VersionTest extends TestBase {

    @Test
    void laVersionMinimaEsPublica() throws Exception {
        mvc.perform(get("/api/v1/version"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versionMinima").value("1.2.3"));
    }

    @Test
    void rechazaUnaVersionMalEscrita() {
        assertThatThrownBy(() -> new VersionController("1.0"))
                .isInstanceOf(IllegalStateException.class);
    }
}
