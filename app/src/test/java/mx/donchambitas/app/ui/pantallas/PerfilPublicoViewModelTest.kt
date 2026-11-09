package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioTrabajadorFalso
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

/** P-07 como vista previa del trabajador (S3-T14, DISENO-PERFIL-SERVICIOS.md 11). */
@OptIn(ExperimentalCoroutinesApi::class)
class PerfilPublicoViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val repositorioTrabajador = RepositorioTrabajadorFalso(FuenteDatosFalsa()).apply { retrasoMs = 0 }

    private fun crearViewModel(trabajadorId: String) = PerfilPublicoViewModel(
        repositorioTrabajador,
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
        val fuente = FuenteDatosFalsa()
        fuente.servicios.removeAll { it.perfilId == "usr-trab-1" }
        fuente.resenas.removeAll { it.trabajadorId == "usr-trab-1" }
        val viewModel = PerfilPublicoViewModel(
            RepositorioTrabajadorFalso(fuente).apply { retrasoMs = 0 },
            SavedStateHandle(mapOf(Ruta.PerfilTrabajador.ARG_TRABAJADOR_ID to "usr-trab-1"))
        )

        advanceUntilIdle()

        val perfil = viewModel.estado.value.perfil
        assertNotNull(perfil)
        assertTrue(perfil!!.servicios.isEmpty())
        assertTrue(perfil.resenas.isEmpty())
        assertEquals(0, perfil.calificacion.totalResenas)
    }
}
