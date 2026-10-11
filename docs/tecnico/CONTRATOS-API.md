# Contratos de datos

Este documento define **qué operaciones existen y qué devuelve cada una**. Es el
contrato entre la interfaz y los datos, y se respeta esté quien esté del otro
lado.

**Si necesitas una operación que no está aquí, detente y repórtalo.** No la
inventes sobre la marcha. Esa regla es lo más importante del documento.

## Qué cambió con Supabase, y qué no

Antes esto era una lista de endpoints REST (`POST /auth/registro`, `GET
/servicios/mios`) de un backend que íbamos a escribir. Con DEC-16 ese backend
no existe: no hay servidor propio, no hay rutas que programar, no hay
`BASE_URL`. Esa parte del documento era ficción y se fue.

Lo que **no** se fue, porque no dependía del backend:

| Sigue en pie | Por qué es innegociable |
|---|---|
| **El catálogo cerrado de operaciones** | Es lo que impide que cada quien invente su propia forma de leer servicios. Si no está aquí, no se hace |
| **La forma de cada respuesta** | Las pantallas se programaron contra estas formas. Cambiar un campo rompe una pantalla |
| **La taxonomía de errores** | `TipoError` es lo que los ViewModels saben manejar y lo que `EstadoError` sabe pintar |
| **Qué es una operación atómica** | Aceptar una postulación son tres escrituras. Partirlas en tres llamadas es un error de corrección, no de estilo |
| **El contrato de la IA** | `429` y `503` son condiciones normales, no caídas. La aplicación tiene que seguir funcionando |

Lo que cambió es **con qué se cumple**: en vez de un endpoint nuestro, una
tabla, una vista, una función RPC o un servicio de Supabase.

## Cómo se usa este documento

La columna «Operación» es el método del repositorio. La columna «Con qué» es lo
que ese repositorio llama por dentro, y **no sale de `datos/`**: ninguna pantalla
ni ViewModel sabe que Supabase existe (ver `ARQUITECTURA.md`).

Todos los métodos son `suspend` y devuelven `Resultado<T>`.

---

## Autenticación · `RepositorioAuth`

Contrato de detalle escrito en `S2-T06`. Lo implementa `S2-T07` en
`RepositorioAuthReal`. Todo lo de esta sección está verificado contra el
código fuente de **`supabase-kt` 3.0.3**, que es la versión de
`gradle/libs.versions.toml`. Si esa versión cambia, esta sección se revisa.

### Resumen

| Operación | Devuelve | Con qué |
|---|---|---|
| `registrar(correo, contrasena, nombre, apellidos, telefono, rol)` | `Resultado<Sesion>` | `auth.signUpWith(Email)`, con nombre, apellidos, teléfono y rol en `data`; después, la ficha de `public.usuarios` |
| `iniciarSesion(correo, contrasena)` | `Resultado<Sesion>` | `auth.signInWith(Email)`; después, la ficha de `public.usuarios` |
| `recuperarContrasena(correo)` | `Resultado<Unit>` | `auth.resetPasswordForEmail(correo, redirectUrl = ENLACE_AUTH)` |
| `cambiarContrasena(nueva)` | `Resultado<Unit>` | `auth.updateUser { password = nueva }` |
| `cerrarSesion()` | `Resultado<Unit>` | `auth.signOut()` |
| `sesionActual()` | `Flow<Sesion?>` | `auth.sessionStatus`, más la ficha de `public.usuarios` |

**Lo que el repositorio recibe ya normalizado.** El correo llega con `trim()`
y en minúsculas, y el nombre y los apellidos con `trim()`. El teléfono llega
solo con dígitos. Lo hace el ViewModel al enviar (sección 1.6 de
`DISENO-AUTENTICACION.md`), y el repositorio **no** lo repite. La contraseña
llega tal cual.

### Cómo se arma la `Sesion`

Supabase Auth solo sabe de credenciales. El nombre, el rol y lo demás viven
en `public.usuarios`, así que la `Sesion` se arma en dos pasos:

1. La operación de Auth deja la sesión abierta en `supabase-kt`.
2. Se lee la ficha propia:
   `from("usuarios").select { filter { eq("id", <id del usuario de Auth>) } }.decodeSingle<UsuarioDto>()`.
   La deja pasar la política RLS "usuario ve su propia ficha" (`id = auth.uid()`,
   `basedatos/02_politicas_rls.sql`).

`UsuarioDto` vive en `datos/remoto/dto/` y trae las columnas de
`public.usuarios` con su nombre exacto:

| Columna | Tipo en la base | Campo de `Usuario` |
|---|---|---|
| `id` | `uuid` | `id` |
| `correo` | `varchar(160)` | `correo` |
| `nombre` | `varchar(80)` | `nombre` |
| `apellidos` | `varchar(120)` | `apellidos` |
| `telefono` | `varchar(20)`, nulo | `telefono` |
| `rol` | `rol_usuario` | `rol`, con `RolUsuario.desdeValor` |
| `foto_url` | `text`, nulo | `fotoUrl` |
| `activo` | `boolean` | `activo` |
| `creado_en` | `timestamptz` | `creadoEn` |
| `actualizado_en` | `timestamptz` | `actualizadoEn` |

**`Sesion` no lleva token** desde `S2-T08` (`DEC-30`, `H-12`). El token de
acceso, el de refresco y su persistencia los lleva `supabase-kt`, y la
aplicación solo decide **dónde** se guardan:

- `install(Auth) { sessionManager = ... }` recibe `AlmacenSesionCifrada`
  (`datos/local/`), en lugar del `SettingsSessionManager` por omisión, que
  guardaba la `UserSession` en texto plano en las `SharedPreferences`.
