package co.edu.unicauca.saberpro.question.domain.ports.outbound;

import co.edu.unicauca.saberpro.question.domain.model.Pregunta;

/**
 * Puerto de salida (driven) de notificaciones. El hexágono avisa que una pregunta pasó a revisión sin saber
 * si el aviso va a RabbitMQ ({@code RabbitMQPublisher}), al log o a otro canal.
 */
public interface NotificacionPort {

    /**
     * Notifica que una pregunta quedó pendiente de revisión.
     *
     * @param pregunta pregunta enviada a revisión, ya guardada
     */
    void notificarEnvioARevision(Pregunta pregunta);
}
