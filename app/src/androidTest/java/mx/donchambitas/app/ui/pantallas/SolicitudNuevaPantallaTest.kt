package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.Categoria
import mx.donchambitas.app.dominio.modelo.Estado
import mx.donchambitas.app.dominio.modelo.Municipio
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** P-08 Publicar solicitud (S4-T15, DISENO-SOLICITUDES-BUSQUEDA.md 4). */
class SolicitudNuevaPantallaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun texto(@StringRes id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private val plomeria = Categoria(id = 1, nombre = "Plomeria")
    private val electricidad = Categoria(id = 2, nombre = "Electricidad")
    private val cdmx = Estado(id = 9, nombre = "Ciudad de Mexico", clave = "CMX")
    private val coyoacan = Municipio(id = 4, estadoId = 9, nombre = "Coyoacan")

    private class Eventos {
        var titulo: String? = null
        var categoria: Int? = null
        var descripcion: String? = null
        var presupuesto: String? = null
        var estado: Int? = null
        var municipio: Int? = null
        var publico = 0
        var reintento = 0
    }

    /**
     * Guarda el estado como lo haria el ViewModel: un campo con valor externo vuelve a ese valor si
     * nadie lo actualiza, y la prueba escribiria "algo" y leeria "".
     */
    private fun montar(estado: EstadoSolicitudNueva, eventos: Eventos = Eventos()) {
        composeTestRule.setContent {
            var actual by remember { mutableStateOf(estado) }
            DonChambitasTema {
                SolicitudNuevaContenido(
                    estado = actual,
                    alCambiarTitulo = {
                        eventos.titulo = it
                        actual = actual.copy(titulo = it)
                    },
                    alElegirCategoria = { eventos.categoria = it },
                    alCambiarDescripcion = {
                        eventos.descripcion = it
                        actual = actual.copy(descripcion = it)
                    },
                    alCambiarPresupuesto = {
                        eventos.presupuesto = it
                        actual = actual.copy(presupuesto = it)
                    },
                    alElegirEstado = { eventos.estado = it },
                    alElegirMunicipio = { eventos.municipio = it },
                    alPublicar = { eventos.publico++ },
                    alRegresar = {},
                    alReintentarCarga = { eventos.reintento++ }
                )
            }
        }
    }

    private fun listo(
        titulo: String = "",
        estadoElegido: Int? = null,
        municipios: List<Municipio> = emptyList()
    ) = EstadoSolicitudNueva(
        cargando = false,
        titulo = titulo,
        estadoId = estadoElegido,
        categorias = listOf(plomeria, electricidad),
        estados = listOf(cdmx),
        municipios = municipios
    )

    /** El boton es lo unico pulsable que dice "Publicar solicitud": el titulo de la barra tambien lo dice. */
    private fun botonPublicar() =
        composeTestRule.onNode(hasText(texto(R.string.solicitud_publicar)) and hasClickAction())

    /** "Presupuesto" tambien es el encabezado del bloque: el campo es el que se puede editar. */
    private fun campoPresupuesto() =
        composeTestRule.onNode(hasText(texto(R.string.solicitud_campo_presupuesto)) and hasSetTextAction())

    @Test
    fun muestraLosCamposDelFormulario() {
        montar(listo())

        composeTestRule.onNodeWithText(texto(R.string.solicitud_campo_titulo)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Plomeria").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.solicitud_campo_descripcion)).performScrollTo().assertIsDisplayed()
        campoPresupuesto().performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.solicitud_estado)).performScrollTo().assertIsDisplayed()
        botonPublicar().performScrollTo().assertIsDisplayed()
    }

    @Test
    fun cadaCampo_avisaLoQueSeEscribeOSeElige() {
        val eventos = Eventos()
        montar(listo(), eventos)

        composeTestRule.onNodeWithText(texto(R.string.solicitud_campo_titulo)).performTextInput("Reparar una fuga")
        composeTestRule.onNodeWithText("Electricidad").performScrollTo().performClick()
        composeTestRule.onNodeWithText(texto(R.string.solicitud_campo_descripcion)).performScrollTo().performTextInput("Gotea el lavabo")
        campoPresupuesto().performScrollTo().performTextInput("350")

        assertEquals("Reparar una fuga", eventos.titulo)
        assertEquals(2, eventos.categoria)
        assertEquals("Gotea el lavabo", eventos.descripcion)
        assertEquals("350", eventos.presupuesto)
    }

    @Test
    fun elEstadoSeEligeEnUnaHoja() {
        val eventos = Eventos()
        montar(listo(), eventos)

        composeTestRule.onNodeWithText(texto(R.string.solicitud_estado)).performScrollTo().performClick()
        composeTestRule.onNodeWithText(texto(R.string.solicitud_hoja_estado)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Ciudad de Mexico").performClick()

        assertEquals(9, eventos.estado)
    }

    @Test
    fun elMunicipio_estaDeshabilitadoHastaElegirEstado() {
        montar(listo())

        composeTestRule.onNodeWithText(texto(R.string.solicitud_municipio)).performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun conEstadoElegido_elMunicipioSeEligeEnUnaHoja() {
        val eventos = Eventos()
        montar(listo(estadoElegido = 9, municipios = listOf(coyoacan)), eventos)

        composeTestRule.onNodeWithText(texto(R.string.solicitud_municipio)).performScrollTo().performClick()
        composeTestRule.onNodeWithText(texto(R.string.solicitud_hoja_municipio)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Coyoacan").performClick()

        assertEquals(4, eventos.municipio)
    }

    @Test
    fun alPulsarPublicar_avisa() {
        val eventos = Eventos()
        montar(listo(), eventos)

        botonPublicar().performScrollTo().performClick()

        assertEquals(1, eventos.publico)
    }

    @Test
    fun losErroresDeCadaCampo_salenEnRojoDebajoDeEl() {
        montar(
            listo().copy(
                errorTitulo = R.string.validacion_solicitud_titulo_vacio,
                errorCategoria = R.string.validacion_solicitud_categoria_sin_elegir,
                errorDescripcion = R.string.validacion_solicitud_descripcion_vacia,
                errorEstado = R.string.validacion_solicitud_estado_sin_elegir
            )
        )

        composeTestRule.onNodeWithText(texto(R.string.validacion_solicitud_titulo_vacio)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.validacion_solicitud_categoria_sin_elegir)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.validacion_solicitud_descripcion_vacia)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.validacion_solicitud_estado_sin_elegir)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun mientrasPublica_elBotonMuestraElIndicadorEnVezDelTexto() {
        montar(listo().copy(publicando = true))

        // Solo queda el titulo de la barra: el texto del boton lo sustituye el indicador.
        composeTestRule.onAllNodesWithText(texto(R.string.solicitud_publicar)).assertCountEquals(1)
    }

    @Test
    fun siFallaPublicar_elErrorSaleEnLaPantalla_yLoEscritoSeQueda() {
        montar(listo(titulo = "Reparar una fuga").copy(errorPantalla = TipoError.RED))

        composeTestRule.onNodeWithText(texto(R.string.error_red)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Reparar una fuga").assertIsDisplayed()
    }

    @Test
    fun siFallaLaCargaDeLosCatalogos_sacaElErrorConReintentar() {
        val eventos = Eventos()
        montar(EstadoSolicitudNueva(cargando = false, errorCarga = TipoError.RED), eventos)

        composeTestRule.onNodeWithText(texto(R.string.error_red)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.reintentar)).performClick()

        assertEquals(1, eventos.reintento)
    }
}
