-- ============================================================================
--  DON CHAMBITAS
--  01_esquema.sql  ·  Tablas, tipos, funciones, triggers y vistas
-- ============================================================================
--  Motor: PostgreSQL 15+ sobre Supabase (DEC-16).
--  Orden de ejecucion: 01 -> 02 -> 03 -> 04. Ver basedatos/README.md
--
--  COMO SE EJECUTA
--    Supabase:  consola > SQL Editor, pegar este archivo completo y correr.
--               O bien:  supabase db push
--    Local:     psql -d donchambitas -f basedatos/01_esquema.sql
--
--  QUE HAY QUE SABER ANTES DE TOCARLO
--
--  1. La identidad la lleva Supabase Auth. auth.users es la tabla de
--     credenciales y NO se consulta desde la aplicacion. public.usuarios es
--     el perfil, y su id ES el id de auth.users. El trigger
--     tg_auth_usuario_creado crea la fila de perfil sola al registrarse.
--
--  2. Este esquema NO protege nada por si solo. Lo que protege los datos son
--     las politicas RLS de 02_politicas_rls.sql, porque la anon key que lleva
--     la aplicacion es publica por diseno. Sin el 02, la base queda abierta.
--
--  3. Cinco reglas viven en triggers y no en la aplicacion, porque la
--     aplicacion las va a olvidar tarde o temprano:
--       - Nadie se postula a su propia solicitud ni a una que no este abierta.
--       - Solo se resena una solicitud cerrada, solo su cliente, y solo sobre
--         el trabajador asignado.
--       - Nadie escribe en una conversacion de la que no es parte.
--       - Cerrar una solicitud exige trabajador asignado.
--       - Al borrar un trabajador, sus solicitudes asignadas vuelven a
--         abierta y las cerradas conservan el historial sin trabajador.
--
--     TODA funcion de trigger que LEA O ESCRIBA una fila que no sea la suya
--     lleva "security definer" y "set search_path = public". No es adorno:
--     sin eso corre con los permisos de quien dispara el trigger, RLS le
--     esconde las filas de los demas, el SELECT devuelve NULL, el IF evalua
--     a NULL y la regla NO se aplica -- en silencio, sin error. Un UPDATE en
--     esa situacion afecta cero filas y tampoco avisa.
--
--     Ojo al probarlo: 91_prueba_funcional.sql corre como postgres, que se
--     salta RLS, asi que estas fallas NO se ven ahi. Se ven desde la
--     aplicacion, con la anon key, que es donde duelen.
--
--  4. Los roles son excluyentes (PRODUCTO.md) y la base lo sostiene con
--     llaves foraneas compuestas contra usuarios(id, rol). Un cliente no
--     puede tener perfil de trabajador ni aunque la aplicacion se equivoque.
--
--  5. Todos los identificadores van en espanol, sin acentos ni enie, para no
--     tener que entrecomillarlos en cada consulta.
--
--  6. Ubicacion por catalogo de estado y municipio. Sin coordenadas ni mapas
--     (DEC-05). Ojo con el nombre: la columna del FLUJO de una solicitud se
--     llama "estatus" y la de la ENTIDAD FEDERATIVA se llama "estado_id".
--     Se llamaban "estado" las dos y era una trampa.
-- ============================================================================

begin;

-- ----------------------------------------------------------------------------
--  Extensiones
--  En Supabase las extensiones viven en el esquema "extensions", no en public.
--  Por eso los indices de abajo califican el operador: extensions.gin_trgm_ops
-- ----------------------------------------------------------------------------
create extension if not exists pg_trgm with schema extensions;

-- gen_random_uuid() es parte del nucleo desde PostgreSQL 13. No hace falta
-- pgcrypto para esto.

-- ----------------------------------------------------------------------------
--  Tipos enumerados
-- ----------------------------------------------------------------------------
create type public.rol_usuario        as enum ('cliente', 'trabajador');
create type public.estado_solicitud   as enum ('abierta', 'asignada', 'cerrada', 'cancelada');
create type public.estado_postulacion as enum ('enviada', 'aceptada', 'rechazada', 'retirada');
create type public.funcion_ia         as enum ('redactar_perfil', 'redactar_servicio', 'categorizar', 'sugerir');

-- ----------------------------------------------------------------------------
--  Funcion compartida: mantiene actualizado_en al dia
-- ----------------------------------------------------------------------------
create or replace function public.fn_actualizar_marca_tiempo()
returns trigger
language plpgsql
as $$
begin
    new.actualizado_en := now();
    return new;
end;
$$;

-- ============================================================================
--  CATALOGOS
-- ============================================================================

create table public.estados (
    id      smallserial  primary key,
    nombre  varchar(80)  not null unique,
    clave   char(3)      not null unique
);

comment on table public.estados is
    'Entidades federativas. Catalogo cerrado de 32, ver 04_datos_semilla.sql';

