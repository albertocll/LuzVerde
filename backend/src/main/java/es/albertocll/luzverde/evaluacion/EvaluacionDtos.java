package es.albertocll.luzverde.evaluacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public final class EvaluacionDtos {

    private EvaluacionDtos() {
    }

    public record OfertaRequest(
            @NotBlank(message = "Pega el texto de la oferta")
            @Size(min = 80, max = 15000, message = "La oferta debe tener entre 80 y 15.000 caracteres")
            String ofertaTexto) {
    }

    public record EvaluacionResponse(
            Long id,
            String empresa,
            String puesto,
            Semaforo semaforo,
            String resumen,
            List<String> pros,
            List<String> huecos,
            List<String> banderasRojas,
            String accionRecomendada,
            Instant creadoEn,
            long usosRestantesHoy) {

        static EvaluacionResponse de(Evaluacion e, long usosRestantesHoy) {
            return new EvaluacionResponse(
                    e.getId(), e.getEmpresa(), e.getPuesto(), e.getSemaforo(), e.getResumen(),
                    e.getPros(), e.getHuecos(), e.getBanderasRojas(), e.getAccionRecomendada(),
                    e.getCreadoEn(), usosRestantesHoy);
        }
    }

    public record EvaluacionResumen(Long id, String empresa, String puesto, Semaforo semaforo, Instant creadoEn) {

        static EvaluacionResumen de(Evaluacion e) {
            return new EvaluacionResumen(e.getId(), e.getEmpresa(), e.getPuesto(), e.getSemaforo(), e.getCreadoEn());
        }
    }
}
