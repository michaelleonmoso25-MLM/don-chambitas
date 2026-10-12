package mx.donchambitas.app.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Plumbing
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.EstadoSolicitud
import mx.donchambitas.app.ui.tema.Advertencia
import mx.donchambitas.app.ui.tema.Arena
import mx.donchambitas.app.ui.tema.Blanco
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.Cafe
import mx.donchambitas.app.ui.tema.Carbon
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Error
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.Exito
import mx.donchambitas.app.ui.tema.FormaChip
import mx.donchambitas.app.ui.tema.Mostaza
import mx.donchambitas.app.ui.tema.pie
import mx.donchambitas.app.ui.tema.secundario

private val EspacioIconoChip = 6.dp
private val RellenoHorizontalChip = 16.dp
private val RellenoVerticalChip = 8.dp
private val TamanoIconoChip = 18.dp

private val FormaEtiquetaEstado = RoundedCornerShape(8.dp)
private val RellenoHorizontalEtiqueta = 8.dp
private val RellenoVerticalEtiqueta = 4.dp

/**
 * Chip de categoria segun DISENO.md.
 * Fondo Mostaza con texto Carbon cuando esta activo; fondo Arena con texto Carbon/Cafe cuando no.
 */
@Composable
fun ChipCategoria(
    texto: String,
    seleccionado: Boolean = false,
    alSeleccionar: () -> Unit = {},
    icono: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    val colorFondo = if (seleccionado) Mostaza else Arena
    val colorContenido = Carbon
    val borde = if (seleccionado) null else BorderStroke(1.dp, Borde)

    Surface(
        onClick = alSeleccionar,
        shape = FormaChip,
        color = colorFondo,
        border = borde,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(
                horizontal = RellenoHorizontalChip,
                vertical = RellenoVerticalChip
            )
        ) {
            if (icono != null) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = if (seleccionado) Carbon else Cafe,
                    modifier = Modifier
                        .size(TamanoIconoChip)
                        .padding(end = EspacioIconoChip)
                )
            }
            Text(
                text = texto,
                style = DonChambitasTema.tipografia.secundario,
                color = colorContenido,
                fontWeight = if (seleccionado) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

/**
 * Insignia de estado de solicitud segun DISENO.md.
 * abierta: Exito (texto Blanco)
 * asignada: Advertencia (texto Carbon, WCAG 5.15:1)
 * cerrada: Cafe (texto Blanco)
 * cancelada: Error (texto Blanco, WCAG 5.62:1)
 */
@Composable
fun EtiquetaEstado(
    estado: EstadoSolicitud,
    texto: String? = null,
    modifier: Modifier = Modifier
) {
    val (colorFondo, colorTexto, textoRecurso) = when (estado) {
        EstadoSolicitud.ABIERTA -> Triple(Exito, Blanco, R.string.estado_abierta)
        EstadoSolicitud.ASIGNADA -> Triple(Advertencia, Carbon, R.string.estado_asignada)
        EstadoSolicitud.CERRADA -> Triple(Cafe, Blanco, R.string.estado_cerrada)
        EstadoSolicitud.CANCELADA -> Triple(Error, Blanco, R.string.estado_cancelada)
    }

    EtiquetaEstado(
        texto = texto ?: stringResource(textoRecurso),
        colorFondo = colorFondo,
        colorTexto = colorTexto,
        modifier = modifier
    )
}

/**
 * Insignia con colores dados, para lo que no es un estado de solicitud: el rol
 * de P-18 va en Advertencia para el trabajador y en Mostaza para el cliente
 * (WIREFRAMES.md, P-18), los dos con texto Carbon.
 */
@Composable
fun EtiquetaEstado(
    texto: String,
    colorFondo: Color,
    colorTexto: Color,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(color = colorFondo, shape = FormaEtiquetaEstado)
            .padding(
                horizontal = RellenoHorizontalEtiqueta,
                vertical = RellenoVerticalEtiqueta
            )
    ) {
        Text(
            text = texto,
            style = DonChambitasTema.tipografia.pie,
            color = colorTexto,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Sobrecarga de EtiquetaEstado que acepta el valor en String.
 */
@Composable
fun EtiquetaEstado(
    estadoTexto: String,
    texto: String? = null,
    modifier: Modifier = Modifier
) {
    val estado = EstadoSolicitud.desdeValor(estadoTexto) ?: EstadoSolicitud.ABIERTA
    EtiquetaEstado(
        estado = estado,
        texto = texto ?: estadoTexto.replaceFirstChar { it.uppercase() },
        modifier = modifier
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFDF8)
@Composable
private fun PreviaChipActivo() {
    DonChambitasTema {
        ChipCategoria(
            texto = "Plomería",
            seleccionado = true,
            icono = Icons.Outlined.Plumbing
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFDF8)
@Composable
private fun PreviaChipInactivo() {
    DonChambitasTema {
        ChipCategoria(
            texto = "Electricidad",
            seleccionado = false
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFDF8)
@Composable
private fun PreviaEtiquetasEstado() {
    DonChambitasTema {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EtiquetaEstado(estado = EstadoSolicitud.ABIERTA)
            EtiquetaEstado(estado = EstadoSolicitud.ASIGNADA)
            EtiquetaEstado(estado = EstadoSolicitud.CERRADA)
            EtiquetaEstado(estado = EstadoSolicitud.CANCELADA)
        }
    }
}
