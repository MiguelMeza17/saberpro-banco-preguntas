package co.edu.unicauca.saberpro.question.infrastructure.persistencia;

import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.question.infrastructure.memory.InMemoryQuestionAdapter;

/**
 * Contrato del puerto con el adaptador en memoria.
 */
class InMemoryQuestionAdapterTest extends PreguntaRepositoryContrato {

    @Override
    protected PreguntaRepositoryPort crearAdaptador() {
        return new InMemoryQuestionAdapter();
    }
}
