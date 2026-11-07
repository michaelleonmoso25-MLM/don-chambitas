package mx.donchambitas.app.dominio.validacion

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Las reglas de 4.2 de DISENO-SOLICITUDES-BUSQUEDA.md (S4-T02). */
class ValidacionesSolicitudTest {

    @Test
    fun titulo_exigeEntre1y120Caracteres() {
        assertEquals(FalloSolicitud.TITULO_VACIO, ValidacionesSolicitud.validarTitulo(""))
        assertEquals(FalloSolicitud.TITULO_VACIO, ValidacionesSolicitud.validarTitulo("   "))
        assertNull(ValidacionesSolicitud.validarTitulo("Reparar una fuga"))
        assertNull(ValidacionesSolicitud.validarTitulo("a".repeat(120)))
        assertEquals(FalloSolicitud.TITULO_LARGO, ValidacionesSolicitud.validarTitulo("a".repeat(121)))
    }

    @Test
    fun titulo_seMideSinLosEspaciosDeLosExtremos() {
        assertNull(ValidacionesSolicitud.validarTitulo("  " + "a".repeat(120) + "  "))
    }

    @Test
    fun categoria_esObligatoria() {
        assertEquals(FalloSolicitud.CATEGORIA_SIN_ELEGIR, ValidacionesSolicitud.validarCategoria(null))
        assertNull(ValidacionesSolicitud.validarCategoria(3))
    }

    @Test
    fun descripcion_esObligatoriaYTieneTope() {
        assertEquals(FalloSolicitud.DESCRIPCION_VACIA, ValidacionesSolicitud.validarDescripcion("  "))
        assertNull(ValidacionesSolicitud.validarDescripcion("Tengo una fuga en el baño"))
        assertNull(ValidacionesSolicitud.validarDescripcion("a".repeat(1000)))
        assertEquals(FalloSolicitud.DESCRIPCION_LARGA, ValidacionesSolicitud.validarDescripcion("a".repeat(1001)))
    }

    @Test
    fun presupuesto_esOpcional_yVacioEsValido() {
        assertNull(ValidacionesSolicitud.validarPresupuesto(""))
        assertNull(ValidacionesSolicitud.validarPresupuesto("   "))
    }

    @Test
    fun presupuesto_aceptaCantidadesConHastaDosDecimales() {
        assertNull(ValidacionesSolicitud.validarPresupuesto("350"))
        assertNull(ValidacionesSolicitud.validarPresupuesto("350.50"))
        assertNull(ValidacionesSolicitud.validarPresupuesto("350,5"))
        assertNull(ValidacionesSolicitud.validarPresupuesto("0"))
        assertNull(ValidacionesSolicitud.validarPresupuesto("99999999.99"))
    }

    @Test
    fun presupuesto_rechazaLoQueNoEsUnaCantidadValida() {
        assertEquals(FalloSolicitud.PRESUPUESTO_FORMATO, ValidacionesSolicitud.validarPresupuesto("abc"))
        assertEquals(FalloSolicitud.PRESUPUESTO_FORMATO, ValidacionesSolicitud.validarPresupuesto("-5"))
        assertEquals(FalloSolicitud.PRESUPUESTO_FORMATO, ValidacionesSolicitud.validarPresupuesto("10.123"))
        assertEquals(FalloSolicitud.PRESUPUESTO_GRANDE, ValidacionesSolicitud.validarPresupuesto("100000000"))
    }

    @Test
    fun estadoYMunicipio_seExigenAlPublicar() {
        assertEquals(FalloSolicitud.ESTADO_SIN_ELEGIR, ValidacionesSolicitud.validarEstado(null))
        assertNull(ValidacionesSolicitud.validarEstado(9))
        assertEquals(FalloSolicitud.MUNICIPIO_SIN_ELEGIR, ValidacionesSolicitud.validarMunicipio(9, null))
        assertNull(ValidacionesSolicitud.validarMunicipio(9, 40))
    }

    @Test
    fun municipio_noSeQuejaSiAunNoHayEstado() {
        assertNull(ValidacionesSolicitud.validarMunicipio(null, null))
    }

    @Test
    fun normalizarPresupuesto_devuelveNuloSiVacio_yDosDecimalesSiHay() {
        assertNull(ValidacionesSolicitud.normalizarPresupuesto(""))
        assertEquals(BigDecimal("350.00"), ValidacionesSolicitud.normalizarPresupuesto("350"))
        assertEquals(BigDecimal("350.50"), ValidacionesSolicitud.normalizarPresupuesto("350,5"))
    }
}
