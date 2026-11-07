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
| 5 | Si la importación salió bien y `esEnlaceDeRecuperacion` encuentra `type=recovery` en el fragmento, `MainActivity` avisa por `AvisosEnlace` (`ui/navegacion`). `SesionViewModel` espera el primer valor no nulo de `sesionActual()` y entrega el rol; `GrafoNavegacion` deja el marcador temporal con ese rol y navega a P-18 con la pila limpia. P-18 abre con la sección de contraseña desplegada. Si P-01 todavía no decidía, ya no decide | `S2-T11` |
| 6 | P-18 pide solo la contraseña nueva, con el mínimo de 8 del registro, y llama a `cambiarContrasena(nueva)` | `S2-T11` |

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
Verificado en emulador, en frío y con la aplicación abierta. `S2-T11` no
agrega ningún aviso, porque su ticket no lo pide: el usuario puede pedir otro
enlace en P-04. Si se quiere avisar, lo decide el líder.

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
| `same_password` | `cambiarContrasena` | `VALIDACION`. P-18 pinta "La contraseña nueva es igual a la actual" debajo del campo (`S2-T11`). Como `weak_password` no debería llegar, P-18 trata todo `VALIDACION` de `cambiarContrasena` como este caso |
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
| `subirFotoPerfil(bytes)` | Storage, cubeta `perfiles`, ruta `<uid>/<milisegundos>.jpg`; luego `usuarios.foto_url`, y al final borra el resto de la carpeta | `Resultado<String>`, la URL pública nueva (`S2-T14`) |

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

Escrito a detalle en `S2-T14`. Recibe el JPEG de 512 × 512 que arma P-18
(`S2-T13`). **La foto anterior se borra**: lo decidió el líder el 2026-10-11.

```kotlin
val cubeta = storage.from("perfiles")
val ruta = "$uid/${System.currentTimeMillis()}.jpg"
cubeta.upload(ruta, bytes) { contentType = ContentType.Image.JPEG }   // 1. subir
val url = cubeta.publicUrl(ruta)
from("usuarios").update({ set("foto_url", url) }) {                    // 2. apuntar
    filter { eq("id", uid) }
    select()
}
cubeta.delete(cubeta.list(uid).map { "$uid/${it.name}" } - ruta)       // 3. limpiar
```

- **La ruta empieza con el uid**: las políticas de `03_almacenamiento.sql`
  solo dejan escribir y borrar dentro de `perfiles/<uid>/`.
- **Un nombre nuevo cada vez, sin `upsert`.** Con un nombre fijo la URL no
  cambiaría y Coil seguiría pintando la foto vieja.
- **El orden importa**: la ficha nunca apunta a un archivo que no existe.

| Si falla… | Qué pasa |
|---|---|
| 1, subir | Nada cambió. Se devuelve el error |
| 2, apuntar | Se borra el archivo recién subido y se devuelve el error. La ficha sigue con la foto anterior, que no se tocó |
| 3, limpiar | **Es un éxito igual**: la foto nueva ya está y la ficha ya apunta a ella. Lo que quedó se borra en la siguiente subida, porque el paso 3 borra todo lo que no sea la foto nueva |

**Errores de Storage.** Storage contesta todos sus rechazos con HTTP `400`, y
pone el código real como texto dentro del cuerpo (verificado el 2026-10-11):

| Motivo | Cuerpo |
|---|---|
| Ruta repetida sin `upsert` | `"409"`, `KeyAlreadyExists` |
| Carpeta de otro uid | `"403"`, `new row violates row-level security policy` |
| Tipo que no es JPG, PNG ni WebP | `"415"`, `InvalidMimeType` |
| Más de 5 MB | `"413"`, `EntityTooLarge` |

`storage-kt` 3.0.3 espera ese código como número y no lo recupera, así que
desde la aplicación no se distingue el motivo (`H-17`). `traducirErrorStorage`
(`ErroresStorage.kt`) traduce así:

| Qué llega | `TipoError` |
|---|---|
| Sin sesión (antes de ir a la red) | `AUTENTICACION` |
| `HttpRequestException`, `HttpRequestTimeoutException` | `RED` |
| Cualquier `RestException` de Storage | `SERVIDOR` |
| Error de PostgREST en el paso 2 | El de `traducirErrorDatos`. Cero filas, `AUTENTICACION` |
| Cualquier otra cosa | `DESCONOCIDO` |

La aplicación evita los cuatro motivos: sube JPEG de 512 × 512, a su carpeta y
con un nombre nuevo. El `mensaje` es `RestException.error`, nunca `message`
(`H-28`).

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
(`H-28`).

**La base no valida el contenido.** `public.usuarios` no tiene `CHECK` sobre
`nombre`, `apellidos` ni `telefono`: un nombre vacío devuelve `200`. Lo único
que lo impide hoy es la validación de la aplicación.

## Perfil del trabajador · `RepositorioTrabajador`

Escrito a detalle en `S3-T08`, contra `supabase-kt` 3.0.3 y contra los scripts
de `basedatos/`. **Lo implementa `RepositorioTrabajadorReal` (`S3-T09`)**, que lo
probó contra el proyecto el 2026-10-20 con `RepositorioTrabajadorServiciosRealTest`.
Lo que se verificó está al final de "Servicios", en "Qué se verificó".

