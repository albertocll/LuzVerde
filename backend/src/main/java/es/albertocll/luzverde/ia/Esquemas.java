package es.albertocll.luzverde.ia;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ayudas para escribir JSON Schema como mapas sin ruido.
 */
public final class Esquemas {

    private Esquemas() {
    }

    public static Map<String, Object> texto(String descripcion) {
        return Map.of("type", "string", "description", descripcion);
    }

    public static Map<String, Object> entero(String descripcion) {
        return Map.of("type", "integer", "description", descripcion);
    }

    public static Map<String, Object> opciones(String descripcion, List<String> valores) {
        return Map.of("type", "string", "description", descripcion, "enum", valores);
    }

    public static Map<String, Object> listaDeTextos(String descripcion) {
        return Map.of("type", "array", "description", descripcion, "items", Map.of("type", "string"));
    }

    public static Map<String, Object> listaDe(String descripcion, Map<String, Object> elemento) {
        return Map.of("type", "array", "description", descripcion, "items", elemento);
    }

    /** Propiedades en orden: nombre, esquema, nombre, esquema... */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> propiedades(Object... pares) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        for (int i = 0; i < pares.length; i += 2) {
            mapa.put((String) pares[i], (Map<String, Object>) pares[i + 1]);
        }
        return mapa;
    }

    /** Objeto en el que todas las propiedades son obligatorias. */
    public static Map<String, Object> objeto(Map<String, Object> propiedades) {
        Map<String, Object> esquema = new LinkedHashMap<>();
        esquema.put("type", "object");
        esquema.put("properties", propiedades);
        esquema.put("required", List.copyOf(propiedades.keySet()));
        return esquema;
    }
}
