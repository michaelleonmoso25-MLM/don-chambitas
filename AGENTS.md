# Protocolo para agentes de IA

Este archivo es el contrato de trabajo entre el equipo y cualquier agente
(Claude Code, Codex, Cursor, Antigravity u otro). Léelo completo antes de
escribir una sola línea de código.

Si eres una persona: no necesitas leer esto, ve a `README.md`.

---

## 1. Qué eres aquí

Eres un integrante más del equipo, no un asistente que sugiere. Tomas **una**
tarea de la cola, la terminas completa, dejas el repositorio en un estado en
el que otro integrante puede seguir sin preguntarte nada, y te detienes.

No haces dos tareas. No adelantas trabajo de la siguiente. No refactorizas
lo que no te tocó.

---

## 2. Qué leer, y en qué orden

Cuando te pidan la siguiente tarea, lee exactamente esto y nada más:

1. Este archivo.
2. `docs/control/ESTADO.md` — qué se está haciendo ahora mismo.
3. `docs/control/PENDIENTES.md` — decisiones congeladas.
4. `docs/tareas/INDICE.md` — la cola completa de tareas.

Con esos cuatro decides cuál sigue. **Todavía no abras ningún ticket.**

Una vez elegida la tarea, abre **solo** su ticket en `docs/tareas/` y **solo**
los documentos de referencia que ese ticket cite. No leas los 86 tickets. No
leas la carpeta completa de documentación. Cada archivo que abres de más es
presupuesto que le quitas al equipo.

---

## 3. Cómo eliges la siguiente tarea

1. Descarta toda tarea de un sprint **posterior** al sprint en curso, el que
   dice `docs/control/ESTADO.md`. Las de sprints anteriores que quedaron
   abiertas sí cuentan: se arrastran conservando su prioridad (`PROCESO.md`).
2. Descarta toda tarea con estado `hecha` o `en curso`.
3. Descarta toda tarea `bloqueada`.
4. Descarta toda tarea cuyas dependencias no estén en `hecha`.
5. De lo que quede, toma la de **mayor prioridad numérica**.
6. Si hay empate, toma la del sprint más bajo.

**El paso 1 no es opcional.** Cada sprint usa su propia escala de 1000 a 250,
así que la prioridad solo ordena dentro del sprint: sin ese filtro, una tarea
de 1000 del sprint siguiente le ganaría a una de 850 del sprint en curso.

Si después del paso 4 no queda ninguna tarea, **detente y repórtalo**. No
abras el sprint siguiente por tu cuenta: eso lo decide el líder.

Las tareas se trabajan en secuencia. La cola es la verdad, no tu criterio
sobre qué convendría hacer primero.

---

## 4. Lo que reportas antes de empezar

Antes de escribir código, responde con este bloque y espera confirmación:

```
PENDIENTES QUE REQUIEREN AL LÍDER: <n>
  - <id>: <decisión> (bloquea <n> tareas)

SIGUIENTE TAREA: <ID> — <título>
  Prioridad: <n> | Sprint: <n> | Responsable nominal: <nombre>
  Dependencias: <ids> (todas hechas)
  Archivos que voy a tocar: <lista>

¿Continúo?
```

Si no hay pendientes, escribe `PENDIENTES QUE REQUIEREN AL LÍDER: 0`. Igual se
reporta. El líder necesita ver el cero.

---

## 5. Reglas que no se rompen

**No tocas nada bloqueado.** Una tarea marcada `bloqueada` en `INDICE.md`
depende de una decisión que solo el líder puede tomar. No la implementes, no
la implementes "parcialmente", no la implementes "para dejarla lista". Ni
siquiera si la tarea parece trivial y la decisión parece obvia. Repórtala y
sigue con la siguiente desbloqueada.

**No editas `PENDIENTES.md` ni `DECISIONES.md`.** Los lee el agente, los
escribe el líder. Si encuentras una decisión nueva que hay que tomar, no la
agregues: repórtala en tu respuesta y que el líder la registre.

**No inventas alcance.** Si el ticket no lo pide, no existe. `PRODUCTO.md`
tiene una lista de lo que quedó explícitamente fuera del MVP; consúltala antes
de agregar cualquier cosa que se te ocurra que "haría falta".

**No inventas contratos.** Si necesitas un endpoint que no está en
`CONTRATOS-API.md`, detente y repórtalo. No lo inventes sobre la marcha.

**No metes la llave de la API de OpenAI en la aplicación.** Nunca, por ningún
motivo, ni en pruebas. Ver `IA.md`.

**No cambias de stack.** Kotlin, Compose, Hilt, Navigation Compose y
`supabase-kt`. Si crees que otra biblioteca resolvería mejor el problema, dilo
en tu respuesta y sigue con la que está definida.

---

## 6. Cómo trabajas con la capa de datos

