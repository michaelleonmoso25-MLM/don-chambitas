# Modelo entidad relación

El modelo vive en `basedatos/`. **Ese SQL es la fuente de verdad.** Este
documento explica el porqué; si los dos se contradicen, gana el SQL.

Motor: PostgreSQL 15 o superior, sobre Supabase (DEC-16).

El diagrama completo está en `docs/tecnico/diagrama-er.png`: las 15 tablas de
`public` más `auth.users`, con sus llaves primarias, sus llaves foráneas y la
cardinalidad de cada relación. Lo dibuja `docs/tecnico/diagrama-er.py`; si el
esquema cambia, se corre otra vez en vez de retocar la imagen a mano.

| Archivo | Qué deja |
|---|---|
| `01_esquema.sql` | Tablas, tipos, triggers, vistas y funciones RPC |
| `02_politicas_rls.sql` | Permisos, políticas RLS y tiempo real |
| `03_almacenamiento.sql` | Cubetas de Storage y sus políticas |
| `04_datos_semilla.sql` | 16 categorías, 32 estados, 26 municipios |

Se corren en ese orden. El detalle operativo está en `basedatos/README.md`.

## Las tablas

| Tabla | Para qué |
|---|---|
| `estados` | Catálogo de las 32 entidades |
| `municipios` | Catálogo de municipios por estado |
| `categorias` | Los 16 oficios |
| `usuarios` | Perfil público. Su `id` **es** el de `auth.users` |
| `perfiles_trabajador` | Datos profesionales, uno a uno con usuario |
| `perfil_habilidades` | Habilidades del trabajador |
| `servicios` | Lo que el trabajador ofrece |
| `servicio_fotos` | Hasta 3 fotos por servicio |
| `solicitudes` | Lo que el cliente necesita |
| `postulaciones` | El trabajador se ofrece a una solicitud |
| `conversaciones` | Hilo de chat entre un cliente y un trabajador |
| `mensajes` | Los mensajes del hilo |
| `resenas` | Calificación de 1 a 5 más comentario |
| `ia_consumo` | Tope de llamadas por usuario y día |
| `ia_cache` | Respuestas de IA ya pagadas |

**No hay tabla de contraseñas ni de recuperación.** Las dos existían cuando el
backend podía ser propio. Con Supabase Auth, la credencial y el correo de
recuperación (P-04) los lleva Supabase.

## Relaciones

```
auth.users 1 ──── 1 usuarios
                      │
                      ├── 0..1 perfiles_trabajador
                      │          ├── N perfil_habilidades
                      │          ├── N servicios ──── N servicio_fotos
                      │          ├── N postulaciones
                      │          └── N resenas
                      │
                      ├── N solicitudes (como cliente) ──── N postulaciones
                      │                       └── 0..1 resenas
                      └── N conversaciones ──── N mensajes

categorias 1 ──── N servicios, N solicitudes
estados    1 ──── N municipios
```

## Decisiones que conviene entender

**La identidad la lleva Supabase Auth.** `usuarios.id` es el mismo uuid que
`auth.users.id` y que devuelve `auth.uid()`. La aplicación **nunca** consulta
`auth.users`. La fila de `usuarios` la crea sola el trigger
`tg_auth_usuario_creado` al registrarse, leyendo nombre, apellidos, teléfono y
rol del metadata (`options.data` de `signUpWith`).

Consecuencia práctica: **los usuarios no se pueden sembrar con un `INSERT`**.
Para datos de prueba, hay que registrar cuentas desde la aplicación o desde la
consola de Supabase.

**`perfiles_trabajador` usa `usuario_id` como llave primaria.** La relación es
estrictamente uno a uno, y todo lo que apunta a un trabajador apunta a su
perfil. Así es imposible que exista un servicio de alguien sin perfil.

**Los roles son excluyentes y la base lo sostiene.** `perfiles_trabajador` y
`solicitudes` llevan una columna `rol` fija por `CHECK` y una llave foránea
compuesta contra `usuarios(id, rol)`. Un cliente no puede tener perfil de
trabajador ni aunque la aplicación se equivoque, y el rol de un usuario no se
puede cambiar mientras tenga perfil o solicitudes. Eso es `DEC-22`: en el MVP no
se cambia de rol.

**`estatus` no es `estado_id`.** En `solicitudes` y `postulaciones`, `estatus`
es el estado del flujo (`abierta`, `asignada`, `cerrada`, `cancelada`) y
`estado_id` es la entidad federativa. Antes las dos columnas se llamaban
`estado` y era una trampa esperando a alguien.

**Cinco reglas viven en triggers, no en la aplicación.** La aplicación las va a
olvidar tarde o temprano:

