# Índice de tareas

> Archivo **vivo**. Quien cambia el estado de una tarea lo actualiza en el
> mismo commit. Es la cola de trabajo y la fuente de verdad sobre qué sigue.

## Cómo se lee

**Estados:** `pendiente` lista para tomarse · `en curso` alguien la trabaja ·
`hecha` integrada en main · `bloqueada` espera al líder, no se toca ·
`opcional` solo si sobra tiempo, riesgo del equipo.

**Regla de selección:** la de mayor prioridad numérica que esté `pendiente`,
sin dependencias abiertas. Sin adelantarse, sin saltarse.

**Escala de prioridad, 16 niveles:** 1000, 970, 950, 900, 850, 800, 750, 700,
650, 600, 550, 500, 450, 400, 300, 250.

## Resumen

| Sprint | Tareas | Pendientes | Bloqueadas | Opcionales | Hechas |
|---|---|---|---|---|---|
| 1 | 16 | 0 | 0 | 0 | 16 |
| 2 | 16 | 11 | 0 | 0 | 5 |
| 3 | 15 | 15 | 0 | 0 | 0 |
| 4 | 15 | 13 | 0 | 2 | 0 |
| 5 | 12 | 12 | 0 | 0 | 0 |
| 6 | 12 | 12 | 0 | 0 | 0 |
| **Total** | **86** | **63** | **0** | **2** | **21** |

> **No queda ninguna tarea bloqueada, ni ningún pendiente abierto.** PEND-01 se
> resolvió el 2026-09-14 a favor de Supabase (`DEC-16`) y PEND-02 el 2026-09-22
> a favor de los 26 municipios sembrados (`DEC-26`).

---

## Sprint 1

**Objetivo:** Como usuario puedo abrir la aplicación, recorrer la bienvenida y moverme entre las pantallas principales con una identidad visual clara y consistente.

| ID | Prio | Tarea | Resp. | Estado | Depende de |
|---|---|---|---|---|---|
| `S1-T01` | 1000 | Análisis de requerimientos e historias de usuario (documento en .md) | GRI | hecha | — |
| `S1-T02` | 970 | Definición del alcance del MVP y lista completa de pantallas | BCJL | hecha | S1-T01 |
| `S1-T03` | 950 | Modelo entidad-relación (ER) completo del sistema | LMM | hecha | S1-T01 |
| `S1-T04` | 900 | Configuración del proyecto Android (Gradle, Kotlin, Compose, Hilt) | RRC | hecha | — |
| `S1-T05` | 850 | Configuración del repositorio en GitHub: ramas, plantilla de PR y .gitignore | GRI | hecha | — |
| `S1-T06` | 800 | Arquitectura de capas y carpetas (data / domain / ui) documentada en .md | BCJL | hecha | S1-T04 |
| `S1-T07` | 750 | Diccionario de datos y modelado de entidades en Kotlin (data classes) | LMM | hecha | S1-T03, S1-T04 |
| `S1-T08` | 700 | Identidad visual: paleta de colores, tipografía e iconografia | RRC | hecha | — |
| `S1-T09` | 650 | Sistema de diseño en Jetpack Compose (Theme, Color, Typography, Shape) | GRI | hecha | S1-T04, S1-T08 |
| `S1-T10` | 600 | Componentes reutilizables base (botones, campos de texto, tarjetas, chips) | BCJL | hecha | S1-T09 |
| `S1-T11` | 550 | Componentes de estado: carga, vacío, error y mensajes al usuario | LMM | hecha | S1-T09 |
| `S1-T12` | 500 | Navegación con Navigation Compose y definición del grafo de rutas | RRC | hecha | S1-T06 |
| `S1-T13` | 450 | Interfaces de repositorio y fuente de datos falsa (fake) para desbloquear la UI | GRI | hecha | S1-T07 |
| `S1-T14` | 400 | Wireframes de las pantallas de autenticación e inicio | BCJL | hecha | S1-T08 |
| `S1-T15` | 300 | Pantalla de bienvenida (splash) | LMM | hecha | S1-T10, S1-T12 |
| `S1-T16` | 250 | Estrategia de pruebas y configuración de las pruebas base (JUnit / Compose test) | RRC | hecha | S1-T04 |