- La `UserSession` se guarda como JSON cifrado con AES-256-GCM, con una llave
  generada dentro del Android Keystore (`CifradorKeystore`), en el DataStore
  `sesion_cifrada`. No hay segunda copia.
- Lo que no se puede descifrar o leer, o un archivo dañado, cuenta como "sin
  sesión": se borra y el usuario entra otra vez.
- La sesión en texto plano de antes de `S2-T08` se migra cifrada la primera vez
  que se carga, y se borra.
- `sesion_cifrada` y las `SharedPreferences` por omisión quedan fuera de la
  copia de seguridad y de la transferencia entre dispositivos: la llave del
  Keystore no viaja, así que el respaldo no serviría.

**La sesión se carga cuando se crea el cliente de Supabase**, que es perezoso
desde `S2-T07`. Desde `S2-T09` eso ocurre al abrir la aplicación, porque P-01
lee `sesionActual()`.

Si el paso 2 falla, la operación devuelve el error del paso 2 según la tabla
de errores, aunque el paso 1 haya salido bien. Ver la nota de `registrar`.

### `registrar`

```kotlin
auth.signUpWith(Email, redirectUrl = null) {
    email = correo
    password = contrasena
    data = buildJsonObject {
        put("nombre", nombre)
        put("apellidos", apellidos)
        put("telefono", telefono)
        put("rol", rol.valor)
    }
}
```

**Las llaves de `data` son exactamente las que lee
`fn_crear_usuario_desde_auth`** en `basedatos/01_esquema.sql`, la función que
dispara `tg_auth_usuario_creado`:

| Llave | Línea del esquema | Si no llega o llega vacía |
|---|---|---|
| `nombre` | 172 | `'Sin nombre'` |
| `apellidos` | 173 | `'Sin apellidos'` |
| `telefono` | 174 | `null` |
| `rol` | 175, con `::rol_usuario` | `'cliente'` |

`rol` viaja como `RolUsuario.valor`, es decir `"cliente"` o `"trabajador"`.
Una llave mal escrita **no falla**: el trigger pone el valor por omisión sin
avisar, y la cuenta queda con otro nombre o con otro rol. Por eso esta tabla
se respeta letra por letra.

`redirectUrl = null` porque con la confirmación por correo desactivada
(`DEC-25`) el registro no manda correo, y no hay enlace que redirigir.

**Por qué `registrar` devuelve `Sesion`.** Con la confirmación desactivada,
`signUpWith(Email)` devuelve `null` y deja la sesión abierta. Así lo
documenta `Auth.signUpWith` en 3.0.3: *"null if auto-confirm is enabled
(resulting in a login)"*. `DEC-25` usa justo eso.

**Si el alta sale bien pero la lectura de la ficha falla**, por ejemplo porque
se fue la red entre una llamada y otra:
- `registrar` devuelve el error de la lectura, casi siempre `RED`;
- **la cuenta ya existe y la sesión quedó abierta**, así que "Reintentar" en
  P-03 va a devolver correo duplicado;
- la salida para el usuario es iniciar sesión en P-02.

Es un caso raro, y resolverlo bien exigiría un alta atómica que Supabase Auth
no ofrece. Queda documentado para que nadie lo tome por un defecto de `S2-T07`.

### `iniciarSesion`

```kotlin
auth.signInWith(Email) {
    email = correo
    password = contrasena
}
```

Después, la ficha, como en "Cómo se arma la `Sesion`".

### `recuperarContrasena` y el enlace de la aplicación

```kotlin
auth.resetPasswordForEmail(correo, redirectUrl = ENLACE_AUTH)
```

**El enlace de la aplicación es `mx.donchambitas.app://auth`.** Es el único
lugar donde está escrito; el código lo toma de una sola constante.

| Pieza | Valor |
|---|---|
| Esquema (`AuthConfig.scheme`) | `mx.donchambitas.app` |
| Host (`AuthConfig.host`) | `auth` |
| `ENLACE_AUTH`, igual a `AuthConfig.deepLink` | `mx.donchambitas.app://auth` |
| Flujo (`AuthConfig.flowType`) | `FlowType.IMPLICIT`, el valor por omisión de 3.0.3 |

- **Por qué un esquema propio y no un App Link `https://`.** Un App Link exige
  publicar `/.well-known/assetlinks.json` en un dominio web, y `DEC-02`
  descarta la web.
- **Por qué el nombre del paquete como esquema.** Un esquema propio lo puede
  reclamar cualquier otra aplicación instalada. Con el nombre del paquete, un
  choque es mucho menos probable que con `donchambitas://`.
- **Por qué el host es `auth` y no `recuperar`.** `supabase-kt` 3.0.3 guarda
  **un** esquema y **un** host para todos sus flujos, así que el host nombra
  la entrada, no el caso. La recuperación se distingue de otra forma, ver el
  paso 4 abajo.
- **Por qué `IMPLICIT` y no `PKCE`.** Con `IMPLICIT` el enlace trae los tokens
  y `type=recovery` en el fragmento, y `parseSessionFromFragment` conserva ese
  `type` en `UserSession.type`. Con `PKCE` llega un `code` que se canjea con
  `exchangeCodeForSession`, y el `type` se pierde. Sin `type` la aplicación no
  sabe que tiene que llevar al usuario a P-18.

**El recorrido completo (`DEC-27`):**

