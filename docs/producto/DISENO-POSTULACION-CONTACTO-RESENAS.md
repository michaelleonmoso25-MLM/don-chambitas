# Diseño de las pantallas de postulación, contacto y reseñas · P-14, P-15, P-16, P-17 y P-19

> Entregable de `S5-T01`. Dice cómo se ven y se comportan las pantallas con las
> que un trabajador **se postula** (P-19, P-14), un cliente **elige** a quién le
> da el trabajo (P-19), los dos **conversan** (P-15, P-16) y el cliente
> **califica** (P-17). **Solo diseño: ni Kotlin ni `strings.xml`.** Las construyen
> `S5-T02` a `S5-T05` y `S5-T08`.
>
> Sigue la estructura de `DISENO-PERFIL-SERVICIOS.md` y de
> `DISENO-SOLICITUDES-BUSQUEDA.md` y **hereda sus reglas comunes** (estructura,
> cuatro estados, dónde se pinta cada error, cuándo se valida) salvo donde este
> documento diga otra cosa. Lo que el diseño no puede resolver sin decidir algo
> que le toca al líder queda en la sección 9 como hallazgo (`H-49` a `H-55`), no
> resuelto aquí.

## 0. Alcance de esta tarea

### Qué entra

- **P-19 Detalle de solicitud**, con sus dos vistas: la del trabajador (se
  postula) y la del cliente (compara y elige).
- **P-14 Mis postulaciones** (`DEC-41`).
- **P-15 Conversaciones**, **P-16 Chat** y **P-17 Dejar reseña**.
- El botón de contactar y de calificar en las pantallas que ya existen (P-07,
  P-09), como puntos de entrada.
- Las cadenas nuevas, con su clave, y la navegación.

### Qué NO entra

- **Código.** Las claves se listan aquí; las agrega la tarea que construye cada
  pantalla.
- **Los contratos y las operaciones.** `RepositorioPostulaciones`,
  `RepositorioChat` y `RepositorioResenas` aparecen en `CONTRATOS-API.md` solo
  como resumen; su detalle es de `S5-T06`. Aquí solo se dice qué datos necesita
  cada pantalla (hallazgos `H-50`, `H-51` y `H-54`).
- **La mensajería en tiempo real** (suscripción, reconexión): `S5-T07`.
- **Las notificaciones locales** de postulaciones y mensajes: `S5-T10`.
- **Adjuntos, audios, llamadas** (`DEC-04`): el chat es de texto plano.
- **Editar o borrar una reseña**: no existe.
- **Un listado de reseñas**: se leen dentro de P-07 y solo se escriben en P-17
  (`PANTALLAS.md`).
- **Wireframes en PNG.** El documento lleva bocetos en texto.

### De dónde sale cada cosa

| Cosa | Fuente |
|---|---|
| Qué hace cada pantalla | `HISTORIAS.md` HU-20 a HU-28 |
| Quién abre el hilo | `DEC-20` y `DEC-24` |
| Qué datos y reglas hay | `basedatos/01_esquema.sql` (`postulaciones`, `conversaciones`, `mensajes`, `resenas`) y `02_politicas_rls.sql` |
| Qué operaciones existen | `CONTRATOS-API.md`: Postulaciones, Chat y Reseñas |
| Estructura, estados y accesibilidad | `DISENO-PERFIL-SERVICIOS.md` 1.1 a 1.8 |
| Componentes | `DISENO.md` y `ui/componentes` |

---

## 1. Reglas comunes

### 1.1 Estructura

Las de `DISENO-PERFIL-SERVICIOS.md` 1.1: márgenes de 16 dp, bloques a 24 dp,
`Crema`, `BarraSuperior` en `Mostaza`.

| Pantalla | Barra inferior | Pestaña marcada | Flecha de regreso | Por qué |
|---|---|---|---|---|
| P-14 | **no** | — | sí | Se abre desde P-18 y P-12 (`PANTALLAS.md`) |
| P-15 | sí (la del rol) | Chats | no | Es destino de la barra de los dos roles |
| P-16 | **no** | — | sí | `PANTALLAS.md` la lista entre las que se abren encima |
| P-17 | **no** | — | sí | Ídem |
| P-19 | **no** | — | sí | Ídem |

### 1.2 Componentes

