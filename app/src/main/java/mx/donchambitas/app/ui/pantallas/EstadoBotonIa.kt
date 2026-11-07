package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import mx.donchambitas.app.R
import mx.donchambitas.app.util.TipoError

/**
 * Estado del boton "Redactar con IA" de P-11 y P-13.
 * Especificado en docs/producto/DISENO-PERFIL-SERVICIOS.md, seccion 1.9.
 *
 * En ninguno de los cuatro el campo de descripcion deja de ser editable: la IA
 * propone, el usuario escribe (HU-31).
 */
sealed interface EstadoBotonIa {
    /** Faltan los datos de los que sale la entrada; el boton dice cuales. */
    data object FaltanDatos : EstadoBotonIa

    /** @property restantesHoy Usos que quedan hoy, o nulo si todavia no se conocen. */
    data class Disponible(val restantesHoy: Int? = null) : EstadoBotonIa

    data object Pidiendo : EstadoBotonIa

    /** Se gastaron las 10 llamadas del dia, en total (DEC-18). */
    data object LimiteAlcanzado : EstadoBotonIa
}

/**
 * El estado del boton tras cambiar los datos de los que sale la entrada (1.9).
 * Mientras pide o con el limite alcanzado no cambia: escribir no lo destraba.
 */
fun EstadoBotonIa.recalcular(faltanDatos: Boolean): EstadoBotonIa = when {
    this == EstadoBotonIa.Pidiendo || this == EstadoBotonIa.LimiteAlcanzado -> this
    faltanDatos -> EstadoBotonIa.FaltanDatos
    else -> EstadoBotonIa.Disponible((this as? EstadoBotonIa.Disponible)?.restantesHoy)
}

/**
 * El aviso cuando la IA no entrega un texto (1.9 y CONTRATOS-API.md). Con el limite
 * alcanzado no hay aviso: el propio boton ya lo dice. La entrada que el modelo
 * rechaza llega como VALIDACION (H-37) y tiene su aviso, porque "no respondio"
 * no es cierto: si respondio, y dijo que no.
 */
@StringRes
internal fun avisoIaDe(tipo: TipoError): Int? = when (tipo) {
    TipoError.LIMITE_IA -> null
    TipoError.VALIDACION -> R.string.ia_aviso_no_pudo
    else -> R.string.ia_aviso_fallo
}

/** El estado del boton tras un fallo: el limite lo deshabilita; lo demas lo deja como estaba. */
internal fun estadoIaTrasFallo(tipo: TipoError, antes: EstadoBotonIa.Disponible): EstadoBotonIa =
    if (tipo == TipoError.LIMITE_IA) EstadoBotonIa.LimiteAlcanzado else antes
