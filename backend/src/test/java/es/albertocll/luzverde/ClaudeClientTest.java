package es.albertocll.luzverde;

import com.fasterxml.jackson.databind.JsonNode;
import es.albertocll.luzverde.ia.ClaudeClient;
import es.albertocll.luzverde.ia.ClaudeProperties;
import es.albertocll.luzverde.ia.IaException;
import es.albertocll.luzverde.ia.IaNoConfiguradaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Prueba el cliente contra un servidor simulado. No sale nada a internet ni gasta créditos.
 */
class ClaudeClientTest {

    private static final String URL = "https://api.ejemplo.test/v1/messages";
    private static final Map<String, Object> ESQUEMA = Map.of("type", "object", "properties", Map.of());

    private MockRestServiceServer servidor;
    private ClaudeClient cliente;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        cliente = new ClaudeClient(new ClaudeProperties("clave-falsa", "modelo-x", 512, URL, 10), builder);
    }

    @Test
    void enviaLaPeticionCorrectaYDevuelveLaEntradaDeLaHerramienta() {
        servidor.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-api-key", "clave-falsa"))
                .andExpect(header("anthropic-version", "2023-06-01"))
                .andExpect(jsonPath("$.model").value("modelo-x"))
                .andExpect(jsonPath("$.max_tokens").value(512))
                .andExpect(jsonPath("$.tool_choice.name").value("mi_herramienta"))
                .andExpect(jsonPath("$.messages[0].content").value("hola"))
                .andRespond(withSuccess("""
                        {"stop_reason": "tool_use",
                         "content": [
                           {"type": "text", "text": "Vale"},
                           {"type": "tool_use", "name": "mi_herramienta", "input": {"semaforo": "VERDE"}}
                         ]}
                        """, MediaType.APPLICATION_JSON));

        JsonNode entrada = cliente.llamarHerramienta("sistema", "hola", "mi_herramienta", "desc", ESQUEMA);

        assertThat(entrada.path("semaforo").asText()).isEqualTo("VERDE");
        servidor.verify();
    }

    @Test
    void errorDelServidorSeConvierteEnIaException() {
        servidor.expect(requestTo(URL)).andRespond(withServerError());

        assertThatThrownBy(() -> cliente.llamarHerramienta("s", "m", "h", "d", ESQUEMA))
                .isInstanceOf(IaException.class);
    }

    @Test
    void respuestaSinHerramientaEsUnError() {
        servidor.expect(requestTo(URL)).andRespond(withSuccess("""
                {"stop_reason": "end_turn", "content": [{"type": "text", "text": "Hola"}]}
                """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> cliente.llamarHerramienta("s", "m", "h", "d", ESQUEMA))
                .isInstanceOf(IaException.class);
    }

    @Test
    void respuestaCortadaPorTokensEsUnError() {
        servidor.expect(requestTo(URL)).andRespond(withSuccess("""
                {"stop_reason": "max_tokens",
                 "content": [{"type": "tool_use", "name": "h", "input": {}}]}
                """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> cliente.llamarHerramienta("s", "m", "h", "d", ESQUEMA))
                .isInstanceOf(IaException.class);
    }

    @Test
    void sinClaveNoLlamaANadie() {
        ClaudeClient sinClave = new ClaudeClient(new ClaudeProperties("", "m", 10, URL, 10), RestClient.builder());

        assertThat(sinClave.configurado()).isFalse();
        assertThatThrownBy(() -> sinClave.llamarHerramienta("s", "m", "h", "d", ESQUEMA))
                .isInstanceOf(IaNoConfiguradaException.class);
    }
}