Los de `DISENO.md` y los que ya existen en `ui/componentes`. **Faltan cinco**
(`H-49`): no se inventan aquí. Mientras el líder los registra en `DISENO.md`, el
diseño dice qué debe hacer cada uno.

| Componente | Dónde | Estado |
|---|---|---|
| `BarraSuperior`, `BarraInferiorCliente`, `BarraInferiorTrabajador` | Estructura | Existe |
| `TarjetaSolicitud` + `EtiquetaEstado` | Cabecera de P-19 y lista de P-09 | Existe |
| `Estrellas` | Calificación del trabajador en las postulaciones | Existe |
| `CampoTexto` | Mensaje y precio de la postulación, comentario de la reseña | Existe |
| `BotonDestacado`, `BotonPrincipal`, `BotonSecundario`, `BotonTexto` | Acciones | Existe |
| `ContenedorEstado`, `Cargando`, `EstadoVacio`, `EstadoError` | Los cuatro estados | Existe |
| `TarjetaPostulacion` | P-19 (cliente) y P-14 | **Falta** (`H-49`) |
| `TarjetaConversacion` | P-15 | **Falta** (`H-49`) |
| `BurbujaMensaje` | P-16 | **Falta** (`H-49`) |
| `CampoMensaje` (campo de varias líneas con botón de enviar) | P-16 | **Falta** (`H-49`) |
| `SelectorEstrellas` (1 a 5, tocable) | P-17 | **Falta** (`H-49`) |

`EtiquetaEstado` hoy solo pinta los cuatro estados de la **solicitud**. Las
postulaciones tienen otros cuatro; se proponen sus colores en `H-49`.

### 1.3 Los cuatro estados

Cargando, vacío, error y contenido, con `ContenedorEstado`. Cuál aplica a cada
pantalla:

| Pantalla | Cargando | Vacío | Error | Contenido |
|---|---|---|---|---|
| P-14 | sí | sí, con salida a P-10 | sí | sí |
| P-15 | sí | sí, explica cómo empieza un contacto (HU-25) | sí | sí |
| P-16 | sí (el historial) | sí: "Escribe el primer mensaje" | sí, de carga y de enviar | sí |
| P-17 | no | no | sí, de publicar | sí |
| P-19 | sí | no | sí | sí |

### 1.4 Dónde se pinta cada error

Los de campo, debajo del campo. Los de pantalla (`TipoError`), en `EstadoError`
con "Reintentar" si fue la carga, y en una línea sobre el botón si fue una
acción (postular, aceptar, enviar, calificar). Un fallo al **enviar un mensaje**
no borra el texto escrito: el mensaje queda en el campo y la línea de error
dice "No se envió. Toca para reintentar". `LIMITE_IA` no aplica: ninguna de
estas pantallas llama a la IA.

### 1.5 Cuándo se valida

Misma regla que `DISENO-PERFIL-SERVICIOS.md` 1.5: al perder el foco un campo ya
tocado, y todos al enviar, con el foco en el primero que falle.

### 1.6 Normalización antes de enviar

`trim()` en mensaje y comentario, con los saltos de línea de en medio
conservados (es texto de conversación, no de formulario). Un mensaje en blanco
**no se envía**: el botón está deshabilitado.

---

## 2. P-19 · Detalle de solicitud

Una sola ruta con dos vistas, según quién la abre (`PANTALLAS.md`). La de cada
rol solo muestra lo que a ese rol le corresponde.

### 2.1 Cabecera común

De arriba hacia abajo: `BarraSuperior` "Detalle de solicitud" con flecha;
título; `EtiquetaEstado` de la solicitud y fecha; categoría; ubicación (municipio
y estado); presupuesto, **solo si lo hay**; la descripción completa. Nada de
foto ni datos de contacto del cliente: el contacto es por el chat (HU-24).

### 2.2 Vista del trabajador (HU-21, HU-22)

Debajo de la cabecera, **un solo bloque que depende de la situación**:

| Situación | Qué se ve |
|---|---|
| La solicitud está `abierta` y no me he postulado | Formulario: `CampoTexto` "Tu mensaje" (varias líneas, obligatorio), `CampoTexto` "Tu precio" (con "$", obligatorio), `BotonDestacado` "Enviar postulación" |
| Ya me postulé | **En lugar del formulario**, "Tu postulación": mi mensaje, mi precio y la etiqueta de su estado. Con estado `enviada`, un `BotonTexto` "Retirar postulación" |
| La solicitud ya no está `abierta` y no me postulé | Un aviso: "Esta solicitud ya no recibe postulaciones". Sin formulario |
| Mi postulación está `aceptada` | La etiqueta y la línea "El cliente te eligió. Te escribirá por Chats" (`H-55`) |

