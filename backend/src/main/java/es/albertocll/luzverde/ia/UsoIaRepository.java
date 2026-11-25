package es.albertocll.luzverde.ia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.time.Instant;

public interface UsoIaRepository extends JpaRepository<UsoIa, Long> {

    long countByUsuarioIdAndCreadoEnGreaterThanEqual(Long usuarioId, Instant desde);

    /** Todo el historial, para exportar los datos del usuario. */
    List<UsoIa> findByUsuarioIdOrderByCreadoEnDesc(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);
}
