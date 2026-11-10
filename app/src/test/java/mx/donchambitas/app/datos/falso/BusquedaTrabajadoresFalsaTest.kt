package mx.donchambitas.app.datos.falso

import kotlinx.coroutines.runBlocking
import mx.donchambitas.app.dominio.modelo.FiltrosBusquedaTrabajadores
import mx.donchambitas.app.dominio.modelo.OrdenBusqueda
import mx.donchambitas.app.dominio.modelo.ResumenTrabajadorBusqueda
import mx.donchambitas.app.util.DatosPrueba
import mx.donchambitas.app.util.Resultado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * La busqueda del repositorio falso se comporta como la de la base (S4-T09, DEC-36):
 * texto por palabra y sin acentos, ubicacion opcional y desempate fijo. Las pruebas de
 * flujo de la aplicacion corren sobre este falso, asi que si se desvia de la base,
 * esas pruebas pasan por razones equivocadas.
 */
class BusquedaTrabajadoresFalsaTest {

    private lateinit var fuente: FuenteDatosFalsa
    private lateinit var repo: RepositorioCatalogosFalso

    @Before
    fun preparar() {
        fuente = FuenteDatosFalsa()
        fuente.usuarios.clear()
        fuente.perfilesTrabajador.clear()
        fuente.servicios.clear()
        fuente.resenas.clear()
        repo = RepositorioCatalogosFalso(fuente).apply { retrasoMs = 0L }
    }

    private fun agregarTrabajador(
        id: String,
        nombre: String = "Pedro",
        apellidos: String = "Ramírez",
        titulo: String = "Plomero",
        estadoId: Int? = 9,
        municipioId: Int? = 1,
        disponible: Boolean = true
    ) {
        fuente.usuarios.add(DatosPrueba.crearUsuario(id = id, nombre = nombre, apellidos = apellidos))
        fuente.perfilesTrabajador.add(
            DatosPrueba.crearPerfilTrabajador(
                usuarioId = id,
                titulo = titulo,
                estadoId = estadoId,
                municipioId = municipioId,
                disponible = disponible
            )
        )
    }

    private fun buscar(filtros: FiltrosBusquedaTrabajadores, pagina: Int = 1): List<ResumenTrabajadorBusqueda> =
        runBlocking { (repo.buscarTrabajadores(filtros, pagina) as Resultado.Exito).dato }

    @Test
    fun debeEncontrarPlomeriaConAcento_cuandoSeEscribeSinAcento() {
        agregarTrabajador("t1", titulo = "Especialista en plomería")

        val res = buscar(FiltrosBusquedaTrabajadores(texto = "plomeria"))

        assertEquals(listOf("t1"), res.map { it.trabajadorId })
    }

    @Test
    fun debeEncontrarSinAcentoUnNombreConAcento_cuandoSeBuscaPorNombre() {
        agregarTrabajador("t1", nombre = "José", apellidos = "Núñez", titulo = "Albañil")

        assertEquals(1, buscar(FiltrosBusquedaTrabajadores(texto = "jose nunez")).size)
    }

    @Test
    fun debeExigirCadaPalabra_cuandoElTextoTraeVarias() {
        agregarTrabajador("t1", nombre = "Pedro", titulo = "Plomero")
        agregarTrabajador("t2", nombre = "Pedro", titulo = "Electricista")

        val res = buscar(FiltrosBusquedaTrabajadores(texto = "plomero pedro"))

        assertEquals(listOf("t1"), res.map { it.trabajadorId })
    }

    @Test
    fun debeIgnorarElOrdenDeLasPalabras_cuandoElTextoTraeVarias() {
        agregarTrabajador("t1", nombre = "Pedro", titulo = "Plomero")

        assertEquals(1, buscar(FiltrosBusquedaTrabajadores(texto = "pedro plomero")).size)
        assertEquals(1, buscar(FiltrosBusquedaTrabajadores(texto = "plomero pedro")).size)
    }

