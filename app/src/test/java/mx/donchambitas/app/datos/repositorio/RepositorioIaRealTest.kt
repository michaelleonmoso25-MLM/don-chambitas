package mx.donchambitas.app.datos.repositorio

import kotlinx.serialization.json.Json
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** La tabla de errores de ia-generar en CONTRATOS-API.md y el rechazo de H-30 y H-37. */
class RepositorioIaRealTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun tipoDe(codigo: Int) = errorDeIa(codigo, "detalle").tipo

    @Test
    fun cadaCodigoHttp_debeMapearAlTipoDelContrato() {
        assertEquals(TipoError.DESCONOCIDO, tipoDe(400))
        assertEquals(TipoError.AUTENTICACION, tipoDe(401))
        assertEquals(TipoError.IA_RECHAZADA, tipoDe(422))
        assertEquals(TipoError.LIMITE_IA, tipoDe(429))
        assertEquals(TipoError.SERVIDOR, tipoDe(503))
        assertEquals(TipoError.SERVIDOR, tipoDe(500))
    }

    @Test
    fun unCodigoNoPrevisto_debeConservarElDetalle() {
        assertEquals("detalle", errorDeIa(500, "detalle").mensaje)
    }

    @Test
    fun un200ConTexto_debeSerExito() {
        val cuerpo = """{"resultado":"Hola","desde_cache":true,"llamadas_restantes_hoy":7}"""
        val dato = (resultadoDeCuerpo(json, cuerpo) as Resultado.Exito).dato
        assertEquals("Hola", dato.resultado)
        assertTrue(dato.desdeCache)
        assertEquals(7, dato.llamadasRestantesHoy)
    }

    @Test
    fun un200ConNoPuedoRedactar_debeSerElMismoRechazoQueElDe422() {
        val cuerpo = """{"resultado":" NO_PUEDO_REDACTAR ","desde_cache":false,"llamadas_restantes_hoy":9}"""
        val error = resultadoDeCuerpo(json, cuerpo) as Resultado.Error
        assertEquals(TipoError.IA_RECHAZADA, error.tipo)
    }
}