create table public.municipios (
    id         serial       primary key,
    estado_id  smallint     not null references public.estados(id) on delete restrict,
    nombre     varchar(120) not null,

    constraint uq_municipio_por_estado unique (estado_id, nombre)
);

create index ix_municipios_estado on public.municipios (estado_id);

create table public.categorias (
    id           smallserial primary key,
    nombre       varchar(80) not null unique,
    descripcion  varchar(200),
    icono        varchar(60),
    activa       boolean     not null default true,
    orden        smallint    not null default 0
);

comment on column public.categorias.icono is
    'Nombre del icono de Material Icons Outlined, no una URL. Lo asigna S1-T08';

-- ============================================================================
--  USUARIOS
--  El id ES el id de auth.users. No hay contrasena aqui: la guarda Supabase
--  Auth, y la recuperacion de contrasena (P-04) tambien la manda Supabase.
-- ============================================================================

create table public.usuarios (
    id              uuid         primary key references auth.users(id) on delete cascade,
    correo          varchar(160) not null unique,
    nombre          varchar(80)  not null,
    apellidos       varchar(120) not null,
    telefono        varchar(20),
    rol             rol_usuario  not null,
    foto_url        text,
    activo          boolean      not null default true,
    creado_en       timestamptz  not null default now(),
    actualizado_en  timestamptz  not null default now(),

    constraint ck_usuario_correo_valido
        check (correo ~* '^[^@[:space:]]+@[^@[:space:]]+\.[a-z]{2,}$'),
    constraint ck_usuario_correo_minusculas
        check (correo = lower(correo)),

    -- Redundante frente a la llave primaria, pero necesaria: es el destino de
    -- las llaves foraneas compuestas que hacen cumplir los roles excluyentes.
    constraint uq_usuario_rol unique (id, rol)
);

create trigger tg_usuarios_actualizado
    before update on public.usuarios
    for each row execute function public.fn_actualizar_marca_tiempo();

-- Crea el perfil publico en cuanto Supabase Auth crea la credencial.
-- La aplicacion manda nombre, apellidos, telefono y rol en el metadata del
-- registro (options.data de signUpWith). Ver S2-T07.
create or replace function public.fn_crear_usuario_desde_auth()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    insert into public.usuarios (id, correo, nombre, apellidos, telefono, rol)
    values (
        new.id,
        lower(new.email),
        coalesce(nullif(new.raw_user_meta_data ->> 'nombre', ''), 'Sin nombre'),
        coalesce(nullif(new.raw_user_meta_data ->> 'apellidos', ''), 'Sin apellidos'),
        nullif(new.raw_user_meta_data ->> 'telefono', ''),
        coalesce((new.raw_user_meta_data ->> 'rol')::rol_usuario, 'cliente')
    );
    return new;
end;
$$;

create trigger tg_auth_usuario_creado
    after insert on auth.users
    for each row execute function public.fn_crear_usuario_desde_auth();

-- ============================================================================
--  PERFIL DEL TRABAJADOR
--  Uno a uno con usuarios. La llave foranea compuesta contra (id, rol)
--  garantiza que solo un usuario con rol 'trabajador' puede tener perfil.
-- ============================================================================

create table public.perfiles_trabajador (
    usuario_id         uuid         primary key,
    rol                rol_usuario  not null default 'trabajador',
    titulo             varchar(100) not null,
    descripcion        text,
    experiencia_anios  smallint     not null default 0,
    telefono_contacto  varchar(20),
    estado_id          smallint     references public.estados(id)    on delete set null,
    municipio_id       integer      references public.municipios(id) on delete set null,
    disponible         boolean      not null default true,
    creado_en          timestamptz  not null default now(),
    actualizado_en     timestamptz  not null default now(),

    constraint ck_perfil_experiencia check (experiencia_anios between 0 and 70),
    constraint ck_perfil_rol         check (rol = 'trabajador'),
    constraint fk_perfil_usuario_trabajador
        foreign key (usuario_id, rol) references public.usuarios(id, rol)
        on delete cascade
);

create index ix_perfiles_ubicacion on public.perfiles_trabajador (estado_id, municipio_id);
create index ix_perfiles_titulo_trgm
    on public.perfiles_trabajador using gin (titulo extensions.gin_trgm_ops);

create trigger tg_perfiles_actualizado
    before update on public.perfiles_trabajador
    for each row execute function public.fn_actualizar_marca_tiempo();

create table public.perfil_habilidades (
    perfil_id  uuid        not null references public.perfiles_trabajador(usuario_id) on delete cascade,
    habilidad  varchar(60) not null,

    primary key (perfil_id, habilidad)
);

-- ============================================================================
--  SERVICIOS PUBLICADOS POR EL TRABAJADOR
-- ============================================================================

