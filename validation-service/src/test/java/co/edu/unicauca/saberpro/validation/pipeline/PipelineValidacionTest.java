package co.edu.unicauca.saberpro.validation.pipeline;

import co.edu.unicauca.saberpro.validation.pipeline.PreguntaAValidar.Opcion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineValidacionTest {

    private final PipelineValidacion pipeline = new PipelineValidacion(Filtros.todos());

    private static List<Opcion> opcionesValidas() {
        return List.of(new Opcion("Hexagonal", true), new Opcion("Singleton", false),
                new Opcion("Monolito", false), new Opcion("Capas", false));
    }

    @Test
    void preguntaValidaNoTieneErrores() {
        PreguntaAValidar p = new PreguntaAValidar("¿Qué patrón aísla el dominio?",
                "Los puertos y adaptadores aíslan el núcleo.", opcionesValidas());

        assertTrue(pipeline.validar(p).isEmpty());
    }

    @Test
    void acumulaTodosLosErroresDeUnaVez() {
        PreguntaAValidar p = new PreguntaAValidar(" ", "corta",
                List.of(new Opcion("A", true), new Opcion("a", true), new Opcion("", false)));

        List<String> errores = pipeline.validar(p);

        assertEquals(6, errores.size(), errores::toString);
        assertTrue(errores.contains("El enunciado es obligatorio."));
        assertTrue(errores.contains("La justificación debe tener al menos 15 caracteres."));
        assertTrue(errores.contains("Debe tener exactamente 4 opciones de respuesta."));
        assertTrue(errores.contains("La opción C no puede estar vacía."));
        assertTrue(errores.contains("Las opciones de respuesta no pueden repetirse."));
        assertTrue(errores.contains("Debe existir exactamente 1 opción correcta (hay 2)."));
    }

    @Test
    void sinClaveEsInvalida() {
        PreguntaAValidar p = new PreguntaAValidar("Enunciado", "Justificación suficiente.",
                List.of(new Opcion("A", false), new Opcion("B", false), new Opcion("C", false), new Opcion("D", false)));

        assertEquals(List.of("Debe existir exactamente 1 opción correcta (hay 0)."), pipeline.validar(p));
    }

    @Test
    void preguntaNulaEsInvalida() {
        assertEquals(List.of("La pregunta no puede ser nula."), pipeline.validar(null));
    }
}
