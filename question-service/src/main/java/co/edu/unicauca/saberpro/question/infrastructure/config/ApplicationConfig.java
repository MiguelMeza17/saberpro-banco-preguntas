package co.edu.unicauca.saberpro.question.infrastructure.config;

import co.edu.unicauca.saberpro.question.application.QuestionApplicationService;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.NotificacionPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.ValidacionPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra el servicio de aplicación como bean. El servicio no lleva anotaciones de Spring: recibe los
 * puertos de salida que la infraestructura publique (adaptador JPA o en memoria, el cliente de
 * validation-service y el publicador de notificaciones), así el dominio y la aplicación no dependen del framework.
 */
@Configuration
public class ApplicationConfig {

    /**
     * Servicio que implementa {@code CrearPreguntaUseCase} y {@code EvaluarPreguntaUseCase}.
     *
     * @param repositorio    adaptador de persistencia activo
     * @param notificaciones adaptador de notificaciones activo
     * @param validacion     cliente de la validación estructural
     * @return el servicio de aplicación
     */
    @Bean
    public QuestionApplicationService questionApplicationService(PreguntaRepositoryPort repositorio,
                                                                 NotificacionPort notificaciones,
                                                                 ValidacionPort validacion) {
        return new QuestionApplicationService(repositorio, notificaciones, validacion);
    }
}
