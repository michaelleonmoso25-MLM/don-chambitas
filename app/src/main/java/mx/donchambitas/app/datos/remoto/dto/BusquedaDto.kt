package mx.donchambitas.app.datos.remoto.dto

import java.math.BigDecimal
import java.time.OffsetDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mx.donchambitas.app.datos.remoto.supabase.SerializadorBigDecimal
import mx.donchambitas.app.dominio.modelo.ResumenTrabajadorBusqueda

/**
 * Una fila de public.vw_busqueda_trabajadores (CONTRATOS-API.md, "buscarTrabajadores").
 *
 * foto_url, precio_desde y los cuatro campos de ubicacion son nulos en la vista:
 * el trabajador puede no tener foto, servicios ni ubicacion (H-48). texto_busqueda
 * no se lee: solo se filtra, y pedirlo de vuelta cuesta bytes por fila sin usarlo.
 */
@Serializable
data class ResumenTrabajadorDto(
    @SerialName("trabajador_id") val trabajadorId: String,
    val nombre: String,
    val apellidos: String,
    @SerialName("foto_url") val fotoUrl: String? = null,
    val titulo: String,
    val disponible: Boolean,
    @SerialName("estado_id") val estadoId: Int? = null,
    @SerialName("municipio_id") val municipioId: Int? = null,
    val estado: String? = null,
    val municipio: String? = null,
    val promedio: Double,
    @SerialName("total_resenas") val totalResenas: Int,
    @SerialName("servicios_activos") val serviciosActivos: Int,
    @Serializable(with = SerializadorBigDecimal::class)
    @SerialName("precio_desde") val precioDesde: BigDecimal? = null,
    val categorias: List<Int> = emptyList(),
    @SerialName("creado_en") val creadoEn: String
) {
    fun aResumenTrabajador(): ResumenTrabajadorBusqueda = ResumenTrabajadorBusqueda(
        trabajadorId = trabajadorId,
        nombre = nombre,
        apellidos = apellidos,
        fotoUrl = fotoUrl,
        titulo = titulo,
        disponible = disponible,
        estadoId = estadoId,
        municipioId = municipioId,
        estado = estado,
        municipio = municipio,
        promedio = promedio,
        totalResenas = totalResenas,
        serviciosActivos = serviciosActivos,
        precioDesde = precioDesde,
        categorias = categorias,
        creadoEn = OffsetDateTime.parse(creadoEn).toInstant()
    )
}
