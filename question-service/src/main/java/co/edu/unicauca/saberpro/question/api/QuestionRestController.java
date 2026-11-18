package co.edu.unicauca.saberpro.question.api;

import co.edu.unicauca.saberpro.question.api.dto.CrearPreguntaRequest;
import co.edu.unicauca.saberpro.question.api.dto.EvaluacionRequest;
import co.edu.unicauca.saberpro.question.api.dto.PreguntaResponse;
import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.EvaluarPreguntaUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Adaptador de entrada REST de question-service. Traduce HTTP a llamadas de los puertos de entrada; nunca
 * usa repositorios ni sabe qué adaptadores de salida están activos.
 */
@RestController
@RequestMapping("/api/preguntas")
public class QuestionRestController {

    private final CrearPreguntaUseCase crearPregunta;
    private final EvaluarPreguntaUseCase evaluarPregunta;

    public QuestionRestController(CrearPreguntaUseCase crearPregunta, EvaluarPreguntaUseCase evaluarPregunta) {
        this.crearPregunta = crearPregunta;
        this.evaluarPregunta = evaluarPregunta;
    }

    /** Lista todas las preguntas → 200. */
    @GetMapping
    public List<PreguntaResponse> listar() {
        return crearPregunta.listar().stream().map(PreguntaResponse::de).toList();
    }

    /** Consulta una pregunta → 200, o 404 si no existe. */
    @GetMapping("/{id}")
    public PreguntaResponse obtener(@PathVariable Long id) {
        return PreguntaResponse.de(crearPregunta.obtener(id));
    }

    /** Crea una pregunta en BORRADOR → 201 con {@code Location}, o 400 si incumple una regla. */
    @PostMapping
    public ResponseEntity<PreguntaResponse> crear(@RequestBody CrearPreguntaRequest request) {
        Pregunta creada = crearPregunta.crear(request.aComando());
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(PreguntaResponse.de(creada));
    }

    /**
     * Envía la pregunta a revisión → 202 Accepted: la revisión la procesa lifecycle-service de forma
     * asíncrona a través de RabbitMQ.
     */
    @PostMapping("/{id}/enviar-revision")
    public ResponseEntity<PreguntaResponse> enviarARevision(@PathVariable Long id) {
        return ResponseEntity.accepted().body(PreguntaResponse.de(crearPregunta.enviarARevision(id)));
    }

    /** Aprueba una pregunta pendiente o en revisión → 200; las observaciones son opcionales. */
    @PostMapping("/{id}/aprobar")
    public PreguntaResponse aprobar(@PathVariable Long id, @RequestBody(required = false) EvaluacionRequest request) {
        return PreguntaResponse.de(evaluarPregunta.aprobar(id, request == null ? null : request.observaciones()));
    }

    /** Rechaza una pregunta pendiente o en revisión → 200, o 400 sin observaciones. */
    @PostMapping("/{id}/rechazar")
    public PreguntaResponse rechazar(@PathVariable Long id, @RequestBody(required = false) EvaluacionRequest request) {
        return PreguntaResponse.de(evaluarPregunta.rechazar(id, request == null ? null : request.observaciones()));
    }
}
