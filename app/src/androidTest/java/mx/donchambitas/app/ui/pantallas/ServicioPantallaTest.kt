package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
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
import androidx.lifecycle.SavedStateHandle
import androidx.test.platform.app.InstrumentationRegistry
import java.time.Instant
import mx.donchambitas.app.R
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioCatalogosFalso
import mx.donchambitas.app.datos.falso.RepositorioIaFalso
import mx.donchambitas.app.datos.falso.RepositorioServiciosFalso
import mx.donchambitas.app.dominio.modelo.Categoria
import mx.donchambitas.app.dominio.modelo.ServicioFoto
import mx.donchambitas.app.ui.tema.DonChambitasTema
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas instrumentadas de la pantalla Crear o editar servicio (P-13).
 * Cubren los dos modos, las categorias, la unidad, el bloque de fotos, la
 * confirmacion de quitar y el servicio que ya no existe. La carga, el guardado
 * y la subida son de S3-T06.
 */
@RunWith(AndroidJUnit4::class)
class ServicioPantallaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun texto(@StringRes id: Int, vararg argumentos: Any): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *argumentos)

    private val categorias = listOf(
        Categoria(1, "Plomería", icono = "plumbing", orden = 10),
        Categoria(2, "Electricidad", icono = "bolt", orden = 20)
    )

    private fun guardada(posicion: Int) =
        FotoEnPantalla.Guardada(ServicioFoto("f$posicion", "serv-1", "", posicion, Instant.EPOCH))

    /** Lo que la pantalla pidio, para comprobar cada callback sin ViewModel. */
    private class Eventos {
        var categoria: Int? = null
        var cancelada: Int? = null
        var porQuitar: FotoEnPantalla? = null
        var quitarConfirmado = 0
        var regresar = 0
        var guardar = 0
    }

    private fun montarContenido(estado: EstadoServicio): Eventos {
        val eventos = Eventos()
        composeTestRule.setContent {
            DonChambitasTema {
                ServicioContenido(
                    estado = estado,
                    alCambiarTitulo = {},
                    alElegirCategoria = { eventos.categoria = it },
                    alCambiarDescripcion = {},
                    alCambiarPrecioDesde = {},
                    alCambiarPrecioHasta = {},
                    alCambiarUnidad = {},
                    alGuardar = { eventos.guardar++ },
                    alElegirDeGaleria = {},
                    alTomarFoto = {},
                    alCancelarSubida = { eventos.cancelada = it },
                    alPedirQuitarFoto = { eventos.porQuitar = it },
                    alConfirmarQuitarFoto = { eventos.quitarConfirmado++ },
                    alCancelarQuitarFoto = {},
                    alPedirIa = {},
                    alRegresar = { eventos.regresar++ }
                )
            }
        }
        return eventos
    }

    private fun boton(@StringRes id: Int) =
        composeTestRule.onNode(hasText(texto(id)) and hasClickAction())

    @Test
    fun crear_publicaConFotosDeshabilitadas() {
        val eventos = montarContenido(EstadoServicio(cargando = false))

        composeTestRule.onNodeWithText(texto(R.string.servicio_titulo_crear)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.servicio_fotos_despues)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.servicio_agregar_foto)).assertDoesNotExist()
        boton(R.string.servicio_publicar).performScrollTo().performClick()

        assertEquals(1, eventos.guardar)
    }

    @Test
    fun editar_guardaCambiosConElCuadroDeAgregar() {
        montarContenido(EstadoServicio(cargando = false, servicioId = "serv-1"))

        composeTestRule.onNodeWithText(texto(R.string.servicio_titulo_editar)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.servicio_fotos_contador, 0)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.servicio_agregar_foto)).assertIsDisplayed()
        boton(R.string.servicio_guardar_cambios).performScrollTo().assertIsEnabled()
        composeTestRule.onNodeWithText(texto(R.string.servicio_publicar)).assertDoesNotExist()
    }

    @Test
    fun categorias_sonSeleccionUnicaYAvisanLaElegida() {
        val eventos = montarContenido(EstadoServicio(cargando = false, categorias = categorias, categoriaId = 1))

        composeTestRule.onNodeWithText("Plomería").assertIsSelected()
        composeTestRule.onNodeWithText("Electricidad").assertIsNotSelected().performClick()

        assertEquals(2, eventos.categoria)
    }

    @Test
    fun unidad_soloSeHabilitaConAlgunPrecio() {
        montarContenido(EstadoServicio(cargando = false))
        composeTestRule.onNodeWithText(texto(R.string.servicio_unidad)).performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun unidad_conPrecio_estaHabilitada() {
        montarContenido(EstadoServicio(cargando = false, precioHasta = "800"))
        composeTestRule.onNodeWithText(texto(R.string.servicio_unidad)).performScrollTo().assertIsEnabled()
    }

    @Test
    fun tresFotos_quitanElCuadroDeAgregar() {
        montarContenido(
            EstadoServicio(cargando = false, servicioId = "serv-1", fotos = listOf(guardada(1), guardada(2), guardada(3)))
        )

        composeTestRule.onNodeWithText(texto(R.string.servicio_fotos_contador, 3)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.servicio_agregar_foto)).assertDoesNotExist()
    }

    @Test
    fun fotoSubiendo_bloqueaElBotonYPermiteCancelar() {
        val eventos = montarContenido(
            EstadoServicio(cargando = false, servicioId = "serv-1", fotos = listOf(guardada(1), FotoEnPantalla.Subiendo(2)))
        )

        boton(R.string.servicio_guardar_cambios).performScrollTo().assertIsNotEnabled()
        composeTestRule.onNodeWithText(texto(R.string.servicio_espera_fotos)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(texto(R.string.servicio_cancelar_subida)).performClick()

        assertEquals(2, eventos.cancelada)
    }

    @Test
    fun fotoGuardada_pideQuitarla() {
        val eventos = montarContenido(EstadoServicio(cargando = false, servicioId = "serv-1", fotos = listOf(guardada(1))))

        composeTestRule
            .onNodeWithContentDescription(texto(R.string.servicio_quitar_foto_n, 1))
            .performScrollTo()
            .performClick()

        assertEquals(1, eventos.porQuitar?.posicion)
    }

    @Test
    fun quitarFoto_confirmaConUnDialogo() {
        val foto = guardada(1)
        val eventos = montarContenido(
            EstadoServicio(cargando = false, servicioId = "serv-1", fotos = listOf(foto), fotoPorQuitar = foto)
        )

        composeTestRule.onNodeWithText(texto(R.string.servicio_quitar_foto_titulo)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.servicio_quitar_foto)).performClick()

        assertEquals(1, eventos.quitarConfirmado)
    }

    @Test
    fun servicioQueYaNoExiste_ofreceVolver() {
        val eventos = montarContenido(EstadoServicio(cargando = false, servicioId = "serv-borrado", noExiste = true))

        composeTestRule.onNodeWithText(texto(R.string.servicio_no_existe)).assertIsDisplayed()
        boton(R.string.servicio_volver).performClick()

        assertEquals(1, eventos.regresar)
    }

    @Test
    fun agregarFoto_abreLaHojaDeGaleriaOCamara() {
        montarContenido(EstadoServicio(cargando = false, servicioId = "serv-1"))

        composeTestRule.onNodeWithText(texto(R.string.servicio_agregar_foto)).performScrollTo().performClick()

        composeTestRule.onNodeWithText(texto(R.string.servicio_hoja_foto)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.foto_elegir_galeria)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.foto_tomar)).assertIsDisplayed()
    }

    @Test
    fun pantalla_filtraLosPreciosYHabilitaLaUnidad() {
        composeTestRule.setContent {
            DonChambitasTema { ServicioPantalla(alRegresar = {}, alGuardarCambios = {}, viewModel = viewModelSinRetraso()) }
        }

        composeTestRule.onNodeWithText(texto(R.string.servicio_precio_desde)).performScrollTo().performTextInput("12a.345")

        composeTestRule.onNodeWithText("12.34").assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.servicio_unidad)).assertIsEnabled()
    }
}

/** P-13 en modo crear contra los falsos, sin los 300 ms de retraso que la prueba no esperaria. */
private fun viewModelSinRetraso(): ServicioViewModel {
    val fuente = FuenteDatosFalsa()
    return ServicioViewModel(
        RepositorioServiciosFalso(fuente).apply { retrasoMs = 0 },
        RepositorioCatalogosFalso(fuente).apply { retrasoMs = 0 },
        RepositorioIaFalso().apply { retrasoMs = 0 },
        SavedStateHandle()
    )
}
