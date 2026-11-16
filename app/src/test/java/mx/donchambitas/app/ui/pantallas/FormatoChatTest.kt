package mx.donchambitas.app.ui.pantallas

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

/** La hora de un mensaje en P-15 y P-16 (DISENO-POSTULACION-CONTACTO-RESENAS.md, 4.1). */
class FormatoChatTest {

    private val zona = ZoneId.of("America/Mexico_City")
    private val hoy = LocalDate.of(2026, 11, 16)

    @Test
    fun deHoy_debeMostrarLaHora() {
        val instante = Instant.parse("2026-11-16T20:30:00Z") // 14:30 en Ciudad de Mexico
        assertEquals("14:30", textoHoraChat(instante, zona, hoy))
    }

    @Test
    fun deOtroDia_debeMostrarDiaYMes() {
        val instante = Instant.parse("2026-11-12T20:30:00Z")
        assertEquals("12/11", textoHoraChat(instante, zona, hoy))
    }
}
