package co.edu.unicauca.saberpro.question.infrastructure.validation;

import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.ValidacionPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Adaptador de salida que implementa {@link ValidacionPort} llamando de forma síncrona a
 * {@code POST /api/validaciones} de validation-service. La URL base sale de {@code validation.service.url}
 * (en Docker, {@code http://validation-service:8082}).
 */
@Component
public class ValidationServiceClient implements ValidacionPort {

    private final RestClient restClient;

    /**
     * Crea el cliente.
     *
     * @param builder constructor de {@link RestClient} que provee Spring Boot
     * @param baseUrl URL base de validation-service
     */
    public ValidationServiceClient(RestClient.Builder builder,
                                   @Value("${validation.service.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public List<String> validar(Pregunta pregunta) {
        PreguntaAValidar cuerpo = new PreguntaAValidar(
                pregunta.getEnunciado() == null ? null : pregunta.getEnunciado().getTexto(),
                pregunta.getJustificacion() == null ? null : pregunta.getJustificacion().getTexto(),
                pregunta.getOpciones().stream().map(o -> new Opcion(o.getTexto(), o.isEsCorrecta())).toList());
        try {
            ResultadoValidacion resultado = restClient.post()
                    .uri("/api/validaciones")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(ResultadoValidacion.class);
            if (resultado == null) {
                throw new ServicioValidacionNoDisponibleException(null);
            }
            if (resultado.valida()) {
                return List.of();
            }
            return resultado.errores() == null || resultado.errores().isEmpty()
                    ? List.of("La pregunta no es válida.") : List.copyOf(resultado.errores());
        } catch (RestClientException e) {
            throw new ServicioValidacionNoDisponibleException(e);
        }
    }

    /** Cuerpo que espera validation-service. */
    record PreguntaAValidar(String enunciado, String justificacion, List<Opcion> opciones) {
    }

    /** Opción dentro del cuerpo. */
    record Opcion(String texto, boolean esCorrecta) {
    }

    /** Respuesta de validation-service. */
    record ResultadoValidacion(boolean valida, List<String> errores) {
    }
}
