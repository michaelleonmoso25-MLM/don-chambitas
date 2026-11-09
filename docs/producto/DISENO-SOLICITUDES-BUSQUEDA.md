# Diseño de las pantallas del cliente · P-05, P-06, P-08 y P-09 (más P-07 para el cliente)

> Entregable de `S4-T01`. Dice cómo se ven y se comportan las pantallas con las
> que un cliente **encuentra** a un trabajador (P-05, P-06) y **publica lo que
> necesita** (P-08, P-09), y cómo cambia P-07 según quién la mira. **Solo diseño:
> ni Kotlin ni `strings.xml`.** Las construyen `S4-T02` a `S4-T07`.
>
> Sigue la estructura de `DISENO-PERFIL-SERVICIOS.md` y **hereda sus reglas
> comunes** (1.1 a 1.8) salvo donde este documento diga otra cosa.

## 0. Alcance de esta tarea

### Qué entra

- **P-05 Inicio cliente**, **P-06 Resultados**, **P-08 Publicar solicitud** y
  **P-09 Mis solicitudes**: anatomía, contrato de estado, eventos, estados vacío,
  carga y error.
- **P-07 para el cliente**: qué cambia respecto de la vista previa del
  trabajador de `S3-T14` y cómo se resuelve `H-39`.
- Las cadenas nuevas, con su clave, y la navegación entre las cinco.

### Qué NO entra

- **Código.** Las claves se listan aquí; las agrega la tarea que construye cada
  pantalla.
- **P-19 Detalle de solicitud**, la mensajería y las postulaciones: Sprint 5.
- **Los contratos.** `RepositorioCatalogos.buscarTrabajadores` y
  `RepositorioSolicitudes` ya existen en `CONTRATOS-API.md`; el detalle de cada
  operación es de `S4-T08`.
- **El botón "Redactar con IA" de P-08** (`H-29`) y la categoría sugerida por IA
  (HU-32, opcional): no hay función para ellos. Aquí solo se deja dicho que no
  se diseñan.
- **Las sugerencias de trabajadores con IA** (HU-33, opcional).
- **Wireframes en PNG.** El documento lleva bocetos en texto; los PNG, si el
  equipo los quiere, se agregan a `WIREFRAMES.md` aparte.

### De dónde sale cada cosa

| Cosa | Fuente |
|---|---|
| Qué hace cada pantalla | `HISTORIAS.md` HU-12 a HU-18, HU-19 y HU-28 |
| Qué datos hay | `CONTRATOS-API.md`: "Búsqueda y catálogos" y "Solicitudes" |
| Qué columnas y topes existen | `basedatos/01_esquema.sql`: `solicitudes` y `vw_busqueda_trabajadores` |
| Reglas de estructura, estados y accesibilidad | `DISENO-PERFIL-SERVICIOS.md` 1.1 a 1.8 |
| Componentes | `DISENO.md` y `ui/componentes` |

---

## 1. Reglas comunes a las cuatro pantallas

### 1.1 Estructura

Las mismas de `DISENO-PERFIL-SERVICIOS.md` 1.1 (márgenes de 16 dp, bloques a
24 dp, `Crema`, `BarraSuperior` en `Mostaza`), con la barra inferior del
**cliente** (`BarraInferiorCliente`: Inicio, Solicitudes, Chats, Cuenta).

| Pantalla | Barra inferior | Pestaña marcada | Flecha de regreso | Por qué |
|---|---|---|---|---|
| P-05 | sí | Inicio | no | Es destino de la barra |
| P-06 | **no** | — | sí | `PANTALLAS.md` la lista entre las que se abren encima |
| P-07 | **no** | — | sí | Ídem |
| P-08 | **no** | — | sí | Ídem |
| P-09 | sí | Solicitudes | no | Es destino de la barra |

### 1.2 Componentes que se usan

Solo los de `DISENO.md`. Ninguno es nuevo.

