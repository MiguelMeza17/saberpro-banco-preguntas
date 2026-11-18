# Colección de Thunder Client

`thunder-collection_saberpro.json`: 14 requests en orden, cada uno con un test del código HTTP esperado.

## Importar en VS Code

1. Instalar la extensión **Thunder Client**.
2. Panel de Thunder Client → **Collections** → menú `≡` → **Import** → elegir `thunder-collection_saberpro.json`.
3. Levantar el sistema limpio: `docker compose down -v && docker compose up -d --build`.
4. Clic derecho en la colección → **Run All**. Los 14 tests deben pasar.

El orden importa: los ids `1` y `2` son las preguntas que crean los requests 02 y 11 sobre una base vacía.

| # | Request | Esperado |
|---|---|---|
| 01 | `GET /api/preguntas` | 200 |
| 02 | `POST /api/preguntas` válida | 201 |
| 03 | `POST` con justificación de menos de 15 caracteres | 400 |
| 04 | `POST` con dos claves (la rechaza validation-service) | 400 |
| 05 | `GET /api/preguntas/1` | 200 |
| 06 | `GET /api/preguntas/999` | 404 |
| 07 | `POST /api/preguntas/1/enviar-revision` | 202 |
| 08 | `GET :8084/api/revisiones` (mensaje recibido por lifecycle-service) | 200 |
| 09 | `POST /api/preguntas/1/rechazar` sin observaciones | 400 |
| 10 | `POST /api/preguntas/1/aprobar` | 200 |
| 11 | `POST /api/preguntas` segunda pregunta | 201 |
| 12 | `POST /api/preguntas/2/enviar-revision` | 202 |
| 13 | `POST /api/preguntas/2/rechazar` con observaciones | 200 |
| 14 | `POST :8082/api/validaciones` directo | 200 |

## Ejecutarla sin VS Code

```bash
python3 docs/thunder-client/ejecutar-coleccion.py
```

Lee el mismo JSON, ejecuta los requests en orden y compara el código HTTP de cada uno.