| # | Qué pasa | Quién lo hace |
|---|---|---|
| 1 | `recuperarContrasena` pide el correo con `redirectUrl = ENLACE_AUTH` | `S2-T07` |
| 2 | Supabase manda el correo. El enlace pasa por su servidor y redirige a `mx.donchambitas.app://auth#access_token=…&refresh_token=…&type=recovery…` | Supabase |
| 3 | Un `intent-filter` de `MainActivity` con ese esquema y ese host abre la aplicación | `S2-T07` |
| 4 | `MainActivity` revisa el enlace con `esEnlaceConSesion` y llama a `importarSesionDeEnlace` (`datos/remoto/supabase/EnlaceAuth.kt`). Hace lo mismo que `handleDeeplinks`: `parseSessionFromFragment`, `retrieveUser` e `importSession`, pero dentro de un `try`. La sesión queda importada, y `UserSession.type` trae `"recovery"` | `S2-T07` |
| 5 | Con esa marca, la aplicación aterriza en P-18, directo en la sección de contraseña | `S2-T11` |
| 6 | P-18 llama a `cambiarContrasena(nueva)` | `S2-T11` |

**El enlace vencido o ya usado.** En ese caso Supabase redirige con
`#error=…&error_code=…` en vez de tokens. `parseSessionFromFragment` lanza
`IllegalArgumentException` ("No access token found") cuando no hay
`access_token`, y `handleDeeplinks` no lo atrapa. Además, `handleDeeplinks`
lee el usuario en un scope propio de la biblioteca **sin manejador de
errores**, así que abrir el enlace sin red también cerraba la aplicación. Por
las dos razones, `S2-T07` no usa `handleDeeplinks`. `esEnlaceConSesion`
descarta el enlace sin tokens, con `error=` o con partes sin `=`, e
`importarSesionDeEnlace` hace la importación dentro de un `try`.

Con un enlace vencido, la aplicación abre normal, sin sesión, en P-02.
Verificado en emulador, en frío y con la aplicación abierta. Si además hay
que avisarle algo al usuario, lo decide `S2-T11`.

**`recuperarContrasena` siempre reporta éxito cuando la petición llega al
servidor,** exista o no el correo. Decir cuáles correos están registrados es
una fuga de información. Eso incluye los rechazos por límite de envío
(`over_email_send_rate_limit`, `over_request_rate_limit`):
- desde el cliente no se sabe si el límite es del proyecto o de ese correo;
- uno por correo solo puede saltar si la cuenta existe;
- responder distinto delataría la cuenta.

**Y los rechazos del envío del correo** (`email_address_invalid`,
`email_address_not_authorized`), agregados por `S2-T10` el 2026-10-08.
Supabase solo intenta mandar el correo si la cuenta existe; un correo sin
cuenta recibe `200` y nada más. Probado contra el proyecto:
`ana.rls@prueba.donchambitas.mx`, que tiene cuenta en un dominio que no recibe
correo, daba `400 email_address_invalid`, y `nadie@prueba.donchambitas.mx`,
sin cuenta, `200`. Antes P-04 pintaba "Datos incorrectos" en el primero y la
confirmación en el segundo. `email_address_not_authorized` es el rechazo del
SMTP de fábrica de Supabase, que solo entrega a los miembros del equipo; no se
pudo provocar, y se incluye por la misma razón. La función que decide es
`esRechazoQueDelataLaCuenta`, en `ErroresAuth.kt`.

**No** incluye la falta de red. Si la petición no salió, es `RED` y P-04 lo
dice (sección 4.4 de `DISENO-AUTENTICACION.md`).

### `cambiarContrasena`

```kotlin
auth.updateUser { password = nueva }
```

Recibe **solo** la contraseña nueva, nunca la anterior. Eso es lo que permite
reusar P-18 para la recuperación (`DEC-27`): quien llega por el enlace no
puede dar la que olvidó.

### `cerrarSesion`

```kotlin
auth.signOut()
```

Con el alcance por omisión de 3.0.3, `SignOutScope.LOCAL`: cierra la sesión de
este dispositivo, no las de otros.

**Sin red, la sesión no se cierra.** En 3.0.3, `signOut` solo sigue adelante
si el servidor rechaza el cierre porque la sesión ya no valía (la sesión
vencida o la cuenta borrada). Si la petición no sale, lanza la excepción y
**no** borra la sesión local. `cerrarSesion` devuelve `RED`, y la sesión sigue
abierta hasta que el usuario lo intente con red.

### `sesionActual()`

Se deriva de `auth.sessionStatus`:

| `SessionStatus` en 3.0.3 | Qué emite `sesionActual()` |
|---|---|
| `Initializing` | **Nada.** Todavía no se sabe si hay sesión: emitir `null` mandaría a P-02 a alguien que sí la tiene |
| `Authenticated(session, source)` | `Sesion` con la ficha de `public.usuarios`. La ficha se lee una vez por usuario, no en cada refresco del token (`source = Refresh`) |
| `NotAuthenticated(isSignOut)` | `null` |
| `RefreshFailure(cause)` | **Lo último que emitió**, sin cambios |

- **Por qué `RefreshFailure` no emite `null`.** En 3.0.3 ese estado solo se
  usa para fallas pasajeras, de red o `5xx`, y `supabase-kt` reintenta solo.
  Cuando el token de refresco ya no sirve (`4xx`), la biblioteca borra la
  sesión y pasa a `NotAuthenticated`. Ese sí emite `null`.
- **Si la lectura de la ficha falla en `Authenticated`,** no se emite nada, y
  se reintenta con el siguiente cambio de estado. Un tropiezo de red no es un
  cierre de sesión.

**Quién lo lee** (`S2-T09`):

- **P-01** toma el primer valor para decidir a dónde va: `null` a P-02, y una
  `Sesion` a P-05 o P-10 según su rol. Si en **8 segundos** no llega nada,
  muestra "Sin conexión" con "Reintentar" y no borra nada: el flujo no
  distingue "todavía cargando" de "no pude leer la ficha", así que el tope es
  lo único que corta la espera. "Reintentar" vuelve a recoger el flujo, y con
  eso la ficha se lee otra vez. Si el flujo lanza porque faltan las llaves en
  `local.properties`, P-01 manda a P-02.
