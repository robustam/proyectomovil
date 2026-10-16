# Red Patrimonial App — Arzobispado de Santiago

Aplicación móvil Android (MVP) que muestra una red de puntos de interés patrimonial (iglesias, parroquias y monumentos) del Arzobispado de Santiago. Permite iniciar sesión, registrarse o explorar como invitado, ver un listado de templos y revisar la ficha de detalle de cada uno.

Desarrollada con **Kotlin** y **Jetpack Compose (Material 3)**.

---

## 📱 Pantallas incluidas

| Pantalla | Archivo | Qué muestra |
|---|---|---|
| Inicio de sesión | `LoginScreen.kt` | Formulario de correo y contraseña, botón **Iniciar Sesión**, botón **Explorar como Invitado** y enlace a registro. |
| Registro | `RegisterScreen.kt` | Formulario de nombre, correo, contraseña y confirmación. |
| Listado / Mapa | `MapScreen.kt` | Buscador y tarjetas de 4 puntos patrimoniales (Catedral Metropolitana, Iglesia San Francisco, Parroquia San Saturnino, Basílica del Salvador) con su categoría y estado. |
| Detalle patrimonial | `DetailScreen.kt` | Ficha del templo: categoría, dirección, horarios, reseña histórica, botón de favorito y **Solicitar Pase de Visita**. |

### Flujo de navegación

```
Login ──► Registro ──► Listado (Mapa) ──► Detalle
  │                        ▲
  └── Explorar como Invitado ┘
```

> **Nota:** el inicio de sesión y el registro son **simulados** (todavía no hay backend). Se puede escribir cualquier dato, o dejar los campos vacíos, y al presionar el botón la app avanza al listado.

---

## 🧰 Requisitos

| Herramienta | Versión usada en el proyecto |
|---|---|
| Android Studio | Versión reciente (compatible con AGP 9.4) |
| Android Gradle Plugin (AGP) | 9.4.1 |
| Gradle (wrapper) | 9.6.0 (se descarga solo) |
| Kotlin | 2.2.10 |
| JDK | 25 (Android Studio lo gestiona automáticamente) |
| `compileSdk` / `targetSdk` | 37 |
| `minSdk` | 26 (**Android 8.0** o superior) |

Se necesita **conexión a internet** la primera vez, para que Gradle descargue las dependencias.

---

## 🚀 Cómo visualizar el proyecto

Hay tres formas. La más simple es la **Opción A (emulador)**.

### Paso 0 — Abrir el proyecto en Android Studio

1. Descomprime el archivo `proyectomovil.zip`.
2. Abre **Android Studio** → **File → Open…**
3. Selecciona la carpeta **`proyectomovil`** (la que contiene `settings.gradle.kts`) y presiona **OK**.
4. Espera a que termine la **sincronización de Gradle** (barra de progreso abajo a la derecha). La primera vez puede tardar varios minutos.
5. Si aparece un aviso de que falta el **SDK 37**, haz clic en el enlace del mensaje para instalarlo, o ve a **Tools → SDK Manager** y marca la plataforma correspondiente.

> ⚠️ El archivo `local.properties` incluido apunta a la ruta del SDK de otro computador (`C:\Users\mlete\...`). Si Gradle reclama por el SDK, **borra `local.properties`** y vuelve a sincronizar: Android Studio lo regenera con tu propia ruta.

---

### Opción A — Ejecutar en un emulador (recomendado)

1. Abre **Tools → Device Manager**.
2. Presiona **+ (Create Virtual Device)**.
3. Elige un teléfono, por ejemplo **Pixel 8**, y presiona **Next**.
4. Selecciona una imagen de sistema con **API 26 o superior** (idealmente la más reciente) y descárgala si es necesario.
5. Presiona **Finish**.
6. En la barra superior, verifica que la configuración de ejecución sea **`app`** y que esté seleccionado el emulador creado.
7. Presiona el botón **▶ Run 'app'** (o `Shift + F10`).
8. Se abrirá el emulador y aparecerá la pantalla de **Inicio de sesión**.

### Opción B — Ejecutar en un teléfono Android físico

1. En el teléfono: **Ajustes → Acerca del teléfono** y toca **7 veces** "Número de compilación" para activar las *Opciones de desarrollador*.
2. Entra en **Ajustes → Sistema → Opciones de desarrollador** y activa **Depuración USB**.
3. Conecta el teléfono al PC por cable USB y acepta el mensaje *"¿Permitir depuración USB?"*.
4. En Android Studio, el teléfono aparecerá en la lista de dispositivos de la barra superior. Selecciónalo.
5. Presiona **▶ Run 'app'**.

> El teléfono debe tener **Android 8.0 o superior**.

