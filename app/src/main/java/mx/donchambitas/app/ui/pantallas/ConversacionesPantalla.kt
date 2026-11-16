package mx.donchambitas.app.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.launch
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.componentes.BarraSuperior
import mx.donchambitas.app.ui.componentes.ContenedorEstado
import mx.donchambitas.app.ui.componentes.TarjetaConversacion
import mx.donchambitas.app.ui.tema.Crema
import mx.donchambitas.app.ui.tema.Espaciado

/**
 * Conversaciones (P-15), HU-25.
 * Especificada en docs/producto/DISENO-POSTULACION-CONTACTO-RESENAS.md, seccion 4.
 *
 * Conecta [ConversacionesContenido] con [ConversacionesViewModel] y recarga en cada ON_RESUME, para
 * ver lo leido en P-16 al volver. La barra inferior es la del rol y la pone el grafo.
 *
 * @param alAbrirConversacion Abre P-16 con el hilo tocado.
 */
@Composable
fun ConversacionesPantalla(
    alAbrirConversacion: (conversacionId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConversacionesViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.alCargar() }

    ConversacionesContenido(
        estado = estado,
        alAbrirConversacion = alAbrirConversacion,
        alReintentarCarga = viewModel::alReintentarCarga,
        alConsumirAviso = viewModel::alConsumirAviso,
        modifier = modifier
    )
}

/** Contenido visual puro de P-15, sin estado propio, para previsualizarlo y probarlo. */
@Composable
fun ConversacionesContenido(
    estado: EstadoConversaciones,
    alAbrirConversacion: (conversacionId: String) -> Unit,
    modifier: Modifier = Modifier,
    alReintentarCarga: () -> Unit = {},
    alConsumirAviso: () -> Unit = {}
) {
    val avisos = remember { SnackbarHostState() }
    val alcanceAvisos = rememberCoroutineScope()
    val textoAviso = estado.aviso?.let { stringResource(it) }

    LaunchedEffect(textoAviso) {
        if (textoAviso == null) return@LaunchedEffect
        // En otro scope, como en P-09: consumir el aviso cambia la llave de este efecto.
        alcanceAvisos.launch { avisos.showSnackbar(textoAviso) }
        alConsumirAviso()
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.chats_titulo)) },
        snackbarHost = { SnackbarHost(avisos) },
        containerColor = Crema,
        modifier = modifier.fillMaxSize()
    ) { relleno ->
        ContenedorEstado(
            cargando = estado.cargando,
            error = estado.errorCarga,
            vacio = estado.vacio,
            alReintentar = alReintentarCarga,
            tituloVacio = stringResource(R.string.chats_vacio_titulo),
            mensajeVacio = stringResource(R.string.chats_vacio_mensaje),
            iconoVacio = Icons.Outlined.ChatBubbleOutline,
            modifier = Modifier.padding(relleno)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(Espaciado.margenPantalla),
                verticalArrangement = Arrangement.spacedBy(Espaciado.dp12),
                modifier = Modifier.fillMaxSize()
            ) {
                items(estado.conversaciones, key = { it.id }) { conversacion ->
                    TarjetaConversacion(
                        nombre = conversacion.otraPersona.nombreCompleto,
                        ultimoMensaje = conversacion.ultimoMensaje,
                        hora = conversacion.ultimoMensajeEn?.let(::textoHoraChat),
                        sinLeer = conversacion.sinLeer,
                        solicitudTitulo = conversacion.solicitudTitulo,
                        onClick = { alAbrirConversacion(conversacion.id) }
                    )
                }
            }
        }
    }
}
