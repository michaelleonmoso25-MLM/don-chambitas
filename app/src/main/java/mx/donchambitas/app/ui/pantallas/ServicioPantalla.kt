package mx.donchambitas.app.ui.pantallas

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.Categoria
import mx.donchambitas.app.dominio.modelo.ServicioFoto
import mx.donchambitas.app.ui.componentes.BarraSuperior
import mx.donchambitas.app.ui.componentes.BotonDestacado
import mx.donchambitas.app.ui.componentes.BotonPrincipal
import mx.donchambitas.app.ui.componentes.BotonTexto
import mx.donchambitas.app.ui.componentes.CampoTexto
import mx.donchambitas.app.ui.componentes.Cargando
import mx.donchambitas.app.ui.componentes.ChipCategoria
import mx.donchambitas.app.ui.componentes.ContenedorEstado
import mx.donchambitas.app.ui.componentes.EstadoError
import mx.donchambitas.app.ui.componentes.iconoCategoria
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.Cafe
import mx.donchambitas.app.ui.tema.Carbon
import mx.donchambitas.app.ui.tema.Crema
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Error
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.FormaCampo
import mx.donchambitas.app.ui.tema.MostazaOscuro
import mx.donchambitas.app.ui.tema.pie
import mx.donchambitas.app.ui.tema.secundario
import mx.donchambitas.app.ui.tema.subtitulo
import mx.donchambitas.app.util.TipoError

private val LadoMiniatura = 96.dp
private val LadoBotonQuitar = 32.dp
private const val OPACIDAD_VELO_SUBIDA = 0.4f

/**
 * Pantalla Crear o editar servicio (P-13).
 * Especificada en docs/producto/DISENO-PERFIL-SERVICIOS.md, seccion 4.
 *
 * Conecta [ServicioContenido] con [ServicioViewModel], que lee el servicioId
 * de la ruta. La foto elegida se valida aqui, porque leerla necesita el
 * ContentResolver, y sus bytes se le pasan al ViewModel. El boton "Redactar
 * con IA" y su dialogo de reemplazo van contra el ViewModel (1.9).
 *
 * @param alRegresar Regresa a P-12. Descarta lo no guardado.
 * @param alGuardarCambios "Guardar cambios" salio bien: regresa a P-12, que da el aviso (4.5).
 */
@Composable
fun ServicioPantalla(
    alRegresar: () -> Unit,
    alGuardarCambios: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ServicioViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()

    LaunchedEffect(estado.cambiosGuardados) {
        if (estado.cambiosGuardados) alGuardarCambios()
    }

    // Sin recortador: la foto de un trabajo se ve completa (4.2).
    fun recibirFoto(uri: Uri) {
        alcance.launch {
            val resolvedor = contexto.contentResolver
            val fallo = withContext(Dispatchers.IO) { resolvedor.validarImagen(uri) }
            if (fallo != null) {
                viewModel.alRechazarFoto(fallo.mensaje())
                return@launch
            }
            val bytes = withContext(Dispatchers.IO) {
                runCatching { resolvedor.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
            }
            if (bytes == null) viewModel.alRechazarFoto(R.string.foto_no_se_abrio) else viewModel.alAgregarFoto(bytes)
        }
    }

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) recibirFoto(uri)
    }
    val archivoCamara = remember { archivoDeCamara(contexto) }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { tomada ->
        if (tomada) recibirFoto(archivoCamara)
    }

    ServicioContenido(
        estado = estado,
        alCambiarTitulo = viewModel::alCambiarTitulo,
        alElegirCategoria = viewModel::alElegirCategoria,
        alCambiarDescripcion = viewModel::alCambiarDescripcion,
        alCambiarPrecioDesde = viewModel::alCambiarPrecioDesde,
        alCambiarPrecioHasta = viewModel::alCambiarPrecioHasta,
        alCambiarUnidad = viewModel::alCambiarUnidad,
        alGuardar = viewModel::alGuardar,
        alElegirDeGaleria = {
            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        alTomarFoto = {
            try {
                camara.launch(archivoCamara)
            } catch (e: ActivityNotFoundException) {
                viewModel.alRechazarFoto(R.string.foto_sin_camara)
            }
        },
        alCancelarSubida = viewModel::alCancelarSubida,
        alPedirQuitarFoto = viewModel::alPedirQuitarFoto,
        alConfirmarQuitarFoto = viewModel::alConfirmarQuitarFoto,
        alCancelarQuitarFoto = viewModel::alCancelarQuitarFoto,
        alPedirIa = viewModel::alPedirIa,
        alAceptarPropuestaIa = viewModel::alAceptarPropuestaIa,
        alDescartarPropuestaIa = viewModel::alDescartarPropuestaIa,
        alReintentarCarga = viewModel::alReintentarCarga,
        alReintentar = viewModel::alGuardar,
        alConsumirAviso = viewModel::alConsumirAviso,
        alSalirDeCampo = viewModel::alPerderFoco,
        alRegresar = alRegresar,
        modifier = modifier
    )
}

