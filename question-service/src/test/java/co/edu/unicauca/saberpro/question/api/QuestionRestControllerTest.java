package co.edu.unicauca.saberpro.question.api;

import co.edu.unicauca.saberpro.question.domain.exception.PreguntaNoEncontradaException;
import co.edu.unicauca.saberpro.question.domain.exception.ReglaNegocioException;
import co.edu.unicauca.saberpro.question.domain.model.Enunciado;
import co.edu.unicauca.saberpro.question.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.question.domain.model.Justificacion;
import co.edu.unicauca.saberpro.question.domain.model.OpcionRespuesta;
import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaComando;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.EvaluarPreguntaUseCase;
import co.edu.unicauca.saberpro.question.infrastructure.validation.ServicioValidacionNoDisponibleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas del adaptador REST con MockMvc. Los puertos de entrada se simulan: aquí solo se comprueba la
 * traducción HTTP (códigos, JSON y manejo de errores).
 */
@WebMvcTest(QuestionRestController.class)
class QuestionRestControllerTest {

    private static final String CUERPO_VALIDO = """
            {"enunciado": "¿Qué patrón aísla el dominio?",
             "justificacion": "Los puertos y adaptadores aíslan el núcleo.",
             "opciones": [{"texto": "Hexagonal", "esCorrecta": true}, {"texto": "Singleton", "esCorrecta": false},
                          {"texto": "Monolito", "esCorrecta": false}, {"texto": "Capas", "esCorrecta": false}]}
            """;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CrearPreguntaUseCase crearPregunta;

    @MockBean
    private EvaluarPreguntaUseCase evaluarPregunta;

    private static Pregunta pregunta(long id, EstadoPregunta estado, String observaciones) {
        return Pregunta.reconstituir(id, new Enunciado("¿Qué patrón aísla el dominio?"),
                new Justificacion("Los puertos y adaptadores aíslan el núcleo."),
                List.of(new OpcionRespuesta("Hexagonal", true), new OpcionRespuesta("Singleton", false),
                        new OpcionRespuesta("Monolito", false), new OpcionRespuesta("Capas", false)),
                estado, observaciones);
    }

    @Test
    void listarResponde200() throws Exception {
        when(crearPregunta.listar()).thenReturn(List.of(pregunta(1, EstadoPregunta.BORRADOR, null)));

        mvc.perform(get("/api/preguntas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].opciones.length()").value(4))
                .andExpect(jsonPath("$[0].opciones[0].esCorrecta").value(true));
    }

    @Test
    void crearResponde201ConLocation() throws Exception {
        when(crearPregunta.crear(any(CrearPreguntaComando.class))).thenReturn(pregunta(5, EstadoPregunta.BORRADOR, null));

        mvc.perform(post("/api/preguntas").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/preguntas/5"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.estado").value("BORRADOR"));
    }

    @Test
    void crearResponde400SiIncumpleUnaRegla() throws Exception {
        when(crearPregunta.crear(any(CrearPreguntaComando.class)))
                .thenThrow(new ReglaNegocioException("La justificación debe contener al menos 15 caracteres."));

        mvc.perform(post("/api/preguntas").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").value("La justificación debe contener al menos 15 caracteres."));
    }

    @Test
    void crearResponde400ConJsonInvalido() throws Exception {
        mvc.perform(post("/api/preguntas").contentType(MediaType.APPLICATION_JSON).content("{no es json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearResponde503SiValidationServiceNoResponde() throws Exception {
        when(crearPregunta.crear(any(CrearPreguntaComando.class)))
                .thenThrow(new ServicioValidacionNoDisponibleException(new RuntimeException("timeout")));

        mvc.perform(post("/api/preguntas").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void obtenerResponde404SiNoExiste() throws Exception {
        when(crearPregunta.obtener(9L)).thenThrow(new PreguntaNoEncontradaException(9L));

        mvc.perform(get("/api/preguntas/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe la pregunta con id 9."));
    }

    @Test
    void enviarARevisionResponde202() throws Exception {
        when(crearPregunta.enviarARevision(1L)).thenReturn(pregunta(1, EstadoPregunta.PENDIENTE_REVISION, null));

        mvc.perform(post("/api/preguntas/1/enviar-revision"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_REVISION"));
    }

    @Test
    void aprobarSinCuerpoResponde200() throws Exception {
        when(evaluarPregunta.aprobar(eq(1L), isNull())).thenReturn(pregunta(1, EstadoPregunta.APROBADA, null));

        mvc.perform(post("/api/preguntas/1/aprobar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));
        verify(evaluarPregunta).aprobar(1L, null);
    }

    @Test
    void rechazarConObservacionesResponde200() throws Exception {
        when(evaluarPregunta.rechazar(1L, "Mejorar distractores"))
                .thenReturn(pregunta(1, EstadoPregunta.RECHAZADA, "Mejorar distractores"));

        mvc.perform(post("/api/preguntas/1/rechazar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observaciones\": \"Mejorar distractores\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"))
                .andExpect(jsonPath("$.observaciones").value("Mejorar distractores"));
    }

    @Test
    void rechazarSinObservacionesResponde400() throws Exception {
        when(evaluarPregunta.rechazar(eq(1L), isNull()))
                .thenThrow(new ReglaNegocioException("El rechazo debe incluir observaciones del revisor."));

        mvc.perform(post("/api/preguntas/1/rechazar"))
                .andExpect(status().isBadRequest());
    }
}
