-- ============================================================================
--  DON CHAMBITAS
--  02_politicas_rls.sql  ·  Seguridad a nivel de fila, permisos y tiempo real
-- ============================================================================
--  Ejecutar DESPUES de 01_esquema.sql.
--
--  POR QUE ESTE ARCHIVO NO ES OPCIONAL
--
--  La aplicacion lleva la URL del proyecto y la anon key. Las dos son
--  PUBLICAS por diseno: cualquiera puede sacarlas del APK en dos minutos.
--  Lo unico que impide que con esa llave se lea la base entera son estas
--  politicas. Sin este archivo, el proyecto esta abierto de par en par.
--
--  COMO SE LEE UNA POLITICA
--    using       -> que filas puede VER o tocar
--    with check  -> que filas puede DEJAR escritas
--  auth.uid() devuelve el id del usuario de la sesion, y es el mismo id que
--  public.usuarios.id (ver 01_esquema.sql).
--
--  LA REGLA DE ORO: la service_role key se salta TODO esto. Por eso nunca
--  entra al APK ni al repositorio. Vive solo en la Edge Function de IA.
-- ============================================================================

begin;

-- ----------------------------------------------------------------------------
--  Este archivo se puede volver a correr cuantas veces haga falta.
--  Primero borra las politicas que haya y luego las vuelve a crear, para que
--  corregir una politica no obligue a rehacer el proyecto entero.
-- ----------------------------------------------------------------------------
do $$
declare r record;
begin
    for r in select tablename, policyname from pg_policies where schemaname = 'public'
    loop
        execute format('drop policy if exists %I on public.%I', r.policyname, r.tablename);
    end loop;
end
$$;

-- ----------------------------------------------------------------------------
--  Permisos base
--  RLS filtra filas, pero primero hace falta el permiso sobre la tabla.
-- ----------------------------------------------------------------------------
grant usage on schema public to anon, authenticated;

-- Catalogos: lectura para todos, incluso sin sesion.
grant select on public.estados, public.municipios, public.categorias to anon, authenticated;

-- Datos del negocio: solo con sesion. RLS decide cuales filas.
grant select, insert, update, delete on
    public.usuarios,
    public.perfiles_trabajador,
    public.perfil_habilidades,
    public.servicios,
    public.servicio_fotos,
    public.solicitudes,
    public.postulaciones,
    public.conversaciones,
    public.mensajes,
    public.resenas
to authenticated;

grant select on public.vw_trabajador_calificacion, public.vw_busqueda_trabajadores,
    public.vw_mis_solicitudes to authenticated;

-- Las tablas de IA no las toca nadie con la anon key. Punto.
revoke all on public.ia_consumo, public.ia_cache from anon, authenticated;

-- ----------------------------------------------------------------------------
--  Permisos por columna
--
--  RLS decide QUE FILAS puede tocar alguien, nunca QUE COLUMNAS. Con el
--  "grant update" completo de arriba, la politica "usuario edita su propia
--  ficha" deja que cada quien se cambie tambien el correo (y lo desincronice
--  de auth.users para siempre), el rol (cambiar de rol esta FUERA del MVP,
--  ver PRODUCTO.md) y el activo. Y la politica de marcar leidos deja
--  reescribir el CONTENIDO de los mensajes que uno recibio.
--
--  Los permisos por columna son la unica herramienta que cierra eso, y dejan
--  exactamente lo que pide CONTRATOS-API.md:
--    actualizarMiUsuario(nombre, apellidos, telefono) + subirFotoPerfil
--    marcarLeidos(conversacionId)
-- ----------------------------------------------------------------------------
revoke update on public.usuarios from authenticated;
grant  update (nombre, apellidos, telefono, foto_url)
    on public.usuarios to authenticated;

revoke update on public.mensajes from authenticated;
grant  update (leido_en)
    on public.mensajes to authenticated;

