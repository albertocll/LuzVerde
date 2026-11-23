package es.albertocll.luzverde.ia;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

/**
 * Guarda una lista de textos como JSON en una sola columna.
 */
@Converter
public class ListaTextoConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<List<String>> TIPO = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(List<String> lista) {
        try {
            return JSON.writeValueAsString(lista == null ? List.of() : lista);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String texto) {
        if (texto == null || texto.isBlank()) {
            return List.of();
        }
        try {
            return JSON.readValue(texto, TIPO);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
