package mx.donchambitas.app.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import java.math.BigDecimal
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.Categoria
import mx.donchambitas.app.dominio.modelo.Estado
import mx.donchambitas.app.dominio.modelo.FiltrosBusquedaTrabajadores
import mx.donchambitas.app.dominio.modelo.Municipio
import mx.donchambitas.app.dominio.validacion.ValidacionesSolicitud
import mx.donchambitas.app.ui.componentes.BotonPrincipal
import mx.donchambitas.app.ui.componentes.BotonSecundario
import mx.donchambitas.app.ui.componentes.CampoTexto
import mx.donchambitas.app.ui.componentes.ChipCategoria
import mx.donchambitas.app.ui.componentes.obtenerMensajeErrorRes
import mx.donchambitas.app.ui.tema.Carbon
import mx.donchambitas.app.ui.tema.Crema
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Error
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.FormaHojaInferior
import mx.donchambitas.app.ui.tema.pie
import mx.donchambitas.app.ui.tema.subtitulo
import mx.donchambitas.app.util.TipoError

/** Las calificaciones minimas de 3.2 del diseno; nulo es "Cualquiera". */
private val CALIFICACIONES_MINIMAS: List<Double?> = listOf(null, 3.0, 4.0, 4.5)

/** De cual de las dos listas de la hoja es la que esta abierta encima. */
private enum class ListaFiltro { ESTADO, MUNICIPIO }

/** "4.5" o "3", sin el ".0" de sobra. */
internal fun textoCalificacion(valor: Double): String =
    if (valor % 1.0 == 0.0) valor.toInt().toString() else valor.toString()

/** El precio como se escribe en el campo: sin ceros de sobra. */
private fun textoPrecioMaximo(precio: BigDecimal?): String =
    precio?.stripTrailingZeros()?.toPlainString().orEmpty()

