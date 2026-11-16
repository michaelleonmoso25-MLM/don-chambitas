package mx.donchambitas.app.ui.pantallas

import org.junit.Assert.assertEquals
import org.junit.Test

/** El cupo consultado al abrir P-11 y P-13 (DEC-39, H-36). */
class EstadoBotonIaCupoTest {

    @Test
    fun conCupo_debeConservarSiFaltanDatosONo() {
        assertEquals(EstadoBotonIa.Disponible(6), EstadoBotonIa.Disponible().conRestantes(6))
        assertEquals(EstadoBotonIa.FaltanDatos(6), EstadoBotonIa.FaltanDatos().conRestantes(6))
    }

    @Test
    fun sinCupo_debeDeshabilitarDesdeElPrincipio() {
        assertEquals(EstadoBotonIa.LimiteAlcanzado, EstadoBotonIa.Disponible().conRestantes(0))
        assertEquals(EstadoBotonIa.LimiteAlcanzado, EstadoBotonIa.FaltanDatos().conRestantes(0))
    }

    @Test
    fun mientrasPide_noDebeCambiar() {
        assertEquals(EstadoBotonIa.Pidiendo, EstadoBotonIa.Pidiendo.conRestantes(0))
    }

    @Test
    fun alEscribirLosDatos_debeConservarElCupoConocido() {
        val estado = EstadoBotonIa.FaltanDatos().conRestantes(4)
        assertEquals(EstadoBotonIa.Disponible(4), estado.recalcular(faltanDatos = false))
        assertEquals(EstadoBotonIa.FaltanDatos(4), estado.recalcular(faltanDatos = true))
    }
}
