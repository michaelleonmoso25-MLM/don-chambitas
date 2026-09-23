# Estado actual

> Archivo **vivo**. Quien termina una tarea lo actualiza. Es la primera cosa
> que lee el agente y la única fuente confiable sobre qué está pasando hoy.

**Última actualización:** 2026-09-23

---

## Sprint en curso

| Campo | Valor |
|---|---|
| Sprint | 2 |
| Fechas | PENDIENTE |
| Tareas del sprint | 16 |
| Terminadas | 5 |
| En curso | 0 |
| Bloqueadas | 0 |

> Sprint 1 cerrado el 2026-09-20 con sus 16 tareas en `hecha`.

## Qué se puede probar hoy en la aplicación

Léelo antes de instalar el APK y reportar que algo "no funciona". La
aplicación ya inicia sesión y registra, pero contra una fuente de datos **en
memoria**: no hay Supabase todavía.

| Si haces esto | Pasa esto hoy | Lo arregla |
|---|---|---|
| Abres la aplicación | P-01 espera 800 ms y te deja en P-02 | — |
| Estás en P-02 (iniciar sesión) | La pantalla real: marca, los dos campos y los enlaces a P-03 y P-04 | — |
| Entras con una cuenta de la fuente falsa, como `juan.perez@ejemplo.com` | Llegas a P-05 o P-10 **según el rol de la cuenta**. La fuente falsa **no revisa la contraseña**: cualquiera sirve | `S2-T07` |
| Entras con un correo que no existe | "Correo o contraseña incorrectos" y lo escrito se queda | — |
| Entras a P-03 desde el marcador de P-02 | La pantalla real de registro, con sus cinco campos y el selector de rol | — |
| Confirmas el registro **sin elegir rol** | Te reclama el rol y no hace nada más | — |
| Escribes un correo sin arroba y sales del campo | Sale "Ese correo no se ve bien, revísalo" debajo. Se borra en cuanto vuelves a escribir | — |
| Pulsas el botón de P-02 o P-03 con campos mal | Marca **todos** los que fallan, no avanza y deja el foco en el primero | — |
| Entras en P-02 con una contraseña de un carácter | **Entra.** Al iniciar sesión solo se exige que no esté vacía (5.2); el mínimo de 8 es del registro | — |
| Te registras con un correo nuevo | Se crea la cuenta **en memoria** y entras a P-05 o P-10 según el rol. Con ese correo ya puedes iniciar sesión | `S2-T07` |
| Te registras con un correo que ya existe | "El correo ya está registrado, inicia sesión" y no se borra nada | — |
| Cierras y vuelves a abrir | No hay cuenta que recordar, ni sesión | `S2-T08`, `S2-T09` |

**El alta todavía no es real.** Escribe en `FuenteDatosFalsa`, que vive en
memoria: puedes registrarte y entrar, y la cuenta desaparece al reiniciar la
aplicación. Cuentas de verdad, contra
Supabase Auth, son `S2-T07`. `H-10` ya se cerró: `DEC-25` decide que el
registro deja sesión abierta, así que `S2-T07` va con la confirmación por
correo de Supabase Auth desactivada.

## Tarea en curso

_Ninguna._

| Campo | Valor |
|---|---|
| ID | — |
| Título | — |
| Quién la tomó | — |
| Rama | — |
| Desde | — |

## Última tarea terminada

**`S2-T05` — ViewModels y estados de UI del flujo de autenticación.** 2026-09-23.
Rama `feat/S2-T05-viewmodels-autenticacion`, pull request **sin abrir
todavía**. **Sale de la rama de `S2-T04`**,
`feat/S2-T04-validaciones-formularios`, que todavía no está en `main`: hay que
integrar primero esa.

Ticket `docs/tareas/S2-T05.md`, redactado por el agente. El alcance salió de
las secciones 2.2 a 2.4, 3.3 a 3.5 y 6 de `docs/producto/DISENO-AUTENTICACION.md`.

- `ui/pantallas/IniciarSesionViewModel.kt` y `RegistroViewModel.kt`: `@HiltViewModel` que reciben `RepositorioAuth`, la interfaz. Exponen `StateFlow` con los contratos de 2.2 y 3.3 y un método por evento de 2.3 y 3.4. Al enviar normalizan como pide 1.6 y resuelven el resultado con las matrices de 2.4 y 3.5: `Exito` fija `destino` según el rol, y `Error` fija `errorPantalla` conservando lo escrito, con `mensajePantalla` solo en `VALIDACION` del registro. `LIMITE_IA` se pinta como `DESCONOCIDO` (sección 6).
- `EstadoRegistro.kt`: gana `destino`, como pide 3.3.
- `IniciarSesionPantalla.kt` y `RegistroPantalla.kt`: ya no guardan estado. Toman su ViewModel con `hiltViewModel()` y navegan cuando hay `destino`, consumiéndolo de inmediato. Los `listSaver` salieron: el ViewModel sobrevive al giro. El contenido visual no cambió.
- `ui/navegacion/GrafoNavegacion.kt`: `entrarConSesion` navega al destino limpiando la pila del subgrafo de autenticación y sigue fijando `MarcadorSesionTemporal`, que P-01 lee hasta `S2-T09`.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 113 pruebas unitarias, 27 nuevas en `IniciarSesionViewModelTest` y `RegistroViewModelTest`: pasan todas **menos las 2 de `SplashViewModelTest`** que ya fallaban (hallazgo de `S2-T04`).
  - 33 pruebas instrumentadas de P-02 y P-03 pasando, 0 fallas, con el ViewModel real contra la fuente falsa.
  - Recorrido a mano en emulador `Pixel_8` con la aplicación completa: `noexiste@ejemplo.com` muestra "Correo o contraseña incorrectos" y conserva lo escrito; `juan.perez@ejemplo.com` entra a P-05 como cliente; un correo escrito en P-02 sigue ahí después de girar a horizontal. Sin excepciones en logcat.
- **Desvío: el ViewModel de P-04 no se hizo.** La sección 9 lo asigna a esta tarea, pero su pantalla es `S2-T10` y no existe: sería código que nadie llama (AGENTS.md §7). Lo construye `S2-T10` con el contrato de 4.3. **Para que el líder lo confirme.**
- **La fila "`Exito(Usuario)` sin sesión" de 3.5 no se implementó** porque `DEC-25` la eliminó: el registro deja la sesión abierta. `registrar` sigue devolviendo `Usuario`; el cambio a `Sesion` es de `S2-T06` y `S2-T07`.

