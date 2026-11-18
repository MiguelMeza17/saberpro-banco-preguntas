package co.edu.unicauca.saberpro.question.infrastructure.memory;

import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Adaptador de salida en memoria ({@link java.util.HashMap} concurrente). Implementa el mismo puerto que
 * {@code JpaQuestionAdapter}, así que los casos de uso funcionan igual con cualquiera de los dos.
 * Guarda y devuelve copias del agregado, como haría una base de datos: un cambio sobre una pregunta leída no
 * se ve hasta llamar a {@link #save}. Los datos se pierden al reiniciar.
 */
public class InMemoryQuestionAdapter implements PreguntaRepositoryPort {

    private final Map<Long, Pregunta> preguntas = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();

    @Override
    public Pregunta save(Pregunta pregunta) {
        if (pregunta.getId() == null) {
            pregunta.asignarId(secuencia.incrementAndGet());
        }
        preguntas.put(pregunta.getId(), copiar(pregunta));
        return copiar(pregunta);
    }

    @Override
    public Optional<Pregunta> findById(Long id) {
        return Optional.ofNullable(preguntas.get(id)).map(InMemoryQuestionAdapter::copiar);
    }

    @Override
    public List<Pregunta> findAll() {
        List<Pregunta> todas = new ArrayList<>(preguntas.values().stream().map(InMemoryQuestionAdapter::copiar).toList());
        todas.sort((a, b) -> Long.compare(a.getId(), b.getId()));
        return todas;
    }

    private static Pregunta copiar(Pregunta p) {
        return Pregunta.reconstituir(p.getId(), p.getEnunciado(), p.getJustificacion(), p.getOpciones(),
                p.getEstado(), p.getObservaciones());
    }
}
