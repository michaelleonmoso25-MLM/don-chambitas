package mx.donchambitas.app.ui.navegacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth

/**
 * Escucha la sesión mientras la aplicación está abierta, uno por actividad.
 * Lleva el [EstadoSesion] que leen las guardas y las barras (S2-T15), avisa
 * cuando quien estaba adentro se queda sin sesión, sea porque la cerró o
 * porque el token de refresco dejó de servir (CONTRATOS-API.md,
 * "sesionActual()"), y cuando se entró por el enlace de recuperación (DEC-27).
 */
@HiltViewModel
class SesionViewModel @Inject constructor(
    private val repositorioAuth: RepositorioAuth,
    private val avisosEnlace: AvisosEnlace
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoSesion.VERIFICANDO)
    private val _sinSesion = Channel<Unit>(Channel.CONFLATED)
    private val _entradaPorRecuperacion = Channel<RolUsuario>(Channel.CONFLATED)

    /** La sesión real, con su rol. Arranca en VERIFICANDO. */
    val estado: StateFlow<EstadoSesion> = _estado.asStateFlow()

    /**
     * Un aviso cada vez que alguien que estaba adentro se queda sin sesión.
     * El `null` del arranque no es aviso: a dónde ir sin sesión lo decide P-01.
     */
    val sinSesion: Flow<Unit> = _sinSesion.receiveAsFlow()

    /**
     * El rol de quien acaba de entrar por el enlace de recuperación. Cuando
     * llega, [estado] ya tiene ese rol, para que la guarda deje pasar a P-18.
     */
    val entradaPorRecuperacion: Flow<RolUsuario> = _entradaPorRecuperacion.receiveAsFlow()

    init {
        viewModelScope.launch {
            try {
                repositorioAuth.sesionActual().collect { sesion ->
                    if (sesion == null) {
                        val habiaAlguienAdentro = _estado.value.conSesion()
                        _estado.value = EstadoSesion.SIN_SESION
                        if (habiaAlguienAdentro) _sinSesion.send(Unit)
                    } else {
                        _estado.value = estadoDe(sesion.usuario.rol)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Sin llaves en local.properties el flujo lanza: no hay sesión
                // que escuchar y la aplicación sigue abierta.
                _estado.value = EstadoSesion.SIN_SESION
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
                if (sesion != null) {
                    _estado.value = estadoDe(sesion.usuario.rol)
                    _entradaPorRecuperacion.send(sesion.usuario.rol)
                }
            }
        }
    }

    /**
     * P-01, P-02 y P-03 avisan con qué rol hicieron entrar al usuario. Hace
     * falta porque sesionActual() no emite si la ficha no se pudo leer (sin
     * red al abrir), y el usuario puede entrar después con "Reintentar".
     */
    fun registrarEntrada(rol: RolUsuario) {
        _estado.value = estadoDe(rol)
    }

    private fun estadoDe(rol: RolUsuario): EstadoSesion = when (rol) {
        RolUsuario.CLIENTE -> EstadoSesion.CLIENTE
        RolUsuario.TRABAJADOR -> EstadoSesion.TRABAJADOR
    }

    private fun EstadoSesion.conSesion(): Boolean =
        this == EstadoSesion.CLIENTE || this == EstadoSesion.TRABAJADOR
}