create table public.servicios (
    id              uuid         primary key default gen_random_uuid(),
    perfil_id       uuid         not null references public.perfiles_trabajador(usuario_id) on delete cascade,
    categoria_id    smallint     not null references public.categorias(id) on delete restrict,
    titulo          varchar(120) not null,
    descripcion     text         not null,
    precio_desde    numeric(10,2),
    precio_hasta    numeric(10,2),
    unidad_precio   varchar(30),
    activo          boolean      not null default true,
    creado_en       timestamptz  not null default now(),
    actualizado_en  timestamptz  not null default now(),

    constraint ck_servicio_precios_positivos
        check (precio_desde is null or precio_desde >= 0),
    constraint ck_servicio_rango_precio
        check (precio_hasta is null or precio_desde is null or precio_hasta >= precio_desde)
);

create index ix_servicios_perfil    on public.servicios (perfil_id);
create index ix_servicios_categoria on public.servicios (categoria_id) where activo;
create index ix_servicios_titulo_trgm
    on public.servicios using gin (titulo extensions.gin_trgm_ops);

create trigger tg_servicios_actualizado
    before update on public.servicios
    for each row execute function public.fn_actualizar_marca_tiempo();

-- Maximo 3 fotos por servicio. Es regla de la base, no de la aplicacion.
create table public.servicio_fotos (
    id           uuid        primary key default gen_random_uuid(),
    servicio_id  uuid        not null references public.servicios(id) on delete cascade,
    url          text        not null,
    posicion     smallint    not null default 1,
    creado_en    timestamptz not null default now(),

    constraint ck_foto_posicion       check (posicion between 1 and 3),
    constraint uq_foto_posicion_unica unique (servicio_id, posicion)
);

create index ix_servicio_fotos_servicio on public.servicio_fotos (servicio_id);

-- ============================================================================
--  SOLICITUDES DEL CLIENTE
--
--  OJO: "estatus" es el estado del FLUJO (abierta/asignada/cerrada/cancelada).
--       "estado_id" es la ENTIDAD FEDERATIVA. Son cosas distintas.
-- ============================================================================

create table public.solicitudes (
    id                   uuid              primary key default gen_random_uuid(),
    cliente_id           uuid              not null,
    rol_cliente          rol_usuario       not null default 'cliente',
    categoria_id         smallint          not null references public.categorias(id) on delete restrict,
    titulo               varchar(120)      not null,
    descripcion          text              not null,
    presupuesto          numeric(10,2),
    estado_id            smallint          references public.estados(id)    on delete set null,
    municipio_id         integer           references public.municipios(id) on delete set null,
    estatus              estado_solicitud  not null default 'abierta',
    trabajador_id        uuid              references public.perfiles_trabajador(usuario_id) on delete set null,
    categorizada_por_ia  boolean           not null default false,
    creado_en            timestamptz       not null default now(),
    actualizado_en       timestamptz       not null default now(),
    cerrada_en           timestamptz,

    constraint ck_solicitud_presupuesto
        check (presupuesto is null or presupuesto >= 0),
    constraint ck_solicitud_rol_cliente check (rol_cliente = 'cliente'),
    -- DEC-37 (H-44): el mismo tope que la pantalla de P-08.
    constraint ck_solicitud_descripcion check (char_length(descripcion) <= 1000),
    constraint fk_solicitud_cliente
        foreign key (cliente_id, rol_cliente) references public.usuarios(id, rol)
        on delete cascade,

    -- Solo 'asignada' exige trabajador. 'cerrada' NO lo exige a nivel de
    -- tabla a proposito: si el trabajador borra su cuenta, el historial de
    -- trabajos cerrados tiene que sobrevivir. Que no se pueda CERRAR sin
    -- trabajador lo impone fn_validar_transicion_solicitud, sobre la
    -- transicion y no sobre la fila. Con un CHECK sobre la fila, el
    -- ON DELETE SET NULL de arriba hacia imposible borrar la cuenta.
    constraint ck_solicitud_asignada_con_trabajador
        check (estatus <> 'asignada' or trabajador_id is not null)
);

create index ix_solicitudes_cliente    on public.solicitudes (cliente_id);
create index ix_solicitudes_abiertas   on public.solicitudes (categoria_id, creado_en desc) where estatus = 'abierta';
create index ix_solicitudes_trabajador on public.solicitudes (trabajador_id);
create index ix_solicitudes_titulo_trgm
    on public.solicitudes using gin (titulo extensions.gin_trgm_ops);

create trigger tg_solicitudes_actualizado
    before update on public.solicitudes
    for each row execute function public.fn_actualizar_marca_tiempo();

-- Cerrar exige trabajador, y sella la fecha de cierre sola.
create or replace function public.fn_validar_transicion_solicitud()
returns trigger
language plpgsql
as $$
begin
    if new.estatus = 'cerrada' and old.estatus is distinct from 'cerrada' then
        if new.trabajador_id is null then
            raise exception 'No se puede cerrar una solicitud sin trabajador asignado';
        end if;
        new.cerrada_en := now();
    end if;
    return new;