1. Nadie se postula a su propia solicitud, ni a una que no esté abierta.
2. Solo se reseña una solicitud cerrada, solo el cliente que la publicó, y solo
   sobre el trabajador que quedó asignado.
3. Nadie escribe en una conversación de la que no es parte.
4. Cerrar una solicitud exige trabajador asignado, y sella `cerrada_en` sola.
5. Al borrar un trabajador, sus solicitudes `asignada` vuelven a `abierta` y las
   `cerrada` conservan el historial sin trabajador.

**Las cinco funciones son `security definer` con `search_path` fijo.** No es
adorno ni exceso de celo: sin eso corren con los permisos de quien dispara el
trigger, RLS les esconde las filas de los demás, el `SELECT` devuelve `NULL`,
el `IF` se evalúa a `NULL` y la regla **no se aplica, en silencio y sin error**.
Un `UPDATE` en esa situación afecta cero filas y tampoco avisa: así es como
`ultimo_mensaje_en` se quedaba en `NULL` para siempre y la bandeja de P-15 se
quedaba sin criterio de orden.

Ojo al probarlo: el grueso de `91_prueba_funcional.sql` corre como `postgres`,
que se salta RLS, así que estas fallas **ahí no se ven**. Las que las ven son
las pruebas 10, 12, 13 y 21 a 24, que hacen `set role authenticated`, y las
comprobaciones 37 a 42 de `90_verificacion.sql`. Quien agregue un trigger que
lea o escriba una fila ajena tiene que repetir el `security definer`, y quien
agregue una prueba de esa regla tiene que correrla con el rol puesto.

La quinta merece explicación. `solicitudes.trabajador_id` es `ON DELETE SET
NULL`, y el `CHECK` que exige trabajador aplica **solo a `asignada`**, no a
`cerrada`. Si aplicara a las dos, borrar la cuenta de un trabajador que alguna
vez cerró un trabajo violaría el `CHECK` y abortaría la transacción: el usuario
quedaría atrapado sin poder darse de baja. Que no se pueda *cerrar* sin
trabajador lo impone `fn_validar_transicion_solicitud`, sobre la transición y no
sobre la fila.

**Búsqueda con `pg_trgm`.** Índices GIN sobre los títulos de perfiles,
servicios y solicitudes. Eso hace que un `ilike '%plom%'` no se arrastre.
**No** hace que "plomeria" encuentre "plomería": `pg_trgm` acelera el `ilike`,
no cambia lo que el `ilike` considera igual, y `unaccent` no está instalado.
Es el hueco `H-05`, al final de este documento.

En Supabase la extensión vive en el esquema `extensions`, por eso los índices
califican el operador: `extensions.gin_trgm_ops`.

**Máximo 3 fotos por servicio** por restricción de posición entre 1 y 3 más un
único por servicio y posición. No es una regla de la aplicación, es de la base.

**La caché de IA se indexa por función y hash.** La llave primaria es
`(funcion, entrada_hash)`. Con el hash solo, el mismo texto pedido a
`redactar_perfil` y a `categorizar` se pisaba y devolvía la respuesta
equivocada.

**El tope de IA son 10 llamadas al día en total** (DEC-18), no 10 por función.
La llave primaria de `ia_consumo` conserva la desagregación por función para
poder medir, pero `fn_ia_registrar_llamada` suma las cuatro.

## Vistas

Las dos llevan `security_invoker = on`: se ejecutan con los permisos de quien
consulta. Sin eso, una vista se salta las políticas RLS de las tablas que tiene
debajo y se convierte en una fuga.

`vw_trabajador_calificacion` devuelve promedio y total de reseñas por
trabajador.

`vw_busqueda_trabajadores` devuelve la tarjeta de resultados de P-06 ya armada:
nombre, foto, título, ubicación, promedio, número de servicios activos y precio
mínimo. Los filtros de P-06 se aplican encima de esta vista desde postgrest.

Lleva además `categorias`, un arreglo con las categorías de sus servicios
activos, y `creado_en`. Sin esos dos la vista no podía sostener lo que promete
`CONTRATOS-API.md`: **el filtro por categoría es el principal de P-06 y de la
cuadrícula de P-05**, y no había con qué. Es arreglo y no columna suelta porque
un trabajador ofrece varios oficios; se filtra con `categorias=cs.{3}`.

## Funciones RPC

Lo que **no** se resuelve con una consulta suelta, porque son varias escrituras
que tienen que pasar juntas o porque serían cinco viajes de red:

