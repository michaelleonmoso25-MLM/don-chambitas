package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import mx.donchambitas.app.R
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioAuthFalso
import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas instrumentadas de la pantalla de inicio de sesion (P-02).
 * Cubren lo que la pantalla decide por si misma: anatomia, normalizacion del
 * correo al enviar, navegacion a P-03 y P-04, bloqueo durante la carga,
 * pintado de los errores que le llegan en el estado, cuando se valida (1.5)
 * y a donde entra segun la cuenta. La pantalla usa su ViewModel real contra la
 * fuente falsa en memoria; cada regla y cada resultado del repositorio se
 * prueban sin emulador en ValidacionesAuthTest e IniciarSesionViewModelTest.
 */
@RunWith(AndroidJUnit4::class)
class IniciarSesionPantallaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun texto(@StringRes id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    /**
     * El titulo de la barra superior y el boton dicen lo mismo, "Iniciar
     * sesion", asi que buscar por texto encuentra dos nodos. El boton es el
     * que se puede pulsar.
     */
    private fun botonIniciarSesion() = composeTestRule.onNode(
        hasText(texto(R.string.iniciar_sesion_accion)) and hasClickAction()
    )

    private fun montarPantalla(
        alNavegarADestino: (Ruta) -> Unit = {},
        alIrARegistro: () -> Unit = {},
        alIrARecuperarContrasena: () -> Unit = {}
    ) {
        val repositorio = RepositorioAuthFalso(FuenteDatosFalsa()).apply { retrasoMs = 0L }
        composeTestRule.setContent {
            DonChambitasTema {
                IniciarSesionPantalla(
                    alNavegarADestino = alNavegarADestino,
                    alIrARegistro = alIrARegistro,
                    alIrARecuperarContrasena = alIrARecuperarContrasena,
                    viewModel = IniciarSesionViewModel(repositorio)
                )
            }
        }
    }

    private fun montarContenido(
        estado: EstadoIniciarSesion,
        alReintentar: (() -> Unit)? = null
    ) {
        composeTestRule.setContent {
            DonChambitasTema {
                IniciarSesionContenido(
                    estado = estado,
                    alCambiarCorreo = {},
                    alCambiarContrasena = {},
                    alIniciarSesion = {},
                    alIrARegistro = {},
                    alIrARecuperarContrasena = {},
                    alReintentar = alReintentar
                )
            }
        }
    }

    @Test
    fun debeMostrarLaAnatomiaCompleta_cuandoSeAbreLaPantalla() {
        montarPantalla()

        composeTestRule.onNodeWithContentDescription(texto(R.string.splash_logo_descripcion))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.splash_nombre_app)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.auth_correo)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.auth_contrasena)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.iniciar_sesion_olvide)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.iniciar_sesion_ir_registro)).assertIsDisplayed()
        botonIniciarSesion().assertIsDisplayed()
    }

    /**
     * P-02 es la raiz del subgrafo de autenticacion: P-01 sale de la pila, asi
     * que no hay a donde volver y la flecha no debe existir.
     */
    @Test
    fun debeVenirSinFlechaDeRegreso_cuandoSeAbreLaPantalla() {
        montarPantalla()

        composeTestRule.onNodeWithContentDescription(texto(R.string.regresar))
            .assertDoesNotExist()
    }

    @Test
    fun debeEntrarAInicioCliente_cuandoLaCuentaExisteAunqueLleveMayusculasYEspacios() {
        var destinoRecibido: Ruta? = null
        montarPantalla(alNavegarADestino = { destinoRecibido = it })

        composeTestRule.onNodeWithText(texto(R.string.auth_correo))
            .performTextInput("  Juan.Perez@Ejemplo.COM  ")
        composeTestRule.onNodeWithText(texto(R.string.auth_contrasena))
            .performTextInput("secreta")
        botonIniciarSesion().performClick()

        composeTestRule.waitUntil { destinoRecibido != null }
        assertEquals(Ruta.InicioCliente, destinoRecibido)
    }

    @Test
    fun debeDecirCorreoOContrasenaYNoEntrar_cuandoLaCuentaNoExiste() {
        var destinoRecibido: Ruta? = null
        montarPantalla(alNavegarADestino = { destinoRecibido = it })

        composeTestRule.onNodeWithText(texto(R.string.auth_correo)).performTextInput("noexiste@ejemplo.com")
        composeTestRule.onNodeWithText(texto(R.string.auth_contrasena)).performTextInput("secreta")
        botonIniciarSesion().performClick()

        composeTestRule.onNodeWithText(texto(R.string.error_credenciales_invalidas)).assertIsDisplayed()
        composeTestRule.onNodeWithText("noexiste@ejemplo.com").assertIsDisplayed()
        assertNull("Con la cuenta rechazada no debe entrar", destinoRecibido)
    }

    @Test
    fun debeMarcarLosDosCamposYNoEnviar_cuandoSeEnviaVacio() {
        var destinoRecibido: Ruta? = null
        montarPantalla(alNavegarADestino = { destinoRecibido = it })

        botonIniciarSesion().performClick()

        composeTestRule.onNodeWithText(texto(R.string.validacion_correo_vacio)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.validacion_contrasena_vacia)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.auth_correo)).assertIsFocused()
        assertNull("Con errores no debe entrar", destinoRecibido)
    }

    /** En P-02 la contrasena solo se exige no vacia (5.2): el minimo de 8 es del registro. */
    @Test
    fun debeEntrar_cuandoLaContrasenaTieneUnSoloCaracter() {
        var destinoRecibido: Ruta? = null
        montarPantalla(alNavegarADestino = { destinoRecibido = it })

        composeTestRule.onNodeWithText(texto(R.string.auth_correo)).performTextInput("juan.perez@ejemplo.com")
        composeTestRule.onNodeWithText(texto(R.string.auth_contrasena)).performTextInput("1")
        botonIniciarSesion().performClick()

        composeTestRule.waitUntil { destinoRecibido != null }
        assertEquals(Ruta.InicioCliente, destinoRecibido)
    }

    @Test
    fun debeMarcarElCorreo_cuandoSaleDelCampoConUnFormatoQueNoSirve() {
        montarPantalla()

        composeTestRule.onNodeWithText(texto(R.string.auth_correo)).performTextInput("hola")
        composeTestRule.onNodeWithText(texto(R.string.auth_contrasena)).performClick()

        composeTestRule.onNodeWithText(texto(R.string.validacion_correo_formato)).assertIsDisplayed()
    }

    @Test
    fun debeNoMarcarNada_cuandoSePasaPorLosCamposSinEscribir() {
        montarPantalla()

        composeTestRule.onNodeWithText(texto(R.string.auth_correo)).performClick()
        composeTestRule.onNodeWithText(texto(R.string.auth_contrasena)).performClick()
        composeTestRule.onNodeWithText(texto(R.string.auth_correo)).performClick()

        composeTestRule.onNodeWithText(texto(R.string.validacion_correo_vacio)).assertDoesNotExist()
        composeTestRule.onNodeWithText(texto(R.string.validacion_contrasena_vacia)).assertDoesNotExist()
    }

    @Test
    fun debeNoMarcarError_mientrasSeEscribeElCorreo() {
        montarPantalla()

        composeTestRule.onNodeWithText(texto(R.string.auth_correo)).performTextInput("hola")

        composeTestRule.onNodeWithText(texto(R.string.validacion_correo_formato)).assertDoesNotExist()
    }

    /**
     * La conversion es solo para enviar: en pantalla se sigue viendo lo que el
     * usuario tecleo, por 1.6 de DISENO-AUTENTICACION.md.
     */
    @Test
    fun debeConservarLoTecleadoEnPantalla_cuandoElCorreoLlevaMayusculas() {
        montarPantalla()

        composeTestRule.onNodeWithText(texto(R.string.auth_correo))
            .performTextInput("Refugio@Ejemplo.MX")

        composeTestRule.onNodeWithText("Refugio@Ejemplo.MX", substring = true).assertIsDisplayed()
    }

    @Test
    fun debeLlevarARegistro_cuandoSeTocaElEnlaceDeAbajo() {
        var fueARegistro = false
        montarPantalla(alIrARegistro = { fueARegistro = true })

        composeTestRule.onNodeWithText(texto(R.string.iniciar_sesion_ir_registro)).performClick()

        assertTrue("El enlace del pie debe llevar a P-03", fueARegistro)
    }

    @Test
    fun debeLlevarARecuperarContrasena_cuandoSeTocaElEnlaceDeOlvide() {
        var fueARecuperar = false
        montarPantalla(alIrARecuperarContrasena = { fueARecuperar = true })

        composeTestRule.onNodeWithText(texto(R.string.iniciar_sesion_olvide)).performClick()

        assertTrue("El enlace debe llevar a P-04", fueARecuperar)
    }

    @Test
    fun debeMostrarElMensajeDeCadaCampo_cuandoElEstadoTraeErrores() {
        montarContenido(
            EstadoIniciarSesion(
                errorCorreo = R.string.validacion_correo_formato,
                errorContrasena = R.string.validacion_contrasena_vacia
            )
        )

        composeTestRule.onNodeWithText(texto(R.string.validacion_correo_formato)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.validacion_contrasena_vacia)).assertIsDisplayed()
    }

    /**
     * El boton no se comprueba con assertIsNotEnabled: mientras carga sustituye
     * su etiqueta por el indicador, asi que deja de existir un nodo pulsable con
     * ese texto. Que desaparezca es justo lo que pide 1.3, el cargando va dentro
     * del boton, y BotonPrincipal ya lo deshabilita con habilitado && !cargando.
     */
    @Test
    fun debeBloquearLaCapturaYLosEnlaces_cuandoEstaCargando() {
        montarContenido(EstadoIniciarSesion(cargando = true))

        composeTestRule.onNodeWithText(texto(R.string.auth_correo)).assertIsNotEnabled()
        composeTestRule.onNodeWithText(texto(R.string.auth_contrasena)).assertIsNotEnabled()
        composeTestRule.onNodeWithText(texto(R.string.iniciar_sesion_olvide)).assertIsNotEnabled()
        composeTestRule.onNodeWithText(texto(R.string.iniciar_sesion_ir_registro)).assertIsNotEnabled()
        botonIniciarSesion().assertDoesNotExist()
    }

    /**
     * HU-02 pide que nunca se diga cual de los dos datos fallo, y 2.4 sustituye
     * aqui el mensaje generico de AUTENTICACION de S1-T11.
     */
    @Test
    fun debeDecirCorreoOContrasenaSinReintentar_cuandoLasCredencialesSonRechazadas() {
        montarContenido(
            estado = EstadoIniciarSesion(errorPantalla = TipoError.AUTENTICACION),
            alReintentar = {}
        )

        composeTestRule.onNodeWithText(texto(R.string.error_credenciales_invalidas))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.reintentar)).assertDoesNotExist()
    }

    @Test
    fun debeOfrecerReintentar_cuandoElErrorEsDeRed() {
        montarContenido(
            estado = EstadoIniciarSesion(errorPantalla = TipoError.RED),
            alReintentar = {}
        )

        composeTestRule.onNodeWithText(texto(R.string.reintentar)).assertIsDisplayed()
    }

    /**
     * El error de pantalla se pinta en linea: el formulario sigue visible y
     * editable debajo, con lo que el usuario escribio.
     */
    @Test
    fun debeConservarElFormularioVisible_cuandoHayErrorDePantalla() {
        montarContenido(
            EstadoIniciarSesion(
                correo = "refugio@ejemplo.mx",
                contrasena = "12345678",
                errorPantalla = TipoError.RED
            )
        )

        composeTestRule.onNodeWithText(texto(R.string.auth_correo)).assertIsDisplayed()
        composeTestRule.onNodeWithText("refugio@ejemplo.mx", substring = true).assertIsDisplayed()
        botonIniciarSesion().assertIsDisplayed()
    }
}
