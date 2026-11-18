package co.edu.unicauca.saberpro.question;

import co.edu.unicauca.saberpro.question.domain.ports.outbound.NotificacionPort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Comprueba que el contexto de Spring arranca con los adaptadores reales de persistencia. La notificación se
 * simula hasta que exista el publicador de RabbitMQ (#6).
 */
@SpringBootTest
class QuestionServiceApplicationTests {

    @MockBean
    private NotificacionPort notificaciones;

    @Test
    void contextoArranca() {
    }
}
