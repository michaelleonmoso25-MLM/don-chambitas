package mx.donchambitas.app.datos.remoto.supabase

import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

/**
 * Traduccion de los errores de Supabase Storage a TipoError (S2-T14,
 * CONTRATOS-API.md, "subirFotoPerfil").
 *
 * Storage contesta todos sus rechazos con HTTP 400 y el codigo real (403,
 * 409, 413, 415) como texto dentro del cuerpo, y storage-kt 3.0.3 no lo
 * recupera: desde aqui no se sabe el motivo. Por eso todo rechazo es
 * SERVIDOR. La aplicacion ya evita los cuatro: sube JPEG de 512 x 512, a su
 * propia carpeta y con un nombre nuevo cada vez.
 *
 * El mensaje es RestException.error y nunca RestException.message, que trae
 * las cabeceras de la peticion con el token (H-13).
 */
fun traducirErrorStorage(error: Throwable): Resultado.Error = when (error) {
    is HttpRequestException, is HttpRequestTimeoutException ->
        Resultado.Error(TipoError.RED, "Sin conexion con el servidor")
    is RestException -> Resultado.Error(TipoError.SERVIDOR, error.error)
    else -> Resultado.Error(TipoError.DESCONOCIDO, error::class.simpleName.orEmpty())
}
