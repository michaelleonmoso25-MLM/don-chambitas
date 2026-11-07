# Inteligencia artificial

## La regla que no se rompe

**La llave de la API de OpenAI nunca vive dentro de la aplicación.** Ni en el
código, ni en `local.properties`, ni en `BuildConfig`, ni ofuscada, ni en una
rama de pruebas. Un APK se descompila en dos minutos y esa llave está ligada a
una tarjeta.

La aplicación llama a **un proxy nuestro**, que con DEC-16 es una **Edge
Function de Supabase**. La función tiene la llave, en sus variables de entorno, y
llama a OpenAI. Si un ticket te pide lo contrario, el ticket está mal.

## Un aclaración importante sobre los GPT

Un GPT configurado dentro de ChatGPT **no se puede consumir por API**. No
existe un endpoint que lo exponga, y la suscripción de ChatGPT Plus no incluye
créditos de API: son dos plataformas con facturación separada.

Lo que sí hacemos:

1. Se diseña el prompt de redacción. Eso es **S3-T11**, y quedó en
   `docs/producto/PROMPT-REDACCION.md`.
2. Se arma y se afina el "GPT" dentro de ChatGPT, cómodamente, hasta que las
   instrucciones den buenos resultados. Eso es **S3-T10**.
3. Esas instrucciones se copian tal cual como *system prompt* en la Edge
   Function, cuyo contrato es **S3-T12**.
4. La Edge Function llama a la API de OpenAI con ese system prompt. Eso es
   **S4-T10**, la tarea marcada PRIORITARIO.

Los 20 dólares de Plus sirven para diseñar el prompt. El consumo real se paga
aparte, por tokens, en la plataforma de desarrolladores.

## Las tres funciones

### F1 · Redactar perfil y descripción de servicio — PRIORITARIA

El usuario escribe 3 o 4 datos sueltos y la IA devuelve un texto presentable.

- **Dónde:** botón "Redactar con IA" en P-11, P-13 y P-08.
- **Entrada:** oficio, años de experiencia y habilidades en P-11; título,
  categoría y borrador en P-13. El formato exacto y las instrucciones del
  modelo están en `PROMPT-REDACCION.md`.
- **Salida:** de 40 a 80 palabras, en primera persona, sin inventar
  certificaciones ni precios.
- **Qué pasa si falla:** el campo se queda como estaba y se muestra un aviso.
  El usuario siempre puede escribir a mano.

Esta es la que sí o sí va. Si solo alcanza el tiempo para una, es esta.

### F2 · Categorizar la solicitud — OPCIONAL

Lee el texto libre del cliente en P-08 y sugiere la categoría.

- El usuario **siempre** puede cambiarla. La IA sugiere, no decide.
- Se guarda `solicitudes.categorizada_por_ia = true` para poder medir qué tan
  bien lo hace.

### F3 · Sugerir trabajadores — OPCIONAL

Dada una solicitud, ordena a los trabajadores de la categoría por qué tan bien
encajan, usando su perfil y sus reseñas.

- **Sin ubicación.** El filtro geográfico ya lo hace el catálogo.
- Si falla, se cae al orden por calificación promedio. Nunca se rompe la
  pantalla.

## Riesgo asumido

F2 y F3 están marcadas como **opcionales** en el índice de tareas. Se
desarrollan solo si sobra tiempo después de cerrar todo lo demás del sprint, y
**el riesgo de no terminarlas lo asume el equipo**, no el líder. Nadie debe
empezar F2 o F3 con tareas obligatorias abiertas.

## Control de costo

El presupuesto es prácticamente cero. Tres candados:

**Tope diario por usuario.** La tabla `ia_consumo` lleva llamadas por usuario,
día y función. El límite es de **10 llamadas por usuario por día EN TOTAL**,
sumando las cuatro funciones, no 10 por cada una (DEC-18). La desagregación por
función existe para poder medir, no para repartir el presupuesto. Al llegar al
tope, el botón se deshabilita con el mensaje "Alcanzaste el límite de hoy". No es
un error, es una condición normal.

**Caché.** La tabla `ia_cache` guarda la respuesta indexada por el hash SHA-256
del texto de entrada normalizado **y la función**. Dos peticiones idénticas se
cobran una vez. Se consulta la caché **antes** de llamar a OpenAI, siempre.

La llave primaria es `(funcion, entrada_hash)`, las dos cosas. Con el hash
solo, el mismo texto pedido a `redactar_perfil` y a `categorizar` se pisaba y
devolvía la respuesta equivocada.

**Modelo barato y salida corta.** El modelo más económico disponible y
`max_tokens` acotado a lo que la función necesita. Nada de dejarlo abierto.

## Contrato del proxy

Un solo endpoint, servido por la Edge Function. El detalle está en
`docs/tecnico/CONTRATOS-API.md`.

```
POST /ia/generar
{
  "funcion": "redactar_perfil" | "redactar_servicio" | "categorizar" | "sugerir",
  "entrada": "texto del usuario"
}
```

Respuestas: `200` con el resultado, `429` cuando se alcanzó el tope diario,
`503` cuando OpenAI no responde. La aplicación trata `429` y `503` como
situaciones normales con mensaje al usuario, nunca como una caída.

## Dónde vive el proxy

Es una **Edge Function de Supabase** (DEC-16, 2026-09-14). La llave de OpenAI
vive en las variables de entorno del proyecto de Supabase, que no están en el
repositorio ni pueden llegar al APK.

La función hace, en este orden: valida la sesión del usuario que llama, consulta
`ia_cache`, llama a `fn_ia_registrar_llamada` para el tope, y solo entonces
llama a OpenAI. Si cualquiera de los tres primeros pasos resuelve, no se paga
nada.

`ia_consumo` e `ia_cache` tienen RLS activo y **cero políticas**: con la
`anon key` ahí no entra nadie. Solo la Edge Function, con la `service_role`
key, que se salta RLS. Por eso esa llave no puede estar en ningún otro lado.
