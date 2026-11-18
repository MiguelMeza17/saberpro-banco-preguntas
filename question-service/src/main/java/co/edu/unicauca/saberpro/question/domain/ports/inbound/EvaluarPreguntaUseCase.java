package co.edu.unicauca.saberpro.question.domain.ports.inbound;

import co.edu.unicauca.saberpro.question.domain.exception.PreguntaNoEncontradaException;
import co.edu.unicauca.saberpro.question.domain.exception.ReglaNegocioException;
import co.edu.unicauca.saberpro.question.domain.model.Pregunta;

/**
 * Puerto de entrada (driving) del revisor: aprobar o rechazar una pregunta pendiente o en revisión.
 */
public interface EvaluarPreguntaUseCase {

    /**
     * Aprueba una pregunta pendiente o en revisión.
     *
     * @param id            identificador de la pregunta
     * @param observaciones comentario opcional del revisor (puede ser nulo)
     * @return la pregunta en estado APROBADA
     * @throws PreguntaNoEncontradaException si no existe
     * @throws ReglaNegocioException         si su estado no admite evaluación
     */
    Pregunta aprobar(Long id, String observaciones);

    /**
     * Rechaza una pregunta pendiente o en revisión. Las observaciones son obligatorias.
     *
     * @param id            identificador de la pregunta
     * @param observaciones motivo del rechazo
     * @return la pregunta en estado RECHAZADA
     * @throws PreguntaNoEncontradaException si no existe
     * @throws ReglaNegocioException         si faltan observaciones o su estado no admite evaluación
     */
    Pregunta rechazar(Long id, String observaciones);
}
