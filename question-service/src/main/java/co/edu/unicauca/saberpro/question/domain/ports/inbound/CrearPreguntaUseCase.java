package co.edu.unicauca.saberpro.question.domain.ports.inbound;

import co.edu.unicauca.saberpro.question.domain.exception.PreguntaNoEncontradaException;
import co.edu.unicauca.saberpro.question.domain.exception.ReglaNegocioException;
import co.edu.unicauca.saberpro.question.domain.model.Pregunta;

import java.util.List;

/**
 * Puerto de entrada (driving) del autor: crear preguntas, enviarlas a revisión y consultarlas.
 * <p>
 * Los adaptadores de entrada (el controlador REST) solo conocen esta interfaz, nunca la implementación
 * ni los repositorios.
 */
public interface CrearPreguntaUseCase {

    /**
     * Crea una pregunta en estado BORRADOR con su enunciado, justificación y opciones.
     *
     * @param comando datos de la pregunta
     * @return la pregunta creada, con identificador
     * @throws ReglaNegocioException si los datos incumplen una regla del dominio
     */
    Pregunta crear(CrearPreguntaComando comando);

    /**
     * Envía a revisión una pregunta en BORRADOR. El agregado valida que tenga exactamente
     * 4 opciones y 1 sola clave antes de pasar a PENDIENTE_REVISION; después se notifica el envío.
     *
     * @param id identificador de la pregunta
     * @return la pregunta en estado PENDIENTE_REVISION
     * @throws PreguntaNoEncontradaException si no existe
     * @throws ReglaNegocioException         si no cumple las invariantes
     */
    Pregunta enviarARevision(Long id);

    /**
     * Lista todas las preguntas del banco.
     *
     * @return preguntas guardadas (vacía si no hay)
     */
    List<Pregunta> listar();

    /**
     * Consulta una pregunta por su identificador.
     *
     * @param id identificador
     * @return la pregunta
     * @throws PreguntaNoEncontradaException si no existe
     */
    Pregunta obtener(Long id);
}
