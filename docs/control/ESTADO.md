# Estado actual

> Archivo **vivo**. Quien termina una tarea lo actualiza. Es la primera cosa
> que lee el agente y la única fuente confiable sobre qué está pasando hoy.

**Última actualización:** 2026-11-16

---

## Seguridad y despliegue — confirmado por el líder el 2026-11-16

El líder confirma completado el bloque «Seguridad y despliegue (Supabase en
producción y aplicación firmada)» del Sprint 5 de `docs/SCRUM1.ods`.
En el índice técnico se marcan hechas `S6-T04`, `S6-T05` y `S6-T07`.
Es un registro del trabajo ya realizado, no una nueva verificación del agente.

La aplicación ya fue publicada y pasó la fase de testers, según el líder.
**Queda pendiente el AAB de la versión final y su lanzamiento como actualización**
(`S6-T11`); se reutiliza la firma existente. La optimización de rendimiento y
tamaño (`S6-T03`) conserva su estado pendiente. Esta actualización no cierra
las tareas de IA ni el resto del sprint.

## Sprint en curso

| Campo | Valor |
|---|---|
| Sprint | 4 |
| Fechas | Apertura: 2026-11-07; cierre previsto: 2026-11-10 |
| Tareas del sprint | 11 |
| Terminadas | 11 |
| En curso | 0 |
| Bloqueadas | 0 |

