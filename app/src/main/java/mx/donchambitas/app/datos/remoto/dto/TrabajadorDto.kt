package mx.donchambitas.app.datos.remoto.dto

import java.math.BigDecimal
import java.time.OffsetDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mx.donchambitas.app.datos.remoto.supabase.SerializadorBigDecimal
import mx.donchambitas.app.dominio.modelo.CalificacionTrabajador
import mx.donchambitas.app.dominio.modelo.PerfilPublicoTrabajador
import mx.donchambitas.app.dominio.modelo.PerfilTrabajador
import mx.donchambitas.app.dominio.modelo.ResenaPublica
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.modelo.ServicioPublico

/**
 * Fila de public.perfiles_trabajador tal como la devuelve PostgREST.
 * CONTRATOS-API.md, "obtenerMiPerfil".
 */
@Serializable
data class PerfilTrabajadorDto(
    @SerialName("usuario_id") val usuarioId: String,
    val rol: String,
    val titulo: String,
    val descripcion: String? = null,
    @SerialName("experiencia_anios") val experienciaAnios: Int,
    @SerialName("telefono_contacto") val telefonoContacto: String? = null,
    @SerialName("estado_id") val estadoId: Int? = null,
    @SerialName("municipio_id") val municipioId: Int? = null,
    val disponible: Boolean,
    @SerialName("creado_en") val creadoEn: String,
    @SerialName("actualizado_en") val actualizadoEn: String
) {
    fun aPerfilTrabajador(): PerfilTrabajador = PerfilTrabajador(
        usuarioId = usuarioId,
        rol = requireNotNull(RolUsuario.desdeValor(rol)) { "Rol desconocido en perfiles_trabajador: $rol" },
        titulo = titulo,
        descripcion = descripcion,
        experienciaAnios = experienciaAnios,
        telefonoContacto = telefonoContacto,
        estadoId = estadoId,
        municipioId = municipioId,
        disponible = disponible,
        creadoEn = OffsetDateTime.parse(creadoEn).toInstant(),
        actualizadoEn = OffsetDateTime.parse(actualizadoEn).toInstant()
    )
}

/**
 * Las ocho columnas que escribe guardarMiPerfil. Sin valores por omision a
 * proposito: kotlinx.serialization no escribe una propiedad que vale lo mismo
 * que su valor por omision, y una descripcion vaciada no se guardaria.
 */
@Serializable
data class PerfilEscrituraDto(
    @SerialName("usuario_id") val usuarioId: String,
    val titulo: String,
    val descripcion: String?,
    @SerialName("experiencia_anios") val experienciaAnios: Int,
    @SerialName("telefono_contacto") val telefonoContacto: String?,
    @SerialName("estado_id") val estadoId: Int?,
    @SerialName("municipio_id") val municipioId: Int?,
    val disponible: Boolean
)

/** Una fila de public.perfil_habilidades, para leer y para escribir. */
@Serializable
data class HabilidadDto(
    @SerialName("perfil_id") val perfilId: String,
    val habilidad: String
)

/** Solo la columna habilidad, que es lo que lee obtenerMisHabilidades. */
@Serializable
data class NombreHabilidadDto(val habilidad: String)

/**
 * Lo que devuelve fn_perfil_publico_trabajador (CONTRATOS-API.md,
 * "obtenerPerfilPublico"). estado y municipio pueden venir null: un trabajador
 * que aun no pone su ubicacion tiene perfil igual (H-25, DEC-36).
 */
@Serializable
data class PerfilPublicoDto(
    val id: String,
    val nombre: String,
    val apellidos: String,
    @SerialName("foto_url") val fotoUrl: String? = null,
    val titulo: String,
    val descripcion: String? = null,
    @SerialName("experiencia_anios") val experienciaAnios: Int,
    @SerialName("telefono_contacto") val telefonoContacto: String? = null,
    val disponible: Boolean,
    val estado: String? = null,
    val municipio: String? = null,
    val habilidades: List<String>,
    val calificacion: CalificacionDto,
    val servicios: List<ServicioPublicoDto>,
    val resenas: List<ResenaPublicaDto>
) {
    fun aPerfilPublico(): PerfilPublicoTrabajador = PerfilPublicoTrabajador(
        id = id,
        nombre = nombre,
        apellidos = apellidos,
        fotoUrl = fotoUrl,
        titulo = titulo,
        descripcion = descripcion,
        experienciaAnios = experienciaAnios,
        telefonoContacto = telefonoContacto,
        disponible = disponible,
        estado = estado,
        municipio = municipio,
        habilidades = habilidades,
        calificacion = CalificacionTrabajador(calificacion.promedio, calificacion.totalResenas),
        servicios = servicios.map { it.aServicioPublico() },
        resenas = resenas.map { it.aResenaPublica() }
    )
}

@Serializable
data class CalificacionDto(
    val promedio: Double,
    @SerialName("total_resenas") val totalResenas: Int
)

@Serializable
data class ServicioPublicoDto(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    @SerialName("categoria_id") val categoriaId: Int,
    @Serializable(with = SerializadorBigDecimal::class)
    @SerialName("precio_desde") val precioDesde: BigDecimal? = null,
    @Serializable(with = SerializadorBigDecimal::class)
    @SerialName("precio_hasta") val precioHasta: BigDecimal? = null,
    @SerialName("unidad_precio") val unidadPrecio: String? = null,
    val fotos: List<String>
) {
    fun aServicioPublico(): ServicioPublico = ServicioPublico(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        categoria = categoria,
        categoriaId = categoriaId,
        precioDesde = precioDesde,
        precioHasta = precioHasta,
        unidadPrecio = unidadPrecio,
        fotos = fotos
    )
}

@Serializable
data class ResenaPublicaDto(
    val calificacion: Int,
    val comentario: String? = null,
    @SerialName("cliente_nombre") val clienteNombre: String,
    @SerialName("creado_en") val creadoEn: String
) {
    fun aResenaPublica(): ResenaPublica = ResenaPublica(
        calificacion = calificacion,
        comentario = comentario,
        clienteNombre = clienteNombre,
        creadoEn = OffsetDateTime.parse(creadoEn).toInstant()
    )
}
