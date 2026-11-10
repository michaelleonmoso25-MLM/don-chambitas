package mx.donchambitas.app.datos.falso

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import mx.donchambitas.app.dominio.modelo.CalificacionTrabajador
import mx.donchambitas.app.dominio.modelo.PerfilHabilidad
import mx.donchambitas.app.dominio.modelo.PerfilPublicoTrabajador
import mx.donchambitas.app.dominio.modelo.PerfilTrabajador
import mx.donchambitas.app.dominio.modelo.ResenaPublica
import mx.donchambitas.app.dominio.modelo.ServicioPublico
import mx.donchambitas.app.dominio.repositorio.RepositorioTrabajador
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

@Singleton
class RepositorioTrabajadorFalso @Inject constructor(
    private val fuente: FuenteDatosFalsa
) : RepositorioTrabajador {

    var retrasoMs: Long = 300L
    var errorForzado: TipoError? = null

    private suspend fun verificarSimulacion(): Resultado.Error? {
        if (retrasoMs > 0) delay(retrasoMs)
        return errorForzado?.let { Resultado.Error(it, "Error simulado en RepositorioTrabajador: $it") }
    }

    override suspend fun obtenerPerfilPublico(id: String): Resultado<PerfilPublicoTrabajador> {
        verificarSimulacion()?.let { return it }

        val usuario = fuente.usuarios.firstOrNull { it.id == id }
            ?: return Resultado.Error(TipoError.VALIDACION, "Trabajador no encontrado")

        val perfil = fuente.perfilesTrabajador.firstOrNull { it.usuarioId == id }
            ?: return Resultado.Error(TipoError.VALIDACION, "Perfil de trabajador no encontrado")

        val estadoNombre = fuente.estados.firstOrNull { it.id == perfil.estadoId }?.nombre
        val municipioNombre = fuente.municipios.firstOrNull { it.id == perfil.municipioId }?.nombre

        val habilidadesLista = fuente.habilidades
            .filter { it.perfilId == id }
            .map { it.habilidad }

        val resenasTrabajador = fuente.resenas.filter { it.trabajadorId == id }
        val promedio = if (resenasTrabajador.isNotEmpty()) {
            resenasTrabajador.map { it.calificacion }.average()
        } else {
            0.0
        }

        val serviciosPublicos = fuente.servicios
            .filter { it.perfilId == id && it.activo }
            .map { s ->
                val catNombre = fuente.categorias.firstOrNull { it.id == s.categoriaId }?.nombre ?: "General"
                val fotos = fuente.fotosServicio
                    .filter { it.servicioId == s.id }
                    .sortedBy { it.posicion }
                    .map { it.url }

                ServicioPublico(
                    id = s.id,
                    titulo = s.titulo,
                    descripcion = s.descripcion,
                    categoria = catNombre,
                    categoriaId = s.categoriaId,
                    precioDesde = s.precioDesde,
                    precioHasta = s.precioHasta,
                    unidadPrecio = s.unidadPrecio,
                    fotos = fotos
                )
            }

        val resenasPublicas = resenasTrabajador.map { r ->
            val cliente = fuente.usuarios.firstOrNull { it.id == r.clienteId }
            val clienteNombre = if (cliente != null) "${cliente.nombre} ${cliente.apellidos.take(1)}." else "Cliente"
            ResenaPublica(
                calificacion = r.calificacion,
                comentario = r.comentario,
                clienteNombre = clienteNombre,
                creadoEn = r.creadoEn
            )
        }

        val perfilPublico = PerfilPublicoTrabajador(
            id = usuario.id,
            nombre = usuario.nombre,
            apellidos = usuario.apellidos,
            fotoUrl = usuario.fotoUrl,
            titulo = perfil.titulo,
            descripcion = perfil.descripcion,
            experienciaAnios = perfil.experienciaAnios,
            telefonoContacto = perfil.telefonoContacto,
            disponible = perfil.disponible,
            estado = estadoNombre,
            municipio = municipioNombre,
            habilidades = habilidadesLista,
            calificacion = CalificacionTrabajador(
                promedio = Math.round(promedio * 10.0) / 10.0,
                totalResenas = resenasTrabajador.size
            ),
            servicios = serviciosPublicos,
            resenas = resenasPublicas
        )

        return Resultado.Exito(perfilPublico)
    }

    override suspend fun obtenerMiPerfil(): Resultado<PerfilTrabajador?> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        val perfil = fuente.perfilesTrabajador.firstOrNull { it.usuarioId == usuarioId }
        return Resultado.Exito(perfil)
    }

    override suspend fun obtenerMisHabilidades(): Resultado<List<String>> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        val habilidades = fuente.habilidades
            .filter { it.perfilId == usuarioId }
            .map { it.habilidad }
            .sorted()
        return Resultado.Exito(habilidades)
    }

    override suspend fun guardarMiPerfil(perfil: PerfilTrabajador): Resultado<PerfilTrabajador> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        val index = fuente.perfilesTrabajador.indexOfFirst { it.usuarioId == usuarioId }
        val perfilAjustado = perfil.copy(usuarioId = usuarioId)
        if (index != -1) {
            fuente.perfilesTrabajador[index] = perfilAjustado
        } else {
            fuente.perfilesTrabajador.add(perfilAjustado)
        }
        return Resultado.Exito(perfilAjustado)
    }

    override suspend fun reemplazarHabilidades(lista: List<String>): Resultado<List<PerfilHabilidad>> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        fuente.habilidades.removeAll { it.perfilId == usuarioId }
        val nuevasHabilidades = lista.map { PerfilHabilidad(usuarioId, it) }
        fuente.habilidades.addAll(nuevasHabilidades)
        return Resultado.Exito(nuevasHabilidades)
    }
}
