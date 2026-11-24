package es.albertocll.luzverde.perfil;

import es.albertocll.luzverde.seguridad.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/perfil")
@Tag(name = "Perfil")
public class PerfilController {

    private final PerfilService service;

    public PerfilController(PerfilService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Devuelve el perfil del usuario (vacío si aún no lo ha rellenado)")
    public PerfilDto obtener(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return service.obtener(usuario.id());
    }

    @PutMapping
    @Operation(summary = "Crea o sustituye el perfil del usuario")
    public PerfilDto guardar(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
                             @Valid @RequestBody PerfilDto datos) {
        return service.guardar(usuario.id(), datos);
    }
}
