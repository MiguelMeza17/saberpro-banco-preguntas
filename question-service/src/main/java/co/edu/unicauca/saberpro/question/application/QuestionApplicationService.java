package co.edu.unicauca.saberpro.question.application;

import co.edu.unicauca.saberpro.question.domain.exception.PreguntaNoEncontradaException;
import co.edu.unicauca.saberpro.question.domain.exception.ReglaNegocioException;
import co.edu.unicauca.saberpro.question.domain.model.Enunciado;
import co.edu.unicauca.saberpro.question.domain.model.Justificacion;
import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaComando;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.EvaluarPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.NotificacionPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.ValidacionPort;

import java.util.List;
import java.util.Objects;

/**
 * Servicio de aplicación que implementa los puertos de entrada de question-service.
 * <p>
 * Solo orquesta: las reglas de negocio las aplica el agregado {@link Pregunta}. Depende únicamente de los
 * puertos de salida, recibidos por constructor, así que no sabe si la persistencia es JPA o en memoria, si
 * la validación estructural es un microservicio REST ni si la notificación va a RabbitMQ. No lleva anotaciones de Spring; la infraestructura lo registra con
 * {@code @Bean}.
 */
public class QuestionApplicationService implements CrearPreguntaUseCase, EvaluarPreguntaUseCase {

    private final PreguntaRepositoryPort repositorio;
    private final NotificacionPort notificaciones;
    private final ValidacionPort validacion;

    /**
     * Crea el servicio con sus puertos de salida.
     *
     * @param repositorio    persistencia de preguntas
     * @param notificaciones aviso de envíos a revisión
     * @param validacion     validación estructural previa al guardado
     */
    public QuestionApplicationService(PreguntaRepositoryPort repositorio, NotificacionPort notificaciones,
                                      ValidacionPort validacion) {
        this.repositorio = Objects.requireNonNull(repositorio, "repositorio");
        this.notificaciones = Objects.requireNonNull(notificaciones, "notificaciones");
        this.validacion = Objects.requireNonNull(validacion, "validacion");
    }

    /**
     * Crea la pregunta en borrador. Primero el agregado aplica sus invariantes y después la validación
     * estructural (HU03); si alguna falla no se guarda nada.
     *
     * @throws ReglaNegocioException si incumple una regla del dominio o la validación estructural
     */
    @Override
    public Pregunta crear(CrearPreguntaComando comando) {
        Objects.requireNonNull(comando, "comando");
        Pregunta pregunta = new Pregunta(new Enunciado(comando.enunciado()), new Justificacion(comando.justificacion()));
        comando.opciones().forEach(o -> pregunta.agregarOpcion(o.texto(), o.esCorrecta()));
        List<String> errores = validacion.validar(pregunta);
        if (!errores.isEmpty()) {
            throw new ReglaNegocioException("La pregunta no pasó la validación estructural: "
                    + String.join(" ", errores));
        }
        return repositorio.save(pregunta);
    }

    /**
     * Envía la pregunta a revisión. Primero se notifica y después se guarda: si la notificación falla
     * (por ejemplo, RabbitMQ caído) el cambio de estado no se persiste y el autor puede reintentar, en lugar de
     * quedar una pregunta pendiente que nadie va a revisar.
     */
    @Override
    public Pregunta enviarARevision(Long id) {
        Pregunta pregunta = obtener(id);
        pregunta.enviarARevision();
        notificaciones.notificarEnvioARevision(pregunta);
        return repositorio.save(pregunta);
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
