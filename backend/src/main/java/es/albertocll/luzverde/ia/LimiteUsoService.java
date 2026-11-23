package es.albertocll.luzverde.ia;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Límite de usos de IA por usuario y día natural (hora de Madrid).
 * Las evaluaciones y los CV comparten el mismo contador.
 */
@Service
public class LimiteUsoService {

    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");

    private final UsoIaRepository usos;
    private final ClaudeProperties propiedades;
    private final Clock reloj;

    public LimiteUsoService(UsoIaRepository usos, ClaudeProperties propiedades, Clock reloj) {
        this.usos = usos;
        this.propiedades = propiedades;
        this.reloj = reloj;
    }

    /**
     * Comprueba el límite y apunta el uso. Se guarda en su propia transacción para que cuente
     * aunque luego la llamada a la IA falle: el gasto ya se habrá producido.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void consumir(Long usuarioId, String tipo) {
        if (usadosHoy(usuarioId) >= propiedades.limiteDiario()) {
            throw new LimiteSuperadoException(propiedades.limiteDiario());
        }
        usos.save(new UsoIa(usuarioId, tipo));
    }

    @Transactional(readOnly = true)
    public long usadosHoy(Long usuarioId) {
        Instant inicioDelDia = LocalDate.now(reloj.withZone(ZONA)).atStartOfDay(ZONA).toInstant();
        return usos.countByUsuarioIdAndCreadoEnGreaterThanEqual(usuarioId, inicioDelDia);
    }

    public int limiteDiario() {
        return propiedades.limiteDiario();
    }
}
