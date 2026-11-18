package co.edu.unicauca.saberpro.lifecycle.revision;

import java.time.Instant;

/**
 * Revisión asignada a partir de una solicitud recibida por RabbitMQ.
 *
 * @param preguntaId identificador de la pregunta
 * @param enunciado  texto del enunciado
 * @param revisor    revisor asignado
 * @param estado     estado de la revisión ({@code EN_REVISION})
 * @param fechaEnvio instante en que question-service la envió a revisión
 * @param recibidaEn instante en que lifecycle-service procesó el mensaje
 */
public record RevisionAsignada(Long preguntaId, String enunciado, String revisor, String estado,
                               Instant fechaEnvio, Instant recibidaEn) {
}