end;
$$;

create trigger tg_solicitudes_transicion
    before update on public.solicitudes
    for each row execute function public.fn_validar_transicion_solicitud();

-- La ubicacion de una solicitud es obligatoria (HU-12) y el municipio tiene que
-- ser del estado (DEC-37, H-41). Solo en insert, y no como CHECK: estado_id y
-- municipio_id son "on delete set null", y un CHECK sobre la fila contradiria
-- esas llaves e impediria borrar un estado o un municipio. Con el trigger solo
-- en insert, las solicitudes antiguas no se invalidan.
create or replace function public.fn_validar_ubicacion_solicitud()
returns trigger
language plpgsql
as $$
begin
    if new.estado_id is null or new.municipio_id is null then
        raise exception 'La solicitud necesita estado y municipio';
    end if;
    if not exists (select 1 from public.municipios
                    where id = new.municipio_id and estado_id = new.estado_id) then
        raise exception 'El municipio no es del estado elegido';
    end if;
    return new;
end;
$$;

create trigger tg_solicitudes_ubicacion
    before insert on public.solicitudes
    for each row execute function public.fn_validar_ubicacion_solicitud();

-- ============================================================================
--  POSTULACIONES
-- ============================================================================

create table public.postulaciones (
    id                uuid                primary key default gen_random_uuid(),
    solicitud_id      uuid                not null references public.solicitudes(id) on delete cascade,
    trabajador_id     uuid                not null references public.perfiles_trabajador(usuario_id) on delete cascade,
    mensaje           text,
    precio_propuesto  numeric(10,2),
    estatus           estado_postulacion  not null default 'enviada',
    creado_en         timestamptz         not null default now(),
    actualizado_en    timestamptz         not null default now(),

    constraint uq_una_postulacion_por_solicitud unique (solicitud_id, trabajador_id),
    constraint ck_postulacion_precio
        check (precio_propuesto is null or precio_propuesto >= 0)
);

create index ix_postulaciones_solicitud  on public.postulaciones (solicitud_id);
create index ix_postulaciones_trabajador on public.postulaciones (trabajador_id, creado_en desc);

create trigger tg_postulaciones_actualizado
    before update on public.postulaciones
    for each row execute function public.fn_actualizar_marca_tiempo();

-- Nadie se postula a su propia solicitud, ni a una que no este abierta.
-- security definer: el trabajador no ve por RLS una solicitud cerrada o
-- cancelada ajena. Sin esto el select devuelve NULL, el if no dispara y la
-- postulacion invalida entra.
create or replace function public.fn_validar_postulacion()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_cliente uuid;
    v_estatus estado_solicitud;
begin
    select cliente_id, estatus into v_cliente, v_estatus
      from public.solicitudes where id = new.solicitud_id;

    if v_cliente = new.trabajador_id then
        raise exception 'Un trabajador no puede postularse a su propia solicitud';
    end if;

    if tg_op = 'INSERT' and v_estatus <> 'abierta' then
        raise exception 'Solo se puede postular a una solicitud abierta';
    end if;

    return new;
end;
$$;

create trigger tg_postulaciones_validar
    before insert or update on public.postulaciones
    for each row execute function public.fn_validar_postulacion();

-- ============================================================================
--  CHAT INTERNO
-- ============================================================================

create table public.conversaciones (
    id                 uuid        primary key default gen_random_uuid(),
    cliente_id         uuid        not null references public.usuarios(id) on delete cascade,
    trabajador_id      uuid        not null references public.perfiles_trabajador(usuario_id) on delete cascade,
    solicitud_id       uuid        references public.solicitudes(id) on delete set null,
    creado_en          timestamptz not null default now(),
    ultimo_mensaje_en  timestamptz,

    constraint ck_conversacion_partes_distintas check (cliente_id <> trabajador_id)
);

-- Una sola conversacion por par de usuarios y solicitud. El COALESCE permite
-- tratar la conversacion sin solicitud como un caso mas.
create unique index uq_conversacion_unica
    on public.conversaciones (cliente_id, trabajador_id,
                              coalesce(solicitud_id, '00000000-0000-0000-0000-000000000000'::uuid));

create index ix_conversaciones_cliente
    on public.conversaciones (cliente_id, ultimo_mensaje_en desc nulls last);
create index ix_conversaciones_trabajador
    on public.conversaciones (trabajador_id, ultimo_mensaje_en desc nulls last);

create table public.mensajes (
    id               uuid        primary key default gen_random_uuid(),
    conversacion_id  uuid        not null references public.conversaciones(id) on delete cascade,
    emisor_id        uuid        not null references public.usuarios(id) on delete cascade,
    contenido        text        not null,
    leido_en         timestamptz,
    creado_en        timestamptz not null default now(),

    constraint ck_mensaje_no_vacio check (length(btrim(contenido)) > 0)
);

