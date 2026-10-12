package mx.donchambitas.app.ui.pantallas

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import mx.donchambitas.app.ui.componentes.Recorte
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * prepararFoto usa Bitmap, que en JVM no existe: se prueba aquí que lo que
 * recibe subirFotoPerfil es un JPEG de 512 x 512 y del cuadrado pedido.
 */
@RunWith(AndroidJUnit4::class)
class FotoPerfilInstrumentadaTest {

    @Test
    fun debeEntregarUnJpegDe512_delCuadradoRecortado() {
        // Mitad izquierda roja y mitad derecha azul: el recorte de la derecha sale azul.
        val original = Bitmap.createBitmap(2000, 1000, Bitmap.Config.ARGB_8888).apply {
            for (x in 0 until 2000) for (y in 0 until 1000) setPixel(x, y, if (x < 1000) Color.RED else Color.BLUE)
        }

        val bytes = prepararFoto(original, Recorte(izquierda = 1000, arriba = 0, lado = 1000))

        assertEquals(0xFF.toByte(), bytes[0])
        assertEquals(0xD8.toByte(), bytes[1])
        val salida = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        assertEquals(LimitesFoto.LADO_SALIDA, salida.width)
        assertEquals(LimitesFoto.LADO_SALIDA, salida.height)
        val centro = salida.getPixel(256, 256)
        assertTrue(Color.blue(centro) > 200 && Color.red(centro) < 60)
        assertTrue(bytes.size < LimitesFoto.BYTES_MAXIMOS)
    }
}
