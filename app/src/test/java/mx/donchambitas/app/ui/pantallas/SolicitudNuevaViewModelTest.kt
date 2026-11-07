package mx.donchambitas.app.ui.pantallas

import java.math.BigDecimal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.R
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioCatalogosFalso
import mx.donchambitas.app.datos.falso.RepositorioSolicitudesFalso
import mx.donchambitas.app.dominio.modelo.EstadoSolicitud
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.util.ReglaCorrutinas
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** P-08 contra los falsos, sin retraso (S4-T02, DISENO-SOLICITUDES-BUSQUEDA.md 4). */
@OptIn(ExperimentalCoroutinesApi::class)
class SolicitudNuevaViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val fuente = FuenteDatosFalsa().apply {
        fijarSesionActiva(Sesion(usuarios.first { it.id == ID_CLIENTE }))
    }
    private val repositorioSolicitudes = RepositorioSolicitudesFalso(fuente).apply { retrasoMs = 0 }
    private val repositorioCatalogos = RepositorioCatalogosFalso(fuente).apply { retrasoMs = 0 }

    private fun crearViewModel() = SolicitudNuevaViewModel(repositorioSolicitudes, repositorioCatalogos)

    /** Llena todos los campos obligatorios con datos validos. */
    private fun SolicitudNuevaViewModel.llenar() {
        alCambiarTitulo("Reparar una fuga")
        alElegirCategoria(1)
        alCambiarDescripcion("Hay una fuga bajo el lavabo del baño")
        // El primer estado de la lista no siempre trae municipios en el catalogo falso.
        alElegirEstado(fuente.municipios.first().estadoId)
    }

    private suspend fun kotlinx.coroutines.test.TestScope.cargadoConUbicacion(): SolicitudNuevaViewModel {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        viewModel.llenar()
        advanceUntilIdle()
        viewModel.alElegirMunicipio(viewModel.estado.value.municipios.first().id)
        return viewModel
    }

    @Test
    fun debeArrancarCargandoYLlenarLosCatalogos() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        assertTrue(viewModel.estado.value.cargando)

        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.cargando)
        assertNull(estado.errorCarga)
        assertEquals(16, estado.categorias.size)
        assertTrue(estado.estados.isNotEmpty())
    }

    @Test
    fun debeMostrarElErrorDeCarga_yReintentar() = runTest(reglaCorrutinas.testDispatcher) {
        repositorioCatalogos.errorForzado = TipoError.RED
        val viewModel = crearViewModel()
        advanceUntilIdle()
        assertEquals(TipoError.RED, viewModel.estado.value.errorCarga)

        repositorioCatalogos.errorForzado = null
        viewModel.alReintentarCarga()
        advanceUntilIdle()

        assertNull(viewModel.estado.value.errorCarga)
        assertTrue(viewModel.estado.value.categorias.isNotEmpty())
    }

    @Test
    fun debeTraerLosMunicipiosDelEstado_yBorrarElMunicipioAlCambiarlo() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = cargadoConUbicacion()
        assertNotNull(viewModel.estado.value.municipioId)
        assertTrue(viewModel.estado.value.municipios.isNotEmpty())

        val otroEstado = viewModel.estado.value.estados.first { it.id != viewModel.estado.value.estadoId }
        viewModel.alElegirEstado(otroEstado.id)

        assertNull(viewModel.estado.value.municipioId)
        assertTrue(viewModel.estado.value.municipios.isEmpty())
    }

    @Test
    fun noDebeLlamarAlRepositorio_cuandoFaltanCampos_yMarcaTodosLosErrores() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.alPublicar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(R.string.validacion_solicitud_titulo_vacio, estado.errorTitulo)
        assertEquals(R.string.validacion_solicitud_categoria_sin_elegir, estado.errorCategoria)
        assertEquals(R.string.validacion_solicitud_descripcion_vacia, estado.errorDescripcion)
        assertEquals(R.string.validacion_solicitud_estado_sin_elegir, estado.errorEstado)
        assertNull(estado.errorPresupuesto)
        assertFalse(estado.publicada)
        assertTrue(fuente.solicitudes.none { it.titulo == "Reparar una fuga" })
    }

    @Test
    fun debePublicarUnaSolicitudAbierta_conElPresupuestoVacioComoNulo() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = cargadoConUbicacion()

        viewModel.alPublicar()
        advanceUntilIdle()

        assertTrue(viewModel.estado.value.publicada)
        assertFalse(viewModel.estado.value.publicando)
        val creada = fuente.solicitudes.last { it.titulo == "Reparar una fuga" }
        assertEquals(EstadoSolicitud.ABIERTA, creada.estatus)
        assertEquals(ID_CLIENTE, creada.clienteId)
        assertNull(creada.presupuesto)
    }

    @Test
    fun debeGuardarElPresupuestoConDosDecimales_yRecortarElTexto() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = cargadoConUbicacion()
        viewModel.alCambiarTitulo("  Pintar una recámara  ")
        viewModel.alCambiarPresupuesto("1500")

        viewModel.alPublicar()
        advanceUntilIdle()

        val creada = fuente.solicitudes.last { it.titulo == "Pintar una recámara" }
        assertEquals(BigDecimal("1500.00"), creada.presupuesto)
    }

    @Test
    fun noDebePublicar_cuandoElPresupuestoNoEsValido() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = cargadoConUbicacion()
        // El filtro de escritura ya recorta a dos decimales; lo que sobra es una cantidad que no cabe.
        viewModel.alCambiarPresupuesto("100000000")

        viewModel.alPublicar()
        advanceUntilIdle()

        assertEquals(R.string.validacion_solicitud_presupuesto_grande, viewModel.estado.value.errorPresupuesto)
        assertFalse(viewModel.estado.value.publicada)
    }

    @Test
    fun debeMostrarElErrorEnLaPantalla_sinPerderLoEscrito_cuandoFallaPublicar() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = cargadoConUbicacion()
        repositorioSolicitudes.errorForzado = TipoError.RED

        viewModel.alPublicar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(TipoError.RED, estado.errorPantalla)
        assertFalse(estado.publicada)
        assertFalse(estado.publicando)
        assertEquals("Reparar una fuga", estado.titulo)
    }

    @Test
    fun noDebePublicarDosVeces_mientrasSeEstaPublicando() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = cargadoConUbicacion()
        val antes = fuente.solicitudes.size

        viewModel.alPublicar()
        viewModel.alPublicar()
        advanceUntilIdle()

        assertEquals(antes + 1, fuente.solicitudes.size)
    }

    @Test
    fun debeValidarAlPerderElFoco_soloLosCamposTocados() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.alPerderFoco(CampoSolicitud.TITULO)
        assertNull(viewModel.estado.value.errorTitulo)

        viewModel.alCambiarTitulo("")
        viewModel.alPerderFoco(CampoSolicitud.TITULO)
        assertEquals(R.string.validacion_solicitud_titulo_vacio, viewModel.estado.value.errorTitulo)
    }

    @Test
    fun debeLimpiarElErrorDeUnCampo_alEscribirEnEl() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        viewModel.alPublicar()
        assertNotNull(viewModel.estado.value.errorTitulo)

        viewModel.alCambiarTitulo("a")

        assertNull(viewModel.estado.value.errorTitulo)
    }

    @Test
    fun debeFiltrarLoQueNoEsPrecio_alEscribirElPresupuesto() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.alCambiarPresupuesto("12a.345")

        assertEquals("12.34", viewModel.estado.value.presupuesto)
    }

    private companion object {
        const val ID_CLIENTE = "usr-cli-1"
    }
}
