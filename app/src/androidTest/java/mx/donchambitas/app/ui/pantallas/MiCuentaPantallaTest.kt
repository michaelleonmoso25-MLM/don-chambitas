package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.modelo.Usuario
import mx.donchambitas.app.ui.tema.DonChambitasTema
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * Pruebas instrumentadas de Mi cuenta (P-18) sobre el contenido puro: que
 * accesos ve cada rol, que el correo y el rol no se editan, y el dialogo de
 * cierre. La logica se prueba sin emulador en MiCuentaViewModelTest.
 */
@RunWith(AndroidJUnit4::class)
class MiCuentaPantallaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun texto(@StringRes id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private fun usuario(rol: RolUsuario) = Usuario(
        id = "usr-prueba",
        correo = "refugio@ejemplo.mx",
        nombre = "Refugio",
        apellidos = "Martinez Luna",
        telefono = "4771234567",
        rol = rol,
        creadoEn = Instant.EPOCH,
        actualizadoEn = Instant.EPOCH
    )

    private fun montar(
        estado: EstadoMiCuenta,
        alAbrirSeccion: (SeccionMiCuenta) -> Unit = {},
        alCerrarSesion: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            DonChambitasTema {
                MiCuentaContenido(
                    estado = estado,
                    alReintentar = {},
                    alAbrirSeccion = alAbrirSeccion,
                    alCancelar = {},
                    alCambiarNombre = {},
                    alCambiarApellidos = {},
                    alCambiarTelefono = {},
                    alGuardarDatos = {},
                    alCambiarContrasena = {},
                    alGuardarContrasena = {},
                    alCerrarSesion = alCerrarSesion,
                    alConsumirMensaje = {},
                    alIrAMiPerfil = {},
                    alIrAMisPostulaciones = {}
                )
            }
        }
    }

    @Test
    fun clienteVeSuFichaSinAccesosDeTrabajador() {
        montar(EstadoMiCuenta(usuario = usuario(RolUsuario.CLIENTE), cargando = false))

        composeTestRule.onNodeWithText("Refugio Martinez Luna").assertIsDisplayed()
        composeTestRule.onNodeWithText("refugio@ejemplo.mx").assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.mi_cuenta_rol_cliente)).assertIsDisplayed()
        composeTestRule.onNodeWithText("RM").assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.mi_cuenta_mi_perfil)).assertDoesNotExist()
        composeTestRule.onNodeWithText(texto(R.string.mi_cuenta_mis_postulaciones)).assertDoesNotExist()
    }

    @Test
    fun trabajadorVeSusDosAccesos() {
        montar(EstadoMiCuenta(usuario = usuario(RolUsuario.TRABAJADOR), cargando = false))

        composeTestRule.onNodeWithText(texto(R.string.mi_cuenta_rol_trabajador)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.mi_cuenta_mi_perfil)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.mi_cuenta_mis_postulaciones)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun laEdicionNoTraeCorreoNiRol() {
        montar(
            EstadoMiCuenta(
                usuario = usuario(RolUsuario.CLIENTE),
                cargando = false,
                seccion = SeccionMiCuenta.DATOS,
                nombre = "Refugio",
                apellidos = "Martinez Luna",
                telefono = "4771234567"
            )
        )

        composeTestRule.onNodeWithText(texto(R.string.registro_nombre)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.registro_telefono)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.auth_correo)).assertDoesNotExist()
        composeTestRule.onNodeWithText("refugio@ejemplo.mx").assertDoesNotExist()
        composeTestRule.onNodeWithText(texto(R.string.mi_cuenta_rol_cliente)).assertDoesNotExist()
    }

    @Test
    fun editarDatosAbreSuSeccion() {
        var abierta: SeccionMiCuenta? = null
        montar(
            EstadoMiCuenta(usuario = usuario(RolUsuario.CLIENTE), cargando = false),
            alAbrirSeccion = { abierta = it }
        )

        composeTestRule.onNodeWithText(texto(R.string.mi_cuenta_editar_datos)).performClick()

        assertTrue(abierta == SeccionMiCuenta.DATOS)
    }

    @Test
    fun tocarElAvatarOfreceGaleriaYCamara() {
        var galeria = 0
        composeTestRule.setContent {
            DonChambitasTema {
                MiCuentaContenido(
                    estado = EstadoMiCuenta(usuario = usuario(RolUsuario.CLIENTE), cargando = false),
                    alReintentar = {},
                    alAbrirSeccion = {},
                    alCancelar = {},
                    alCambiarNombre = {},
                    alCambiarApellidos = {},
                    alCambiarTelefono = {},
                    alGuardarDatos = {},
                    alCambiarContrasena = {},
                    alGuardarContrasena = {},
                    alCerrarSesion = {},
                    alConsumirMensaje = {},
                    alIrAMiPerfil = {},
                    alIrAMisPostulaciones = {},
                    alElegirDeGaleria = { galeria++ }
                )
            }
        }

        composeTestRule.onNodeWithText("RM").performClick()
        composeTestRule.onNodeWithText(texto(R.string.foto_tomar)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.foto_elegir_galeria)).performClick()

        assertTrue(galeria == 1)
    }

    @Test
    fun cerrarSesionPideConfirmacionAntesDeLlamar() {
        var cierres = 0
        montar(
            EstadoMiCuenta(usuario = usuario(RolUsuario.CLIENTE), cargando = false),
            alCerrarSesion = { cierres++ }
        )

        composeTestRule.onNode(hasText(texto(R.string.mi_cuenta_cerrar_sesion)) and hasClickAction())
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithText(texto(R.string.mi_cuenta_confirmar_cierre)).assertIsDisplayed()
        assertTrue(cierres == 0)

        // Con el dialogo abierto hay dos "Cerrar sesion" que se pueden pulsar:
        // el de la pantalla y el de confirmar, que es el ultimo en el arbol.
        composeTestRule.onAllNodes(hasText(texto(R.string.mi_cuenta_cerrar_sesion)) and hasClickAction())
            .onLast()
            .performClick()

        assertTrue(cierres == 1)
    }
}
