package es.albertocll.luzverde.cv;

import es.albertocll.luzverde.perfil.NivelTecnologia;
import es.albertocll.luzverde.perfil.PerfilDto;
import es.albertocll.luzverde.perfil.TecnologiaPerfil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Aplica la regla dura del generador de CV: nada que no esté en el perfil.
 *
 * <p>No se fía del modelo. Calcula qué tecnologías puede nombrar el CV (las del perfil con nivel
 * sólido o medio, más las que aparecen en lo que el propio usuario ha escrito) y quita cualquier
 * frase que nombre otra. Lo que se quita pasa a la lista de huecos.</p>
 */
public class GuardianVeracidad {

    private final Map<String, String> tecnologiasReales = new LinkedHashMap<>();
    private final Set<String> sinTocar = new LinkedHashSet<>();
    private final String textoDelUsuario;
    private final Set<String> prohibidas = new LinkedHashSet<>();

    /**
     * @param perfil          perfil del usuario
     * @param mencionadas     tecnologías que salen en la oferta o que el modelo ha querido usar
     */
    public GuardianVeracidad(PerfilDto perfil, Collection<String> mencionadas) {
        for (TecnologiaPerfil t : nulos(perfil.tecnologias())) {
            if (t.getNombre() == null || t.getNombre().isBlank()) {
                continue;
            }
            if (t.getNivel() == NivelTecnologia.SIN_TOCAR) {
                sinTocar.add(clave(t.getNombre()));
            } else {
                tecnologiasReales.putIfAbsent(clave(t.getNombre()), t.getNombre().trim());
            }
        }

        StringBuilder texto = new StringBuilder();
        nulos(perfil.experiencias()).forEach(e -> texto.append(' ').append(e.getPuesto())
                .append(' ').append(e.getDescripcion()));
        nulos(perfil.proyectos()).forEach(p -> texto.append(' ').append(p.getNombre())
                .append(' ').append(p.getDescripcion()).append(' ').append(p.getTecnologias()));
        nulos(perfil.formaciones()).forEach(f -> texto.append(' ').append(f.getTitulo()));
        this.textoDelUsuario = texto.toString();

        List<String> candidatas = new ArrayList<>(mencionadas);
        nulos(perfil.tecnologias()).stream()
                .filter(t -> t.getNivel() == NivelTecnologia.SIN_TOCAR)
                .map(TecnologiaPerfil::getNombre)
                .forEach(candidatas::add);

        for (String candidata : candidatas) {
            if (candidata != null && !candidata.isBlank() && !permitida(candidata)) {
                prohibidas.add(candidata.trim());
            }
        }
    }

    /** True si el usuario tiene esta tecnología o la ha escrito él mismo en su perfil. */
    public boolean permitida(String tecnologia) {
        // Si el usuario la marcó como "sin tocar", no vale aunque aparezca en su texto.
        return tecnologiasReales.containsKey(clave(tecnologia))
                || (contiene(textoDelUsuario, tecnologia) && !marcadaSinTocar(tecnologia));
    }

    private boolean marcadaSinTocar(String tecnologia) {
        return sinTocar.contains(clave(tecnologia));
    }

    /** Tecnologías del CV: primero las que eligió el modelo, en su orden, y luego el resto de las reales. */
    public List<String> tecnologias(List<String> elegidasPorElModelo) {
        Set<String> resultado = new LinkedHashSet<>();
        for (String elegida : elegidasPorElModelo) {
            String real = tecnologiasReales.get(clave(elegida));
            if (real != null) {
                resultado.add(real);
            }
        }
        resultado.addAll(tecnologiasReales.values());
        return List.copyOf(resultado);
    }

    /** Huecos: los que dio el modelo más las tecnologías prohibidas que pide la oferta. */
    public List<String> huecos(List<String> delModelo, List<String> tecnologiasOferta) {
        Map<String, String> resultado = new LinkedHashMap<>();
        for (String hueco : delModelo) {
            resultado.putIfAbsent(clave(hueco), hueco);
        }
        for (String tecnologia : tecnologiasOferta) {
            if (prohibidas.contains(tecnologia.trim()) && resultado.keySet().stream()
                    .noneMatch(h -> contiene(h, tecnologia))) {
                resultado.putIfAbsent(clave(tecnologia), tecnologia.trim());
            }
        }
        return List.copyOf(resultado.values());
    }

    /** Devuelve el texto sin las frases que nombran tecnologías prohibidas, o null si no queda nada. */
    public String limpiar(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String[] frases = texto.trim().split("(?<=[.!?])\\s+");
        StringBuilder limpio = new StringBuilder();
        for (String frase : frases) {
            if (esLimpio(frase)) {
                if (!limpio.isEmpty()) {
                    limpio.append(' ');
                }
                limpio.append(frase);
            }
        }
        return limpio.isEmpty() ? null : limpio.toString();
    }

    public List<String> limpiar(List<String> textos) {
        return textos.stream().map(this::limpiar).filter(Objects::nonNull).toList();
    }

    public boolean esLimpio(String texto) {
        return prohibidas.stream().noneMatch(p -> contiene(texto, p));
    }

    public Set<String> prohibidas() {
        return Set.copyOf(prohibidas);
    }

    /** Busca la palabra completa sin distinguir mayúsculas. Sirve para "C#", ".NET" o "Node.js". */
    static boolean contiene(String texto, String termino) {
        if (texto == null || termino == null || termino.isBlank()) {
            return false;
        }
        Pattern patron = Pattern.compile("(?<![\\p{L}\\p{N}])" + Pattern.quote(termino.trim()) + "(?![\\p{L}\\p{N}])",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        return patron.matcher(texto).find();
    }

    private static String clave(String texto) {
        return texto.trim().toLowerCase(Locale.ROOT);
    }

    private static <T> List<T> nulos(List<T> lista) {
        return lista == null ? List.of() : lista;
    }
}
