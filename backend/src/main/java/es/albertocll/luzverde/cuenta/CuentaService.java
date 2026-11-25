package es.albertocll.luzverde.cuenta;

import es.albertocll.luzverde.candidatura.CandidaturaDtos.CandidaturaResponse;
import es.albertocll.luzverde.candidatura.CandidaturaRepository;
import es.albertocll.luzverde.cv.CvRepository;
import es.albertocll.luzverde.evaluacion.EvaluacionRepository;
import es.albertocll.luzverde.ia.UsoIaRepository;
import es.albertocll.luzverde.perfil.PerfilRepository;
import es.albertocll.luzverde.error.NoEncontradoException;
import es.albertocll.luzverde.perfil.PerfilService;
import es.albertocll.luzverde.usuario.Usuario;
import es.albertocll.luzverde.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/**
 * Exporta y borra la cuenta con todos los datos que cuelgan de ella. Google Play exige el borrado
 * y el RGPD pide que el usuario pueda llevarse sus datos.
 */
@Service
public class CuentaService {

    private final UsuarioRepository usuarios;
    private final PerfilRepository perfiles;
    private final CandidaturaRepository candidaturas;
    private final EvaluacionRepository evaluaciones;
    private final CvRepository cvs;
    private final UsoIaRepository usosIa;
    private final PerfilService perfilService;
    private final Clock reloj;

    public CuentaService(UsuarioRepository usuarios, PerfilRepository perfiles, CandidaturaRepository candidaturas,
                         EvaluacionRepository evaluaciones, CvRepository cvs, UsoIaRepository usosIa,
                         PerfilService perfilService, Clock reloj) {
        this.usuarios = usuarios;
        this.perfiles = perfiles;
        this.candidaturas = candidaturas;
        this.evaluaciones = evaluaciones;
        this.cvs = cvs;
        this.usosIa = usosIa;
        this.perfilService = perfilService;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public Exportacion exportar(Long usuarioId) {
        Usuario usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> new NoEncontradoException("Cuenta no encontrada"));
        return new Exportacion(
                reloj.instant(),
                new Exportacion.Cuenta(usuario.getEmail(), usuario.getNombre(), usuario.getCreadoEn()),
                perfilService.obtener(usuarioId),
                candidaturas.findByUsuarioIdOrderByFechaDescIdDesc(usuarioId).stream()
                        .map(CandidaturaResponse::de).toList(),
                evaluaciones.findByUsuarioIdOrderByCreadoEnDesc(usuarioId).stream()
                        .map(e -> new Exportacion.EvaluacionCompleta(e.getId(), e.getOfertaTexto(), e.getEmpresa(),
                                e.getPuesto(), e.getSemaforo(), e.getResumen(), e.getPros(), e.getHuecos(),
                                e.getBanderasRojas(), e.getAccionRecomendada(), e.getCreadoEn()))
                        .toList(),
                cvs.findByUsuarioIdOrderByCreadoEnDesc(usuarioId).stream()
                        .map(cv -> new Exportacion.CvGenerado(cv.getId(), cv.getEmpresa(), cv.getPuesto(),
                                cv.getHuecos(), cv.getCarta(), cv.getCreadoEn(),
                                "/api/v1/cv/" + cv.getId() + "/pdf", "/api/v1/cv/" + cv.getId() + "/docx"))
                        .toList(),
                usosIa.findByUsuarioIdOrderByCreadoEnDesc(usuarioId).stream()
                        .map(u -> new Exportacion.UsoIa(u.getTipo(), u.getCreadoEn()))
                        .toList());
    }

    @Transactional
    public void borrar(Long usuarioId) {
        cvs.deleteByUsuarioId(usuarioId);
        evaluaciones.deleteByUsuarioId(usuarioId);
        candidaturas.deleteByUsuarioId(usuarioId);
        usosIa.deleteByUsuarioId(usuarioId);
        perfiles.findByUsuarioId(usuarioId).ifPresent(perfiles::delete);
        usuarios.deleteById(usuarioId);
    }
}
