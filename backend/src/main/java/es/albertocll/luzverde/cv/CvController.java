package es.albertocll.luzverde.cv;

import es.albertocll.luzverde.cv.CvDtos.CvResponse;
import es.albertocll.luzverde.evaluacion.EvaluacionDtos.OfertaRequest;
import es.albertocll.luzverde.seguridad.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/cv")
@Tag(name = "CV")
public class CvController {

    static final MediaType DOCX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private final CvService service;

    public CvController(CvService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Genera un CV adaptado a la oferta (PDF y DOCX) y una carta de presentación")
    public CvResponse generar(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
                              @Valid @RequestBody OfertaRequest peticion) {
        return service.generar(usuario.id(), peticion.ofertaTexto());
    }

    @GetMapping
    @Operation(summary = "CV generados por el usuario")
    public List<CvResponse> listar(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return service.listar(usuario.id());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Datos de un CV generado")
    public CvResponse obtener(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
                              @PathVariable Long id) {
        return service.obtener(usuario.id(), id);
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Descarga el CV en PDF")
    public ResponseEntity<byte[]> pdf(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
                                      @PathVariable Long id) {
        return archivo(service.pdf(usuario.id(), id), MediaType.APPLICATION_PDF, "cv-" + id + ".pdf");
    }

    @GetMapping("/{id}/docx")
    @Operation(summary = "Descarga el CV en Word")
    public ResponseEntity<byte[]> docx(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
                                       @PathVariable Long id) {
        return archivo(service.docx(usuario.id(), id), DOCX, "cv-" + id + ".docx");
    }

    private static ResponseEntity<byte[]> archivo(byte[] contenido, MediaType tipo, String nombre) {
        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombre).build().toString())
                .body(contenido);
    }
}
