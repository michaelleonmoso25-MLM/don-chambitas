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
| 2026-09-23 | S2-T05 | BCJL | feat/S2-T05-viewmodels-autenticacion |  | IniciarSesionViewModel y RegistroViewModel contra RepositorioAuth: P-02 entra segun el rol de la cuenta y P-03 crea la cuenta en la fuente falsa y entra. El estado y las reglas de S2-T04 salen de las pantallas; normalizacion de 1.6 al enviar. **Sale de la rama de S2-T04 (feat/S2-T04-validaciones-formularios)**, que aun no esta en main. **Dos desvios:** el ViewModel de P-04 se deja para S2-T10 porque su pantalla no existe, y la fila 'registro sin sesion' de 3.5 no se implementa por DEC-25. 27 pruebas unitarias nuevas; 33 instrumentadas de P-02 y P-03 pasando |

---

## Formato

```
| AAAA-MM-DD | S1-T01 | GRI | feat/S1-T01-analisis-requerimientos | #12 | Nota breve si hace falta |
```

La columna de nota se usa solo cuando pasó algo que el siguiente necesita
saber: un desvío del ticket, una deuda técnica que se dejó a propósito, o algo
que se descubrió y no estaba previsto. Si no pasó nada, se deja vacía.
