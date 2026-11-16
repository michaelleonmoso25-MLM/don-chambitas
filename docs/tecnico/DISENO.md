# Sistema de diseño

Paleta **Taller**. La identidad nace del casco de seguridad amarillo: mostaza
como color principal, terracota como acento, fondo cálido.

## Colores

```kotlin
val Mostaza        = Color(0xFFE0A11B)  // primario
val MostazaOscuro  = Color(0xFFB37D08)  // presionado, bordes de énfasis
val Terracota      = Color(0xFFC1440E)  // acento, acciones destacadas
val Carbon         = Color(0xFF2B1F14)  // texto principal
val Cafe           = Color(0xFF7A6A58)  // texto secundario
val Crema          = Color(0xFFFFFDF8)  // fondo
val Arena          = Color(0xFFF7F0E4)  // superficie, tarjetas
val Borde          = Color(0xFFE6DAC6)  // separadores

val Exito       = Color(0xFF2E7D32)
val Advertencia = Color(0xFFED6C02)
val Error       = Color(0xFFC62828)
```

### Reglas de contraste, obligatorias

**Sobre mostaza va texto `Carbon`, nunca blanco.** El amarillo con texto blanco
no pasa contraste (2.26:1) y se ve ilegible. Carbon sobre Mostaza alcanza **7.09:1**
(supera el estándar WCAG AAA de 7.0:1). Es la regla que más se rompe.

**Sobre terracota va texto blanco.** Blanco sobre Terracota alcanza **5.12:1**
(pasa WCAG AA de 4.5:1).

**Nunca terracota y mostaza pegados** en dos elementos grandes. La terracota es
acento: aparece en un botón, un chip activo, una insignia, no en bloques.

### Tabla de contrastes (WCAG 2.1)

Verificación realizada conforme al algoritmo de luminancia relativa WCAG 2.1:

| Combinación (Frente / Fondo) | Códigos Hex | Contraste | Nivel WCAG | Evaluación y regla de uso |
|---|---|---|---|---|
| **Carbon sobre Mostaza** | `#2B1F14` / `#E0A11B` | **7.09:1** | AAA (≥ 7.0:1) | **PASA**. Botón primario, barra superior, chips activos. Obligatorio. |
| **Blanco sobre Mostaza** | `#FFFFFF` / `#E0A11B` | **2.26:1** | Falla (< 3.0:1) | **FALLA**. Prohibido en cualquier texto o icono. |
| **Blanco sobre Terracota** | `#FFFFFF` / `#C1440E` | **5.12:1** | AA (≥ 4.5:1) | **PASA**. Botón de acción destacada (postularse, publicar). |
| **Carbon sobre Crema** | `#2B1F14` / `#FFFDF8` | **15.78:1** | AAA (≥ 7.0:1) | **PASA**. Texto principal de lectura en pantalla. |
| **Carbon sobre Arena** | `#2B1F14` / `#F7F0E4` | **14.16:1** | AAA (≥ 7.0:1) | **PASA**. Texto principal sobre tarjetas y superficies. |
| **Cafe sobre Crema** | `#7A6A58` / `#FFFDF8` | **5.13:1** | AA (≥ 4.5:1) | **PASA**. Texto secundario, ayudas y fechas sobre fondo. |
| **Cafe sobre Arena** | `#7A6A58` / `#F7F0E4` | **4.60:1** | AA (≥ 4.5:1) | **PASA**. Texto secundario sobre tarjetas. |
| **Mostaza Oscuro sobre Crema** | `#B37D08` / `#FFFDF8` | **3.53:1** | UI (≥ 3.0:1) | **PASA**. Bordes y contornos interactivos en fondo. |
| **Mostaza Oscuro sobre Arena** | `#B37D08` / `#F7F0E4` | **3.17:1** | UI (≥ 3.0:1) | **PASA**. Borde del campo enfocado sobre tarjeta. |
| **Blanco sobre Éxito** | `#FFFFFF` / `#2E7D32` | **5.13:1** | AA (≥ 4.5:1) | **PASA**. Insignia de estado 'abierta'. |
| **Carbon sobre Advertencia** | `#2B1F14` / `#ED6C02` | **5.15:1** | AA (≥ 4.5:1) | **PASA**. Insignia de estado 'asignada' con texto Carbon. |
| **Blanco sobre Advertencia** | `#FFFFFF` / `#ED6C02` | **3.11:1** | Grande (≥ 3.0:1) | Pasa solo texto grande; para lectura general usar Carbon. |
| **Blanco sobre Error** | `#FFFFFF` / `#C62828` | **5.62:1** | AA (≥ 4.5:1) | **PASA**. Insignia 'cancelada' y mensajes de error. |
| **Carbon sobre Borde** | `#2B1F14` / `#E6DAC6` | **11.62:1** | AAA (≥ 7.0:1) | **PASA**. Elementos gráficos y separadores. |

