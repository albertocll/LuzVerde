package es.albertocll.luzverde.perfil;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Un puesto de trabajo o unas prácticas. Si fin es null, sigue en curso.
 */
@Embeddable
public class Experiencia {

    @NotBlank(message = "El puesto es obligatorio")
    @Size(max = 150)
    @Column(name = "puesto", nullable = false, length = 150)
    private String puesto;

    @NotBlank(message = "La empresa es obligatoria")
    @Size(max = 150)
    @Column(name = "empresa", nullable = false, length = 150)
    private String empresa;

    @NotBlank(message = "La fecha de inicio es obligatoria")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "Usa el formato AAAA-MM")
    @Column(name = "inicio", nullable = false, length = 7)
    private String inicio;

    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "Usa el formato AAAA-MM")
    @Column(name = "fin", length = 7)
    private String fin;

    @Size(max = 2000)
    @Column(name = "descripcion", length = 2000)
    private String descripcion;

    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getInicio() {
        return inicio;
    }

    public void setInicio(String inicio) {
        this.inicio = inicio;
    }

    public String getFin() {
        return fin;
    }

    public void setFin(String fin) {
        this.fin = fin;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