### Resumen

| Operación | Con qué | Devuelve |
|---|---|---|
| `obtenerPerfilPublico(id)` | RPC `fn_perfil_publico_trabajador(p_trabajador_id)` | `Resultado<PerfilPublicoTrabajador>` |
| `obtenerMiPerfil()` | `select` sobre `perfiles_trabajador` con `usuario_id = <uid>` | `Resultado<PerfilTrabajador?>`; `null` si aún no hay perfil |
| `obtenerMisHabilidades()` | `select` sobre `perfil_habilidades` con `perfil_id = <uid>`, ordenadas por `habilidad` | `Resultado<List<String>>`, vacía si no hay |
| `guardarMiPerfil(perfil)` | `upsert` sobre `perfiles_trabajador`, conflicto en `usuario_id` | `Resultado<PerfilTrabajador>`, ya guardado |
| `reemplazarHabilidades(lista)` | `delete` y luego `insert` sobre `perfil_habilidades` | `Resultado<List<PerfilHabilidad>>` |

**El `<uid>` sale de la sesión de `supabase-kt`**, como en "Usuario", y nunca
del objeto que manda la pantalla: `guardarMiPerfil` ignora
`perfil.usuarioId`. Sin sesión, `AUTENTICACION` sin ir a la red.

**El repositorio no valida ni normaliza.** Las reglas de `ValidacionesTrabajador`
y la normalización de 1.6 las aplica `MiPerfilViewModel` (`S3-T07`).

**Leer siempre lleva el filtro del uid.** Las políticas "perfil de trabajador
visible" y "habilidades visibles" dejan leer las filas de **todos** los
trabajadores, porque P-07 las necesita. Una consulta sin
`eq("usuario_id", uid)` devuelve los perfiles de todo el mundo, sin error.

### `obtenerMiPerfil`

```kotlin
from("perfiles_trabajador")
    .select { filter { eq("usuario_id", uid) } }
    .decodeList<PerfilTrabajadorDto>()
    .firstOrNull()
    ?.aPerfilTrabajador()
```

`PerfilTrabajadorDto` trae las once columnas con su `@SerialName`:
`usuario_id`, `rol`, `titulo`, `descripcion`, `experiencia_anios`,
`telefono_contacto`, `estado_id`, `municipio_id`, `disponible`, `creado_en` y
`actualizado_en`.

**Cero filas es `Exito(null)`, no `AUTENTICACION`.** Es al revés que en
"Usuario": un trabajador sin perfil es el estado normal de quien acaba de
registrarse (HU-06), y P-11 arranca en blanco con él (`DISENO-PERFIL-SERVICIOS.md`
1.3).

### `obtenerMisHabilidades`

```kotlin
from("perfil_habilidades")
    .select(Columns.list("habilidad")) {
        filter { eq("perfil_id", uid) }
        order("habilidad", Order.ASCENDING)
    }
    .decodeList<HabilidadDto>()
    .map { it.habilidad }
```

Cerró `H-19` (líder, 2026-10-18). P-11 tiene que pintar las habilidades ya
guardadas, y `reemplazarHabilidades` borra todas antes de insertar: si la
pantalla no las leyera, guardar el perfil las perdería. Sin perfil o sin
habilidades devuelve una lista vacía, no un error.

### `guardarMiPerfil`

```kotlin
from("perfiles_trabajador")
    .upsert(
        PerfilEscrituraDto(
            usuarioId = uid,
            titulo = perfil.titulo,
            descripcion = perfil.descripcion,
            experienciaAnios = perfil.experienciaAnios,
            telefonoContacto = perfil.telefonoContacto,
            estadoId = perfil.estadoId,
            municipioId = perfil.municipioId,
            disponible = perfil.disponible
        )
    ) {
        onConflict = "usuario_id"
        select()
    }
    .decodeSingle<PerfilTrabajadorDto>()
```

- **Es un `upsert`** porque el primer guardado crea la fila y los siguientes la
  editan (2.4). Necesita las dos políticas, "trabajador crea su perfil" y
  "trabajador edita su perfil", y las dos exigen `usuario_id = auth.uid()`.
- **Manda ocho columnas.** No manda `rol`: su valor por omisión es
  `'trabajador'` y `ck_perfil_rol` no admite otro. Tampoco las fechas: al
  insertar las pone `now()`, y al editar `creado_en` no se toca y
  `actualizado_en` lo pone `tg_perfiles_actualizado`.
- **Un `null` borra.** La descripción y el teléfono vacíos llegan como `null`
  desde el ViewModel (1.6) y tienen que viajar como `null` en el JSON. Por eso
  `PerfilEscrituraDto` **no tiene valores por omisión**: `kotlinx.serialization`
  no escribe una propiedad que vale lo mismo que su valor por omisión, y una
  descripción vaciada no se guardaría.
- `select()` devuelve la fila guardada, con las fechas que puso la base. No
  hay segunda lectura.
