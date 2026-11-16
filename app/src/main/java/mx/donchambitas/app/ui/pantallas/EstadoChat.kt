package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import java.time.Instant
import mx.donchambitas.app.dominio.modelo.ParticipanteChat
import mx.donchambitas.app.util.TipoError

/** Maximo de caracteres de un mensaje (propuesta H-52 en CONTRATOS-API.md). */
const val MAXIMO_CARACTERES_MENSAJE = 1000

/** Donde va un mensaje: entregado, en camino o que no se pudo enviar (5.2). */
enum class SituacionMensaje { ENVIADO, ENVIANDO, FALLIDO }

/**
 * Un mensaje tal como lo pinta P-16: el de la base mas su situacion local.
 *
 * @property leidoPorOtro Solo importa en los propios: la otra persona ya lo vio (leido_en).
 */
data class MensajeEnPantalla(
    val id: String,
    val contenido: String,
    val creadoEn: Instant,
    val esMio: Boolean,
    val situacion: SituacionMensaje = SituacionMensaje.ENVIADO,
    val leidoPorOtro: Boolean = false
)

/**
 * Estado de Chat (P-16), HU-26.
 * Especificado en docs/producto/DISENO-POSTULACION-CONTACTO-RESENAS.md, seccion 5.
 *
 * @property mensajes Del mas reciente al mas antiguo, como llegan; la pantalla los pinta al reves.
 * @property aviso Mensaje de un solo uso para el SnackbarHost.
 */
data class EstadoChat(
    val cargando: Boolean = true,
    val errorCarga: TipoError? = null,
    val otraPersona: ParticipanteChat? = null,
    val solicitudTitulo: String? = null,
    val mensajes: List<MensajeEnPantalla> = emptyList(),
    val borrador: String = "",
    val hayMasAntiguos: Boolean = false,
    val cargandoAnteriores: Boolean = false,
    @StringRes val aviso: Int? = null
) {
    /** 5.5: un hilo recien abierto, sin mensajes, invita a escribir el primero. */
    val sinMensajes: Boolean get() = !cargando && errorCarga == null && mensajes.isEmpty()

    /** El boton de enviar esta apagado con el texto en blanco o pasado del tope (1.6). */
    val puedeEnviar: Boolean
        get() = borrador.isNotBlank() && borrador.trim().length <= MAXIMO_CARACTERES_MENSAJE
}
