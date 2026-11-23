package es.albertocll.luzverde.cv;

import es.albertocll.luzverde.ia.ListaTextoConverter;
import es.albertocll.luzverde.usuario.Usuario;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "cvs")
public class Cv {

    /** 5 MB por archivo: un CV de dos páginas ocupa unos pocos KB. */
    private static final int MAX_BYTES = 5_000_000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(length = 150)
    private String empresa;

    @Column(length = 150)
    private String puesto;

    @Convert(converter = ListaTextoConverter.class)
    @Column(length = 8000)
    private List<String> huecos;

    @Column(length = 2000)
    private String carta;

    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false, length = MAX_BYTES)
    private byte[] pdf;

    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false, length = MAX_BYTES)
    private byte[] docx;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    protected Cv() {
    }

    public Cv(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }
    public String getPuesto() { return puesto; }
    public void setPuesto(String puesto) { this.puesto = puesto; }
    public List<String> getHuecos() { return huecos; }
    public void setHuecos(List<String> huecos) { this.huecos = huecos; }
    public String getCarta() { return carta; }
    public void setCarta(String carta) { this.carta = carta; }
    public byte[] getPdf() { return pdf; }
    public void setPdf(byte[] pdf) { this.pdf = pdf; }
    public byte[] getDocx() { return docx; }
    public void setDocx(byte[] docx) { this.docx = docx; }
    public Instant getCreadoEn() { return creadoEn; }
}