- **Si la cuenta no es de trabajador, falla con `409`.** La llave
  `fk_perfil_usuario_trabajador` amarra `(usuario_id, rol)` a
  `usuarios(id, rol)`. Las guardas de rol de `S2-T15` impiden llegar a P-11
  como cliente.

### `reemplazarHabilidades`

```kotlin
from("perfil_habilidades").delete { filter { eq("perfil_id", uid) } }   // 1. borrar todas
if (lista.isEmpty()) return Resultado.Exito(emptyList())
from("perfil_habilidades")                                               // 2. insertar las nuevas
    .insert(lista.map { HabilidadEscrituraDto(perfilId = uid, habilidad = it) }) { select() }
    .decodeList<HabilidadEscrituraDto>()
    .map { PerfilHabilidad(it.perfilId, it.habilidad) }
```

- **Va después de `guardarMiPerfil`.** `perfil_habilidades.perfil_id` apunta a
  `perfiles_trabajador`: sin la fila del perfil, el `insert` falla con `409`
  (2.4).
- **La lista llega normalizada** (1.6): sin repetidas, ni siquiera por
  mayúsculas. Una repetida rompe la llave primaria `(perfil_id, habilidad)`
  con `409`, y con ella todo el `insert`.
- **No es atómico.** Si 1 pasa y 2 falla, el perfil queda sin habilidades en
  la base. P-11 conserva la lista en pantalla y "Reintentar" repite los dos
  pasos (2.4), así que nada se pierde mientras no se cierre la pantalla. Ver
  `H-26`.

| Si falla… | Qué pasa |
|---|---|
| 1, borrar | Nada cambió. Se devuelve el error |
| 2, insertar | El perfil quedó sin habilidades. Se devuelve el error y P-11 ofrece "Reintentar", que repite solo este paso (`soloFaltanHabilidades`) |

### `obtenerPerfilPublico`

```kotlin
postgrest.rpc(
    "fn_perfil_publico_trabajador",
    buildJsonObject { put("p_trabajador_id", id) }
).decodeAsOrNull<PerfilPublicoDto>()
```

Es RPC y no cinco consultas porque **P-07 necesita perfil, habilidades,
servicios, fotos, calificación y reseñas de un golpe**. Solo trae los
servicios activos, con sus fotos como lista de URL en orden de posición.
Devuelve:

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

- **Sin perfil, o con la cuenta inactiva, la función devuelve `null`**, con
  `200`. Se traduce a `Resultado.Error(VALIDACION, "Trabajador no
  encontrado")`, como ya hace el repositorio falso.
- **`estado` y `municipio` llegan `null`** si el trabajador no los ha elegido:
  la función los une con `left join`. `PerfilPublicoTrabajador` los declara no
  nulos. Ver `H-25`.

## Servicios · `RepositorioServicios`

Escrito a detalle en `S3-T08`. **Lo implementa `RepositorioServiciosReal`
(`S3-T09`)**, probado contra el proyecto igual que "Perfil del trabajador".

### Resumen

| Operación | Con qué | Devuelve |
|---|---|---|
| `obtenerMisServicios()` | `select` sobre `servicios` con `servicio_fotos` embebidas, `perfil_id = <uid>` | `Resultado<List<Servicio>>`, activos y pausados, cada uno con sus fotos |
| `crear(servicio)` | `insert` sobre `servicios` | `Resultado<Servicio>`, con el `id` nuevo y sin fotos |
| `actualizar(servicio)` | `update` sobre `servicios` con `id = servicio.id` | `Resultado<Servicio>`, con sus fotos |
| `pausar(id, activo)` | `update servicios set activo` | `Resultado<Servicio>`, con sus fotos |
| `eliminar(id)` | Lee las fotos, borra la fila y después los archivos | `Resultado<Unit>` |
| `subirFoto(servicioId, bytes, posicion)` | Storage, cubeta `servicios`, ruta `<uid>/<servicioId>/<milisegundos>.<ext>`; luego `servicio_fotos` | `Resultado<ServicioFoto>` |
| `eliminarFoto(fotoId)` | Borra la fila de `servicio_fotos` y después el archivo | `Resultado<Unit>` |

El uid, la falta de validación y el filtro obligatorio al leer valen igual que
en "Perfil del trabajador": la política "servicios visibles" deja leer los
servicios de todos.

**Máximo 3 fotos por servicio, y lo impone la base**, no la aplicación:
`ck_foto_posicion` (posición de 1 a 3) más `uq_foto_posicion_unica`
(`servicio_id`, `posicion`). Storage no sabe contar fotos.

### `obtenerMisServicios`

```kotlin
from("servicios")
    .select(Columns.raw("*, servicio_fotos(*)")) {
        filter { eq("perfil_id", uid) }
        order("posicion", Order.ASCENDING, referencedTable = "servicio_fotos")
    }
    .decodeList<ServicioDto>()
    .map { it.aServicio() }
```

- **Las fotos vienen embebidas: así se cerró `H-18`** (líder, 2026-10-19).
  `ServicioDto` lleva `@SerialName("servicio_fotos") val fotos:
  List<ServicioFotoDto>`, y `Servicio` ganó `fotos: List<ServicioFoto>`. P-13
  las necesita con su `id` para pintarlas y quitarlas al editar, y P-12 pinta
  la de la posición 1 en la tarjeta. Es una sola petición: PostgREST arma la
  relación por la llave `servicio_fotos.servicio_id`.
