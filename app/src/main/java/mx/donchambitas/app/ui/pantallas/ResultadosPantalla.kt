package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.FiltrosBusquedaTrabajadores
import mx.donchambitas.app.dominio.modelo.OrdenBusqueda
import mx.donchambitas.app.ui.componentes.BarraSuperior
import mx.donchambitas.app.ui.componentes.BotonTexto
import mx.donchambitas.app.ui.componentes.CampoTexto
import mx.donchambitas.app.ui.componentes.ChipCategoria
import mx.donchambitas.app.ui.componentes.ContenedorEstado
import mx.donchambitas.app.ui.componentes.TarjetaTrabajador
import mx.donchambitas.app.ui.tema.Cafe
import mx.donchambitas.app.ui.tema.Carbon
import mx.donchambitas.app.ui.tema.Crema
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.Mostaza
import mx.donchambitas.app.ui.tema.cuerpo
import mx.donchambitas.app.ui.tema.pie

/**
 * Resultados (P-06), HU-16, HU-17 y HU-18.
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
        alCambiarTexto = viewModel::alCambiarTexto,
        alBuscar = viewModel::alBuscar,
        alBorrarTexto = viewModel::alBorrarTexto,
        alAbrirFiltros = viewModel::alAbrirFiltros,
        alCerrarFiltros = viewModel::alCerrarFiltros,
        alAplicarFiltros = viewModel::alAplicarFiltros,
        alQuitarFiltro = viewModel::alQuitarFiltro,
        alQuitarTodosLosFiltros = viewModel::alQuitarTodosLosFiltros,
        alElegirEstadoEnFiltros = viewModel::alElegirEstadoEnFiltros,
        alElegirOrden = viewModel::alElegirOrden,
        alLlegarAlFinal = viewModel::alLlegarAlFinal,
        alReintentarMas = viewModel::alReintentarMas,
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
    alReintentarCarga: () -> Unit = {},
    alCambiarTexto: (String) -> Unit = {},
    alBuscar: () -> Unit = {},
    alBorrarTexto: () -> Unit = {},
    alAbrirFiltros: () -> Unit = {},
    alCerrarFiltros: () -> Unit = {},
    alAplicarFiltros: (FiltrosBusquedaTrabajadores) -> Unit = {},
    alQuitarFiltro: (FiltroResultados) -> Unit = {},
    alQuitarTodosLosFiltros: () -> Unit = {},
    alElegirEstadoEnFiltros: (Int?) -> Unit = {},
    alElegirOrden: (OrdenBusqueda) -> Unit = {},
    alLlegarAlFinal: () -> Unit = {},
    alReintentarMas: () -> Unit = {}
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
        // imePadding: con el teclado abierto, el vacio y la lista se quedan arriba de el, no tapados.
        Column(modifier = Modifier.padding(relleno).imePadding().fillMaxSize()) {
            // Fuera del contenedor de estados: el campo se sigue viendo, con el foco, mientras carga.
            Buscador(
                texto = estado.texto,
                alCambiarTexto = alCambiarTexto,
                alBuscar = alBuscar,
                alBorrarTexto = alBorrarTexto
            )
            FilaFiltros(estado = estado, alAbrirFiltros = alAbrirFiltros, alQuitarFiltro = alQuitarFiltro)
            FilaOrden(orden = estado.filtros.orden ?: OrdenBusqueda.PROMEDIO, alElegirOrden = alElegirOrden)
            ContenedorEstado(
                cargando = estado.cargando,
                error = estado.errorCarga,
                vacio = estado.vacio,
                alReintentar = alReintentarCarga,
                tituloVacio = stringResource(R.string.resultados_vacio_titulo),
                mensajeVacio = stringResource(R.string.resultados_vacio_mensaje),
                // HU-17: con filtros puestos, el vacio ofrece quitarlos.
                textoBotonVacio = if (estado.hayFiltros) stringResource(R.string.filtros_quitar) else null,
                alHacerClickBotonVacio = if (estado.hayFiltros) alQuitarTodosLosFiltros else null,
                modifier = Modifier.weight(1f)
            ) {
                // Dentro del contenedor: cada busqueda nueva lo vacia y la lista vuelve arriba (3.5);
                // pedir mas paginas no lo toca, asi que conserva su posicion.
                ListaResultados(
                    estado = estado,
                    alAbrirPerfil = alAbrirPerfil,
                    alLlegarAlFinal = alLlegarAlFinal,
                    alReintentarMas = alReintentarMas
                )
            }
        }

        if (estado.hojaFiltrosAbierta) {
            HojaFiltros(
                aplicados = estado.filtros,
                categorias = estado.categorias,
                estados = estado.estados,
                municipios = estado.municipios,
                errorCatalogos = estado.errorCatalogos,
                alElegirEstado = alElegirEstadoEnFiltros,
                alAplicar = alAplicarFiltros,
                alQuitarTodos = alQuitarTodosLosFiltros,
                alCerrar = alCerrarFiltros
            )
        }
    }
}

/**
 * El buscador de 3.1 y 3.4 del diseno: busca con la accion "Buscar" del teclado, no al escribir,
 * y trae un boton para borrar el texto cuando lo hay.
 */