| Componente | Dónde |
|---|---|
| `ChipCategoria` | Las 16 categorías en P-05 y el selector de P-06 y P-08 |
| `TarjetaTrabajador` | Mejores calificados (P-05) y la lista de P-06 |
| `TarjetaSolicitud` + `EtiquetaEstado` | La lista de P-09 |
| `Estrellas` | Calificación en las tarjetas y en P-07 |
| `CampoTexto` | Buscador, título, descripción, presupuesto |
| `ContenedorEstado`, `Cargando`, `EstadoVacio`, `EstadoError` | Los cuatro estados |
| `BotonPrincipal`, `BotonSecundario`, `BotonTexto` | Acciones |

Dos tarjetas **no alcanzan** para lo que piden las historias: ver `H-42` y
`H-43`. Se reportan; no se inventa un componente.

### 1.3 Los cuatro estados

Igual que en `DISENO-PERFIL-SERVICIOS.md` 1.3: cargando, vacío, error y
contenido, con `ContenedorEstado`. Cuál aplica a cada pantalla:

| Pantalla | Cargando | Vacío | Error | Contenido |
|---|---|---|---|---|
| P-05 | sí | no (las secciones sin datos se ocultan) | sí | sí |
| P-06 | sí, y "cargando más" al pie | sí, con las dos salidas de HU-17 | sí, y "error al cargar más" al pie | sí |
| P-08 | sí, solo los catálogos | no | sí, de carga y de publicar | sí |
| P-09 | sí | sí, con salida a P-08 | sí | sí |

### 1.4 Dónde se pinta cada error

Los de campo, debajo del campo, como en 1.4 de `DISENO-PERFIL-SERVICIOS.md`.
Los de pantalla (`TipoError`), en `EstadoError` con "Reintentar" si fue la
carga, y en una línea sobre el botón si fue publicar. `LIMITE_IA` no aplica
aquí: ninguna de estas pantallas llama a la IA.

### 1.5 Cuándo se valida

Solo P-08 tiene formulario. Misma regla que `DISENO-PERFIL-SERVICIOS.md` 1.5:
al perder el foco un campo ya tocado, y todos al publicar, con el foco en el
primero que falle. El buscador de P-05 y de P-06 **no valida**: busca lo que
haya, incluido nada.

### 1.6 Normalización antes de enviar

`trim()` en título y descripción; en la búsqueda, `trim()` y espacios de en
medio colapsados a uno solo. Un texto de búsqueda en blanco **no se manda**: es
"sin texto".

---

## 2. P-05 · Inicio cliente

### 2.1 Anatomía, de arriba hacia abajo

```
BarraSuperior "Inicio" (sin flecha)
 ├─ CampoTexto buscador  "¿Qué necesitas?"      (acción del teclado: Buscar)
 ├─ Título "Oficios"
 │    └─ Las 16 categorías como ChipCategoria, en filas que se acomodan
 ├─ Título "Los mejor calificados"
 │    └─ Hasta 5 TarjetaTrabajador, una bajo otra
 └─ Botón flotante (+)  → P-08
BarraInferiorCliente (Inicio marcada)
```

- **El buscador** no busca en esta pantalla: al pulsar "Buscar" lleva a P-06 con
  el texto ya escrito (HU-17).
- **Las categorías** vienen de `obtenerCategorias()` (cacheado). Tocar una lleva
  a P-06 con esa categoría ya filtrada (HU-16).
- **Los mejor calificados** son `buscarTrabajadores(sin filtros, orden =
  PROMEDIO)`, primera página, **recortada a 5**. Tocar uno lleva a P-07.
- **Si no hay ningún trabajador**, esa sección **no se pinta**: P-05 no es el
  lugar para decir "no hay nadie".
- **El botón flotante (+)** lleva a P-08. Es la única entrada a publicar fuera
  de P-09.

### 2.2 Contrato de estado

