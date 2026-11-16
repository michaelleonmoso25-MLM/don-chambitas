package mx.donchambitas.app.datos.falso

import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import mx.donchambitas.app.dominio.modelo.Conversacion
import mx.donchambitas.app.dominio.modelo.Mensaje
import mx.donchambitas.app.dominio.modelo.ParticipanteChat
import mx.donchambitas.app.dominio.modelo.ResumenConversacion
import mx.donchambitas.app.dominio.repositorio.RepositorioChat
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

@Singleton
class RepositorioChatFalso @Inject constructor(
    private val fuente: FuenteDatosFalsa
) : RepositorioChat {

    var retrasoMs: Long = 300L
    var errorForzado: TipoError? = null

    private val flujoMensajes = MutableSharedFlow<Mensaje>(extraBufferCapacity = 64)
    private val flujoCambios = MutableSharedFlow<Unit>(extraBufferCapacity = 64)

    private suspend fun verificarSimulacion(): Resultado.Error? {
        if (retrasoMs > 0) delay(retrasoMs)
        return errorForzado?.let { Resultado.Error(it, "Error simulado en RepositorioChat: $it") }
    }

    override suspend fun obtenerConversaciones(): Resultado<List<ResumenConversacion>> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        val lista = fuente.conversaciones
            .filter { it.clienteId == usuarioId || it.trabajadorId == usuarioId }
            .sortedByDescending { it.ultimoMensajeEn ?: it.creadoEn }
            .mapNotNull { conversacion ->
                val otroId = if (conversacion.clienteId == usuarioId) conversacion.trabajadorId else conversacion.clienteId
                val otro = fuente.usuarios.firstOrNull { it.id == otroId } ?: return@mapNotNull null
                val mensajes = fuente.mensajes.filter { it.conversacionId == conversacion.id }
                ResumenConversacion(
                    id = conversacion.id,
                    solicitudId = conversacion.solicitudId,
                    solicitudTitulo = fuente.solicitudes.firstOrNull { it.id == conversacion.solicitudId }?.titulo,
                    otraPersona = ParticipanteChat(otro.id, otro.nombre, otro.apellidos, otro.fotoUrl),
                    ultimoMensaje = mensajes.maxByOrNull { it.creadoEn }?.contenido,
                    ultimoMensajeEn = conversacion.ultimoMensajeEn,
                    sinLeer = mensajes.count { it.emisorId != usuarioId && it.leidoEn == null }
                )
            }

        return Resultado.Exito(lista)
    }

    override suspend fun abrirConversacion(
        trabajadorId: String,
        solicitudId: String?
    ): Resultado<Conversacion> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        // DEC-20: La conversacion la abre siempre el cliente
        val usuario = fuente.usuarios.firstOrNull { it.id == usuarioId }
        val existente = fuente.conversaciones.firstOrNull {
            it.clienteId == usuarioId && it.trabajadorId == trabajadorId && it.solicitudId == solicitudId
        }

        if (existente != null) {
            return Resultado.Exito(existente)
        }

        val ahora = Instant.now()
        val nueva = Conversacion(
            id = "conv-${UUID.randomUUID().toString().take(8)}",
            clienteId = usuarioId,
            trabajadorId = trabajadorId,
            solicitudId = solicitudId,
            creadoEn = ahora,
            ultimoMensajeEn = ahora
        )
        fuente.conversaciones.add(nueva)
        flujoCambios.tryEmit(Unit)
        return Resultado.Exito(nueva)
    }

    override suspend fun obtenerMensajes(
        conversacionId: String,
        pagina: Int
    ): Resultado<List<Mensaje>> {
        verificarSimulacion()?.let { return it }

        // CONTRATOS-API.md: pagina desde 1, de 30 en 30, del mas reciente al mas antiguo.
        val mensajesConv = fuente.mensajes
            .filter { it.conversacionId == conversacionId }
            .sortedByDescending { it.creadoEn }
            .drop((pagina - 1) * TAMANO_PAGINA)
            .take(TAMANO_PAGINA)

        return Resultado.Exito(mensajesConv)
    }

    override suspend fun enviar(
        conversacionId: String,
        contenido: String
    ): Resultado<Mensaje> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        val ahora = Instant.now()
        val nuevoMensaje = Mensaje(
            id = "msg-${UUID.randomUUID().toString().take(8)}",
            conversacionId = conversacionId,
            emisorId = usuarioId,
            contenido = contenido,
            leidoEn = null,
            creadoEn = ahora
        )

        fuente.mensajes.add(nuevoMensaje)

        val convIndex = fuente.conversaciones.indexOfFirst { it.id == conversacionId }
        if (convIndex != -1) {
            val conv = fuente.conversaciones[convIndex]
            fuente.conversaciones[convIndex] = conv.copy(ultimoMensajeEn = ahora)
        }

        flujoMensajes.tryEmit(nuevoMensaje)
        flujoCambios.tryEmit(Unit)
        return Resultado.Exito(nuevoMensaje)
    }

    override suspend fun marcarLeidos(conversacionId: String): Resultado<Unit> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        val ahora = Instant.now()
        fuente.mensajes.indices.forEach { i ->
            val m = fuente.mensajes[i]
            if (m.conversacionId == conversacionId && m.emisorId != usuarioId && m.leidoEn == null) {
                fuente.mensajes[i] = m.copy(leidoEn = ahora)
            }
        }
        flujoCambios.tryEmit(Unit)
        return Resultado.Exito(Unit)
    }

    override fun mensajesNuevos(conversacionId: String): Flow<Mensaje> {
        return flujoMensajes.filter { it.conversacionId == conversacionId }
    }

    override fun cambiosEnConversaciones(): Flow<Unit> = flujoCambios

    private companion object {
        const val TAMANO_PAGINA = 30
    }
}
