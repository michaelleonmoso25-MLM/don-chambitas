package mx.donchambitas.app.ui.navegacion

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Puente entre MainActivity, que recibe el enlace del correo, y la
 * navegacion, que tiene que llevar al usuario a P-18 (DEC-27, pasos 5 y 6 de
 * CONTRATOS-API.md). Vive lo que vive el proceso: el enlace puede llegar antes
 * de que exista cualquier ViewModel.
 */
@Singleton
class AvisosEnlace @Inject constructor() {

    private val _recuperacion = Channel<Unit>(Channel.CONFLATED)

    /** Un aviso por cada enlace de recuperacion cuya sesion ya quedo importada. */
    val recuperacion: Flow<Unit> = _recuperacion.receiveAsFlow()

    @Volatile
    private var contrasenaPendiente = false

    fun avisarRecuperacion() {
        contrasenaPendiente = true
        _recuperacion.trySend(Unit)
    }

    /**
     * Lo consulta P-18 al abrirse: true una sola vez despues de cada aviso, para
     * abrir la seccion de contrasena solo cuando se llego por el enlace.
     */
    fun tomarContrasenaPendiente(): Boolean {
        val pendiente = contrasenaPendiente
        contrasenaPendiente = false
        return pendiente
    }
}