### A qué se aplica cada uno

| Color | Dónde | Contraste de texto aplicable |
|---|---|---|
| Mostaza | Botón principal, barra superior, chip activo, indicador de la barra inferior | 7.09:1 (con Carbon) |
| Mostaza oscuro | Estado presionado, borde del campo enfocado | 3.53:1 / 3.17:1 (borde UI) |
| Terracota | Botón de acción destacada (postularse, publicar solicitud), estrellas | 5.12:1 (con Blanco) |
| Carbon | Títulos y texto de lectura | 15.78:1 (en Crema) / 14.16:1 (en Arena) |
| Cafe | Etiquetas, ayudas, fechas, texto deshabilitado | 5.13:1 (en Crema) / 4.60:1 (en Arena) |
| Crema | Fondo de pantalla | Superficie base cálida |
| Arena | Tarjetas, campos, hojas inferiores | Superficie secundaria |
| Borde | Separadores y contorno de campos en reposo | Delimitador visual |

**Sin modo oscuro en el MVP.** Es DEC-15.
Muestra gráfica de la paleta: ver `docs/tecnico/recursos/muestra-paleta.png`.
Identidad y logotipo: ver `docs/tecnico/recursos/logo.svg`.

## Tipografía

Fuente del sistema, sin descargar nada.

| Estilo | Tamaño | Peso | Uso |
|---|---|---|---|
| `titulo` | 24 sp | SemiBold | Encabezado de pantalla |
| `subtitulo` | 20 sp | Medium | Sección |
| `cuerpoFuerte` | 16 sp | Medium | Título de tarjeta |
| `cuerpo` | 16 sp | Normal | Texto de lectura |
| `secundario` | 14 sp | Normal | Etiquetas y apoyos |
| `pie` | 12 sp | Normal | Fechas, contadores |

Nada por debajo de 12 sp. Un usuario en obra ve la pantalla con el sol encima.

## Espaciado

Escala de 4: `4, 8, 12, 16, 24, 32, 48`.

Margen lateral de pantalla: **16 dp**. Separación entre tarjetas: **12 dp**.
Relleno interno de tarjeta: **16 dp**.

## Formas

| Elemento | Radio |
|---|---|
| Botones y campos | 12 dp |
| Tarjetas | 16 dp |
| Chips | completamente redondeado |
| Hoja inferior | 20 dp arriba |
| Foto de perfil | círculo |

## Componentes base

Se construyen en el Sprint 1 y **nadie escribe un botón a mano después**. Si
falta un componente, se agrega aquí primero.

| Componente | Notas |
|---|---|
| `BotonPrincipal` | Mostaza, texto Carbon, ancho completo, 48 dp de alto |
| `BotonSecundario` | Contorno mostaza oscuro, fondo transparente |
| `BotonDestacado` | Terracota, texto blanco. Para postularse y publicar |
| `BotonTexto` | Sin fondo, texto mostaza oscuro |
| `CampoTexto` | Fondo Arena, borde Borde, borde mostaza oscuro al enfocar |
| `CampoContrasena` | Igual, con ojo para mostrar y ocultar |
| `TarjetaTrabajador` | Foto, nombre, título, estrellas, municipio |
| `TarjetaServicio` | Foto, título, categoría, rango de precio. Sin precio, el renglón no se pinta. Con `pausado = true` va al 60 % de opacidad y la categoría dice "Plomería · Pausado" (`H-21`, decidido por el líder el 2026-10-18 para P-12) |
| `TarjetaSolicitud` | Título, categoría, presupuesto, estado, fecha |
| `ChipCategoria` | Mostaza cuando está activo, Arena cuando no |
| `Estrellas` | Terracota, media estrella permitida en lectura |
| `TarjetaConversacion` | Foto, nombre, título de la solicitud, último mensaje, hora y punto de «sin leer» (P-15). Registrada por `S5-T05` (`H-49`) |
| `BurbujaMensaje` | Propios a la derecha en Mostaza, de la otra persona a la izquierda en Arena, con hora y estado de envío (P-16). Registrada por `S5-T05` (`H-49`) |
| `CampoMensaje` | Campo de hasta 4 líneas con botón de enviar apagado con el texto en blanco (P-16). Registrada por `S5-T05` (`H-49`) |
| `EtiquetaEstado` | Color según estado de la solicitud |
| `Cargando` | Indicador centrado, mostaza |
| `EstadoVacio` | Icono, texto y botón opcional |
| `EstadoError` | Mensaje y botón de reintentar |
| `BarraSuperior` | Mostaza, título Carbon, flecha de regreso |
| `BarraInferior` | 4 destinos, indicador mostaza |

