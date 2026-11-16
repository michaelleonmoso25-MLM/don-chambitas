package mx.donchambitas.app.datos.remoto.dto

import java.time.OffsetDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mx.donchambitas.app.dominio.modelo.Conversacion
import mx.donchambitas.app.dominio.modelo.Mensaje
import mx.donchambitas.app.dominio.modelo.ParticipanteChat
import mx.donchambitas.app.dominio.modelo.ResumenConversacion

/**
 * Una fila de public.vw_mis_conversaciones (CONTRATOS-API.md, "Chat"): todo lo que pinta P-15.
 * La otra persona es la contraparte de auth.uid(), no el cliente ni el trabajador a secas.
 */
@Serializable
data class ResumenConversacionDto(
    val id: String,
    @SerialName("solicitud_id") val solicitudId: String? = null,
    @SerialName("solicitud_titulo") val solicitudTitulo: String? = null,
    @SerialName("ultimo_mensaje_en") val ultimoMensajeEn: String? = null,
    @SerialName("otro_id") val otroId: String,
    @SerialName("otro_nombre") val otroNombre: String,
    @SerialName("otro_apellidos") val otroApellidos: String,
    @SerialName("otro_foto_url") val otroFotoUrl: String? = null,
    @SerialName("ultimo_mensaje") val ultimoMensaje: String? = null,
    @SerialName("sin_leer") val sinLeer: Int = 0
) {
    fun aResumen(): ResumenConversacion = ResumenConversacion(
        id = id,
        solicitudId = solicitudId,
        solicitudTitulo = solicitudTitulo,
        otraPersona = ParticipanteChat(otroId, otroNombre, otroApellidos, otroFotoUrl),
        ultimoMensaje = ultimoMensaje,
        ultimoMensajeEn = ultimoMensajeEn?.let { OffsetDateTime.parse(it).toInstant() },
        sinLeer = sinLeer
    )
}

/** Una fila de public.conversaciones, la que devuelve abrirConversacion. */
@Serializable
data class ConversacionDto(
    val id: String,
    @SerialName("cliente_id") val clienteId: String,
    @SerialName("trabajador_id") val trabajadorId: String,
    @SerialName("solicitud_id") val solicitudId: String? = null,
    @SerialName("creado_en") val creadoEn: String,
    @SerialName("ultimo_mensaje_en") val ultimoMensajeEn: String? = null
) {
    fun aConversacion(): Conversacion = Conversacion(
        id = id,
        clienteId = clienteId,
        trabajadorId = trabajadorId,
        solicitudId = solicitudId,
        creadoEn = OffsetDateTime.parse(creadoEn).toInstant(),
        ultimoMensajeEn = ultimoMensajeEn?.let { OffsetDateTime.parse(it).toInstant() }
    )
}

/** Una fila de public.mensajes, de una lectura o de un evento de Realtime. */
@Serializable
data class MensajeDto(
    val id: String,
    @SerialName("conversacion_id") val conversacionId: String,
    @SerialName("emisor_id") val emisorId: String,
    val contenido: String,
    @SerialName("leido_en") val leidoEn: String? = null,
    @SerialName("creado_en") val creadoEn: String
) {
    fun aMensaje(): Mensaje = Mensaje(
        id = id,
        conversacionId = conversacionId,
        emisorId = emisorId,
        contenido = contenido,
        leidoEn = leidoEn?.let { OffsetDateTime.parse(it).toInstant() },
        creadoEn = OffsetDateTime.parse(creadoEn).toInstant()
    )
}

/**
 * Lo unico que la aplicacion manda al enviar (CONTRATOS-API.md, "enviar"): leido_en y creado_en los
 * pone la base.
 */
@Serializable
data class MensajeEscrituraDto(
    @SerialName("conversacion_id") val conversacionId: String,
    @SerialName("emisor_id") val emisorId: String,
    val contenido: String
)
