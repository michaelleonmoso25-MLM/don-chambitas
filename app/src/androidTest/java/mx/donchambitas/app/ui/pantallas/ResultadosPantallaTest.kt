package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import java.time.Instant
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.Categoria
import mx.donchambitas.app.dominio.modelo.Estado
import mx.donchambitas.app.dominio.modelo.FiltrosBusquedaTrabajadores
import mx.donchambitas.app.dominio.modelo.OrdenBusqueda
import mx.donchambitas.app.dominio.modelo.ResumenTrabajadorBusqueda
import mx.donchambitas.app.ui.tema.DonChambitasTema
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** P-06 y su buscador (S4-T03 a S4-T07, DISENO-SOLICITUDES-BUSQUEDA.md 3). */
class ResultadosPantallaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun texto(@StringRes id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private val pedro = ResumenTrabajadorBusqueda(
        trabajadorId = "t1",
        nombre = "Pedro",
        apellidos = "Ramirez",
        titulo = "Plomero",
        disponible = true,
        estadoId = 1,
        municipioId = 1,
        estado = "Ciudad de Mexico",
        municipio = "Coyoacan",
        promedio = 4.5,
        totalResenas = 2,
        serviciosActivos = 1,
        precioDesde = null,
        categorias = listOf(1),
        creadoEn = Instant.parse("2026-10-01T00:00:00Z")
    )

    private class Eventos {
        var escrito: String? = null
        var buscado = 0
        var borrado = 0
        var abrioFiltros = 0
        var aplicado: FiltrosBusquedaTrabajadores? = null
        var quitado: FiltroResultados? = null
        var quitoTodos = 0
        var ordenElegido: OrdenBusqueda? = null
        var pidioMas = 0
        var reintentoMas = 0
    }

    private fun montar(estado: EstadoResultados, eventos: Eventos = Eventos()) {
        composeTestRule.setContent {
            DonChambitasTema {
                ResultadosContenido(
                    estado = estado,
                    alRegresar = {},
                    alAbrirPerfil = {},
                    alCambiarTexto = { eventos.escrito = it },
                    alBuscar = { eventos.buscado++ },
                    alBorrarTexto = { eventos.borrado++ },
                    alAbrirFiltros = { eventos.abrioFiltros++ },
                    alAplicarFiltros = { eventos.aplicado = it },
                    alQuitarFiltro = { eventos.quitado = it },
                    alQuitarTodosLosFiltros = { eventos.quitoTodos++ },
                    alElegirOrden = { eventos.ordenElegido = it },
                    alLlegarAlFinal = { eventos.pidioMas++ },
                    alReintentarMas = { eventos.reintentoMas++ }
                )
            }
        }
    }

    @Test
    fun muestraElBuscadorYLaLista() {
        montar(EstadoResultados(cargando = false, trabajadores = listOf(pedro)))

        composeTestRule.onNodeWithText(texto(R.string.resultados_buscador_marcador)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Pedro Ramirez").assertIsDisplayed()
    }

    @Test
    fun alEscribir_avisaElTextoSinBuscar() {
        val eventos = Eventos()
        montar(EstadoResultados(cargando = false, trabajadores = listOf(pedro)), eventos)

        composeTestRule.onNode(hasSetTextAction()).performTextInput("pedro")

        assertEquals("pedro", eventos.escrito)
        assertEquals(0, eventos.buscado)
    }

    @Test
    fun alPulsarBuscarEnElTeclado_busca() {
        val eventos = Eventos()
        montar(EstadoResultados(cargando = false, texto = "pedro", trabajadores = listOf(pedro)), eventos)

        composeTestRule.onNode(hasSetTextAction()).performImeAction()

        assertEquals(1, eventos.buscado)
    }

    @Test
    fun sinTexto_noSaleElBotonDeBorrar() {
        montar(EstadoResultados(cargando = false, trabajadores = listOf(pedro)))

        composeTestRule.onNodeWithContentDescription(texto(R.string.resultados_buscador_borrar)).assertDoesNotExist()
    }

    @Test
    fun conTexto_elBotonDeBorrarBorra() {
        val eventos = Eventos()
        montar(EstadoResultados(cargando = false, texto = "pedro", trabajadores = listOf(pedro)), eventos)

        composeTestRule.onNodeWithContentDescription(texto(R.string.resultados_buscador_borrar)).performClick()

        assertEquals(1, eventos.borrado)
    }

    @Test
    fun conTextoYSinResultados_muestraElVacioYElBuscadorSigueAhi() {
        montar(EstadoResultados(cargando = false, texto = "zzz"))

        composeTestRule.onNodeWithText(texto(R.string.resultados_vacio_titulo)).assertIsDisplayed()
        composeTestRule.onNodeWithText("zzz").assertIsDisplayed()
    }

    // ---- S4-T05: filtros ----

    private val plomeria = Categoria(id = 1, nombre = "Plomeria")
    private val cdmx = Estado(id = 9, nombre = "Ciudad de Mexico", clave = "CDMX")

    @Test
    fun sinFiltros_elBotonDiceFiltros_yAbreLaHoja() {
        val eventos = Eventos()
        montar(EstadoResultados(cargando = false, trabajadores = listOf(pedro)), eventos)

        composeTestRule.onNodeWithText(texto(R.string.filtros_boton)).performClick()

        assertEquals(1, eventos.abrioFiltros)
    }

    @Test
    fun conFiltros_elBotonCuentaLosActivos_yCadaUnoSaleComoChip() {
        montar(
            EstadoResultados(
                cargando = false,
                trabajadores = listOf(pedro),
                categorias = listOf(plomeria),
                filtros = FiltrosBusquedaTrabajadores(categoriaId = 1, precioDesdeMax = BigDecimal("500.00"))
            )
        )

        composeTestRule.onNodeWithText("Filtros (2)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Plomeria").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hasta $500").assertIsDisplayed()
    }

    @Test
    fun elChipDelEstadoLlevaSuNombre() {
        montar(
            EstadoResultados(
                cargando = false,
                trabajadores = listOf(pedro),
                estados = listOf(cdmx),
                filtros = FiltrosBusquedaTrabajadores(estadoId = 9)
            )
        )

        composeTestRule.onNodeWithText("Filtros (1)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ciudad de Mexico").assertIsDisplayed()
    }

    @Test
    fun elChipDeLaCalificacionNoLlevaElCeroDeSobra() {
        montar(
            EstadoResultados(
                cargando = false,
                trabajadores = listOf(pedro),
                filtros = FiltrosBusquedaTrabajadores(promedioMin = 4.0)
            )
        )

        composeTestRule.onNodeWithText("4 o más").assertIsDisplayed()
    }

    @Test
    fun alTocarUnChip_quitaEseFiltro() {
        val eventos = Eventos()
        montar(
            EstadoResultados(
                cargando = false,
                trabajadores = listOf(pedro),
                categorias = listOf(plomeria),
                filtros = FiltrosBusquedaTrabajadores(categoriaId = 1)
            ),
            eventos
        )

        composeTestRule.onNodeWithText("Plomeria").performClick()

        assertEquals(FiltroResultados.CATEGORIA, eventos.quitado)
    }

    @Test
    fun conFiltrosYSinResultados_elVacioOfreceQuitarlos() {
        val eventos = Eventos()
        montar(
            EstadoResultados(
                cargando = false,
                filtros = FiltrosBusquedaTrabajadores(promedioMin = 4.5)
            ),
            eventos
        )

        composeTestRule.onNodeWithText(texto(R.string.resultados_vacio_titulo)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.filtros_quitar)).performClick()

        assertEquals(1, eventos.quitoTodos)
    }

    @Test
    fun sinFiltrosYSinResultados_elVacioNoOfreceQuitarlos() {
        montar(EstadoResultados(cargando = false))

        composeTestRule.onNodeWithText(texto(R.string.resultados_vacio_titulo)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.filtros_quitar)).assertDoesNotExist()
    }

    @Test
    fun laHoja_muestraLosFiltros_yAplicaLaCalificacionElegida() {
        val eventos = Eventos()
        montar(
            EstadoResultados(
                cargando = false,
                trabajadores = listOf(pedro),
                hojaFiltrosAbierta = true,
                categorias = listOf(plomeria),
                estados = listOf(cdmx)
            ),
            eventos
        )

        // "Filtros" sale dos veces (el boton y el titulo de la hoja): la hoja se reconoce por sus controles.
        composeTestRule.onAllNodesWithText(texto(R.string.filtros_titulo)).assertCountEquals(2)
        composeTestRule.onNodeWithText(texto(R.string.filtros_precio_maximo)).assertIsDisplayed()
        composeTestRule.onNodeWithText("4 o más").performScrollTo().performClick()
        composeTestRule.onNodeWithText(texto(R.string.filtros_aplicar)).performScrollTo().performClick()

        assertEquals(4.0, eventos.aplicado?.promedioMin)
    }

    @Test
    fun laHoja_noAplicaUnPrecioQueNoCabe_yMarcaElError() {
        val eventos = Eventos()
        montar(
            EstadoResultados(
                cargando = false,
                trabajadores = listOf(pedro),
                hojaFiltrosAbierta = true,
                categorias = listOf(plomeria),
                estados = listOf(cdmx)
            ),
            eventos
        )

        composeTestRule.onNodeWithText(texto(R.string.filtros_precio_maximo)).performScrollTo().performTextInput("100000000")
        composeTestRule.onNodeWithText(texto(R.string.filtros_aplicar)).performScrollTo().performClick()

        assertNull(eventos.aplicado)
        composeTestRule.onNodeWithText(texto(R.string.validacion_solicitud_presupuesto_grande)).assertIsDisplayed()
    }

    @Test
    fun laHoja_aplicaElPrecioEscrito() {
        val eventos = Eventos()
        montar(
            EstadoResultados(
                cargando = false,
                trabajadores = listOf(pedro),
                hojaFiltrosAbierta = true,
                categorias = listOf(plomeria),
                estados = listOf(cdmx)
            ),
            eventos
        )

        composeTestRule.onNodeWithText(texto(R.string.filtros_precio_maximo)).performScrollTo().performTextInput("500")
        composeTestRule.onNodeWithText(texto(R.string.filtros_aplicar)).performScrollTo().performClick()

        assertEquals(BigDecimal("500.00"), eventos.aplicado?.precioDesdeMax)
    }

    @Test
    fun laHoja_quitaTodosLosFiltros() {
        val eventos = Eventos()
        montar(
            EstadoResultados(
                cargando = false,
                trabajadores = listOf(pedro),
                hojaFiltrosAbierta = true,
                categorias = listOf(plomeria),
                estados = listOf(cdmx),
                filtros = FiltrosBusquedaTrabajadores(categoriaId = 1)
            ),
            eventos
        )

        composeTestRule.onNodeWithText(texto(R.string.filtros_quitar)).performScrollTo().performClick()

        assertEquals(1, eventos.quitoTodos)
    }

    @Test
    fun laHoja_traeElMunicipioDeshabilitadoHastaElegirEstado() {
        montar(
            EstadoResultados(
                cargando = false,
                trabajadores = listOf(pedro),
                hojaFiltrosAbierta = true,
                categorias = listOf(plomeria),
                estados = listOf(cdmx)
            )
        )

        composeTestRule.onNodeWithText(texto(R.string.filtros_municipio)).performScrollTo().assertIsNotEnabled()
    }

    // ---- S4-T07: orden y paginas ----

    @Test
    fun laFilaDeOrden_diceElOrdenActual() {
        montar(
            EstadoResultados(
                cargando = false,
                trabajadores = listOf(pedro),
                filtros = FiltrosBusquedaTrabajadores(orden = OrdenBusqueda.CREADO_EN)
            )
        )

        composeTestRule.onNodeWithText("Ordenar por: Más recientes").assertIsDisplayed()
    }

    @Test
    fun sinOrdenElegido_laFilaDiceMejorCalificados() {
        montar(EstadoResultados(cargando = false, trabajadores = listOf(pedro)))

        composeTestRule.onNodeWithText("Ordenar por: Mejor calificados").assertIsDisplayed()
    }

    @Test
    fun alTocarLaFilaDeOrden_salenLasTresOpciones_yElegirUnaLaAvisa() {
        val eventos = Eventos()
        montar(EstadoResultados(cargando = false, trabajadores = listOf(pedro)), eventos)

        composeTestRule.onNodeWithText("Ordenar por: Mejor calificados").performClick()
        composeTestRule.onNodeWithText(texto(R.string.resultados_orden_calificacion)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.resultados_orden_recientes)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.resultados_orden_precio)).performClick()

        assertEquals(OrdenBusqueda.PRECIO_DESDE, eventos.ordenElegido)
    }

    @Test
    fun alAcercarseAlFinal_pideLaPaginaSiguiente() {
        val eventos = Eventos()
        montar(EstadoResultados(cargando = false, trabajadores = listOf(pedro)), eventos)

        composeTestRule.waitForIdle()

        assertTrue(eventos.pidioMas >= 1)
    }

    @Test
    fun mientrasLlegaUnaPagina_elPieMuestraElIndicador() {
        montar(EstadoResultados(cargando = false, trabajadores = listOf(pedro), cargandoMas = true))

        composeTestRule.onNodeWithContentDescription(texto(R.string.cargando)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.resultados_mas_error)).assertDoesNotExist()
    }

    @Test
    fun sinPaginaEnCamino_elPieNoMuestraNada() {
        montar(EstadoResultados(cargando = false, trabajadores = listOf(pedro), hayMas = false))

        composeTestRule.onNodeWithContentDescription(texto(R.string.cargando)).assertDoesNotExist()
        composeTestRule.onNodeWithText(texto(R.string.resultados_mas_error)).assertDoesNotExist()
    }

    @Test
    fun siFalloLaPagina_laListaSeQueda_yElPieOfreceReintentar() {
        val eventos = Eventos()
        montar(
            EstadoResultados(cargando = false, trabajadores = listOf(pedro), errorAlCargarMas = true),
            eventos
        )

        composeTestRule.onNodeWithText("Pedro Ramirez").assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.resultados_mas_error)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.reintentar)).performScrollTo().performClick()

        assertEquals(1, eventos.reintentoMas)
    }
}
