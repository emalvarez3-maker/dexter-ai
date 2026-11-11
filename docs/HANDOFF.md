# Relevo: cómo seguir este proyecto desde otra cuenta de Claude

Este archivo es para una sesión nueva de Claude Code que no conoce nada del proyecto. Léelo junto con [`CLAUDE.md`](../CLAUDE.md) (reglas y checks) y [`docs/WORKFLOW.md`](WORKFLOW.md) (método de trabajo). No contiene claves ni datos personales.

## Cómo empezar en la otra cuenta

1. Clona el repo y abre Claude Code en la carpeta:
   ```bash
   git clone https://github.com/tebl22/dexter-ai.git ancla-habitos
   ```
2. Pega este mensaje como primer mensaje de la sesión:
   > Lee `CLAUDE.md`, `docs/HANDOFF.md` y `docs/WORKFLOW.md`. Guarda en tu memoria las reglas de la sección «Reglas del usuario» de HANDOFF.md. Luego dime en tres líneas el estado del proyecto y qué sigue.
3. Si quieres seguir un trabajo a medias, copia también a mano la carpeta `.claude/runs/` del PC original (no está en git) y dile a Claude qué ejecución retomar.

## Qué es

**Dexter**: app Android (paquete `ec.ancla.habitos`) con un asistente de voz, «Oye Dexter», construida sobre Loop Habit Tracker (GPLv3). Rama de trabajo: `ancla/automaticidad`. Repos: `emalvarez3-maker/dexter-ai` (remoto `origin`) y `tebl22/dexter-ai` (remoto `tebl22`); se sube a los dos.

El código propio está en `uhabits-android/src/main/java/org/isoron/uhabits/ancla` y sus tests en `uhabits-android/src/test/java/org/isoron/uhabits/ancla`.

| Carpeta | Qué hay |
|---|---|
| `assistant/` | Servicio «Oye Dexter» (`DexterService`), palabra de activación (`DexterWake`, Vosk), agentes Gemini y Claude con ~65 herramientas (`GeminiAgent`, `ClaudeAgent`, `AnclaActions`), voz (`Speaker`, `GeminiVoice`), tarjeta flotante (`DexterOverlay`), seguridad (`PhoneSafety`), memoria (`DexterMemory`, `DexterArchive`, `DexterConversation`), recordatorios sin IA (`OfflineReminder`, `QuickCommands`) |
| `ui/` | Tema oscuro, búho, navegación, Hoy, Camino (roadmaps, WOOP, imagen de roadmap) |
| `finance/` | Finanzas por día, semana, mes y año; ahorro y metas de ahorro |
| `consciousness/` | Consciencia: pausa, rueda, examen, meditaciones guiadas |
| `mail/` | Gmail con inicio de sesión de Google |
| raíz de `ancla/` | Recordatorios y alarma a pantalla completa, avisos del día (`Nudges`), frase diaria, bloqueo del app, almacenes cifrados |

## Lo que no viaja con el repo

| Cosa | Dónde está | Qué hacer |
|---|---|---|
| Claves de IA (Gemini, Claude) | Cifradas en el teléfono (Android Keystore) | Nada: siguen en el teléfono. Nunca van al repo |
| Firma del APK | `~/.android/debug.keystore` del PC original | Cópiala al PC nuevo. Sin ella, instalar exige desinstalar el app y se pierden los datos |
| Datos del usuario | Solo en el teléfono, cifrados | Nada |
| Memoria de Claude | Carpeta de la cuenta original | Está resumida abajo, en «Reglas del usuario» |
| Ejecuciones del grafo | `.claude/runs/` (fuera de git) | Copiar a mano si se va a retomar una |
| Gmail (OAuth) | Proyecto de Google Cloud del usuario | Pendiente: crear el cliente OAuth Android con el paquete y la huella SHA-1 de la firma de arriba |

## Entorno

- Windows 11, JDK 17, Android SDK. `adb` no está en el PATH: se usa el de `platform-tools` del SDK local.
- Teléfono de prueba: Pixel 7 con Android 13, por USB.
- Comandos:
  ```bash
  ./gradlew :uhabits-android:assembleDebug
  ./gradlew :uhabits-android:testDebugUnitTest --tests "org.isoron.uhabits.ancla.*"
  ./gradlew :uhabits-android:installDebug
  ```
- Los tests tardan unos 10 minutos: córrelos en segundo plano.
- Las capturas del README se regeneran con `DEXTER_SCREENSHOTS=1` y `--tests "*ScreenshotsTest*"`; las pantallas del app bloquean capturas en el teléfono.

## Reglas del usuario

Guárdalas en memoria en la cuenta nueva.

1. **Tras cada cambio:** commit, push a `ancla/automaticidad` en los dos remotos, reinstalar en el Pixel, abrir el app y revisar logcat sin `FATAL EXCEPTION`. Antes, `git pull --ff-only`: otras sesiones también suben.
2. **Siempre tema oscuro gótico morado:** fondo `#0E0817`, tarjetas `#1A1027`, violeta `#9D7BFF`, oro `#FFB627`, jade `#3FBF95`. Nunca modo claro. El app se llama Dexter, nunca Ancla.
3. **Método de grafos:** toda tarea que cambie código sigue `docs/WORKFLOW.md`. Se escribe `graph.md` y se espera la aprobación del usuario antes de ejecutar.
4. **Seguridad:** nada personal se muestra ni se dice con el teléfono bloqueado; lo riesgoso (enviar, borrar) necesita un «sí» del usuario en un mensaje posterior; no cambiar ajustes de seguridad por adb; no desbloquear el teléfono.
5. **El teléfono es compartido:** otra sesión (la del app Don Bolón) puede usar el mismo Pixel por adb. No tocarlo mientras el usuario u otra sesión lo usa.
6. **El usuario habla español** y quiere respuestas claras y cortas, con pruebas hechas antes de decir que algo funciona.

