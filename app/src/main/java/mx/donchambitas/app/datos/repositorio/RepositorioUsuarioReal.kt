package mx.donchambitas.app.datos.repositorio

import dagger.Lazy
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import mx.donchambitas.app.datos.remoto.dto.UsuarioDto
import mx.donchambitas.app.datos.remoto.supabase.traducirErrorDatos
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

    // La foto es de S2-T14 (DEC-31): esta clase todavia no habla con Storage.
    override suspend fun subirFotoPerfil(bytes: ByteArray): Resultado<String> =
        Resultado.Error(TipoError.SERVIDOR, "La foto de perfil se guarda a partir de S2-T14")

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
}
