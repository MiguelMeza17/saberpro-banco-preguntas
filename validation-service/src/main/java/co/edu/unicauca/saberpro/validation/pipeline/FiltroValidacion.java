package co.edu.unicauca.saberpro.validation.pipeline;

import java.util.List;

/**
 * Filtro del pipeline de validación estructural (patrón Pipes &amp; Filters de HU03).
 * Cada filtro revisa una regla y agrega sus errores; no detiene a los demás, para que
 * el autor reciba todos los problemas de una vez.
 */
public interface FiltroValidacion {

    /**
     * Aplica la regla del filtro.
     *
     * @param pregunta pregunta a validar
     * @param errores  lista donde se acumulan los errores encontrados
     */
    void aplicar(PreguntaAValidar pregunta, List<String> errores);

    /**
     * Indica si un texto es nulo o está en blanco.
     *
     * @param texto texto a revisar
     * @return {@code true} si no tiene contenido
     */
    static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
