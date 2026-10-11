# Estado actual

> Archivo **vivo**. Quien termina una tarea lo actualiza. Es la primera cosa
> que lee el agente y la única fuente confiable sobre qué está pasando hoy.

**Última actualización:** 2026-10-11

---

## Sprint en curso

| Campo | Valor |
|---|---|
| Sprint | 2 |
| Fechas | PENDIENTE |
| Tareas del sprint | 16 |
| Terminadas | 11 |
| En curso | 0 |
| Bloqueadas | 0 |

> Sprint 1 cerrado el 2026-09-20 con sus 16 tareas en `hecha`.

## Qué se puede probar hoy en la aplicación

Léelo antes de instalar el APK y reportar que algo "no funciona". Desde que
`S2-T07` se integró en `main`, `RepositorioAuth` está enlazado a
`RepositorioAuthReal`: P-02, P-03 y P-04 hablan con Supabase Auth, y **las
cuentas sembradas de `FuenteDatosFalsa` ya no sirven para entrar**. Las filas
de P-04 describen la rama de `S2-T10` mientras no se integre.

| Si haces esto | Pasa esto hoy | Lo arregla |
|---|---|---|
| Abres la aplicación **sin sesión guardada** | P-01 dura al menos 800 ms y te deja en P-02 | — |
| Abres la aplicación **con sesión guardada** | P-01 y luego P-05 o P-10 según tu rol, sin pasar por P-02. Atrás cierra la aplicación. Probado con `ana.rls@…` y `beto.rls@…` | — |
| Abres la aplicación con sesión guardada y **sin red** | A los 8 segundos, "Sin conexión" con "Reintentar" en P-01. La sesión no se borra, y con la red de vuelta "Reintentar" entra | — |
| Pulsas "Cerrar sesión (P-02)" en el marcador de P-18 | Cierra la sesión real: vas a P-02, atrás cierra la aplicación y al reabrir sigues en P-02 | — |
| Pulsas "Cerrar sesión (P-02)" **sin red** | No pasa nada: sigues en P-18 y la sesión sigue guardada. Todavía no hay mensaje | `S2-T11` |
| Estás en P-02 (iniciar sesión) | La pantalla real: marca, los dos campos y los enlaces a P-03 y P-04 | — |
| Pulsas **Iniciar sesión** o **Crear cuenta** **sin las llaves** en `local.properties` | "Algo salió mal" con "Reintentar". No se cierra la aplicación | Poner `SUPABASE_URL` y `SUPABASE_ANON_KEY` |
| Pulsas **Iniciar sesión** con una cuenta real del proyecto | Entra a P-05 o P-10 según su rol en `public.usuarios`. Probado con las cuentas de `92_usuarios_prueba.sql`: `ana.rls@…` entra a P-05 y `beto.rls@…` a P-10 | — |
| Pulsas **Iniciar sesión** con credenciales malas | "Correo o contraseña incorrectos", sin "Reintentar". Probado contra Supabase | — |
| Pulsas **Iniciar sesión** sin red | "Sin conexión" con "Reintentar". Con la red de vuelta, "Reintentar" entra | — |
| Abres `mx.donchambitas.app://auth#error=…` (enlace vencido) | La aplicación abre en P-02 y no se cierra, en frío y con la aplicación abierta | — |
| Entras a P-03 desde el marcador de P-02 | La pantalla real de registro, con sus cinco campos y el selector de rol | — |
| Confirmas el registro **sin elegir rol** | Te reclama el rol y no hace nada más | — |
| Escribes un correo sin arroba y sales del campo | Sale "Ese correo no se ve bien, revísalo" debajo. Se borra en cuanto vuelves a escribir | — |
| Pulsas el botón de P-02 o P-03 con campos mal | Marca **todos** los que fallan, no avanza y deja el foco en el primero | — |
| Entras en P-02 con una contraseña de un carácter y una cuenta que existe | **Entra.** Al iniciar sesión solo se exige que no esté vacía (5.2); el mínimo de 8 es del registro | — |
| Confirmas el registro **con rol** | Crea la cuenta en Supabase Auth y la ficha en `public.usuarios`, y te manda a P-05 o P-10 | — |
| Te registras con un correo que ya existe | "Correo ya registrado / El correo ya está registrado, inicia sesión", sin "Reintentar" | — |
| Tocas "¿Olvidaste tu contraseña?" en P-02 | P-04 real: explicación, correo, "Enviar enlace" y "Volver a iniciar sesión", con una sola barra | — |
| Pides el enlace con cualquier correo bien escrito | "Revisa tu correo", sin repetir el correo, **exista o no la cuenta** y aunque su correo no pueda recibir | — |
| Pides el enlace sin red | "Sin conexión" con "Reintentar", y el correo sigue escrito | — |
| Esperas el correo en una cuenta que no es del equipo | **No llega.** El SMTP de fábrica de Supabase solo entrega a los miembros del equipo de la organización, y muy pocos por hora | `S6-T13` |
| Abres el enlace del correo | La aplicación abre y no se cierra. Todavía no te lleva a P-18 | `S2-T11` |
| Cambias de pantalla con los botones "Sin sesión / Cliente / Trabajador" de los marcadores | Las guardas y las barras todavía leen el marcador temporal, no la sesión real. P-01 y el cierre de sesión lo mantienen al día | `S2-T15` |