```kotlin
data class EstadoInicioCliente(
    val cargando: Boolean = true,
    val errorCarga: TipoError? = null,
    val categorias: List<Categoria> = emptyList(),
    val mejores: List<ResumenTrabajadorBusqueda> = emptyList(),
    val texto: String = ""
)
```

### 2.3 Eventos

| Evento | Qué hace |
|---|---|
| `alCambiarTexto(valor)` | Actualiza `texto` |
| `alBuscar()` | Si hay texto, navega a P-06 con él; si está en blanco, navega a P-06 sin texto |
| `alElegirCategoria(id)` | Navega a P-06 con esa categoría |
| `alAbrirTrabajador(id)` | Navega a P-07 |
| `alPublicar()` | Navega a P-08 |
| `alReintentarCarga()` | Repite la carga |

### 2.4 Estados

- **Cargando:** `Cargando` a pantalla completa. Primero salen las categorías, que
  casi siempre están en caché; los mejor calificados pueden llegar después, con su
  propio indicador dentro de la sección.
- **Error de carga:** `EstadoError` con "Reintentar". Si fallan solo los mejor
  calificados, **las categorías se quedan** y la sección muestra el aviso: el
  usuario todavía puede buscar.

---

## 3. P-06 · Resultados

### 3.1 Anatomía, de arriba hacia abajo

```
BarraSuperior "Resultados" (con flecha)
 ├─ CampoTexto buscador  (con el texto que traía P-05; botón de borrar)
 ├─ Fila de filtros:  [Filtros (n)]  y un chip por cada filtro activo, removible
 ├─ Fila de orden:    "Ordenar por: Mejor calificados ▾"
 ├─ Lista (LazyColumn) de TarjetaTrabajador, 12 dp entre tarjetas
 │    └─ Al pie: indicador "cargando más" o aviso de error con "Reintentar"
 └─ (sin barra inferior)
```

### 3.2 Los filtros

Se eligen en una **hoja inferior** ("Filtros") con los cinco de HU-18, todos
opcionales y combinables a la vez:

| Filtro | Cómo se elige | Va contra |
|---|---|---|
| Categoría | Selector de una sola con las 16 | `categorias=cs.{id}` (arreglo) |
| Estado | Hoja de lista, como P-11 | `estado_id` |
| Municipio | Hoja de lista; **deshabilitado hasta elegir estado** | `municipio_id` |
| Precio máximo | Campo numérico, en pesos | `precio_desde` ≤ ese valor |
| Calificación mínima | Selector: "Cualquiera", "3 o más", "4 o más", "4.5 o más" | `promedio` ≥ ese valor |

- **Cambiar de estado borra el municipio**, igual que P-11.
- La hoja tiene "Aplicar" y "Quitar filtros". El botón "Filtros (n)" muestra
  cuántos hay activos.
- Cada filtro activo sale además como **chip removible** en la fila de filtros,
  para quitar uno sin abrir la hoja.

### 3.3 El orden

| Opción | Columna |
|---|---|
| **Mejor calificados** (por omisión) | `promedio`, de mayor a menor |
| Precio más bajo | `precio_desde`, de menor a mayor |
| Más recientes | `creado_en`, de más nuevo a más viejo |

**Cambiar el orden conserva los filtros** (HU-18).

### 3.4 La búsqueda

- Busca **al pulsar "Buscar"** en el teclado, no mientras se escribe: cada
  petición pega en la base y el usuario escribe más rápido de lo que responde.
- HU-17 pide que "plomeria" encuentre "plomería". Con `ilike` eso **no
  funciona**: ver `H-40`.

### 3.5 Las páginas

- Cada página son **20 trabajadores** (`range()`, ver `CONTRATOS-API.md`).
- La siguiente se pide cuando **faltan 5 tarjetas** para el final. Mientras
  llega, el pie muestra el indicador.