**`S2-T04` — Validaciones de formularios y mensajes de error.** 2026-09-23.
Rama `feat/S2-T04-validaciones-formularios`, pull request **#11**, abierto desde el fork `michaelleonmoso25-MLM/don-chambitas`. **El #10 de Ricardo5690 implementa la misma tarea**: se trabajaron en paralelo sin saberlo, y el líder decide cuál integrar.

Ticket `docs/tareas/S2-T04.md`, redactado por el agente como el de `S2-T03`.
El alcance salió de las secciones 1.5, 1.6, 5 y 7 de
`docs/producto/DISENO-AUTENTICACION.md`.

- `dominio/validacion/ValidacionesAuth.kt`: las 12 reglas de la tabla 5.2, una función pura por campo, sin Android. El correo usa la expresión de 5.1, espejo de `ck_usuario_correo_valido`. `LimitesRegistro` se mudó aquí desde `EstadoRegistro.kt`, con los mismos topes, y ganó `LARGO_MINIMO_CONTRASENA = 8`.
- `ui/pantallas/ValidacionEnPantalla.kt`: traduce cada `FalloValidacion` a su `R.string.validacion_*`, y `alPerderFoco`, que avisa cuando el foco sale de un campo.
- `IniciarSesionPantalla.kt` y `RegistroPantalla.kt`: validan cuando dice 1.5. Al salir de un campo **en el que ya se escribió** se valida ese campo; al pulsar el botón se validan todos, se marcan todos los que fallan y el foco salta al primero. Si el primero es el rol, la pantalla sube hasta el selector. Escribir limpia el error del campo, como ya pasaba.
- `strings.xml`: `validacion_contrasena_vacia`, la única clave de la sección 7 que faltaba.
- La vista previa "P-02 con errores" mostraba `validacion_contrasena_corta`, que en P-02 nunca sale. Ahora muestra `validacion_contrasena_vacia`.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 86 pruebas unitarias, 29 nuevas en `ValidacionesAuthTest`: pasan todas **menos 2 de `SplashViewModelTest`**, que fallan igual sin esta tarea. Ver el hallazgo abajo.
  - 31 pruebas instrumentadas de P-02 y P-03 pasando, 0 fallas: 16 en `IniciarSesionPantallaTest` y 15 en `RegistroPantallaTest`, 8 de ellas nuevas. Dos pruebas viejas se ajustaron porque enviaban el formulario vacío o sin contraseña, que ahora no pasa. Las 5 de `S1-T16` no se corrieron en esta máquina.
  - Recorrido a mano en emulador `Pixel_8` (Android 17): P-02 vacío marca los dos campos y enfoca el correo; `hola` al salir marca el formato y se limpia al escribir; P-03 con rol y todo vacío marca los cinco campos y enfoca el nombre. Sin excepciones en logcat.
- **Desvío de la letra de la sección 5, para que el líder lo revise.** La sección dice que cada regla devuelve `Int?`, el recurso del mensaje, pero `ARQUITECTURA.md` prohíbe Android en `dominio`, y `R` es de Android. Las reglas devuelven `FalloValidacion` y la capa `ui` lo traduce. En pantalla no cambia nada.
- **"Campo tocado" se interpretó como "ya se escribió en él".** Con "tuvo el foco", tocar un campo vacío y pasar a otro ya lo pinta de rojo, que es lo que 1.5 llama hostil.
- **Hallazgo, fuera del alcance: `SplashViewModelTest` es intermitente.** `SplashViewModel` llama a `verificarSesion()` en su `init`, y la prueba lanza una segunda verificación. Las dos terminan a los 800 ms simulados, pero cada una resta tiempo real (`System.currentTimeMillis`), así que cuál escribe al último depende de la velocidad de la máquina. `debeResolverDestinoInicioCliente_...` y `debeResolverDestinoInicioTrabajador_...` fallan de forma constante en esta máquina, con y sin los cambios de `S2-T04`. Es de `S1-T15`/`S1-T16`; no se tocó.
- **Lo que esta tarea NO trae:** la normalización al enviar y la llamada a `RepositorioAuth` son `S2-T05`, que además moverá esta validación al ViewModel sin cambiar las reglas. P-04 es `S2-T10` y usará `validarCorreo`.

**`S2-T03` — Pantalla de inicio de sesión.** 2026-09-22.
Rama `feat/S2-T03-pantalla-inicio-sesion`, pull request **sin abrir todavía**.

**Primera tarea del Sprint 2 con ticket**, `docs/tareas/S2-T03.md`, redactado el mismo día por instrucción del líder. El alcance salió de las secciones 1, 2 y 7 de `docs/producto/DISENO-AUTENTICACION.md`.

Implementación de P-02 conforme a esa especificación:
- `ui/pantallas/EstadoIniciarSesion.kt`: los siete campos del contrato 2.2. Los errores viajan como `@StringRes Int?` y `destino` es un evento de un solo uso.
- `ui/pantallas/IniciarSesionPantalla.kt`: `IniciarSesionPantalla` con estado local —provisional hasta que `S2-T05` traiga el ViewModel— y `IniciarSesionContenido`, el contenido visual puro. Los ocho elementos de la tabla 2.1 en orden, **sin flecha de regreso** por ser la raíz del subgrafo, isotipo del casco a 72 dp reutilizando el vector de P-01, y el correo normalizado con `trim().lowercase()` solo al enviar (regla 1.6): en pantalla se sigue viendo lo que se tecleó. Cuatro `@Preview`.
- `ui/navegacion/GrafoNavegacion.kt`: sustitución del marcador de P-02 por la pantalla real.
- `strings.xml`: 5 cadenas nuevas con las claves que fijó `S2-T01`. `auth_correo` y `auth_contrasena` ya existían desde `S2-T02`.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 57 pruebas unitarias pasando (`./gradlew testDebugUnitTest`).
  - 27 pruebas instrumentadas pasando, 0 fallas (`./gradlew connectedDebugAndroidTest`): las 16 anteriores y 13 nuevas en `IniciarSesionPantallaTest`.
  - Recorrido a mano en emulador `Medium_Phone` (Android 17): P-02 arranca sin flecha, "Regístrate" y "¿Olvidaste tu contraseña?" apilan P-03 y P-04 y el botón atrás regresa a P-02, el correo se conserva al girar el dispositivo y el alta entra a P-05 con el botón atrás cerrando la aplicación. Sin excepciones en logcat.
