package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import mx.donchambitas.app.dominio.modelo.ResumenConversacion
import mx.donchambitas.app.util.TipoError

/**
 * Estado de Conversaciones (P-15), HU-25.
 * Especificado en docs/producto/DISENO-POSTULACION-CONTACTO-RESENAS.md, seccion 4.
 *
 * @property conversaciones Ya ordenadas por el mensaje mas reciente (lo hace la lectura).
 * @property aviso Mensaje de un solo uso para el SnackbarHost.
 */
data class EstadoConversaciones(
    val cargando: Boolean = true,
    val errorCarga: TipoError? = null,
    val conversaciones: List<ResumenConversacion> = emptyList(),
    @StringRes val aviso: Int? = null
) {
    /** HU-25: sin conversaciones sale el estado vacio que explica como empieza un contacto. */
    val vacio: Boolean get() = !cargando && errorCarga == null && conversaciones.isEmpty()
}
