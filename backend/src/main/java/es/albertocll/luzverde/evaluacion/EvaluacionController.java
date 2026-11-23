package es.albertocll.luzverde.evaluacion;

import es.albertocll.luzverde.evaluacion.EvaluacionDtos.EvaluacionResponse;
import es.albertocll.luzverde.evaluacion.EvaluacionDtos.EvaluacionResumen;
import es.albertocll.luzverde.evaluacion.EvaluacionDtos.OfertaRequest;
import es.albertocll.luzverde.seguridad.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/evaluaciones")
@Tag(name = "Evaluaciones")
public class EvaluacionController {

    private final EvaluacionService service;

    public EvaluacionController(EvaluacionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Evalúa una oferta con la IA usando el perfil del usuario")
    public EvaluacionResponse evaluar(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
                                      @Valid @RequestBody OfertaRequest peticion) {
        return service.evaluar(usuario.id(), peticion.ofertaTexto());
    }

    @GetMapping
    @Operation(summary = "Últimas evaluaciones del usuario")
    public List<EvaluacionResumen> listar(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return service.listar(usuario.id());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una evaluación")
    public EvaluacionResponse obtener(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
                                      @PathVariable Long id) {
        return service.obtener(usuario.id(), id);
    }
}
