package es.albertocll.luzverde.error;

public class ConflictoException extends RuntimeException {

    private final String codigo;

    /** @param codigo código fijo en MAYÚSCULAS que la app usa para elegir su mensaje, como EMAIL_TAKEN */
    public ConflictoException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