- **Se corrigió un defecto del andamio de `S1-T12`, fuera del alcance del ticket.** El `Scaffold` del grafo pintaba `BarraSuperior` en **toda** ruta, así que cada pantalla real —que trae la suya por la regla 1.1— salía con **dos barras encimadas, dos títulos y dos flechas**. Afectaba a P-01, P-02 y P-03. `P-03 ya estaba así en main`: entró con el pull request #8 y su criterio de aceptación se marcó cumplido igual. Se corrigió con `RUTAS_SIN_BARRA_DEL_ANDAMIO` en `GrafoNavegacion.kt`, que además devuelve el inset superior a la pantalla para que su barra dibuje bajo la barra de estado. **Cada pantalla que sustituya a su marcador tiene que agregarse a ese conjunto.**
- **Lo que esta tarea NO trae, por estar en la cola aparte:** las reglas de validación de formato y longitud son `S2-T04`, y el ViewModel contra `RepositorioAuth` es `S2-T05`. Hoy el botón entra **siempre como cliente**, porque sin `Sesion` no hay rol que leer.

**`S2-T02` — Pantalla de registro con selección de rol (cliente / trabajador).** 2026-09-21.
Rama `docs/S2-T01-diseno-pantallas-autenticacion`, pull request **sin abrir todavía**.

**También se trabajó sin ticket**, con la misma autorización del líder del 2026-09-21. El alcance se tomó de las secciones 1, 3 y 7 de `docs/producto/DISENO-AUTENTICACION.md`.

Implementación de P-03 conforme a esa especificación:
- `ui/pantallas/EstadoRegistro.kt`: estado inmutable con los seis valores de captura, el rol y un identificador de recurso por cada error de campo. Los mensajes viajan como `@StringRes Int?` y no como texto, para que ninguna cadena de interfaz viva fuera de `strings.xml`. `LimitesRegistro` fija los topes de captura con los de `public.usuarios` (80, 120, 160 y 10 dígitos).
- `ui/pantallas/RegistroPantalla.kt`: `RegistroPantalla` con el estado local —provisional hasta que `S2-T05` traiga el ViewModel— y `RegistroContenido`, el contenido visual puro. Selector de rol de dos `ChipCategoria` sin preselección (`DEC-22`), los cinco campos con su teclado y su acción de avance, ayuda de contraseña siempre visible, error en línea que no tapa el formulario, y `rememberSaveable` que conserva lo capturado al girar el dispositivo. Cuatro `@Preview`: vacío, con errores, cargando y correo duplicado.
- `ui/navegacion/GrafoNavegacion.kt`: sustitución del marcador de P-03 por la pantalla real. El alta marca la sesión con el mecanismo temporal de `S1-T12` y navega a P-05 o P-10 limpiando la pila del subgrafo de autenticación.
- `strings.xml`: 24 cadenas nuevas con las claves que fijó `S2-T01`.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 57 pruebas unitarias pasando (`./gradlew testDebugUnitTest`).
  - 16 pruebas instrumentadas pasando, 0 fallas (`./gradlew connectedDebugAndroidTest`): las 5 de `S1-T16` y 11 nuevas en `RegistroPantallaTest`.
  - Recorrido a mano en emulador `emulator-5554`: confirmar sin rol reclama el rol, elegirlo limpia el mensaje, el teléfono descarta lo que no sea dígito y corta en 10 (`477-12ab34x5678901` quedó en `4771234567`), y el alta como trabajador entra a P-10 con el botón atrás cerrando la aplicación. Sin excepciones en logcat.
- **Lo que esta tarea NO trae, por estar en la cola aparte:** las reglas de validación de formato y longitud con sus mensajes son `S2-T04`, y el ViewModel contra `RepositorioAuth` es `S2-T05`. Lo único que la pantalla decide hoy es exigir el rol, que es el título de la tarea.
- **Desvío de `CONVENCIONES.md`: las dos tareas van en la misma rama.** `S2-T02` se trabajó sobre `docs/S2-T01-diseno-pantallas-autenticacion` en vez de abrir `feat/S2-T02-pantalla-registro`. La regla es rama por tarea y pull request por tarea; aquí sale un solo pull request con las dos. Los commits sí llevan su `S2-T01` o `S2-T02` en el scope, así que se pueden separar después si el líder lo prefiere.
- **Se actualizó Espresso de 3.6.1 a 3.7.0 y `androidx.test.ext:junit` de 1.2.1 a 1.3.0.** No es parte de la tarea: las 5 pruebas instrumentadas de `S1-T16` ya venían fallando en la imagen actual del emulador porque Espresso 3.6.1 llama por reflexión a `InputManager.getInstance`, que ya no existe. Con la actualización las 16 pasan.

**`S2-T01` — Diseño de las pantallas de registro, inicio de sesión y recuperación.** 2026-09-21.
Rama `docs/S2-T01-diseno-pantallas-autenticacion`, pull request **sin abrir todavía**.

**Se trabajó sin ticket.** `docs/tareas/S2-T01.md` no existe —los tickets del Sprint 2 no se han redactado— y el líder autorizó avanzar sin él el 2026-09-21. El alcance acordado quedó escrito en la sección 0 del entregable y hace las veces de ticket.

Especificación de detalle de P-02, P-03 y P-04, que convierte los wireframes de `S1-T14` en algo implementable sin volver a decidir nada:
- `docs/producto/DISENO-AUTENTICACION.md`: documento nuevo, diez secciones.
  - Reglas comunes a las tres pantallas: esqueleto con medidas, scroll e `imePadding()` obligatorios, catálogo cerrado de componentes, cuándo se valida, normalización previa al envío, teclado y orden de foco, accesibilidad.
  - Los cuatro estados por pantalla, con el estado vacío declarado **no aplicable** en las tres y el porqué: son formularios, no listan datos. El cargando va dentro del `BotonPrincipal`, no a pantalla completa, para que HU-02 pueda conservar lo escrito.
  - Anatomía de arriba hacia abajo de cada pantalla, contrato de `Estado...` y lista de eventos con los nombres de `CONVENCIONES.md` (`EstadoIniciarSesion`, `EstadoRegistro`, `EstadoRecuperarContrasena`).
  - Matriz de cada resultado posible de `RepositorioAuth` contra lo que ve el usuario, en las tres pantallas.
  - Validaciones campo por campo con mensaje y clave, usando **la misma expresión regular** que `ck_usuario_correo_valido` en el esquema, y con los topes de longitud de `public.usuarios` aplicados en el propio campo.
  - Las 39 cadenas nuevas de `strings.xml` con su clave definitiva, para que `S2-T02`, `S2-T03`, `S2-T04` y `S2-T10` no inventen tres nombres distintos para lo mismo.
  - Navegación entre las tres, con `popBackStack` en los regresos y limpieza de la pila del subgrafo al entrar.
- `docs/producto/WIREFRAMES.md`: nota al inicio que enlaza el documento nuevo y deslinda qué resuelve cada uno.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 57 pruebas unitarias pasando, 0 fallas (`./gradlew testDebugUnitTest`).
  - Instalación y arranque limpio en emulador `emulator-5554` (`Displayed MainActivity`, sin excepciones en logcat).
