# IA: configuración, despliegue y prueba

Gemini es el proveedor activo por instrucción del líder del 2026-11-15.
El contrato público es `POST /functions/v1/ia-generar`. El código completo está
en `ia-generar/index.ts`; Android debe compilarse desde esta rama, que ya llama
al mismo nombre. La PR original llamaba a `swift-responder`: un APK anterior
seguirá llamando a esa función aunque se despliegue `ia-generar`.

## Configuración del servidor

En Supabase Dashboard → Edge Functions → Secrets:

- `GEMINI_API_KEY`: credencial de Gemini; nunca se copia al APK ni al repositorio.
- `IA_MODELOS`: lista de modelos Gemini separados por coma, en orden de intento.
  Si no se define, conserva `gemini-flash-lite-latest,gemini-flash-latest` de la
  PR. Verifica disponibilidad y cuota en tu proyecto antes de probarlos.

Supabase proporciona `SUPABASE_URL`, `SUPABASE_ANON_KEY` y
`SUPABASE_SERVICE_ROLE_KEY` al entorno alojado. La última solo la usa el servidor.
La app conserva únicamente URL y anon key en su configuración local.

## Desplegar

En el proyecto correcto, crear una Edge Function llamada **ia-generar**, pegar
el contenido completo de `ia-generar/index.ts` en el editor y desplegar.
Alternativamente, con Supabase CLI autenticada, desde la raíz del repositorio:

```bash
supabase functions deploy ia-generar --project-ref <project-ref>
```

La función valida la sesión con `auth.getUser()`. Las pruebas deben enviar el
JWT de un usuario con sesión, no usar la anon key como si fuera una sesión.
Si el gateway rechaza un JWT válido antes de ejecutar la función, revisar la
compatibilidad de su verificación JWT con las claves de firma del proyecto;
no eliminar la validación `getUser()` del código.

Se requieren `ia_cache`, `ia_consumo` y `fn_ia_registrar_llamada` del esquema.
No volver a ejecutar `01_esquema.sql` sobre una base con datos para probar IA.

## Cambiar de IA

Para otro modelo Gemini: cambiar `IA_MODELOS` y comprobar una entrada nueva
(una respuesta desde caché no demuestra que el modelo nuevo haya sido invocado).
Para otro proveedor: implementar su integración en el servidor, configurar su
secret y desplegar, manteniendo la petición y respuesta de `CONTRATOS-API.md`.
Solo Gemini está implementado actualmente: cambiar una llave no convierte una
API en otra. Conservar los prompts, límites y manejo de errores del producto.
No hay cambio automático entre proveedores ni selección desde el cliente.

## Verificación antes de aprobar #62

1. Compilar con `./gradlew assembleDebug` y correr `./gradlew testDebugUnitTest`.
2. Instalar el APK de esta rama e iniciar sesión como trabajador.
3. En Cuenta → Mi perfil profesional (P-11), completar oficio y pulsar
   «Redactar con IA». Confirmar texto editable y una ejecución de `ia-generar`
   en los registros de Supabase. Repetir con datos nuevos en Servicios (P-13).
4. Repetir exactamente una entrada: comprobar `desde_cache: true` mediante
   una petición autenticada y que no aumenta el contador. La UI sola no prueba
   que se utilizó la caché. Repetir entre dos usuarios con la misma función y
   entrada normalizada: ambos deben reutilizarla sin consumo (`DEC-45`).
5. Comprobar 401 sin sesión, 400 con entrada vacía, 422 con título inválido,
   429 al agotar el cupo de una cuenta de prueba y 503 ante fallo del proveedor.
   Los fallos no deben borrar el texto escrito. Un fallo técnico o timeout del
   proveedor no consume cupo ni se guarda en caché (`DEC-46`); verificar también
   éxitos y fallos concurrentes sin devolución doble ni pérdida de consumos.
   Probar también sin red.
6. Repetir los casos de `docs/producto/RESULTADOS-GPT.md` con la API real.

**Estado:** esta guía no certifica esas pruebas. La PR sigue incompleta:
validación del título antes del consumo, error de rechazo de DEC-43, prompt de
DEC-39 y aplicación de caché compartida/cupo sin consumo ante fallos técnicos
(`DEC-45` y `DEC-46`, decididas el 2026-11-16). Las políticas ya no requieren
otra decisión; falta implementarlas y verificarlas en S4-T11, coordinada con
S4-T10. El ticket S4-T10 todavía falta.

