package es.albertocll.luzverde.perfil;

import es.albertocll.luzverde.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PerfilService {

    private final PerfilRepository perfiles;
    private final UsuarioRepository usuarios;

    public PerfilService(PerfilRepository perfiles, UsuarioRepository usuarios) {
        this.perfiles = perfiles;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public PerfilDto obtener(Long usuarioId) {
        return perfiles.findByUsuarioId(usuarioId)
                .map(PerfilDto::de)
                .orElseGet(PerfilDto::vacio);
    }

    @Transactional
    public PerfilDto guardar(Long usuarioId, PerfilDto datos) {
        Perfil perfil = perfiles.findByUsuarioId(usuarioId)
                .orElseGet(() -> perfiles.save(new Perfil(usuarios.getReferenceById(usuarioId))));

        perfil.setSalarioMinimo(datos.salarioMinimo());
        perfil.setModalidad(datos.modalidad());
        perfil.setUbicacion(datos.ubicacion() == null || datos.ubicacion().isBlank() ? null : datos.ubicacion().trim());
        reemplazar(perfil.getTecnologias(), datos.tecnologias());
        reemplazar(perfil.getExperiencias(), datos.experiencias());
        reemplazar(perfil.getFormaciones(), datos.formaciones());
        reemplazar(perfil.getProyectos(), datos.proyectos());
        reemplazar(perfil.getIdiomas(), datos.idiomas());

        perfiles.flush();
        return PerfilDto.de(perfil);
    }

    private static <T> void reemplazar(List<T> actual, List<T> nueva) {
        actual.clear();
        if (nueva != null) {
            actual.addAll(nueva);
        }
    }
}
