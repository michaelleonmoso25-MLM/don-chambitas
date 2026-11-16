package mx.donchambitas.app.datos.repositorio

import dagger.Lazy
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.statement.bodyAsText
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mx.donchambitas.app.dominio.ia.EntradaIa
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
            resultadoDeCuerpo(json, cuerpo)
        } catch (e: CancellationException) {
            throw e
        } catch (e: RestException) {
            errorDeIa(e.statusCode, e.error)
        } catch (e: HttpRequestException) {
            Resultado.Error(TipoError.RED, "Sin conexión. Revisa tu conexión e intenta de nuevo.")
        } catch (e: Exception) {
            Resultado.Error(TipoError.DESCONOCIDO, e.message ?: "Error al consultar la IA")
        }
    }
    override suspend fun restantesHoy(): Resultado<Int> {
        return try {
            Resultado.Exito(supabase.postgrest.rpc("fn_ia_restantes").decodeAs<Int>())
        } catch (e: CancellationException) {
            throw e
        } catch (e: RestException) {
            Resultado.Error(TipoError.SERVIDOR, e.error)
        } catch (e: HttpRequestException) {
            Resultado.Error(TipoError.RED, "Sin conexión. Revisa tu conexión e intenta de nuevo.")
        } catch (e: Exception) {
            Resultado.Error(TipoError.DESCONOCIDO, e.message ?: "Error al consultar el cupo de IA")
        }
    }
}

/**
 * Traduce el cuerpo de un 200 de ia-generar. Un 200 cuyo texto sea exactamente
 * NO_PUEDO_REDACTAR es el mismo rechazo que el 422: nunca se pega en un campo (H-30).
 */
internal fun resultadoDeCuerpo(json: Json, cuerpo: String): Resultado<ResultadoIa> {
    val dto = json.decodeFromString<RespuestaIaDto>(cuerpo)
    if (dto.resultado.trim() == EntradaIa.NO_PUEDO_REDACTAR) return errorDeIa(422, "")
    return Resultado.Exito(
        ResultadoIa(
            resultado = dto.resultado,
            desdeCache = dto.desdeCache,
            llamadasRestantesHoy = dto.llamadasRestantesHoy
        )
    )
}

/**
 * La tabla de CONTRATOS-API.md. El 400 es un bug nuestro y no se disfraza (DESCONOCIDO);
 * el 422 es el rechazo de la IA (IA_RECHAZADA, H-37).
 */
internal fun errorDeIa(codigoHttp: Int, detalle: String): Resultado.Error = when (codigoHttp) {
    400 -> Resultado.Error(TipoError.DESCONOCIDO, "Petición no válida")
    401 -> Resultado.Error(TipoError.AUTENTICACION, "Inicia sesión para usar la IA")
    422 -> Resultado.Error(TipoError.IA_RECHAZADA, "No puedo redactar una descripción con esos datos")
    429 -> Resultado.Error(TipoError.LIMITE_IA, "Alcanzaste el límite de hoy")
    503 -> Resultado.Error(TipoError.SERVIDOR, "El servicio de IA no está disponible en este momento")
    else -> Resultado.Error(TipoError.SERVIDOR, detalle)
}
