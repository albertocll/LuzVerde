package es.albertocll.luzverde.cuenta;

import es.albertocll.luzverde.candidatura.CandidaturaDtos.CandidaturaResponse;
import es.albertocll.luzverde.evaluacion.Semaforo;
import es.albertocll.luzverde.perfil.PerfilDto;

import java.time.Instant;
import java.util.List;

/**
 * Todos los datos de un usuario, tal como se guardan. Es lo que devuelve "Exportar mis datos"
 * (derecho de acceso y portabilidad del RGPD). No incluye el hash de la contraseña.
 */
public record Exportacion(
        Instant exportadoEn,
        Cuenta cuenta,
        PerfilDto perfil,
        List<CandidaturaResponse> candidaturas,
        List<EvaluacionCompleta> evaluaciones,
        List<CvGenerado> cvs,
        List<UsoIa> usosIa) {

    public record Cuenta(String email, String nombre, Instant creadaEn) {
    }

    /** La evaluación con el texto de la oferta que pegó el usuario. */
    public record EvaluacionCompleta(
            Long id,
            String ofertaTexto,
            String empresa,
            String puesto,
            Semaforo semaforo,
            String resumen,
            List<String> pros,
            List<String> huecos,
            List<String> banderasRojas,
            String accionRecomendada,
            Instant creadoEn) {
    }

    /** Los archivos PDF y DOCX no van dentro: se descargan en sus enlaces mientras la cuenta exista. */
    public record CvGenerado(
            Long id,
            String empresa,
            String puesto,
            List<String> huecos,
            String carta,
            Instant creadoEn,
            String urlPdf,
            String urlDocx) {
    }

    public record UsoIa(String tipo, Instant creadoEn) {
    }
}
