package co.edu.unicauca.saberpro.question.infrastructure.persistencia;

import co.edu.unicauca.saberpro.question.domain.ports.outbound.NotificacionPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.JpaQuestionAdapter;
import co.edu.unicauca.saberpro.question.infrastructure.memory.InMemoryQuestionAdapter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Comprueba que la propiedad {@code saberpro.persistencia} cambia el adaptador que Spring inyecta en el
 * puerto, sin tocar ninguna otra clase.
 */
class IntercambioAdaptadoresTest {

    @Nested
    @SpringBootTest
    class PorDefecto {

        @MockBean
        private NotificacionPort notificaciones;

        @Autowired
        private PreguntaRepositoryPort repositorio;

        @Test
        void usaJpa() {
            assertInstanceOf(JpaQuestionAdapter.class, repositorio);
        }
    }

    @Nested
    @SpringBootTest(properties = "saberpro.persistencia=memoria")
    class EnMemoria {

        @MockBean
        private NotificacionPort notificaciones;

        @Autowired
        private PreguntaRepositoryPort repositorio;

        @Test
        void usaElHashMap() {
            assertInstanceOf(InMemoryQuestionAdapter.class, repositorio);
        }
    }
}
