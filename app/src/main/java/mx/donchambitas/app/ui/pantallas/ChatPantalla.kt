package mx.donchambitas.app.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.componentes.BarraSuperior
import mx.donchambitas.app.ui.componentes.BurbujaMensaje
import mx.donchambitas.app.ui.componentes.Cargando
import mx.donchambitas.app.ui.componentes.CampoMensaje
import mx.donchambitas.app.ui.componentes.ContenedorEstado
import mx.donchambitas.app.ui.tema.Crema
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.MostazaOscuro
import mx.donchambitas.app.ui.tema.secundario

/** Cuantos mensajes antes del final del historial se pide la pagina anterior. */
private const val UMBRAL_PAGINA_ANTERIOR = 5

/**
 * Chat (P-16), HU-26.
 * Especificada en docs/producto/DISENO-POSTULACION-CONTACTO-RESENAS.md, seccion 5.
 *
 * @param alRegresar Vuelve a P-15 (o a donde se abrio el hilo).
 */
@Composable
fun ChatPantalla(
    alRegresar: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsState()

    ChatContenido(
        estado = estado,
        alRegresar = alRegresar,
        alCambiarBorrador = viewModel::alCambiarBorrador,
        alEnviar = viewModel::alEnviar,
        alReintentarEnvio = viewModel::alReintentarEnvio,
        alPedirAnteriores = viewModel::alPedirAnteriores,
        alReintentarCarga = viewModel::alReintentarCarga,
        alConsumirAviso = viewModel::alConsumirAviso,
        modifier = modifier
    )
}

/** Contenido visual puro de P-16, sin estado propio, para previsualizarlo y probarlo. */
@Composable
fun ChatContenido(
    estado: EstadoChat,
    alRegresar: () -> Unit,
    alCambiarBorrador: (String) -> Unit,
    alEnviar: () -> Unit,
    modifier: Modifier = Modifier,
    alReintentarEnvio: (String) -> Unit = {},
    alPedirAnteriores: () -> Unit = {},
    alReintentarCarga: () -> Unit = {},
    alConsumirAviso: () -> Unit = {}
) {
    val avisos = remember { SnackbarHostState() }
    val alcanceAvisos = rememberCoroutineScope()
    val textoAviso = estado.aviso?.let { stringResource(it) }
    val estadoLista = rememberLazyListState()

    LaunchedEffect(textoAviso) {
        if (textoAviso == null) return@LaunchedEffect
        alcanceAvisos.launch { avisos.showSnackbar(textoAviso) }
        alConsumirAviso()
    }

    // La lista va al reves (lo mas reciente abajo): el "final" del historial es el indice mas alto.
    val cercaDelFinal by remember {
        androidx.compose.runtime.derivedStateOf {
            val ultimo = estadoLista.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            ultimo >= estadoLista.layoutInfo.totalItemsCount - UMBRAL_PAGINA_ANTERIOR
        }
    }
    LaunchedEffect(cercaDelFinal, estado.hayMasAntiguos) {
        if (cercaDelFinal && estado.hayMasAntiguos) alPedirAnteriores()
    }

    // Un mensaje nuevo sube la lista al final, si ya se estaba leyendo lo ultimo.
    val masReciente = estado.mensajes.firstOrNull()?.id
    LaunchedEffect(masReciente) {
        if (masReciente != null && estadoLista.firstVisibleItemIndex <= 1) estadoLista.animateScrollToItem(0)
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = estado.otraPersona?.nombreCompleto ?: stringResource(R.string.chat_titulo),
                alRegresar = alRegresar
            )
        },
        snackbarHost = { SnackbarHost(avisos) },
        containerColor = Crema,
        modifier = modifier.fillMaxSize().imePadding()
    ) { relleno ->
        Column(modifier = Modifier.padding(relleno).fillMaxSize()) {
            if (estado.solicitudTitulo != null) {
                Text(
                    text = estado.solicitudTitulo,
                    style = DonChambitasTema.tipografia.secundario,
                    color = MostazaOscuro,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Espaciado.margenPantalla, vertical = Espaciado.dp4)
                )
            }
            ContenedorEstado(
                cargando = estado.cargando,
                error = estado.errorCarga,
                vacio = estado.sinMensajes,
                alReintentar = alReintentarCarga,
                tituloVacio = stringResource(R.string.chat_primer_mensaje),
                iconoVacio = Icons.Outlined.ChatBubbleOutline,
                modifier = Modifier.weight(1f)
            ) {
                LazyColumn(
                    state = estadoLista,
                    reverseLayout = true,
                    contentPadding = PaddingValues(Espaciado.margenPantalla),
                    verticalArrangement = Arrangement.spacedBy(Espaciado.dp8),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(estado.mensajes, key = { it.id }) { mensaje ->
                        val fallido = mensaje.situacion == SituacionMensaje.FALLIDO
                        BurbujaMensaje(
                            contenido = mensaje.contenido,
                            hora = textoHoraChat(mensaje.creadoEn),
                            esMio = mensaje.esMio,
                            estadoEnvio = when {
                                fallido -> stringResource(R.string.chat_no_enviado)
                                mensaje.situacion == SituacionMensaje.ENVIANDO -> stringResource(R.string.chat_enviando)
                                mensaje.esMio && mensaje.leidoPorOtro -> stringResource(R.string.chat_leido)
                                else -> null
                            },
                            fallido = fallido,
                            alReintentar = { alReintentarEnvio(mensaje.id) }
                        )
                    }
                    if (estado.cargandoAnteriores) {
                        item(key = "cargando-anteriores") {
                            Cargando(modifier = Modifier.fillMaxWidth().padding(Espaciado.dp8))
                        }
                    }
                }
            }
            if (estado.errorCarga == null) {
                CampoMensaje(
                    valor = estado.borrador,
                    alCambiarValor = alCambiarBorrador,
                    alEnviar = alEnviar,
                    puedeEnviar = estado.puedeEnviar,
                    marcadorPosicion = stringResource(R.string.chat_campo_hint)
                )
            }
        }
    }
}
