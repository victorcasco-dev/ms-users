# ms-users

Usuarios, profesores, catálogo de líneas de investigación y relación M:N profesor ↔ línea.

## Parte #1 — cumplimiento

| Requisito | Implementación |
|-----------|----------------|
| Entidades simples | `User` (y subtipos), `ResearchLine` |
| M:N | `ProfessorResearchLine` — `/professor-research-lines` y anidado en `/professors/{id}/research-lines` |
| Capas | `controller` → `service` → `repository` |
| REST completo | POST, PUT, GET /{id}, GET paginado, POST /search, DELETE lógico (`active=false` en user/line) |
| Detalle M:N DELETE | Desvincula fila puente (sin columna `active` en Entrega #1) |
| Commons JAR, MapStruct, Swagger, Logback, excepciones | Sí |

Puerto por defecto: **8081**. Variables: ver `.env.example`.
