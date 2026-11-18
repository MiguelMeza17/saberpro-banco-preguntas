package co.edu.unicauca.saberpro.lifecycle.revision;

import co.edu.unicauca.saberpro.lifecycle.messaging.SolicitudRevisionMessage;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AsignacionRevisoresTest {

    private static final Instant AHORA = Instant.parse("2026-11-18T15:00:00Z");

    private final AsignacionRevisores asignacion =
            new AsignacionRevisores(List.of("ana", "luis"), Clock.fixed(AHORA, ZoneOffset.UTC));

    private static SolicitudRevisionMessage solicitud(long id) {
        return new SolicitudRevisionMessage(id, "Enunciado " + id, "PENDIENTE_REVISION", AHORA.minusSeconds(5));
    }

    @Test
    void asignaRevisoresPorTurnos() {
        assertEquals("ana", asignacion.asignar(solicitud(1)).revisor());
        assertEquals("luis", asignacion.asignar(solicitud(2)).revisor());
        assertEquals("ana", asignacion.asignar(solicitud(3)).revisor());
    }

    @Test
    void registraLaRevisionEnRevision() {
        RevisionAsignada revision = asignacion.asignar(solicitud(7));

        assertEquals(7L, revision.preguntaId());
        assertEquals("Enunciado 7", revision.enunciado());
        assertEquals("EN_REVISION", revision.estado());
        assertEquals(AHORA, revision.recibidaEn());
        assertEquals(List.of(revision), asignacion.listar());
    }

    @Test
    void unaReentregaNoDuplicaNiCambiaElRevisor() {
        RevisionAsignada primera = asignacion.asignar(solicitud(1));

        RevisionAsignada repetida = asignacion.asignar(solicitud(1));

        assertEquals(primera, repetida);
        assertEquals(1, asignacion.listar().size());
        assertEquals("luis", asignacion.asignar(solicitud(2)).revisor());
    }

    @Test
    void exigeAlMenosUnRevisor() {
        assertThrows(IllegalArgumentException.class, () -> new AsignacionRevisores(List.of(), Clock.systemUTC()));
    }
}