create index ix_mensajes_conversacion on public.mensajes (conversacion_id, creado_en desc);

-- Nadie escribe en una conversacion de la que no es parte.
-- security definer por lo mismo que fn_validar_postulacion: si el emisor no
-- ve la conversacion, el select devuelve NULL y la comparacion se evalua a
-- NULL, que no es "verdadero" y por lo tanto no rechaza nada.
create or replace function public.fn_validar_mensaje()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_cliente    uuid;
    v_trabajador uuid;
begin
    select cliente_id, trabajador_id into v_cliente, v_trabajador
      from public.conversaciones where id = new.conversacion_id;

    if new.emisor_id <> v_cliente and new.emisor_id <> v_trabajador then
        raise exception 'El emisor no participa en esta conversacion';
    end if;

    return new;
end;
$$;

create trigger tg_mensajes_validar
    before insert on public.mensajes
    for each row execute function public.fn_validar_mensaje();

-- Mantiene ultimo_mensaje_en para ordenar la bandeja (P-15) sin recorrer
-- los mensajes.
--
-- security definer, y aqui es donde mas se nota: conversaciones NO tiene
-- politica de UPDATE, a proposito, para que nadie falsifique la marca. Sin
-- security definer este update afecta CERO filas, sin error, y
-- ultimo_mensaje_en se queda en NULL para siempre: la bandeja de P-15 se
-- queda sin criterio de orden y nadie se entera.
create or replace function public.fn_tocar_conversacion()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    update public.conversaciones
       set ultimo_mensaje_en = new.creado_en
     where id = new.conversacion_id;
    return new;
end;
$$;

create trigger tg_mensajes_tocar_conversacion
    after insert on public.mensajes
    for each row execute function public.fn_tocar_conversacion();

-- ============================================================================
--  RESENAS
-- ============================================================================

create table public.resenas (
    id             uuid        primary key default gen_random_uuid(),
    solicitud_id   uuid        not null unique references public.solicitudes(id) on delete cascade,
    cliente_id     uuid        not null references public.usuarios(id) on delete cascade,
    trabajador_id  uuid        not null references public.perfiles_trabajador(usuario_id) on delete cascade,
    calificacion   smallint    not null,
    comentario     text,
    creado_en      timestamptz not null default now(),

    constraint ck_resena_calificacion     check (calificacion between 1 and 5),
    constraint ck_resena_partes_distintas check (cliente_id <> trabajador_id)
);

create index ix_resenas_trabajador on public.resenas (trabajador_id, creado_en desc);

-- Solo se resena una solicitud cerrada, solo su cliente, y solo sobre el
-- trabajador que quedo asignado.
-- security definer: la validacion tiene que leer la solicitud completa,
-- incluida la de otro cliente, para poder rechazarla.
create or replace function public.fn_validar_resena()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_cliente    uuid;
    v_trabajador uuid;
    v_estatus    estado_solicitud;
begin
    select cliente_id, trabajador_id, estatus
      into v_cliente, v_trabajador, v_estatus
      from public.solicitudes where id = new.solicitud_id;

    if v_estatus <> 'cerrada' then
        raise exception 'Solo se puede resenar una solicitud cerrada';
    end if;
    if new.cliente_id <> v_cliente then
        raise exception 'Solo el cliente que publico la solicitud puede resenarla';
    end if;
    if new.trabajador_id is distinct from v_trabajador then
        raise exception 'La resena debe corresponder al trabajador asignado';
    end if;

    return new;
end;
$$;

create trigger tg_resenas_validar
    before insert or update on public.resenas
    for each row execute function public.fn_validar_resena();

-- ============================================================================
--  BAJA DE UN TRABAJADOR
--
--  Antes de que la llave foranea aplique su ON DELETE SET NULL, hay que dejar
--  las solicitudes en un estado coherente. Si no, borrar la cuenta de un
--  trabajador con trabajos asignados o cerrados aborta la transaccion y el
--  usuario queda atrapado sin poder darse de baja.
-- ============================================================================

-- security definer: estas solicitudes son de OTROS usuarios, los clientes.
-- Sin esto los dos updates afectan cero filas, el ON DELETE SET NULL deja una
-- solicitud 'asignada' con trabajador_id nulo, eso viola
-- ck_solicitud_asignada_con_trabajador y el trabajador no puede borrar su
-- perfil ni darse de baja.
create or replace function public.fn_liberar_solicitudes_del_trabajador()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    -- Las asignadas vuelven a la fila para que el cliente reasigne.
    update public.solicitudes
       set estatus = 'abierta', trabajador_id = null
     where trabajador_id = old.usuario_id
       and estatus = 'asignada';

    -- Las cerradas y canceladas conservan el historial, sin trabajador.
    update public.solicitudes
       set trabajador_id = null
     where trabajador_id = old.usuario_id
       and estatus in ('cerrada', 'cancelada');

    return old;
