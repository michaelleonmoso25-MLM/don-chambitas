package mx.donchambitas.app.dominio.repositorio

import mx.donchambitas.app.dominio.modelo.FuncionIa
import mx.donchambitas.app.dominio.modelo.ResultadoIa
import mx.donchambitas.app.util.Resultado

/**
 * Contrato de operaciones para generacion de textos y asistencia con Inteligencia Artificial.
 * Especificado en docs/tecnico/CONTRATOS-API.md.
 */
interface RepositorioIa {
    /**
     * Consume la Edge Function ia-generar con la funcion y el texto de entrada especificados.
     */
    suspend fun generar(funcion: FuncionIa, entrada: String): Resultado<ResultadoIa>

    /**
     * Las llamadas que le quedan hoy al usuario de la sesion, sin gastar ninguna
     * (fn_ia_restantes, DEC-39). P-11 y P-13 lo piden al abrir.
     */
    suspend fun restantesHoy(): Resultado<Int>
}
