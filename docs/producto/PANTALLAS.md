# Pantallas del MVP

19 pantallas. No hay una vigésima. Si un ticket parece necesitar una pantalla
que no está aquí, detente y repórtalo.

Cada pantalla se identifica por su ID (`P-07`). Los tickets citan el ID en vez
de describir la pantalla otra vez.

---

## Autenticación

| ID | Pantalla | Qué hace |
|---|---|---|
| P-01 | Splash | Logo, verifica si hay sesión guardada y decide a dónde va. Sin onboarding detrás |
| P-02 | Iniciar sesión | Correo, contraseña, enlace a registro y a recuperación |
| P-03 | Registro | Nombre, apellidos, correo, contraseña, teléfono y **selección de rol** |
| P-04 | Recuperar contraseña | Se pide el correo, se avisa que se envió el enlace |

## Cliente

| ID | Pantalla | Qué hace |
|---|---|---|
| P-05 | Inicio cliente | Buscador, cuadrícula de categorías y trabajadores mejor calificados |
| P-06 | Resultados | Lista de trabajadores con filtros de categoría, estado, municipio, precio y calificación. Se ordena por calificación, precio o más recientes, y se carga por páginas |
| P-07 | Perfil público del trabajador | Datos, habilidades, **sus servicios**, **sus reseñas** y botón de contactar |
| P-08 | Publicar solicitud | Título, descripción, categoría, presupuesto y ubicación |
| P-09 | Mis solicitudes | Lista de las solicitudes del cliente con su estado |

## Trabajador

| ID | Pantalla | Qué hace |
|---|---|---|
| P-10 | Inicio trabajador | Solicitudes abiertas, filtrables por categoría |
| P-11 | Mi perfil | Ver y editar título, descripción, habilidades, experiencia, contacto y ubicación |
| P-12 | Mis servicios | Lista de servicios publicados con editar, pausar y eliminar |
| P-13 | Crear o editar servicio | Título, descripción, categoría, precio, hasta 3 fotos. Botón de redactar con IA |
| P-14 | Mis postulaciones | Lista de postulaciones enviadas con su estado |

## Compartidas

| ID | Pantalla | Qué hace |
|---|---|---|
| P-15 | Conversaciones | Bandeja de chats ordenada por mensaje más reciente |
| P-16 | Chat | Conversación de texto plano con un usuario |
| P-17 | Dejar reseña | Calificación de 1 a 5 estrellas y comentario. Solo tras cerrar una solicitud |
| P-18 | Mi cuenta | Datos personales, foto, cambiar contraseña, cerrar sesión. **Es también donde aterriza el enlace de recuperación de HU-04** (`DEC-27`) |
| P-19 | Detalle de solicitud | Datos de la solicitud, postulaciones recibidas y acciones según el rol |

---

## Navegación

**Antes de iniciar sesión:** P-01 → P-02 ↔ P-03 ↔ P-04

**El enlace del correo de recuperación entra por fuera de ese mapa.** Abre la
aplicación por *deep link*, canjea el token por sesión y deja al usuario en
P-18, ya dentro. No pasa por P-02. Ver `DEC-27`.

**Cliente, barra inferior de 4:** P-05 Inicio · P-09 Solicitudes · P-15 Chats ·
P-18 Cuenta. Botón flotante en P-05 que lleva a P-08.

**Trabajador, barra inferior de 4:** P-10 Inicio · P-12 Servicios ·
P-15 Chats · P-18 Cuenta. Acceso a P-11 y P-14 desde P-18 y P-12.

**Pantallas que se abren encima, sin barra inferior:** P-04, P-06, P-07, P-08,
P-13, P-16, P-17, P-19.

## Decisiones de recorte que están dentro de este mapa

**El detalle de servicio no es pantalla.** Los servicios se leen dentro del
perfil del trabajador (P-07). La búsqueda devuelve trabajadores, no servicios
sueltos.

**Las reseñas no tienen pantalla de listado.** Se leen dentro de P-07 y solo se
escriben en P-17.

**No hay onboarding.** P-01 lleva directo a P-02.

**El chat lo abre siempre el cliente** (DEC-20). El botón de contactar está en
P-07 y en P-19 del lado del cliente. El trabajador ve el hilo en P-15 y
responde en P-16, pero no tiene desde dónde iniciar uno.

---

## Verificación de cobertura

> Resultado del cruce de `S1-T02` (2026-09-15) entre las 33 historias de
> `docs/producto/HISTORIAS.md` y las 19 pantallas de este archivo. Es un
> control de consistencia: **no se agregó ni se quitó ninguna pantalla**, y lo
> que no cuadró se reporta abajo en vez de resolverse.
>
> El mapa se reconstruyó desde el encabezado **Pantallas:** de cada historia,
> una por una, no copiando la tabla de `S1-T01`. Ambas coinciden.

### Cobertura, pantalla por historia

| ID | Pantalla | Historias que la usan | N |
|---|---|---|---|
| P-01 | Splash | HU-03 | 1 |
| P-02 | Iniciar sesión | HU-01, HU-02, HU-04 | 3 |
| P-03 | Registro | HU-01 | 1 |
| P-04 | Recuperar contraseña | HU-04 | 1 |
| P-05 | Inicio cliente | HU-16, HU-17 | 2 |
| P-06 | Resultados | HU-16, HU-17, HU-18, HU-28, HU-33 \* | 5 |
| P-07 | Perfil público del trabajador | HU-07, HU-08, HU-19, HU-24, HU-28 | 5 |
| P-08 | Publicar solicitud | HU-12, HU-31, HU-32 \* | 3 |
| P-09 | Mis solicitudes | HU-13 | 1 |
| P-10 | Inicio trabajador | HU-20, HU-21 | 2 |
| P-11 | Mi perfil | HU-06, HU-08, HU-29, HU-31 | 4 |
| P-12 | Mis servicios | HU-09, HU-10 | 2 |
| P-13 | Crear o editar servicio | HU-09, HU-10, HU-11, HU-30, HU-31 | 5 |
| P-14 | Mis postulaciones | HU-22 | 1 |
| P-15 | Conversaciones | HU-25 | 1 |
| P-16 | Chat | HU-24, HU-26 | 2 |
| P-17 | Dejar reseña | HU-15, HU-27 | 2 |
| P-18 | Mi cuenta | HU-05, HU-07 | 2 |
| P-19 | Detalle de solicitud | HU-14, HU-15, HU-21, HU-23, HU-24 | 5 |

