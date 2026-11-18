package co.edu.unicauca.saberpro.question.application;

import co.edu.unicauca.saberpro.question.domain.exception.PreguntaNoEncontradaException;
import co.edu.unicauca.saberpro.question.domain.model.Enunciado;
import co.edu.unicauca.saberpro.question.domain.model.Justificacion;
import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaComando;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.EvaluarPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.NotificacionPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;

import java.util.List;
import java.util.Objects;

/**
 * Servicio de aplicación que implementa los puertos de entrada de question-service.
 * <p>
 * Solo orquesta: las reglas de negocio las aplica el agregado {@link Pregunta}. Depende únicamente de los
 * puertos de salida, recibidos por constructor, así que no sabe si la persistencia es JPA o en memoria ni
 * si la notificación va a RabbitMQ. No lleva anotaciones de Spring; la infraestructura lo registra con
 * {@code @Bean}.
 */
public class QuestionApplicationService implements CrearPreguntaUseCase, EvaluarPreguntaUseCase {

    private final PreguntaRepositoryPort repositorio;
    private final NotificacionPort notificaciones;

    /**
     * Crea el servicio con sus puertos de salida.
     *
     * @param repositorio    persistencia de preguntas
     * @param notificaciones aviso de envíos a revisión
     */
    public QuestionApplicationService(PreguntaRepositoryPort repositorio, NotificacionPort notificaciones) {
        this.repositorio = Objects.requireNonNull(repositorio, "repositorio");
        this.notificaciones = Objects.requireNonNull(notificaciones, "notificaciones");
    }

    @Override
    public Pregunta crear(CrearPreguntaComando comando) {
        Objects.requireNonNull(comando, "comando");
        Pregunta pregunta = new Pregunta(new Enunciado(comando.enunciado()), new Justificacion(comando.justificacion()));
        comando.opciones().forEach(o -> pregunta.agregarOpcion(o.texto(), o.esCorrecta()));
        return repositorio.save(pregunta);
    }

    @Override
    public Pregunta enviarARevision(Long id) {
        Pregunta pregunta = obtener(id);
        pregunta.enviarARevision();
        Pregunta guardada = repositorio.save(pregunta);
        notificaciones.notificarEnvioARevision(guardada);
        return guardada;
    }

    @Override
    public List<Pregunta> listar() {
        return List.copyOf(repositorio.findAll());
    }

    @Override
    public Pregunta obtener(Long id) {
        return repositorio.findById(id).orElseThrow(() -> new PreguntaNoEncontradaException(id));
    }

    @Override
    public Pregunta aprobar(Long id, String observaciones) {
        Pregunta pregunta = obtener(id);
        pregunta.aprobar(observaciones);
        return repositorio.save(pregunta);
    }

    @Override
    public Pregunta rechazar(Long id, String observaciones) {
        Pregunta pregunta = obtener(id);
        pregunta.rechazar(observaciones);
        return repositorio.save(pregunta);
    }
}
