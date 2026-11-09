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
 * ViewModel de Resultados (P-06), HU-16, HU-17 y HU-18.
 * Especificado en docs/producto/DISENO-SOLICITUDES-BUSQUEDA.md, seccion 3.
 *
 * Pide paginas de RepositorioCatalogos.buscarTrabajadores con el texto del buscador (S4-T04), los
 * filtros (S4-T05) y el orden y las paginas de [TAMANO_PAGINA] (S4-T07). La categoria opcional de la
 * ruta (H-45) entra como un filtro mas: se ve como chip y se puede quitar.
 *
 * El texto se manda tal cual: que ignore acentos es H-40 y es de la base (S4-T08 y S4-T09).
 * Hoy el repositorio es el falso: la busqueda real es S4-T09.
 */
@HiltViewModel
class ResultadosViewModel @Inject constructor(
    private val repositorioCatalogos: RepositorioCatalogos,
    estadoGuardado: SavedStateHandle
) : ViewModel() {

    private val _estado = MutableStateFlow(
        EstadoResultados(
            filtros = FiltrosBusquedaTrabajadores(
                // Nulo cuando la ruta no trae categoria: se listan todos los trabajadores.
                categoriaId = estadoGuardado.get<Int>(Ruta.Resultados.ARG_CATEGORIA_ID)
                    ?.takeIf { it != Ruta.Resultados.SIN_CATEGORIA },
                // Por omision, los mejor calificados (3.3 del diseno).
                orden = OrdenBusqueda.PROMEDIO
            )
        )
    )
    val estado: StateFlow<EstadoResultados> = _estado.asStateFlow()

    /** El texto con el que se hizo la ultima busqueda; no cambia mientras se escribe. */
    private var textoAplicado: String = ""

    /** La ultima pagina que ya esta en la lista; la siguiente es esta mas uno. */
    private var paginaActual: Int = 1

    private var busqueda: Job? = null
    private var cargaMas: Job? = null
    private var cargaCatalogos: Job? = null
    private var cargaMunicipios: Job? = null

    init {
        cargar()
        // Los chips de filtros necesitan el nombre de la categoria de la ruta, antes de abrir la hoja.
        cargarCatalogos()
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

    /** Cambia el orden y conserva los filtros y el texto; vuelve a la primera pagina (3.3 del diseno). */
    fun alElegirOrden(orden: OrdenBusqueda) {
        _estado.update { it.copy(filtros = it.filtros.copy(orden = orden)) }
        cargar()
    }

    /**
     * La lista llego cerca del final: pide la pagina siguiente si hay mas y no hay otra en camino.
     * Si la anterior fallo no se pide sola: eso es de [alReintentarMas], para no insistir en bucle.
     */
    fun alLlegarAlFinal() {
        val actual = _estado.value
        if (!actual.hayMas || actual.cargando || actual.cargandoMas) return
        if (actual.errorCarga != null || actual.errorAlCargarMas) return
        cargarMas()
    }

    /** Repite la pagina que fallo; la lista no se vacia. */
    fun alReintentarMas() {
        val actual = _estado.value
        if (!actual.errorAlCargarMas || actual.cargandoMas) return
        cargarMas()
    }

    /** Abre la hoja; trae los catalogos si faltan y los municipios del estado aplicado. */
    fun alAbrirFiltros() {
        _estado.update { it.copy(hojaFiltrosAbierta = true, errorCatalogos = null) }
        val actual = _estado.value
        if (actual.categorias.isEmpty() || actual.estados.isEmpty()) cargarCatalogos()
        cargarMunicipios(actual.filtros.estadoId)
    }

    /** Descarta el borrador; los municipios vuelven a ser los del estado aplicado, que son los de los chips. */
    fun alCerrarFiltros() {
        _estado.update { it.copy(hojaFiltrosAbierta = false) }
        cargarMunicipios(_estado.value.filtros.estadoId)
    }

    /** Guarda los filtros de la hoja, la cierra y pide la primera pagina. */
    fun alAplicarFiltros(filtros: FiltrosBusquedaTrabajadores) {
        _estado.update { it.copy(filtros = filtros, hojaFiltrosAbierta = false) }
        cargar()
    }

    /** Quita un filtro desde su chip; quitar el estado quita tambien el municipio, que depende de el. */
    fun alQuitarFiltro(cual: FiltroResultados) {
        _estado.update {
            it.copy(
                filtros = when (cual) {
                    FiltroResultados.CATEGORIA -> it.filtros.copy(categoriaId = null)
                    FiltroResultados.ESTADO -> it.filtros.copy(estadoId = null, municipioId = null)
                    FiltroResultados.MUNICIPIO -> it.filtros.copy(municipioId = null)
                    FiltroResultados.PRECIO -> it.filtros.copy(precioDesdeMax = null)
                    FiltroResultados.CALIFICACION -> it.filtros.copy(promedioMin = null)
                }
            )
        }
        cargar()
    }

    /** Quita los cinco, incluida la categoria que trajo la ruta; el orden no es un filtro y se queda. */
    fun alQuitarTodosLosFiltros() {
        _estado.update {
            it.copy(
                filtros = FiltrosBusquedaTrabajadores(orden = it.filtros.orden),
                hojaFiltrosAbierta = false
            )
        }
        cargar()
    }

    /** La hoja eligio otro estado: trae sus municipios. El borrador del municipio lo limpia la hoja. */
    fun alElegirEstadoEnFiltros(estadoId: Int?) {
        cargarMunicipios(estadoId)
    }

    /** La primera pagina. Cancela la busqueda anterior y cualquier pagina en vuelo, para que no la pisen. */
    private fun cargar() {
        busqueda?.cancel()
        cargaMas?.cancel()
        paginaActual = 1
        _estado.update {
            it.copy(
                cargando = true,
                errorCarga = null,
                cargandoMas = false,
                errorAlCargarMas = false,
                hayMas = true
            )
        }
        busqueda = viewModelScope.launch {
            when (val resultado = repositorioCatalogos.buscarTrabajadores(filtrosDeLaBusqueda(), pagina = 1)) {
                is Resultado.Exito -> _estado.update {
                    it.copy(
                        cargando = false,
                        trabajadores = resultado.dato,
                        hayMas = resultado.dato.size >= TAMANO_PAGINA
                    )
                }
                is Resultado.Error -> _estado.update {
                    it.copy(cargando = false, trabajadores = emptyList(), errorCarga = resultado.tipo)
                }
            }
        }
    }

    /** La pagina siguiente se agrega al final; si falla, la lista se queda y el pie ofrece reintentar. */
    private fun cargarMas() {
        val siguiente = paginaActual + 1
        _estado.update { it.copy(cargandoMas = true, errorAlCargarMas = false) }
        cargaMas = viewModelScope.launch {
            when (val resultado = repositorioCatalogos.buscarTrabajadores(filtrosDeLaBusqueda(), pagina = siguiente)) {
                is Resultado.Exito -> {
                    paginaActual = siguiente
                    _estado.update {
                        // Sin repetidos: la lista usa el identificador como llave y repetirla la rompe.
                        val yaEstan = it.trabajadores.mapTo(HashSet()) { t -> t.trabajadorId }
                        it.copy(
                            cargandoMas = false,
                            trabajadores = it.trabajadores + resultado.dato.filter { t -> t.trabajadorId !in yaEstan },
                            hayMas = resultado.dato.size >= TAMANO_PAGINA
                        )
                    }
                }
                is Resultado.Error -> _estado.update { it.copy(cargandoMas = false, errorAlCargarMas = true) }
            }
        }
    }

    private fun filtrosDeLaBusqueda(): FiltrosBusquedaTrabajadores =
        _estado.value.filtros.copy(texto = textoAplicado.ifBlank { null })

    /** Los catalogos de la hoja: si fallan, la hoja lo dice; los resultados no se bloquean. */
    private fun cargarCatalogos() {
        cargaCatalogos?.cancel()
        _estado.update { it.copy(errorCatalogos = null) }
        cargaCatalogos = viewModelScope.launch {
            val categorias = repositorioCatalogos.obtenerCategorias()
            val estados = repositorioCatalogos.obtenerEstados()
            _estado.update {
                it.copy(
                    categorias = (categorias as? Resultado.Exito)?.dato ?: it.categorias,
                    estados = (estados as? Resultado.Exito)?.dato ?: it.estados,
                    errorCatalogos = (categorias as? Resultado.Error)?.tipo ?: (estados as? Resultado.Error)?.tipo
                )
            }
        }
    }

    private fun cargarMunicipios(estadoId: Int?) {
        cargaMunicipios?.cancel()
        _estado.update { it.copy(municipios = emptyList()) }
        if (estadoId == null) return
        cargaMunicipios = viewModelScope.launch {
            when (val resultado = repositorioCatalogos.obtenerMunicipios(estadoId)) {
                is Resultado.Exito -> _estado.update { it.copy(municipios = resultado.dato) }
                is Resultado.Error -> _estado.update { it.copy(errorCatalogos = resultado.tipo) }
            }
        }
    }
}
