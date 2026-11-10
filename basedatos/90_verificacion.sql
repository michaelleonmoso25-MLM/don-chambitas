-- ============================================================================
--  DON CHAMBITAS
--  90_verificacion.sql  ·  Comprobacion estructural. NO escribe nada.
-- ============================================================================
--  Se corre despues de 01, 02, 03 y 04. Es una sola consulta: pegala completa
--  en el SQL Editor de Supabase y devuelve 42 renglones.
--
--  Todo tiene que decir PASA. Cualquier renglon con ">>> FALLA" senala
--  exactamente que quedo mal y con que valor.
-- ============================================================================

select orden,
       prueba,
       coalesce(obtenido, '(nulo)') as obtenido,
       esperado,
       case when coalesce(obtenido, '(nulo)') = esperado then 'PASA' else '>>> FALLA' end as resultado
from (values

  -- ---- Inventario ---------------------------------------------------------
  ( 1, 'tablas en public',
       (select count(*)::text from pg_tables where schemaname = 'public'), '15'),
  ( 2, 'vistas en public',
       (select count(*)::text from pg_views where schemaname = 'public'), '2'),
  ( 3, 'tipos enum',
       (select count(*)::text from pg_type t join pg_namespace n on n.oid = t.typnamespace
         where n.nspname = 'public' and t.typtype = 'e'), '4'),
  ( 4, 'funciones en public',
       (select count(*)::text from pg_proc p join pg_namespace n on n.oid = p.pronamespace
         where n.nspname = 'public'), '18'),
  ( 5, 'triggers de negocio en public',
       (select count(*)::text from pg_trigger g
          join pg_class c on c.oid = g.tgrelid
          join pg_namespace n on n.oid = c.relnamespace
         where n.nspname = 'public' and not g.tgisinternal), '11'),
  ( 6, 'trigger sobre auth.users',
       (select count(*)::text from pg_trigger
         where tgname = 'tg_auth_usuario_creado' and not tgisinternal), '1'),
  ( 7, 'indices trigram para la busqueda',
       (select count(*)::text from pg_indexes
         where schemaname = 'public' and indexdef ilike '%gin_trgm_ops%'), '3'),

  -- ---- Semilla ------------------------------------------------------------
  ( 8, 'categorias sembradas',
       (select count(*)::text from public.categorias), '16'),
  ( 9, 'estados sembrados',
       (select count(*)::text from public.estados), '32'),
  (10, 'municipios sembrados',
       (select count(*)::text from public.municipios), '26'),

  -- ---- Enlace con Supabase Auth -------------------------------------------
  (11, 'usuarios.id apunta a auth.users',
       (select count(*)::text from pg_constraint
         where conrelid = 'public.usuarios'::regclass and contype = 'f'
           and confrelid = 'auth.users'::regclass), '1'),
  (12, 'no quedan columnas de backend propio',
       (select count(*)::text from information_schema.columns
         where table_schema = 'public' and table_name = 'usuarios'
           and column_name in ('contrasena_hash', 'auth_uid')), '0'),
  (13, 'tabla recuperaciones_contrasena eliminada',
       (select count(*)::text from pg_tables
         where schemaname = 'public' and tablename = 'recuperaciones_contrasena'), '0'),

  -- ---- Las cinco correcciones ---------------------------------------------
  (14, 'S-01 el CHECK ya no cubre cerrada',
       (select case when pg_get_constraintdef(oid) ilike '%cerrada%'
                    then 'cubre cerrada' else 'solo asignada' end
          from pg_constraint where conname = 'ck_solicitud_asignada_con_trabajador'),
       'solo asignada'),
  (15, 'S-01 trigger que libera solicitudes al dar de baja',
       (select count(*)::text from pg_trigger
         where tgname = 'tg_perfiles_liberar_solicitudes'), '1'),
  (16, 'S-02 llave primaria de ia_cache',
       (select string_agg(a.attname, ',' order by k.ord)
          from pg_constraint c
          cross join lateral unnest(c.conkey) with ordinality k(attnum, ord)
          join pg_attribute a on a.attrelid = c.conrelid and a.attnum = k.attnum
         where c.conrelid = 'public.ia_cache'::regclass and c.contype = 'p'),
       'funcion,entrada_hash'),
  (17, 'S-03 llaves foraneas compuestas de rol',
       (select count(*)::text from pg_constraint
         where contype = 'f' and confrelid = 'public.usuarios'::regclass
           and array_length(conkey, 1) = 2), '2'),
  (18, 'S-04 trigger que valida el emisor del mensaje',
       (select count(*)::text from pg_trigger where tgname = 'tg_mensajes_validar'), '1'),
  (19, 'S-05 columna estatus en solicitudes y postulaciones',
       (select count(*)::text from information_schema.columns
         where table_schema = 'public' and table_name in ('solicitudes', 'postulaciones')
           and column_name = 'estatus'), '2'),
  (20, 'S-05 ya no hay columna estado ambigua',
       (select count(*)::text from information_schema.columns
         where table_schema = 'public' and table_name in ('solicitudes', 'postulaciones')
           and column_name = 'estado'), '0'),

  -- ---- Seguridad a nivel de fila ------------------------------------------
  (21, 'tablas con RLS activo',
       (select count(*)::text from pg_tables
         where schemaname = 'public' and rowsecurity), '15'),
  (22, 'politicas en public',
       (select count(*)::text from pg_policies where schemaname = 'public'), '36'),
  (23, 'ia_consumo e ia_cache con RLS activo',
       (select count(*)::text from pg_tables
         where schemaname = 'public' and tablename in ('ia_consumo', 'ia_cache')
           and rowsecurity), '2'),
  (24, 'ia_consumo e ia_cache SIN politicas (a proposito)',
       (select count(*)::text from pg_policies
         where schemaname = 'public' and tablename in ('ia_consumo', 'ia_cache')), '0'),
  (25, 'anon y authenticated sin permisos sobre las tablas de IA',
       (select count(*)::text from information_schema.role_table_grants
         where table_schema = 'public' and table_name in ('ia_consumo', 'ia_cache')
           and grantee in ('anon', 'authenticated')), '0'),
  (26, 'vistas con security_invoker',
       (select count(*)::text from pg_class c join pg_namespace n on n.oid = c.relnamespace
         where n.nspname = 'public' and c.relkind = 'v'
           and array_to_string(c.reloptions, ',') ilike '%security_invoker%'), '2'),
  (27, 'catalogos legibles sin sesion',
       (select count(*)::text from pg_policies
         where schemaname = 'public'
           and tablename in ('estados', 'municipios', 'categorias')
           and 'anon' = any(roles)), '3'),

  -- ---- Almacenamiento y tiempo real ---------------------------------------
  (28, 'cubetas creadas',
       (select count(*)::text from storage.buckets where id in ('perfiles', 'servicios')), '2'),
  (29, 'cubetas publicas para lectura',
       (select count(*)::text from storage.buckets
         where id in ('perfiles', 'servicios') and public), '2'),
  (30, 'limite de 5 MB por archivo',
       (select count(*)::text from storage.buckets
         where id in ('perfiles', 'servicios') and file_size_limit = 5242880), '2'),
  (31, 'politicas de almacenamiento',
       (select count(*)::text from pg_policies
         where schemaname = 'storage' and tablename = 'objects'
           and policyname in (
               'fotos de perfil visibles para todos',
               'fotos de servicio visibles para todos',
               'usuario sube su foto de perfil',
               'usuario reemplaza su foto de perfil',
               'usuario borra su foto de perfil',
               'trabajador sube fotos de sus servicios',
               'trabajador reemplaza fotos de sus servicios',
               'trabajador borra fotos de sus servicios')), '8'),
  (32, 'tablas en la publicacion de Realtime',
       (select count(*)::text from pg_publication_tables
         where pubname = 'supabase_realtime' and schemaname = 'public'), '2'),

  -- ---- Funciones RPC ------------------------------------------------------
  (33, 'funciones RPC presentes',
       (select count(*)::text from pg_proc p join pg_namespace n on n.oid = p.pronamespace
         where n.nspname = 'public' and p.proname in (
               'fn_perfil_publico_trabajador', 'fn_aceptar_postulacion',
               'fn_cerrar_solicitud', 'fn_abrir_conversacion',
               'fn_ia_registrar_llamada')), '5'),
  (34, 'security definer con search_path fijo',
       (select count(*)::text from pg_proc p join pg_namespace n on n.oid = p.pronamespace
         where n.nspname = 'public' and p.prosecdef and p.proconfig is not null), '16'),
  (35, 'auxiliares que rompen la recursion de RLS',
       (select count(*)::text from pg_proc p join pg_namespace n on n.oid = p.pronamespace
         where n.nspname = 'public'
           and p.proname in ('fn_es_mi_solicitud', 'fn_me_postule_a')
           and p.prosecdef), '2'),
  (36, 'ninguna politica consulta la tabla del otro lado del ciclo',
       (select count(*)::text from pg_policies
         where schemaname = 'public'
           and ((tablename = 'solicitudes'  and qual ilike '%postulaciones%')
             or (tablename = 'postulaciones' and qual ilike '%solicitudes%'))), '0'),

  -- ---- Lo que 91 NO puede ver -------------------------------------------
  --
  --  Estas seis vigilan los arreglos que se hicieron justo antes de la
  --  primera ejecucion. Van aqui, y no en 91, por una razon concreta: 91
  --  corre como postgres, que se salta RLS y es dueno de todas las funciones,
  --  asi que para el TODO funciona siempre. Estas preguntan por el permiso y
  --  por la definicion, no por el resultado, y eso si se puede comprobar.
  (37, 'A-1 los 5 triggers que tocan filas ajenas son security definer',
       (select count(*)::text from pg_proc p join pg_namespace n on n.oid = p.pronamespace
         where n.nspname = 'public' and p.prosecdef
           and p.proname in ('fn_validar_postulacion', 'fn_validar_mensaje',
                             'fn_validar_resena', 'fn_tocar_conversacion',
                             'fn_liberar_solicitudes_del_trabajador')), '5'),
  (38, 'A-2 la vista de busqueda filtra por categoria y ordena por fecha',
       (select count(*)::text from information_schema.columns
         where table_schema = 'public' and table_name = 'vw_busqueda_trabajadores'
           and column_name in ('categorias', 'creado_en')), '2'),
  (39, 'A-3 con la anon key no se ejecuta el tope de IA ni el perfil publico',
       (select (has_function_privilege('anon',
                    'public.fn_ia_registrar_llamada(uuid, public.funcion_ia, integer)', 'execute')
             or has_function_privilege('anon',
                    'public.fn_perfil_publico_trabajador(uuid)', 'execute'))::text), 'false'),
  (40, 'A-3 la Edge Function si registra el tope, y la sesion si lee el perfil',
       (select (has_function_privilege('service_role',
                    'public.fn_ia_registrar_llamada(uuid, public.funcion_ia, integer)', 'execute')
            and has_function_privilege('authenticated',
                    'public.fn_perfil_publico_trabajador(uuid)', 'execute'))::text), 'true'),
  (41, 'A-4 nadie se cambia el correo ni el rol, pero si su nombre y telefono',
       (select (not has_column_privilege('authenticated', 'public.usuarios', 'correo', 'update')
            and not has_column_privilege('authenticated', 'public.usuarios', 'rol',    'update')
            and     has_column_privilege('authenticated', 'public.usuarios', 'nombre', 'update')
            and     has_column_privilege('authenticated', 'public.usuarios', 'telefono', 'update')
            and     has_column_privilege('authenticated', 'public.usuarios', 'foto_url', 'update'))::text), 'true'),
  (42, 'A-4 un mensaje recibido se marca leido, no se reescribe',
       (select (    has_column_privilege('authenticated', 'public.mensajes', 'leido_en',  'update')
            and not has_column_privilege('authenticated', 'public.mensajes', 'contenido', 'update'))::text), 'true'),

  -- S4-T16 (DEC-37): las reglas de las solicitudes viven en la base, no solo en la pantalla.
  (43, 'S4-T16 la vista de mis solicitudes cuenta las postulaciones sin revisar',
       (select count(*)::text from information_schema.columns
         where table_schema = 'public' and table_name = 'vw_mis_solicitudes'
           and column_name = 'postulaciones_sin_revisar'), '1'),
  (44, 'S4-T16 una solicitud nueva exige estado y municipio del mismo estado',
       (select count(*)::text from pg_trigger
         where tgname = 'tg_solicitudes_ubicacion' and not tgisinternal), '1'),
  (45, 'S4-T16 la descripcion de una solicitud tiene tope de 1000 caracteres',
       (select count(*)::text from pg_constraint where conname = 'ck_solicitud_descripcion'), '1'),
  (46, 'S4-T16 las solicitudes abiertas de otros solo las lee un trabajador',
       (select count(*)::text from pg_policies
         where schemaname = 'public' and tablename = 'solicitudes'
           and policyname = 'trabajador ve las solicitudes abiertas'
           and qual like '%fn_soy_trabajador%'), '1')

) as t(orden, prueba, obtenido, esperado)
order by orden;
