package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import mx.donchambitas.app.dominio.modelo.Categoria
import mx.donchambitas.app.dominio.modelo.ServicioFoto
import mx.donchambitas.app.util.TipoError

/** Campos de P-13 en orden visual: el foco salta al primero que falle (5.4). */
enum class CampoServicio { TITULO, CATEGORIA, DESCRIPCION, PRECIO_DESDE, PRECIO_HASTA, UNIDAD }

/** Maximo de fotos por servicio: lo impone la base con ck_foto_posicion (HU-11). */
const val MAXIMO_FOTOS_SERVICIO = 3

/**
 * Una miniatura del bloque de fotos de P-13: ya guardada, o subiendo en su
 * posicion. Especificada en DISENO-PERFIL-SERVICIOS.md, seccion 4.3.
 */
sealed interface FotoEnPantalla {
    val posicion: Int

    data class Guardada(val foto: ServicioFoto) : FotoEnPantalla {
        override val posicion: Int get() = foto.posicion
    }

    data class Subiendo(override val posicion: Int) : FotoEnPantalla
}

/**
 * Estado inmutable de la pantalla Crear o editar servicio (P-13).
 * Especificado en docs/producto/DISENO-PERFIL-SERVICIOS.md, seccion 4.3.
 *
 * Los precios viajan como texto, igual que la experiencia en [EstadoMiPerfil]:
 * el campo puede quedar a medio escribir. Los errores, como recurso.
 *
 * @property servicioId Nulo al crear. Con valor, la pantalla edita ese servicio.
 * @property noExiste Al editar, el servicio ya no esta: se borro desde otro telefono (1.3).
 * @property fotoPorQuitar Foto cuya confirmacion de quitar esta abierta.
 * @property aviso Mensaje de un solo uso para el SnackbarHost.
 * @property cambiosGuardados Evento de un solo uso: "Guardar cambios" salio bien y se regresa a P-12 (4.5).
 */
data class EstadoServicio(
    val cargando: Boolean = true,
    val errorCarga: TipoError? = null,
    val noExiste: Boolean = false,
    val servicioId: String? = null,
    val titulo: String = "",
    val categorias: List<Categoria> = emptyList(),
    val categoriaId: Int? = null,
    val descripcion: String = "",
    val precioDesde: String = "",
    val precioHasta: String = "",
    val unidadPrecio: String = "",
    val fotos: List<FotoEnPantalla> = emptyList(),
    @StringRes val errorTitulo: Int? = null,
    @StringRes val errorCategoria: Int? = null,
    @StringRes val errorDescripcion: Int? = null,
    @StringRes val errorPrecioDesde: Int? = null,
    @StringRes val errorPrecioHasta: Int? = null,
    @StringRes val errorUnidad: Int? = null,
    val errorPantalla: TipoError? = null,
    val guardando: Boolean = false,
    val ia: EstadoBotonIa = EstadoBotonIa.FaltanDatos(),
    val propuestaIa: String? = null,
    val fotoPorQuitar: FotoEnPantalla? = null,
    @StringRes val aviso: Int? = null,
    val cambiosGuardados: Boolean = false
) {
    val creando: Boolean get() = servicioId == null
    val hayPrecio: Boolean get() = precioDesde.isNotBlank() || precioHasta.isNotBlank()
    val subiendoFoto: Boolean get() = fotos.any { it is FotoEnPantalla.Subiendo }

    @StringRes
    fun errorDe(campo: CampoServicio): Int? = when (campo) {
        CampoServicio.TITULO -> errorTitulo
        CampoServicio.CATEGORIA -> errorCategoria
        CampoServicio.DESCRIPCION -> errorDescripcion
        CampoServicio.PRECIO_DESDE -> errorPrecioDesde
        CampoServicio.PRECIO_HASTA -> errorPrecioHasta
        CampoServicio.UNIDAD -> errorUnidad
    }
}

/**
 * Lo que un campo de precio deja escribir (4.4): digitos y un solo separador,
 * coma o punto, con hasta dos decimales. Lo demas se descarta al teclear.
 */
fun filtrarPrecio(valor: String): String {
    val limpio = valor.filter { it.isDigit() || it == '.' || it == ',' }
    val separador = limpio.indexOfFirst { it == '.' || it == ',' }
    if (separador == -1) return limpio
    val entera = limpio.substring(0, separador)
    val decimales = limpio.substring(separador + 1).filter(Char::isDigit).take(2)
    return entera + limpio[separador] + decimales
}
