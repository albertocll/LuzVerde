package es.albertocll.luzverde.ia;

/**
 * Fallo al hablar con la API de Claude o respuesta que no se puede usar.
 */
public class IaException extends RuntimeException {

    public IaException(String mensaje) {
        super(mensaje);
    }

    public IaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
