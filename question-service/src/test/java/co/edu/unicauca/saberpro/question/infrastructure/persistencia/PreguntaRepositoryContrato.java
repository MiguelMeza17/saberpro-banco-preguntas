package co.edu.unicauca.saberpro.question.infrastructure.persistencia;

import co.edu.unicauca.saberpro.question.application.QuestionApplicationService;
import co.edu.unicauca.saberpro.question.domain.exception.PreguntaNoEncontradaException;
import co.edu.unicauca.saberpro.question.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaComando;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contrato de {@link PreguntaRepositoryPort}: cada adaptador (JPA y memoria) hereda estas pruebas y debe
 * pasarlas igual. Incluye el flujo completo de los casos de uso sobre el adaptador, que es la evidencia de
 * que se pueden intercambiar sin tocar el dominio ni el servicio.
 */
abstract class PreguntaRepositoryContrato {

    private PreguntaRepositoryPort repositorio;
    private QuestionApplicationService servicio;
    private final List<Long> notificadas = new ArrayList<>();

    /** @return el adaptador bajo prueba, vacío */
    protected abstract PreguntaRepositoryPort crearAdaptador();

    @BeforeEach
    void prepararContrato() {
        repositorio = crearAdaptador();
        notificadas.clear();
        servicio = new QuestionApplicationService(repositorio, p -> notificadas.add(p.getId()), p -> List.of());
    }

    private static CrearPreguntaComando comando(String enunciado) {
        return new CrearPreguntaComando(enunciado, "Los puertos y adaptadores aíslan el núcleo.",
                List.of(new CrearPreguntaComando.Opcion("Hexagonal", true),
                        new CrearPreguntaComando.Opcion("Singleton", false),
                        new CrearPreguntaComando.Opcion("Monolito", false),
                        new CrearPreguntaComando.Opcion("Capas", false)));
    }

    @Test
    void saveAsignaIdentificadorYConservaElContenido() {
        Pregunta creada = servicio.crear(comando("¿Qué patrón aísla el dominio?"));

        assertNotNull(creada.getId());
        Pregunta leida = repositorio.findById(creada.getId()).orElseThrow();
        assertEquals("¿Qué patrón aísla el dominio?", leida.getEnunciado().getTexto());
        assertEquals(4, leida.getOpciones().size());
        assertEquals("Hexagonal", leida.getOpciones().get(0).getTexto());
        assertTrue(leida.getOpciones().get(0).isEsCorrecta());
        assertEquals(EstadoPregunta.BORRADOR, leida.getEstado());
    }

    @Test
    void findAllDevuelveTodasEnOrden() {
        Long primera = servicio.crear(comando("Primera pregunta")).getId();
        Long segunda = servicio.crear(comando("Segunda pregunta")).getId();

        assertEquals(List.of(primera, segunda), repositorio.findAll().stream().map(Pregunta::getId).toList());
    }

    @Test
    void findByIdVacioSiNoExiste() {
        assertTrue(repositorio.findById(999L).isEmpty());
        assertThrows(PreguntaNoEncontradaException.class, () -> servicio.obtener(999L));
    }

    @Test
    void flujoCompletoDeLosCasosDeUso() {
        Long id = servicio.crear(comando("¿Qué patrón aísla el dominio?")).getId();

        servicio.enviarARevision(id);
        assertEquals(EstadoPregunta.PENDIENTE_REVISION, repositorio.findById(id).orElseThrow().getEstado());
        assertEquals(List.of(id), notificadas);

        servicio.rechazar(id, "Mejorar los distractores");
        Pregunta rechazada = repositorio.findById(id).orElseThrow();
        assertEquals(EstadoPregunta.RECHAZADA, rechazada.getEstado());
        assertEquals("Mejorar los distractores", rechazada.getObservaciones());
        assertEquals(1, repositorio.findAll().size());
    }
}
