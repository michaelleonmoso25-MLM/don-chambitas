package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.SavedStateHandle
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
 * RepositorioCatalogos.buscarTrabajadores, ordenada por calificacion y con el texto del
 * buscador (S4-T04). El texto se manda tal cual: que ignore acentos es H-40 y es de la
 * base (S4-T08 y S4-T09). Hoy el repositorio es el falso: la busqueda real es S4-T09.
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

    /** El texto con el que se hizo la ultima busqueda; no cambia mientras se escribe. */
    private var textoAplicado: String = ""

    private var busqueda: Job? = null

    init {
        cargar()
    }

    /** Solo actualiza el campo: cada busqueda pega en la base (3.4 del diseno). */
    fun alCambiarTexto(valor: String) {
        _estado.update { it.copy(texto = valor) }
    }

    /** Aplica el texto escrito, sin espacios en los extremos, y pide la primera pagina. */
    fun alBuscar() {
        textoAplicado = _estado.value.texto.trim()
        cargar()
    }

    /** Vacia el campo y vuelve a la lista sin texto. */
    fun alBorrarTexto() {
        _estado.update { it.copy(texto = "") }
        alBuscar()
    }

    /** Repite la busqueda aplicada, no lo que se haya escrito despues sin buscar. */
    fun alReintentarCarga() {
        cargar()
    }

    private fun cargar() {
        // Una busqueda nueva cancela la anterior para que una respuesta tardia no la pise.
        busqueda?.cancel()
        _estado.update { it.copy(cargando = true, errorCarga = null) }
        busqueda = viewModelScope.launch {
            val filtros = FiltrosBusquedaTrabajadores(
                texto = textoAplicado.ifBlank { null },
                categoriaId = categoriaId,
                orden = OrdenBusqueda.PROMEDIO
            )
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
