package mx.donchambitas.app.datos.falso

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import mx.donchambitas.app.dominio.modelo.FuncionIa
import mx.donchambitas.app.dominio.modelo.ResultadoIa
import mx.donchambitas.app.dominio.repositorio.RepositorioIa
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

@Singleton
class RepositorioIaFalso @Inject constructor() : RepositorioIa {

    var retrasoMs: Long = 300L
    var errorForzado: TipoError? = null
    var llamadasRestantes: Int = 8

    private suspend fun verificarSimulacion(): Resultado.Error? {
        if (retrasoMs > 0) delay(retrasoMs)
        return errorForzado?.let { Resultado.Error(it, "Error simulado en RepositorioIa: $it") }
    }

    override suspend fun generar(
        funcion: FuncionIa,
        entrada: String
    ): Resultado<ResultadoIa> {
        verificarSimulacion()?.let { return it }

        if (llamadasRestantes <= 0) {
            return Resultado.Error(TipoError.LIMITE_IA, "Alcanzaste el limite de hoy")
        }

        llamadasRestantes--

        val textoGenerado = when (funcion) {
            // H-31: textos fijos que cumplen PROMPT-REDACCION.md (40 a 80 palabras, primera
            // persona, sin garantias, marcas ni materiales inventados) y no intercalan la entrada cruda.
            FuncionIa.REDACTAR_PERFIL ->
                "Me dedico a mi oficio con cuidado y me gusta explicar lo que voy a hacer antes de empezar. Escucho lo que necesitas y busco una buena forma de resolverlo, con claridad y buen trato. Si tienes un trabajo pendiente, cuéntame de qué se trata y lo revisamos juntos para que sepas qué esperar."

            FuncionIa.REDACTAR_SERVICIO ->
                "Me encargo de este servicio con cuidado y atención a los detalles. Antes de empezar te explico en qué consiste el trabajo y resuelvo tus dudas, para que sepas qué esperar. Si necesitas este servicio, cuéntame tu situación y lo revisamos juntos para encontrar una buena solución."

            FuncionIa.CATEGORIZAR ->
                "Plomería"

            FuncionIa.SUGERIR ->
                "Sugerencia: describe el problema con el mayor detalle posible e incluye medidas aproximadas para recibir cotizaciones más precisas."
        }

        return Resultado.Exito(
            ResultadoIa(
                resultado = textoGenerado,
                desdeCache = false,
                llamadasRestantesHoy = llamadasRestantes
            )
        )
    }
}
