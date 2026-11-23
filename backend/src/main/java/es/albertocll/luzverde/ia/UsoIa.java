package es.albertocll.luzverde.ia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Un registro por cada llamada a la IA, para aplicar el límite diario.
 */
@Entity
@Table(name = "usos_ia", indexes = @Index(name = "idx_usos_ia_usuario_fecha", columnList = "usuario_id, creado_en"))
public class UsoIa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    protected UsoIa() {
    }

    public UsoIa(Long usuarioId, String tipo) {
        this.usuarioId = usuarioId;
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getTipo() {
        return tipo;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
