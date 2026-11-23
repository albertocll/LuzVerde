package es.albertocll.luzverde.evaluacion;

import es.albertocll.luzverde.ia.ListaTextoConverter;
import es.albertocll.luzverde.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "evaluaciones")
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "oferta_texto", nullable = false, length = 15000)
    private String ofertaTexto;

    @Column(length = 150)
    private String empresa;

    @Column(length = 150)
    private String puesto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Semaforo semaforo;

    @Column(length = 1000)
    private String resumen;

    @Convert(converter = ListaTextoConverter.class)
    @Column(length = 8000)
    private List<String> pros;

    @Convert(converter = ListaTextoConverter.class)
    @Column(length = 8000)
    private List<String> huecos;

    @Convert(converter = ListaTextoConverter.class)
    @Column(name = "banderas_rojas", length = 8000)
    private List<String> banderasRojas;

    @Column(name = "accion_recomendada", length = 1000)
    private String accionRecomendada;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    protected Evaluacion() {
    }

    public Evaluacion(Usuario usuario, String ofertaTexto) {
        this.usuario = usuario;
        this.ofertaTexto = ofertaTexto;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getOfertaTexto() { return ofertaTexto; }
    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }
    public String getPuesto() { return puesto; }
    public void setPuesto(String puesto) { this.puesto = puesto; }
    public Semaforo getSemaforo() { return semaforo; }
    public void setSemaforo(Semaforo semaforo) { this.semaforo = semaforo; }
    public String getResumen() { return resumen; }
    public void setResumen(String resumen) { this.resumen = resumen; }
    public List<String> getPros() { return pros; }
    public void setPros(List<String> pros) { this.pros = pros; }
    public List<String> getHuecos() { return huecos; }
    public void setHuecos(List<String> huecos) { this.huecos = huecos; }
    public List<String> getBanderasRojas() { return banderasRojas; }
    public void setBanderasRojas(List<String> banderasRojas) { this.banderasRojas = banderasRojas; }
    public String getAccionRecomendada() { return accionRecomendada; }
    public void setAccionRecomendada(String accionRecomendada) { this.accionRecomendada = accionRecomendada; }
    public Instant getCreadoEn() { return creadoEn; }
}