end;
$$;

create trigger tg_perfiles_liberar_solicitudes
    before delete on public.perfiles_trabajador
    for each row execute function public.fn_liberar_solicitudes_del_trabajador();

-- ============================================================================
--  CONSUMO DE INTELIGENCIA ARTIFICIAL
--
--  Estas dos tablas NO las toca la aplicacion. Las escribe unicamente la
--  Edge Function de IA con la service_role key. En 02_politicas_rls.sql se
--  quedan con RLS activo y sin una sola politica, que es la forma de decir
--  "nadie entra por la anon key".
-- ============================================================================

create table public.ia_consumo (
    usuario_id  uuid        not null references public.usuarios(id) on delete cascade,
    fecha       date        not null default current_date,
    funcion     funcion_ia  not null,
    llamadas    integer     not null default 0,
    tokens      integer     not null default 0,

    primary key (usuario_id, fecha, funcion),
    constraint ck_ia_llamadas check (llamadas >= 0),
    constraint ck_ia_tokens   check (tokens   >= 0)
);

-- Cache de respuestas de IA, para no pagar dos veces lo mismo.
-- La llave incluye la FUNCION ademas del hash: el mismo texto pedido a
-- redactar_perfil y a categorizar son dos respuestas distintas, y con el hash
-- solo como llave se pisaban entre ellas.
create table public.ia_cache (
    funcion       funcion_ia  not null,
    entrada_hash  text        not null,
    respuesta     text        not null,
    creado_en     timestamptz not null default now(),

    primary key (funcion, entrada_hash)
);

create index ix_ia_cache_creado on public.ia_cache (creado_en);

-- Tope diario: 10 llamadas por usuario al dia EN TOTAL, sumando las cuatro
-- funciones, no 10 por cada una (DEC-18). La desagregacion por funcion existe
-- para poder medir, no para repartir el presupuesto.
--
-- La llama la Edge Function DESPUES de resolver que hay que pegarle a OpenAI.
-- Devuelve cuantas llamadas le quedan al usuario hoy, o -1 si ya no le quedan
-- y no se registro nada.
create or replace function public.fn_ia_registrar_llamada(
    p_usuario_id uuid,
    p_funcion    funcion_ia,
    p_tokens     integer default 0
)
returns integer
language plpgsql
security definer
set search_path = public
as $$
declare
    k_limite_diario constant integer := 10;
    v_usadas integer;
begin
    select coalesce(sum(llamadas), 0) into v_usadas
      from public.ia_consumo
     where usuario_id = p_usuario_id and fecha = current_date;

    if v_usadas >= k_limite_diario then
        return -1;
    end if;

    insert into public.ia_consumo (usuario_id, fecha, funcion, llamadas, tokens)
    values (p_usuario_id, current_date, p_funcion, 1, coalesce(p_tokens, 0))
    on conflict (usuario_id, fecha, funcion) do update
        set llamadas = public.ia_consumo.llamadas + 1,
            tokens   = public.ia_consumo.tokens + coalesce(p_tokens, 0);

    return k_limite_diario - v_usadas - 1;
end;
$$;

comment on function public.fn_ia_registrar_llamada is
    'Tope diario de IA (DEC-18). Devuelve llamadas restantes hoy, o -1 si ya no hay';

-- ============================================================================
--  VISTAS
--
--  Las dos llevan security_invoker: se ejecutan con los permisos de quien
--  consulta, no de quien las creo. Sin eso, una vista se salta las politicas
--  RLS de las tablas que hay debajo y se convierte en una fuga.
-- ============================================================================

-- Calificacion promedio del trabajador. Alimenta P-07 y el orden de P-06.
create or replace view public.vw_trabajador_calificacion
with (security_invoker = on) as
select p.usuario_id                               as trabajador_id,
       count(r.id)                                as total_resenas,
       round(coalesce(avg(r.calificacion), 0), 2) as promedio
  from public.perfiles_trabajador p
  left join public.resenas r on r.trabajador_id = p.usuario_id
 group by p.usuario_id;

