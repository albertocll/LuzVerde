package es.albertocll.luzverde.candidatura;

import es.albertocll.luzverde.candidatura.CandidaturaDtos.CandidaturaRequest;
import es.albertocll.luzverde.candidatura.CandidaturaDtos.CandidaturaResponse;
import es.albertocll.luzverde.seguridad.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/candidaturas")
@Tag(name = "Candidaturas")
public class CandidaturaController {

    private final CandidaturaService service;

    public CandidaturaController(CandidaturaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista las candidaturas del usuario, con filtro opcional por estado")
    public List<CandidaturaResponse> listar(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
                                            @RequestParam(required = false) EstadoCandidatura estado) {
        return service.listar(usuario.id(), estado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Devuelve una candidatura del usuario")
    public CandidaturaResponse obtener(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
                                       @PathVariable Long id) {
        return service.obtener(usuario.id(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea una candidatura")
    public CandidaturaResponse crear(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
                                     @Valid @RequestBody CandidaturaRequest peticion) {
        return service.crear(usuario.id(), peticion);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza una candidatura")
    public CandidaturaResponse actualizar(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
                                          @PathVariable Long id,
                                          @Valid @RequestBody CandidaturaRequest peticion) {
        return service.actualizar(usuario.id(), id, peticion);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Borra una candidatura")
    public void borrar(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
                       @PathVariable Long id) {
        service.borrar(usuario.id(), id);
    }
}
