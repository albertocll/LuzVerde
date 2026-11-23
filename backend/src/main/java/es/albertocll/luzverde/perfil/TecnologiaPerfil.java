package es.albertocll.luzverde.perfil;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Una tecnología del stack del usuario con su nivel.
 */
@Embeddable
public class TecnologiaPerfil {

    @NotBlank(message = "El nombre de la tecnología es obligatorio")
    @Size(max = 60)
    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "El nivel es obligatorio")
    @Column(name = "nivel", nullable = false, length = 20)
    private NivelTecnologia nivel;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public NivelTecnologia getNivel() {
        return nivel;
    }

    public void setNivel(NivelTecnologia nivel) {
        this.nivel = nivel;
    }
}
