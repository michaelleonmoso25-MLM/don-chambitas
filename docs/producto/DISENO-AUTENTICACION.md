# Diseño de las pantallas de autenticación · P-02, P-03 y P-04

> Entregable de **`S2-T01`**. Es el documento que consumen `S2-T02` (registro),
> `S2-T03` (inicio de sesión), `S2-T04` (validaciones), `S2-T05` (ViewModels) y
> `S2-T10` (recuperar contraseña). Lo que aquí no esté, no se implementa; lo
> que aquí esté, se implementa tal cual.

**Fecha:** 2026-09-21 · **Responsable:** BCJL ·
**Rama:** `docs/S2-T01-diseno-pantallas-autenticacion`

---

## 0. Alcance de esta tarea

`docs/tareas/S2-T01.md` **no existe**: los tickets del Sprint 2 no se
redactaron. El líder autorizó avanzar sin ticket el 2026-09-21, así que el
alcance queda escrito aquí y hace las veces de ticket.

### Qué entra

`S1-T14` ya dibujó P-02, P-03 y P-04 y describió a dónde lleva cada toque; eso
está en [`WIREFRAMES.md`](WIREFRAMES.md) y **no se repite**. Esta tarea
convierte ese dibujo en algo implementable sin volver a decidir nada:

1. La anatomía de cada pantalla con medidas, en orden de arriba hacia abajo.
2. El contrato de estado y de eventos de cada pantalla: qué `Estado...`
   expone, qué eventos recibe, qué hace cada uno.
3. Qué estados de pantalla existen y cuáles no aplican, con el porqué.
4. Las reglas de validación campo por campo, cuándo se disparan y con qué
   texto exacto.
5. El mapa de `TipoError` a lo que ve el usuario en cada pantalla.
6. Las claves de `strings.xml` que hay que crear, con su texto.
7. Teclado, orden de foco y accesibilidad.

### Qué NO entra

- **Código.** Esta tarea no escribe Kotlin. Las pantallas las construyen
  `S2-T02`, `S2-T03` y `S2-T10`; los ViewModels, `S2-T05`.
- **Cadenas en `strings.xml`.** Se especifican aquí con su clave, pero las
  agrega la tarea que construye cada pantalla, para no dejar recursos sin usar.
- **El contrato de la API.** Es `S2-T06`. Aquí solo se consume
  `RepositorioAuth`, que ya existe desde `S1-T13`.
- **Supabase.** `S2-T01` no es una de las cinco tareas de implementación real
  (AGENTS.md §6). La fuente activa sigue siendo `FuenteDatosFalsa`.
- **P-18 (Mi cuenta) y el cambio de contraseña dentro de la aplicación.** Son
  `S2-T11` y HU-05.
- **Definir la contraseña nueva desde el enlace del correo.** Ese segundo
  tramo de HU-04 ocurre fuera de la aplicación (enlace de Supabase Auth) y no
  tiene pantalla en `PANTALLAS.md`. Ver el hallazgo H-09 al final.

### De dónde sale cada cosa

| Fuente | Qué aporta |
|---|---|
| `PANTALLAS.md` | Qué hace cada pantalla y el mapa P-01 → P-02 ↔ P-03 ↔ P-04 |
| `WIREFRAMES.md` | El dibujo y el comportamiento de cada elemento tocable |
| `DISENO.md` | Paleta, tipografía, espaciado, formas y catálogo de componentes |
| `HISTORIAS.md` | HU-01, HU-02 y HU-04 con sus criterios de aceptación |
| `CONTRATOS-API.md` | Firma de `RepositorioAuth` y tabla de errores |
| `basedatos/01_esquema.sql` | Restricciones de `public.usuarios` que la interfaz debe respetar antes de enviar |
| `CONVENCIONES.md` | Nombres de archivos, clases y pruebas |

---

## 1. Reglas comunes a las tres pantallas

### 1.1 Estructura

Las tres comparten el mismo esqueleto y **ninguna tiene barra inferior**: son
pantallas de sesión cerrada, y la barra inferior solo existe dentro de los
subgrafos de cliente y de trabajador (`PANTALLAS.md`).

```
Scaffold
 ├─ topBar: BarraSuperior (Mostaza, título Carbon)
 └─ content: Column con scroll vertical
      ├─ margen lateral 16 dp
      ├─ bloques separados 24 dp
      ├─ campos del mismo bloque separados 16 dp
      └─ relleno inferior 24 dp
```

| Medida | Valor | De dónde sale |
|---|---|---|
| Margen lateral de pantalla | 16 dp | `DISENO.md` · Espaciado |
| Separación entre bloques | 24 dp | Escala de 4 |
| Separación entre campos | 16 dp | Escala de 4 |
| Separación etiqueta ↔ mensaje de error | 4 dp | Ya implementado en `Campos.kt` |
| Alto del botón principal | 48 dp | `DISENO.md` · `BotonPrincipal` |
| Radio de botones y campos | 12 dp | `DISENO.md` · Formas |
| Fondo | `Crema` | `DISENO.md` · Colores |

**Scroll obligatorio en las tres.** Con el teclado abierto en un equipo de
360 × 640 dp, P-03 no cabe. La `Column` lleva `verticalScroll` y
`imePadding()`; sin eso el botón "Crear cuenta" queda debajo del teclado.

### 1.2 Componentes que se usan

Solo estos, todos de `DISENO.md` y ya construidos en `S1-T10` y `S1-T11`:

