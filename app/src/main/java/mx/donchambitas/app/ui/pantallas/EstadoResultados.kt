package mx.donchambitas.app.ui.pantallas

import mx.donchambitas.app.dominio.modelo.Categoria
import mx.donchambitas.app.dominio.modelo.Estado
import mx.donchambitas.app.dominio.modelo.FiltrosBusquedaTrabajadores
import mx.donchambitas.app.dominio.modelo.Municipio
import mx.donchambitas.app.dominio.modelo.ResumenTrabajadorBusqueda
import mx.donchambitas.app.util.TipoError

/** Los cinco filtros de HU-18, para quitar uno desde su chip (3.2 del diseno). */
enum class FiltroResultados { CATEGORIA, ESTADO, MUNICIPIO, PRECIO, CALIFICACION }

/**
 * Estado de Resultados (P-06), HU-16, HU-17 y HU-18.
 * Especificado en docs/producto/DISENO-SOLICITUDES-BUSQUEDA.md, seccion 3.
 *
 * Esta version trae la primera pagina con el texto del buscador (S4-T04) y los filtros
 * (S4-T05); el orden y las paginas se agregan en S4-T07.
 *
 * @property texto Lo que hay escrito en el buscador. Escribir no busca: se aplica con "Buscar".
 * @property filtros Los filtros aplicados. La categoria de la ruta (H-45) entra aqui como un filtro mas.
 * @property trabajadores La primera pagina, ordenada por calificacion.
 * @property errorCarga El fallo de la carga, para que ContenedorEstado pinte el error con "Reintentar".
 * @property hojaFiltrosAbierta Si la hoja de filtros esta a la vista.
 * @property categorias Catalogos de la hoja: se cargan sin bloquear los resultados.
 * @property municipios Los del estado que se esta eligiendo en la hoja.
 * @property errorCatalogos El fallo de los catalogos, que la hoja muestra; no afecta a los resultados.
 */
data class EstadoResultados(
    val cargando: Boolean = true,
    val errorCarga: TipoError? = null,
    val texto: String = "",
    val filtros: FiltrosBusquedaTrabajadores = FiltrosBusquedaTrabajadores(),
    val trabajadores: List<ResumenTrabajadorBusqueda> = emptyList(),
    val hojaFiltrosAbierta: Boolean = false,
    val categorias: List<Categoria> = emptyList(),
    val estados: List<Estado> = emptyList(),
    val municipios: List<Municipio> = emptyList(),
    val errorCatalogos: TipoError? = null
) {
    /** HU-17: sin resultados sale el estado vacio, nunca una lista en blanco. */
    val vacio: Boolean get() = !cargando && errorCarga == null && trabajadores.isEmpty()

    /** Cuantos de los cinco filtros estan puestos; el orden no cuenta. */
    val filtrosActivos: Int
        get() = listOf(
            filtros.categoriaId,
            filtros.estadoId,
            filtros.municipioId,
            filtros.precioDesdeMax,
            filtros.promedioMin
        ).count { it != null }

    val hayFiltros: Boolean get() = filtrosActivos > 0
}
