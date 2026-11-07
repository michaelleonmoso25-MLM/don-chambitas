package mx.donchambitas.app.dominio.validacion

import java.math.BigDecimal

/**
 * Motivo por el que un campo de P-08 no pasa. Como [FalloTrabajador], es Kotlin
 * puro: la capa ui lo traduce a su recurso validacion_solicitud_*.
 */
enum class FalloSolicitud {
    TITULO_VACIO,
    TITULO_LARGO,
    CATEGORIA_SIN_ELEGIR,
    DESCRIPCION_VACIA,
    DESCRIPCION_LARGA,
    PRESUPUESTO_FORMATO,
    PRESUPUESTO_GRANDE,
    ESTADO_SIN_ELEGIR,
    MUNICIPIO_SIN_ELEGIR
}

/**
 * Topes de solicitudes en basedatos/01_esquema.sql.
 * Especificados en docs/producto/DISENO-SOLICITUDES-BUSQUEDA.md, seccion 4.2.
 */
object LimitesSolicitud {
    /** solicitudes.titulo varchar(120). */
    const val LARGO_MAXIMO_TITULO = 120

    /**
     * La base no limita solicitudes.descripcion (text). Mil caracteres es una propuesta
     * de la pantalla, que el lider debe confirmar (H-44).
     */
    const val LARGO_MAXIMO_DESCRIPCION = 1000
}

/**
 * Reglas del formulario de P-08.
 * Especificadas en docs/producto/DISENO-SOLICITUDES-BUSQUEDA.md, seccion 4.2.
 *
 * Cada funcion devuelve el fallo del campo, o nulo si esta bien. El presupuesto
 * se lee igual que el precio de P-13, asi que reusa [ValidacionesTrabajador].
 */
object ValidacionesSolicitud {

    fun validarTitulo(titulo: String): FalloSolicitud? {
        val limpio = titulo.trim()
        return when {
            limpio.isEmpty() -> FalloSolicitud.TITULO_VACIO
            limpio.length > LimitesSolicitud.LARGO_MAXIMO_TITULO -> FalloSolicitud.TITULO_LARGO
            else -> null
        }
    }

    fun validarCategoria(categoriaId: Int?): FalloSolicitud? =
        if (categoriaId == null) FalloSolicitud.CATEGORIA_SIN_ELEGIR else null

    fun validarDescripcion(descripcion: String): FalloSolicitud? {
        val limpia = descripcion.trim()
        return when {
            limpia.isEmpty() -> FalloSolicitud.DESCRIPCION_VACIA
            limpia.length > LimitesSolicitud.LARGO_MAXIMO_DESCRIPCION -> FalloSolicitud.DESCRIPCION_LARGA
            else -> null
        }
    }

    /** Opcional (HU-12): vacio es valido. Si viene, un numero >= 0, con hasta dos decimales, que quepa en numeric(10,2). */
    fun validarPresupuesto(presupuesto: String): FalloSolicitud? =
        when (ValidacionesTrabajador.validarPrecio(presupuesto)) {
            null -> null
            FalloTrabajador.PRECIO_GRANDE -> FalloSolicitud.PRESUPUESTO_GRANDE
            else -> FalloSolicitud.PRESUPUESTO_FORMATO
        }

    fun validarEstado(estadoId: Int?): FalloSolicitud? =
        if (estadoId == null) FalloSolicitud.ESTADO_SIN_ELEGIR else null

    /** Sin estado no hay municipio que elegir: el fallo de estado ya lo marca. */
    fun validarMunicipio(estadoId: Int?, municipioId: Int?): FalloSolicitud? =
        if (estadoId != null && municipioId == null) FalloSolicitud.MUNICIPIO_SIN_ELEGIR else null

    /** Vacio es nulo (sin presupuesto); lo demas, con punto y dos decimales. */
    fun normalizarPresupuesto(presupuesto: String): BigDecimal? =
        if (presupuesto.isBlank()) null else ValidacionesTrabajador.normalizarPrecio(presupuesto)
}
