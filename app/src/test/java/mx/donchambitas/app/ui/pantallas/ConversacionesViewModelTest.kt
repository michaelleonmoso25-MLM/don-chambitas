package mx.donchambitas.app.ui.pantallas

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioChatFalso
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.util.ReglaCorrutinas
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** P-15 contra el falso, sin retraso (S5-T05, DISENO-POSTULACION-CONTACTO-RESENAS.md 4). */
@OptIn(ExperimentalCoroutinesApi::class)
class ConversacionesViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val fuente = FuenteDatosFalsa()
    private val repositorioChat = RepositorioChatFalso(fuente).apply { retrasoMs = 0 }

    private fun entrarComo(id: String) =
        fuente.fijarSesionActiva(Sesion(fuente.usuarios.first { it.id == id }))

    @Test
    fun debeArrancarCargando_yListarLasConversacionesDelUsuario() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = ConversacionesViewModel(repositorioChat)
        assertTrue(viewModel.estado.value.cargando)

        viewModel.alCargar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.cargando)
        // El cliente semilla tiene dos hilos; el mas reciente va primero (HU-25).
        assertEquals(listOf("conv-1", "conv-2"), estado.conversaciones.map { it.id })
    }

    @Test
    fun debeTraerLaOtraPersonaElUltimoMensajeYLosSinLeer() = runTest(reglaCorrutinas.testDispatcher) {
        entrarComo("usr-trab-1")
        val viewModel = ConversacionesViewModel(repositorioChat)
        viewModel.alCargar()
        advanceUntilIdle()

        val hilo = viewModel.estado.value.conversaciones.single { it.id == "conv-1" }
        assertEquals("Juan", hilo.otraPersona.nombre)
        assertEquals("Perfecto, aqui lo espero. Muchas gracias.", hilo.ultimoMensaje)
        // El trabajador tiene un mensaje del cliente sin leer (HU-25).
        assertEquals(1, hilo.sinLeer)
        assertNotNull(hilo.solicitudTitulo)
    }

    @Test
    fun debeMostrarElVacio_cuandoNoHayConversaciones() = runTest(reglaCorrutinas.testDispatcher) {
        fuente.conversaciones.clear()
        val viewModel = ConversacionesViewModel(repositorioChat)
        viewModel.alCargar()
        advanceUntilIdle()

        assertTrue(viewModel.estado.value.vacio)
    }

    @Test
    fun debeMostrarElError_cuandoFallaLaPrimeraCarga() = runTest(reglaCorrutinas.testDispatcher) {
        repositorioChat.errorForzado = TipoError.RED
        val viewModel = ConversacionesViewModel(repositorioChat)
        viewModel.alCargar()
        advanceUntilIdle()

        assertEquals(TipoError.RED, viewModel.estado.value.errorCarga)
        assertFalse(viewModel.estado.value.cargando)
    }

    @Test
    fun noDebeQuitarLaLista_cuandoFallaUnaRecarga() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = ConversacionesViewModel(repositorioChat)
        viewModel.alCargar()
        advanceUntilIdle()

        repositorioChat.errorForzado = TipoError.RED
        viewModel.alCargar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(2, estado.conversaciones.size)
        assertEquals(null, estado.errorCarga)
        assertNotNull(estado.aviso)
    }

    @Test
    fun debeSubirElHilo_cuandoLlegaUnMensaje() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = ConversacionesViewModel(repositorioChat)
        viewModel.alCargar()
        advanceUntilIdle()
        assertEquals("conv-1", viewModel.estado.value.conversaciones.first().id)

        // Un mensaje nuevo en el hilo mas viejo lo sube al principio sin que nadie recargue (HU-26).
        repositorioChat.enviar("conv-2", "Hola de nuevo")
        advanceUntilIdle()

        assertEquals("conv-2", viewModel.estado.value.conversaciones.first().id)
    }
}
