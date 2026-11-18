package co.edu.unicauca.saberpro.question.infrastructure.jpa;

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
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import co.edu.unicauca.saberpro.question.domain.model.EstadoPregunta;

import java.util.ArrayList;
import java.util.List;

/**
 * Registro JPA de una pregunta. Vive en la infraestructura para que el agregado {@code Pregunta} siga sin
 * anotaciones; {@link QuestionDomainMapper} traduce entre ambos.
 */
@Entity
@Table(name = "preguntas")
public class QuestionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enunciado", length = 2000)
    private String enunciado;

    @Column(name = "justificacion", length = 2000)
    private String justificacion;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pregunta_opciones", joinColumns = @JoinColumn(name = "pregunta_id"))
    @OrderColumn(name = "posicion")
    private List<OpcionRecord> opciones = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoPregunta estado;

    @Column(name = "observaciones", length = 2000)
    private String observaciones;

    protected QuestionRecord() {
    }

    QuestionRecord(Long id, String enunciado, String justificacion, List<OpcionRecord> opciones,
                   EstadoPregunta estado, String observaciones) {
        this.id = id;
        this.enunciado = enunciado;
        this.justificacion = justificacion;
        this.opciones = new ArrayList<>(opciones);
        this.estado = estado;
        this.observaciones = observaciones;
    }

    public Long getId() {
        return id;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public List<OpcionRecord> getOpciones() {
        return opciones;
    }

    public EstadoPregunta getEstado() {
        return estado;
    }

    public String getObservaciones() {
        return observaciones;
    }
}
