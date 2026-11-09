package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import mx.donchambitas.app.R
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioCatalogosFalso
import mx.donchambitas.app.datos.falso.RepositorioIaFalso
import mx.donchambitas.app.datos.falso.RepositorioTrabajadorFalso
import mx.donchambitas.app.dominio.modelo.Estado
import mx.donchambitas.app.dominio.modelo.Municipio
import mx.donchambitas.app.ui.tema.DonChambitasTema
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas instrumentadas de la pantalla Mi perfil (P-11).
 * Cubren el aviso de perfil incompleto, la vista previa, las habilidades, el
 * selector de municipio, la disponibilidad, los estados del boton de IA y el
 * bloqueo mientras se guarda. La carga y el guardado son de S3-T06.
 */
@RunWith(AndroidJUnit4::class)
class MiPerfilPantallaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun texto(@StringRes id: Int, vararg argumentos: Any): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *argumentos)

    private val estados = listOf(Estado(14, "Jalisco", "JAL"), Estado(21, "Puebla", "PUE"))
    private val municipios = listOf(Municipio(1, 14, "Guadalajara"), Municipio(2, 14, "Zapopan"))

    /** Lo que la pantalla pidio, para comprobar cada callback sin ViewModel. */
    private class Eventos {
        var vistaPrevia = 0
        var guardar = 0
        var ia = 0
        var quitada: String? = null
        var estadoElegido: Int? = null
        var disponible: Boolean? = null
    }

    private fun montarContenido(estado: EstadoMiPerfil): Eventos {
        val eventos = Eventos()
        composeTestRule.setContent {
            DonChambitasTema {
                MiPerfilContenido(
                    estado = estado,
                    alCambiarTitulo = {},
                    alCambiarExperiencia = {},
                    alCambiarHabilidadNueva = {},
                    alAgregarHabilidad = {},
                    alQuitarHabilidad = { eventos.quitada = it },
                    alCambiarDescripcion = {},
                    alElegirEstado = { eventos.estadoElegido = it },
                    alElegirMunicipio = {},
                    alCambiarTelefono = {},
                    alCambiarDisponible = { eventos.disponible = it },
                    alGuardar = { eventos.guardar++ },
                    alPedirIa = { eventos.ia++ },
                    alRegresar = {},
                    alVerComoCliente = { eventos.vistaPrevia++ }
                )
            }
        }
        return eventos
    }

    private fun montarPantalla() {
        composeTestRule.setContent {
            DonChambitasTema {
                MiPerfilPantalla(alRegresar = {}, alVerComoCliente = {}, viewModel = viewModelSinRetraso())
            }
        }
    }

    private fun boton(@StringRes id: Int) =
        composeTestRule.onNode(hasText(texto(id)) and hasClickAction())

    @Test
    fun avisoIncompleto_uneLoQueFaltaEnUnaSolaFrase() {
        montarContenido(
            EstadoMiPerfil(
                cargando = false,
                faltantes = listOf(FaltaPerfil.DESCRIPCION, FaltaPerfil.HABILIDAD, FaltaPerfil.UBICACION)
            )
        )

        composeTestRule.onNodeWithText(texto(R.string.mi_perfil_incompleto_titulo)).assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Te falta una descripción, al menos una habilidad y dónde trabajas")
            .assertIsDisplayed()
    }

    @Test
    fun avisoIncompleto_noSePintaSiNoFaltaNada() {
        montarContenido(EstadoMiPerfil(cargando = false, faltantes = emptyList()))

        composeTestRule.onNodeWithText(texto(R.string.mi_perfil_incompleto_titulo)).assertDoesNotExist()
    }

    @Test
    fun vistaPrevia_soloSeHabilitaConElPerfilGuardado() {
        montarContenido(EstadoMiPerfil(cargando = false, perfilGuardado = false))
        boton(R.string.mi_perfil_vista_previa).assertIsNotEnabled()
    }

    @Test
    fun vistaPrevia_conPerfilGuardado_avisaAlTocarla() {
        val eventos = montarContenido(EstadoMiPerfil(cargando = false, perfilGuardado = true))

        boton(R.string.mi_perfil_vista_previa).assertIsEnabled().performClick()

        assertEquals(1, eventos.vistaPrevia)
    }

    @Test
    fun habilidad_alTocarSuChip_pideQuitarla() {
        val eventos = montarContenido(EstadoMiPerfil(cargando = false, habilidades = listOf("Tinacos", "Boilers")))

        composeTestRule
            .onNodeWithContentDescription(texto(R.string.mi_perfil_quitar_habilidad, "Boilers"))
            .performScrollTo()
            .performClick()

        assertEquals("Boilers", eventos.quitada)
    }

    @Test
    fun habilidad_agregarLaPoneComoChipYVaciaElCampo() {
        montarPantalla()

        composeTestRule.onNodeWithText(texto(R.string.mi_perfil_habilidad_nueva)).performTextInput("Drenaje")
        composeTestRule.onNodeWithContentDescription(texto(R.string.mi_perfil_agregar_habilidad)).performClick()

        composeTestRule
            .onNodeWithContentDescription(texto(R.string.mi_perfil_quitar_habilidad, "Drenaje"))
            .assertExists()
        // El campo quedo vacio: el texto "Drenaje" ya solo vive en el chip.
        composeTestRule.onAllNodes(hasText("Drenaje")).assertCountEquals(1)
    }

    @Test
    fun habilidad_vaciaNoSeAgrega() {
        montarPantalla()

        composeTestRule.onNodeWithText(texto(R.string.mi_perfil_habilidad_nueva)).performTextInput("   ")
        composeTestRule.onNodeWithContentDescription(texto(R.string.mi_perfil_agregar_habilidad)).performClick()

        composeTestRule
            .onNodeWithContentDescription(texto(R.string.mi_perfil_quitar_habilidad, ""))
            .assertDoesNotExist()
    }

    @Test
    fun municipio_sinEstado_estaDeshabilitadoYLoExplica() {
        montarContenido(EstadoMiPerfil(cargando = false, estados = estados))

        composeTestRule.onNodeWithText(texto(R.string.mi_perfil_municipio_ayuda)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.mi_perfil_municipio)).performScrollTo().performClick()
        composeTestRule.onNodeWithText(texto(R.string.mi_perfil_hoja_municipio)).assertDoesNotExist()
    }

    @Test
    fun estado_abreLaHojaYAvisaElElegido() {
        val eventos = montarContenido(EstadoMiPerfil(cargando = false, estados = estados))

        composeTestRule.onNodeWithText(texto(R.string.mi_perfil_estado)).performScrollTo().performClick()
        composeTestRule.onNodeWithText(texto(R.string.mi_perfil_hoja_estado)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Puebla").performClick()

        assertEquals(21, eventos.estadoElegido)
        composeTestRule.onNodeWithText(texto(R.string.mi_perfil_hoja_estado)).assertDoesNotExist()
    }

    @Test
    fun municipio_conEstado_soloListaLosQueRecibe() {
        montarContenido(
            EstadoMiPerfil(cargando = false, estados = estados, municipios = municipios, estadoId = 14)
        )

        composeTestRule.onNodeWithText(texto(R.string.mi_perfil_municipio)).performScrollTo().performClick()

        composeTestRule.onNodeWithText("Guadalajara").assertIsDisplayed()
        composeTestRule.onNodeWithText("Zapopan").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cholula").assertDoesNotExist()
    }

    @Test
    fun disponibilidad_esSeleccionUnica() {
        val eventos = montarContenido(EstadoMiPerfil(cargando = false, disponible = true))

        composeTestRule.onNodeWithText(texto(R.string.mi_perfil_disponible)).performScrollTo().assertIsSelected()
        composeTestRule.onNodeWithText(texto(R.string.mi_perfil_no_disponible)).assertIsNotSelected().performClick()

        assertEquals(false, eventos.disponible)
    }

    @Test
    fun botonIa_faltanDatos_estaDeshabilitadoYDiceQueFalta() {
        montarContenido(EstadoMiPerfil(cargando = false, ia = EstadoBotonIa.FaltanDatos))

        boton(R.string.ia_redactar).performScrollTo().assertIsNotEnabled()
        composeTestRule.onNodeWithText(texto(R.string.mi_perfil_ia_falta_titulo)).assertIsDisplayed()
    }

    @Test
    fun botonIa_disponible_muestraLosUsosRestantesYAvisa() {
        val eventos = montarContenido(EstadoMiPerfil(cargando = false, ia = EstadoBotonIa.Disponible(7)))

        composeTestRule.onNodeWithText(texto(R.string.ia_restantes, 7)).performScrollTo().assertIsDisplayed()
        boton(R.string.ia_redactar).assertIsEnabled().performClick()

        assertEquals(1, eventos.ia)
    }

    @Test
    fun botonIa_limiteAlcanzado_dejaLaDescripcionEditable() {
        montarContenido(EstadoMiPerfil(cargando = false, ia = EstadoBotonIa.LimiteAlcanzado))

        boton(R.string.ia_redactar).performScrollTo().assertIsNotEnabled()
        composeTestRule.onNodeWithText(texto(R.string.ia_limite)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.mi_perfil_descripcion)).assertIsEnabled()
    }

    @Test
    fun guardando_bloqueaLosCamposYCambiaElTextoDelBotonPorElIndicador() {
        montarContenido(EstadoMiPerfil(cargando = false, titulo = "Plomero", guardando = true))

        composeTestRule.onNodeWithText("Plomero").assertIsNotEnabled()
        // BotonPrincipal pinta el indicador en lugar del texto mientras carga.
        composeTestRule.onNodeWithText(texto(R.string.mi_perfil_guardar)).assertDoesNotExist()
    }

    @Test
    fun guardar_sinGuardando_avisaAlTocarlo() {
        val eventos = montarContenido(EstadoMiPerfil(cargando = false))

        boton(R.string.mi_perfil_guardar).performScrollTo().performClick()

        assertEquals(1, eventos.guardar)
    }

    @Test
    fun pantalla_arrancaComoPerfilNuevo() {
        montarPantalla()

        composeTestRule
            .onNodeWithText("Te falta una descripción, al menos una habilidad, dónde trabajas y un teléfono de contacto")
            .assertIsDisplayed()
        boton(R.string.mi_perfil_vista_previa).assertIsNotEnabled()
    }
}

/**
 * P-11 contra los falsos, sin retraso. La fuente arranca con la sesion de
 * cliente1, que no tiene perfil: la pantalla abre como perfil nuevo.
 */
private fun viewModelSinRetraso(): MiPerfilViewModel {
    val fuente = FuenteDatosFalsa()
    return MiPerfilViewModel(
        RepositorioTrabajadorFalso(fuente).apply { retrasoMs = 0 },
        RepositorioCatalogosFalso(fuente).apply { retrasoMs = 0 },
        RepositorioIaFalso().apply { retrasoMs = 0 }
    )
}
