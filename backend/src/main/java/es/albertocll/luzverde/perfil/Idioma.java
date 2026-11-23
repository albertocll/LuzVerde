package es.albertocll.luzverde.perfil;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Un idioma y su nivel en texto libre (por ejemplo, B1).
 */
@Embeddable
public class Idioma {

    @NotBlank(message = "El idioma es obligatorio")
    @Size(max = 60)
    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;

    @Size(max = 60)
    @Column(name = "nivel", length = 60)
    private String nivel;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }
}
