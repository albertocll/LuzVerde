package es.albertocll.luzverde.cuenta;

import es.albertocll.luzverde.candidatura.CandidaturaRepository;
import es.albertocll.luzverde.cv.CvRepository;
import es.albertocll.luzverde.evaluacion.EvaluacionRepository;
import es.albertocll.luzverde.ia.UsoIaRepository;
import es.albertocll.luzverde.perfil.PerfilRepository;
import es.albertocll.luzverde.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Borra la cuenta y todos los datos que cuelgan de ella. Google Play lo exige:
 * borrar la cuenta tiene que borrar también los datos asociados.
 */
@Service
public class CuentaService {

    private final UsuarioRepository usuarios;
    private final PerfilRepository perfiles;
    private final CandidaturaRepository candidaturas;
    private final EvaluacionRepository evaluaciones;
    private final CvRepository cvs;
    private final UsoIaRepository usosIa;

    public CuentaService(UsuarioRepository usuarios, PerfilRepository perfiles, CandidaturaRepository candidaturas,
                         EvaluacionRepository evaluaciones, CvRepository cvs, UsoIaRepository usosIa) {
        this.usuarios = usuarios;
        this.perfiles = perfiles;
        this.candidaturas = candidaturas;
        this.evaluaciones = evaluaciones;
        this.cvs = cvs;
        this.usosIa = usosIa;
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
