package es.albertocll.luzverde.version;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dice qué versión de la app es la mínima que funciona con este backend. Si se cambia la API
 * de forma que rompe versiones viejas, se sube APP_VERSION_MINIMA y la app pide actualizar.
 */
@RestController
@RequestMapping("/api/v1/version")
@Tag(name = "Versión")
@SecurityRequirements
public class VersionController {

    public record VersionResponse(String versionMinima) {
    }

    private final VersionResponse respuesta;

    public VersionController(@Value("${luzverde.app.version-minima}") String versionMinima) {
        if (!versionMinima.matches("\\d+\\.\\d+\\.\\d+")) {
            throw new IllegalStateException("APP_VERSION_MINIMA debe tener el formato 1.2.3");
        }
        this.respuesta = new VersionResponse(versionMinima);
    }

    @GetMapping
    @Operation(summary = "Versión mínima de la app que admite el backend")
    public VersionResponse obtener() {
        return respuesta;
    }
}
