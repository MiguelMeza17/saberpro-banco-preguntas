package co.edu.unicauca.saberpro.question.infrastructure.jpa;

import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida que implementa {@link PreguntaRepositoryPort} con Spring Data JPA
 * (PostgreSQL en Docker, H2 en local). Se registra como bean en {@code PersistenceConfig}.
 */
public class JpaQuestionAdapter implements PreguntaRepositoryPort {

    private final SpringDataQuestionRepository repositorio;

    public JpaQuestionAdapter(SpringDataQuestionRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Pregunta save(Pregunta pregunta) {
        QuestionRecord guardado = repositorio.save(QuestionDomainMapper.aRecord(pregunta));
        return QuestionDomainMapper.aDominio(guardado);
    }

    @Override
    public Optional<Pregunta> findById(Long id) {
        return repositorio.findById(id).map(QuestionDomainMapper::aDominio);
    }

    @Override
    public List<Pregunta> findAll() {
        return repositorio.findAll().stream().map(QuestionDomainMapper::aDominio).toList();
    }
}
