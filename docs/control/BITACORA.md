# Bitácora

> Archivo **vivo**. Quien termina una tarea agrega su renglón al final, y ese
> renglón queda a su cargo: cuando abre el pull request vuelve y le pone el
> número. Los renglones de otros no se tocan y ninguno se borra.
>
> Sirve para dos cosas: que nadie rehaga algo ya hecho, y que el avance del
> proyecto sea auditable de un vistazo.

| Fecha | Tarea | Quién | Rama | Pull request | Nota |
|---|---|---|---|---|---|
| 2026-09-15 | S1-T01 | GRI | docs/S1-T01-analisis-requerimientos | #1 | 33 historias, 9 módulos, 93 criterios. Las 19 pantallas cubiertas |
| 2026-09-15 | S1-T02 | BCJL | docs/S1-T02-alcance-mvp-pantallas | #2 | Cruce limpio: 19/19 pantallas con historia, 33/33 historias con pantalla, 0 choques con FUERA del MVP. Cuatro hallazgos para el líder al final de PANTALLAS.md; H-01 conviene cerrarlo antes de S4-T05 |
| 2026-09-16 | S1-T03 | LMM | docs/S1-T03-validacion-modelo-er | #5, #6 | 90 dio 42/42, 91 25/25 y el paso 2c 10/10 con la anon key contra PostgREST. Diagrama ER y cruce de las 33 historias contra las tablas. Cuatro huecos para el líder, H-05 a H-08; H-08 toca un criterio de aceptación de este mismo ticket |
| 2026-09-20 | S1-T04 | RRC | feat/S1-T04-configuracion-proyecto-android | #8 | Proyecto configurado en mx.donchambitas.app con AGP 9.2.1, Kotlin 2.2.10, Hilt 2.60.1 con KSP 2.2.10-2.0.2 y supabase-kt 3.1.1 en libs.versions.toml. Compila, pasa pruebas unitarias y corre en emulador Pixel 8 Pro |
| 2026-09-20 | S1-T05 | GRI | chore/S1-T05-configuracion-repositorio-github | #8 | Repositorio documentado (aztemik/don-chambitas), CODEOWNERS asigna al líder (@aztemik), plantilla de PR en .github/pull_request_template.md, gitignore unificado y reglas de protección para main |
| 2026-09-20 | S1-T06 | BCJL | feat/S1-T06-arquitectura-capas | #8 | Estructura de paquetes completa bajo mx.donchambitas.app (.gitkeep), Resultado<T> y TipoError en util/, ModuloRepositorios y ModuloSupabase anotados con Hilt en di/. Pruebas unitarias pasando |
| 2026-09-20 | S1-T07 | LMM | feat/S1-T07-modelado-entidades | #8 | Modelado completo de entidades en Kotlin (data classes y 4 enums) en dominio/modelo, sin serializacion ni dependencias de framework. Diccionario de datos documentado en docs/tecnico/DICCIONARIO-DATOS.md. Compila y pasa pruebas unitarias |
| 2026-09-20 | S1-T08 | RRC | feat/S1-T08-identidad-visual | #8 | Identidad visual cerrada: contrastes WCAG documentados (Carbon/Mostaza 7.09:1 pasa AAA), logo SVG a 48 dp, muestra-paleta.png, 16 categorias mapeadas a Material Icons y actualizadas en semillero SQL, icono adaptativo y mipmaps en todas las densidades verificados en emulador |
| 2026-09-20 | S1-T09 | GRI | feat/S1-T09-sistema-diseno | #8 | Sistema de diseno Compose implementado: paleta Taller con onPrimary en Carbon, escala tipografica de 6 niveles, escala base 4 en Espaciado, Formas y MainActivity envuelta en DonChambitasTema. Pruebas unitarias pasando |
| 2026-09-20 | S1-T10 | BCJL | feat/S1-T10-componentes-base | #8 | Los 14 componentes base reutilizables implementados con @Preview segun DISENO.md (Botones, Campos, Tarjetas, Chips, Estrellas, Barras). Cero colores literales, modifier al final, contrastes WCAG verificados y pruebas unitarias pasando |
| 2026-09-20 | S1-T11 | LMM | feat/S1-T11-componentes-estado | #8 | Componentes de estado (Cargando, EstadoVacio, EstadoError) y patron ContenedorEstado con orden de precedencia estricto. Mensajes de accion por TipoError, cadenas en strings.xml y previews para todos los estados. 8 pruebas unitarias pasando |
| 2026-09-20 | S1-T12 | RRC | feat/S1-T12-navegacion-compose | #8 | Grafo completo con 3 subgrafos y 19 pantallas en Rutas.kt sin cadenas sueltas. Barras inferiores de 4 destinos para Cliente y Trabajador, boton flotante en P-05 hacia P-08, guardas reactivas por rol/sesion y 8 pantallas superiores sin barra. 9 pruebas unitarias pasando y verificado en emulador |
| 2026-09-20 | S1-T13 | GRI | feat/S1-T13-repositorios-falsos | #8 | 10 interfaces de repositorio en dominio/repositorio, modelos de soporte, FuenteDatosFalsa en memoria con 8 trabajadores, 5 solicitudes, 3 chats, catalogos de datos semilla, 10 falsas con retraso de 300 ms y error forzado, enlazadas en ModuloRepositorios. 10 pruebas unitarias pasando y verificado en emulador |
| 2026-09-20 | S1-T14 | BCJL | docs/S1-T14-wireframes-pantallas | #8 | 11 wireframes generados a 360x800 dp con paleta Taller y anotaciones exactas de componentes de DISENO.md (P-01, P-02, P-03, P-04, P-05, P-10, P-18 más estados vacío y error de P-05 y P-10). Documento maestro WIREFRAMES.md con índice y comportamiento de cada elemento tocable |
| 2026-09-20 | S1-T15 | LMM | feat/S1-T15-pantalla-splash | #8 | Implementación de P-01 Splash: logo Don Chambitas con isotipo de casco, nombre y lema sobre fondo Crema, verificación con 800 ms mínimos hacia P-02, P-05 o P-10, salida de la pila de navegación con botón Atrás y eliminación de destello blanco en arranque de Activity. 7 pruebas unitarias nuevas |
| 2026-09-20 | S1-T16 | RRC | test/S1-T16-estrategia-pruebas | #8 | Infraestructura de pruebas lista: ReglaCorrutinas para ViewModels, DatosPrueba para entidades principales, 4 pruebas instrumentadas en ComponentesTest (Compose UI), PRUEBAS.md y cobertura JaCoCo configurada en Gradle. 57 pruebas unitarias y 5 instrumentadas pasando |
| 2026-09-21 | S2-T01 | BCJL | docs/S2-T01-diseno-pantallas-autenticacion | #8 | DISENO-AUTENTICACION.md especifica P-02, P-03 y P-04: anatomia con medidas, contratos de Estado y eventos, matriz de resultados de RepositorioAuth, validaciones con la misma regex del esquema y 39 claves de strings.xml. Trabajada SIN ticket por autorizacion del lider: los del Sprint 2 no estan escritos. Dos hallazgos nuevos, H-09 y H-10, sin resolver |
| 2026-09-21 | S2-T02 | LMM | docs/S2-T01-diseno-pantallas-autenticacion | #8 | P-03 implementada segun DISENO-AUTENTICACION.md: EstadoRegistro con errores por recurso, LimitesRegistro con los topes del esquema, selector de rol sin preseleccion (DEC-22), telefono filtrado a 10 digitos y estado conservado al girar. Conectada al grafo sustituyendo el marcador. 11 pruebas instrumentadas nuevas, 16 en total pasando. Trabajada SIN ticket, misma autorizacion que S2-T01. Validaciones de formato pendientes en S2-T04 y ViewModel en S2-T05. Comparte rama con S2-T01: un solo pull request para las dos, desvio de la regla de rama por tarea |
| 2026-09-22 | S2-T03 | RRC | feat/S2-T03-pantalla-inicio-sesion |  | P-02 implementada segun DISENO-AUTENTICACION.md: EstadoIniciarSesion con errores por recurso y destino de un solo uso, sin flecha de regreso por ser raiz del subgrafo, correo normalizado a minusculas solo al enviar y mensaje unico para credenciales rechazadas. Primera tarea del Sprint 2 con ticket. 13 pruebas instrumentadas nuevas, 27 en total. **Se corrigio el andamio de S1-T12**: pintaba barra superior en toda ruta, asi que P-01, P-02 y P-03 salian con dos barras encimadas; P-03 ya estaba asi en main |
| 2026-09-23 | S2-T04 | GRI | feat/S2-T04-validaciones-formularios | #11 | Las 12 reglas de la tabla 5.2 en dominio/validacion/ValidacionesAuth.kt, Kotlin puro y con la misma expresion del correo que el esquema. P-02 y P-03 validan al salir de un campo ya escrito y al enviar, marcan todos los fallos y llevan el foco al primero. **Desvio de la seccion 5:** las reglas devuelven FalloValidacion y la ui lo traduce a R.string, porque el dominio no puede conocer R. LimitesRegistro pasa al dominio. 29 pruebas unitarias nuevas; 31 instrumentadas de P-02 y P-03 pasando. **Hallazgo:** 2 pruebas de SplashViewModelTest fallan tambien sin esta tarea (carrera entre el init del ViewModel y la prueba) |
| 2026-09-27 | S2-T05 | BCJL | feat/S2-T05-viewmodels-autenticacion | #12 | IniciarSesionViewModel y RegistroViewModel con @HiltViewModel contra RepositorioAuth: la validacion de S2-T04 se muda sin cambiar, el correo se normaliza al enviar y el destino sale del rol del usuario. P-02 y P-03 ya entran y registran contra RepositorioAuthFalso. Ticket redactado por el agente. **Desvios:** el ViewModel de P-04 (4.3) queda para S2-T10 porque sin pantalla seria codigo muerto; el registro exitoso entra directo sin la rama 'Exito sin sesion' de 3.5, que DEC-25 dejo sin objeto; perder el foco es un evento con el enum de campos. 25 pruebas unitarias nuevas, 111 en total; 39 instrumentadas pasando |
| 2026-09-27 | S2-T06 | LMM | docs/S2-T06-contrato-api-autenticacion-v2 | #13 | Contrato de detalle de RepositorioAuth en CONTRATOS-API.md, verificado contra el codigo fuente de supabase-kt 3.0.3 (la version de libs.versions.toml; la fila de S1-T04 dice 3.1.1). Enlace de la app: mx.donchambitas.app://auth con flujo IMPLICIT, porque PKCE pierde el type=recovery. registrar devuelve Sesion (DEC-25). **Rama apilada sobre la de S2-T05** por decision del lider. **Dos hallazgos nuevos:** H-11, de donde sale el mensaje en espanol del correo duplicado (bloquea en la practica el alta real de S2-T07), y H-12, Sesion.tokenAcceso sobra |
| 2026-09-27 | S2-T07 | RRC | feat/S2-T07-autenticacion-supabase-v2 | #14 | RepositorioAuthReal contra Supabase Auth segun CONTRATOS-API.md, enlazado en ModuloRepositorios; ModuloSupabase con URL y anon key. TipoError.CORREO_DUPLICADO por H-11 opcion 1, y P-03 pierde mensajePantalla. **Desvio:** no usa handleDeeplinks de supabase-kt 3.0.3 (sin red cerraba la app); EnlaceAuth.kt hace lo mismo dentro de un try. Probado contra el proyecto: alta con fila correcta en public.usuarios, entrada tras reiniciar, correo duplicado, credenciales malas, sin red y enlace vencido. El lider desactivo la confirmacion por correo en la consola. Rama apilada sobre la de S2-T06. 136 unitarias y 39 instrumentadas pasando |
| 2026-10-08 | S2-T10 | LMM | feat/S2-T10-pantalla-recuperar-contrasena | #13 | P-04 con formulario y confirmacion contra RepositorioAuth real, sin repetir el correo. Vigencia confirmada por el lider en la consola: 3600 s, la cadena dice 1 hora. **Correccion fuera del ticket, por instruccion del lider:** email_address_invalid y email_address_not_authorized en recuperarContrasena delataban que cuentas existen (400 contra 200); ahora se reportan como exito. Probado contra Supabase: cuenta sin correo entregable, cuenta inexistente, sin red y un enlace real recibido y abierto en el emulador. **Hallazgo:** el SMTP de fabrica solo entrega a miembros del equipo. 150 unitarias y 49 instrumentadas pasando |
| 2026-10-08 | S2-T08 | GRI | feat/S2-T08-almacenamiento-seguro-sesion |  | La sesion de Supabase Auth ya no se guarda en texto plano: AlmacenSesionCifrada sustituye al SettingsSessionManager, con AES-256-GCM y llave del Android Keystore sobre DataStore (opcion A del ticket, sin dependencia nueva, eleccion del lider). Migra la sesion en claro existente y la borra; sesion y SharedPreferences fuera del respaldo de Google. Sesion pierde tokenAcceso (H-12). **Desvios:** la migracion ocurre con la primera accion de Auth y no al abrir, porque el cliente es perezoso y P-01 aun no lee la sesion (S2-T09); se tocaron FuenteDatosFalsa y RepositorioAuthFalso; se agrego manejador de corrupcion al DataStore. Probado en emulador: migracion, recarga tras reinicio, inicio de sesion nuevo cifrado y archivo danado sin cierre. 159 unitarias y 54 instrumentadas pasando |
| 2026-10-11 | S2-T09 | BCJL | feat/S2-T09-manejo-sesion | #15 | P-01 lee la sesion real (sesionActual) y lleva a P-05 o P-10; SesionViewModel saca a P-02 con la pila limpia cuando la sesion termina, y el marcador de P-18 cierra la sesion real. Opcion A en las dos decisiones. Probado en Pixel_10 contra Supabase el 2026-10-11. **No se probo en el dispositivo la expiracion:** queda en SesionViewModelTest, como preve el ticket. **Desvio:** SesionViewModel avisa en cada null y GrafoNavegacion decide con el marcador. mailer_autoconfirm estaba en false, contra DEC-25; el lider lo corrigio el mismo dia y se verifico en true. El marcador temporal sigue hasta S2-T15. 169 unitarias y 54 instrumentadas pasando |
| 2026-10-11 | S2-T12 | GRI | feat/S2-T12-repositorio-usuario-real | #16 | Contrato de perfil de usuario a detalle y RepositorioUsuarioReal enlazado (DEC-31): leer y editar la ficha propia en public.usuarios. Opcion A en las tres decisiones: subirFotoPerfil devuelve error hasta S2-T14, sesionActual() no se entera de la edicion, y el repositorio se prueba con una prueba instrumentada contra Supabase que se omite sin llaves. Los errores de PostgREST se clasifican por codigo HTTP porque supabase-kt 3.0.3 no deja pasar el de Postgres. **Rama apilada sobre la de S2-T09.** **Hallazgos para el lider:** H-13, el mensaje de los errores de Auth lleva el token; H-14, public.usuarios acepta un nombre vacio. 180 unitarias y 59 instrumentadas pasando |
| 2026-10-11 | S2-T11 | RRC | feat/S2-T11-perfil-usuario |  | P-18 real: ficha de public.usuarios, edicion de nombre, apellidos y telefono (obligatorio, como P-03), cambio de contrasena solo con la nueva y cierre de sesion con aviso sin red. El enlace de recuperacion aterriza en P-18 con la seccion de contrasena abierta (AvisosEnlace). Opcion A en las cinco decisiones. Se borro SesionViewModel.cerrarSesion. El enlace se probo armado con adb, no con un correo real. H-15: el wireframe pedia municipio |
| 2026-10-11 | S2-T13 | BCJL | feat/S2-T13-foto-perfil-interfaz |  | Avatar de P-18: hoja con galeria (PickVisualMedia) y camara (TakePicture + FileProvider), validacion de 5 MB y JPG/PNG/WebP, recortador propio en Compose dentro de un Dialog y JPEG de 512 x 512 a subirFotoPerfil. Opcion A en las tres decisiones. Apilada sobre S2-T11. Hasta S2-T14 el repositorio real responde Error del servidor. El zoom con dos dedos no se probo en el dispositivo |
| 2026-10-11 | S2-T14 | LMM | feat/S2-T14-almacenamiento-imagenes |  | subirFotoPerfil real: sube a perfiles/<uid>/<ms>.jpg, escribe foto_url y borra el resto de la carpeta (la foto anterior no se conserva, por decision del lider). install(Storage) y traducirErrorStorage. Apilada sobre S2-T13. H-16: las fotos de servicio quedan en S3-T09. H-17: Storage responde 400 a todo rechazo |
| 2026-10-14 | S2-T15 | RRC | feat/S2-T15-guardas-navegacion | #18 | Las guardas y las barras leen la sesion real: SesionViewModel lleva EstadoSesion y P-01, P-02 y P-03 avisan el rol al entrar (cubre el caso sin red de S2-T09). Sin MarcadorSesionTemporal ni botones de rol en los marcadores; con sesion, P-02 a P-04 llevan al inicio. Opcion A en las tres decisiones **sin el lider disponible**. **Desvio:** la guarda regresa al inicio que ya esta en la pila en vez de apilar otro. Probado en Pixel_8 contra Supabase, incluido el enlace de recuperacion de un correo real hasta P-18 (con la app abierta; en frio no se repitio). **Hallazgos:** el proyecto estaba pausado, y Redirect URLs estaba vacia (el correo mandaba a localhost:3000); se agrego mx.donchambitas.app://auth. 217 unitarias y 67 instrumentadas pasando |