**Lo que el trabajador no ve:** las postulaciones de otros ni cuántos compiten
(HU-22, lo impide RLS). No hay botón de contactar al cliente (`DEC-20`).

Reglas del formulario: mensaje de 1 a 500 caracteres y precio de 0 a 99,999.99
(`H-52`). Se valida al perder el foco y al enviar (1.5). Si la base rechaza
(solicitud ya no abierta, mi propia solicitud), el mensaje de la base se muestra
sobre el botón.

### 2.3 Vista del cliente (HU-23, HU-24)

Cuando la solicitud es **mía**. Debajo de la cabecera:

- **Si está `abierta`:** la sección "Postulaciones" con un `TarjetaPostulacion`
  por cada una: foto y nombre del trabajador, `Estrellas` con su promedio, su
  mensaje, su precio y la fecha. Cada tarjeta tiene "Ver perfil" (abre P-07),
  "Contactar" (abre P-16 con esta solicitud, `DEC-24`), "Rechazar" y "Aceptar".
  Las `rechazada` y `retirada` se muestran atenuadas y sin acciones.
- **Si está `asignada`:** una sola tarjeta con el trabajador elegido, "Ver
  perfil" y "Contactar". Las demás postulaciones no se listan.
- **Si está `cerrada`:** igual que `asignada`, más el bloque de reseña (2.5).
- **Si está `cancelada`:** solo la cabecera y el aviso "Cancelaste esta
  solicitud".

**Confirmaciones.** Aceptar y rechazar pasan por un diálogo, como retirar:

| Acción | Diálogo | Efecto |
|---|---|---|
| Aceptar | "¿Aceptar a {nombre}? Las demás postulaciones se rechazarán y la solicitud quedará asignada" | `fn_aceptar_postulacion`: todo junto o nada (HU-23) |
| Rechazar | "¿Rechazar la postulación de {nombre}?" | Esa queda `rechazada`; la solicitud sigue `abierta` |
| Retirar (trabajador) | "¿Retirar tu postulación?" | Pasa a `retirada` |

Las acciones de la **solicitud** misma (cerrar, cancelar) las definen las
historias HU-14 y HU-15 y las construye `S5-T04`; aquí solo se dice dónde van:
al pie de la vista del cliente, `BotonSecundario` "Cerrar solicitud" cuando está
`asignada` y `BotonTexto` "Cancelar solicitud" cuando está `abierta`. Cancelar
una `asignada` sigue sin definirse (`H-47`).

### 2.4 Contrato de estado

```text
EstadoDetalleSolicitud(
    cargando: Boolean,
    errorCarga: TipoError?,
    solicitud: Solicitud?,
    esMia: Boolean,                    // decide la vista
    // trabajador
    miPostulacion: Postulacion?,
    mensaje: String, errorMensaje: String?,
    precio: String,  errorPrecio: String?,
    enviando: Boolean,
    // cliente
    postulaciones: List<PostulacionConTrabajador>,   // ver H-54
    confirmando: AccionPendiente?,     // aceptar | rechazar | retirar
    resena: Resena?,                   // solo cerrada, ver 2.5
    errorAccion: TipoError?
)
```

### 2.5 Eventos

| Evento | Qué hace |
|---|---|
| Abrir | Carga la solicitud y, según el rol, mi postulación o las recibidas |
| Cambiar mensaje o precio | Actualiza; borra el error de ese campo |
| Perder el foco | Valida ese campo |
| Enviar postulación | Valida todo, `postularse`; al terminar, el formulario se sustituye por "Tu postulación" |
| Retirar / Aceptar / Rechazar | Abre el diálogo; al confirmar, ejecuta y recarga |
| Ver perfil | Abre P-07 |
| Contactar | `abrirConversacion` y abre P-16 |
| Calificar | Abre P-17 (solo `cerrada` sin reseña) |

**Bloque de reseña (solo cliente, solicitud `cerrada`).** Sin reseña: un
`BotonPrincipal` "Calificar el trabajo" que abre P-17. Con reseña: se muestra
publicada, con sus estrellas, su comentario y la fecha, sin acciones
(HU-27; necesita `H-51`).