| Función | Por qué existe |
|---|---|
| `fn_perfil_publico_trabajador(uuid)` | P-07 entera en un viaje |
| `fn_aceptar_postulacion(uuid)` | Acepta una, rechaza las demás y asigna la solicitud, o no hace nada |
| `fn_cerrar_solicitud(uuid)` | Valida y sella el cierre, habilitando la reseña |
| `fn_abrir_conversacion(uuid, uuid)` | Upsert con unicidad sobre un `COALESCE` |
| `fn_ia_registrar_llamada(...)` | Tope diario. Solo la llama la Edge Function |

## Seguridad

**`02_politicas_rls.sql` no es opcional.** La aplicación lleva la `anon key`,
que es pública por diseño: cualquiera la saca del APK. Lo único que impide leer
la base entera con esa llave son las políticas RLS. Sin ese archivo, el proyecto
está abierto.

Lo que las políticas garantizan, en corto:

- Si eres **cliente**, tu correo y tu teléfono solo los ves tú y la contraparte
  con la que ya estás tratando.
- Un trabajador **no ve** las postulaciones de sus competidores.
- Nadie lee una conversación de la que no es parte.
- `ia_consumo` e `ia_cache` tienen RLS activo y **cero políticas**: con la
  `anon key` ahí no entra nadie. Las escribe solo la Edge Function con la
  `service_role` key.

**RLS no basta para cerrar una función.** PostgreSQL le da `EXECUTE` a `PUBLIC`
en cuanto se crea una, y postgrest publica como RPC toda función del esquema
`public` que el rol de quien llama pueda ejecutar. Una función `security
definer` se salta las políticas por definición, así que `fn_ia_registrar_llamada`
quedaba llamable con la `anon key`: cualquiera podía escribir en `ia_consumo` y
quemarle el tope diario a otro usuario con solo conocer su uuid. Por eso
`02_politicas_rls.sql` termina revocando `EXECUTE` función por función. Quien
agregue una RPC nueva tiene que decidir quién puede ejecutarla; no hacerlo la
deja abierta.

**RLS tampoco distingue columnas**, solo filas. `usuarios` y `mensajes` llevan
permisos por columna para que nadie se cambie el correo o el rol, ni reescriba
el contenido de un mensaje que recibió.

Las reseñas no se editan ni se borran. Si hubiera que moderar alguna, se hace
desde la consola (DEC-13).

### Lo que las políticas NO impiden, a propósito

Tres cosas que el modelo deja abiertas con decisión tomada. Están aquí para que
nadie las descubra en una revisión y crea que encontró un hueco.

**La ficha de un trabajador se ve entera, correo y teléfono incluidos, para
cualquier usuario con sesión** (DEC-19). La política `usuario ve la ficha de
cualquier trabajador` abre la fila completa, y tiene que hacerlo:
`vw_busqueda_trabajadores` es `security_invoker` y necesita el `join` contra
`usuarios`. El trabajador publica su contacto a propósito, que es a lo que vino
a la aplicación. Lo que sigue cerrado es la ficha del **cliente**. Esto hay que
declararlo en el formulario de seguridad de los datos de Play Store (S6-T08).

**El trabajador no puede abrir una conversación** (DEC-20). `fn_abrir_conversacion`
fija `cliente_id = auth.uid()`, y `conversaciones.trabajador_id` apunta a
`perfiles_trabajador`: si la llama un trabajador, la llave foránea la rechaza.
El hilo nace en P-07 o al aceptarle la postulación, y de ahí el trabajador
responde. No es una limitación accidental: evita el mensaje en frío.

**Que aceptar una postulación sea atómico es regla del contrato, no de la
base** (DEC-21). El cliente puede llevar su propia solicitud a `asignada` con un
`update` suelto y poner al trabajador que quiera, sin postulación de por medio:
es su fila, la política se lo permite y el `CHECK` solo exige que
`trabajador_id` no sea nulo. Cerrarlo pedía un trigger de transición de estado
que hoy no cabe. El único que puede hacerlo es el dueño de la solicitud, así que
el daño se lo hace a sí mismo. La aplicación usa `fn_aceptar_postulacion`
siempre; un `update` suelto a `estatus` es un bug de quien lo escriba.

### La trampa de la recursión

Una política **no puede consultar directamente una tabla cuya política
consulte de vuelta la primera.** Leer cualquiera de las dos dispara la política
de la otra, que dispara la de la primera, y PostgreSQL aborta con
`infinite recursion detected in policy`.

Pasó exactamente entre `solicitudes` y `postulaciones`: una preguntaba «¿me
postulé a esta?» y la otra «¿es mi solicitud?». Se detectó el **2026-09-16 en
UTC** al correr `91_prueba_funcional.sql` contra el proyecto real, la primera
vez que se levantó el esquema. El reloj de Supabase va en UTC, así que las
fechas que salen de la base pueden ir un día por delante de la local; no es un
error de quien lo anotó.

