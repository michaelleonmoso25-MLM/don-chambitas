package mx.donchambitas.app.dominio.modelo

import java.math.BigDecimal
import java.time.Instant

/**
 * Solicitud de trabajo publicada por un cliente.
 * Corresponde a la tabla `public.solicitudes`.
 */
data class Solicitud(
    val id: String,
    val clienteId: String,
    val rolCliente: RolUsuario = RolUsuario.CLIENTE,
    val categoriaId: Int,
    val titulo: String,
    val descripcion: String,
    val presupuesto: BigDecimal? = null,
    val estadoId: Int? = null,
    val municipioId: Int? = null,
    val estatus: EstadoSolicitud = EstadoSolicitud.ABIERTA,
    val trabajadorId: String? = null,
    val categorizadaPorIa: Boolean = false,
    val creadoEn: Instant,
    val actualizadoEn: Instant,
    val cerradaEn: Instant? = null,
    /**
     * Cuantas postulaciones siguen en `enviada`, o sea sin decidir (DEC-37, H-42).
     * Solo lo llena obtenerMisSolicitudes, que lee de vw_mis_solicitudes; en las
     * demas operaciones vale 0 aunque haya postulaciones.
     */
    val postulacionesSinRevisar: Int = 0
)

/**
 * Estados posibles del flujo de una solicitud de trabajo.
 * Corresponde al tipo enum `public.estado_solicitud` en PostgreSQL.
 */
enum class EstadoSolicitud(val valor: String) {
    ABIERTA("abierta"),
    ASIGNADA("asignada"),
    CERRADA("cerrada"),
    CANCELADA("cancelada");

    companion object {
        fun desdeValor(valor: String): EstadoSolicitud? =
            entries.firstOrNull { it.valor.equals(valor, ignoreCase = true) }
    }
}
