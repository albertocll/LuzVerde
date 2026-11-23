package es.albertocll.luzverde.ia;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Lectura defensiva de la salida del modelo: recorta longitudes y descarta vacíos.
 */
public final class LectorJson {

    private LectorJson() {
    }

    public static String texto(JsonNode nodo, String campo, int maximo) {
        String valor = nodo.path(campo).asText("").trim();
        if (valor.isEmpty()) {
            return null;
        }
        return valor.length() > maximo ? valor.substring(0, maximo) : valor;
    }

    public static List<String> textos(JsonNode nodo, String campo, int maxElementos, int maxLongitud) {
        List<String> resultado = new ArrayList<>();
        for (JsonNode elemento : nodo.path(campo)) {
            String valor = elemento.asText("").trim();
            if (!valor.isEmpty() && resultado.size() < maxElementos) {
                resultado.add(valor.length() > maxLongitud ? valor.substring(0, maxLongitud) : valor);
            }
        }
        return resultado;
    }
}
