# saberpro-banco-preguntas

Banco de Preguntas Saber Pro: entrega final (Taller 13) del Laboratorio de Ingeniería de Software II,
Universidad del Cauca, 2026.2. Integra DDD, arquitectura hexagonal y microservicios contenerizados con Docker Compose.

## Microservicios

| Servicio | Puerto | Responsabilidad |
|---|---|---|
| `validation-service` | 8082 | Validación estructural de preguntas (HU03) por REST síncrono |
| `question-service` | 8083 | Servicio principal: dominio puro, puertos y adaptadores (JPA / memoria / RabbitMQ) |
| `lifecycle-service` | 8084 | Ciclo de vida: consume los envíos a revisión desde RabbitMQ |
| `rabbitmq` | 5672 / 15672 | Broker AMQP y consola de administración |

Todos los contenedores se comunican por la red bridge `saberpro-net`.

## Estructura

```
saberpro-banco-preguntas/
├── docker-compose.yml
├── README.md
├── validation-service/        (:8082)  co.edu.unicauca.saberpro.validation
│   └── api/
├── question-service/          (:8083)  co.edu.unicauca.saberpro.question
│   ├── domain/                dominio puro (sin @Entity ni Spring)
│   │   ├── model/
│   │   ├── exception/
│   │   └── ports/{inbound,outbound}/
│   ├── application/           servicios de aplicación
│   ├── infrastructure/{jpa,memory,messaging}/
│   └── api/                   controlador REST
└── lifecycle-service/         (:8084)  co.edu.unicauca.saberpro.lifecycle
    └── messaging/
```

## Ejecutar en local (sin Docker)

Cada servicio es un proyecto Maven independiente con su wrapper:

```bash
cd question-service
./mvnw test                         # pruebas
./mvnw clean package -DskipTests    # genera target/question-service.jar
./mvnw spring-boot:run
```

## Ejecutar con Docker

Cada servicio tiene un `Dockerfile` multi-stage en su raíz: la primera etapa compila el `.jar` con Maven
(equivale a `./mvnw clean package -DskipTests`) y la segunda lo ejecuta sobre `eclipse-temurin:17-jre`.
No hace falta compilar antes a mano. Se usa `eclipse-temurin` porque `openjdk:17-jdk-slim`, la imagen
de la guía, está descontinuada en Docker Hub.

```bash
docker compose up -d --build
docker ps
docker logs -f saberpro-question-service
```

`docker compose up -d --build` levanta 5 contenedores en la red `saberpro-net`:

| Contenedor | Imagen | Puertos | Arranca después de |
|---|---|---|---|
| `saberpro-rabbitmq` | `rabbitmq:3-management` | 5672, 15672 | — |
| `saberpro-postgres` | `postgres:16-alpine` | 5432 | — |
| `saberpro-validation-service` | build `./validation-service` | 8082 | — |
| `saberpro-question-service` | build `./question-service` | 8083 | RabbitMQ y PostgreSQL sanos, validation-service |
| `saberpro-lifecycle-service` | build `./lifecycle-service` | 8084 | RabbitMQ sano |

Variables que inyecta el compose (en local se usan los valores por defecto de `application.properties`):

| Servicio | Variable | Valor en Docker | Valor local |
|---|---|---|---|
| question-service | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres:5432/saberprodb` | H2 en memoria |
| question-service | `SABERPRO_VALIDATION_URL` | `http://validation-service:8082` | `http://localhost:8082` |
| question-service, lifecycle-service | `SPRING_RABBITMQ_HOST` | `rabbitmq` | `localhost` |

- Consola de RabbitMQ: <http://localhost:15672> (`guest` / `guest`), cola `pregunta.revisar.queue`.
- Revisiones recibidas por lifecycle-service: `GET http://localhost:8084/api/revisiones`.
- Detener todo: `docker compose down` (añadir `-v` para borrar también los datos de PostgreSQL).
