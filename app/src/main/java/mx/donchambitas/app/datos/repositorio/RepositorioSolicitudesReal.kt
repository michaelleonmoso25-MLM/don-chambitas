package mx.donchambitas.app.datos.repositorio

import dagger.Lazy
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import mx.donchambitas.app.datos.remoto.dto.SolicitudDto
import mx.donchambitas.app.datos.remoto.dto.SolicitudEscrituraDto
import mx.donchambitas.app.datos.remoto.supabase.traducirErrorDatos
import mx.donchambitas.app.dominio.modelo.DetalleSolicitud
import mx.donchambitas.app.dominio.modelo.Solicitud
import mx.donchambitas.app.dominio.repositorio.RepositorioSolicitudes
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

/**
 * RepositorioSolicitudes contra solicitudes, vw_mis_solicitudes y la RPC
 * fn_cerrar_solicitud (S4-T16). Implementa la seccion "Solicitudes" de
 * docs/tecnico/CONTRATOS-API.md; cualquier diferencia con el contrato es un
 * defecto de esta clase o un hallazgo para el lider, no una decision local.
 *
 * Regla del contrato (DEC-21): el unico cambio de estatus que se hace desde
 * aqui es a "cancelada" (cancelar) y a "cerrada" por la RPC. Ningun update
 * toca estatus ni trabajador_id por otro camino.
 */
@Singleton
class RepositorioSolicitudesReal @Inject constructor(
    private val cliente: Lazy<SupabaseClient>
) : RepositorioSolicitudes {

    private val supabase get() = cliente.get()

    /** De la vista, para traer cuantas postulaciones siguen sin revisar (H-42). */
    override suspend fun obtenerMisSolicitudes(): Resultado<List<Solicitud>> = conMiId { uid ->
        val filas = supabase.from(VISTA_MIS_SOLICITUDES)
            .select {
                filter { eq("cliente_id", uid) }
                order("creado_en", Order.DESCENDING)
            }
            .decodeList<SolicitudDto>()
        Resultado.Exito(filas.map { it.aSolicitud() })
    }

    /** Solo los trabajadores las ven (H-46): un cliente recibe la lista vacia. */
    override suspend fun obtenerAbiertas(categoriaId: Int?): Resultado<List<Solicitud>> = intentar {
        val filas = supabase.from(TABLA_SOLICITUDES)
            .select {
                filter {
                    eq("estatus", ESTATUS_ABIERTA)
                    categoriaId?.let { eq("categoria_id", it) }
                }
                order("creado_en", Order.DESCENDING)
            }
            .decodeList<SolicitudDto>()
        Resultado.Exito(filas.map { it.aSolicitud() })
    }

    /**
     * decodeList y el primer elemento, no single(): con cero filas single() da
     * 406, que traducirErrorDatos no conoce. Lo que viene en postulaciones lo
     * decide RLS: el dueno ve todas, un trabajador solo la suya.
     */
    override suspend fun obtenerDetalle(id: String): Resultado<DetalleSolicitud> = intentar {
        val fila = supabase.from(TABLA_SOLICITUDES)
            .select(Columns.raw("*, postulaciones(*)")) {
                filter { eq("id", id) }
                order("creado_en", Order.DESCENDING, referencedTable = TABLA_POSTULACIONES)
            }
            .decodeList<SolicitudDto>()
            .firstOrNull()
            ?: return@intentar Resultado.Error(TipoError.VALIDACION, "Solicitud no encontrada")
        Resultado.Exito(DetalleSolicitud(fila.aSolicitud(), fila.postulaciones.map { it.aPostulacion() }))
    }

    /**
     * Siete columnas: id, estatus, trabajador_id, categorizada_por_ia y las
     * fechas los pone la base. Lo que traiga la solicitud en esos campos se ignora.
     */
    override suspend fun crear(solicitud: Solicitud): Resultado<Solicitud> = conMiId { uid ->
        val creada = supabase.from(TABLA_SOLICITUDES)
            .insert(
                SolicitudEscrituraDto(
                    clienteId = uid,
                    categoriaId = solicitud.categoriaId,
                    titulo = solicitud.titulo,
                    descripcion = solicitud.descripcion,
                    presupuesto = solicitud.presupuesto,
                    estadoId = solicitud.estadoId,
                    municipioId = solicitud.municipioId
                )
            ) { select() }
            .decodeSingle<SolicitudDto>()
        Resultado.Exito(creada.aSolicitud())
    }

    /** Seis columnas y solo si sigue abierta; la base no lo impide, el contrato si. */
    override suspend fun actualizar(solicitud: Solicitud): Resultado<Solicitud> = intentar {
        val filas = supabase.from(TABLA_SOLICITUDES)
            .update({
                set("categoria_id", solicitud.categoriaId)
                set("titulo", solicitud.titulo)
                set("descripcion", solicitud.descripcion)
                set("presupuesto", solicitud.presupuesto?.toPlainString())
                set("estado_id", solicitud.estadoId)
                set("municipio_id", solicitud.municipioId)
            }) {
                filter {
                    eq("id", solicitud.id)
                    eq("estatus", ESTATUS_ABIERTA)
                }
                select()
            }
            .decodeList<SolicitudDto>()
        unaFila(filas)
    }

    /** Solo se cancela una abierta (H-47). Cero filas: no existe, no es suya o ya no esta abierta. */
    override suspend fun cancelar(id: String): Resultado<Solicitud> = intentar {
        val filas = supabase.from(TABLA_SOLICITUDES)
            .update({ set("estatus", "cancelada") }) {
                filter {
                    eq("id", id)
                    eq("estatus", ESTATUS_ABIERTA)
                }
                select()
            }
            .decodeList<SolicitudDto>()
        unaFila(filas)
    }

    /**
     * La RPC devuelve void, asi que se vuelve a leer la fila para tener
     * cerrada_en. Sus RAISE EXCEPTION llegan como 400, o sea VALIDACION.
     */
    override suspend fun cerrar(id: String): Resultado<Solicitud> = intentar {
        supabase.postgrest.rpc(FUNCION_CERRAR, buildJsonObject { put("p_solicitud_id", id) })
        val filas = supabase.from(TABLA_SOLICITUDES)
            .select { filter { eq("id", id) } }
            .decodeList<SolicitudDto>()
        unaFila(filas)
    }

    private fun unaFila(filas: List<SolicitudDto>): Resultado<Solicitud> =
        filas.firstOrNull()?.let { Resultado.Exito(it.aSolicitud()) }
            ?: Resultado.Error(TipoError.VALIDACION, "Solicitud no encontrada o ya no esta abierta")

    /** El uid sale de la sesion que ya tiene supabase-kt, sin ir a la red. */
    private suspend fun <T> conMiId(operacion: suspend (String) -> Resultado<T>): Resultado<T> = intentar {
        val uid = supabase.auth.currentUserOrNull()?.id
            ?: return@intentar Resultado.Error(TipoError.AUTENTICACION, "No hay sesion abierta")
        operacion(uid)
    }

    private suspend fun <T> intentar(operacion: suspend () -> Resultado<T>): Resultado<T> =
        try {
            operacion()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            traducirErrorDatos(e)
        }

    private companion object {
        const val TABLA_SOLICITUDES = "solicitudes"
        const val TABLA_POSTULACIONES = "postulaciones"
        const val VISTA_MIS_SOLICITUDES = "vw_mis_solicitudes"
        const val FUNCION_CERRAR = "fn_cerrar_solicitud"
        const val ESTATUS_ABIERTA = "abierta"
    }
}
