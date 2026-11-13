package co.edu.unicauca.saberpro.validation.api;

import co.edu.unicauca.saberpro.validation.pipeline.PipelineValidacion;
import co.edu.unicauca.saberpro.validation.pipeline.PreguntaAValidar;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API de validación estructural que question-service consume de forma síncrona antes de guardar.
 * Siempre responde 200 con el resultado; quien llama decide qué hacer si la pregunta no es válida.
 */
@RestController
@RequestMapping("/api/validaciones")
public class ValidationRestController {

    private final PipelineValidacion pipeline;

    public ValidationRestController(PipelineValidacion pipeline) {
        this.pipeline = pipeline;
    }

    /**
     * Valida la estructura de una pregunta.
     *
     * @param pregunta enunciado, justificación y opciones
     * @return si es válida y la lista de errores
     */
    @PostMapping
    public ResultadoValidacion validar(@RequestBody PreguntaAValidar pregunta) {
        List<String> errores = pipeline.validar(pregunta);
        return new ResultadoValidacion(errores.isEmpty(), errores);
    }

    /**
     * Respuesta de la validación.
     *
     * @param valida  {@code true} si no hay errores
     * @param errores errores encontrados
     */
    public record ResultadoValidacion(boolean valida, List<String> errores) {
    }
}
