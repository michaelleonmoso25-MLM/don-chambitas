package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import java.math.BigDecimal
import java.time.Instant
import mx.donchambitas.app.dominio.modelo.CalificacionTrabajador
import mx.donchambitas.app.dominio.modelo.PerfilPublicoTrabajador
import mx.donchambitas.app.dominio.modelo.ResenaPublica
import mx.donchambitas.app.dominio.modelo.ServicioPublico
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.util.TipoError
import org.junit.Rule
import org.junit.Test

/** P-07: vista previa del trabajador (S3-T14) y perfil del cliente (S4-T03, H-39). */
class PerfilPublicoPantallaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun texto(@StringRes id: Int, vararg argumentos: Any): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *argumentos)

    private fun montar(estado: EstadoPerfilPublico) {
        composeTestRule.setContent {
            DonChambitasTema { PerfilPublicoContenido(estado = estado, alRegresar = {}) }
        }
    }

    private val vacio = PerfilPublicoTrabajador(
        id = "t1",
        nombre = "Pedro",
        apellidos = "Ramirez",
        titulo = "Plomero",
        estado = "Ciudad de Mexico",
        municipio = "Coyoacan"
    )

    private val completo = vacio.copy(
        descripcion = "Atiendo fugas y boilers.",
        experienciaAnios = 5,
        habilidades = listOf("Fugas", "Boilers"),
        calificacion = CalificacionTrabajador(promedio = 4.5, totalResenas = 2),
        servicios = listOf(
            ServicioPublico(
                id = "s1",
                titulo = "Instalacion de boiler",
                descripcion = "x",
                categoria = "Plomeria",
                categoriaId = 1,
                precioDesde = BigDecimal("350")
            )
        ),
        resenas = listOf(ResenaPublica(5, "Muy puntual", "Ana", Instant.parse("2026-10-01T00:00:00Z")))
    )

    @Test
    fun perfilSinNada_muestraUnVacioQueExplicaQueHacerEnCadaSeccion() {
        montar(EstadoPerfilPublico(cargando = false, perfil = vacio, esVistaPrevia = true))

        composeTestRule.onNodeWithText("Pedro Ramirez").assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(mx.donchambitas.app.R.string.perfil_publico_sin_calificacion)).assertIsDisplayed()
        for (id in listOf(
            mx.donchambitas.app.R.string.perfil_publico_sin_descripcion,
            mx.donchambitas.app.R.string.perfil_publico_sin_habilidades,
            mx.donchambitas.app.R.string.perfil_publico_sin_servicios,
            mx.donchambitas.app.R.string.perfil_publico_sin_resenas
        )) {
            composeTestRule.onNodeWithText(texto(id)).performScrollTo().assertIsDisplayed()
        }
    }

    @Test
    fun perfilCompleto_muestraSusDatosYNoLosVacios() {
        montar(EstadoPerfilPublico(cargando = false, perfil = completo))

        composeTestRule.onNodeWithText("Atiendo fugas y boilers.").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Fugas").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Instalacion de boiler").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Muy puntual").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(mx.donchambitas.app.R.string.perfil_publico_experiencia_varios, 5)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(mx.donchambitas.app.R.string.perfil_publico_sin_servicios)).assertDoesNotExist()
        composeTestRule.onNodeWithText(texto(mx.donchambitas.app.R.string.perfil_publico_sin_resenas)).assertDoesNotExist()
    }

    @Test
    fun vistaPrevia_muestraElAvisoYNoElBotonDeContactar() {
        montar(EstadoPerfilPublico(cargando = false, perfil = completo, esVistaPrevia = true))

        composeTestRule.onNodeWithText(texto(mx.donchambitas.app.R.string.perfil_publico_aviso_vista_previa)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(mx.donchambitas.app.R.string.perfil_publico_contactar)).assertDoesNotExist()
    }

    @Test
    fun cliente_ocultaLoVacioQueNoPuedeArreglarYUsaSusTextos() {
        montar(EstadoPerfilPublico(cargando = false, perfil = vacio))

        composeTestRule.onNodeWithText(texto(mx.donchambitas.app.R.string.perfil_publico_aviso_vista_previa)).assertDoesNotExist()
        composeTestRule.onNodeWithText(texto(mx.donchambitas.app.R.string.perfil_publico_sobre_mi)).assertDoesNotExist()
        composeTestRule.onNodeWithText(texto(mx.donchambitas.app.R.string.perfil_publico_habilidades)).assertDoesNotExist()
        composeTestRule.onNodeWithText(texto(mx.donchambitas.app.R.string.perfil_publico_sin_servicios_cliente)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(mx.donchambitas.app.R.string.perfil_publico_sin_resenas_cliente)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(mx.donchambitas.app.R.string.perfil_publico_sin_servicios)).assertDoesNotExist()
    }

    @Test
    fun cliente_veElBotonDeContactar() {
        montar(EstadoPerfilPublico(cargando = false, perfil = completo))

        composeTestRule.onNodeWithText(texto(mx.donchambitas.app.R.string.perfil_publico_contactar)).assertIsDisplayed()
    }

    @Test
    fun errorDeCarga_muestraElErrorConReintentar() {
        montar(EstadoPerfilPublico(cargando = false, errorCarga = TipoError.RED))

        composeTestRule.onNodeWithText(texto(mx.donchambitas.app.R.string.error_red)).assertIsDisplayed()
    }
}