**El alta ya es real** desde `S2-T07`: la cuenta vive en Supabase Auth y en
`public.usuarios`, y sobrevive al reinicio. Por `DEC-25` el registro deja
sesión abierta, con la confirmación por correo desactivada en la consola.

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

**`S2-T12` — Contrato de la API de perfil de usuario (y `RepositorioUsuario` real).** 2026-10-11.
Rama `feat/S2-T12-repositorio-usuario-real`, **apilada sobre la de `S2-T09`**, pull request **sin abrir todavía**: se integra después del #15. Ticket redactado por el agente el mismo día; el líder eligió la opción A en las tres decisiones.

- **`datos/repositorio/RepositorioUsuarioReal.kt`** (`DEC-31`), ya enlazado en `ModuloRepositorios`. Los otros ocho repositorios siguen falsos.
  - `obtenerMiUsuario()` lee la ficha propia de `public.usuarios`.
  - `actualizarMiUsuario(...)` manda solo `nombre`, `apellidos` y `telefono`, y devuelve la fila ya actualizada sin una segunda lectura.
  - Sin sesión, o si vuelven cero filas, devuelve `AUTENTICACION`: RLS no rechaza, filtra, y PostgREST contesta `200` con una lista vacía.
  - `subirFotoPerfil` devuelve `SERVIDOR` hasta `S2-T14`.
- **`datos/remoto/supabase/ErroresDatos.kt`:** `traducirErrorDatos`, por código HTTP. `supabase-kt` 3.0.3 no deja pasar el código de Postgres.
- **`CONTRATOS-API.md`:** la sección "Usuario" a detalle, con las llamadas exactas, la tabla de errores y la regla de que de la `Sesion` solo se leen el usuario y su rol.
- **En pantalla no cambia nada.** Ninguna pantalla llama todavía a `RepositorioUsuario`; P-18 es `S2-T11`.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 180 pruebas unitarias (11 nuevas) y 59 instrumentadas (5 nuevas), 0 fallas.
  - **Contra el proyecto de Supabase**, en emulador `Pixel_10`, con `RepositorioUsuarioRealTest`: lee la ficha de `ana.rls@…`, la edita y la vuelve a leer, borra el teléfono con `null`, un nombre de 81 caracteres da `VALIDACION` y sin sesión da `AUTENTICACION`. La ficha quedó como estaba.
  - **Sin llaves** en `local.properties`: la aplicación abre y llega a P-02, y los 5 casos nuevos salen omitidos.
  - Con llaves, `ana.rls@…` entra a P-05 y al reabrir sigue en P-05. Sin excepciones en logcat.
- **Lo que NO se probó en el dispositivo:** que sin red devuelva `RED`. Lo cubre `ErroresDatosTest`.
- **`RepositorioUsuarioRealTest` es la primera prueba de la suite que necesita red** y un proyecto de Supabase vivo con las cuentas de `92_usuarios_prueba.sql`. Sin llaves se omite; con llaves y sin esas cuentas, falla.
- **Dos hallazgos para el líder**, al final de `CONTRATOS-API.md`:
  - **`H-13` · El `mensaje` de los errores de autenticación lleva el token.** `ErroresAuth.kt` guarda `error.message`, que en `supabase-kt` 3.0.3 incluye las cabeceras de la petición. Hoy nadie lo pinta ni lo registra. El código nuevo no lo repite.
  - **`H-14` · `public.usuarios` acepta un nombre vacío.** No hay `CHECK` sobre nombre, apellidos ni teléfono; solo lo impide la validación de la aplicación.