- **No se pierde la posición**: la lista conserva su `LazyListState`.
- **Cambiar un filtro, el orden o el texto** vuelve a la primera página y arriba
  de la lista. Eso sí es empezar de nuevo.
- **Si una página es más corta que 20**, es la última: el pie desaparece y no se
  vuelve a pedir.

### 3.6 Contrato de estado

```kotlin
data class EstadoResultados(
    val cargando: Boolean = true,
    val errorCarga: TipoError? = null,
    val texto: String = "",
    val filtros: FiltrosBusquedaTrabajadores = FiltrosBusquedaTrabajadores(),
    val resultados: List<ResumenTrabajadorBusqueda> = emptyList(),
    val cargandoMas: Boolean = false,
    val errorAlCargarMas: Boolean = false,
    val hayMas: Boolean = true,
    val hojaFiltrosAbierta: Boolean = false,
    val categorias: List<Categoria> = emptyList(),
    val estados: List<Estado> = emptyList(),
    val municipios: List<Municipio> = emptyList()
)
```

`FiltrosBusquedaTrabajadores` y `OrdenBusqueda` ya existen en el dominio: el
orden y los cinco filtros viajan dentro.

### 3.7 Eventos

| Evento | Qué hace |
|---|---|
| `alCambiarTexto(valor)` | Actualiza `texto`; no busca |
| `alBuscar()` | Aplica `texto` y pide la primera página |
| `alAbrirFiltros()` / `alCerrarFiltros()` | Muestra u oculta la hoja |
| `alAplicarFiltros(filtros)` | Guarda los filtros y pide la primera página |
| `alQuitarFiltro(cual)` / `alQuitarTodosLosFiltros()` | Quita uno o todos y pide la primera página |
| `alElegirEstadoEnFiltros(id)` | Borra el municipio y trae los suyos |
| `alElegirOrden(orden)` | Guarda el orden, **conserva los filtros** y pide la primera página |
| `alLlegarAlFinal()` | Pide la página siguiente si `hayMas` y no está cargando |
| `alReintentarCarga()` / `alReintentarMas()` | Repite la primera página o la que falló |
| `alAbrirTrabajador(id)` | Navega a P-07 |

### 3.8 Estados

- **Cargando la primera página:** `Cargando` a pantalla completa bajo la fila de
  filtros: los filtros se siguen viendo y se pueden cambiar.
- **Vacío (HU-17):** `EstadoVacio` con el título "No encontramos trabajadores" y
  el mensaje "Prueba quitando filtros o revisa la ubicación". Si hay filtros
  activos, lleva el botón "Quitar filtros". **Nunca una lista en blanco.**
- **Error de la primera página:** `EstadoError` con "Reintentar".
- **Error al cargar más:** la lista **se queda** y el pie muestra "No pudimos
  cargar más" con "Reintentar". No se vacía la pantalla por un fallo de la
  página 2.

---

## 4. P-08 · Publicar solicitud

### 4.1 Anatomía, de arriba hacia abajo

Un formulario con scroll (`imePadding()` y `verticalScroll`, como P-11 y P-13):

```
BarraSuperior "Publicar solicitud" (con flecha)
 ├─ CampoTexto  Título                     "¿Qué necesitas?"
 ├─ Título "Categoría"
 │    └─ Las 16 como ChipCategoria, selección única (como P-13)
 ├─ CampoTexto  Descripción (varias líneas) "Cuéntanos con detalle"
 ├─ CampoTexto  Presupuesto (opcional)      con "Opcional" como ayuda
 ├─ Título "Dónde"
 │    ├─ Selector Estado        (hoja)
 │    └─ Selector Municipio     (hoja; deshabilitado hasta elegir estado)
 ├─ Línea de error de pantalla (si publicar falló)
 └─ BotonPrincipal "Publicar solicitud"
```

