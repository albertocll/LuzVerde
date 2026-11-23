package es.albertocll.luzverde;

import es.albertocll.luzverde.cv.GuardianVeracidad;
import es.albertocll.luzverde.perfil.NivelTecnologia;
import es.albertocll.luzverde.perfil.PerfilDto;
import es.albertocll.luzverde.perfil.Proyecto;
import es.albertocll.luzverde.perfil.TecnologiaPerfil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GuardianVeracidadTest {

    private static TecnologiaPerfil tec(String nombre, NivelTecnologia nivel) {
        TecnologiaPerfil t = new TecnologiaPerfil();
        t.setNombre(nombre);
        t.setNivel(nivel);
        return t;
    }

    private static PerfilDto perfil() {
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre("Juego 2D");
        proyecto.setDescripcion("Juego con backend en ASP.NET Core y Docker.");
        return new PerfilDto(null, null, null, null, null, null, null,
                List.of(tec("Java", NivelTecnologia.SOLIDO), tec("C#", NivelTecnologia.MEDIO),
                        tec("Docker", NivelTecnologia.SIN_TOCAR)),
                List.of(), List.of(), List.of(proyecto), List.of());
    }

    @Test
    void permiteLoDelPerfilYLoQueElUsuarioEscribio() {
        var guardian = new GuardianVeracidad(perfil(), List.of("Java", "C#", "ASP.NET Core", "Go", "Docker"));

        assertThat(guardian.permitida("java")).isTrue();
        assertThat(guardian.permitida("C#")).isTrue();
        assertThat(guardian.permitida("ASP.NET Core")).isTrue();
        assertThat(guardian.permitida("Go")).isFalse();
        // Marcada "sin tocar": no vale aunque aparezca en una descripción.
        assertThat(guardian.permitida("Docker")).isFalse();
        assertThat(guardian.prohibidas()).containsExactlyInAnyOrder("Go", "Docker");
    }

    @Test
    void quitaSoloLasFrasesQueNombranAlgoProhibido() {
        var guardian = new GuardianVeracidad(perfil(), List.of("Go", "Docker"));

        String limpio = guardian.limpiar("Programo en Java. Tengo experiencia con Go. Uso C# a diario.");

        assertThat(limpio).isEqualTo("Programo en Java. Uso C# a diario.");
        // "Google" contiene "Go" pero no es la palabra Go.
        assertThat(guardian.limpiar("Trabajé con Google Maps.")).isEqualTo("Trabajé con Google Maps.");
    }

    @Test
    void tecnologiasDelCvSalenSoloDelPerfil() {
        var guardian = new GuardianVeracidad(perfil(), List.of("Kotlin"));

        assertThat(guardian.tecnologias(List.of("Kotlin", "c#", "Docker")))
                .containsExactly("C#", "Java");
    }

    @Test
    void huecosIncluyenLoQueFaltaSinDuplicar() {
        var guardian = new GuardianVeracidad(perfil(), List.of("Go", "Java"));

        assertThat(guardian.huecos(List.of("Experiencia con Go en producción"), List.of("Go", "Java")))
                .containsExactly("Experiencia con Go en producción");
        assertThat(guardian.huecos(List.of(), List.of("Go", "Java")))
                .containsExactly("Go");
    }
}
