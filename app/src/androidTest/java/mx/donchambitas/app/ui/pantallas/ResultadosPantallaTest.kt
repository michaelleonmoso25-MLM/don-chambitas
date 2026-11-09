package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import java.time.Instant
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.ResumenTrabajadorBusqueda
import mx.donchambitas.app.ui.tema.DonChambitasTema
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** P-06 y su buscador (S4-T03 y S4-T04, DISENO-SOLICITUDES-BUSQUEDA.md 3). */
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
                    alBorrarTexto = { eventos.borrado++ }
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
}
