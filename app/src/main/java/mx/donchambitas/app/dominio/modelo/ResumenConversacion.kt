package mx.donchambitas.app.dominio.modelo

import java.time.Instant

/**
 * La otra persona de un hilo de chat: nombre y foto, de `public.usuarios`.
 * Especificado en docs/tecnico/CONTRATOS-API.md, "Postulaciones" y "Chat".
 */
data class ParticipanteChat(
    val id: String,
    val nombre: String,
    val apellidos: String,
    val fotoUrl: String? = null
) {
    val nombreCompleto: String get() = "$nombre $apellidos".trim()
}

/**
 * Una fila de la bandeja de chats (P-15): todo lo que la pantalla pinta de un hilo, en una sola
 * lectura (vw_mis_conversaciones, H-50). Un hilo sin mensajes tiene [ultimoMensaje] y
 * [ultimoMensajeEn] nulos.
 */
data class ResumenConversacion(
    val id: String,
    val solicitudId: String? = null,
    val solicitudTitulo: String? = null,
    val otraPersona: ParticipanteChat,
    val ultimoMensaje: String? = null,
    val ultimoMensajeEn: Instant? = null,
    val sinLeer: Int = 0
)