**`S2-T09` — Manejo de sesión: inicio automático, cierre de sesión y expiración.** 2026-10-11.
Rama `feat/S2-T09-manejo-sesion`, pull request **#15**. Ticket redactado por el agente el 2026-10-08; el líder eligió la opción A en las dos decisiones.

- **`SplashViewModel`** lee el primer valor de `RepositorioAuth.sesionActual()`: sin sesión va a P-02, cliente a P-05 y trabajador a P-10. El mínimo de 800 ms corre a la par de la lectura y se mide con `delay`. Si en 8 segundos no llega nada, `error = TipoError.RED`; si el flujo lanza (sin llaves), P-02.
- **`SplashPantalla`:** con error, el bloque de abajo muestra "Sin conexión" y "Reintentar". Sin cadenas nuevas.
- **`ui/navegacion/SesionViewModel.kt`**, uno por actividad: escucha `sesionActual()` y avisa cuando no hay sesión; `cerrarSesion()` llama al repositorio y no navega.
- **`GrafoNavegacion`:** al terminar la sesión deja el marcador en `SIN_SESION` y va a P-02 con la pila limpia. P-01 deja el marcador con el rol de su destino. El botón "Cerrar sesión (P-02)" del marcador de P-18 cierra la sesión real.
- `CONTRATOS-API.md` al día en "`sesionActual()`".
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 169 pruebas unitarias (10 nuevas) y 54 instrumentadas, 0 fallas. `SplashViewModelTest` pasó diez corridas seguidas y ya no usa `System.currentTimeMillis`.
  - **Contra el proyecto de Supabase**, en emulador `Pixel_10`, el 2026-10-11:
    - sin sesión guardada, abre en P-02;
    - con la sesión de `ana.rls@…`, `force-stop` y abrir lleva a P-05 sin pasar por P-02; con `beto.rls@…`, a P-10. En los dos, atrás cierra la aplicación;
    - en modo avión, P-01 muestra "Sin conexión" con "Reintentar" y `sesion_cifrada.preferences_pb` sigue igual; con la red de vuelta, "Reintentar" entra a P-05;
    - "Cerrar sesión (P-02)" lleva a P-02, `sesion_cifrada.preferences_pb` queda en 0 bytes, atrás cierra la aplicación y al reabrir sigue en P-02;
    - "Cerrar sesión (P-02)" en modo avión deja al usuario en P-18 con la sesión guardada;
    - **migración al abrir:** con una sesión en claro puesta a mano en `shared_prefs` y sin `sesion_cifrada`, abrir la aplicación entra directo a P-05, la llave en claro desaparece y la sesión queda cifrada, sin tocar ningún botón.
  - Sin excepciones en logcat.
- **Lo que NO se probó en el dispositivo: la expiración.** Hace falta revocar la sesión desde la consola y bajar el *JWT expiry*, y el emulador no deja adelantar el reloj sin root. El ticket lo prevé: el caso queda cubierto en `SesionViewModelTest`, y el camino de `GrafoNavegacion` que lleva a P-02 es el mismo que se probó con el cierre a mano.
- **Sin llaves** en `local.properties` se probó el 2026-10-08: abre y llega a P-02 sin cerrarse. No se repitió el 2026-10-11.
- **Un desvío del ticket**, anotado en él: `SesionViewModel` avisa en cada `null`, también en el del arranque, y `GrafoNavegacion` decide si había alguien adentro mirando el marcador.
- **La confirmación por correo estuvo activada y ya se corrigió.** El 2026-10-11 `GET /auth/v1/settings` devolvía `mailer_autoconfirm: false`, contra `DEC-25` (el 2026-09-27 daba `true`). El líder la desactivó ese mismo día en la consola y se verificó que volvió a `true`. No se probó un alta nueva desde P-03 después del cambio.
- **Las cuatro cuentas `*.rls@prueba.donchambitas.mx` se volvieron a crear** el 2026-10-11 y siguen en el proyecto.