/**
 * Contenido visual puro de P-13, sin estado propio. Un callback por evento de
 * la seccion 4.4; la hoja de galeria o camara la abre el propio contenido.
 */
@Composable
fun ServicioContenido(
    estado: EstadoServicio,
    alCambiarTitulo: (String) -> Unit,
    alElegirCategoria: (Int) -> Unit,
    alCambiarDescripcion: (String) -> Unit,
    alCambiarPrecioDesde: (String) -> Unit,
    alCambiarPrecioHasta: (String) -> Unit,
    alCambiarUnidad: (String) -> Unit,
    alGuardar: () -> Unit,
    alElegirDeGaleria: () -> Unit,
    alTomarFoto: () -> Unit,
    alCancelarSubida: (Int) -> Unit,
    alPedirQuitarFoto: (FotoEnPantalla) -> Unit,
    alConfirmarQuitarFoto: () -> Unit,
    alCancelarQuitarFoto: () -> Unit,
    alPedirIa: () -> Unit,
    alRegresar: () -> Unit,
    modifier: Modifier = Modifier,
    alAceptarPropuestaIa: () -> Unit = {},
    alDescartarPropuestaIa: () -> Unit = {},
    alReintentarCarga: () -> Unit = {},
    alReintentar: (() -> Unit)? = null,
    alConsumirAviso: () -> Unit = {},
    alSalirDeCampo: (CampoServicio) -> Unit = {}
) {
    val administradorFoco = LocalFocusManager.current
    val siguienteCampo = KeyboardActions(onNext = { administradorFoco.moveFocus(FocusDirection.Down) })
    val avisos = remember { SnackbarHostState() }
    val alcanceAvisos = rememberCoroutineScope()
    val textoAviso = estado.aviso?.let { stringResource(it) }
    var eligiendoFoto by rememberSaveable { mutableStateOf(false) }
    val solicitantesFoco = remember { CampoServicio.entries.associateWith { FocusRequester() } }
    var irAlPrimerError by remember { mutableStateOf(false) }
    val guardar = {
        alGuardar()
        irAlPrimerError = true
    }

    // 5.4: el foco va al primero que fallo, en orden visual. Una categoria sin
    // elegir lo lleva al titulo del bloque, que es donde se pinta su error.
    LaunchedEffect(irAlPrimerError) {
        if (!irAlPrimerError) return@LaunchedEffect
        irAlPrimerError = false
        CampoServicio.entries.firstOrNull { estado.errorDe(it) != null }
            ?.let { solicitantesFoco.getValue(it).requestFocus() }
    }

    fun Modifier.campo(campo: CampoServicio): Modifier = this
        .focusRequester(solicitantesFoco.getValue(campo))
        .alPerderFoco { alSalirDeCampo(campo) }
    val editable = !estado.guardando

    LaunchedEffect(textoAviso) {
        if (textoAviso == null) return@LaunchedEffect
        // En otro scope, como en P-18: consumir el aviso cambia la llave de este
        // efecto y cancelaria el Snackbar mientras se muestra.
        alcanceAvisos.launch { avisos.showSnackbar(textoAviso) }
        alConsumirAviso()
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(
                    if (estado.creando) R.string.servicio_titulo_crear else R.string.servicio_titulo_editar
                ),
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
            vacio = estado.noExiste,
            alReintentar = alReintentarCarga,
            tituloVacio = stringResource(R.string.servicio_no_existe),
            mensajeVacio = "",
            textoBotonVacio = stringResource(R.string.servicio_volver),
            alHacerClickBotonVacio = alRegresar,
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
                    etiqueta = stringResource(R.string.servicio_campo_titulo),
                    marcadorPosicion = stringResource(R.string.servicio_campo_titulo_ejemplo),
                    error = estado.errorTitulo?.let { stringResource(it) },
                    habilitado = editable,
                    tecladoOpciones = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    tecladoAcciones = siguienteCampo,
                    modifier = Modifier.campo(CampoServicio.TITULO)
                )

                SelectorCategoria(
                    categorias = estado.categorias,
                    elegida = estado.categoriaId,
                    error = estado.errorCategoria,
                    habilitado = editable,
                    alElegir = alElegirCategoria,
                    modifier = Modifier.focusRequester(solicitantesFoco.getValue(CampoServicio.CATEGORIA))
                )

                BotonIa(
                    estado = estado.ia,
                    textoFaltanDatos = stringResource(R.string.servicio_ia_faltan_datos),
                    habilitado = editable,
                    alPedir = alPedirIa
                )

                CampoTexto(
                    valor = estado.descripcion,
                    alCambiarValor = alCambiarDescripcion,
                    etiqueta = stringResource(R.string.servicio_descripcion),
                    error = estado.errorDescripcion?.let { stringResource(it) },
                    habilitado = editable,
                    lineasMinimas = 4,
                    lineasMaximas = 8,
                    tecladoOpciones = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.campo(CampoServicio.DESCRIPCION)
                )

                Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp4)) {
                    TituloBloqueServicio(R.string.servicio_bloque_precio)
                    Text(
                        text = stringResource(R.string.servicio_precio_ayuda),
                        style = DonChambitasTema.tipografia.secundario,
                        color = Cafe
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.dp12)) {
                    CampoPrecio(
                        valor = estado.precioDesde,
                        alCambiar = alCambiarPrecioDesde,
                        etiqueta = R.string.servicio_precio_desde,
                        error = estado.errorPrecioDesde,
                        habilitado = editable,
                        siguiente = siguienteCampo,
                        modifier = Modifier.weight(1f).campo(CampoServicio.PRECIO_DESDE)
                    )
                    CampoPrecio(
                        valor = estado.precioHasta,
                        alCambiar = alCambiarPrecioHasta,
                        etiqueta = R.string.servicio_precio_hasta,
                        error = estado.errorPrecioHasta,
                        habilitado = editable,
                        siguiente = siguienteCampo,
                        modifier = Modifier.weight(1f).campo(CampoServicio.PRECIO_HASTA)
                    )
                }

                CampoTexto(
                    valor = estado.unidadPrecio,
                    alCambiarValor = alCambiarUnidad,
                    etiqueta = stringResource(R.string.servicio_unidad),
                    marcadorPosicion = stringResource(R.string.servicio_unidad_ejemplo),
                    error = estado.errorUnidad?.let { stringResource(it) },
                    habilitado = editable && estado.hayPrecio,
                    tecladoOpciones = KeyboardOptions(imeAction = ImeAction.Done),
                    tecladoAcciones = KeyboardActions(onDone = { administradorFoco.clearFocus() }),
                    modifier = Modifier.campo(CampoServicio.UNIDAD)
                )

                BloqueFotos(
                    estado = estado,
                    alAgregar = { eligiendoFoto = true },
                    alCancelarSubida = alCancelarSubida,
                    alPedirQuitar = alPedirQuitarFoto
                )

                ErrorDePantallaServicio(tipoError = estado.errorPantalla, alReintentar = alReintentar)

                Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp4)) {
                    val habilitado = !estado.subiendoFoto
                    if (estado.creando) {
                        BotonDestacado(
                            texto = stringResource(R.string.servicio_publicar),
                            onClick = guardar,
                            habilitado = habilitado,
                            cargando = estado.guardando,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        BotonPrincipal(
                            texto = stringResource(R.string.servicio_guardar_cambios),
                            onClick = guardar,
                            habilitado = habilitado,
                            cargando = estado.guardando,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (estado.subiendoFoto) {
                        Text(
                            text = stringResource(R.string.servicio_espera_fotos),
                            style = DonChambitasTema.tipografia.pie,
                            color = Cafe
                        )
                    }
                }
            }
        }
    }

    if (eligiendoFoto) {
        OpcionesFoto(
            titulo = stringResource(R.string.servicio_hoja_foto),
            alElegirDeGaleria = {
                eligiendoFoto = false
                alElegirDeGaleria()
            },
            alTomarFoto = {
                eligiendoFoto = false
                alTomarFoto()
            },
            alCerrar = { eligiendoFoto = false }
        )
    }

    if (estado.fotoPorQuitar != null) {
        AlertDialog(
            onDismissRequest = alCancelarQuitarFoto,
            title = { Text(stringResource(R.string.servicio_quitar_foto_titulo)) },
            confirmButton = {
                BotonTexto(texto = stringResource(R.string.servicio_quitar_foto), onClick = alConfirmarQuitarFoto)
            },
            dismissButton = {
                BotonTexto(texto = stringResource(R.string.trabajador_cancelar), onClick = alCancelarQuitarFoto)
            },
            containerColor = Crema
        )
    }

    // 1.9: con la descripcion ya escrita, la IA propone y la persona decide (HU-30).
    if (estado.propuestaIa != null) {
        AlertDialog(
            onDismissRequest = alDescartarPropuestaIa,
            title = { Text(stringResource(R.string.ia_reemplazar_titulo)) },
            text = { Text(estado.propuestaIa) },
            confirmButton = {
                BotonTexto(texto = stringResource(R.string.ia_reemplazar), onClick = alAceptarPropuestaIa)
            },
            dismissButton = {
                BotonTexto(texto = stringResource(R.string.ia_conservar), onClick = alDescartarPropuestaIa)
            },
            containerColor = Crema
        )
    }
}

