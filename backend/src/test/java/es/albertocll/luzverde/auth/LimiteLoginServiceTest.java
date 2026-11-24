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

class LimiteLoginServiceTest {

    private final AtomicReference<Instant> ahora = new AtomicReference<>(Instant.parse("2026-11-24T10:00:00Z"));

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

    private final LimiteLoginService limite = new LimiteLoginService(reloj, 5, 20, 15);

    @Test
    void bloqueaTrasCincoFallosYSeLiberaAlPasarLaVentana() {
        for (int i = 0; i < 5; i++) {
            limite.registrarFallo("ana@ejemplo.com", "10.0.0.1");
        }
        assertThatThrownBy(() -> limite.comprobar("ana@ejemplo.com", "10.0.0.2"))
                .isInstanceOf(DemasiadosIntentosException.class);

        ahora.set(ahora.get().plus(Duration.ofMinutes(16)));
        assertThatCode(() -> limite.comprobar("ana@ejemplo.com", "10.0.0.2")).doesNotThrowAnyException();
    }

    @Test
    void probarMilesDeEmailsNoLlenaLaMemoria() {
        // Un atacante prueba un email distinto en cada intento desde muchas IP.
        for (int i = 0; i < 30_000; i++) {
            limite.registrarFallo("victima" + i + "@ejemplo.com", "10.0." + (i / 250) + "." + (i % 250));
            ahora.set(ahora.get().plusMillis(100));
        }
        // 30.000 intentos a 10 por segundo son 50 minutos y crean 60.000 claves. Solo deben quedar
        // las de los últimos 15 minutos (18.000) más lo acumulado desde la última limpieza.
        assertThat(limite.clavesGuardadas()).isLessThan(20_000);
    }
}
