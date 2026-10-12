package mx.donchambitas.app.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.tema.Blanco
import mx.donchambitas.app.ui.tema.Carbon
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.FormaBoton
import mx.donchambitas.app.ui.tema.Mostaza
import mx.donchambitas.app.ui.tema.MostazaOscuro
import mx.donchambitas.app.ui.tema.Terracota
import mx.donchambitas.app.ui.tema.cuerpoFuerte

private val AlturaBoton = 48.dp
private val GrosorBorde = 1.dp
private val TamanoIndicador = 20.dp
private val GrosorIndicador = 2.dp

/**
 * Boton principal de la aplicacion: color Mostaza, texto Carbon, 48 dp de alto.
 * Sobre Mostaza va texto Carbon (7.09:1 WCAG AAA).
 */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = habilitado && !cargando,
        shape = FormaBoton,
        colors = ButtonDefaults.buttonColors(
            containerColor = Mostaza,
            contentColor = Carbon,
            disabledContainerColor = Mostaza.copy(alpha = 0.5f),
            disabledContentColor = Carbon.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(AlturaBoton)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(TamanoIndicador),
                color = Carbon,
                strokeWidth = GrosorIndicador
            )
        } else {
            Text(
                text = texto,
                style = DonChambitasTema.tipografia.cuerpoFuerte
            )
        }
    }
}

/**
 * Boton secundario con contorno Mostaza Oscuro y fondo transparente.
 *
 * @param color Contorno y texto. Mostaza Oscuro salvo en acciones que cierran
 * algo, como "Cerrar sesion" de P-18, que van en Error (WIREFRAMES.md, P-18).
 */
@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    color: Color = MostazaOscuro,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado && !cargando,
        shape = FormaBoton,
        border = BorderStroke(
            width = GrosorBorde,
            color = if (habilitado && !cargando) color else color.copy(alpha = 0.5f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = color,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = color.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(AlturaBoton)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(TamanoIndicador),
                color = color,
                strokeWidth = GrosorIndicador
            )
        } else {
            Text(
                text = texto,
                style = DonChambitasTema.tipografia.cuerpoFuerte
            )
        }
    }
}

/**
 * Boton destacado para acciones de conversion (postularse, publicar): color Terracota, texto Blanco.
 * Sobre Terracota va texto Blanco (5.12:1 WCAG AA).
 */
@Composable
fun BotonDestacado(
    texto: String,
    onClick: () -> Unit,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = habilitado && !cargando,
        shape = FormaBoton,
        colors = ButtonDefaults.buttonColors(
            containerColor = Terracota,
            contentColor = Blanco,
            disabledContainerColor = Terracota.copy(alpha = 0.5f),
            disabledContentColor = Blanco.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(AlturaBoton)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(TamanoIndicador),
                color = Blanco,
                strokeWidth = GrosorIndicador
            )
        } else {
            Text(
                text = texto,
                style = DonChambitasTema.tipografia.cuerpoFuerte
            )
        }
    }
}

/**
 * Boton plano de texto sin fondo, color Mostaza Oscuro.
 */
@Composable
fun BotonTexto(
    texto: String,
    onClick: () -> Unit,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        enabled = habilitado && !cargando,
        colors = ButtonDefaults.textButtonColors(
            containerColor = Color.Transparent,
            contentColor = MostazaOscuro,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MostazaOscuro.copy(alpha = 0.5f)
        ),
        modifier = modifier
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(TamanoIndicador),
                color = MostazaOscuro,
                strokeWidth = GrosorIndicador
            )
        } else {
            Text(
                text = texto,
                style = DonChambitasTema.tipografia.cuerpoFuerte
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFDF8)
@Composable
private fun PreviaBotonPrincipal() {
    DonChambitasTema {
        BotonPrincipal(
            texto = "Iniciar sesión",
            onClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFDF8)
@Composable
private fun PreviaBotonPrincipalCargando() {
    DonChambitasTema {
        BotonPrincipal(
            texto = "Iniciar sesión",
            onClick = {},
            cargando = true
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFDF8)
@Composable
private fun PreviaBotonSecundario() {
    DonChambitasTema {
        BotonSecundario(
            texto = "Crear cuenta",
            onClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFDF8)
@Composable
private fun PreviaBotonDestacado() {
    DonChambitasTema {
        BotonDestacado(
            texto = "Postularme al trabajo",
            onClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFDF8)
@Composable
private fun PreviaBotonTexto() {
    DonChambitasTema {
        BotonTexto(
            texto = "¿Olvidaste tu contraseña?",
            onClick = {}
        )
    }
}