---

## Formato

```
| AAAA-MM-DD | S1-T01 | GRI | feat/S1-T01-analisis-requerimientos | #12 | Nota breve si hace falta |
```

La columna de nota se usa solo cuando pasó algo que el siguiente necesita
saber: un desvío del ticket, una deuda técnica que se dejó a propósito, o algo
que se descubrió y no estaba previsto. Si no pasó nada, se deja vacía.

## Detalle de las tareas terminadas

> Lo que cada tarea dejó escrito en `ESTADO.md` como "Última tarea terminada"
> antes de que la siguiente la relevara. La más reciente va arriba. Se agrega,
> no se edita.

**`S2-T14` — Almacenamiento real de imágenes en Supabase Storage.** 2026-10-11.
Rama `feat/S2-T14-almacenamiento-imagenes`, **apilada sobre la de `S2-T13`**, pull request **sin abrir todavía**. Orden de integración: `S2-T11`, `S2-T13` y luego esta. Ticket redactado por el agente el mismo día; el líder decidió al pedirla que **la foto anterior se borra**.

- **`RepositorioUsuarioReal.subirFotoPerfil`** real: sube a `perfiles/<uid>/<milisegundos>.jpg`, escribe la URL pública en `usuarios.foto_url` y borra todo lo demás de la carpeta. Si escribir `foto_url` falla, borra lo que subió. Si limpiar falla, sigue siendo éxito y la siguiente subida limpia.
- **`install(Storage)`** en `ModuloSupabase`, y `traducirErrorStorage` (`ErroresStorage.kt`).
- `CONTRATOS-API.md` ("`subirFotoPerfil`" a detalle) y `ARQUITECTURA.md` al día.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 212 pruebas unitarias (4 nuevas) y 67 instrumentadas (1 nueva, contra Supabase), 0 fallas.
  - **Contra el proyecto de Supabase**, en emulador `Medium_Phone`: subir desde la galería, reabrir, cambiar desde la cámara (queda un solo archivo en la carpeta) y subir sin red ("Sin conexión"). La carpeta de `ana.rls@…` quedó vacía y su `foto_url` en `null`.
  - **Sin llaves**: la aplicación abre y llega a P-02.
- **Lo que NO se probó en el dispositivo:** la falla del paso 2 (escribir `foto_url` después de subir); no se puede provocar desde la aplicación.
- **Dos hallazgos para el líder**, al final de `docs/tareas/S2-T14.md`:
  - **`H-16` · `ARQUITECTURA.md` le daba a `S2-T14` también las fotos de servicio.** Son de `RepositorioServicios`, real en `S3-T09`; esta tarea hizo solo la de perfil y la tabla ya lo dice.
  - **`H-17` · Storage no deja saber por qué rechazó un archivo:** todo llega como `400`. Hoy no estorba; con las fotos de servicio, sí.

**`S2-T13` — Selección y recorte de la foto de perfil (solo interfaz).** 2026-10-11.
Rama `feat/S2-T13-foto-perfil-interfaz`, **apilada sobre la de `S2-T11`**, pull request **sin abrir todavía**: se integra después del de `S2-T11`. Ticket redactado por el agente el mismo día; el líder eligió la opción A en las tres decisiones.

- **Tocar el avatar de P-18** abre una hoja con "Elegir de la galería" (`PickVisualMedia`) y "Tomar foto" (`TakePicture` con un `FileProvider` sobre `cache/fotos/`). No se pide ningún permiso.
- **Antes de recortar** se valida tipo y peso con los topes de la cubeta `perfiles` (5 MB; JPG, PNG o WebP), y se decodifica reducida y derecha según el EXIF (`FotoPerfil.kt`).
- **`RecortadorFoto`** (`ui/componentes`), propio, en un `Dialog` de pantalla completa: círculo fijo, mover y acercar de 1× a 4× sin dejar hueco.
- **"Usar foto"** manda a `subirFotoPerfil` un JPEG de 512 × 512. **Hoy el repositorio real responde "Error del servidor"** hasta `S2-T14`, por la decisión 3.
- `MensajeMiCuenta.Exito` pasó a llamarse `Texto`. `WIREFRAMES.md` §7 al día.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 208 pruebas unitarias (12 nuevas) y 66 instrumentadas (2 nuevas), 0 fallas.
  - En emulador `Medium_Phone`: hoja, galería, recortador, cancelar, rechazo de una PNG de 7,3 MB y de un GIF, arrastre hasta el borde y cámara virtual con la foto derecha.
  - **Sin llaves**: la aplicación abre y llega a P-02.
