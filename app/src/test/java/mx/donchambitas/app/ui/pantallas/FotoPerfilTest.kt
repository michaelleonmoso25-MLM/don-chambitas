package mx.donchambitas.app.ui.pantallas

import androidx.compose.ui.geometry.Offset
import mx.donchambitas.app.ui.componentes.Recorte
import mx.donchambitas.app.ui.componentes.calcularRecorte
import mx.donchambitas.app.ui.componentes.limitarDesplazamiento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FotoPerfilTest {

    @Test
    fun debeAceptar_losTresTiposDeLaCubetaHastaCincoMegas() {
        assertNull(validarImagen("image/jpeg", 1_000))
        assertNull(validarImagen("image/png", LimitesFoto.BYTES_MAXIMOS))
        assertNull(validarImagen("IMAGE/WEBP", 0))
    }

    @Test
    fun debeRechazarPorPeso_cuandoPasaDeCincoMegas() {
        assertEquals(FalloFoto.PESADA, validarImagen("image/jpeg", LimitesFoto.BYTES_MAXIMOS + 1))
    }

    @Test
    fun debeRechazarPorTipo_antesQuePorPeso() {
        assertEquals(FalloFoto.TIPO, validarImagen("image/gif", 10))
        assertEquals(FalloFoto.TIPO, validarImagen("image/gif", LimitesFoto.BYTES_MAXIMOS * 2))
        assertEquals(FalloFoto.TIPO, validarImagen(null, 10))
    }

    @Test
    fun debeElegirElMuestreo_queDejaElLadoMayorEnElObjetivo() {
        assertEquals(1, calcularMuestreo(2048, 1000, 2048))
        assertEquals(2, calcularMuestreo(4000, 3000, 2048))
        assertEquals(4, calcularMuestreo(8160, 6120, 2048))
    }

    @Test
    fun debeRecortarElCuadradoCentral_sinZoomNiDesplazamiento() {
        // Horizontal de 2000 x 1000 con un círculo de 500 px: escala 0.5.
        assertEquals(Recorte(izquierda = 500, arriba = 0, lado = 1000), calcularRecorte(2000, 1000, 500f, 1f, Offset.Zero))
        // Vertical.
        assertEquals(Recorte(izquierda = 0, arriba = 500, lado = 1000), calcularRecorte(1000, 2000, 500f, 1f, Offset.Zero))
    }

    @Test
    fun debeAchicarElLado_cuandoSeAcerca() {
        val recorte = calcularRecorte(2000, 1000, 500f, 2f, Offset.Zero)

        assertEquals(Recorte(izquierda = 750, arriba = 250, lado = 500), recorte)
    }

    @Test
    fun debeMoverElRecorte_alContrarioDelArrastre() {
        // Arrastrar la imagen 100 px a la derecha muestra lo que estaba a su izquierda: 200 px de imagen.
        val recorte = calcularRecorte(2000, 1000, 500f, 1f, Offset(100f, 0f))

        assertEquals(Recorte(izquierda = 300, arriba = 0, lado = 1000), recorte)
    }

    @Test
    fun noDebeSalirseDeLaImagen_cuandoElArrastrePasaDelBorde() {
        val limitado = limitarDesplazamiento(2000, 1000, 500f, 1f, Offset(10_000f, 10_000f))
        assertEquals(Offset(250f, 0f), limitado)

        val recorte = calcularRecorte(2000, 1000, 500f, 1f, Offset(10_000f, -10_000f))
        assertEquals(Recorte(izquierda = 0, arriba = 0, lado = 1000), recorte)
    }
}