**No hay botón "Redactar con IA"** (`H-29`): `PANTALLAS.md` lo menciona pero no
existe `FuncionIa` para P-08. Cuando haya función, se agrega sobre la
descripción, como en P-13. Tampoco se sugiere la categoría (HU-32, opcional).

### 4.2 Los campos y sus reglas

| Campo | ¿Obligatorio? | Regla | Fuente |
|---|---|---|---|
| Título | sí | De 1 a 120 caracteres | `solicitudes.titulo varchar(120)` |
| Categoría | sí | Una de las 16 | `categoria_id not null` |
| Descripción | sí | No vacía; tope de **1000** caracteres (propuesta, `H-44`) | `descripcion text not null`, sin tope |
| Presupuesto | **no** | Si viene: número ≥ 0, hasta 2 decimales, máximo 99,999,999.99 | `numeric(10,2)`, `>= 0` |
| Estado | sí | Uno de los 32 | HU-12; la base lo permite nulo (`H-41`) |
| Municipio | sí | Del estado elegido | HU-12; la base lo permite nulo (`H-41`) |

El presupuesto se escribe igual que el precio de P-13
(`DISENO-PERFIL-SERVICIOS.md` 3.4). Vacío es válido: HU-12.

### 4.3 Contrato de estado

```kotlin
data class EstadoSolicitudNueva(
    val cargando: Boolean = true,
    val errorCarga: TipoError? = null,
    val titulo: String = "",
    val categoriaId: Int? = null,
    val descripcion: String = "",
    val presupuesto: String = "",
    val estadoId: Int? = null,
    val municipioId: Int? = null,
    val categorias: List<Categoria> = emptyList(),
    val estados: List<Estado> = emptyList(),
    val municipios: List<Municipio> = emptyList(),
    @StringRes val errorTitulo: Int? = null,
    @StringRes val errorCategoria: Int? = null,
    @StringRes val errorDescripcion: Int? = null,
    @StringRes val errorPresupuesto: Int? = null,
    @StringRes val errorEstado: Int? = null,
    @StringRes val errorMunicipio: Int? = null,
    val errorPantalla: TipoError? = null,
    val publicando: Boolean = false,
    val publicada: Boolean = false     // evento de un solo uso: navegar a P-09
)
```

### 4.4 Eventos

| Evento | Qué hace |
|---|---|
| `alCambiarTitulo/Descripcion/Presupuesto(valor)` | Actualiza y limpia su error |
| `alElegirCategoria(id)` | Actualiza y limpia su error |
| `alElegirEstado(id)` | Borra el municipio y trae los suyos |
| `alElegirMunicipio(id)` | Actualiza |
| `alPerderFoco(campo)` | Valida el campo si ya se tocó |
| `alPublicar()` | Valida todo; si pasa, `RepositorioSolicitudes.crear` |
| `alReintentarCarga()` | Repite la carga de catálogos |

### 4.5 Qué pasa al pulsar "Publicar solicitud"

1. Valida todo. Con un error, no llama al repositorio y lleva el foco al primero.
2. Si pasa, `publicando = true`: el botón queda en `cargando` y los campos no se
   bloquean.
3. **Éxito:** la solicitud nace `abierta`, la pantalla se cierra y P-09 se
   muestra con el aviso "Solicitud publicada".
4. **Error:** se queda en la pantalla con la línea de error sobre el botón
   (`RED`, `SERVIDOR`, `DESCONOCIDO`). Lo escrito no se pierde.

Un trabajador no llega a esta pantalla: la guarda lo redirige (HU-12).

---

## 5. P-09 · Mis solicitudes

### 5.1 Anatomía

```
BarraSuperior "Mis solicitudes" (sin flecha)
 ├─ LazyColumn de TarjetaSolicitud, 12 dp entre tarjetas
 │    └─ título, categoría, presupuesto o "A convenir", EtiquetaEstado, fecha
 └─ Botón flotante (+)  → P-08
BarraInferiorCliente (Solicitudes marcada)
```

