**SISTEMA DE RESERVA DE**

**TUTORÍAS DE TESIS**

_Coordinación de reuniones alumno-profesor_

Especificación de Arquitectura, Requisitos y Flujo de Negocio

Arquitectura de Microservicios — Spring Boot \+ Vue.js

Proyecto Universitario — Integrantes: Víctor Casco / Rodrigo Ibarra

Versión 2.0

# **1\. Introducción**

El sistema permite a los alumnos reservar un horario para reunirse con su profesor tutor y presentar los avances de su tesis. Cada profesor habilita franjas horarias de atención, y los alumnos reservan el turno que les convenga. Una vez que un horario es tomado, queda bloqueado para el resto: no pueden existir dos reservas sobre la misma fecha y hora con el mismo profesor.

# **2\. Arquitectura General**

El sistema mantiene la arquitectura de microservicios definida para el proyecto:

Frontend (Vue.js)  
 |  
 v  
 API Gateway (autenticación / autorización)  
 |  
 v  
 \+--------------------------------------------------+  
 | Microservicios |  
 | ms-usuarios | ms-disponibilidad | ms-reservas | |  
 | ms-notificaciones |  
 \+--------------------------------------------------+  
 |  
 v  
 Caché (Redis)  
 |  
 v  
 Base de Datos Compartida

El Gateway valida el token de sesión y el rol del usuario antes de enrutar la solicitud. Cada microservicio consulta primero la caché (por ejemplo, disponibilidad horaria de un profesor consultada con frecuencia) y, si no encuentra el dato, recurre a la base de datos compartida.

# **3\. Roles de Usuario**

| Rol                                    | Descripción                                                                                                                                                                     |
| :------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Alumno                                 | Busca disponibilidad, reserva un horario con su profesor y gestiona sus propias reservas (ver, cancelar).                                                                       |
| Profesor                               | Define y administra sus franjas horarias de atención y las áreas de tesis que domina; visualiza sus reuniones agendadas.                                                        |
| Administrador / Coordinador de Carrera | Rol independiente. Registra usuarios (alumnos y profesores), mantiene los catálogos (áreas de tesis, tipos de consulta), configura módulos del sistema y accede a los reportes. |

# **4\. Módulos Cabecera-Detalle y División de Tareas**

El proyecto contempla dos formularios cabecera-detalle, uno por integrante. Ambos son relaciones muchos a muchos (M:N): la cabecera se vincula con un catálogo reutilizable a través de una tabla intermedia, igual que una factura se vincula con un catálogo de productos.

| Integrante | Microservicios                 | Cabecera-Detalle (M:N)                                                                                                                                                                                       |
| :--------- | :----------------------------- | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Víctor     | ms-usuarios, ms-disponibilidad | Profesor (header) ↔ Lineas de investigacion (catálogo), vinculados por Profesor_Linea (detail). Un profesor puede dominar varias Lineas de investigacion; la misma puede ser tutorada por varios profesores. |
| Rodrigo    | ms-reservas, ms-notificaciones | Reserva (header) ↔ Tipo de Consulta (catálogo), vinculados por Detalle_Reserva (detail). Una reserva puede incluir varios tipos de consulta; un tipo de consulta puede aparecer en muchas reservas.          |

## **4.1 Módulo de Víctor: Profesor ↔ Área de Tesis**

Tres tablas trabajan juntas:

| Tabla                                  | Campo             | Descripción                                        |
| :------------------------------------- | :---------------- | :------------------------------------------------- |
| PROFESOR (header)                      | id                | Identificador único                                |
| PROFESOR (header)                      | nombre / email    | Datos del profesor                                 |
| LINEA_INVESTIGACION(catálogo)          | id                | Identificador único                                |
| LINEA_INVESTIGACION (catálogo)         | nombre            | Ej: Inteligencia Artificial, Redes, Bases de Datos |
| PROFESOR_LINEA (detail / tabla puente) | id                | Identificador único                                |
| PROFESOR_LINEA (detail / tabla puente) | id_profesor (FK)  | A qué profesor pertenece la fila                   |
| PROFESOR_LINEA (detail / tabla puente) | id_area (FK)      | Qué área cubre                                     |
| PROFESOR_LINEA (detail / tabla puente) | nivel_experiencia | Básico / medio / experto                           |

PROFESOR y LINEA_INVESTIGACION nunca se tocan directamente entre sí: todo pasa por PROFESOR_AREA, que por cada fila indica "este profesor domina esta área". Así, un mismo profesor puede tener varias filas (varias áreas) y una misma área puede repetirse en varias filas (varios profesores).

En pantalla: al editar el perfil de un profesor (cabecera: nombre, email), se agregan filas dinámicamente eligiendo áreas del catálogo — igual que se agregan ítems a una factura.

## **4.2 Módulo de Rodrigo: Reserva ↔ Tipo de Consulta**

Misma lógica, aplicada a la reserva:

