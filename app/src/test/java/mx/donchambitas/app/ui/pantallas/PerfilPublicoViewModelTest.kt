package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioAuthFalso
import mx.donchambitas.app.datos.falso.RepositorioTrabajadorFalso
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.util.ReglaCorrutinas
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** P-07: vista previa del trabajador (S3-T14) y perfil del cliente (S4-T03, H-39). */
@OptIn(ExperimentalCoroutinesApi::class)
class PerfilPublicoViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val fuente = FuenteDatosFalsa()
    private val repositorioTrabajador = RepositorioTrabajadorFalso(fuente).apply { retrasoMs = 0 }

    private fun iniciarSesionComo(usuarioId: String) {
        fuente.fijarSesionActiva(Sesion(fuente.usuarios.first { it.id == usuarioId }))
    }

    private fun crearViewModel(trabajadorId: String) = PerfilPublicoViewModel(
        repositorioTrabajador,
        RepositorioAuthFalso(fuente),
        SavedStateHandle(mapOf(Ruta.PerfilTrabajador.ARG_TRABAJADOR_ID to trabajadorId))
    )

    @Test
    fun debeCargarElPerfilDelTrabajadorDeLaRuta() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel("usr-trab-1")
        assertTrue(viewModel.estado.value.cargando)

        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.cargando)
        assertNull(estado.errorCarga)
        assertEquals("Pedro", estado.perfil?.nombre)
    }

    @Test
    fun debeMostrarElError_cuandoElTrabajadorNoExiste() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel("no-existe")

        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.cargando)
        assertNull(estado.perfil)
        assertEquals(TipoError.VALIDACION, estado.errorCarga)
    }

    @Test
    fun debeReintentarLaCarga_cuandoAntesFallo() = runTest(reglaCorrutinas.testDispatcher) {
        repositorioTrabajador.errorForzado = TipoError.RED
        val viewModel = crearViewModel("usr-trab-1")
        advanceUntilIdle()
        assertEquals(TipoError.RED, viewModel.estado.value.errorCarga)

        repositorioTrabajador.errorForzado = null
        viewModel.alReintentarCarga()
        assertTrue(viewModel.estado.value.cargando)
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertNull(estado.errorCarga)
        assertNotNull(estado.perfil)
    }

    @Test
    fun debeTraerLasListasVacias_cuandoNoHayServiciosNiResenas() = runTest(reglaCorrutinas.testDispatcher) {
        fuente.servicios.removeAll { it.perfilId == "usr-trab-1" }
        fuente.resenas.removeAll { it.trabajadorId == "usr-trab-1" }
        val viewModel = crearViewModel("usr-trab-1")

        advanceUntilIdle()

        val perfil = viewModel.estado.value.perfil
        assertNotNull(perfil)
        assertTrue(perfil!!.servicios.isEmpty())
        assertTrue(perfil.resenas.isEmpty())
        assertEquals(0, perfil.calificacion.totalResenas)
    }

    @Test
    fun debeSerVistaPrevia_cuandoElPerfilEsElDeLaSesion() = runTest(reglaCorrutinas.testDispatcher) {
        iniciarSesionComo("usr-trab-1")
        val viewModel = crearViewModel("usr-trab-1")

        advanceUntilIdle()

        assertTrue(viewModel.estado.value.esVistaPrevia)
    }

    @Test
    fun noDebeSerVistaPrevia_cuandoLoAbreUnCliente() = runTest(reglaCorrutinas.testDispatcher) {
        iniciarSesionComo("usr-cli-1")
        val viewModel = crearViewModel("usr-trab-1")

        advanceUntilIdle()

        assertFalse(viewModel.estado.value.esVistaPrevia)
        assertNotNull(viewModel.estado.value.perfil)
    }

    @Test
    fun noDebeSerVistaPrevia_cuandoElTrabajadorAbreElPerfilDeOtro() = runTest(reglaCorrutinas.testDispatcher) {
        iniciarSesionComo("usr-trab-1")
        val viewModel = crearViewModel("usr-trab-2")

        advanceUntilIdle()

        assertFalse(viewModel.estado.value.esVistaPrevia)
    }
}
