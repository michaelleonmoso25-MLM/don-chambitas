package mx.donchambitas.app.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.componentes.BotonSecundario
import mx.donchambitas.app.ui.tema.Cafe
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.pie

/**
 * Boton "Redactar con IA" de P-11 y P-13 con la linea que explica su estado
 * (DISENO-PERFIL-SERVICIOS.md, 1.9). Cuando esta deshabilitado, el lector de
 * pantalla anuncia el motivo.
 *
 * @param textoFaltanDatos Lo que le falta a cada pantalla para poder pedir: no es lo mismo en las dos.
 */
@Composable
internal fun BotonIa(
    estado: EstadoBotonIa,
    textoFaltanDatos: String,
    habilitado: Boolean,
    alPedir: () -> Unit
) {
    val linea = when (estado) {
        is EstadoBotonIa.FaltanDatos -> textoFaltanDatos
        is EstadoBotonIa.Disponible -> estado.restantesHoy?.let { stringResource(R.string.ia_restantes, it) }
        EstadoBotonIa.Pidiendo -> null
        EstadoBotonIa.LimiteAlcanzado -> stringResource(R.string.ia_limite)
    }
    val disponible = estado is EstadoBotonIa.Disponible

    Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp4)) {
        BotonSecundario(
            texto = stringResource(R.string.ia_redactar),
            onClick = alPedir,
            habilitado = habilitado && disponible,
            cargando = estado == EstadoBotonIa.Pidiendo,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { if (!disponible && linea != null) stateDescription = linea }
        )
        if (linea != null) {
            Text(text = linea, style = DonChambitasTema.tipografia.pie, color = Cafe)
        }
    }
}