**`S2-T08` — Almacenamiento seguro de la sesión y el token.** 2026-10-08.
Rama `feat/S2-T08-almacenamiento-seguro-sesion`, pull request **#14**, integrado en `main`. Ticket redactado por el agente el mismo día; el líder eligió la opción A.

- **`datos/local/CifradorSesion.kt`:** AES-256-GCM con una llave que se genera dentro del Android Keystore y nunca sale de ahí. IV nuevo en cada cifrado. Lo que no se puede descifrar devuelve `null`, nunca lanza. Sin dependencia nueva.
- **`datos/local/AlmacenSesionCifrada.kt`:** el `SessionManager` de `supabase-kt`, sobre el DataStore `sesion_cifrada`. Sustituye al `SettingsSessionManager`, que guardaba la sesión **en texto plano** en `shared_prefs/mx.donchambitas.app_preferences.xml`.
  - Migra una sola vez la sesión en claro de antes y la borra, así que nadie tiene que volver a entrar.
  - Un dato indescifrable, ilegible o un archivo dañado cuenta como "sin sesión".
- **`ModuloSupabase`** le pasa ese almacén a `install(Auth)`.
- **Respaldo:** la sesión cifrada y las `SharedPreferences` por omisión quedan fuera de la copia de seguridad de Google y de la transferencia entre dispositivos. Antes la sesión en claro sí se respaldaba.
- **`Sesion.tokenAcceso` ya no existe** (`H-12`, `DEC-30`).
- `CONTRATOS-API.md` y `ARQUITECTURA.md` al día.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 159 pruebas unitarias (9 nuevas) y 54 instrumentadas (5 nuevas, el cifrador contra el Keystore real), 0 fallas.
  - **En emulador `Medium_Phone`, instalando encima de la versión de `main` con sesión abierta:**
    - antes, el token estaba en claro en `shared_prefs`;
    - después de la primera acción de Auth, la llave en claro desapareció y `sesion_cifrada` tiene la sesión sin `access_token`, sin JWT y sin el correo en claro;
    - tras cerrar y abrir, "Successfully loaded session from storage";
    - un inicio de sesión nuevo se guarda cifrado;
    - con el archivo dañado a mano, Auth da "No session found" y la aplicación no se cierra.
- **Cuatro desvíos del ticket**, anotados en él:
  - la migración ocurre con la primera acción de Auth, no al abrir: el cliente es perezoso desde `S2-T07` y P-01 aún no lee la sesión. Con `S2-T09` pasará al abrir;
  - también se tocaron `FuenteDatosFalsa.kt` y `RepositorioAuthFalso.kt`;
  - se agregó un manejador de corrupción al DataStore;
  - que `cerrarSesion()` borre la sesión cifrada se probó en la JVM, porque todavía no hay botón de cerrar sesión (`S2-T11`).

**`S2-T10` — Pantalla de recuperación de contraseña.** 2026-10-08.
Rama `feat/S2-T10-pantalla-recuperar-contrasena`, pull request **#13**, integrado en `main`. La trabajó el líder con el agente; el responsable nominal es LMM.

