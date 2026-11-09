package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.componentes.BarraSuperior
import mx.donchambitas.app.ui.componentes.BotonPrincipal
import mx.donchambitas.app.ui.componentes.CampoTexto
import mx.donchambitas.app.ui.componentes.ContenedorEstado
import mx.donchambitas.app.ui.componentes.obtenerMensajeErrorRes
import mx.donchambitas.app.ui.tema.Cafe
import mx.donchambitas.app.ui.tema.Carbon
import mx.donchambitas.app.ui.tema.Crema
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Error
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.pie
import mx.donchambitas.app.ui.tema.secundario
import mx.donchambitas.app.ui.tema.subtitulo

/** Hoja inferior abierta en P-08 (4.1): la misma pareja de P-11. */
private enum class HojaSolicitud { ESTADO, MUNICIPIO }

/**
 * Pantalla Publicar solicitud (P-08).
 * Especificada en docs/producto/DISENO-SOLICITUDES-BUSQUEDA.md, seccion 4.
 *
 * Conecta [SolicitudNuevaContenido] con [SolicitudNuevaViewModel].
 *
 * @param alRegresar Regresa a P-05 o a P-09, de donde haya venido. Descarta lo escrito.
 * @param alPublicada La solicitud se creo: va a P-09 sacando P-08 de la pila (4.5).
 */
@Composable
fun SolicitudNuevaPantalla(
    alRegresar: () -> Unit,
    alPublicada: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SolicitudNuevaViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(estado.publicada) {
        if (estado.publicada) {
            viewModel.alConsumirPublicada()
            alPublicada()
        }
    }

    SolicitudNuevaContenido(
        estado = estado,
        alCambiarTitulo = viewModel::alCambiarTitulo,
        alElegirCategoria = viewModel::alElegirCategoria,
        alCambiarDescripcion = viewModel::alCambiarDescripcion,
        alCambiarPresupuesto = viewModel::alCambiarPresupuesto,
        alElegirEstado = viewModel::alElegirEstado,
        alElegirMunicipio = viewModel::alElegirMunicipio,
        alPublicar = viewModel::alPublicar,
        alRegresar = alRegresar,
        alReintentarCarga = viewModel::alReintentarCarga,
        alConsumirAviso = viewModel::alConsumirAviso,
        alSalirDeCampo = viewModel::alPerderFoco,
        modifier = modifier
    )
}

/**
 * Contenido visual puro de P-08, sin estado propio, para previsualizarlo y
 * probarlo con cualquier combinacion de valores y errores. Un callback por evento
 * de la seccion 4.4.
 */
