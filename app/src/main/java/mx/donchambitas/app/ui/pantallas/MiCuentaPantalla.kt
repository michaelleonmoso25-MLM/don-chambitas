package mx.donchambitas.app.ui.pantallas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.modelo.Usuario
import mx.donchambitas.app.ui.componentes.BarraSuperior
import mx.donchambitas.app.ui.componentes.BotonPrincipal
import mx.donchambitas.app.ui.componentes.BotonSecundario
import mx.donchambitas.app.ui.componentes.BotonTexto
import mx.donchambitas.app.ui.componentes.CampoContrasena
import mx.donchambitas.app.ui.componentes.CampoTexto
import mx.donchambitas.app.ui.componentes.ContenedorEstado
import mx.donchambitas.app.ui.componentes.EtiquetaEstado
import mx.donchambitas.app.ui.componentes.obtenerMensajeErrorRes
import mx.donchambitas.app.ui.componentes.obtenerTituloErrorRes
import mx.donchambitas.app.ui.tema.Advertencia
import mx.donchambitas.app.ui.tema.Arena
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.Cafe
import mx.donchambitas.app.ui.tema.Carbon
import mx.donchambitas.app.ui.tema.Crema
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Error
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.FormaFotoPerfil
import mx.donchambitas.app.ui.tema.FormaTarjeta
import mx.donchambitas.app.ui.tema.Mostaza
import mx.donchambitas.app.ui.tema.MostazaOscuro
import mx.donchambitas.app.ui.tema.cuerpoFuerte
import mx.donchambitas.app.ui.tema.pie
import mx.donchambitas.app.ui.tema.secundario
import mx.donchambitas.app.ui.tema.subtitulo
import mx.donchambitas.app.ui.tema.titulo
import java.time.Instant
import kotlinx.coroutines.launch

private val TamanoAvatar = 80.dp
private val GrosorBordeAvatar = 2.dp

/**
 * Mi cuenta (P-18), HU-05.
 * Especificada en docs/tareas/S2-T11.md y docs/producto/WIREFRAMES.md, seccion 7.
 *
 * El estado y las llamadas a los repositorios viven en [MiCuentaViewModel];
 * aqui solo se conecta con el contenido y se pintan los avisos una vez. La
 * barra inferior la sigue pintando el andamio de GrafoNavegacion.
 *
 * @param alIrAMiPerfil Lleva a P-11. Solo se ofrece al trabajador.
 * @param alIrAMisPostulaciones Lleva a P-14. Solo se ofrece al trabajador.
 */
@Composable
fun MiCuentaPantalla(
    alIrAMiPerfil: () -> Unit,
    alIrAMisPostulaciones: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MiCuentaViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsState()

    MiCuentaContenido(
        estado = estado,
        alReintentar = viewModel::alReintentar,
        alAbrirSeccion = viewModel::alAbrirSeccion,
        alCancelar = viewModel::alCancelar,
        alCambiarNombre = viewModel::alCambiarNombre,
        alCambiarApellidos = viewModel::alCambiarApellidos,
        alCambiarTelefono = viewModel::alCambiarTelefono,
        alGuardarDatos = viewModel::alGuardarDatos,
        alCambiarContrasena = viewModel::alCambiarContrasena,
        alGuardarContrasena = viewModel::alGuardarContrasena,
        alCerrarSesion = viewModel::alCerrarSesion,
        alConsumirMensaje = viewModel::alConsumirMensaje,
        alIrAMiPerfil = alIrAMiPerfil,
        alIrAMisPostulaciones = alIrAMisPostulaciones,
        modifier = modifier
    )
}

/**
 * Contenido visual puro de P-18, sin estado propio salvo el dialogo de
 * confirmacion, para previsualizarlo y probarlo con cualquier estado.
 */
