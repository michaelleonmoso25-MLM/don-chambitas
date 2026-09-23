package mx.donchambitas.app.ui.pantallas

import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.R
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioAuthFalso
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.util.ReglaCorrutinas
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class IniciarSesionViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private lateinit var repositorio: RepositorioAuthFalso
    private lateinit var viewModel: IniciarSesionViewModel

    @Before
    fun preparar() {
        repositorio = RepositorioAuthFalso(FuenteDatosFalsa()).apply { retrasoMs = 0L }
        viewModel = IniciarSesionViewModel(repositorio)
    }

    private fun escribir(correo: String, contrasena: String) {
        viewModel.alCambiarCorreo(correo)
        viewModel.alCambiarContrasena(contrasena)
    }

    @Test
    fun debeIrAInicioCliente_cuandoLaCuentaEsDeCliente() = runTest(reglaCorrutinas.testDispatcher) {
        escribir("juan.perez@ejemplo.com", "cualquiera")

        viewModel.alIniciarSesion()
        advanceUntilIdle()

        assertEquals(Ruta.InicioCliente, viewModel.estado.value.destino)
        assertFalse(viewModel.estado.value.cargando)
    }

    @Test
    fun debeIrAInicioTrabajador_cuandoLaCuentaEsDeTrabajador() = runTest(reglaCorrutinas.testDispatcher) {
        val trabajador = FuenteDatosFalsa().usuarios.first { it.rol == RolUsuario.TRABAJADOR }
        escribir(trabajador.correo, "cualquiera")

        viewModel.alIniciarSesion()
        advanceUntilIdle()

        assertEquals(Ruta.InicioTrabajador, viewModel.estado.value.destino)
    }

    @Test
    fun debeMarcarAutenticacionYConservarLoEscrito_cuandoLaCuentaNoExiste() =
        runTest(reglaCorrutinas.testDispatcher) {
            escribir("noexiste@ejemplo.com", "secreta")

            viewModel.alIniciarSesion()
            advanceUntilIdle()

            val estado = viewModel.estado.value
            assertEquals(TipoError.AUTENTICACION, estado.errorPantalla)
            assertNull(estado.destino)
            assertEquals("noexiste@ejemplo.com", estado.correo)
            assertEquals("secreta", estado.contrasena)
        }

    @Test
    fun debeMarcarRed_cuandoNoHayConexion() = runTest(reglaCorrutinas.testDispatcher) {
        repositorio.errorForzado = TipoError.RED
        escribir("juan.perez@ejemplo.com", "secreta")

        viewModel.alIniciarSesion()
        advanceUntilIdle()

        assertEquals(TipoError.RED, viewModel.estado.value.errorPantalla)
        assertFalse(viewModel.estado.value.cargando)
    }

    @Test
    fun debePintarDesconocido_cuandoLlegaLimiteIa() = runTest(reglaCorrutinas.testDispatcher) {
        repositorio.errorForzado = TipoError.LIMITE_IA
        escribir("juan.perez@ejemplo.com", "secreta")

        viewModel.alIniciarSesion()
        advanceUntilIdle()

        assertEquals(TipoError.DESCONOCIDO, viewModel.estado.value.errorPantalla)
    }

    @Test
    fun debeEntrar_cuandoSeReintentaYaConConexion() = runTest(reglaCorrutinas.testDispatcher) {
        repositorio.errorForzado = TipoError.RED
        escribir("juan.perez@ejemplo.com", "secreta")
        viewModel.alIniciarSesion()
        advanceUntilIdle()

        repositorio.errorForzado = null
        viewModel.alReintentar()
        advanceUntilIdle()

        assertNull(viewModel.estado.value.errorPantalla)
        assertEquals(Ruta.InicioCliente, viewModel.estado.value.destino)
    }

    @Test
    fun debeEntregarElCorreoNormalizado_cuandoSeEnviaConMayusculasYEspacios() =
        runTest(reglaCorrutinas.testDispatcher) {
            val espia = RepositorioAuthEspia()
            val viewModelEspiado = IniciarSesionViewModel(espia)
            viewModelEspiado.alCambiarCorreo("  Juan.Perez@Ejemplo.COM  ")
            viewModelEspiado.alCambiarContrasena("secreta")

            viewModelEspiado.alIniciarSesion()
            advanceUntilIdle()

            assertEquals("juan.perez@ejemplo.com", espia.ultimoCorreo)
            assertEquals("  Juan.Perez@Ejemplo.COM  ", viewModelEspiado.estado.value.correo)
        }

    @Test
    fun debeMarcarLosDosCamposYNoLlamarAlRepositorio_cuandoSeEnviaVacio() =
        runTest(reglaCorrutinas.testDispatcher) {
            val espia = RepositorioAuthEspia()
            val viewModelEspiado = IniciarSesionViewModel(espia)

            viewModelEspiado.alIniciarSesion()
            advanceUntilIdle()

            val estado = viewModelEspiado.estado.value
            assertEquals(R.string.validacion_correo_vacio, estado.errorCorreo)
            assertEquals(R.string.validacion_contrasena_vacia, estado.errorContrasena)
            assertNull(espia.ultimoCorreo)
        }

    @Test
    fun debeNoMarcarError_cuandoSaleDeUnCampoSinHaberEscrito() {
        viewModel.alPerderFoco(CampoIniciarSesion.CORREO)

        assertNull(viewModel.estado.value.errorCorreo)
    }

    @Test
    fun debeMarcarElFormato_cuandoSaleDelCorreoDespuesDeEscribir() {
        viewModel.alCambiarCorreo("hola")

        viewModel.alPerderFoco(CampoIniciarSesion.CORREO)

        assertEquals(R.string.validacion_correo_formato, viewModel.estado.value.errorCorreo)
    }

    @Test
    fun debeLimpiarElError_cuandoSeVuelveAEscribir() {
        viewModel.alCambiarCorreo("hola")
        viewModel.alPerderFoco(CampoIniciarSesion.CORREO)

        viewModel.alCambiarCorreo("hola@")

        assertNull(viewModel.estado.value.errorCorreo)
    }

    @Test
    fun debeQuedarCargando_mientrasEsperaAlRepositorio() = runTest(reglaCorrutinas.testDispatcher) {
        escribir("juan.perez@ejemplo.com", "secreta")

        viewModel.alIniciarSesion()

        assertTrue(viewModel.estado.value.cargando)
        advanceUntilIdle()
        assertFalse(viewModel.estado.value.cargando)
    }

    @Test
    fun debeVaciarElDestino_cuandoSeConsume() = runTest(reglaCorrutinas.testDispatcher) {
        escribir("juan.perez@ejemplo.com", "secreta")
        viewModel.alIniciarSesion()
        advanceUntilIdle()

        viewModel.alConsumirDestino()

        assertNull(viewModel.estado.value.destino)
    }
}
