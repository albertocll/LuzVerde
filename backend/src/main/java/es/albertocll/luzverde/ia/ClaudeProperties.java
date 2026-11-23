package es.albertocll.luzverde.ia;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de la API de Claude. La clave solo llega por la variable de entorno ANTHROPIC_API_KEY.
 *
 * @param apiKey       clave de la API (vacía si el servidor no tiene IA configurada)
 * @param modelo       modelo a usar
 * @param maxTokens    tope de tokens de salida por llamada
 * @param url          endpoint de Messages
 * @param limiteDiario usos de IA por usuario y día (evaluaciones y CV juntos)
 */
@ConfigurationProperties(prefix = "luzverde.claude")
public record ClaudeProperties(String apiKey, String modelo, int maxTokens, String url, int limiteDiario) {
}
