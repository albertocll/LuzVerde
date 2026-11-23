package es.albertocll.luzverde.ia;

public class LimiteSuperadoException extends RuntimeException {

    public LimiteSuperadoException(int limite) {
        super("Has llegado al límite de " + limite + " usos de IA por hoy. Vuelve a intentarlo mañana.");
    }
}
