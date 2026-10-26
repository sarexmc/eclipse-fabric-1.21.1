# DEDsafio Eclipse (Fabric 1.21.1)

Port a Fabric del mod de eclipse, más cinematográfico y con Herobrine reforzado.
Requiere **Java 21**, **Fabric Loader 0.16+** y **Fabric API para 1.21.1**, en servidor y en cada jugador.

## Comandos (nivel de operador)
| Comando | Qué hace |
|---|---|
| `/eclipse start [segundos]` | Empieza el eclipse (por defecto 120 s hasta la totalidad). Pone mediodía y congela el ciclo de día mientras dura. |
| `/eclipse stop [segundos]` | La luz vuelve poco a poco (por defecto 60 s). |
| `/eclipse estado` | Muestra el progreso. |
| `/eclipse bunker add <nombre> <radio>` / `addbox <nombre> <desde> <hasta>` / `remove` / `list` | Zonas donde Herobrine no aparece (y desaparece si tú o él están dentro). |
| `/eclipse herobrine spawn` / `clear` | Prueba: invoca / elimina Herobrines. |

## Cinematográfico
- Barras de cine y título "ECLIPSE" al empezar; barras otra vez al llegar a la totalidad y al terminar.
- La luna entra lenta, acelera y frena al tapar el sol; destello de "anillo de diamante" justo antes de la totalidad.
- Latido que se acelera y se hace más fuerte, trueno grave al llegar a la oscuridad total y temblor de cámara.

## Reglas
- Mirar el eclipse sin nada que tape el cielo te prende fuego.
- **Linterna** (`dedsafio_eclipse:linterna`): clic derecho, cuenta regresiva de 2 s y se enciende; otro clic la apaga. 1 de durabilidad por segundo (1200 = 20 min), se repara con redstone. Receta: panel de vidrio, 3 lingotes de hierro y antorcha de redstone.
- **Herobrine** (160 de vida, armadura, 16 de daño, ciega 2,5 s al golpear): aparece en la oscuridad total. Si pasan 5 s sin alumbrarlo te persigue. Para hacerlo desaparecer hay que **acumular 6 s de luz** sobre él con un cono estrecho; si dejas de alumbrarlo pierde la luz rápido. Persiguiendo, la luz solo lo frena y, si te alejas, se teletransporta a tu espalda.

## Compilar
- GitHub: sube el repo y mira **Actions**; el `.jar` sale en *Artifacts*.
- Local: Java 21 + Gradle 8.10 (`gradle build`), jar en `build/libs/` (usa el que no termina en `-sources`).

## Notas técnicas
- No usa mixins: el cielo se dibuja al final del render del mundo (evento `WorldRenderEvents.LAST`) con una cúpula opaca a mayor distancia que el terreno, que tapa el cielo vanilla. Sin eclipse todo es vanilla. Las nubes vanilla siguen visibles (la oscuridad las tapa).
- El temblor de cámara se aplica sumando un pequeño giro al jugador cada tick (sin desvío neto).
- La oscuridad y el haz de las linternas son una capa dibujada en el mundo (no iluminación real de bloques); la mano y el HUD no se oscurecen.
- Si el build dice que no encuentra `fabric-api 0.115.0+1.21.1`, cambia `fabric_version` en `gradle.properties` por cualquier versión `+1.21.1` publicada en https://modrinth.com/mod/fabric-api/versions
