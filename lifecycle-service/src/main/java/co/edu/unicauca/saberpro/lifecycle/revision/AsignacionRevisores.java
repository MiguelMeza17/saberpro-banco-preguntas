package co.edu.unicauca.saberpro.lifecycle.revision;

import co.edu.unicauca.saberpro.lifecycle.messaging.SolicitudRevisionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Asigna revisores por turnos (round-robin) a las preguntas enviadas a revisión y guarda en memoria las
 * revisiones asignadas para consultarlas por REST. Si llega dos veces la misma pregunta (reentrega de
 * RabbitMQ), se conserva la primera asignación.
 */
@Service
public class AsignacionRevisores {

    static final String ESTADO_EN_REVISION = "EN_REVISION";

    private final List<String> revisores;
    private final Clock reloj;
    private final AtomicInteger turno = new AtomicInteger();
    private final Map<Long, RevisionAsignada> revisiones = new LinkedHashMap<>();

    @Autowired
    public AsignacionRevisores(@Value("${saberpro.revisores:revisor1,revisor2,revisor3}") List<String> revisores) {
        this(revisores, Clock.systemUTC());
    }

    AsignacionRevisores(List<String> revisores, Clock reloj) {
        if (revisores == null || revisores.isEmpty()) {
            throw new IllegalArgumentException("Debe configurarse al menos un revisor (saberpro.revisores).");
        }
        this.revisores = List.copyOf(revisores);
        this.reloj = reloj;
    }

    /**
     * Asigna un revisor a la pregunta de la solicitud.
     *
     * @param solicitud mensaje recibido de RabbitMQ
     * @return la revisión asignada (la existente si la pregunta ya se había recibido)
     */
    public synchronized RevisionAsignada asignar(SolicitudRevisionMessage solicitud) {
        return revisiones.computeIfAbsent(solicitud.preguntaId(), id -> new RevisionAsignada(
                id,
                solicitud.enunciado(),
                revisores.get(Math.floorMod(turno.getAndIncrement(), revisores.size())),
                ESTADO_EN_REVISION,
                solicitud.fechaEnvio(),
                Instant.now(reloj)));
    }

    /**
     * Revisiones asignadas, en orden de llegada.
     *
     * @return copia de solo lectura
     */
    public synchronized List<RevisionAsignada> listar() {
        return Collections.unmodifiableList(new ArrayList<>(revisiones.values()));
    }
}
