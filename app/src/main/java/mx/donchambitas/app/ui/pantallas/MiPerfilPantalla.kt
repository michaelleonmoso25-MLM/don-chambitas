package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
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
import kotlinx.coroutines.launch
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.Estado
import mx.donchambitas.app.dominio.modelo.Municipio
import mx.donchambitas.app.ui.componentes.BarraSuperior
import mx.donchambitas.app.ui.componentes.BotonPrincipal
import mx.donchambitas.app.ui.componentes.BotonSecundario
import mx.donchambitas.app.ui.componentes.BotonTexto
import mx.donchambitas.app.ui.componentes.CampoTexto
import mx.donchambitas.app.ui.componentes.ChipCategoria
import mx.donchambitas.app.ui.componentes.ContenedorEstado
import mx.donchambitas.app.ui.componentes.EstadoError
import mx.donchambitas.app.ui.tema.Advertencia
import mx.donchambitas.app.ui.tema.Arena
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.Cafe
import mx.donchambitas.app.ui.tema.Carbon
import mx.donchambitas.app.ui.tema.Crema
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.FormaHojaInferior
import mx.donchambitas.app.ui.tema.FormaTarjeta
import mx.donchambitas.app.ui.tema.MostazaOscuro
import mx.donchambitas.app.ui.tema.cuerpo
import mx.donchambitas.app.ui.tema.cuerpoFuerte
import mx.donchambitas.app.ui.tema.pie
import mx.donchambitas.app.ui.tema.secundario
import mx.donchambitas.app.ui.tema.subtitulo
import mx.donchambitas.app.util.TipoError

private val AnchoExperiencia = 200.dp
private val AltoRenglonHoja = 48.dp

/** Hoja inferior abierta en P-11 (2.6). */
private enum class HojaMiPerfil { ESTADO, MUNICIPIO }

/**
 * Pantalla Mi perfil del trabajador (P-11).
 * Especificada en docs/producto/DISENO-PERFIL-SERVICIOS.md, seccion 2.
 *
 * Conecta [MiPerfilContenido] con [MiPerfilViewModel], incluido el boton
 * "Redactar con IA" y su dialogo de reemplazo (1.9).
 *
 * @param alRegresar Regresa a P-18 o a P-12, de donde haya venido. Descarta lo no guardado.
 * @param alVerComoCliente Abre P-07 con el identificador del propio trabajador (HU-08).
 */
@Composable
fun MiPerfilPantalla(
    alRegresar: () -> Unit,
    alVerComoCliente: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MiPerfilViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsState()

    MiPerfilContenido(
        estado = estado,
        alCambiarTitulo = viewModel::alCambiarTitulo,
        alCambiarExperiencia = viewModel::alCambiarExperiencia,
        alCambiarHabilidadNueva = viewModel::alCambiarHabilidadNueva,
        alAgregarHabilidad = viewModel::alAgregarHabilidad,
        alQuitarHabilidad = viewModel::alQuitarHabilidad,
        alCambiarDescripcion = viewModel::alCambiarDescripcion,
        alElegirEstado = viewModel::alElegirEstado,
        alElegirMunicipio = viewModel::alElegirMunicipio,
        alCambiarTelefono = viewModel::alCambiarTelefono,
        alCambiarDisponible = viewModel::alCambiarDisponible,
        alGuardar = viewModel::alGuardar,
        alPedirIa = viewModel::alPedirIa,
        alAceptarPropuestaIa = viewModel::alAceptarPropuestaIa,
        alDescartarPropuestaIa = viewModel::alDescartarPropuestaIa,
        alReintentarCarga = viewModel::alReintentarCarga,
        alReintentar = viewModel::alReintentarGuardado,
        alConsumirAviso = viewModel::alConsumirAviso,
        alSalirDeCampo = viewModel::alPerderFoco,
        alRegresar = alRegresar,
        alVerComoCliente = { alVerComoCliente(estado.usuarioId) },
        modifier = modifier
    )
}