### 2.6 Estados

| Estado | Qué se ve |
|---|---|
| Cargando | `Cargando` centrado |
| Error | `EstadoError` con "Reintentar" |
| Sin postulaciones (cliente, abierta) | `EstadoVacio`: "Aún nadie se ha postulado. Te avisamos cuando llegue una" |
| Enviando / aceptando | El botón muestra el indicador y se deshabilita |

---

## 3. P-14 · Mis postulaciones

Solo trabajador (HU-22, `DEC-41`). La construye `S5-T02`.

### 3.1 Anatomía

`BarraSuperior` "Mis postulaciones" con flecha; la lista, **de la más reciente
a la más antigua**. Cada `TarjetaPostulacion`: título y categoría de la
solicitud, mi precio propuesto, la etiqueta de mi estado y la fecha en que me
postulé. Tocar la tarjeta abre P-19. Las `enviada` llevan `BotonTexto` "Retirar"
con su diálogo (2.3). **Ninguna** muestra a los demás postulantes ni su número.

### 3.2 Contrato de estado

```text
EstadoMisPostulaciones(
    cargando: Boolean,
    errorCarga: TipoError?,
    postulaciones: List<PostulacionConSolicitud>,    // ver H-54
    retirando: String?,         // id en confirmación
    errorAccion: TipoError?
)
```

### 3.3 Eventos y estados

| Evento | Qué hace |
|---|---|
| Abrir | Carga la lista |
| Tocar una tarjeta | Abre P-19 de esa solicitud |
| Retirar | Diálogo; al confirmar pasa a `retirada` y la tarjeta se actualiza |
| Reintentar | Vuelve a cargar |

| Estado | Qué se ve |
|---|---|
| Vacío | `EstadoVacio`: "Aún no te postulas a nada" con `BotonPrincipal` "Ver solicitudes" (P-10) |
| Error | `EstadoError` con "Reintentar" |

---

## 4. P-15 · Conversaciones

Los dos roles (HU-25). La construye `S5-T05`. La barra inferior es la del rol
(`BarraInferiorCliente` o `BarraInferiorTrabajador`), con "Chats" marcada.

### 4.1 Anatomía

`BarraSuperior` "Chats" sin flecha; la lista **ordenada por el mensaje más
reciente** (`ultimo_mensaje_en`). Cada `TarjetaConversacion`: foto y nombre de
la **otra** persona, el texto del último mensaje en una línea recortada, la hora
o la fecha, y un **punto de "sin leer"** más el nombre en negrita cuando hay
mensajes recibidos sin leer. Si el hilo viene de una solicitud, una segunda
línea chica con su título; el hilo suelto de P-07 no la lleva. Tocar la tarjeta
abre P-16. Datos: `H-50`.

### 4.2 Contrato de estado

```text
EstadoConversaciones(
    cargando: Boolean,
    errorCarga: TipoError?,
    conversaciones: List<ResumenConversacion>   // ver H-50
)
```

### 4.3 Estados

| Estado | Qué se ve |
|---|---|
| Vacío, cliente | `EstadoVacio`: "Aún no tienes conversaciones. Escríbele a un trabajador desde su perfil" con salida a P-05 |
| Vacío, trabajador | `EstadoVacio`: "Aún no tienes conversaciones. Cuando un cliente te escriba, aparecerá aquí". **Sin botón:** el trabajador no inicia hilos (`DEC-20`) |
| Cargando / error | Los de siempre |

---

## 5. P-16 · Chat

Dos personas, texto plano (HU-26, `DEC-04`). La abre el cliente desde P-07 o P-19
(`DEC-20`, `DEC-24`); el trabajador entra desde P-15.

### 5.1 Anatomía

`BarraSuperior` con flecha, y en lugar del título, la foto y el nombre de la
otra persona (tocar el nombre de un trabajador abre su P-07; no hay perfil
público de un cliente). Si el hilo es de una solicitud, una línea bajo la barra
con su título.

La lista de `BurbujaMensaje`, **del más antiguo al más nuevo**, anclada al final:
las mías a la derecha en `Mostaza`, las de la otra persona a la izquierda en
`Arena`. Cada una lleva su hora; las mías, una marca de "leído" cuando la otra
persona ya lo vio (`leido_en`). Una línea de fecha separa los días.