`BarraSuperior`, `CampoTexto`, `CampoContrasena`, `ChipCategoria`,
`BotonPrincipal`, `BotonTexto`, `EstadoError`.

**Ningún componente nuevo.** Si al implementar parece faltar uno, se detiene la
tarea y se reporta: primero se agrega a `DISENO.md`.

### 1.3 Los cuatro estados, y cuál no aplica

`DISENO.md` exige cargando, vacío, error y contenido en **toda pantalla con
datos**. Las tres de autenticación son formularios: no listan datos, así que
el estado vacío no tiene qué representar. Queda escrito para que no se discuta
en revisión de pull request:

| Estado | P-02 | P-03 | P-04 | Cómo se pinta |
|---|---|---|---|---|
| Contenido | sí | sí | sí | El formulario. Es el estado por omisión |
| Cargando | sí | sí | sí | **Dentro del botón**, con `cargando = true` en `BotonPrincipal`, no con `Cargando` a pantalla completa |
| Error | sí | sí | sí | Ver 1.4: de campo o de pantalla según el tipo |
| Vacío | **no aplica** | **no aplica** | **no aplica** | No hay lista ni colección que pueda venir vacía |

**Por qué el cargando va dentro del botón.** Tapar el formulario con un
indicador a pantalla completa borra lo que el usuario escribió de su vista y,
si la llamada falla, lo obliga a reubicarse. HU-02 pide explícitamente que los
campos conserven lo escrito cuando falla por red. Mientras `cargando = true`:
el botón queda bloqueado, los campos quedan en `habilitado = false` y los
`BotonTexto` de navegación no responden.

### 1.4 Dónde se pinta cada error

Dos lugares, y la regla decide cuál:

- **Error de campo** — lo pinta el propio `CampoTexto` en su parámetro `error`.
  Es para lo que el usuario puede corregir en ese campo: formato de correo,
  contraseña corta, teléfono incompleto, campo vacío.
- **Error de pantalla** — un `EstadoError` **por encima del formulario**, sin
  reemplazarlo, con su botón de reintentar. Es para lo que no pertenece a
  ningún campo: sin red, servidor caído, credenciales rechazadas.

`EstadoError` aquí se usa **en línea**, no como pantalla completa: el
formulario sigue visible y editable debajo. Es la diferencia con P-05 y P-10,
donde `ContenedorEstado` sí sustituye el contenido, porque allá el error
significa que no hay nada que mostrar; aquí sí lo hay, es lo que el usuario
acaba de escribir.

### 1.5 Cuándo se valida

**Nunca mientras se escribe.** Marcar en rojo un correo a medio teclear es
hostil. La regla, para las tres pantallas:

1. **Al perder el foco** un campo que ya fue tocado: se valida ese campo solo.
2. **Al pulsar el botón principal**: se validan todos. Si alguno falla, no se
   llama al repositorio, se marcan todos los que fallaron y el foco salta al
   primero.
3. **Al volver a escribir** en un campo marcado: su error se limpia de
   inmediato, sin esperar a que pierda el foco.

El error de pantalla (1.4) se limpia al pulsar de nuevo el botón principal o
"Reintentar".

### 1.6 Normalización antes de enviar

El esquema tiene dos restricciones que la interfaz debe respetar **antes** de
llamar al repositorio, o el rechazo llega desde la base con un mensaje que no
es para el usuario:

| Restricción de `public.usuarios` | Qué hace la interfaz |
|---|---|
| `ck_usuario_correo_minusculas` (`correo = lower(correo)`) | El correo se envía con `trim()` y `lowercase()`. En pantalla se muestra tal cual lo escribió el usuario; la conversión ocurre en el ViewModel al enviar |
| `ck_usuario_correo_valido` (expresión regular) | La validación de la interfaz usa **esa misma** expresión. Ver 5.1 |

Nombre, apellidos y teléfono se envían con `trim()`. El teléfono se envía solo
con dígitos, sin espacios ni guiones.

### 1.7 Teclado y foco

| Campo | `KeyboardType` | `ImeAction` | Qué hace la acción |
|---|---|---|---|
| Correo | `Email` | `Next` | Pasa al siguiente campo |
| Contraseña (último campo de P-02) | `Password` | `Done` | Cierra el teclado y dispara el botón principal |
| Contraseña (en P-03, no es el último) | `Password` | `Next` | Pasa a teléfono |
| Nombre, apellidos | `Text` con `capitalization = Words` | `Next` | Pasa al siguiente campo |
| Teléfono | `Phone` | `Done` | Cierra el teclado y dispara el botón principal |

El orden de foco es el orden visual, de arriba hacia abajo. `Done` dispara el
botón principal **solo si no está en estado de carga**.

### 1.8 Accesibilidad

- Todo icono tocable lleva `contentDescription`. El ojo de `CampoContrasena`
  ya lo trae de `S1-T10` (`mostrar_contrasena` / `ocultar_contrasena`).
- Los chips de rol de P-03 se exponen como grupo de selección única
  (`Role.RadioButton` con `selectableGroup`), no como dos botones sueltos: un
  lector de pantalla debe anunciar "1 de 2 seleccionado".
- El mensaje de error de un campo se anuncia junto con el campo, mediante
  `semantics { error(...) }`, no como texto suelto debajo.
- Objetivo táctil mínimo de 48 × 48 dp en los `BotonTexto` de navegación:
  con `secundario` a 14 sp el texto mide menos, así que llevan relleno
  vertical hasta alcanzarlo.
