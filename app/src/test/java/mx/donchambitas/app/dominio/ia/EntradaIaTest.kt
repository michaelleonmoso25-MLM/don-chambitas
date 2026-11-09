package mx.donchambitas.app.dominio.ia

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * El formato de entrada de docs/producto/PROMPT-REDACCION.md, seccion 2 (S3-T13).
 * Tiene que ser determinista: la cache de HU-31 compara la entrada exacta.
 */
class EntradaIaTest {

    @Test
    fun debeArmarElEjemploDe2_1_alPedirElPerfil() {
        val entrada = EntradaIa.perfil(
            titulo = "Plomero con 10 años de experiencia",
            experienciaAnios = "10",
            habilidades = listOf("Boilers", "Fugas", "Tinacos")
        )

        assertEquals(
            "Oficio: Plomero con 10 años de experiencia\nAños de experiencia: 10\nHabilidades: Boilers; Fugas; Tinacos",
            entrada
        )
    }

    @Test
    fun debeArmarElEjemploDe2_2_alPedirElServicio() {
        val entrada = EntradaIa.servicio(
            titulo = "Instalación de boiler",
            categoria = "Plomería",
            borrador = "instalo boilers de paso y de deposito, tambien reviso la presion"
        )

        assertEquals(
            "Servicio: Instalación de boiler\nCategoría: Plomería\nBorrador: instalo boilers de paso y de deposito, tambien reviso la presion",
            entrada
        )
    }

    @Test
    fun debeOmitirLosRenglonesVacios_sinEtiquetaNiSaltoFinal() {
        assertEquals("Oficio: Electricista", EntradaIa.perfil("Electricista", "", emptyList()))
        assertEquals(
            "Servicio: Poda\nCategoría: Jardinería",
            EntradaIa.servicio("Poda", "Jardinería", "   \n  ")
        )
    }

    @Test
    fun debeMandarCero_porqueEsUnaCantidadValida() {
        assertEquals("Oficio: Albañil\nAños de experiencia: 0", EntradaIa.perfil("Albañil", "0", emptyList()))
    }

    @Test
    fun debeOmitirLosAnios_cuandoNoPasanLaValidacion() {
        assertEquals("Oficio: Pintor", EntradaIa.perfil("Pintor", "99", emptyList()))
        assertEquals("Oficio: Pintor", EntradaIa.perfil("Pintor", "abc", emptyList()))
    }

    @Test
    fun debeMandarLosAniosSinCerosALaIzquierda() {
        assertEquals("Oficio: Pintor\nAños de experiencia: 7", EntradaIa.perfil("Pintor", "007", emptyList()))
    }

    @Test
    fun debeColapsarEspaciosYSaltosDeLinea_enCadaValor() {
        val entrada = EntradaIa.perfil("  Plomero \n  con   gas ", " 5 ", listOf("  Fugas   de agua "))

        assertEquals("Oficio: Plomero con gas\nAños de experiencia: 5\nHabilidades: Fugas de agua", entrada)
    }

    @Test
    fun debeQuitarHabilidadesRepetidasYVacias_conservandoElOrden() {
        val entrada = EntradaIa.perfil("Plomero", "", listOf("Boilers", " ", "boilers", "Fugas"))

        assertEquals("Oficio: Plomero\nHabilidades: Boilers; Fugas", entrada)
    }

    @Test
    fun debeDarLaMismaEntrada_conLosMismosDatos() {
        val primera = EntradaIa.servicio("Poda", "Jardinería", "arbustos")
        val segunda = EntradaIa.servicio(" Poda ", "Jardinería", "arbustos ")

        assertEquals(primera, segunda)
    }
}
