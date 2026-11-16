package mx.donchambitas.app.datos.remoto.dto

import java.time.Instant
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Las filas que devuelve Supabase para el chat (CONTRATOS-API.md, "Chat"). */
class ChatDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun unaFilaDeLaBandeja_debeLeerseConLaOtraPersonaYLosSinLeer() {
        val cuerpo = """
            {"id":"c1","solicitud_id":"s1","solicitud_titulo":"Fuga en el baño",
             "creado_en":"2026-11-16T10:00:00+00:00","ultimo_mensaje_en":"2026-11-16T12:30:00.123456+00:00",
             "otro_id":"u2","otro_nombre":"Pedro","otro_apellidos":"Ramirez","otro_foto_url":null,
             "ultimo_mensaje":"Perfecto, lo espero","sin_leer":2}
        """.trimIndent()

        val resumen = json.decodeFromString<ResumenConversacionDto>(cuerpo).aResumen()

        assertEquals("c1", resumen.id)
        assertEquals("Fuga en el baño", resumen.solicitudTitulo)
        assertEquals("Pedro Ramirez", resumen.otraPersona.nombreCompleto)
        assertEquals("Perfecto, lo espero", resumen.ultimoMensaje)
        assertEquals(2, resumen.sinLeer)
        assertEquals(Instant.parse("2026-11-16T12:30:00.123456Z"), resumen.ultimoMensajeEn)
    }

    @Test
    fun unHiloSinMensajesNiSolicitud_debeTraerLosCamposEnNulo() {
        val cuerpo = """
            {"id":"c2","solicitud_id":null,"solicitud_titulo":null,"creado_en":"2026-11-16T10:00:00+00:00",
             "ultimo_mensaje_en":null,"otro_id":"u3","otro_nombre":"Ana","otro_apellidos":"Lopez",
             "otro_foto_url":null,"ultimo_mensaje":null,"sin_leer":0}
        """.trimIndent()

        val resumen = json.decodeFromString<ResumenConversacionDto>(cuerpo).aResumen()

        assertNull(resumen.solicitudId)
        assertNull(resumen.ultimoMensaje)
        assertNull(resumen.ultimoMensajeEn)
        assertEquals(0, resumen.sinLeer)
    }

    @Test
    fun unMensaje_debeLeerseDeUnaLecturaODeUnEventoDeRealtime() {
        val cuerpo = """
            {"id":"m1","conversacion_id":"c1","emisor_id":"u2","contenido":"Hola",
             "leido_en":null,"creado_en":"2026-11-16T12:30:00+00:00"}
        """.trimIndent()

        val mensaje = json.decodeFromString<MensajeDto>(cuerpo).aMensaje()

        assertEquals("m1", mensaje.id)
        assertEquals("u2", mensaje.emisorId)
        assertEquals("Hola", mensaje.contenido)
        assertNull(mensaje.leidoEn)
        assertEquals(Instant.parse("2026-11-16T12:30:00Z"), mensaje.creadoEn)
    }

    @Test
    fun unaConversacion_debeLeerseConSuSolicitudOSinElla() {
        val cuerpo = """
            {"id":"c1","cliente_id":"u1","trabajador_id":"u2","solicitud_id":null,
             "creado_en":"2026-11-16T10:00:00+00:00","ultimo_mensaje_en":null}
        """.trimIndent()

        val conversacion = json.decodeFromString<ConversacionDto>(cuerpo).aConversacion()

        assertEquals("u1", conversacion.clienteId)
        assertEquals("u2", conversacion.trabajadorId)
        assertNull(conversacion.solicitudId)
    }

    @Test
    fun alEnviar_solo_debenViajarTresColumnas() {
        val enviado = Json.encodeToString(MensajeEscrituraDto.serializer(), MensajeEscrituraDto("c1", "u1", "Hola"))

        assertEquals("""{"conversacion_id":"c1","emisor_id":"u1","contenido":"Hola"}""", enviado)
    }
}
