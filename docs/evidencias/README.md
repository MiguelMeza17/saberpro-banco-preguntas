# Evidencias de ejecución

Capturadas el 18 de noviembre de 2026 con `docker compose up -d --build` desde un stack limpio (`docker compose down -v`).
Cada `.txt` es la salida real del comando que aparece en su primera línea; el `.png` del mismo nombre es esa
salida en formato de terminal, lista para el informe.

| # | Evidencia | Archivos |
|---|---|---|
| 1 | `docker compose up -d --build` termina bien (build multi-stage de los 3 servicios y arranque en orden por healthcheck) | [txt](01-docker-compose-up.txt) · [png](01-docker-compose-up.png) |
| 2 | `docker ps`: los 5 contenedores arriba, RabbitMQ y PostgreSQL `healthy` | [txt](02-docker-ps.txt) · [png](02-docker-ps.png) |
| 3 | Respuestas HTTP: `GET` 200, `POST` 201 con `Location`, 400 por regla del dominio, 400 por validation-service, `enviar-revision` 202 y la revisión asignada en lifecycle-service | [txt](03-respuestas-http.txt) · [png](03-respuestas-http.png) |
| 4 | `docker logs saberpro-question-service`: adaptador activo y mensaje publicado | [txt](04-logs-question-service.txt) · [png](04-logs-question-service.png) |
| 5 | `docker logs saberpro-lifecycle-service`: mensaje recibido y revisor asignado | [txt](05-logs-lifecycle-service.txt) · [png](05-logs-lifecycle-service.png) |
| 6 | Cola `pregunta.revisar.queue` según la API de la consola de RabbitMQ: durable, 1 consumidor, 1 publicado, 1 entregado, 1 ack | [txt](06-rabbitmq-cola.txt) · [png](06-rabbitmq-cola.png) |
| 7 | Intercambiabilidad en vivo: el mismo contenedor pasa de `JpaQuestionAdapter` a `InMemoryQuestionAdapter` y vuelve, sin tocar código | [txt](07-intercambio-jpa-memoria.txt) · [png](07-intercambio-jpa-memoria.png) |
| 8 | Intercambiabilidad en pruebas: el mismo contrato pasa con los dos adaptadores | [txt](08-pruebas-intercambio.txt) · [png](08-pruebas-intercambio.png) |

## Pendientes (capturas manuales)

- **Dashboard de RabbitMQ** en <http://localhost:15672> (`guest` / `guest`) → *Queues* → `pregunta.revisar.queue`: guardar como `09-rabbitmq-dashboard.png`.
- **Thunder Client** (VS Code) con las respuestas 200, 201, 400 y 202: guardar como `10-thunder-client-<codigo>.png`.

## Cómo repetirlas

```bash
docker compose down -v && docker compose up -d --build
# intercambiar el adaptador de persistencia en caliente
SABERPRO_PERSISTENCIA=memoria docker compose up -d question-service
docker compose up -d question-service          # vuelve a jpa
```