/**
 * Contenido visual puro de P-11, sin estado propio, para previsualizarlo y
 * probarlo con cualquier combinacion de valores y errores. Un callback por
 * evento de la seccion 2.3.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MiPerfilContenido(
    estado: EstadoMiPerfil,
    alCambiarTitulo: (String) -> Unit,
    alCambiarExperiencia: (String) -> Unit,
    alCambiarHabilidadNueva: (String) -> Unit,
    alAgregarHabilidad: () -> Unit,
    alQuitarHabilidad: (String) -> Unit,
    alCambiarDescripcion: (String) -> Unit,
    alElegirEstado: (Int) -> Unit,
    alElegirMunicipio: (Int) -> Unit,
    alCambiarTelefono: (String) -> Unit,
    alCambiarDisponible: (Boolean) -> Unit,
    alGuardar: () -> Unit,
    alPedirIa: () -> Unit,
    alRegresar: () -> Unit,
    alVerComoCliente: () -> Unit,
    modifier: Modifier = Modifier,
    alAceptarPropuestaIa: () -> Unit = {},
    alDescartarPropuestaIa: () -> Unit = {},
    alReintentarCarga: () -> Unit = {},
    alReintentar: (() -> Unit)? = null,
    alConsumirAviso: () -> Unit = {},
    alSalirDeCampo: (CampoMiPerfil) -> Unit = {}
) {
    val administradorFoco = LocalFocusManager.current
    val siguienteCampo = KeyboardActions(onNext = { administradorFoco.moveFocus(FocusDirection.Down) })
    val avisos = remember { SnackbarHostState() }
    val alcanceAvisos = rememberCoroutineScope()
    val textoAviso = estado.aviso?.let { stringResource(it) }
    var hojaAbierta by rememberSaveable { mutableStateOf<HojaMiPerfil?>(null) }
    val solicitantesFoco = remember { CampoMiPerfil.entries.associateWith { FocusRequester() } }
    var irAlPrimerError by remember { mutableStateOf(false) }
    val guardar = {
        alGuardar()
        irAlPrimerError = true
    }

    // 5.4: el foco va al primero que fallo, en orden visual. El municipio no
    // abre teclado: recibir el foco solo lo trae a la vista.
    LaunchedEffect(irAlPrimerError) {
        if (!irAlPrimerError) return@LaunchedEffect
        irAlPrimerError = false
        CampoMiPerfil.entries.firstOrNull { estado.errorDe(it) != null }
            ?.let { solicitantesFoco.getValue(it).requestFocus() }
    }

    fun Modifier.campo(campo: CampoMiPerfil): Modifier = this
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
                titulo = stringResource(R.string.mi_perfil_titulo),
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
                if (estado.faltantes.isNotEmpty()) {
                    AvisoPerfilIncompleto(estado.faltantes)
                }

                BotonSecundario(
                    texto = stringResource(R.string.mi_perfil_vista_previa),
                    onClick = alVerComoCliente,
                    habilitado = estado.perfilGuardado,
                    modifier = Modifier.fillMaxWidth()
                )

                TituloBloque(R.string.mi_perfil_bloque_oficio)

                CampoTexto(
                    valor = estado.titulo,
                    alCambiarValor = alCambiarTitulo,
                    etiqueta = stringResource(R.string.mi_perfil_campo_titulo),
                    marcadorPosicion = stringResource(R.string.mi_perfil_campo_titulo_ejemplo),
                    error = estado.errorTitulo?.let { stringResource(it) },
                    habilitado = editable,
                    tecladoOpciones = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    tecladoAcciones = siguienteCampo,
                    modifier = Modifier.campo(CampoMiPerfil.TITULO)
                )

                CampoTexto(
                    valor = estado.experienciaAnios,
                    alCambiarValor = alCambiarExperiencia,
                    etiqueta = stringResource(R.string.mi_perfil_experiencia),
                    error = estado.errorExperiencia?.let { stringResource(it) },
                    habilitado = editable,
                    tecladoOpciones = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    tecladoAcciones = siguienteCampo,
                    modifier = Modifier.width(AnchoExperiencia).campo(CampoMiPerfil.EXPERIENCIA)
                )

                CampoTexto(
                    valor = estado.habilidadNueva,
                    alCambiarValor = alCambiarHabilidadNueva,
                    etiqueta = stringResource(R.string.mi_perfil_habilidad_nueva),
                    error = estado.errorHabilidad?.let { stringResource(it) },
                    habilitado = editable,
                    iconoFin = {
                        IconButton(onClick = alAgregarHabilidad, enabled = editable) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = stringResource(R.string.mi_perfil_agregar_habilidad),
                                tint = MostazaOscuro
                            )
                        }
                    },
                    tecladoOpciones = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    tecladoAcciones = KeyboardActions(onDone = { alAgregarHabilidad() })
                )

                if (estado.habilidades.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Espaciado.dp8),
                        verticalArrangement = Arrangement.spacedBy(Espaciado.dp8)
                    ) {
                        estado.habilidades.forEach { habilidad ->
                            ChipHabilidad(
                                habilidad = habilidad,
                                alQuitar = { if (editable) alQuitarHabilidad(habilidad) }
                            )
                        }
                    }
                }

                BotonIa(
                    estado = estado.ia,
                    textoFaltanDatos = stringResource(R.string.mi_perfil_ia_falta_titulo),
                    habilitado = editable,
                    alPedir = alPedirIa
                )

                CampoTexto(
                    valor = estado.descripcion,
                    alCambiarValor = alCambiarDescripcion,
                    etiqueta = stringResource(R.string.mi_perfil_descripcion),
                    habilitado = editable,
                    lineasMinimas = 4,
                    lineasMaximas = 8,
                    tecladoOpciones = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                TituloBloque(R.string.mi_perfil_bloque_ubicacion)

                Selector(
                    etiqueta = stringResource(R.string.mi_perfil_estado),
                    valor = estado.estados.firstOrNull { it.id == estado.estadoId }?.nombre.orEmpty(),
                    habilitado = editable,
                    alAbrir = { hojaAbierta = HojaMiPerfil.ESTADO }
                )

                Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp4)) {
                    Selector(
                        etiqueta = stringResource(R.string.mi_perfil_municipio),
                        valor = estado.municipios.firstOrNull { it.id == estado.municipioId }?.nombre.orEmpty(),
                        habilitado = editable && estado.estadoId != null,
                        error = estado.errorMunicipio?.let { stringResource(it) },
                        alAbrir = { hojaAbierta = HojaMiPerfil.MUNICIPIO },
                        modifier = Modifier.focusRequester(solicitantesFoco.getValue(CampoMiPerfil.MUNICIPIO))
                    )
                    if (estado.estadoId == null) {
                        Ayuda(R.string.mi_perfil_municipio_ayuda)
                    }
                }

                TituloBloque(R.string.mi_perfil_bloque_contacto)

                Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp4)) {
                    CampoTexto(
                        valor = estado.telefonoContacto,
                        alCambiarValor = alCambiarTelefono,
                        etiqueta = stringResource(R.string.mi_perfil_telefono),
                        error = estado.errorTelefono?.let { stringResource(it) },
                        habilitado = editable,
                        tecladoOpciones = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        tecladoAcciones = KeyboardActions(onDone = { administradorFoco.clearFocus() }),
                        modifier = Modifier.campo(CampoMiPerfil.TELEFONO)
                    )
                    Ayuda(R.string.mi_perfil_telefono_ayuda)
                }

                SelectorDisponibilidad(
                    disponible = estado.disponible,
                    habilitado = editable,
                    alCambiar = alCambiarDisponible
                )

                ErrorDePantalla(
                    tipoError = estado.errorPantalla,
                    soloFaltanHabilidades = estado.soloFaltanHabilidades,
                    alReintentar = alReintentar
                )

                BotonPrincipal(
                    texto = stringResource(R.string.mi_perfil_guardar),
                    onClick = guardar,
                    cargando = estado.guardando,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
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

    when (hojaAbierta) {
        HojaMiPerfil.ESTADO -> HojaLista(
            titulo = stringResource(R.string.mi_perfil_hoja_estado),
            opciones = estado.estados.map { it.id to it.nombre },
            elegido = estado.estadoId,
            alElegir = {
                hojaAbierta = null
                alElegirEstado(it)
            },
            alCerrar = { hojaAbierta = null }
        )
        HojaMiPerfil.MUNICIPIO -> HojaLista(
            titulo = stringResource(R.string.mi_perfil_hoja_municipio),
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

/** Aviso de 2.5: lo que falta, unido en una sola frase, y por que conviene completarlo. */
@Composable
private fun AvisoPerfilIncompleto(faltantes: List<FaltaPerfil>, modifier: Modifier = Modifier) {
    val partes = faltantes.map { stringResource(it.texto()) }
    val coma = stringResource(R.string.mi_perfil_incompleto_coma)
    val y = stringResource(R.string.mi_perfil_incompleto_y)
    val lista = if (partes.size == 1) {
        partes.first()
    } else {
        val inicio = partes.dropLast(1).reduce { acumulado, parte -> coma.format(acumulado, parte) }
        y.format(inicio, partes.last())
    }

    Surface(
        shape = FormaTarjeta,
        color = Arena,
        border = BorderStroke(1.dp, Borde),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(Espaciado.dp16),
            horizontalArrangement = Arrangement.spacedBy(Espaciado.dp12)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = Advertencia,
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp4)) {
                Text(
                    text = stringResource(R.string.mi_perfil_incompleto_titulo),
                    style = DonChambitasTema.tipografia.cuerpoFuerte,
                    color = Carbon
                )
                Text(
                    text = stringResource(R.string.mi_perfil_incompleto_te_falta, lista),
                    style = DonChambitasTema.tipografia.secundario,
                    color = Cafe
                )
                Text(
                    text = stringResource(R.string.mi_perfil_incompleto_por_que),
                    style = DonChambitasTema.tipografia.pie,
                    color = Cafe
                )
            }
        }
    }
}

