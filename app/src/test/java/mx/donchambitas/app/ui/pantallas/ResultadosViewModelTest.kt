package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.SavedStateHandle
import java.math.BigDecimal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioCatalogosFalso
import mx.donchambitas.app.dominio.modelo.FiltrosBusquedaTrabajadores
import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.util.ReglaCorrutinas
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** P-06 contra el buscador falso, sin retraso (S4-T03 a S4-T05, DISENO-SOLICITUDES-BUSQUEDA.md 3). */
@OptIn(ExperimentalCoroutinesApi::class)
class ResultadosViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val fuente = FuenteDatosFalsa()
    private val repositorioCatalogos = RepositorioCatalogosFalso(fuente).apply { retrasoMs = 0 }

    private fun crearViewModel(categoriaId: Int? = null) = ResultadosViewModel(
        repositorioCatalogos,
        SavedStateHandle(
            if (categoriaId == null) emptyMap() else mapOf(Ruta.Resultados.ARG_CATEGORIA_ID to categoriaId)
        )
    )

    @Test
    fun debeArrancarCargando_yListarTodosLosTrabajadores_sinCategoria() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        assertTrue(viewModel.estado.value.cargando)

        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.cargando)
        assertNull(estado.errorCarga)
        assertFalse(estado.vacio)
        assertEquals(fuente.perfilesTrabajador.size, estado.trabajadores.size)
    }

    @Test
    fun debeTomarLaRutaSinCategoriaComoTodos_cuandoLlegaElValorPorDefecto() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel(Ruta.Resultados.SIN_CATEGORIA)

        advanceUntilIdle()

        assertEquals(fuente.perfilesTrabajador.size, viewModel.estado.value.trabajadores.size)
    }

    @Test
    fun debeListarSoloLosDeLaCategoria_cuandoLaRutaLaTrae() = runTest(reglaCorrutinas.testDispatcher) {
        val categoria = fuente.servicios.first { it.activo }.categoriaId
        val viewModel = crearViewModel(categoria)

        advanceUntilIdle()

        val trabajadores = viewModel.estado.value.trabajadores
        assertTrue(trabajadores.isNotEmpty())
        assertTrue(trabajadores.all { categoria in it.categorias })
    }

    @Test
    fun debeOrdenarPorCalificacion_delMejorAlPeor() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()

        advanceUntilIdle()

        val promedios = viewModel.estado.value.trabajadores.map { it.promedio }
        assertEquals(promedios.sortedDescending(), promedios)
    }

    @Test
    fun debeMostrarElVacio_cuandoNadieEsDeLaCategoria() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel(categoriaId = 9999)

        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertTrue(estado.trabajadores.isEmpty())
        assertNull(estado.errorCarga)
        assertTrue(estado.vacio)
    }

    @Test
    fun noDebeMostrarElVacio_mientrasCarga() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel(categoriaId = 9999)

        assertFalse(viewModel.estado.value.vacio)
    }

    @Test
    fun debeMostrarElError_yReintentar() = runTest(reglaCorrutinas.testDispatcher) {
        repositorioCatalogos.errorForzado = TipoError.RED
        val viewModel = crearViewModel()
        advanceUntilIdle()

        val conError = viewModel.estado.value
        assertEquals(TipoError.RED, conError.errorCarga)
        assertFalse(conError.vacio)

        repositorioCatalogos.errorForzado = null
        viewModel.alReintentarCarga()
        assertTrue(viewModel.estado.value.cargando)
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertNull(estado.errorCarga)
        assertTrue(estado.trabajadores.isNotEmpty())
    }

    @Test
    fun noDebeBuscar_alEscribir_soloActualizaElCampo() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        val total = viewModel.estado.value.trabajadores.size

        viewModel.alCambiarTexto("pedro")

        val estado = viewModel.estado.value
        assertEquals("pedro", estado.texto)
        assertFalse(estado.cargando)
        assertEquals(total, estado.trabajadores.size)
    }

    @Test
    fun debeBuscarElTextoEscrito_alPulsarBuscar() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        val total = viewModel.estado.value.trabajadores.size

        viewModel.alCambiarTexto("pedro")
        viewModel.alBuscar()
        assertTrue(viewModel.estado.value.cargando)
        advanceUntilIdle()

        val trabajadores = viewModel.estado.value.trabajadores
        assertTrue(trabajadores.isNotEmpty())
        assertTrue(trabajadores.size < total)
        assertTrue(trabajadores.all { "${it.nombre} ${it.apellidos} ${it.titulo}".contains("pedro", ignoreCase = true) })
    }

    @Test
    fun debeQuitarLosEspaciosDeLosExtremos_alBuscar() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        viewModel.alCambiarTexto("pedro")
        viewModel.alBuscar()
        advanceUntilIdle()
        val esperado = viewModel.estado.value.trabajadores.map { it.trabajadorId }

        viewModel.alCambiarTexto("   pedro  ")
        viewModel.alBuscar()
        advanceUntilIdle()

        assertEquals(esperado, viewModel.estado.value.trabajadores.map { it.trabajadorId })
    }

    @Test
    fun debeBuscarComoSinTexto_cuandoElTextoEstaEnBlanco() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.alCambiarTexto("    ")
        viewModel.alBuscar()
        advanceUntilIdle()

        assertEquals(fuente.perfilesTrabajador.size, viewModel.estado.value.trabajadores.size)
    }

    @Test
    fun debeVaciarElCampo_yVolverALaListaCompleta_alBorrarElTexto() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        viewModel.alCambiarTexto("pedro")
        viewModel.alBuscar()
        advanceUntilIdle()

        viewModel.alBorrarTexto()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals("", estado.texto)
        assertEquals(fuente.perfilesTrabajador.size, estado.trabajadores.size)
    }

    @Test
    fun debeMostrarElVacio_cuandoNadieCoincideConElTexto() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.alCambiarTexto("zzzzz")
        viewModel.alBuscar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertTrue(estado.trabajadores.isEmpty())
        assertTrue(estado.vacio)
    }

    @Test
    fun debeReintentarLaBusquedaAplicada_noLoEscritoSinBuscar() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        viewModel.alCambiarTexto("pedro")
        viewModel.alBuscar()
        advanceUntilIdle()
        val esperado = viewModel.estado.value.trabajadores.map { it.trabajadorId }

        viewModel.alCambiarTexto("zzzzz")
        repositorioCatalogos.errorForzado = TipoError.RED
        viewModel.alReintentarCarga()
        advanceUntilIdle()
        assertEquals(TipoError.RED, viewModel.estado.value.errorCarga)

        repositorioCatalogos.errorForzado = null
        viewModel.alReintentarCarga()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(esperado, estado.trabajadores.map { it.trabajadorId })
        assertEquals("zzzzz", estado.texto)
    }

    @Test
    fun debeCombinarElTextoConLaCategoriaDeLaRuta() = runTest(reglaCorrutinas.testDispatcher) {
        val categoria = fuente.servicios.first { it.activo }.categoriaId
        val viewModel = crearViewModel(categoria)
        advanceUntilIdle()
        val nombre = viewModel.estado.value.trabajadores.first().nombre

        viewModel.alCambiarTexto(nombre)
        viewModel.alBuscar()
        advanceUntilIdle()

        val trabajadores = viewModel.estado.value.trabajadores
        assertTrue(trabajadores.isNotEmpty())
        assertTrue(trabajadores.all { categoria in it.categorias && it.nombre.contains(nombre, ignoreCase = true) })

        viewModel.alCambiarTexto("zzzzz")
        viewModel.alBuscar()
        advanceUntilIdle()

        assertTrue(viewModel.estado.value.vacio)
    }

    @Test
    fun noDebeDejarQueUnaRespuestaTardia_pisaALaBusquedaNueva() = runTest(reglaCorrutinas.testDispatcher) {
        // La carga inicial tarda; la busqueda nueva, no. Sin cancelar la vieja, llegaria despues y la pisaria.
        repositorioCatalogos.retrasoMs = 1_000
        val viewModel = crearViewModel()
        repositorioCatalogos.retrasoMs = 0

        viewModel.alCambiarTexto("zzzzz")
        viewModel.alBuscar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertTrue(estado.vacio)
        assertTrue(estado.trabajadores.isEmpty())
    }

    // ---- S4-T05: filtros ----

    private fun filtros(
        categoriaId: Int? = null,
        estadoId: Int? = null,
        municipioId: Int? = null,
        precioDesdeMax: BigDecimal? = null,
        promedioMin: Double? = null
    ) = FiltrosBusquedaTrabajadores(
        categoriaId = categoriaId,
        estadoId = estadoId,
        municipioId = municipioId,
        precioDesdeMax = precioDesdeMax,
        promedioMin = promedioMin
    )

    @Test
    fun noDebeTenerFiltros_cuandoLaRutaNoTraeCategoria() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        assertEquals(0, viewModel.estado.value.filtrosActivos)
        assertFalse(viewModel.estado.value.hayFiltros)
    }

    @Test
    fun debeContarLaCategoriaDeLaRutaComoUnFiltro() = runTest(reglaCorrutinas.testDispatcher) {
        val categoria = fuente.servicios.first { it.activo }.categoriaId
        val viewModel = crearViewModel(categoria)
        advanceUntilIdle()

        assertEquals(1, viewModel.estado.value.filtrosActivos)
        assertEquals(categoria, viewModel.estado.value.filtros.categoriaId)
    }

    @Test
    fun debeAbrirLaHoja_yTraerLosCatalogos() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.alAbrirFiltros()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertTrue(estado.hojaFiltrosAbierta)
        assertEquals(16, estado.categorias.size)
        assertTrue(estado.estados.isNotEmpty())
        assertNull(estado.errorCatalogos)
    }

    @Test
    fun alCerrarLaHoja_noCambiaLosFiltrosNiLaLista() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        val antes = viewModel.estado.value.trabajadores

        viewModel.alAbrirFiltros()
        viewModel.alCerrarFiltros()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.hojaFiltrosAbierta)
        assertEquals(0, estado.filtrosActivos)
        assertEquals(antes, estado.trabajadores)
    }

    @Test
    fun debeAplicarElEstado_yCerrarLaHoja() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        val estadoId = fuente.perfilesTrabajador.first { it.estadoId != null }.estadoId!!
        val esperados = fuente.perfilesTrabajador.count { it.estadoId == estadoId }
        viewModel.alAbrirFiltros()

        viewModel.alAplicarFiltros(filtros(estadoId = estadoId))
        assertTrue(viewModel.estado.value.cargando)
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.hojaFiltrosAbierta)
        assertEquals(1, estado.filtrosActivos)
        assertEquals(esperados, estado.trabajadores.size)
        assertTrue(estado.trabajadores.all { it.estadoId == estadoId })
    }

    @Test
    fun debeCombinarEstadoYMunicipio() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        val perfil = fuente.perfilesTrabajador.first { it.estadoId != null && it.municipioId != null }

        viewModel.alAplicarFiltros(filtros(estadoId = perfil.estadoId, municipioId = perfil.municipioId))
        advanceUntilIdle()

        val trabajadores = viewModel.estado.value.trabajadores
        assertTrue(trabajadores.isNotEmpty())
        assertTrue(trabajadores.all { it.estadoId == perfil.estadoId && it.municipioId == perfil.municipioId })
        assertEquals(2, viewModel.estado.value.filtrosActivos)
    }

    @Test
    fun debeFiltrarPorPrecioMaximo() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        val total = viewModel.estado.value.trabajadores.size
        val precios = viewModel.estado.value.trabajadores.mapNotNull { it.precioDesde }
        val maximo = precios.min()

        viewModel.alAplicarFiltros(filtros(precioDesdeMax = maximo))
        advanceUntilIdle()

        val trabajadores = viewModel.estado.value.trabajadores
        assertTrue(trabajadores.isNotEmpty())
        assertTrue(trabajadores.size < total)
        assertTrue(trabajadores.all { it.precioDesde != null && it.precioDesde!! <= maximo })
    }

    @Test
    fun debeFiltrarPorCalificacionMinima() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        val total = viewModel.estado.value.trabajadores.size

        viewModel.alAplicarFiltros(filtros(promedioMin = 4.5))
        advanceUntilIdle()

        val trabajadores = viewModel.estado.value.trabajadores
        assertTrue(trabajadores.isNotEmpty())
        assertTrue(trabajadores.size < total)
        assertTrue(trabajadores.all { it.promedio >= 4.5 })
    }

    @Test
    fun debeCombinarLosFiltrosConElTextoDelBuscador() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        viewModel.alCambiarTexto("pedro")
        viewModel.alBuscar()
        advanceUntilIdle()
        assertTrue(viewModel.estado.value.trabajadores.isNotEmpty())

        // Un estado donde Pedro no trabaja: el texto sigue aplicado y no queda nadie.
        val estadoDePedro = viewModel.estado.value.trabajadores.first().estadoId
        val otroEstado = fuente.perfilesTrabajador.first { it.estadoId != null && it.estadoId != estadoDePedro }.estadoId
        viewModel.alAplicarFiltros(filtros(estadoId = otroEstado))
        advanceUntilIdle()

        assertTrue(viewModel.estado.value.vacio)
        assertEquals("pedro", viewModel.estado.value.texto)
    }

    @Test
    fun alQuitarElEstado_quitaTambienElMunicipio() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        val perfil = fuente.perfilesTrabajador.first { it.estadoId != null && it.municipioId != null }
        viewModel.alAplicarFiltros(filtros(estadoId = perfil.estadoId, municipioId = perfil.municipioId))
        advanceUntilIdle()

        viewModel.alQuitarFiltro(FiltroResultados.ESTADO)
        advanceUntilIdle()

        assertNull(viewModel.estado.value.filtros.estadoId)
        assertNull(viewModel.estado.value.filtros.municipioId)
        assertEquals(fuente.perfilesTrabajador.size, viewModel.estado.value.trabajadores.size)
    }

    @Test
    fun alQuitarElMunicipio_dejaElEstado() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        val perfil = fuente.perfilesTrabajador.first { it.estadoId != null && it.municipioId != null }
        viewModel.alAplicarFiltros(filtros(estadoId = perfil.estadoId, municipioId = perfil.municipioId))
        advanceUntilIdle()

        viewModel.alQuitarFiltro(FiltroResultados.MUNICIPIO)
        advanceUntilIdle()

        assertEquals(perfil.estadoId, viewModel.estado.value.filtros.estadoId)
        assertNull(viewModel.estado.value.filtros.municipioId)
    }

    @Test
    fun debeQuitarCadaFiltroPorSuChip() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        viewModel.alAplicarFiltros(filtros(categoriaId = 1, precioDesdeMax = BigDecimal("500"), promedioMin = 3.0))
        advanceUntilIdle()
        assertEquals(3, viewModel.estado.value.filtrosActivos)

        viewModel.alQuitarFiltro(FiltroResultados.CATEGORIA)
        viewModel.alQuitarFiltro(FiltroResultados.PRECIO)
        viewModel.alQuitarFiltro(FiltroResultados.CALIFICACION)
        advanceUntilIdle()

        assertEquals(0, viewModel.estado.value.filtrosActivos)
    }

    @Test
    fun alQuitarTodos_quitaTambienLaCategoriaDeLaRuta_yConservaElTexto() = runTest(reglaCorrutinas.testDispatcher) {
        val categoria = fuente.servicios.first { it.activo }.categoriaId
        val viewModel = crearViewModel(categoria)
        advanceUntilIdle()
        viewModel.alCambiarTexto("pedro")
        viewModel.alBuscar()
        advanceUntilIdle()
        viewModel.alAplicarFiltros(viewModel.estado.value.filtros.copy(promedioMin = 4.5))
        advanceUntilIdle()

        viewModel.alQuitarTodosLosFiltros()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(0, estado.filtrosActivos)
        assertEquals("pedro", estado.texto)
        assertTrue(estado.trabajadores.all { "${it.nombre} ${it.apellidos} ${it.titulo}".contains("pedro", ignoreCase = true) })
        assertTrue(estado.trabajadores.isNotEmpty())
    }

    @Test
    fun alElegirUnEstadoEnLaHoja_traeSusMunicipios_yAlQuitarlo_losVacia() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        val estadoId = fuente.municipios.first().estadoId

        viewModel.alElegirEstadoEnFiltros(estadoId)
        advanceUntilIdle()
        assertTrue(viewModel.estado.value.municipios.isNotEmpty())
        assertTrue(viewModel.estado.value.municipios.all { it.estadoId == estadoId })

        viewModel.alElegirEstadoEnFiltros(null)
        assertTrue(viewModel.estado.value.municipios.isEmpty())
    }

    @Test
    fun alCerrarLaHoja_losMunicipiosVuelvenAlEstadoAplicado() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        val perfil = fuente.perfilesTrabajador.first { it.estadoId != null && it.municipioId != null }
        viewModel.alAplicarFiltros(filtros(estadoId = perfil.estadoId, municipioId = perfil.municipioId))
        advanceUntilIdle()
        viewModel.alAbrirFiltros()
        advanceUntilIdle()

        // En la hoja se hojea otro estado, y se cierra sin aplicar.
        val otro = fuente.municipios.first { it.estadoId != perfil.estadoId }.estadoId
        viewModel.alElegirEstadoEnFiltros(otro)
        advanceUntilIdle()
        viewModel.alCerrarFiltros()
        advanceUntilIdle()

        assertTrue(viewModel.estado.value.municipios.all { it.estadoId == perfil.estadoId })
        assertTrue(viewModel.estado.value.municipios.any { it.id == perfil.municipioId })
    }

    @Test
    fun alAbrirLaHoja_reintentaLosCatalogosQueFallaron() = runTest(reglaCorrutinas.testDispatcher) {
        repositorioCatalogos.errorForzado = TipoError.RED
        val viewModel = crearViewModel()
        advanceUntilIdle()
        assertEquals(TipoError.RED, viewModel.estado.value.errorCatalogos)
        assertTrue(viewModel.estado.value.categorias.isEmpty())

        repositorioCatalogos.errorForzado = null
        viewModel.alAbrirFiltros()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertNull(estado.errorCatalogos)
        assertEquals(16, estado.categorias.size)
    }
}
