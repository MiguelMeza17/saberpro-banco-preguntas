package co.edu.unicauca.saberpro.lifecycle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Arranque del microservicio lifecycle-service (puerto 8084).
 */
@SpringBootApplication
public class LifecycleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LifecycleServiceApplication.class, args);
    }
}