La salida es meter la consulta dentro de una función `security definer`, que
corre como el dueño de la tabla y no evalúa políticas. De ahí salen
`fn_es_mi_solicitud` y `fn_me_postule_a`. **No es un atajo: es la única forma
de expresarlo.**

Quien agregue una política nueva que mire otra tabla tiene que comprobar que
no cierra un ciclo. Las pruebas 21 a 24 de `91_prueba_funcional.sql` lo
vigilan: hacen 30 lecturas cruzadas con tres usuarios distintos.

## Cobertura de las historias

Cruce de las 33 historias de `S1-T01` contra las tablas, una por una. Es el
paso 3 de `S1-T03`. Lo que aquí dice "sin tabla" no es un hueco: es trabajo que
resuelve Supabase Auth o la aplicación, y está anotado como tal.

**Resultado: 30 historias sostenidas sin reserva, 3 con hueco** (`H-05`, `H-06`
y `H-07`, abajo). Ninguna historia pide una tabla que no exista.

### 1 · Autenticación

| HU | Lo que exige | Qué lo sostiene |
|---|---|---|
| HU-01 | Registro con rol, correo único, rol definitivo | `auth.users` + `tg_auth_usuario_creado` → `usuarios`; `usuarios.correo unique`; `uq_usuario_rol(id, rol)` y las dos llaves foráneas compuestas |
| HU-02 | Iniciar sesión y entrar según el rol | Supabase Auth; `usuarios.rol` decide el destino. Sin tabla propia, a propósito |
| HU-03 | Sesión recordada y expiración | Supabase Auth. Sin tabla |
| HU-04 | Enlace de recuperación, mismo aviso exista o no el correo | Supabase Auth. Sin tabla, y por eso la fuga que la historia teme es imposible desde la base |
| HU-05 | Editar nombre, apellidos y teléfono; nunca correo ni rol | `grant update (nombre, apellidos, telefono, foto_url)` sobre `usuarios` tras el `revoke update` completo. El correo y el rol no están en el grant: no hay forma de tocarlos con la `anon key` |

### 2 · Perfil del trabajador

| HU | Lo que exige | Qué lo sostiene |
|---|---|---|
| HU-06 | Título, descripción, habilidades, experiencia, teléfono, estado y municipio; municipios filtrados por estado; marcarse no disponible | `perfiles_trabajador` (`titulo`, `descripcion`, `experiencia_anios`, `telefono_contacto`, `estado_id`, `municipio_id`, `disponible`) y `perfil_habilidades`; `municipios.estado_id` para la cascada; `vw_busqueda_trabajadores` expone `disponible`, así que la tarjeta puede decirlo |
| HU-07 | Foto de perfil; rechazar más de 5 MB o formato distinto de JPG, PNG y WebP | `usuarios.foto_url` y la cubeta `perfiles` con `file_size_limit = 5242880` y `allowed_mime_types` en `03_almacenamiento.sql`. Borrar el archivo viejo es `H-06` |
| HU-08 | Vista previa del perfil público con estados vacíos | `fn_perfil_publico_trabajador(uuid)` devuelve perfil, habilidades, servicios con fotos, calificación y reseñas en un viaje; los arreglos vacíos son los que la pantalla pinta como vacío |

### 3 · Servicios

| HU | Lo que exige | Qué lo sostiene |
|---|---|---|
| HU-09 | Título, descripción, categoría y precio; las 16 categorías y ninguna más; rechazar `desde > hasta`; precio opcional | `servicios` con `ck_servicio_rango_precio` y `ck_servicio_precios_positivos`; `categorias.activa`; `precio_desde` y `precio_hasta` son nulables |
| HU-10 | Pausar sin perder, eliminar con sus fotos | `servicios.activo`; `servicio_fotos.servicio_id` es `on delete cascade` |
| HU-11 | Hasta 3 fotos, borrar la foto y su archivo | `ck_foto_posicion` (1 a 3) más `uq_foto_posicion_unica (servicio_id, posicion)`. El archivo de Storage es `H-06` |

### 4 · Solicitudes

