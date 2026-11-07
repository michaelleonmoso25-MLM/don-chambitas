package mx.donchambitas.app.dominio.ia

import mx.donchambitas.app.dominio.validacion.ValidacionesTrabajador

/**
 * Arma la entrada que la aplicacion le manda a RepositorioIa.generar.
 * El formato es el de docs/producto/PROMPT-REDACCION.md, seccion 2: renglones
 * "Etiqueta: valor", siempre con las mismas etiquetas y en el mismo orden. Asi dos
 * peticiones con los mismos datos producen exactamente el mismo texto y la cache
 * del proxy las reconoce (HU-31).
 *
 * Nada de nombre, telefono, ubicacion ni precio: lo que no viaja a OpenAI no se
 * puede filtrar.
 */
object EntradaIa {

    /** La respuesta fija con la que el modelo dice que la entrada no sirve (PROMPT-REDACCION.md, 4). */
    const val NO_PUEDO_REDACTAR = "NO_PUEDO_REDACTAR"

    private const val ETIQUETA_OFICIO = "Oficio"
    private const val ETIQUETA_ANIOS = "Años de experiencia"
    private const val ETIQUETA_HABILIDADES = "Habilidades"
    private const val ETIQUETA_SERVICIO = "Servicio"
    private const val ETIQUETA_CATEGORIA = "Categoría"
    private const val ETIQUETA_BORRADOR = "Borrador"

    private val espacios = Regex("\\s+")

    /**
     * Entrada de redactar_perfil (P-11). Los anios solo van si pasan validarExperiencia,
     * como entero y sin ceros a la izquierda; las habilidades, en el orden de la
     * pantalla y separadas por "; ".
     */
    fun perfil(titulo: String, experienciaAnios: String, habilidades: List<String>): String {
        val anios = experienciaAnios.trim()
            .takeIf { it.isNotEmpty() && ValidacionesTrabajador.validarExperiencia(it) == null }
            ?.toInt()
            ?.toString()
            .orEmpty()
        val lista = ValidacionesTrabajador.normalizarHabilidades(habilidades)
            .map(::normalizar)
            .joinToString("; ")
        return armar(
            ETIQUETA_OFICIO to titulo,
            ETIQUETA_ANIOS to anios,
            ETIQUETA_HABILIDADES to lista
        )
    }

    /**
     * Entrada de redactar_servicio (P-13). El borrador solo va si no esta en blanco.
     *
     * @param categoria El nombre de la categoria tal como llega de obtenerCategorias().
     */
    fun servicio(titulo: String, categoria: String, borrador: String): String = armar(
        ETIQUETA_SERVICIO to titulo,
        ETIQUETA_CATEGORIA to categoria,
        ETIQUETA_BORRADOR to borrador
    )

    /** Un renglon cuyo valor queda vacio no se manda, ni su etiqueta. Sin salto al final. */
    private fun armar(vararg renglones: Pair<String, String>): String =
        renglones
            .map { (etiqueta, valor) -> etiqueta to normalizar(valor) }
            .filter { (_, valor) -> valor.isNotEmpty() }
            .joinToString("\n") { (etiqueta, valor) -> "$etiqueta: $valor" }

    /** trim() y los espacios y saltos de linea de en medio, a un solo espacio. */
    private fun normalizar(valor: String): String = valor.trim().replace(espacios, " ")
}
