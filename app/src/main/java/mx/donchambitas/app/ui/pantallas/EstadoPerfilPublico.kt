package mx.donchambitas.app.ui.pantallas

import mx.donchambitas.app.dominio.modelo.PerfilPublicoTrabajador
import mx.donchambitas.app.util.TipoError

/**
 * Estado de Perfil publico (P-07), HU-08: vista previa del trabajador o perfil que ve el cliente.
 * Especificado en docs/producto/DISENO-PERFIL-SERVICIOS.md, seccion 11 y
 * docs/producto/DISENO-SOLICITUDES-BUSQUEDA.md, seccion 6 (H-39).
 *
 * @property perfil Lo que devuelve obtenerPerfilPublico; nulo mientras carga o si fallo.
 * @property errorCarga El fallo de la carga, para que ContenedorEstado pinte el error con "Reintentar".
 * @property esVistaPrevia Verdadero si el perfil es el de la sesion: el trabajador se ve a si mismo.
 *   Falso para cualquier otro, que es el cliente (o alguien sin sesion que ya no llega aqui).
 */
data class EstadoPerfilPublico(
    val cargando: Boolean = true,
    val errorCarga: TipoError? = null,
    val perfil: PerfilPublicoTrabajador? = null,
    val esVistaPrevia: Boolean = false
)
