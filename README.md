# Red Patrimonial · App móvil (Kotlin + Jetpack Compose)

App Android del MVP para la Red Patrimonial del Arzobispado de Santiago (DSY1105).

## Cómo ejecutarla
1. Enciende los 5 microservicios de `RedPatrimonialBackend` (ver su README).
2. Abre esta carpeta en Android Studio y ejecuta la app en un **emulador**. La app se conecta a `10.0.2.2`, que en el emulador apunta al `localhost` del computador.
3. **En un celular físico:** cambia `HOST` en `data/remote/ApiConfig.kt` por la IP del computador (por ejemplo `192.168.1.15`) y conecta ambos a la misma red Wi-Fi.

> Si el backend no está encendido, la app **igual funciona**: usa los mismos datos de prueba en memoria y muestra el aviso "Sin conexión al servidor". Sirve para la presentación, pero lo ideal es mostrarla conectada.

Cuentas de prueba (clave `demo123`): `turista@demo.cl`, `profesor@demo.cl`, `guia@demo.cl`, `encargado@demo.cl`, `admin@demo.cl`. También puedes entrar como invitado.

## Arquitectura (MVVM + Navigation)
```
data/
  model/        Modelos (los mismos campos que el JSON del backend)
  remote/       Retrofit: ApiConfig, interfaces de los 5 microservicios
  repository/   Repositorios: llaman a la API y, si no hay servidor, usan datos de prueba
  mock/         Datos de prueba sintéticos
viewmodel/      AuthVM, NodoVM, EventoVM, RutaVM, SalidaVM, GuiaVM, AvisoVM (StateFlow)
navigation/     AppScreens (rutas) y AppNavigation (barra inferior según el rol)
ui/screens/     22 pantallas en Compose
ui/components/  Componentes reutilizables, mapa esquemático y QR simulado
util/           Formatos de fecha y cálculo de rutas
```
Flujo: **Pantalla → ViewModel → Repositorio → Retrofit → Microservicio**. Las pantallas no tienen datos escritos a mano: todo viene del ViewModel.

## Navegación según el rol
| Rol | Barra inferior |
|---|---|
| Invitado / Turista | Mapa · Buscar · Rutas · Avisos · Perfil |
| Profesor | Mapa · Salidas · Rutas · Avisos · Perfil |
| Guía | Mi panel · Mapa · Avisos · Perfil |
| Encargado de templo | Mi templo · Mapa · Avisos · Perfil |
| Administrador | Panel · Mapa · Avisos · Perfil |

## Pantallas y casos de uso
| Pantalla | Casos de uso |
|---|---|
| Login, Registro (con elección de perfil), Recuperar contraseña | Acceso |
| Mapa (inicio) | CU01, CU04 |
| Explorar (filtros por tipo, comuna y "abiertos ahora") | CU02 |
| Ficha patrimonial (Historia / Horarios / Galería / Eventos) | CU03 |
| Agenda cultural y Ticket QR | CU05 |
| Rutas, Detalle de ruta, Crear ruta (con distancia y tiempo calculados) | CU06, CU09 |
| Avisos en vivo (filtros y reportes) | CU14 |
| Perfil (entradas QR, rutas, favoritos) | — |
| Salidas pedagógicas y Nueva salida (asistente de 3 pasos) | CU07, CU08 |
| Panel del guía y Disponibilidad | CU16, CU17, CU18 |
| Mi templo: estado en tiempo real, horarios excepcionales, publicar evento, publicar aviso, solicitudes, editar ficha y galería | CU04, CU10–CU15 |
| Panel de administración (indicadores y permisos por nodo) | CU20, CU21 |

## Vistas previas (panel Split)
Cada pantalla tiene al final una función `@Preview` que usa datos de prueba (`ui/preview/VistaPrevia.kt`).
Abre cualquier archivo de `ui/screens`, elige **Split** y, si aparece el aviso, presiona **Build & Refresh**.

## Pendiente para próximas unidades
- **Room:** favoritos, rutas guardadas y una copia local de nodos y avisos. Hoy los favoritos solo duran la sesión.
- **GPS:** reemplazar el mapa esquemático por un mapa real y activar la navegación paso a paso.
- **Cámara:** subir fotos a la galería desde el teléfono, en lugar de usar URLs de prueba.