- `ui/pantallas/EstadoRecuperarContrasena.kt`, `RecuperarContrasenaViewModel.kt` y `RecuperarContrasenaPantalla.kt`, conforme a la sección 4 de `DISENO-AUTENTICACION.md`: formulario y confirmación en la misma pantalla, el correo normalizado solo al enviar, "Reintentar" solo en `RED`, `SERVIDOR` y `DESCONOCIDO`, y `Done` del teclado envía.
- `GrafoNavegacion.kt`: P-04 sustituye su marcador, regresa con `popBackStack` y entra a `RUTAS_SIN_BARRA_DEL_ANDAMIO`.
- `strings.xml`: las ocho cadenas de la sección 7.
- **Vigencia del enlace: una hora.** El líder leyó *Email OTP Expiration* en la consola, **3600 segundos**, y decidió no cambiarlo. `recuperar_vigencia` dice "El enlace vence en 1 hora", y `DISENO-AUTENTICACION.md` quedó igual.
- **Corrección de seguridad fuera del ticket, por instrucción del líder.** Con una cuenta de `@prueba.donchambitas.mx`, que no recibe correo, Supabase respondía `400 email_address_invalid`; con un correo sin cuenta, `200`. P-04 pintaba "Datos incorrectos" en el primero y la confirmación en el segundo, así que delataba qué correos tienen cuenta. Ahora `recuperarContrasena` reporta como éxito `email_address_invalid` y `email_address_not_authorized`, como ya hacía con los límites de envío. Toca `ErroresAuth.kt` (`esLimiteDeEnvio` pasa a `esRechazoQueDelataLaCuenta`), `RepositorioAuthReal.kt`, su prueba y `CONTRATOS-API.md`.
  - **Lo que queda abierto:** si el envío falla con `500` (por ejemplo, un SMTP propio mal configurado), sigue siendo `SERVIDOR`, y eso también solo pasa con cuentas que existen. No se tocó: tratar todo `5xx` como éxito escondería las caídas reales. Lo decide el líder si algún día hay SMTP propio.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 150 pruebas unitarias (14 nuevas) y 49 instrumentadas (10 nuevas), 0 fallas.
  - **Contra el proyecto de Supabase**, en emulador `Medium_Phone`:
    - vacío marca el campo y le deja el foco;
    - `  Ana.RLS@…` y `nadie@…` dan la misma confirmación, sin el correo;
    - sin red sale "Sin conexión" con el correo escrito, y "Reintentar" funciona con la red de vuelta;
    - girar no pierde ni el correo ni la vista;
    - atrás regresa a P-02 y el segundo atrás cierra la aplicación;
    - **un enlace real llegó** al correo del líder y, abierto en el emulador, la aplicación abrió sin cerrarse.
- **Cuenta creada para la prueba:** la del correo del líder, como cliente, nombre "Lider Prueba S2-T10". La contraseña la tiene el líder.
- **Por qué no llegaba el correo al principio:** esa dirección no tenía cuenta, y sin cuenta Supabase no envía nada.
- **Dos hallazgos para el líder:**
  - **El SMTP de fábrica de Supabase solo entrega a los miembros del equipo de la organización**, y muy pocos correos por hora. Un usuario real no recibe el enlace hasta que se configure un SMTP propio en la consola. El líder lo registró el 2026-10-08 como `S6-T13`.
  - **Enter de teclado físico** en P-04 envía la petición y además regresa a P-02, con lo que la petición se cancela. Es el mismo defecto que anotó `S2-T05` en P-03, y probablemente esté en P-02. La tecla de acción del teclado en pantalla funciona bien. No se tocó: arreglarlo solo en P-04 dejaría tres pantallas distintas.

**`S2-T07` — Implementación real de autenticación con Supabase Auth.** 2026-09-27.
Rama `feat/S2-T07-autenticacion-supabase-v2` en el remoto, **apilada sobre la de `S2-T06`**, que a su vez va sobre la de `S2-T05`. Pull request **#14**: se integra después de #12 y #13.

- `datos/repositorio/RepositorioAuthReal.kt` implementa la sección de autenticación de `CONTRATOS-API.md` y ya está enlazado en `ModuloRepositorios`. Los otros nueve repositorios siguen falsos.
- `di/ModuloSupabase.kt` provee el cliente con la URL y la `anon key` de `local.properties`. `RepositorioAuthReal` y `MainActivity` lo piden perezoso: **sin llaves, la aplicación abre y el botón muestra "Algo salió mal"** en vez de cerrarse.
- `datos/remoto/`:
  - `UsuarioDto`;
  - `ErroresAuth`, una función pura con una prueba por fila de la tabla del contrato;
  - `EnlaceAuth`;
  - `ConfiguracionSupabase`.
