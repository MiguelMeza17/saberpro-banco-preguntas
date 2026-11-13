package co.edu.unicauca.saberpro.validation.config;

import co.edu.unicauca.saberpro.validation.pipeline.Filtros;
import co.edu.unicauca.saberpro.validation.pipeline.PipelineValidacion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Arma el pipeline con los filtros de HU03. Para agregar una regla basta con sumar un filtro aquí.
 */
@Configuration
public class ValidationConfiguration {

    @Bean
    public PipelineValidacion pipelineValidacion() {
        return new PipelineValidacion(Filtros.todos());
    }
}
