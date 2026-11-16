package mx.donchambitas.app.dominio.repositorio

import kotlinx.coroutines.flow.Flow
import mx.donchambitas.app.dominio.modelo.Conversacion
import mx.donchambitas.app.dominio.modelo.Mensaje
import mx.donchambitas.app.dominio.modelo.ResumenConversacion
import mx.donchambitas.app.util.Resultado

/**
 * Contrato de operaciones de mensajeria instantanea interna.
 * Especificado en docs/tecnico/CONTRATOS-API.md.
 */
interface RepositorioChat {
    /**
     * Obtiene las conversaciones del usuario autenticado ordenadas por fecha del ultimo mensaje.
     */
    suspend fun obtenerConversaciones(): Resultado<List<ResumenConversacion>>

    /**
     * Inicia una conversacion con un trabajador. Segun DEC-20, siempre la abre el cliente.
     */
    suspend fun abrirConversacion(trabajadorId: String, solicitudId: String? = null): Resultado<Conversacion>

    /**
     * Obtiene los mensajes paginados pertenecientes a una conversacion.
     */
    suspend fun obtenerMensajes(conversacionId: String, pagina: Int = 1): Resultado<List<Mensaje>>

    /**
     * Envia un nuevo mensaje en la conversacion.
     */
    suspend fun enviar(conversacionId: String, contenido: String): Resultado<Mensaje>

    /**
     * Marca los mensajes de la conversacion como leidos por el receptor.
     */
    suspend fun marcarLeidos(conversacionId: String): Resultado<Unit>

    /**
     * Suscripcion reactiva a nuevos mensajes en tiempo real para una conversacion.
     */
    fun mensajesNuevos(conversacionId: String): Flow<Mensaje>

    /**
     * Emite Unit cada vez que cambia una conversacion (llego un mensaje): P-15 vuelve a pedir la
     * bandeja. Sin ella, "la bandeja sube ese hilo" (HU-26) no se cumple sin sondear.
     */
    fun cambiosEnConversaciones(): Flow<Unit>
}