- **Lo que NO se probó en el dispositivo:** acercar con dos dedos (`adb` no hace el gesto; lo cubre `FotoPerfilTest`) y el éxito de punta a punta (llega con `S2-T14`).
- **Para `S2-T14`:** la interfaz ya manda los bytes listos. Falta guardarlos en `perfiles/<uid>/`, escribir `usuarios.foto_url` y decidir qué pasa con la foto anterior (`H-06`).

**`S2-T11` — Diseño y pantalla del perfil de usuario (ver y editar).** 2026-10-11.
Rama `feat/S2-T11-perfil-usuario`, pull request **sin abrir todavía**. Ticket redactado por el agente el mismo día; el líder eligió la opción A en sus cuatro decisiones y en una quinta que salió al implementar (el teléfono es obligatorio en P-18).

- **P-18 real** (`MiCuentaPantalla`, `MiCuentaViewModel`, `EstadoMiCuenta`), en lugar del marcador:
  - la ficha se lee con `obtenerMiUsuario()`; sin foto, el avatar muestra las iniciales;
  - "Editar datos personales" cambia la tarjeta por nombre, apellidos y teléfono, con las reglas de P-03, y pinta la fila que devuelve `actualizarMiUsuario`;
  - "Cambiar contraseña" pide solo la nueva; `same_password` se pinta bajo el campo;
  - "Cerrar sesión" pide confirmación y, sin red, avisa "Sin conexión";
  - el trabajador ve "Mi perfil profesional" (P-11) y "Mis postulaciones" (P-14).
- **El enlace de recuperación aterriza en P-18** (`DEC-27`, pasos 5 y 6): `esEnlaceDeRecuperacion` en `EnlaceAuth.kt`, `AvisosEnlace` en `ui/navegacion`, y `SesionViewModel` entrega el rol para poner el marcador. P-01 ya no navega si el enlace llegó primero.
- **`SesionViewModel.cerrarSesion()` se borró**; cerrar sesión ahora es de `MiCuentaViewModel`.
- `BotonSecundario` acepta `color` y `EtiquetaEstado` tiene una sobrecarga con colores dados.
- `CONTRATOS-API.md` (pasos 5 y 6 del recorrido, `same_password`) y `WIREFRAMES.md` §7 al día.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 196 pruebas unitarias (16 nuevas, 2 borradas con `cerrarSesion`) y 64 instrumentadas (5 nuevas), 0 fallas.
  - **Contra el proyecto de Supabase**, en emulador `Medium_Phone`: editar y reabrir, nombre vacío, cambiar la contraseña y entrar con la nueva, `same_password`, cerrar sesión con y sin red, accesos del trabajador con `beto.rls@…`. La ficha y la contraseña de `ana.rls@…` quedaron como estaban.
  - **Sin llaves**: la aplicación abre y llega a P-02.
- **Lo que NO se probó con un correo real:** el enlace de recuperación. Se armó con una sesión real de `ana.rls@…` y `type=recovery`, y se abrió con `adb`: aterrizó en P-18 con la aplicación abierta, en frío sin sesión y en frío con sesión. El enlace vencido sigue dejando la aplicación en P-02.
- **Un hallazgo para el líder:** `H-15` · el wireframe de P-18 pedía municipio y ubicación, que `public.usuarios` no tiene. `WIREFRAMES.md` ya no los pide.

**`S2-T12` — Contrato de la API de perfil de usuario (y `RepositorioUsuario` real).** 2026-10-11.
Rama `feat/S2-T12-repositorio-usuario-real`, **apilada sobre la de `S2-T09`**, pull request **#16**. Ese pull request integró también los commits de `S2-T09`: el #15 nunca se fusionó por separado. Ticket redactado por el agente el mismo día; el líder eligió la opción A en las tres decisiones.

- **`datos/repositorio/RepositorioUsuarioReal.kt`** (`DEC-31`), ya enlazado en `ModuloRepositorios`. Los otros ocho repositorios siguen falsos.
  - `obtenerMiUsuario()` lee la ficha propia de `public.usuarios`.
  - `actualizarMiUsuario(...)` manda solo `nombre`, `apellidos` y `telefono`, y devuelve la fila ya actualizada sin una segunda lectura.
  - Sin sesión, o si vuelven cero filas, devuelve `AUTENTICACION`: RLS no rechaza, filtra, y PostgREST contesta `200` con una lista vacía.
  - `subirFotoPerfil` devuelve `SERVIDOR` hasta `S2-T14`.
- **`datos/remoto/supabase/ErroresDatos.kt`:** `traducirErrorDatos`, por código HTTP. `supabase-kt` 3.0.3 no deja pasar el código de Postgres.
- **`CONTRATOS-API.md`:** la sección "Usuario" a detalle, con las llamadas exactas, la tabla de errores y la regla de que de la `Sesion` solo se leen el usuario y su rol.
- **En pantalla no cambia nada.** Ninguna pantalla llama todavía a `RepositorioUsuario`; P-18 es `S2-T11`.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 180 pruebas unitarias (11 nuevas) y 59 instrumentadas (5 nuevas), 0 fallas.
  - **Contra el proyecto de Supabase**, en emulador `Pixel_10`, con `RepositorioUsuarioRealTest`: lee la ficha de `ana.rls@…`, la edita y la vuelve a leer, borra el teléfono con `null`, un nombre de 81 caracteres da `VALIDACION` y sin sesión da `AUTENTICACION`. La ficha quedó como estaba.
  - **Sin llaves** en `local.properties`: la aplicación abre y llega a P-02, y los 5 casos nuevos salen omitidos.
  - Con llaves, `ana.rls@…` entra a P-05 y al reabrir sigue en P-05. Sin excepciones en logcat.
- **Lo que NO se probó en el dispositivo:** que sin red devuelva `RED`. Lo cubre `ErroresDatosTest`.
- **`RepositorioUsuarioRealTest` es la primera prueba de la suite que necesita red** y un proyecto de Supabase vivo con las cuentas de `92_usuarios_prueba.sql`. Sin llaves se omite; con llaves y sin esas cuentas, falla.
- **Dos hallazgos para el líder**, al final de `CONTRATOS-API.md`:
  - **`H-13` · El `mensaje` de los errores de autenticación lleva el token.** `ErroresAuth.kt` guarda `error.message`, que en `supabase-kt` 3.0.3 incluye las cabeceras de la petición. Hoy nadie lo pinta ni lo registra. El código nuevo no lo repite.
  - **`H-14` · `public.usuarios` acepta un nombre vacío.** No hay `CHECK` sobre nombre, apellidos ni teléfono; solo lo impide la validación de la aplicación.

**`S2-T09` — Manejo de sesión: inicio automático, cierre de sesión y expiración.** 2026-10-11.
Rama `feat/S2-T09-manejo-sesion`, pull request **#15**. Ticket redactado por el agente el 2026-10-08; el líder eligió la opción A en las dos decisiones.

- **`SplashViewModel`** lee el primer valor de `RepositorioAuth.sesionActual()`: sin sesión va a P-02, cliente a P-05 y trabajador a P-10. El mínimo de 800 ms corre a la par de la lectura y se mide con `delay`. Si en 8 segundos no llega nada, `error = TipoError.RED`; si el flujo lanza (sin llaves), P-02.
- **`SplashPantalla`:** con error, el bloque de abajo muestra "Sin conexión" y "Reintentar". Sin cadenas nuevas.
- **`ui/navegacion/SesionViewModel.kt`**, uno por actividad: escucha `sesionActual()` y avisa cuando no hay sesión; `cerrarSesion()` llama al repositorio y no navega.
- **`GrafoNavegacion`:** al terminar la sesión deja el marcador en `SIN_SESION` y va a P-02 con la pila limpia. P-01 deja el marcador con el rol de su destino. El botón "Cerrar sesión (P-02)" del marcador de P-18 cierra la sesión real.
- `CONTRATOS-API.md` al día en "`sesionActual()`".
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 169 pruebas unitarias (10 nuevas) y 54 instrumentadas, 0 fallas. `SplashViewModelTest` pasó diez corridas seguidas y ya no usa `System.currentTimeMillis`.
  - **Contra el proyecto de Supabase**, en emulador `Pixel_10`, el 2026-10-11:
    - sin sesión guardada, abre en P-02;
    - con la sesión de `ana.rls@…`, `force-stop` y abrir lleva a P-05 sin pasar por P-02; con `beto.rls@…`, a P-10. En los dos, atrás cierra la aplicación;
    - en modo avión, P-01 muestra "Sin conexión" con "Reintentar" y `sesion_cifrada.preferences_pb` sigue igual; con la red de vuelta, "Reintentar" entra a P-05;
    - "Cerrar sesión (P-02)" lleva a P-02, `sesion_cifrada.preferences_pb` queda en 0 bytes, atrás cierra la aplicación y al reabrir sigue en P-02;
    - "Cerrar sesión (P-02)" en modo avión deja al usuario en P-18 con la sesión guardada;
    - **migración al abrir:** con una sesión en claro puesta a mano en `shared_prefs` y sin `sesion_cifrada`, abrir la aplicación entra directo a P-05, la llave en claro desaparece y la sesión queda cifrada, sin tocar ningún botón.
  - Sin excepciones en logcat.
- **Lo que NO se probó en el dispositivo: la expiración.** Hace falta revocar la sesión desde la consola y bajar el *JWT expiry*, y el emulador no deja adelantar el reloj sin root. El ticket lo prevé: el caso queda cubierto en `SesionViewModelTest`, y el camino de `GrafoNavegacion` que lleva a P-02 es el mismo que se probó con el cierre a mano.
- **Sin llaves** en `local.properties` se probó el 2026-10-08: abre y llega a P-02 sin cerrarse. No se repitió el 2026-10-11.
- **Un desvío del ticket**, anotado en él: `SesionViewModel` avisa en cada `null`, también en el del arranque, y `GrafoNavegacion` decide si había alguien adentro mirando el marcador.
- **La confirmación por correo estuvo activada y ya se corrigió.** El 2026-10-11 `GET /auth/v1/settings` devolvía `mailer_autoconfirm: false`, contra `DEC-25` (el 2026-09-27 daba `true`). El líder la desactivó ese mismo día en la consola y se verificó que volvió a `true`. No se probó un alta nueva desde P-03 después del cambio.
- **Las cuatro cuentas `*.rls@prueba.donchambitas.mx` se volvieron a crear** el 2026-10-11 y siguen en el proyecto.

**`S2-T08` — Almacenamiento seguro de la sesión y el token.** 2026-10-08.
Rama `feat/S2-T08-almacenamiento-seguro-sesion`, pull request **#14**, integrado en `main`. Ticket redactado por el agente el mismo día; el líder eligió la opción A.

- **`datos/local/CifradorSesion.kt`:** AES-256-GCM con una llave que se genera dentro del Android Keystore y nunca sale de ahí. IV nuevo en cada cifrado. Lo que no se puede descifrar devuelve `null`, nunca lanza. Sin dependencia nueva.
- **`datos/local/AlmacenSesionCifrada.kt`:** el `SessionManager` de `supabase-kt`, sobre el DataStore `sesion_cifrada`. Sustituye al `SettingsSessionManager`, que guardaba la sesión **en texto plano** en `shared_prefs/mx.donchambitas.app_preferences.xml`.
  - Migra una sola vez la sesión en claro de antes y la borra, así que nadie tiene que volver a entrar.
  - Un dato indescifrable, ilegible o un archivo dañado cuenta como "sin sesión".