- **No se escribió código:** la tarea es de diseño. Las pantallas las construyen `S2-T02`, `S2-T03` y `S2-T10`; los ViewModels, `S2-T05`; las validaciones, `S2-T04`.
- **Dos hallazgos nuevos para el líder, `H-09` y `H-10`.** Están al final del documento y se repiten abajo. Los dos se cerraron el 2026-09-22: `H-10` con `DEC-25` y `H-09` con `DEC-27`.

**`S1-T16` — Estrategia de pruebas y configuración de las pruebas base (JUnit / Compose test).** 2026-09-20.
Rama `test/S1-T16-estrategia-pruebas`, pull request **sin abrir todavía**.

Configuración completa de la infraestructura, utilidades y documentación de pruebas según `CONVENCIONES.md` y `ARQUITECTURA.md`:
- `docs/tecnico/PRUEBAS.md`: Documento maestro de estrategia de pruebas definiendo qué se prueba (ViewModels, dominio, repositorios falsos, interacción UI) y qué no (Composables pasivos, código generado), convenciones `debe..._cuando...`, comandos de ejecución y generación de reportes de cobertura JaCoCo.
- `app/src/test/.../util/ReglaCorrutinas.kt`: Regla de JUnit 4 (`TestWatcher`) para pruebas unitarias que reemplaza `Dispatchers.Main` con `StandardTestDispatcher`, proveyendo `testDispatcher` y `testScope` sin requerir el Looper de Android.
- `app/src/test/.../util/DatosPrueba.kt`: Fábrica con valores por defecto y argumentos con nombre para todas las entidades principales (`Usuario`, `PerfilTrabajador`, `PerfilHabilidad`, `Servicio`, `ServicioFoto`, `Solicitud`, `Postulacion`, `Conversacion`, `Mensaje`, `Resena`, `Categoria`, `Estado`, `Municipio`, `Sesion`, etc.).
- `app/src/test/.../util/DatosPruebaTest.kt`: Pruebas unitarias para validar `DatosPrueba` y la ejecución en el despachador principal mediante `ReglaCorrutinas`.
- `app/src/test/.../ui/pantallas/SplashViewModelTest.kt`: Actualizado para utilizar `ReglaCorrutinas` y la convención de nomenclatura `debe..._cuando...`, funcionando como plantilla oficial para ViewModels con corrutinas.
- `app/src/androidTest/.../ui/componentes/ComponentesTest.kt`: 4 pruebas instrumentadas en Jetpack Compose (`createComposeRule`) validando renderizado y eventos de `BotonPrincipal`, `CampoTexto` y `EstadoVacio`.
- `app/build.gradle.kts`: Activación de `enableUnitTestCoverage = true` y `enableAndroidTestCoverage = true` en el build type `debug` para soportar las tareas de reporte JaCoCo (`createDebugUnitTestCoverageReport` y `createDebugCoverageReport`).
- Verificación del proyecto:
  - 57 pruebas unitarias pasando (`./gradlew testDebugUnitTest`).
  - 5 pruebas instrumentadas pasando en emulador (`./gradlew connectedDebugAndroidTest`).
  - Reporte de cobertura generado exitosamente (`./gradlew createDebugUnitTestCoverageReport`).
  - Compilación exitosa (`./gradlew assembleDebug`).
  - Instalación y ejecución interactiva limpia en emulador `emulator-5554` (`Displayed MainActivity`).

**`S1-T15` — Pantalla de bienvenida (splash).** 2026-09-20.
Rama `feat/S1-T15-pantalla-splash`, pull request **sin abrir todavía**.

Implementación completa de P-01 (Splash) según `PANTALLAS.md`, `WIREFRAMES.md` y `DISENO.md`:
- `ui/pantallas/EstadoSplash.kt`: Data class de estado inmutable con propiedades `cargando: Boolean` y `destino: Ruta?`.
- `ui/pantallas/SplashViewModel.kt`: ViewModel con `@HiltViewModel` que consulta el estado de sesión temporal (según S1-T12 hasta S2-T09) e impone un retraso mínimo de 800 ms para evitar parpadeos, resolviendo los tres destinos posibles:
  - Sin sesión -> `P-02` (Iniciar sesión)
  - Cliente -> `P-05` (Inicio cliente)
  - Trabajador -> `P-10` (Inicio trabajador)
- `ui/pantallas/SplashPantalla.kt`: Composable con fondo `Crema` (`#FFFDF8`), isotipo del casco oficial de seguridad Don Chambitas, nombre de aplicación en `Carbon` (negrita), eslogan "Tu oficio, tu chamba" en `Cafe`, e indicador circular `Cargando` en color `Mostaza` (48 dp) con etiqueta "Verificando sesión…".
- `ui/navegacion/GrafoNavegacion.kt`: Sustitución del marcador provisional por `SplashPantalla`, saliendo de la pila de navegación con `popUpTo(Ruta.Splash.ruta) { inclusive = true }` de modo que presionar el botón Atrás desde el destino cierra la aplicación.
- `themes.xml` y `colors.xml`: Configuración de `android:windowBackground` con `color_crema` (`#FFFDF8`) eliminando cualquier parpadeo de fondo blanco antes de renderizar Compose.
- Pruebas unitarias: 7 pruebas unitarias nuevas en `SplashViewModelTest.kt` cubriendo estados iniciales, resolución de destinos y temporizador mínimo (51 pruebas totales en el proyecto pasando limpiamente).
- Compilación (`./gradlew assembleDebug`), instalación y verificación interactiva en emulador `emulator-5554` comprobando arranque en Splash, transición a destino y cierre limpio con botón Atrás.

**`S1-T14` — Wireframes de las pantallas de autenticación e inicio.** 2026-09-20.
Rama `docs/S1-T14-wireframes-pantallas`, pull request **sin abrir todavía**.

