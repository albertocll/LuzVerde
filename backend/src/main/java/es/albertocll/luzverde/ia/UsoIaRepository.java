package es.albertocll.luzverde.ia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface UsoIaRepository extends JpaRepository<UsoIa, Long> {

    long countByUsuarioIdAndCreadoEnGreaterThanEqual(Long usuarioId, Instant desde);
}