| Tabla                                   | Campo                                  | Descripción                                                             |
| :-------------------------------------- | :------------------------------------- | :---------------------------------------------------------------------- |
| RESERVA (header)                        | id                                     | Identificador único                                                     |
| RESERVA (header)                        | id_alumno / id_franja / fecha / estado | Datos de la reserva                                                     |
| TIPO_CONSULTA (catálogo)                | id                                     | Identificador único                                                     |
| TIPO_CONSULTA (catálogo)                | nombre                                 | Ej: Revisión de metodología, Corrección de capítulo, Defensa preliminar |
| DETALLE_RESERVA (detail / tabla puente) | id                                     | Identificador único                                                     |
| DETALLE_RESERVA (detail / tabla puente) | id_reserva (FK)                        | A qué reserva pertenece la fila                                         |
| DETALLE_RESERVA (detail / tabla puente) | id_tipo_consulta (FK)                  | Qué tipo de consulta es                                                 |
| DETALLE_RESERVA (detail / tabla puente) | comentario                             | Detalle libre opcional                                                  |

En pantalla: el alumno completa su reserva (cabecera: profesor, fecha, hora) y luego marca uno o más motivos de consulta del catálogo (detail) — cada motivo elegido genera una fila nueva en DETALLE_RESERVA.

# **5\. Flujo de Negocio**

Recorrido completo del proceso de reserva de una tutoría:

1. El administrador registra a los alumnos y profesores en el sistema (formulario de registro de usuario, exclusivo del admin).

2. El profesor inicia sesión, habilita sus franjas horarias disponibles y selecciona las áreas de tesis que domina, del catálogo compartido.

3. El alumno inicia sesión y busca un profesor que trabaje su área de tesis, y consulta los horarios disponibles (con búsqueda y paginación si hay muchos resultados).

4. El alumno selecciona una franja horaria libre y completa el formulario de reserva, marcando uno o más tipos de consulta del catálogo (detalle de la reserva).

5. Al confirmar, el sistema valida en tiempo real que el horario siga disponible: si otro alumno ya lo reservó primero, la reserva se rechaza y se le informa al alumno que elija otro horario.

6. Si el horario sigue libre, se registra la reserva (cabecera) junto con su detalle, y la franja horaria queda marcada como ocupada para todos los demás alumnos.

7. Se envía un mail de confirmación al alumno y al profesor con la fecha, hora y los tipos de consulta elegidos.

8. El alumno puede cancelar su reserva antes de la fecha; al hacerlo, la franja horaria se libera automáticamente y vuelve a estar disponible.

9. Un proceso automático (cron) revisa varias veces al día las reuniones próximas y envía recordatorios por mail, además de marcar como "completadas" las reuniones cuya fecha ya pasó.

## **5.1 Regla de no solapamiento de reservas**

La franja horaria de un profesor solo puede tener una reserva activa a la vez. Cuando un alumno intenta confirmar una reserva, el microservicio ms-reservas verifica el estado de la franja en ms-disponibilidad antes de registrar la reserva: si ya está OCUPADA, la operación se rechaza; si está DISPONIBLE, se marca como OCUPADA y recién ahí se guarda la reserva. Esta verificación evita que dos alumnos reserven el mismo horario al mismo tiempo, aplicando el principio de "el primero que confirma, se queda con el turno".

# **6\. Cumplimiento de Requisitos Mínimos**

A continuación se detalla cómo el proyecto cubre cada uno de los requisitos mínimos solicitados:

| Requisito                                            | Cómo se cumple en este proyecto                                                                                                                                                       |
| :--------------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Autenticación                                        | Login con JWT para alumnos, profesores y administrador, gestionado por ms-usuarios.                                                                                                   |
| Autorización                                         | Control de acceso por rol (Alumno / Profesor / Admin) en cada endpoint, validado en el Gateway.                                                                                       |
| Formulario cabecera-detalle (M:N)                    | Víctor: Profesor ↔ Área de Tesis vía Profesor_Area. Rodrigo: Reserva ↔ Tipo de Consulta vía Detalle_Reserva.                                                                          |
| Envío de mails                                       | ms-notificaciones envía confirmaciones y recordatorios; las plantillas se configuran desde la UI de administración.                                                                   |
| Reportes (mínimo 1 por integrante)                   | Víctor: reporte de profesores por área de tesis / ocupación. Rodrigo: reporte de reservas por período, tipo de consulta y alumno.                                                     |
| 2 roles de usuario, admin independiente              | Alumno y Profesor como roles operativos; Administrador como rol independiente para gestión y configuración.                                                                           |
| Paginación (incluida búsqueda)                       | Listados de reservas, profesores y usuarios paginados, con filtros de búsqueda por nombre, área, fecha o estado.                                                                      |
| Búsquedas                                            | Búsqueda de profesores por área de tesis y horario disponible; búsqueda de reservas propias por fecha o estado.                                                                       |
| Formulario de registro de usuario (lo hace el admin) | Pantalla exclusiva del Administrador para dar de alta alumnos y profesores.                                                                                                           |
| Formulario de contacto                               | Formulario público que envía un mail a soporte de la plataforma ante consultas o problemas.                                                                                           |
| UI amigable con framework CSS                        | Frontend en Vue 3 con un framework de estilos (ej. Bootstrap, Tailwind o PrimeVue).                                                                                                   |
| Varios tipos de transacciones (backend)              | ms-reservas maneja distintos tipos de operación: creación, confirmación, cancelación y liberación por expiración de una reserva.                                                      |
| Cron (tareas varias veces al día)                    | Job programado que envía recordatorios de reuniones próximas y libera franjas no confirmadas a tiempo.                                                                                |
| Configuraciones                                      | Panel de administración donde se configura la duración estándar de la reunión, anticipación mínima para reservar, catálogos (áreas de tesis, tipos de consulta) y plantillas de mail. |

