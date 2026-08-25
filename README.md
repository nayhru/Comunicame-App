# Comunícame

Aplicación móvil Android de accesibilidad para personas con **discapacidad sensorial auditiva**.
El usuario escribe lo que quiere decir y el teléfono lo reproduce en voz alta, permitiéndole
comunicarse con una persona oyente sin depender de que esta sepa lengua de señas.

Proyecto de la asignatura **Desarrollo de Aplicaciones Móviles (DSY2204)** — Experiencia 1,
Evaluación Sumativa 1.

## Stack

| | |
|---|---|
| Lenguaje | Kotlin 2.1.10 |
| UI | Jetpack Compose + Material Design 3 |
| Navegación | Navigation Compose 2.8.5 |
| AGP / Gradle | 8.9.1 / 8.11.1 |
| compileSdk / targetSdk | 36 |
| minSdk | 24 (Android 7.0) |

## Pantallas

**Acceso**

| View | Qué hace |
|---|---|
| Login | Destino de inicio. Valida contra el arreglo de usuarios en memoria. |
| Registro | Alta de cuenta. Agrega el usuario al mismo arreglo. |
| Recuperar contraseña | Cambia la clave validando el correo registrado. |

**Panel del usuario autenticado** (barra de navegación inferior)

| Sección | Qué hace |
|---|---|
| Comunicar | Texto a voz, frases sugeridas y frases propias del usuario. |
| Emergencia | Tarjeta para mostrar a un tercero y números de emergencia. |
| Mi perfil | Datos de la cuenta y preferencias de accesibilidad. |

## Decisiones de accesibilidad

El usuario objetivo no percibe el canal auditivo, así que la app sigue estas reglas:

- **Ningún estado se comunica solo con sonido.** Todo evento se informa con un banner visual
  (icono + color + texto) y con vibración.
- **Ningún estado se comunica solo con color**, para no dejar fuera a quien es daltónico.
- **La vibración se declara como accesibilidad** (`USAGE_ASSISTANCE_ACCESSIBILITY`), no como
  notificación. Si no, el modo silencio del teléfono la suprime y el usuario se queda sin
  ningún aviso.
- **Sin _dynamic color_**: la paleta es fija y sus pares texto/fondo superan la razón de
  contraste 4.5:1 de la WCAG.
- **Tipografía más grande** que la escala por defecto de Material 3.
- `contentDescription` en los iconos, `liveRegion` en los mensajes y `selectableGroup()` en
  los grupos de radio buttons, para que TalkBack los lea bien.
- En **Emergencia**, la vía principal no es llamar —una persona sorda no sostiene una llamada
  telefónica— sino una tarjeta de alta legibilidad que se muestra a quien esté al lado.

## Estructura

```
app/src/main/java/com/example/comunicame/
├── MainActivity.kt              Activity única
├── data/
│   ├── Usuario.kt               Modelo y preferencias
│   ├── RepositorioUsuarios.kt   Arreglo con 5 usuarios precargados
│   ├── RepositorioFrases.kt     Frases sugeridas y las del usuario
│   └── Emergencia.kt            Servicios de emergencia
├── navigation/
│   ├── Rutas.kt                 Rutas centralizadas
│   └── NavegacionApp.kt         NavHost principal
└── ui/
    ├── components/
    │   ├── MensajeEstado.kt     Banner: icono + color + texto
    │   └── Retroalimentacion.kt Patrones de vibración
    ├── screens/
    │   ├── LoginScreen.kt
    │   ├── RegistroScreen.kt
    │   ├── RecuperarScreen.kt
    │   ├── PanelPrincipal.kt    Barra inferior + NavHost anidado
    │   ├── ComunicarScreen.kt
    │   ├── EmergenciaScreen.kt
    │   └── PerfilScreen.kt
    └── theme/                   Paleta, tipografía y tema
```

## Usuarios de prueba

| Usuario | Contraseña |
|---|---|
| ana | Ana12345 |
| carlos | Carlos123 |
| maria | Maria2026 |
| luis | Luis4567 |
| sofia | Sofia789 |

## Cómo ejecutar

1. Abrir la carpeta del proyecto en Android Studio.
2. Esperar la sincronización de Gradle.
3. Ejecutar sobre un emulador o dispositivo con Android 7.0 o superior.

> La vibración y el motor de voz dependen del dispositivo, así que conviene probarlos en un
> equipo físico. En teléfonos Xiaomi/Redmi hay que activar **Instalar vía USB** en las opciones
> de desarrollador para poder instalar el APK.

## Alcance de esta entrega

Solo Front End. **No** hay base de datos, backend ni autenticación real: todo se maneja en
arreglos en memoria y se pierde al cerrar la app.

Quedan **en proceso, documentadas para futuras entregas**: el modo voz a texto, el historial de
conversaciones, los ajustes de accesibilidad editables y la ficha médica con contacto de
emergencia. Están declaradas dentro de la propia app para que el alcance real quede visible.
