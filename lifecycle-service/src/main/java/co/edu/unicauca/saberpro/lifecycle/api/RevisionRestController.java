package co.edu.unicauca.saberpro.lifecycle.api;

import co.edu.unicauca.saberpro.lifecycle.revision.AsignacionRevisores;
import co.edu.unicauca.saberpro.lifecycle.revision.RevisionAsignada;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Consulta de las revisiones asignadas a partir de los mensajes de RabbitMQ. Sirve para comprobar con
 * Thunder Client ({@code GET http://localhost:8084/api/revisiones}) que el mensaje llegó a lifecycle-service.
 */
@RestController
@RequestMapping("/api/revisiones")
public class RevisionRestController {

    private final AsignacionRevisores asignacion;

    public RevisionRestController(AsignacionRevisores asignacion) {
        this.asignacion = asignacion;
    }

    @GetMapping
    public List<RevisionAsignada> listar() {
        return asignacion.listar();
    }
}
