package mx.donchambitas.app.ui.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.componentes.BotonPrincipal
import mx.donchambitas.app.ui.componentes.obtenerMensajeErrorRes
import mx.donchambitas.app.ui.componentes.obtenerTituloErrorRes
import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.ui.tema.Cafe
import mx.donchambitas.app.ui.tema.Carbon
import mx.donchambitas.app.ui.tema.Crema
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.Mostaza
import mx.donchambitas.app.ui.tema.cuerpo
import mx.donchambitas.app.ui.tema.secundario
import mx.donchambitas.app.ui.tema.subtitulo
import mx.donchambitas.app.ui.tema.titulo
import mx.donchambitas.app.util.TipoError

/**
 * Pantalla de bienvenida (P-01 Splash).
 * Muestra la identidad Don Chambitas sobre fondo Crema mientras verifica
 * el estado de la sesión y navega al destino correspondiente (P-02, P-05 o P-10).
 * Si no logra saberlo, cambia el indicador por el error con "Reintentar".
 *
 * @param alNavegarADestino Callback invocado cuando se resuelve el destino de navegación.
 * @param modifier Modificador de diseño Compose.
 * @param viewModel ViewModel que gestiona la verificación de sesión y el tiempo mínimo de espera.
 */
@Composable
fun SplashPantalla(
    alNavegarADestino: (Ruta) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(estado.destino) {
        val destino = estado.destino
        if (destino != null) {
            alNavegarADestino(destino)
        }
    }

    SplashContenido(
        modifier = modifier,
        error = estado.error,
        alReintentar = viewModel::alReintentar
    )
}

/**
 * Contenido visual puro de la pantalla Splash para desacoplar la UI de la lógica de estado.
 */
@Composable
fun SplashContenido(
    modifier: Modifier = Modifier,
    error: TipoError? = null,
    alReintentar: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Crema)
    ) {
        // Bloque central de marca: Isotipo del casco, nombre y lema
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = Espaciado.dp24),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(id = R.string.splash_logo_descripcion),
                modifier = Modifier.size(130.dp)
            )
            Spacer(modifier = Modifier.height(Espaciado.dp8))
            Text(
                text = stringResource(id = R.string.splash_nombre_app),
                style = DonChambitasTema.tipografia.titulo,
                color = Carbon,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Espaciado.dp4))
            Text(
                text = stringResource(id = R.string.splash_eslogan),
                style = DonChambitasTema.tipografia.cuerpo,
                color = Cafe,
                textAlign = TextAlign.Center
            )
        }

        // Bloque inferior: Indicador de carga circular Mostaza de 48 dp con etiqueta,
        // o el error con "Reintentar" si no se pudo saber si hay sesión
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = Espaciado.margenPantalla)
                .padding(bottom = Espaciado.dp48),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (error == null) {
                CircularProgressIndicator(
                    color = Mostaza,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(Espaciado.dp16))
                Text(
                    text = stringResource(id = R.string.splash_verificando_sesion),
                    style = DonChambitasTema.tipografia.secundario,
                    color = Cafe,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = stringResource(id = obtenerTituloErrorRes(error)),
                    style = DonChambitasTema.tipografia.subtitulo,
                    color = Carbon,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(Espaciado.dp8))
                Text(
                    text = stringResource(id = obtenerMensajeErrorRes(error)),
                    style = DonChambitasTema.tipografia.cuerpo,
                    color = Cafe,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(Espaciado.dp24))
                BotonPrincipal(
                    texto = stringResource(id = R.string.reintentar),
                    onClick = alReintentar,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SplashPantallaPreview() {
    DonChambitasTema {
        SplashContenido()
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-01 sin conexión")
@Composable
private fun SplashPantallaSinConexionPreview() {
    DonChambitasTema {
        SplashContenido(error = TipoError.RED)
    }
}
