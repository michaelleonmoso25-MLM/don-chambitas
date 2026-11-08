package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth
import mx.donchambitas.app.dominio.repositorio.RepositorioTrabajador
import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.util.Resultado

/**
 * ViewModel de Perfil publico (P-07), HU-08.
 * Especificado en docs/producto/DISENO-PERFIL-SERVICIOS.md, seccion 11.
 *
 * El id del trabajador sale del argumento de la ruta y se pide con
 * RepositorioTrabajador.obtenerPerfilPublico. La pantalla tiene dos modos (H-39): si el
 * perfil es el de la sesion es la vista previa del trabajador; si no, es lo que ve el cliente.
 */
@HiltViewModel
class PerfilPublicoViewModel @Inject constructor(
    private val repositorioTrabajador: RepositorioTrabajador,
    private val repositorioAuth: RepositorioAuth,
    estadoGuardado: SavedStateHandle
) : ViewModel() {

    private val trabajadorId: String =
        estadoGuardado.get<String>(Ruta.PerfilTrabajador.ARG_TRABAJADOR_ID).orEmpty()

    private val _estado = MutableStateFlow(EstadoPerfilPublico())
    val estado: StateFlow<EstadoPerfilPublico> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun alReintentarCarga() {
        cargar()
    }

    private fun cargar() {
        _estado.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            when (val resultado = repositorioTrabajador.obtenerPerfilPublico(trabajadorId)) {
                is Resultado.Exito -> {
                    val usuarioId = repositorioAuth.sesionActual().first()?.usuario?.id
                    _estado.update {
                        it.copy(
                            cargando = false,
                            perfil = resultado.dato,
                            esVistaPrevia = usuarioId != null && usuarioId == trabajadorId
                        )
                    }
                }
                is Resultado.Error -> _estado.update {
                    it.copy(cargando = false, perfil = null, errorCarga = resultado.tipo)
                }
            }
        }
    }
}
