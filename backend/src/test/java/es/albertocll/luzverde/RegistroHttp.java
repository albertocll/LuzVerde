package es.albertocll.luzverde;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

/** Registro por HTTP real, pasando por Tomcat. MockMvc no lee X-Forwarded-For. */
final class RegistroHttp {

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private RegistroHttp() {
    }

    static int registrarDesde(int puerto, String forwardedFor) throws Exception {
        String cuerpo = """
                {"email": "test-%s@ejemplo.com", "password": "contrasena-segura", "nombre": "Prueba"}
                """.formatted(UUID.randomUUID());
        HttpRequest peticion = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + puerto + "/api/v1/auth/registro"))
                .header("Content-Type", "application/json")
                .header("X-Forwarded-For", forwardedFor)
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                .build();
        return HTTP.send(peticion, HttpResponse.BodyHandlers.discarding()).statusCode();
    }
}
