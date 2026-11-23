package es.albertocll.luzverde.perfil;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Perfil completo. Se usa igual para leer (GET) y para guardar (PUT).
 * En el PUT, las listas sustituyen a las anteriores.
 */
public record PerfilDto(
        @Size(max = 150)
        String titular,

        @Size(max = 30)
        String telefono,

        @Size(max = 200)
        String linkedin,

        @Size(max = 200)
        String github,

        @Min(value = 0, message = "El salario no puede ser negativo")
        @Max(value = 1_000_000)
        Integer salarioMinimo,

        Modalidad modalidad,

        @Size(max = 150)
        String ubicacion,

        @Size(max = 100) List<@Valid TecnologiaPerfil> tecnologias,
        @Size(max = 30) List<@Valid Experiencia> experiencias,
        @Size(max = 30) List<@Valid Formacion> formaciones,
        @Size(max = 30) List<@Valid Proyecto> proyectos,
        @Size(max = 20) List<@Valid Idioma> idiomas) {

    static PerfilDto vacio() {
        return new PerfilDto(null, null, null, null, null, null, null, List.of(), List.of(), List.of(), List.of(), List.of());
    }

    static PerfilDto de(Perfil p) {
        return new PerfilDto(
                p.getTitular(),
                p.getTelefono(),
                p.getLinkedin(),
                p.getGithub(),
                p.getSalarioMinimo(),
                p.getModalidad(),
                p.getUbicacion(),
                List.copyOf(p.getTecnologias()),
                List.copyOf(p.getExperiencias()),
                List.copyOf(p.getFormaciones()),
                List.copyOf(p.getProyectos()),
                List.copyOf(p.getIdiomas()));
    }
}