Diseño y documentación completa de las pantallas para el arranque del Sprint 2 conforme a `PANTALLAS.md` y `DISENO.md`:
- `docs/producto/wireframes/`:
  - 11 wireframes vectorizados a escala móvil estándar **360 × 800 dp** con la paleta oficial **Taller** (Mostaza, MostazaOscuro, Terracota, Carbon, Cafe, Crema, Arena, Borde y semánticos Exito, Advertencia y Error):
    - `P-01-splash.png`: Splash con casco de seguridad, slogan y widget de carga.
    - `P-02-iniciar-sesion.png`: Login con campos de captura, visibilidad y enlaces de navegación.
    - `P-03-registro.png`: Alta de cuenta con selector de rol único (`ChipCategoria`), 5 campos y enlaces.
    - `P-04-recuperar.png`: Formulario de recuperación de contraseña con aviso de vigencia (24h).
    - `P-05-inicio-cliente.png`: Inicio cliente con buscador, chips de oficios, tarjetas de trabajadores con estrellas, botón flotante "+ Publicar solicitud" y barra inferior de 4 destinos.
    - `P-05-vacio.png`: Estado vacío de búsqueda con icono, explicación y acción de limpiar filtros.
    - `P-05-error.png`: Estado error con icono de advertencia, mensaje explicativo y botón de reintentar.
    - `P-10-inicio-trabajador.png`: Inicio trabajador con filtro de oficios, tarjetas de solicitudes abiertas, etiqueta de estado y barra inferior.
    - `P-10-vacio.png`: Estado vacío de solicitudes por categoría con sugerencias y botón de ver todas.
    - `P-10-error.png`: Estado error con mensaje de reintento.
    - `P-18-cuenta.png`: Mi cuenta con cabecera de perfil, badge de rol, acciones de configuración y cierre de sesión.
  - Cada zona cuenta con anotaciones exactas de componentes (`BarraSuperior`, `BarraInferior`, `BotonPrincipal`, `BotonSecundario`, `BotonDestacado`, `BotonTexto`, `CampoTexto`, `CampoContrasena`, `TarjetaTrabajador`, `TarjetaSolicitud`, `ChipCategoria`, `Estrellas`, `EtiquetaEstado`, `Cargando`, `EstadoVacio`, `EstadoError`). Cero componentes ajenos a `DISENO.md`.
- `docs/producto/WIREFRAMES.md`:
  - Índice maestro con tabla resumen, imágenes incrustadas, desglose de componentes por zona y documentación exhaustiva del comportamiento de cada elemento tocable (eventos al pulsar, navegación, validaciones y cambios de estado).
- Verificación del proyecto:
  - Pruebas unitarias pasando limpiamente (44 pruebas, `./gradlew testDebugUnitTest`).
  - Compilación exitosa (`./gradlew assembleDebug`).
  - Instalación y ejecución interactiva limpia en emulador `emulator-5554` (`Displayed MainActivity`).

**`S1-T13` — Interfaces de repositorio y fuente de datos falsa (fake) para desbloquear la UI.** 2026-09-20.
Rama `feat/S1-T13-repositorios-falsos`, pull request **sin abrir todavía**.

Implementación completa de la capa de datos en memoria y contratos de repositorio según `ARQUITECTURA.md` y `CONTRATOS-API.md`:
- `dominio/repositorio/`:
  - 10 interfaces de dominio puras (`RepositorioAuth`, `RepositorioUsuario`, `RepositorioTrabajador`, `RepositorioServicios`, `RepositorioSolicitudes`, `RepositorioPostulaciones`, `RepositorioChat`, `RepositorioResenas`, `RepositorioCatalogos`, `RepositorioIa`) con métodos `suspend` devolviendo `Resultado<T>` y `Flow` reactivo para sesiones y mensajes. Cero dependencias de Android o Supabase.
- `dominio/modelo/ModelosRepositorio.kt`:
  - Modelos de soporte de dominio (`Sesion`, `PerfilPublicoTrabajador`, `CalificacionTrabajador`, `ServicioPublico`, `ResenaPublica`, `FiltrosBusquedaTrabajadores`, `ResumenTrabajadorBusqueda`, `DetalleSolicitud`, `ResultadoIa`).
- `datos/falso/`:
  - `FuenteDatosFalsa.kt`: Singleton en memoria con semillero coherente de `04_datos_semilla.sql` (16 categorías, 32 estados, 26 municipios, 8 trabajadores completos con servicios y fotos, 2 clientes, 5 solicitudes en estados abierta/asignada/cerrada/cancelada, 3 chats con mensajes y reseñas válidas respetando las restricciones relacionales del esquema).
  - 10 implementaciones falsas correspondientes (`Repositorio*Falso`) aplicando simulación de latencia de red (300 ms) y propiedad `errorForzado: TipoError?` para pruebas de `EstadoError`.
- `di/ModuloRepositorios.kt`:
  - Módulo de Hilt vinculando las 10 interfaces de dominio a sus implementaciones falsas con `@Binds` en `SingletonComponent`. Único archivo a modificar cuando entren las implementaciones reales.
- Pruebas unitarias:
  - 10 pruebas unitarias nuevas en `RepositoriosFalsosTest.kt` cubriendo las 10 implementaciones, validaciones de negocio, operaciones atómicas (aceptar postulación) y control de errores (44 pruebas totales en el proyecto pasando limpiamente).
  - Compilación (`./gradlew assembleDebug`), instalación y ejecución limpia en emulador Pixel 8 Pro.

**`S1-T12` — Navegación con Navigation Compose y definición del grafo de rutas.** 2026-09-20.
Rama `feat/S1-T12-navegacion-compose`, pull request **sin abrir todavía**.

Implementación completa de la arquitectura de navegación en Jetpack Compose según `ARQUITECTURA.md`, `PANTALLAS.md` y `DISENO.md`:
- `Rutas.kt`:
  - Las 19 rutas del sistema modeladas con la clase sellada `Ruta` (`P-01` a `P-19`), con identificadores únicos, títulos y argumentos fuertemente tipados (`NavType.StringType`, nulabilidad y valores por defecto).
  - Subgrafos definidos en `Subgrafo`: `Autenticacion`, `Cliente`, `Trabajador`.
  - Cero cadenas de ruta sueltas fuera de `Rutas.kt` (verificado con `grep` y pruebas unitarias).
  - Las 8 pantallas que se abren encima y no muestran barra inferior registradas en `PANTALLAS_ENCIMA`.
  - Sistema de guardas de navegación reactivo (`resolverGuarda` y `MarcadorSesionTemporal`) con control temporal de rol (Sin sesión, Cliente, Trabajador):
    - Sin sesión: cualquier ruta privada redirige a `P-02` (Iniciar sesión).
    - Con sesión Cliente: las rutas de trabajador redirigen a `P-05` (Inicio cliente).
    - Con sesión Trabajador: las rutas de cliente redirigen a `P-10` (Inicio trabajador).
- `BarraInferiorCliente.kt`: Barra inferior con exactamente 4 destinos para Cliente (`Inicio` P-05, `Solicitudes` P-09, `Chats` P-15, `Cuenta` P-18) con indicador Mostaza e iconos Carbon/Cafe.
- `BarraInferiorTrabajador.kt`: Barra inferior con exactamente 4 destinos para Trabajador (`Inicio` P-10, `Servicios` P-12, `Chats` P-15, `Cuenta` P-18).
- `GrafoNavegacion.kt`:
  - Grafo completo con tres subgrafos y pantallas compartidas.
  - Botón flotante (+) en `P-05` (Inicio cliente) que navega a `P-08` (Publicar solicitud).
  - Marcador interactivo para las 19 pantallas con información de ruta, argumentos recibidos, conmutador de sesión en tiempo real, pruebas de guardas y mapa completo de navegación.
  - Botón de regreso del sistema integrado con `Scaffold` y `BarraSuperior`.