- **`aServicio()` vuelve a ordenar las fotos por posición**, para no depender de
  que el `order` sobre la tabla embebida llegue como se pidió.
- Trae activos y pausados: P-12 muestra los dos (HU-10). El orden de la lista lo
  pone la pantalla, con `ordenarMisServicios`.
- Sin servicios devuelve una lista vacía, no un error: es el vacío de P-12.

### `crear`

```kotlin
from("servicios")
    .insert(
        ServicioEscrituraDto(
            perfilId = uid,
            categoriaId = servicio.categoriaId,
            titulo = servicio.titulo,
            descripcion = servicio.descripcion,
            precioDesde = servicio.precioDesde,
            precioHasta = servicio.precioHasta,
            unidadPrecio = servicio.unidadPrecio
        )
    ) { select() }
    .decodeSingle<ServicioDto>()
```

- **Manda siete columnas.** No manda `id` (`gen_random_uuid()`), `activo`
  (`true` por omisión) ni las fechas. `servicio.id` y `servicio.perfilId` se
  ignoran.
- **Los precios viajan como texto con punto y dos decimales**, `"350.50"`, con
  un serializador propio de `BigDecimal`: `kotlinx.serialization` no trae uno,
  y pasarlos por `Double` pierde exactitud. PostgREST convierte el texto a
  `numeric`. Al leer, el número del JSON se convierte con `toBigDecimal()`
  desde su texto, nunca desde `Double`.
- Como en `guardarMiPerfil`, `ServicioEscrituraDto` no tiene valores por
  omisión, para que un precio `null` viaje como `null`.
- Devuelve el servicio con `fotos` vacía: todavía no tiene ninguna. P-13 pasa a
  modo editar con ese `id` (4.1).
- **Si el trabajador todavía no guarda su perfil, falla con `409`.**
  `servicios.perfil_id` apunta a `perfiles_trabajador`, no a `usuarios`. Ver
  `H-27`.

### `actualizar`

```kotlin
from("servicios")
    .update({
        set("categoria_id", servicio.categoriaId)
        set("titulo", servicio.titulo)
        set("descripcion", servicio.descripcion)
        set("precio_desde", servicio.precioDesde?.toPlainString())
        set("precio_hasta", servicio.precioHasta?.toPlainString())
        set("unidad_precio", servicio.unidadPrecio)
    }) {
        filter { eq("id", servicio.id) }
        select(Columns.raw("*, servicio_fotos(*)"))
    }
    .decodeList<ServicioDto>()
```

- **Manda seis columnas y ninguna más.** `activo` lo cambia `pausar`; las
  fechas, la base (`tg_servicios_actualizado`). `perfil_id` no se manda: la
  política exige que siga siendo el uid, y no hay por qué tocarlo.
- `set(..., null)` borra el precio o la unidad.
- **La respuesta trae las fotos embebidas**, para que P-12 no pierda la
  miniatura al recibir el servicio actualizado.
- **Cero filas es `VALIDACION`**, no `AUTENTICACION` como en "Usuario". Aquí es
  un caso normal: el servicio se borró desde otro teléfono (1.3). RLS también
  filtra el servicio ajeno, y no se distingue del borrado. El repositorio falso
  responde igual.

### `pausar`

```kotlin
from("servicios")
    .update({ set("activo", activo) }) {
        filter { eq("id", id) }
        select(Columns.raw("*, servicio_fotos(*)"))
    }
    .decodeList<ServicioDto>()
```

Cero filas, `VALIDACION`, igual que `actualizar`. Pausar no borra nada: el
servicio pausado deja de salir en la búsqueda y en P-07 porque la vista y la
RPC filtran `activo` (HU-10).

### `eliminar`

El orden lo cerró el líder en `H-06` (2026-10-19): **la fila primero, el
archivo después**, con las URL leídas antes. Así ninguna fila apunta a un archivo que ya no existe; lo
peor que queda es un archivo sin fila, que nadie ve.

```kotlin
val urls = from("servicio_fotos")                                       // 1. leer las fotos
    .select(Columns.list("url")) { filter { eq("servicio_id", id) } }
    .decodeList<UrlDto>()
val borrados = from("servicios")                                        // 2. borrar la fila
    .delete { filter { eq("id", id) }; select() }
    .decodeList<ServicioDto>()
if (borrados.isEmpty()) return Resultado.Error(TipoError.VALIDACION, "Servicio no encontrado")
storage.from("servicios").delete(urls.map { rutaEnCubeta(it.url) })     // 3. borrar los archivos
```

- **El paso 1 va antes porque la cascada no avisa.** `servicio_fotos` cae con
  `on delete cascade` al borrar el servicio, y con ella las URL. Después del
  paso 2 ya no hay cómo saber qué archivos borrar: eso era `H-06`.
- **`rutaEnCubeta(url)`** es lo que sigue a `/object/public/servicios/` en la
  URL pública: `<uid>/<servicioId>/<archivo>`. La tabla guarda la URL, no la
  ruta.

