package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.repositorio.RepositorioCatalogos
import mx.donchambitas.app.dominio.repositorio.RepositorioSolicitudes
import mx.donchambitas.app.ui.componentes.obtenerMensajeErrorRes
import mx.donchambitas.app.util.Resultado

/**
 * ViewModel de Mis solicitudes (P-09), HU-13.
 * Especificado en docs/producto/DISENO-SOLICITUDES-BUSQUEDA.md, seccion 5.
 *
 * Pide las solicitudes del cliente con RepositorioSolicitudes.obtenerMisSolicitudes. Hoy ese
 * repositorio es el falso: lo publicado se pierde al cerrar la app hasta S4-T09.
 */
@HiltViewModel
class MisSolicitudesViewModel @Inject constructor(
    private val repositorioSolicitudes: RepositorioSolicitudes,
    private val repositorioCatalogos: RepositorioCatalogos
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoMisSolicitudes())
    val estado: StateFlow<EstadoMisSolicitudes> = _estado.asStateFlow()

    private var cargadoUnaVez = false
    private var carga: Job? = null

    /**
     * La primera vez tapa la pantalla con el cargando; despues recarga sin quitar la lista, y si
     * falla solo avisa. Se llama en cada ON_RESUME, para que lo publicado en P-08 aparezca al volver.
     *
     * @param solicitudPublicada Se vuelve de P-08 tras "Publicar": el aviso sale aqui (4.5).
     */
    fun alCargar(solicitudPublicada: Boolean = false) {
        _estado.update {
            it.copy(
                cargando = !cargadoUnaVez,
                errorCarga = null,
                aviso = if (solicitudPublicada) R.string.solicitud_publicada_aviso else it.aviso
            )
        }
        carga?.cancel()
        carga = viewModelScope.launch {
            val solicitudes = repositorioSolicitudes.obtenerMisSolicitudes()
            val categorias = repositorioCatalogos.obtenerCategorias()
            val error = listOf(solicitudes, categorias).filterIsInstance<Resultado.Error>().firstOrNull()
            _estado.update {
                when {
                    error != null && cargadoUnaVez -> it.copy(aviso = obtenerMensajeErrorRes(error.tipo))
                    error != null -> it.copy(cargando = false, errorCarga = error.tipo)
                    else -> it.copy(
                        cargando = false,
                        solicitudes = ordenarMisSolicitudes((solicitudes as Resultado.Exito).dato),
                        categorias = (categorias as Resultado.Exito).dato
                    )
                }
            }
            if (error == null) cargadoUnaVez = true
        }
    }

    fun alReintentarCarga() {
        alCargar()
    }

    fun alConsumirAviso() {
        _estado.update { it.copy(aviso = null) }
    }
}