| HU | Lo que exige | Qué lo sostiene |
|---|---|---|
| HU-12 | Publicar con presupuesto opcional; un trabajador no publica solicitudes | `solicitudes`; `presupuesto` nulable; `fk_solicitud_cliente` contra `usuarios(id, rol)` con `ck_solicitud_rol_cliente` |
| HU-13 | Ver cada solicitud con su estado y cuántas postulaciones sin revisar | `solicitudes.estatus`; el conteo sale de `postulaciones` gracias a `fn_es_mi_solicitud`. "Sin revisar" es `H-07` |
| HU-14 | Detalle, cancelar, y que el trabajador asignado vea el trabajo | `estatus = 'cancelada'` corta las postulaciones nuevas por `fn_validar_postulacion`; la política `trabajador ve las solicitudes en las que participa` le abre la fila al asignado |
| HU-15 | Cerrar solo si hay trabajador; guardar la fecha | `fn_cerrar_solicitud` y `fn_validar_transicion_solicitud`, que sella `cerrada_en` y rechaza el cierre sin trabajador |

### 5 · Búsqueda

| HU | Lo que exige | Qué lo sostiene |
|---|---|---|
| HU-16 | Las 16 categorías y los mejor calificados; al tocar una, solo quien ofrece servicios activos de ella | `categorias`; `vw_busqueda_trabajadores.categorias` es un arreglo agregado **solo con los servicios activos**, y lleva `promedio` para ordenar |
| HU-17 | Que "plomeria" encuentre "plomería" | **Hueco `H-05`.** Los índices GIN de `pg_trgm` están, pero el contrato busca con `ilike` y `unaccent` no está instalado |
| HU-18 | Filtrar por categoría, estado, municipio, precio y calificación; ordenar; paginar | `vw_busqueda_trabajadores` expone `categorias`, `estado_id`, `municipio_id`, `precio_desde`, `promedio`, `total_resenas` y `creado_en`: los cinco filtros de `DEC-23` y los tres órdenes salen de ahí, y la paginación es `limit`/`offset` de postgrest |
| HU-19 | Perfil completo en una pantalla y botón de contacto | `fn_perfil_publico_trabajador` y `fn_abrir_conversacion` |

### 6 · Postulaciones

| HU | Lo que exige | Qué lo sostiene |
|---|---|---|
| HU-20 | Solicitudes abiertas, más recientes primero, filtrables por categoría | Política `trabajador ve las solicitudes abiertas`; `creado_en` y `categoria_id` |
| HU-21 | Una sola postulación por solicitud; nada si no está abierta | `uq_una_postulacion_por_solicitud` y `fn_validar_postulacion` |
| HU-22 | Los cuatro estados, retirar, y no ver a la competencia | `postulaciones.estatus` (`enviada`, `aceptada`, `rechazada`, `retirada`); política `trabajador retira su postulacion`; `trabajador ve sus postulaciones` filtra por `trabajador_id = auth.uid()` |
| HU-23 | Comparar con nombre y calificación; aceptar una y rechazar el resto, todo junto | `fn_aceptar_postulacion` hace las tres escrituras en una transacción; `DEC-19` abre la ficha del trabajador; `vw_trabajador_calificacion` da el promedio; la política `cliente rechaza postulaciones a sus solicitudes` cubre el rechazo suelto |

### 7 · Chat

| HU | Lo que exige | Qué lo sostiene |
|---|---|---|
| HU-24 | Un hilo por cliente-trabajador-solicitud y uno suelto por pareja; el trabajador no lo abre | El índice único sobre `(cliente_id, trabajador_id, coalesce(solicitud_id, uuid cero))` es exactamente `DEC-24`; `fn_abrir_conversacion` fija `cliente_id = auth.uid()`, y `conversaciones.trabajador_id` apunta a `perfiles_trabajador`, así que a un trabajador lo rechaza la llave foránea (`DEC-20`) |
| HU-25 | Hilos ordenados por el más reciente; distinguir los no leídos | `conversaciones.ultimo_mensaje_en`, que mantiene `fn_tocar_conversacion`, con índice por `(cliente_id, ultimo_mensaje_en desc)` y su gemelo para el trabajador; `mensajes.leido_en` |
| HU-26 | Mensaje que llega sin actualizar; marcar leídos; solo texto | `mensajes` está en la publicación `supabase_realtime`; la política `marcar leidos los mensajes recibidos` más el `grant update (leido_en)`; no hay columna de adjunto, así que `DEC-04` lo sostiene la tabla |

### 8 · Reseñas

| HU | Lo que exige | Qué lo sostiene |
|---|---|---|
| HU-27 | De 1 a 5; solo si está cerrada; una por solicitud y definitiva | `ck_resena_calificacion`, `fn_validar_resena`, `resenas.solicitud_id` es `unique`, y `resenas` no tiene políticas de `update` ni de `delete` |
| HU-28 | Promedio recalculado y visible en perfil y resultados | `vw_trabajador_calificacion`, que `vw_busqueda_trabajadores` ya trae unida; `total_resenas` para el desempate |

### 9 · Inteligencia artificial