Al pie, fijo sobre el teclado, el `CampoMensaje`: varias líneas hasta 4, botón de
enviar **deshabilitado con el texto en blanco**. Sin botón de adjuntar, de foto
ni de llamada.

### 5.2 Comportamiento

- **Mensajes nuevos sin actualizar** (HU-26): si la otra persona escribe con la
  pantalla abierta, el mensaje aparece solo y la lista baja al final si ya
  estaba al final; si no, aparece una pastilla "Mensaje nuevo" que lo lleva
  abajo. El mecanismo es de `S5-T07`.
- **Marcar como leído**: al abrir el hilo, y al llegar un mensaje con la
  pantalla abierta, los recibidos se marcan leídos.
- **Historial por páginas**: se cargan los más recientes y al subir se piden los
  anteriores, con "Cargando mensajes anteriores" al principio de la lista.
- **Enviar**: el mensaje aparece de inmediato como "enviando" y se confirma al
  entregarse; si falla, queda marcado con "No se envió. Toca para reintentar" y
  conserva su texto (1.4).
- Mensaje de 1 a 1,000 caracteres (`H-52`).

### 5.3 Contrato de estado

```text
EstadoChat(
    cargando: Boolean,
    errorCarga: TipoError?,
    otraPersona: ParticipanteChat,    // nombre, foto, id, esTrabajador
    solicitudTitulo: String?,
    mensajes: List<MensajeEnPantalla>,    // Mensaje + estado local: enviando | fallido
    borrador: String,
    hayMasAntiguos: Boolean,
    cargandoAnteriores: Boolean,
    mensajeNuevoFueraDeVista: Boolean,
    errorEnvio: TipoError?
)
```

### 5.4 Eventos

| Evento | Qué hace |
|---|---|
| Abrir | Carga la página más reciente y marca los recibidos como leídos |
| Escribir | Actualiza el borrador |
| Enviar | Agrega el mensaje local, `enviar`, lo confirma o lo marca fallido |
| Reintentar un mensaje fallido | Vuelve a enviarlo |
| Subir al principio | Pide la página anterior |
| Llegó un mensaje | Lo agrega, marca leído si está a la vista |
| Tocar el nombre (trabajador) | Abre P-07 |

### 5.5 Estados

| Estado | Qué se ve |
|---|---|
| Hilo recién abierto, sin mensajes | `EstadoVacio` breve: "Escribe el primer mensaje" |
| Cargando historial | `Cargando` centrado |
| Error de carga | `EstadoError` con "Reintentar"; el campo de mensaje se oculta |

El hilo **no se bloquea** cuando la solicitud se cierra o se cancela (`H-53`).

---

## 6. P-17 · Dejar reseña

Solo cliente, solo sobre una solicitud `cerrada` (HU-27). La construye `S5-T08`.
Se abre desde P-19 (2.5) y desde P-09 en las solicitudes cerradas sin reseña.

### 6.1 Anatomía

`BarraSuperior` "Calificar" con flecha. Debajo: el título de la solicitud y el
nombre y la foto del trabajador asignado; el `SelectorEstrellas`, **vacío al
principio**; `CampoTexto` "Comentario (opcional)" de varias líneas, con contador
de 500; y `BotonPrincipal` "Publicar reseña", **deshabilitado hasta elegir las
estrellas**. Un recuadro previo al botón avisa: "Tu reseña es definitiva: no se
puede editar ni borrar".

### 6.2 Reglas

- Calificación de 1 a 5, obligatoria. Comentario opcional, de hasta 500
  caracteres (`H-52`).
- Una por solicitud y definitiva: la base lo impide y la pantalla lo dice antes
  de publicar. Si el cliente ya reseñó, **esta pantalla no se abre**: P-19 le
  muestra su reseña publicada (2.5).
- Si la solicitud no está `cerrada`, la acción no existe en la pantalla que la
  ofrece (HU-27). Si de todos modos la base rechaza, se muestra su mensaje.
- Al publicar, vuelve a P-19, que muestra la reseña publicada. El promedio del
  trabajador se recalcula en la base (HU-28); P-17 no lo calcula.

### 6.3 Contrato de estado y eventos

