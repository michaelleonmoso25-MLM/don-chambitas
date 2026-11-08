package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.SavedStateHandle
import java.math.BigDecimal
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.R
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioAuthFalso
import mx.donchambitas.app.datos.falso.RepositorioCatalogosFalso
import mx.donchambitas.app.datos.falso.RepositorioSolicitudesFalso
import mx.donchambitas.app.datos.falso.RepositorioTrabajadorFalso
import mx.donchambitas.app.dominio.modelo.EstadoSolicitud
import mx.donchambitas.app.dominio.modelo.OrdenBusqueda
import mx.donchambitas.app.dominio.modelo.PerfilTrabajador
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.dominio.modelo.Solicitud
import mx.donchambitas.app.dominio.modelo.Usuario
import mx.donchambitas.app.ui.componentes.obtenerMensajeErrorRes
import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.util.ReglaCorrutinas
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * El camino del cliente entre P-06, P-07, P-08 y P-09 con los repositorios falsos sobre una sola
 * fuente, y las filas de error de DISENO-SOLICITUDES-BUSQUEDA.md 3.8, 4 y 5.4 que las pruebas de
 * cada pantalla no cubren (S4-T15).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FlujoClienteTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val fuente = FuenteDatosFalsa().apply {
        fijarSesionActiva(Sesion(usuarios.first { it.id == ID_CLIENTE }))
    }
    private val repositorioSolicitudes = RepositorioSolicitudesFalso(fuente).apply { retrasoMs = 0 }
    private val repositorioCatalogos = RepositorioCatalogosFalso(fuente).apply { retrasoMs = 0 }
    private val repositorioTrabajador = RepositorioTrabajadorFalso(fuente).apply { retrasoMs = 0 }
    private val repositorioAuth = RepositorioAuthFalso(fuente)

    private fun publicar() = SolicitudNuevaViewModel(repositorioSolicitudes, repositorioCatalogos)

    private fun misSolicitudes() = MisSolicitudesViewModel(repositorioSolicitudes, repositorioCatalogos)

    private fun resultados() = ResultadosViewModel(repositorioCatalogos, SavedStateHandle())

    private fun perfilPublico(trabajadorId: String) = PerfilPublicoViewModel(
        repositorioTrabajador,
        repositorioAuth,
        SavedStateHandle(mapOf(Ruta.PerfilTrabajador.ARG_TRABAJADOR_ID to trabajadorId))
    )

    /** Deja al cliente sin solicitudes, para no depender de las de prueba ni del reloj. */
    private fun sinSolicitudes() {
        fuente.solicitudes.removeAll { it.clienteId == ID_CLIENTE }
    }

    /** Llena P-08 con datos validos; el presupuesto vacio es valido (HU-12). */
    private fun TestScope.formularioLleno(titulo: String = "Reparar una fuga", presupuesto: String = ""): SolicitudNuevaViewModel {
        val viewModel = publicar()
        advanceUntilIdle()
        viewModel.alCambiarTitulo(titulo)
        viewModel.alElegirCategoria(1)
        viewModel.alCambiarDescripcion("Hay una fuga bajo el lavabo del baño")
        viewModel.alElegirEstado(fuente.municipios.first().estadoId)
        advanceUntilIdle()
        viewModel.alElegirMunicipio(viewModel.estado.value.municipios.first().id)
        if (presupuesto.isNotEmpty()) viewModel.alCambiarPresupuesto(presupuesto)
        return viewModel
    }

    private fun solicitudAjena(id: String) = Solicitud(
        id = id,
        clienteId = "otro-cliente",
        categoriaId = 1,
        titulo = "De otro cliente",
        descripcion = "No es mia",
        creadoEn = Instant.parse("2026-10-01T00:00:00Z"),
        actualizadoEn = Instant.parse("2026-10-01T00:00:00Z")
    )

    /** Suma trabajadores al servidor falso: con los de prueba no hay una segunda pagina. */
    private fun agregarTrabajadores(cuantos: Int) {
        val base = Instant.parse("2026-01-01T00:00:00Z")
        repeat(cuantos) { i ->
            val id = "usr-extra-$i"
            val creado = base.plusSeconds(i * 3_600L)
            fuente.usuarios.add(
                Usuario(
                    id = id,
                    correo = "extra$i@ejemplo.com",
                    nombre = "Extra$i",
                    apellidos = "Prueba",
                    rol = RolUsuario.TRABAJADOR,
                    creadoEn = creado,
                    actualizadoEn = creado
                )
            )
            fuente.perfilesTrabajador.add(
                PerfilTrabajador(usuarioId = id, titulo = "Oficio $i", creadoEn = creado, actualizadoEn = creado)
            )
        }
    }

    // ---- Recorridos completos ----

    @Test
    fun loPublicadoEnP08_loLeeP09_conSuUbicacion_yElAviso() = runTest(reglaCorrutinas.testDispatcher) {
        val formulario = formularioLleno(titulo = "Reparar una fuga", presupuesto = "350")
        val estadoElegido = formulario.estado.value.estadoId
        val municipioElegido = formulario.estado.value.municipioId

        formulario.alPublicar()
        advanceUntilIdle()
        assertTrue(formulario.estado.value.publicada)

        val lista = misSolicitudes()
        lista.alCargar(solicitudPublicada = true)
        advanceUntilIdle()

        val creada = lista.estado.value.solicitudes.first { it.titulo == "Reparar una fuga" }
        assertEquals(EstadoSolicitud.ABIERTA, creada.estatus)
        assertEquals(ID_CLIENTE, creada.clienteId)
        assertEquals(estadoElegido, creada.estadoId)
        assertEquals(municipioElegido, creada.municipioId)
        assertEquals(BigDecimal("350.00"), creada.presupuesto)
        assertEquals(R.string.solicitud_publicada_aviso, lista.estado.value.aviso)
    }

    @Test
    fun conElPresupuestoVacio_P09LaTraeSinPresupuesto() = runTest(reglaCorrutinas.testDispatcher) {
        val formulario = formularioLleno(titulo = "Pintar una recámara", presupuesto = "")
        formulario.alPublicar()
        advanceUntilIdle()

        val lista = misSolicitudes()
        lista.alCargar()
        advanceUntilIdle()

        assertNull(lista.estado.value.solicitudes.first { it.titulo == "Pintar una recámara" }.presupuesto)
    }

    @Test
    fun unClienteSinSolicitudes_veP09Vacio_yAlPublicarYaNo() = runTest(reglaCorrutinas.testDispatcher) {
        sinSolicitudes()
        val lista = misSolicitudes()
        lista.alCargar()
        advanceUntilIdle()
        assertTrue(lista.estado.value.vacio)

        val formulario = formularioLleno()
        formulario.alPublicar()
        advanceUntilIdle()
        lista.alCargar()
        advanceUntilIdle()

        assertFalse(lista.estado.value.vacio)
        assertEquals(1, lista.estado.value.solicitudes.size)
    }

    @Test
    fun laSolicitudDeOtroCliente_noSaleEnP09() = runTest(reglaCorrutinas.testDispatcher) {
        sinSolicitudes()
        fuente.solicitudes.add(solicitudAjena("sol-ajena"))
        val lista = misSolicitudes()

        lista.alCargar()
        advanceUntilIdle()

        assertTrue(lista.estado.value.vacio)
    }

    @Test
    fun P06_combinaTextoFiltrosOrdenYPaginas_yQuitarLosFiltrosDevuelveLaLista() = runTest(reglaCorrutinas.testDispatcher) {
        agregarTrabajadores(45)
        val total = fuente.perfilesTrabajador.size
        val busqueda = resultados()
        advanceUntilIdle()
        assertEquals(TAMANO_PAGINA, busqueda.estado.value.trabajadores.size)

        // Orden y paginas: cambiar el orden vuelve a la primera pagina.
        busqueda.alLlegarAlFinal()
        advanceUntilIdle()
        assertEquals(2 * TAMANO_PAGINA, busqueda.estado.value.trabajadores.size)
        busqueda.alElegirOrden(OrdenBusqueda.CREADO_EN)
        advanceUntilIdle()
        val fechas = busqueda.estado.value.trabajadores.map { it.creadoEn }
        assertEquals(TAMANO_PAGINA, fechas.size)
        assertEquals(fechas.sortedDescending(), fechas)

        // Texto: solo los extra, que son 45, en tres paginas de 20, 20 y 5.
        busqueda.alCambiarTexto("extra")
        busqueda.alBuscar()
        advanceUntilIdle()
        repeat(2) {
            busqueda.alLlegarAlFinal()
            advanceUntilIdle()
        }
        assertEquals(45, busqueda.estado.value.trabajadores.size)
        assertFalse(busqueda.estado.value.hayMas)

        // Un filtro de estado deja a quien no tiene ubicacion fuera: ninguno de los extra la tiene.
        val estadoDePrueba = fuente.perfilesTrabajador.first { it.estadoId != null }.estadoId
        busqueda.alAplicarFiltros(busqueda.estado.value.filtros.copy(estadoId = estadoDePrueba))
        advanceUntilIdle()
        assertTrue(busqueda.estado.value.vacio)
        assertTrue(busqueda.estado.value.hayFiltros)

        // Quitar los filtros conserva el texto y el orden; borrar el texto devuelve toda la lista.
        busqueda.alQuitarTodosLosFiltros()
        advanceUntilIdle()
        assertEquals("extra", busqueda.estado.value.texto)
        assertEquals(OrdenBusqueda.CREADO_EN, busqueda.estado.value.filtros.orden)
        busqueda.alBorrarTexto()
        advanceUntilIdle()
        assertEquals(TAMANO_PAGINA, busqueda.estado.value.trabajadores.size)
        repeat(2) {
            busqueda.alLlegarAlFinal()
            advanceUntilIdle()
        }
        assertEquals(total, busqueda.estado.value.trabajadores.size)
    }

    @Test
    fun cadaTrabajadorDeP06_seAbreEnP07_comoCliente() = runTest(reglaCorrutinas.testDispatcher) {
        val busqueda = resultados()
        advanceUntilIdle()
        val ids = busqueda.estado.value.trabajadores.map { it.trabajadorId }
        assertTrue(ids.isNotEmpty())

        for (id in ids) {
            val perfil = perfilPublico(id)
            advanceUntilIdle()
            assertNull("El perfil de $id no carga", perfil.estado.value.errorCarga)
            assertNotNull(perfil.estado.value.perfil)
            assertFalse("Un cliente no ve la vista previa", perfil.estado.value.esVistaPrevia)
        }
    }

    @Test
    fun elPropioTrabajador_abreSuPerfilComoVistaPrevia_yElDeOtro_no() = runTest(reglaCorrutinas.testDispatcher) {
        fuente.fijarSesionActiva(Sesion(fuente.usuarios.first { it.id == ID_TRABAJADOR }))

        val propio = perfilPublico(ID_TRABAJADOR)
        val ajeno = perfilPublico("usr-trab-2")
        advanceUntilIdle()

        assertTrue(propio.estado.value.esVistaPrevia)
        assertFalse(ajeno.estado.value.esVistaPrevia)
    }

    // ---- Las filas de error que las pantallas no prueban ----

    @Test
    fun P06_laPrimeraPaginaConError_dejaElTipo_ySeRecuperaConReintentar() = runTest(reglaCorrutinas.testDispatcher) {
        for (tipo in TIPOS) {
            repositorioCatalogos.errorForzado = tipo
            val busqueda = resultados()
            advanceUntilIdle()

            assertEquals(tipo, busqueda.estado.value.errorCarga)
            assertTrue(busqueda.estado.value.trabajadores.isEmpty())
            assertFalse(busqueda.estado.value.vacio)

            repositorioCatalogos.errorForzado = null
            busqueda.alReintentarCarga()
            advanceUntilIdle()

            assertNull(busqueda.estado.value.errorCarga)
            assertTrue(busqueda.estado.value.trabajadores.isNotEmpty())
        }
    }

    @Test
    fun P06_unaPaginaSiguienteConError_dejaLaLista_yReintentarLaAgrega() = runTest(reglaCorrutinas.testDispatcher) {
        agregarTrabajadores(45)
        for (tipo in TIPOS) {
            val busqueda = resultados()
            advanceUntilIdle()
            val primera = busqueda.estado.value.trabajadores.map { it.trabajadorId }

            repositorioCatalogos.errorForzado = tipo
            busqueda.alLlegarAlFinal()
            advanceUntilIdle()

            assertTrue(busqueda.estado.value.errorAlCargarMas)
            assertNull(busqueda.estado.value.errorCarga)
            assertEquals(primera, busqueda.estado.value.trabajadores.map { it.trabajadorId })

            repositorioCatalogos.errorForzado = null
            busqueda.alReintentarMas()
            advanceUntilIdle()

            assertFalse(busqueda.estado.value.errorAlCargarMas)
            assertEquals(2 * TAMANO_PAGINA, busqueda.estado.value.trabajadores.size)
        }
    }

    @Test
    fun P08_publicarConError_dejaElTipo_conservaLoEscrito_yNoAgregaNada() = runTest(reglaCorrutinas.testDispatcher) {
        for (tipo in TIPOS) {
            val formulario = formularioLleno(titulo = "Reparar una fuga")
            val antes = fuente.solicitudes.size

            repositorioSolicitudes.errorForzado = tipo
            formulario.alPublicar()
            advanceUntilIdle()

            val estado = formulario.estado.value
            assertEquals(tipo, estado.errorPantalla)
            assertFalse(estado.publicada)
            assertFalse(estado.publicando)
            assertEquals("Reparar una fuga", estado.titulo)
            assertEquals(antes, fuente.solicitudes.size)

            // Con el falso sano, publicar de nuevo funciona y borra el error.
            repositorioSolicitudes.errorForzado = null
            formulario.alPublicar()
            advanceUntilIdle()

            assertTrue(formulario.estado.value.publicada)
            assertNull(formulario.estado.value.errorPantalla)
            assertEquals(antes + 1, fuente.solicitudes.size)
        }
    }

    @Test
    fun P09_laPrimeraCargaConError_dejaElTipo_ySeRecuperaConReintentar() = runTest(reglaCorrutinas.testDispatcher) {
        for (tipo in TIPOS) {
            repositorioSolicitudes.errorForzado = tipo
            val lista = misSolicitudes()
            lista.alCargar()
            advanceUntilIdle()

            assertEquals(tipo, lista.estado.value.errorCarga)
            assertFalse(lista.estado.value.vacio)

            repositorioSolicitudes.errorForzado = null
            lista.alReintentarCarga()
            advanceUntilIdle()

            assertNull(lista.estado.value.errorCarga)
            assertTrue(lista.estado.value.solicitudes.isNotEmpty())
        }
    }

    @Test
    fun P09_unaRecargaConError_dejaLaLista_ySoloAvisa() = runTest(reglaCorrutinas.testDispatcher) {
        for (tipo in TIPOS) {
            val lista = misSolicitudes()
            lista.alCargar()
            advanceUntilIdle()
            val antes = lista.estado.value.solicitudes.map { it.id }

            repositorioSolicitudes.errorForzado = tipo
            lista.alCargar()
            advanceUntilIdle()

            assertEquals(antes, lista.estado.value.solicitudes.map { it.id })
            assertNull(lista.estado.value.errorCarga)
            assertEquals(obtenerMensajeErrorRes(tipo), lista.estado.value.aviso)
            repositorioSolicitudes.errorForzado = null
        }
    }

    @Test
    fun sinSesionEnLaFuente_publicarYCargarP09_terminanEnAutenticacion() = runTest(reglaCorrutinas.testDispatcher) {
        val formulario = formularioLleno()
        fuente.fijarSesionActiva(null)

        formulario.alPublicar()
        advanceUntilIdle()
        assertEquals(TipoError.AUTENTICACION, formulario.estado.value.errorPantalla)
        assertFalse(formulario.estado.value.publicada)

        val lista = misSolicitudes()
        lista.alCargar()
        advanceUntilIdle()
        assertEquals(TipoError.AUTENTICACION, lista.estado.value.errorCarga)
    }

    private companion object {
        const val ID_CLIENTE = "usr-cli-1"
        const val ID_TRABAJADOR = "usr-trab-1"

        /** Cada tipo de error que una pantalla tiene que saber pintar. */
        val TIPOS = listOf(
            TipoError.RED,
            TipoError.SERVIDOR,
            TipoError.DESCONOCIDO,
            TipoError.AUTENTICACION,
            TipoError.VALIDACION
        )
    }
}
