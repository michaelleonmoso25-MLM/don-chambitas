package mx.donchambitas.app.dominio.modelo

import java.math.BigDecimal
import java.time.Instant

/**
 * Representa la sesion activa de un usuario autenticado.
 *
 * Sin token: el de acceso, su refresco y su almacenamiento cifrado los lleva
 * supabase-kt (DEC-30). Un token en el dominio invita a guardarlo o a
 * registrarlo en un log.
 */
data class Sesion(
    val usuario: Usuario
)

/**
 * Resumen de calificacion obtenido por un trabajador.
 */
data class CalificacionTrabajador(
    val promedio: Double,
    val totalResenas: Int
)

/**
 * Informacion publica de un servicio ofrecido por un trabajador.
 */
data class ServicioPublico(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val categoriaId: Int,
    val precioDesde: BigDecimal? = null,
    val precioHasta: BigDecimal? = null,
    val unidadPrecio: String? = null,
    val fotos: List<String> = emptyList()
)

/**
 * Resena publica visible en el perfil del trabajador.
 */
data class ResenaPublica(
    val calificacion: Int,
    val comentario: String?,
    val clienteNombre: String,
    val creadoEn: Instant
)

/**
 * Perfil publico completo de un trabajador (resultado de la RPC fn_perfil_publico_trabajador).
 */
data class PerfilPublicoTrabajador(
    val id: String,
    val nombre: String,
    val apellidos: String,
    val fotoUrl: String? = null,
    val titulo: String,
    val descripcion: String? = null,
    val experienciaAnios: Int = 0,
    val telefonoContacto: String? = null,
    val disponible: Boolean = true,
    val estado: String? = null,
    val municipio: String? = null,
    val habilidades: List<String> = emptyList(),
    val calificacion: CalificacionTrabajador = CalificacionTrabajador(0.0, 0),
    val servicios: List<ServicioPublico> = emptyList(),
    val resenas: List<ResenaPublica> = emptyList()
)

/**
 * Criterios de ordenamiento para la busqueda de trabajadores.
 */
enum class OrdenBusqueda {
    PROMEDIO,
    CREADO_EN,
    PRECIO_DESDE
}

/**
 * Filtros aplicables al buscador de trabajadores.
 */
data class FiltrosBusquedaTrabajadores(
    val texto: String? = null,
    val categoriaId: Int? = null,
    val estadoId: Int? = null,
    val municipioId: Int? = null,
    val precioDesdeMax: BigDecimal? = null,
    val promedioMin: Double? = null,
    val orden: OrdenBusqueda? = null
)

/**
 * Fila resumida devuelta por la vista vw_busqueda_trabajadores.
 *
 * La ubicacion es opcional (H-48, DEC-36): un trabajador que aun no la pone
 * sale en la busqueda igual, y la tarjeta se pinta sin ella.
 */
data class ResumenTrabajadorBusqueda(
    val trabajadorId: String,
    val nombre: String,
    val apellidos: String,
    val fotoUrl: String? = null,
    val titulo: String,
    val disponible: Boolean,
    val estadoId: Int? = null,
    val municipioId: Int? = null,
    val estado: String? = null,
    val municipio: String? = null,
    val promedio: Double,
    val totalResenas: Int,
    val serviciosActivos: Int,
    val precioDesde: BigDecimal?,
    val categorias: List<Int>,
    val creadoEn: Instant
)

/**
 * Detalle completo de una solicitud junto con sus postulaciones recibidas.
 */
data class DetalleSolicitud(
    val solicitud: Solicitud,
    val postulaciones: List<Postulacion> = emptyList()
)

/**
 * Respuesta devuelta por el servicio de Inteligencia Artificial (Edge Function ia-generar).
 */
data class ResultadoIa(
    val resultado: String,
    val desdeCache: Boolean = false,
    val llamadasRestantesHoy: Int = 10
)
