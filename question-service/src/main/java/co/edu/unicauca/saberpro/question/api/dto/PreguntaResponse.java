package co.edu.unicauca.saberpro.question.api.dto;

import co.edu.unicauca.saberpro.question.domain.model.Pregunta;

import java.util.List;

/**
 * Representación JSON de una pregunta. Separa el contrato HTTP del agregado del dominio.
 *
 * @param id            identificador
 * @param enunciado     texto del enunciado
 * @param justificacion justificación de la clave
 * @param opciones      opciones en orden
 * @param estado        estado del ciclo de vida
 * @param observaciones observaciones de la última evaluación (puede ser nulo)
 */
public record PreguntaResponse(Long id, String enunciado, String justificacion, List<OpcionResponse> opciones,
                               String estado, String observaciones) {

    /**
     * Opción de respuesta en el JSON.
     *
     * @param texto      texto de la opción
     * @param esCorrecta {@code true} si es la clave
     */
    public record OpcionResponse(String texto, boolean esCorrecta) {
    }

    /**
     * Construye la respuesta a partir del agregado.
     *
     * @param pregunta agregado del dominio
     * @return respuesta HTTP
     */
    public static PreguntaResponse de(Pregunta pregunta) {
        return new PreguntaResponse(
                pregunta.getId(),
                pregunta.getEnunciado() == null ? null : pregunta.getEnunciado().getTexto(),
                pregunta.getJustificacion() == null ? null : pregunta.getJustificacion().getTexto(),
                pregunta.getOpciones().stream().map(o -> new OpcionResponse(o.getTexto(), o.isEsCorrecta())).toList(),
                pregunta.getEstado().name(),
                pregunta.getObservaciones());
    }
}
