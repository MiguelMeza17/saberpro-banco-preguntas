package co.edu.unicauca.saberpro.question.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología AMQP del envío a revisión, la misma que declara lifecycle-service: exchange directo
 * {@value #EXCHANGE}, clave {@value #ROUTING_KEY} y cola durable {@value #COLA_REVISION}. Declararla en los
 * dos servicios es idempotente y evita depender del orden de arranque.
 */
@Configuration
public class RabbitMQConfig {

    /** Cola que consume lifecycle-service. */
    public static final String COLA_REVISION = "pregunta.revisar.queue";
    /** Exchange directo de los eventos de preguntas. */
    public static final String EXCHANGE = "pregunta.exchange";
    /** Clave de enrutamiento del envío a revisión. */
    public static final String ROUTING_KEY = "pregunta.revisar";

    @Bean
    Queue colaRevision() {
        return QueueBuilder.durable(COLA_REVISION).build();
    }

    @Bean
    DirectExchange preguntaExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    Binding bindingRevision(Queue colaRevision, DirectExchange preguntaExchange) {
        return BindingBuilder.bind(colaRevision).to(preguntaExchange).with(ROUTING_KEY);
    }

    /**
     * Publica los mensajes como JSON con una copia del ObjectMapper de Spring Boot que siempre escribe las
     * fechas java.time en ISO-8601 ({@code "2026-11-18T15:00:00Z"}). Spring Boot lo conecta al
     * {@code RabbitTemplate}.
     *
     * @param objectMapper ObjectMapper de Spring Boot
     * @return conversor JSON
     */
    @Bean
    MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(
                objectMapper.copy().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }
}
