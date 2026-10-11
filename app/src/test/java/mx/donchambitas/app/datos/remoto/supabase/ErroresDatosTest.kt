package mx.donchambitas.app.datos.remoto.supabase

import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestBuilder
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Una prueba por fila de la tabla de errores de la seccion "Usuario" de
 * docs/tecnico/CONTRATOS-API.md.
 */
class ErroresDatosTest {

    // Asi arma supabase-kt 3.0.3 el texto de la excepcion: con las cabeceras.
    private val mensajeConCabeceras = """
        permission denied for table usuarios
        URL: https://proyecto.supabase.co/rest/v1/usuarios
        Headers: [Authorization=[Bearer token-secreto], apikey=[llave-anon]]
        Http Method: PATCH
    """.trimIndent()

    private fun rest(estado: Int, texto: String = "mensaje de PostgREST") =
        RestException(texto, null, estado, mensajeConCabeceras)

    private fun tipoDe(error: Throwable) = traducirErrorDatos(error).tipo

    @Test
    fun debeSerRed_cuandoNoHayConexion() {
        assertEquals(TipoError.RED, tipoDe(HttpRequestException("sin red", HttpRequestBuilder())))
    }

    @Test
    fun debeSerRed_cuandoSeAgotaElTiempo() {
        assertEquals(TipoError.RED, tipoDe(HttpRequestTimeoutException("https://proyecto.supabase.co", 10_000L)))
    }

    @Test
    fun debeSerAutenticacion_cuandoElTokenNoVale() {
        assertEquals(TipoError.AUTENTICACION, tipoDe(rest(401)))
    }

    @Test
    fun debeSerAutenticacion_cuandoNoHayPermisoSobreLaColumna() {
        assertEquals(TipoError.AUTENTICACION, tipoDe(rest(403)))
    }

    @Test
    fun debeSerValidacion_cuandoElValorNoCabeEnLaColumna() {
        assertEquals(TipoError.VALIDACION, tipoDe(rest(400)))
    }

    @Test
    fun debeSerValidacion_cuandoHayConflicto() {
        assertEquals(TipoError.VALIDACION, tipoDe(rest(409)))
    }

    @Test
    fun debeSerServidor_cuandoElServidorFalla() {
        listOf(500, 502, 503, 599).forEach {
            assertEquals(it.toString(), TipoError.SERVIDOR, tipoDe(rest(it)))
        }
    }

    @Test
    fun debeSerDesconocido_cuandoElCodigoNoEstaEnLaTabla() {
        assertEquals(TipoError.DESCONOCIDO, tipoDe(rest(404)))
        assertEquals(TipoError.DESCONOCIDO, tipoDe(rest(418)))
    }

    @Test
    fun debeSerDesconocido_cuandoElErrorNoEsDeSupabase() {
        assertEquals(TipoError.DESCONOCIDO, tipoDe(IllegalStateException("otra cosa")))
    }

    @Test
    fun debeLlevarElTextoDelServidor_cuandoElErrorEsDePostgrest() {
        val error = traducirErrorDatos(rest(403, "permission denied for table usuarios"))

        assertEquals("permission denied for table usuarios", error.mensaje)
    }

    @Test
    fun noDebeLlevarLasCabeceras_cuandoSeTraduceUnError() {
        val errores = listOf(
            traducirErrorDatos(rest(403)),
            traducirErrorDatos(HttpRequestException("Bearer token-secreto", HttpRequestBuilder())),
            traducirErrorDatos(IllegalStateException("Bearer token-secreto"))
        )

        errores.forEach {
            assertFalse(it.mensaje, it.mensaje.contains("token-secreto"))
            assertFalse(it.mensaje, it.mensaje.contains("llave-anon"))
        }
    }
}