### Colores de los estados de la solicitud

| Estado | Color |
|---|---|
| abierta | Éxito |
| asignada | Advertencia |
| cerrada | Cafe |
| cancelada | Error |

## Iconos

Material Icons, variante `Outlined`. Sin librerías externas y sin emoji en la
interfaz.

### Iconos de categorías de oficios

Las 16 categorías del sistema (`categorias` en base de datos) tienen asignado
su icono correspondiente de Material Icons Outlined:

| Orden | Categoría | Icono Material Outlined | Nombre Compose (`Icons.Outlined.*`) | Uso / Representación |
|---|---|---|---|---|
| 10 | Plomería | `plumbing` | `Plumbing` | Llave y tubería hidráulica |
| 20 | Electricidad | `bolt` | `Bolt` | Rayo de energía e instalaciones eléctricas |
| 30 | Albañilería | `construction` | `Construction` | Herramientas de obra y edificación |
| 40 | Carpintería | `carpenter` | `Carpenter` | Sierra y trabajos en madera |
| 50 | Pintura | `format_paint` | `FormatPaint` | Rodillo de pintura y acabados |
| 60 | Herrería | `hardware` | `Hardware` | Yunque y herramientas de metalistería |
| 70 | Limpieza | `cleaning_services` | `CleaningServices` | Botella rociadora y limpieza |
| 80 | Jardinería | `yard` | `Yard` | Jardín, poda y exteriores |
| 90 | Mudanzas y carga | `local_shipping` | `LocalShipping` | Camión de fletes y transporte de carga |
| 100 | Aire y refrigeración | `ac_unit` | `AcUnit` | Copo de nieve y climatización |
| 110 | Mecánica | `car_repair` | `CarRepair` | Automóvil y mantenimiento mecánico |
| 120 | Cómputo | `computer` | `Computer` | Computadora y soporte técnico |
| 130 | Cerrajería | `key` | `Key` | Llave y seguridad de acceso |
| 140 | Costura | `content_cut` | `ContentCut` | Tijeras y confección textil |
| 150 | Cocina y eventos | `restaurant` | `Restaurant` | Cubiertos, gastronomía y banquetes |
| 999 | Otros | `more_horiz` | `MoreHoriz` | Puntos suspensivos / oficios diversos |

Estos valores están sincronizados con la columna `categorias.icono` en
`basedatos/04_datos_semilla.sql`.

La primera columna es `categorias.orden`, el que decide cómo se listan, **no el
`id`**: los `id` son un `smallserial` del 1 al 16 y no se usan en la interfaz.

La traducción de `categorias.icono` al icono vive en una sola función,
`iconoCategoria(nombre)` de `ui/componentes/IconosCategoria.kt` (`S3-T02`). Un
nombre nulo o desconocido cae en `MoreHoriz`, el de "Otros". Si se agrega una
categoría, se agrega aquí, en la semilla y en esa función.

## Textos

Todo en español. Tuteo, sin usted. Frases cortas.

**Los mensajes de error dicen qué hacer**, no solo qué falló. "El correo ya
está registrado, inicia sesión" y no "Error 409".

Todas las cadenas van en `strings.xml`. Ninguna cadena de interfaz vive dentro
de un `@Composable`.

## Estados que toda pantalla con datos debe tener

Cargando, vacío, error y contenido. **Las cuatro.** Una pantalla que solo
maneja el caso feliz no pasa revisión de pull request.
