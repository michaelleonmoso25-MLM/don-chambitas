# Base de datos

PostgreSQL sobre **Supabase** (DEC-16). Los cuatro archivos numerados `01` a
`04` son la fuente de verdad del modelo de datos. Si un documento y el SQL se
contradicen, gana el SQL.

## Orden de ejecución

Se corren **en orden y completos**. Cada uno depende del anterior.

| # | Archivo | Qué deja |
|---|---|---|
| 1 | `01_esquema.sql` | Tablas, tipos, triggers, vistas y funciones RPC |
| 2 | `02_politicas_rls.sql` | Permisos, políticas RLS y tiempo real |
| 3 | `03_almacenamiento.sql` | Cubetas de Storage y sus políticas |
| 4 | `04_datos_semilla.sql` | 16 categorías, 32 estados, 26 municipios |

Y dos que **no** crean nada, para comprobar que lo de arriba quedó bien:

| Archivo | Qué hace |
|---|---|
| `90_verificacion.sql` | 46 comprobaciones de estructura. Solo lee |
| `91_prueba_funcional.sql` | Recorre los 7 pasos de `PRODUCTO.md` con usuarios de prueba y se limpia solo |

Los dos devuelven una tabla donde **todo tiene que decir `PASA`**. Cualquier
renglón con `>>> FALLA` dice exactamente qué quedó mal y con qué valor.

Y dos para el paso 2c de `S1-T03`, que prueba lo que desde el SQL Editor no se
puede probar:

| Archivo | Qué hace |
|---|---|
| `92_usuarios_prueba.sql` | Crea las cuatro cuentas de prueba, confirmadas y con contraseña |
| `92_prueba_rls_anon.py` | Inicia sesión con ellas y comprueba que un usuario no alcanza los datos de otro |

```bash
# 1. pega 92_usuarios_prueba.sql en el SQL Editor y correlo
# 2.
cp .env.ejemplo .env      # y pon SUPABASE_URL y SUPABASE_ANON_KEY
python3 basedatos/92_prueba_rls_anon.py
```

**Por qué las cuentas se crean por SQL y no desde el guion.** Porque por la API
no se puede: el alta las rechaza con `email_address_invalid`. Supabase valida
que el dominio del correo exista, y `@prueba.donchambitas.mx` es ficticio
—`donchambitas.mx` no tiene registro A ni MX—. El dominio se eligió cuando solo
se usaba desde SQL, en `91`, que inserta directo en `auth.users` y por eso
nunca se topó con la validación.

No debilita la prueba: lo único que se saltan esas cuentas es el formulario de
registro. El guion inicia sesión de verdad y cada lectura va con un JWT real
contra PostgREST, que es justo lo que el paso 2c tiene que demostrar.

> Si algún día hace falta probar el **registro** desde la API —y hará falta en
> `S2-T07`— ese dominio no sirve. Es cosa de `S2-T07`, no de aquí.

**Por qué hace falta, si `91` ya prueba RLS.** `91` usa `set role authenticated`
dentro de una transacción. Eso no pasa por el JWT, ni por el rol `anon` sin
sesión, ni por PostgREST, y ahí es donde vive el riesgo: la `anon key` va
dentro del APK y cualquiera la saca. Este guion lee como leería esa persona.

**Escribe y no limpia.** Registra cuatro cuentas `@prueba.donchambitas.mx` con
su perfil, su solicitud, sus postulaciones y una conversación. Borrarlas
necesita la `service_role`, que no entra al repositorio, así que al terminar
imprime el `DELETE` que hay que pegar en el SQL Editor.

`.env` está en `.gitignore`; `.env.ejemplo` es la plantilla sin valores y esa
sí se comparte. La `service_role` y la llave de OpenAI **no van en ninguno de
los dos**.

### Empezar de cero

`00_reinicio.sql` borra todo —tablas, funciones, tipos, políticas y **todas las
cuentas de `auth.users`**— y deja el proyecto como recién creado. Después se
corren 01 a 04 otra vez.