| HU | Lo que exige | Qué lo sostiene |
|---|---|---|
| HU-29 | Redactar el perfil | `funcion_ia = 'redactar_perfil'` en `ia_consumo` e `ia_cache` |
| HU-30 | Redactar el servicio | `funcion_ia = 'redactar_servicio'` |
| HU-31 | Tope de 10 al día en total; caché que no consume uso | `fn_ia_registrar_llamada` suma las cuatro funciones y devuelve `-1` en la llamada 11 (`DEC-18`); `ia_cache(funcion, entrada_hash)` se consulta antes de registrar nada |
| HU-32 | `OPCIONAL` Categoría sugerida | `funcion_ia = 'categorizar'` y `solicitudes.categorizada_por_ia` |
| HU-33 | `OPCIONAL` Trabajadores sugeridos | `funcion_ia = 'sugerir'`; el respaldo por calificación sale de `vw_busqueda_trabajadores` |

---

## Huecos detectados

Los tres salen del cruce de arriba. **Ninguno se corrigió**: el esquema no se
rediseña en `S1-T03` y las tres decisiones son del líder (`AGENTS.md` §5 y §9).
La numeración sigue la de los hallazgos de `S1-T02`, que llegaron hasta `H-04`.

### H-05 · La búsqueda por texto no ignora los acentos

**CERRADO el 2026-11-09 por `DEC-36`:** columna `texto_busqueda` normalizada con `unaccent`; lo aplica `S4-T09`. El hallazgo, como se reportó:

**Qué dice la historia.** HU-17, primer criterio: *"Dado que escribo 'plomeria'
sin acento, cuando busco, entonces los resultados incluyen a quienes escribieron
'plomería' con acento."*

**Qué hay.** `01_esquema.sql` instala `pg_trgm` y crea tres índices GIN sobre
los títulos. `CONTRATOS-API.md` define el filtro de `buscarTrabajadores` como
*"texto libre contra `titulo` (`ilike`)"*.

**Por qué no se cumple.** `pg_trgm` hace que un `ilike '%plom%'` use índice en
vez de recorrer la tabla; **no cambia qué considera igual el `ilike`**, que
compara carácter por carácter. `'plomería' ilike '%plomeria%'` es falso, con
índice y sin él. La extensión `unaccent`, que es la que normaliza los acentos,
no está instalada: `01_esquema.sql` solo crea `pg_trgm`.

Con el operador de similitud sí funcionaría —`titulo % 'plomeria'` da 0.5 y el
umbral de `pg_trgm` es 0.3—, pero ese operador no es el que pide el contrato y
postgrest no lo expone como filtro.

**A quién le llega.** A `S4-T04` (buscador por texto) y a `S4-T09`
(implementación real). Conviene cerrarlo antes de `S4-T04`, porque decide si
hace falta columna normalizada, índice nuevo o una función RPC de búsqueda.

**Las salidas, para que el líder elija:** instalar `unaccent` y colgar el índice
de `unaccent(titulo)`; o guardar una columna generada ya normalizada; o cambiar
el contrato al operador de similitud con una RPC. Las tres tocan `01_esquema.sql`
y la primera y la tercera tocan además `CONTRATOS-API.md`.

### H-06 · Nada borra el archivo de Storage cuando desaparece su fila

> **CERRADO el 2026-10-19 por el líder, en `S3-T08`: la fila primero y el
> archivo después.** La aplicación **lee las URL antes** de borrar, así que la
> cascada ya no se lleva la única copia. Así, ninguna fila apunta a un archivo
> que ya no existe. Si falla el borrado del archivo, la operación es un éxito
> igual y el archivo queda sin fila, sin que nadie lo vea. Eso no cumple al pie
> de la letra el "no quedan archivos huérfanos" de HU-11: se eligió a
> sabiendas, porque la alternativa deja imágenes rotas a la vista. Es el mismo
> criterio que `subirFotoPerfil` (`S2-T14`). El orden de cada operación está
> en `CONTRATOS-API.md`, en "Servicios". El hallazgo, como se reportó, sigue
> abajo; su última frase del párrafo del orden ya no aplica.

**Qué dicen las historias.** HU-11: *"cuando confirmo, entonces se borra el
archivo además del registro: no quedan archivos huérfanos."* Y HU-07: la foto
nueva *"reemplaza a la anterior."*

**Qué hay.** `servicio_fotos.servicio_id` es `on delete cascade`, y
`usuarios.foto_url` se va con la cuenta. Las filas se limpian solas. **Los
archivos de las cubetas `perfiles` y `servicios` no**: ninguna política, ningún
trigger y ninguna función los toca.

