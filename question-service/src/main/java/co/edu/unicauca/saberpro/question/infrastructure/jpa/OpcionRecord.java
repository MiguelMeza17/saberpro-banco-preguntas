package co.edu.unicauca.saberpro.question.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Fila de la tabla {@code pregunta_opciones}: una opción de respuesta de una pregunta.
 */
@Embeddable
public class OpcionRecord {

    @Column(name = "texto", nullable = false, length = 500)
    private String texto;

    @Column(name = "es_correcta", nullable = false)
    private boolean esCorrecta;

    protected OpcionRecord() {
    }

    public OpcionRecord(String texto, boolean esCorrecta) {
        this.texto = texto;
        this.esCorrecta = esCorrecta;
    }

    public String getTexto() {
        return texto;
    }

    public boolean isEsCorrecta() {
        return esCorrecta;
    }
}
