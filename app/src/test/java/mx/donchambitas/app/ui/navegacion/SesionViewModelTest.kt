package mx.donchambitas.app.ui.navegacion

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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
    fun noDebeAvisar_cuandoNoHaySesionAlArrancar() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(MutableStateFlow(null))

        val viewModel = SesionViewModel(repositorio, avisosEnlace)
        val avisos = avisosDe(viewModel)

        assertEquals(0, avisos.size)
        assertEquals(EstadoSesion.SIN_SESION, viewModel.estado.value)
    }

    @Test
    fun noDebeAvisar_cuandoSeAbreUnaSesion() = runTest(reglaCorrutinas.testDispatcher) {
        val sesiones = MutableStateFlow<Sesion?>(null)
        responderCon(sesiones)
        val avisos = avisosDe(SesionViewModel(repositorio, avisosEnlace))

        sesiones.value = DatosPrueba.crearSesion()
        runCurrent()

        assertEquals(0, avisos.size)
    }

    @Test
    fun noDebeAvisarNiLanzar_cuandoElFlujoDeSesionLanza() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(flow { throw IllegalStateException("faltan las llaves de Supabase") })

        val viewModel = SesionViewModel(repositorio, avisosEnlace)
        val avisos = avisosDe(viewModel)

        assertEquals(0, avisos.size)
        assertEquals(EstadoSesion.SIN_SESION, viewModel.estado.value)
    }

    @Test
    fun debeArrancarEnVerificando_mientrasAuthNoResponde() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(MutableSharedFlow())

        val viewModel = SesionViewModel(repositorio, avisosEnlace)
        runCurrent()

        assertEquals(EstadoSesion.VERIFICANDO, viewModel.estado.value)
    }

    @Test
    fun debeLlevarElRolDeLaSesion() = runTest(reglaCorrutinas.testDispatcher) {
        val sesiones = MutableStateFlow<Sesion?>(
            Sesion(DatosPrueba.crearUsuario(rol = RolUsuario.CLIENTE))
        )
        responderCon(sesiones)
        val viewModel = SesionViewModel(repositorio, avisosEnlace)
        runCurrent()
        assertEquals(EstadoSesion.CLIENTE, viewModel.estado.value)

        sesiones.value = Sesion(DatosPrueba.crearUsuario(rol = RolUsuario.TRABAJADOR))
        runCurrent()
        assertEquals(EstadoSesion.TRABAJADOR, viewModel.estado.value)

        sesiones.value = null
        runCurrent()
        assertEquals(EstadoSesion.SIN_SESION, viewModel.estado.value)
    }

    @Test
    fun debeAvisar_cuandoQuienEntroConReintentarCierraSesion() = runTest(reglaCorrutinas.testDispatcher) {
        // Sin red al abrir, Auth no emitió la sesión; el usuario entró desde P-01
        val sesiones = MutableSharedFlow<Sesion?>()
        responderCon(sesiones)
        val viewModel = SesionViewModel(repositorio, avisosEnlace)
        val avisos = avisosDe(viewModel)

        viewModel.registrarEntrada(RolUsuario.TRABAJADOR)
        assertEquals(EstadoSesion.TRABAJADOR, viewModel.estado.value)

        sesiones.emit(null)
        runCurrent()

        assertEquals(1, avisos.size)
        assertEquals(EstadoSesion.SIN_SESION, viewModel.estado.value)
    }

    @Test
    fun debeEntregarElRol_cuandoLlegaElEnlaceDeRecuperacion() = runTest(reglaCorrutinas.testDispatcher) {
        val sesiones = MutableStateFlow<Sesion?>(null)
        responderCon(sesiones)
        val viewModel = SesionViewModel(repositorio, avisosEnlace)
        val roles = mutableListOf<RolUsuario>()
        val estadoAlLlegar = mutableListOf<EstadoSesion>()
        backgroundScope.launch {
            viewModel.entradaPorRecuperacion.collect { rol ->
                roles += rol
                estadoAlLlegar += viewModel.estado.value
            }
        }

        avisosEnlace.avisarRecuperacion()
        runCurrent()
        assertEquals(emptyList<RolUsuario>(), roles)

        sesiones.value = Sesion(DatosPrueba.crearUsuario(rol = RolUsuario.TRABAJADOR))
        runCurrent()

        assertEquals(listOf(RolUsuario.TRABAJADOR), roles)
        assertEquals(listOf(EstadoSesion.TRABAJADOR), estadoAlLlegar)
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
