package mx.donchambitas.app.ui.navegacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth

/**
 * Escucha la sesión mientras la aplicación está abierta, uno por actividad.
 * Avisa cuando Auth se queda sin sesión, sea porque el usuario la cerró o
 * porque el token de refresco dejó de servir (CONTRATOS-API.md,
 * "sesionActual()"). Entrar lo siguen decidiendo P-01, P-02 y P-03.
 */
@HiltViewModel
class SesionViewModel @Inject constructor(
    private val repositorioAuth: RepositorioAuth
) : ViewModel() {

    private val _sinSesion = Channel<Unit>(Channel.CONFLATED)

    /**
     * Un aviso por cada vez que Auth reporta que no hay sesión, incluido el
     * del arranque. Quien lo recibe decide si había alguien adentro.
     */
    val sinSesion: Flow<Unit> = _sinSesion.receiveAsFlow()

    init {
        viewModelScope.launch {
            try {
                repositorioAuth.sesionActual().collect { sesion ->
                    if (sesion == null) _sinSesion.send(Unit)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Sin llaves en local.properties el flujo lanza: no hay sesión
                // que escuchar y la aplicación sigue abierta.
            }
        }
    }

    /**
     * No navega: si el cierre sale bien, sesionActual() emite null y el aviso
     * llega solo. Si falla (sin red), la sesión sigue abierta.
     */
    fun cerrarSesion() {
        viewModelScope.launch { repositorioAuth.cerrarSesion() }
    }
}
