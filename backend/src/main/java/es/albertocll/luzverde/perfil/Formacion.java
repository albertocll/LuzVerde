package es.albertocll.luzverde.perfil;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Un título o curso. Si fin es null, sigue en curso.
 */
@Embeddable
public class Formacion {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200)
    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Size(max = 150)
    @Column(name = "centro", length = 150)
    private String centro;

    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "Usa el formato AAAA-MM")
    @Column(name = "inicio", length = 7)
    private String inicio;

    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "Usa el formato AAAA-MM")
    @Column(name = "fin", length = 7)
    private String fin;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCentro() {
        return centro;
    }

    public void setCentro(String centro) {
        this.centro = centro;
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
}
