package es.albertocll.luzverde.candidatura;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CandidaturaRepository extends JpaRepository<Candidatura, Long> {

    List<Candidatura> findByUsuarioIdOrderByFechaDescIdDesc(Long usuarioId);

    List<Candidatura> findByUsuarioIdAndEstadoOrderByFechaDescIdDesc(Long usuarioId, EstadoCandidatura estado);

    Optional<Candidatura> findByIdAndUsuarioId(Long id, Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);
}