- **`SesionViewModel`**, uno por actividad, lo escucha mientras la aplicación
  está abierta. Cada `null` es un aviso de "no hay sesión"; si había alguien
  adentro, la aplicación sale a P-02 con la pila limpia. Cubre el cierre a mano
  y el token de refresco que ya no sirve.

Cada uno recoge el flujo por su cuenta, así que al abrir con sesión guardada
la ficha se lee dos veces.

### Errores de autenticación

Se traducen en `datos/`, con la regla general de "Errores" más abajo. Los
códigos son los de `AuthErrorCode` en 3.0.3 y llegan en
`AuthRestException.errorCode`.

| Qué llega | Operaciones | `TipoError` |
|---|---|---|
| `HttpRequestException` o `HttpRequestTimeoutException` (sin red, tiempo agotado) | todas | `RED` |
| `invalid_credentials` | `iniciarSesion` | `AUTENTICACION` |
| `email_not_confirmed` | `iniciarSesion` | `AUTENTICACION`. No debería llegar con la confirmación desactivada |
| `user_banned` | `iniciarSesion` | `AUTENTICACION` |
| `session_not_found`, `session_expired`, `refresh_token_not_found`, `bad_jwt` | `cambiarContrasena` | `AUTENTICACION` |
| `user_already_exists`, `email_exists` | `registrar` | `CORREO_DUPLICADO` (`H-11`). La pantalla pinta `error_correo_duplicado` |
| `weak_password` (`AuthWeakPasswordException`) | `registrar`, `cambiarContrasena` | `VALIDACION`. Con el mínimo en 8 en el servidor y en la interfaz no debería llegar. Se pinta el mensaje genérico de `VALIDACION` |
| `same_password` | `cambiarContrasena` | `VALIDACION`. Qué mensaje pinta P-18 lo decide `S2-T11` |
| `validation_failed`, `email_address_invalid` | `registrar`, `iniciarSesion` | `VALIDACION`. La interfaz ya validó con la misma expresión que el esquema, así que no debería llegar |
| `over_request_rate_limit` | `registrar`, `iniciarSesion`, `cambiarContrasena` | `SERVIDOR` |
| `over_email_send_rate_limit`, `over_request_rate_limit`, `email_address_invalid`, `email_address_not_authorized` | `recuperarContrasena` | **Éxito**, ver arriba |
| `signup_disabled`, `email_provider_disabled` | `registrar`, `iniciarSesion` | `SERVIDOR`. Es configuración nuestra, no del usuario |
| `unexpected_failure` o `5xx` | todas | `SERVIDOR`. Si el trigger `tg_auth_usuario_creado` falla en un alta, lo esperado es que Auth lo reporte así, como error de base de datos, y **no** con el mensaje del trigger. `S2-T07` lo confirma con una prueba contra el proyecto |
| La ficha de `public.usuarios` no llega (`RestException` al leerla) | `registrar`, `iniciarSesion` | `SERVIDOR`. Es un defecto nuestro: la crea el trigger en la misma transacción que la credencial |
| Cualquier otra cosa | todas | `DESCONOCIDO` |

**Confirmado contra el proyecto el 2026-09-27 (`S2-T07`):**
- `invalid_credentials` da "Correo o contraseña incorrectos";
- con la confirmación desactivada, un correo repetido da `user_already_exists`,
  que se traduce a `CORREO_DUPLICADO`;
- el alta con el dominio ficticio `@prueba.donchambitas.mx` pasa.

**No se confirmó** cómo reporta Auth la falla del trigger en un alta: no hay
forma de provocarla desde la aplicación sin romper el esquema.

`email_address_invalid` y `email_address_not_authorized` son códigos que el
servidor de Auth devuelve pero que no están en `AuthErrorCode` de 3.0.3: se
comparan contra el texto crudo del error.

**Los mensajes en inglés de Supabase Auth nunca llegan a la pantalla.** Todas
las pantallas de autenticación pintan el mensaje de `strings.xml` que le toca
a su `TipoError` (sección 6 de `DISENO-AUTENTICACION.md`). El `mensaje` del
`Resultado.Error` es para el log, no para el usuario.

### Configuración del proyecto de Supabase que este contrato supone

La aplica `S2-T07` en la consola y la repite `S6-T05` en producción.
Ninguna se configura desde la aplicación.

Los nombres de los menús de la consola cambian con el tiempo. Lo que manda es
el ajuste, no la ruta.

| Ajuste de Authentication | Valor | Por qué |
|---|---|---|
| Confirmación por correo del proveedor Email | **Desactivada** | `DEC-25`. Con la confirmación activa, `signUpWith` no abre sesión |
| Largo mínimo de contraseña | **8** | El de la tabla 5.2. Así la base no acepta lo que la interfaz rechaza, ni al revés |
| URLs de redirección permitidas | **`mx.donchambitas.app://auth`** | Si no está en la lista, Supabase no redirige a la aplicación |

## Usuario · `RepositorioUsuario`

Escrito a detalle en `S2-T12`, contra `supabase-kt` 3.0.3 y contra el proyecto
de Supabase. La implementación es `RepositorioUsuarioReal` (`DEC-31`).

### Resumen

| Operación | Con qué | Devuelve |
|---|---|---|
| `obtenerMiUsuario()` | `select` sobre `usuarios` con `id = <uid>` | `Resultado<Usuario>` |
| `actualizarMiUsuario(nombre, apellidos, telefono)` | `update` sobre `usuarios` con `id = <uid>` | `Resultado<Usuario>`, ya actualizado |
| `subirFotoPerfil(bytes)` | Storage, cubeta `perfiles`, ruta `<uid>/<archivo>`, y luego `usuarios.foto_url` | **Todavía no:** llega con `S2-T14` |