-- Tarjeta de resultados de P-06 ya armada. Los filtros de busqueda
-- (texto, categoria, ubicacion, precio, calificacion) se aplican encima de
-- esta vista desde postgrest. Ver docs/tecnico/CONTRATOS-API.md
create or replace view public.vw_busqueda_trabajadores
with (security_invoker = on) as
select p.usuario_id  as trabajador_id,
       u.nombre,
       u.apellidos,
       u.foto_url,
       p.titulo,
       p.disponible,
       p.estado_id,
       p.municipio_id,
       e.nombre      as estado,
       m.nombre      as municipio,
       coalesce(c.promedio, 0)      as promedio,
       coalesce(c.total_resenas, 0) as total_resenas,
       count(s.id) filter (where s.activo) as servicios_activos,
       min(s.precio_desde) filter (where s.activo) as precio_desde,
       -- Categorias de los servicios activos del trabajador. Es un arreglo
       -- porque un trabajador ofrece varios oficios, y va aqui porque el
       -- filtro por categoria es el principal de P-06 y de la cuadricula de
       -- P-05. Desde postgrest se filtra con "categorias=cs.{3}" (contiene).
       coalesce(
           array_agg(distinct s.categoria_id) filter (where s.activo),
           '{}'::smallint[]
       ) as categorias,
       -- Para el orden "mas recientes" del contrato. Es la fecha de alta del
       -- perfil, no la del usuario.
       p.creado_en
  from public.perfiles_trabajador p
  join public.usuarios u                     on u.id = p.usuario_id
  left join public.estados e                 on e.id = p.estado_id
  left join public.municipios m              on m.id = p.municipio_id
  left join public.vw_trabajador_calificacion c on c.trabajador_id = p.usuario_id
  left join public.servicios s               on s.perfil_id = p.usuario_id
 where u.activo
 group by p.usuario_id, u.nombre, u.apellidos, u.foto_url, p.titulo,
          p.disponible, p.estado_id, p.municipio_id, e.nombre, m.nombre,
          c.promedio, c.total_resenas, p.creado_en;

-- P-09 Mis solicitudes: las columnas de solicitudes mas cuantas postulaciones
-- siguen sin revisar, o sea en 'enviada' (DEC-37, H-42). Es una vista y no un
-- conteo embebido de PostgREST porque no depende de que PostgREST tenga las
-- funciones de agregacion habilitadas. Respeta RLS (security_invoker): el
-- cliente dueno ve todas las postulaciones de sus solicitudes.
create or replace view public.vw_mis_solicitudes
with (security_invoker = on) as
select s.id,
       s.cliente_id,
       s.rol_cliente,
       s.categoria_id,
       s.titulo,
       s.descripcion,
       s.presupuesto,
       s.estado_id,
       s.municipio_id,
       s.estatus,
       s.trabajador_id,
       s.categorizada_por_ia,
       s.creado_en,
       s.actualizado_en,
       s.cerrada_en,
       (select count(*) from public.postulaciones po
         where po.solicitud_id = s.id and po.estatus = 'enviada')::int
           as postulaciones_sin_revisar
  from public.solicitudes s
 where s.cliente_id = auth.uid();

-- ============================================================================
--  FUNCIONES RPC
--
--  Aqui vive lo que NO se puede resolver con una consulta suelta desde la
--  aplicacion: o porque son varias escrituras que tienen que pasar juntas o
--  ninguna, o porque serian cinco viajes de red para pintar una pantalla.
--  La aplicacion las llama con postgrest.rpc("nombre").
-- ============================================================================

-- P-07 completa en un solo viaje: perfil, habilidades, servicios con fotos,
-- calificacion y resenas.
--
-- security definer a proposito: necesita leer el nombre del cliente que dejo
-- cada resena, y las politicas RLS de usuarios no lo permiten en general. Lo
-- que sale de aqui son EXACTAMENTE los campos listados abajo y nada mas;
-- ningun correo, ningun telefono de cliente.
create or replace function public.fn_perfil_publico_trabajador(p_trabajador_id uuid)
returns jsonb
language sql
stable
security definer
set search_path = public
as $$
    select jsonb_build_object(
        'id',                p.usuario_id,
        'nombre',            u.nombre,
        'apellidos',         u.apellidos,
        'foto_url',          u.foto_url,
        'titulo',            p.titulo,
        'descripcion',       p.descripcion,
        'experiencia_anios', p.experiencia_anios,
        'telefono_contacto', p.telefono_contacto,
        'disponible',        p.disponible,
        'estado',            e.nombre,
        'municipio',         m.nombre,
        'habilidades', coalesce((
            select jsonb_agg(h.habilidad order by h.habilidad)
              from public.perfil_habilidades h
             where h.perfil_id = p.usuario_id), '[]'::jsonb),
        'calificacion', jsonb_build_object(
            'promedio',      coalesce(c.promedio, 0),
            'total_resenas', coalesce(c.total_resenas, 0)),
        'servicios', coalesce((
            select jsonb_agg(jsonb_build_object(
                       'id',            s.id,
                       'titulo',        s.titulo,
                       'descripcion',   s.descripcion,
                       'categoria',     cat.nombre,
                       'categoria_id',  s.categoria_id,
                       'precio_desde',  s.precio_desde,
                       'precio_hasta',  s.precio_hasta,
                       'unidad_precio', s.unidad_precio,
                       'fotos', coalesce((
                           select jsonb_agg(f.url order by f.posicion)
                             from public.servicio_fotos f
                            where f.servicio_id = s.id), '[]'::jsonb))
                   order by s.creado_en desc)
              from public.servicios s
              join public.categorias cat on cat.id = s.categoria_id
             where s.perfil_id = p.usuario_id and s.activo), '[]'::jsonb),
        'resenas', coalesce((
            select jsonb_agg(jsonb_build_object(
                       'calificacion',   r.calificacion,
                       'comentario',     r.comentario,
                       'cliente_nombre', ru.nombre,
                       'creado_en',      r.creado_en)
                   order by r.creado_en desc)
              from public.resenas r
              join public.usuarios ru on ru.id = r.cliente_id
             where r.trabajador_id = p.usuario_id), '[]'::jsonb)
    )
      from public.perfiles_trabajador p
      join public.usuarios u                        on u.id = p.usuario_id
      left join public.estados e                    on e.id = p.estado_id
      left join public.municipios m                 on m.id = p.municipio_id
      left join public.vw_trabajador_calificacion c on c.trabajador_id = p.usuario_id
     where p.usuario_id = p_trabajador_id and u.activo;
