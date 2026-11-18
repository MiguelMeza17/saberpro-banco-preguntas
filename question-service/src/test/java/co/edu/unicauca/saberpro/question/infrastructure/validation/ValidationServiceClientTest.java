package co.edu.unicauca.saberpro.question.infrastructure.validation;

import co.edu.unicauca.saberpro.question.domain.model.Enunciado;
import co.edu.unicauca.saberpro.question.domain.model.Justificacion;
import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Pruebas del cliente de validation-service contra un servidor HTTP simulado.
 */
class ValidationServiceClientTest {

    private MockRestServiceServer servidor;
    private ValidationServiceClient cliente;
    private Pregunta pregunta;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        cliente = new ValidationServiceClient(builder, "http://validation-service:8082");
        pregunta = new Pregunta(new Enunciado("¿Qué patrón aísla el dominio?"),
                new Justificacion("Los puertos y adaptadores aíslan el núcleo."));
        pregunta.agregarOpcion("Hexagonal", true);
    }

    @Test
    void enviaLaPreguntaYDevuelveVacioSiEsValida() {
        servidor.expect(requestTo("http://validation-service:8082/api/validaciones"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"enunciado": "¿Qué patrón aísla el dominio?",
                         "justificacion": "Los puertos y adaptadores aíslan el núcleo.",
                         "opciones": [{"texto": "Hexagonal", "esCorrecta": true}]}
                        """))
                .andRespond(withSuccess("{\"valida\": true, \"errores\": []}", MediaType.APPLICATION_JSON));

        assertTrue(cliente.validar(pregunta).isEmpty());
        servidor.verify();
    }

    @Test
    void devuelveLosErroresSiNoEsValida() {
        servidor.expect(requestTo("http://validation-service:8082/api/validaciones"))
                .andRespond(withSuccess("{\"valida\": false, \"errores\": [\"Debe tener exactamente 4 opciones.\"]}",
                        MediaType.APPLICATION_JSON));

        assertEquals(List.of("Debe tener exactamente 4 opciones."), cliente.validar(pregunta));
    }

    @Test
    void fallaConExcepcionPropiaSiElServicioResponde500() {
        servidor.expect(requestTo("http://validation-service:8082/api/validaciones")).andRespond(withServerError());

        assertThrows(ServicioValidacionNoDisponibleException.class, () -> cliente.validar(pregunta));
    }
}
