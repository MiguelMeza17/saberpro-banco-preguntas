package co.edu.unicauca.saberpro.question.infrastructure.messaging;

import java.time.Instant;

/**
 * Mensaje JSON que se publica en {@code pregunta.revisar.queue}; es el mismo contrato que lee
 * {@code QuestionReviewListener} en lifecycle-service:
 * <pre>
 * {"preguntaId": 1, "enunciado": "...", "estado": "PENDIENTE_REVISION", "fechaEnvio": "2026-11-18T15:00:00Z"}
 * </pre>
 *
 * @param preguntaId identificador de la pregunta
 * @param enunciado  texto del enunciado
 * @param estado     estado al publicarse (PENDIENTE_REVISION)
 * @param fechaEnvio instante del envío
 */
public record SolicitudRevisionMessage(Long preguntaId, String enunciado, String estado, Instant fechaEnvio) {
}
