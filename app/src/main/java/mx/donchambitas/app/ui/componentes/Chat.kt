package mx.donchambitas.app.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.tema.Arena
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.Cafe
import mx.donchambitas.app.ui.tema.Carbon
import mx.donchambitas.app.ui.tema.Crema
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Error
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.Mostaza
import mx.donchambitas.app.ui.tema.MostazaOscuro
import mx.donchambitas.app.ui.tema.cuerpo
import mx.donchambitas.app.ui.tema.cuerpoFuerte
import mx.donchambitas.app.ui.tema.secundario

/**
 * Fila de la bandeja de chats (P-15), HU-25.
 * Especificada en docs/producto/DISENO-POSTULACION-CONTACTO-RESENAS.md, seccion 4.1 (H-49).
 *
 * @param solicitudTitulo El titulo de la solicitud de la que viene el hilo; nulo en el hilo suelto.
 * @param sinLeer Si es mayor que cero, el nombre va en negrita y la fila lleva un punto.
 */
@Composable
fun TarjetaConversacion(
    nombre: String,
    ultimoMensaje: String?,
    hora: String?,
    sinLeer: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    solicitudTitulo: String? = null
) {
    val tieneSinLeer = sinLeer > 0
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Arena),
        border = BorderStroke(1.dp, Borde)
    ) {
        Row(
            modifier = Modifier.padding(Espaciado.dp12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Espaciado.dp12)
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Crema),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = Cafe)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombre,
                    style = DonChambitasTema.tipografia.cuerpoFuerte,
                    fontWeight = if (tieneSinLeer) FontWeight.Bold else FontWeight.Normal,
                    color = Carbon,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (solicitudTitulo != null) {
                    Text(
                        text = solicitudTitulo,
                        style = DonChambitasTema.tipografia.secundario,
                        color = MostazaOscuro,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (ultimoMensaje != null) {
                    Text(
                        text = ultimoMensaje,
                        style = DonChambitasTema.tipografia.cuerpo,
                        color = if (tieneSinLeer) Carbon else Cafe,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(Espaciado.dp4)) {
                if (hora != null) {
                    Text(text = hora, style = DonChambitasTema.tipografia.secundario, color = Cafe)
                }
                if (tieneSinLeer) {
                    Box(
                        modifier = Modifier.size(10.dp).clip(CircleShape).background(Mostaza)
                    )
                }
            }
        }
    }
}

/**
 * Un mensaje de P-16: los propios a la derecha en mostaza, los de la otra persona a la izquierda en
 * arena. Texto plano (DEC-04).
 * Especificada en docs/producto/DISENO-POSTULACION-CONTACTO-RESENAS.md, seccion 5.1 (H-49).
 *
 * @param estadoEnvio Texto bajo el propio mensaje: "Enviando…", "Leído" o el aviso de reintento.
 * @param fallido Si no se pudo enviar: el mensaje se pinta con borde de error y se toca para reintentar.
 */
@Composable
fun BurbujaMensaje(
    contenido: String,
    hora: String,
    esMio: Boolean,
    modifier: Modifier = Modifier,
    estadoEnvio: String? = null,
    fallido: Boolean = false,
    alReintentar: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (esMio) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (esMio) Alignment.End else Alignment.Start,
            modifier = Modifier.padding(start = if (esMio) 48.dp else 0.dp, end = if (esMio) 0.dp else 48.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (esMio) Mostaza else Arena)
                    .let { if (fallido && alReintentar != null) it.clickable(onClick = alReintentar) else it }
                    .padding(horizontal = Espaciado.dp12, vertical = Espaciado.dp8)
            ) {
                Text(text = contenido, style = DonChambitasTema.tipografia.cuerpo, color = Carbon)
            }
            Text(
                text = if (estadoEnvio != null) "$hora · $estadoEnvio" else hora,
                style = DonChambitasTema.tipografia.secundario,
                color = if (fallido) Error else Cafe,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/**
 * Campo de mensaje de P-16: varias lineas (hasta 4) y un boton de enviar apagado con el texto en
 * blanco. Sin adjuntar, foto ni llamada: el chat es de texto plano (DEC-04).
 * Especificado en docs/producto/DISENO-POSTULACION-CONTACTO-RESENAS.md, seccion 5.1 (H-49).
 */
@Composable
fun CampoMensaje(
    valor: String,
    alCambiarValor: (String) -> Unit,
    alEnviar: () -> Unit,
    puedeEnviar: Boolean,
    marcadorPosicion: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Crema)
            .padding(horizontal = Espaciado.margenPantalla, vertical = Espaciado.dp8),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(Espaciado.dp8)
    ) {
        CampoTexto(
            valor = valor,
            alCambiarValor = alCambiarValor,
            marcadorPosicion = marcadorPosicion,
            lineasMaximas = 4,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = alEnviar, enabled = puedeEnviar) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.chat_enviar),
                tint = if (puedeEnviar) MostazaOscuro else Borde
            )
        }
    }
}
