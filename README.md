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

## API de question-service (:8083)

| Método y ruta | Respuesta |
|---|---|
| `GET /api/preguntas` | 200 con la lista |
| `GET /api/preguntas/{id}` | 200, o 404 si no existe |
| `POST /api/preguntas` | 201 con la pregunta y `Location`; 400 si incumple una regla o la validación de `validation-service`; 503 si `validation-service` no responde |
| `POST /api/preguntas/{id}/enviar-revision` | 202 Accepted (la revisión sigue por RabbitMQ) |
| `POST /api/preguntas/{id}/aprobar` | 200; body opcional `{"observaciones": "..."}` |
| `POST /api/preguntas/{id}/rechazar` | 200; 400 si falta `{"observaciones": "..."}` |

La URL de `validation-service` se configura con `SABERPRO_VALIDATION_URL` (por defecto `http://localhost:8082`;
en Docker, `http://validation-service:8082`).

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
│   ├── infrastructure/{jpa,memory,messaging,validation,config}/
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
| question-service | `SABERPRO_PERSISTENCIA` | `jpa` (PostgreSQL) | `jpa` (H2); `memoria` usa el `InMemoryQuestionAdapter` |
| question-service, lifecycle-service | `SPRING_RABBITMQ_HOST` | `rabbitmq` | `localhost` |

- Consola de RabbitMQ: <http://localhost:15672> (`guest` / `guest`), cola `pregunta.revisar.queue`.
- Revisiones recibidas por lifecycle-service: `GET http://localhost:8084/api/revisiones`.
- Detener todo: `docker compose down` (añadir `-v` para borrar también los datos de PostgreSQL).

## Diagramas

Fuentes Mermaid en `docs/diagramas/*.mmd` y exportados a PNG (mismo nombre) para el informe.
Para regenerar los PNG: `npx -p @mermaid-js/mermaid-cli mmdc -i docs/diagramas/<nombre>.mmd -o docs/diagramas/<nombre>.png -b white`.

### C4 nivel 1: Contexto

Fuente: [`c4-contexto.mmd`](docs/diagramas/c4-contexto.mmd) · PNG: [`c4-contexto.png`](docs/diagramas/c4-contexto.png)

```mermaid
flowchart TB
    autor["<b>Autor de preguntas</b><br/>[Persona]<br/>Crea preguntas y las envía a revisión"]
    revisor["<b>Revisor</b><br/>[Persona]<br/>Aprueba o rechaza con observaciones"]
    admin["<b>Administrador</b><br/>[Persona]<br/>Supervisa el banco y las revisiones"]
    sistema["<b>Sistema Banco de Preguntas Saber Pro</b><br/>[Sistema de software]<br/>Autoría, validación estructural (HU03)<br/>y ciclo de vida de las preguntas"]
    autor -- "Crea y envía a revisión<br/>[REST / JSON]" --> sistema
    revisor -- "Evalúa preguntas<br/>[REST / JSON]" --> sistema
    admin -- "Consulta preguntas y revisiones asignadas<br/>[REST / JSON]" --> sistema
    style sistema fill:#0b3a75,color:#ffffff,stroke:#0b3a75
    style autor fill:#08427b,color:#ffffff,stroke:#052e56
    style revisor fill:#08427b,color:#ffffff,stroke:#052e56
    style admin fill:#08427b,color:#ffffff,stroke:#052e56
```

### C4 nivel 2: Contenedores

Fuente: [`c4-contenedores.mmd`](docs/diagramas/c4-contenedores.mmd) · PNG: [`c4-contenedores.png`](docs/diagramas/c4-contenedores.png)

```mermaid
flowchart TB
    cliente["<b>Cliente HTTP</b><br/>[Thunder Client / navegador]<br/>Autor, revisor, administrador"]
    subgraph red["Red Docker bridge: saberpro-net"]
        vs["<b>validation-service</b><br/>[Spring Boot] :8082<br/>Validación estructural HU03<br/>(Pipes & Filters)"]
        qs["<b>question-service</b><br/>[Spring Boot] :8083<br/>Dominio DDD y arquitectura hexagonal"]
        ls["<b>lifecycle-service</b><br/>[Spring Boot] :8084<br/>Consume envíos a revisión<br/>y asigna revisores"]
        mq[("<b>rabbitmq</b><br/>[RabbitMQ 3 management]<br/>:5672 AMQP / :15672 consola<br/>pregunta.revisar.queue")]
        db[("<b>postgres</b><br/>[PostgreSQL 16]<br/>:5432 saberprodb")]
    end
    cliente -- "/api/preguntas<br/>[REST / JSON]" --> qs
    cliente -- "/api/revisiones<br/>[REST / JSON]" --> ls
    qs -- "POST /api/validaciones<br/>[REST síncrono]" --> vs
    qs -- "Lee y escribe preguntas<br/>[JDBC / JPA]" --> db
    qs -- "Publica SolicitudRevision<br/>[AMQP, pregunta.exchange]" --> mq
    mq -- "Entrega mensajes<br/>[@RabbitListener]" --> ls
    style qs fill:#1168bd,color:#ffffff,stroke:#0b4884
    style vs fill:#1168bd,color:#ffffff,stroke:#0b4884
    style ls fill:#1168bd,color:#ffffff,stroke:#0b4884
```