**El `<uid>` sale de la sesión que ya tiene `supabase-kt`**:
`auth.currentUserOrNull()?.id`, sin ir a la red. Si no hay sesión, la
operación devuelve `AUTENTICACION` sin hacer ninguna petición.

**El repositorio no valida ni recorta.** Manda lo que recibe. Las reglas de
nombre, apellidos y teléfono viven en `ValidacionesAuth` y las aplica el
ViewModel de P-18, igual que en P-03.

### `obtenerMiUsuario`

```kotlin
from("usuarios")
    .select { filter { eq("id", uid) } }
    .decodeList<UsuarioDto>()
```

La deja pasar la política "usuario ve su propia ficha" (`id = auth.uid()`). Se
convierte con `UsuarioDto.aUsuario()`, el mismo DTO de "Cómo se arma la
`Sesion`".

### `actualizarMiUsuario`

```kotlin
from("usuarios")
    .update({
        set("nombre", nombre)
        set("apellidos", apellidos)
        set("telefono", telefono)
    }) {
        filter { eq("id", uid) }
        select()
    }
    .decodeList<UsuarioDto>()
```

- **Manda esas tres columnas y ninguna más.** RLS filtra filas, nunca
  columnas, así que quien cierra esto son los permisos por columna de
  `02_politicas_rls.sql`: `authenticated` puede actualizar `nombre`,
  `apellidos`, `telefono` y `foto_url`. Con una sola columna de más, el
  `update` entero falla con `403` (`permission denied for table usuarios`).
  Por eso no se serializa un DTO: se nombra cada columna con `set`. Lo mismo
  aplica a `marcarLeidos`, que solo puede tocar `leido_en`.
- **`select()` pide la fila de vuelta** (`Prefer: return=representation`), y
  de ahí sale el `Usuario` que se devuelve, con el `actualizado_en` que puso el
  trigger. No hay segunda lectura.
- **`telefono = null` borra el teléfono.** Convertir un campo vacío en `null`
  es del ViewModel.
- El correo y el rol no se cambian: el correo vive en `auth.users` y el cambio
  de rol está fuera del MVP (`DEC-22`).

### Cero filas es `AUTENTICACION`

RLS no rechaza la fila ajena ni la petición sin sesión: las filtra. PostgREST
contesta **`200` con una lista vacía**, tanto en el `select` como en el
`update`. En las dos operaciones, una lista vacía se devuelve como
`Resultado.Error(AUTENTICACION)`: la sesión ya no corresponde a ninguna ficha.
Nunca es un éxito.

### `subirFotoPerfil`

`RepositorioUsuarioReal` devuelve `Resultado.Error(SERVIDOR)` hasta `S2-T14`
(`DEC-31`, y la decisión 1 de `S2-T12`). Nadie la llama todavía.

Cuando llegue: la ruta de Storage **tiene que empezar con el uid del
usuario**, porque de ahí salen las políticas. Ver
`basedatos/03_almacenamiento.sql`.

### La `Sesion` no se entera de la edición

`sesionActual()` lee la ficha una vez por usuario. Después de
`actualizarMiUsuario`, la `Sesion` que emite sigue con los datos de antes
hasta que se cierre la aplicación o se vuelva a entrar (decisión 2 de
`S2-T12`).

**Regla:** de la `Sesion` se lee quién es el usuario y su rol, que no cambian.
Una pantalla que pinte nombre, apellidos, teléfono o foto los pide a
`obtenerMiUsuario()`.

### Errores de `RepositorioUsuario`

`traducirErrorDatos`, en `datos/remoto/supabase/ErroresDatos.kt`. Si esta
tabla cambia, esa función cambia igual.

**Se clasifica por el código HTTP**, porque `supabase-kt` 3.0.3 no deja pasar
el código de Postgres: arma la excepción con `message`, `details` y `hint`, y
`code` (`42501`, `22001`) se pierde.

| Qué llega | Cuándo pasa, probado el 2026-10-11 | `TipoError` |
|---|---|---|
| `HttpRequestException`, `HttpRequestTimeoutException` | Sin red, tiempo agotado | `RED` |
| Sin usuario en `auth` | No hay sesión abierta | `AUTENTICACION` |
| `200` con lista vacía | La sesión no corresponde a ninguna ficha | `AUTENTICACION` |
| `401` | El token no se puede leer o venció (`PGRST301`) | `AUTENTICACION` |
| `403` | El `update` nombra una columna sin permiso (`42501`). **Es un defecto nuestro**, no del usuario | `AUTENTICACION` |
| `400` | El valor no cabe en la columna (`22001`): nombre de más de 80, apellidos de más de 120, teléfono de más de 20 | `VALIDACION` |
| `409` | Llave duplicada. No pasa con estas tres columnas; queda por la tabla general | `VALIDACION` |
| `5xx` | El servidor falla | `SERVIDOR` |
| Cualquier otra cosa | | `DESCONOCIDO` |

**El `mensaje` del `Resultado.Error` es `RestException.error`**, el texto que
mandó el servidor. **Nunca `RestException.message`**: en 3.0.3 ese texto
incluye las cabeceras de la petición, con `Authorization: Bearer <token>`
(`H-13`).

**La base no valida el contenido.** `public.usuarios` no tiene `CHECK` sobre
`nombre`, `apellidos` ni `telefono`: un nombre vacío devuelve `200`. Lo único
que lo impide hoy es la validación de la aplicación.

## Perfil del trabajador · `RepositorioTrabajador`

| Operación | Con qué |
|---|---|
| `obtenerPerfilPublico(id)` | RPC `fn_perfil_publico_trabajador(id)` |
| `obtenerMiPerfil()` | `perfiles_trabajador` where `usuario_id = auth.uid()` |
| `guardarMiPerfil(perfil)` | `upsert` sobre `perfiles_trabajador` |
| `reemplazarHabilidades(lista)` | `delete` + `insert` sobre `perfil_habilidades` |

