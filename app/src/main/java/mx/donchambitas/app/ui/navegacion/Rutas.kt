package mx.donchambitas.app.ui.navegacion

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

const val ARG_TRABAJADOR_ID: String = "trabajadorId"
const val ARG_SERVICIO_ID: String = "servicioId"
const val ARG_CONVERSACION_ID: String = "conversacionId"
const val ARG_SOLICITUD_ID: String = "solicitudId"

/**
 * Subgrafos de navegación definidos según ARQUITECTURA.md.
 */
sealed class Subgrafo(val ruta: String, val nombre: String) {
    data object Autenticacion : Subgrafo("subgrafo_autenticacion", "Autenticación")
    data object Cliente : Subgrafo("subgrafo_cliente", "Cliente")
    data object Trabajador : Subgrafo("subgrafo_trabajador", "Trabajador")
}

/**
 * Clase sellada con las 19 rutas de la aplicación Don Chambitas según PANTALLAS.md.
 * Ninguna cadena de ruta suelta debe existir fuera de este archivo.
 */
sealed class Ruta(
    val ruta: String,
    val idPantalla: String,
    val titulo: String,
    val argumentos: List<NamedNavArgument> = emptyList()
) {
    // Autenticación (P-01 a P-04)
    data object Splash : Ruta(
        ruta = "splash",
        idPantalla = "P-01",
        titulo = "Splash"
    )

    data object IniciarSesion : Ruta(
        ruta = "iniciar_sesion",
        idPantalla = "P-02",
        titulo = "Iniciar sesión"
    )

    data object Registro : Ruta(
        ruta = "registro",
        idPantalla = "P-03",
        titulo = "Registro"
    )

    data object RecuperarContrasena : Ruta(
        ruta = "recuperar_contrasena",
        idPantalla = "P-04",
        titulo = "Recuperar contraseña"
    )

    // Cliente (P-05 a P-09)
    data object InicioCliente : Ruta(
        ruta = "inicio_cliente",
        idPantalla = "P-05",
        titulo = "Inicio cliente"
    )

    data object Resultados : Ruta(
        ruta = "resultados",
        idPantalla = "P-06",
        titulo = "Resultados"
    )

    data object PerfilTrabajador : Ruta(
        ruta = "perfil_trabajador/{$ARG_TRABAJADOR_ID}",
        idPantalla = "P-07",
        titulo = "Perfil público del trabajador",
        argumentos = listOf(
            navArgument(ARG_TRABAJADOR_ID) {
                type = NavType.StringType
            }
        )
    ) {
        const val ARG_TRABAJADOR_ID = mx.donchambitas.app.ui.navegacion.ARG_TRABAJADOR_ID
        const val RUTA_BASE = "perfil_trabajador"
        fun crearRuta(trabajadorId: String): String = "$RUTA_BASE/$trabajadorId"
    }

    data object PublicarSolicitud : Ruta(
        ruta = "publicar_solicitud",
        idPantalla = "P-08",
        titulo = "Publicar solicitud"
    )

    data object MisSolicitudes : Ruta(
        ruta = "mis_solicitudes",
        idPantalla = "P-09",
        titulo = "Mis solicitudes"
    )

    // Trabajador (P-10 a P-14)
    data object InicioTrabajador : Ruta(
        ruta = "inicio_trabajador",
        idPantalla = "P-10",
        titulo = "Inicio trabajador"
    )

    data object MiPerfil : Ruta(
        ruta = "mi_perfil",
        idPantalla = "P-11",
        titulo = "Mi perfil"
    )

    data object MisServicios : Ruta(
        ruta = "mis_servicios",
        idPantalla = "P-12",
        titulo = "Mis servicios"
    )

    data object CrearEditarServicio : Ruta(
        ruta = "crear_editar_servicio?$ARG_SERVICIO_ID={$ARG_SERVICIO_ID}",
        idPantalla = "P-13",
        titulo = "Crear o editar servicio",
        argumentos = listOf(
            navArgument(ARG_SERVICIO_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) {
        const val ARG_SERVICIO_ID = mx.donchambitas.app.ui.navegacion.ARG_SERVICIO_ID
        const val RUTA_BASE = "crear_editar_servicio"
        fun crearRuta(servicioId: String? = null): String =
            if (servicioId != null) "$RUTA_BASE?$ARG_SERVICIO_ID=$servicioId" else RUTA_BASE
    }

    data object MisPostulaciones : Ruta(
        ruta = "mis_postulaciones",
        idPantalla = "P-14",
        titulo = "Mis postulaciones"
    )

    // Compartidas (P-15 a P-19)
    data object Conversaciones : Ruta(
        ruta = "conversaciones",
        idPantalla = "P-15",
        titulo = "Conversaciones"
    )

    data object Chat : Ruta(
        ruta = "chat/{$ARG_CONVERSACION_ID}",
        idPantalla = "P-16",
        titulo = "Chat",
        argumentos = listOf(
            navArgument(ARG_CONVERSACION_ID) {
                type = NavType.StringType
            }
        )
    ) {
        const val ARG_CONVERSACION_ID = mx.donchambitas.app.ui.navegacion.ARG_CONVERSACION_ID
        const val RUTA_BASE = "chat"
        fun crearRuta(conversacionId: String): String = "$RUTA_BASE/$conversacionId"
    }

    data object DejarResena : Ruta(
        ruta = "dejar_resena/{$ARG_SOLICITUD_ID}",
        idPantalla = "P-17",
        titulo = "Dejar reseña",
        argumentos = listOf(
            navArgument(ARG_SOLICITUD_ID) {
                type = NavType.StringType
            }
        )
    ) {
        const val ARG_SOLICITUD_ID = mx.donchambitas.app.ui.navegacion.ARG_SOLICITUD_ID
        const val RUTA_BASE = "dejar_resena"
        fun crearRuta(solicitudId: String): String = "$RUTA_BASE/$solicitudId"
    }

    data object MiCuenta : Ruta(
        ruta = "mi_cuenta",
        idPantalla = "P-18",
        titulo = "Mi cuenta"
    )

    data object DetalleSolicitud : Ruta(
        ruta = "detalle_solicitud/{$ARG_SOLICITUD_ID}",
        idPantalla = "P-19",
        titulo = "Detalle de solicitud",
        argumentos = listOf(
            navArgument(ARG_SOLICITUD_ID) {
                type = NavType.StringType
            }
        )
    ) {
        const val ARG_SOLICITUD_ID = mx.donchambitas.app.ui.navegacion.ARG_SOLICITUD_ID
        const val RUTA_BASE = "detalle_solicitud"
        fun crearRuta(solicitudId: String): String = "$RUTA_BASE/$solicitudId"
    }
}

/**
 * Estado de la sesión real que leen las guardas y las barras (S2-T15). Lo
 * lleva [SesionViewModel]. VERIFICANDO es el arranque, mientras Auth no ha
 * dicho si hay sesión: ahí ninguna guarda redirige, porque decide P-01.
 */
enum class EstadoSesion {
    VERIFICANDO,
    SIN_SESION,
    CLIENTE,
    TRABAJADOR
}

/**
 * Colección de todas las 19 rutas de la aplicación.
 */
val TODAS_LAS_RUTAS: List<Ruta> = listOf(
    Ruta.Splash,
    Ruta.IniciarSesion,
    Ruta.Registro,
    Ruta.RecuperarContrasena,
    Ruta.InicioCliente,
    Ruta.Resultados,
    Ruta.PerfilTrabajador,
    Ruta.PublicarSolicitud,
    Ruta.MisSolicitudes,
    Ruta.InicioTrabajador,
    Ruta.MiPerfil,
    Ruta.MisServicios,
    Ruta.CrearEditarServicio,
    Ruta.MisPostulaciones,
    Ruta.Conversaciones,
    Ruta.Chat,
    Ruta.DejarResena,
    Ruta.MiCuenta,
    Ruta.DetalleSolicitud
)

/**
 * Rutas pertenecientes al subgrafo de autenticación.
 */
val RUTAS_AUTENTICACION: Set<Ruta> = setOf(
    Ruta.Splash,
    Ruta.IniciarSesion,
    Ruta.Registro,
    Ruta.RecuperarContrasena
)

/**
 * Rutas exclusivas del cliente.
 */
val RUTAS_CLIENTE: Set<Ruta> = setOf(
    Ruta.InicioCliente,
    Ruta.Resultados,
    Ruta.PerfilTrabajador,
    Ruta.PublicarSolicitud,
    Ruta.MisSolicitudes
)

/**
 * Rutas exclusivas del trabajador.
 */
val RUTAS_TRABAJADOR: Set<Ruta> = setOf(
    Ruta.InicioTrabajador,
    Ruta.MiPerfil,
    Ruta.MisServicios,
    Ruta.CrearEditarServicio,
    Ruta.MisPostulaciones
)

/**
 * Rutas compartidas accesibles por ambos roles.
 */
val RUTAS_COMPARTIDAS: Set<Ruta> = setOf(
    Ruta.Conversaciones,
    Ruta.Chat,
    Ruta.DejarResena,
    Ruta.MiCuenta,
    Ruta.DetalleSolicitud
)

/**
 * Las 8 pantallas que se abren encima y no muestran barra inferior según PANTALLAS.md:
 * P-04, P-06, P-07, P-08, P-13, P-16, P-17, P-19.
 */
val PANTALLAS_ENCIMA: Set<Ruta> = setOf(
    Ruta.RecuperarContrasena,
    Ruta.Resultados,
    Ruta.PerfilTrabajador,
    Ruta.PublicarSolicitud,
    Ruta.CrearEditarServicio,
    Ruta.Chat,
    Ruta.DejarResena,
    Ruta.DetalleSolicitud
)

/**
 * Encuentra el objeto [Ruta] correspondiente a partir de una ruta de navegación o patrón.
 */
fun encontrarRuta(rutaOUrl: String): Ruta? {
    val rutaLimpia = rutaOUrl.substringBefore('?').substringBefore('/')
    return TODAS_LAS_RUTAS.firstOrNull { ruta ->
        ruta.ruta == rutaOUrl ||
        ruta.ruta.substringBefore('?').substringBefore('/') == rutaLimpia
    }
}

/**
 * Resuelve las guardas de navegación.
 * - Verificando: no redirige nada, P-01 todavía decide.
 * - Sin sesión: cualquier ruta fuera de autenticación redirige a P-02 (Iniciar sesión).
 * - Con sesión de cliente: las rutas de trabajador redirigen a P-05 (Inicio cliente).
 * - Con sesión de trabajador: las rutas de cliente redirigen a P-10 (Inicio trabajador).
 * - Con cualquier sesión: P-02, P-03 y P-04 redirigen al inicio del rol, para
 *   no entrar con otra cuenta encima de la abierta. P-01 siempre pasa.
 *
 * Devuelve la ruta a redirigir, o null si la navegación está permitida.
 */
fun resolverGuarda(
    rutaDestino: String,
    estadoSesion: EstadoSesion
): String? {
    val ruta = encontrarRuta(rutaDestino) ?: return null
    val esFormularioDeEntrada = ruta in RUTAS_AUTENTICACION && ruta != Ruta.Splash
    return when (estadoSesion) {
        EstadoSesion.VERIFICANDO -> null
        EstadoSesion.SIN_SESION -> {
            if (RUTAS_AUTENTICACION.contains(ruta)) null else Ruta.IniciarSesion.ruta
        }
        EstadoSesion.CLIENTE -> {
            if (esFormularioDeEntrada || RUTAS_TRABAJADOR.contains(ruta)) Ruta.InicioCliente.ruta else null
        }
        EstadoSesion.TRABAJADOR -> {
            if (esFormularioDeEntrada || RUTAS_CLIENTE.contains(ruta)) Ruta.InicioTrabajador.ruta else null
        }
    }
}

/**
 * Determina si debe mostrarse la barra inferior para una ruta y estado de sesión dados.
 * - Verificando, sin sesión o rutas de autenticación: no muestra barra.
 * - Las 8 pantallas que van encima: no muestran barra.
 * - Pantallas principales con sesión activa: muestran su barra correspondiente.
 */
fun debeMostrarBarraInferior(
    rutaDestino: String?,
    estadoSesion: EstadoSesion
): Boolean {
    if (rutaDestino == null) return false
    if (estadoSesion == EstadoSesion.VERIFICANDO || estadoSesion == EstadoSesion.SIN_SESION) return false
    val ruta = encontrarRuta(rutaDestino) ?: return false
    if (PANTALLAS_ENCIMA.contains(ruta)) return false
    if (RUTAS_AUTENTICACION.contains(ruta)) return false
    return true
}
