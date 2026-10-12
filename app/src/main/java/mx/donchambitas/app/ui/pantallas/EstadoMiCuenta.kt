package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import mx.donchambitas.app.dominio.modelo.Usuario
import mx.donchambitas.app.util.TipoError

/** Lo que P-18 tiene desplegado. Una sección a la vez (decisión 1 de S2-T11). */
enum class SeccionMiCuenta { NINGUNA, DATOS, CONTRASENA }

/** Campos de la sección de datos, en orden visual: el foco salta al primero que falle. */
enum class CampoMiCuenta { NOMBRE, APELLIDOS, TELEFONO }

/**
 * Aviso de una sola vez de P-18, que se pinta en un Snackbar: un texto fijo
 * (un éxito o el motivo de rechazar una foto), o un error con su TipoError,
 * que la pantalla traduce igual que en P-02 y P-03.
 */
sealed interface MensajeMiCuenta {
    data class Texto(@StringRes val texto: Int) : MensajeMiCuenta
    data class Error(val tipo: TipoError) : MensajeMiCuenta
}

/**
 * Estado inmutable de Mi cuenta (P-18).
 * Especificado en docs/tareas/S2-T11.md.
 *
 * @property usuario La ficha de public.usuarios. Nula mientras carga o si no se pudo leer.
 * @property errorCarga Por qué no se pudo leer la ficha; la pantalla ofrece reintentar.
 * @property guardando Una operación de datos o de contraseña va en camino.
 * @property cerrandoSesion Se pidió cerrar sesión. Si sale bien, quien navega es SesionViewModel.
 * @property subiendoFoto La foto recortada va camino a subirFotoPerfil (S2-T13).
 */
data class EstadoMiCuenta(
    val usuario: Usuario? = null,
    val cargando: Boolean = true,
    val errorCarga: TipoError? = null,
    val seccion: SeccionMiCuenta = SeccionMiCuenta.NINGUNA,
    val nombre: String = "",
    val apellidos: String = "",
    val telefono: String = "",
    @StringRes val errorNombre: Int? = null,
    @StringRes val errorApellidos: Int? = null,
    @StringRes val errorTelefono: Int? = null,
    val contrasenaNueva: String = "",
    @StringRes val errorContrasena: Int? = null,
    val guardando: Boolean = false,
    val cerrandoSesion: Boolean = false,
    val subiendoFoto: Boolean = false,
    val mensaje: MensajeMiCuenta? = null
)
