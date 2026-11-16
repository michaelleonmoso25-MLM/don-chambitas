package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.donchambitas.app.dominio.repositorio.RepositorioChat
import mx.donchambitas.app.ui.componentes.obtenerMensajeErrorRes
import mx.donchambitas.app.util.Resultado

/**
 * ViewModel de Conversaciones (P-15), HU-25 y HU-26.
 * Especificado en docs/producto/DISENO-POSTULACION-CONTACTO-RESENAS.md, seccion 4.
 *
 * Pide la bandeja con RepositorioChat.obtenerConversaciones y la vuelve a pedir, sin tapar la lista,
 * cada vez que cambia una conversacion: asi el hilo con mensaje nuevo sube al principio (HU-26).
 */
@HiltViewModel
class ConversacionesViewModel @Inject constructor(
    private val repositorioChat: RepositorioChat
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoConversaciones())
    val estado: StateFlow<EstadoConversaciones> = _estado.asStateFlow()

    private var cargadoUnaVez = false
    private var carga: Job? = null

    init {
        viewModelScope.launch {
            repositorioChat.cambiosEnConversaciones()
                .catch { /* sin tiempo real la bandeja se actualiza al volver a la pantalla */ }
                .collect { alCargar() }
        }
    }

    /**
     * La primera vez tapa la pantalla con el cargando; despues recarga sin quitar la lista, y si
     * falla solo avisa. Se llama en cada ON_RESUME, para ver lo leido en P-16 al volver.
     */
    fun alCargar() {
        _estado.update { it.copy(cargando = !cargadoUnaVez, errorCarga = null) }
        carga?.cancel()
        carga = viewModelScope.launch {
            val resultado = repositorioChat.obtenerConversaciones()
            _estado.update {
                when {
                    resultado is Resultado.Error && cargadoUnaVez ->
                        it.copy(aviso = obtenerMensajeErrorRes(resultado.tipo))
                    resultado is Resultado.Error -> it.copy(cargando = false, errorCarga = resultado.tipo)
                    else -> it.copy(cargando = false, conversaciones = (resultado as Resultado.Exito).dato)
                }
            }
            if (resultado is Resultado.Exito) cargadoUnaVez = true
        }
    }

    fun alReintentarCarga() {
        alCargar()
    }

    fun alConsumirAviso() {
        _estado.update { it.copy(aviso = null) }
    }
}