## Trampas conocidas

- Cada URL de remoto lleva su usuario (`https://emalvarez3-maker@github.com/...`); sin él, git usa la otra cuenta y `origin` da 403.
- En Git Bash de Windows, las rutas de adb tipo `/sdcard/...` necesitan `MSYS_NO_PATHCONV=1`.
- `sed` rompe los `\n` dentro de cadenas de Kotlin: para esas ediciones usa la herramienta Edit.
- `.gitignore` ignora `*.md` salvo las excepciones listadas: un `.md` nuevo fuera de `docs/` necesita su excepción.
- Los modelos gratis de Gemini devuelven 429 o 503 a menudo. Dexter rota modelos y reintenta; no es un fallo del app. No se puede continuar una llamada a herramientas con otro modelo («missing thought_signature»).
- Android 13 silencia el micrófono de un servicio iniciado en segundo plano; `DexterMicActivity` lo recupera. Con otra app grabando o una llamada, Dexter cede el micrófono.

## Estado y pendientes

- Hecho y probado: voz, hábitos y tareas, Camino con roadmaps, Finanzas con ahorro, Consciencia, memoria, avisos, alarma a pantalla completa, recordatorios sin IA.
- Sin probar con el usuario: que «Oye Dexter» arranque solo tras reiniciar el teléfono.
- Bloqueado por el usuario: Gmail necesita el cliente OAuth en Google Cloud.
- En curso: «Encargos» (Dexter trabaja en segundo plano y con horario). Ver la sección siguiente.

## Trabajo a medias: Encargos (cortado el 2026-11-10)

Grafo aprobado por el usuario. Idea tomada de wangzan101/Mika (solo conceptos; su código y sus binarios no se usan). El grafo y el progreso completos están en `.claude/runs/2026-11-10-encargos/` del PC original (fuera de git); lo esencial está aquí.

**Qué es:** el usuario dice «averigua X y avísame» y un agente trabaja sin voz ni pantalla; también encargos con horario y condición («cada mañana revisa el clima y dime solo si llueve»).

**Hecho y subido (N0):** `ancla/tasks/Encargo.kt` es el contrato: `Encargo`, `EncargoState`, `Recurrence`, `EncargoBook`, `EncargoControl`, `UsageSink`, `AgentUsage`. Los agentes Gemini y Claude ya reportan sus tokens a `AgentUsage`. Compila; línea base previa: 188 tests, 0 fallos.

**Lanzado en subagentes y sin verificar (puede haber archivos a medias en el PC original, sin subir):**

| Nodo | Archivos nuevos | Qué hace |
|---|---|---|
| N1 | `tasks/EncargoStore.kt`, `tasks/EncargoSchedule.kt` + tests | Almacén cifrado y cálculo de próximas ejecuciones |
| N2 | `tasks/EncargoRunner.kt`, `tasks/EncargoActions.kt` + test | Corre el agente en segundo plano; `EncargoPrompts` arma el mensaje y decide si avisar |
| N4 | `tasks/TokenLedger.kt`, `tasks/UsageScreen.kt`, `strings_usage.xml` + test | Gasto de IA por día y modelo |
| N5 | `tasks/EncargosScreen.kt`, `tasks/EncargoNotifier.kt`, `strings_encargos.xml` | Pantalla de encargos y notificaciones |
| N6 | `assistant/WebLookup.kt` + test | Búsqueda web con fuentes vía Gemini (falta probar que funciona en la capa gratis) |

**Falta:**
1. Compilar y correr los tests con esos archivos; corregir lo que falle (si no existen, rehacer el nodo con la tabla de arriba).
2. N3: `EncargoService` (servicio en primer plano que aloja el runner) y alarmas exactas para los programados, con arranque tras reinicio.
3. N7: herramienta `encargo` (crear, listar, parar) y `web_lookup` en `GeminiAgent`, `ClaudeAgent` y `AnclaActions`; instrucciones del agente; registrar actividad, servicio, receptor y permiso `FOREGROUND_SERVICE_DATA_SYNC` en el manifest; entrada «Encargos» en el menú.
4. Checks fijos, instalar en el Pixel y subir a las dos cuentas.

**Reglas de este trabajo:**
- Un encargo nunca confirma nada: si una herramienta pide «CONFIRMACIÓN NECESARIA», se detiene en `WAITING_USER` y avisa.
- En segundo plano no se toca la pantalla ni se abre nada; solo `phone_status`, `phone_calendar` y `phone_alarm` del teléfono.
- El resultado se guarda cifrado y la notificación no lo muestra en la pantalla de bloqueo.
- Tope: 6 pasos de modelo por encargo y 3 encargos a la vez.
- Los subagentes no ejecutan Gradle (varios builds a la vez se bloquean); compila y prueba solo el orquestador. Archivos compartidos (agentes, `AnclaActions`, `DexterService`, manifest, `strings_ancla.xml`) solo los toca el orquestador.

## Otros proyectos del mismo usuario

- **Don Bolón**: app de seguimiento de comidas en una carpeta hermana (`don-bolon`), con sus propios `CLAUDE.md` y `WORKFLOW.md`. No comparte nada con Dexter.
