package mx.donchambitas.app.ui.pantallas

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioAuthFalso
import mx.donchambitas.app.datos.falso.RepositorioUsuarioFalso
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.ui.navegacion.AvisosEnlace
import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.util.ReglaCorrutinas
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * El camino completo de la autenticacion contra RepositorioAuthFalso (S2-T16):
 * las pantallas se pasan la sesion entre si a traves del repositorio, sin
 * mockk y sin red. Cubre tambien las filas de 2.4 y 3.5 de
 * DISENO-AUTENTICACION.md que las pruebas de cada ViewModel no tenian.
 *
 * El falso no revisa la contrasena (decision 2 del ticket): "credenciales
 * rechazadas" se prueba con un correo que no existe.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FlujoAutenticacionTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private lateinit var fuente: FuenteDatosFalsa
    private lateinit var repositorioAuth: RepositorioAuthFalso

    @Before
    fun preparar() {
        // La fuente arranca con la sesion de cliente1 para desarrollar
        // pantallas. Aqui se empieza como una instalacion nueva: sin ella, el
        // registro de cliente pasaria aunque no dejara sesion.
        fuente = FuenteDatosFalsa().apply { fijarSesionActiva(null) }
        repositorioAuth = RepositorioAuthFalso(fuente).apply { retrasoMs = 0 }
    }

    private fun registroLleno(
        rol: RolUsuario = RolUsuario.CLIENTE,
        correo: String = CORREO_ESCRITO
    ) = RegistroViewModel(repositorioAuth).apply {
        alElegirRol(rol)
        alCambiarNombre("Refugio")
        alCambiarApellidos("Sánchez Mora")
        alCambiarCorreo(correo)
        alCambiarContrasena(CONTRASENA)
        alCambiarTelefono("5512345678")
    }

    private fun inicioLleno(correo: String = CORREO_ESCRITO) =
        IniciarSesionViewModel(repositorioAuth).apply {
            alCambiarCorreo(correo)
            alCambiarContrasena(CONTRASENA)
        }

    private fun TestScope.registrar(rol: RolUsuario = RolUsuario.CLIENTE): RegistroViewModel =
        registroLleno(rol).apply {
            alRegistrar()
            advanceUntilIdle()
        }

    private fun TestScope.destinoDeSplash(): Ruta? {
        val splash = SplashViewModel(repositorioAuth)
        advanceUntilIdle()
        return splash.estado.value.destino
    }

    // Recorridos completos

    @Test
    fun debeIrAIniciarSesion_cuandoSeAbreSinCuentaNiSesion() = runTest(reglaCorrutinas.testDispatcher) {
        assertEquals(Ruta.IniciarSesion, destinoDeSplash())
    }

    @Test
    fun debeEntrarAlInicioDelClienteYRecordarlo_cuandoSeRegistraComoCliente() = runTest(reglaCorrutinas.testDispatcher) {
        val registro = registrar(RolUsuario.CLIENTE)

        assertEquals(Ruta.InicioCliente, registro.estado.value.destino)
        assertEquals(Ruta.InicioCliente, destinoDeSplash())
    }

    @Test
    fun debeEntrarAlInicioDelTrabajadorYRecordarlo_cuandoSeRegistraComoTrabajador() = runTest(reglaCorrutinas.testDispatcher) {
        val registro = registrar(RolUsuario.TRABAJADOR)

        assertEquals(Ruta.InicioTrabajador, registro.estado.value.destino)
        assertEquals(Ruta.InicioTrabajador, destinoDeSplash())
    }

    @Test
    fun debeGuardarLaCuentaConElCorreoNormalizado_cuandoSeRegistra() = runTest(reglaCorrutinas.testDispatcher) {
        registrar()

        assertTrue(fuente.usuarios.any { it.correo == CORREO_NORMALIZADO })
    }

    @Test
    fun debeVolverAEntrarConElMismoCorreo_cuandoCierraSesionYAbreLaAplicacion() = runTest(reglaCorrutinas.testDispatcher) {
        registrar(RolUsuario.TRABAJADOR)
        val miCuenta = MiCuentaViewModel(
            RepositorioUsuarioFalso(fuente).apply { retrasoMs = 0 },
            repositorioAuth,
            AvisosEnlace()
        )
        advanceUntilIdle()

        miCuenta.alCerrarSesion()
        advanceUntilIdle()
        assertEquals(Ruta.IniciarSesion, destinoDeSplash())

        val inicio = inicioLleno()
        inicio.alIniciarSesion()
        advanceUntilIdle()
        assertEquals(Ruta.InicioTrabajador, inicio.estado.value.destino)
        assertEquals(Ruta.InicioTrabajador, destinoDeSplash())
    }

    @Test
    fun debeRechazarElCorreoDuplicadoYConservarLoCapturado_cuandoSeRegistraDosVeces() = runTest(reglaCorrutinas.testDispatcher) {
        registrar()

        val segundo = registroLleno(correo = "  REFUGIO@EJEMPLO.MX ")
        segundo.alRegistrar()
        advanceUntilIdle()

        val estado = segundo.estado.value
        assertEquals(TipoError.CORREO_DUPLICADO, estado.errorPantalla)
        assertNull(estado.destino)
        assertEquals("  REFUGIO@EJEMPLO.MX ", estado.correo)
        assertEquals(CONTRASENA, estado.contrasena)
        assertEquals(RolUsuario.CLIENTE, estado.rol)
        assertEquals(1, fuente.usuarios.count { it.correo == CORREO_NORMALIZADO })
    }

    @Test
    fun debeRechazarSinNavegarYConservarLaContrasena_cuandoElCorreoNoTieneCuenta() = runTest(reglaCorrutinas.testDispatcher) {
        val inicio = inicioLleno(correo = "nadie@ejemplo.mx")

        inicio.alIniciarSesion()
        advanceUntilIdle()

        val estado = inicio.estado.value
        assertEquals(TipoError.AUTENTICACION, estado.errorPantalla)
        assertNull(estado.destino)
        assertEquals(CONTRASENA, estado.contrasena)
        assertEquals(Ruta.IniciarSesion, destinoDeSplash())
    }

    @Test
    fun debeConfirmarElEnvio_existaONoLaCuenta() = runTest(reglaCorrutinas.testDispatcher) {
        registrar()

        listOf(CORREO_ESCRITO, "nadie@ejemplo.mx").forEach { correo ->
            val recuperar = RecuperarContrasenaViewModel(repositorioAuth)
            recuperar.alCambiarCorreo(correo)
            recuperar.alEnviar()
            advanceUntilIdle()

            assertTrue("P-04 con $correo debe pasar a la confirmación", recuperar.estado.value.enviado)
            assertNull(recuperar.estado.value.errorPantalla)
        }
    }

    // Filas de 2.4 sin prueba en IniciarSesionViewModelTest

    @Test
    fun debeMostrarElErrorSinNavegarNiBorrar_cuandoIniciarSesionFallaPorServidorDesconocidoOValidacion() = runTest(reglaCorrutinas.testDispatcher) {
        registrar()

        listOf(TipoError.SERVIDOR, TipoError.DESCONOCIDO, TipoError.VALIDACION).forEach { tipo ->
            repositorioAuth.errorForzado = tipo
            val inicio = inicioLleno()
            inicio.alIniciarSesion()
            advanceUntilIdle()

            val estado = inicio.estado.value
            assertEquals(tipo, estado.errorPantalla)
            assertNull("Con $tipo P-02 no debe navegar", estado.destino)
            assertEquals(CORREO_ESCRITO, estado.correo)
            assertEquals(CONTRASENA, estado.contrasena)
        }
    }

    @Test
    fun debeEntrarAlReintentar_cuandoLaRedVuelveEnIniciarSesion() = runTest(reglaCorrutinas.testDispatcher) {
        registrar(RolUsuario.TRABAJADOR)
        repositorioAuth.errorForzado = TipoError.RED
        val inicio = inicioLleno()
        inicio.alIniciarSesion()
        advanceUntilIdle()
        assertEquals(TipoError.RED, inicio.estado.value.errorPantalla)

        repositorioAuth.errorForzado = null
        inicio.alReintentar()
        advanceUntilIdle()

        assertNull(inicio.estado.value.errorPantalla)
        assertEquals(Ruta.InicioTrabajador, inicio.estado.value.destino)
    }

    // Filas de 3.5 sin prueba en RegistroViewModelTest

    @Test
    fun debeMostrarElErrorSinNavegarNiCrearCuenta_cuandoElRegistroFallaPorOtroTipo() = runTest(reglaCorrutinas.testDispatcher) {
        listOf(TipoError.SERVIDOR, TipoError.DESCONOCIDO, TipoError.VALIDACION, TipoError.AUTENTICACION).forEach { tipo ->
            repositorioAuth.errorForzado = tipo
            val registro = registroLleno(RolUsuario.TRABAJADOR)
            registro.alRegistrar()
            advanceUntilIdle()

            val estado = registro.estado.value
            assertEquals(tipo, estado.errorPantalla)
            assertNull("Con $tipo P-03 no debe navegar", estado.destino)
            assertEquals(CORREO_ESCRITO, estado.correo)
            assertEquals(CONTRASENA, estado.contrasena)
            assertEquals(RolUsuario.TRABAJADOR, estado.rol)
        }
        assertTrue(fuente.usuarios.none { it.correo == CORREO_NORMALIZADO })
    }

    @Test
    fun debeCrearLaCuentaAlReintentar_cuandoLaRedVuelveEnElRegistro() = runTest(reglaCorrutinas.testDispatcher) {
        repositorioAuth.errorForzado = TipoError.RED
        val registro = registroLleno(RolUsuario.TRABAJADOR)
        registro.alRegistrar()
        advanceUntilIdle()
        assertEquals(TipoError.RED, registro.estado.value.errorPantalla)

        repositorioAuth.errorForzado = null
        registro.alReintentar()
        advanceUntilIdle()

        assertNull(registro.estado.value.errorPantalla)
        assertEquals(Ruta.InicioTrabajador, registro.estado.value.destino)
        assertEquals(1, fuente.usuarios.count { it.correo == CORREO_NORMALIZADO })
    }

    private companion object {
        const val CORREO_ESCRITO = " Refugio@Ejemplo.mx "
        const val CORREO_NORMALIZADO = "refugio@ejemplo.mx"
        const val CONTRASENA = "12345678"
    }
}