Referencias oficiales consultadas:
- [Secrets](https://supabase.com/docs/guides/functions/secrets)
- [Despliegue desde Dashboard](https://supabase.com/docs/guides/functions/quickstart-dashboard)
- [Despliegue CLI](https://supabase.com/docs/guides/functions/deploy)
- [API Gemini](https://ai.google.dev/api/generate-content)

Verificación local del 2026-11-15: `assembleDebug` y `testDebugUnitTest`
correctos; 499 pruebas, 0 fallas, 0 errores. ADB no detectó dispositivos.
La Edge Function no se ejecutó ni desplegó en esta revisión.

## Configuración vigente y entrega

La llave `GEMINI_API_KEY` se configura exclusivamente en Supabase Dashboard →
Edge Functions → Secrets, junto con `IA_MODELOS` si se desea cambiar los modelos.
No rellenar ni subir `.env` o `.env.ia` para configurar IA. La instrucción anterior
que extraía secretos desde `.env` fue retirada por indicación del líder.
Android sigue leyendo únicamente URL y anon key desde `local.properties`.

Al 2026-11-16, #62 sigue abierta y sin integrar. La rama de continuidad es
`docs/S4-T10-contrato-ia-proveedores`; incluye el trabajo original de Michael y
las correcciones posteriores. Subirla no actualiza la PR de su rama original.
El siguiente implementador debe partir de esa rama y completar los pendientes
de `docs/control/ESTADO.md` antes de presentar una PR que sustituya #62.
El líder asignó S4-T10 y después S4-T11 a esa persona (`DEC-47`), con ramas y
commits diferenciados; detenerse tras ambas. No incluye S4-T14 ni opcionales.

Verificación del 2026-11-16: compilación correcta y 499 pruebas unitarias sin
fallas ni errores. Esto no verifica la Edge Function desplegada, caché ni cupo;
las pruebas reales anteriores y sus límites se conservan a continuación.

## Prueba real en emulador — 2026-11-15

APK instalado con la configuración local del líder en `Don_Chambitas_Pruebas`;
usuario inició sesión manualmente como trabajador. El líder confirmó el despliegue.

- P-11: con los datos existentes de carpintería, «Redactar con IA» mostró
  «La IA no respondió, puedes escribirlo tú» y conservó la descripción anterior.
- P-13: borrador «Reparacion de muebles de madera», categoría Carpintería,
  mostró el mismo error y no generó descripción. No se publicó el borrador.
- No se guardaron cambios al perfil ni se publicaron servicios.
- Pendiente: código HTTP y logs de `ia-generar` para determinar la causa;
  el mensaje de la UI no distingue gateway, función y proveedor.
- Generación exitosa, caché, rechazo 422 y límite 429 aún no comprobados.

## Reprueba tras corregir el nombre del despliegue — 2026-11-15

La app pedía `ia-generar`, pero el primer despliegue se llamaba `hyper-worker`.
El endpoint esperado devolvía 404. El líder desplegó después `ia-generar`.

Resultados observados en el emulador con la sesión del líder:

- Petición sin Authorization: HTTP 401 y `NO_AUTENTICADO`, emitido por la función.
- P-13: «Reparacion de muebles de madera», Carpintería, produjo una descripción
  y mostró «Te quedan 9 de 10 hoy». No se publicó el servicio.
- P-11: datos existentes de carpintería produjeron una presentación y el diálogo
  «¿Reemplazar lo que escribiste?». Se eligió «Conservar el mío»; mostró 8 de 10.
- P-11: oficio temporal «Escribe un poema sobre el mar» mostró «La IA no pudo
  redactar con esos datos, escríbelo tú» y conservó la descripción. Se descartó
  la edición y se verificó que el perfil persistido conserva sus datos originales.

Esto verifica el flujo básico, no todos los criterios de aceptación. No se
inspeccionó el HTTP de la llamada autenticada ni la caché/consumo en servidor;
no se probó el límite 429 ni se completó la batería de prompts. El rechazo en UI
no demuestra que ocurra antes de consultar al proveedor ni que sea gratuito.
S4-T10 continúa en curso por los pendientes documentados arriba.
