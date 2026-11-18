package co.edu.unicauca.saberpro.lifecycle;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Arranque del contexto sin RabbitMQ: el contenedor del listener no se inicia en esta prueba.
 */
@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
class LifecycleServiceApplicationTests {

    @Test
    void contextoArranca() {
    }
}
