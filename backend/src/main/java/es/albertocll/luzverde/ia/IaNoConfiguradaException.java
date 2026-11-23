package es.albertocll.luzverde.ia;

public class IaNoConfiguradaException extends RuntimeException {

    public IaNoConfiguradaException() {
        super("El servidor no tiene configurada la clave de la API de Claude");
    }
}