- `MainActivity.kt`: Envoltorio limpio llamando a `GrafoNavegacion()` dentro de `DonChambitasTema`.
- 9 pruebas unitarias nuevas en `NavegacionTest.kt` (34 pruebas totales en el proyecto pasando limpiamente), compilación (`./gradlew assembleDebug`), instalación y verificación interactiva en emulador (`emulator-5554`).

**`S1-T11` — Componentes de estado: carga, vacío, error y mensajes al usuario.** 2026-09-20.
Rama `feat/S1-T11-componentes-estado`, pull request **sin abrir todavía**.

Construcción completa de los componentes de estado y el patrón de pantalla con datos según `DISENO.md` y `ARQUITECTURA.md`:
- `Estados.kt`:
  - `Cargando`: Indicador circular centrado en color Mostaza (48 dp) y mensaje opcional, con descripción semántica de accesibilidad.
  - `EstadoVacio`: Icono grande (56 dp en color Cafe), título (subtítulo en Carbon), mensaje (cuerpo en Cafe) y botón opcional (`BotonPrincipal`). Soporta valores por defecto desde `strings.xml`.
  - `EstadoError`: Icono de advertencia en color Error, título (subtítulo en Carbon), mensaje de error que dice qué hacer derivado de cada `TipoError` (o mensaje personalizado) y botón de reintentar opcional (`BotonPrincipal` con texto "Reintentar").
  - Mapeo de `TipoError` a recursos de cadenas (`obtenerMensajeErrorRes` y `obtenerTituloErrorRes`), garantizando mensajes distintos orientados a la acción para `RED`, `AUTENTICACION`, `VALIDACION`, `LIMITE_IA`, `SERVIDOR` y `DESCONOCIDO`.
- `ContenedorEstado.kt`:
  - `ContenedorEstado`: Patrón de pantalla con datos que recibe `cargando`, `error` (`TipoError?`), `vacio`, `alReintentar` y `contenido`. Utiliza `resolverEstadoVisual` para garantizar orden de precedencia estricto (cargando > error > vacío > contenido) asegurando que ningún estado se pinte encima de otro. Permite personalización total mediante slots de vista.
- `strings.xml`: Cadenas agregadas para reintentar, títulos de error y mensajes explicativos por `TipoError` centrados en la acción.
- 8 pruebas unitarias nuevas en `EstadosTest.kt` (25 pruebas totales en el proyecto pasando limpiamente), compilación (`./gradlew assembleDebug`), instalación y verificación en emulador.

**`S1-T10` — Componentes reutilizables base (botones, campos de texto, tarjetas, chips).** 2026-09-20.
Rama `feat/S1-T10-componentes-base`, pull request **sin abrir todavía**.

Construcción completa de los 14 componentes reutilizables base de la tabla de `DISENO.md` dentro de `mx.donchambitas.app.ui.componentes`:
- `Botones.kt`: `BotonPrincipal` (Mostaza con texto Carbon 7.09:1 WCAG AAA), `BotonSecundario` (contorno Mostaza Oscuro), `BotonDestacado` (Terracota con texto blanco 5.12:1 WCAG AA) y `BotonTexto` (sin fondo, texto Mostaza Oscuro). Soportan estados `habilitado` y `cargando` con indicador de progreso y bloqueo táctil.
- `Campos.kt`: `CampoTexto` (fondo Arena, contorno Borde/Mostaza Oscuro y texto de error en color Error) y `CampoContrasena` (con alternador de visibilidad e iconos de ojo).
- `Estrellas.kt`: `Estrellas` en color Terracota permitiendo media estrella en modo lectura con iconos vectoriales y descripción de accesibilidad.
- `Chips.kt`: `ChipCategoria` (Mostaza activo con texto Carbon, Arena inactivo con borde) y `EtiquetaEstado` con los cuatro colores de estado (`abierta` en Exito, `asignada` en Advertencia con texto Carbon, `cerrada` en Cafe, `cancelada` en Error con texto blanco).
- `Barras.kt`: `BarraSuperior` (fondo Mostaza, texto Carbon, flecha de regreso opcional) y `BarraInferior` (4 destinos: Inicio, Buscar, Solicitudes, Perfil con indicador en Mostaza e iconos Carbon/Cafe).
- `Tarjetas.kt`: `TarjetaTrabajador` (foto, nombre, oficio, estrellas, municipio), `TarjetaServicio` (foto, titulo, categoria, precio) y `TarjetaSolicitud` (titulo, categoria, presupuesto, estado, fecha).
- Cada componente acepta `modifier: Modifier = Modifier` como último parámetro con valor por defecto, no importa capas de repositorio ni datos, no contiene colores literales y cuenta con su `@Preview` funcional.
- 17 pruebas unitarias pasando (`./gradlew testDebugUnitTest`), compilación (`./gradlew assembleDebug`) e instalación/ejecución limpia en emulador Pixel 8 Pro.

**`S1-T09` — Sistema de diseño en Jetpack Compose (Theme, Color, Typography, Shape).** 2026-09-20.
Rama `feat/S1-T09-sistema-diseno`, pull request **sin abrir todavía**.

Implementación completa de la paleta Taller como tema de Jetpack Compose (`DonChambitasTema`) en `mx.donchambitas.app.ui.tema`.
- `Color.kt`: los 8 colores de la paleta (`Mostaza`, `MostazaOscuro`, `Terracota`, `Carbon`, `Cafe`, `Crema`, `Arena`, `Borde`) y 3 semánticos (`Exito`, `Advertencia`, `Error`), más `Blanco`. Ni una sola declaración de `Color(0xFF...)` fuera de este archivo.
- `Tema.kt`: `ColorScheme` de Material 3 con `onPrimary` mapeado obligatoriamente a `Carbon` (relación 7.09:1 WCAG AAA), `secondary` a `Terracota` con `onSecondary` en blanco, `background` en `Crema`, `surface` en `Arena` con `onSurface` en `Carbon`, `outline` en `Borde` y `error` en `Error`.
- `Tipografia.kt`: los 6 estilos tipográficos de `DISENO.md` (`titulo`, `subtitulo`, `cuerpoFuerte`, `cuerpo`, `secundario`, `pie`) con la fuente del sistema, integrados en `Typography` de Material 3 y accesibles vía propiedades de extensión.
- `Espaciado.kt`: escala base de 4 (`dp4` a `dp48`) y valores semánticos (`margenPantalla`, `separacionTarjetas`, `rellenoTarjeta`).
- `Formas.kt`: `Shapes` de Material 3 y formas de componentes (botones y campos 12 dp, tarjetas 16 dp, chips círculo, hoja inferior 20 dp).
- `MainActivity.kt`: envuelta en `DonChambitasTema`, arrancando con fondo Crema verificado en emulador.
- Pruebas unitarias en `TemaTest.kt` comprobando los contrastes obligatorios, la escala de tipografía y espaciado. Compilación (`./gradlew assembleDebug`) y pruebas (`./gradlew testDebugUnitTest`) exitosas.

