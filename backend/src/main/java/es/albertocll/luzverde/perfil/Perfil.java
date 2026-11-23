package es.albertocll.luzverde.perfil;

import es.albertocll.luzverde.usuario.Usuario;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "perfiles")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    /** Salario mínimo aceptable, en euros brutos al año. */
    @Column(name = "salario_minimo")
    private Integer salarioMinimo;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Modalidad modalidad;

    @Column(length = 150)
    private String ubicacion;

    @ElementCollection
    @CollectionTable(name = "perfil_tecnologias", joinColumns = @JoinColumn(name = "perfil_id"))
    @OrderColumn(name = "orden")
    private List<TecnologiaPerfil> tecnologias = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "perfil_experiencias", joinColumns = @JoinColumn(name = "perfil_id"))
    @OrderColumn(name = "orden")
    private List<Experiencia> experiencias = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "perfil_formaciones", joinColumns = @JoinColumn(name = "perfil_id"))
    @OrderColumn(name = "orden")
    private List<Formacion> formaciones = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "perfil_proyectos", joinColumns = @JoinColumn(name = "perfil_id"))
    @OrderColumn(name = "orden")
    private List<Proyecto> proyectos = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "perfil_idiomas", joinColumns = @JoinColumn(name = "perfil_id"))
    @OrderColumn(name = "orden")
    private List<Idioma> idiomas = new ArrayList<>();

    protected Perfil() {
    }

    public Perfil(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Integer getSalarioMinimo() {
        return salarioMinimo;
    }

    public void setSalarioMinimo(Integer salarioMinimo) {
        this.salarioMinimo = salarioMinimo;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(Modalidad modalidad) {
        this.modalidad = modalidad;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public List<TecnologiaPerfil> getTecnologias() {
        return tecnologias;
    }

    public List<Experiencia> getExperiencias() {
        return experiencias;
    }

    public List<Formacion> getFormaciones() {
        return formaciones;
    }

    public List<Proyecto> getProyectos() {
        return proyectos;
    }

    public List<Idioma> getIdiomas() {
        return idiomas;
    }
}