`obtenerPerfilPublico` es RPC y no cinco consultas porque **P-07 necesita perfil,
habilidades, servicios, fotos, calificación y reseñas de un golpe**. Devuelve:

```json
{
  "id": "...", "nombre": "...", "apellidos": "...", "foto_url": "...",
  "titulo": "...", "descripcion": "...", "experiencia_anios": 10,
  "telefono_contacto": "...", "disponible": true,
  "estado": "Puebla", "municipio": "Cholula",
  "habilidades": ["..."],
  "calificacion": { "promedio": 4.7, "total_resenas": 23 },
  "servicios": [ { "id": "...", "titulo": "...", "descripcion": "...",
                   "categoria": "...", "categoria_id": 1,
                   "precio_desde": 300, "precio_hasta": 800,
                   "unidad_precio": "por trabajo", "fotos": ["..."] } ],
  "resenas": [ { "calificacion": 5, "comentario": "...",
                 "cliente_nombre": "...", "creado_en": "..." } ]
}
```

## Servicios · `RepositorioServicios`

| Operación | Con qué |
|---|---|
| `obtenerMisServicios()` | `servicios` where `perfil_id = auth.uid()` |
| `crear(servicio)` / `actualizar(servicio)` / `eliminar(id)` | `servicios` |
| `pausar(id, activo)` | `update servicios set activo` |
| `subirFoto(servicioId, bytes, posicion)` | Storage `servicios/<uid>/…` y luego `servicio_fotos` |
| `eliminarFoto(fotoId)` | `servicio_fotos` y el archivo en Storage |

**Máximo 3 fotos por servicio**, y lo impone la base (`ck_foto_posicion` más
`uq_foto_posicion_unica`), no la aplicación. Se sube el archivo primero y se
inserta la fila después; si la fila falla por el tope, hay que borrar el archivo
recién subido o quedan huérfanos.

## Búsqueda y catálogos · `RepositorioCatalogos`

| Operación | Con qué |
|---|---|
| `buscarTrabajadores(filtros, pagina)` | Vista `vw_busqueda_trabajadores` con filtros de postgrest |
| `obtenerCategorias()` / `obtenerEstados()` / `obtenerMunicipios(estadoId)` | Catálogos. Se cachean localmente |

Filtros, todos opcionales: texto libre contra `titulo` (`ilike`), `estado_id`,
`municipio_id`, `precio_desde` máximo, `promedio` mínimo. Orden por `promedio`,
`creado_en` o `precio_desde`. Paginación de 20 con `range()`.

**El filtro por categoría va contra `categorias`, que es un arreglo**, no contra
un `categoria_id` suelto: un trabajador ofrece varios oficios a la vez. Desde
postgrest se usa el operador de contención: `categorias=cs.{3}`.

La vista devuelve por renglón: `trabajador_id`, `nombre`, `apellidos`,
`foto_url`, `titulo`, `disponible`, `estado_id`, `municipio_id`, `estado`,
`municipio`, `promedio`, `total_resenas`, `servicios_activos`, `precio_desde`,
`categorias` y `creado_en`.

Los catálogos son de lectura pública, incluso sin sesión.

## Solicitudes · `RepositorioSolicitudes`

| Operación | Con qué |
|---|---|
| `obtenerMisSolicitudes()` | `solicitudes` where `cliente_id = auth.uid()` |
| `obtenerAbiertas(categoriaId?)` | `solicitudes` where `estatus = 'abierta'` |
| `obtenerDetalle(id)` | `solicitudes` con sus `postulaciones` |
| `crear(solicitud)` / `actualizar(solicitud)` / `cancelar(id)` | `solicitudes` |
| `cerrar(id)` | RPC `fn_cerrar_solicitud(id)` |

Estados válidos de `estatus`: `abierta`, `asignada`, `cerrada`, `cancelada`.

**Pasar a `asignada` no se hace a mano**: es consecuencia de aceptar una
postulación. **Cerrar es RPC** porque exige trabajador asignado y sella
`cerrada_en`.

Ojo con el primero: eso es **regla de este contrato, no de la base** (DEC-21).
La base deja que el cliente lleve su propia solicitud a `asignada` con un
`update` suelto. Nadie te va a detener; simplemente no se hace, y un `update` a
`estatus` que no sea `cancelada` es un bug en revisión de código.

> Ojo: la columna del flujo se llama `estatus`. `estado_id` es la entidad
> federativa. Son cosas distintas.

## Postulaciones · `RepositorioPostulaciones`

| Operación | Con qué |
|---|---|
| `obtenerMisPostulaciones()` | `postulaciones` where `trabajador_id = auth.uid()` |
| `obtenerDeSolicitud(solicitudId)` | `postulaciones` de esa solicitud |
| `postularse(solicitudId, mensaje, precio)` | `insert` en `postulaciones` |
| `retirar(id)` | `update estatus = 'retirada'` |
| `aceptar(id)` | **RPC `fn_aceptar_postulacion(id)`** |

**`aceptar` es RPC y no se negocia.** Son tres escrituras: aceptar esa, rechazar
las demás y asignar la solicitud. Sueltas desde la aplicación, una caída a medio
camino deja a dos trabajadores creyendo que ganaron.

Un trabajador **no ve** las postulaciones de sus competidores. Lo impide RLS.

## Chat · `RepositorioChat`

