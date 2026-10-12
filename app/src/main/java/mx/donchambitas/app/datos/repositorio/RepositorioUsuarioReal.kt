package mx.donchambitas.app.datos.repositorio

import dagger.Lazy
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import mx.donchambitas.app.datos.remoto.dto.UsuarioDto
import mx.donchambitas.app.datos.remoto.supabase.traducirErrorDatos
import mx.donchambitas.app.datos.remoto.supabase.traducirErrorStorage
import mx.donchambitas.app.dominio.modelo.Usuario
import mx.donchambitas.app.dominio.repositorio.RepositorioUsuario
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

/**
 * RepositorioUsuario contra public.usuarios (DEC-31).
 * Implementa la seccion "Usuario" de docs/tecnico/CONTRATOS-API.md, escrita
 * contra supabase-kt 3.0.3; cualquier diferencia con el contrato es un
 * defecto de esta clase o un hallazgo para el lider, no una decision local.
 *
 * No valida ni recorta lo que recibe: las reglas viven en ValidacionesAuth y
 * las aplica el ViewModel.
 */
@Singleton
class RepositorioUsuarioReal @Inject constructor(
    // Perezoso por lo mismo que en RepositorioAuthReal: sin llaves en
    // local.properties la falla llega como Resultado.Error.
    private val cliente: Lazy<SupabaseClient>
) : RepositorioUsuario {

    private val supabase get() = cliente.get()

    override suspend fun obtenerMiUsuario(): Resultado<Usuario> = conMiId { id ->
        supabase.from("usuarios")
            .select { filter { eq("id", id) } }
            .decodeList<UsuarioDto>()
    }

    override suspend fun actualizarMiUsuario(
        nombre: String,
        apellidos: String,
        telefono: String?
    ): Resultado<Usuario> = conMiId { id ->
        // Solo estas tres columnas: authenticated no tiene permiso sobre las
        // demas, y con una sola de mas el update entero falla con 403.
        supabase.from("usuarios")
            .update({
                set("nombre", nombre)
                set("apellidos", apellidos)
                set("telefono", telefono)
            }) {
                filter { eq("id", id) }
                select()
            }
            .decodeList<UsuarioDto>()
    }

    /**
     * Sube, apunta y limpia (S2-T14, CONTRATOS-API.md "subirFotoPerfil"):
     * la ficha nunca apunta a un archivo que no existe, y despues de un exito
     * en la carpeta del usuario queda solo la foto nueva. La anterior se
     * borra por decision del lider.
     */
    override suspend fun subirFotoPerfil(bytes: ByteArray): Resultado<String> {
        try {
            val id = supabase.auth.currentUserOrNull()?.id
                ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion abierta")
            val cubeta = supabase.storage.from(CUBETA_PERFILES)
            // Un nombre nuevo cada vez: con uno fijo la URL no cambiaria y
            // Coil seguiria pintando la foto vieja.
            val archivo = "${System.currentTimeMillis()}.jpg"
            val ruta = "$id/$archivo"
            cubeta.upload(ruta, bytes) { contentType = ContentType.Image.JPEG }
            val url = cubeta.publicUrl(ruta)

            val apuntada = apuntarFoto(id, url)
            if (apuntada is Resultado.Error) {
                borrarSinFallar(cubeta, listOf(ruta))
                return apuntada
            }
            // Todo lo demas de la carpeta: la foto anterior y los restos de una
            // subida que fallo a la mitad. Si esto falla, la siguiente limpia.
            borrarSinFallar(cubeta, cubeta.list(id).map { "$id/${it.name}" }.filter { it != ruta })
            return Resultado.Exito(url)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return traducirErrorStorage(e)
        }
    }

    /** Escribe foto_url. Cero filas es AUTENTICACION, por lo mismo que en [conMiId]. */
    private suspend fun apuntarFoto(id: String, url: String): Resultado<Unit> =
        try {
            val filas = supabase.from("usuarios")
                .update({ set("foto_url", url) }) {
                    filter { eq("id", id) }
                    select()
                }
                .decodeList<UsuarioDto>()
            if (filas.isEmpty()) {
                Resultado.Error(TipoError.AUTENTICACION, "La sesion no corresponde a ninguna ficha")
            } else {
                Resultado.Exito(Unit)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            traducirErrorDatos(e)
        }

    private suspend fun borrarSinFallar(cubeta: BucketApi, rutas: List<String>) {
        if (rutas.isEmpty()) return
        try {
            cubeta.delete(rutas)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Un archivo de mas en la carpeta no rompe nada y se va en la siguiente subida.
        }
    }

    /**
     * El id sale de la sesion que ya tiene supabase-kt, sin ir a la red.
     *
     * Cero filas no es un exito vacio: RLS no rechaza la fila ajena ni la
     * peticion sin sesion, las filtra, y PostgREST contesta 200 con una lista
     * vacia. Aqui eso es que la sesion ya no corresponde a ninguna ficha.
     */
    private suspend fun conMiId(consulta: suspend (String) -> List<UsuarioDto>): Resultado<Usuario> =
        try {
            val id = supabase.auth.currentUserOrNull()?.id
                ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion abierta")
            consulta(id).firstOrNull()
                ?.let { Resultado.Exito(it.aUsuario()) }
                ?: Resultado.Error(TipoError.AUTENTICACION, "La sesion no corresponde a ninguna ficha")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            traducirErrorDatos(e)
        }

    private companion object {
        /** basedatos/03_almacenamiento.sql. Las politicas exigen que la ruta empiece con el uid. */
        const val CUBETA_PERFILES = "perfiles"
    }
}
