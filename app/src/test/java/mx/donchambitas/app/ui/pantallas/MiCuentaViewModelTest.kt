package mx.donchambitas.app.ui.pantallas

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.Usuario
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth
import mx.donchambitas.app.dominio.repositorio.RepositorioUsuario
import mx.donchambitas.app.ui.navegacion.AvisosEnlace
import mx.donchambitas.app.util.DatosPrueba
import mx.donchambitas.app.util.ReglaCorrutinas
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MiCuentaViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val repositorioUsuario = mockk<RepositorioUsuario>()
    private val repositorioAuth = mockk<RepositorioAuth>()
    private val avisosEnlace = AvisosEnlace()
    private val usuario = DatosPrueba.crearUsuario()

    private fun crearViewModel(
        lectura: Resultado<Usuario> = Resultado.Exito(usuario)
    ): MiCuentaViewModel {
        coEvery { repositorioUsuario.obtenerMiUsuario() } returns lectura
        return MiCuentaViewModel(repositorioUsuario, repositorioAuth, avisosEnlace)
    }

    @Test
    fun debeMostrarLaFicha_cuandoSeLee() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        assertTrue(viewModel.estado.value.cargando)

        advanceUntilIdle()

        assertFalse(viewModel.estado.value.cargando)
        assertEquals(usuario, viewModel.estado.value.usuario)
        assertEquals(SeccionMiCuenta.NINGUNA, viewModel.estado.value.seccion)
    }

    @Test
    fun debeOfrecerReintentar_cuandoLaLecturaFalla() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel(Resultado.Error(TipoError.RED, "sin red"))
        advanceUntilIdle()
        assertEquals(TipoError.RED, viewModel.estado.value.errorCarga)

        coEvery { repositorioUsuario.obtenerMiUsuario() } returns Resultado.Exito(usuario)
        viewModel.alReintentar()
        advanceUntilIdle()

        assertNull(viewModel.estado.value.errorCarga)
        assertEquals(usuario, viewModel.estado.value.usuario)
    }

    @Test
    fun debeLlenarLosCamposConLaFicha_cuandoSeAbreLaEdicion() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.alAbrirSeccion(SeccionMiCuenta.DATOS)

        val estado = viewModel.estado.value
        assertEquals(SeccionMiCuenta.DATOS, estado.seccion)
        assertEquals(usuario.nombre, estado.nombre)
        assertEquals(usuario.apellidos, estado.apellidos)
        assertEquals(usuario.telefono, estado.telefono)
    }

    @Test
    fun debeMandarLosDatosRecortadosYPintarLaFichaNueva_cuandoSeGuarda() = runTest(reglaCorrutinas.testDispatcher) {
        val actualizado = usuario.copy(nombre = "Refugio", apellidos = "Martinez Luna", telefono = "4771234567")
        coEvery { repositorioUsuario.actualizarMiUsuario(any(), any(), any()) } returns Resultado.Exito(actualizado)
        val viewModel = crearViewModel()
        advanceUntilIdle()
        viewModel.alAbrirSeccion(SeccionMiCuenta.DATOS)
        viewModel.alCambiarNombre("  Refugio ")
        viewModel.alCambiarApellidos(" Martinez Luna ")
        viewModel.alCambiarTelefono("477-123-4567")

        viewModel.alGuardarDatos()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repositorioUsuario.actualizarMiUsuario("Refugio", "Martinez Luna", "4771234567")
        }
        coVerify(exactly = 1) { repositorioUsuario.obtenerMiUsuario() }
        val estado = viewModel.estado.value
        assertEquals(actualizado, estado.usuario)
        assertEquals(SeccionMiCuenta.NINGUNA, estado.seccion)
        assertEquals(MensajeMiCuenta.Exito(R.string.mi_cuenta_datos_guardados), estado.mensaje)
    }

    @Test
    fun debeMarcarTodosLosCamposYNoLlamarAlRepositorio_cuandoFallan() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        viewModel.alAbrirSeccion(SeccionMiCuenta.DATOS)
        viewModel.alCambiarNombre("  ")
        viewModel.alCambiarApellidos("")
        viewModel.alCambiarTelefono("")

        viewModel.alGuardarDatos()
        advanceUntilIdle()

        coVerify(exactly = 0) { repositorioUsuario.actualizarMiUsuario(any(), any(), any()) }
        val estado = viewModel.estado.value
        assertEquals(R.string.validacion_nombre_vacio, estado.errorNombre)
        assertEquals(R.string.validacion_apellidos_vacio, estado.errorApellidos)
        assertEquals(R.string.validacion_telefono_vacio, estado.errorTelefono)
        assertEquals(SeccionMiCuenta.DATOS, estado.seccion)
    }

    @Test
    fun debeAvisarYDejarLaSeccionAbierta_cuandoGuardarFalla() = runTest(reglaCorrutinas.testDispatcher) {
        coEvery { repositorioUsuario.actualizarMiUsuario(any(), any(), any()) } returns
            Resultado.Error(TipoError.RED, "sin red")
        val viewModel = crearViewModel()
        advanceUntilIdle()
        viewModel.alAbrirSeccion(SeccionMiCuenta.DATOS)

        viewModel.alGuardarDatos()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(MensajeMiCuenta.Error(TipoError.RED), estado.mensaje)
        assertEquals(SeccionMiCuenta.DATOS, estado.seccion)
        assertFalse(estado.guardando)
        assertEquals(usuario, estado.usuario)
    }

    @Test
    fun debeDejarLaFichaComoEstaba_cuandoSeCancela() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        viewModel.alAbrirSeccion(SeccionMiCuenta.DATOS)
        viewModel.alCambiarNombre("Otro")

        viewModel.alCancelar()

        assertEquals(SeccionMiCuenta.NINGUNA, viewModel.estado.value.seccion)
        assertEquals(usuario, viewModel.estado.value.usuario)
        coVerify(exactly = 0) { repositorioUsuario.actualizarMiUsuario(any(), any(), any()) }
    }

    @Test
    fun noDebeLlamarAlRepositorio_cuandoLaContrasenaEsCorta() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        viewModel.alAbrirSeccion(SeccionMiCuenta.CONTRASENA)
        viewModel.alCambiarContrasena("1234567")

        viewModel.alGuardarContrasena()
        advanceUntilIdle()

        coVerify(exactly = 0) { repositorioAuth.cambiarContrasena(any()) }
        assertEquals(R.string.validacion_contrasena_corta, viewModel.estado.value.errorContrasena)
    }

    @Test
    fun debeCambiarlaYCerrarLaSeccion_cuandoLaContrasenaEsValida() = runTest(reglaCorrutinas.testDispatcher) {
        coEvery { repositorioAuth.cambiarContrasena(any()) } returns Resultado.Exito(Unit)
        val viewModel = crearViewModel()
        advanceUntilIdle()
        viewModel.alAbrirSeccion(SeccionMiCuenta.CONTRASENA)
        viewModel.alCambiarContrasena("nueva1234")

        viewModel.alGuardarContrasena()
        advanceUntilIdle()

        coVerify(exactly = 1) { repositorioAuth.cambiarContrasena("nueva1234") }
        val estado = viewModel.estado.value
        assertEquals(SeccionMiCuenta.NINGUNA, estado.seccion)
        assertEquals("", estado.contrasenaNueva)
        assertEquals(MensajeMiCuenta.Exito(R.string.mi_cuenta_contrasena_actualizada), estado.mensaje)
    }

    @Test
    fun debeDecirQueEsIgual_cuandoAuthRechazaLaMismaContrasena() = runTest(reglaCorrutinas.testDispatcher) {
        coEvery { repositorioAuth.cambiarContrasena(any()) } returns
            Resultado.Error(TipoError.VALIDACION, "same_password")
        val viewModel = crearViewModel()
        advanceUntilIdle()
        viewModel.alAbrirSeccion(SeccionMiCuenta.CONTRASENA)
        viewModel.alCambiarContrasena("actual1234")

        viewModel.alGuardarContrasena()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(R.string.mi_cuenta_contrasena_igual, estado.errorContrasena)
        assertEquals(SeccionMiCuenta.CONTRASENA, estado.seccion)
        assertNull(estado.mensaje)
    }

    @Test
    fun debeAvisarSinConexion_cuandoCerrarSesionFalla() = runTest(reglaCorrutinas.testDispatcher) {
        coEvery { repositorioAuth.cerrarSesion() } returns Resultado.Error(TipoError.RED, "sin red")
        val viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.alCerrarSesion()
        advanceUntilIdle()

        assertEquals(MensajeMiCuenta.Error(TipoError.RED), viewModel.estado.value.mensaje)
        assertFalse(viewModel.estado.value.cerrandoSesion)
    }

    @Test
    fun noDebeAvisarNada_cuandoCerrarSesionSaleBien() = runTest(reglaCorrutinas.testDispatcher) {
        coEvery { repositorioAuth.cerrarSesion() } returns Resultado.Exito(Unit)
        val viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.alCerrarSesion()
        advanceUntilIdle()

        coVerify(exactly = 1) { repositorioAuth.cerrarSesion() }
        assertNull(viewModel.estado.value.mensaje)
    }

    @Test
    fun debeAbrirLaContrasenaUnaSolaVez_cuandoSeLlegoPorElEnlace() = runTest(reglaCorrutinas.testDispatcher) {
        avisosEnlace.avisarRecuperacion()

        val primera = crearViewModel()
        val segunda = crearViewModel()

        assertEquals(SeccionMiCuenta.CONTRASENA, primera.estado.value.seccion)
        assertEquals(SeccionMiCuenta.NINGUNA, segunda.estado.value.seccion)
    }

    @Test
    fun debeSacarLasIniciales_delNombreYElPrimerApellido() {
        assertEquals("JP", iniciales("Juan", "Pérez Hernández"))
        assertEquals("R", iniciales(" refugio ", ""))
    }
}
