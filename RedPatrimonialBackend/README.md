# Red Patrimonial · Backend (microservicios Spring Boot + Kotlin)

Backend del MVP para la Red Patrimonial del Arzobispado de Santiago (DSY1105).
Son **5 microservicios independientes**. Cada uno tiene su propio puerto y **su propia base de datos local H2** en archivo, así que no hay que instalar MySQL ni Oracle.

| Microservicio | Puerto | Responsabilidad | Casos de uso |
|---|---|---|---|
| `ms-usuarios` | 8081 | Registro, login (BCrypt), roles, permisos por nodo | CU20 |
| `ms-nodos` | 8082 | Fichas patrimoniales, estado en tiempo real, horarios excepcionales, galería, indicadores | CU01–CU04, CU11, CU13, CU21 |
| `ms-eventos` | 8083 | Oferta cultural y litúrgica, inscripciones con código QR | CU05, CU12 |
| `ms-rutas` | 8084 | Rutas (con cálculo de distancia y tiempo), salidas pedagógicas, guías y disponibilidad | CU06–CU10, CU15–CU18 |
| `ms-avisos` | 8085 | Avisos comunitarios en tiempo real y reportes | CU14 |

## Requisitos
- JDK 17 o superior
- IntelliJ IDEA (recomendado) o la terminal. Gradle se descarga solo con el wrapper.

## Cómo ejecutarlo

**Con IntelliJ:** abre la carpeta `RedPatrimonialBackend`, espera que Gradle sincronice y ejecuta (botón ▶) cada una de estas clases:
`MsUsuariosApplication`, `MsNodosApplication`, `MsEventosApplication`, `MsRutasApplication` y `MsAvisosApplication`.

**Con la terminal:** abre una terminal por microservicio:
```bash
./gradlew :ms-usuarios:bootRun
./gradlew :ms-nodos:bootRun
./gradlew :ms-eventos:bootRun
./gradlew :ms-rutas:bootRun
./gradlew :ms-avisos:bootRun
```
En Windows usa `gradlew.bat` en vez de `./gradlew`.

La primera vez, cada servicio crea su base en `./data/` y carga **datos sintéticos** (ficticios), tal como pide el anexo: 12 nodos (incluye templos de Maipú, Quinta Normal, Las Condes, Puente Alto y La Florida), 7 eventos, 3 rutas, 3 guías, objetivos pedagógicos, 2 salidas y 4 avisos.
Para volver a los datos iniciales, detén los servicios y borra la carpeta `data/`.

## Probar la API
- **Swagger (documentación interactiva):** `http://localhost:808X/swagger-ui.html`. Por ejemplo, http://localhost:8082/swagger-ui.html
- **Consola de la base de datos:** `http://localhost:808X/h2-console` (JDBC URL `jdbc:h2:file:./data/ms-nodos`, usuario `sa`, sin clave)

### Cuentas de prueba (clave `demo123`)
| Correo | Rol |
|---|---|
| turista@demo.cl | TURISTA |
| profesor@demo.cl | PROFESOR |
| guia@demo.cl | GUIA |
| encargado@demo.cl | ENCARGADO (administra el nodo 1, la Catedral) |
| admin@demo.cl | ADMIN |

## Endpoints principales
```
POST /api/auth/login                       POST /api/auth/registro
GET  /api/nodos?q=&tipo=&comuna=&soloAbiertos=
GET  /api/nodos/{id}                       PUT  /api/nodos/{id}
PUT  /api/nodos/{id}/estado                (ABIERTO | CERRADO | EN_CELEBRACION)
POST /api/nodos/{id}/horarios-excepcionales
POST /api/nodos/{id}/recursos              GET  /api/nodos/indicadores
GET  /api/eventos?idNodo=                  POST /api/eventos
POST /api/eventos/{id}/inscripciones       GET  /api/inscripciones/usuario/{id}
GET  /api/rutas?idCreador=                 POST /api/rutas   (calcula distancia y tiempo)
GET  /api/guias/disponibles?fecha=&hora=   POST /api/guias/{id}/disponibilidad
POST /api/salidas                          GET  /api/salidas?idProfesor=&idGuia=&idNodo=
PUT  /api/salidas/{id}/estado              PUT  /api/salidas/{id}/respuesta-guia
PUT  /api/salidas/{id}/finalizar           POST /api/salidas/{id}/evaluacion
GET  /api/avisos?soloVigentes=true         POST /api/avisos
PUT  /api/avisos/{id}/retirar              POST /api/avisos/{id}/reportes
```

## Arquitectura
Cada microservicio sigue la misma estructura:
```
model/        entidades JPA (tablas del modelo entidad-relación)
repository/   acceso a datos (Spring Data JPA)
dto/          objetos de entrada y salida con validaciones
service/      reglas de negocio
controller/   endpoints REST
config/       manejo de errores y datos iniciales
```
- **Cada servicio es dueño de sus datos.** Cuando necesita algo de otro servicio, guarda solo el id (por ejemplo `id_nodo` en un evento) y, si hace falta, consulta al otro microservicio por HTTP:
  - `ms-eventos` consulta a `ms-nodos` el nombre y la ubicación del templo.
  - `ms-usuarios` le pide a `ms-rutas` que cree el perfil de un guía al registrarse.
- Los errores se devuelven en un formato JSON común: `{ "estado": 400, "error": "...", "mensaje": "..." }`.

## Pruebas unitarias
```bash
./gradlew test
```
Hay pruebas de las reglas de negocio en cada servicio: registro y login, estado en tiempo real y horarios, cupos e inscripciones, cálculo de rutas, finalización de recorridos y vigencia de avisos.

## Simplificaciones del MVP
- `AUDIO_GUIA` se maneja como un `RECURSO_MULTIMEDIA` de tipo AUDIO.
- `COLEGIO` y `CURSO_GRUPO` se guardan como texto dentro de la salida pedagógica.
- No hay JWT: el login devuelve el usuario con su rol y la app controla los permisos. Se puede agregar Spring Security más adelante.