@StringRes
private fun FaltaPerfil.texto(): Int = when (this) {
    FaltaPerfil.DESCRIPCION -> R.string.mi_perfil_falta_descripcion
    FaltaPerfil.HABILIDAD -> R.string.mi_perfil_falta_habilidad
    FaltaPerfil.UBICACION -> R.string.mi_perfil_falta_ubicacion
    FaltaPerfil.TELEFONO -> R.string.mi_perfil_falta_telefono
}

@Composable
private fun TituloBloque(@StringRes texto: Int) {
    Text(
        text = stringResource(texto),
        style = DonChambitasTema.tipografia.subtitulo,
        color = Carbon,
        modifier = Modifier.padding(top = Espaciado.dp8)
    )
}

@Composable
private fun Ayuda(@StringRes texto: Int) {
    Text(
        text = stringResource(texto),
        style = DonChambitasTema.tipografia.pie,
        color = Cafe
    )
}

/** Chip activo con la cruz al frente. Tocarlo quita la habilidad; se anuncia como "Quitar ...". */
@Composable
private fun ChipHabilidad(habilidad: String, alQuitar: () -> Unit) {
    val descripcion = stringResource(R.string.mi_perfil_quitar_habilidad, habilidad)
    ChipCategoria(
        texto = habilidad,
        seleccionado = true,
        alSeleccionar = alQuitar,
        icono = Icons.Outlined.Close,
        modifier = Modifier.semantics {
            contentDescription = descripcion
            role = Role.Button
        }
    )
}

