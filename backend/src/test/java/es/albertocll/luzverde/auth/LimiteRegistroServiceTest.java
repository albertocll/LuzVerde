package es.albertocll.luzverde.auth;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LimiteRegistroServiceTest {

    private final AtomicReference<Instant> ahora = new AtomicReference<>(Instant.parse("2026-11-25T10:00:00Z"));

    private final Clock reloj = new Clock() {
        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora.get();
        }
    };

    private final LimiteRegistroService limite = new LimiteRegistroService(reloj, 3, 60);

    @Test
    void bloqueaAlCuartoIntentoYSeLiberaAlPasarLaVentana() {
        for (int i = 0; i < 3; i++) {
            limite.comprobarYRegistrar("203.0.113.1");
        }
        assertThatThrownBy(() -> limite.comprobarYRegistrar("203.0.113.1"))
                .isInstanceOf(DemasiadosIntentosException.class);

        ahora.set(ahora.get().plus(Duration.ofMinutes(61)));
        assertThatCode(() -> limite.comprobarYRegistrar("203.0.113.1")).doesNotThrowAnyException();
    }

    @Test
    void cadaIpTieneSuPropioContador() {
        for (int i = 0; i < 3; i++) {
            limite.comprobarYRegistrar("203.0.113.1");
        }
        assertThatCode(() -> limite.comprobarYRegistrar("203.0.113.2")).doesNotThrowAnyException();
    }

    @Test
    void losIntentosRechazadosNoAlarganElBloqueo() {
        for (int i = 0; i < 3; i++) {
            limite.comprobarYRegistrar("203.0.113.1");
        }
        ahora.set(ahora.get().plus(Duration.ofMinutes(30)));
        assertThatThrownBy(() -> limite.comprobarYRegistrar("203.0.113.1"))
                .isInstanceOf(DemasiadosIntentosException.class);

        ahora.set(ahora.get().plus(Duration.ofMinutes(31)));
        assertThatCode(() -> limite.comprobarYRegistrar("203.0.113.1")).doesNotThrowAnyException();
    }

    @Test
    void borraLasIpsCaducadasCuandoHayDemasiadas() {
        for (int i = 0; i <= LimiteRegistroService.LIMPIAR_A_PARTIR_DE; i++) {
            limite.comprobarYRegistrar("ip-" + i);
        }
        ahora.set(ahora.get().plus(Duration.ofMinutes(61)));
        limite.comprobarYRegistrar("ip-nueva");

        assertThat(limite.ipsGuardadas()).isEqualTo(1);
    }
}
