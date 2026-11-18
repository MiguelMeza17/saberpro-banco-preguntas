package co.edu.unicauca.saberpro.question.infrastructure.jpa;

import co.edu.unicauca.saberpro.question.domain.model.Enunciado;
import co.edu.unicauca.saberpro.question.domain.model.Justificacion;
import co.edu.unicauca.saberpro.question.domain.model.OpcionRespuesta;
import co.edu.unicauca.saberpro.question.domain.model.Pregunta;

import java.util.List;

/**
 * Traduce entre el agregado {@link Pregunta} y {@link QuestionRecord}. La vuelta al dominio usa
 * {@link Pregunta#reconstituir}, que vuelve a aplicar las invariantes: un registro inconsistente no produce
 * un agregado inválido.
 */
public final class QuestionDomainMapper {

    private QuestionDomainMapper() {
    }

    /**
     * Convierte el agregado en registro (conserva el id si ya lo tiene).
     *
     * @param pregunta agregado
     * @return registro JPA
     */
    public static QuestionRecord aRecord(Pregunta pregunta) {
        List<OpcionRecord> opciones = pregunta.getOpciones().stream()
                .map(o -> new OpcionRecord(o.getTexto(), o.isEsCorrecta()))
                .toList();
        return new QuestionRecord(
                pregunta.getId(),
                pregunta.getEnunciado() == null ? null : pregunta.getEnunciado().getTexto(),
                pregunta.getJustificacion() == null ? null : pregunta.getJustificacion().getTexto(),
                opciones,
                pregunta.getEstado(),
                pregunta.getObservaciones());
    }

    /**
     * Rehidrata el agregado desde el registro.
     *
     * @param record registro JPA
     * @return agregado del dominio
     */
    public static Pregunta aDominio(QuestionRecord record) {
        List<OpcionRespuesta> opciones = record.getOpciones().stream()
                .map(o -> new OpcionRespuesta(o.getTexto(), o.isEsCorrecta()))
                .toList();
        return Pregunta.reconstituir(
                record.getId(),
                record.getEnunciado() == null ? null : new Enunciado(record.getEnunciado()),
                record.getJustificacion() == null ? null : new Justificacion(record.getJustificacion()),
                opciones,
                record.getEstado(),
                record.getObservaciones());
    }
}
