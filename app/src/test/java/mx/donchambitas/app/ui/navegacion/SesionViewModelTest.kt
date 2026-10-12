package mx.donchambitas.app.ui.navegacion

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
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth
import mx.donchambitas.app.util.DatosPrueba
import mx.donchambitas.app.util.ReglaCorrutinas
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SesionViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val repositorio = mockk<RepositorioAuth>()
    private val avisosEnlace = AvisosEnlace()

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
        val avisos = avisosDe(SesionViewModel(repositorio, avisosEnlace))
        assertEquals(0, avisos.size)

        sesiones.value = null
        runCurrent()

        assertEquals(1, avisos.size)
    }

    @Test
    fun debeAvisar_cuandoNoHaySesionAlArrancar() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(MutableStateFlow(null))

        val avisos = avisosDe(SesionViewModel(repositorio, avisosEnlace))

        assertEquals(1, avisos.size)
    }

    @Test
    fun noDebeAvisar_cuandoSeAbreUnaSesion() = runTest(reglaCorrutinas.testDispatcher) {
        val sesiones = MutableStateFlow<Sesion?>(null)
        responderCon(sesiones)
        val avisos = avisosDe(SesionViewModel(repositorio, avisosEnlace))

        sesiones.value = DatosPrueba.crearSesion()
        runCurrent()

        assertEquals(1, avisos.size)
    }

    @Test
    fun noDebeAvisarNiLanzar_cuandoElFlujoDeSesionLanza() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(flow { throw IllegalStateException("faltan las llaves de Supabase") })

        val avisos = avisosDe(SesionViewModel(repositorio, avisosEnlace))

        assertEquals(0, avisos.size)
    }

    @Test
    fun debeEntregarElRol_cuandoLlegaElEnlaceDeRecuperacion() = runTest(reglaCorrutinas.testDispatcher) {
        val sesiones = MutableStateFlow<Sesion?>(null)
        responderCon(sesiones)
        val viewModel = SesionViewModel(repositorio, avisosEnlace)
        val roles = mutableListOf<RolUsuario>()
        backgroundScope.launch { viewModel.entradaPorRecuperacion.toList(roles) }

        avisosEnlace.avisarRecuperacion()
        runCurrent()
        assertEquals(emptyList<RolUsuario>(), roles)

        sesiones.value = Sesion(DatosPrueba.crearUsuario(rol = RolUsuario.TRABAJADOR))
        runCurrent()

        assertEquals(listOf(RolUsuario.TRABAJADOR), roles)
    }

    @Test
    fun noDebeEntregarRol_sinEnlaceDeRecuperacion() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(MutableStateFlow(DatosPrueba.crearSesion()))
        val viewModel = SesionViewModel(repositorio, avisosEnlace)
        val roles = mutableListOf<RolUsuario>()
        backgroundScope.launch { viewModel.entradaPorRecuperacion.toList(roles) }

        runCurrent()

        assertEquals(emptyList<RolUsuario>(), roles)
    }
}
