package mx.donchambitas.app.ui.pantallas

import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.util.TipoError

/**
 * Estado inmutable de la pantalla de bienvenida (P-01 Splash).
 * Conforme a CONVENCIONES.md (prefijo Estado... y data class).
 *
 * @property cargando Indica si la verificación y el tiempo mínimo en pantalla siguen en curso.
 * @property destino Ruta hacia la que debe navegarse una vez resuelto el estado de sesión.
 * @property error Por qué no se pudo saber si hay sesión; la pantalla ofrece reintentar.
 */
data class EstadoSplash(
    val cargando: Boolean = true,
    val destino: Ruta? = null,
    val error: TipoError? = null
)
