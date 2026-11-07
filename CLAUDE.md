# Dexter

App Android con un asistente de voz («Oye Dexter»), construida sobre Loop Habit Tracker. Paquete `ec.ancla.habitos`; el código de Dexter vive en `uhabits-android/src/main/java/org/isoron/uhabits/ancla`.

## Cómo se desarrolla

Toda tarea que cambie código sigue el método de grafos de [docs/WORKFLOW.md](docs/WORKFLOW.md):

1. Rellena las entradas (TAREA, SALIDA, CHECKS, FUENTES, ALCANCE, LIMITE). Si falta un dato que cambia el resultado, haz una sola pregunta.
2. Mide la línea base: build y tests antes de tocar nada.
3. Escribe `graph.md` con nodos, aristas y checks, y espera la aprobación del usuario antes de ejecutar.
4. Ejecuta el bucle: nodos independientes en paralelo, cada salida por su puerta, fallos de vuelta solo a su nodo (máximo tres intentos).
5. Cierra con `checks.md`, `progress.md` y las reglas nuevas apuntadas aquí.

Cada ejecución vive en `.claude/runs/AAAA-MM-DD-tarea/` (graph.md, checks.md, progress.md). Esa carpeta no se sube a git porque puede contener datos personales. Al retomar una tarea, lee primero su graph.md y progress.md.

Un cambio de una línea o una pregunta no necesita grafo: basta con correr los checks fijos.

## Checks fijos (puertas deterministas)

| Puerta | Comando o evidencia |
|---|---|
| Al día | `git pull --ff-only origin ancla/automaticidad` antes de empezar: otras sesiones también suben aquí |
| Compila | `gradlew.bat :uhabits-android:assembleDebug` termina en BUILD SUCCESSFUL |
| Tests | `gradlew.bat :uhabits-android:testDebugUnitTest --tests "org.isoron.uhabits.ancla.*"`, 0 fallos |
| Funciona en el teléfono | `gradlew.bat :uhabits-android:installDebug` en el Pixel 7, abrir `ec.ancla.habitos` y revisar logcat sin FATAL EXCEPTION |
| Sin secretos | `git diff` sin `AIza…`, `AQ.…` ni `sk-ant-…`, ni correos, mensajes o datos personales del usuario |

adb no está en el PATH: usa el de `platform-tools` del Android SDK local.

## Reglas aprendidas

- Tras cada cambio: commit, push a `ancla/automaticidad` en las dos cuentas (`origin` = emalvarez3-maker/dexter-ai, `tebl22` = tebl22/dexter-ai) y reinstalar en el Pixel.
- Cada URL de remoto lleva su usuario (`https://emalvarez3-maker@github.com/...`); sin él git usa la sesión de tebl22 y origin da 403.
- Siempre tema oscuro gótico morado: fondo #0E0817, tarjetas #1A1027, violeta #9D7BFF, oro #FFB627, jade #3FBF95. Nunca modo claro.
- No tocar el teléfono mientras el usuario lo usa; nunca desbloquearlo ni cambiar ajustes del sistema. Otras sesiones (Don Bolón) pueden usar el mismo teléfono por adb.
