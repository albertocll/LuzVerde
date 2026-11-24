package es.albertocll.luzverde.error;

import es.albertocll.luzverde.auth.DemasiadosIntentosException;
import es.albertocll.luzverde.ia.IaException;
import es.albertocll.luzverde.ia.IaNoConfiguradaException;
import es.albertocll.luzverde.ia.LimiteSuperadoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);
    private static final String CODIGO = "code";

    @ExceptionHandler(NoEncontradoException.class)
    public ProblemDetail noEncontrado(NoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(ConflictoException.class)
    public ProblemDetail conflicto(ConflictoException e) {
        return problema(HttpStatus.CONFLICT, e.getCodigo(), e.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ProblemDetail credenciales(CredencialesInvalidasException e) {
        return problema(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", e.getMessage());
    }

    @ExceptionHandler(DemasiadosIntentosException.class)
    public ProblemDetail demasiadosIntentos(DemasiadosIntentosException e) {
        return problema(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_ATTEMPTS", e.getMessage());
    }

    @ExceptionHandler(LimiteSuperadoException.class)
    public ProblemDetail limite(LimiteSuperadoException e) {
        return problema(HttpStatus.TOO_MANY_REQUESTS, "AI_DAILY_LIMIT", e.getMessage());
    }

    @ExceptionHandler(IaNoConfiguradaException.class)
    public ProblemDetail iaNoConfigurada(IaNoConfiguradaException e) {
        return problema(HttpStatus.SERVICE_UNAVAILABLE, "AI_NOT_CONFIGURED", e.getMessage());
    }

    @ExceptionHandler(IaException.class)
    public ProblemDetail ia(IaException e) {
        return problema(HttpStatus.BAD_GATEWAY, "AI_FAILED", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacion(MethodArgumentNotValidException e) {
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Datos no válidos");
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(err -> campos.putIfAbsent(err.getField(), err.getDefaultMessage()));
        problema.setProperty("campos", campos);
        return problema;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail peticionIlegible(Exception e) {
        return problema(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "La petición tiene un formato incorrecto");
    }

    /**
     * Último recurso: nunca se devuelve la traza ni el mensaje interno al cliente.
     * Las excepciones propias de Spring MVC (404, 405...) conservan su código.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail inesperado(Exception e) {
        if (e instanceof ErrorResponse respuesta) {
            ProblemDetail cuerpo = respuesta.getBody();
            HttpStatus estado = HttpStatus.resolve(cuerpo.getStatus());
            cuerpo.setProperty(CODIGO, estado != null ? estado.name() : "HTTP_" + cuerpo.getStatus());
            return cuerpo;
        }
        log.error("Error no controlado", e);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Ha ocurrido un error inesperado. Inténtalo de nuevo más tarde.");
    }

    /**
     * Todas las respuestas de error llevan {@code code}: un texto fijo en MAYÚSCULAS que no cambia
     * aunque cambie el mensaje. La app elige su mensaje por {@code code}, nunca por el texto.
     */
    private static ProblemDetail problema(HttpStatus estado, String codigo, String mensaje) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, mensaje);
        problema.setProperty(CODIGO, codigo);
        return problema;
    }
}