- Contrastes: los de `DISENO.md`, sin excepción. Texto `Carbon` sobre
  `Mostaza` en el botón principal y en la barra superior.

---

## 2. P-02 · Iniciar sesión

**Ruta:** `Ruta.IniciarSesion` (ya definida en `Rutas.kt`) ·
**Historias:** HU-02, y es la salida de HU-01 y HU-04 ·
**La construye:** `S2-T03`

### 2.1 Anatomía, de arriba hacia abajo

| # | Elemento | Componente | Detalle |
|---|---|---|---|
| 1 | Barra superior | `BarraSuperior` | Título "Iniciar sesión". **Sin flecha de regreso**: es la raíz del subgrafo, no hay a dónde volver |
| 2 | Bloque de marca | — | Isotipo del casco a 72 dp y "Don Chambitas" en `titulo` `Carbon`, centrados. 32 dp de aire arriba y 24 dp abajo |
| 3 | Correo | `CampoTexto` | Etiqueta "Correo electrónico" |
| 4 | Contraseña | `CampoContrasena` | Etiqueta "Contraseña" |
| 5 | Olvidé mi contraseña | `BotonTexto` | "¿Olvidaste tu contraseña?", alineado a la derecha, 8 dp bajo el campo |
| 6 | Error de pantalla | `EstadoError` | Solo si lo hay. En línea, 16 dp arriba y abajo. Ver 1.4 |
| 7 | Acción principal | `BotonPrincipal` | "Iniciar sesión", ancho completo, 48 dp |
| 8 | Ir a registro | `BotonTexto` | "¿No tienes cuenta? Regístrate", centrado, 24 dp bajo el botón |

El bloque de marca reutiliza el isotipo de P-01 (`splash_logo_descripcion`),
a 72 dp en vez de los 120 dp del splash. No es un componente nuevo: es el
mismo vector.

### 2.2 Contrato de estado

Archivos, con la nomenclatura de `CONVENCIONES.md`:
`EstadoIniciarSesion.kt`, `IniciarSesionViewModel.kt`, `IniciarSesionPantalla.kt`.

```kotlin
data class EstadoIniciarSesion(
    val correo: String = "",
    val contrasena: String = "",
    val errorCorreo: Int? = null,        // @StringRes
    val errorContrasena: Int? = null,    // @StringRes
    val errorPantalla: TipoError? = null,
    val cargando: Boolean = false,
    val destino: Ruta? = null            // evento de navegacion de un solo uso
)
```

**Los errores viajan como `@StringRes Int?`, nunca como `String`.**
`CONVENCIONES.md` prohíbe cadenas de interfaz fuera de `strings.xml`, y un
ViewModel que arma texto en español ya es una cadena de interfaz fuera de su
lugar. La pantalla resuelve el recurso con `stringResource`.

`destino` es un evento de un solo uso: la pantalla navega y llama de inmediato
a `alConsumirDestino()`, que lo devuelve a `null`. Sin eso, una rotación
vuelve a navegar.

### 2.3 Eventos

| Evento | Qué hace |
|---|---|
| `alCambiarCorreo(valor)` | Actualiza `correo` y limpia `errorCorreo` |
| `alCambiarContrasena(valor)` | Actualiza `contrasena` y limpia `errorContrasena` |
| `alPerderFocoCorreo()` | Valida el correo si ya fue tocado (1.5) |
| `alIniciarSesion()` | Valida los dos campos. Si pasan, `cargando = true`, llama a `RepositorioAuth.iniciarSesion(correo.trim().lowercase(), contrasena)` y resuelve según 2.4 |
| `alReintentar()` | Limpia `errorPantalla` y repite `alIniciarSesion()` |
| `alConsumirDestino()` | Pone `destino` en `null` |

Navegar a P-03 y a P-04 **no pasa por el ViewModel**: son `BotonTexto` que
llaman directo al `NavController`, porque no hay estado que decidir.

### 2.4 Qué pasa al pulsar "Iniciar sesión"

| Resultado del repositorio | Qué ve el usuario |
|---|---|
| `Exito(Sesion)` con rol `CLIENTE` | Navega a `P-05` limpiando la pila hasta el subgrafo de autenticación, inclusive |
| `Exito(Sesion)` con rol `TRABAJADOR` | Navega a `P-10`, misma limpieza de pila |
| `Error(AUTENTICACION, _)` | `EstadoError` en línea con "Correo o contraseña incorrectos". **Nunca se dice cuál de los dos falló** (HU-02). Los campos conservan lo escrito; la contraseña **no** se borra |
| `Error(RED, _)` | `EstadoError` en línea con el mensaje de `RED` y botón "Reintentar". Los campos conservan lo escrito (HU-02) |
| `Error(SERVIDOR, _)` o `Error(DESCONOCIDO, _)` | `EstadoError` en línea con el mensaje del tipo y botón "Reintentar" |
| `Error(VALIDACION, _)` | No debería ocurrir: la interfaz ya validó. Si llega, se pinta como error de pantalla con el mensaje del tipo |

**La pila se limpia con `popUpTo(Subgrafo.Autenticacion.ruta) { inclusive = true }`.**
Después de entrar, el botón atrás no puede regresar al formulario de inicio de
sesión: cierra la aplicación o vuelve al inicio del rol, según dónde esté.

### 2.5 Qué NO tiene esta pantalla

