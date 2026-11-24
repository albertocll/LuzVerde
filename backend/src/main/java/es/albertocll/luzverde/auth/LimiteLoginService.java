package es.albertocll.luzverde.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Frena los ataques de fuerza bruta contra el login.
 *
 * <p>Cuenta los fallos recientes por email y por IP en memoria. Si se pasa del límite dentro de la
 * ventana, el login responde 429 aunque la contraseña sea correcta, hasta que caduquen los fallos.
 * Al ser memoria local vale para una sola instancia, que es lo que tiene este proyecto.</p>
 *
 * <p>Para que nadie pueda llenar la memoria probando miles de emails distintos, cuando hay más de
 * {@value #LIMPIAR_A_PARTIR_DE} claves se borran las que ya no tienen fallos dentro de la ventana,
 * como mucho una vez por minuto.</p>
 */
@Service
public class LimiteLoginService {

    static final int LIMPIAR_A_PARTIR_DE = 10_000;

    private volatile Instant ultimaLimpieza = Instant.EPOCH;
    private final Map<String, Deque<Instant>> fallos = new ConcurrentHashMap<>();
    private final Clock reloj;
    private final int maxPorEmail;
    private final int maxPorIp;
    private final Duration ventana;

    public LimiteLoginService(Clock reloj,
                              @Value("${luzverde.login.max-fallos-email:5}") int maxPorEmail,
                              @Value("${luzverde.login.max-fallos-ip:20}") int maxPorIp,
                              @Value("${luzverde.login.ventana-minutos:15}") long ventanaMinutos) {
        this.reloj = reloj;
        this.maxPorEmail = maxPorEmail;
        this.maxPorIp = maxPorIp;
        this.ventana = Duration.ofMinutes(ventanaMinutos);
    }

    public void comprobar(String email, String ip) {
        if (recientes(claveEmail(email)) >= maxPorEmail || recientes(claveIp(ip)) >= maxPorIp) {
            throw new DemasiadosIntentosException(ventana.toMinutes());
        }
    }

    public void registrarFallo(String email, String ip) {
        Instant ahora = reloj.instant();
        fallos.computeIfAbsent(claveEmail(email), k -> new ArrayDeque<>()).add(ahora);
        fallos.computeIfAbsent(claveIp(ip), k -> new ArrayDeque<>()).add(ahora);
        if (fallos.size() > LIMPIAR_A_PARTIR_DE && ahora.isAfter(ultimaLimpieza.plusSeconds(60))) {
            ultimaLimpieza = ahora;
            limpiarCaducados();
        }
    }

    /** Número de claves guardadas. Solo para tests. */
    int clavesGuardadas() {
        return fallos.size();
    }

    private void limpiarCaducados() {
        for (String clave : fallos.keySet()) {
            if (recientes(clave) == 0) {
                fallos.remove(clave);
            }
        }
    }

    public void registrarExito(String email) {
        fallos.remove(claveEmail(email));
    }

    private int recientes(String clave) {
        Deque<Instant> cola = fallos.get(clave);
        if (cola == null) {
            return 0;
        }
        Instant limite = reloj.instant().minus(ventana);
        synchronized (cola) {
            while (!cola.isEmpty() && cola.peekFirst().isBefore(limite)) {
                cola.pollFirst();
            }
            return cola.size();
        }
    }

    private static String claveEmail(String email) {
        return "email:" + (email == null ? "" : email.trim().toLowerCase(Locale.ROOT));
    }

    private static String claveIp(String ip) {
        return "ip:" + ip;
    }
}