**Las dos cubetas de Storage sobreviven, y no pasa nada.** Está comprobado
contra el proyecto real: Supabase rechaza el `DELETE` sobre `storage.objects`
**y** sobre `storage.buckets` con *"Direct deletion from storage tables is not
allowed. Use the Storage API instead."* (`SQLSTATE 42501`, trigger
`storage.protect_delete`). No hay forma de saltárselo desde el SQL Editor —ni
con este archivo ni con un `DELETE` a mano— y tampoco conviene: la protección
existe porque borrar la fila dejaría el archivo real huérfano.

Por eso `00` **no se detiene por eso**. Las cubetas quedan vacías y sin
políticas, y `03_almacenamiento.sql` las reconcilia con `on conflict do update`:
el resultado final es idéntico a haberlas borrado. El último renglón del reporte
acepta `0` o `2` y dice cuál de los dos pasó.

Si las quieres en cero de verdad, bórralas **antes** desde la consola →
**Storage** → `⋮` → *Delete bucket*. El panel usa la Storage API, no SQL, por
eso ahí sí puede. Por SQL no, da igual cómo lo intentes.

**Lo que sí detiene a `00` son los archivos dentro de las cubetas.** Un archivo
de la vida anterior sobreviviendo a una base recién creada es una incoherencia
real: ninguna fila lo referencia ya. Se resuelve con *Empty bucket* en la
consola.

Borra las cuentas **a propósito**: `public.usuarios` nace de un trigger que
solo dispara al insertar en `auth.users`. Si borras las tablas y dejas las
cuentas, esas cuentas quedan huérfanas para siempre —pueden iniciar sesión
pero no tienen perfil, y nada se lo va a crear. Un reinicio a medias es peor
que no reiniciar.

Hoy eso no cuesta nada porque no hay ni una cuenta real. **El día que la haya,
ese archivo deja de ser seguro.**

`02` y `03` **se pueden volver a correr** cuantas veces haga falta: borran sus
políticas y las vuelven a crear. Así, corregir una política no obliga a rehacer
el proyecto. `01` no: crea tablas, y correrlo dos veces falla.

### En Supabase

Consola del proyecto → **SQL Editor** → pegar cada archivo completo y correr,
uno por uno en ese orden. O con la CLI:

```bash
supabase db push
```

### En PostgreSQL local

```bash
createdb donchambitas
psql -d donchambitas -f basedatos/01_esquema.sql
psql -d donchambitas -f basedatos/04_datos_semilla.sql
```

Los archivos 02 y 03 **no corren en local**: dependen de `auth.uid()`,
`storage.objects` y la publicación `supabase_realtime`, que solo existen en
Supabase. Local sirve para validar el modelo y los triggers, no la seguridad.

> `01_esquema.sql` crea un trigger sobre `auth.users`. En local esa tabla no
> existe, así que esa parte falla. Para validar solo el modelo, comenta el
> bloque `tg_auth_usuario_creado` y la referencia `references auth.users(id)`.

## Lo que hay que entender antes de escribir una consulta

**El id del usuario es el de Supabase Auth.** `public.usuarios.id` es el mismo
uuid que `auth.users.id`, que es el mismo que devuelve `auth.uid()`. No hay
tabla de contraseñas: la credencial la guarda Supabase, y la recuperación
(P-04) también la manda Supabase. La fila de `usuarios` la crea sola el trigger
`tg_auth_usuario_creado` cuando alguien se registra, leyendo nombre, apellidos,
teléfono y rol del metadata del registro.

**`estatus` no es `estado_id`.** En `solicitudes` y `postulaciones`, `estatus`
es el estado del flujo (`abierta`, `asignada`, …) y `estado_id` es la entidad
federativa. Antes las dos se llamaban `estado` y era una trampa.

**Los roles son excluyentes y la base lo sostiene.** `perfiles_trabajador` y
`solicitudes` usan llaves foráneas compuestas contra `usuarios(id, rol)`. Un
cliente no puede tener perfil de trabajador ni aunque la aplicación se
equivoque.

**Cinco reglas viven en triggers, no en la aplicación.** Las cinco funciones
son `security definer` con `search_path` fijo, y eso **no es opcional**: sin
eso corren con los permisos de quien dispara el trigger, RLS les esconde las
filas de los demás, el `SELECT` devuelve `NULL`, el `IF` se evalúa a `NULL` y
la regla no se aplica — en silencio, sin error. Ojo al probarlo: el grueso de
`91_prueba_funcional.sql` corre como `postgres`, que se salta RLS, así que esas
fallas **ahí no se ven**. Las que sí las ven son las comprobaciones 37 a 42 de
`90_verificacion.sql`, que preguntan por la definición y por el permiso, y las
pruebas 10, 12, 13 y 21 a 24 de `91`, que son las únicas que hacen
`set role authenticated`.