**`S1-T08` — Identidad visual: paleta de colores, tipografía e iconografia.** 2026-09-20.
Rama `feat/S1-T08-identidad-visual`, pull request **sin abrir todavía**.

Cierre y documentación completa de la identidad visual de la aplicación. Tabla exhaustiva de relaciones de contraste WCAG 2.1 (Carbon sobre Mostaza 7.09:1 pasa AAA, Blanco sobre Mostaza 2.26:1 falla y queda prohibido). Diseño del logotipo vectorial legible a 48 dp en `docs/tecnico/recursos/logo.svg`. Lámina visual de la paleta Taller y reglas de aplicación en `docs/tecnico/recursos/muestra-paleta.png`. Asignación formal de los 16 iconos de oficios con Material Icons Outlined en `docs/tecnico/DISENO.md` y reemplazo total de los nombres provisionales Tabler en `basedatos/04_datos_semilla.sql`. Icono adaptativo vectorial (background y foreground con casco de seguridad en Mostaza) e iconos rasterizados en todas las densidades de mipmap (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi) para versión estándar y redonda. Probado e inspeccionado exitosamente en emulador Pixel 8 Pro (lanzador, cajón de apps y ajustes del sistema). Compilación (`./gradlew assembleDebug`) y pruebas unitarias (`./gradlew testDebugUnitTest`) exitosas.

**`S1-T07` — Diccionario de datos y modelado de entidades en Kotlin (data classes).** 2026-09-20.
Rama `feat/S1-T07-modelado-entidades`, pull request **sin abrir todavía**.

Modelado completo de las entidades del esquema relacional en Kotlin dentro de `mx.donchambitas.app.dominio.modelo` (`Usuario`, `PerfilTrabajador`, `PerfilHabilidad`, `Servicio`, `ServicioFoto`, `Solicitud`, `Postulacion`, `Conversacion`, `Mensaje`, `Resena`, `Estado`, `Municipio`, `Categoria`) y los 4 tipos enumerados exactos (`RolUsuario`, `EstadoSolicitud`, `EstadoPostulacion`, `FuncionIa`). Mapeo estricto de tipos (`UUID` a `String`, `TIMESTAMPTZ` a `Instant`, `NUMERIC(10,2)` a `BigDecimal`, `SMALLINT`/`SERIAL` a `Int`) y nulabilidad correspondiente. Sin anotaciones de serialización ni dependencias de `supabase-kt` o `android.*`. Documentado en `docs/tecnico/DICCIONARIO-DATOS.md`. Pruebas unitarias en `ModelosTest.kt`. Compilación (`./gradlew assembleDebug`), pruebas unitarias (`./gradlew testDebugUnitTest`) e instalación y ejecución en emulador Pixel 8 Pro exitosas.


- **El diagrama**, en `docs/tecnico/diagrama-er.png`, con su generador al lado.
- **El cruce de las 33 historias contra las tablas**, en `MODELO-ER.md`.
  Ninguna historia pide una tabla que no exista.
- **Las pruebas 10, 12 y 13 de `91` ahora corren con `set role authenticated`.**
  Antes corrían como `postgres`, que se salta RLS, y por eso no demostraban
  nada: con RLS apagado, a una función de trigger le puede faltar el
  `security definer` y comportarse igual de bien. Las tres exigen además de qué
  capa viene el rechazo, y las tres trajeron el mensaje de su trigger.
- **`92_usuarios_prueba.sql` y `92_prueba_rls_anon.py`**, que son el paso 2c.
  Cuatro de sus diez comprobaciones son controles a propósito: si las tablas
  estuvieran rotas del todo, todo volvería vacío y las otras seis pasarían sin
  que nada funcionara.

**Dos cosas que conviene saber antes de tocar esto:**

- `ia_cache` devuelve `HTTP 403`, no una lista vacía como pedía el ticket. Es
  el `revoke all` quitando el permiso antes de que RLS entre a filtrar. Más
  estricto, no menos.
- **El dominio `@prueba.donchambitas.mx` no sirve para el alta por la API.**
  Supabase valida que el dominio exista y ese es ficticio. `91` nunca se topó
  con ello porque inserta directo en `auth.users`. Le va a tocar a `S2-T07`.

**Las cuentas de prueba siguen en el proyecto.** Borrarlas necesita la
`service_role`, que no entra al repositorio:

```sql
delete from auth.users where email like '%@prueba.donchambitas.mx';
```

## Cuatro huecos que esperan al líder

Salen del cruce contra las historias y de revisar el paso 2 del ticket.
**Ninguno se corrigió**: el esquema no se rediseña en `S1-T03`. Están
explicados al final de `MODELO-ER.md`.

- **`H-05` — La búsqueda por texto no ignora los acentos.** HU-17 promete que
  "plomeria" encuentre "plomería". `pg_trgm` acelera el `ilike`, no cambia lo
  que el `ilike` considera igual, y `unaccent` no está instalado; el contrato
  de `buscarTrabajadores` busca justamente con `ilike`. Es el más serio y
  **conviene cerrarlo antes de `S4-T04`**.
- **`H-06` — Nada borra el archivo de Storage cuando desaparece su fila.** La
  cascada se lleva la URL antes de que nadie pueda usarla. Le toca a `S3-T08`
  y `S2-T12` escribir el orden de operaciones.
- **`H-07` — "Postulaciones sin revisar" no existe como dato.** `enviada` es
  "no decidida", no "no vista". Probablemente sea la redacción de HU-13.
- **`H-08` — Uno de los cinco triggers del paso 2 no se puede probar, y eso
  toca un criterio de aceptación de este ticket.** "Postularse a tu propia
  solicitud" exigiría un uuid que fuera cliente y trabajador a la vez, y los
  roles excluyentes lo impiden antes de que el trigger opine. Los otros cuatro
  están demostrados. **Este es el único punto por el que `S1-T03` podría
  devolverse:** o el criterio baja a cuatro triggers, o se anota que al quinto
  lo sostiene el esquema.