- **Orden:** primero las **activas** (`abierta` y `asignada`) y luego las
  `cerrada` y `cancelada`; dentro de cada grupo, la más reciente arriba.
- **Tocar una** lleva a P-19, que es de Sprint 5.
- **El número de postulaciones sin revisar** (HU-13) no se puede mostrar hoy:
  ver `H-42`.

### 5.2 Contrato de estado

```kotlin
data class EstadoMisSolicitudes(
    val cargando: Boolean = true,
    val errorCarga: TipoError? = null,
    val solicitudes: List<Solicitud> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    @StringRes val aviso: Int? = null
)
```

### 5.3 Eventos

| Evento | Qué hace |
|---|---|
| `alAbrirSolicitud(id)` | Navega a P-19 |
| `alPublicar()` | Navega a P-08 |
| `alReintentarCarga()` | Repite la carga |
| `alConsumirAviso()` | Pone `aviso` en `null` |

### 5.4 Estados

- **Vacío (HU-13):** `EstadoVacio` con "Aún no has publicado solicitudes",
  "Cuéntanos qué necesitas y los trabajadores se acercan a ti" y el botón
  "Publicar mi primera solicitud", que lleva a P-08.
- **Cargando y error:** `Cargando` y `EstadoError` con "Reintentar".

---

## 6. P-07 para el cliente (resuelve `H-39`)

`S3-T14` dejó P-07 como **vista previa del trabajador**: sus textos vacíos le
hablan a él ("Aún no tienes servicios publicados…"). Cuando la abre un cliente,
tienen que cambiar. La pantalla decide el modo comparando el identificador del
perfil con el de la sesión.

| Elemento | Vista previa (el trabajador) | Cliente |
|---|---|---|
| Aviso de arriba | **"Así te ven los clientes"** | Ninguno |
| Sobre mí vacío | "Aún no escribes tu descripción. Agrégala en Mi perfil…" | **La sección se oculta** |
| Habilidades vacías | "Aún no agregas habilidades. Agrégalas en Mi perfil…" | **La sección se oculta** |
| Servicios vacíos | "Aún no tienes servicios publicados. Publica uno en Mis servicios…" | "Este trabajador todavía no publica servicios." |
| Reseñas vacías | "Todavía no tienes reseñas. Aparecen cuando un cliente califica un trabajo terminado." | "Todavía no tiene reseñas." |
| Calificación vacía | "Aún sin calificaciones" | Igual |
| Botón "Contactar" | No sale | **Sale**, fijo al pie, `BotonPrincipal` |

- **Una sección que el cliente no necesita ver vacía se oculta** en vez de
  mostrar un hueco: el cliente no puede arreglarlo.
- **"Contactar"** abre la conversación con ese trabajador (P-16). Su contrato y su
  comportamiento son de `S5-T06`; aquí solo se fija **dónde está y quién lo ve**.
- Sigue en pie `H-25`: un perfil sin estado o municipio no se puede leer.

---

## 7. Cadenas nuevas de `strings.xml`

Las agrega la tarea que construye cada pantalla. Las claves no se repiten con
las de `S3-T14` (`perfil_publico_*`), salvo las dos de P-07 que cambian.