@Composable
private fun Buscador(
    texto: String,
    alCambiarTexto: (String) -> Unit,
    alBuscar: () -> Unit,
    alBorrarTexto: () -> Unit
) {
    CampoTexto(
        valor = texto,
        alCambiarValor = alCambiarTexto,
        marcadorPosicion = stringResource(R.string.resultados_buscador_marcador),
        iconoInicio = { Icon(imageVector = Icons.Outlined.Search, contentDescription = null) },
        iconoFin = if (texto.isNotEmpty()) {
            {
                IconButton(onClick = alBorrarTexto) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.resultados_buscador_borrar)
                    )
                }
            }
        } else null,
        tecladoOpciones = KeyboardOptions(imeAction = ImeAction.Search),
        tecladoAcciones = KeyboardActions(onSearch = { alBuscar() }),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Espaciado.margenPantalla, vertical = Espaciado.dp12)
    )
}

/**
 * La fila de 3.1 del diseno: el boton "Filtros (n)" y un chip con X por cada filtro activo,
 * para quitar uno sin abrir la hoja.
 */
@Composable
private fun FilaFiltros(
    estado: EstadoResultados,
    alAbrirFiltros: () -> Unit,
    alQuitarFiltro: (FiltroResultados) -> Unit
) {
    val filtros = estado.filtros
    val activos = buildList {
        filtros.categoriaId?.let { id ->
            add(FiltroResultados.CATEGORIA to (
                estado.categorias.firstOrNull { it.id == id }?.nombre
                    ?: stringResource(R.string.filtros_categoria)
                ))
        }
        filtros.estadoId?.let { id ->
            add(FiltroResultados.ESTADO to (
                estado.estados.firstOrNull { it.id == id }?.nombre
                    ?: stringResource(R.string.filtros_estado)
                ))
        }
        filtros.municipioId?.let { id ->
            add(FiltroResultados.MUNICIPIO to (
                estado.municipios.firstOrNull { it.id == id }?.nombre
                    ?: stringResource(R.string.filtros_municipio)
                ))
        }
        filtros.precioDesdeMax?.let { precio ->
            add(FiltroResultados.PRECIO to stringResource(
                R.string.filtros_chip_precio,
                precio.stripTrailingZeros().toPlainString()
            ))
        }
        filtros.promedioMin?.let { minimo ->
            add(FiltroResultados.CALIFICACION to stringResource(
                R.string.filtros_calificacion_n,
                textoCalificacion(minimo)
            ))
        }
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = Espaciado.margenPantalla),
        horizontalArrangement = Arrangement.spacedBy(Espaciado.dp8)
    ) {
        item {
            ChipCategoria(
                texto = if (estado.hayFiltros) {
                    stringResource(R.string.filtros_boton_n, estado.filtrosActivos)
                } else {
                    stringResource(R.string.filtros_boton)
                },
                seleccionado = estado.hayFiltros,
                alSeleccionar = alAbrirFiltros,
                icono = Icons.Outlined.FilterList
            )
        }
        items(activos, key = { it.first }) { (cual, texto) ->
            ChipCategoria(
                texto = texto,
                seleccionado = true,
                alSeleccionar = { alQuitarFiltro(cual) },
                icono = Icons.Outlined.Close
            )
        }
    }
}

