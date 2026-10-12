package mx.donchambitas.app.datos.remoto.supabase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnlaceAuthTest {

    private val esquema = ConfiguracionSupabase.ESQUEMA_ENLACE
    private val host = ConfiguracionSupabase.HOST_ENLACE

    @Test
    fun debeAceptar_cuandoElEnlaceTraeTokens() {
        val fragmento = "access_token=abc&expires_in=3600&refresh_token=def&token_type=bearer&type=recovery"

        assertTrue(esEnlaceConSesion(esquema, host, fragmento))
    }

    @Test
    fun debeDescartar_cuandoElEnlaceVencio() {
        val fragmento = "error=access_denied&error_code=otp_expired&error_description=Email+link+is+invalid"

        assertFalse(esEnlaceConSesion(esquema, host, fragmento))
    }

    @Test
    fun debeDescartar_cuandoUnaParteNoTraeIgual() {
        assertFalse(esEnlaceConSesion(esquema, host, "access_token=abc&roto"))
    }

    @Test
    fun debeDescartar_cuandoNoHayFragmento() {
        assertFalse(esEnlaceConSesion(esquema, host, null))
        assertFalse(esEnlaceConSesion(esquema, host, ""))
    }

    @Test
    fun debeDescartar_cuandoElEsquemaOElHostSonOtros() {
        assertFalse(esEnlaceConSesion("donchambitas", host, "access_token=abc"))
        assertFalse(esEnlaceConSesion(esquema, "recuperar", "access_token=abc"))
    }

    @Test
    fun debeReconocerLaRecuperacion_cuandoElTipoEsRecovery() {
        val fragmento = "access_token=abc&expires_in=3600&refresh_token=def&token_type=bearer&type=recovery"

        assertTrue(esEnlaceDeRecuperacion(fragmento))
    }

    @Test
    fun noDebeReconocerLaRecuperacion_cuandoElTipoEsOtroOFalta() {
        assertFalse(esEnlaceDeRecuperacion("access_token=abc&type=signup"))
        assertFalse(esEnlaceDeRecuperacion("access_token=abc&refresh_token=def"))
        assertFalse(esEnlaceDeRecuperacion("access_token=abc&type=recovery2"))
        assertFalse(esEnlaceDeRecuperacion("access_token=abc&type"))
        assertFalse(esEnlaceDeRecuperacion(null))
        assertFalse(esEnlaceDeRecuperacion(""))
    }
}
