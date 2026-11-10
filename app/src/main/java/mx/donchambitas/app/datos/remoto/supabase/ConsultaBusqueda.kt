package mx.donchambitas.app.datos.remoto.supabase

import java.text.Normalizer
import mx.donchambitas.app.dominio.modelo.OrdenBusqueda

/**
 * Las piezas puras de la consulta de buscarTrabajadores (CONTRATOS-API.md,
 * "Busqueda y catalogos"): sin red y sin supabase-kt, para probarlas sin ellos.
 */

/** Trabajadores por pagina. El ViewModel (S4-T07) usa el mismo tamano para saber cual es la ultima. */
const val TAMANO_PAGINA_BUSQUEDA = 20

/** Todas las columnas de la vista menos texto_busqueda, que solo se filtra. */
const val COLUMNAS_BUSQUEDA =
    "trabajador_id,nombre,apellidos,foto_url,titulo,disponible,estado_id,municipio_id," +
        "estado,municipio,promedio,total_resenas,servicios_activos,precio_desde,categorias,creado_en"

/**
 * Minusculas y sin acentos, igual que public.fn_normalizar del lado de la base
 * (DEC-36). Quitar los acentos solo de lo que se escribe no alcanzaria: el dato
 * guardado los conserva, por eso la vista trae su propia copia normalizada.
 */
fun normalizarTexto(texto: String): String =
    Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replace(MARCAS_COMBINANTES, "")
        .lowercase()

private val MARCAS_COMBINANTES = Regex("\\p{Mn}+")
private val ESPACIOS = Regex("\\s+")

/**
 * Las palabras que el texto de P-06 exige, ya normalizadas. Cada una tiene que
 * aparecer en nombre, apellidos o titulo, en cualquier orden. Un texto nulo o
 * vacio no es un filtro y devuelve una lista vacia.
 */
fun palabrasBusqueda(texto: String?): List<String> =
    normalizarTexto(texto.orEmpty())
        .trim()
        .split(ESPACIOS)
        .filter { it.isNotEmpty() }

/** Antepone la barra a los comodines de like, para que "50%" o "a_b" se busquen tal cual. */
fun escaparComodines(palabra: String): String =
    palabra.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

/** El patron de ilike de una palabra: contenida en cualquier parte del texto. */
fun patronContiene(palabra: String): String = "%${escaparComodines(palabra)}%"

/** Una columna del order, con su direccion. Los nulos van siempre al final. */
data class CriterioOrden(val columna: String, val ascendente: Boolean)

/**
 * El orden de cada opcion, con desempate fijo: con tantos trabajadores en 0 de
 * promedio, sin el las paginas pueden repetir o saltarse filas. Sin orden
 * elegido, el de por omision es el promedio.
 */
fun criteriosDeOrden(orden: OrdenBusqueda?): List<CriterioOrden> = when (orden ?: OrdenBusqueda.PROMEDIO) {
    OrdenBusqueda.PROMEDIO -> listOf(
        CriterioOrden("promedio", ascendente = false),
        CriterioOrden("creado_en", ascendente = false),
        CriterioOrden("trabajador_id", ascendente = true)
    )
    OrdenBusqueda.CREADO_EN -> listOf(
        CriterioOrden("creado_en", ascendente = false),
        CriterioOrden("trabajador_id", ascendente = true)
    )
    OrdenBusqueda.PRECIO_DESDE -> listOf(
        CriterioOrden("precio_desde", ascendente = true),
        CriterioOrden("creado_en", ascendente = false),
        CriterioOrden("trabajador_id", ascendente = true)
    )
}

/**
 * Los extremos de range para una pagina (empieza en 1), ambos incluidos.
 * Una pagina menor que 1 es un error de quien llama y se trata como la 1.
 */
fun rangoDePagina(pagina: Int): LongRange {
    val numero = pagina.coerceAtLeast(1)
    val desde = (numero - 1).toLong() * TAMANO_PAGINA_BUSQUEDA
    return desde until desde + TAMANO_PAGINA_BUSQUEDA
}