| Clave | Texto |
|---|---|
| `inicio_cliente_titulo` | Inicio |
| `inicio_cliente_buscador` | ¿Qué necesitas? |
| `inicio_cliente_oficios` | Oficios |
| `inicio_cliente_mejores` | Los mejor calificados |
| `inicio_cliente_publicar` | Publicar solicitud (descripción del botón flotante) |
| `resultados_titulo` | Resultados |
| `resultados_filtros` | Filtros |
| `resultados_filtros_n` | Filtros (%1$d) |
| `resultados_aplicar` | Aplicar |
| `resultados_quitar_filtros` | Quitar filtros |
| `resultados_orden_etiqueta` | Ordenar por: %1$s |
| `resultados_orden_calificacion` | Mejor calificados |
| `resultados_orden_precio` | Precio más bajo |
| `resultados_orden_recientes` | Más recientes |
| `resultados_filtro_precio_max` | Precio máximo |
| `resultados_filtro_calificacion` | Calificación mínima |
| `resultados_calificacion_cualquiera` | Cualquiera |
| `resultados_calificacion_n` | %1$s o más |
| `resultados_vacio_titulo` | No encontramos trabajadores |
| `resultados_vacio_mensaje` | Prueba quitando filtros o revisa la ubicación |
| `resultados_error_mas` | No pudimos cargar más |
| `solicitud_titulo` | Publicar solicitud |
| `solicitud_campo_titulo` | ¿Qué necesitas? |
| `solicitud_campo_descripcion` | Cuéntanos con detalle |
| `solicitud_campo_presupuesto` | Presupuesto |
| `solicitud_presupuesto_ayuda` | Opcional |
| `solicitud_donde` | Dónde |
| `solicitud_publicar` | Publicar solicitud |
| `solicitud_publicada` | Solicitud publicada |
| `mis_solicitudes_titulo` | Mis solicitudes |
| `mis_solicitudes_vacio_titulo` | Aún no has publicado solicitudes |
| `mis_solicitudes_vacio_mensaje` | Cuéntanos qué necesitas y los trabajadores se acercan a ti |
| `mis_solicitudes_vacio_boton` | Publicar mi primera solicitud |
| `solicitud_presupuesto_convenir` | A convenir |
| `perfil_publico_aviso_vista_previa` | Así te ven los clientes |
| `perfil_publico_cliente_sin_servicios` | Este trabajador todavía no publica servicios. |
| `perfil_publico_cliente_sin_resenas` | Todavía no tiene reseñas. |
| `perfil_publico_contactar` | Contactar |

Los mensajes de validación de P-08 (título vacío o largo, categoría sin elegir,
descripción vacía o larga, presupuesto inválido, estado y municipio sin elegir)
siguen el patrón `validacion_*` de `DISENO-PERFIL-SERVICIOS.md` sección 5.

---

## 8. Navegación

| Desde | Acción | Hacia | Cómo |
|---|---|---|---|
| P-05 | Buscar, con o sin texto | P-06 | `Ruta.Resultados` con el texto |
| P-05 | Elegir una categoría | P-06 | `Ruta.Resultados` con la categoría |
| P-05 | Tocar un trabajador | P-07 | `Ruta.PerfilTrabajador.crearRuta(id)` |
| P-05, P-09 | Botón flotante (+) | P-08 | `Ruta.PublicarSolicitud` |
| P-06 | Tocar un trabajador | P-07 | `Ruta.PerfilTrabajador.crearRuta(id)` |
| P-08 | Publicar con éxito | P-09 | `Ruta.MisSolicitudes`, sacando P-08 de la pila |
| P-09 | Tocar una solicitud | P-19 | `Ruta.DetalleSolicitud.crearRuta(id)` |
| P-09 | "Publicar mi primera solicitud" | P-08 | `Ruta.PublicarSolicitud` |
| P-07 (cliente) | Contactar | P-16 | `S5-T06` |

**`Ruta.Resultados` hoy no recibe argumentos.** Para llevar texto y categoría
hay que agregárselos. Eso es de `S4-T03` y se reporta como `H-45`.

---

## 9. Qué consume cada tarea del Sprint 4

| Tarea | Qué toma de aquí |
|---|---|
| `S4-T02` Publicar solicitud (P-08) | Secciones 4 y 7 |
| `S4-T03` Resultados y perfil público | Secciones 3, 6 y 7 |
| `S4-T04` Buscador simple | 2.1 y 3.4 |
| `S4-T05` Filtros | 3.2 |
| `S4-T06` Panel del cliente (P-09) | Sección 5 |
| `S4-T07` ViewModels, paginación y orden | 3.3, 3.5 a 3.7 |
| `S4-T08` Contrato de solicitudes y búsqueda | `H-40` a `H-45` |
| `S4-T09` Búsqueda real en Supabase | `H-40` |