/**
 * Campo de solo lectura que abre una hoja (2.6). El campo no recibe toques
 * cuando es de solo lectura, asi que una capa transparente encima es la que
 * abre la hoja.
 */
@Composable
private fun Selector(
    etiqueta: String,
    valor: String,
    habilitado: Boolean,
    alAbrir: () -> Unit,
    error: String? = null,
    modifier: Modifier = Modifier
) {
    Box {
        CampoTexto(
            valor = valor,
            alCambiarValor = {},
            etiqueta = etiqueta,
            error = error,
            habilitado = habilitado,
            soloLectura = true,
            modifier = modifier,
            iconoFin = {
                Icon(
                    imageVector = Icons.Outlined.ArrowDropDown,
                    contentDescription = null,
                    tint = if (habilitado) Carbon else Cafe
                )
            }
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = habilitado, role = Role.DropdownList, onClickLabel = etiqueta, onClick = alAbrir)
        )
    }
}

/** Disponible o no, seleccion unica como el rol de P-03 (1.8). */
@Composable
private fun SelectorDisponibilidad(disponible: Boolean, habilitado: Boolean, alCambiar: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(Espaciado.dp8)
    ) {
        listOf(
            true to R.string.mi_perfil_disponible,
            false to R.string.mi_perfil_no_disponible
        ).forEach { (valor, texto) ->
            ChipCategoria(
                texto = stringResource(texto),
                seleccionado = disponible == valor,
                alSeleccionar = { if (habilitado) alCambiar(valor) },
                modifier = Modifier.semantics {
                    role = Role.RadioButton
                    selected = disponible == valor
                }
            )
        }
    }
}

/** Hoja con la lista de estados o de municipios; el elegido lleva una palomita. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaLista(
    titulo: String,
    opciones: List<Pair<Int, String>>,
    elegido: Int?,
    alElegir: (Int) -> Unit,
    alCerrar: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = alCerrar,
        shape = FormaHojaInferior,
        containerColor = Crema
    ) {
        Text(
            text = titulo,
            style = DonChambitasTema.tipografia.subtitulo,
            color = Carbon,
            modifier = Modifier.padding(horizontal = Espaciado.margenPantalla, vertical = Espaciado.dp8)
        )
        LazyColumn(modifier = Modifier.padding(bottom = Espaciado.dp24)) {
            items(opciones, key = { it.first }) { (id, nombre) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = AltoRenglonHoja)
                        .clickable { alElegir(id) }
                        .padding(horizontal = Espaciado.margenPantalla),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = nombre,
                        style = DonChambitasTema.tipografia.cuerpo,
                        color = Carbon,
                        modifier = Modifier.weight(1f)
                    )
                    if (id == elegido) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = MostazaOscuro
                        )
                    }
                }
            }
        }
    }
}

/**
 * Error al guardar. Se pinta en linea, encima del boton y sin tapar el
 * formulario, como en P-03. Si el perfil se guardo y las habilidades no, lo
 * dice en vez del mensaje del tipo (2.4).
 */
