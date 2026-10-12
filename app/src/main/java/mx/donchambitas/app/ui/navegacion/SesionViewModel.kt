package mx.donchambitas.app.ui.navegacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth

/**
 * Escucha la sesión mientras la aplicación está abierta, uno por actividad.
 * Avisa cuando Auth se queda sin sesión, sea porque el usuario la cerró o
 * porque el token de refresco dejó de servir (CONTRATOS-API.md,
 * "sesionActual()"), y cuando se entró por el enlace de recuperación (DEC-27).
 * Entrar por formulario lo siguen decidiendo P-01, P-02 y P-03.
 */
@HiltViewModel
class SesionViewModel @Inject constructor(
    private val repositorioAuth: RepositorioAuth,
    private val avisosEnlace: AvisosEnlace
) : ViewModel() {

    private val _sinSesion = Channel<Unit>(Channel.CONFLATED)
    private val _entradaPorRecuperacion = Channel<RolUsuario>(Channel.CONFLATED)

    /**
     * Un aviso por cada vez que Auth reporta que no hay sesión, incluido el
     * del arranque. Quien lo recibe decide si había alguien adentro.
     */
    val sinSesion: Flow<Unit> = _sinSesion.receiveAsFlow()

    /**
     * El rol de quien acaba de entrar por el enlace de recuperación. Las
     * guardas todavía leen el marcador temporal, y quien lleva a P-18 tiene que
     * dejarlo con ese rol (S2-T15 lo retira).
     */
    val entradaPorRecuperacion: Flow<RolUsuario> = _entradaPorRecuperacion.receiveAsFlow()

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
        viewModelScope.launch {
            avisosEnlace.recuperacion.collect {
                // El aviso llega cuando la sesión ya quedó importada, pero
                // sesionActual() todavía tiene que leer la ficha para saber el rol.
                val sesion = try {
                    repositorioAuth.sesionActual().filterNotNull().first()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    null
                }
                if (sesion != null) _entradaPorRecuperacion.send(sesion.usuario.rol)
            }
        }
    }
}
