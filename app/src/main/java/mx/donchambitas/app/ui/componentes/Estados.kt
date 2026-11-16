package mx.donchambitas.app.ui.componentes

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.tema.Cafe
import mx.donchambitas.app.ui.tema.Carbon
import mx.donchambitas.app.ui.tema.Crema
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Error
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.Mostaza
import mx.donchambitas.app.ui.tema.cuerpo
import mx.donchambitas.app.ui.tema.subtitulo
import mx.donchambitas.app.util.TipoError

private val TamanoIndicadorCarga = 48.dp
private val GrosorIndicadorCarga = 4.dp
private val TamanoIconoEstado = 56.dp

/**
 * Devuelve el recurso de cadena con el mensaje de accion correspondiente al TipoError.
 * Cada valor tiene un mensaje unico que orienta al usuario sobre que hacer.
 */
@StringRes
fun obtenerMensajeErrorRes(tipo: TipoError): Int = when (tipo) {
    TipoError.RED -> R.string.error_red
    TipoError.AUTENTICACION -> R.string.error_autenticacion
    TipoError.VALIDACION -> R.string.error_validacion
    TipoError.CORREO_DUPLICADO -> R.string.error_correo_duplicado
    TipoError.LIMITE_IA -> R.string.error_limite_ia
    TipoError.IA_RECHAZADA -> R.string.error_ia_rechazada
    TipoError.SERVIDOR -> R.string.error_servidor
    TipoError.DESCONOCIDO -> R.string.error_desconocido
}

/**
 * Devuelve el recurso de cadena con el titulo correspondiente al TipoError.
 */
@StringRes
fun obtenerTituloErrorRes(tipo: TipoError): Int = when (tipo) {
    TipoError.RED -> R.string.error_titulo_red
    TipoError.AUTENTICACION -> R.string.error_titulo_autenticacion
    TipoError.VALIDACION -> R.string.error_titulo_validacion
    TipoError.CORREO_DUPLICADO -> R.string.error_titulo_correo_duplicado
    TipoError.LIMITE_IA -> R.string.error_titulo_limite_ia
    TipoError.IA_RECHAZADA -> R.string.error_titulo_ia_rechazada
    TipoError.SERVIDOR -> R.string.error_titulo_servidor
    TipoError.DESCONOCIDO -> R.string.error_titulo_desconocido
}

/**
 * Componente de carga con indicador circular centrado en color Mostaza segun DISENO.md.
 */
