package co.edu.unicauca.saberpro.question.infrastructure.validation;

/**
 * Se lanza cuando validation-service no responde o responde con error; el controlador la traduce a 503.
 */
public class ServicioValidacionNoDisponibleException extends RuntimeException {

    /**
     * Crea la excepción conservando la causa técnica.
     *
     * @param causa error de la llamada HTTP
     */
    public ServicioValidacionNoDisponibleException(Throwable causa) {
        super("El servicio de validación no está disponible.", causa);
    }
}
