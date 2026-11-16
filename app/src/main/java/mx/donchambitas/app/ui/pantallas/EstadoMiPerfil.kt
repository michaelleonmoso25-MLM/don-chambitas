package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import mx.donchambitas.app.dominio.modelo.Estado
import mx.donchambitas.app.dominio.modelo.Municipio
import mx.donchambitas.app.dominio.modelo.PerfilTrabajador
import mx.donchambitas.app.util.TipoError

/**
 * Campos de P-11 que se validan al guardar, en orden visual: el foco salta al
 * primero que falle (5.4). La habilidad nueva no esta: se valida al agregarla.
 */
enum class CampoMiPerfil { TITULO, EXPERIENCIA, MUNICIPIO, TELEFONO }

/** Lo que el aviso de perfil incompleto puede listar, en el orden en que se lista (2.5). */
enum class FaltaPerfil { DESCRIPCION, HABILIDAD, UBICACION, TELEFONO }

/**
 * Estado inmutable de la pantalla Mi perfil (P-11).
 * Especificado en docs/producto/DISENO-PERFIL-SERVICIOS.md, seccion 2.2.
 *
 * Los errores viajan como identificador de recurso, igual que en
 * [EstadoRegistro]. Los textos que el usuario escribe se guardan tal cual,
 * incluida la experiencia, que puede quedar a medio escribir.
 *
 * @property perfilGuardado Ya existe la fila en perfiles_trabajador; sin ella no hay vista previa.
 * @property faltantes Lo que le falta al perfil guardado. Se calcula sobre lo guardado y no
 * sobre lo que se escribe, para que el aviso no aparezca y desaparezca con cada letra.
 * @property soloFaltanHabilidades El perfil se guardo y las habilidades no: el error en linea lo
 * dice y "Reintentar" repite solo ese paso (2.4).
 * @property propuestaIa Texto de la IA pendiente de confirmar antes de reemplazar la descripcion.
 * @property aviso Mensaje de un solo uso para el SnackbarHost, como `destino` en P-02.
 */
data class EstadoMiPerfil(
    val cargando: Boolean = true,
    val errorCarga: TipoError? = null,
    val perfilGuardado: Boolean = false,
    val usuarioId: String = "",
    val titulo: String = "",
    val experienciaAnios: String = "",
    val habilidadNueva: String = "",
    val habilidades: List<String> = emptyList(),
    val descripcion: String = "",
    val estados: List<Estado> = emptyList(),
    val municipios: List<Municipio> = emptyList(),
    val estadoId: Int? = null,
    val municipioId: Int? = null,
    val telefonoContacto: String = "",
    val disponible: Boolean = true,
    val faltantes: List<FaltaPerfil> = emptyList(),
    @StringRes val errorTitulo: Int? = null,
    @StringRes val errorExperiencia: Int? = null,
    @StringRes val errorHabilidad: Int? = null,
    @StringRes val errorMunicipio: Int? = null,
    @StringRes val errorTelefono: Int? = null,
    val errorPantalla: TipoError? = null,
    val soloFaltanHabilidades: Boolean = false,
    val guardando: Boolean = false,
    val ia: EstadoBotonIa = EstadoBotonIa.FaltanDatos(),
    val propuestaIa: String? = null,
    @StringRes val aviso: Int? = null
) {
    @StringRes
    fun errorDe(campo: CampoMiPerfil): Int? = when (campo) {
        CampoMiPerfil.TITULO -> errorTitulo
        CampoMiPerfil.EXPERIENCIA -> errorExperiencia
        CampoMiPerfil.MUNICIPIO -> errorMunicipio
        CampoMiPerfil.TELEFONO -> errorTelefono
    }
}

/**
 * Lo que le falta al perfil guardado, en el orden del aviso (2.5). Sin perfil
 * falta todo. El titulo no se lista: sin el no se puede guardar.
 */
fun calcularFaltantes(perfil: PerfilTrabajador?, habilidades: List<String>): List<FaltaPerfil> {
    if (perfil == null) return FaltaPerfil.entries
    return buildList {
        if (perfil.descripcion.isNullOrBlank()) add(FaltaPerfil.DESCRIPCION)
        if (habilidades.isEmpty()) add(FaltaPerfil.HABILIDAD)
        if (perfil.estadoId == null || perfil.municipioId == null) add(FaltaPerfil.UBICACION)
        if (perfil.telefonoContacto.isNullOrBlank()) add(FaltaPerfil.TELEFONO)
    }
}
