package co.edu.unicauca.saberpro.lifecycle.messaging;

import co.edu.unicauca.saberpro.lifecycle.revision.AsignacionRevisores;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Prueba el listener y el contrato JSON del mensaje sin levantar RabbitMQ.
 */
class QuestionReviewListenerTest {

    private final AsignacionRevisores asignacion = new AsignacionRevisores(List.of("revisor1", "revisor2"));
    private final QuestionReviewListener listener = new QuestionReviewListener(asignacion);

    @Test
    void registraLaSolicitudRecibida() {
        listener.recibir(new SolicitudRevisionMessage(5L, "¿Qué es un puerto?", "PENDIENTE_REVISION",
                Instant.parse("2026-11-18T15:00:00Z")));

        assertEquals(1, asignacion.listar().size());
        assertEquals(5L, asignacion.listar().get(0).preguntaId());
        assertEquals("revisor1", asignacion.listar().get(0).revisor());
    }

    @Test
    void descartaMensajesSinPreguntaId() {
        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> listener.recibir(new SolicitudRevisionMessage(null, "x", "PENDIENTE_REVISION", null)));
        assertEquals(0, asignacion.listar().size());
    }

    @Test
    void convierteElJsonDeQuestionServiceAunqueTraigaOtraCabeceraDeTipo() {
        ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
        MessageConverter conversor = new RabbitMQConfig().jsonMessageConverter(objectMapper);
        String json = """
                {"preguntaId": 3, "enunciado": "¿Qué es un adaptador?", "estado": "PENDIENTE_REVISION",
                 "fechaEnvio": "2026-11-18T15:00:00Z"}""";
        MessageProperties propiedades = new MessageProperties();
        propiedades.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        propiedades.setHeader("__TypeId__", "co.edu.unicauca.saberpro.question.infrastructure.messaging.Otro");
        propiedades.setInferredArgumentType(SolicitudRevisionMessage.class);

        Object convertido = conversor.fromMessage(new Message(json.getBytes(StandardCharsets.UTF_8), propiedades));

        assertEquals(new SolicitudRevisionMessage(3L, "¿Qué es un adaptador?", "PENDIENTE_REVISION",
                Instant.parse("2026-11-18T15:00:00Z")), convertido);
        assertEquals(Jackson2JsonMessageConverter.class, conversor.getClass());
    }
}