## Sprint 2

**Objetivo:** Como usuario (cliente o trabajador) puedo crear mi cuenta, iniciar sesión y completar mi perfil de forma sencilla y segura.

| ID | Prio | Tarea | Resp. | Estado | Depende de |
|---|---|---|---|---|---|
| `S2-T01` | 1000 | Diseño de las pantallas de registro, inicio de sesión y recuperación | BCJL | hecha | — |
| `S2-T02` | 970 | Pantalla de registro con selección de rol (cliente / trabajador) | LMM | hecha | S1-T10, S1-T12 |
| `S2-T03` | 950 | Pantalla de inicio de sesión | RRC | hecha | S1-T10, S1-T12 |
| `S2-T04` | 900 | Validaciones de formularios y mensajes de error | GRI | hecha | S2-T02, S2-T03 |
| `S2-T05` | 850 | ViewModels y estados de UI del flujo de autenticación | BCJL | hecha | S1-T13 |
| `S2-T06` | 800 | Contrato de la API de autenticación (endpoints, payloads y errores) | LMM | pendiente | — |
| `S2-T07` | 750 | Implementación real de autenticación con Supabase Auth | RRC | pendiente | S2-T06 |
| `S2-T08` | 700 | Almacenamiento seguro de la sesión y el token (DataStore cifrado) | GRI | pendiente | S2-T05 |
| `S2-T09` | 650 | Manejo de sesión: inicio automatico, cierre de sesión y expiracion | BCJL | pendiente | S2-T08 |
| `S2-T10` | 600 | Pantalla de recuperación de contraseña | LMM | pendiente | — |
| `S2-T11` | 550 | Diseño y pantalla del perfil de usuario (ver y editar) | RRC | pendiente | — |
| `S2-T12` | 500 | Contrato de la API de perfil de usuario | GRI | pendiente | — |
| `S2-T13` | 450 | Selección y recorte de la foto de perfil (solo interfaz) | BCJL | pendiente | — |
| `S2-T14` | 400 | Almacenamiento real de imagenes en Supabase Storage | LMM | pendiente | S2-T13 |
| `S2-T15` | 300 | Guardas de navegación por sesión y por rol | RRC | pendiente | S2-T09 |
| `S2-T16` | 250 | Pruebas del flujo de autenticación con datos falsos | GRI | pendiente | S2-T05 |

## Sprint 3

**Objetivo:** Como trabajador puedo crear mi perfil y publicar los oficios y servicios que ofrezco, apoyandome en la inteligencia artificial para redactarlos.

| ID | Prio | Tarea | Resp. | Estado | Depende de |
|---|---|---|---|---|---|
| `S3-T01` | 1000 | Diseño de las pantallas de perfil del trabajador y de servicio | LMM | pendiente | — |
| `S3-T02` | 970 | Catálogo de categorías de oficios: definición y datos semilla | RRC | pendiente | — |
| `S3-T03` | 950 | Pantalla del perfil del trabajador (habilidades, experiencia, contacto) | GRI | pendiente | S1-T10 |
| `S3-T04` | 900 | Pantalla de creacion y edicion de un servicio | BCJL | pendiente | S3-T02 |
| `S3-T05` | 850 | Listado 'mis servicios publicados' con acciones (editar, pausar, eliminar) | LMM | pendiente | — |
| `S3-T06` | 800 | ViewModels y estados de UI del módulo del trabajador | RRC | pendiente | S3-T04 |
| `S3-T07` | 750 | Validaciones y reglas de negocio de la publicación de servicios | GRI | pendiente | S1-T13 |
| `S3-T08` | 700 | Contrato de la API de perfil del trabajador y de servicios | BCJL | pendiente | — |
| `S3-T09` | 650 | Persistencia real de perfiles y servicios en Supabase | LMM | pendiente | S3-T08 |
| `S3-T11` | 600 | Diseño del prompt de redacción de perfil y descripción de servicio | GRI | pendiente | — |
| `S3-T10` | 550 | Entrenar el GPT: instrucciones, base de conocimiento y pruebas en ChatGPT | RRC | pendiente | S3-T11 |
| `S3-T12` | 500 | Contrato del proxy de IA (la llave de la API nunca vive dentro de la app) | BCJL | pendiente | S3-T11 |
| `S3-T13` | 450 | Boton 'Redactar con IA' dentro del formulario de servicio (interfaz) | LMM | pendiente | S3-T12 |
| `S3-T14` | 400 | Estado vacío y vista previa del perfil público del trabajador | RRC | pendiente | S3-T13 |
| `S3-T15` | 300 | Pruebas del módulo de perfil y servicios con datos falsos | GRI | pendiente | — |

