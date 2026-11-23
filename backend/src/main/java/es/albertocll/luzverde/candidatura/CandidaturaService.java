package es.albertocll.luzverde.candidatura;

import es.albertocll.luzverde.candidatura.CandidaturaDtos.CandidaturaRequest;
import es.albertocll.luzverde.candidatura.CandidaturaDtos.CandidaturaResponse;
import es.albertocll.luzverde.error.NoEncontradoException;
import es.albertocll.luzverde.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CandidaturaService {

    private final CandidaturaRepository candidaturas;
    private final UsuarioRepository usuarios;

    public CandidaturaService(CandidaturaRepository candidaturas, UsuarioRepository usuarios) {
        this.candidaturas = candidaturas;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<CandidaturaResponse> listar(Long usuarioId, EstadoCandidatura estado) {
        List<Candidatura> resultado = estado == null
                ? candidaturas.findByUsuarioIdOrderByFechaDescIdDesc(usuarioId)
                : candidaturas.findByUsuarioIdAndEstadoOrderByFechaDescIdDesc(usuarioId, estado);
        return resultado.stream().map(CandidaturaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public CandidaturaResponse obtener(Long usuarioId, Long id) {
        return CandidaturaResponse.de(buscarPropia(usuarioId, id));
    }

    @Transactional
    public CandidaturaResponse crear(Long usuarioId, CandidaturaRequest peticion) {
        Candidatura candidatura = new Candidatura(usuarios.getReferenceById(usuarioId));
        aplicar(candidatura, peticion);
        return CandidaturaResponse.de(candidaturas.save(candidatura));
    }

    @Transactional
    public CandidaturaResponse actualizar(Long usuarioId, Long id, CandidaturaRequest peticion) {
        Candidatura candidatura = buscarPropia(usuarioId, id);
        aplicar(candidatura, peticion);
        return CandidaturaResponse.de(candidatura);
    }

    @Transactional
    public void borrar(Long usuarioId, Long id) {
        candidaturas.delete(buscarPropia(usuarioId, id));
    }

    // Si la candidatura es de otro usuario, respondemos igual que si no existiera.
    private Candidatura buscarPropia(Long usuarioId, Long id) {
        return candidaturas.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new NoEncontradoException("Candidatura no encontrada"));
    }

    private static void aplicar(Candidatura c, CandidaturaRequest p) {
        c.setEmpresa(p.empresa().trim());
        c.setPuesto(p.puesto().trim());
        c.setEnlace(vacioANull(p.enlace()));
        c.setEstado(p.estado());
        c.setFecha(p.fecha());
        c.setNotas(vacioANull(p.notas()));
    }

    private static String vacioANull(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