| Si falla… | Qué pasa |
|---|---|
| 1, leer | Nada cambió. Se devuelve el error |
| 2, borrar la fila | Nada cambió. Se devuelve el error. Cero filas, `VALIDACION` |
| 3, borrar los archivos | **Es un éxito igual**: el servicio ya no existe para nadie. Los archivos se quedan en la cubeta sin fila que los nombre. Nada los vuelve a borrar; limpiarlos es manual, desde la consola, y no urge |

### `subirFoto`

Sigue el orden que `03_almacenamiento.sql` pide y que ya usa
`subirFotoPerfil`: **primero el archivo, después la fila.**

```kotlin
val tipo = tipoDeImagen(bytes)                                          // JPEG, PNG o WebP
    ?: return Resultado.Error(TipoError.VALIDACION, "Tipo de imagen no admitido")
val cubeta = storage.from("servicios")
val ruta = "$uid/$servicioId/${System.currentTimeMillis()}.${tipo.extension}"
cubeta.upload(ruta, bytes) { contentType = tipo.contentType }           // 1. subir
from("servicio_fotos")                                                  // 2. registrar
    .insert(FotoEscrituraDto(servicioId = servicioId, url = cubeta.publicUrl(ruta), posicion = posicion)) {
        select()
    }
    .decodeSingle<ServicioFotoDto>()
```

- **La ruta empieza con el uid**: las políticas de la cubeta solo dejan
  escribir y borrar dentro de `servicios/<uid>/`. La carpeta del servicio
  agrupa sus fotos para limpiarlas a mano si algún paso 3 falla.
- **Los bytes llegan tal como el usuario los eligió**: JPG, PNG o WebP de hasta
  5 MB, sin recorte ni recompresión (4.2). El tipo se lee de los primeros bytes:
  `FF D8 FF` es JPEG, `89 50 4E 47` es PNG, y `RIFF` con `WEBP` en el byte 8 es
  WebP. Cualquier otro no se sube. P-13 ya lo rechazó antes, con `LimitesFoto`.
- **La posición la elige P-13**: la primera libre. Si otra ya la ocupa, o pasa
  de 3, el paso 2 falla con `409` o `400`.
- **Sin avance, así se cerró `H-20`** (líder, 2026-10-19). Se usa `upload`, no
  `uploadAsFlow`, y P-13 pinta un indicador indeterminado sobre la miniatura.
  HU-11 se cumple así; con fotos de 5 MB como máximo, el porcentaje no se
  justifica.
- **Cancelar es cancelar la corrutina** (4.4). Si la cancelación llega antes
  del paso 2, el archivo puede quedar sin fila. Si llega después, la foto quedó
  guardada y aparece la próxima vez que se abra P-13.

| Si falla… | Qué pasa |
|---|---|
| 1, subir | Nada cambió. Se devuelve el error de Storage |
| 2, registrar | Se borra el archivo recién subido y se devuelve el error. Si el borrado también falla, el archivo se queda sin fila; nadie lo ve |

### `eliminarFoto`

```kotlin
val borradas = from("servicio_fotos")                                   // 1. borrar la fila
    .delete { filter { eq("id", fotoId) }; select() }
    .decodeList<ServicioFotoDto>()
if (borradas.isEmpty()) return Resultado.Error(TipoError.VALIDACION, "Foto no encontrada")
storage.from("servicios").delete(rutaEnCubeta(borradas.single().url))   // 2. borrar el archivo
```

- **La fila primero, por `H-06`.** El `delete` con `select()` devuelve la fila
  borrada, y de ahí sale la URL: no hace falta leerla antes.
- **Cero filas, `VALIDACION`**: la foto ya no está, o no es de un servicio
  propio. La política usa `fn_es_dueno_del_servicio`.

| Si falla… | Qué pasa |
|---|---|
| 1, borrar la fila | Nada cambió. La miniatura se queda en P-13 y sale el aviso (4.4) |
| 2, borrar el archivo | **Es un éxito igual**: la foto ya no está en el servicio. El archivo se queda sin fila |

### Errores de `RepositorioTrabajador` y `RepositorioServicios`

Los dos usan `traducirErrorDatos` para PostgREST y `traducirErrorStorage`
para la cubeta, las mismas de "Usuario". Se clasifica por código HTTP, porque
`supabase-kt` 3.0.3 no deja pasar el código de Postgres.

| Qué llega | Cuándo pasa aquí | `TipoError` |
|---|---|---|
| `HttpRequestException`, `HttpRequestTimeoutException` | Sin red, tiempo agotado | `RED` |
| Sin usuario en `auth` | No hay sesión abierta | `AUTENTICACION` |
| `401` | El token no se puede leer o venció | `AUTENTICACION` |
| `403` | RLS rechaza la escritura: un servicio o una foto ajenos. **Es un defecto nuestro**: la aplicación solo escribe lo propio | `AUTENTICACION` |
| `400` | Un `CHECK` o un valor que no cabe: experiencia fuera de 0 a 70, precio negativo, hasta menor que desde, posición fuera de 1 a 3, un texto más largo que su columna | `VALIDACION` |
| `409` | Una llave: habilidad repetida, posición ocupada, perfil inexistente (`H-27`), categoría inexistente, cuenta que no es de trabajador | `VALIDACION` |
| Cero filas en `actualizar`, `pausar`, `eliminar` o `eliminarFoto` | El servicio o la foto ya no están, o no son propios | `VALIDACION` |
| Cero filas en `obtenerMiPerfil` | Todavía no hay perfil | **`Exito(null)`**, no es error |
| `null` de `fn_perfil_publico_trabajador` | Ese trabajador no tiene perfil o su cuenta está inactiva | `VALIDACION` |
| Cualquier `RestException` de Storage | Ver "Errores de Storage" en `subirFotoPerfil` (`H-17`) | `SERVIDOR` |
| `5xx` | El servidor falla | `SERVIDOR` |
| Cualquier otra cosa | | `DESCONOCIDO` |