---

## 10. Hallazgos para el líder

Salen de diseñar estas pantallas contra `HISTORIAS.md`, `CONTRATOS-API.md` y el
esquema. **Ninguno se resolvió aquí**: los decide el líder o la tarea que se
nombra.

**`H-40` · La búsqueda por texto no ignora acentos, y HU-17 lo exige.** El
contrato dice "texto libre contra `titulo` (`ilike`)". `ilike` ignora mayúsculas,
no acentos: "plomeria" no encuentra "plomería". Hace falta `unaccent` en la
vista o una columna de búsqueda normalizada. **Le toca a `S4-T08`, y al líder
porque cambia la base.**

**`H-41` · La ubicación de una solicitud es obligatoria en HU-12 y opcional en la
base.** `solicitudes.estado_id` y `municipio_id` aceptan `null`. Este diseño las
pide en la pantalla; la base no lo impone, así que otro cliente de la API podría
publicar sin ubicación. **Lo decide el líder**: dejarlo en la pantalla o agregar
el `CHECK`.

**`H-42` · No hay cómo mostrar las postulaciones sin revisar en P-09.** HU-13 lo
pide. `TarjetaSolicitud` no tiene ese dato y `obtenerMisSolicitudes()` no trae el
conteo. Se necesita una columna o vista con el conteo y extender la tarjeta
(como se hizo con `TarjetaServicio`, `H-21`). **Le toca a `S4-T08`, y al líder
para la tarjeta.**

**`H-43` · `TarjetaTrabajador` no muestra el precio ni si está disponible.** El
filtro y el orden por precio de P-06 piden ver el "desde $", y la vista devuelve
`disponible`. Falta extender la tarjeta y decidir si un trabajador no disponible
sale en la lista, con su etiqueta, o se oculta. **Lo decide el líder.**

**`H-44` · La descripción de una solicitud no tiene tope en la base.**
`descripcion text not null`. Este diseño propone 1000 caracteres en la pantalla,
como se propuso para la entrada de IA (`H-35`). **Lo confirma el líder.**

**`H-45` · `Ruta.Resultados` no recibe argumentos.** P-05 necesita pasarle texto o
categoría. **Le toca a `S4-T03`.**

### Qué propuso `S4-T08` para cada uno

`S4-T08` escribió el contrato de detalle en `CONTRATOS-API.md` ("Búsqueda y catálogos" y
"Solicitudes") y dejó los hallazgos, con su SQL propuesto, en "Hallazgos de `S4-T08`". **Nada se
aplicó a `basedatos/`.**

| Hallazgo | Qué se propuso | Quién decide |
|---|---|---|
| `H-40` | Columna `texto_busqueda` en la vista, con `unaccent` | Líder; la implementa `S4-T09` |
| `H-41` | Trigger `before insert` que exija estado y municipio, y que el municipio sea del estado | Líder |
| `H-42` | Vista `vw_mis_solicitudes` con las postulaciones sin revisar (o el conteo embebido, sin verificar) | Líder; la implementa `S4-T09` |
| `H-43` | No cambia el contrato; falta decidir la tarjeta y si un no disponible sale | Líder |
| `H-44` | `CHECK` de 1,000 caracteres, si se confirma | Líder |
| `H-45` | Cerrado por `S4-T03` | — |

Y tres nuevos, que salieron de leer la base: **`H-46`** (cualquier usuario con sesión lee las
solicitudes abiertas de todos), **`H-47`** (cancelar una `asignada` no está definido) y **`H-48`**
(un trabajador sin ubicación hace fallar la página de resultados).