@Composable
fun SolicitudNuevaContenido(
    estado: EstadoSolicitudNueva,
    alCambiarTitulo: (String) -> Unit,
    alElegirCategoria: (Int) -> Unit,
    alCambiarDescripcion: (String) -> Unit,
    alCambiarPresupuesto: (String) -> Unit,
    alElegirEstado: (Int) -> Unit,
    alElegirMunicipio: (Int) -> Unit,
    alPublicar: () -> Unit,
    alRegresar: () -> Unit,
    modifier: Modifier = Modifier,
    alReintentarCarga: () -> Unit = {},
    alConsumirAviso: () -> Unit = {},
    alSalirDeCampo: (CampoSolicitud) -> Unit = {}
) {
    val administradorFoco = LocalFocusManager.current
    val siguienteCampo = KeyboardActions(onNext = { administradorFoco.moveFocus(FocusDirection.Down) })
    val avisos = remember { SnackbarHostState() }
    val alcanceAvisos = rememberCoroutineScope()
    val textoAviso = estado.aviso?.let { stringResource(it) }
    var hojaAbierta by rememberSaveable { mutableStateOf<HojaSolicitud?>(null) }
    val solicitantesFoco = remember { CampoSolicitud.entries.associateWith { FocusRequester() } }
    var irAlPrimerError by remember { mutableStateOf(false) }
    val publicar = {
        alPublicar()
        irAlPrimerError = true
    }

    // 1.5: el foco va al primero que fallo, en orden visual.
    LaunchedEffect(irAlPrimerError) {
        if (!irAlPrimerError) return@LaunchedEffect
        irAlPrimerError = false
        CampoSolicitud.entries.firstOrNull { estado.errorDe(it) != null }
            ?.let { solicitantesFoco.getValue(it).requestFocus() }
    }

    fun Modifier.campo(campo: CampoSolicitud): Modifier = this
        .focusRequester(solicitantesFoco.getValue(campo))
        .alPerderFoco { alSalirDeCampo(campo) }
    val editable = !estado.publicando

    LaunchedEffect(textoAviso) {
        if (textoAviso == null) return@LaunchedEffect
        // En otro scope, como en P-11: consumir el aviso cambia la llave de este
        // efecto y cancelaria el Snackbar mientras se muestra.
        alcanceAvisos.launch { avisos.showSnackbar(textoAviso) }
        alConsumirAviso()
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.solicitud_titulo),
                alRegresar = alRegresar
            )
        },
        snackbarHost = { SnackbarHost(avisos) },
        containerColor = Crema,
        modifier = modifier.fillMaxSize()
    ) { relleno ->
        ContenedorEstado(
            cargando = estado.cargando,
            error = estado.errorCarga,
            alReintentar = alReintentarCarga,
            modifier = Modifier.padding(relleno)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = Espaciado.margenPantalla, vertical = Espaciado.dp16),
                verticalArrangement = Arrangement.spacedBy(Espaciado.dp16)
            ) {
                CampoTexto(
                    valor = estado.titulo,
                    alCambiarValor = alCambiarTitulo,
                    etiqueta = stringResource(R.string.solicitud_campo_titulo),
                    error = estado.errorTitulo?.let { stringResource(it) },
                    habilitado = editable,
                    tecladoOpciones = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    tecladoAcciones = siguienteCampo,
                    modifier = Modifier.campo(CampoSolicitud.TITULO)
                )

                SelectorCategoria(
                    categorias = estado.categorias,
                    elegida = estado.categoriaId,
                    error = estado.errorCategoria,
                    habilitado = editable,
                    alElegir = alElegirCategoria,
                    modifier = Modifier.focusRequester(solicitantesFoco.getValue(CampoSolicitud.CATEGORIA))
                )

                CampoTexto(
                    valor = estado.descripcion,
                    alCambiarValor = alCambiarDescripcion,
                    etiqueta = stringResource(R.string.solicitud_campo_descripcion),
                    error = estado.errorDescripcion?.let { stringResource(it) },
                    habilitado = editable,
                    lineasMinimas = 4,
                    lineasMaximas = 8,
                    tecladoOpciones = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.campo(CampoSolicitud.DESCRIPCION)
                )

                Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp4)) {
                    TituloBloqueSolicitud(R.string.solicitud_bloque_presupuesto)
                    Text(
                        text = stringResource(R.string.solicitud_presupuesto_ayuda),
                        style = DonChambitasTema.tipografia.secundario,
                        color = Cafe
                    )
                }

                CampoTexto(
                    valor = estado.presupuesto,
                    alCambiarValor = alCambiarPresupuesto,
                    etiqueta = stringResource(R.string.solicitud_campo_presupuesto),
                    error = estado.errorPresupuesto?.let { stringResource(it) },
                    habilitado = editable,
                    iconoInicio = {
                        Text(
                            text = stringResource(R.string.servicio_moneda),
                            style = DonChambitasTema.tipografia.secundario,
                            color = Cafe
                        )
                    },
                    tecladoOpciones = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    tecladoAcciones = siguienteCampo,
                    modifier = Modifier.campo(CampoSolicitud.PRESUPUESTO)
                )

                TituloBloqueSolicitud(R.string.solicitud_bloque_donde)

                Selector(
                    etiqueta = stringResource(R.string.solicitud_estado),
                    valor = estado.estados.firstOrNull { it.id == estado.estadoId }?.nombre.orEmpty(),
                    habilitado = editable,
                    alAbrir = { hojaAbierta = HojaSolicitud.ESTADO },
                    error = estado.errorEstado?.let { stringResource(it) },
                    modifier = Modifier.campo(CampoSolicitud.ESTADO)
                )

                Selector(
                    etiqueta = stringResource(R.string.solicitud_municipio),
                    valor = estado.municipios.firstOrNull { it.id == estado.municipioId }?.nombre.orEmpty(),
                    habilitado = editable && estado.estadoId != null,
                    alAbrir = { hojaAbierta = HojaSolicitud.MUNICIPIO },
                    error = estado.errorMunicipio?.let { stringResource(it) },
                    modifier = Modifier.campo(CampoSolicitud.MUNICIPIO)
                )
                if (estado.estadoId == null) {
                    Text(
                        text = stringResource(R.string.solicitud_municipio_primero),
                        style = DonChambitasTema.tipografia.pie,
                        color = Cafe
                    )
                }

                estado.errorPantalla?.let { tipo ->
                    Text(
                        text = stringResource(obtenerMensajeErrorRes(tipo)),
                        style = DonChambitasTema.tipografia.pie,
                        color = Error
                    )
                }

                BotonPrincipal(
                    texto = stringResource(R.string.solicitud_publicar),
                    onClick = publicar,
                    cargando = estado.publicando,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    when (hojaAbierta) {
        HojaSolicitud.ESTADO -> HojaLista(
            titulo = stringResource(R.string.solicitud_hoja_estado),
            opciones = estado.estados.map { it.id to it.nombre },
            elegido = estado.estadoId,
            alElegir = {
                hojaAbierta = null
                alElegirEstado(it)
            },
            alCerrar = { hojaAbierta = null }
        )
        HojaSolicitud.MUNICIPIO -> HojaLista(
            titulo = stringResource(R.string.solicitud_hoja_municipio),
            opciones = estado.municipios.map { it.id to it.nombre },
            elegido = estado.municipioId,
            alElegir = {
                hojaAbierta = null
                alElegirMunicipio(it)
            },
            alCerrar = { hojaAbierta = null }
        )
        null -> Unit
    }
}

@Composable
private fun TituloBloqueSolicitud(@StringRes texto: Int) {
    Text(
        text = stringResource(texto),
        style = DonChambitasTema.tipografia.subtitulo,
        color = Carbon,
        modifier = Modifier.padding(top = Espaciado.dp8)
    )
}
