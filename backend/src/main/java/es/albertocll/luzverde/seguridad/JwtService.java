package es.albertocll.luzverde.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    /** Quién firma el token y para quién es. Un token de otro sistema con la misma clave no vale. */
    static final String EMISOR = "luz-verde";
    static final String DESTINATARIO = "luz-verde-app";

    private final SecretKey clave;
    private final long expiracionMinutos;

    public JwtService(JwtProperties propiedades) {
        if (propiedades.secreto() == null || propiedades.secreto().isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno JWT_SECRET");
        }
        byte[] bytes = Decoders.BASE64.decode(propiedades.secreto());
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 bytes en Base64");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.expiracionMinutos = propiedades.expiracionMinutos();
    }

    public String generarToken(Long usuarioId, String email) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .issuer(EMISOR)
                .audience().add(DESTINATARIO).and()
                .subject(String.valueOf(usuarioId))
                .claim("email", email)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(expiracionMinutos, ChronoUnit.MINUTES)))
                .signWith(clave)
                .compact();
    }

    public long getExpiracionSegundos() {
        return expiracionMinutos * 60;
    }

    public Optional<UsuarioAutenticado> validar(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(clave)
                    .requireIssuer(EMISOR)
                    .requireAudience(DESTINATARIO)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Long id = Long.valueOf(claims.getSubject());
            String email = claims.get("email", String.class);
            return Optional.of(new UsuarioAutenticado(id, email));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
