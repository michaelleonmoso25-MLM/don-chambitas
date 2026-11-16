package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.SavedStateHandle
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioChatFalso
import mx.donchambitas.app.dominio.modelo.Mensaje
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.ui.navegacion.ARG_CONVERSACION_ID
import mx.donchambitas.app.util.ReglaCorrutinas
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** P-16 contra el falso, sin retraso (S5-T05, DISENO-POSTULACION-CONTACTO-RESENAS.md 5). */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val fuente = FuenteDatosFalsa()
    private val repositorioChat = RepositorioChatFalso(fuente).apply { retrasoMs = 0 }

    private fun crearViewModel(conversacionId: String = "conv-1") =
        ChatViewModel(repositorioChat, SavedStateHandle(mapOf(ARG_CONVERSACION_ID to conversacionId)))

    @Test
    fun debeCargarElHistorial_conLaOtraPersona_yMarcarLosPropios() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.cargando)
        assertEquals("Pedro", estado.otraPersona?.nombre)
        // Del mas reciente al mas antiguo; el cliente escribio el 1 y el 3, el trabajador el 2.
        assertEquals(listOf("msg-1-3", "msg-1-2", "msg-1-1"), estado.mensajes.map { it.id })
        assertEquals(listOf(true, false, true), estado.mensajes.map { it.esMio })
        assertFalse(estado.hayMasAntiguos)
    }

    @Test
    fun debeMarcarComoLeidosLosRecibidos_alAbrir() = runTest(reglaCorrutinas.testDispatcher) {
        fuente.fijarSesionActiva(Sesion(fuente.usuarios.first { it.id == "usr-trab-1" }))
        crearViewModel()
        advanceUntilIdle()

        // El mensaje del cliente que estaba sin leer queda leido (HU-26).
        assertNotNull(fuente.mensajes.first { it.id == "msg-1-3" }.leidoEn)
    }

    @Test
    fun debeMostrarElVacio_enUnHiloSinMensajes() = runTest(reglaCorrutinas.testDispatcher) {
        fuente.mensajes.removeAll { it.conversacionId == "conv-1" }
        val viewModel = crearViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.estado.value.sinMensajes)
    }

    @Test
    fun debeMostrarElError_cuandoFallaLaCarga() = runTest(reglaCorrutinas.testDispatcher) {
        repositorioChat.errorForzado = TipoError.RED
        val viewModel = crearViewModel()
        advanceUntilIdle()

        assertEquals(TipoError.RED, viewModel.estado.value.errorCarga)
    }

    @Test
    fun noDebePoderEnviar_conElTextoEnBlancoOPasadoDelTope() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.alCambiarBorrador("   ")
        assertFalse(viewModel.estado.value.puedeEnviar)
        viewModel.alCambiarBorrador("a".repeat(MAXIMO_CARACTERES_MENSAJE + 1))
        assertFalse(viewModel.estado.value.puedeEnviar)
        viewModel.alCambiarBorrador("Hola")
        assertTrue(viewModel.estado.value.puedeEnviar)
    }

    @Test
    fun debeEnviar_yDejarElMensajeEnviadoSinDuplicarlo() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.alCambiarBorrador("  Nos vemos a las 10  ")
        viewModel.alEnviar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals("", estado.borrador)
        val propios = estado.mensajes.filter { it.contenido == "Nos vemos a las 10" }
        assertEquals(1, propios.size)
        assertEquals(SituacionMensaje.ENVIADO, propios.single().situacion)
        assertTrue(propios.single().esMio)
        assertTrue(fuente.mensajes.any { it.contenido == "Nos vemos a las 10" })
    }

    @Test
    fun debeConservarElTexto_yPermitirReintentar_cuandoFallaElEnvio() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        repositorioChat.errorForzado = TipoError.RED
        viewModel.alCambiarBorrador("Mensaje importante")
        viewModel.alEnviar()
        advanceUntilIdle()

        val fallido = viewModel.estado.value.mensajes.first()
        assertEquals(SituacionMensaje.FALLIDO, fallido.situacion)
        assertEquals("Mensaje importante", fallido.contenido)
        assertNotNull(viewModel.estado.value.aviso)

        repositorioChat.errorForzado = null
        viewModel.alReintentarEnvio(fallido.id)
        advanceUntilIdle()

        val reintentado = viewModel.estado.value.mensajes.filter { it.contenido == "Mensaje importante" }
        assertEquals(1, reintentado.size)
        assertEquals(SituacionMensaje.ENVIADO, reintentado.single().situacion)
    }

    @Test
    fun debeMostrarSinRecargar_unMensajeDeLaOtraPersona() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        // Escribe el trabajador, con la pantalla del cliente abierta (HU-26).
        fuente.fijarSesionActiva(Sesion(fuente.usuarios.first { it.id == "usr-trab-1" }))
        repositorioChat.enviar("conv-1", "Voy en camino")
        advanceUntilIdle()

        val nuevo = viewModel.estado.value.mensajes.first()
        assertEquals("Voy en camino", nuevo.contenido)
        assertFalse(nuevo.esMio)
    }

    @Test
    fun debePedirLaPaginaAnterior_cuandoHayMasDeTreinta() = runTest(reglaCorrutinas.testDispatcher) {
        val base = Instant.parse("2026-10-01T00:00:00Z")
        repeat(35) { i ->
            fuente.mensajes.add(
                Mensaje("extra-$i", "conv-2", "usr-trab-3", "Mensaje $i", base.plusSeconds(i.toLong()), base.plusSeconds(i.toLong()))
            )
        }
        val viewModel = crearViewModel("conv-2")
        advanceUntilIdle()
        assertEquals(30, viewModel.estado.value.mensajes.size)
        assertTrue(viewModel.estado.value.hayMasAntiguos)

        viewModel.alPedirAnteriores()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        // 35 nuevos mas los 2 de la semilla, sin repetidos.
        assertEquals(37, estado.mensajes.size)
        assertEquals(37, estado.mensajes.map { it.id }.toSet().size)
        assertFalse(estado.hayMasAntiguos)
        assertNull(estado.aviso)
    }
}