**El `mensaje` es `RestException.error`, nunca `message`** (`H-28`).

**Estos `VALIDACION` no traen un texto en español.** Las cuatro tablas no
tienen triggers con mensaje propio: un `CHECK` o una llave que fallan devuelven
el texto de Postgres, en inglés. Por eso P-11, P-12 y P-13 pintan siempre el
mensaje genérico de `VALIDACION` (`obtenerMensajeErrorRes`) y nunca el
`mensaje`. Cuando `DISENO-PERFIL-SERVICIOS.md` §6 dice "con el mensaje del
trigger", en estas tablas eso es el mensaje genérico. Las validaciones de
`S3-T07` evitan casi todos estos casos antes de salir de la aplicación.

### Qué se verificó

`S3-T08` dejó cuatro cosas dadas por hecho sin haberlas visto. `S3-T09` las
probó contra el proyecto el 2026-10-20, con `beto.rls@…`, en
`RepositorioTrabajadorServiciosRealTest`:

1. **El embebido en la respuesta de un `update` funciona.** `actualizar` y
   `pausar` devuelven el servicio con sus `servicio_fotos`; no hace falta
   volver a leerlo.
2. **Los `null` viajan.** Guardar el perfil con `descripcion = null` la borra,
   y actualizar un servicio con `precio_desde = null` también. Los DTO de
   escritura sin valores por omisión funcionan como se esperaba.
3. **Los códigos son los de la tabla.** Un `CHECK` (experiencia de 71, hasta
   menor que desde) llega como `400` y una llave (habilidad repetida, posición
   de foto ocupada) como `409`: las dos van a `VALIDACION`. El `403` de RLS no
   se provocó, porque la aplicación no tiene cómo escribir lo ajeno.
4. **Storage, a medias.** Subir, quitar y eliminar dejan la cubeta como dice
   `H-06`: la foto rechazada por posición ocupada no deja archivo, y quitar una
   foto o eliminar el servicio borra los suyos. Los cuatro rechazos propios de
   la cubeta no se provocaron: el tipo no admitido se detiene antes de subir,
   y los otros tres la aplicación no los produce.

Los precios llegan exactos: `350.50` se guarda y se lee como `350.50`.

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

**Los tres catálogos ya son reales desde `S3-T09`**, en
`RepositorioCatalogosReal`, por decisión del líder del 2026-10-19. Los ids de
municipio del catálogo falso no coinciden con los de la base: el falso numera
Cholula como 19, y en la base el 19 es Naucalpan. Con el perfil real y el
catálogo falso, P-11 habría guardado el municipio equivocado, y la base no lo
rechaza porque no revisa que el municipio sea del estado. Las tres son un
`select` de la tabla:

| Operación | Filtro y orden |
|---|---|
| `obtenerCategorias()` | `activa = true`, por `orden` |
| `obtenerEstados()` | Todos, por `nombre` |
| `obtenerMunicipios(estadoId)` | `estado_id = estadoId`, por `nombre` |

Se guardan en memoria después de la primera lectura buena: no cambian
mientras la aplicación está abierta. **`buscarTrabajadores` sigue en el
falso** hasta `S4-T09`; `RepositorioCatalogosReal` se lo pasa al falso tal cual.

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

### Detalle de `generar` (`S3-T12`)

Escrito a detalle en `S3-T12`, contra `basedatos/01_esquema.sql` y contra
`PROMPT-REDACCION.md`. **Lo construye `S4-T10`.** Hasta entonces `generar` lo
resuelve `RepositorioIaFalso`.

| Operación | Con qué | Devuelve |
|---|---|---|
| `generar(funcion, entrada)` | `POST functions/v1/ia-generar` | `Resultado<ResultadoIa>` |

`ResultadoIa` ya existe en `ModelosRepositorio.kt`: `resultado`, `desdeCache` y
`llamadasRestantesHoy`.

**La aplicación solo manda `funcion` y `entrada`.** El usuario sale del JWT de
la sesión de `supabase-kt`, nunca del cuerpo. Sin sesión, `AUTENTICACION` sin
ir a la red.

#### La petición

```
POST functions/v1/ia-generar
Authorization: Bearer <JWT de la sesión>
{ "funcion": "redactar_perfil" | "redactar_servicio" | "categorizar" | "sugerir",
  "entrada": "Oficio: Plomero\nAños de experiencia: 10\nHabilidades: Boilers; Fugas" }
```