### Opción C — Vista previa sin ejecutar (Compose Preview)

Cada pantalla tiene una función `@Preview`, así que se puede ver el diseño sin emulador:

1. Abre cualquiera de estos archivos en `app/src/main/java/com/arzobispado/redpatrimonial/ui/screens/`:
   - `LoginScreen.kt`
   - `RegisterScreen.kt`
   - `MapScreen.kt`
   - `DetailScreen.kt`
2. Arriba a la derecha del editor, cambia a la vista **Split** o **Design**.
3. Si se pide, presiona **Build & Refresh**.
4. Se mostrará la pantalla dibujada dentro de un teléfono de ejemplo.

> La vista previa muestra el diseño, pero no permite navegar entre pantallas. Para probar el flujo completo usa la Opción A o B.

---

## 🖥️ Alternativa por línea de comandos (opcional)

Desde la carpeta `proyectomovil`:

**Windows**
```bat
gradlew.bat assembleDebug
```

**macOS / Linux**
```bash
./gradlew assembleDebug
```

El APK queda en:

```
app/build/outputs/apk/debug/app-debug.apk
```

Para instalarlo en un emulador o teléfono conectado:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

O compilar e instalar en un solo paso:

```bash
./gradlew installDebug
```

---

## 🧭 Qué probar al ejecutar la app

1. **Login** → presiona **Iniciar Sesión** (sin datos reales) y llegarás al listado.
2. Vuelve atrás y prueba **Explorar como Invitado**: el listado muestra "Modo Invitado" y un botón **Ingresar** arriba a la derecha.
3. En **Login**, toca **"¿No tienes cuenta? Regístrate aquí"** para ver el formulario de registro.
4. En el **listado**, toca cualquier tarjeta para abrir su **ficha de detalle**.
5. En el **detalle**, presiona el ❤️ o **Solicitar Pase de Visita**: aparece un aviso pidiendo iniciar sesión (función reservada a usuarios registrados).

---

## 📂 Estructura del proyecto

```
proyectomovil/
├── app/
│   ├── build.gradle.kts                 # Configuración y dependencias del módulo
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/arzobispado/redpatrimonial/
│       │   ├── MainActivity.kt          # Punto de entrada; carga el tema y la navegación
│       │   ├── data/model/
│       │   │   └── NodoPatrimonial.kt   # Modelo de datos de cada punto patrimonial
│       │   ├── navigation/
│       │   │   ├── AppScreens.kt        # Rutas de navegación (sealed class)
│       │   │   └── AppNavigation.kt     # NavHost con todas las pantallas
│       │   └── ui/
│       │       ├── screens/             # Login, Register, Map y Detail
│       │       └── theme/               # Colores, tipografía y tema Material 3
│       └── res/                         # Íconos, strings, temas
├── gradle/libs.versions.toml            # Catálogo de versiones
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🛠️ Tecnologías

- Kotlin
- Jetpack Compose + Material 3
- Navigation Compose (navegación entre pantallas)
- Material Icons Extended
- Lifecycle ViewModel Compose

---

## 📌 Estado actual y próximos pasos

- Los datos de los templos están **escritos directamente en el código** (no hay base de datos ni API todavía).
- El login y el registro **no validan credenciales**.
- El parámetro `isGuest` vale `true` por defecto, por lo que el listado y el detalle se muestran siempre en modo invitado.
- La pantalla de detalle siempre muestra la Catedral Metropolitana, sin importar el templo seleccionado.
- Rutas ya declaradas pero aún sin pantalla: **Eventos**, **Planificador de rutas** y **Avisos comunitarios**.
- El nombre "Mapa" es provisional: aún no se integra un mapa real (por ejemplo, Google Maps).

---

## 🧯 Problemas comunes

| Problema | Solución |
|---|---|
| *"SDK location not found"* o error con la ruta del SDK | Borra `local.properties` y sincroniza de nuevo (**File → Sync Project with Gradle Files**). |
| *"Failed to find target with hash string 'android-37'"* | Instala la plataforma SDK 37 desde **Tools → SDK Manager**. |
| Error de versión de Java / JDK | Ve a **Settings → Build, Execution, Deployment → Build Tools → Gradle** y en *Gradle JDK* elige el JDK incluido en Android Studio, o uno recomendado por el IDE. |
| *"This version of the Android Gradle plugin requires a newer Android Studio"* | Actualiza Android Studio a la versión más reciente (**Help → Check for Updates**). |
| El emulador no inicia o va muy lento | Activa la virtualización (VT-x / AMD-V) en la BIOS y usa una imagen de sistema **x86_64**. |
| La vista previa (Preview) no aparece | Presiona **Build & Refresh** o compila una vez con **Build → Make Project**. |
