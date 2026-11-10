package mx.donchambitas.app.datos.falso

import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import mx.donchambitas.app.dominio.modelo.DetalleSolicitud
import mx.donchambitas.app.dominio.modelo.EstadoPostulacion
import mx.donchambitas.app.dominio.modelo.EstadoSolicitud
import mx.donchambitas.app.dominio.modelo.Solicitud
import mx.donchambitas.app.dominio.repositorio.RepositorioSolicitudes
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

@Singleton
class RepositorioSolicitudesFalso @Inject constructor(
    private val fuente: FuenteDatosFalsa
) : RepositorioSolicitudes {

    var retrasoMs: Long = 300L
    var errorForzado: TipoError? = null

    private suspend fun verificarSimulacion(): Resultado.Error? {
        if (retrasoMs > 0) delay(retrasoMs)
        return errorForzado?.let { Resultado.Error(it, "Error simulado en RepositorioSolicitudes: $it") }
    }

    override suspend fun obtenerMisSolicitudes(): Resultado<List<Solicitud>> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        // Como vw_mis_solicitudes: cuantas postulaciones siguen en enviada (H-42).
        val lista = fuente.solicitudes.filter { it.clienteId == usuarioId }.map { solicitud ->
            solicitud.copy(
                postulacionesSinRevisar = fuente.postulaciones.count {
                    it.solicitudId == solicitud.id && it.estatus == EstadoPostulacion.ENVIADA
                }
            )
        }
        return Resultado.Exito(lista)
    }

    override suspend fun obtenerAbiertas(categoriaId: Int?): Resultado<List<Solicitud>> {
        verificarSimulacion()?.let { return it }

        val abiertas = fuente.solicitudes.filter {
            it.estatus == EstadoSolicitud.ABIERTA && (categoriaId == null || it.categoriaId == categoriaId)
        }
        return Resultado.Exito(abiertas)
    }

    override suspend fun obtenerDetalle(id: String): Resultado<DetalleSolicitud> {
        verificarSimulacion()?.let { return it }

        val solicitud = fuente.solicitudes.firstOrNull { it.id == id }
            ?: return Resultado.Error(TipoError.VALIDACION, "Solicitud no encontrada")

        val postulaciones = fuente.postulaciones.filter { it.solicitudId == id }
        return Resultado.Exito(DetalleSolicitud(solicitud, postulaciones))
    }

    override suspend fun crear(solicitud: Solicitud): Resultado<Solicitud> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        val ahora = Instant.now()
        val nueva = solicitud.copy(
            id = "sol-${UUID.randomUUID().toString().take(8)}",
            clienteId = usuarioId,
            estatus = EstadoSolicitud.ABIERTA,
            trabajadorId = null,
            creadoEn = ahora,
            actualizadoEn = ahora
        )
        fuente.solicitudes.add(nueva)
        return Resultado.Exito(nueva)
    }

    override suspend fun actualizar(solicitud: Solicitud): Resultado<Solicitud> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        val index = fuente.solicitudes.indexOfFirst { it.id == solicitud.id && it.clienteId == usuarioId }
        if (index == -1) {
            return Resultado.Error(TipoError.VALIDACION, "Solicitud no encontrada o sin permisos")
        }

        val actual = fuente.solicitudes[index]
        if (actual.estatus != EstadoSolicitud.ABIERTA) {
            return Resultado.Error(TipoError.VALIDACION, "Solo se pueden editar solicitudes abiertas")
        }

        val actualizada = solicitud.copy(
            clienteId = usuarioId,
            estatus = actual.estatus,
            trabajadorId = actual.trabajadorId,
            actualizadoEn = Instant.now()
        )
        fuente.solicitudes[index] = actualizada
        return Resultado.Exito(actualizada)
    }

    override suspend fun cancelar(id: String): Resultado<Solicitud> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        val index = fuente.solicitudes.indexOfFirst { it.id == id && it.clienteId == usuarioId }
        if (index == -1) {
            return Resultado.Error(TipoError.VALIDACION, "Solicitud no encontrada o sin permisos")
        }

        val actual = fuente.solicitudes[index]
        if (actual.estatus == EstadoSolicitud.CERRADA) {
            return Resultado.Error(TipoError.VALIDACION, "No se puede cancelar una solicitud ya cerrada")
        }

        val cancelada = actual.copy(estatus = EstadoSolicitud.CANCELADA, actualizadoEn = Instant.now())
        fuente.solicitudes[index] = cancelada
        return Resultado.Exito(cancelada)
    }

    override suspend fun cerrar(id: String): Resultado<Solicitud> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        val index = fuente.solicitudes.indexOfFirst { it.id == id && it.clienteId == usuarioId }
        if (index == -1) {
            return Resultado.Error(TipoError.VALIDACION, "Solicitud no encontrada o sin permisos")
        }

        val actual = fuente.solicitudes[index]
        if (actual.estatus != EstadoSolicitud.ASIGNADA || actual.trabajadorId == null) {
            return Resultado.Error(TipoError.VALIDACION, "Solo se puede cerrar una solicitud asignada con trabajador")
        }

        val ahora = Instant.now()
        val cerrada = actual.copy(
            estatus = EstadoSolicitud.CERRADA,
            cerradaEn = ahora,
            actualizadoEn = ahora
        )
        fuente.solicitudes[index] = cerrada
        return Resultado.Exito(cerrada)
    }
}