@Composable
private fun ErrorDePantalla(
    tipoError: TipoError?,
    soloFaltanHabilidades: Boolean,
    alReintentar: (() -> Unit)?
) {
    if (tipoError == null) return
    val reintento = alReintentar.takeIf {
        tipoError in setOf(TipoError.RED, TipoError.SERVIDOR, TipoError.DESCONOCIDO)
    }
    EstadoError(
        tipoError = tipoError,
        alReintentar = reintento,
        mensajePersonalizado = if (soloFaltanHabilidades) {
            stringResource(R.string.mi_perfil_habilidades_no_guardadas)
        } else {
            null
        }
    )
}

private val EstadosDeMuestra = listOf(Estado(14, "Jalisco", "JAL"), Estado(21, "Puebla", "PUE"))
private val MunicipiosDeMuestra = listOf(Municipio(1, 14, "Guadalajara"), Municipio(2, 14, "Zapopan"))

private val PerfilCompletoDeMuestra = EstadoMiPerfil(
    cargando = false,
    perfilGuardado = true,
    usuarioId = "usr-trab-1",
    titulo = "Plomero con 12 años de experiencia",
    experienciaAnios = "12",
    habilidades = listOf("Fugas de agua", "Tinacos", "Boilers", "Drenaje"),
    descripcion = "Reparo fugas, instalo tinacos y boilers. Llego el mismo día.",
    estados = EstadosDeMuestra,
    municipios = MunicipiosDeMuestra,
    estadoId = 14,
    municipioId = 1,
    telefonoContacto = "3312345678",
    ia = EstadoBotonIa.Disponible(restantesHoy = 7)
)

@Composable
private fun MiPerfilPreview(estado: EstadoMiPerfil) {
    DonChambitasTema {
        MiPerfilContenido(
            estado = estado,
            alCambiarTitulo = {},
            alCambiarExperiencia = {},
            alCambiarHabilidadNueva = {},
            alAgregarHabilidad = {},
            alQuitarHabilidad = {},
            alCambiarDescripcion = {},
            alElegirEstado = {},
            alElegirMunicipio = {},
            alCambiarTelefono = {},
            alCambiarDisponible = {},
            alGuardar = {},
            alPedirIa = {},
            alRegresar = {},
            alVerComoCliente = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1400, name = "P-11 perfil nuevo")
@Composable
private fun MiPerfilNuevoPreview() {
    MiPerfilPreview(EstadoMiPerfil(cargando = false, faltantes = FaltaPerfil.entries))
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1400, name = "P-11 perfil completo")
@Composable
private fun MiPerfilCompletoPreview() {
    MiPerfilPreview(PerfilCompletoDeMuestra)
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1400, name = "P-11 con errores")
@Composable
private fun MiPerfilConErroresPreview() {
    MiPerfilPreview(
        PerfilCompletoDeMuestra.copy(
            titulo = "",
            errorTitulo = R.string.validacion_perfil_titulo_vacio,
            habilidadNueva = "Cobre",
            errorHabilidad = R.string.validacion_habilidad_repetida,
            municipioId = null,
            errorMunicipio = R.string.validacion_municipio_sin_elegir,
            telefonoContacto = "33123",
            errorTelefono = R.string.validacion_telefono_digitos,
            errorPantalla = TipoError.RED
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1400, name = "P-11 guardando")
@Composable
private fun MiPerfilGuardandoPreview() {
    MiPerfilPreview(PerfilCompletoDeMuestra.copy(guardando = true))
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1400, name = "P-11 límite de IA")
@Composable
private fun MiPerfilLimiteIaPreview() {
    MiPerfilPreview(
        PerfilCompletoDeMuestra.copy(
            ia = EstadoBotonIa.LimiteAlcanzado,
            faltantes = listOf(FaltaPerfil.DESCRIPCION, FaltaPerfil.TELEFONO)
        )
    )
}