```text
EstadoResena(
    solicitud: Solicitud?, trabajador: ParticipanteChat?,
    cargando: Boolean, errorCarga: TipoError?,
    calificacion: Int,                // 0 = sin elegir
    comentario: String, errorComentario: String?,
    publicando: Boolean, errorPublicar: TipoError?
)
```

| Evento | Qué hace |
|---|---|
| Elegir estrellas | Fija la calificación y habilita publicar |
| Cambiar comentario | Actualiza; valida el tope |
| Publicar | `dejarResena`; al terminar vuelve a P-19 |

---

## 7. Puntos de entrada en pantallas que ya existen

Ningún cambio de estructura en ellas, solo la acción que las conecta. El botón
"Calificar" de P-09 es **propuesta** de este diseño: HU-27 solo menciona P-17.

| Pantalla | Acción | Lleva a | Quién la construye |
|---|---|---|---|
| P-07 (cliente) | "Contactar" (ya diseñado en `S4-T01`, sección 6) | P-16, hilo suelto | `S5-T05` y `S5-T07` |
| P-09 | Tocar una solicitud | P-19, vista del cliente | `S5-T03` |
| P-09, cerrada sin reseña | "Calificar" | P-17 | `S5-T08` |
| P-10 | Tocar una solicitud | P-19, vista del trabajador | `S5-T02` |
| P-12 y P-18 (trabajador) | "Mis postulaciones" | P-14 | `S5-T02` |

---

## 8. Cadenas nuevas de `strings.xml`

Las claves se agregan al construir cada pantalla; el texto es el del diseño.

| Clave | Texto |
|---|---|
| `detalle_solicitud_titulo` | Detalle de solicitud |
| `postular_mensaje_etiqueta` | Tu mensaje |
| `postular_precio_etiqueta` | Tu precio |
| `postular_enviar` | Enviar postulación |
| `postular_cerrada` | Esta solicitud ya no recibe postulaciones |
| `postular_tu_postulacion` | Tu postulación |
| `postular_retirar` | Retirar postulación |
| `postular_retirar_confirmar` | ¿Retirar tu postulación? |
| `postular_aceptada_trabajador` | El cliente te eligió. Te escribirá por Chats |
| `postulaciones_titulo` | Postulaciones |
| `postulaciones_vacio` | Aún nadie se ha postulado. Te avisamos cuando llegue una |
| `postulacion_aceptar` | Aceptar |
| `postulacion_rechazar` | Rechazar |
| `postulacion_ver_perfil` | Ver perfil |
| `postulacion_contactar` | Contactar |
| `postulacion_aceptar_confirmar` | ¿Aceptar a %1$s? Las demás postulaciones se rechazarán y la solicitud quedará asignada |
| `postulacion_rechazar_confirmar` | ¿Rechazar la postulación de %1$s? |
| `solicitud_cancelada_aviso` | Cancelaste esta solicitud |
| `mis_postulaciones_titulo` | Mis postulaciones |
| `mis_postulaciones_vacio` | Aún no te postulas a nada |
| `mis_postulaciones_ver_solicitudes` | Ver solicitudes |
| `estado_postulacion_enviada` | Enviada |
| `estado_postulacion_aceptada` | Aceptada |
| `estado_postulacion_rechazada` | Rechazada |
| `estado_postulacion_retirada` | Retirada |
| `chats_titulo` | Chats |
| `chats_vacio_cliente` | Aún no tienes conversaciones. Escríbele a un trabajador desde su perfil |
| `chats_vacio_trabajador` | Aún no tienes conversaciones. Cuando un cliente te escriba, aparecerá aquí |
| `chat_campo_hint` | Escribe un mensaje |
| `chat_enviar` | Enviar |
| `chat_primer_mensaje` | Escribe el primer mensaje |
| `chat_mensaje_nuevo` | Mensaje nuevo |
| `chat_cargando_anteriores` | Cargando mensajes anteriores |
| `chat_no_enviado` | No se envió. Toca para reintentar |
| `resena_titulo` | Calificar |
| `resena_comentario_etiqueta` | Comentario (opcional) |
| `resena_definitiva` | Tu reseña es definitiva: no se puede editar ni borrar |
| `resena_publicar` | Publicar reseña |
| `resena_calificar_trabajo` | Calificar el trabajo |

---

## 9. Navegación