- **`DEC-28` (cierra `H-11`):** `TipoError.CORREO_DUPLICADO` con su título y su mensaje en `strings.xml`. P-03 pierde `mensajePantalla` y pinta todo error desde `strings.xml`.
- El `intent-filter` de `mx.donchambitas.app://auth` y `launchMode="singleTop"` en `MainActivity`.
- **Desvío del contrato, ya reflejado en él:** no se usa `handleDeeplinks`. En 3.0.3 lee el usuario en un scope sin manejador de errores, y abrir el enlace sin red cerraba la aplicación. `EnlaceAuth.kt` hace lo mismo dentro de un `try`.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 136 pruebas unitarias (25 nuevas) y 39 instrumentadas, 0 fallas.
  - **Contra el proyecto de Supabase**, en emulador `Medium_Phone`:
    - un alta de trabajador entra a P-10, y su fila de `public.usuarios` trae nombre, apellidos, teléfono y rol correctos;
    - la misma cuenta entra otra vez después de cerrar la aplicación;
    - el correo repetido da `CORREO_DUPLICADO`;
    - `ana.rls@…` entra a P-05 y `beto.rls@…` a P-10;
    - una contraseña mala da "Correo o contraseña incorrectos";
    - sin red sale "Sin conexión", y "Reintentar" funciona;
    - un enlace vencido no cierra la aplicación, ni en frío ni con la aplicación abierta.
  - Sin excepciones en logcat.
- **Consola:** el líder desactivó la confirmación por correo el 2026-09-27, verificado con `GET /auth/v1/settings` (`mailer_autoconfirm: true`). El mínimo de contraseña en 8 y la URL de redirección los aplicó el líder, pero **no se pueden verificar desde fuera**.
- **Cuenta de prueba creada:** `s2t07.trabajador@prueba.donchambitas.mx`. Se borra con el mismo `delete` de las de `S1-T03`, que está más abajo en este archivo.
- **`@prueba.donchambitas.mx` sí pasa el alta con la confirmación desactivada**, así que lo que `DEC-25` suponía se confirmó.
- **Lo que esta tarea NO trae:**
  - aterrizar en P-18 al llegar por recuperación, que es `S2-T11`;
  - que P-01 y las guardas lean la sesión real: al reabrir, la sesión existe, pero P-01 manda a P-02. Son `S2-T09` y `S2-T15`.
- **Riesgo para `S2-T11`, pendiente del líder:** con la autenticación real, los repositorios falsos ya no ven al usuario con sesión, y P-18 lee su perfil con `RepositorioUsuario`, que no tiene implementación real en el Sprint 2. Detalle en el ticket.

**`S2-T06` — Contrato de la API de autenticación (endpoints, payloads y errores).** 2026-09-27.
Rama `docs/S2-T06-contrato-api-autenticacion-v2` en el remoto, **apilada sobre la de `S2-T05`** por decisión del líder. Pull request **#13**: se integra después de #12.

Ticket `docs/tareas/S2-T06.md`, redactado por el agente el mismo día por instrucción del líder.

- `docs/tecnico/CONTRATOS-API.md`, sección de autenticación reescrita a detalle. **Se verificó contra el código fuente de `supabase-kt` 3.0.3**, la versión de `libs.versions.toml`, sacado de la caché de Gradle; no de memoria.
  - **Cada operación** tiene su llamada exacta, lo que devuelve y de dónde sale cada campo.
  - **La `Sesion` se arma en dos pasos:** primero Auth y luego la ficha de `public.usuarios`, con su `UsuarioDto` columna por columna.
  - **Las llaves del metadata de `registrar`** están cotejadas con las líneas 172 a 175 del esquema.
  - **El enlace de la aplicación es `mx.donchambitas.app://auth`**, con flujo `IMPLICIT`. Con `PKCE`, el `type=recovery` se pierde y la aplicación no sabría que tiene que ir a P-18. Está el recorrido de la recuperación en seis pasos, con la tarea que hace cada uno.
  - **`sesionActual()` tiene su tabla de `SessionStatus`.** `RefreshFailure` no cierra la sesión, porque en 3.0.3 solo marca fallas pasajeras.
  - **Hay una tabla de errores** con los códigos de `AuthErrorCode`, y la lista de ajustes que `S2-T07` aplica en la consola.
- `RepositorioAuth.registrar` devuelve `Resultado<Sesion>` (`DEC-25`). `RepositorioAuthFalso` devuelve la sesión que abre, y sus mensajes ya llevan acentos. `RegistroViewModel` lee el rol de `sesion.usuario`.
- `DISENO-AUTENTICACION.md`: la tabla 3.5 dice `Exito(Sesion)` y marca retirada la fila de "sin sesión".
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 111 pruebas unitarias y 39 instrumentadas, 0 fallas.
  - Recorrido a mano en emulador `Medium_Phone`: un alta nueva entra a P-05, y el correo duplicado muestra "El correo ya está registrado, inicia sesión" con acentos y sin "Reintentar". Sin excepciones en logcat.