@Composable
fun MiCuentaContenido(
    estado: EstadoMiCuenta,
    alReintentar: () -> Unit,
    alAbrirSeccion: (SeccionMiCuenta) -> Unit,
    alCancelar: () -> Unit,
    alCambiarNombre: (String) -> Unit,
    alCambiarApellidos: (String) -> Unit,
    alCambiarTelefono: (String) -> Unit,
    alGuardarDatos: () -> Unit,
    alCambiarContrasena: (String) -> Unit,
    alGuardarContrasena: () -> Unit,
    alCerrarSesion: () -> Unit,
    alConsumirMensaje: () -> Unit,
    alIrAMiPerfil: () -> Unit,
    alIrAMisPostulaciones: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val avisos = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    var confirmandoCierre by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(estado.mensaje) {
        val mensaje = estado.mensaje ?: return@LaunchedEffect
        val texto = when (mensaje) {
            is MensajeMiCuenta.Exito -> contexto.getString(mensaje.texto)
            is MensajeMiCuenta.Error -> contexto.getString(
                R.string.mi_cuenta_formato_error,
                contexto.getString(obtenerTituloErrorRes(mensaje.tipo)),
                contexto.getString(obtenerMensajeErrorRes(mensaje.tipo))
            )
        }
        // En otro scope: consumir el mensaje cambia la llave de este efecto y
        // cancelaria el Snackbar mientras se muestra.
        alcance.launch { avisos.showSnackbar(texto) }
        alConsumirMensaje()
    }

    // La barra inferior ya la reserva el andamio, y la superior trae su
    // propio inset: el contenido no necesita otro relleno de sistema.
    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.mi_cuenta_titulo)) },
        snackbarHost = { SnackbarHost(avisos) },
        containerColor = Crema,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize()
    ) { relleno ->
        ContenedorEstado(
            cargando = estado.cargando,
            error = estado.errorCarga,
            alReintentar = alReintentar,
            modifier = Modifier
                .padding(relleno)
                .fillMaxSize()
        ) {
            val usuario = estado.usuario ?: return@ContenedorEstado
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = Espaciado.margenPantalla, vertical = Espaciado.dp24),
                verticalArrangement = Arrangement.spacedBy(Espaciado.dp16)
            ) {
                if (estado.seccion == SeccionMiCuenta.DATOS) {
                    SeccionDatos(
                        estado = estado,
                        alCambiarNombre = alCambiarNombre,
                        alCambiarApellidos = alCambiarApellidos,
                        alCambiarTelefono = alCambiarTelefono,
                        alGuardar = alGuardarDatos,
                        alCancelar = alCancelar
                    )
                } else {
                    TarjetaCuenta(usuario)
                }

                Titulo(stringResource(R.string.mi_cuenta_configuracion))

                if (estado.seccion != SeccionMiCuenta.DATOS) {
                    BotonSecundario(
                        texto = stringResource(R.string.mi_cuenta_editar_datos),
                        onClick = { alAbrirSeccion(SeccionMiCuenta.DATOS) },
                        habilitado = !estado.guardando
                    )
                }

                if (estado.seccion == SeccionMiCuenta.CONTRASENA) {
                    SeccionContrasena(
                        estado = estado,
                        alCambiarContrasena = alCambiarContrasena,
                        alGuardar = alGuardarContrasena,
                        alCancelar = alCancelar
                    )
                } else {
                    BotonSecundario(
                        texto = stringResource(R.string.mi_cuenta_cambiar_contrasena),
                        onClick = { alAbrirSeccion(SeccionMiCuenta.CONTRASENA) },
                        habilitado = !estado.guardando
                    )
                }

                // Decision 3 de S2-T11: P-11 y P-14, como dice PANTALLAS.md.
                if (usuario.rol == RolUsuario.TRABAJADOR) {
                    Titulo(stringResource(R.string.mi_cuenta_accesos_trabajador))
                    BotonSecundario(
                        texto = stringResource(R.string.mi_cuenta_mi_perfil),
                        onClick = alIrAMiPerfil
                    )
                    BotonSecundario(
                        texto = stringResource(R.string.mi_cuenta_mis_postulaciones),
                        onClick = alIrAMisPostulaciones
                    )
                }

                BotonSecundario(
                    texto = stringResource(R.string.mi_cuenta_cerrar_sesion),
                    onClick = { confirmandoCierre = true },
                    cargando = estado.cerrandoSesion,
                    color = Error,
                    modifier = Modifier.padding(top = Espaciado.dp16)
                )
            }
        }
    }

    if (confirmandoCierre) {
        AlertDialog(
            onDismissRequest = { confirmandoCierre = false },
            title = { Text(stringResource(R.string.mi_cuenta_confirmar_cierre)) },
            confirmButton = {
                BotonTexto(
                    texto = stringResource(R.string.mi_cuenta_cerrar_sesion),
                    onClick = {
                        confirmandoCierre = false
                        alCerrarSesion()
                    }
                )
            },
            dismissButton = {
                BotonTexto(
                    texto = stringResource(R.string.mi_cuenta_cancelar),
                    onClick = { confirmandoCierre = false }
                )
            },
            containerColor = Crema
        )
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(
        text = texto,
        style = DonChambitasTema.tipografia.subtitulo,
        color = Carbon
    )
}

