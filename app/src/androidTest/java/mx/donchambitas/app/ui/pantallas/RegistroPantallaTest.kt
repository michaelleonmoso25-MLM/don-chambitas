package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import mx.donchambitas.app.R
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioAuthFalso
import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas instrumentadas de la pantalla de registro (P-03).
 * Cubren lo que la pantalla decide por si misma: seleccion de rol, filtrado
 * del telefono, bloqueo durante la carga, pintado de los errores que le
 * llegan en el estado, cuando se valida (1.5) y a donde entra una cuenta
 * nueva. La pantalla usa su ViewModel real contra la fuente falsa en memoria;
 * cada regla y cada resultado del repositorio se prueban sin emulador en
 * ValidacionesAuthTest y RegistroViewModelTest.
 */
@RunWith(AndroidJUnit4::class)
class RegistroPantallaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun texto(@StringRes id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    /**
     * El titulo de la barra superior y el boton dicen lo mismo, "Crear cuenta",
     * asi que buscar por texto encuentra dos nodos. El boton es el que se puede
     * pulsar.
     */
    private fun botonCrearCuenta() = composeTestRule.onNode(
        hasText(texto(R.string.registro_accion)) and hasClickAction()
    )

    private fun montarPantalla(alNavegarADestino: (Ruta) -> Unit = {}) {
        val repositorio = RepositorioAuthFalso(FuenteDatosFalsa()).apply { retrasoMs = 0L }
        composeTestRule.setContent {
            DonChambitasTema {
                RegistroPantalla(
                    alNavegarADestino = alNavegarADestino,
                    alRegresar = {},
                    alIrAIniciarSesion = {},
                    viewModel = RegistroViewModel(repositorio)
                )
            }
        }
    }

    private fun escribir(@StringRes etiqueta: Int, valor: String) {
        composeTestRule.onNodeWithText(texto(etiqueta)).performTextInput(valor)
    }

    private fun llenarCamposValidos() {
        escribir(R.string.registro_nombre, "Refugio")
        escribir(R.string.registro_apellidos, "Martínez Luna")
        escribir(R.string.auth_correo, "refugio@ejemplo.mx")
        escribir(R.string.auth_contrasena, "12345678")
        escribir(R.string.registro_telefono, "4771234567")
    }

    private fun montarContenido(estado: EstadoRegistro) {
        composeTestRule.setContent {
            DonChambitasTema {
                RegistroContenido(
                    estado = estado,
                    alElegirRol = {},
                    alCambiarNombre = {},
                    alCambiarApellidos = {},
                    alCambiarCorreo = {},
                    alCambiarContrasena = {},
                    alCambiarTelefono = {},
                    alRegistrar = {},
                    alRegresar = {},
                    alIrAIniciarSesion = {}
                )
            }
        }
    }

    @Test
    fun debeMostrarLosSeisCamposYLosDosRoles_cuandoSeAbreLaPantalla() {
        montarPantalla()

        composeTestRule.onNodeWithText(texto(R.string.registro_rol_cliente)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.registro_rol_trabajador)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.registro_nombre)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.registro_apellidos)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.auth_correo)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.auth_contrasena)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.registro_telefono)).assertIsDisplayed()
    }

    @Test
    fun debeVenirSinRolElegido_cuandoSeAbreLaPantalla() {
        montarPantalla()

        composeTestRule.onNodeWithText(texto(R.string.registro_rol_cliente)).assertIsNotSelected()
        composeTestRule.onNodeWithText(texto(R.string.registro_rol_trabajador)).assertIsNotSelected()
    }

    @Test
    fun debeDejarUnSoloRolSeleccionado_cuandoSeTocaElOtroChip() {
        montarPantalla()

        composeTestRule.onNodeWithText(texto(R.string.registro_rol_cliente)).performClick()
        composeTestRule.onNodeWithText(texto(R.string.registro_rol_cliente)).assertIsSelected()
        composeTestRule.onNodeWithText(texto(R.string.registro_rol_trabajador)).assertIsNotSelected()

        composeTestRule.onNodeWithText(texto(R.string.registro_rol_trabajador)).performClick()
        composeTestRule.onNodeWithText(texto(R.string.registro_rol_trabajador)).assertIsSelected()
        composeTestRule.onNodeWithText(texto(R.string.registro_rol_cliente)).assertIsNotSelected()
    }

    @Test
    fun debeReclamarElRolYNoEnviar_cuandoSeConfirmaSinElegirlo() {
        var destinoRecibido: Ruta? = null
        montarPantalla(alNavegarADestino = { destinoRecibido = it })

        botonCrearCuenta().performClick()

        composeTestRule.onNodeWithText(texto(R.string.validacion_rol_sin_elegir)).assertIsDisplayed()
        assertNull("Sin rol elegido no debe enviarse el registro", destinoRecibido)
    }

    @Test
    fun debeEntrarAInicioTrabajador_cuandoSeRegistraComoTrabajador() {
        var destinoRecibido: Ruta? = null
        montarPantalla(alNavegarADestino = { destinoRecibido = it })

        composeTestRule.onNodeWithText(texto(R.string.registro_rol_trabajador)).performClick()
        llenarCamposValidos()
        botonCrearCuenta().performClick()

        composeTestRule.waitUntil { destinoRecibido != null }
        assertEquals(Ruta.InicioTrabajador, destinoRecibido)
    }

    @Test
    fun debeAvisarQueElCorreoYaExisteYConservarLoCapturado_cuandoSeRepite() {
        var destinoRecibido: Ruta? = null
        montarPantalla(alNavegarADestino = { destinoRecibido = it })

        composeTestRule.onNodeWithText(texto(R.string.registro_rol_cliente)).performClick()
        escribir(R.string.registro_nombre, "Juan")
        escribir(R.string.registro_apellidos, "Pérez")
        escribir(R.string.auth_correo, "juan.perez@ejemplo.com")
        escribir(R.string.auth_contrasena, "12345678")
        escribir(R.string.registro_telefono, "4771234567")
        botonCrearCuenta().performClick()

        composeTestRule.onNodeWithText("El correo ya esta registrado, inicia sesion").assertExists()
        composeTestRule.onNodeWithText("juan.perez@ejemplo.com").assertExists()
        assertNull("Con el correo repetido no debe entrar", destinoRecibido)
    }

    @Test
    fun debeMarcarLosCincoCamposYNoEnviar_cuandoSeEnviaConRolYTodoVacio() {
        var destinoRecibido: Ruta? = null
        montarPantalla(alNavegarADestino = { destinoRecibido = it })

        composeTestRule.onNodeWithText(texto(R.string.registro_rol_cliente)).performClick()
        botonCrearCuenta().performClick()

        listOf(
            R.string.validacion_nombre_vacio,
            R.string.validacion_apellidos_vacio,
            R.string.validacion_correo_vacio,
            R.string.validacion_contrasena_corta,
            R.string.validacion_telefono_vacio
        ).forEach { composeTestRule.onNodeWithText(texto(it)).assertExists() }
        composeTestRule.onNodeWithText(texto(R.string.registro_nombre)).assertIsFocused()
        assertNull("Con errores no debe enviarse el registro", destinoRecibido)
    }

    @Test
    fun debeNoEnviar_cuandoLaContrasenaTieneSieteCaracteres() {
        var destinoRecibido: Ruta? = null
        montarPantalla(alNavegarADestino = { destinoRecibido = it })

        composeTestRule.onNodeWithText(texto(R.string.registro_rol_cliente)).performClick()
        escribir(R.string.registro_nombre, "Refugio")
        escribir(R.string.registro_apellidos, "Martínez Luna")
        escribir(R.string.auth_correo, "refugio@ejemplo.mx")
        escribir(R.string.auth_contrasena, "1234567")
        escribir(R.string.registro_telefono, "4771234567")
        botonCrearCuenta().performClick()

        composeTestRule.onNodeWithText(texto(R.string.validacion_contrasena_corta)).assertExists()
        composeTestRule.onNodeWithText(texto(R.string.auth_contrasena)).assertIsFocused()
        assertNull("Con la contrasena corta no debe enviarse el registro", destinoRecibido)
    }

    @Test
    fun debeMarcarElTelefono_cuandoSaleDelCampoConMenosDeDiezDigitos() {
        montarPantalla()

        escribir(R.string.registro_telefono, "55123")
        composeTestRule.onNodeWithText(texto(R.string.registro_nombre)).performClick()

        composeTestRule.onNodeWithText(texto(R.string.validacion_telefono_digitos)).assertExists()
    }

    @Test
    fun debeNoMarcarNada_cuandoSePasaPorUnCampoSinEscribir() {
        montarPantalla()

        composeTestRule.onNodeWithText(texto(R.string.registro_nombre)).performClick()
        composeTestRule.onNodeWithText(texto(R.string.registro_apellidos)).performClick()

        composeTestRule.onNodeWithText(texto(R.string.validacion_nombre_vacio)).assertDoesNotExist()
    }

    @Test
    fun debeDescartarLoQueNoSeaDigito_cuandoSeEscribeElTelefono() {
        montarPantalla()

        composeTestRule.onNodeWithText(texto(R.string.registro_telefono))
            .performTextInput("477-123 45ab67")

        composeTestRule.onNodeWithText("4771234567", substring = true).assertIsDisplayed()
    }

    @Test
    fun debeCortarEnDiezDigitos_cuandoSeEscribeUnTelefonoMasLargo() {
        montarPantalla()

        composeTestRule.onNodeWithText(texto(R.string.registro_telefono))
            .performTextInput("47712345678901")

        composeTestRule.onNodeWithText("4771234567", substring = true).assertIsDisplayed()
    }

    @Test
    fun debeMostrarElMensajeDeCadaCampo_cuandoElEstadoTraeErrores() {
        montarContenido(
            EstadoRegistro(
                errorRol = R.string.validacion_rol_sin_elegir,
                errorNombre = R.string.validacion_nombre_vacio,
                errorApellidos = R.string.validacion_apellidos_vacio,
                errorCorreo = R.string.validacion_correo_formato,
                errorContrasena = R.string.validacion_contrasena_corta,
                errorTelefono = R.string.validacion_telefono_digitos
            )
        )

        composeTestRule.onNodeWithText(texto(R.string.validacion_rol_sin_elegir)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.validacion_nombre_vacio)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.validacion_apellidos_vacio)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.validacion_correo_formato)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.validacion_contrasena_corta)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.validacion_telefono_digitos)).assertIsDisplayed()
    }

    @Test
    fun debeBloquearLaCapturaYElBoton_cuandoEstaCargando() {
        montarContenido(EstadoRegistro(rol = RolUsuario.CLIENTE, cargando = true))

        composeTestRule.onNodeWithText(texto(R.string.registro_nombre)).assertIsNotEnabled()
        composeTestRule.onNodeWithText(texto(R.string.auth_correo)).assertIsNotEnabled()
        composeTestRule.onNodeWithText(texto(R.string.registro_telefono)).assertIsNotEnabled()
        composeTestRule.onNodeWithText(texto(R.string.registro_ir_iniciar_sesion)).assertIsNotEnabled()
    }

    @Test
    fun debeMostrarElMensajeDeLaBaseSinReintentar_cuandoElErrorEsDeValidacion() {
        val mensajeDeLaBase = "El correo ya está registrado, inicia sesión"
        montarContenido(
            EstadoRegistro(
                errorPantalla = TipoError.VALIDACION,
                mensajePantalla = mensajeDeLaBase
            )
        )

        composeTestRule.onNodeWithText(mensajeDeLaBase).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.reintentar)).assertDoesNotExist()
    }

    @Test
    fun debeSeguirMostrandoElFormulario_cuandoHayErrorDeRed() {
        montarContenido(
            EstadoRegistro(
                nombre = "Refugio",
                errorPantalla = TipoError.RED
            )
        )

        composeTestRule.onNodeWithText(texto(R.string.error_red)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Refugio", substring = true).assertIsDisplayed()
        botonCrearCuenta().assertIsDisplayed()
    }
}
