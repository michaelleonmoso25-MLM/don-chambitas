package mx.donchambitas.app.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.FiltrosBusquedaTrabajadores
import mx.donchambitas.app.ui.componentes.BarraSuperior
import mx.donchambitas.app.ui.componentes.CampoTexto
import mx.donchambitas.app.ui.componentes.ChipCategoria
import mx.donchambitas.app.ui.componentes.ContenedorEstado
import mx.donchambitas.app.ui.componentes.TarjetaTrabajador
import mx.donchambitas.app.ui.tema.Crema
import mx.donchambitas.app.ui.tema.Espaciado

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
    alElegirEstadoEnFiltros: (Int?) -> Unit = {}
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