- `funcion` toma los mismos valores que el enum `funcion_ia` de la base. Cualquier
  otro valor es un `400`.
- `entrada` es una sola cadena. Para `redactar_perfil` y `redactar_servicio` la
  arma la aplicación con el formato de `PROMPT-REDACCION.md`, sección 2: renglones
  `Etiqueta: valor`, siempre en el mismo orden. `categorizar` y `sugerir` no
  tienen formato todavía: son F2 y F3.
- **Tope de largo: 1000 caracteres** (propuesta, `H-35`). Más que eso es un `400`.

#### Qué hace la función, en este orden

Los pasos 1 a 6 **no pagan nada**. Solo el paso 7 le pega a OpenAI.

1. **Valida la sesión.** Sin JWT válido, `401`.
2. **Valida la forma.** `funcion` conocida, `entrada` no vacía después de
   `trim()`, no más larga que el tope, y los renglones obligatorios de la función:
   `Oficio` en `redactar_perfil`; `Servicio` y `Categoría` en `redactar_servicio`.
   Si falla, `400`: la aplicación ya valida esto, así que un `400` es un bug nuestro.
3. **Normaliza la entrada** igual que `PROMPT-REDACCION.md` sección 2 (`trim()` y
   espacios colapsados) y calcula el SHA-256. La llave de la caché es
   `(funcion, entrada_hash)`, como en `ia_cache`.
4. **Valida el título** (`H-33`). Si el valor de `Oficio` o de `Servicio` no es un
   trabajo de oficio que alguien ofrece (una orden, una pregunta, un texto sin
   sentido, algo ilegal), responde `422` con el código `ENTRADA_NO_VALIDA`. **No
   consulta la caché, no cuenta para el tope y no se guarda.** Con qué mecanismo
   se decide lo elige `S4-T10`; se prueba con los casos 7, 8 y 13 de
   `RESULTADOS-GPT.md`, que deben terminar aquí.
5. **Consulta `ia_cache`.** Si está, responde `200` con `desde_cache: true` y sin
   pagar ni contar nada. Si lo guardado es `NO_PUEDO_REDACTAR`, responde el `422`
   del paso 8.
6. **Registra la llamada** con `fn_ia_registrar_llamada(uid, funcion, 0)`. Si
   devuelve `-1`, responde `429`.
7. **Llama a OpenAI** con la instrucción de `PROMPT-REDACCION.md` sección 3 como
   *system prompt* y la `entrada` como mensaje del usuario, con los parámetros de
   la sección 4. Si no responde a tiempo o falla, `503`.
8. **Revisa la respuesta.** Con `trim()`, si es exactamente `NO_PUEDO_REDACTAR`,
   la guarda en `ia_cache` y responde `422` con `ENTRADA_NO_VALIDA`. Si no, la
   guarda en `ia_cache` y responde `200` con `desde_cache: false`.

#### Las respuestas

```
200 → { "resultado": "...", "desde_cache": false, "llamadas_restantes_hoy": 7 }
400 → { "error": { "codigo": "PETICION_INVALIDA", "mensaje": "..." } }
401 → { "error": { "codigo": "NO_AUTENTICADO", "mensaje": "..." } }
422 → { "error": { "codigo": "ENTRADA_NO_VALIDA", "mensaje": "..." } }
429 → { "error": { "codigo": "LIMITE_DIARIO_IA", "mensaje": "Alcanzaste el límite de hoy" } }
503 → { "error": { "codigo": "IA_NO_DISPONIBLE", "mensaje": "..." } }
```

| Respuesta | `TipoError` | Qué ve el usuario |
|---|---|---|
| `200` | no hay error | El texto, pegado en el campo |
| `400` | `DESCONOCIDO` | "Algo salió mal, intenta de nuevo". **Es un bug nuestro: no se tapa con un mensaje bonito** |
| `401` | `AUTENTICACION` | La sesión venció: la aplicación sigue el camino normal de `AUTENTICACION` |
| `422` | `VALIDACION` provisional (`H-37`) | "La IA no pudo redactar con esos datos, escríbelo tú" |
| `429` | `LIMITE_IA` | "Alcanzaste el límite de hoy" |
| `503` | `SERVIDOR` | "Algo falló de nuestro lado, intenta más tarde" |
| Sin red, tiempo agotado | `RED` | "Revisa tu conexión e intenta de nuevo" |

**`NO_PUEDO_REDACTAR` nunca llega a la pantalla como texto** (`H-30`, resuelto
aquí). La función la convierte en `422`, y el repositorio, por si acaso, trata
también un `200` cuyo `resultado` sea exactamente esa cadena como el mismo error.
La mentira de "La IA no respondió, puedes escribirlo tú" no aplica en este caso:
sí respondió, y dijo que no.

**Qué cuenta para el tope, qué se guarda y qué no:**

| Caso | Cuenta para el tope | Se guarda en `ia_cache` |
|---|---|---|
| Rechazo del paso 4 (título) | No | No |
| Acierto de caché (paso 5) | No | Ya estaba |
| `NO_PUEDO_REDACTAR` del modelo (paso 8) | **Sí**: se pagó | **Sí**, para no pagarlo otra vez |
| Texto normal (paso 8) | Sí | Sí |
| `503` de OpenAI | Sí: el paso 6 ya registró | No |