@Composable
private fun TituloBloqueServicio(@StringRes texto: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(texto),
        style = DonChambitasTema.tipografia.subtitulo,
        color = Carbon,
        modifier = modifier.padding(top = Espaciado.dp8)
    )
}

/**
 * Las 16 categorias a la vista, con su icono (HU-09): el trabajador no tiene
 * que abrir nada para verlas. Seleccion unica, como el rol de P-03.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SelectorCategoria(
    categorias: List<Categoria>,
    elegida: Int?,
    @StringRes error: Int?,
    habilitado: Boolean,
    alElegir: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp8)) {
        // Enfocable solo para recibir el foco cuando falta elegir (5.4).
        TituloBloqueServicio(R.string.servicio_bloque_categoria, modifier.focusable())
        FlowRow(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(Espaciado.dp8),
            verticalArrangement = Arrangement.spacedBy(Espaciado.dp8)
        ) {
            categorias.forEach { categoria ->
                ChipCategoria(
                    texto = categoria.nombre,
                    seleccionado = categoria.id == elegida,
                    alSeleccionar = { if (habilitado) alElegir(categoria.id) },
                    icono = iconoCategoria(categoria.icono),
                    modifier = Modifier.semantics {
                        role = Role.RadioButton
                        selected = categoria.id == elegida
                    }
                )
            }
        }
        if (error != null) {
            Text(text = stringResource(error), style = DonChambitasTema.tipografia.pie, color = Error)
        }
    }
}

@Composable
private fun CampoPrecio(
    valor: String,
    alCambiar: (String) -> Unit,
    @StringRes etiqueta: Int,
    @StringRes error: Int?,
    habilitado: Boolean,
    siguiente: KeyboardActions,
    modifier: Modifier = Modifier
) {
    CampoTexto(
        valor = valor,
        alCambiarValor = alCambiar,
        etiqueta = stringResource(etiqueta),
        error = error?.let { stringResource(it) },
        habilitado = habilitado,
        iconoInicio = { Text(text = stringResource(R.string.servicio_moneda), style = DonChambitasTema.tipografia.secundario, color = Cafe) },
        tecladoOpciones = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
        tecladoAcciones = siguiente,
        modifier = modifier
    )
}

/**
 * Bloque de fotos (4.2, renglones 9 a 11). Al crear esta deshabilitado: subirFoto
 * pide el id del servicio, que todavia no existe (H-22).
 */
