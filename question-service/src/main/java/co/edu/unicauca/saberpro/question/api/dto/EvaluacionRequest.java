package co.edu.unicauca.saberpro.question.api.dto;

/**
 * Cuerpo HTTP de los endpoints de evaluación.
 *
 * @param observaciones comentario del revisor (obligatorio al rechazar)
 */
public record EvaluacionRequest(String observaciones) {
}
