package co.edu.unicauca.saberpro.question;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Comprueba que el contexto de Spring arranca con los adaptadores reales (persistencia, validación y
 * RabbitMQ). No hace falta un broker: la conexión AMQP se abre en la primera publicación.
 */
@SpringBootTest
class QuestionServiceApplicationTests {

    @Test
    void contextoArranca() {
    }
}
