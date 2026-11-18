package co.edu.unicauca.saberpro.question.infrastructure.config;

import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.JpaQuestionAdapter;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.SpringDataQuestionRepository;
import co.edu.unicauca.saberpro.question.infrastructure.memory.InMemoryQuestionAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elige el adaptador de persistencia con la propiedad {@code saberpro.persistencia}
 * (variable {@code SABERPRO_PERSISTENCIA}): {@code jpa} (por defecto) o {@code memoria}.
 * El intercambio no toca el dominio ni el servicio de aplicación: solo cambia qué bean implementa
 * {@link PreguntaRepositoryPort}.
 */
@Configuration
public class PersistenceConfig {

    /**
     * Persistencia relacional con JPA (PostgreSQL en Docker, H2 en local).
     *
     * @param repositorio repositorio Spring Data
     * @return adaptador JPA
     */
    @Bean
    @ConditionalOnProperty(name = "saberpro.persistencia", havingValue = "jpa", matchIfMissing = true)
    public PreguntaRepositoryPort jpaQuestionAdapter(SpringDataQuestionRepository repositorio) {
        return new JpaQuestionAdapter(repositorio);
    }

    /**
     * Persistencia en memoria con un HashMap.
     *
     * @return adaptador en memoria
     */
    @Bean
    @ConditionalOnProperty(name = "saberpro.persistencia", havingValue = "memoria")
    public PreguntaRepositoryPort inMemoryQuestionAdapter() {
        return new InMemoryQuestionAdapter();
    }
}