- **`ModuloSupabase`** le pasa ese almacén a `install(Auth)`.
- **Respaldo:** la sesión cifrada y las `SharedPreferences` por omisión quedan fuera de la copia de seguridad de Google y de la transferencia entre dispositivos. Antes la sesión en claro sí se respaldaba.
- **`Sesion.tokenAcceso` ya no existe** (`H-12`, `DEC-30`).
- `CONTRATOS-API.md` y `ARQUITECTURA.md` al día.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 159 pruebas unitarias (9 nuevas) y 54 instrumentadas (5 nuevas, el cifrador contra el Keystore real), 0 fallas.
  - **En emulador `Medium_Phone`, instalando encima de la versión de `main` con sesión abierta:**
    - antes, el token estaba en claro en `shared_prefs`;
    - después de la primera acción de Auth, la llave en claro desapareció y `sesion_cifrada` tiene la sesión sin `access_token`, sin JWT y sin el correo en claro;
    - tras cerrar y abrir, "Successfully loaded session from storage";
    - un inicio de sesión nuevo se guarda cifrado;
    - con el archivo dañado a mano, Auth da "No session found" y la aplicación no se cierra.
- **Cuatro desvíos del ticket**, anotados en él:
  - la migración ocurre con la primera acción de Auth, no al abrir: el cliente es perezoso desde `S2-T07` y P-01 aún no lee la sesión. Con `S2-T09` pasará al abrir;
  - también se tocaron `FuenteDatosFalsa.kt` y `RepositorioAuthFalso.kt`;
  - se agregó un manejador de corrupción al DataStore;
  - que `cerrarSesion()` borre la sesión cifrada se probó en la JVM, porque todavía no hay botón de cerrar sesión (`S2-T11`).

**`S2-T10` — Pantalla de recuperación de contraseña.** 2026-10-08.
Rama `feat/S2-T10-pantalla-recuperar-contrasena`, pull request **#13**, integrado en `main`. La trabajó el líder con el agente; el responsable nominal es LMM.

- `ui/pantallas/EstadoRecuperarContrasena.kt`, `RecuperarContrasenaViewModel.kt` y `RecuperarContrasenaPantalla.kt`, conforme a la sección 4 de `DISENO-AUTENTICACION.md`: formulario y confirmación en la misma pantalla, el correo normalizado solo al enviar, "Reintentar" solo en `RED`, `SERVIDOR` y `DESCONOCIDO`, y `Done` del teclado envía.
- `GrafoNavegacion.kt`: P-04 sustituye su marcador, regresa con `popBackStack` y entra a `RUTAS_SIN_BARRA_DEL_ANDAMIO`.
- `strings.xml`: las ocho cadenas de la sección 7.
- **Vigencia del enlace: una hora.** El líder leyó *Email OTP Expiration* en la consola, **3600 segundos**, y decidió no cambiarlo. `recuperar_vigencia` dice "El enlace vence en 1 hora", y `DISENO-AUTENTICACION.md` quedó igual.
- **Corrección de seguridad fuera del ticket, por instrucción del líder.** Con una cuenta de `@prueba.donchambitas.mx`, que no recibe correo, Supabase respondía `400 email_address_invalid`; con un correo sin cuenta, `200`. P-04 pintaba "Datos incorrectos" en el primero y la confirmación en el segundo, así que delataba qué correos tienen cuenta. Ahora `recuperarContrasena` reporta como éxito `email_address_invalid` y `email_address_not_authorized`, como ya hacía con los límites de envío. Toca `ErroresAuth.kt` (`esLimiteDeEnvio` pasa a `esRechazoQueDelataLaCuenta`), `RepositorioAuthReal.kt`, su prueba y `CONTRATOS-API.md`.
  - **Lo que queda abierto:** si el envío falla con `500` (por ejemplo, un SMTP propio mal configurado), sigue siendo `SERVIDOR`, y eso también solo pasa con cuentas que existen. No se tocó: tratar todo `5xx` como éxito escondería las caídas reales. Lo decide el líder si algún día hay SMTP propio.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 150 pruebas unitarias (14 nuevas) y 49 instrumentadas (10 nuevas), 0 fallas.
  - **Contra el proyecto de Supabase**, en emulador `Medium_Phone`:
    - vacío marca el campo y le deja el foco;
    - `  Ana.RLS@…` y `nadie@…` dan la misma confirmación, sin el correo;
    - sin red sale "Sin conexión" con el correo escrito, y "Reintentar" funciona con la red de vuelta;
    - girar no pierde ni el correo ni la vista;
    - atrás regresa a P-02 y el segundo atrás cierra la aplicación;
    - **un enlace real llegó** al correo del líder y, abierto en el emulador, la aplicación abrió sin cerrarse.
- **Cuenta creada para la prueba:** la del correo del líder, como cliente, nombre "Lider Prueba S2-T10". La contraseña la tiene el líder.
- **Por qué no llegaba el correo al principio:** esa dirección no tenía cuenta, y sin cuenta Supabase no envía nada.
- **Dos hallazgos para el líder:**
  - **El SMTP de fábrica de Supabase solo entrega a los miembros del equipo de la organización**, y muy pocos correos por hora. Un usuario real no recibe el enlace hasta que se configure un SMTP propio en la consola. El líder lo registró el 2026-10-08 como `S6-T13`.
  - **Enter de teclado físico** en P-04 envía la petición y además regresa a P-02, con lo que la petición se cancela. Es el mismo defecto que anotó `S2-T05` en P-03, y probablemente esté en P-02. La tecla de acción del teclado en pantalla funciona bien. No se tocó: arreglarlo solo en P-04 dejaría tres pantallas distintas.

**`S2-T07` — Implementación real de autenticación con Supabase Auth.** 2026-09-27.
Rama `feat/S2-T07-autenticacion-supabase-v2` en el remoto, **apilada sobre la de `S2-T06`**, que a su vez va sobre la de `S2-T05`. Pull request **#14**: se integra después de #12 y #13.

- `datos/repositorio/RepositorioAuthReal.kt` implementa la sección de autenticación de `CONTRATOS-API.md` y ya está enlazado en `ModuloRepositorios`. Los otros nueve repositorios siguen falsos.
- `di/ModuloSupabase.kt` provee el cliente con la URL y la `anon key` de `local.properties`. `RepositorioAuthReal` y `MainActivity` lo piden perezoso: **sin llaves, la aplicación abre y el botón muestra "Algo salió mal"** en vez de cerrarse.
- `datos/remoto/`:
  - `UsuarioDto`;
  - `ErroresAuth`, una función pura con una prueba por fila de la tabla del contrato;
  - `EnlaceAuth`;
  - `ConfiguracionSupabase`.
- **`DEC-28` (cierra `H-11`):** `TipoError.CORREO_DUPLICADO` con su título y su mensaje en `strings.xml`. P-03 pierde `mensajePantalla` y pinta todo error desde `strings.xml`.
- El `intent-filter` de `mx.donchambitas.app://auth` y `launchMode="singleTop"` en `MainActivity`.
- **Desvío del contrato, ya reflejado en él:** no se usa `handleDeeplinks`. En 3.0.3 lee el usuario en un scope sin manejador de errores, y abrir el enlace sin red cerraba la aplicación. `EnlaceAuth.kt` hace lo mismo dentro de un `try`.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 136 pruebas unitarias (25 nuevas) y 39 instrumentadas, 0 fallas.
  - **Contra el proyecto de Supabase**, en emulador `Medium_Phone`:
    - un alta de trabajador entra a P-10, y su fila de `public.usuarios` trae nombre, apellidos, teléfono y rol correctos;
    - la misma cuenta entra otra vez después de cerrar la aplicación;
    - el correo repetido da `CORREO_DUPLICADO`;
    - `ana.rls@…` entra a P-05 y `beto.rls@…` a P-10;
    - una contraseña mala da "Correo o contraseña incorrectos";
    - sin red sale "Sin conexión", y "Reintentar" funciona;
    - un enlace vencido no cierra la aplicación, ni en frío ni con la aplicación abierta.
  - Sin excepciones en logcat.
- **Consola:** el líder desactivó la confirmación por correo el 2026-09-27, verificado con `GET /auth/v1/settings` (`mailer_autoconfirm: true`). El mínimo de contraseña en 8 y la URL de redirección los aplicó el líder, pero **no se pueden verificar desde fuera**.
- **Cuenta de prueba creada:** `s2t07.trabajador@prueba.donchambitas.mx`. Se borra con el mismo `delete` de las de `S1-T03`, que está en `ESTADO.md`, en "Cuentas de prueba en Supabase".
- **`@prueba.donchambitas.mx` sí pasa el alta con la confirmación desactivada**, así que lo que `DEC-25` suponía se confirmó.
- **Lo que esta tarea NO trae:**
  - aterrizar en P-18 al llegar por recuperación, que es `S2-T11`;
  - que P-01 y las guardas lean la sesión real: al reabrir, la sesión existe, pero P-01 manda a P-02. Son `S2-T09` y `S2-T15`.
- **Riesgo para `S2-T11`, pendiente del líder:** con la autenticación real, los repositorios falsos ya no ven al usuario con sesión, y P-18 lee su perfil con `RepositorioUsuario`, que no tiene implementación real en el Sprint 2. Detalle en el ticket.

**`S2-T06` — Contrato de la API de autenticación (endpoints, payloads y errores).** 2026-09-27.
Rama `docs/S2-T06-contrato-api-autenticacion-v2` en el remoto, **apilada sobre la de `S2-T05`** por decisión del líder. Pull request **#13**: se integra después de #12.

Ticket `docs/tareas/S2-T06.md`, redactado por el agente el mismo día por instrucción del líder.

- `docs/tecnico/CONTRATOS-API.md`, sección de autenticación reescrita a detalle. **Se verificó contra el código fuente de `supabase-kt` 3.0.3**, la versión de `libs.versions.toml`, sacado de la caché de Gradle; no de memoria.
  - **Cada operación** tiene su llamada exacta, lo que devuelve y de dónde sale cada campo.
  - **La `Sesion` se arma en dos pasos:** primero Auth y luego la ficha de `public.usuarios`, con su `UsuarioDto` columna por columna.
  - **Las llaves del metadata de `registrar`** están cotejadas con las líneas 172 a 175 del esquema.
  - **El enlace de la aplicación es `mx.donchambitas.app://auth`**, con flujo `IMPLICIT`. Con `PKCE`, el `type=recovery` se pierde y la aplicación no sabría que tiene que ir a P-18. Está el recorrido de la recuperación en seis pasos, con la tarea que hace cada uno.
  - **`sesionActual()` tiene su tabla de `SessionStatus`.** `RefreshFailure` no cierra la sesión, porque en 3.0.3 solo marca fallas pasajeras.
  - **Hay una tabla de errores** con los códigos de `AuthErrorCode`, y la lista de ajustes que `S2-T07` aplica en la consola.