- **Tres cosas que el contrato encontró en la biblioteca, para que `S2-T07` no las descubra tarde:**
  - Sin red, `signOut` **no** cierra la sesión local.
  - Un enlace de recuperación vencido hace que `handleDeeplinks` lance una excepción que nadie atrapa: `S2-T07` tiene que revisar el fragmento antes.
  - Si el alta sale bien y falla la lectura de la ficha, reintentar da correo duplicado.
- **Dos hallazgos nuevos para el líder**, al final de `CONTRATOS-API.md`:
  - **`H-11` · De dónde sale el mensaje en español de un error de `VALIDACION` en el registro.** Supabase Auth rechaza el correo duplicado en inglés, y `datos/` no puede leer `strings.xml`. Hay tres salidas; la recomendada es un `TipoError` nuevo. **`S2-T07` no puede cerrar el alta real sin esta decisión.**
  - **`H-12` · `Sesion.tokenAcceso` sobra.** Nadie lo lee y el token lo lleva `supabase-kt`.
- **Discrepancia de versión:** `BITACORA.md`, en la fila de `S1-T04`, dice `supabase-kt` 3.1.1, pero `libs.versions.toml` fija 3.0.3. No se tocó esa fila porque es de otra tarea.

**`S2-T05` — ViewModels y estados de UI del flujo de autenticación.** 2026-09-27.
Rama `feat/S2-T05-viewmodels-autenticacion`, pull request **#12**.

Ticket `docs/tareas/S2-T05.md`, redactado por el agente como los de `S2-T03` y
`S2-T04`. El alcance salió de las secciones 1.4 a 1.6, 2.2 a 2.4, 3.3 a 3.5 y 6
de `docs/producto/DISENO-AUTENTICACION.md`.

- `ui/pantallas/IniciarSesionViewModel.kt` y `RegistroViewModel.kt`: `@HiltViewModel` contra la interfaz `RepositorioAuth`, un solo `StateFlow` cada uno.
  - La validación de `S2-T04` se mudó de la pantalla sin cambiar: mismas reglas, mismo "campo tocado", mismos topes y filtro del teléfono.
  - Al enviar se normaliza según 1.6: el correo con `trim().lowercase()`, y el nombre y los apellidos con `trim()`.
  - El destino, P-05 o P-10, sale del rol del usuario que devuelve el repositorio.
  - Errores del repositorio en `errorPantalla`. En P-03, un error de `VALIDACION` trae además `mensajePantalla` tal cual.
  - Mientras carga, un segundo toque no hace una segunda llamada. `alReintentar` repite el envío.
- `IniciarSesionPantalla.kt` y `RegistroPantalla.kt` solo conectan el ViewModel con su `...Contenido`, que no cambió. Se quitaron los `rememberSaveable`: lo capturado sobrevive al giro porque vive en el ViewModel.
- `EstadoRegistro.kt`: gana `destino`, que le faltaba respecto a 3.3.
- `GrafoNavegacion.kt`: `entrarConSesion` marca el `MarcadorSesionTemporal` según el destino y limpia la pila del subgrafo, para P-02 y P-03 por igual. El marcador sigue ahí hasta `S2-T15`.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 111 pruebas unitarias, 0 fallas: 25 nuevas en `IniciarSesionViewModelTest` y `RegistroViewModelTest`. En esta corrida pasaron también las 2 intermitentes de `SplashViewModelTest`.
  - 39 pruebas instrumentadas, 0 fallas. Las de P-02 y P-03 ahora montan la pantalla con su ViewModel sobre `RepositorioAuthFalso` sin retraso; 4 son nuevas: cliente, trabajador, cuenta inexistente y correo duplicado.
  - Recorrido a mano en emulador `Medium_Phone` (Android 17):
    - La cuenta inexistente muestra el error de credenciales sin "Reintentar".
    - `Pedro.Plomero@ejemplo.com` entra a P-10, y el botón atrás cierra la aplicación.
    - El correo duplicado en P-03 muestra el mensaje del repositorio.
    - Un trabajador nuevo entra a P-10. Tras volver a "Sin sesión", la misma cuenta entra desde P-02.
    - El correo escrito se conserva al girar. Sin excepciones en logcat.
