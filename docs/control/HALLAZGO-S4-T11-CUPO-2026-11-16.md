# Hallazgo de consumo y reservas S4-T11 — 2026-11-16

Cotejo de consultas SQL compartidas por el líder en Supabase.

| Rol | Función | Consumo | Reservas observadas |
|---|---|---:|---|
| Cliente | `redactar_perfil` | 5 llamadas, 3,951 tokens | 5 confirmadas, 3 liberadas |
| Cliente | `redactar_servicio` | 4 llamadas, 3,506 tokens | 4 confirmadas, 1 liberada |
| Trabajador | `redactar_perfil` | 9 llamadas, 5,534 tokens | 7 confirmadas, 1 liberada |
| Trabajador | `redactar_servicio` | 1 llamada, 0 tokens | 1 liberada |

No había reservas pendientes vigentes. La consulta unió las reservas con el
consumo agregado por usuario, fecha y función; por eso repite los mismos
contadores de consumo en cada fila de reserva. Esos valores repetidos no se deben
sumar. Para el cliente, las confirmadas coinciden con las llamadas por función.
Para el trabajador, hay dos llamadas de perfil sin una reserva confirmada visible
y una llamada de servicio pese a que su reserva visible está liberada.

La evidencia no atribuye cada consumo a un ID de reserva ni determina si la
causa son registros anteriores, un despliegue distinto u otra operación. No
concluye doble cobro ni un defecto concreto en la RPC. La implementación vigente
documentada en la rama de corrección devuelve el cupo al repetir una confirmación,
sin incrementar de nuevo `ia_consumo`. Correlacionar ID, fecha, función y estado
con los logs de `ia-generar` y revisar qué versión estaba desplegada en el
momento de las peticiones. No modificar las filas productivas durante el análisis.

S4-T11 queda pendiente de aclarar este hallazgo antes de declararse terminada.
El registro de implementación y pruebas de las ramas de corrección está en
`fix/S4-T11-reservas-idempotentes`; este documento deja constancia en `main` sin
integrar el código de esa rama.