@Composable
private fun BloqueFotos(
    estado: EstadoServicio,
    alAgregar: () -> Unit,
    alCancelarSubida: (Int) -> Unit,
    alPedirQuitar: (FotoEnPantalla) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp8)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TituloBloqueServicio(R.string.servicio_bloque_fotos, Modifier.weight(1f))
            if (!estado.creando) {
                Text(
                    text = stringResource(R.string.servicio_fotos_contador, estado.fotos.size),
                    style = DonChambitasTema.tipografia.pie,
                    color = Cafe
                )
            }
        }

        if (estado.creando) {
            Text(
                text = stringResource(R.string.servicio_fotos_despues),
                style = DonChambitasTema.tipografia.secundario,
                color = Cafe
            )
            return@Column
        }

        Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.dp12)) {
            estado.fotos.sortedBy { it.posicion }.forEach { foto ->
                Miniatura(
                    foto = foto,
                    alQuitar = {
                        when (foto) {
                            is FotoEnPantalla.Guardada -> alPedirQuitar(foto)
                            is FotoEnPantalla.Subiendo -> alCancelarSubida(foto.posicion)
                        }
                    }
                )
            }
            if (estado.fotos.size < MAXIMO_FOTOS_SERVICIO) {
                CuadroAgregar(habilitado = !estado.guardando, alTocar = alAgregar)
            }
        }
    }
}