- **Tres desvíos del documento, para que el líder los revise.**
  - **El ViewModel de P-04 (4.3) no entra**, aunque la sección 9 se lo asigna a esta tarea: P-04 no existe hasta `S2-T10`, y un ViewModel sin pantalla es código muerto. Lo escribe `S2-T10`.
  - **La fila "`Exito(Usuario)` sin sesión → volver a P-02" de 3.5 no se implementó.** `DEC-25` la dejó sin objeto, así que el registro exitoso entra directo y `registro_cuenta_creada` sigue sin usarse.
  - **La pérdida de foco es un solo evento `alPerderFoco(campo)`** en vez de una función por campo.
- **Dos hallazgos, fuera del alcance:**
  - **El mensaje de correo duplicado sale sin acentos**, "El correo ya esta registrado, inicia sesion", porque así lo escribe `RepositorioAuthFalso`. Con Supabase lo dará el trigger.
  - **Con Enter de teclado físico** (`adb shell input keyevent 66`) en el teléfono de P-03, la aplicación regresó a P-02 en vez de enviar. Con la tecla de acción del teclado en pantalla envía bien. No se investigó: el `onDone` es el mismo de `S2-T02`, y lo más probable es que el Enter active el `BotonTexto` de abajo.

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

`S2-T11` — Diseño y pantalla del perfil de usuario (ver y editar)
(prioridad 550, sprint 2, depende de `S2-T12`, que ya está hecha)

**No tiene ticket.** Es P-18: ver y editar la ficha con `RepositorioUsuario`,
cambiar la contraseña, cerrar sesión con su mensaje de error sin red, y
aterrizar ahí al llegar por el enlace de recuperación.

Lo que `S2-T12` le deja dicho: nombre, apellidos y teléfono se piden a
`obtenerMiUsuario()`, no a la `Sesion`; el repositorio no valida ni recorta; y
un campo de teléfono vacío se manda como `null`.

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

**2026-09-27 · Se integra la versión de `S2-T05` a `S2-T07` hecha hoy, no la de
Ricardo5690.** Ricardo5690 implementó las mismas tres tareas entre el 23 y el
24 de septiembre, en `feat/S2-T05-viewmodels-estados-ui-autenticacion`,
`docs/S2-T06-contrato-api-autenticacion` y `feat/S2-T07-autenticacion-supabase`.
Nunca llegaron a `main`, y por eso no aparecían aquí. El líder eligió la
versión de hoy, que se sube con los nombres
`feat/S2-T05-viewmodels-autenticacion`,
`docs/S2-T06-contrato-api-autenticacion-v2` y
`feat/S2-T07-autenticacion-supabase-v2`. Las razones:
- parte del `main` actual, con `S2-T04`;
- la sesión persiste;
- está probada contra Supabase;
- ya aplica `DEC-28` a `DEC-31`.

**Las ramas de Ricardo no se borran.** Conviene revisar con él si algo se
rescata, por ejemplo su prueba del repositorio o la llegada a P-18, que le
sirve a `S2-T11`. Es la segunda vez que una tarea se hace dos veces, después
de `S2-T04` (#10 y #11): una tarea tomada en una rama sin PR no se ve desde
`main`.

**2026-09-27 · Dos decisiones más: `DEC-30` y `DEC-31`.** `DEC-30` cierra
`H-12`: `S2-T08` cifra la sesión que guarda `supabase-kt` y quita
`Sesion.tokenAcceso`. `DEC-31` resuelve el riesgo de P-18: `S2-T12` trae
`RepositorioUsuario` real, `S2-T11` pasa a depender de `S2-T12`, y AGENTS.md
§6 pasa a seis tareas reales. De ahí salió un hallazgo nuevo, `H-13`:
solicitudes, postulaciones y reseñas no tienen tarea real. Está al final de
`CONTRATOS-API.md`.

**2026-09-27 · Dos decisiones nuevas: `DEC-28` y `DEC-29`.** Las tomó el líder
y las registró el agente por instrucción suya. `DEC-28` cierra `H-11`: el correo
duplicado es `TipoError.CORREO_DUPLICADO`, y P-03 deja de pintar mensajes tal
cual. `DEC-29` limita la elección de la siguiente tarea al sprint en curso y
a los anteriores.

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
