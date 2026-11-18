package co.edu.unicauca.saberpro.question.application;

import co.edu.unicauca.saberpro.question.domain.exception.PreguntaNoEncontradaException;
import co.edu.unicauca.saberpro.question.domain.exception.ReglaNegocioException;
import co.edu.unicauca.saberpro.question.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaComando;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.NotificacionPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.ValidacionPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del servicio de aplicación en Java puro: los puertos de salida se sustituyen por dobles en
 * memoria, sin Spring ni base de datos.
 */
class QuestionApplicationServiceTest {

    private RepositorioFalso repositorio;
    private NotificacionesFalsas notificaciones;
    private List<String> erroresDeValidacion;
    private QuestionApplicationService servicio;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioFalso();
        notificaciones = new NotificacionesFalsas();
        erroresDeValidacion = new ArrayList<>();
        ValidacionPort validacion = pregunta -> List.copyOf(erroresDeValidacion);
        servicio = new QuestionApplicationService(repositorio, notificaciones, validacion);
    }

    private static CrearPreguntaComando comandoValido() {
        return new CrearPreguntaComando(
                "¿Qué patrón aísla el dominio de la infraestructura?",
                "Los puertos y adaptadores separan el núcleo de la tecnología.",
                List.of(new CrearPreguntaComando.Opcion("Hexagonal", true),
                        new CrearPreguntaComando.Opcion("Capas", false),
                        new CrearPreguntaComando.Opcion("MVC", false),
                        new CrearPreguntaComando.Opcion("Monolito", false)));
    }

    private Pregunta pendiente() {
        Pregunta creada = servicio.crear(comandoValido());
        return servicio.enviarARevision(creada.getId());
    }

    @Test
    void exigeLosPuertosDeSalida() {
        ValidacionPort validacion = pregunta -> List.of();
        assertThrows(NullPointerException.class, () -> new QuestionApplicationService(null, notificaciones, validacion));
        assertThrows(NullPointerException.class, () -> new QuestionApplicationService(repositorio, null, validacion));
        assertThrows(NullPointerException.class, () -> new QuestionApplicationService(repositorio, notificaciones, null));
    }

    @Nested
    class Crear {

        @Test
        void guardaLaPreguntaEnBorradorConIdentificador() {
            Pregunta creada = servicio.crear(comandoValido());

            assertNotNull(creada.getId());
            assertEquals(EstadoPregunta.BORRADOR, creada.getEstado());
            assertEquals(4, creada.getOpciones().size());
            assertTrue(repositorio.findById(creada.getId()).isPresent());
        }

        @Test
        void rechazaUnaJustificacionCorta() {
            CrearPreguntaComando comando = new CrearPreguntaComando("Enunciado", "Corta", List.of());

            assertThrows(ReglaNegocioException.class, () -> servicio.crear(comando));
            assertTrue(repositorio.findAll().isEmpty());
        }

        @Test
        void noGuardaSiFallaLaValidacionEstructural() {
            erroresDeValidacion.add("Debe tener exactamente 4 opciones.");

            ReglaNegocioException e = assertThrows(ReglaNegocioException.class, () -> servicio.crear(comandoValido()));
            assertTrue(e.getMessage().contains("Debe tener exactamente 4 opciones."));
            assertTrue(repositorio.findAll().isEmpty());
        }

        @Test
        void rechazaOpcionesRepetidas() {
            CrearPreguntaComando comando = new CrearPreguntaComando("Enunciado",
                    "Justificación con suficiente detalle.",
                    List.of(new CrearPreguntaComando.Opcion("A", true), new CrearPreguntaComando.Opcion("a", false)));

            assertThrows(ReglaNegocioException.class, () -> servicio.crear(comando));
        }
    }

    @Nested
    class EnviarARevision {

        @Test
        void cambiaElEstadoYNotificaUnaSolaVez() {
            Pregunta enviada = pendiente();

            assertEquals(EstadoPregunta.PENDIENTE_REVISION, enviada.getEstado());
            assertEquals(List.of(enviada.getId()), notificaciones.enviadas);
        }

        @Test
        void noNotificaSiIncumpleLasInvariantes() {
            CrearPreguntaComando incompleta = new CrearPreguntaComando("Enunciado",
                    "Justificación con suficiente detalle.", List.of(new CrearPreguntaComando.Opcion("A", true)));
            Pregunta creada = servicio.crear(incompleta);

            assertThrows(ReglaNegocioException.class, () -> servicio.enviarARevision(creada.getId()));
            assertTrue(notificaciones.enviadas.isEmpty());
            assertEquals(EstadoPregunta.BORRADOR, repositorio.findById(creada.getId()).orElseThrow().getEstado());
        }

        @Test
        void fallaSiLaPreguntaNoExiste() {
            assertThrows(PreguntaNoEncontradaException.class, () -> servicio.enviarARevision(99L));
        }

        @Test
        void siLaNotificacionFallaNoGuardaElCambioDeEstado() {
            Long id = servicio.crear(comandoValido()).getId();
            notificaciones.fallar = true;

            assertThrows(IllegalStateException.class, () -> servicio.enviarARevision(id));
            assertEquals(EstadoPregunta.BORRADOR, repositorio.findById(id).orElseThrow().getEstado());
        }
    }

    @Nested
    class Consultar {

        @Test
        void listaTodasLasPreguntas() {
            servicio.crear(comandoValido());
            servicio.crear(comandoValido());

            assertEquals(2, servicio.listar().size());
        }

        @Test
        void obtenerFallaSiNoExiste() {
            assertThrows(PreguntaNoEncontradaException.class, () -> servicio.obtener(7L));
        }
    }

    @Nested
    class Evaluar {

        @Test
        void apruebaUnaPreguntaPendiente() {
            Pregunta aprobada = servicio.aprobar(pendiente().getId(), "Bien planteada");

            assertEquals(EstadoPregunta.APROBADA, aprobada.getEstado());
            assertEquals("Bien planteada", aprobada.getObservaciones());
        }

        @Test
        void rechazaConObservaciones() {
            Pregunta rechazada = servicio.rechazar(pendiente().getId(), "Mejorar los distractores");

            assertEquals(EstadoPregunta.RECHAZADA, rechazada.getEstado());
            assertEquals("Mejorar los distractores", rechazada.getObservaciones());
        }

        @Test
        void noRechazaSinObservaciones() {
            Long id = pendiente().getId();

            assertThrows(ReglaNegocioException.class, () -> servicio.rechazar(id, " "));
        }

        @Test
        void noEvaluaUnBorrador() {
            Long id = servicio.crear(comandoValido()).getId();

            assertThrows(ReglaNegocioException.class, () -> servicio.aprobar(id, null));
        }
    }

    /** Repositorio en memoria solo para las pruebas. */
    private static final class RepositorioFalso implements PreguntaRepositoryPort {

        private final Map<Long, Pregunta> datos = new LinkedHashMap<>();
        private long secuencia;

        @Override
        public Pregunta save(Pregunta pregunta) {
            if (pregunta.getId() == null) {
                pregunta.asignarId(++secuencia);
            }
            datos.put(pregunta.getId(), copiar(pregunta));
            return pregunta;
        }

        @Override
        public Optional<Pregunta> findById(Long id) {
            return Optional.ofNullable(datos.get(id)).map(RepositorioFalso::copiar);
        }

        private static Pregunta copiar(Pregunta p) {
            return Pregunta.reconstituir(p.getId(), p.getEnunciado(), p.getJustificacion(), p.getOpciones(),
                    p.getEstado(), p.getObservaciones());
        }

        @Override
        public List<Pregunta> findAll() {
            return new ArrayList<>(datos.values());
        }
    }

    /** Registra los identificadores notificados. */
    private static final class NotificacionesFalsas implements NotificacionPort {

        private final List<Long> enviadas = new ArrayList<>();
        private boolean fallar;

        @Override
        public void notificarEnvioARevision(Pregunta pregunta) {
            if (fallar) {
                throw new IllegalStateException("broker caído");
            }
            enviadas.add(pregunta.getId());
        }
    }
}
