package es.albertocll.luzverde.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita las altas de cuenta por IP.
 *
 * <p>Cuenta cada petición de registro, se cree la cuenta o no. El registro responde igual en los
 * dos casos, así que contar solo las altas reales delataría qué emails existen. Con el límite
 * superado responde 429 hasta que caduquen los intentos de la ventana.</p>
 *
 * <p>Igual que {@link LimiteLoginService}, guarda los datos en memoria y vale para una sola
 * instancia. Cuando hay más de {@value #LIMPIAR_A_PARTIR_DE} IPs se borran las caducadas.</p>
 */
@Service
public class LimiteRegistroService {

    static final int LIMPIAR_A_PARTIR_DE = 10_000;

    private volatile Instant ultimaLimpieza = Instant.EPOCH;
    private final Map<String, Deque<Instant>> intentos = new ConcurrentHashMap<>();
    private final Clock reloj;
    private final int maxPorIp;
    private final Duration ventana;

    public LimiteRegistroService(Clock reloj,
                                 @Value("${luzverde.registro.max-por-ip:10}") int maxPorIp,
                                 @Value("${luzverde.registro.ventana-minutos:60}") long ventanaMinutos) {
        this.reloj = reloj;
        this.maxPorIp = maxPorIp;
        this.ventana = Duration.ofMinutes(ventanaMinutos);
    }

    /** Apunta el intento y lanza 429 si la IP ya ha llegado al límite. */
    public void comprobarYRegistrar(String ip) {
        Instant ahora = reloj.instant();
        Deque<Instant> cola = intentos.computeIfAbsent(ip, k -> new ArrayDeque<>());
        synchronized (cola) {
            quitarCaducados(cola, ahora);
            if (cola.size() >= maxPorIp) {
                throw new DemasiadosIntentosException(ventana.toMinutes());
            }
            cola.add(ahora);
        }
        if (intentos.size() > LIMPIAR_A_PARTIR_DE && ahora.isAfter(ultimaLimpieza.plusSeconds(60))) {
            ultimaLimpieza = ahora;
            limpiarCaducados(ahora);
        }
    }

    /** Número de IPs guardadas. Solo para tests. */
    int ipsGuardadas() {
        return intentos.size();
    }

    private void limpiarCaducados(Instant ahora) {
        intentos.forEach((ip, cola) -> {
            synchronized (cola) {
                quitarCaducados(cola, ahora);
                if (cola.isEmpty()) {
                    intentos.remove(ip, cola);
                }
            }
        });
    }

    private void quitarCaducados(Deque<Instant> cola, Instant ahora) {
        Instant limite = ahora.minus(ventana);
        while (!cola.isEmpty() && cola.peekFirst().isBefore(limite)) {
            cola.pollFirst();
        }
    }
}
