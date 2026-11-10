package mx.donchambitas.app.datos.remoto.supabase

import mx.donchambitas.app.dominio.modelo.OrdenBusqueda
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsultaBusquedaTest {

    // ---- normalizarTexto ---------------------------------------------------

    @Test
    fun debeQuitarAcentosYMayusculas_cuandoElTextoLosTrae() {
        assertEquals("plomeria", normalizarTexto("Plomería"))
        assertEquals("jose maria nunez", normalizarTexto("José María Núñez"))
        assertEquals("pinguino", normalizarTexto("PINGÜINO"))
    }

    @Test
    fun debeDejarElTextoIgual_cuandoYaEstaNormalizado() {
        assertEquals("pedro plomero", normalizarTexto("pedro plomero"))
    }

    @Test
    fun debeEncontrarPlomeriaConAcento_cuandoSeEscribeSinAcento() {
        // La promesa de HU-17: lo guardado y lo escrito se normalizan igual.
        assertTrue(normalizarTexto("Plomero y electricista de Plomería").contains(palabrasBusqueda("plomeria").single()))
    }

    // ---- palabrasBusqueda --------------------------------------------------

    @Test
    fun debeDevolverListaVacia_cuandoElTextoEsNuloOVacioOSoloEspacios() {
        assertTrue(palabrasBusqueda(null).isEmpty())
        assertTrue(palabrasBusqueda("").isEmpty())
        assertTrue(palabrasBusqueda("   \t ").isEmpty())
    }

    @Test
    fun debePartirPorEspaciosYNormalizar_cuandoHayVariasPalabras() {
        assertEquals(listOf("pedro", "plomero"), palabrasBusqueda("  Pedro   PLOMERO "))
        assertEquals(listOf("jose", "plomeria"), palabrasBusqueda("José Plomería"))
    }

    // ---- comodines ---------------------------------------------------------

    @Test
    fun debeEscaparLosComodines_cuandoLaPalabraLosTrae() {
        assertEquals("50\\%", escaparComodines("50%"))
        assertEquals("a\\_b", escaparComodines("a_b"))
        assertEquals("a\\\\b", escaparComodines("a\\b"))
    }

    @Test
    fun debeEscaparLaBarraPrimero_cuandoHayBarrasYComodines() {
        assertEquals("\\\\\\%", escaparComodines("\\%"))
    }

    @Test
    fun debeArmarElPatronContiene_cuandoSeDaUnaPalabra() {
        assertEquals("%plomero%", patronContiene("plomero"))
        assertEquals("%50\\%%", patronContiene("50%"))
    }

    // ---- orden -------------------------------------------------------------

    @Test
    fun debeOrdenarPorPromedioConDesempate_cuandoNoSeEligeOrden() {
        val esperado = listOf(
            CriterioOrden("promedio", ascendente = false),
            CriterioOrden("creado_en", ascendente = false),
            CriterioOrden("trabajador_id", ascendente = true)
        )

        assertEquals(esperado, criteriosDeOrden(null))
        assertEquals(esperado, criteriosDeOrden(OrdenBusqueda.PROMEDIO))
    }

    @Test
    fun debeOrdenarPorFechaConDesempate_cuandoSeEligeLosMasRecientes() {
        assertEquals(
            listOf(CriterioOrden("creado_en", false), CriterioOrden("trabajador_id", true)),
            criteriosDeOrden(OrdenBusqueda.CREADO_EN)
        )
    }

    @Test
    fun debeOrdenarPorPrecioAscendenteConDesempate_cuandoSeEligeElMasBarato() {
        assertEquals(
            listOf(
                CriterioOrden("precio_desde", true),
                CriterioOrden("creado_en", false),
                CriterioOrden("trabajador_id", true)
            ),
            criteriosDeOrden(OrdenBusqueda.PRECIO_DESDE)
        )
    }

    @Test
    fun debeTerminarTodoOrdenEnElIdentificador_paraQueLasPaginasNoSeRepitan() {
        OrdenBusqueda.entries.forEach { orden ->
            assertEquals("trabajador_id", criteriosDeOrden(orden).last().columna)
        }
    }

    // ---- paginas -----------------------------------------------------------

    @Test
    fun debeDarLasPrimerasVeinteFilas_cuandoLaPaginaEsLaUno() {
        val rango = rangoDePagina(1)

        assertEquals(0L, rango.first)
        assertEquals(19L, rango.last)
    }

    @Test
    fun debeAvanzarDeVeinteEnVeinte_cuandoSePideOtraPagina() {
        assertEquals(20L, rangoDePagina(2).first)
        assertEquals(39L, rangoDePagina(2).last)
        assertEquals(40L, rangoDePagina(3).first)
    }

    @Test
    fun debeTratarComoPaginaUno_cuandoLaPaginaEsMenorQueUno() {
        assertEquals(rangoDePagina(1), rangoDePagina(0))
        assertEquals(rangoDePagina(1), rangoDePagina(-4))
    }

    @Test
    fun debePedirSoloLasColumnasDeLaVistaSinElTextoDeBusqueda_cuandoArmaElSelect() {
        assertFalse(COLUMNAS_BUSQUEDA.contains("texto_busqueda"))
        assertTrue(COLUMNAS_BUSQUEDA.split(",").containsAll(listOf("trabajador_id", "precio_desde", "categorias", "creado_en")))
    }
}
