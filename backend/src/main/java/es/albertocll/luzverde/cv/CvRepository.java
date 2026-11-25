package es.albertocll.luzverde.cv;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CvRepository extends JpaRepository<Cv, Long> {

    List<Cv> findTop50ByUsuarioIdOrderByCreadoEnDesc(Long usuarioId);

    Optional<Cv> findByIdAndUsuarioId(Long id, Long usuarioId);

    /** Todo el historial, para exportar los datos del usuario. */
    List<Cv> findByUsuarioIdOrderByCreadoEnDesc(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);
}
