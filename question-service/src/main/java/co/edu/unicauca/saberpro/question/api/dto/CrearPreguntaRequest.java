package co.edu.unicauca.saberpro.question.api.dto;

import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaComando;

import java.util.List;

/**
 * Cuerpo HTTP de {@code POST /api/preguntas}. Se traduce al comando del puerto de entrada para que el
 * dominio no dependa del formato JSON.
 *
 * @param enunciado     texto del enunciado
 * @param justificacion justificación de la clave
 * @param opciones      opciones de respuesta
 */
public record CrearPreguntaRequest(String enunciado, String justificacion, List<OpcionRequest> opciones) {

    /**
     * Opción de respuesta en el JSON.
     *
     * @param texto      texto de la opción
     * @param esCorrecta {@code true} si es la clave
     */
    public record OpcionRequest(String texto, boolean esCorrecta) {
    }

    /**
     * Convierte el cuerpo HTTP en el comando del caso de uso.
     *
     * @return comando para {@code CrearPreguntaUseCase#crear}
     */
    public CrearPreguntaComando aComando() {
        List<CrearPreguntaComando.Opcion> lista = opciones == null ? List.of()
                : opciones.stream().map(o -> new CrearPreguntaComando.Opcion(o.texto(), o.esCorrecta())).toList();
        return new CrearPreguntaComando(enunciado, justificacion, lista);
    }
}
