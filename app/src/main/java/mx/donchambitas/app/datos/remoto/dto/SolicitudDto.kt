package mx.donchambitas.app.datos.remoto.dto

import java.math.BigDecimal
import java.time.OffsetDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mx.donchambitas.app.datos.remoto.supabase.SerializadorBigDecimal
import mx.donchambitas.app.dominio.modelo.EstadoPostulacion
import mx.donchambitas.app.dominio.modelo.EstadoSolicitud
import mx.donchambitas.app.dominio.modelo.Postulacion
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.modelo.Solicitud

/**
 * Una fila de public.solicitudes o de public.vw_mis_solicitudes
 * (CONTRATOS-API.md, "Solicitudes").
 *
 * Dos campos opcionales segun de donde venga la fila: postulaciones_sin_revisar
 * solo la trae la vista, y postulaciones solo llega si se pidio el embebido de
 * obtenerDetalle. Sin ellos valen 0 y lista vacia.
 */
@Serializable
data class SolicitudDto(
    val id: String,
    @SerialName("cliente_id") val clienteId: String,
    @SerialName("rol_cliente") val rolCliente: String,
    @SerialName("categoria_id") val categoriaId: Int,
    val titulo: String,
    val descripcion: String,
    @Serializable(with = SerializadorBigDecimal::class)
    val presupuesto: BigDecimal? = null,
    @SerialName("estado_id") val estadoId: Int? = null,
    @SerialName("municipio_id") val municipioId: Int? = null,
    val estatus: String,
    @SerialName("trabajador_id") val trabajadorId: String? = null,
    @SerialName("categorizada_por_ia") val categorizadaPorIa: Boolean = false,
    @SerialName("creado_en") val creadoEn: String,
    @SerialName("actualizado_en") val actualizadoEn: String,
    @SerialName("cerrada_en") val cerradaEn: String? = null,
    @SerialName("postulaciones_sin_revisar") val postulacionesSinRevisar: Int = 0,
    val postulaciones: List<PostulacionDto> = emptyList()
) {
    fun aSolicitud(): Solicitud = Solicitud(
        id = id,
        clienteId = clienteId,
        rolCliente = requireNotNull(RolUsuario.desdeValor(rolCliente)) {
            "Rol desconocido en solicitudes: $rolCliente"
        },
        categoriaId = categoriaId,
        titulo = titulo,
        descripcion = descripcion,
        presupuesto = presupuesto,
        estadoId = estadoId,
        municipioId = municipioId,
        estatus = requireNotNull(EstadoSolicitud.desdeValor(estatus)) {
            "Estatus desconocido en solicitudes: $estatus"
        },
        trabajadorId = trabajadorId,
        categorizadaPorIa = categorizadaPorIa,
        creadoEn = OffsetDateTime.parse(creadoEn).toInstant(),
        actualizadoEn = OffsetDateTime.parse(actualizadoEn).toInstant(),
        cerradaEn = cerradaEn?.let { OffsetDateTime.parse(it).toInstant() },
        postulacionesSinRevisar = postulacionesSinRevisar
    )
}

/** Una fila de public.postulaciones, la que viene embebida en obtenerDetalle. */
@Serializable
data class PostulacionDto(
    val id: String,
    @SerialName("solicitud_id") val solicitudId: String,
    @SerialName("trabajador_id") val trabajadorId: String,
    val mensaje: String? = null,
    @Serializable(with = SerializadorBigDecimal::class)
    @SerialName("precio_propuesto") val precioPropuesto: BigDecimal? = null,
    val estatus: String,
    @SerialName("creado_en") val creadoEn: String,
    @SerialName("actualizado_en") val actualizadoEn: String
) {
    fun aPostulacion(): Postulacion = Postulacion(
        id = id,
        solicitudId = solicitudId,
        trabajadorId = trabajadorId,
        mensaje = mensaje,
        precioPropuesto = precioPropuesto,
        estatus = requireNotNull(EstadoPostulacion.desdeValor(estatus)) {
            "Estatus desconocido en postulaciones: $estatus"
        },
        creadoEn = OffsetDateTime.parse(creadoEn).toInstant(),
        actualizadoEn = OffsetDateTime.parse(actualizadoEn).toInstant()
    )
}

/**
 * Las siete columnas que escribe crear. Sin valores por omision, como
 * ServicioEscrituraDto: un presupuesto null tiene que viajar como null.
 * No lleva id, estatus, trabajador_id, categorizada_por_ia ni fechas: los pone
 * la base (CONTRATOS-API.md, "crear").
 */
@Serializable
data class SolicitudEscrituraDto(
    @SerialName("cliente_id") val clienteId: String,
    @SerialName("categoria_id") val categoriaId: Int,
    val titulo: String,
    val descripcion: String,
    @Serializable(with = SerializadorBigDecimal::class)
    val presupuesto: BigDecimal?,
    @SerialName("estado_id") val estadoId: Int?,
    @SerialName("municipio_id") val municipioId: Int?
)