$$;

-- Aceptar una postulacion son TRES escrituras que tienen que pasar juntas:
-- aceptar esa, rechazar las demas y asignar la solicitud. Si se hacen sueltas
-- desde la aplicacion, una caida a medio camino deja dos trabajadores
-- creyendo que ganaron.
create or replace function public.fn_aceptar_postulacion(p_postulacion_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_solicitud   uuid;
    v_trabajador  uuid;
    v_cliente     uuid;
    v_estatus     estado_solicitud;
begin
    select po.solicitud_id, po.trabajador_id, s.cliente_id, s.estatus
      into v_solicitud, v_trabajador, v_cliente, v_estatus
      from public.postulaciones po
      join public.solicitudes s on s.id = po.solicitud_id
     where po.id = p_postulacion_id;

    if v_solicitud is null then
        raise exception 'La postulacion no existe';
    end if;
    if v_cliente is distinct from auth.uid() then
        raise exception 'Solo el cliente que publico la solicitud puede aceptar postulaciones';
    end if;
    if v_estatus <> 'abierta' then
        raise exception 'La solicitud ya no esta abierta';
    end if;

    update public.postulaciones
       set estatus = 'aceptada'
     where id = p_postulacion_id;

    update public.postulaciones
       set estatus = 'rechazada'
     where solicitud_id = v_solicitud
       and id <> p_postulacion_id
       and estatus = 'enviada';

    update public.solicitudes
       set estatus = 'asignada', trabajador_id = v_trabajador
     where id = v_solicitud;
end;
$$;

-- Cerrar la solicitud. Habilita la resena (P-17): el trigger de resenas exige
-- que la solicitud este cerrada.
create or replace function public.fn_cerrar_solicitud(p_solicitud_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_cliente uuid;
    v_estatus estado_solicitud;
begin
    select cliente_id, estatus into v_cliente, v_estatus
      from public.solicitudes where id = p_solicitud_id;

    if v_cliente is null then
        raise exception 'La solicitud no existe';
    end if;
    if v_cliente is distinct from auth.uid() then
        raise exception 'Solo el cliente que publico la solicitud puede cerrarla';
    end if;
    if v_estatus <> 'asignada' then
        raise exception 'Solo se puede cerrar una solicitud asignada';
    end if;

    update public.solicitudes
       set estatus = 'cerrada'
     where id = p_solicitud_id;
end;
$$;

-- Abre la conversacion con un trabajador, o devuelve la que ya existia.
-- Resuelve el "POST /conversaciones" del contrato, que era un upsert.
--
-- LA ABRE SIEMPRE EL CLIENTE, y esta decidido asi (DEC-20). Fija
-- cliente_id = auth.uid(), y conversaciones.trabajador_id apunta a
-- perfiles_trabajador, asi que un trabajador que la llame crearia una fila
-- con un cliente_id que no tiene perfil de trabajador y la llave foranea la
-- rechaza. El trabajador responde, no inicia: el contacto nace en P-07 o al
-- aceptarle la postulacion.
create or replace function public.fn_abrir_conversacion(
    p_trabajador_id uuid,
    p_solicitud_id  uuid default null
)
returns uuid
language plpgsql
security definer
set search_path = public
as $$
declare
    v_id uuid;
begin
    if p_trabajador_id = auth.uid() then
        raise exception 'No se puede abrir una conversacion consigo mismo';
    end if;

    select id into v_id
      from public.conversaciones
     where cliente_id = auth.uid()
       and trabajador_id = p_trabajador_id
       and coalesce(solicitud_id, '00000000-0000-0000-0000-000000000000'::uuid)
         = coalesce(p_solicitud_id, '00000000-0000-0000-0000-000000000000'::uuid);

    if v_id is not null then
        return v_id;
    end if;

    insert into public.conversaciones (cliente_id, trabajador_id, solicitud_id)
    values (auth.uid(), p_trabajador_id, p_solicitud_id)
    returning id into v_id;

    return v_id;
end;
$$;

commit;
