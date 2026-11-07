package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.Categoria
import mx.donchambitas.app.dominio.modelo.Estado
import mx.donchambitas.app.dominio.modelo.Municipio
import mx.donchambitas.app.dominio.validacion.FalloSolicitud
import mx.donchambitas.app.util.TipoError

/** Los campos de P-08, en el orden visual en que el foco busca el primer error (1.5). */
enum class CampoSolicitud { TITULO, CATEGORIA, DESCRIPCION, PRESUPUESTO, ESTADO, MUNICIPIO }

/**
 * Estado de Publicar solicitud (P-08), HU-12.
 * Especificado en docs/producto/DISENO-SOLICITUDES-BUSQUEDA.md, seccion 4.3.
 *
 * @property publicada Evento de un solo uso: la solicitud se creo y hay que ir a P-09.
 * @property aviso Mensaje de un solo uso para el SnackbarHost (falla al traer los municipios).
 */
data class EstadoSolicitudNueva(
    val cargando: Boolean = true,
    val errorCarga: TipoError? = null,
    val titulo: String = "",
    val categoriaId: Int? = null,
    val descripcion: String = "",
    val presupuesto: String = "",
    val estadoId: Int? = null,
    val municipioId: Int? = null,
    val categorias: List<Categoria> = emptyList(),
    val estados: List<Estado> = emptyList(),
    val municipios: List<Municipio> = emptyList(),
    @StringRes val errorTitulo: Int? = null,
    @StringRes val errorCategoria: Int? = null,
    @StringRes val errorDescripcion: Int? = null,
    @StringRes val errorPresupuesto: Int? = null,
    @StringRes val errorEstado: Int? = null,
    @StringRes val errorMunicipio: Int? = null,
    val errorPantalla: TipoError? = null,
    val publicando: Boolean = false,
    val publicada: Boolean = false,
    @StringRes val aviso: Int? = null
) {
    @StringRes
    fun errorDe(campo: CampoSolicitud): Int? = when (campo) {
        CampoSolicitud.TITULO -> errorTitulo
        CampoSolicitud.CATEGORIA -> errorCategoria
        CampoSolicitud.DESCRIPCION -> errorDescripcion
        CampoSolicitud.PRESUPUESTO -> errorPresupuesto
        CampoSolicitud.ESTADO -> errorEstado
        CampoSolicitud.MUNICIPIO -> errorMunicipio
    }
}

/** El recurso validacion_solicitud_* de cada fallo. */
@StringRes
fun FalloSolicitud.mensaje(): Int = when (this) {
    FalloSolicitud.TITULO_VACIO -> R.string.validacion_solicitud_titulo_vacio
    FalloSolicitud.TITULO_LARGO -> R.string.validacion_solicitud_titulo_largo
    FalloSolicitud.CATEGORIA_SIN_ELEGIR -> R.string.validacion_solicitud_categoria_sin_elegir
    FalloSolicitud.DESCRIPCION_VACIA -> R.string.validacion_solicitud_descripcion_vacia
    FalloSolicitud.DESCRIPCION_LARGA -> R.string.validacion_solicitud_descripcion_larga
    FalloSolicitud.PRESUPUESTO_FORMATO -> R.string.validacion_solicitud_presupuesto_formato
    FalloSolicitud.PRESUPUESTO_GRANDE -> R.string.validacion_solicitud_presupuesto_grande
    FalloSolicitud.ESTADO_SIN_ELEGIR -> R.string.validacion_solicitud_estado_sin_elegir
    FalloSolicitud.MUNICIPIO_SIN_ELEGIR -> R.string.validacion_solicitud_municipio_sin_elegir
}
