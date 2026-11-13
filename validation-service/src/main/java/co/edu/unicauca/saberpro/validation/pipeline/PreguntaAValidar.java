package co.edu.unicauca.saberpro.validation.pipeline;

import java.util.List;

/**
 * Datos de la pregunta que recorren el pipeline de validación.
 *
 * @param enunciado     texto del enunciado
 * @param justificacion justificación de la clave
 * @param opciones      opciones de respuesta
 */
public record PreguntaAValidar(String enunciado, String justificacion, List<Opcion> opciones) {

    public PreguntaAValidar {
        opciones = opciones == null ? List.of() : List.copyOf(opciones);
    }

    /**
     * Opción de respuesta.
     *
     * @param texto      texto de la opción
     * @param esCorrecta {@code true} si es la clave
     */
    public record Opcion(String texto, boolean esCorrecta) {
    }
}
