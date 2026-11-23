package es.albertocll.luzverde.ia;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * Cliente mínimo de la API Messages de Anthropic.
 * Fuerza al modelo a responder llamando a una herramienta, así la salida siempre llega como JSON
 * con el esquema que pedimos.
 */
@Component
public class ClaudeClient {

    private static final Logger log = LoggerFactory.getLogger(ClaudeClient.class);
    private static final String VERSION_API = "2023-06-01";

    private final ClaudeProperties propiedades;
    private final RestClient http;

    public ClaudeClient(ClaudeProperties propiedades, RestClient.Builder builder) {
        this.propiedades = propiedades;
        this.http = builder.build();
    }

    public boolean configurado() {
        return propiedades.apiKey() != null && !propiedades.apiKey().isBlank();
    }

    /**
     * @param sistema     instrucciones de sistema
     * @param mensaje     mensaje del usuario
     * @param herramienta nombre de la herramienta que el modelo debe llamar
     * @param descripcion descripción de la herramienta
     * @param esquema     JSON Schema de la entrada de la herramienta
     * @return la entrada con la que el modelo llamó a la herramienta
     */
    public JsonNode llamarHerramienta(String sistema, String mensaje, String herramienta,
                                      String descripcion, Map<String, Object> esquema) {
        if (!configurado()) {
            throw new IaNoConfiguradaException();
        }

        Map<String, Object> cuerpo = Map.of(
                "model", propiedades.modelo(),
                "max_tokens", propiedades.maxTokens(),
                "system", sistema,
                "messages", List.of(Map.of("role", "user", "content", mensaje)),
                "tools", List.of(Map.of(
                        "name", herramienta,
                        "description", descripcion,
                        "input_schema", esquema)),
                "tool_choice", Map.of("type", "tool", "name", herramienta));

        JsonNode respuesta;
        try {
            respuesta = http.post()
                    .uri(propiedades.url())
                    .header("x-api-key", propiedades.apiKey())
                    .header("anthropic-version", VERSION_API)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            log.warn("Fallo llamando a la API de Claude: {}", e.getMessage());
            throw new IaException("No se ha podido contactar con el servicio de IA", e);
        }

        if (respuesta == null) {
            throw new IaException("El servicio de IA no ha devuelto nada");
        }
        if ("max_tokens".equals(respuesta.path("stop_reason").asText())) {
            throw new IaException("La respuesta de la IA se ha cortado por el tope de tokens");
        }
        for (JsonNode bloque : respuesta.path("content")) {
            if ("tool_use".equals(bloque.path("type").asText())
                    && herramienta.equals(bloque.path("name").asText())) {
                return bloque.path("input");
            }
        }
        throw new IaException("La respuesta de la IA no tiene el formato esperado");
    }
}