    @Test
    fun debeDevolverTodos_cuandoElTextoSoloTraeEspacios() {
        agregarTrabajador("t1")
        agregarTrabajador("t2")

        assertEquals(2, buscar(FiltrosBusquedaTrabajadores(texto = "   ")).size)
    }

    @Test
    fun debeDevolverListaVacia_cuandoNadieCoincide() {
        agregarTrabajador("t1")

        assertTrue(buscar(FiltrosBusquedaTrabajadores(texto = "carpintero")).isEmpty())
    }

    @Test
    fun debeIncluirAlTrabajadorSinUbicacion_conLosCamposNulos() {
        // H-48: antes se rellenaba con 0 y "", y el filtro de municipio podia darle un id falso.
        agregarTrabajador("t1", estadoId = null, municipioId = null)

        val resumen = buscar(FiltrosBusquedaTrabajadores()).single()

        assertNull(resumen.estadoId)
        assertNull(resumen.municipioId)
        assertNull(resumen.estado)
        assertNull(resumen.municipio)
    }

    @Test
    fun debeExcluirAlTrabajadorSinUbicacion_cuandoSeFiltraPorMunicipio() {
        agregarTrabajador("t1", estadoId = null, municipioId = null)
        agregarTrabajador("t2", estadoId = 9, municipioId = 1)

        val res = buscar(FiltrosBusquedaTrabajadores(municipioId = 1))

        assertEquals(listOf("t2"), res.map { it.trabajadorId })
    }

    @Test
    fun debeIncluirAlTrabajadorNoDisponible_conSuEtiqueta() {
        // H-43: no se oculta a nadie.
        agregarTrabajador("t1", disponible = false)

        val resumen = buscar(FiltrosBusquedaTrabajadores()).single()

        assertFalse(resumen.disponible)
    }

    @Test
    fun debeDesempatarPorIdentificador_cuandoTodosTienenElMismoPromedioYFecha() {
        listOf("t3", "t1", "t2").forEach { agregarTrabajador(it) }

        val res = buscar(FiltrosBusquedaTrabajadores(orden = OrdenBusqueda.PROMEDIO))

        assertEquals(listOf("t1", "t2", "t3"), res.map { it.trabajadorId })
    }

    @Test
    fun debeUsarElPromedioComoOrdenPorOmision_cuandoNoSeEligeOrden() {
        listOf("t2", "t1").forEach { agregarTrabajador(it) }

        assertEquals(
            buscar(FiltrosBusquedaTrabajadores(orden = OrdenBusqueda.PROMEDIO)).map { it.trabajadorId },
            buscar(FiltrosBusquedaTrabajadores()).map { it.trabajadorId }
        )
    }

    @Test
    fun debePartirEnPaginasDeVeinte_ySinRepetirNiSaltarFilas() {
        (1..45).forEach { agregarTrabajador("t%02d".format(it)) }

        val p1 = buscar(FiltrosBusquedaTrabajadores(), pagina = 1)
        val p2 = buscar(FiltrosBusquedaTrabajadores(), pagina = 2)
        val p3 = buscar(FiltrosBusquedaTrabajadores(), pagina = 3)

        assertEquals(listOf(20, 20, 5), listOf(p1.size, p2.size, p3.size))
        assertEquals(45, (p1 + p2 + p3).map { it.trabajadorId }.distinct().size)
        assertTrue(buscar(FiltrosBusquedaTrabajadores(), pagina = 4).isEmpty())
    }

    @Test
    fun debeTratarComoPaginaUno_cuandoLaPaginaEsMenorQueUno() {
        agregarTrabajador("t1")

        assertEquals(
            buscar(FiltrosBusquedaTrabajadores(), pagina = 1).map { it.trabajadorId },
            buscar(FiltrosBusquedaTrabajadores(), pagina = 0).map { it.trabajadorId }
        )
    }
}
