package es.albertocll.luzverde.perfil;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Un proyecto propio. tecnologias es texto libre separado por comas.
 */
@Embeddable
public class Proyecto {

    @NotBlank(message = "El nombre del proyecto es obligatorio")
    @Size(max = 150)
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Size(max = 2000)
    @Column(name = "descripcion", length = 2000)
    private String descripcion;

    @Size(max = 500)
    @Column(name = "enlace", length = 500)
    private String enlace;

    @Size(max = 500)
    @Column(name = "tecnologias", length = 500)
    private String tecnologias;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEnlace() {
        return enlace;
    }

    public void setEnlace(String enlace) {
        this.enlace = enlace;
    }

    public String getTecnologias() {
        return tecnologias;
    }

    public void setTecnologias(String tecnologias) {
        this.tecnologias = tecnologias;
    }
}