### C4 nivel 3: Componentes de question-service

Fuente: [`c4-componentes.mmd`](docs/diagramas/c4-componentes.mmd) · PNG: [`c4-componentes.png`](docs/diagramas/c4-componentes.png)

```mermaid
flowchart TB
    cliente["Cliente HTTP"]
    vs["validation-service :8082"]
    mq[("rabbitmq")]
    db[("postgres / H2")]
    subgraph qs["question-service :8083"]
        subgraph api["api (adaptador de entrada)"]
            ctrl["<b>QuestionRestController</b><br/>DTOs + ApiExceptionHandler"]
        end
        subgraph dominio["domain (Java puro)"]
            in["<b>Puertos de entrada</b><br/>CrearPreguntaUseCase<br/>EvaluarPreguntaUseCase"]
            agg["<b>Agregado Pregunta</b><br/>Enunciado, Justificacion,<br/>OpcionRespuesta, EstadoPregunta"]
            out["<b>Puertos de salida</b><br/>PreguntaRepositoryPort<br/>ValidacionPort<br/>NotificacionPort"]
        end
        subgraph app["application"]
            srv["<b>QuestionApplicationService</b><br/>Orquesta los casos de uso"]
        end
        subgraph infra["infrastructure (adaptadores de salida)"]
            jpa["<b>JpaQuestionAdapter</b><br/>QuestionRecord + QuestionDomainMapper"]
            mem["<b>InMemoryQuestionAdapter</b><br/>HashMap"]
            val["<b>ValidationServiceClient</b><br/>RestClient"]
            pub["<b>RabbitMQPublisher</b><br/>Spring AMQP"]
            cfg["<b>Configuración @Bean</b><br/>Elige JPA o memoria"]
        end
    end
    cliente --> ctrl --> in
    srv -. "implementa" .-> in
    srv --> agg
    srv --> out
    jpa -. "implementa" .-> out
    mem -. "implementa" .-> out
    val -. "implementa" .-> out
    pub -. "implementa" .-> out
    cfg -. "registra" .-> srv
    jpa --> db
    val --> vs
    pub --> mq
    style dominio fill:#fff4d6,stroke:#c99a1a
    style app fill:#e6f0ff,stroke:#1168bd
```

### C4 nivel 4: Código (agregado Pregunta y puertos)

Fuente: [`c4-codigo.mmd`](docs/diagramas/c4-codigo.mmd) · PNG: [`c4-codigo.png`](docs/diagramas/c4-codigo.png)

```mermaid
classDiagram
    direction LR
    class Pregunta {
        <<Aggregate Root>>
        -Long id
        -Enunciado enunciado
        -Justificacion justificacion
        -List~OpcionRespuesta~ opciones
        -EstadoPregunta estado
        -String observaciones
        +reconstituir(...)$ Pregunta
        +agregarOpcion(texto, esCorrecta)
        +enviarARevision()
        +aprobar(observaciones)
        +rechazar(observaciones)
        +publicar()
        +archivar()
    }
    class Enunciado {
        <<Value Object>>
        -String texto
    }
    class Justificacion {
        <<Value Object>>
        +LONGITUD_MINIMA = 15
        -String texto
    }
    class OpcionRespuesta {
        <<Value Object>>
        -String texto
        -boolean esCorrecta
    }
    class EstadoPregunta {
        <<enumeration>>
        BORRADOR
        PENDIENTE_REVISION
        EN_REVISION
        APROBADA
        RECHAZADA
        PUBLICADA
        ARCHIVADA
        +puedeTransitarA(destino) boolean
    }
    class CrearPreguntaUseCase {
        <<interface>>
        +crear(CrearPreguntaComando) Pregunta
        +enviarARevision(id) Pregunta
        +listar() List~Pregunta~
        +obtener(id) Pregunta
    }
    class EvaluarPreguntaUseCase {
        <<interface>>
        +aprobar(id, observaciones) Pregunta
        +rechazar(id, observaciones) Pregunta
    }
    class PreguntaRepositoryPort {
        <<interface>>
        +save(Pregunta) Pregunta
        +findById(id) Optional~Pregunta~
        +findAll() List~Pregunta~
    }
    class ValidacionPort {
        <<interface>>
        +validar(Pregunta) List~String~
    }
    class NotificacionPort {
        <<interface>>
        +notificarEnvioARevision(Pregunta)
    }
    class QuestionApplicationService
    Pregunta *-- "1" Enunciado
    Pregunta *-- "1" Justificacion
    Pregunta *-- "0..4" OpcionRespuesta
    Pregunta --> EstadoPregunta
    QuestionApplicationService ..|> CrearPreguntaUseCase
    QuestionApplicationService ..|> EvaluarPreguntaUseCase
    QuestionApplicationService --> PreguntaRepositoryPort
    QuestionApplicationService --> ValidacionPort
    QuestionApplicationService --> NotificacionPort
    QuestionApplicationService ..> Pregunta
```

