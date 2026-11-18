package co.edu.unicauca.saberpro.lifecycle.messaging;

import co.edu.unicauca.saberpro.lifecycle.revision.AsignacionRevisores;
import co.edu.unicauca.saberpro.lifecycle.revision.RevisionAsignada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor asíncrono de las solicitudes de revisión. Por cada mensaje de {@code pregunta.revisar.queue}
 * registra la recepción en el log (evidencia con {@code docker logs -f saberpro-lifecycle-service}) y
 * asigna un revisor.
 */
@Component
public class QuestionReviewListener {

    private static final Logger LOG = LoggerFactory.getLogger(QuestionReviewListener.class);

    private final AsignacionRevisores asignacion;

    public QuestionReviewListener(AsignacionRevisores asignacion) {
        this.asignacion = asignacion;
    }

    /**
     * Procesa una solicitud de revisión. Un mensaje sin identificador de pregunta se descarta sin reencolar
     * para no bloquear la cola con reintentos infinitos.
     *
     * @param mensaje solicitud publicada por question-service
     */
    @RabbitListener(queues = RabbitMQConfig.COLA_REVISION)
    public void recibir(SolicitudRevisionMessage mensaje) {
        if (mensaje == null || mensaje.preguntaId() == null) {
            LOG.warn("Mensaje de revisión descartado: no trae preguntaId ({})", mensaje);
            throw new AmqpRejectAndDontRequeueException("Mensaje de revisión sin preguntaId");
        }
        LOG.info("Solicitud de revisión recibida desde {}: pregunta id={}, estado={}, enunciado=\"{}\"",
                RabbitMQConfig.COLA_REVISION, mensaje.preguntaId(), mensaje.estado(), mensaje.enunciado());

        RevisionAsignada revision = asignacion.asignar(mensaje);

        LOG.info("Pregunta id={} asignada al revisor {} (estado de la revisión: {})",
                revision.preguntaId(), revision.revisor(), revision.estado());
    }
}