- `RepositorioAuth.registrar` devuelve `Resultado<Sesion>` (`DEC-25`). `RepositorioAuthFalso` devuelve la sesión que abre, y sus mensajes ya llevan acentos. `RegistroViewModel` lee el rol de `sesion.usuario`.
- `DISENO-AUTENTICACION.md`: la tabla 3.5 dice `Exito(Sesion)` y marca retirada la fila de "sin sesión".
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 111 pruebas unitarias y 39 instrumentadas, 0 fallas.
  - Recorrido a mano en emulador `Medium_Phone`: un alta nueva entra a P-05, y el correo duplicado muestra "El correo ya está registrado, inicia sesión" con acentos y sin "Reintentar". Sin excepciones en logcat.
- **Tres cosas que el contrato encontró en la biblioteca, para que `S2-T07` no las descubra tarde:**
  - Sin red, `signOut` **no** cierra la sesión local.
  - Un enlace de recuperación vencido hace que `handleDeeplinks` lance una excepción que nadie atrapa: `S2-T07` tiene que revisar el fragmento antes.
  - Si el alta sale bien y falla la lectura de la ficha, reintentar da correo duplicado.
- **Dos hallazgos nuevos para el líder**, al final de `CONTRATOS-API.md`:
  - **`H-11` · De dónde sale el mensaje en español de un error de `VALIDACION` en el registro.** Supabase Auth rechaza el correo duplicado en inglés, y `datos/` no puede leer `strings.xml`. Hay tres salidas; la recomendada es un `TipoError` nuevo. **`S2-T07` no puede cerrar el alta real sin esta decisión.**
  - **`H-12` · `Sesion.tokenAcceso` sobra.** Nadie lo lee y el token lo lleva `supabase-kt`.
- **Discrepancia de versión:** `BITACORA.md`, en la fila de `S1-T04`, dice `supabase-kt` 3.1.1, pero `libs.versions.toml` fija 3.0.3. No se tocó esa fila porque es de otra tarea.

**`S2-T05` — ViewModels y estados de UI del flujo de autenticación.** 2026-09-27.
Rama `feat/S2-T05-viewmodels-autenticacion`, pull request **#12**.

Ticket `docs/tareas/S2-T05.md`, redactado por el agente como los de `S2-T03` y
`S2-T04`. El alcance salió de las secciones 1.4 a 1.6, 2.2 a 2.4, 3.3 a 3.5 y 6
de `docs/producto/DISENO-AUTENTICACION.md`.

- `ui/pantallas/IniciarSesionViewModel.kt` y `RegistroViewModel.kt`: `@HiltViewModel` contra la interfaz `RepositorioAuth`, un solo `StateFlow` cada uno.
  - La validación de `S2-T04` se mudó de la pantalla sin cambiar: mismas reglas, mismo "campo tocado", mismos topes y filtro del teléfono.
  - Al enviar se normaliza según 1.6: el correo con `trim().lowercase()`, y el nombre y los apellidos con `trim()`.
  - El destino, P-05 o P-10, sale del rol del usuario que devuelve el repositorio.
  - Errores del repositorio en `errorPantalla`. En P-03, un error de `VALIDACION` trae además `mensajePantalla` tal cual.
  - Mientras carga, un segundo toque no hace una segunda llamada. `alReintentar` repite el envío.
- `IniciarSesionPantalla.kt` y `RegistroPantalla.kt` solo conectan el ViewModel con su `...Contenido`, que no cambió. Se quitaron los `rememberSaveable`: lo capturado sobrevive al giro porque vive en el ViewModel.
- `EstadoRegistro.kt`: gana `destino`, que le faltaba respecto a 3.3.
- `GrafoNavegacion.kt`: `entrarConSesion` marca el `MarcadorSesionTemporal` según el destino y limpia la pila del subgrafo, para P-02 y P-03 por igual. El marcador sigue ahí hasta `S2-T15`.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 111 pruebas unitarias, 0 fallas: 25 nuevas en `IniciarSesionViewModelTest` y `RegistroViewModelTest`. En esta corrida pasaron también las 2 intermitentes de `SplashViewModelTest`.
  - 39 pruebas instrumentadas, 0 fallas. Las de P-02 y P-03 ahora montan la pantalla con su ViewModel sobre `RepositorioAuthFalso` sin retraso; 4 son nuevas: cliente, trabajador, cuenta inexistente y correo duplicado.
  - Recorrido a mano en emulador `Medium_Phone` (Android 17):
    - La cuenta inexistente muestra el error de credenciales sin "Reintentar".
    - `Pedro.Plomero@ejemplo.com` entra a P-10, y el botón atrás cierra la aplicación.
    - El correo duplicado en P-03 muestra el mensaje del repositorio.
    - Un trabajador nuevo entra a P-10. Tras volver a "Sin sesión", la misma cuenta entra desde P-02.
    - El correo escrito se conserva al girar. Sin excepciones en logcat.
- **Tres desvíos del documento, para que el líder los revise.**
  - **El ViewModel de P-04 (4.3) no entra**, aunque la sección 9 se lo asigna a esta tarea: P-04 no existe hasta `S2-T10`, y un ViewModel sin pantalla es código muerto. Lo escribe `S2-T10`.
  - **La fila "`Exito(Usuario)` sin sesión → volver a P-02" de 3.5 no se implementó.** `DEC-25` la dejó sin objeto, así que el registro exitoso entra directo y `registro_cuenta_creada` sigue sin usarse.
  - **La pérdida de foco es un solo evento `alPerderFoco(campo)`** en vez de una función por campo.
- **Dos hallazgos, fuera del alcance:**
  - **El mensaje de correo duplicado sale sin acentos**, "El correo ya esta registrado, inicia sesion", porque así lo escribe `RepositorioAuthFalso`. Con Supabase lo dará el trigger.
  - **Con Enter de teclado físico** (`adb shell input keyevent 66`) en el teléfono de P-03, la aplicación regresó a P-02 en vez de enviar. Con la tecla de acción del teclado en pantalla envía bien. No se investigó: el `onDone` es el mismo de `S2-T02`, y lo más probable es que el Enter active el `BotonTexto` de abajo.

**`S2-T04` — Validaciones de formularios y mensajes de error.** 2026-09-23.
Rama `feat/S2-T04-validaciones-formularios`, pull request **#11**, abierto desde el fork `michaelleonmoso25-MLM/don-chambitas`. **El #10 de Ricardo5690 implementa la misma tarea**: se trabajaron en paralelo sin saberlo, y el líder decide cuál integrar.

Ticket `docs/tareas/S2-T04.md`, redactado por el agente como el de `S2-T03`.
El alcance salió de las secciones 1.5, 1.6, 5 y 7 de
`docs/producto/DISENO-AUTENTICACION.md`.

- `dominio/validacion/ValidacionesAuth.kt`: las 12 reglas de la tabla 5.2, una función pura por campo, sin Android. El correo usa la expresión de 5.1, espejo de `ck_usuario_correo_valido`. `LimitesRegistro` se mudó aquí desde `EstadoRegistro.kt`, con los mismos topes, y ganó `LARGO_MINIMO_CONTRASENA = 8`.
- `ui/pantallas/ValidacionEnPantalla.kt`: traduce cada `FalloValidacion` a su `R.string.validacion_*`, y `alPerderFoco`, que avisa cuando el foco sale de un campo.
- `IniciarSesionPantalla.kt` y `RegistroPantalla.kt`: validan cuando dice 1.5. Al salir de un campo **en el que ya se escribió** se valida ese campo; al pulsar el botón se validan todos, se marcan todos los que fallan y el foco salta al primero. Si el primero es el rol, la pantalla sube hasta el selector. Escribir limpia el error del campo, como ya pasaba.
- `strings.xml`: `validacion_contrasena_vacia`, la única clave de la sección 7 que faltaba.
- La vista previa "P-02 con errores" mostraba `validacion_contrasena_corta`, que en P-02 nunca sale. Ahora muestra `validacion_contrasena_vacia`.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 86 pruebas unitarias, 29 nuevas en `ValidacionesAuthTest`: pasan todas **menos 2 de `SplashViewModelTest`**, que fallan igual sin esta tarea. Ver el hallazgo abajo.
  - 31 pruebas instrumentadas de P-02 y P-03 pasando, 0 fallas: 16 en `IniciarSesionPantallaTest` y 15 en `RegistroPantallaTest`, 8 de ellas nuevas. Dos pruebas viejas se ajustaron porque enviaban el formulario vacío o sin contraseña, que ahora no pasa. Las 5 de `S1-T16` no se corrieron en esta máquina.
  - Recorrido a mano en emulador `Pixel_8` (Android 17): P-02 vacío marca los dos campos y enfoca el correo; `hola` al salir marca el formato y se limpia al escribir; P-03 con rol y todo vacío marca los cinco campos y enfoca el nombre. Sin excepciones en logcat.
- **Desvío de la letra de la sección 5, para que el líder lo revise.** La sección dice que cada regla devuelve `Int?`, el recurso del mensaje, pero `ARQUITECTURA.md` prohíbe Android en `dominio`, y `R` es de Android. Las reglas devuelven `FalloValidacion` y la capa `ui` lo traduce. En pantalla no cambia nada.
- **"Campo tocado" se interpretó como "ya se escribió en él".** Con "tuvo el foco", tocar un campo vacío y pasar a otro ya lo pinta de rojo, que es lo que 1.5 llama hostil.
- **Hallazgo, fuera del alcance: `SplashViewModelTest` es intermitente.** `SplashViewModel` llama a `verificarSesion()` en su `init`, y la prueba lanza una segunda verificación. Las dos terminan a los 800 ms simulados, pero cada una resta tiempo real (`System.currentTimeMillis`), así que cuál escribe al último depende de la velocidad de la máquina. `debeResolverDestinoInicioCliente_...` y `debeResolverDestinoInicioTrabajador_...` fallan de forma constante en esta máquina, con y sin los cambios de `S2-T04`. Es de `S1-T15`/`S1-T16`; no se tocó.
- **Lo que esta tarea NO trae:** la normalización al enviar y la llamada a `RepositorioAuth` son `S2-T05`, que además moverá esta validación al ViewModel sin cambiar las reglas. P-04 es `S2-T10` y usará `validarCorreo`.

**`S2-T03` — Pantalla de inicio de sesión.** 2026-09-22.
Rama `feat/S2-T03-pantalla-inicio-sesion`, pull request **sin abrir todavía**.

**Primera tarea del Sprint 2 con ticket**, `docs/tareas/S2-T03.md`, redactado el mismo día por instrucción del líder. El alcance salió de las secciones 1, 2 y 7 de `docs/producto/DISENO-AUTENTICACION.md`.