@Composable
private fun Miniatura(foto: FotoEnPantalla, alQuitar: () -> Unit) {
    val descripcionFoto = stringResource(R.string.servicio_foto_n, foto.posicion)
    val descripcionQuitar = when (foto) {
        is FotoEnPantalla.Guardada -> stringResource(R.string.servicio_quitar_foto_n, foto.posicion)
        is FotoEnPantalla.Subiendo -> stringResource(R.string.servicio_cancelar_subida)
    }

    Box(
        modifier = Modifier
            .size(LadoMiniatura)
            .clip(FormaCampo)
            .background(Borde)
            .semantics { contentDescription = descripcionFoto }
    ) {
        if (foto is FotoEnPantalla.Guardada) {
            AsyncImage(
                model = foto.foto.url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Carbon.copy(alpha = OPACIDAD_VELO_SUBIDA))
            ) {
                Cargando()
            }
        }
        IconButton(
            onClick = alQuitar,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(Espaciado.dp4)
                .size(LadoBotonQuitar)
                .background(Crema, CircleShape)
        ) {
            Icon(imageVector = Icons.Outlined.Close, contentDescription = descripcionQuitar, tint = Carbon)
        }
    }
}

/** Cuadro con borde punteado que abre la hoja de galeria o camara. */
@Composable
private fun CuadroAgregar(habilitado: Boolean, alTocar: () -> Unit) {
    val color = if (habilitado) MostazaOscuro else Cafe
    Column(
        modifier = Modifier
            .size(LadoMiniatura)
            .clip(FormaCampo)
            .drawBehind {
                drawRoundRect(
                    color = color,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 4.dp.toPx()))
                    ),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            }
            .clickable(enabled = habilitado, role = Role.Button, onClick = alTocar),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Outlined.AddAPhoto, contentDescription = null, tint = color)
        Text(
            text = stringResource(R.string.servicio_agregar_foto),
            style = DonChambitasTema.tipografia.secundario,
            color = color
        )
    }
}

