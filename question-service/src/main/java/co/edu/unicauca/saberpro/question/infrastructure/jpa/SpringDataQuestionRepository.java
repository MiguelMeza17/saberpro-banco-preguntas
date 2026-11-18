package co.edu.unicauca.saberpro.question.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data de {@link QuestionRecord}. Solo lo usa {@link JpaQuestionAdapter}.
 */
public interface SpringDataQuestionRepository extends JpaRepository<QuestionRecord, Long> {
}
