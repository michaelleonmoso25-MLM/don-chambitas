package mx.donchambitas.app.ui.pantallas

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth
import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.util.DatosPrueba
import mx.donchambitas.app.util.ReglaCorrutinas
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Pruebas unitarias para SplashViewModel y EstadoSplash.
 * Sirve como plantilla oficial para pruebas unitarias de ViewModels con corrutinas.
 *
 * Utiliza [ReglaCorrutinas] para fijar el despachador de pruebas sin requerir el Looper de Android.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val repositorio = mockk<RepositorioAuth>()

    private val sesionCliente = DatosPrueba.crearSesion()
    private val sesionTrabajador = DatosPrueba.crearSesion(
        usuario = DatosPrueba.crearUsuario(rol = RolUsuario.TRABAJADOR)
    )

    private fun responderCon(sesiones: Flow<Sesion?>) {
        every { repositorio.sesionActual() } returns sesiones
    }

    private fun sesionQueTarda(milisegundos: Long, sesion: Sesion?): Flow<Sesion?> = flow {
        delay(milisegundos)
        emit(sesion)
    }

    @Test
    fun debeTenerCargandoVerdaderoYSinDestino_cuandoIniciaEstado() {
        responderCon(flowOf(null))

        val estado = SplashViewModel(repositorio).estado.value

        assertTrue("El estado inicial debe tener cargando en true", estado.cargando)
        assertNull("El estado inicial no debe tener destino", estado.destino)
        assertNull(estado.error)
    }

    @Test
    fun debeIrAIniciarSesion_cuandoNoHaySesion() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(flowOf(null))
        val viewModel = SplashViewModel(repositorio)

        advanceTimeBy(SplashViewModel.TIEMPO_MINIMO_SPLASH_MS)
        runCurrent()

        assertFalse(viewModel.estado.value.cargando)
        assertEquals(Ruta.IniciarSesion, viewModel.estado.value.destino)
    }

    @Test
    fun debeIrAInicioCliente_cuandoLaSesionEsDeCliente() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(flowOf(sesionCliente))
        val viewModel = SplashViewModel(repositorio)

        advanceTimeBy(SplashViewModel.TIEMPO_MINIMO_SPLASH_MS)
        runCurrent()

        assertEquals(Ruta.InicioCliente, viewModel.estado.value.destino)
    }

    @Test
    fun debeIrAInicioTrabajador_cuandoLaSesionEsDeTrabajador() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(flowOf(sesionTrabajador))
        val viewModel = SplashViewModel(repositorio)

        advanceTimeBy(SplashViewModel.TIEMPO_MINIMO_SPLASH_MS)
        runCurrent()

        assertEquals(Ruta.InicioTrabajador, viewModel.estado.value.destino)
    }

    @Test
    fun debeSeguirCargando_cuandoLaSesionLlegaAntesDelTiempoMinimo() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(flowOf(sesionCliente))
        val viewModel = SplashViewModel(repositorio)

        advanceTimeBy(SplashViewModel.TIEMPO_MINIMO_SPLASH_MS - 1)
        runCurrent()

        assertTrue(viewModel.estado.value.cargando)
        assertNull(viewModel.estado.value.destino)
    }

    @Test
    fun debeDurarSoloElTiempoMinimo_cuandoLaSesionTardaMenos() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(sesionQueTarda(300, sesionCliente))
        val viewModel = SplashViewModel(repositorio)

        advanceTimeBy(SplashViewModel.TIEMPO_MINIMO_SPLASH_MS)
        runCurrent()

        assertEquals(Ruta.InicioCliente, viewModel.estado.value.destino)
    }

    @Test
    fun debeEsperarALaSesion_cuandoTardaMasQueElTiempoMinimo() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(sesionQueTarda(2_000, sesionTrabajador))
        val viewModel = SplashViewModel(repositorio)

        advanceTimeBy(1_999)
        runCurrent()
        assertTrue(viewModel.estado.value.cargando)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(Ruta.InicioTrabajador, viewModel.estado.value.destino)
    }

    @Test
    fun debeMostrarErrorDeRed_cuandoLaSesionNoLlegaEnElTope() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(MutableSharedFlow())
        val viewModel = SplashViewModel(repositorio)

        advanceTimeBy(SplashViewModel.TIEMPO_MAXIMO_ESPERA_MS - 1)
        runCurrent()
        assertTrue(viewModel.estado.value.cargando)
        assertNull(viewModel.estado.value.error)

        advanceTimeBy(1)
        runCurrent()
        val estado = viewModel.estado.value
        assertFalse(estado.cargando)
        assertEquals(TipoError.RED, estado.error)
        assertNull(estado.destino)
    }

    @Test
    fun debeVolverACargarYEntrar_cuandoSeReintentaConLaSesionDisponible() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(MutableSharedFlow())
        val viewModel = SplashViewModel(repositorio)
        advanceTimeBy(SplashViewModel.TIEMPO_MAXIMO_ESPERA_MS)
        runCurrent()
        responderCon(flowOf(sesionCliente))

        viewModel.alReintentar()
        assertTrue(viewModel.estado.value.cargando)
        assertNull(viewModel.estado.value.error)
        advanceTimeBy(SplashViewModel.TIEMPO_MINIMO_SPLASH_MS)
        runCurrent()

        assertEquals(Ruta.InicioCliente, viewModel.estado.value.destino)
        assertNull(viewModel.estado.value.error)
    }

    @Test
    fun noDebeLanzarOtraVerificacion_cuandoSeReintentaConUnaEnCurso() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(MutableSharedFlow())
        val viewModel = SplashViewModel(repositorio)
        runCurrent()

        viewModel.alReintentar()
        runCurrent()

        verify(exactly = 1) { repositorio.sesionActual() }
    }

    @Test
    fun debeIrAIniciarSesion_cuandoElFlujoDeSesionLanza() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(flow { throw IllegalStateException("faltan las llaves de Supabase") })
        val viewModel = SplashViewModel(repositorio)

        advanceTimeBy(SplashViewModel.TIEMPO_MINIMO_SPLASH_MS)
        runCurrent()

        assertEquals(Ruta.IniciarSesion, viewModel.estado.value.destino)
        assertNull(viewModel.estado.value.error)
    }
}
