package co.edu.unicauca.saberpro.question;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Arranque del microservicio question-service (puerto 8083).
 */
@SpringBootApplication
public class QuestionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuestionServiceApplication.class, args);
    }
}