- **No hay "recordarme".** La sesión la sostiene `supabase-kt` y la persiste
  `S2-T08`; una casilla que prometa otra cosa es alcance inventado.
- **No hay acceso con Google ni con teléfono.** `PRODUCTO.md` no los incluye.
- **No hay flecha de regreso.** P-01 sale de la pila (`S1-T15`), así que P-02
  es la raíz cuando no hay sesión.

---

## 3. P-03 · Registro con selección de rol

**Ruta:** `Ruta.Registro` · **Historia:** HU-01 · **La construye:** `S2-T02`

### 3.1 Anatomía, de arriba hacia abajo

| # | Elemento | Componente | Detalle |
|---|---|---|---|
| 1 | Barra superior | `BarraSuperior` | Título "Crear cuenta", **con flecha de regreso** a P-02 |
| 2 | Pregunta de rol | Texto `subtitulo` `Carbon` | "¿Qué vienes a hacer?" |
| 3 | Selector de rol | Dos `ChipCategoria` en fila | "Quiero contratar" y "Ofrezco mi trabajo" |
| 4 | Aviso del rol | Texto `secundario` `Cafe` | "El rol no se puede cambiar después" (`DEC-22`) |
| 5 | Nombre(s) | `CampoTexto` | Máximo 80 caracteres |
| 6 | Apellidos | `CampoTexto` | Máximo 120 caracteres |
| 7 | Correo electrónico | `CampoTexto` | Máximo 160 caracteres |
| 8 | Contraseña | `CampoContrasena` | Mínimo 8 caracteres |
| 9 | Ayuda de contraseña | Texto `pie` `Cafe` | "Mínimo 8 caracteres". Visible siempre, no solo al fallar |
| 10 | Teléfono celular | `CampoTexto` | Exactamente 10 dígitos |
| 11 | Error de pantalla | `EstadoError` | Solo si lo hay. En línea (1.4) |
| 12 | Acción principal | `BotonPrincipal` | "Crear cuenta", ancho completo, 48 dp |
| 13 | Ir a inicio de sesión | `BotonTexto` | "¿Ya tienes cuenta? Inicia sesión", centrado |

Los topes de longitud **no son decorativos**: son los de
`public.usuarios` (`varchar(80)`, `varchar(120)`, `varchar(160)`). El campo
impide escribir de más en lugar de dejar que la base rechace el registro.

### 3.2 El selector de rol

Dos `ChipCategoria` de ancho igual, separados 12 dp. El activo va `Mostaza`
con texto `Carbon`; el inactivo, `Arena` con borde `Borde` y texto `Cafe`.

**Ninguno viene preseleccionado.** Es deliberado: por `DEC-22` el rol es
permanente y no se cambia en el MVP, así que la aplicación no lo elige por el
usuario. Un valor por omisión convierte un descuido en una cuenta con el rol
equivocado que nadie puede arreglar desde la aplicación.

La consecuencia es que "no elegiste rol" es un error de validación como
cualquier otro, y se pinta como texto `pie` en color `Error` bajo la fila de
chips, a 4 dp.

### 3.3 Contrato de estado

Archivos: `EstadoRegistro.kt`, `RegistroViewModel.kt`, `RegistroPantalla.kt`.

```kotlin
data class EstadoRegistro(
    val rol: RolUsuario? = null,
    val nombre: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val telefono: String = "",
    val errorRol: Int? = null,           // @StringRes
    val errorNombre: Int? = null,
    val errorApellidos: Int? = null,
    val errorCorreo: Int? = null,
    val errorContrasena: Int? = null,
    val errorTelefono: Int? = null,
    val errorPantalla: TipoError? = null,
    val cargando: Boolean = false,
    val destino: Ruta? = null
)
```

**Sin excepciones a la regla de 2.2.** Hasta `S2-T07` había un
`mensajePantalla` para pintar tal cual el mensaje de un error de `VALIDACION`,
porque se suponía que venía en español desde un trigger. En el registro no es
así: el correo duplicado lo rechaza Supabase Auth, y en inglés (`H-11` en
`CONTRATOS-API.md`). Se retiró, y el correo duplicado tiene su propio
`TipoError.CORREO_DUPLICADO`.

### 3.4 Eventos

| Evento | Qué hace |
|---|---|
| `alElegirRol(rol)` | Fija `rol` y limpia `errorRol` |
| `alCambiar<Campo>(valor)` | Actualiza el campo y limpia su error |
| `alPerderFoco<Campo>()` | Valida ese campo si ya fue tocado |
| `alRegistrar()` | Valida los seis. Si pasan, `cargando = true` y llama a `RepositorioAuth.registrar(...)` con los valores ya normalizados (1.6) |
| `alReintentar()` | Limpia el error de pantalla y repite `alRegistrar()` |
| `alConsumirDestino()` | Pone `destino` en `null` |

El teléfono se filtra al escribir: `alCambiarTelefono` descarta todo lo que no
sea dígito y corta en 10. No es validación, es no dejar teclear basura.

### 3.5 Qué pasa al pulsar "Crear cuenta"

