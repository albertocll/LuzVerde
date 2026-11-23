package es.albertocll.luzverde.seguridad;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "luzverde.jwt")
public record JwtProperties(String secreto, long expiracionMinutos) {
}
