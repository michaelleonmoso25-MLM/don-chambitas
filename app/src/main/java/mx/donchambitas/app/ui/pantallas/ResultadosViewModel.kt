package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.donchambitas.app.dominio.modelo.FiltrosBusquedaTrabajadores
import mx.donchambitas.app.dominio.modelo.OrdenBusqueda
import mx.donchambitas.app.dominio.repositorio.RepositorioCatalogos
import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.util.Resultado

/**
 * ViewModel de Resultados (P-06), HU-16 y HU-17.
 * Especificado en docs/producto/DISENO-SOLICITUDES-BUSQUEDA.md, seccion 3.
 *
 * La categoria opcional sale del argumento de la ruta (H-45). Pide la primera pagina de
 * RepositorioCatalogos.buscarTrabajadores, ordenada por calificacion. Hoy el repositorio
 * es el falso: la busqueda real es S4-T09.
 */
@HiltViewModel
class ResultadosViewModel @Inject constructor(
    private val repositorioCatalogos: RepositorioCatalogos,
    estadoGuardado: SavedStateHandle
) : ViewModel() {

    /** Nulo cuando la ruta no trae categoria: se listan todos los trabajadores. */
    private val categoriaId: Int? = estadoGuardado.get<Int>(Ruta.Resultados.ARG_CATEGORIA_ID)
        ?.takeIf { it != Ruta.Resultados.SIN_CATEGORIA }

    private val _estado = MutableStateFlow(EstadoResultados())
    val estado: StateFlow<EstadoResultados> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun alReintentarCarga() {
        cargar()
    }

    private fun cargar() {
        _estado.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            val filtros = FiltrosBusquedaTrabajadores(categoriaId = categoriaId, orden = OrdenBusqueda.PROMEDIO)
            when (val resultado = repositorioCatalogos.buscarTrabajadores(filtros, pagina = 1)) {
                is Resultado.Exito -> _estado.update {
                    it.copy(cargando = false, trabajadores = resultado.dato)
                }
                is Resultado.Error -> _estado.update {
                    it.copy(cargando = false, trabajadores = emptyList(), errorCarga = resultado.tipo)
                }
            }
        }
    }
}
