package co.edu.unicauca.saberpro.question.domain.ports.outbound;

import co.edu.unicauca.saberpro.question.domain.model.Pregunta;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (driven) de persistencia del agregado {@link Pregunta}.
 * <p>
 * Interfaz pura: no conoce JPA, SQL ni Spring. La implementan {@code JpaQuestionAdapter} e
 * {@code InMemoryQuestionAdapter}, que se intercambian sin tocar el dominio.
 */
public interface PreguntaRepositoryPort {

    /**
     * Guarda una pregunta nueva (le asigna identificador) o actualiza una existente.
     *
     * @param pregunta agregado completo
     * @return la pregunta guardada, con identificador
     */
    Pregunta save(Pregunta pregunta);

    /**
     * Busca una pregunta por su identificador.
     *
     * @param id identificador
     * @return la pregunta, si existe
     */
    Optional<Pregunta> findById(Long id);

    /**
     * Devuelve todas las preguntas guardadas.
     *
     * @return preguntas (vacía si no hay)
     */
    List<Pregunta> findAll();
}