| Operación | Con qué |
|---|---|
| `obtenerConversaciones()` | `conversaciones` ordenadas por `ultimo_mensaje_en` |
| `abrirConversacion(trabajadorId, solicitudId?)` | RPC `fn_abrir_conversacion` |
| `obtenerMensajes(conversacionId, pagina)` | `mensajes` paginados |
| `enviar(conversacionId, contenido)` | `insert` en `mensajes` |
| `marcarLeidos(conversacionId)` | `update mensajes set leido_en` |
| `mensajesNuevos(conversacionId): Flow<Mensaje>` | **Supabase Realtime** sobre `mensajes` |

El tiempo real es una suscripción, no un sondeo. El sondeo cada 5 segundos era
el plan de respaldo mientras PEND-01 estaba abierto y ya no aplica.

`abrirConversacion` es RPC porque es un upsert con una condición de unicidad que
incluye un `COALESCE`, y eso no se expresa bien desde postgrest.

**La conversación la abre siempre el cliente** (DEC-20). La RPC fija
`cliente_id = auth.uid()`, así que si la llama un trabajador, falla. El
trabajador responde en un hilo que ya existe; nunca inicia uno. Para S5-T05 y
S5-T06 eso significa que el botón de contactar vive en P-07 y en P-19 del lado
del cliente, y **no** en las pantallas del trabajador.

## Reseñas · `RepositorioResenas`

| Operación | Con qué |
|---|---|
| `dejarResena(solicitudId, calificacion, comentario)` | `insert` en `resenas` |

Una sola por solicitud, solo el cliente que la publicó, solo si está cerrada, y
solo sobre el trabajador asignado. Lo verifica el trigger `tg_resenas_validar`
además de RLS. **No se editan ni se borran.**

La calificación promedio no se pide aparte: viene en
`fn_perfil_publico_trabajador` y en `vw_busqueda_trabajadores`.

## Inteligencia artificial · `RepositorioIa`

| Operación | Con qué |
|---|---|
| `generar(funcion, entrada)` | **Edge Function** `ia-generar` |

Un solo punto de entrada, contra nuestra Edge Function, **nunca contra OpenAI**.
La llave de OpenAI vive en las variables de entorno de esa función y en ningún
otro lugar. Ver `docs/producto/IA.md`.

```
POST functions/v1/ia-generar
{ "funcion": "redactar_perfil" | "redactar_servicio" | "categorizar" | "sugerir",
  "entrada": "texto del usuario" }

200 → { "resultado": "...", "desde_cache": false, "llamadas_restantes_hoy": 7 }
429 → { "error": { "codigo": "LIMITE_DIARIO_IA",
                   "mensaje": "Alcanzaste el límite de hoy" } }
503 → { "error": { "codigo": "IA_NO_DISPONIBLE", ... } }
```

La función hace, en este orden: valida la sesión, consulta `ia_cache`, llama a
`fn_ia_registrar_llamada` para el tope, y solo entonces llama a OpenAI. Si
cualquiera de los tres primeros resuelve, no se paga nada.

**`429` y `503` son situaciones normales, no caídas.** La aplicación muestra un
aviso y deja al usuario escribir a mano. Una pantalla que se rompe con un 429
está mal hecha.

---

## Errores

Nada de excepciones cruzando capas. El repositorio atrapa lo que venga de
Supabase y devuelve `Resultado.Error(tipo, mensaje)`.

| Qué llega de Supabase | `TipoError` | Qué ve el usuario |
|---|---|---|
| Sin red, tiempo agotado | `RED` | "Revisa tu conexión e intenta de nuevo" |
| 401, sesión vencida, credenciales malas | `AUTENTICACION` | "Correo o contraseña incorrectos" |
| 403, RLS rechaza la fila | `AUTENTICACION` | "No tienes permiso para hacer eso" |
| Violación de CHECK o de trigger | `VALIDACION` | El mensaje del trigger, ya está escrito en español |
| Correo ya registrado en el alta de Supabase Auth | `CORREO_DUPLICADO` | "El correo ya está registrado, inicia sesión" (`H-11`) |
| 409, otra llave duplicada | `VALIDACION` | El mensaje del trigger, ya está escrito en español |
| 429 de la Edge Function | `LIMITE_IA` | "Alcanzaste el límite de hoy" |
| 5xx, 503 de la Edge Function | `SERVIDOR` | "Algo falló de nuestro lado, intenta más tarde" |
| Cualquier otra cosa | `DESCONOCIDO` | "Algo salió mal, intenta de nuevo" |

**Un `403` de RLS casi nunca es un problema de permisos del usuario: es un bug
nuestro.** Si en pruebas sale un 403 donde debería funcionar, la política está
mal, no el usuario. No lo tapes con un mensaje bonito.

Los mensajes **dicen qué hacer**, no solo qué falló. Es la regla de `DISENO.md`.
Y todos viven en `strings.xml`.

---

## Lo que esto no cubre

Las traducciones concretas de arriba son **el contrato**, no la
implementación. Cada tarea de contrato detalla su módulo con los nombres de
columna exactos y los DTO: S2-T06 autenticación y S2-T12 perfil de usuario (ya escritos, arriba),
S3-T08 trabajador y servicios, S3-T12 la Edge Function de IA, S4-T08 solicitudes
y búsqueda, S5-T06 mensajería y postulaciones.

Mientras esas tareas no lleguen, la implementación activa de todos estos
repositorios es la falsa, en memoria. Ver `AGENTS.md` §6.

---

## Hallazgos para el líder

Salen de escribir el contrato de detalle de autenticación en `S2-T06`.
**Ninguno se resolvió aquí**, conforme a AGENTS.md §9.

**`H-11` · CERRADO el 2026-09-27 por `DEC-28`: opción 1, un `TipoError` nuevo
para el correo duplicado.** La aplicó `S2-T07`:
- agrega `TipoError.CORREO_DUPLICADO` y su mensaje en `strings.xml`;
- actualiza la tabla de errores de arriba;
- P-03 deja de pintar el `mensaje` tal cual, porque en el registro nunca llega
  en español.