## Siguiente en la cola

`S2-T06` — Contrato de la API de autenticación (endpoints, payloads y errores)
(prioridad 800, sprint 2, sin dependencias)

**No tiene ticket**: hay que redactarlo antes de tomarla. Recoge dos
consecuencias ya decididas: `DEC-25` (`registrar` devuelve `Sesion`) y
`DEC-27` (el `redirectTo` de recuperación).

## Los dos huecos de S2-T01, ya cerrados

Salían de `S2-T01`, de cruzar HU-01 y HU-04 contra `CONTRATOS-API.md`. Están
explicados al final de `docs/producto/DISENO-AUTENTICACION.md`. **Al 2026-09-22
no queda ninguno abierto.**

- **`H-09` — CERRADO el 2026-09-22 por `DEC-27`.** El enlace de recuperación
  abre la aplicación por *deep link*, canjea el token por sesión y aterriza en
  **P-18**, que ya cambia la contraseña por HU-05. **No se agrega pantalla:
  siguen siendo 19 y `DEC-10` queda intacto.** Al investigarlo se cayó la
  premisa del hallazgo: la "página alojada de Supabase" no existe —Supabase no
  hospeda formulario de contraseña nueva, la pantalla la pone uno— y una web
  propia la descarta `DEC-02`. Lo recoge `S2-T06`, lo implementa `S2-T07` y
  `S2-T11` especifica la llegada a P-18.
- **`H-10` — CERRADO el 2026-09-22 por `DEC-25`.** El registro deja sesión
  abierta: el usuario entra directo a la pantalla de su rol y la sesión vive
  hasta que él la cierre. Dos consecuencias que `S2-T06` y `S2-T07` tienen que
  respetar: la confirmación por correo de Supabase Auth **queda desactivada**,
  porque con ella activa `signUp` no abre sesión; y `registrar` pasa a devolver
  `Sesion` en vez de `Usuario`, así que `CONTRATOS-API.md` y `RepositorioAuth`
  cambian en `S2-T06`.


## Decisiones recientes

**2026-09-16 · La base se volvió a levantar y a verificar.** `01` a `04` sin
error, `90_verificacion.sql` **42 de 42** y `91_prueba_funcional.sql`
**25 de 25**, ya con las pruebas 10, 12 y 13 corriendo con RLS activo. Las tres
pasaron por la razón correcta.

**2026-09-16 · `S1-T03` terminada.** Diagrama, cruce de las 33 historias, las
tres pruebas de `91` corriendo por fin con RLS activo, y el paso 2c cerrado con
la `anon key` contra PostgREST. Salieron cuatro huecos —`H-05` a `H-08`— y
ninguno se corrigió: son del líder. `H-08` toca un criterio de aceptación de
este mismo ticket.

**2026-09-15 · Dos decisiones nuevas: `DEC-23` y `DEC-24`.** Salen de los
hallazgos que dejó `S1-T02`. La primera amplía los filtros de búsqueda de tres
a cinco —se agregan estado y municipio— y con eso cambia `PRODUCTO.md`, el
renglón de P-06 y el título de `S4-T05`. La segunda pone por escrito la regla
de unicidad del hilo de chat que el esquema ya aplicaba, y que HU-24 no
describía para el caso de P-07.

Los otros dos hallazgos no necesitaron número: H-02 era la descripción de P-06,
que había quedado corta frente a HU-18, y H-03 se cerró aceptando que las
notificaciones locales sigan cubiertas por los criterios de HU-22 y HU-26 en
vez de tener historia propia. Los cuatro están explicados al final de
`PANTALLAS.md`.

**2026-09-15 · La base de datos está levantada y verificada.** Reinstalación
desde cero contra el proyecto de Supabase: `00_reinicio.sql` dejó todo en cero
—las once comprobaciones en `OK`, cubetas incluidas— y `01` a `04` corrieron sin
un solo error. `90_verificacion.sql` dio **42 de 42** y `91_prueba_funcional.sql`
**25 de 25**.

Falta lo que ningún script puede hacer solo, y es lo que cierra `S1-T03`:
probar RLS con la `anon key` y dos sesiones reales, mover las pruebas 10, 12 y
13 de `91` al bloque de `set role authenticated` (hoy corren como `postgres` y
pasan siempre), el diagrama ER y el cruce contra las historias de `S1-T01`.

**2026-09-15 · Revisión de `basedatos/` antes de volver a levantar el esquema.**
Se encontraron cinco defectos que `91_prueba_funcional.sql` **no detecta**,
porque corre como `postgres` y se salta RLS: cinco funciones de trigger sin
`security definer` que dejaban sus reglas sin aplicar en silencio, la vista de
búsqueda sin con qué filtrar por categoría, `fn_ia_registrar_llamada`
ejecutable con la `anon key`, permisos de `update` más anchos que el contrato,
y el semillero de categorías sin poder reconciliar los iconos de S1-T08. Los
cinco están corregidos en los archivos; `90_verificacion.sql` pasó de 36 a 42
comprobaciones y las seis nuevas son las que vigilan justo eso.

`00_reinicio.sql` ahora borra también los archivos y las dos cubetas de
Storage: el reinicio es total.

**2026-09-15 · Tres decisiones tomadas: `DEC-19`, `DEC-20` y `DEC-21`.** El
correo del trabajador es visible para cualquier usuario con sesión, el chat lo
abre siempre el cliente, y la atomicidad al aceptar una postulación es regla
del contrato y no de la base. Las tres están explicadas en `MODELO-ER.md`, en
"Lo que las políticas NO impiden, a propósito". `DEC-22` solo pone número a
algo que ya estaba decidido: los roles no se cambian.

**2026-09-14 · PEND-01 resuelto: el backend es Supabase.** Ver `DEC-16` y
`DEC-17`. Las seis tareas que estaban `bloqueada` pasaron a `pendiente` y ya no
queda ninguna tarea bloqueada en toda la cola. El cliente es `supabase-kt`:
Retrofit y OkHttp salieron del stack.

Esto **no** cambia cómo se trabaja hoy. La implementación activa sigue siendo
`FuenteDatosFalsa` hasta que cada tarea real llegue en su turno.

---

## Cómo se llena

Al **tomar** una tarea: llena "Tarea en curso" y pon la tarea en `en curso`
dentro de `docs/tareas/INDICE.md`.

Al **terminarla**: mueve la tarea a "Última tarea terminada", vacía "Tarea en
curso", recalcula el contador del sprint y actualiza "Siguiente en la cola".

Si dos personas van a trabajar el mismo día, la segunda revisa este archivo
**antes** de pedirle nada al agente. Si dice que hay una tarea en curso, esa
tarea no se toca.
