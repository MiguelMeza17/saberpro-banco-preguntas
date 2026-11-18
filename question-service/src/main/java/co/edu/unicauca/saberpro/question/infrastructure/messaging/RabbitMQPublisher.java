package co.edu.unicauca.saberpro.question.infrastructure.messaging;

import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.NotificacionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

/**
 * Adaptador de salida que implementa {@link NotificacionPort} publicando una {@link SolicitudRevisionMessage}
 * en {@value RabbitMQConfig#EXCHANGE} con la clave {@value RabbitMQConfig#ROUTING_KEY}, que enruta a
 * {@value RabbitMQConfig#COLA_REVISION}. El host sale de {@code SPRING_RABBITMQ_HOST} (en Docker: {@code rabbitmq}).
 */
@Component
public class RabbitMQPublisher implements NotificacionPort {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final Clock reloj;

    @Autowired
    public RabbitMQPublisher(RabbitTemplate rabbitTemplate) {
        this(rabbitTemplate, Clock.systemUTC());
    }

    RabbitMQPublisher(RabbitTemplate rabbitTemplate, Clock reloj) {
        this.rabbitTemplate = rabbitTemplate;
        this.reloj = reloj;
    }

    @Override
    public void notificarEnvioARevision(Pregunta pregunta) {
        SolicitudRevisionMessage mensaje = new SolicitudRevisionMessage(
                pregunta.getId(),
                pregunta.getEnunciado() == null ? null : pregunta.getEnunciado().getTexto(),
                pregunta.getEstado().name(),
                Instant.now(reloj));
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, mensaje);
        LOG.info("Solicitud de revisión publicada en {}: pregunta id={}, estado={}",
                RabbitMQConfig.COLA_REVISION, mensaje.preguntaId(), mensaje.estado());
    }
}