Parte 1:
Cada estudiante debe desarrollar un microservicio independiente en Java con Spring Boot, aplicando principios de arquitectura REST y programación orientada a objetos (POO) para garantizar que el código sea reutilizable, fácilmente extensible y mantenible.
El conjunto de microservicios desarrollados por el grupo conformará un sistema integrado, donde cada microservicio cubrirá una parte específica de los requerimientos globales del sistema.1. Trabajo en Grupo y Estructura de Datos
Distribución: El grupo debe definir qué módulo del sistema cubrirá cada microservicio.
Desarrollo: El desarrollo es individual, pero todos los microservicios deben integrarse con la base de datos remota compartida (definida en la Entrega #1).
Estructura mínima de datos por microservicio:
Al menos una entidad simple (sin relaciones complejas).
Al menos una entidad con relación muchos a muchos (Many-to-Many), gestionando correctamente sus asociaciones.
Entidades con dependencias externas: Si un microservicio requiere interactuar o depende de otras entidades del sistema, puede utilizar las clases y estructuras definidas en la Entrega #1. Esto garantiza que cada estudiante trabaje de forma independiente en torno a la capa de datos.2. Arquitectura y Capas de Programación
El proyecto debe organizarse bajo el patrón estándar de Spring (controller, service, repository, model/dto), aplicando Inversión de Control (IoC) y las anotaciones correspondientes (@RestController, @Service, @Repository, etc.). Además, se deben cumplir estrictamente las siguientes directrices arquitectónicas:
Respeto estricto de capas (Separación de responsabilidades): Los niveles deben comunicarse de forma jerárquica y unidireccional (Controlador -> Servicio -> Repositorio). Está prohibido realizar saltos entre capas (por ejemplo: invocar repositorios directamente desde el controlador, o llamar a controladores desde los servicios, etc).
Especialización por entidad: Cada clase componente debe especializarse y trabajar exclusivamente en un único tipo de entidad. (Nota: Es posible que un microservicio contenga servicios o repositorios de apoyo que no necesiten implementar un controlador expuesto, de acuerdo con la lógica requerida, conversar sobre esto si se presenta el caso).
Jerarquía de clases: Definir una jerarquía de clases adecuada que garantice la reutilización de código transversal y/o la compartición de lógica común entre ellas.
Entities y DTOs: Utilizar las clases de los archivos JAR provistos sin modificar su definición (reutilizando los de la Entrega #1).
Mapeo: Implementar la conversión bidireccional entre DTO y Entity.
Repositorios: Gestionar la persistencia mediante Spring Data JPA.
Servicios: Contener la lógica de negocio de la aplicación.
Controladores: Implementar los endpoints definidos en las interfaces y delegar las peticiones en la capa de servicios. 3. Operaciones REST Obligatorias
Cada microservicio debe exponer sus recursos siguiendo las convenciones REST (ej. /entidades, /entidades/{id}), utilizando los verbos y códigos de estado HTTP correctos (201, 200, 400, 404, 500, etc.). Las operaciones mínimas son:
Crear un registro (POST).
Actualizar un registro (PUT).
Obtener un registro por ID (GET /{id}).
Obtener múltiples registros (GET) con paginación obligatoria.
Búsqueda mediante filtros, también con paginación obligatoria.
Borrado lógico (DELETE), marcando el estado del registro como inactivo. 4. Requisitos Técnicos y Transversales
Configuración: Maven debe configurarse para resolver correctamente las dependencias de los JARs provistos. Toda la aplicación debe ser altamente configurable (mediante application.properties o application.yml).
Dependencias de terceros: Se pueden utilizar bibliotecas o dependencias externas adicionales que faciliten el desarrollo, siempre y cuando se consulte y acuerde previamente su uso dentro del grupo de la clase.
Tratamiento de Fallos: Implementar un manejo centralizado de excepciones y tratamiento de fallos (ocultamiento de errores técnicos y respuestas estandarizadas).
Sistema de Logs: Configurar SLF4J / Logback para registrar eventos en disco utilizando los niveles adecuados (INFO, DEBUG, ERROR):
Controller: Solicitudes y llamadas entrantes.
Service: Pasos clave de la lógica de negocio.
Repository: Consultas importantes en nivel DEBUG.
Documentación y Pruebas: Documentar todos los endpoints con Swagger / OpenAPI (se puede extender el trabajo de la Entrega #1).Base de Datos Remota: Conexión obligatoria a la base de datos compartida, garantizando la consistencia en operaciones sobre entidades comunes.NOTA: TODO DEBE SER CONFIGURABLE
