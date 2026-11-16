package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.donchambitas.app.dominio.modelo.Mensaje
import mx.donchambitas.app.dominio.repositorio.RepositorioChat
import mx.donchambitas.app.ui.componentes.obtenerMensajeErrorRes
import mx.donchambitas.app.ui.navegacion.ARG_CONVERSACION_ID
import mx.donchambitas.app.util.Resultado

/** Cuantos mensajes trae cada pagina del historial (CONTRATOS-API.md, obtenerMensajes). */
private const val TAMANO_PAGINA_MENSAJES = 30

/**
 * ViewModel de Chat (P-16), HU-26.
 * Especificado en docs/producto/DISENO-POSTULACION-CONTACTO-RESENAS.md, seccion 5.
 *
 * Carga la pagina mas reciente, marca como leidos los recibidos y escucha los mensajes nuevos. Un
 * mensaje propio aparece de inmediato como "enviando" y se confirma al entregarse; si falla,
 * conserva su texto y se puede reintentar (1.4). Hoy el repositorio es el falso: S5-T07 lo hace real.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repositorioChat: RepositorioChat,
    estadoGuardado: SavedStateHandle
) : ViewModel() {

    private val conversacionId: String = checkNotNull(estadoGuardado[ARG_CONVERSACION_ID])

    private val _estado = MutableStateFlow(EstadoChat())
    val estado: StateFlow<EstadoChat> = _estado.asStateFlow()

    private var pagina = 1
    private var siguienteLocal = 0

    init {
        cargar()
        escucharMensajesNuevos()
    }

    private fun cargar() {
        _estado.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            val conversaciones = repositorioChat.obtenerConversaciones()
            val mensajes = repositorioChat.obtenerMensajes(conversacionId, pagina = 1)
            val error = listOf(conversaciones, mensajes).filterIsInstance<Resultado.Error>().firstOrNull()
            if (error != null) {
                _estado.update { it.copy(cargando = false, errorCarga = error.tipo) }
                return@launch
            }
            val resumen = (conversaciones as Resultado.Exito).dato.firstOrNull { it.id == conversacionId }
            val trozo = (mensajes as Resultado.Exito).dato
            val otraId = resumen?.otraPersona?.id
            _estado.update {
                it.copy(
                    cargando = false,
                    otraPersona = resumen?.otraPersona,
                    solicitudTitulo = resumen?.solicitudTitulo,
                    mensajes = trozo.map { m -> m.aPantalla(otraId) },
                    hayMasAntiguos = trozo.size >= TAMANO_PAGINA_MENSAJES
                )
            }
            repositorioChat.marcarLeidos(conversacionId)
        }
    }

    private fun escucharMensajesNuevos() {
        viewModelScope.launch {
            repositorioChat.mensajesNuevos(conversacionId)
                .catch { /* sin tiempo real, lo nuevo aparece al volver a abrir el hilo */ }
                .collect { nuevo ->
                    val otraId = _estado.value.otraPersona?.id
                    _estado.update { actual ->
                        if (actual.mensajes.any { it.id == nuevo.id }) actual
                        else actual.copy(mensajes = listOf(nuevo.aPantalla(otraId)) + actual.mensajes)
                    }
                    // Un mensaje de la otra persona con el hilo abierto se lee en ese momento.
                    if (nuevo.emisorId == otraId) repositorioChat.marcarLeidos(conversacionId)
                }
        }
    }

    fun alReintentarCarga() {
        pagina = 1
        cargar()
    }

    fun alCambiarBorrador(valor: String) {
        _estado.update { it.copy(borrador = valor) }
    }

    /** Sube al principio del historial y pide la pagina anterior (5.2). */
    fun alPedirAnteriores() {
        val actual = _estado.value
        if (!actual.hayMasAntiguos || actual.cargandoAnteriores) return
        _estado.update { it.copy(cargandoAnteriores = true) }
        viewModelScope.launch {
            val resultado = repositorioChat.obtenerMensajes(conversacionId, pagina + 1)
            _estado.update {
                if (resultado is Resultado.Error) {
                    it.copy(cargandoAnteriores = false, aviso = obtenerMensajeErrorRes(resultado.tipo))
                } else {
                    pagina += 1
                    val trozo = (resultado as Resultado.Exito).dato
                    val otraId = it.otraPersona?.id
                    val conocidos = it.mensajes.map { m -> m.id }.toSet()
                    it.copy(
                        cargandoAnteriores = false,
                        mensajes = it.mensajes + trozo.filter { m -> m.id !in conocidos }.map { m -> m.aPantalla(otraId) },
                        hayMasAntiguos = trozo.size >= TAMANO_PAGINA_MENSAJES
                    )
                }
            }
        }
    }

    /** Envia lo escrito: el mensaje aparece al momento y el campo queda libre (5.2). */
    fun alEnviar() {
        val actual = _estado.value
        if (!actual.puedeEnviar) return
        val local = MensajeEnPantalla(
            id = "local-${siguienteLocal++}",
            contenido = actual.borrador.trim(),
            creadoEn = Instant.now(),
            esMio = true,
            situacion = SituacionMensaje.ENVIANDO
        )
        _estado.update { it.copy(borrador = "", mensajes = listOf(local) + it.mensajes) }
        enviarLocal(local)
    }

    /** Un mensaje que no se pudo enviar vuelve a intentarse con su mismo texto (1.4). */
    fun alReintentarEnvio(id: String) {
        val fallido = _estado.value.mensajes.firstOrNull {
            it.id == id && it.situacion == SituacionMensaje.FALLIDO
        } ?: return
        reemplazar(id) { it.copy(situacion = SituacionMensaje.ENVIANDO) }
        enviarLocal(fallido)
    }

    private fun enviarLocal(local: MensajeEnPantalla) {
        viewModelScope.launch {
            val resultado = repositorioChat.enviar(conversacionId, local.contenido)
            if (resultado is Resultado.Error) {
                reemplazar(local.id) { it.copy(situacion = SituacionMensaje.FALLIDO) }
                _estado.update { it.copy(aviso = obtenerMensajeErrorRes(resultado.tipo)) }
                return@launch
            }
            val real = (resultado as Resultado.Exito).dato
            _estado.update { actual ->
                // El flujo de mensajes nuevos puede haber entregado ya el mismo mensaje: no se duplica.
                val sinLocal = actual.mensajes.filter { it.id != local.id }
                if (sinLocal.any { it.id == real.id }) {
                    actual.copy(mensajes = sinLocal)
                } else {
                    actual.copy(
                        mensajes = actual.mensajes.map {
                            if (it.id == local.id) {
                                it.copy(id = real.id, creadoEn = real.creadoEn, situacion = SituacionMensaje.ENVIADO)
                            } else {
                                it
                            }
                        }
                    )
                }
            }
        }
    }

    private fun reemplazar(id: String, cambio: (MensajeEnPantalla) -> MensajeEnPantalla) {
        _estado.update { actual -> actual.copy(mensajes = actual.mensajes.map { if (it.id == id) cambio(it) else it }) }
    }

    fun alConsumirAviso() {
        _estado.update { it.copy(aviso = null) }
    }

    /**
     * Mio es todo lo que no manda la otra persona: en un hilo hay solo dos. Si todavia no se conoce
     * a la otra persona, nada se marca como propio.
     */
    private fun Mensaje.aPantalla(otraPersonaId: String?) = MensajeEnPantalla(
        id = id,
        contenido = contenido,
        creadoEn = creadoEn,
        esMio = otraPersonaId != null && emisorId != otraPersonaId,
        leidoPorOtro = leidoEn != null
    )
}
