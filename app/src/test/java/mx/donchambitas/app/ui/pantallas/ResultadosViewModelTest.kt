package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioCatalogosFalso
import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.util.ReglaCorrutinas
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** P-06 contra el buscador falso, sin retraso (S4-T03, DISENO-SOLICITUDES-BUSQUEDA.md 3). */
@OptIn(ExperimentalCoroutinesApi::class)
class ResultadosViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val fuente = FuenteDatosFalsa()
    private val repositorioCatalogos = RepositorioCatalogosFalso(fuente).apply { retrasoMs = 0 }

    private fun crearViewModel(categoriaId: Int? = null) = ResultadosViewModel(
        repositorioCatalogos,
        SavedStateHandle(
            if (categoriaId == null) emptyMap() else mapOf(Ruta.Resultados.ARG_CATEGORIA_ID to categoriaId)
        )
    )

    @Test
    fun debeArrancarCargando_yListarTodosLosTrabajadores_sinCategoria() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        assertTrue(viewModel.estado.value.cargando)

        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.cargando)
        assertNull(estado.errorCarga)
        assertFalse(estado.vacio)
        assertEquals(fuente.perfilesTrabajador.size, estado.trabajadores.size)
    }

    @Test
    fun debeTomarLaRutaSinCategoriaComoTodos_cuandoLlegaElValorPorDefecto() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel(Ruta.Resultados.SIN_CATEGORIA)

        advanceUntilIdle()

        assertEquals(fuente.perfilesTrabajador.size, viewModel.estado.value.trabajadores.size)
    }

    @Test
    fun debeListarSoloLosDeLaCategoria_cuandoLaRutaLaTrae() = runTest(reglaCorrutinas.testDispatcher) {
        val categoria = fuente.servicios.first { it.activo }.categoriaId
        val viewModel = crearViewModel(categoria)

        advanceUntilIdle()

        val trabajadores = viewModel.estado.value.trabajadores
        assertTrue(trabajadores.isNotEmpty())
        assertTrue(trabajadores.all { categoria in it.categorias })
    }

    @Test
    fun debeOrdenarPorCalificacion_delMejorAlPeor() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()

        advanceUntilIdle()

        val promedios = viewModel.estado.value.trabajadores.map { it.promedio }
        assertEquals(promedios.sortedDescending(), promedios)
    }

    @Test
    fun debeMostrarElVacio_cuandoNadieEsDeLaCategoria() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel(categoriaId = 9999)

        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertTrue(estado.trabajadores.isEmpty())
        assertNull(estado.errorCarga)
        assertTrue(estado.vacio)
    }

    @Test
    fun noDebeMostrarElVacio_mientrasCarga() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel(categoriaId = 9999)

        assertFalse(viewModel.estado.value.vacio)
    }

    @Test
    fun debeMostrarElError_yReintentar() = runTest(reglaCorrutinas.testDispatcher) {
        repositorioCatalogos.errorForzado = TipoError.RED
        val viewModel = crearViewModel()
        advanceUntilIdle()

        val conError = viewModel.estado.value
        assertEquals(TipoError.RED, conError.errorCarga)
        assertFalse(conError.vacio)

        repositorioCatalogos.errorForzado = null
        viewModel.alReintentarCarga()
        assertTrue(viewModel.estado.value.cargando)
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertNull(estado.errorCarga)
        assertTrue(estado.trabajadores.isNotEmpty())
    }
}
