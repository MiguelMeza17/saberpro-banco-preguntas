package co.edu.unicauca.saberpro.question;

import co.edu.unicauca.saberpro.question.domain.ports.outbound.NotificacionPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Comprueba que el contexto de Spring arranca. Los puertos de salida se simulan porque sus adaptadores
 * (JPA / memoria en #4 y RabbitMQ en #6) se agregan en otros issues; con ellos el mock sigue siendo válido.
 */
@SpringBootTest
class QuestionServiceApplicationTests {

    @MockBean
    private PreguntaRepositoryPort repositorio;

    @MockBean
    private NotificacionPort notificaciones;

    @Test
    void contextoArranca() {
    }
}
