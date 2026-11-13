package co.edu.unicauca.saberpro.validation.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Pipeline de validación estructural: pasa la pregunta por cada filtro y acumula los errores.
 */
public class PipelineValidacion {

    private final List<FiltroValidacion> filtros;

    /**
     * Crea el pipeline con los filtros en el orden en que se aplican.
     *
     * @param filtros filtros del pipeline
     */
    public PipelineValidacion(List<FiltroValidacion> filtros) {
        this.filtros = List.copyOf(Objects.requireNonNull(filtros, "filtros"));
    }

    /**
     * Valida la pregunta.
     *
     * @param pregunta pregunta a validar
     * @return errores encontrados; vacía si la pregunta es válida
     */
    public List<String> validar(PreguntaAValidar pregunta) {
        List<String> errores = new ArrayList<>();
        if (pregunta == null) {
            errores.add("La pregunta no puede ser nula.");
            return errores;
        }
        filtros.forEach(filtro -> filtro.aplicar(pregunta, errores));
        return errores;
    }
}
