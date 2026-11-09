# Decisiones pendientes

> **Este archivo lo escribe únicamente el líder.** El agente lo lee y lo
> reporta, nunca lo edita.
>
> Una tarea que depende de un pendiente **no se implementa**, ni siquiera
> parcialmente, ni siquiera si la decisión parece obvia.

---

## PEND-01 · Backend y base de datos

**Estado:** resuelto — 2026-09-14
**Quién decidió:** GRI (líder)
**Resultado:** **Supabase.** Ver `DEC-16` y `DEC-17` en
`docs/control/DECISIONES.md`.

### Qué se decidió

Supabase, no backend propio. El cliente de la aplicación es `supabase-kt`, que
cubre Postgrest, Auth, Storage, Realtime y Functions; Retrofit y OkHttp salen
del stack.

El requisito académico de implementar backend se cubre con el esquema
PostgreSQL, las políticas RLS y las Edge Functions: eso es trabajo de servidor,
no configuración.

### Qué se desbloquea

Las seis tareas que este pendiente congelaba pasan a `pendiente`:

| ID | Tarea |
|---|---|
| S2-T07 | Implementación real de autenticación con Supabase Auth |
| S2-T14 | Almacenamiento real de imágenes en Supabase Storage |
| S3-T09 | Persistencia real de perfiles y servicios en Supabase |
| S4-T09 | Implementación real de la búsqueda y el filtrado en Supabase |
| S5-T07 | Implementación real de la mensajería en tiempo real |
| S6-T05 | Despliegue del proyecto de Supabase en producción |

> Antes del 2026-09-14 este bloque nombraba `S3-T10` y `S4-T10`, que son las
> dos tareas de inteligencia artificial. Los IDs correctos son `S3-T09` y
> `S4-T09`, y así quedaron.

Las políticas RLS **dejaron de estar congeladas y ya están escritas**, en
`basedatos/02_politicas_rls.sql`. No son un extra: la `anon key` que lleva la
aplicación es pública, así que son lo único que protege los datos. Las valida
S1-T03.

### Qué no cambia

Toda la aplicación sigue hablando contra interfaces de repositorio, y la
implementación activa sigue siendo `FuenteDatosFalsa` hasta que cada tarea real
aterrice. La decisión no autoriza a saltarse ese diseño: lo único que cambia el
día que una implementación real entra es el enlace en `ModuloRepositorios`.

---

## PEND-02 · Catálogo completo de municipios

**Estado:** resuelto — 2026-09-22
**Quién decidió:** GRI (líder)
**Resultado:** **Se queda con los 26 municipios sembrados.** Ver `DEC-26` en
`docs/control/DECISIONES.md`.

### Qué se decidió

`04_datos_semilla.sql` siembra 26 municipios de zonas de prueba y así se queda.
Los 2,469 del país, que se habrían cargado del catálogo abierto del INEGI con
un `COPY` desde CSV, no entran al MVP.

### Qué no cambia

Nunca bloqueó a nadie, así que no desbloquea nada. `DEC-23` dejó al municipio
como filtro de búsqueda en P-06: `S4-T05` construye y prueba sus cinco filtros
igual, y `S6-T10` corre las pruebas cerradas dentro de las zonas sembradas.

### Si algún día hace falta el catálogo completo

Se carga sin tocar código ni esquema: es dato. La ruta sigue siendo el CSV del
INEGI con un `COPY` contra `municipios`.

---

## PEND-03 · Pausa de la línea de inteligencia artificial

**Estado:** resuelto — 2026-11-09
**Quién decidió:** GRI (líder)
**Resultado:** **Se reanuda entera.** Ver `DEC-33` en
`docs/control/DECISIONES.md`.

### Qué se decidió

La pausa del 2026-10-25, que congeló `S3-T10`, `S3-T11`, `S3-T12`, `S3-T13`,
`S4-T10` y `S4-T11` por indicación del equipo, queda sin efecto. Las cuatro del
Sprint 3 se retomaron el 2026-11-07 (PR #46 a #49) y este registro lo confirma.

### Qué se desbloquea

| ID | Tarea |
|---|---|
| S4-T10 | Consumir la API del GPT desde la Edge Function |
| S4-T11 | Tope de llamadas de IA por usuario al día y caché de respuestas |

Con `S4-T11` se pueden tomar después `S4-T14` y las opcionales `S4-T12` y
`S4-T13`. Lo que esas tareas tienen que aplicar está en `DEC-35` y `DEC-39`.

---

## Plantilla para pendientes nuevos

```
## PEND-NN · Título

**Estado:** abierto | resuelto
**Quién decide:** 
**Bloquea:** N tareas

### La disyuntiva

### Qué ya se hizo para que esto no frene a nadie

### Tareas bloqueadas por esto

### Fecha límite recomendada
```