```
P-10 ──► P-19 (trabajador) ──► postularse ──► P-14 ──► P-19
P-12 / P-18 ──► P-14
P-09 ──► P-19 (cliente) ──► Ver perfil ──► P-07
                       ├──► Contactar ──► P-16 ◄── P-15 (ambos roles)
                       └──► Calificar (cerrada) ──► P-17 ──► P-19
P-07 (cliente) ──► Contactar ──► P-16 (hilo suelto)
```

P-15 es destino de la barra inferior de los dos roles; P-14, P-16, P-17 y P-19
se abren encima y **no** llevan barra. El trabajador solo llega a P-16 desde P-15.

---

## 10. Hallazgos

Salen de escribir este diseño. **Ninguno se resolvió aquí**: los decide el
líder o la tarea que se nombra.

**`H-49` · Faltan cinco componentes y los colores de estado de postulación.**
`DISENO.md` no tiene `TarjetaPostulacion`, `TarjetaConversacion`,
`BurbujaMensaje`, `CampoMensaje` ni `SelectorEstrellas`, y `EtiquetaEstado` solo
conoce los estados de la solicitud. **Propuesta:** el líder los registra en
`DISENO.md` antes de que `S5-T02`, `S5-T05` y `S5-T08` los construyan; para las
postulaciones, `enviada` con `Advertencia`, `aceptada` con `Éxito`, `rechazada`
con `Error` y `retirada` con `Cafe`. Mientras tanto, nadie escribe un botón o
una tarjeta a mano (`DISENO.md`).

**`H-50` · P-15 no tiene de dónde leer la bandeja.** Cada fila necesita el
nombre y la foto de la otra persona, el último mensaje y cuántos hay sin leer, y
`conversaciones` solo trae ids y `ultimo_mensaje_en`. Armarla con la tabla
cuesta una consulta por hilo. **Propuesta:** una vista o RPC (`vw_mis_conversaciones`)
que lo devuelva en una sola llamada. **Le toca a `S5-T06`.**

**`H-51` · No hay operación para leer mi reseña.** HU-27 pide ver la reseña ya
publicada, y `RepositorioResenas` solo tiene `dejarResena`. Hace falta una
lectura por solicitud (`obtenerResena(solicitudId)`). **Le toca a `S5-T06`.**

**`H-52` · Nada limita el largo de lo que se escribe.** `postulaciones.mensaje`,
`mensajes.contenido` y `resenas.comentario` son `text` sin tope, y
`postulaciones.mensaje` y `precio_propuesto` aceptan nulo aunque HU-21 pide los
dos. **Propuesta**, como en `H-35`: mensaje de postulación de 1 a 500, mensaje de
chat de 1 a 1,000, comentario de reseña de hasta 500, y precio obligatorio de 0 a
99,999.99 **en la aplicación**; el tope en la base (`CHECK`) lo decide el líder.

**`H-53` · No se define qué pasa con el chat al cerrar o cancelar la solicitud.**
La base no bloquea escribir. **Propuesta:** el hilo queda abierto, sin cambios;
es lo más sencillo y no pierde un cierre de acuerdo. Lo confirma el líder.

**`H-54` · P-14 y P-19 necesitan datos que `postulaciones` no trae.** P-14 pide el
título y la categoría de la solicitud; P-19 del cliente, el nombre, la foto y la
calificación del trabajador. La tabla solo tiene ids. **Propuesta:** un `select`
con las relaciones embebidas, o una vista. **Le toca a `S5-T06`.**

**`H-55` · Entre "aceptada" y el primer mensaje, el trabajador no tiene canal.**
Por `DEC-20` solo el cliente abre el hilo y `fn_aceptar_postulacion` no lo
abre. **Propuesta:** que la función lo abra al aceptar (hilo de esa solicitud,
`DEC-24`), o que P-19 del trabajador diga que debe esperar (como se diseñó aquí).
Lo decide el líder.

Siguen abiertos de antes y tocan estas pantallas: **`H-47`** (cancelar una
solicitud `asignada` no está definido, 2.3).

**Actualización de `S5-T06`:** el contrato resuelve `H-50` (vista `vw_mis_conversaciones`), `H-51`
(`obtenerResena`) y `H-54` (datos embebidos) en `CONTRATOS-API.md`, y agrega tres hallazgos de la base:
`H-56`, `H-57` y `H-58`. Siguen abiertos `H-49`, `H-52` (con valores propuestos), `H-53` y `H-55`.
