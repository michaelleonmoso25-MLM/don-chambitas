package mx.donchambitas.app.ui.pantallas

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.OpenableColumns
import androidx.annotation.StringRes
import java.io.ByteArrayOutputStream
import java.io.InputStream
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.componentes.Recorte

/** Por qué se rechaza una imagen antes de recortarla (HU-07). */
enum class FalloFoto { PESADA, TIPO }

/**
 * Topes de la foto de perfil. Los de entrada son los de la cubeta `perfiles`
 * en basedatos/03_almacenamiento.sql (file_size_limit y allowed_mime_types):
 * si aquellos cambian, estos cambian el mismo día.
 */
object LimitesFoto {
    const val BYTES_MAXIMOS = 5_242_880L
    val TIPOS_ADMITIDOS = setOf("image/jpeg", "image/png", "image/webp")

    /** Lo que sube siempre es un JPEG cuadrado de este lado (S2-T13). */
    const val LADO_SALIDA = 512
    const val CALIDAD_JPEG = 85

    /** Lado mayor con que se decodifica para recortar: sobra para 512 y cabe en memoria. */
    const val LADO_DECODIFICADO = 2048
}

/** Primero el tipo y luego el peso: un GIF de 6 MB se rechaza por ser GIF. */
fun validarImagen(tipoMime: String?, bytes: Long): FalloFoto? = when {
    tipoMime?.lowercase() !in LimitesFoto.TIPOS_ADMITIDOS -> FalloFoto.TIPO
    bytes > LimitesFoto.BYTES_MAXIMOS -> FalloFoto.PESADA
    else -> null
}

@StringRes
fun FalloFoto.mensaje(): Int = when (this) {
    FalloFoto.PESADA -> R.string.foto_pesada
    FalloFoto.TIPO -> R.string.foto_tipo
}

/**
 * El menor inSampleSize, potencia de 2, que deja el lado mayor en
 * [ladoObjetivo] o menos.
 */
fun calcularMuestreo(ancho: Int, alto: Int, ladoObjetivo: Int): Int {
    var muestreo = 1
    while (maxOf(ancho, alto) / muestreo > ladoObjetivo) muestreo *= 2
    return muestreo
}

/** Tipo y tamaño de lo que eligió el usuario, sin cargarlo en memoria. */
fun ContentResolver.validarImagen(uri: Uri): FalloFoto? =
    validarImagen(getType(uri), tamanoDe(uri))

private fun ContentResolver.tamanoDe(uri: Uri): Long {
    val declarado = query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getLong(0) else null
    }
    if (declarado != null) return declarado
    // Hay proveedores que no lo declaran: se cuenta, sin pasar del tope.
    return openInputStream(uri)?.use { it.contarHasta(LimitesFoto.BYTES_MAXIMOS + 1) } ?: 0L
}

private fun InputStream.contarHasta(tope: Long): Long {
    val bufer = ByteArray(8192)
    var total = 0L
    while (total < tope) {
        val leidos = read(bufer)
        if (leidos < 0) break
        total += leidos
    }
    return total
}

/**
 * Decodifica reducida y derecha: la cámara guarda de lado y anota el giro en
 * EXIF. Devuelve null si no es una imagen que Android sepa leer.
 */
fun ContentResolver.decodificarFoto(uri: Uri): Bitmap? {
    // Con inJustDecodeBounds, decodeStream siempre devuelve null: lo que
    // importa son las medidas que deja en las opciones.
    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    val flujo = openInputStream(uri) ?: return null
    flujo.use { BitmapFactory.decodeStream(it, null, limites) }
    if (limites.outWidth <= 0 || limites.outHeight <= 0) return null

    val opciones = BitmapFactory.Options().apply {
        inSampleSize = calcularMuestreo(limites.outWidth, limites.outHeight, LimitesFoto.LADO_DECODIFICADO)
    }
    val bitmap = openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) } ?: return null
    val grados = openInputStream(uri)?.use { gradosDeExif(it) } ?: 0
    if (grados == 0) return bitmap
    val giro = Matrix().apply { postRotate(grados.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, giro, true)
}

private fun gradosDeExif(flujo: InputStream): Int =
    when (ExifInterface(flujo).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }

/** Recorta, escala a [LimitesFoto.LADO_SALIDA] y comprime a JPEG: lo que recibe subirFotoPerfil. */
fun prepararFoto(bitmap: Bitmap, recorte: Recorte): ByteArray {
    val cuadrado = Bitmap.createBitmap(bitmap, recorte.izquierda, recorte.arriba, recorte.lado, recorte.lado)
    val salida = Bitmap.createScaledBitmap(cuadrado, LimitesFoto.LADO_SALIDA, LimitesFoto.LADO_SALIDA, true)
    return ByteArrayOutputStream().use { flujo ->
        salida.compress(Bitmap.CompressFormat.JPEG, LimitesFoto.CALIDAD_JPEG, flujo)
        flujo.toByteArray()
    }
}