> **Sprint 4:** sus 11 tareas están hechas; `S4-T09` y `S4-T16` ya se
> integraron mediante #60 y #61. La apertura fue confirmada en `DEC-34`.
> Falta registrar el cierre formal y la apertura del Sprint 5 por el líder.
> S4-T10 pertenece al Sprint 5 (`DEC-44`) y tiene trabajo iniciado en #62;
> se conserva en curso, sin considerar abierto todo el sprint.
>
> Sprint 3 cerrado con sus 15 tareas en `hecha` (PR #46 a #50), abierto el 2026-10-18
> por autorización del líder. Sus tickets se
> redactan uno por uno al tomar cada tarea, como en el Sprint 2.
>
> Sprint 2 cerrado con sus 16 tareas en `hecha` (la última, `S2-T16`, con el
> #19). Sprint 1 cerrado el 2026-09-20 con sus 16 tareas en `hecha`.

## Qué se puede probar hoy en la aplicación

Léelo antes de instalar el APK y reportar que algo "no funciona". Desde que
`S2-T07` se integró en `main`, `RepositorioAuth` está enlazado a
`RepositorioAuthReal`: P-02, P-03 y P-04 hablan con Supabase Auth, y **las
cuentas sembradas de `FuenteDatosFalsa` ya no sirven para entrar**. Las filas
de P-04 describen lo que dejó `S2-T10`, integrada con el #13.

| Si haces esto | Pasa esto hoy | Lo arregla |
|---|---|---|
| Abres la aplicación **sin sesión guardada** | P-01 dura al menos 800 ms y te deja en P-02 | — |
| Abres la aplicación **con sesión guardada** | P-01 y luego P-05 o P-10 según tu rol, sin pasar por P-02. Atrás cierra la aplicación. Probado con `ana.rls@…` y `beto.rls@…` | — |
| Abres la aplicación con sesión guardada y **sin red** | A los 8 segundos, "Sin conexión" con "Reintentar" en P-01. La sesión no se borra, y con la red de vuelta "Reintentar" entra | — |
| Tocas la pestaña **Cuenta** | P-18 real: tu ficha de `public.usuarios`, editar datos, cambiar contraseña y cerrar sesión. El trabajador ve además "Mi perfil profesional" (P-11) y "Mis postulaciones" (P-14) | — |
| Editas nombre, apellidos o teléfono y guardas | La tarjeta cambia en ese momento y sigue así al reabrir. El teléfono es obligatorio, como en P-03 | — |
| Tocas el avatar de P-18 | Hoja con "Elegir de la galería" y "Tomar foto", y luego el recortador. Una imagen de más de 5 MB o que no sea JPG, PNG o WebP se rechaza con el motivo | — |
| Como trabajador, tocas "Mi perfil profesional" en P-18 | P-11 **guarda en Supabase** (`S3-T09`): estados y municipios salen de la base, "Perfil guardado" y el aviso de incompleto recalculado, y al reabrir la aplicación lo guardado sigue. Valida como P-03: cada campo al salir de él y todos al guardar, con el foco en el primer error. Probado con `beto.rls@…` y Puebla → Cholula | — |
| Como trabajador, tocas la pestaña **Servicios** | P-12 **lee de Supabase** tus servicios, activos primero, con la miniatura de la foto de la posición 1. La hoja de acciones edita, pausa o reactiva y elimina con confirmación, cada una con su aviso. Recarga al volver de P-13 | — |
| Como trabajador, publicas un servicio en P-13 | **Guarda en Supabase.** Las 16 categorías salen de la base, **todavía sin acentos** ("Plomeria"), hasta que el líder vuelva a correr la semilla (`S3-T02`). "Publicar" deja la pantalla en "Editar servicio"; las fotos suben a la cubeta `servicios` con un indicador que gira. **Sin haber guardado antes el perfil profesional, "Publicar" falla con el error genérico** (`H-27`). "Redactar con IA" **llena la descripción con IA real (Gemini)**, el mismo texto para los mismos datos (caché compartida, `DEC-45`), con tope de 10 llamadas al día y el cupo visible al abrir (`S4-T10` y `S4-T11`, en curso); con texto ya escrito pregunta "¿Reemplazar lo que escribiste?" | `DEC-40` (`H-27`), `S4-T10` |
| Pulsas "Usar foto" en el recortador | La foto se guarda en Supabase Storage, el avatar la muestra y sigue al reabrir. La anterior se borra de la cubeta. Sin red, "Sin conexión" | — |
| Cambias la contraseña en P-18 | Solo se pide la nueva (mínimo 8). Si repites la actual: "La contraseña nueva es igual a la actual" | — |
| Pulsas "Cerrar sesión" en P-18 y confirmas | Cierra la sesión real: vas a P-02, atrás cierra la aplicación y al reabrir sigues en P-02 | — |
| Pulsas "Cerrar sesión" **sin red** | "Sin conexión. Revisa tu conexión e intenta de nuevo." Sigues en P-18 y la sesión sigue guardada | — |
| Estás en P-02 (iniciar sesión) | La pantalla real: marca, los dos campos y los enlaces a P-03 y P-04 | — |
| Pulsas **Iniciar sesión** o **Crear cuenta** **sin las llaves** en `local.properties` | "Algo salió mal" con "Reintentar". No se cierra la aplicación | Poner `SUPABASE_URL` y `SUPABASE_ANON_KEY` |
| Pulsas **Iniciar sesión** con una cuenta real del proyecto | Entra a P-05 o P-10 según su rol en `public.usuarios`. Probado con las cuentas de `92_usuarios_prueba.sql`: `ana.rls@…` entra a P-05 y `beto.rls@…` a P-10 | — |
| Pulsas **Iniciar sesión** con credenciales malas | "Correo o contraseña incorrectos", sin "Reintentar". Probado contra Supabase | — |
| Pulsas **Iniciar sesión** sin red | "Sin conexión" con "Reintentar". Con la red de vuelta, "Reintentar" entra | — |
| Abres `mx.donchambitas.app://auth#error=…` (enlace vencido) | La aplicación abre en P-02 y no se cierra, en frío y con la aplicación abierta | — |
| Entras a P-03 desde el marcador de P-02 | La pantalla real de registro, con sus cinco campos y el selector de rol | — |
| Confirmas el registro **sin elegir rol** | Te reclama el rol y no hace nada más | — |
| Escribes un correo sin arroba y sales del campo | Sale "Ese correo no se ve bien, revísalo" debajo. Se borra en cuanto vuelves a escribir | — |
| Pulsas el botón de P-02 o P-03 con campos mal | Marca **todos** los que fallan, no avanza y deja el foco en el primero | — |
| Entras en P-02 con una contraseña de un carácter y una cuenta que existe | **Entra.** Al iniciar sesión solo se exige que no esté vacía (5.2); el mínimo de 8 es del registro | — |
| Confirmas el registro **con rol** | Crea la cuenta en Supabase Auth y la ficha en `public.usuarios`, y te manda a P-05 o P-10 | — |
| Te registras con un correo que ya existe | "Correo ya registrado / El correo ya está registrado, inicia sesión", sin "Reintentar" | — |
| Tocas "¿Olvidaste tu contraseña?" en P-02 | P-04 real: explicación, correo, "Enviar enlace" y "Volver a iniciar sesión", con una sola barra | — |
| Pides el enlace con cualquier correo bien escrito | "Revisa tu correo", sin repetir el correo, **exista o no la cuenta** y aunque su correo no pueda recibir | — |
| Pides el enlace sin red | "Sin conexión" con "Reintentar", y el correo sigue escrito | — |
| Esperas el correo en una cuenta que no es del equipo | **No llega.** El SMTP de fábrica de Supabase solo entrega a los miembros del equipo de la organización, y muy pocos por hora | `S6-T13` |
| Abres el enlace del correo | P-18 con la sección de contraseña abierta, con la aplicación abierta o en frío. Atrás cierra la aplicación. Con un correo real se probó el 2026-10-14, con la aplicación abierta. **Necesita `mx.donchambitas.app://auth` en "Redirect URLs"**; sin ella, el correo manda a `localhost:3000` | — |
| Con sesión de cliente, intentas abrir una pantalla de trabajador, o al revés | Regresas al inicio de tu rol. Ya no hay botones "Sin sesión / Cliente / Trabajador": las guardas y las barras leen la sesión real | — |
| Con sesión abierta, intentas abrir P-02, P-03 o P-04 desde el mapa | Regresas al inicio de tu rol | — |

**El alta ya es real** desde `S2-T07`: la cuenta vive en Supabase Auth y en
`public.usuarios`, y sobrevive al reinicio. Por `DEC-25` el registro deja
sesión abierta, con la confirmación por correo desactivada en la consola.

## Continuidad — PR #62 / S4-T10 (actualizada el 2026-11-16)

**S4-T10 sigue en curso, no terminada; implementación detenida para su entrega.**
Rama local: `docs/S4-T10-contrato-ia-proveedores`, basada en `origin/pr-62`.
Incluye los commits de Michael de #62 y las correcciones locales posteriores.
La entrega se prepara en un commit de esta rama el 2026-11-16; el líder hará
el push. La identidad Git ya está configurada; el impedimento del 2026-11-15
quedó resuelto. No usar la identidad de otro integrante.

**#62 sigue abierta y no integrada**, confirmado en GitHub el 2026-11-16.
Su origen es `michaelleonmoso25-MLM:feat/S4-T10-ia-gemini-edge-function`;
subir esta rama local al repositorio del líder no actualiza esa PR.

**Entrega acordada por el líder (`DEC-47`):** otra persona retoma esta rama y
trabaja **S4-T10 y después S4-T11**, sin nueva confirmación inicial ni selección
automática de otra tarea. Redactar los tickets faltantes y separar ramas y
commits. Las verificaciones de integración que requieran ambas se completan al
final, sin declarar ninguna tarea terminada antes de cumplir sus criterios.
No incluye S4-T14, tareas opcionales ni apertura general del Sprint 5.

La persona abre las PR y el líder integra. La PR que complete S4-T10 conserva
el trabajo de Michael y sustituye #62; cerrar #62 como reemplazada solo después
de verificar e integrar esa PR. No integrar #62 primero. Si S4-T11 se apila sobre
S4-T10, indicar la rama base y el orden de integración. El agente se detiene
tras ambas tareas. Las políticas DEC-45 y DEC-46 ya están aprobadas.

**Verificación del 2026-11-16:** `assembleDebug` y `testDebugUnitTest` correctos;
499 pruebas, 0 fallas, 0 errores. No se repitieron las pruebas del servidor ni
las de emulador de la sesión anterior.

### Lo confirmado

- El líder confirmó Gemini como proveedor activo y contrato independiente del
  proveedor. Se actualizaron IA.md, CONTRATOS-API.md y referencias; cambiar a
  otra API requiere adaptar el servidor, sin modificar Android. Solo Gemini
  está implementado. Modelos configurables mediante `IA_MODELOS`.
- Android llama a `ia-generar`. El despliegue inicial del líder se llamaba
  `hyper-worker`, por lo que la URL esperada devolvía 404. El líder desplegó
  después `ia-generar` en su proyecto y la app ya obtiene respuestas.
- `.env` y `local.properties` son locales e ignorados. Se copiaron únicamente
  URL y anon key del líder a `local.properties`, conservando `sdk.dir`.
  A petición del líder se quitó GEMINI_API_KEY de `.env`; se configura en
  Supabase Secrets, según `.env.ejemplo`. No copiarla al APK.
- `assembleDebug` y 499 pruebas unitarias pasaron; después se recompiló e
  instaló con las credenciales del líder. Emulador: `Don_Chambitas_Pruebas`.
  El líder inició sesión manualmente como trabajador.
- P-13 generó descripción para «Reparacion de muebles de madera» / Carpintería,
  mostrando 9 de 10 llamadas. P-11 generó presentación, mostró confirmación de
  reemplazo y 8 de 10; se eligió conservar el texto original.
- P-11 rechazó «Escribe un poema sobre el mar» con el aviso correspondiente.
  Esto prueba el comportamiento visible, no el HTTP ni que el rechazo no cobre.
- No se guardaron cambios al perfil ni se publicaron servicios; se descartó la
  entrada inválida y se comprobó el perfil original. La prueba sí consumió IA.

### Qué retomar: políticas decididas, implementación y pruebas pendientes

1. **Caché compartida (S4-T11, hecho, `DEC-45`):** hash sin usuario sobre la entrada
   normalizada, por función. Verificada contra el servidor con dos usuarios, espacios
   distintos y dos funciones. Las filas antiguas de la caché por usuario ya no se reutilizan.
2. **Límite diario (S4-T11, hecho):** `429` en la llamada 11; un acierto de caché responde
   `200` con el cupo en 0; con cupo 1 y 4 peticiones simultáneas, un `200` y tres `429`
   (candado por usuario en `fn_ia_registrar_llamada`).
3. **Cupo en errores (S4-T11, hecho, `DEC-46`):** `fn_ia_liberar_llamada` descuenta una sola
   vez la reserva de la petición. Fallo forzado: `503` sin consumo ni caché, también con 8
   simultáneas; una ráfaga real de 12 dejó el cupo igual a los éxitos. `fn_ia_restantes` y los
   tokens reales (`DEC-39`) también hechos; P-11 y P-13 consultan el cupo al abrir.
   **El SQL nuevo ya está aplicado en Supabase** (bloques `S4-T11` de `01` y `02`).
4. **Validación previa del título (S4-T10, hecho, 2026-11-16):** reglas
   deterministas más una consulta mínima al modelo, antes de caché y cupo.
   Contra el servidor desplegado, los casos 6, 7, 8 y 13 de RESULTADOS-GPT.md
   se comportan como se espera (6: presentación; 7, 8 y 13: `422` sin consumo).
5. **Errores Android (S4-T10, hecho):** `TipoError.IA_RECHAZADA` (`H-37`);
   422 y un 200 con `NO_PUEDO_REDACTAR` lo usan; 400 → `DESCONOCIDO`. 503
   pruebas unitarias, 0 fallas; P-11 y P-13 probados en emulador con la rama apilada.
6. **Prompt y parámetros (S4-T10, hecho):** mínimo 50 palabras, redacción neutra y
   sin frases hechas; temperatura 0.5 y 200 tokens como la tabla. Se quitó
   `thinkingConfig`: `gemini-flash-lite-latest` lo rechaza con 400. Casos 1 a 5 y
   9 a 12 con la API real: de 41 a 71 palabras; **dos quedan por debajo de 50**
   (caso 10 con 49, caso 11 con 41 por respetar el borrador).
7. **Cierre:** `S4-T10` y `S4-T11` siguen `en curso` hasta que el líder integre sus PR.
   Criterios verificados salvo: **50 palabras no está garantizado por el modelo** (49 y 41 en
   dos casos), sin conexión en emulador y la mezcla determinista de éxitos y fallos. Pendientes
   del líder: `IA.md` aún dice 40 a 80 palabras; decidir si basta el mínimo en el prompt; el
   validador del título hace una consulta extra al modelo sin cupo (cuota del proveedor).
   Orden de integración: primero `feat/S4-T10-ia-integracion`, después
   `feat/S4-T11-cache-compartida-y-cupo` (apilada sobre la primera).

Guía y evidencias de pruebas: `supabase/functions/README.md`. La llave se
configura exclusivamente en Supabase Secrets; no copiarla a `.env` ni al APK.

## Última tarea del Sprint 5 trabajada (2026-11-16)

**`S5-T01` — Diseño de postulación, contacto y reseñas**, `hecha`. Rama
`docs/S5-T01-diseno-contacto-postulacion-resenas`. Solo diseño:
`docs/producto/DISENO-POSTULACION-CONTACTO-RESENAS.md`. El líder indicó a la persona
(verbalmente) continuar con las tareas del chat, en este orden: `S5-T01`, `S5-T06`,
`S5-T05`, `S5-T07`. **Falta que el líder registre ese permiso**; el Sprint 5 no se abrió
formalmente. Hallazgos `H-49` a `H-55` pendientes de decisión (ver el documento, sección 10).

**`S5-T06` — Contrato de mensajería, postulaciones y reseñas**, `hecha`. Rama
`docs/S5-T06-contrato-mensajeria-postulaciones-resenas`, apilada sobre la de `S5-T01` (integrar esa
primero). Resuelve `H-50`, `H-51` y `H-54`; deja `H-56` a `H-58` para el líder. Solo documento.

## Tarea en curso

| Campo | Valor |
|---|---|
| ID | S4-T10 |
| Título | S4-T10 (integración de IA con Gemini) y S4-T11 (caché compartida y cupo) |
| Quién la tomó | Michael (implementación original); líder (revisión y entrega) |
| Rama | `feat/S4-T10-ia-integracion` (T11 apilada en `feat/S4-T11-cache-compartida-y-cupo`) |
| Desde | 2026-11-14; continuidad revisada el 2026-11-16 |
| Estado | Implementadas y verificadas contra el servidor y en emulador; en curso hasta que el líder integre las PR |

## Última tarea terminada

**`S4-T16` — Implementación real de las solicitudes en Supabase.** 2026-11-09. Rama
`feat/S4-T16-solicitudes-real-supabase`, pull request **#61**. Tomada por LMM
(sin responsable nominal). Ticket: `docs/tareas/S4-T16.md`. Sale de `main`, **sin los cambios de
`S4-T09`** (#60): las dos tocan `ModuloRepositorios.kt`, `ModelosRepositorio.kt`, los tres archivos
vivos y el contrato, así que quien integre la segunda resuelve un conflicto pequeño.

- `RepositorioSolicitudesReal`, ya enlazado en `ModuloRepositorios`: mis solicitudes (de la vista),
  abiertas, detalle con postulaciones embebidas, crear, actualizar, cancelar y cerrar. Cancelar y
  actualizar solo actúan sobre una `abierta`.
- **`DEC-37` en la base**, en `01_esquema.sql` y `02_politicas_rls.sql`: trigger de ubicación
  (`H-41`), tope de 1,000 en la descripción (`H-44`), las abiertas solo las leen los trabajadores
  con `fn_soy_trabajador()` (`H-46`) y `vw_mis_solicitudes` (`H-42`). Comprobaciones 43 a 46 en
  `90_verificacion.sql`.
- `Solicitud.postulacionesSinRevisar` y `TarjetaSolicitud` ("N postulaciones sin revisar").
- Compila; 463 pruebas unitarias, 0 fallas (15 nuevas); las instrumentadas compilan.
- **SQL aplicado a mano en Supabase el 2026-11-09 por LMM**, pegando solo lo nuevo: `01_esquema.sql`
  no se puede volver a correr en una base con datos.
- **Probado en emulador `Pixel_8` contra Supabase**, con `ana.rls@…` (cliente): P-08 publica
  "Fuga en el bano prueba S4T16" (Plomería, $450, Ciudad de México, Coyoacán), P-09 la muestra
  "Abierta" y **sigue ahí después de cerrar la aplicación por completo y reabrirla**. Esa solicitud
  de prueba quedó en la base; se borra con
  `delete from public.solicitudes where titulo like '%prueba S4T16%';`.
- **Lo que no se probó:** las reglas de la base con una petición que se salte la pantalla; que un
  cliente no vea las abiertas de otros y un trabajador sí; `obtenerAbiertas`, `obtenerDetalle`,
  `actualizar`, `cancelar` y `cerrar` (ninguna pantalla las usa todavía); `postulaciones_sin_revisar`
  con postulaciones reales (no hay `RepositorioPostulaciones` real hasta `S5`);
  `90_verificacion.sql` completo.

Las anteriores, con su detalle:

**`S4-T15` — Pruebas del módulo de búsqueda y solicitudes.** 2026-11-07. Rama
`feat/S4-T15-pruebas-busqueda-solicitudes`, pull request **#59**, integrado con
los #46 a #58 que tenía apilados. Ticket redactado por el agente al tomar la
tarea.

- **`FlujoClienteTest`**, 13 pruebas con los repositorios falsos sobre una sola
  fuente, sin `mockk`:
  - **Recorridos:** lo que publica P-08 lo lee P-09; sin presupuesto llega nulo;
    la solicitud de otro cliente no sale; P-06 combina texto, filtros, orden y
    páginas (53 trabajadores: 20, 20 y 13); los trabajadores de P-06 se abren
    en P-07 como cliente, y el propio como vista previa.
  - **Filas de error** con `RED`, `SERVIDOR`, `DESCONOCIDO`, `AUTENTICACION` y
    `VALIDACION` en P-06, P-08 y P-09, cada una con su recuperación.
- **`SolicitudNuevaPantallaTest`**, 10 pruebas de Compose de P-08, que era la
  única pantalla del módulo sin ellas.
- **`app/src/main` no cambió** y ninguna prueba encontró un defecto.
- **Límite:** con los falsos todo es consistente, así que estas pruebas no ven
  el desajuste de municipios ni que P-07 lea de Supabase lo que P-06 saca del
  falso. Es de `S4-T09`.
- Verificación:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 448 pruebas unitarias, 0 fallas. `FlujoClienteTest` pasó diez corridas
    seguidas con `--rerun`.
  - Las 10 instrumentadas de `SolicitudNuevaPantallaTest` pasan en el emulador.

Las anteriores, con su detalle completo, están al final de
`docs/control/BITACORA.md`, en "Detalle de las tareas terminadas".

## Cuentas de prueba en Supabase

Las de `92_usuarios_prueba.sql` (`S1-T03`), la de `S2-T07`
(`s2t07.trabajador@…`) y las dos de `S2-T15` (`cliente.s2t15@donchambitas.test`
y `trabajador.s2t15@donchambitas.test`) siguen en el proyecto. Borrarlas necesita la
`service_role`, que no entra al repositorio:

```sql
delete from auth.users
where email like '%@prueba.donchambitas.mx'
   or email like '%@donchambitas.test';
```

## Siguiente en la cola

**No quedan tareas pendientes del Sprint 4.** `S4-T09` y `S4-T16` ya están
hechas. No volver a seleccionarlas ni abrir el Sprint 5 automáticamente.

**Continuidad acordada (`DEC-47`):** entregar la rama de S4-T10 para que otra
persona complete S4-T10 y después S4-T11. Esta asignación prevalece sobre la
selección automática; detenerse tras ambas. La apertura general del Sprint 5
sigue correspondiendo al líder; su primera pendiente por prioridad es `S5-T01`.

**La IA se reanudó entera el 2026-11-09** (`DEC-33`, cierra `PEND-03`). `S3-T10`
se queda `hecha` con 12 de 13 casos (`DEC-35`). Lo que `S4-T10` y `S4-T11` tienen
que aplicar está en `DEC-39`. **Toda la IA es del Sprint 5** (`DEC-44`).

**Antes conviene que el líder vuelva a correr `04_datos_semilla.sql`**: con
los catálogos reales, la app ya muestra las categorías sin acentos.

## Huecos: todos decididos el 2026-11-09

El líder decidió los huecos abiertos en `DEC-35` a `DEC-43`. Cada uno quedó
marcado `CERRADO` donde se reportó. **Decidido no es aplicado**: esta tabla dice
quién hace cada cosa.

| Huecos | Decisión | Lo aplica |
|---|---|---|
| `H-05`, `H-40`, `H-25`, `H-48`, `H-43` | Búsqueda sin acentos, ubicación nula y tarjeta con precio y "No disponible" (`DEC-36`) | `S4-T09` |
| `H-07`, `H-41`, `H-42`, `H-44`, `H-46`, `H-47` | Reglas de solicitudes en la base y `vw_mis_solicitudes` (`DEC-37`) | `S4-T16` |
| `H-13` | `S4-T16` hace real a `RepositorioSolicitudes` (`DEC-38`); `S5-T13`, postulaciones y reseñas (`DEC-43`) | `S4-T16`, `S5-T13` |
| `H-23`, `H-35`, `H-36`, `H-32`, `H-34` | Tope consultable, entrada de 1,000 y prompt neutro (`DEC-39`) | `S4-T10`, `S4-T11` |
| `H-37` | `TipoError` nuevo para el rechazo de la IA (`DEC-39`) | `S4-T10` (`DEC-43`) |
| `H-29` | F1 solo en P-11 y P-13 (`DEC-39`) | Ya aplicado en `IA.md` y `PANTALLAS.md` |
| `H-27`, `H-28`, `H-17` | P-12 pide el perfil antes de publicar, `ErroresAuth` sin token y fotos de servicio validadas antes de subir (`DEC-40`) | `S4-T14` (`DEC-43`) |
| `H-24` | P-14 la construye `S5-T02` (`DEC-41`) | `S5-T02` |
| `H-33` | `S3-T10` cierra con 12 de 13 (`DEC-35`) | — |
| `H-08`, `H-14`, `H-22`, `H-26`, `H-38` | Se quedan como están (`DEC-42`) | — |

Todo tiene tarea desde `DEC-43`. Los tickets de `S4-T10`, `S4-T14`, `S5-T06` y
`S5-T13` tienen que nombrar lo que les tocó aquí.

## Los dos huecos de S2-T01, ya cerrados

Salían de `S2-T01`, de cruzar HU-01 y HU-04 contra `CONTRATOS-API.md`. Están
explicados al final de `docs/producto/DISENO-AUTENTICACION.md`. **Al 2026-09-22
no queda ninguno abierto.**

- **`H-09` — CERRADO el 2026-09-22 por `DEC-27`.** El enlace de recuperación
  abre la aplicación por *deep link*, canjea el token por sesión y aterriza en
  **P-18**, que ya cambia la contraseña por HU-05. **No se agrega pantalla:
  siguen siendo 19 y `DEC-10` queda intacto.** Al investigarlo se cayó la
  premisa del hallazgo: la "página alojada de Supabase" no existe —Supabase no
  hospeda formulario de contraseña nueva, la pantalla la pone uno— y una web
  propia la descarta `DEC-02`. Lo recoge `S2-T06`, lo implementa `S2-T07` y
  `S2-T11` especifica la llegada a P-18.
- **`H-10` — CERRADO el 2026-09-22 por `DEC-25`.** El registro deja sesión
  abierta: el usuario entra directo a la pantalla de su rol y la sesión vive
  hasta que él la cierre. Dos consecuencias que `S2-T06` y `S2-T07` tienen que
  respetar: la confirmación por correo de Supabase Auth **queda desactivada**,
  porque con ella activa `signUp` no abre sesión; y `registrar` pasa a devolver
  `Sesion` en vez de `Usuario`, así que `CONTRATOS-API.md` y `RepositorioAuth`
  cambian en `S2-T06`.


## Decisiones recientes

**2026-11-09 · El líder decidió todos los huecos abiertos: `DEC-33` a
`DEC-42`.** Los registró el agente por instrucción suya. Reanuda la IA entera
(`PEND-03`), confirma el Sprint 4, cierra `S3-T10` con 12 de 13 casos, agrega
`S4-T16` para las solicitudes reales y amplía `S5-T02` con P-14. El resumen,
en "Huecos: todos decididos el 2026-11-09". Ese mismo día `DEC-43` repartió lo
que había quedado sin tarea y agregó `S5-T13`, y `DEC-44` pasó la IA al
Sprint 5 para cerrar el 4 con `S4-T09` y `S4-T16`.

**2026-09-27 · Se integra la versión de `S2-T05` a `S2-T07` hecha hoy, no la de
Ricardo5690.** Ricardo5690 implementó las mismas tres tareas entre el 23 y el
24 de septiembre, en `feat/S2-T05-viewmodels-estados-ui-autenticacion`,
`docs/S2-T06-contrato-api-autenticacion` y `feat/S2-T07-autenticacion-supabase`.
Nunca llegaron a `main`, y por eso no aparecían aquí. El líder eligió la
versión de hoy, que se sube con los nombres
`feat/S2-T05-viewmodels-autenticacion`,
`docs/S2-T06-contrato-api-autenticacion-v2` y
`feat/S2-T07-autenticacion-supabase-v2`. Las razones:
- parte del `main` actual, con `S2-T04`;
- la sesión persiste;
- está probada contra Supabase;
- ya aplica `DEC-28` a `DEC-31`.

**Las ramas de Ricardo no se borran.** Conviene revisar con él si algo se
rescata, por ejemplo su prueba del repositorio o la llegada a P-18, que le
sirve a `S2-T11`. Es la segunda vez que una tarea se hace dos veces, después
de `S2-T04` (#10 y #11): una tarea tomada en una rama sin PR no se ve desde
`main`.

**2026-09-27 · Dos decisiones más: `DEC-30` y `DEC-31`.** `DEC-30` cierra
`H-12`: `S2-T08` cifra la sesión que guarda `supabase-kt` y quita
`Sesion.tokenAcceso`. `DEC-31` resuelve el riesgo de P-18: `S2-T12` trae
`RepositorioUsuario` real, `S2-T11` pasa a depender de `S2-T12`, y AGENTS.md
§6 pasa a seis tareas reales. De ahí salió un hallazgo nuevo, `H-13`:
solicitudes, postulaciones y reseñas no tienen tarea real. Está al final de
`CONTRATOS-API.md`.

**2026-09-27 · Dos decisiones nuevas: `DEC-28` y `DEC-29`.** Las tomó el líder
y las registró el agente por instrucción suya. `DEC-28` cierra `H-11`: el correo
duplicado es `TipoError.CORREO_DUPLICADO`, y P-03 deja de pintar mensajes tal
cual. `DEC-29` limita la elección de la siguiente tarea al sprint en curso y
a los anteriores.

**2026-09-16 · La base se volvió a levantar y a verificar.** `01` a `04` sin
error, `90_verificacion.sql` **42 de 42** y `91_prueba_funcional.sql`
**25 de 25**, ya con las pruebas 10, 12 y 13 corriendo con RLS activo. Las tres
pasaron por la razón correcta.

**2026-09-16 · `S1-T03` terminada.** Diagrama, cruce de las 33 historias, las
tres pruebas de `91` corriendo por fin con RLS activo, y el paso 2c cerrado con
la `anon key` contra PostgREST. Salieron cuatro huecos —`H-05` a `H-08`— y
ninguno se corrigió: son del líder. `H-08` toca un criterio de aceptación de
este mismo ticket.

**2026-09-15 · Dos decisiones nuevas: `DEC-23` y `DEC-24`.** Salen de los
hallazgos que dejó `S1-T02`. La primera amplía los filtros de búsqueda de tres
a cinco —se agregan estado y municipio— y con eso cambia `PRODUCTO.md`, el
renglón de P-06 y el título de `S4-T05`. La segunda pone por escrito la regla
de unicidad del hilo de chat que el esquema ya aplicaba, y que HU-24 no
describía para el caso de P-07.

Los otros dos hallazgos no necesitaron número: H-02 era la descripción de P-06,
que había quedado corta frente a HU-18, y H-03 se cerró aceptando que las
notificaciones locales sigan cubiertas por los criterios de HU-22 y HU-26 en
vez de tener historia propia. Los cuatro están explicados al final de
`PANTALLAS.md`.

**2026-09-15 · La base de datos está levantada y verificada.** Reinstalación
desde cero contra el proyecto de Supabase: `00_reinicio.sql` dejó todo en cero
—las once comprobaciones en `OK`, cubetas incluidas— y `01` a `04` corrieron sin
un solo error. `90_verificacion.sql` dio **42 de 42** y `91_prueba_funcional.sql`
**25 de 25**.

Falta lo que ningún script puede hacer solo, y es lo que cierra `S1-T03`:
probar RLS con la `anon key` y dos sesiones reales, mover las pruebas 10, 12 y
13 de `91` al bloque de `set role authenticated` (hoy corren como `postgres` y
pasan siempre), el diagrama ER y el cruce contra las historias de `S1-T01`.

**2026-09-15 · Revisión de `basedatos/` antes de volver a levantar el esquema.**
Se encontraron cinco defectos que `91_prueba_funcional.sql` **no detecta**,
porque corre como `postgres` y se salta RLS: cinco funciones de trigger sin
`security definer` que dejaban sus reglas sin aplicar en silencio, la vista de
búsqueda sin con qué filtrar por categoría, `fn_ia_registrar_llamada`
ejecutable con la `anon key`, permisos de `update` más anchos que el contrato,
y el semillero de categorías sin poder reconciliar los iconos de S1-T08. Los
cinco están corregidos en los archivos; `90_verificacion.sql` pasó de 36 a 42
comprobaciones y las seis nuevas son las que vigilan justo eso.

`00_reinicio.sql` ahora borra también los archivos y las dos cubetas de
Storage: el reinicio es total.

**2026-09-15 · Tres decisiones tomadas: `DEC-19`, `DEC-20` y `DEC-21`.** El
correo del trabajador es visible para cualquier usuario con sesión, el chat lo
abre siempre el cliente, y la atomicidad al aceptar una postulación es regla
del contrato y no de la base. Las tres están explicadas en `MODELO-ER.md`, en
"Lo que las políticas NO impiden, a propósito". `DEC-22` solo pone número a
algo que ya estaba decidido: los roles no se cambian.

**2026-09-14 · PEND-01 resuelto: el backend es Supabase.** Ver `DEC-16` y
`DEC-17`. Las seis tareas que estaban `bloqueada` pasaron a `pendiente` y ya no
queda ninguna tarea bloqueada en toda la cola. El cliente es `supabase-kt`:
Retrofit y OkHttp salieron del stack.

Esto **no** cambia cómo se trabaja hoy. La implementación activa sigue siendo
`FuenteDatosFalsa` hasta que cada tarea real llegue en su turno.

---

## Cómo se llena

Al **tomar** una tarea: llena "Tarea en curso" y pon la tarea en `en curso`
dentro de `docs/tareas/INDICE.md`.

Al **terminarla**: mueve la tarea a "Última tarea terminada", vacía "Tarea en
curso", recalcula el contador del sprint y actualiza "Siguiente en la cola".
La que ocupaba "Última tarea terminada" pasa, tal cual, al principio de
"Detalle de las tareas terminadas" en `BITACORA.md`: aquí solo vive una.

Si dos personas van a trabajar el mismo día, la segunda revisa este archivo
**antes** de pedirle nada al agente. Si dice que hay una tarea en curso, esa
tarea no se toca.

Verificación local del 2026-11-15: `assembleDebug` y `testDebugUnitTest`
correctos; 499 pruebas, 0 fallas, 0 errores. En esa primera comprobación aún
no había dispositivo. La prueba posterior en emulador se resume arriba.

Reprueba de IA del 2026-11-15: después de que el líder desplegó `ia-generar`
(el despliegue inicial tenía nombre `hyper-worker`), P-11 y P-13 generan texto
en el emulador. P-11 confirma antes de reemplazar y rechaza una petición de poema.
Se conservaron los datos guardados y no se publicó el servicio de prueba.
Detalle y límites de la verificación en `supabase/functions/README.md`.