**El tope sigue siendo de 10 llamadas por usuario al día en total** (`DEC-18`), no
por función. En un acierto de caché, `llamadas_restantes_hoy` lo calcula la
función leyendo `ia_consumo` con la `service_role` (`H-36`).

**La llave de OpenAI vive solo en las variables de entorno de la función.** Ni
este contrato, ni la aplicación, ni el repositorio la llevan.

#### Hallazgos de `S3-T12`

Salen de escribir este contrato. **Ninguno se resolvió aquí**: los decide el líder
o la tarea que se nombra.

**`H-35` · Nada limita el largo del borrador que se manda a la IA.**
`perfiles_trabajador.descripcion` y `servicios.descripcion` son `text` sin tope en
el esquema, y `ValidacionesTrabajador` solo acota títulos y habilidades. Un
borrador largo encarece cada llamada. Este contrato pone un tope de **1000
caracteres** en la `entrada`, como propuesta. **Lo confirma el líder**, o el
tope se mueve a los campos de origen.

**`H-36` · La función no tiene cómo saber lo que queda ni cuántos tokens gastó.**
`fn_ia_registrar_llamada` solo registra y devuelve lo que queda *después* de
registrar, y recibe los tokens **antes** de llamar a OpenAI, cuando aún no se
conocen. Con un acierto de caché no hay función que consulte lo que queda: hay
que sumar `ia_consumo` a mano y repetir el `10` que ya vive en SQL. Y la columna
`tokens` queda siempre en `0`. **Le toca a `S4-T10` o al líder**: una función
SQL de consulta y una forma de sumar los tokens después.

**`H-37` · `TipoError` no tiene un valor para "la IA rechazó la entrada".** Hoy se
traduce a `VALIDACION`, pero `VALIDACION` pinta el mensaje del trigger, que aquí
no existe. Hace falta un `TipoError` nuevo y su mensaje en `strings.xml`, como se
hizo con `CORREO_DUPLICADO` (`DEC-28`). **Lo decide el líder y lo aplica
`S3-T13`.**

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
| 422 de la Edge Function (`ENTRADA_NO_VALIDA`) | `VALIDACION` provisional (`H-37`) | "La IA no pudo redactar con esos datos, escríbelo tú" |
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
columna exactos y los DTO: S2-T06 autenticación, S2-T12 perfil de usuario y S3-T08 trabajador y
servicios (ya escritos, arriba), S3-T12 la Edge Function de IA (ya escrito, arriba), S4-T08 solicitudes
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

**`H-28` · El `mensaje` de los errores de autenticación lleva el token.**
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

Salen de escribir el contrato de trabajador y servicios en `S3-T08`.
**Ninguno se resolvió aquí.**

**`H-25` · El perfil público de un trabajador sin estado o sin municipio no
se puede leer.**
- `fn_perfil_publico_trabajador` une `estados` y `municipios` con `left join`:
  si el trabajador no los ha elegido, `estado` y `municipio` llegan `null`.
- `PerfilPublicoTrabajador` los declara `String`, no nulos. La deserialización
  falla, y P-07 de ese trabajador sale como error.
- Pasa justo en la vista previa de HU-08: un trabajador que guardó su perfil
  sin ubicación y pulsa "Ver mi perfil como cliente". Es lo que ya anotaba
  `H-19` cuando se descartó usar esta RPC en P-11.
- La salida simple es volver nulos los dos campos del modelo y que P-07 pinte
  el vacío; eso es de `S3-T14` y `S4-T03`. **Lo decide el líder.**

**`H-26` · `reemplazarHabilidades` no es atómico.**
- Son dos peticiones: borrar todas y luego insertar. Si la segunda falla, el
  perfil queda sin habilidades en la base.
- P-11 no pierde nada mientras la pantalla siga abierta: conserva la lista y
  "Reintentar" repite el paso (2.4). Si el usuario sale antes de reintentar,
  las habilidades se perdieron.
- La salida completa es una función en la base que haga las dos cosas en una
  transacción, y eso es una operación nueva de `RepositorioTrabajador` y un
  cambio de `basedatos/`. Con lo que hay, el riesgo es bajo. **Lo decide el
  líder.**

**`H-27` · Un trabajador no puede publicar un servicio antes de guardar su
perfil.**
- `servicios.perfil_id` apunta a `perfiles_trabajador(usuario_id)`, no a
  `usuarios`. Sin la fila del perfil, `crear` falla con `409` y P-13 pinta el
  error genérico de `VALIDACION`, que no dice qué hacer.
- Es el camino natural de quien se acaba de registrar: la pestaña Servicios,
  "Publicar mi primer servicio", y P-13 ya está a la mano sin haber pasado por
  P-11. El repositorio falso no lo detecta, porque no revisa la llave.
- Salidas posibles: P-12 o P-13 piden guardar primero el perfil, o
  `guardarMiPerfil` se llama solo con un título mínimo. Cualquiera de las dos
  cambia el diseño de P-12 o P-13. **Lo decide el líder.**
