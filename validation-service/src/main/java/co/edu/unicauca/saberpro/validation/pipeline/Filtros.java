package co.edu.unicauca.saberpro.validation.pipeline;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static co.edu.unicauca.saberpro.validation.pipeline.FiltroValidacion.vacio;

/**
 * Filtros de la validación estructural HU03, en el orden en que se aplican.
 */
public final class Filtros {

    /** Número exacto de opciones de una pregunta de selección múltiple con única respuesta. */
    public static final int NUMERO_OPCIONES = 4;

    /** Longitud mínima de la justificación. */
    public static final int LONGITUD_MINIMA_JUSTIFICACION = 15;

    private Filtros() {
    }

    /** El enunciado es obligatorio. */
    public static final FiltroValidacion ENUNCIADO = (p, errores) -> {
        if (vacio(p.enunciado())) {
            errores.add("El enunciado es obligatorio.");
        }
    };

    /** La justificación debe tener al menos 15 caracteres. */
    public static final FiltroValidacion JUSTIFICACION = (p, errores) -> {
        if (p.justificacion() == null || p.justificacion().trim().length() < LONGITUD_MINIMA_JUSTIFICACION) {
            errores.add("La justificación debe tener al menos " + LONGITUD_MINIMA_JUSTIFICACION + " caracteres.");
        }
    };

    /** Deben existir exactamente 4 opciones, ninguna vacía. */
    public static final FiltroValidacion OPCIONES_COMPLETAS = (p, errores) -> {
        if (p.opciones().size() != NUMERO_OPCIONES) {
            errores.add("Debe tener exactamente " + NUMERO_OPCIONES + " opciones de respuesta.");
        }
        for (int i = 0; i < p.opciones().size(); i++) {
            PreguntaAValidar.Opcion opcion = p.opciones().get(i);
            if (opcion == null || vacio(opcion.texto())) {
                errores.add("La opción " + (char) ('A' + i) + " no puede estar vacía.");
            }
        }
    };

    /** Las opciones no pueden repetirse (sin distinguir mayúsculas ni espacios de los extremos). */
    public static final FiltroValidacion OPCIONES_NO_REPETIDAS = (p, errores) -> {
        Set<String> vistas = new HashSet<>();
        boolean repetidas = p.opciones().stream()
                .filter(o -> o != null && !vacio(o.texto()))
                .map(o -> o.texto().trim().toLowerCase(Locale.ROOT))
                .anyMatch(texto -> !vistas.add(texto));
        if (repetidas) {
            errores.add("Las opciones de respuesta no pueden repetirse.");
        }
    };

    /** Debe haber una sola clave correcta. */
    public static final FiltroValidacion CLAVE_UNICA = (p, errores) -> {
        long claves = p.opciones().stream().filter(o -> o != null && o.esCorrecta()).count();
        if (claves != 1) {
            errores.add("Debe existir exactamente 1 opción correcta (hay " + claves + ").");
        }
    };

    /**
     * Filtros en orden de aplicación.
     *
     * @return lista inmutable de filtros
     */
    public static List<FiltroValidacion> todos() {
        return List.of(ENUNCIADO, JUSTIFICACION, OPCIONES_COMPLETAS, OPCIONES_NO_REPETIDAS, CLAVE_UNICA);
    }
}