## Sprint 4

**Objetivo:** Como cliente puedo publicar una solicitud de trabajo y encontrar fácilmente al trabajador adecuado mediante búsqueda, filtros y sugerencias con inteligencia artificial.

| ID | Prio | Tarea | Resp. | Estado | Depende de |
|---|---|---|---|---|---|
| `S4-T01` | 1000 | Diseño de las pantallas de solicitud, búsqueda y resultados | RRC | pendiente | — |
| `S4-T02` | 970 | Pantalla de publicación de una solicitud de trabajo | GRI | pendiente | S3-T02 |
| `S4-T03` | 950 | Listado de resultados y perfil público del trabajador | BCJL | pendiente | S1-T10 |
| `S4-T04` | 900 | Buscador simple por texto | LMM | pendiente | — |
| `S4-T05` | 850 | Filtros de búsqueda (categoría, estado, municipio, precio, calificación) | RRC | pendiente | — |
| `S4-T06` | 800 | Panel del cliente: mis solicitudes y su estado | GRI | pendiente | S4-T03 |
| `S4-T07` | 750 | ViewModels, paginación y ordenamiento de resultados | BCJL | pendiente | — |
| `S4-T08` | 700 | Contrato de la API de solicitudes, búsqueda y filtros | LMM | pendiente | S4-T03 |
| `S4-T09` | 650 | Implementación real de la búsqueda y el filtrado en Supabase | RRC | pendiente | S4-T08 |
| `S4-T10` | 600 | Consumir la API del GPT previamente entrenado desde la Edge Function (PRIORITARIO) | GRI | pendiente | S3-T10, S3-T12 |
| `S4-T11` | 550 | Tope de llamadas de IA por usuario al día y caché de respuestas | BCJL | pendiente | S3-T13 |
| `S4-T12` | 500 | Categorización automática de la solicitud a partir de texto libre | LMM | opcional | S4-T11 |
| `S4-T13` | 450 | Ranking de trabajadores sugeridos para una solicitud (sin ubicacion) | RRC | opcional | S4-T11 |
| `S4-T14` | 400 | Manejo de errores y reintentos | GRI | pendiente | S4-T11 |
| `S4-T15` | 300 | Pruebas del módulo de búsqueda y solicitudes | BCJL | pendiente | — |

> Las tareas `opcional` solo se desarrollan si sobra tiempo tras cerrar
> todo lo obligatorio del sprint. El riesgo lo asume el equipo.

## Sprint 5

**Objetivo:** Como cliente y trabajador nos conectamos: el trabajador se postula, nos contactamos dentro de la aplicación y calificamos el servicio recibido.

