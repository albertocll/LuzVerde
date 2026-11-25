package es.albertocll.luzverde.cuenta;

import es.albertocll.luzverde.seguridad.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cuenta")
@Tag(name = "Cuenta")
public class CuentaController {

    private final CuentaService service;

    public CuentaController(CuentaService service) {
        this.service = service;
    }

    @GetMapping("/exportacion")
    @Operation(summary = "Descarga en JSON todos los datos de la cuenta",
            description = "Cuenta, perfil, candidaturas, evaluaciones con su oferta, CV y usos de IA. "
                    + "Los PDF y DOCX se descargan en los enlaces de cada CV.")
    public ResponseEntity<Exportacion> exportar(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        Exportacion datos = service.exportar(usuario.id());
        String nombre = "luz-verde-datos-" + datos.exportadoEn().toString().substring(0, 10) + ".json";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombre).build().toString())
                .body(datos);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Borra la cuenta y todos sus datos: perfil, candidaturas, evaluaciones, CV y usos de IA")
    public void borrar(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        service.borrar(usuario.id());
    }
}