/** Cabecera de WIREFRAMES.md, P-18, sin municipio: public.usuarios no lo tiene (H-15). */
@Composable
private fun TarjetaCuenta(usuario: Usuario) {
    Surface(
        color = Arena,
        shape = FormaTarjeta,
        border = BorderStroke(1.dp, Borde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(Espaciado.rellenoTarjeta),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Espaciado.dp16)
        ) {
            Avatar(usuario)
            Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp4)) {
                Text(
                    text = "${usuario.nombre} ${usuario.apellidos}",
                    style = DonChambitasTema.tipografia.cuerpoFuerte,
                    color = Carbon
                )
                if (usuario.rol == RolUsuario.TRABAJADOR) {
                    EtiquetaEstado(
                        texto = stringResource(R.string.mi_cuenta_rol_trabajador),
                        colorFondo = Advertencia,
                        colorTexto = Carbon
                    )
                } else {
                    EtiquetaEstado(
                        texto = stringResource(R.string.mi_cuenta_rol_cliente),
                        colorFondo = Mostaza,
                        colorTexto = Carbon
                    )
                }
                Text(
                    text = usuario.correo,
                    style = DonChambitasTema.tipografia.secundario,
                    color = Cafe
                )
                usuario.telefono?.takeIf { it.isNotBlank() }?.let { telefono ->
                    Text(
                        text = stringResource(R.string.mi_cuenta_telefono, telefono),
                        style = DonChambitasTema.tipografia.pie,
                        color = Cafe
                    )
                }
            }
        }
    }
}

/**
 * Foto si la hay; si no, las iniciales (HU-07), nunca un espacio vacio.
 * Tocarlo no hace nada todavia: elegir la foto es S2-T13.
 */
@Composable
private fun Avatar(usuario: Usuario) {
    val forma = Modifier
        .size(TamanoAvatar)
        .clip(FormaFotoPerfil)
        .border(GrosorBordeAvatar, MostazaOscuro, FormaFotoPerfil)
    if (usuario.fotoUrl != null) {
        AsyncImage(
            model = usuario.fotoUrl,
            contentDescription = stringResource(R.string.foto_perfil),
            contentScale = ContentScale.Crop,
            modifier = forma
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = forma.background(Mostaza)
        ) {
            Text(
                text = iniciales(usuario.nombre, usuario.apellidos),
                style = DonChambitasTema.tipografia.titulo,
                color = Carbon
            )
        }
    }
}

/** Primera letra del nombre y del primer apellido. */
internal fun iniciales(nombre: String, apellidos: String): String =
    listOfNotNull(nombre.trim().firstOrNull(), apellidos.trim().firstOrNull())
        .joinToString("")
        .uppercase()

/**
 * Edicion de nombre, apellidos y telefono. El correo y el rol no aparecen:
 * ninguno se edita (DEC-22). Al guardar con errores, el foco va al primero (5.4).
 */