Implementación de P-02 conforme a esa especificación:
- `ui/pantallas/EstadoIniciarSesion.kt`: los siete campos del contrato 2.2. Los errores viajan como `@StringRes Int?` y `destino` es un evento de un solo uso.
- `ui/pantallas/IniciarSesionPantalla.kt`: `IniciarSesionPantalla` con estado local —provisional hasta que `S2-T05` traiga el ViewModel— y `IniciarSesionContenido`, el contenido visual puro. Los ocho elementos de la tabla 2.1 en orden, **sin flecha de regreso** por ser la raíz del subgrafo, isotipo del casco a 72 dp reutilizando el vector de P-01, y el correo normalizado con `trim().lowercase()` solo al enviar (regla 1.6): en pantalla se sigue viendo lo que se tecleó. Cuatro `@Preview`.
- `ui/navegacion/GrafoNavegacion.kt`: sustitución del marcador de P-02 por la pantalla real.
- `strings.xml`: 5 cadenas nuevas con las claves que fijó `S2-T01`. `auth_correo` y `auth_contrasena` ya existían desde `S2-T02`.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 57 pruebas unitarias pasando (`./gradlew testDebugUnitTest`).
  - 27 pruebas instrumentadas pasando, 0 fallas (`./gradlew connectedDebugAndroidTest`): las 16 anteriores y 13 nuevas en `IniciarSesionPantallaTest`.
  - Recorrido a mano en emulador `Medium_Phone` (Android 17): P-02 arranca sin flecha, "Regístrate" y "¿Olvidaste tu contraseña?" apilan P-03 y P-04 y el botón atrás regresa a P-02, el correo se conserva al girar el dispositivo y el alta entra a P-05 con el botón atrás cerrando la aplicación. Sin excepciones en logcat.
- **Se corrigió un defecto del andamio de `S1-T12`, fuera del alcance del ticket.** El `Scaffold` del grafo pintaba `BarraSuperior` en **toda** ruta, así que cada pantalla real —que trae la suya por la regla 1.1— salía con **dos barras encimadas, dos títulos y dos flechas**. Afectaba a P-01, P-02 y P-03. `P-03 ya estaba así en main`: entró con el pull request #8 y su criterio de aceptación se marcó cumplido igual. Se corrigió con `RUTAS_SIN_BARRA_DEL_ANDAMIO` en `GrafoNavegacion.kt`, que además devuelve el inset superior a la pantalla para que su barra dibuje bajo la barra de estado. **Cada pantalla que sustituya a su marcador tiene que agregarse a ese conjunto.**
- **Lo que esta tarea NO trae, por estar en la cola aparte:** las reglas de validación de formato y longitud son `S2-T04`, y el ViewModel contra `RepositorioAuth` es `S2-T05`. Hoy el botón entra **siempre como cliente**, porque sin `Sesion` no hay rol que leer.

**`S2-T02` — Pantalla de registro con selección de rol (cliente / trabajador).** 2026-09-21.
Rama `docs/S2-T01-diseno-pantallas-autenticacion`, pull request **sin abrir todavía**.

**También se trabajó sin ticket**, con la misma autorización del líder del 2026-09-21. El alcance se tomó de las secciones 1, 3 y 7 de `docs/producto/DISENO-AUTENTICACION.md`.

Implementación de P-03 conforme a esa especificación:
- `ui/pantallas/EstadoRegistro.kt`: estado inmutable con los seis valores de captura, el rol y un identificador de recurso por cada error de campo. Los mensajes viajan como `@StringRes Int?` y no como texto, para que ninguna cadena de interfaz viva fuera de `strings.xml`. `LimitesRegistro` fija los topes de captura con los de `public.usuarios` (80, 120, 160 y 10 dígitos).
- `ui/pantallas/RegistroPantalla.kt`: `RegistroPantalla` con el estado local —provisional hasta que `S2-T05` traiga el ViewModel— y `RegistroContenido`, el contenido visual puro. Selector de rol de dos `ChipCategoria` sin preselección (`DEC-22`), los cinco campos con su teclado y su acción de avance, ayuda de contraseña siempre visible, error en línea que no tapa el formulario, y `rememberSaveable` que conserva lo capturado al girar el dispositivo. Cuatro `@Preview`: vacío, con errores, cargando y correo duplicado.
- `ui/navegacion/GrafoNavegacion.kt`: sustitución del marcador de P-03 por la pantalla real. El alta marca la sesión con el mecanismo temporal de `S1-T12` y navega a P-05 o P-10 limpiando la pila del subgrafo de autenticación.
- `strings.xml`: 24 cadenas nuevas con las claves que fijó `S2-T01`.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 57 pruebas unitarias pasando (`./gradlew testDebugUnitTest`).
  - 16 pruebas instrumentadas pasando, 0 fallas (`./gradlew connectedDebugAndroidTest`): las 5 de `S1-T16` y 11 nuevas en `RegistroPantallaTest`.
  - Recorrido a mano en emulador `emulator-5554`: confirmar sin rol reclama el rol, elegirlo limpia el mensaje, el teléfono descarta lo que no sea dígito y corta en 10 (`477-12ab34x5678901` quedó en `4771234567`), y el alta como trabajador entra a P-10 con el botón atrás cerrando la aplicación. Sin excepciones en logcat.
- **Lo que esta tarea NO trae, por estar en la cola aparte:** las reglas de validación de formato y longitud con sus mensajes son `S2-T04`, y el ViewModel contra `RepositorioAuth` es `S2-T05`. Lo único que la pantalla decide hoy es exigir el rol, que es el título de la tarea.
- **Desvío de `CONVENCIONES.md`: las dos tareas van en la misma rama.** `S2-T02` se trabajó sobre `docs/S2-T01-diseno-pantallas-autenticacion` en vez de abrir `feat/S2-T02-pantalla-registro`. La regla es rama por tarea y pull request por tarea; aquí sale un solo pull request con las dos. Los commits sí llevan su `S2-T01` o `S2-T02` en el scope, así que se pueden separar después si el líder lo prefiere.
- **Se actualizó Espresso de 3.6.1 a 3.7.0 y `androidx.test.ext:junit` de 1.2.1 a 1.3.0.** No es parte de la tarea: las 5 pruebas instrumentadas de `S1-T16` ya venían fallando en la imagen actual del emulador porque Espresso 3.6.1 llama por reflexión a `InputManager.getInstance`, que ya no existe. Con la actualización las 16 pasan.

**`S2-T01` — Diseño de las pantallas de registro, inicio de sesión y recuperación.** 2026-09-21.
Rama `docs/S2-T01-diseno-pantallas-autenticacion`, pull request **sin abrir todavía**.

**Se trabajó sin ticket.** `docs/tareas/S2-T01.md` no existe —los tickets del Sprint 2 no se han redactado— y el líder autorizó avanzar sin él el 2026-09-21. El alcance acordado quedó escrito en la sección 0 del entregable y hace las veces de ticket.

Especificación de detalle de P-02, P-03 y P-04, que convierte los wireframes de `S1-T14` en algo implementable sin volver a decidir nada:
- `docs/producto/DISENO-AUTENTICACION.md`: documento nuevo, diez secciones.
  - Reglas comunes a las tres pantallas: esqueleto con medidas, scroll e `imePadding()` obligatorios, catálogo cerrado de componentes, cuándo se valida, normalización previa al envío, teclado y orden de foco, accesibilidad.
  - Los cuatro estados por pantalla, con el estado vacío declarado **no aplicable** en las tres y el porqué: son formularios, no listan datos. El cargando va dentro del `BotonPrincipal`, no a pantalla completa, para que HU-02 pueda conservar lo escrito.
  - Anatomía de arriba hacia abajo de cada pantalla, contrato de `Estado...` y lista de eventos con los nombres de `CONVENCIONES.md` (`EstadoIniciarSesion`, `EstadoRegistro`, `EstadoRecuperarContrasena`).
  - Matriz de cada resultado posible de `RepositorioAuth` contra lo que ve el usuario, en las tres pantallas.
  - Validaciones campo por campo con mensaje y clave, usando **la misma expresión regular** que `ck_usuario_correo_valido` en el esquema, y con los topes de longitud de `public.usuarios` aplicados en el propio campo.
  - Las 39 cadenas nuevas de `strings.xml` con su clave definitiva, para que `S2-T02`, `S2-T03`, `S2-T04` y `S2-T10` no inventen tres nombres distintos para lo mismo.
  - Navegación entre las tres, con `popBackStack` en los regresos y limpieza de la pila del subgrafo al entrar.
- `docs/producto/WIREFRAMES.md`: nota al inicio que enlaza el documento nuevo y deslinda qué resuelve cada uno.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 57 pruebas unitarias pasando, 0 fallas (`./gradlew testDebugUnitTest`).
  - Instalación y arranque limpio en emulador `emulator-5554` (`Displayed MainActivity`, sin excepciones en logcat).
- **No se escribió código:** la tarea es de diseño. Las pantallas las construyen `S2-T02`, `S2-T03` y `S2-T10`; los ViewModels, `S2-T05`; las validaciones, `S2-T04`.
- **Dos hallazgos nuevos para el líder, `H-09` y `H-10`.** Están al final del documento y se repiten abajo. Los dos se cerraron el 2026-09-22: `H-10` con `DEC-25` y `H-09` con `DEC-27`.

**`S1-T16` — Estrategia de pruebas y configuración de las pruebas base (JUnit / Compose test).** 2026-09-20.
Rama `test/S1-T16-estrategia-pruebas`, pull request **sin abrir todavía**.

Configuración completa de la infraestructura, utilidades y documentación de pruebas según `CONVENCIONES.md` y `ARQUITECTURA.md`:
- `docs/tecnico/PRUEBAS.md`: Documento maestro de estrategia de pruebas definiendo qué se prueba (ViewModels, dominio, repositorios falsos, interacción UI) y qué no (Composables pasivos, código generado), convenciones `debe..._cuando...`, comandos de ejecución y generación de reportes de cobertura JaCoCo.
- `app/src/test/.../util/ReglaCorrutinas.kt`: Regla de JUnit 4 (`TestWatcher`) para pruebas unitarias que reemplaza `Dispatchers.Main` con `StandardTestDispatcher`, proveyendo `testDispatcher` y `testScope` sin requerir el Looper de Android.
- `app/src/test/.../util/DatosPrueba.kt`: Fábrica con valores por defecto y argumentos con nombre para todas las entidades principales (`Usuario`, `PerfilTrabajador`, `PerfilHabilidad`, `Servicio`, `ServicioFoto`, `Solicitud`, `Postulacion`, `Conversacion`, `Mensaje`, `Resena`, `Categoria`, `Estado`, `Municipio`, `Sesion`, etc.).
- `app/src/test/.../util/DatosPruebaTest.kt`: Pruebas unitarias para validar `DatosPrueba` y la ejecución en el despachador principal mediante `ReglaCorrutinas`.
- `app/src/test/.../ui/pantallas/SplashViewModelTest.kt`: Actualizado para utilizar `ReglaCorrutinas` y la convención de nomenclatura `debe..._cuando...`, funcionando como plantilla oficial para ViewModels con corrutinas.
- `app/src/androidTest/.../ui/componentes/ComponentesTest.kt`: 4 pruebas instrumentadas en Jetpack Compose (`createComposeRule`) validando renderizado y eventos de `BotonPrincipal`, `CampoTexto` y `EstadoVacio`.
- `app/build.gradle.kts`: Activación de `enableUnitTestCoverage = true` y `enableAndroidTestCoverage = true` en el build type `debug` para soportar las tareas de reporte JaCoCo (`createDebugUnitTestCoverageReport` y `createDebugCoverageReport`).
- Verificación del proyecto:
  - 57 pruebas unitarias pasando (`./gradlew testDebugUnitTest`).
  - 5 pruebas instrumentadas pasando en emulador (`./gradlew connectedDebugAndroidTest`).
  - Reporte de cobertura generado exitosamente (`./gradlew createDebugUnitTestCoverageReport`).
  - Compilación exitosa (`./gradlew assembleDebug`).
  - Instalación y ejecución interactiva limpia en emulador `emulator-5554` (`Displayed MainActivity`).

