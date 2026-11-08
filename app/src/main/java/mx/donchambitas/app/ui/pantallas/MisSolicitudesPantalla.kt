package mx.donchambitas.app.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.launch
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.componentes.BarraSuperior
import mx.donchambitas.app.ui.componentes.ContenedorEstado
import mx.donchambitas.app.ui.componentes.TarjetaSolicitud
import mx.donchambitas.app.ui.tema.Carbon
import mx.donchambitas.app.ui.tema.Crema
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.Mostaza

/** Lo que se deja libre abajo para que el boton flotante no tape la ultima tarjeta. */
private val RellenoInferiorLista = 88.dp

/**
 * Mis solicitudes (P-09), HU-13.
 * Especificada en docs/producto/DISENO-SOLICITUDES-BUSQUEDA.md, seccion 5.
 *
 * Conecta [MisSolicitudesContenido] con [MisSolicitudesViewModel] y recarga en cada ON_RESUME,
 * para que lo publicado en P-08 aparezca al volver.
 *
 * @param alPublicar Abre P-08.
 * @param alAbrirSolicitud Abre P-19 con la solicitud tocada.
 * @param tomarSolicitudPublicada Dice, una sola vez, si se llego tras publicar en P-08. Lo responde
 *   el grafo con la entrada de P-09, que es donde P-08 deja la marca.
 */
@Composable
fun MisSolicitudesPantalla(
    alPublicar: () -> Unit,
    alAbrirSolicitud: (solicitudId: String) -> Unit,
    tomarSolicitudPublicada: () -> Boolean,
    modifier: Modifier = Modifier,
    viewModel: MisSolicitudesViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.alCargar(tomarSolicitudPublicada()) }

    MisSolicitudesContenido(
        estado = estado,
        alPublicar = alPublicar,
        alAbrirSolicitud = alAbrirSolicitud,
        alReintentarCarga = viewModel::alReintentarCarga,
        alConsumirAviso = viewModel::alConsumirAviso,
        modifier = modifier
    )
}

/** Contenido visual puro de P-09, sin estado propio, para previsualizarlo y probarlo. */
@Composable
fun MisSolicitudesContenido(
    estado: EstadoMisSolicitudes,
    alPublicar: () -> Unit,
    alAbrirSolicitud: (solicitudId: String) -> Unit,
    modifier: Modifier = Modifier,
    alReintentarCarga: () -> Unit = {},
    alConsumirAviso: () -> Unit = {}
) {
    val avisos = remember { SnackbarHostState() }
    val alcanceAvisos = rememberCoroutineScope()
    val textoAviso = estado.aviso?.let { stringResource(it) }

    LaunchedEffect(textoAviso) {
        if (textoAviso == null) return@LaunchedEffect
        // En otro scope, como en P-12: consumir el aviso cambia la llave de este efecto y
        // cancelaria el Snackbar mientras se muestra.
        alcanceAvisos.launch { avisos.showSnackbar(textoAviso) }
        alConsumirAviso()
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.mis_solicitudes_titulo)) },
        floatingActionButton = {
            // En el vacio no: el estado vacio ya ofrece la misma accion (5.4).
            if (!estado.cargando && estado.errorCarga == null && !estado.vacio) {
                FloatingActionButton(
                    onClick = alPublicar,
                    containerColor = Mostaza,
                    contentColor = Carbon
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.boton_publicar_solicitud)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(avisos) },
        containerColor = Crema,
        modifier = modifier.fillMaxSize()
    ) { relleno ->
        ContenedorEstado(
            cargando = estado.cargando,
            error = estado.errorCarga,
            vacio = estado.vacio,
            alReintentar = alReintentarCarga,
            tituloVacio = stringResource(R.string.mis_solicitudes_vacio_titulo),
            mensajeVacio = stringResource(R.string.mis_solicitudes_vacio_mensaje),
            iconoVacio = Icons.Outlined.Assignment,
            textoBotonVacio = stringResource(R.string.mis_solicitudes_vacio_accion),
            alHacerClickBotonVacio = alPublicar,
            modifier = Modifier.padding(relleno)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = Espaciado.margenPantalla,
                    top = Espaciado.margenPantalla,
                    end = Espaciado.margenPantalla,
                    bottom = RellenoInferiorLista
                ),
                verticalArrangement = Arrangement.spacedBy(Espaciado.dp12),
                modifier = Modifier.fillMaxSize()
            ) {
                items(estado.solicitudes, key = { it.id }) { solicitud ->
                    TarjetaSolicitud(
                        titulo = solicitud.titulo,
                        categoria = estado.categorias.firstOrNull { it.id == solicitud.categoriaId }?.nombre.orEmpty(),
                        presupuesto = solicitud.presupuesto?.let {
                            stringResource(R.string.mis_solicitudes_presupuesto, formatearCantidad(it))
                        } ?: stringResource(R.string.mis_solicitudes_a_convenir),
                        estado = solicitud.estatus,
                        fecha = textoFechaSolicitud(solicitud.creadoEn),
                        onClick = { alAbrirSolicitud(solicitud.id) }
                    )
                }
            }
        }
    }
}
