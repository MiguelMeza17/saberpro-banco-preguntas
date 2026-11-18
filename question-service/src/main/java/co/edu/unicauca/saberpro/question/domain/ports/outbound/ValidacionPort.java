package co.edu.unicauca.saberpro.question.domain.ports.outbound;

import co.edu.unicauca.saberpro.question.domain.model.Pregunta;

import java.util.List;

/**
 * Puerto de salida (driven) de la validación estructural (HU03). El hexágono pide validar una pregunta antes
 * de guardarla sin saber que la validación vive en otro microservicio ({@code validation-service}).
 */
public interface ValidacionPort {

    /**
     * Valida la estructura de una pregunta.
     *
     * @param pregunta pregunta por guardar
     * @return errores encontrados; vacía si la pregunta es válida
     */
    List<String> validar(Pregunta pregunta);
}