Las cinco reglas:

1. Nadie se postula a su propia solicitud ni a una que no esté abierta.
2. Solo se reseña una solicitud cerrada, solo su cliente, y solo sobre el
   trabajador asignado.
3. Nadie escribe en una conversación de la que no es parte.
4. Cerrar una solicitud exige trabajador asignado.
5. Al borrar un trabajador, sus solicitudes asignadas vuelven a `abierta` y
   las cerradas conservan el historial sin trabajador.

**Lo que no se hace con una consulta suelta.** Estas cuatro operaciones son
funciones RPC porque son varias escrituras que tienen que pasar juntas, o
porque serían cinco viajes de red:

| Función | Para qué |
|---|---|
| `fn_perfil_publico_trabajador(uuid)` | P-07 entera en un viaje |
| `fn_aceptar_postulacion(uuid)` | Acepta una, rechaza las demás y asigna |
| `fn_cerrar_solicitud(uuid)` | Cierra y habilita la reseña |
| `fn_abrir_conversacion(uuid, uuid)` | Abre o recupera la conversación |
| `fn_ia_registrar_llamada(uuid, funcion_ia, int)` | Tope diario de IA (DEC-18) |

**Las tablas `ia_consumo` e `ia_cache` no las toca la aplicación.** Tienen RLS
activo y cero políticas a propósito. Las escribe solo la Edge Function de IA
con la `service_role` key.

## Las llaves

| Llave | Dónde vive | Quién la ve |
|---|---|---|
| URL del proyecto | `local.properties` → `BuildConfig` | pública |
| `anon key` | `local.properties` → `BuildConfig` | pública por diseño |
| `service_role key` | Variables de entorno de la Edge Function | nadie más |
| Llave de OpenAI | Variables de entorno de la Edge Function | nadie más |

**La `anon key` siendo pública no es un descuido.** Lo que protege los datos
son las políticas RLS de `02_politicas_rls.sql`. Por eso ese archivo no es
opcional: sin él, con la llave que va dentro del APK se lee la base entera.

La `service_role` key se salta todas las políticas. Nunca entra al repositorio
ni al APK, por ningún motivo.

## Estado

**Levantado y verificado el 2026-09-16**, la segunda vez. `01` a `04`
corrieron sin un error.

| Verificación | Resultado |
|---|---|
| `90_verificacion.sql` | **42 de 42 `PASA`** |
| `91_prueba_funcional.sql` | **25 de 25 `PASA`** |

Los cinco arreglos de la revisión previa están confirmados en vivo por las
comprobaciones 37 a 42, no solo escritos en el archivo.

**Las pruebas 10, 12 y 13 ya corren con `set role authenticated`**, desde el
2026-09-16, y pasaron. Antes corrían como `postgres`, que se salta RLS, y no
demostraban nada sobre lo que vigilan. Que pasaran **por la razón correcta** se
comprueba en el detalle que devuelven: las tres traen el mensaje de su trigger,
no el de una política. El desglose está en `MODELO-ER.md`.

**El paso 2c también pasó: 10 de 10**, con `92_prueba_rls_anon.py` y cuatro
sesiones reales contra PostgREST. Un usuario no alcanza la ficha de otro
cliente, ni las postulaciones de su competidor, ni una conversación ajena, ni
`ia_cache`. Y lo que sí debe verse, se ve.

`ia_cache` responde `HTTP 403`, no una lista vacía: el `revoke all` quita el
permiso sobre la tabla antes de que RLS entre a filtrar. Es más estricto que
lo que pedía el ticket, no menos.

`91` **no cubre dos de los cinco triggers**: cerrar una solicitud sin
trabajador asignado, que se probó a mano y falla por las dos vías, y
postularse a la propia solicitud, que no puede ocurrir porque los roles
excluyentes lo impiden antes. Es el hueco `H-08` de `MODELO-ER.md`.