/** Error al guardar, en linea encima del boton, como en P-03 y P-11. */
@Composable
private fun ErrorDePantallaServicio(tipoError: TipoError?, alReintentar: (() -> Unit)?) {
    if (tipoError == null) return
    val reintento = alReintentar.takeIf {
        tipoError in setOf(TipoError.RED, TipoError.SERVIDOR, TipoError.DESCONOCIDO)
    }
    EstadoError(tipoError = tipoError, alReintentar = reintento)
}

private val CategoriasDeMuestra = listOf(
    Categoria(1, "Plomería", icono = "plumbing", orden = 10),
    Categoria(2, "Electricidad", icono = "bolt", orden = 20),
    Categoria(3, "Albañilería", icono = "construction", orden = 30),
    Categoria(4, "Carpintería", icono = "carpenter", orden = 40),
    Categoria(16, "Otros", icono = "more_horiz", orden = 999)
)

private val ServicioDeMuestra = EstadoServicio(
    cargando = false,
    servicioId = "serv-1",
    titulo = "Reparación de fugas",
    categorias = CategoriasDeMuestra,
    categoriaId = 1,
    descripcion = "Reparo fugas en tuberías de cobre y PVC, llaves, tinacos y calentadores.",
    precioDesde = "300",
    precioHasta = "800",
    unidadPrecio = "por trabajo",
    fotos = listOf(
        FotoEnPantalla.Guardada(ServicioFoto("f1", "serv-1", "", 1, Instant.EPOCH)),
        FotoEnPantalla.Subiendo(2)
    ),
    ia = EstadoBotonIa.Disponible(restantesHoy = 7)
)

@Composable
private fun ServicioPreview(estado: EstadoServicio) {
    DonChambitasTema {
        ServicioContenido(
            estado = estado,
            alCambiarTitulo = {},
            alElegirCategoria = {},
            alCambiarDescripcion = {},
            alCambiarPrecioDesde = {},
            alCambiarPrecioHasta = {},
            alCambiarUnidad = {},
            alGuardar = {},
            alElegirDeGaleria = {},
            alTomarFoto = {},
            alCancelarSubida = {},
            alPedirQuitarFoto = {},
            alConfirmarQuitarFoto = {},
            alCancelarQuitarFoto = {},
            alPedirIa = {},
            alRegresar = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1300, name = "P-13 crear")
@Composable
private fun ServicioCrearPreview() {
    ServicioPreview(EstadoServicio(cargando = false, categorias = CategoriasDeMuestra))
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1300, name = "P-13 editar con fotos")
@Composable
private fun ServicioEditarPreview() {
    ServicioPreview(ServicioDeMuestra)
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1300, name = "P-13 con errores")
@Composable
private fun ServicioConErroresPreview() {
    ServicioPreview(
        ServicioDeMuestra.copy(
            titulo = "",
            errorTitulo = R.string.validacion_servicio_titulo_vacio,
            categoriaId = null,
            errorCategoria = R.string.validacion_categoria_sin_elegir,
            precioHasta = "200",
            errorPrecioHasta = R.string.validacion_precio_rango,
            fotos = emptyList(),
            errorPantalla = TipoError.SERVIDOR
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-13 ya no existe")
@Composable
private fun ServicioNoExistePreview() {
    ServicioPreview(EstadoServicio(cargando = false, servicioId = "serv-borrado", noExiste = true))
}
