package co.edu.unicauca.saberpro.lifecycle.messaging;

import java.time.Instant;

/**
 * Contrato del mensaje JSON que publica question-service (RabbitMQPublisher) en
 * {@code pregunta.revisar.queue} cuando una pregunta se envía a revisión.
 * <pre>
 * {"preguntaId": 1, "enunciado": "...", "estado": "PENDIENTE_REVISION", "fechaEnvio": "2026-11-18T15:00:00Z"}
 * </pre>
 *
 * @param preguntaId identificador de la pregunta
 * @param enunciado  texto del enunciado
 * @param estado     estado de la pregunta al publicarse (PENDIENTE_REVISION)
 * @param fechaEnvio instante del envío a revisión
 */
public record SolicitudRevisionMessage(Long preguntaId, String enunciado, String estado, Instant fechaEnvio) {
}