-- ----------------------------------------------------------------------------
--  RLS activo en absolutamente todo
-- ----------------------------------------------------------------------------
alter table public.estados             enable row level security;
alter table public.municipios          enable row level security;
alter table public.categorias          enable row level security;
alter table public.usuarios            enable row level security;
alter table public.perfiles_trabajador enable row level security;
alter table public.perfil_habilidades  enable row level security;
alter table public.servicios           enable row level security;
alter table public.servicio_fotos      enable row level security;
alter table public.solicitudes         enable row level security;
alter table public.postulaciones       enable row level security;
alter table public.conversaciones      enable row level security;
alter table public.mensajes            enable row level security;
alter table public.resenas             enable row level security;
alter table public.ia_consumo          enable row level security;
alter table public.ia_cache            enable row level security;

-- ----------------------------------------------------------------------------
--  Auxiliares
-- ----------------------------------------------------------------------------

-- Dos usuarios son contraparte si comparten una conversacion, o si uno se
-- postulo a una solicitud del otro. Sirve para que el trabajador pueda ver el
-- nombre del cliente con el que esta tratando, y al reves, sin abrir la tabla
-- de usuarios entera.
create or replace function public.fn_es_contraparte(p_otro uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists (
        select 1 from public.conversaciones c
         where (c.cliente_id    = auth.uid() and c.trabajador_id = p_otro)
            or (c.trabajador_id = auth.uid() and c.cliente_id    = p_otro)
    ) or exists (
        select 1
          from public.postulaciones po
          join public.solicitudes s on s.id = po.solicitud_id
         where (s.cliente_id     = auth.uid() and po.trabajador_id = p_otro)
            or (po.trabajador_id = auth.uid() and s.cliente_id     = p_otro)
    );
$$;

-- Si auth.uid() es parte de la conversacion.
create or replace function public.fn_participa_en_conversacion(p_conversacion_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists (
        select 1 from public.conversaciones c
         where c.id = p_conversacion_id
           and (c.cliente_id = auth.uid() or c.trabajador_id = auth.uid())
    );
$$;

-- ROMPEN LA RECURSION ENTRE solicitudes Y postulaciones.
--
-- La politica de solicitudes necesita saber si te postulaste, y la de
-- postulaciones necesita saber si la solicitud es tuya. Si cada una consulta
-- la tabla de la otra directamente, leer cualquiera de las dos dispara la
-- politica de la otra, que dispara la de la primera, y PostgreSQL aborta con
-- "infinite recursion detected in policy".
--
-- Al meter la consulta dentro de una funcion SECURITY DEFINER, esa consulta
-- corre como el dueno de la tabla y NO evalua politicas. El ciclo se corta.
-- Es la unica forma de expresar esto; no es un atajo.
create or replace function public.fn_es_mi_solicitud(p_solicitud_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists (
        select 1 from public.solicitudes s
         where s.id = p_solicitud_id and s.cliente_id = auth.uid()
    );
$$;

create or replace function public.fn_me_postule_a(p_solicitud_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists (
        select 1 from public.postulaciones po
         where po.solicitud_id = p_solicitud_id and po.trabajador_id = auth.uid()
    );
$$;

-- Si auth.uid() tiene cuenta de trabajador. Mira usuarios.rol y no
-- perfiles_trabajador: un trabajador recien registrado todavia no tiene
-- perfil y ya puede ver las solicitudes abiertas (P-10). Es SECURITY DEFINER
-- para no depender de las politicas de usuarios (DEC-37, H-46).
create or replace function public.fn_soy_trabajador()
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists (
        select 1 from public.usuarios u
         where u.id = auth.uid() and u.rol = 'trabajador'
    );
$$;

-- Si auth.uid() es el trabajador dueno de ese servicio.
create or replace function public.fn_es_dueno_del_servicio(p_servicio_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists (
        select 1 from public.servicios s
         where s.id = p_servicio_id and s.perfil_id = auth.uid()
    );
$$;

-- ============================================================================
--  CATALOGOS  ·  lectura publica, escritura de nadie
--  Se administran desde la consola de Supabase (DEC-13).
-- ============================================================================

create policy "catalogo estados visible para todos"
    on public.estados for select to anon, authenticated using (true);

create policy "catalogo municipios visible para todos"
    on public.municipios for select to anon, authenticated using (true);

create policy "catalogo categorias visible para todos"
    on public.categorias for select to anon, authenticated using (true);

-- ============================================================================
--  USUARIOS
--  Cada quien ve lo suyo. Ademas se ve el perfil de cualquier trabajador,
--  porque el catalogo de trabajadores es publico dentro de la aplicacion, y
--  el de la contraparte con la que ya se esta tratando.
--
--  OJO, Y ESTA DECIDIDO ASI (DEC-19): la ficha de un trabajador se ve
--  ENTERA, con correo y telefono incluidos, para cualquier usuario con
--  sesion. No es un descuido: la vista de busqueda y el perfil publico
--  necesitan leer esa fila, y el trabajador publica su contacto a proposito,
--  que es a lo que vino. El de los CLIENTES si esta cerrado: su ficha solo la
--  ve el mismo y la contraparte con la que ya esta tratando.
-- ============================================================================

create policy "usuario ve su propia ficha"
    on public.usuarios for select to authenticated
    using (id = auth.uid());

create policy "usuario ve la ficha de cualquier trabajador"
    on public.usuarios for select to authenticated
    using (exists (select 1 from public.perfiles_trabajador p where p.usuario_id = usuarios.id));

create policy "usuario ve la ficha de su contraparte"
    on public.usuarios for select to authenticated
    using (public.fn_es_contraparte(id));

create policy "usuario edita su propia ficha"
    on public.usuarios for update to authenticated
    using (id = auth.uid())
    with check (id = auth.uid());

-- Sin politica de INSERT: la fila la crea el trigger tg_auth_usuario_creado
-- al registrarse. Nadie inserta usuarios a mano.
-- Sin politica de DELETE: la baja se hace borrando de auth.users, y el
-- ON DELETE CASCADE se encarga del resto.

-- ============================================================================
--  PERFIL DEL TRABAJADOR  ·  publico para leer, privado para escribir
-- ============================================================================

create policy "perfil de trabajador visible"
    on public.perfiles_trabajador for select to authenticated using (true);

create policy "trabajador crea su perfil"
    on public.perfiles_trabajador for insert to authenticated
    with check (usuario_id = auth.uid());

create policy "trabajador edita su perfil"
    on public.perfiles_trabajador for update to authenticated
    using (usuario_id = auth.uid())
    with check (usuario_id = auth.uid());

create policy "trabajador borra su perfil"
    on public.perfiles_trabajador for delete to authenticated
    using (usuario_id = auth.uid());

create policy "habilidades visibles"
    on public.perfil_habilidades for select to authenticated using (true);

create policy "trabajador administra sus habilidades"
    on public.perfil_habilidades for all to authenticated
    using (perfil_id = auth.uid())
    with check (perfil_id = auth.uid());

-- ============================================================================
--  SERVICIOS
-- ============================================================================

create policy "servicios visibles"
    on public.servicios for select to authenticated using (true);

create policy "trabajador administra sus servicios"
    on public.servicios for all to authenticated
    using (perfil_id = auth.uid())
    with check (perfil_id = auth.uid());

create policy "fotos de servicio visibles"
    on public.servicio_fotos for select to authenticated using (true);

create policy "trabajador administra las fotos de sus servicios"
    on public.servicio_fotos for all to authenticated
    using (public.fn_es_dueno_del_servicio(servicio_id))
    with check (public.fn_es_dueno_del_servicio(servicio_id));

-- ============================================================================
--  SOLICITUDES
--  El cliente ve las suyas. El trabajador ve las abiertas (P-10), mas
--  aquellas en las que participa. Nadie mas ve nada.
-- ============================================================================

create policy "cliente ve sus solicitudes"
    on public.solicitudes for select to authenticated
    using (cliente_id = auth.uid());

-- Solo los trabajadores leen las abiertas de otros (DEC-37, H-46): un cliente
-- no necesita ver lo que publican los demas clientes.
create policy "trabajador ve las solicitudes abiertas"
    on public.solicitudes for select to authenticated
    using (estatus = 'abierta' and public.fn_soy_trabajador());

create policy "trabajador ve las solicitudes en las que participa"
    on public.solicitudes for select to authenticated
    using (trabajador_id = auth.uid() or public.fn_me_postule_a(id));

create policy "cliente publica sus solicitudes"
    on public.solicitudes for insert to authenticated
    with check (cliente_id = auth.uid());

create policy "cliente edita sus solicitudes"
    on public.solicitudes for update to authenticated
    using (cliente_id = auth.uid())
    with check (cliente_id = auth.uid());

create policy "cliente borra sus solicitudes"
    on public.solicitudes for delete to authenticated
    using (cliente_id = auth.uid());

-- ============================================================================
--  POSTULACIONES
--  Las ve el trabajador que la mando y el cliente que publico la solicitud.
--  Un trabajador NO ve con quien mas compite.
-- ============================================================================

create policy "trabajador ve sus postulaciones"
    on public.postulaciones for select to authenticated
    using (trabajador_id = auth.uid());

create policy "cliente ve las postulaciones a sus solicitudes"
    on public.postulaciones for select to authenticated
    using (public.fn_es_mi_solicitud(solicitud_id));

create policy "trabajador se postula"
    on public.postulaciones for insert to authenticated
    with check (trabajador_id = auth.uid());

create policy "trabajador retira su postulacion"
    on public.postulaciones for update to authenticated
    using (trabajador_id = auth.uid())
    with check (trabajador_id = auth.uid());

create policy "trabajador elimina su postulacion"
    on public.postulaciones for delete to authenticated
    using (trabajador_id = auth.uid());

-- El cliente rechaza una postulacion suelta sin aceptar a nadie (P-19). Sin
-- esta politica, "rechazar" del contrato no tenia como ejecutarse: la unica
-- via era fn_aceptar_postulacion, que rechaza a los demas de paso.
create policy "cliente rechaza postulaciones a sus solicitudes"
    on public.postulaciones for update to authenticated
    using (public.fn_es_mi_solicitud(solicitud_id))
    with check (public.fn_es_mi_solicitud(solicitud_id));

-- Aceptar y rechazar NO se hace con un update suelto: son tres escrituras que
-- tienen que pasar juntas. Se hace con fn_aceptar_postulacion(), que valida
-- que quien llama sea el cliente dueno de la solicitud.
--
-- Eso es REGLA DEL CONTRATO, no de la base, y esta decidido asi (DEC-21). El
-- cliente puede llevar SU solicitud a 'asignada' con un update suelto y
-- ponerle el trabajador que quiera, sin postulacion de por medio: es su fila,
-- la politica de arriba se lo permite y el CHECK solo exige que el
-- trabajador_id no sea nulo. Cerrarlo pedia un trigger de transicion de
-- estado que hoy no cabe. El unico que puede hacerlo es el dueno de la
-- solicitud, asi que el dano se lo hace a si mismo. Un update suelto a
-- estatus desde la aplicacion es un bug de quien lo escriba.

-- ============================================================================
--  CHAT
-- ============================================================================

create policy "conversaciones propias"
    on public.conversaciones for select to authenticated
    using (cliente_id = auth.uid() or trabajador_id = auth.uid());

create policy "cliente abre conversacion"
    on public.conversaciones for insert to authenticated
    with check (cliente_id = auth.uid());

create policy "mensajes de conversaciones propias"
    on public.mensajes for select to authenticated
    using (public.fn_participa_en_conversacion(conversacion_id));

create policy "enviar mensaje a conversacion propia"
    on public.mensajes for insert to authenticated
    with check (emisor_id = auth.uid()
                and public.fn_participa_en_conversacion(conversacion_id));

-- Marcar como leido: solo los mensajes que recibiste, no los que mandaste.
create policy "marcar leidos los mensajes recibidos"
    on public.mensajes for update to authenticated
    using (emisor_id <> auth.uid()
           and public.fn_participa_en_conversacion(conversacion_id))
    with check (emisor_id <> auth.uid()
                and public.fn_participa_en_conversacion(conversacion_id));

-- ============================================================================
--  RESENAS  ·  publicas para leer, las escribe solo el cliente
--  Que la solicitud este cerrada y que el trabajador sea el asignado lo
--  verifica ademas el trigger tg_resenas_validar de 01_esquema.sql.
-- ============================================================================

create policy "resenas visibles"
    on public.resenas for select to authenticated using (true);

create policy "cliente deja su resena"
    on public.resenas for insert to authenticated
    with check (cliente_id = auth.uid());

-- Sin update ni delete: una resena no se edita ni se borra. Si hubiera que
-- moderar alguna, se hace desde la consola de Supabase (DEC-13).

-- ============================================================================
--  TABLAS DE IA
--
--  RLS activo y CERO politicas. Eso no es un olvido: es la forma de decir
--  "con la anon key aqui no entra nadie". Las escribe unicamente la Edge
--  Function con la service_role key, que se salta RLS por definicion.
-- ============================================================================

-- (sin politicas a proposito)

-- ============================================================================
--  PERMISOS DE EJECUCION DE LAS FUNCIONES
--
--  PostgreSQL le da EXECUTE a PUBLIC en cuanto se crea una funcion, y
--  postgrest publica como RPC toda funcion del esquema public que el rol de
--  quien llama pueda ejecutar. Las dos cosas juntas abren un agujero que las
--  politicas de arriba NO tapan, porque una funcion security definer se las
--  salta por definicion.
--
--  El caso grave es fn_ia_registrar_llamada: con la anon key, que cualquiera
--  saca del APK, se puede llamar por RPC y escribir en ia_consumo para
--  quemarle el tope diario a otro usuario con solo conocer su uuid. Que
--  ia_consumo tenga RLS activo y cero politicas no lo impide.
-- ============================================================================

-- El tope de IA lo registra la Edge Function y nadie mas.
revoke execute on function
    public.fn_ia_registrar_llamada(uuid, public.funcion_ia, integer)
from public, anon, authenticated;
grant execute on function
    public.fn_ia_registrar_llamada(uuid, public.funcion_ia, integer)
to service_role;

-- El perfil publico es publico DENTRO de la aplicacion, o sea: exige sesion.
-- Sin esto se lee sin iniciar sesion, con solo conocer el uuid, y trae el
-- telefono de contacto del trabajador.
revoke execute on function public.fn_perfil_publico_trabajador(uuid)
from public, anon;
grant execute on function public.fn_perfil_publico_trabajador(uuid)
to authenticated, service_role;

-- Las tres RPC de escritura ya validan auth.uid() por dentro, asi que a anon
-- le responderian con una excepcion. Igual se le quitan: una funcion que no
-- tiene nada que hacer sin sesion no tiene por que estar publicada.
revoke execute on function
    public.fn_aceptar_postulacion(uuid),
    public.fn_cerrar_solicitud(uuid),
    public.fn_abrir_conversacion(uuid, uuid)
from public, anon;
grant execute on function
    public.fn_aceptar_postulacion(uuid),
    public.fn_cerrar_solicitud(uuid),
    public.fn_abrir_conversacion(uuid, uuid)
to authenticated, service_role;

-- Los auxiliares de RLS no filtran nada (todo lo que hacen cuelga de
-- auth.uid()), pero tampoco tienen por que aparecer como endpoints.
--
-- OJO: a authenticated NO se le pueden quitar. Las politicas de arriba los
-- evaluan con los permisos de quien consulta, y sin EXECUTE toda la base
-- empieza a responder "permission denied for function".
revoke execute on function
    public.fn_es_contraparte(uuid),
    public.fn_participa_en_conversacion(uuid),
    public.fn_es_mi_solicitud(uuid),
    public.fn_me_postule_a(uuid),
    public.fn_es_dueno_del_servicio(uuid),
    public.fn_soy_trabajador()
from public, anon;
grant execute on function
    public.fn_es_contraparte(uuid),
    public.fn_participa_en_conversacion(uuid),
    public.fn_es_mi_solicitud(uuid),
    public.fn_me_postule_a(uuid),
    public.fn_es_dueno_del_servicio(uuid),
    public.fn_soy_trabajador()
to authenticated, service_role;

-- ============================================================================
--  TIEMPO REAL
--  El chat (P-16) se suscribe a los mensajes de su conversacion. Realtime
--  respeta las politicas de arriba: solo llegan los mensajes que el usuario
--  podria haber leido con un select.
-- ============================================================================

-- add table falla si la tabla ya esta en la publicacion, y este archivo se
-- vuelve a correr. Por eso el bloque.
do $$
begin
    alter publication supabase_realtime add table public.mensajes;
exception when duplicate_object then null;
end
$$;

do $$
begin
    alter publication supabase_realtime add table public.conversaciones;
exception when duplicate_object then null;
end
$$;

commit;
