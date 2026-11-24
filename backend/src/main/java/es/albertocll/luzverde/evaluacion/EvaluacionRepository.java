package es.albertocll.luzverde.evaluacion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {

    List<Evaluacion> findTop50ByUsuarioIdOrderByCreadoEnDesc(Long usuarioId);

    Optional<Evaluacion> findByIdAndUsuarioId(Long id, Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);
}
