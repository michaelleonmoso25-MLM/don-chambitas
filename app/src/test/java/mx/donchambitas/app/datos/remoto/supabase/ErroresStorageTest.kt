package mx.donchambitas.app.datos.remoto.supabase

import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestBuilder
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/** La tabla de errores de "subirFotoPerfil" en docs/tecnico/CONTRATOS-API.md. */
class ErroresStorageTest {

    // Storage contesta 400 aunque el motivo real sea otro, y el texto de la
    // excepcion lleva las cabeceras, como en ErroresDatosTest.
    private fun rechazo(texto: String) = RestException(
        texto,
        null,
        400,
        "Headers: [Authorization=[Bearer token-secreto], apikey=[llave-anon]]"
    )

    @Test
    fun debeSerRed_cuandoNoHayConexion() {
        assertEquals(TipoError.RED, traducirErrorStorage(HttpRequestException("sin red", HttpRequestBuilder())).tipo)
    }

    @Test
    fun debeSerRed_cuandoSeAgotaElTiempo() {
        val error = HttpRequestTimeoutException("https://proyecto.supabase.co", 10_000L)

        assertEquals(TipoError.RED, traducirErrorStorage(error).tipo)
    }

    @Test
    fun debeSerServidorConElTextoDelServidor_cuandoStorageRechaza() {
        val resultado = traducirErrorStorage(rechazo("Duplicate"))

        assertEquals(TipoError.SERVIDOR, resultado.tipo)
        assertEquals("Duplicate", resultado.mensaje)
        assertFalse(resultado.mensaje.contains("token-secreto"))
        assertFalse(resultado.mensaje.contains("llave-anon"))
    }

    @Test
    fun debeSerDesconocido_cuandoEsOtraCosa() {
        assertEquals(TipoError.DESCONOCIDO, traducirErrorStorage(IllegalStateException("x")).tipo)
    }
}
