package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.SavedStateHandle
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.R
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioCatalogosFalso
import mx.donchambitas.app.datos.falso.RepositorioIaFalso
import mx.donchambitas.app.datos.falso.RepositorioServiciosFalso
import mx.donchambitas.app.datos.falso.RepositorioTrabajadorFalso
import mx.donchambitas.app.dominio.modelo.ServicioFoto
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.ui.componentes.obtenerMensajeErrorRes
import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.util.ReglaCorrutinas
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * El camino del trabajador entre P-11, P-12 y P-13 con los repositorios falsos
 * sobre una sola fuente, y las filas de 2.4, 3.3, 4.4 y 4.5 de
 * DISENO-PERFIL-SERVICIOS.md que las pruebas de cada pantalla no cubren (S3-T15).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FlujoTrabajadorTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val fuente = FuenteDatosFalsa().apply {
        fijarSesionActiva(Sesion(usuarios.first { it.id == ID_TRABAJADOR }))
    }
    private val repositorioTrabajador = RepositorioTrabajadorFalso(fuente).apply { retrasoMs = 0 }
    private val repositorioServicios = RepositorioServiciosFalso(fuente).apply { retrasoMs = 0 }
    private val repositorioCatalogos = RepositorioCatalogosFalso(fuente).apply { retrasoMs = 0 }

    private val repositorioIa = RepositorioIaFalso().apply { retrasoMs = 0 }

    private fun miPerfil() = MiPerfilViewModel(repositorioTrabajador, repositorioCatalogos, repositorioIa)

    private fun misServicios() = MisServiciosViewModel(repositorioServicios, repositorioCatalogos)

    private fun servicio(servicioId: String? = null) = ServicioViewModel(
        repositorioServicios,
        repositorioCatalogos,
        repositorioIa,
        SavedStateHandle(mapOf(Ruta.CrearEditarServicio.ARG_SERVICIO_ID to servicioId))
    )

    // Recorridos completos

    @Test
    fun debeLeerLoGuardado_alVolverAAbrirP11YEnElPerfilPublico() = runTest(reglaCorrutinas.testDispatcher) {
        val primero = miPerfil()
        advanceUntilIdle()
        primero.alCambiarTitulo("Plomero certificado")
        primero.alCambiarHabilidadNueva("Tinacos")
        primero.alAgregarHabilidad()
        primero.alCambiarTelefono("2225559999")
        primero.alGuardar()
        advanceUntilIdle()
        assertEquals(R.string.mi_perfil_guardado, primero.estado.value.aviso)

        val segundo = miPerfil()
        advanceUntilIdle()

        val estado = segundo.estado.value
        assertEquals("Plomero certificado", estado.titulo)
        assertEquals("2225559999", estado.telefonoContacto)
        assertEquals(listOf("Boilers", "Cobre", "Drenajes", "Termofusion", "Tinacos"), estado.habilidades)
        val publico = (repositorioTrabajador.obtenerPerfilPublico(ID_TRABAJADOR) as Resultado.Exito).dato
        assertEquals("Plomero certificado", publico.titulo)
        assertTrue("Tinacos" in publico.habilidades)
    }

    @Test
    fun debeListarLoPublicadoYLoEditado_entreP13YP12() = runTest(reglaCorrutinas.testDispatcher) {
        val id = publicar("Instalación de boiler")

        val lista = misServicios()
        lista.alCargar()
        advanceUntilIdle()
        assertEquals(id, lista.estado.value.servicios.first().id)

        val editar = servicio(id)
        advanceUntilIdle()
        assertEquals("Instalación de boiler", editar.estado.value.titulo)
        editar.alCambiarTitulo("Boiler de paso")
        editar.alGuardar()
        advanceUntilIdle()
        assertTrue(editar.estado.value.cambiosGuardados)

        lista.alCargar(cambiosGuardados = true)
        advanceUntilIdle()

        val estado = lista.estado.value
        assertEquals(R.string.servicio_cambios_guardados, estado.aviso)
        assertEquals("Boiler de paso", estado.servicios.first { it.id == id }.titulo)
        assertEquals(2, estado.servicios.size)
    }

    @Test
    fun debeTraerLasTresFotos_alVolverDeP13() = runTest(reglaCorrutinas.testDispatcher) {
        val publicado = servicio()
        advanceUntilIdle()
        llenarServicio(publicado, "Instalación de boiler")
        publicado.alGuardar()
        advanceUntilIdle()
        repeat(3) { n ->
            publicado.alAgregarFoto(byteArrayOf(n.toByte()))
            advanceUntilIdle()
        }
        val id = publicado.estado.value.servicioId

        val lista = misServicios()
        lista.alCargar()
        advanceUntilIdle()

        val fotos = lista.estado.value.servicios.first { it.id == id }.fotos
        assertEquals(listOf(1, 2, 3), fotos.map { it.posicion })
    }

    @Test
    fun debeSacarDelPerfilPublicoLoPausado_yRegresarloAlReactivar() = runTest(reglaCorrutinas.testDispatcher) {
        val lista = misServicios()
        lista.alCargar()
        advanceUntilIdle()

        lista.alCambiarActivo(lista.estado.value.servicios.single())
        advanceUntilIdle()
        assertTrue(serviciosPublicos().isEmpty())

        lista.alCambiarActivo(lista.estado.value.servicios.single())
        advanceUntilIdle()
        assertEquals(listOf(ID_SERVICIO), serviciosPublicos())
    }

    @Test
    fun debePintarNoExisteEnP13_loQueEliminoP12() = runTest(reglaCorrutinas.testDispatcher) {
        val lista = misServicios()
        lista.alCargar()
        advanceUntilIdle()

        lista.alPedirEliminar(lista.estado.value.servicios.single())
        lista.alConfirmarEliminar()
        advanceUntilIdle()
        val editar = servicio(ID_SERVICIO)
        advanceUntilIdle()

        assertTrue(editar.estado.value.noExiste)
        assertFalse(fuente.fotosServicio.any { it.servicioId == ID_SERVICIO })
    }

    @Test
    fun debeQuedarVacia_alEliminarElUnicoServicio() = runTest(reglaCorrutinas.testDispatcher) {
        val lista = misServicios()
        lista.alCargar()
        advanceUntilIdle()

        lista.alPedirEliminar(lista.estado.value.servicios.single())
        lista.alConfirmarEliminar()
        advanceUntilIdle()

        val estado = lista.estado.value
        assertTrue(estado.servicios.isEmpty())
        assertEquals(R.string.mis_servicios_aviso_eliminado, estado.aviso)

        lista.alCargar()
        advanceUntilIdle()
        assertTrue(lista.estado.value.servicios.isEmpty())
        assertNull(lista.estado.value.error)
    }

    // 2.4, P-11: el paso 1 falla

    @Test
    fun debeMostrarElErrorEnLineaSinGuardar_cuandoFallaElPerfil() = runTest(reglaCorrutinas.testDispatcher) {
        val original = fuente.perfilesTrabajador.first { it.usuarioId == ID_TRABAJADOR }
        for (tipo in listOf(TipoError.RED, TipoError.DESCONOCIDO, TipoError.VALIDACION, TipoError.AUTENTICACION)) {
            val viewModel = miPerfil()
            advanceUntilIdle()
            viewModel.alCambiarTitulo("Plomero certificado")
            repositorioTrabajador.errorForzado = tipo
            viewModel.alGuardar()
            advanceUntilIdle()
            repositorioTrabajador.errorForzado = null

            val estado = viewModel.estado.value
            assertEquals(tipo, estado.errorPantalla)
            assertFalse(estado.guardando)
            assertFalse(estado.soloFaltanHabilidades)
            assertNull(estado.aviso)
            assertEquals("Plomero certificado", estado.titulo)
            assertEquals(original, fuente.perfilesTrabajador.first { it.usuarioId == ID_TRABAJADOR })
        }
    }

    @Test
    fun debeGuardarTodo_alReintentarTrasFallarElPerfilSinRed() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = miPerfil()
        advanceUntilIdle()
        viewModel.alCambiarTitulo("Plomero certificado")
        viewModel.alQuitarHabilidad("Cobre")
        repositorioTrabajador.errorForzado = TipoError.RED
        viewModel.alGuardar()
        advanceUntilIdle()

        repositorioTrabajador.errorForzado = null
        viewModel.alReintentarGuardado()
        advanceUntilIdle()

        assertNull(viewModel.estado.value.errorPantalla)
        assertEquals(R.string.mi_perfil_guardado, viewModel.estado.value.aviso)
        assertEquals("Plomero certificado", fuente.perfilesTrabajador.first { it.usuarioId == ID_TRABAJADOR }.titulo)
        assertFalse(fuente.habilidades.any { it.perfilId == ID_TRABAJADOR && it.habilidad == "Cobre" })
    }

    @Test
    fun debeTerminarEnAutenticacion_cuandoLaSesionSeFueAntesDeGuardar() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = miPerfil()
        advanceUntilIdle()

        fuente.fijarSesionActiva(null)
        viewModel.alGuardar()
        advanceUntilIdle()

        assertEquals(TipoError.AUTENTICACION, viewModel.estado.value.errorPantalla)
        assertNull(viewModel.estado.value.aviso)
    }

    // 3.3, P-12

    @Test
    fun debeDejarLaListaYAvisar_cuandoEliminarFalla() = runTest(reglaCorrutinas.testDispatcher) {
        val lista = misServicios()
        lista.alCargar()
        advanceUntilIdle()
        val antes = lista.estado.value.servicios

        repositorioServicios.errorForzado = TipoError.SERVIDOR
        lista.alPedirEliminar(antes.single())
        lista.alConfirmarEliminar()
        advanceUntilIdle()

        val estado = lista.estado.value
        assertEquals(antes, estado.servicios)
        assertNull(estado.procesandoId)
        assertNull(estado.servicioPorEliminar)
        assertEquals(obtenerMensajeErrorRes(TipoError.SERVIDOR), estado.aviso)
        assertTrue(fuente.servicios.any { it.id == ID_SERVICIO })
    }

    @Test
    fun debeAvisarConElMensajeDelTipo_cuandoPausarFalla() = runTest(reglaCorrutinas.testDispatcher) {
        val lista = misServicios()
        lista.alCargar()
        advanceUntilIdle()
        val antes = lista.estado.value.servicios

        for (tipo in listOf(TipoError.SERVIDOR, TipoError.DESCONOCIDO, TipoError.AUTENTICACION, TipoError.VALIDACION)) {
            repositorioServicios.errorForzado = tipo
            lista.alCambiarActivo(antes.single())
            advanceUntilIdle()

            assertEquals(antes, lista.estado.value.servicios)
            assertEquals(obtenerMensajeErrorRes(tipo), lista.estado.value.aviso)
            lista.alConsumirAviso()
        }
        assertTrue(fuente.servicios.first { it.id == ID_SERVICIO }.activo)
    }

    // 4.4, fotos

    @Test
    fun debeAvisarConValidacion_cuandoLaBaseYaTieneTresFotos() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = servicio(ID_SERVICIO)
        advanceUntilIdle()
        // Otra sesion llego al tope de 3 mientras esta pantalla mostraba una sola.
        fuente.fotosServicio += fotoSembrada(2)
        fuente.fotosServicio += fotoSembrada(3)

        viewModel.alAgregarFoto(byteArrayOf(1))
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(listOf(1), estado.fotos.map { it.posicion })
        assertFalse(estado.subiendoFoto)
        assertEquals(obtenerMensajeErrorRes(TipoError.VALIDACION), estado.aviso)
        assertEquals(3, fuente.fotosServicio.count { it.servicioId == ID_SERVICIO })
    }

    @Test
    fun debeDejarLugar_alQuitarUnaDeTresFotos() = runTest(reglaCorrutinas.testDispatcher) {
        fuente.fotosServicio += fotoSembrada(2)
        fuente.fotosServicio += fotoSembrada(3)
        val viewModel = servicio(ID_SERVICIO)
        advanceUntilIdle()
        assertEquals(3, viewModel.estado.value.fotos.size)

        viewModel.alPedirQuitarFoto(viewModel.estado.value.fotos.first { it.posicion == 2 })
        viewModel.alConfirmarQuitarFoto()
        advanceUntilIdle()
        viewModel.alAgregarFoto(byteArrayOf(1))
        advanceUntilIdle()

        val fotos = viewModel.estado.value.fotos
        assertTrue(fotos.all { it is FotoEnPantalla.Guardada })
        assertEquals(listOf(1, 2, 3), fotos.map { it.posicion }.sorted())
        assertNull(viewModel.estado.value.aviso)
    }

    // 4.5, P-13

    @Test
    fun debeSeguirEnCrearSinAgregarNada_cuandoPublicarFalla() = runTest(reglaCorrutinas.testDispatcher) {
        val antes = fuente.servicios.size
        for (tipo in listOf(TipoError.RED, TipoError.DESCONOCIDO, TipoError.VALIDACION, TipoError.AUTENTICACION)) {
            val viewModel = servicio()
            advanceUntilIdle()
            llenarServicio(viewModel, "Instalación de boiler")
            repositorioServicios.errorForzado = tipo
            viewModel.alGuardar()
            advanceUntilIdle()
            repositorioServicios.errorForzado = null

            val estado = viewModel.estado.value
            assertEquals(tipo, estado.errorPantalla)
            assertTrue(estado.creando)
            assertNull(estado.servicioId)
            assertNull(estado.aviso)
            assertEquals("Instalación de boiler", estado.titulo)
            assertEquals(antes, fuente.servicios.size)
        }
    }

    @Test
    fun debeConservarLoEscritoSinRegresar_cuandoGuardarCambiosFalla() = runTest(reglaCorrutinas.testDispatcher) {
        val original = fuente.servicios.first { it.id == ID_SERVICIO }
        for (tipo in listOf(TipoError.RED, TipoError.DESCONOCIDO, TipoError.VALIDACION, TipoError.AUTENTICACION)) {
            val viewModel = servicio(ID_SERVICIO)
            advanceUntilIdle()
            viewModel.alCambiarTitulo("Fugas y grifería")
            repositorioServicios.errorForzado = tipo
            viewModel.alGuardar()
            advanceUntilIdle()
            repositorioServicios.errorForzado = null

            val estado = viewModel.estado.value
            assertEquals(tipo, estado.errorPantalla)
            assertFalse(estado.guardando)
            assertFalse(estado.cambiosGuardados)
            assertEquals("Fugas y grifería", estado.titulo)
            assertEquals(original, fuente.servicios.first { it.id == ID_SERVICIO })
        }
    }

    /** Publica en una P-13 nueva y devuelve el id con el que paso a modo editar. */
    private fun TestScope.publicar(titulo: String): String {
        val viewModel = servicio()
        advanceUntilIdle()
        llenarServicio(viewModel, titulo)
        viewModel.alGuardar()
        advanceUntilIdle()
        assertEquals(R.string.servicio_publicado, viewModel.estado.value.aviso)
        return checkNotNull(viewModel.estado.value.servicioId)
    }

    private fun llenarServicio(viewModel: ServicioViewModel, titulo: String) {
        viewModel.alCambiarTitulo(titulo)
        viewModel.alElegirCategoria(1)
        viewModel.alCambiarDescripcion("Instalo boilers de paso y de depósito.")
    }

    private suspend fun serviciosPublicos(): List<String> =
        (repositorioTrabajador.obtenerPerfilPublico(ID_TRABAJADOR) as Resultado.Exito).dato.servicios.map { it.id }

    private fun fotoSembrada(posicion: Int) = ServicioFoto(
        id = "foto-prueba-$posicion",
        servicioId = ID_SERVICIO,
        url = "https://ejemplo.com/$posicion.jpg",
        posicion = posicion,
        creadoEn = Instant.EPOCH
    )

    private companion object {
        const val ID_TRABAJADOR = "usr-trab-1"
        const val ID_SERVICIO = "serv-1"
    }
}
