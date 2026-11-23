package es.albertocll.luzverde.candidatura;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public final class CandidaturaDtos {

    private CandidaturaDtos() {
    }

    public record CandidaturaRequest(
            @NotBlank(message = "La empresa es obligatoria")
            @Size(max = 150)
            String empresa,

            @NotBlank(message = "El puesto es obligatorio")
            @Size(max = 150)
            String puesto,

            @Size(max = 500)
            String enlace,

            @NotNull(message = "El estado es obligatorio")
            EstadoCandidatura estado,

            @NotNull(message = "La fecha es obligatoria")
            LocalDate fecha,

            @Size(max = 4000)
            String notas) {
    }

    public record CandidaturaResponse(
            Long id,
            String empresa,
            String puesto,
            String enlace,
            EstadoCandidatura estado,
            LocalDate fecha,
            String notas) {

        static CandidaturaResponse de(Candidatura c) {
            return new CandidaturaResponse(
                    c.getId(), c.getEmpresa(), c.getPuesto(), c.getEnlace(),
                    c.getEstado(), c.getFecha(), c.getNotas());
        }
    }
}