| Resultado del repositorio | Qué ve el usuario |
|---|---|
| `Exito(Sesion)` | Navega a `P-05` o `P-10` según el rol de `sesion.usuario`, limpiando la pila del subgrafo de autenticación |
| ~~`Exito(Usuario)` sin sesión~~ | **Retirada por `DEC-25`**: el registro siempre deja sesión abierta, y desde `S2-T06` `registrar` devuelve `Sesion`. Era la rama de H-10 |
| `Error(CORREO_DUPLICADO, _)` | `EstadoError` en línea con `error_correo_duplicado`: "El correo ya está registrado, inicia sesión", sin "Reintentar". El `BotonTexto` de abajo es el acceso directo a P-02 que pide HU-01 (`H-11`) |
| `Error(VALIDACION, _)` | `EstadoError` en línea con el mensaje de `S1-T11` para ese tipo. No debería llegar: la interfaz ya validó |
| `Error(RED, _)` | `EstadoError` en línea, botón "Reintentar", **todo lo capturado se conserva**, incluida la contraseña y el rol elegido |
| `Error(SERVIDOR, _)` o `Error(DESCONOCIDO, _)` | `EstadoError` en línea con el mensaje del tipo y "Reintentar" |
| `Error(AUTENTICACION, _)` | No aplica al registro. Si llega, se pinta con el mensaje de su tipo |

**La fila de `public.usuarios` no se inserta desde la aplicación.** La crea el
trigger `tg_auth_usuario_creado` con el metadata del registro
(`CONTRATOS-API.md`). La pantalla solo manda los seis valores y el rol; si el
rol no viaja, la cuenta queda como `cliente`, y por eso 3.2 lo vuelve
obligatorio antes de enviar.

### 3.6 La flecha de regreso descarta

Volver a P-02 con la flecha o con el botón del sistema **descarta el
formulario sin preguntar**. No hay diálogo de confirmación: el formulario se
llena en menos de un minuto y un diálogo de "¿seguro que quieres salir?" en
una pantalla de alta es fricción sin beneficio. Es una decisión, no un olvido.

---

## 4. P-04 · Recuperar contraseña

**Ruta:** `Ruta.RecuperarContrasena` · **Historia:** HU-04 ·
**La construye:** `S2-T10`

Es la única de las tres que tiene **dos vistas**: el formulario y la
confirmación. No son dos pantallas: es la misma, con `enviado` en `true`.

### 4.1 Vista de formulario

| # | Elemento | Componente | Detalle |
|---|---|---|---|
| 1 | Barra superior | `BarraSuperior` | Título "Recuperar contraseña", con flecha de regreso a P-02 |
| 2 | Explicación | Texto `cuerpo` `Cafe` | "Escribe tu correo y te enviamos un enlace para crear una contraseña nueva" |
| 3 | Correo | `CampoTexto` | Etiqueta "Correo electrónico" |
| 4 | Error de pantalla | `EstadoError` | Solo si lo hay (1.4) |
| 5 | Acción principal | `BotonPrincipal` | "Enviar enlace", ancho completo, 48 dp |
| 6 | Volver | `BotonTexto` | "Volver a iniciar sesión", centrado |

### 4.2 Vista de confirmación

Sustituye los elementos 2 a 5. La barra superior y el `BotonTexto` de volver
se quedan donde están.

| # | Elemento | Componente | Detalle |
|---|---|---|---|
| 1 | Icono | `Icons.Outlined.MarkEmailRead` a 56 dp | Color `Exito` |
| 2 | Título | Texto `subtitulo` `Carbon` | "Revisa tu correo" |
| 3 | Mensaje | Texto `cuerpo` `Cafe` | "Si **ese correo** está registrado, te enviamos un enlace para crear una contraseña nueva" |
| 4 | Aviso de vigencia | Superficie `Arena`, borde `Borde`, radio 16 dp, relleno 16 dp | "El enlace vence en 1 hora" |

**El mensaje dice "si ese correo está registrado", y eso no es una cortesía.**
`recuperarContrasena` siempre devuelve `Exito`, exista o no la cuenta
(`CONTRATOS-API.md`, y HU-04 lo pide con todas sus letras). Un mensaje que
diga "te enviamos el enlace" en indicativo convierte la pantalla en un
comprobador de qué correos están dados de alta. El texto **no se suaviza** en
revisión de pull request.

Por la misma razón, la pantalla **no repite el correo escrito** en la
confirmación: leer "te enviamos un enlace a juan@ejemplo.mx" en una pantalla
que no verificó nada refuerza justo la lectura equivocada.

> **Confirmado por `S2-T10` el 2026-10-08: una hora.** El "24 horas" original
> venía de `WIREFRAMES.md`, no de la configuración. El líder leyó *Email OTP
> Expiration* en la consola, 3600 segundos, y decidió dejarlo así. Si algún día
> cambia ese ajuste, cambia `recuperar_vigencia` el mismo día: es un solo
> recurso de `strings.xml`.

### 4.3 Contrato de estado

Archivos: `EstadoRecuperarContrasena.kt`, `RecuperarContrasenaViewModel.kt`,
`RecuperarContrasenaPantalla.kt`.

```kotlin
data class EstadoRecuperarContrasena(
    val correo: String = "",
    val errorCorreo: Int? = null,        // @StringRes
    val errorPantalla: TipoError? = null,
    val cargando: Boolean = false,
    val enviado: Boolean = false
)
```

No hay `destino`: esta pantalla no navega sola. Se sale por la flecha de
regreso o por el `BotonTexto`, y las dos llevan a P-02.

### 4.4 Eventos y resultados

| Evento | Qué hace |
|---|---|
| `alCambiarCorreo(valor)` | Actualiza `correo` y limpia `errorCorreo` |
| `alPerderFocoCorreo()` | Valida el correo si ya fue tocado |
| `alEnviar()` | Valida el correo. Si pasa, `cargando = true` y llama a `RepositorioAuth.recuperarContrasena(correo.trim().lowercase())` |
| `alReintentar()` | Limpia `errorPantalla` y repite `alEnviar()` |