**`S1-T15` — Pantalla de bienvenida (splash).** 2026-09-20.
Rama `feat/S1-T15-pantalla-splash`, pull request **sin abrir todavía**.

Implementación completa de P-01 (Splash) según `PANTALLAS.md`, `WIREFRAMES.md` y `DISENO.md`:
- `ui/pantallas/EstadoSplash.kt`: Data class de estado inmutable con propiedades `cargando: Boolean` y `destino: Ruta?`.
- `ui/pantallas/SplashViewModel.kt`: ViewModel con `@HiltViewModel` que consulta el estado de sesión temporal (según S1-T12 hasta S2-T09) e impone un retraso mínimo de 800 ms para evitar parpadeos, resolviendo los tres destinos posibles:
  - Sin sesión -> `P-02` (Iniciar sesión)
  - Cliente -> `P-05` (Inicio cliente)
  - Trabajador -> `P-10` (Inicio trabajador)
- `ui/pantallas/SplashPantalla.kt`: Composable con fondo `Crema` (`#FFFDF8`), isotipo del casco oficial de seguridad Don Chambitas, nombre de aplicación en `Carbon` (negrita), eslogan "Tu oficio, tu chamba" en `Cafe`, e indicador circular `Cargando` en color `Mostaza` (48 dp) con etiqueta "Verificando sesión…".
- `ui/navegacion/GrafoNavegacion.kt`: Sustitución del marcador provisional por `SplashPantalla`, saliendo de la pila de navegación con `popUpTo(Ruta.Splash.ruta) { inclusive = true }` de modo que presionar el botón Atrás desde el destino cierra la aplicación.
- `themes.xml` y `colors.xml`: Configuración de `android:windowBackground` con `color_crema` (`#FFFDF8`) eliminando cualquier parpadeo de fondo blanco antes de renderizar Compose.
- Pruebas unitarias: 7 pruebas unitarias nuevas en `SplashViewModelTest.kt` cubriendo estados iniciales, resolución de destinos y temporizador mínimo (51 pruebas totales en el proyecto pasando limpiamente).
- Compilación (`./gradlew assembleDebug`), instalación y verificación interactiva en emulador `emulator-5554` comprobando arranque en Splash, transición a destino y cierre limpio con botón Atrás.

**`S1-T14` — Wireframes de las pantallas de autenticación e inicio.** 2026-09-20.
Rama `docs/S1-T14-wireframes-pantallas`, pull request **sin abrir todavía**.

Diseño y documentación completa de las pantallas para el arranque del Sprint 2 conforme a `PANTALLAS.md` y `DISENO.md`:
- `docs/producto/wireframes/`:
  - 11 wireframes vectorizados a escala móvil estándar **360 × 800 dp** con la paleta oficial **Taller** (Mostaza, MostazaOscuro, Terracota, Carbon, Cafe, Crema, Arena, Borde y semánticos Exito, Advertencia y Error):
    - `P-01-splash.png`: Splash con casco de seguridad, slogan y widget de carga.
    - `P-02-iniciar-sesion.png`: Login con campos de captura, visibilidad y enlaces de navegación.
    - `P-03-registro.png`: Alta de cuenta con selector de rol único (`ChipCategoria`), 5 campos y enlaces.
    - `P-04-recuperar.png`: Formulario de recuperación de contraseña con aviso de vigencia (24h).
    - `P-05-inicio-cliente.png`: Inicio cliente con buscador, chips de oficios, tarjetas de trabajadores con estrellas, botón flotante "+ Publicar solicitud" y barra inferior de 4 destinos.
    - `P-05-vacio.png`: Estado vacío de búsqueda con icono, explicación y acción de limpiar filtros.
    - `P-05-error.png`: Estado error con icono de advertencia, mensaje explicativo y botón de reintentar.
    - `P-10-inicio-trabajador.png`: Inicio trabajador con filtro de oficios, tarjetas de solicitudes abiertas, etiqueta de estado y barra inferior.
    - `P-10-vacio.png`: Estado vacío de solicitudes por categoría con sugerencias y botón de ver todas.
    - `P-10-error.png`: Estado error con mensaje de reintento.
    - `P-18-cuenta.png`: Mi cuenta con cabecera de perfil, badge de rol, acciones de configuración y cierre de sesión.
  - Cada zona cuenta con anotaciones exactas de componentes (`BarraSuperior`, `BarraInferior`, `BotonPrincipal`, `BotonSecundario`, `BotonDestacado`, `BotonTexto`, `CampoTexto`, `CampoContrasena`, `TarjetaTrabajador`, `TarjetaSolicitud`, `ChipCategoria`, `Estrellas`, `EtiquetaEstado`, `Cargando`, `EstadoVacio`, `EstadoError`). Cero componentes ajenos a `DISENO.md`.
- `docs/producto/WIREFRAMES.md`:
  - Índice maestro con tabla resumen, imágenes incrustadas, desglose de componentes por zona y documentación exhaustiva del comportamiento de cada elemento tocable (eventos al pulsar, navegación, validaciones y cambios de estado).
- Verificación del proyecto:
  - Pruebas unitarias pasando limpiamente (44 pruebas, `./gradlew testDebugUnitTest`).
  - Compilación exitosa (`./gradlew assembleDebug`).
  - Instalación y ejecución interactiva limpia en emulador `emulator-5554` (`Displayed MainActivity`).

**`S1-T13` — Interfaces de repositorio y fuente de datos falsa (fake) para desbloquear la UI.** 2026-09-20.
Rama `feat/S1-T13-repositorios-falsos`, pull request **sin abrir todavía**.

Implementación completa de la capa de datos en memoria y contratos de repositorio según `ARQUITECTURA.md` y `CONTRATOS-API.md`:
- `dominio/repositorio/`:
  - 10 interfaces de dominio puras (`RepositorioAuth`, `RepositorioUsuario`, `RepositorioTrabajador`, `RepositorioServicios`, `RepositorioSolicitudes`, `RepositorioPostulaciones`, `RepositorioChat`, `RepositorioResenas`, `RepositorioCatalogos`, `RepositorioIa`) con métodos `suspend` devolviendo `Resultado<T>` y `Flow` reactivo para sesiones y mensajes. Cero dependencias de Android o Supabase.
- `dominio/modelo/ModelosRepositorio.kt`:
  - Modelos de soporte de dominio (`Sesion`, `PerfilPublicoTrabajador`, `CalificacionTrabajador`, `ServicioPublico`, `ResenaPublica`, `FiltrosBusquedaTrabajadores`, `ResumenTrabajadorBusqueda`, `DetalleSolicitud`, `ResultadoIa`).
- `datos/falso/`:
  - `FuenteDatosFalsa.kt`: Singleton en memoria con semillero coherente de `04_datos_semilla.sql` (16 categorías, 32 estados, 26 municipios, 8 trabajadores completos con servicios y fotos, 2 clientes, 5 solicitudes en estados abierta/asignada/cerrada/cancelada, 3 chats con mensajes y reseñas válidas respetando las restricciones relacionales del esquema).
  - 10 implementaciones falsas correspondientes (`Repositorio*Falso`) aplicando simulación de latencia de red (300 ms) y propiedad `errorForzado: TipoError?` para pruebas de `EstadoError`.
- `di/ModuloRepositorios.kt`:
  - Módulo de Hilt vinculando las 10 interfaces de dominio a sus implementaciones falsas con `@Binds` en `SingletonComponent`. Único archivo a modificar cuando entren las implementaciones reales.
- Pruebas unitarias:
  - 10 pruebas unitarias nuevas en `RepositoriosFalsosTest.kt` cubriendo las 10 implementaciones, validaciones de negocio, operaciones atómicas (aceptar postulación) y control de errores (44 pruebas totales en el proyecto pasando limpiamente).
  - Compilación (`./gradlew assembleDebug`), instalación y ejecución limpia en emulador Pixel 8 Pro.

**`S1-T12` — Navegación con Navigation Compose y definición del grafo de rutas.** 2026-09-20.
Rama `feat/S1-T12-navegacion-compose`, pull request **sin abrir todavía**.

Implementación completa de la arquitectura de navegación en Jetpack Compose según `ARQUITECTURA.md`, `PANTALLAS.md` y `DISENO.md`:
- `Rutas.kt`:
  - Las 19 rutas del sistema modeladas con la clase sellada `Ruta` (`P-01` a `P-19`), con identificadores únicos, títulos y argumentos fuertemente tipados (`NavType.StringType`, nulabilidad y valores por defecto).
  - Subgrafos definidos en `Subgrafo`: `Autenticacion`, `Cliente`, `Trabajador`.
  - Cero cadenas de ruta sueltas fuera de `Rutas.kt` (verificado con `grep` y pruebas unitarias).
  - Las 8 pantallas que se abren encima y no muestran barra inferior registradas en `PANTALLAS_ENCIMA`.
  - Sistema de guardas de navegación reactivo (`resolverGuarda` y `MarcadorSesionTemporal`) con control temporal de rol (Sin sesión, Cliente, Trabajador):
    - Sin sesión: cualquier ruta privada redirige a `P-02` (Iniciar sesión).
    - Con sesión Cliente: las rutas de trabajador redirigen a `P-05` (Inicio cliente).
    - Con sesión Trabajador: las rutas de cliente redirigen a `P-10` (Inicio trabajador).
- `BarraInferiorCliente.kt`: Barra inferior con exactamente 4 destinos para Cliente (`Inicio` P-05, `Solicitudes` P-09, `Chats` P-15, `Cuenta` P-18) con indicador Mostaza e iconos Carbon/Cafe.
- `BarraInferiorTrabajador.kt`: Barra inferior con exactamente 4 destinos para Trabajador (`Inicio` P-10, `Servicios` P-12, `Chats` P-15, `Cuenta` P-18).
- `GrafoNavegacion.kt`:
  - Grafo completo con tres subgrafos y pantallas compartidas.
  - Botón flotante (+) en `P-05` (Inicio cliente) que navega a `P-08` (Publicar solicitud).
  - Marcador interactivo para las 19 pantallas con información de ruta, argumentos recibidos, conmutador de sesión en tiempo real, pruebas de guardas y mapa completo de navegación.
  - Botón de regreso del sistema integrado con `Scaffold` y `BarraSuperior`.