El hallazgo, como se reportó:

**`H-11` · Los errores de `VALIDACION` de P-03 no tienen de dónde sacar su
mensaje en español.**
- La sección 3.5 de `DISENO-AUTENTICACION.md` dice que, en `VALIDACION`, P-03
  pinta el mensaje **tal cual llega**, porque viene en español desde un
  trigger.
- Esa premisa no se cumple en el registro. El correo duplicado
  (`user_already_exists`) lo rechaza Supabase Auth antes de que exista la
  fila, y en inglés ("User already registered"). Si el trigger llegara a
  fallar, Auth tampoco pasa su mensaje: lo esperado es un error de base de
  datos genérico.
- La tabla "Errores" de este documento quiere el texto en español y en
  `strings.xml`, pero `datos/` no puede leer `strings.xml`: no conoce `R`.

Lo mismo les pasa a `weak_password` y a `same_password`, que P-18 va a
necesitar. **El caso que hoy se ve es el correo duplicado:** HU-01 lo pide, y
P-03 tiene el acceso directo a P-02 justo para ese caso.

Las salidas que se ven, las tres del líder:

1. **Un tipo de error nuevo, por ejemplo `TipoError.CORREO_DUPLICADO`.** La
   pantalla lo traduce con `strings.xml`, como ya hace con los demás tipos, y
   el `when` exhaustivo obliga a cada pantalla a decidir qué hace con él. Toca
   `TipoError`, `Estados.kt` y la tabla 3.5.
2. **Un código estable en `mensaje`**, por ejemplo `"correo_duplicado"`, que
   P-03 traduce, y el texto genérico de `VALIDACION` para lo que no conozca.
   No toca `TipoError`, pero `mensaje` deja de ser texto para el usuario
   solo en este caso.
3. **Aceptar texto en español dentro de `datos/`.** Es lo que ya hace
   `RepositorioAuthFalso`, y no cambia nada más. Es una excepción escrita a
   la regla de `CONVENCIONES.md` de no tener cadenas de interfaz fuera de
   `strings.xml`.

**Recomendación: la 1.** Es la única en la que el compilador avisa si una
pantalla se olvida del caso. Mientras no se decida, `RepositorioAuthFalso`
sigue devolviendo `VALIDACION` con el texto en español, que es lo que P-03
pinta hoy. **`S2-T07` no puede cerrar el alta real sin esta decisión.**

**`H-12` · CERRADO el 2026-09-27 por `DEC-30`:** `S2-T08` cifra la sesión que
guarda `supabase-kt` y quita `tokenAcceso`. El hallazgo, como se reportó:

**`H-12` · `Sesion.tokenAcceso` probablemente sobra.** El contrato dice que el
token, su refresco y su persistencia los lleva `supabase-kt`, y que no se
guarda a mano. Ninguna pantalla ni ViewModel lo lee. Con eso el campo no
tiene quien lo use, y un token en un modelo de dominio invita a que alguien
lo guarde o lo registre en un log. Quitarlo toca el modelo `Sesion`, así que
no se hizo aquí. Sugerencia: que lo retire `S2-T07` o `S2-T08`, si el líder
está de acuerdo.

**`H-13` · Tres repositorios no tienen tarea de implementación real.** Salió
al resolver el riesgo de P-18 que dejó `S2-T07`, y que cerró `DEC-31`. Las
seis tareas reales de AGENTS.md §6 cubren:
- autenticación (`S2-T07`) y usuario (`S2-T12`);
- imágenes (`S2-T14`);
- trabajador y servicios (`S3-T09`);
- búsqueda y catálogos (`S4-T09`);
- mensajería (`S5-T07`).

La IA la cubre `S4-T10`. **`RepositorioSolicitudes`, `RepositorioPostulaciones`
y `RepositorioResenas` no aparecen en ninguna**, y dependen de ellos P-08
Publicar solicitud, P-09 Mis solicitudes, P-14 Mis postulaciones, P-17 Dejar
reseña y P-19 Detalle de solicitud. Los tres leen la sesión de `FuenteDatosFalsa`,
así que con la autenticación real fallan igual que fallaba `RepositorioUsuario`.
No urge hasta los sprints 4 y 5, pero hay que asignarlos antes de redactar
esos tickets. **Lo decide el líder.**


Salen de escribir el contrato de perfil de usuario en `S2-T12`. **Ninguno se
resolvió aquí.**

**`H-13` · El `mensaje` de los errores de autenticación lleva el token.**
- `traducirErrorAuth` y `traducirErrorFicha` guardan `error.message` en
  `Resultado.Error.mensaje`.
- En `supabase-kt` 3.0.3, `RestException.message` incluye las cabeceras de la
  petición, y entre ellas van `Authorization: Bearer <token>` y la `anon key`.
- Hoy ninguna pantalla pinta ni registra ese `mensaje`, así que el token no
  sale del proceso. El día que alguien lo mande a un registro o a un reporte
  de fallas, el token va con él.
- `ErroresDatos.kt`, de `S2-T12`, ya usa `RestException.error` y tiene una
  prueba que lo cuida. `ErroresAuth.kt` no se tocó: es de otra tarea.
  **Lo decide el líder.**

**`H-14` · `public.usuarios` no valida el contenido de nombre, apellidos ni
teléfono.**
- No hay `CHECK` que impida un nombre vacío: `nombre = ''` devuelve `200`,
  probado el 2026-10-11. El teléfono solo tiene el tope de 20 caracteres.
- Lo único que lo impide es `ValidacionesAuth`, en la aplicación, y la `anon
  key` es pública: cualquiera con una cuenta puede escribir directo.
- El daño se limita a la ficha propia. Cerrarlo es un cambio de
  `01_esquema.sql`. **Lo decide el líder.**
