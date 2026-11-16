package mx.donchambitas.app.ui.pantallas

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val LOCALE_MEXICO: Locale = Locale.forLanguageTag("es-MX")

/**
 * La hora de un mensaje en la bandeja y en el chat: "HH:mm" si es de hoy, "dd/MM" si es de otro
 * dia (DISENO-POSTULACION-CONTACTO-RESENAS.md, 4.1). La zona y el dia de hoy se reciben para probarlo.
 */
fun textoHoraChat(
    instante: Instant,
    zona: ZoneId = ZoneId.systemDefault(),
    hoy: LocalDate = LocalDate.now(zona)
): String {
    val dia = instante.atZone(zona).toLocalDate()
    val patron = if (dia == hoy) "HH:mm" else "dd/MM"
    return DateTimeFormatter.ofPattern(patron, LOCALE_MEXICO).withZone(zona).format(instante)
}