**Por qué importa el orden.** Borrar el servicio primero arrastra las filas de
`servicio_fotos` por cascada, y con ellas la única copia de la URL. A partir de
ahí el archivo queda en la cubeta para siempre y ya nadie sabe que existía. La
aplicación tiene que borrar el objeto de Storage **antes** de borrar la fila, y
eso hoy no está escrito en ningún lado.

**A quién le llega.** A `S3-T08` y `S2-T12`, los contratos que deben decirlo, y
a `S2-T14` y `S3-T09`, que lo implementan. No bloquea a nadie hoy.

### H-08 · Uno de los cinco triggers del paso 2 no se puede probar

**CERRADO el 2026-11-09 por `DEC-42`:** el quinto trigger lo sostienen los roles excluyentes de `DEC-22`, y `S1-T03` se queda cerrada. El hallazgo, como se reportó:

**Qué pide el ticket.** El paso 2 de `S1-T03`: probar a mano los cinco
triggers, entre ellos *"postularte a tu propia solicitud"*, y que los cinco
fallen.

**Qué cubre `91`.** Tres de los cinco: postularse a una solicitud no abierta
(prueba 10), reseñar una sin cerrar (14) y escribir en una conversación ajena
(13). El cuarto —cerrar una solicitud sin trabajador asignado— se probó a mano
el 2026-09-16 y **quedó demostrado por las dos vías**, que son capas distintas
y las dos importan: `fn_cerrar_solicitud` responde *"Solo se puede cerrar una
solicitud asignada"*, que es la que usa la aplicación, y un `update` suelto a
`estatus` choca contra `fn_validar_transicion_solicitud` con *"No se puede
cerrar una solicitud sin trabajador asignado"*, que es la que aguanta si
alguien se salta la RPC. `91` solo recorría el camino feliz, en la prueba 15.

**El quinto no se puede probar, porque no puede ocurrir.** Para postularse a
la propia solicitud haría falta un uuid que fuera cliente y trabajador a la
vez, y el esquema lo impide por tres lados: `solicitudes.cliente_id` va con
`rol_cliente = 'cliente'` contra `usuarios(id, rol)`,
`postulaciones.trabajador_id` apunta a `perfiles_trabajador`, cuyo `rol` está
fijo en `'trabajador'` por `CHECK` y también amarrado a `usuarios(id, rol)`, y
`usuarios` tiene **una sola** columna `rol`. La comparación
`v_cliente = new.trabajador_id` dentro de `fn_validar_postulacion` es código
defensivo que nunca se va a ejecutar.

No es un defecto: un trigger que sobrevive a que le quiten la llave foránea de
debajo está bien puesto. Pero el ticket pide demostrarlo y no se puede, así que
o el criterio se ajusta a cuatro triggers, o se anota que el quinto lo sostiene
el esquema y no el trigger.

**A quién le llega.** Al líder, para cerrar el criterio de aceptación de
`S1-T03`. No afecta a ninguna tarea posterior.

### H-07 · "Postulaciones sin revisar" no existe como dato

**CERRADO el 2026-11-09 por `DEC-37`:** vista `vw_mis_solicitudes` con `postulaciones_sin_revisar`; lo aplica `S4-T16`. El hallazgo, como se reportó:

**Qué dice la historia.** HU-13, segundo criterio: *"Dado que una solicitud
tiene postulaciones sin revisar, cuando la veo en la lista, entonces se
distingue cuántas hay."*

**Qué hay.** `postulaciones.estatus` con `enviada`, `aceptada`, `rechazada` y
`retirada`. No hay marca de vista ni de leída.

**Por qué no es lo mismo.** `enviada` significa "todavía no la acepté ni la
rechacé", no "todavía no la vi". Un cliente que abrió P-19, leyó las cinco
postulaciones y salió sin decidir sigue viendo las cinco como pendientes.

**Lo más probable es que no sea un hueco del esquema sino de la redacción**: si
"sin revisar" quiere decir `enviada`, la historia se sostiene tal cual y basta
con decirlo. Si quiere decir "sin ver", hace falta una columna. Es del líder
decidir cuál de las dos, y es la más barata de las tres.

**A quién le llega.** A `S5-T03` (bandeja de postulaciones) y a `S4-T06` (panel
del cliente).

## Lo que queda pendiente

**Catálogo de municipios: cerrado en 26.** `PEND-02` se resolvió el 2026-09-22
(`DEC-26`): los 26 municipios de zonas de prueba son los del MVP. Los 2,469 del
país no entran. Si algún día hacen falta, se cargan del INEGI con un `COPY`
desde CSV, sin tocar el esquema.

## Estado

