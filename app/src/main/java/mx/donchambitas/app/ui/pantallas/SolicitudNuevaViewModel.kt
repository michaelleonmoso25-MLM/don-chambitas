package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.donchambitas.app.dominio.modelo.Solicitud
import mx.donchambitas.app.dominio.repositorio.RepositorioCatalogos
import mx.donchambitas.app.dominio.repositorio.RepositorioSolicitudes
import mx.donchambitas.app.dominio.validacion.ValidacionesSolicitud
import mx.donchambitas.app.ui.componentes.obtenerMensajeErrorRes
import mx.donchambitas.app.util.Resultado

/**
 * ViewModel de Publicar solicitud (P-08), HU-12.
 * Especificado en docs/producto/DISENO-SOLICITUDES-BUSQUEDA.md, secciones 4.3 a 4.5.
 *
 * Valida con ValidacionesSolicitud cuando dice 1.5: al perder el foco un campo ya
 * tocado y todos al publicar, con el foco en el primero. No hay boton de IA
 * (H-29): no existe funcion para redactar la solicitud.
 */
@HiltViewModel
class SolicitudNuevaViewModel @Inject constructor(
    private val repositorioSolicitudes: RepositorioSolicitudes,
    private val repositorioCatalogos: RepositorioCatalogos
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoSolicitudNueva())
    val estado: StateFlow<EstadoSolicitudNueva> = _estado.asStateFlow()

    private var cargaMunicipios: Job? = null

    /** Los campos en los que ya se escribio: solo esos se validan al perder el foco (1.5). */
    private var tocados: Set<CampoSolicitud> = emptySet()

    init {
        cargar()
    }

    fun alReintentarCarga() {
        cargar()
    }

    private fun cargar() {
        _estado.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            val categorias = repositorioCatalogos.obtenerCategorias()
            val estados = repositorioCatalogos.obtenerEstados()
            val error = listOf(categorias, estados).filterIsInstance<Resultado.Error>().firstOrNull()
            if (error != null) {
                _estado.update { it.copy(cargando = false, errorCarga = error.tipo) }
                return@launch
            }
            _estado.update {
                it.copy(
                    cargando = false,
                    categorias = (categorias as Resultado.Exito).dato,
                    estados = (estados as Resultado.Exito).dato
                )
            }
        }
    }

    fun alCambiarTitulo(valor: String) {
        tocados = tocados + CampoSolicitud.TITULO
        _estado.update { it.copy(titulo = valor, errorTitulo = null) }
    }

    fun alElegirCategoria(id: Int) {
        _estado.update { it.copy(categoriaId = id, errorCategoria = null) }
    }

    fun alCambiarDescripcion(valor: String) {
        tocados = tocados + CampoSolicitud.DESCRIPCION
        _estado.update { it.copy(descripcion = valor, errorDescripcion = null) }
    }

    fun alCambiarPresupuesto(valor: String) {
        tocados = tocados + CampoSolicitud.PRESUPUESTO
        _estado.update { it.copy(presupuesto = filtrarPrecio(valor), errorPresupuesto = null) }
    }

    /** Otro estado borra el municipio y trae solo los suyos (HU-12). */
    fun alElegirEstado(id: Int) {
        if (id == _estado.value.estadoId) return
        _estado.update {
            it.copy(
                estadoId = id,
                municipioId = null,
                municipios = emptyList(),
                errorEstado = null,
                errorMunicipio = null
            )
        }
        cargaMunicipios?.cancel()
        cargaMunicipios = viewModelScope.launch {
            when (val resultado = repositorioCatalogos.obtenerMunicipios(id)) {
                is Resultado.Exito -> _estado.update { it.copy(municipios = resultado.dato) }
                is Resultado.Error -> _estado.update { it.copy(aviso = obtenerMensajeErrorRes(resultado.tipo)) }
            }
        }
    }

    fun alElegirMunicipio(id: Int) {
        _estado.update { it.copy(municipioId = id, errorMunicipio = null) }
    }

    fun alPerderFoco(campo: CampoSolicitud) {
        if (campo in tocados) _estado.update { it.validado(campo) }
    }

    /**
     * Valida todo y, con algun error, no llama al repositorio: la pantalla lleva el
     * foco al primero (1.5). Si pasa, crea la solicitud, que nace abierta (4.5).
     */
    fun alPublicar() {
        val actual = _estado.value
        if (actual.publicando || actual.cargando) return
        val validado = CampoSolicitud.entries.fold(actual) { parcial, campo -> parcial.validado(campo) }
        if (CampoSolicitud.entries.any { validado.errorDe(it) != null }) {
            _estado.value = validado
            return
        }
        _estado.update { it.copy(publicando = true, errorPantalla = null) }
        viewModelScope.launch {
            when (val resultado = repositorioSolicitudes.crear(armarSolicitud(actual))) {
                is Resultado.Exito -> _estado.update { it.copy(publicando = false, publicada = true) }
                is Resultado.Error -> _estado.update { it.copy(publicando = false, errorPantalla = resultado.tipo) }
            }
        }
    }

    /** La pantalla ya navego a P-09: el evento se consume. */
    fun alConsumirPublicada() {
        _estado.update { it.copy(publicada = false) }
    }

    fun alConsumirAviso() {
        _estado.update { it.copy(aviso = null) }
    }

    /**
     * El id y el cliente van vacios: los pone quien crea la fila (el falso hoy; la
     * implementacion real en S4-T09 los toma de la sesion, como P-11 con el usuario).
     */
    private fun armarSolicitud(estado: EstadoSolicitudNueva): Solicitud {
        val ahora = Instant.now()
        return Solicitud(
            id = "",
            clienteId = "",
            categoriaId = requireNotNull(estado.categoriaId),
            titulo = estado.titulo.trim(),
            descripcion = estado.descripcion.trim(),
            presupuesto = ValidacionesSolicitud.normalizarPresupuesto(estado.presupuesto),
            estadoId = estado.estadoId,
            municipioId = estado.municipioId,
            creadoEn = ahora,
            actualizadoEn = ahora
        )
    }
}

/** El campo con su regla de 4.2 aplicada. */
private fun EstadoSolicitudNueva.validado(campo: CampoSolicitud): EstadoSolicitudNueva = when (campo) {
    CampoSolicitud.TITULO ->
        copy(errorTitulo = ValidacionesSolicitud.validarTitulo(titulo)?.mensaje())
    CampoSolicitud.CATEGORIA ->
        copy(errorCategoria = ValidacionesSolicitud.validarCategoria(categoriaId)?.mensaje())
    CampoSolicitud.DESCRIPCION ->
        copy(errorDescripcion = ValidacionesSolicitud.validarDescripcion(descripcion)?.mensaje())
    CampoSolicitud.PRESUPUESTO ->
        copy(errorPresupuesto = ValidacionesSolicitud.validarPresupuesto(presupuesto)?.mensaje())
    CampoSolicitud.ESTADO ->
        copy(errorEstado = ValidacionesSolicitud.validarEstado(estadoId)?.mensaje())
    CampoSolicitud.MUNICIPIO ->
        copy(errorMunicipio = ValidacionesSolicitud.validarMunicipio(estadoId, municipioId)?.mensaje())
}
