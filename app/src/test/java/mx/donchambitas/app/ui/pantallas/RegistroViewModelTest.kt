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

class RegistroViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private lateinit var fuente: FuenteDatosFalsa
    private lateinit var repositorio: RepositorioAuthFalso
    private lateinit var viewModel: RegistroViewModel

    @Before
    fun preparar() {
        fuente = FuenteDatosFalsa()
        repositorio = RepositorioAuthFalso(fuente).apply { retrasoMs = 0L }
        viewModel = RegistroViewModel(repositorio)
    }

    private fun llenar(
        vm: RegistroViewModel = viewModel,
        rol: RolUsuario? = RolUsuario.CLIENTE,
        correo: String = "refugio@ejemplo.mx"
    ) {
        rol?.let(vm::alElegirRol)
        vm.alCambiarNombre("Refugio")
        vm.alCambiarApellidos("Martínez Luna")
        vm.alCambiarCorreo(correo)
        vm.alCambiarContrasena("12345678")
        vm.alCambiarTelefono("4771234567")
    }

    @Test
    fun debeIrAInicioCliente_cuandoSeRegistraComoCliente() = runTest(reglaCorrutinas.testDispatcher) {
        llenar(rol = RolUsuario.CLIENTE)

        viewModel.alRegistrar()
        advanceUntilIdle()

        assertEquals(Ruta.InicioCliente, viewModel.estado.value.destino)
        assertFalse(viewModel.estado.value.cargando)
    }

    @Test
    fun debeIrAInicioTrabajador_cuandoSeRegistraComoTrabajador() = runTest(reglaCorrutinas.testDispatcher) {
        llenar(rol = RolUsuario.TRABAJADOR)

        viewModel.alRegistrar()
        advanceUntilIdle()

        assertEquals(Ruta.InicioTrabajador, viewModel.estado.value.destino)
    }

    @Test
    fun debePoderIniciarSesion_cuandoLaCuentaAcabaDeCrearse() = runTest(reglaCorrutinas.testDispatcher) {
        llenar(rol = RolUsuario.TRABAJADOR, correo = "nuevo@ejemplo.mx")
        viewModel.alRegistrar()
        advanceUntilIdle()

        val inicioSesion = IniciarSesionViewModel(repositorio)
        inicioSesion.alCambiarCorreo("nuevo@ejemplo.mx")
        inicioSesion.alCambiarContrasena("12345678")
        inicioSesion.alIniciarSesion()
        advanceUntilIdle()

        assertEquals(Ruta.InicioTrabajador, inicioSesion.estado.value.destino)
    }

    @Test
    fun debeMostrarElMensajeDeLaBaseYConservarTodo_cuandoElCorreoYaExiste() =
        runTest(reglaCorrutinas.testDispatcher) {
            val existente = fuente.usuarios.first().correo
            llenar(rol = RolUsuario.TRABAJADOR, correo = existente)

            viewModel.alRegistrar()
            advanceUntilIdle()

            val estado = viewModel.estado.value
            assertEquals(TipoError.VALIDACION, estado.errorPantalla)
            assertEquals("El correo ya esta registrado, inicia sesion", estado.mensajePantalla)
            assertNull(estado.destino)
            assertEquals(RolUsuario.TRABAJADOR, estado.rol)
            assertEquals("12345678", estado.contrasena)
        }

    @Test
    fun debeMarcarRedSinMensajeDeLaBase_cuandoNoHayConexion() = runTest(reglaCorrutinas.testDispatcher) {
        repositorio.errorForzado = TipoError.RED
        llenar()

        viewModel.alRegistrar()
        advanceUntilIdle()

        assertEquals(TipoError.RED, viewModel.estado.value.errorPantalla)
        assertNull(viewModel.estado.value.mensajePantalla)
        assertEquals("Refugio", viewModel.estado.value.nombre)
    }

    @Test
    fun debeCrearLaCuenta_cuandoSeReintentaYaConConexion() = runTest(reglaCorrutinas.testDispatcher) {
        repositorio.errorForzado = TipoError.RED
        llenar()
        viewModel.alRegistrar()
        advanceUntilIdle()

        repositorio.errorForzado = null
        viewModel.alReintentar()
        advanceUntilIdle()

        assertNull(viewModel.estado.value.errorPantalla)
        assertEquals(Ruta.InicioCliente, viewModel.estado.value.destino)
    }

    @Test
    fun debeEntregarLosDatosNormalizados_cuandoSeEnvia() = runTest(reglaCorrutinas.testDispatcher) {
        val espia = RepositorioAuthEspia()
        val vm = RegistroViewModel(espia)
        vm.alElegirRol(RolUsuario.CLIENTE)
        vm.alCambiarNombre("  Refugio ")
        vm.alCambiarApellidos(" Martínez Luna  ")
        vm.alCambiarCorreo("  Refugio@Ejemplo.MX ")
        vm.alCambiarContrasena("12345678")
        vm.alCambiarTelefono("477 123 45 67")

        vm.alRegistrar()
        advanceUntilIdle()

        assertEquals("refugio@ejemplo.mx", espia.ultimoCorreo)
        assertEquals("Refugio", espia.ultimoNombre)
        assertEquals("Martínez Luna", espia.ultimosApellidos)
        assertEquals("4771234567", espia.ultimoTelefono)
    }

    @Test
    fun debeMarcarTodoYNoLlamarAlRepositorio_cuandoSeEnviaVacio() = runTest(reglaCorrutinas.testDispatcher) {
        val espia = RepositorioAuthEspia()
        val vm = RegistroViewModel(espia)

        vm.alRegistrar()
        advanceUntilIdle()

        val estado = vm.estado.value
        assertEquals(R.string.validacion_rol_sin_elegir, estado.errorRol)
        assertEquals(R.string.validacion_nombre_vacio, estado.errorNombre)
        assertEquals(R.string.validacion_apellidos_vacio, estado.errorApellidos)
        assertEquals(R.string.validacion_correo_vacio, estado.errorCorreo)
        assertEquals(R.string.validacion_contrasena_corta, estado.errorContrasena)
        assertEquals(R.string.validacion_telefono_vacio, estado.errorTelefono)
        assertNull(espia.ultimoCorreo)
    }

    @Test
    fun debeNoEnviar_cuandoFaltaSoloElRol() = runTest(reglaCorrutinas.testDispatcher) {
        val espia = RepositorioAuthEspia()
        val vm = RegistroViewModel(espia)
        llenar(vm = vm, rol = null)

        vm.alRegistrar()
        advanceUntilIdle()

        assertEquals(R.string.validacion_rol_sin_elegir, vm.estado.value.errorRol)
        assertNull(espia.ultimoCorreo)
    }

    @Test
    fun debeDescartarLoQueNoSeaDigitoYCortarEnDiez_cuandoSeEscribeElTelefono() {
        viewModel.alCambiarTelefono("477-123 45ab678901")

        assertEquals("4771234567", viewModel.estado.value.telefono)
    }

    @Test
    fun debeMarcarLaContrasena_cuandoSaleDelCampoConSieteCaracteres() {
        viewModel.alCambiarContrasena("1234567")

        viewModel.alPerderFoco(CampoRegistro.CONTRASENA)

        assertEquals(R.string.validacion_contrasena_corta, viewModel.estado.value.errorContrasena)
    }

    @Test
    fun debeNoMarcarError_cuandoSaleDeUnCampoSinHaberEscrito() {
        viewModel.alPerderFoco(CampoRegistro.NOMBRE)

        assertNull(viewModel.estado.value.errorNombre)
    }

    @Test
    fun debeQuedarCargando_mientrasEsperaAlRepositorio() = runTest(reglaCorrutinas.testDispatcher) {
        llenar()

        viewModel.alRegistrar()

        assertTrue(viewModel.estado.value.cargando)
        advanceUntilIdle()
        assertFalse(viewModel.estado.value.cargando)
    }

    @Test
    fun debeVaciarElDestino_cuandoSeConsume() = runTest(reglaCorrutinas.testDispatcher) {
        llenar()
        viewModel.alRegistrar()
        advanceUntilIdle()

        viewModel.alConsumirDestino()

        assertNull(viewModel.estado.value.destino)
    }
}