### Arquitectura hexagonal de question-service

Fuente: [`hexagonal.mmd`](docs/diagramas/hexagonal.mmd) · PNG: [`hexagonal.png`](docs/diagramas/hexagonal.png)

```mermaid
flowchart LR
    subgraph entrada["Adaptadores de entrada (driving)"]
        rest["QuestionRestController<br/>REST /api/preguntas"]
    end
    subgraph hexagono["Hexágono de question-service"]
        direction TB
        pin["Puertos de entrada<br/>CrearPreguntaUseCase<br/>EvaluarPreguntaUseCase"]
        srv["QuestionApplicationService"]
        dom["Dominio puro<br/>Pregunta + Value Objects<br/>sin Spring ni JPA"]
        pout["Puertos de salida<br/>PreguntaRepositoryPort<br/>ValidacionPort<br/>NotificacionPort"]
        pin --> srv --> dom
        srv --> pout
    end
    subgraph salida["Adaptadores de salida (driven)"]
        jpa["JpaQuestionAdapter<br/>PostgreSQL / H2"]
        mem["InMemoryQuestionAdapter<br/>HashMap"]
        val["ValidationServiceClient<br/>validation-service :8082"]
        pub["RabbitMQPublisher<br/>pregunta.revisar.queue"]
    end
    rest --> pin
    pout -. "implementado por" .-> jpa
    pout -. "implementado por" .-> mem
    pout -. "implementado por" .-> val
    pout -. "implementado por" .-> pub
    style hexagono fill:#fff4d6,stroke:#c99a1a
    style dom fill:#ffe9a8,stroke:#c99a1a
```

### Secuencia: crear → enviar a revisión → RabbitMQ → lifecycle-service

Fuente: [`secuencia.mmd`](docs/diagramas/secuencia.mmd) · PNG: [`secuencia.png`](docs/diagramas/secuencia.png)

```mermaid
sequenceDiagram
    actor Autor
    participant C as QuestionRestController
    participant S as QuestionApplicationService
    participant P as Pregunta (agregado)
    participant V as validation-service :8082
    participant R as PreguntaRepositoryPort
    participant N as RabbitMQPublisher
    participant MQ as RabbitMQ<br/>pregunta.revisar.queue
    participant L as lifecycle-service :8084
    Autor->>C: POST /api/preguntas
    C->>S: crear(comando)
    S->>P: new Pregunta + agregarOpcion x4 (invariantes)
    S->>V: POST /api/validaciones
    V-->>S: {valida: true, errores: []}
    S->>R: save(pregunta)
    R-->>S: pregunta con id (BORRADOR)
    C-->>Autor: 201 Created + Location
    Autor->>C: POST /api/preguntas/{id}/enviar-revision
    C->>S: enviarARevision(id)
    S->>R: findById(id)
    S->>P: enviarARevision() (4 opciones, 1 clave)
    S->>R: save(pregunta PENDIENTE_REVISION)
    S->>N: notificarEnvioARevision(pregunta)
    N-)MQ: SolicitudRevisionMessage (JSON)
    C-->>Autor: 202 Accepted
    MQ-)L: QuestionReviewListener.recibir(mensaje)
    L->>L: AsignacionRevisores.asignar() (round-robin)
    Note over L: docker logs -f saberpro-lifecycle-service<br/>muestra el mensaje recibido
```
