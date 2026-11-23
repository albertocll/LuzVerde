package es.albertocll.luzverde.cv;

import java.util.List;

/**
 * Lo que se pinta en el CV, ya revisado. Los renderizadores no deciden nada: solo maquetan esto.
 */
public record ContenidoCv(
        String nombre,
        String titular,
        List<String> contacto,
        String resumen,
        List<String> tecnologias,
        List<Apartado> experiencias,
        List<Apartado> proyectos,
        List<Apartado> formacion,
        List<String> idiomas) {

    /**
     * Una entrada de experiencia, proyecto o formación.
     *
     * @param titulo    puesto, nombre del proyecto o título
     * @param subtitulo empresa, centro o tecnologías
     * @param fechas    texto de fechas ya formateado, o null
     * @param puntos    viñetas
     */
    public record Apartado(String titulo, String subtitulo, String fechas, List<String> puntos) {
    }
}