| Resultado del repositorio | Qué ve el usuario |
|---|---|
| `Exito(Unit)` | `enviado = true`. Se pinta la vista de confirmación |
| `Error(RED, _)` | `EstadoError` en línea con "Reintentar". Sigue en el formulario, con el correo escrito |
| `Error(SERVIDOR, _)` o `Error(DESCONOCIDO, _)` | Igual, con el mensaje de su tipo |

Un fallo de red **no** pinta la confirmación. La protección contra enumeración
de usuarios es sobre si la cuenta existe, no sobre si la petición salió: si no
salió, el usuario tiene que saberlo, o se queda esperando un correo que nunca
se pidió.

### 4.5 Volver a enviar

Desde la confirmación no hay botón de "enviar de nuevo". Para reintentar se
vuelve a P-02 y se entra otra vez a P-04. Es alcance que ningún criterio de
HU-04 pide, y agregarlo aquí sería inventarlo.

---

## 5. Validaciones

Las implementa `S2-T04` como funciones puras de dominio, sin Android, para que
`S2-T16` las pueda probar sin emulador. Cada una devuelve `Int?`: el recurso
del mensaje, o `null` si el campo está bien.

### 5.1 El correo

La expresión regular es **la misma del esquema**, para que la interfaz y la
base nunca discrepen:

```kotlin
// Espejo de ck_usuario_correo_valido en basedatos/01_esquema.sql.
// Si aquella cambia, esta cambia el mismo dia, o la base rechaza
// registros que la aplicacion dio por buenos.
private val CORREO_VALIDO = Regex("^[^@\\s]+@[^@\\s]+\\.[a-zA-Z]{2,}$")
```

No se usa `android.util.Patterns.EMAIL_ADDRESS`: acepta cosas que la base
rechaza, y el rechazo llegaría hasta el final del registro.

### 5.2 Tabla de reglas

| Campo | Regla | Mensaje | Clave |
|---|---|---|---|
| Correo | No vacío | Escribe tu correo electrónico | `validacion_correo_vacio` |
| Correo | Cumple `CORREO_VALIDO` | Ese correo no se ve bien, revísalo | `validacion_correo_formato` |
| Correo | Máximo 160 caracteres | El correo es demasiado largo | `validacion_correo_largo` |
| Contraseña (P-02) | No vacía | Escribe tu contraseña | `validacion_contrasena_vacia` |
| Contraseña (P-03) | Mínimo 8 caracteres | Usa al menos 8 caracteres | `validacion_contrasena_corta` |
| Rol (P-03) | Elegido | Elige si vienes a contratar o a trabajar | `validacion_rol_sin_elegir` |
| Nombre | No vacío tras `trim()` | Escribe tu nombre | `validacion_nombre_vacio` |
| Nombre | Máximo 80 caracteres | El nombre es demasiado largo | `validacion_nombre_largo` |
| Apellidos | No vacío tras `trim()` | Escribe tus apellidos | `validacion_apellidos_vacio` |
| Apellidos | Máximo 120 caracteres | Los apellidos son demasiado largos | `validacion_apellidos_largo` |
| Teléfono | No vacío | Escribe tu teléfono | `validacion_telefono_vacio` |
| Teléfono | Exactamente 10 dígitos | El teléfono lleva 10 dígitos | `validacion_telefono_digitos` |

**La contraseña no valida nada más.** Ni mayúscula obligatoria, ni número, ni
símbolo. `PRODUCTO.md` no lo pide, y una regla de complejidad inventada aquí
manda a un plomero en obra a pelear con el teclado del teléfono.

**En P-02 la contraseña solo se valida "no vacía".** Exigir 8 caracteres al
entrar delataría la longitud mínima a quien está probando contraseñas, y
sobre todo dejaría fuera a quien se registró antes de que la regla existiera.
Quien se equivoca, recibe "Correo o contraseña incorrectos" y nada más.

### 5.3 El teléfono es obligatorio en la interfaz, opcional en la base

`public.usuarios.telefono` es `varchar(20)` **nulable** y
`RepositorioAuth.registrar` lo recibe como `String?`. Aun así, P-03 lo exige.

No es una contradicción: HU-01 lo lista entre los campos que van completos al
confirmar el registro, y la columna es nulable porque otros caminos —el
trigger, una carga administrativa— pueden no tenerlo. La regla de la interfaz
es más estricta que la de la base, que es la dirección correcta.

### 5.4 Qué pasa si fallan varios campos a la vez

Se marcan **todos** los que fallaron, no el primero. El foco salta al primero
en orden visual y la pantalla hace scroll hasta él si quedó fuera de vista.
No se llama al repositorio.

---

## 6. De `TipoError` a lo que ve el usuario

`S1-T11` ya dejó los mensajes por tipo en `strings.xml` y el mapeo en
`Estados.kt` (`obtenerMensajeErrorRes`, `obtenerTituloErrorRes`). Las tres
pantallas **reutilizan ese mapeo**; no se escribe uno nuevo.

La única sustitución es en P-02: el mensaje genérico de `AUTENTICACION` es
"Inicia sesión de nuevo para continuar", que no dice nada útil en la pantalla
de inicio de sesión. Ahí se pinta `error_credenciales_invalidas`.

