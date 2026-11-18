package co.edu.unicauca.saberpro.lifecycle.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología AMQP del envío a revisión. question-service publica en el exchange {@value #EXCHANGE} con la
 * clave {@value #ROUTING_KEY}; la cola {@value #COLA_REVISION} queda enlazada a esa clave. Ambos servicios
 * declaran la misma topología (la declaración es idempotente), así no importa cuál arranque primero.
 */
@Configuration
public class RabbitMQConfig {

    /** Cola que consume {@link QuestionReviewListener}. */
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
     * Convierte el JSON del mensaje al tipo del parámetro del listener ({@link SolicitudRevisionMessage}),
     * ignorando la cabecera de tipo que pone el publicador: las clases de question-service no existen aquí.
     *
     * @param objectMapper ObjectMapper de Spring Boot (con soporte de fechas java.time)
     * @return conversor JSON para los listeners
     */
    @Bean
    MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter conversor = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper mapper = new DefaultJackson2JavaTypeMapper();
        mapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        mapper.setTrustedPackages("*");
        conversor.setJavaTypeMapper(mapper);
        return conversor;
    }
}