@Composable
private fun SeccionDatos(
    estado: EstadoMiCuenta,
    alCambiarNombre: (String) -> Unit,
    alCambiarApellidos: (String) -> Unit,
    alCambiarTelefono: (String) -> Unit,
    alGuardar: () -> Unit,
    alCancelar: () -> Unit
) {
    val administradorFoco = LocalFocusManager.current
    val siguienteCampo = KeyboardActions(onNext = { administradorFoco.moveFocus(FocusDirection.Down) })
    val solicitantesFoco = remember { CampoMiCuenta.entries.associateWith { FocusRequester() } }
    var irAlPrimerError by remember { mutableStateOf(false) }
    val guardar = {
        alGuardar()
        irAlPrimerError = true
    }

    LaunchedEffect(irAlPrimerError) {
        if (!irAlPrimerError) return@LaunchedEffect
        irAlPrimerError = false
        CampoMiCuenta.entries.firstOrNull { estado.errorDe(it) != null }
            ?.let { solicitantesFoco.getValue(it).requestFocus() }
    }

    Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp16)) {
        Titulo(stringResource(R.string.mi_cuenta_editar_datos))
        CampoTexto(
            valor = estado.nombre,
            alCambiarValor = alCambiarNombre,
            etiqueta = stringResource(R.string.registro_nombre),
            error = estado.errorNombre?.let { stringResource(it) },
            habilitado = !estado.guardando,
            modifier = Modifier.focusRequester(solicitantesFoco.getValue(CampoMiCuenta.NOMBRE)),
            tecladoOpciones = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            tecladoAcciones = siguienteCampo
        )
        CampoTexto(
            valor = estado.apellidos,
            alCambiarValor = alCambiarApellidos,
            etiqueta = stringResource(R.string.registro_apellidos),
            error = estado.errorApellidos?.let { stringResource(it) },
            habilitado = !estado.guardando,
            modifier = Modifier.focusRequester(solicitantesFoco.getValue(CampoMiCuenta.APELLIDOS)),
            tecladoOpciones = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            tecladoAcciones = siguienteCampo
        )
        CampoTexto(
            valor = estado.telefono,
            alCambiarValor = alCambiarTelefono,
            etiqueta = stringResource(R.string.registro_telefono),
            error = estado.errorTelefono?.let { stringResource(it) },
            habilitado = !estado.guardando,
            modifier = Modifier.focusRequester(solicitantesFoco.getValue(CampoMiCuenta.TELEFONO)),
            tecladoOpciones = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            tecladoAcciones = KeyboardActions(
                onDone = {
                    administradorFoco.clearFocus()
                    if (!estado.guardando) guardar()
                }
            )
        )
        BotonPrincipal(
            texto = stringResource(R.string.mi_cuenta_guardar),
            onClick = guardar,
            cargando = estado.guardando
        )
        BotonTexto(
            texto = stringResource(R.string.mi_cuenta_cancelar),
            onClick = alCancelar,
            habilitado = !estado.guardando,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

private fun EstadoMiCuenta.errorDe(campo: CampoMiCuenta): Int? = when (campo) {
    CampoMiCuenta.NOMBRE -> errorNombre
    CampoMiCuenta.APELLIDOS -> errorApellidos
    CampoMiCuenta.TELEFONO -> errorTelefono
}

/** Solo la contrasena nueva (decision 2 de S2-T11), con el minimo del registro. */
@Composable
private fun SeccionContrasena(
    estado: EstadoMiCuenta,
    alCambiarContrasena: (String) -> Unit,
    alGuardar: () -> Unit,
    alCancelar: () -> Unit
) {
    val administradorFoco = LocalFocusManager.current
    Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp16)) {
        Titulo(stringResource(R.string.mi_cuenta_cambiar_contrasena))
        Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp4)) {
            CampoContrasena(
                valor = estado.contrasenaNueva,
                alCambiarValor = alCambiarContrasena,
                etiqueta = stringResource(R.string.mi_cuenta_contrasena_nueva),
                error = estado.errorContrasena?.let { stringResource(it) },
                habilitado = !estado.guardando,
                tecladoOpciones = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                tecladoAcciones = KeyboardActions(
                    onDone = {
                        administradorFoco.clearFocus()
                        if (!estado.guardando) alGuardar()
                    }
                )
            )
            Text(
                text = stringResource(R.string.registro_ayuda_contrasena),
                style = DonChambitasTema.tipografia.pie,
                color = Cafe
            )
        }
        BotonPrincipal(
            texto = stringResource(R.string.mi_cuenta_guardar),
            onClick = alGuardar,
            cargando = estado.guardando
        )
        BotonTexto(
            texto = stringResource(R.string.mi_cuenta_cancelar),
            onClick = alCancelar,
            habilitado = !estado.guardando,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

private val UsuarioPrevia = Usuario(
    id = "usr-previa",
    correo = "juan.perez@correo.com",
    nombre = "Juan",
    apellidos = "Pérez Hernández",
    telefono = "3312345678",
    rol = RolUsuario.TRABAJADOR,
    creadoEn = Instant.EPOCH,
    actualizadoEn = Instant.EPOCH
)

@Composable
private fun MiCuentaPrevia(estado: EstadoMiCuenta) {
    DonChambitasTema {
        MiCuentaContenido(
            estado = estado,
            alReintentar = {},
            alAbrirSeccion = {},
            alCancelar = {},
            alCambiarNombre = {},
            alCambiarApellidos = {},
            alCambiarTelefono = {},
            alGuardarDatos = {},
            alCambiarContrasena = {},
            alGuardarContrasena = {},
            alCerrarSesion = {},
            alConsumirMensaje = {},
            alIrAMiPerfil = {},
            alIrAMisPostulaciones = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-18 trabajador")
@Composable
private fun MiCuentaTrabajadorPreview() {
    MiCuentaPrevia(EstadoMiCuenta(usuario = UsuarioPrevia, cargando = false))
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-18 editando datos")
@Composable
private fun MiCuentaEditandoPreview() {
    MiCuentaPrevia(
        EstadoMiCuenta(
            usuario = UsuarioPrevia.copy(rol = RolUsuario.CLIENTE),
            cargando = false,
            seccion = SeccionMiCuenta.DATOS,
            nombre = "Juan",
            apellidos = "",
            telefono = "33123",
            errorApellidos = R.string.validacion_apellidos_vacio,
            errorTelefono = R.string.validacion_telefono_digitos
        )
    )
}
