package co.edu.unicauca.saberpro.question.infrastructure.messaging;

import co.edu.unicauca.saberpro.question.domain.model.Enunciado;
import co.edu.unicauca.saberpro.question.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.question.domain.model.Justificacion;
import co.edu.unicauca.saberpro.question.domain.model.OpcionRespuesta;
import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Pruebas del publicador: destino del mensaje y forma del JSON que espera lifecycle-service.
 */
class RabbitMQPublisherTest {

    private static final Instant AHORA = Instant.parse("2026-11-18T15:00:00Z");

    private static Pregunta pendiente() {
        return Pregunta.reconstituir(7L, new Enunciado("¿Qué patrón aísla el dominio?"),
                new Justificacion("Los puertos y adaptadores aíslan el núcleo."),
                List.of(new OpcionRespuesta("Hexagonal", true), new OpcionRespuesta("Singleton", false),
                        new OpcionRespuesta("Monolito", false), new OpcionRespuesta("Capas", false)),
                EstadoPregunta.PENDIENTE_REVISION);
    }

    @Test
    void publicaEnElExchangeConLaClaveDeRevision() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        RabbitMQPublisher publicador = new RabbitMQPublisher(template, Clock.fixed(AHORA, ZoneOffset.UTC));

        publicador.notificarEnvioARevision(pendiente());

        ArgumentCaptor<Object> mensaje = ArgumentCaptor.forClass(Object.class);
        verify(template).convertAndSend(eq("pregunta.exchange"), eq("pregunta.revisar"), mensaje.capture());
        assertEquals(new SolicitudRevisionMessage(7L, "¿Qué patrón aísla el dominio?", "PENDIENTE_REVISION", AHORA),
                mensaje.getValue());
    }

    @Test
    void elJsonTieneElContratoDeLifecycleService() throws Exception {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
        RabbitMQConfig config = new RabbitMQConfig();
        Message amqp = config.jsonMessageConverter(objectMapper).toMessage(
                new SolicitudRevisionMessage(7L, "Enunciado", "PENDIENTE_REVISION", AHORA), new MessageProperties());

        JsonNode json = objectMapper.readTree(amqp.getBody());
        assertEquals(7L, json.get("preguntaId").asLong());
        assertEquals("Enunciado", json.get("enunciado").asText());
        assertEquals("PENDIENTE_REVISION", json.get("estado").asText());
        assertEquals("2026-11-18T15:00:00Z", json.get("fechaEnvio").asText());
        assertEquals("application/json", amqp.getMessageProperties().getContentType());
    }
}
