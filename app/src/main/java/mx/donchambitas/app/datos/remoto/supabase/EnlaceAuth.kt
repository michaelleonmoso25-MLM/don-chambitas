package mx.donchambitas.app.datos.remoto.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.parseSessionFromFragment
import io.github.jan.supabase.auth.status.SessionSource
import kotlinx.coroutines.CancellationException

/**
 * Decide si un enlace que abrio la aplicacion trae una sesion que importar.
 *
 * Un enlace vencido o ya usado llega con `error=` y sin `access_token`, y un
 * fragmento con una parte sin `=` rompe el analisis de supabase-kt 3.0.3 con
 * una excepcion que nadie atrapa. Los dos casos se descartan aqui, antes de
 * tocar la biblioteca.
 */
fun esEnlaceConSesion(esquema: String?, host: String?, fragmento: String?): Boolean {
    if (esquema != ConfiguracionSupabase.ESQUEMA_ENLACE || host != ConfiguracionSupabase.HOST_ENLACE) return false
    if (fragmento.isNullOrBlank()) return false
    val partes = fragmento.split("&")
    if (partes.any { '=' !in it }) return false
    val llaves = partes.map { it.substringBefore('=') }.toSet()
    return "access_token" in llaves && "error" !in llaves
}

/**
 * Decide si el enlace es el del correo de recuperacion: Supabase lo marca con
 * `type=recovery` en el fragmento, que con el flujo IMPLICIT llega tal cual
 * (CONTRATOS-API.md, "El recorrido completo"). Con eso la aplicacion aterriza
 * en P-18 en vez de quedarse donde estaba (DEC-27).
 */
fun esEnlaceDeRecuperacion(fragmento: String?): Boolean {
    if (fragmento.isNullOrBlank()) return false
    return fragmento.split("&").any { parte ->
        parte.substringBefore('=') == "type" && parte.substringAfter('=', "") == "recovery"
    }
}

/**
 * Importa la sesion que trae el fragmento del enlace (flujo IMPLICIT).
 *
 * Hace lo mismo que `handleDeeplinks` de supabase-kt 3.0.3, pero dentro de un
 * try: aquella lee el usuario en un scope propio sin manejador de errores, y
 * abrir el enlace sin red cerraba la aplicacion.
 *
 * @return true si la sesion quedo importada.
 */
suspend fun SupabaseClient.importarSesionDeEnlace(fragmento: String): Boolean =
    try {
        val sesion = auth.parseSessionFromFragment(fragmento)
        val usuario = auth.retrieveUser(sesion.accessToken)
        auth.importSession(sesion.copy(user = usuario), source = SessionSource.External)
        true
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        false
    }
