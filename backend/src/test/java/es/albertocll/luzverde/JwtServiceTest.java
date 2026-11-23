package es.albertocll.luzverde;

import es.albertocll.luzverde.seguridad.JwtProperties;
import es.albertocll.luzverde.seguridad.JwtService;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static String claveDe(int bytes) {
        return Base64.getEncoder().encodeToString(new byte[bytes]);
    }

    @Test
    void generaYValidaUnToken() {
        JwtService servicio = new JwtService(new JwtProperties(claveDe(32), 60));
        String token = servicio.generarToken(7L, "ana@ejemplo.com");

        var usuario = servicio.validar(token);

        assertThat(usuario).isPresent();
        assertThat(usuario.get().id()).isEqualTo(7L);
        assertThat(usuario.get().email()).isEqualTo("ana@ejemplo.com");
    }

    @Test
    void rechazaTokenFirmadoConOtraClave() {
        byte[] otra = new byte[32];
        otra[0] = 1;
        JwtService emisor = new JwtService(new JwtProperties(Base64.getEncoder().encodeToString(otra), 60));
        JwtService receptor = new JwtService(new JwtProperties(claveDe(32), 60));

        assertThat(receptor.validar(emisor.generarToken(1L, "a@b.com"))).isEmpty();
        assertThat(receptor.validar("basura")).isEmpty();
    }

    @Test
    void rechazaTokenCaducado() {
        JwtService servicio = new JwtService(new JwtProperties(claveDe(32), -1));
        assertThat(servicio.validar(servicio.generarToken(1L, "a@b.com"))).isEmpty();
    }

    @Test
    void exigeClaveSuficientementeLarga() {
        assertThatThrownBy(() -> new JwtService(new JwtProperties(claveDe(16), 60)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new JwtService(new JwtProperties(null, 60)))
                .isInstanceOf(IllegalStateException.class);
    }
}
