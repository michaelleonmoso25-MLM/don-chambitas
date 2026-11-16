package mx.donchambitas.app.ui.componentes

import mx.donchambitas.app.R
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EstadosTest {

    @Test
    fun cadaTipoError_debeTenerMensajeDistinto() {
        val tipos = TipoError.entries
        val recursosMensajes = tipos.map { obtenerMensajeErrorRes(it) }

        // Cada TipoError tiene un recurso no nulo y valido
        tipos.forEach { tipo ->
            val resId = obtenerMensajeErrorRes(tipo)
            assertNotEquals(0, resId)
        }

        // Todos los recursos de mensaje deben ser unicos entre si
        assertEquals(
            "Cada TipoError debe tener un recurso de mensaje distinto",
            tipos.size,
            recursosMensajes.toSet().size
        )
    }

    @Test
    fun mapeoDeMensajesError_debeApuntarALosRecursosCorrectos() {
        assertEquals(R.string.error_red, obtenerMensajeErrorRes(TipoError.RED))
        assertEquals(R.string.error_autenticacion, obtenerMensajeErrorRes(TipoError.AUTENTICACION))
        assertEquals(R.string.error_validacion, obtenerMensajeErrorRes(TipoError.VALIDACION))
        assertEquals(R.string.error_correo_duplicado, obtenerMensajeErrorRes(TipoError.CORREO_DUPLICADO))
        assertEquals(R.string.error_limite_ia, obtenerMensajeErrorRes(TipoError.LIMITE_IA))
        assertEquals(R.string.error_ia_rechazada, obtenerMensajeErrorRes(TipoError.IA_RECHAZADA))
        assertEquals(R.string.error_servidor, obtenerMensajeErrorRes(TipoError.SERVIDOR))
        assertEquals(R.string.error_desconocido, obtenerMensajeErrorRes(TipoError.DESCONOCIDO))
    }

    @Test
    fun cadaTipoError_debeTenerTituloDistinto() {
        val tipos = TipoError.entries
        val recursosTitulos = tipos.map { obtenerTituloErrorRes(it) }

        tipos.forEach { tipo ->
            val resId = obtenerTituloErrorRes(tipo)
            assertNotEquals(0, resId)
        }

        assertEquals(
            "Cada TipoError debe tener un recurso de titulo distinto",
            tipos.size,
            recursosTitulos.toSet().size
        )
    }

    @Test
    fun mapeoDeTitulosError_debeApuntarALosRecursosCorrectos() {
        assertEquals(R.string.error_titulo_red, obtenerTituloErrorRes(TipoError.RED))
        assertEquals(R.string.error_titulo_autenticacion, obtenerTituloErrorRes(TipoError.AUTENTICACION))
        assertEquals(R.string.error_titulo_validacion, obtenerTituloErrorRes(TipoError.VALIDACION))
        assertEquals(R.string.error_titulo_correo_duplicado, obtenerTituloErrorRes(TipoError.CORREO_DUPLICADO))
        assertEquals(R.string.error_titulo_limite_ia, obtenerTituloErrorRes(TipoError.LIMITE_IA))
        assertEquals(R.string.error_titulo_ia_rechazada, obtenerTituloErrorRes(TipoError.IA_RECHAZADA))
        assertEquals(R.string.error_titulo_servidor, obtenerTituloErrorRes(TipoError.SERVIDOR))
        assertEquals(R.string.error_titulo_desconocido, obtenerTituloErrorRes(TipoError.DESCONOCIDO))
    }

    @Test
    fun resolverEstadoVisual_debePriorizarCargandoSobreErrorYVacio() {
        val estado = resolverEstadoVisual(
            cargando = true,
            tieneError = true,
            vacio = true
        )
        assertEquals(TipoEstadoVisual.CARGANDO, estado)
    }

    @Test
    fun resolverEstadoVisual_debePriorizarErrorSobreVacio_cuandoNoEstaCargando() {
        val estado = resolverEstadoVisual(
            cargando = false,
            tieneError = true,
            vacio = true
        )
        assertEquals(TipoEstadoVisual.ERROR, estado)
    }

    @Test
    fun resolverEstadoVisual_debeRetornarVacio_cuandoNoHayCargaNiError() {
        val estado = resolverEstadoVisual(
            cargando = false,
            tieneError = false,
            vacio = true
        )
        assertEquals(TipoEstadoVisual.VACIO, estado)
    }

    @Test
    fun resolverEstadoVisual_debeRetornarContenido_cuandoNoHayCargaNiErrorNiVacio() {
        val estado = resolverEstadoVisual(
            cargando = false,
            tieneError = false,
            vacio = false
        )
        assertEquals(TipoEstadoVisual.CONTENIDO, estado)
    }
}
