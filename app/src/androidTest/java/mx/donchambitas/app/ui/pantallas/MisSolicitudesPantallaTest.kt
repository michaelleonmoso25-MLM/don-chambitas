package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import java.math.BigDecimal
import java.time.Instant
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.Categoria
import mx.donchambitas.app.dominio.modelo.EstadoSolicitud
import mx.donchambitas.app.dominio.modelo.Solicitud
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/** P-09 (S4-T06, DISENO-SOLICITUDES-BUSQUEDA.md 5). */
class MisSolicitudesPantallaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun texto(@StringRes id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private val plomeria = Categoria(id = 1, nombre = "Plomeria")

    private val conPresupuesto = Solicitud(
        id = "s1",
        clienteId = "c1",
        categoriaId = 1,
        titulo = "Reparar una fuga",
        descripcion = "Hay una fuga bajo el lavabo",
        presupuesto = BigDecimal("350.00"),
        creadoEn = Instant.parse("2026-11-07T12:00:00Z"),
        actualizadoEn = Instant.parse("2026-11-07T12:00:00Z")
    )

    private val aConvenir = conPresupuesto.copy(
        id = "s2",
        titulo = "Pintar una recamara",
        presupuesto = null,
        estatus = EstadoSolicitud.CERRADA
    )

    private class Eventos {
        var publico = 0
        var abrio: String? = null
        var reintento = 0
        var consumioAviso = 0
    }

    private fun montar(estado: EstadoMisSolicitudes, eventos: Eventos = Eventos()) {
        composeTestRule.setContent {
            DonChambitasTema {
                MisSolicitudesContenido(
                    estado = estado,
                    alPublicar = { eventos.publico++ },
                    alAbrirSolicitud = { eventos.abrio = it },
                    alReintentarCarga = { eventos.reintento++ },
                    alConsumirAviso = { eventos.consumioAviso++ }
                )
            }
        }
    }

    @Test
    fun muestraCadaSolicitud_conSuCategoria_presupuestoOAConvenir_yEstado() {
        montar(
            EstadoMisSolicitudes(
                cargando = false,
                solicitudes = listOf(conPresupuesto, aConvenir),
                categorias = listOf(plomeria)
            )
        )

        composeTestRule.onNodeWithText(texto(R.string.mis_solicitudes_titulo)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Reparar una fuga").assertIsDisplayed()
        composeTestRule.onNodeWithText("$350").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pintar una recamara").assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.mis_solicitudes_a_convenir)).assertIsDisplayed()
    }

    @Test
    fun tocarUnaSolicitud_avisaCualFue() {
        val eventos = Eventos()
        montar(
            EstadoMisSolicitudes(cargando = false, solicitudes = listOf(conPresupuesto), categorias = listOf(plomeria)),
            eventos
        )

        composeTestRule.onNodeWithText("Reparar una fuga").performClick()

        assertEquals("s1", eventos.abrio)
    }

    @Test
    fun elBotonMas_publica() {
        val eventos = Eventos()
        montar(
            EstadoMisSolicitudes(cargando = false, solicitudes = listOf(conPresupuesto), categorias = listOf(plomeria)),
            eventos
        )

        composeTestRule.onNodeWithContentDescription(texto(R.string.boton_publicar_solicitud)).performClick()

        assertEquals(1, eventos.publico)
    }

    @Test
    fun sinSolicitudes_muestraElVacio_conSuBoton_yNoElBotonMas() {
        val eventos = Eventos()
        montar(EstadoMisSolicitudes(cargando = false), eventos)

        composeTestRule.onNodeWithText(texto(R.string.mis_solicitudes_vacio_titulo)).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(texto(R.string.boton_publicar_solicitud)).assertDoesNotExist()
        composeTestRule.onNodeWithText(texto(R.string.mis_solicitudes_vacio_accion)).performClick()

        assertEquals(1, eventos.publico)
    }

    @Test
    fun mientrasCarga_noSaleElBotonMas() {
        montar(EstadoMisSolicitudes(cargando = true))

        composeTestRule.onNodeWithContentDescription(texto(R.string.boton_publicar_solicitud)).assertDoesNotExist()
    }

    @Test
    fun conError_muestraElErrorConReintentar_yNoElBotonMas() {
        val eventos = Eventos()
        montar(EstadoMisSolicitudes(cargando = false, errorCarga = TipoError.RED), eventos)

        composeTestRule.onNodeWithText(texto(R.string.error_red)).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(texto(R.string.boton_publicar_solicitud)).assertDoesNotExist()
        composeTestRule.onNodeWithText("Reintentar").performClick()

        assertEquals(1, eventos.reintento)
        assertNull(eventos.abrio)
    }

    @Test
    fun elAviso_seMuestra_yAvisaQueSeConsumio() {
        val eventos = Eventos()
        montar(
            EstadoMisSolicitudes(
                cargando = false,
                solicitudes = listOf(conPresupuesto),
                categorias = listOf(plomeria),
                aviso = R.string.solicitud_publicada_aviso
            ),
            eventos
        )

        composeTestRule.onNodeWithText(texto(R.string.solicitud_publicada_aviso)).assertIsDisplayed()
        composeTestRule.waitForIdle()
        assertEquals(1, eventos.consumioAviso)
    }
}