El backend es **Supabase** (DEC-16, decidido el 2026-09-14) y el cliente es
`supabase-kt` (DEC-17). PEND-01 está cerrado y **no queda ninguna tarea
bloqueada en la cola**.

Que la decisión ya esté tomada **no** te autoriza a saltarte este diseño. La
regla sigue igual:

- Toda pantalla y todo ViewModel hablan con una **interfaz de repositorio**,
  nunca con una fuente de datos concreta, y nunca con Supabase directamente.
- La implementación activa hoy **sigue siendo la falsa** (`FuenteDatosFalsa`),
  en memoria. Las implementaciones reales entran una por una, en su tarea, en
  su turno: S2-T07, S2-T12 (solo `RepositorioUsuario`, por `DEC-31`), S2-T14,
  S3-T09, S4-T09, S4-T16 (`RepositorioSolicitudes`, por `DEC-38`), S5-T07, S5-T13
  (`RepositorioPostulaciones` y `RepositorioResenas`, por `DEC-43`).
- Si tu tarea no es una de esas ocho, **no escribes código que hable con
  Supabase**. Aunque ya se pueda. Aunque sea de una línea.

Cuando una implementación real aterriza, se cambia el enlace en el módulo de
Hilt y nada más. Ni una pantalla se toca.

**Las llaves.** En la aplicación viven la URL del proyecto y la `anon key`, y
nada más. La `anon key` es pública por diseño; lo que protege los datos son las
políticas RLS. La `service_role` key y la llave de OpenAI **no entran al
repositorio por ningún motivo**: la de OpenAI vive en la Edge Function, la
`service_role` en la consola de Supabase.

## 7. Cuándo una tarea está terminada

Todas estas, sin excepción:

- [ ] Los criterios de aceptación del ticket se cumplen, uno por uno.
- [ ] El proyecto compila: `./gradlew assembleDebug` sin errores.
- [ ] Las pruebas pasan: `./gradlew testDebugUnitTest`.
- [ ] La aplicación se instaló y se probó en un dispositivo o emulador real.
- [ ] No quedaron `TODO`, código comentado ni funciones muertas.
- [ ] `docs/control/ESTADO.md` actualizado.
- [ ] `docs/tareas/INDICE.md` con la tarea en `hecha`.
- [ ] `docs/control/BITACORA.md` con un renglón nuevo.
- [ ] Rama y commits con la nomenclatura de `PROCESO.md`.

Si los últimos tres no se hicieron, la tarea **no está terminada** y el pull
request no se aprueba. Actualizar la documentación es parte del trabajo, no un
extra.

---

## 8. Qué haces al final de tu turno

1. Actualiza los tres archivos vivos.
2. Deja la rama creada con los commits hechos.
3. **No abras el pull request tú.** Lo abre la persona.
4. Reporta:

```
TAREA TERMINADA: <ID> — <título>
  Archivos creados: <lista>
  Archivos modificados: <lista>
  Cómo probarlo: <pasos concretos en el dispositivo>
  Compila: sí | Pruebas: <n> pasan
  Rama: <nombre>

SIGUIENTE EN LA COLA: <ID> — <título>
```

Y te detienes. No empieces la siguiente.

---

## 9. Cuando algo no cuadra

Si el ticket está mal, contradice a otro documento, o descubres que la tarea
depende de algo que nadie previó: **detente y repórtalo**. No lo resuelvas por
tu cuenta. Un ticket mal escrito que se corrige cuesta diez minutos; un ticket
mal escrito que se implementa cuesta un sprint.

---

## 10. Mapa de la documentación

| Necesitas saber | Archivo |
|---|---|
| Qué sigue y en qué estado va todo | `docs/tareas/INDICE.md` |
| Qué se está haciendo ahora | `docs/control/ESTADO.md` |
| Qué está congelado esperando al líder | `docs/control/PENDIENTES.md` |
| Qué ya se decidió y por qué | `docs/control/DECISIONES.md` |
| Qué se ha hecho hasta hoy | `docs/control/BITACORA.md` |
| Qué es el producto y qué quedó fuera | `docs/producto/PRODUCTO.md` |
| Qué pantallas existen | `docs/producto/PANTALLAS.md` |
| Qué hace la IA y con qué límites | `docs/producto/IA.md` |
| Cómo está armado el proyecto | `docs/tecnico/ARQUITECTURA.md` |
| Cómo es la base de datos | `docs/tecnico/MODELO-ER.md` |
| Cómo levantar la base y qué hay en cada script | `basedatos/README.md` |
| Qué operaciones existen y qué devuelven | `docs/tecnico/CONTRATOS-API.md` |
| Colores, tipografía, componentes | `docs/tecnico/DISENO.md` |
| Cómo se nombra todo | `docs/tecnico/CONVENCIONES.md` |
| Flujo de git y definición de hecho | `docs/proceso/PROCESO.md` |
| Prompts listos para usar | `docs/proceso/PROMPTS.md` |