\* `HU-32` y `HU-33` están marcadas `OPCIONAL`. Ninguna de las dos pantallas
depende solo de ellas: P-06 se sostiene con HU-16, HU-17, HU-18 y HU-28, y
P-08 con HU-12 y HU-31. Si el equipo no alcanza a hacer las opcionales, no se
cae ninguna pantalla.

### Los tres cruces

**1 · Historias sin pantalla que las soporte: ninguna.** Las 33 declaran al
menos una pantalla y todas las que declaran existen en este archivo. No se
citó ningún ID de pantalla inexistente ni por encima de P-19.

**2 · Pantallas que ninguna historia usa: ninguna.** Los 19 renglones de la
tabla tienen al menos una historia. La menos cubierta es un grupo de seis
pantallas con una sola historia cada una (P-01, P-03, P-04, P-09, P-14, P-15);
en las seis es una historia obligatoria, no opcional.

**3 · Historias que exigen algo de "FUERA del MVP": ninguna.** Se revisaron
los 17 renglones de esa lista de `PRODUCTO.md` contra las 33 historias. Los
cuatro casos que se acercan al borde están del lado correcto, y conviene que
quede escrito para que nadie los vuelva a discutir:

| Cerca del borde | Por qué NO lo cruza |
|---|---|
| HU-11, fotos de trabajos anteriores | "Portafolio o galería" está fuera, pero `PRODUCTO.md` resuelve el caso con "3 fotos por servicio", que es justo el tope de HU-11 |
| HU-22 y HU-26, avisos de postulación y de mensaje | Dicen **notificación local** con todas sus letras. Las push están fuera |
| HU-18, filtro por estado y municipio | Es catálogo, no mapa ni GPS. "Mapas y geolocalización" está fuera y esto no lo es. Aun así abre el hallazgo H-01 |
| HU-08 y HU-19, reseñas y servicios dentro de P-07 | Se leen dentro del perfil. Ni el detalle de servicio ni el listado de reseñas piden pantalla propia |

### Hallazgos para el líder

Los cuatro salieron del cruce de `S1-T02` y **los cuatro los resolvió el líder
el 2026-09-15**. Quedan aquí con lo que se decidió, para que el que llegue a
`S4-T05` o a `S5-T06` no los vuelva a abrir.

**H-01 · El número de filtros de búsqueda no coincidía en cuatro documentos.**
HU-18 pedía filtrar por categoría, estado, municipio, precio y calificación
—cinco—, y HU-17 lo reforzaba al sugerir "revisar la ubicación" en su estado
vacío, pero `PRODUCTO.md`, el renglón de P-06 y el título de `S4-T05` decían
tres. **Resuelto: se amplía el alcance a los cinco.** El dato ya existe sin
costo extra, porque HU-06 obliga al trabajador a capturar estado y municipio, y
filtrar por catálogo no es "mapas y geolocalización", que es lo que quedó
fuera. Se corrigieron `PRODUCTO.md`, el renglón de P-06 y el título de `S4-T05`
en `INDICE.md`; HU-18 y HU-17 se quedaron como estaban. Ojo al implementar:
`PEND-02` se cerró el 2026-09-22 (`DEC-26`) y los 26 municipios sembrados son
los definitivos, así que el desplegable de municipio se llena con esos.

**H-02 · P-06 no mencionaba el ordenamiento ni la paginación que HU-18 exige.**
No era alcance nuevo —`S4-T07` ya los cubre como tarea—, era la descripción de
la pantalla que había quedado corta. **Resuelto: se actualizó el renglón de
P-06**, que ahora dice que se ordena por calificación, precio o más recientes y
que se carga por páginas.

**H-03 · "Notificaciones locales" está DENTRO del MVP sin historia propia.**
Vive como criterio suelto dentro de HU-22 (postulación aceptada) y HU-26
(mensaje con la aplicación cerrada). **Resuelto: se acepta así, no se escribe
una HU-34.** No rompe la cobertura porque las notificaciones no son pantalla.
Lo que falta —qué pasa al tocar la notificación— lo define el ticket de
`S5-T10` cuando se redacte el Sprint 5.

**H-04 · HU-24 ataba el hilo de chat a un trabajo que desde P-07 no existe.**
Resultó no ser una decisión de diseño: `basedatos/01_esquema.sql` ya la había
tomado. `conversaciones.solicitud_id` es nulable y el índice único
`uq_conversacion_unica` va sobre `(cliente_id, trabajador_id,
coalesce(solicitud_id, uuid-cero))`, y `fn_abrir_conversacion` recibe
`p_solicitud_id` con valor por omisión nulo y devuelve el hilo existente si ya
lo hay. Es decir: un hilo por cliente,
trabajador y solicitud, más un hilo suelto para el contacto que nace en P-07.
**Resuelto: se escribió esa regla en HU-24**, que ahora tiene un criterio para
cada caso, los dos marcados como regla de la base. El esquema no se tocó.