| Pantalla | `TipoError` | Qué se pinta |
|---|---|---|
| P-02 | `AUTENTICACION` | `error_credenciales_invalidas` — "Correo o contraseña incorrectos" |
| P-02, P-03, P-04 | `RED`, `SERVIDOR`, `DESCONOCIDO` | El mensaje de `S1-T11` para ese tipo |
| P-03 | `CORREO_DUPLICADO` | `error_correo_duplicado` — "El correo ya está registrado, inicia sesión" |
| P-02, P-03, P-04 | `VALIDACION` | El mensaje de `S1-T11`. No debería llegar |
| Todas | `LIMITE_IA` | No aplica. Si llega, `DESCONOCIDO` |

El botón "Reintentar" aparece en `RED`, `SERVIDOR` y `DESCONOCIDO`, que es
donde volver a intentar puede servir de algo. En `AUTENTICACION`,
`VALIDACION` y `CORREO_DUPLICADO` no aparece: lo que hay que cambiar es lo que
está escrito.

---

## 7. Cadenas nuevas de `strings.xml`

Las agrega la tarea que construye cada pantalla, no esta. Se listan con su
clave definitiva para que las tres tareas no inventen tres nombres distintos
para lo mismo.

```xml
<!-- Autenticación · común (S2-T01) -->
<string name="auth_correo">Correo electrónico</string>
<string name="auth_contrasena">Contraseña</string>

<!-- P-02 Iniciar sesión -->
<string name="iniciar_sesion_titulo">Iniciar sesión</string>
<string name="iniciar_sesion_accion">Iniciar sesión</string>
<string name="iniciar_sesion_olvide">¿Olvidaste tu contraseña?</string>
<string name="iniciar_sesion_ir_registro">¿No tienes cuenta? Regístrate</string>
<string name="error_credenciales_invalidas">Correo o contraseña incorrectos</string>

<!-- P-03 Registro -->
<string name="registro_titulo">Crear cuenta</string>
<string name="registro_pregunta_rol">¿Qué vienes a hacer?</string>
<string name="registro_rol_cliente">Quiero contratar</string>
<string name="registro_rol_trabajador">Ofrezco mi trabajo</string>
<string name="registro_rol_definitivo">El rol no se puede cambiar después</string>
<string name="registro_nombre">Nombre(s)</string>
<string name="registro_apellidos">Apellidos</string>
<string name="registro_telefono">Teléfono celular</string>
<string name="registro_ayuda_contrasena">Mínimo 8 caracteres</string>
<string name="registro_accion">Crear cuenta</string>
<string name="registro_ir_iniciar_sesion">¿Ya tienes cuenta? Inicia sesión</string>
<string name="registro_cuenta_creada">Tu cuenta quedó creada, inicia sesión</string>

<!-- P-04 Recuperar contraseña -->
<string name="recuperar_titulo">Recuperar contraseña</string>
<string name="recuperar_explicacion">Escribe tu correo y te enviamos un enlace para crear una contraseña nueva</string>
<string name="recuperar_accion">Enviar enlace</string>
<string name="recuperar_volver">Volver a iniciar sesión</string>
<string name="recuperar_enviado_titulo">Revisa tu correo</string>
<string name="recuperar_enviado_mensaje">Si ese correo está registrado, te enviamos un enlace para crear una contraseña nueva</string>
<string name="recuperar_vigencia">El enlace vence en 1 hora</string>
<string name="recuperar_enviado_descripcion">Correo enviado</string>

<!-- Validaciones (S2-T04) -->
<string name="validacion_correo_vacio">Escribe tu correo electrónico</string>
<string name="validacion_correo_formato">Ese correo no se ve bien, revísalo</string>
<string name="validacion_correo_largo">El correo es demasiado largo</string>
<string name="validacion_contrasena_vacia">Escribe tu contraseña</string>
<string name="validacion_contrasena_corta">Usa al menos 8 caracteres</string>
<string name="validacion_rol_sin_elegir">Elige si vienes a contratar o a trabajar</string>
<string name="validacion_nombre_vacio">Escribe tu nombre</string>
<string name="validacion_nombre_largo">El nombre es demasiado largo</string>
<string name="validacion_apellidos_vacio">Escribe tus apellidos</string>
<string name="validacion_apellidos_largo">Los apellidos son demasiado largos</string>
<string name="validacion_telefono_vacio">Escribe tu teléfono</string>
<string name="validacion_telefono_digitos">El teléfono lleva 10 dígitos</string>
```

Todos tutean y dicen qué hacer, que es la regla de `DISENO.md`.

---

## 8. Navegación entre las tres

El mapa de `PANTALLAS.md` es `P-01 → P-02 ↔ P-03 ↔ P-04`. En términos de la
pila:

| Desde | Acción | A dónde | Cómo |
|---|---|---|---|
| P-01 | Sin sesión | P-02 | `popUpTo(Ruta.Splash) { inclusive = true }` — ya hecho en `S1-T15` |
| P-02 | "Regístrate" | P-03 | `navigate`, se apila |
| P-02 | "¿Olvidaste tu contraseña?" | P-04 | `navigate`, se apila |
| P-03 | Flecha, atrás o "Inicia sesión" | P-02 | `popBackStack()`, no `navigate` |
| P-04 | Flecha, atrás o "Volver" | P-02 | `popBackStack()`, no `navigate` |
| P-02 | Sesión iniciada | P-05 o P-10 | `popUpTo(Subgrafo.Autenticacion) { inclusive = true }` |
| P-03 | Cuenta creada con sesión | P-05 o P-10 | Igual |