- `MainActivity.kt`: Envoltorio limpio llamando a `GrafoNavegacion()` dentro de `DonChambitasTema`.
- 9 pruebas unitarias nuevas en `NavegacionTest.kt` (34 pruebas totales en el proyecto pasando limpiamente), compilación (`./gradlew assembleDebug`), instalación y verificación interactiva en emulador (`emulator-5554`).

**`S1-T11` — Componentes de estado: carga, vacío, error y mensajes al usuario.** 2026-09-20.
Rama `feat/S1-T11-componentes-estado`, pull request **sin abrir todavía**.

Construcción completa de los componentes de estado y el patrón de pantalla con datos según `DISENO.md` y `ARQUITECTURA.md`:
- `Estados.kt`:
  - `Cargando`: Indicador circular centrado en color Mostaza (48 dp) y mensaje opcional, con descripción semántica de accesibilidad.
  - `EstadoVacio`: Icono grande (56 dp en color Cafe), título (subtítulo en Carbon), mensaje (cuerpo en Cafe) y botón opcional (`BotonPrincipal`). Soporta valores por defecto desde `strings.xml`.
  - `EstadoError`: Icono de advertencia en color Error, título (subtítulo en Carbon), mensaje de error que dice qué hacer derivado de cada `TipoError` (o mensaje personalizado) y botón de reintentar opcional (`BotonPrincipal` con texto "Reintentar").
  - Mapeo de `TipoError` a recursos de cadenas (`obtenerMensajeErrorRes` y `obtenerTituloErrorRes`), garantizando mensajes distintos orientados a la acción para `RED`, `AUTENTICACION`, `VALIDACION`, `LIMITE_IA`, `SERVIDOR` y `DESCONOCIDO`.
- `ContenedorEstado.kt`:
  - `ContenedorEstado`: Patrón de pantalla con datos que recibe `cargando`, `error` (`TipoError?`), `vacio`, `alReintentar` y `contenido`. Utiliza `resolverEstadoVisual` para garantizar orden de precedencia estricto (cargando > error > vacío > contenido) asegurando que ningún estado se pinte encima de otro. Permite personalización total mediante slots de vista.
- `strings.xml`: Cadenas agregadas para reintentar, títulos de error y mensajes explicativos por `TipoError` centrados en la acción.
- 8 pruebas unitarias nuevas en `EstadosTest.kt` (25 pruebas totales en el proyecto pasando limpiamente), compilación (`./gradlew assembleDebug`), instalación y verificación en emulador.

**`S1-T10` — Componentes reutilizables base (botones, campos de texto, tarjetas, chips).** 2026-09-20.
Rama `feat/S1-T10-componentes-base`, pull request **sin abrir todavía**.

Construcción completa de los 14 componentes reutilizables base de la tabla de `DISENO.md` dentro de `mx.donchambitas.app.ui.componentes`:
- `Botones.kt`: `BotonPrincipal` (Mostaza con texto Carbon 7.09:1 WCAG AAA), `BotonSecundario` (contorno Mostaza Oscuro), `BotonDestacado` (Terracota con texto blanco 5.12:1 WCAG AA) y `BotonTexto` (sin fondo, texto Mostaza Oscuro). Soportan estados `habilitado` y `cargando` con indicador de progreso y bloqueo táctil.
- `Campos.kt`: `CampoTexto` (fondo Arena, contorno Borde/Mostaza Oscuro y texto de error en color Error) y `CampoContrasena` (con alternador de visibilidad e iconos de ojo).
- `Estrellas.kt`: `Estrellas` en color Terracota permitiendo media estrella en modo lectura con iconos vectoriales y descripción de accesibilidad.
- `Chips.kt`: `ChipCategoria` (Mostaza activo con texto Carbon, Arena inactivo con borde) y `EtiquetaEstado` con los cuatro colores de estado (`abierta` en Exito, `asignada` en Advertencia con texto Carbon, `cerrada` en Cafe, `cancelada` en Error con texto blanco).
- `Barras.kt`: `BarraSuperior` (fondo Mostaza, texto Carbon, flecha de regreso opcional) y `BarraInferior` (4 destinos: Inicio, Buscar, Solicitudes, Perfil con indicador en Mostaza e iconos Carbon/Cafe).
- `Tarjetas.kt`: `TarjetaTrabajador` (foto, nombre, oficio, estrellas, municipio), `TarjetaServicio` (foto, titulo, categoria, precio) y `TarjetaSolicitud` (titulo, categoria, presupuesto, estado, fecha).
- Cada componente acepta `modifier: Modifier = Modifier` como último parámetro con valor por defecto, no importa capas de repositorio ni datos, no contiene colores literales y cuenta con su `@Preview` funcional.
- 17 pruebas unitarias pasando (`./gradlew testDebugUnitTest`), compilación (`./gradlew assembleDebug`) e instalación/ejecución limpia en emulador Pixel 8 Pro.

**`S1-T09` — Sistema de diseño en Jetpack Compose (Theme, Color, Typography, Shape).** 2026-09-20.
Rama `feat/S1-T09-sistema-diseno`, pull request **sin abrir todavía**.

Implementación completa de la paleta Taller como tema de Jetpack Compose (`DonChambitasTema`) en `mx.donchambitas.app.ui.tema`.
- `Color.kt`: los 8 colores de la paleta (`Mostaza`, `MostazaOscuro`, `Terracota`, `Carbon`, `Cafe`, `Crema`, `Arena`, `Borde`) y 3 semánticos (`Exito`, `Advertencia`, `Error`), más `Blanco`. Ni una sola declaración de `Color(0xFF...)` fuera de este archivo.
- `Tema.kt`: `ColorScheme` de Material 3 con `onPrimary` mapeado obligatoriamente a `Carbon` (relación 7.09:1 WCAG AAA), `secondary` a `Terracota` con `onSecondary` en blanco, `background` en `Crema`, `surface` en `Arena` con `onSurface` en `Carbon`, `outline` en `Borde` y `error` en `Error`.
- `Tipografia.kt`: los 6 estilos tipográficos de `DISENO.md` (`titulo`, `subtitulo`, `cuerpoFuerte`, `cuerpo`, `secundario`, `pie`) con la fuente del sistema, integrados en `Typography` de Material 3 y accesibles vía propiedades de extensión.
- `Espaciado.kt`: escala base de 4 (`dp4` a `dp48`) y valores semánticos (`margenPantalla`, `separacionTarjetas`, `rellenoTarjeta`).
- `Formas.kt`: `Shapes` de Material 3 y formas de componentes (botones y campos 12 dp, tarjetas 16 dp, chips círculo, hoja inferior 20 dp).
- `MainActivity.kt`: envuelta en `DonChambitasTema`, arrancando con fondo Crema verificado en emulador.
- Pruebas unitarias en `TemaTest.kt` comprobando los contrastes obligatorios, la escala de tipografía y espaciado. Compilación (`./gradlew assembleDebug`) y pruebas (`./gradlew testDebugUnitTest`) exitosas.

**`S1-T08` — Identidad visual: paleta de colores, tipografía e iconografia.** 2026-09-20.
Rama `feat/S1-T08-identidad-visual`, pull request **sin abrir todavía**.

Cierre y documentación completa de la identidad visual de la aplicación. Tabla exhaustiva de relaciones de contraste WCAG 2.1 (Carbon sobre Mostaza 7.09:1 pasa AAA, Blanco sobre Mostaza 2.26:1 falla y queda prohibido). Diseño del logotipo vectorial legible a 48 dp en `docs/tecnico/recursos/logo.svg`. Lámina visual de la paleta Taller y reglas de aplicación en `docs/tecnico/recursos/muestra-paleta.png`. Asignación formal de los 16 iconos de oficios con Material Icons Outlined en `docs/tecnico/DISENO.md` y reemplazo total de los nombres provisionales Tabler en `basedatos/04_datos_semilla.sql`. Icono adaptativo vectorial (background y foreground con casco de seguridad en Mostaza) e iconos rasterizados en todas las densidades de mipmap (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi) para versión estándar y redonda. Probado e inspeccionado exitosamente en emulador Pixel 8 Pro (lanzador, cajón de apps y ajustes del sistema). Compilación (`./gradlew assembleDebug`) y pruebas unitarias (`./gradlew testDebugUnitTest`) exitosas.

**`S1-T07` — Diccionario de datos y modelado de entidades en Kotlin (data classes).** 2026-09-20.
Rama `feat/S1-T07-modelado-entidades`, pull request **sin abrir todavía**.

Modelado completo de las entidades del esquema relacional en Kotlin dentro de `mx.donchambitas.app.dominio.modelo` (`Usuario`, `PerfilTrabajador`, `PerfilHabilidad`, `Servicio`, `ServicioFoto`, `Solicitud`, `Postulacion`, `Conversacion`, `Mensaje`, `Resena`, `Estado`, `Municipio`, `Categoria`) y los 4 tipos enumerados exactos (`RolUsuario`, `EstadoSolicitud`, `EstadoPostulacion`, `FuncionIa`). Mapeo estricto de tipos (`UUID` a `String`, `TIMESTAMPTZ` a `Instant`, `NUMERIC(10,2)` a `BigDecimal`, `SMALLINT`/`SERIAL` a `Int`) y nulabilidad correspondiente. Sin anotaciones de serialización ni dependencias de `supabase-kt` o `android.*`. Documentado en `docs/tecnico/DICCIONARIO-DATOS.md`. Pruebas unitarias en `ModelosTest.kt`. Compilación (`./gradlew assembleDebug`), pruebas unitarias (`./gradlew testDebugUnitTest`) e instalación y ejecución en emulador Pixel 8 Pro exitosas.

**Restos del cierre de `S1-T03`.** 2026-09-16.

- **El diagrama**, en `docs/tecnico/diagrama-er.png`, con su generador al lado.
- **El cruce de las 33 historias contra las tablas**, en `MODELO-ER.md`.
  Ninguna historia pide una tabla que no exista.
- **Las pruebas 10, 12 y 13 de `91` ahora corren con `set role authenticated`.**
  Antes corrían como `postgres`, que se salta RLS, y por eso no demostraban
  nada: con RLS apagado, a una función de trigger le puede faltar el
  `security definer` y comportarse igual de bien. Las tres exigen además de qué
  capa viene el rechazo, y las tres trajeron el mensaje de su trigger.
- **`92_usuarios_prueba.sql` y `92_prueba_rls_anon.py`**, que son el paso 2c.
  Cuatro de sus diez comprobaciones son controles a propósito: si las tablas
  estuvieran rotas del todo, todo volvería vacío y las otras seis pasarían sin
  que nada funcionara.

**Dos cosas que conviene saber antes de tocar esto:**

- `ia_cache` devuelve `HTTP 403`, no una lista vacía como pedía el ticket. Es
  el `revoke all` quitando el permiso antes de que RLS entre a filtrar. Más
  estricto, no menos.
- **El dominio `@prueba.donchambitas.mx` no sirve para el alta por la API.**
  Supabase valida que el dominio exista y ese es ficticio. `91` nunca se topó
  con ello porque inserta directo en `auth.users`. Le va a tocar a `S2-T07`.
