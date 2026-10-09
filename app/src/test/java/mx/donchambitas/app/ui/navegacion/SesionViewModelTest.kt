package mx.donchambitas.app.ui.navegacion

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth
import mx.donchambitas.app.util.DatosPrueba
import mx.donchambitas.app.util.ReglaCorrutinas
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SesionViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val repositorio = mockk<RepositorioAuth>()

    private fun responderCon(sesiones: Flow<Sesion?>) {
        every { repositorio.sesionActual() } returns sesiones
    }

    private fun TestScope.avisosDe(viewModel: SesionViewModel): List<Unit> {
        val avisos = mutableListOf<Unit>()
        backgroundScope.launch { viewModel.sinSesion.toList(avisos) }
        runCurrent()
        return avisos
    }

    @Test
    fun debeAvisar_cuandoLaSesionAbiertaTermina() = runTest(reglaCorrutinas.testDispatcher) {
        val sesiones = MutableStateFlow<Sesion?>(DatosPrueba.crearSesion())
        responderCon(sesiones)
        val avisos = avisosDe(SesionViewModel(repositorio))
        assertEquals(0, avisos.size)

        sesiones.value = null
        runCurrent()

        assertEquals(1, avisos.size)
    }

    @Test
    fun debeAvisar_cuandoNoHaySesionAlArrancar() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(MutableStateFlow(null))

        val avisos = avisosDe(SesionViewModel(repositorio))

        assertEquals(1, avisos.size)
    }

    @Test
    fun noDebeAvisar_cuandoSeAbreUnaSesion() = runTest(reglaCorrutinas.testDispatcher) {
        val sesiones = MutableStateFlow<Sesion?>(null)
        responderCon(sesiones)
        val avisos = avisosDe(SesionViewModel(repositorio))

        sesiones.value = DatosPrueba.crearSesion()
        runCurrent()

        assertEquals(1, avisos.size)
    }

    @Test
    fun debeLlamarAlRepositorio_cuandoSeCierraSesion() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(MutableStateFlow(DatosPrueba.crearSesion()))
        coEvery { repositorio.cerrarSesion() } returns Resultado.Exito(Unit)
        val viewModel = SesionViewModel(repositorio)

        viewModel.cerrarSesion()
        runCurrent()

        coVerify(exactly = 1) { repositorio.cerrarSesion() }
    }

    @Test
    fun noDebeAvisar_cuandoElCierreFallaYLaSesionSigueAbierta() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(MutableStateFlow(DatosPrueba.crearSesion()))
        coEvery { repositorio.cerrarSesion() } returns Resultado.Error(TipoError.RED, "sin red")
        val viewModel = SesionViewModel(repositorio)
        val avisos = avisosDe(viewModel)

        viewModel.cerrarSesion()
        runCurrent()

        assertEquals(0, avisos.size)
    }

    @Test
    fun noDebeAvisarNiLanzar_cuandoElFlujoDeSesionLanza() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(flow { throw IllegalStateException("faltan las llaves de Supabase") })

        val avisos = avisosDe(SesionViewModel(repositorio))

        assertEquals(0, avisos.size)
    }
}
