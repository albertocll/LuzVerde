package es.albertocll.luzverde.auth;

public class DemasiadosIntentosException extends RuntimeException {

    public DemasiadosIntentosException(long minutos) {
        super("Demasiados intentos. Espera " + minutos + " minutos y vuelve a probar.");
    }
}
