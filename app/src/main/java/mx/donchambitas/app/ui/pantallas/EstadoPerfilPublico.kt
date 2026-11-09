package mx.donchambitas.app.ui.pantallas

import mx.donchambitas.app.dominio.modelo.PerfilPublicoTrabajador
import mx.donchambitas.app.util.TipoError

/**
 * Estado de Perfil publico (P-07) en su uso como vista previa del trabajador (HU-08).
 * Especificado en docs/producto/DISENO-PERFIL-SERVICIOS.md, seccion 11.
 *
 * @property perfil Lo que devuelve obtenerPerfilPublico; nulo mientras carga o si fallo.
 * @property errorCarga El fallo de la carga, para que ContenedorEstado pinte el error con "Reintentar".
 */
data class EstadoPerfilPublico(
    val cargando: Boolean = true,
    val errorCarga: TipoError? = null,
    val perfil: PerfilPublicoTrabajador? = null
)
