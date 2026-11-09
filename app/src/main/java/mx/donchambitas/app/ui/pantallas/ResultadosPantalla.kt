package mx.donchambitas.app.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.componentes.BarraSuperior
import mx.donchambitas.app.ui.componentes.ContenedorEstado
import mx.donchambitas.app.ui.componentes.TarjetaTrabajador
import mx.donchambitas.app.ui.tema.Crema
import mx.donchambitas.app.ui.tema.Espaciado

/**
 * Resultados (P-06), HU-16 y HU-17.
 * Especificada en docs/producto/DISENO-SOLICITUDES-BUSQUEDA.md, seccion 3.
 *
 * Conecta [ResultadosContenido] con [ResultadosViewModel].
 *
 * @param alRegresar Regresa a donde vino, normalmente P-05.
 * @param alAbrirPerfil Abre P-07 con el identificador del trabajador tocado.
 */
@Composable
fun ResultadosPantalla(
    alRegresar: () -> Unit,
    alAbrirPerfil: (trabajadorId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResultadosViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsState()

    ResultadosContenido(
        estado = estado,
        alRegresar = alRegresar,
        alAbrirPerfil = alAbrirPerfil,
        alReintentarCarga = viewModel::alReintentarCarga,
        modifier = modifier
    )
}

/** Contenido visual puro de P-06, sin estado propio, para previsualizarlo y probarlo. */
@Composable
fun ResultadosContenido(
    estado: EstadoResultados,
    alRegresar: () -> Unit,
    alAbrirPerfil: (trabajadorId: String) -> Unit,
    modifier: Modifier = Modifier,
    alReintentarCarga: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.resultados_titulo),
                alRegresar = alRegresar
            )
        },
        containerColor = Crema,
        modifier = modifier.fillMaxSize()
    ) { relleno ->
        ContenedorEstado(
            cargando = estado.cargando,
            error = estado.errorCarga,
            vacio = estado.vacio,
            alReintentar = alReintentarCarga,
            tituloVacio = stringResource(R.string.resultados_vacio_titulo),
            mensajeVacio = stringResource(R.string.resultados_vacio_mensaje),
            modifier = Modifier.padding(relleno)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(Espaciado.margenPantalla),
                verticalArrangement = Arrangement.spacedBy(Espaciado.dp12),
                modifier = Modifier.fillMaxSize()
            ) {
                items(estado.trabajadores, key = { it.trabajadorId }) { trabajador ->
                    TarjetaTrabajador(
                        nombre = "${trabajador.nombre} ${trabajador.apellidos}".trim(),
                        oficio = trabajador.titulo,
                        calificacion = trabajador.promedio.toFloat(),
                        municipio = trabajador.municipio,
                        fotoUrl = trabajador.fotoUrl,
                        totalResenas = trabajador.totalResenas,
                        onClick = { alAbrirPerfil(trabajador.trabajadorId) }
                    )
                }
            }
        }
    }
}