/** Cuantas tarjetas antes del final se pide la pagina siguiente (3.5 del diseno). */
private const val UMBRAL_PAGINA = 5

/** Las tres opciones de 3.3 del diseno, en el orden en que se ofrecen. */
private val ORDENES: List<OrdenBusqueda> =
    listOf(OrdenBusqueda.PROMEDIO, OrdenBusqueda.PRECIO_DESDE, OrdenBusqueda.CREADO_EN)

@StringRes
private fun textoOrden(orden: OrdenBusqueda): Int = when (orden) {
    OrdenBusqueda.PROMEDIO -> R.string.resultados_orden_calificacion
    OrdenBusqueda.PRECIO_DESDE -> R.string.resultados_orden_precio
    OrdenBusqueda.CREADO_EN -> R.string.resultados_orden_recientes
}

/**
 * La lista de trabajadores con su pie (3.5 y 3.8 del diseno): el indicador mientras llega la
 * pagina siguiente, o "No pudimos cargar mas" con "Reintentar" si fallo. Pide la siguiente cuando
 * faltan [UMBRAL_PAGINA] tarjetas para el final.
 */
@Composable
private fun ListaResultados(
    estado: EstadoResultados,
    alAbrirPerfil: (trabajadorId: String) -> Unit,
    alLlegarAlFinal: () -> Unit,
    alReintentarMas: () -> Unit
) {
    val posicion = rememberLazyListState()
    val total = estado.trabajadores.size
    val cercaDelFinal by remember(total) {
        derivedStateOf {
            val ultimo = posicion.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            ultimo >= total - UMBRAL_PAGINA
        }
    }
    // El ViewModel decide si de verdad hay que pedir: aqui solo se avisa que se llego cerca del final.
    LaunchedEffect(cercaDelFinal, total) {
        if (cercaDelFinal) alLlegarAlFinal()
    }

    LazyColumn(
        state = posicion,
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
                precioDesdeTexto = trabajador.precioDesde?.let {
                    stringResource(R.string.precio_desde, formatearCantidad(it))
                },
                disponible = trabajador.disponible,
                onClick = { alAbrirPerfil(trabajador.trabajadorId) }
            )
        }
        if (estado.cargandoMas) {
            item(key = "pie-cargando") {
                val descripcion = stringResource(R.string.cargando)
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = Mostaza,
                        modifier = Modifier.semantics { contentDescription = descripcion }
                    )
                }
            }
        } else if (estado.errorAlCargarMas) {
            item(key = "pie-error") {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.resultados_mas_error),
                        style = DonChambitasTema.tipografia.pie,
                        color = Cafe
                    )
                    BotonTexto(texto = stringResource(R.string.reintentar), onClick = alReintentarMas)
                }
            }
        }
    }
}

/**
 * La fila "Ordenar por: Mejor calificados" de 3.1 del diseno: abre la lista de las tres opciones.
 * Cambiar el orden conserva los filtros y el texto.
 */
@Composable
private fun FilaOrden(orden: OrdenBusqueda, alElegirOrden: (OrdenBusqueda) -> Unit) {
    var abierta by rememberSaveable { mutableStateOf(false) }
    val titulo = stringResource(R.string.resultados_orden_titulo)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(role = Role.DropdownList, onClickLabel = titulo) { abierta = true }
            .padding(horizontal = Espaciado.margenPantalla)
    ) {
        Text(
            text = stringResource(R.string.resultados_orden_etiqueta, stringResource(textoOrden(orden))),
            style = DonChambitasTema.tipografia.cuerpo,
            color = Carbon
        )
        Icon(imageVector = Icons.Outlined.ArrowDropDown, contentDescription = null, tint = Carbon)
    }

    if (abierta) {
        HojaLista(
            titulo = titulo,
            opciones = ORDENES.map { it.ordinal to stringResource(textoOrden(it)) },
            elegido = orden.ordinal,
            alElegir = { id ->
                alElegirOrden(OrdenBusqueda.entries[id])
                abierta = false
            },
            alCerrar = { abierta = false }
        )
    }
}
