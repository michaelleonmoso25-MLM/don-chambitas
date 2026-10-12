package mx.donchambitas.app.ui.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.tema.Blanco
import mx.donchambitas.app.ui.tema.Carbon
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.secundario
import kotlin.math.min
import kotlin.math.roundToInt

/** Cuánto se puede acercar la imagen sobre el tamaño que cubre justo el círculo. */
const val ZOOM_MAXIMO_RECORTE = 4f

/** El círculo ocupa esta fracción del lado corto del área de la imagen. */
private const val FRACCION_CIRCULO = 0.85f

private val GrosorCirculo = 2.dp
private val ColorVelo = Color.Black.copy(alpha = 0.6f)

/** Cuadrado a recortar, en píxeles de la imagen original. */
data class Recorte(val izquierda: Int, val arriba: Int, val lado: Int)

/**
 * Escala que deja la imagen cubriendo justo el círculo: su lado corto mide
 * lo mismo que el diámetro. Por debajo de ella quedaría hueco dentro del círculo.
 */
private fun escalaBase(ancho: Int, alto: Int, diametro: Float): Float = diametro / min(ancho, alto)

/**
 * Lleva el desplazamiento al más cercano que no deja hueco dentro del
 * círculo. El desplazamiento es el del centro de la imagen respecto al del
 * círculo, en píxeles de pantalla.
 */
fun limitarDesplazamiento(
    ancho: Int,
    alto: Int,
    diametro: Float,
    zoom: Float,
    desplazamiento: Offset
): Offset {
    val escala = escalaBase(ancho, alto, diametro) * zoom
    val holguraX = (ancho * escala - diametro) / 2f
    val holguraY = (alto * escala - diametro) / 2f
    return Offset(
        desplazamiento.x.coerceIn(-holguraX, holguraX),
        desplazamiento.y.coerceIn(-holguraY, holguraY)
    )
}

/**
 * El cuadrado de la imagen original que queda dentro del círculo, siempre
 * dentro de la imagen.
 */
fun calcularRecorte(
    ancho: Int,
    alto: Int,
    diametro: Float,
    zoom: Float,
    desplazamiento: Offset
): Recorte {
    val escala = escalaBase(ancho, alto, diametro) * zoom
    val limitado = limitarDesplazamiento(ancho, alto, diametro, zoom, desplazamiento)
    val lado = (diametro / escala).roundToInt().coerceIn(1, min(ancho, alto))
    val centroX = ancho / 2f - limitado.x / escala
    val centroY = alto / 2f - limitado.y / escala
    return Recorte(
        izquierda = (centroX - lado / 2f).roundToInt().coerceIn(0, ancho - lado),
        arriba = (centroY - lado / 2f).roundToInt().coerceIn(0, alto - lado),
        lado = lado
    )
}

/**
 * Recortador de la foto de perfil (S2-T13, decisión 1): la imagen debajo de
 * un círculo fijo, que se mueve con un dedo y se acerca con dos, entre 1× y
 * [ZOOM_MAXIMO_RECORTE]. Va dentro de un Dialog de pantalla completa, no en
 * una ruta (DEC-10).
 *
 * @param alConfirmar Recibe el cuadrado a recortar, en píxeles de [imagen].
 * @param rellenoSistema Las barras del sistema. Dentro de un Dialog no llegan
 * los WindowInsets, así que quien lo abre le pasa los de la actividad.
 */
@Composable
fun RecortadorFoto(
    imagen: ImageBitmap,
    alConfirmar: (Recorte) -> Unit,
    alCancelar: () -> Unit,
    modifier: Modifier = Modifier,
    rellenoSistema: PaddingValues = PaddingValues()
) {
    var zoom by remember { mutableFloatStateOf(1f) }
    var desplazamiento by remember { mutableStateOf(Offset.Zero) }
    var diametro by remember { mutableFloatStateOf(0f) }
    val descripcion = stringResource(R.string.foto_recortador_descripcion)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = rellenoSistema.calculateTopPadding())
    ) {
        Text(
            text = stringResource(R.string.foto_recortador_ayuda),
            style = DonChambitasTema.tipografia.secundario,
            color = Blanco,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(Espaciado.dp16)
        )

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val anchoArea = constraints.maxWidth.toFloat()
            val altoArea = constraints.maxHeight.toFloat()
            val diametroArea = min(anchoArea, altoArea) * FRACCION_CIRCULO
            // El botón "Usar foto" vive fuera de esta área y necesita el diámetro.
            SideEffect { diametro = diametroArea }
            val grosor = with(LocalDensity.current) { GrosorCirculo.toPx() }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = descripcion }
                    .pointerInput(imagen, diametroArea) {
                        detectTransformGestures { _, arrastre, factor, _ ->
                            zoom = (zoom * factor).coerceIn(1f, ZOOM_MAXIMO_RECORTE)
                            desplazamiento = limitarDesplazamiento(
                                imagen.width, imagen.height, diametroArea, zoom, desplazamiento + arrastre
                            )
                        }
                    }
            ) {
                val escala = escalaBase(imagen.width, imagen.height, diametroArea) * zoom
                val anchoDibujo = imagen.width * escala
                val altoDibujo = imagen.height * escala
                val centro = Offset(size.width / 2f, size.height / 2f)
                drawImage(
                    image = imagen,
                    dstOffset = IntOffset(
                        (centro.x + desplazamiento.x - anchoDibujo / 2f).roundToInt(),
                        (centro.y + desplazamiento.y - altoDibujo / 2f).roundToInt()
                    ),
                    dstSize = IntSize(anchoDibujo.roundToInt(), altoDibujo.roundToInt())
                )
                val circulo = Rect(center = centro, radius = diametroArea / 2f)
                val velo = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRect(Rect(Offset.Zero, size))
                    addOval(circulo)
                }
                drawPath(velo, ColorVelo)
                drawCircle(Blanco, radius = diametroArea / 2f, center = centro, style = Stroke(grosor))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Carbon)
                .padding(bottom = rellenoSistema.calculateBottomPadding())
                .padding(Espaciado.dp16),
            horizontalArrangement = Arrangement.spacedBy(Espaciado.dp16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                BotonTexto(
                    texto = stringResource(R.string.mi_cuenta_cancelar),
                    onClick = alCancelar
                )
            }
            BotonPrincipal(
                texto = stringResource(R.string.foto_usar),
                onClick = {
                    alConfirmar(
                        calcularRecorte(imagen.width, imagen.height, diametro, zoom, desplazamiento)
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
