package mx.donchambitas.app.datos.remoto.supabase

import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

private const val SIN_AUTORIZACION = 401
private const val PROHIBIDO = 403
private const val PETICION_INVALIDA = 400
private const val CONFLICTO = 409
private val CODIGOS_DE_SERVIDOR = 500..599

/**
 * Traduccion de los errores de PostgREST a TipoError.
 * Es la tabla de errores de la seccion "Usuario" de
 * docs/tecnico/CONTRATOS-API.md; si una cambia, la otra cambia igual.
 *
 * Se clasifica por el codigo HTTP porque supabase-kt 3.0.3 no deja pasar el
 * codigo de Postgres (42501, 22001): PostgrestImpl arma la excepcion solo con
 * message, details y hint.
 *
 * El mensaje es RestException.error, el texto del servidor, y nunca
 * RestException.message: ese incluye las cabeceras de la peticion, con el
 * token de la sesion.
 */
fun traducirErrorDatos(error: Throwable): Resultado.Error = when {
    error is HttpRequestException || error is HttpRequestTimeoutException ->
        Resultado.Error(TipoError.RED, "Sin conexion con el servidor")
    error is RestException -> Resultado.Error(
        tipo = when (error.statusCode) {
            SIN_AUTORIZACION, PROHIBIDO -> TipoError.AUTENTICACION
            PETICION_INVALIDA, CONFLICTO -> TipoError.VALIDACION
            in CODIGOS_DE_SERVIDOR -> TipoError.SERVIDOR
            else -> TipoError.DESCONOCIDO
        },
        mensaje = error.error
    )
    else -> Resultado.Error(TipoError.DESCONOCIDO, error::class.simpleName.orEmpty())
}