**Los regresos usan `popBackStack`, no `navigate`.** Si P-03 navegara a P-02,
la pila crecería P-02 → P-03 → P-02 y el botón atrás recorrería el formulario
de registro hacia atrás. Es el error clásico de este trío de pantallas.

Las guardas de `S1-T12` ya mandan cualquier ruta privada a P-02 sin sesión, y
`RUTAS_AUTENTICACION` ya contiene las tres. **Esta tarea no toca `Rutas.kt`**.
Desde `S2-T15` las guardas leen la sesión real en vez del marcador temporal,
y con sesión abierta P-02, P-03 y P-04 llevan al inicio del rol.

---

## 9. Qué consume cada tarea del Sprint 2

| Tarea | Qué toma de aquí |
|---|---|
| `S2-T02` Registro | Secciones 1, 3 y 7 |
| `S2-T03` Inicio de sesión | Secciones 1, 2 y 7 |
| `S2-T04` Validaciones | Secciones 1.5, 5 y 7 |
| `S2-T05` ViewModels | Contratos de estado de 2.2, 3.3 y 4.3, y los eventos |
| `S2-T10` Recuperar contraseña | Secciones 1, 4 y 7 |
| `S2-T16` Pruebas | 5.2 como tabla de casos, y 2.4, 3.5 y 4.4 como matriz de resultados |

### Criterios de aceptación de `S2-T01`

- [x] Las tres pantallas tienen anatomía con medidas, en orden visual.
- [x] Las tres tienen contrato de estado y de eventos, con nombres que siguen
      `CONVENCIONES.md`.
- [x] Está dicho qué estado de pantalla aplica a cada una y cuál no, con el
      porqué.
- [x] Cada resultado posible de `RepositorioAuth` tiene una fila que dice qué
      ve el usuario.
- [x] Las reglas de validación están campo por campo, con mensaje y clave.
- [x] No se usa ni un componente fuera de `DISENO.md`.
- [x] No se agregó ninguna pantalla: siguen siendo 19.
- [x] Lo que no cuadró se reporta al líder en vez de resolverse (sección 10).

---

## 10. Hallazgos para el líder

Los dos salen de cruzar HU-01 y HU-04 contra `CONTRATOS-API.md` y
`RepositorioAuth`. **Ninguno se resolvió aquí**, conforme a AGENTS.md §9.

> **Al 2026-09-22 los dos están cerrados.** El líder resolvió `H-10` con
> `DEC-25` —el registro deja sesión abierta— y `H-09` con `DEC-27` —el enlace
> de recuperación entra por *deep link* y aterriza en P-18—. Lo que sigue abajo
> es cada hallazgo como se reportó; las resoluciones y sus consecuencias están
> en `docs/control/DECISIONES.md`.

**`H-09` · CERRADO el 2026-09-22 por `DEC-27`: el enlace abre la aplicación
por *deep link* y aterriza en P-18, sin pantalla nueva.** De las dos salidas
que se plantean abajo **la primera resultó no existir**: Supabase no hospeda
ningún formulario de contraseña nueva, así que la pantalla la pone uno siempre;
y una página web propia la descarta `DEC-02`. El *deep link* no era una opción
entre dos, era la única. Lo que se decidió fue el aterrizaje: P-18, que ya
cambia la contraseña por HU-05, con lo que `DEC-10` se conserva en 19
pantallas. El hallazgo, como se reportó:

HU-04 pide que al abrir el enlace del correo se pueda definir una
contraseña nueva. `PANTALLAS.md` tiene 19 pantallas y ninguna es esa: P-04
solo pide el enlace. Hay dos salidas y las dos son del líder: que el enlace
abra la página alojada de Supabase Auth, con lo cual no hace falta pantalla y
conviene anotarlo en HU-04 para que nadie la busque; o que el enlace abra la
aplicación por *deep link*, con lo cual hace falta una pantalla nueva, una
tarea nueva en el Sprint 2 y tocar `PANTALLAS.md`. **Conviene cerrarlo antes
de `S2-T07`**, que es quien implementa `recuperarContrasena` de verdad.

**`H-10` · CERRADO el 2026-09-22 por `DEC-25`: el registro deja sesión
abierta.** Con eso, de las dos ramas que la sección 3.5 contempla gana la de
sesión inmediata, y la confirmación por correo de Supabase Auth queda
desactivada. `S2-T06` cambia `registrar` para que devuelva `Sesion` en vez de
`Usuario`. El hallazgo, como se reportó:

HU-01 dice
que al confirmar el registro se entra directo a la pantalla del rol, lo que
supone sesión inmediata. Pero `registrar` devuelve `Resultado<Usuario>`, no
`Sesion`, y si Supabase Auth tiene activada la confirmación por correo, el
`signUp` **no** abre sesión y HU-01 no se puede cumplir tal como está escrita.
La sección 3.5 contempla las dos ramas para no frenar a `S2-T02`, pero la
decisión —confirmación por correo sí o no— es de producto y de seguridad, no
de la pantalla. **Le toca a `S2-T06` recogerla y a `S2-T07` implementarla.**
Ojo, además, con lo que ya dejó anotado `S1-T03`: el dominio de prueba
`@prueba.donchambitas.mx` no pasa la validación de dominio de Supabase, así
que si la confirmación se deja activa, las cuentas de prueba necesitan correos
de un dominio que exista.
