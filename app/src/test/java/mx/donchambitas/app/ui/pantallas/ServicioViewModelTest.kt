package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.math.BigDecimal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.R
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioCatalogosFalso
import mx.donchambitas.app.datos.falso.RepositorioIaFalso
import mx.donchambitas.app.datos.falso.RepositorioServiciosFalso
import mx.donchambitas.app.dominio.modelo.FuncionIa
import mx.donchambitas.app.dominio.modelo.ResultadoIa
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.dominio.repositorio.RepositorioIa
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

@OptIn(ExperimentalCoroutinesApi::class)
class ServicioViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val fuente = FuenteDatosFalsa().apply {
        fijarSesionActiva(Sesion(usuarios.first { it.id == ID_TRABAJADOR }))
    }
    private val repositorioServicios = RepositorioServiciosFalso(fuente).apply { retrasoMs = 0 }
    private val repositorioCatalogos = RepositorioCatalogosFalso(fuente).apply { retrasoMs = 0 }

    private val repositorioIa = RepositorioIaFalso().apply { retrasoMs = 0 }

    private fun crearViewModel(
        servicioId: String? = null,
        estadoGuardado: SavedStateHandle = handle(servicioId),
        ia: RepositorioIa = repositorioIa
    ) = ServicioViewModel(repositorioServicios, repositorioCatalogos, ia, estadoGuardado)

    private fun handle(servicioId: String?) =
        SavedStateHandle(mapOf(Ruta.CrearEditarServicio.ARG_SERVICIO_ID to servicioId))

    @Test
    fun debeArrancarEnBlancoConLasCategorias_alCrear() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.cargando)
        assertTrue(estado.creando)
        assertEquals(16, estado.categorias.size)
        assertEquals("", estado.titulo)
        assertEquals(EstadoBotonIa.FaltanDatos, estado.ia)
    }

    @Test
    fun debeHabilitarLaIa_conTituloYCategoria() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.alCambiarTitulo("Instalación de boiler")
        assertEquals(EstadoBotonIa.FaltanDatos, viewModel.estado.value.ia)
        viewModel.alElegirCategoria(1)
        assertEquals(EstadoBotonIa.Disponible(null), viewModel.estado.value.ia)
    }

    @Test
    fun debeLlenarElFormulario_alEditar() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.creando)
        assertFalse(estado.noExiste)
        assertEquals(1, estado.categoriaId)
        assertEquals("350", estado.precioDesde)
        assertEquals("800", estado.precioHasta)
        assertEquals("por trabajo", estado.unidadPrecio)
        assertEquals(EstadoBotonIa.Disponible(null), estado.ia)
        // H-18: la foto sembrada llega con su id, lista para quitarse.
        val sembrada = fuente.fotosServicio.single { it.servicioId == "serv-1" }
        assertEquals(listOf(FotoEnPantalla.Guardada(sembrada)), estado.fotos)
    }

    @Test
    fun debePintarNoExiste_cuandoElServicioYaNoEsta() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel("serv-borrado")
        advanceUntilIdle()

        assertTrue(viewModel.estado.value.noExiste)
        assertNull(viewModel.estado.value.errorCarga)
    }

    @Test
    fun debeOfrecerReintentar_cuandoLaCargaFalla() = runTest(reglaCorrutinas.testDispatcher) {
        repositorioCatalogos.errorForzado = TipoError.RED
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()
        assertEquals(TipoError.RED, viewModel.estado.value.errorCarga)

        repositorioCatalogos.errorForzado = null
        viewModel.alReintentarCarga()
        advanceUntilIdle()

        assertNull(viewModel.estado.value.errorCarga)
        assertEquals(1, viewModel.estado.value.categoriaId)
    }

    @Test
    fun debePasarAEditarSinNavegar_alPublicar() = runTest(reglaCorrutinas.testDispatcher) {
        val estadoGuardado = handle(null)
        val viewModel = crearViewModel(estadoGuardado = estadoGuardado)
        advanceUntilIdle()

        viewModel.alCambiarTitulo("  Instalación de boiler ")
        viewModel.alElegirCategoria(1)
        viewModel.alCambiarDescripcion("Instalo boilers de paso y de depósito.")
        viewModel.alCambiarPrecioDesde("350,5")
        viewModel.alCambiarUnidad("por trabajo")
        viewModel.alGuardar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        val creado = fuente.servicios.first { it.id == estado.servicioId }
        assertFalse(estado.creando)
        assertFalse(estado.cambiosGuardados)
        assertEquals(R.string.servicio_publicado, estado.aviso)
        assertEquals(creado.id, estadoGuardado.get<String>(Ruta.CrearEditarServicio.ARG_SERVICIO_ID))
        assertEquals("Instalación de boiler", creado.titulo)
        assertEquals(BigDecimal("350.50"), creado.precioDesde)
        assertNull(creado.precioHasta)
        assertEquals(ID_TRABAJADOR, creado.perfilId)
    }

    @Test
    fun debeMarcarTodosYNoGuardar_cuandoHayErrores() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        val antes = fuente.servicios.size

        viewModel.alCambiarTitulo("Instalación de boiler")
        viewModel.alGuardar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.guardando)
        assertEquals(antes, fuente.servicios.size)
        assertNull(estado.errorTitulo)
        assertEquals(R.string.validacion_categoria_sin_elegir, estado.errorCategoria)
        assertEquals(R.string.validacion_servicio_descripcion_vacia, estado.errorDescripcion)
        assertEquals(CampoServicio.CATEGORIA, CampoServicio.entries.first { estado.errorDe(it) != null })
    }

    @Test
    fun noDebeMandarUnidad_sinPrecio() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()

        viewModel.alCambiarPrecioDesde("")
        viewModel.alCambiarPrecioHasta("")
        viewModel.alGuardar()
        advanceUntilIdle()

        val guardado = fuente.servicios.first { it.id == "serv-1" }
        assertNull(guardado.precioDesde)
        assertNull(guardado.unidadPrecio)
    }

    @Test
    fun debeAvisarQueRegresa_alGuardarCambios() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()

        viewModel.alCambiarTitulo("Fugas y grifería")
        viewModel.alGuardar()
        assertTrue(viewModel.estado.value.guardando)
        advanceUntilIdle()

        assertTrue(viewModel.estado.value.cambiosGuardados)
        val guardado = fuente.servicios.first { it.id == "serv-1" }
        assertEquals("Fugas y grifería", guardado.titulo)
        assertTrue(guardado.activo)
    }

    @Test
    fun debeConservarLoEscrito_cuandoGuardarFalla() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()

        viewModel.alCambiarTitulo("Fugas y grifería")
        repositorioServicios.errorForzado = TipoError.SERVIDOR
        viewModel.alGuardar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.guardando)
        assertFalse(estado.cambiosGuardados)
        assertEquals(TipoError.SERVIDOR, estado.errorPantalla)
        assertEquals("Fugas y grifería", estado.titulo)
    }

    @Test
    fun debeSubirEnLaPrimeraPosicionLibre() = runTest(reglaCorrutinas.testDispatcher) {
        fuente.fotosServicio.removeAll { it.servicioId == "serv-1" }
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()

        viewModel.alAgregarFoto(byteArrayOf(1))
        assertEquals(listOf(FotoEnPantalla.Subiendo(1)), viewModel.estado.value.fotos)
        assertTrue(viewModel.estado.value.subiendoFoto)
        advanceUntilIdle()
        viewModel.alAgregarFoto(byteArrayOf(2))
        advanceUntilIdle()

        val fotos = viewModel.estado.value.fotos
        assertTrue(fotos.all { it is FotoEnPantalla.Guardada })
        assertEquals(listOf(1, 2), fotos.map { it.posicion })
        assertEquals(2, fuente.fotosServicio.count { it.servicioId == "serv-1" })
    }

    @Test
    fun noDebeGuardar_mientrasSubeUnaFoto() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()
        repositorioServicios.retrasoMs = 1_000

        viewModel.alAgregarFoto(byteArrayOf(1))
        viewModel.alGuardar()

        assertFalse(viewModel.estado.value.guardando)
    }

    @Test
    fun debeQuitarLaMiniatura_alCancelarLaSubida() = runTest(reglaCorrutinas.testDispatcher) {
        fuente.fotosServicio.removeAll { it.servicioId == "serv-1" }
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()
        repositorioServicios.retrasoMs = 1_000
        viewModel.alCambiarTitulo("Sigue escrito")

        viewModel.alAgregarFoto(byteArrayOf(1))
        viewModel.alCancelarSubida(1)
        advanceUntilIdle()

        assertTrue(viewModel.estado.value.fotos.isEmpty())
        assertEquals("Sigue escrito", viewModel.estado.value.titulo)
        assertEquals(0, fuente.fotosServicio.count { it.servicioId == "serv-1" })
    }

    @Test
    fun debeAvisar_cuandoLaFotoNoSube() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()

        repositorioServicios.errorForzado = TipoError.RED
        viewModel.alAgregarFoto(byteArrayOf(1))
        advanceUntilIdle()

        // Se va la miniatura que subia; la sembrada se queda.
        assertEquals(listOf(1), viewModel.estado.value.fotos.map { it.posicion })
        assertFalse(viewModel.estado.value.subiendoFoto)
        assertEquals(R.string.servicio_foto_no_subio, viewModel.estado.value.aviso)
    }

    @Test
    fun debeQuitarLaFoto_soloTrasConfirmar() = runTest(reglaCorrutinas.testDispatcher) {
        fuente.fotosServicio.removeAll { it.servicioId == "serv-1" }
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()
        viewModel.alAgregarFoto(byteArrayOf(1))
        advanceUntilIdle()
        val foto = viewModel.estado.value.fotos.single()

        viewModel.alPedirQuitarFoto(foto)
        assertEquals(foto, viewModel.estado.value.fotoPorQuitar)
        viewModel.alConfirmarQuitarFoto()
        advanceUntilIdle()

        assertNull(viewModel.estado.value.fotoPorQuitar)
        assertTrue(viewModel.estado.value.fotos.isEmpty())
        assertEquals(0, fuente.fotosServicio.count { it.servicioId == "serv-1" })
    }

    @Test
    fun debeDejarLaFoto_cuandoQuitarFalla() = runTest(reglaCorrutinas.testDispatcher) {
        fuente.fotosServicio.removeAll { it.servicioId == "serv-1" }
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()
        viewModel.alAgregarFoto(byteArrayOf(1))
        advanceUntilIdle()
        val foto = viewModel.estado.value.fotos.single()

        repositorioServicios.errorForzado = TipoError.RED
        viewModel.alPedirQuitarFoto(foto)
        viewModel.alConfirmarQuitarFoto()
        advanceUntilIdle()

        assertEquals(listOf(foto), viewModel.estado.value.fotos)
        assertEquals(obtenerMensajeErrorRes(TipoError.RED), viewModel.estado.value.aviso)
    }

    @Test
    fun debeMarcarElRangoEnHasta() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()

        viewModel.alCambiarPrecioDesde("800")
        viewModel.alCambiarPrecioHasta("300")
        viewModel.alGuardar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertNull(estado.errorPrecioDesde)
        assertEquals(R.string.validacion_precio_rango, estado.errorPrecioHasta)
        assertFalse(estado.cambiosGuardados)
        assertEquals(BigDecimal("350.00"), fuente.servicios.first { it.id == "serv-1" }.precioDesde)
    }

    @Test
    fun debeValidarAlPerderElFoco_soloLoTocado() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.alPerderFoco(CampoServicio.TITULO)
        assertNull(viewModel.estado.value.errorTitulo)

        viewModel.alCambiarTitulo("  ")
        viewModel.alPerderFoco(CampoServicio.TITULO)
        assertEquals(R.string.validacion_servicio_titulo_vacio, viewModel.estado.value.errorTitulo)

        viewModel.alCambiarTitulo("Boiler")
        assertNull(viewModel.estado.value.errorTitulo)
    }

    @Test
    fun debeLimpiarElErrorDeCategoria_alElegirUna() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        viewModel.alGuardar()
        assertEquals(R.string.validacion_categoria_sin_elegir, viewModel.estado.value.errorCategoria)

        viewModel.alElegirCategoria(1)

        assertNull(viewModel.estado.value.errorCategoria)
    }

    // Redactar con IA (S3-T13, DISENO-PERFIL-SERVICIOS.md 1.9)

    @Test
    fun debeLlenarLaDescripcion_cuandoPidesIaConLaDescripcionVacia() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        viewModel.alCambiarTitulo("Instalacion de boiler")
        viewModel.alElegirCategoria(1)

        viewModel.alPedirIa()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertTrue(estado.descripcion.startsWith("Me encargo"))
        assertNull(estado.propuestaIa)
        assertEquals(EstadoBotonIa.Disponible(restantesHoy = 7), estado.ia)
    }

    @Test
    fun debeProponerYLuegoReemplazarOConservar_cuandoYaHabiaTexto() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        viewModel.alCambiarTitulo("Instalacion de boiler")
        viewModel.alElegirCategoria(1)
        viewModel.alCambiarDescripcion("Mi texto")
        viewModel.alPedirIa()
        advanceUntilIdle()
        val propuesta = viewModel.estado.value.propuestaIa
        assertEquals("Mi texto", viewModel.estado.value.descripcion)
        assertTrue(propuesta.orEmpty().startsWith("Me encargo"))

        viewModel.alDescartarPropuestaIa()
        assertEquals("Mi texto", viewModel.estado.value.descripcion)
        assertNull(viewModel.estado.value.propuestaIa)

        viewModel.alPedirIa()
        advanceUntilIdle()
        viewModel.alAceptarPropuestaIa()
        assertEquals(propuesta, viewModel.estado.value.descripcion)
        assertNull(viewModel.estado.value.propuestaIa)
    }

    @Test
    fun debeDeshabilitarElBoton_cuandoLlegaElLimiteDiario() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        viewModel.alCambiarTitulo("Instalacion de boiler")
        viewModel.alElegirCategoria(1)
        viewModel.alCambiarDescripcion("Mi texto")
        repositorioIa.errorForzado = TipoError.LIMITE_IA

        viewModel.alPedirIa()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(EstadoBotonIa.LimiteAlcanzado, estado.ia)
        assertNull(estado.aviso)
        assertEquals("Mi texto", estado.descripcion)
    }

    @Test
    fun debeAvisarSinTocarElCampo_cuandoFallaLaIa() = runTest(reglaCorrutinas.testDispatcher) {
        val esperados = mapOf(
            TipoError.RED to R.string.ia_aviso_fallo,
            TipoError.SERVIDOR to R.string.ia_aviso_fallo,
            TipoError.DESCONOCIDO to R.string.ia_aviso_fallo,
            TipoError.VALIDACION to R.string.ia_aviso_no_pudo
        )
        for ((tipo, aviso) in esperados) {
            val viewModel = crearViewModel()
            advanceUntilIdle()
            viewModel.alCambiarTitulo("Instalacion de boiler")
            viewModel.alElegirCategoria(1)
            viewModel.alCambiarDescripcion("Mi texto")
            repositorioIa.errorForzado = tipo

            viewModel.alPedirIa()
            advanceUntilIdle()

            val estado = viewModel.estado.value
            assertEquals(aviso, estado.aviso)
            assertTrue(estado.ia is EstadoBotonIa.Disponible)
            assertEquals("Mi texto", estado.descripcion)
            assertNull(estado.propuestaIa)
        }
    }

    @Test
    fun noDebePegarNoPuedoRedactar_cuandoLlegaComoTexto() = runTest(reglaCorrutinas.testDispatcher) {
        val ia = mockk<RepositorioIa> {
            coEvery { generar(any(), any()) } returns Resultado.Exito(ResultadoIa("NO_PUEDO_REDACTAR"))
        }
        val viewModel = crearViewModel(ia = ia)
        advanceUntilIdle()
        viewModel.alCambiarTitulo("Instalacion de boiler")
        viewModel.alElegirCategoria(1)

        viewModel.alPedirIa()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals("", estado.descripcion)
        assertNull(estado.propuestaIa)
        assertEquals(R.string.ia_aviso_no_pudo, estado.aviso)
    }

    @Test
    fun noDebePedir_sinTituloOSinCategoria() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        viewModel.alCambiarTitulo("Instalacion de boiler")

        viewModel.alPedirIa()
        advanceUntilIdle()

        assertEquals(EstadoBotonIa.FaltanDatos, viewModel.estado.value.ia)
        assertEquals(8, repositorioIa.llamadasRestantes)
    }

    @Test
    fun debeMandarLaEntradaConElFormatoDeLaSeccion2_conElBorradorSiLoHay() = runTest(reglaCorrutinas.testDispatcher) {
        val ia = mockk<RepositorioIa> {
            coEvery { generar(any(), any()) } returns Resultado.Exito(ResultadoIa("Texto"))
        }
        val viewModel = crearViewModel(ia = ia)
        advanceUntilIdle()
        viewModel.alCambiarTitulo("  Reparacion   de fugas ")
        viewModel.alElegirCategoria(1)

        viewModel.alPedirIa()
        viewModel.alPedirIa()
        advanceUntilIdle()
        viewModel.alCambiarDescripcion("arreglo fugas en bano")
        viewModel.alPedirIa()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            ia.generar(FuncionIa.REDACTAR_SERVICIO, "Servicio: Reparacion de fugas\nCategoría: Plomería")
        }
        coVerify(exactly = 1) {
            ia.generar(
                FuncionIa.REDACTAR_SERVICIO,
                "Servicio: Reparacion de fugas\nCategoría: Plomería\nBorrador: arreglo fugas en bano"
            )
        }
    }

    @Test
    fun debeSeguirEditableLaDescripcion_mientrasPideLaIa() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        viewModel.alCambiarTitulo("Instalacion de boiler")
        viewModel.alElegirCategoria(1)
        repositorioIa.retrasoMs = 100

        viewModel.alPedirIa()
        runCurrent()
        assertEquals(EstadoBotonIa.Pidiendo, viewModel.estado.value.ia)
        viewModel.alCambiarDescripcion("Lo escribi yo")
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals("Lo escribi yo", estado.descripcion)
        assertTrue(estado.propuestaIa.orEmpty().startsWith("Me encargo"))
    }

    private companion object {
        const val ID_TRABAJADOR = "usr-trab-1"
    }
}