| ID | Prio | Tarea | Resp. | Estado | Depende de |
|---|---|---|---|---|---|
| `S5-T01` | 1000 | Diseño de las pantallas de postulación, contacto y reseñas | GRI | pendiente | — |
| `S5-T02` | 970 | Postulación del trabajador a una solicitud | BCJL | pendiente | S4-T02 |
| `S5-T03` | 950 | Bandeja de postulaciones del cliente (aceptar / rechazar) | LMM | pendiente | S5-T02 |
| `S5-T04` | 900 | Gestión del estado de la solicitud (abierta / asignada / cerrada) | RRC | pendiente | S5-T03 |
| `S5-T05` | 850 | Chat interno: lista de conversaciones y detalle (interfaz) | GRI | pendiente | S1-T10 |
| `S5-T06` | 800 | Contrato de la API de mensajería y postulaciones | BCJL | pendiente | — |
| `S5-T07` | 750 | Implementación real de la mensajería en tiempo real (Supabase Realtime) | LMM | pendiente | S5-T06 |
| `S5-T08` | 700 | Pantalla de reseñas y calificaciones | RRC | pendiente | S5-T04 |
| `S5-T09` | 650 | Cálculo y despliegue de la calificación promedio del trabajador | GRI | pendiente | S5-T08 |
| `S5-T10` | 600 | Notificaciones locales de postulaciones y mensajes | BCJL | pendiente | — |
| `S5-T11` | 550 | Integración de módulos y revisión de la navegación completa | LMM | pendiente | — |
| `S5-T12` | 500 | Pruebas de integración del flujo cliente-trabajador | RRC | pendiente | — |

## Sprint 6

**Objetivo:** Como usuario descargo la aplicación desde Google Play y la utilizo de forma estable, segura y disponible.

| ID | Prio | Tarea | Resp. | Estado | Depende de |
|---|---|---|---|---|---|
| `S6-T01` | 1000 | Pruebas generales y correccion de errores criticos | BCJL | pendiente | — |
| `S6-T02` | 970 | Revisión de accesibilidad, textos y consistencia visual | LMM | pendiente | — |
| `S6-T03` | 950 | Optimización de rendimiento y del tamaño del paquete (AAB) | RRC | pendiente | — |
| `S6-T04` | 900 | Revisión de seguridad: llaves, permisos y ofuscación (R8 / ProGuard) | GRI | pendiente | — |
| `S6-T05` | 850 | Despliegue del proyecto de Supabase en producción | BCJL | pendiente | S2-T07 |
| `S6-T06` | 800 | Configuración de la cuenta de Google Play Console | LMM | pendiente | — |
| `S6-T07` | 750 | Generación de la llave de firma y del Android App Bundle firmado | RRC | pendiente | S6-T04 |
| `S6-T08` | 700 | Política de privacidad y formulario de seguridad de los datos | GRI | pendiente | — |
| `S6-T09` | 650 | Ficha de la tienda: nombre, descripción, capturas, icono y gráfico | BCJL | pendiente | S6-T07 |
| `S6-T10` | 600 | Pruebas internas o cerradas y correccion de observaciones | LMM | pendiente | — |
| `S6-T11` | 550 | Envío a revisión y publicación en producción | RRC | pendiente | S6-T10 |
| `S6-T12` | 500 | Documentación final, manual de usuario y entrega | GRI | pendiente | — |

---

## Tickets

Cada tarea tiene su ticket en `docs/tareas/<ID>.md` con objetivo, archivos,
criterios de aceptación y cómo probarla.

Los tickets se redactan **al iniciar cada sprint**, no los seis por adelantado:
detallar hoy el Sprint 5 es trabajo que se va a tirar. Hoy están escritos los
16 del Sprint 1 y el de `S2-T03`. Para los siguientes, ver el prompt 6 de
`docs/proceso/PROMPTS.md`.

> **Los del Sprint 2 se están redactando tarde, tarea por tarea.** `S2-T01` y
> `S2-T02` se trabajaron **sin ticket**, por autorización del líder del
> 2026-09-21, dejando su alcance escrito en el propio entregable. Desde
> `S2-T03` sí hay ticket: el líder ordenó el 2026-09-22 redactarlo antes de
> tomar la tarea. Las 13 restantes del Sprint 2 siguen sin ticket; cada una lo
> necesita antes de empezar.