**El esquema está levantado y verificado.** El 2026-09-15 se reinstaló desde
cero —`00` dejó el proyecto vacío, incluidas las cubetas— y corrieron `01` a
`04` sin un solo error. Las dos verificaciones pasaron completas:
`90_verificacion.sql` **42 de 42**, `91_prueba_funcional.sql` **25 de 25**.

Eso cubre la estructura y el comportamiento: los cinco triggers rechazan lo que
tienen que rechazar, aceptar una postulación asigna y rechaza a las demás,
cerrar sella `cerrada_en`, el tope de IA corta en la llamada 11, la baja de un
trabajador con historial deja la solicitud cerrada con `trabajador_id` nulo, y
las 30 lecturas cruzadas de las pruebas 21 a 24 no vuelven a entrar en
recursión.

**Lo que `S1-T03` cerró el 2026-09-16:**

- **Las pruebas 10, 12 y 13 de `91` ya corren con `set role authenticated`.**
  Antes corrían como `postgres`, que se salta RLS, y por eso no demostraban
  nada sobre lo que vigilan: con RLS apagado, una función de trigger a la que
  le falte el `security definer` se comporta igual de bien que una que lo
  tenga. Ahora las tres se apoyan justo en eso, y además exigen **de qué capa**
  viene el rechazo, no solo que lo haya: la 10 pide el mensaje de
  `fn_validar_postulacion` y la 13 el de `fn_validar_mensaje`. Sin eso la 13
  pasaría igual aunque el trigger callara, porque el `with check` de la
  política la rescataría por detrás.
  Si quien corre el script no puede hacer `set role authenticated`, las tres
  dicen `>>> NO SE PUDO PROBAR` en vez de mentir con un `PASA`.
- **El diagrama** `docs/tecnico/diagrama-er.png`, con su generador al lado.
- **El cruce contra las 33 historias de `S1-T01`**, arriba, con tres huecos
  documentados: `H-05`, `H-06` y `H-07`.

**Corrida del 2026-09-16, contra el proyecto real.** `01` a `04` sin error,
`90_verificacion.sql` **42 de 42** y `91_prueba_funcional.sql` **25 de 25**.

Las tres pruebas reescritas pasaron **por la razón correcta**, que era lo que
estaba en duda. Lo dice el detalle que devolvió cada una:

| # | Detalle | Qué demuestra |
|---|---|---|
| 10 | `Solo se puede postular a una solicitud abierta` | Es el mensaje de `fn_validar_postulacion`, no el de la política. Con RLS activo la función alcanzó a leer una solicitud cancelada ajena: el `security definer` está aplicándose |
| 12 | `2026-09-16 21:58:52.029248+00` | Los dos mensajes entraron con rol `authenticated` y `fn_tocar_conversacion` escribió en `conversaciones`, que no tiene política de `UPDATE`. Sin `security definer` esto habría quedado nulo, en silencio |
| 13 | `El emisor no participa en esta conversacion` | Es el mensaje de `fn_validar_mensaje`. El trigger `BEFORE` lo rechazó antes de que el `with check` de la política llegara a evaluarse, que era justo el riesgo de tapar el hueco |

Ninguna devolvió `>>> NO SE PUDO PROBAR`: el `SET ROLE authenticated` funciona
en el SQL Editor de Supabase.

**RLS con la `anon key`, contra PostgREST: 10 de 10.** Es el paso 2c, y lo
corre `basedatos/92_prueba_rls_anon.py` con cuatro sesiones reales. No es lo
mismo que el `set role authenticated` de `91`: aquí cada lectura lleva un JWT
y pasa por postgrest, que es por donde entraría alguien con la llave sacada
del APK.

Cuatro de las diez comprobaciones son controles, y hacen falta: si las tablas
estuvieran rotas del todo, **todo** volvería vacío y las seis primeras darían
`PASA` sin que nada funcionara. Por eso también se exige que lo que sí debe
verse, se vea: la ficha de un trabajador para cualquier usuario con sesión
(`DEC-19`), la conversación propia para su cliente, y los catálogos sin sesión
para que P-05 pueda pintarse.

Un detalle donde la realidad es **más estricta que el ticket**: `ia_cache` no
devuelve vacío, devuelve `HTTP 403`. El ticket pedía vacío. Es el `revoke all`
de `02_politicas_rls.sql`, que quita el permiso sobre la tabla antes de que
RLS tenga nada que filtrar. Cerrado por permiso es mejor que cerrado por
política, así que se deja y se anota aquí para que nadie lo lea como un
desvío.

**El paso 2 quedó completo**: cuatro de los cinco triggers están demostrados
—tres en `91` y el cuarto a mano— y el quinto no se puede demostrar porque el
esquema no deja que ocurra (`H-08`).