@Composable
fun Cargando(
    modifier: Modifier = Modifier,
    mensaje: String? = null
) {
    val descripcionCargando = stringResource(R.string.cargando)
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = descripcionCargando },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = Mostaza,
                strokeWidth = GrosorIndicadorCarga,
                modifier = Modifier.size(TamanoIndicadorCarga)
            )
            if (mensaje != null) {
                Spacer(modifier = Modifier.height(Espaciado.dp16))
                Text(
                    text = mensaje,
                    style = DonChambitasTema.tipografia.cuerpo,
                    color = Cafe,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Componente para mostrar estado vacio con icono, titulo, mensaje y boton opcional.
 * Se usa cuando no hay resultados de busqueda o no hay elementos publicados todavia.
 */
@Composable
fun EstadoVacio(
    titulo: String = stringResource(R.string.estado_vacio_titulo),
    mensaje: String = stringResource(R.string.estado_vacio_mensaje),
    icono: ImageVector = Icons.Outlined.Inbox,
    textoBoton: String? = null,
    alHacerClickBoton: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(Espaciado.margenPantalla),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = Cafe,
                modifier = Modifier.size(TamanoIconoEstado)
            )
            Spacer(modifier = Modifier.height(Espaciado.dp16))
            Text(
                text = titulo,
                style = DonChambitasTema.tipografia.subtitulo,
                color = Carbon,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Espaciado.dp8))
            Text(
                text = mensaje,
                style = DonChambitasTema.tipografia.cuerpo,
                color = Cafe,
                textAlign = TextAlign.Center
            )
            if (textoBoton != null && alHacerClickBoton != null) {
                Spacer(modifier = Modifier.height(Espaciado.dp24))
                BotonPrincipal(
                    texto = textoBoton,
                    onClick = alHacerClickBoton,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Componente para mostrar estado de error con icono, mensaje y boton de reintentar.
 * El mensaje proviene de TipoError y orienta sobre que hacer, no solo que fallo.
 */
@Composable
fun EstadoError(
    tipoError: TipoError,
    alReintentar: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    titulo: String? = null,
    mensajePersonalizado: String? = null
) {
    val tituloFinal = titulo ?: stringResource(obtenerTituloErrorRes(tipoError))
    val mensajeFinal = mensajePersonalizado ?: stringResource(obtenerMensajeErrorRes(tipoError))

    EstadoErrorBase(
        titulo = tituloFinal,
        mensaje = mensajeFinal,
        alReintentar = alReintentar,
        modifier = modifier
    )
}

/**
 * Sobrecarga de EstadoError que acepta directamente una cadena de texto.
 */
@Composable
fun EstadoError(
    mensaje: String,
    alReintentar: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    titulo: String = stringResource(R.string.error_titulo_desconocido)
) {
    EstadoErrorBase(
        titulo = titulo,
        mensaje = mensaje,
        alReintentar = alReintentar,
        modifier = modifier
    )
}

@Composable
private fun EstadoErrorBase(
    titulo: String,
    mensaje: String,
    alReintentar: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(Espaciado.margenPantalla),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = Error,
                modifier = Modifier.size(TamanoIconoEstado)
            )
            Spacer(modifier = Modifier.height(Espaciado.dp16))
            Text(
                text = titulo,
                style = DonChambitasTema.tipografia.subtitulo,
                color = Carbon,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Espaciado.dp8))
            Text(
                text = mensaje,
                style = DonChambitasTema.tipografia.cuerpo,
                color = Cafe,
                textAlign = TextAlign.Center
            )
            if (alReintentar != null) {
                Spacer(modifier = Modifier.height(Espaciado.dp24))
                BotonPrincipal(
                    texto = stringResource(R.string.reintentar),
                    onClick = alReintentar,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ==========================================
// PREVIEWS
// ==========================================

@Preview(showBackground = true, name = "Cargando - Basico")
@Composable
private fun CargandoPreview() {
    DonChambitasTema {
        Box(modifier = Modifier.background(Crema)) {
            Cargando()
        }
    }
}

@Preview(showBackground = true, name = "Cargando - Con mensaje")
@Composable
private fun CargandoConMensajePreview() {
    DonChambitasTema {
        Box(modifier = Modifier.background(Crema)) {
            Cargando(mensaje = stringResource(R.string.cargando))
        }
    }
}

@Preview(showBackground = true, name = "EstadoVacio - Simple")
@Composable
private fun EstadoVacioSimplePreview() {
    DonChambitasTema {
        Box(modifier = Modifier.background(Crema)) {
            EstadoVacio()
        }
    }
}

@Preview(showBackground = true, name = "EstadoVacio - Con boton")
@Composable
private fun EstadoVacioConBotonPreview() {
    DonChambitasTema {
        Box(modifier = Modifier.background(Crema)) {
            EstadoVacio(
                titulo = "Sin solicitudes",
                mensaje = "Aun no has publicado ninguna chamba. Publica una para recibir ofertas.",
                icono = Icons.Outlined.SearchOff,
                textoBoton = "Publicar solicitud",
                alHacerClickBoton = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "EstadoError - Red")
@Composable
private fun EstadoErrorRedPreview() {
    DonChambitasTema {
        Box(modifier = Modifier.background(Crema)) {
            EstadoError(
                tipoError = TipoError.RED,
                alReintentar = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "EstadoError - Limite IA")
@Composable
private fun EstadoErrorLimiteIaPreview() {
    DonChambitasTema {
        Box(modifier = Modifier.background(Crema)) {
            EstadoError(
                tipoError = TipoError.LIMITE_IA,
                alReintentar = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "EstadoError - Servidor")
@Composable
private fun EstadoErrorServidorPreview() {
    DonChambitasTema {
        Box(modifier = Modifier.background(Crema)) {
            EstadoError(
                tipoError = TipoError.SERVIDOR,
                alReintentar = {}
            )
        }
    }
}
