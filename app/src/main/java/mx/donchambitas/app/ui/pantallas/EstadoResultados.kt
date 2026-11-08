package mx.donchambitas.app.ui.pantallas

import mx.donchambitas.app.dominio.modelo.ResumenTrabajadorBusqueda
import mx.donchambitas.app.util.TipoError

/**
 * Estado de Resultados (P-06), HU-16 y HU-17.
 * Especificado en docs/producto/DISENO-SOLICITUDES-BUSQUEDA.md, seccion 3.
 *
 * Esta version solo trae la primera pagina de una categoria (o de todos): el texto, los
 * filtros, el orden y las paginas se agregan en S4-T04, S4-T05 y S4-T07.
 *
 * @property trabajadores La primera pagina, ordenada por calificacion.
 * @property errorCarga El fallo de la carga, para que ContenedorEstado pinte el error con "Reintentar".
 */
data class EstadoResultados(
    val cargando: Boolean = true,
    val errorCarga: TipoError? = null,
    val trabajadores: List<ResumenTrabajadorBusqueda> = emptyList()
) {
    /** HU-17: sin resultados sale el estado vacio, nunca una lista en blanco. */
    val vacio: Boolean get() = !cargando && errorCarga == null && trabajadores.isEmpty()
}
