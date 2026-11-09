package mx.donchambitas.app.ui.pantallas

import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.R
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioCatalogosFalso
import mx.donchambitas.app.datos.falso.RepositorioSolicitudesFalso
import mx.donchambitas.app.dominio.modelo.EstadoSolicitud
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.dominio.modelo.Solicitud
import mx.donchambitas.app.util.ReglaCorrutinas
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** P-09 contra los falsos, sin retraso (S4-T06, DISENO-SOLICITUDES-BUSQUEDA.md 5). */
@OptIn(ExperimentalCoroutinesApi::class)
class MisSolicitudesViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val fuente = FuenteDatosFalsa().apply {
        fijarSesionActiva(Sesion(usuarios.first { it.id == ID_CLIENTE }))
    }
    private val repositorioSolicitudes = RepositorioSolicitudesFalso(fuente).apply { retrasoMs = 0 }
    private val repositorioCatalogos = RepositorioCatalogosFalso(fuente).apply { retrasoMs = 0 }

    private fun crearViewModel() = MisSolicitudesViewModel(repositorioSolicitudes, repositorioCatalogos)

    private fun solicitud(
        id: String,
        estatus: EstadoSolicitud = EstadoSolicitud.ABIERTA,
        creadoEn: String = "2026-10-01T00:00:00Z",
        clienteId: String = ID_CLIENTE,
        presupuesto: BigDecimal? = null
    ) = Solicitud(
        id = id,
        clienteId = clienteId,
        categoriaId = 1,
        titulo = "Solicitud $id",
        descripcion = "Descripcion de $id",
        presupuesto = presupuesto,
        estatus = estatus,
        creadoEn = Instant.parse(creadoEn),
        actualizadoEn = Instant.parse(creadoEn)
    )

    /** Deja al cliente solo con las solicitudes dadas, para controlar el orden. */
    private fun soloConEstas(vararg solicitudes: Solicitud) {
        fuente.solicitudes.removeAll { it.clienteId == ID_CLIENTE }
        fuente.solicitudes.addAll(solicitudes)
    }

    @Test
    fun debeArrancarCargando_yListarLasSolicitudesDelCliente() = runTest(reglaCorrutinas.testDispatcher) {
        soloConEstas(solicitud("a"), solicitud("b"))
        val viewModel = crearViewModel()
        assertTrue(viewModel.estado.value.cargando)

        viewModel.alCargar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.cargando)
        assertNull(estado.errorCarga)
        assertEquals(setOf("a", "b"), estado.solicitudes.map { it.id }.toSet())
        assertEquals(16, estado.categorias.size)
    }

    @Test
    fun noDebeListarLasSolicitudesDeOtroCliente() = runTest(reglaCorrutinas.testDispatcher) {
        soloConEstas(solicitud("mia"), solicitud("ajena", clienteId = "otro-cliente"))
        val viewModel = crearViewModel()

        viewModel.alCargar()
        advanceUntilIdle()

        assertEquals(listOf("mia"), viewModel.estado.value.solicitudes.map { it.id })
    }

    @Test
    fun debeMostrarElVacio_cuandoElClienteNoHaPublicado() = runTest(reglaCorrutinas.testDispatcher) {
        soloConEstas()
        val viewModel = crearViewModel()

        assertFalse(viewModel.estado.value.vacio)
        viewModel.alCargar()
        advanceUntilIdle()

        assertTrue(viewModel.estado.value.vacio)
    }

    @Test
    fun debeMostrarElError_yReintentar() = runTest(reglaCorrutinas.testDispatcher) {
        soloConEstas(solicitud("a"))
        repositorioSolicitudes.errorForzado = TipoError.RED
        val viewModel = crearViewModel()
        viewModel.alCargar()
        advanceUntilIdle()

        val conError = viewModel.estado.value
        assertEquals(TipoError.RED, conError.errorCarga)
        assertFalse(conError.vacio)

        repositorioSolicitudes.errorForzado = null
        viewModel.alReintentarCarga()
        advanceUntilIdle()

        assertNull(viewModel.estado.value.errorCarga)
        assertEquals(listOf("a"), viewModel.estado.value.solicitudes.map { it.id })
    }

    @Test
    fun debeMostrarElError_cuandoFallanLasCategorias() = runTest(reglaCorrutinas.testDispatcher) {
        soloConEstas(solicitud("a"))
        repositorioCatalogos.errorForzado = TipoError.RED
        val viewModel = crearViewModel()

        viewModel.alCargar()
        advanceUntilIdle()

        assertEquals(TipoError.RED, viewModel.estado.value.errorCarga)
    }

    @Test
    fun alRecargar_apareceLoNuevo_sinPasarPorElCargando() = runTest(reglaCorrutinas.testDispatcher) {
        soloConEstas(solicitud("a"))
        val viewModel = crearViewModel()
        viewModel.alCargar()
        advanceUntilIdle()

        fuente.solicitudes.add(solicitud("nueva", creadoEn = "2026-11-01T00:00:00Z"))
        viewModel.alCargar()
        assertFalse(viewModel.estado.value.cargando)
        advanceUntilIdle()

        assertEquals(listOf("nueva", "a"), viewModel.estado.value.solicitudes.map { it.id })
    }

    @Test
    fun siFallaUnaRecarga_laListaSeQueda_ySoloAvisa() = runTest(reglaCorrutinas.testDispatcher) {
        soloConEstas(solicitud("a"))
        val viewModel = crearViewModel()
        viewModel.alCargar()
        advanceUntilIdle()

        repositorioSolicitudes.errorForzado = TipoError.RED
        viewModel.alCargar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(listOf("a"), estado.solicitudes.map { it.id })
        assertNull(estado.errorCarga)
        assertEquals(R.string.error_red, estado.aviso)
    }

    @Test
    fun debeAvisarSolicitudPublicada_soloSiSeVuelveDeP08() = runTest(reglaCorrutinas.testDispatcher) {
        soloConEstas(solicitud("a"))
        val viewModel = crearViewModel()

        viewModel.alCargar()
        advanceUntilIdle()
        assertNull(viewModel.estado.value.aviso)

        viewModel.alCargar(solicitudPublicada = true)
        advanceUntilIdle()
        assertEquals(R.string.solicitud_publicada_aviso, viewModel.estado.value.aviso)
    }

    @Test
    fun alConsumirElAviso_sePoneEnNulo() = runTest(reglaCorrutinas.testDispatcher) {
        soloConEstas(solicitud("a"))
        val viewModel = crearViewModel()
        viewModel.alCargar(solicitudPublicada = true)
        advanceUntilIdle()

        viewModel.alConsumirAviso()

        assertNull(viewModel.estado.value.aviso)
    }

    @Test
    fun debeOrdenar_activasPrimero_yLaMasRecienteArriba() {
        val ordenadas = ordenarMisSolicitudes(
            listOf(
                solicitud("cerrada-nueva", EstadoSolicitud.CERRADA, "2026-11-05T00:00:00Z"),
                solicitud("abierta-vieja", EstadoSolicitud.ABIERTA, "2026-09-01T00:00:00Z"),
                solicitud("cancelada-vieja", EstadoSolicitud.CANCELADA, "2026-08-01T00:00:00Z"),
                solicitud("asignada-media", EstadoSolicitud.ASIGNADA, "2026-10-01T00:00:00Z"),
                solicitud("abierta-nueva", EstadoSolicitud.ABIERTA, "2026-11-01T00:00:00Z")
            )
        )

        assertEquals(
            listOf("abierta-nueva", "asignada-media", "abierta-vieja", "cerrada-nueva", "cancelada-vieja"),
            ordenadas.map { it.id }
        )
    }

    @Test
    fun laFechaSeEscribeComoDiaMesAnio_enLaZonaDada() {
        val instante = Instant.parse("2026-11-07T01:30:00Z")

        assertEquals("07/11/2026", textoFechaSolicitud(instante, ZoneId.of("UTC")))
        // En Mexico todavia es el dia anterior.
        assertEquals("06/11/2026", textoFechaSolicitud(instante, ZoneId.of("America/Mexico_City")))
    }

    private companion object {
        const val ID_CLIENTE = "usr-cli-1"
    }
}