/**
 * La hoja inferior de filtros de P-06 (3.2 del diseno): categoria, estado, municipio, precio
 * maximo y calificacion minima, todos opcionales. Lo que se elige es un **borrador**: solo
 * "Aplicar" lo manda; cerrar la hoja lo descarta.
 *
 * Al girar la pantalla la hoja sigue abierta pero el borrador vuelve a los filtros aplicados.
 *
 * @param alElegirEstado Avisa que el borrador cambio de estado, para traer sus municipios.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun HojaFiltros(
    aplicados: FiltrosBusquedaTrabajadores,
    categorias: List<Categoria>,
    estados: List<Estado>,
    municipios: List<Municipio>,
    errorCatalogos: TipoError?,
    alElegirEstado: (Int?) -> Unit,
    alAplicar: (FiltrosBusquedaTrabajadores) -> Unit,
    alQuitarTodos: () -> Unit,
    alCerrar: () -> Unit
) {
    var categoriaId by remember { mutableStateOf(aplicados.categoriaId) }
    var estadoId by remember { mutableStateOf(aplicados.estadoId) }
    var municipioId by remember { mutableStateOf(aplicados.municipioId) }
    var promedioMin by remember { mutableStateOf(aplicados.promedioMin) }
    var precio by remember { mutableStateOf(textoPrecioMaximo(aplicados.precioDesdeMax)) }
    var errorPrecio by remember { mutableStateOf<Int?>(null) }
    var lista by rememberSaveable { mutableStateOf<ListaFiltro?>(null) }

    ModalBottomSheet(
        onDismissRequest = alCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = FormaHojaInferior,
        containerColor = Crema
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Espaciado.dp16),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Espaciado.margenPantalla)
                .padding(bottom = Espaciado.dp24)
        ) {
            Text(
                text = stringResource(R.string.filtros_titulo),
                style = DonChambitasTema.tipografia.subtitulo,
                color = Carbon
            )

            if (errorCatalogos != null) {
                Text(
                    text = stringResource(obtenerMensajeErrorRes(errorCatalogos)),
                    style = DonChambitasTema.tipografia.pie,
                    color = Error
                )
            }

            SelectorCategoria(
                categorias = categorias,
                elegida = categoriaId,
                error = null,
                habilitado = true,
                // Tocar la elegida otra vez la quita: el filtro es opcional.
                alElegir = { id -> categoriaId = if (id == categoriaId) null else id }
            )

            Selector(
                etiqueta = stringResource(R.string.filtros_estado),
                valor = estados.firstOrNull { it.id == estadoId }?.nombre.orEmpty(),
                habilitado = true,
                alAbrir = { lista = ListaFiltro.ESTADO }
            )
            Selector(
                etiqueta = stringResource(R.string.filtros_municipio),
                valor = municipios.firstOrNull { it.id == municipioId }?.nombre.orEmpty(),
                habilitado = estadoId != null,
                alAbrir = { lista = ListaFiltro.MUNICIPIO }
            )

            CampoTexto(
                valor = precio,
                alCambiarValor = {
                    precio = filtrarPrecio(it)
                    errorPrecio = null
                },
                etiqueta = stringResource(R.string.filtros_precio_maximo),
                error = errorPrecio?.let { stringResource(it) },
                iconoInicio = { Text(text = "$", color = Carbon) },
                tecladoOpciones = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp8)) {
                Text(
                    text = stringResource(R.string.filtros_calificacion_minima),
                    style = DonChambitasTema.tipografia.subtitulo,
                    color = Carbon
                )
                FlowRow(
                    modifier = Modifier.selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(Espaciado.dp8),
                    verticalArrangement = Arrangement.spacedBy(Espaciado.dp8)
                ) {
                    CALIFICACIONES_MINIMAS.forEach { minimo ->
                        ChipCategoria(
                            texto = if (minimo == null) {
                                stringResource(R.string.filtros_calificacion_cualquiera)
                            } else {
                                stringResource(R.string.filtros_calificacion_n, textoCalificacion(minimo))
                            },
                            seleccionado = promedioMin == minimo,
                            alSeleccionar = { promedioMin = minimo },
                            modifier = Modifier.semantics {
                                role = Role.RadioButton
                                selected = promedioMin == minimo
                            }
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(Espaciado.dp12),
                modifier = Modifier.fillMaxWidth()
            ) {
                BotonSecundario(
                    texto = stringResource(R.string.filtros_quitar),
                    onClick = alQuitarTodos,
                    modifier = Modifier.weight(1f)
                )
                BotonPrincipal(
                    texto = stringResource(R.string.filtros_aplicar),
                    onClick = {
                        val fallo = ValidacionesSolicitud.validarPresupuesto(precio)
                        if (fallo != null) {
                            errorPrecio = fallo.mensaje()
                        } else {
                            alAplicar(
                                aplicados.copy(
                                    categoriaId = categoriaId,
                                    estadoId = estadoId,
                                    municipioId = municipioId,
                                    precioDesdeMax = ValidacionesSolicitud.normalizarPresupuesto(precio),
                                    promedioMin = promedioMin
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    when (lista) {
        ListaFiltro.ESTADO -> HojaLista(
            titulo = stringResource(R.string.filtros_hoja_estado),
            opciones = estados.map { it.id to it.nombre },
            elegido = estadoId,
            alElegir = { id ->
                if (id != estadoId) {
                    // Cambiar de estado borra el municipio, igual que en P-11.
                    estadoId = id
                    municipioId = null
                    alElegirEstado(id)
                }
                lista = null
            },
            alCerrar = { lista = null }
        )
        ListaFiltro.MUNICIPIO -> HojaLista(
            titulo = stringResource(R.string.filtros_hoja_municipio),
            opciones = municipios.map { it.id to it.nombre },
            elegido = municipioId,
            alElegir = { id ->
                municipioId = id
                lista = null
            },
            alCerrar = { lista = null }
        )
        null -> Unit
    }
}
