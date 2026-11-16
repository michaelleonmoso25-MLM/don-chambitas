package mx.donchambitas.app.datos.repositorio

import dagger.Lazy
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.functions.functions
import io.ktor.client.statement.bodyAsText
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mx.donchambitas.app.dominio.modelo.FuncionIa
import mx.donchambitas.app.dominio.modelo.ResultadoIa
import mx.donchambitas.app.dominio.repositorio.RepositorioIa
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

@Serializable
private data class PeticionIaDto(
    val funcion: String,
    val entrada: String
)

@Serializable
private data class RespuestaIaDto(
    val resultado: String,
    @SerialName("desde_cache")
    val desdeCache: Boolean = false,
    @SerialName("llamadas_restantes_hoy")
    val llamadasRestantesHoy: Int = 10
)

@Singleton
class RepositorioIaReal @Inject constructor(
    private val cliente: Lazy<SupabaseClient>
) : RepositorioIa {

    private val supabase get() = cliente.get()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun generar(funcion: FuncionIa, entrada: String): Resultado<ResultadoIa> {
        return try {
            val peticion = PeticionIaDto(
                funcion = funcion.valor,
                entrada = entrada
            )
            val response = supabase.functions.invoke("ia-generar", peticion)
            val cuerpo = response.bodyAsText()
            val resultadoDto = json.decodeFromString<RespuestaIaDto>(cuerpo)
            Resultado.Exito(
                ResultadoIa(
                    resultado = resultadoDto.resultado,
                    desdeCache = resultadoDto.desdeCache,
                    llamadasRestantesHoy = resultadoDto.llamadasRestantesHoy
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: RestException) {
            when (e.statusCode) {
                400 -> Resultado.Error(TipoError.VALIDACION, "Petición no válida")
                401 -> Resultado.Error(TipoError.AUTENTICACION, "Inicia sesión para usar la IA")
                422 -> Resultado.Error(TipoError.VALIDACION, "No puedo redactar una descripción con esos datos")
                429 -> Resultado.Error(TipoError.LIMITE_IA, "Alcanzaste el límite de hoy")
                503 -> Resultado.Error(TipoError.SERVIDOR, "El servicio de IA no está disponible en este momento")
                else -> Resultado.Error(TipoError.SERVIDOR, e.error)
            }
        } catch (e: HttpRequestException) {
            Resultado.Error(TipoError.RED, "Sin conexión. Revisa tu conexión e intenta de nuevo.")
        } catch (e: Exception) {
            Resultado.Error(TipoError.DESCONOCIDO, e.message ?: "Error al consultar la IA")
        }
    }
}
