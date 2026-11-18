package co.edu.unicauca.saberpro.question.infrastructure.persistencia;

import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.JpaQuestionAdapter;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.SpringDataQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Contrato del puerto con el adaptador JPA sobre H2. Cada prueba parte de la tabla vacía.
 */
@DataJpaTest
class JpaQuestionAdapterTest extends PreguntaRepositoryContrato {

    @Autowired
    private SpringDataQuestionRepository springData;

    @Override
    protected PreguntaRepositoryPort crearAdaptador() {
        springData.deleteAll();
        return new JpaQuestionAdapter(springData);
    }
}
