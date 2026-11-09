package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import mx.donchambitas.app.dominio.modelo.Categoria
import mx.donchambitas.app.dominio.modelo.EstadoSolicitud
import mx.donchambitas.app.dominio.modelo.Solicitud
import mx.donchambitas.app.util.TipoError

/**
 * Estado de Mis solicitudes (P-09), HU-13.
 * Especificado en docs/producto/DISENO-SOLICITUDES-BUSQUEDA.md, seccion 5.
 *
 * @property solicitudes Ya ordenadas con [ordenarMisSolicitudes].
 * @property categorias Para poner nombre a la categoria de cada tarjeta.
 * @property aviso Mensaje de un solo uso para el SnackbarHost.
 */
data class EstadoMisSolicitudes(
    val cargando: Boolean = true,
    val errorCarga: TipoError? = null,
    val solicitudes: List<Solicitud> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    @StringRes val aviso: Int? = null
) {
    /** HU-13: sin solicitudes sale el estado vacio, nunca una lista en blanco. */
    val vacio: Boolean get() = !cargando && errorCarga == null && solicitudes.isEmpty()
}

/** Las que siguen vivas: el cliente las quiere a la vista antes que las terminadas (5.1). */
private fun EstadoSolicitud.esActiva(): Boolean =
    this == EstadoSolicitud.ABIERTA || this == EstadoSolicitud.ASIGNADA

/** Activas primero y terminadas despues; en cada grupo, la mas reciente arriba (5.1). */
fun ordenarMisSolicitudes(solicitudes: List<Solicitud>): List<Solicitud> =
    solicitudes.sortedWith(
        compareByDescending<Solicitud> { it.estatus.esActiva() }.thenByDescending { it.creadoEn }
    )

private val LOCALE_MEXICO: Locale = Locale.forLanguageTag("es-MX")

/** La fecha de la tarjeta, como dia/mes/anio, en la zona del telefono. */
fun textoFechaSolicitud(creadoEn: Instant, zona: ZoneId = ZoneId.systemDefault()): String =
    DateTimeFormatter.ofPattern("dd/MM/yyyy", LOCALE_MEXICO).withZone(zona).format(creadoEn)
