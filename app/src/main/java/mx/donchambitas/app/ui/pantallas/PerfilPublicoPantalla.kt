package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.PerfilPublicoTrabajador
import mx.donchambitas.app.dominio.modelo.ResenaPublica
import mx.donchambitas.app.dominio.modelo.ServicioPublico
import mx.donchambitas.app.ui.componentes.BarraSuperior
import mx.donchambitas.app.ui.componentes.BotonPrincipal
import mx.donchambitas.app.ui.componentes.ContenedorEstado
import mx.donchambitas.app.ui.componentes.EtiquetaEstado
import mx.donchambitas.app.ui.componentes.Estrellas
import mx.donchambitas.app.ui.componentes.TarjetaServicio
import mx.donchambitas.app.ui.tema.Arena
import mx.donchambitas.app.ui.tema.Blanco
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.Cafe
import mx.donchambitas.app.ui.tema.Carbon
import mx.donchambitas.app.ui.tema.Crema
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.Exito
import mx.donchambitas.app.ui.tema.FormaChip
import mx.donchambitas.app.ui.tema.FormaFotoPerfil
import mx.donchambitas.app.ui.tema.FormaTarjeta
import mx.donchambitas.app.ui.tema.Mostaza
import mx.donchambitas.app.ui.tema.cuerpo
import mx.donchambitas.app.ui.tema.cuerpoFuerte
import mx.donchambitas.app.ui.tema.pie
import mx.donchambitas.app.ui.tema.subtitulo
import mx.donchambitas.app.ui.tema.titulo

/**
 * Perfil publico del trabajador (P-07), HU-08.
 * Especificada en docs/producto/DISENO-PERFIL-SERVICIOS.md, seccion 11 y en
 * docs/producto/DISENO-SOLICITUDES-BUSQUEDA.md, seccion 6 (H-39).
 *
 * Conecta [PerfilPublicoContenido] con [PerfilPublicoViewModel]. Tiene dos modos: la vista
 * previa del trabajador (sus vacios le hablan a el) y la del cliente (oculta lo vacio que
 * no puede arreglar y trae el boton de contactar).
 *
 * @param alRegresar Regresa a la pantalla de donde vino.
 * @param alContactar Abre la conversacion con el trabajador; hoy no esta enlazado (S5-T06).
 */
@Composable
fun PerfilPublicoPantalla(
    alRegresar: () -> Unit,
    modifier: Modifier = Modifier,
    alContactar: () -> Unit = {},
    viewModel: PerfilPublicoViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsState()

    PerfilPublicoContenido(
        estado = estado,
        alRegresar = alRegresar,
        alReintentarCarga = viewModel::alReintentarCarga,
        alContactar = alContactar,
        modifier = modifier
    )
}

/** Contenido visual puro de P-07, sin estado propio, para previsualizarlo y probarlo. */
@Composable
fun PerfilPublicoContenido(
    estado: EstadoPerfilPublico,
    alRegresar: () -> Unit,
    modifier: Modifier = Modifier,
    alReintentarCarga: () -> Unit = {},
    alContactar: () -> Unit = {}
) {
    // "Contactar" solo sale cuando ya hay perfil y quien lo mira es el cliente (6 del diseno).
    val mostrarContactar = estado.perfil != null && !estado.esVistaPrevia
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.perfil_publico_titulo),
                alRegresar = alRegresar
            )
        },
        bottomBar = {
            if (mostrarContactar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Crema)
                        .padding(horizontal = Espaciado.margenPantalla, vertical = Espaciado.dp12)
                ) {
                    BotonPrincipal(
                        texto = stringResource(R.string.perfil_publico_contactar),
                        onClick = alContactar
                    )
                }
            }
        },
        containerColor = Crema,
        modifier = modifier.fillMaxSize()
    ) { relleno ->
        ContenedorEstado(
            cargando = estado.cargando,
            error = estado.errorCarga,
            alReintentar = alReintentarCarga,
            modifier = Modifier.padding(relleno)
        ) {
            estado.perfil?.let { PerfilPublicoLista(it, esVistaPrevia = estado.esVistaPrevia) }
        }
    }
}

@Composable
private fun PerfilPublicoLista(perfil: PerfilPublicoTrabajador, esVistaPrevia: Boolean) {
    LazyColumn(
        contentPadding = PaddingValues(Espaciado.margenPantalla),
        verticalArrangement = Arrangement.spacedBy(Espaciado.dp16),
        modifier = Modifier.fillMaxSize()
    ) {
        if (esVistaPrevia) {
            item { AvisoVistaPrevia() }
        }
        item { Encabezado(perfil) }
        item { Calificacion(perfil) }

        // Al cliente le sobra una seccion vacia: no puede arreglarla, asi que se oculta.
        if (esVistaPrevia || !perfil.descripcion.isNullOrBlank()) {
            item {
                Seccion(R.string.perfil_publico_sobre_mi) {
                    if (perfil.descripcion.isNullOrBlank()) {
                        Vacio(R.string.perfil_publico_sin_descripcion)
                    } else {
                        Text(text = perfil.descripcion, style = DonChambitasTema.tipografia.cuerpo, color = Carbon)
                    }
                }
            }
        }

        if (esVistaPrevia || perfil.habilidades.isNotEmpty()) {
            item {
                Seccion(R.string.perfil_publico_habilidades) {
                    if (perfil.habilidades.isEmpty()) {
                        Vacio(R.string.perfil_publico_sin_habilidades)
                    } else {
                        Habilidades(perfil.habilidades)
                    }
                }
            }
        }

        item { TituloSeccion(R.string.perfil_publico_servicios) }
        if (perfil.servicios.isEmpty()) {
            item {
                Vacio(
                    if (esVistaPrevia) R.string.perfil_publico_sin_servicios
                    else R.string.perfil_publico_sin_servicios_cliente
                )
            }
        } else {
            items(perfil.servicios.size) { posicion -> TarjetaDeServicio(perfil.servicios[posicion]) }
        }

        item { TituloSeccion(R.string.perfil_publico_resenas) }
        if (perfil.resenas.isEmpty()) {
            item {
                Vacio(
                    if (esVistaPrevia) R.string.perfil_publico_sin_resenas
                    else R.string.perfil_publico_sin_resenas_cliente
                )
            }
        } else {
            items(perfil.resenas.size) { posicion -> TarjetaDeResena(perfil.resenas[posicion]) }
        }
    }
}

/** Le recuerda al trabajador que lo que ve es lo que ven los clientes (6 del diseno). */
@Composable
private fun AvisoVistaPrevia() {
    Text(
        text = stringResource(R.string.perfil_publico_aviso_vista_previa),
        style = DonChambitasTema.tipografia.cuerpoFuerte,
        color = Carbon,
        modifier = Modifier
            .fillMaxWidth()
            .clip(FormaTarjeta)
            .background(Mostaza.copy(alpha = 0.25f))
            .padding(Espaciado.rellenoTarjeta)
    )
}

@Composable
private fun Encabezado(perfil: PerfilPublicoTrabajador) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Espaciado.dp16)) {
        Avatar(perfil)
        Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp4)) {
            Text(
                text = "${perfil.nombre} ${perfil.apellidos}".trim(),
                style = DonChambitasTema.tipografia.titulo,
                color = Carbon
            )
            Text(text = perfil.titulo, style = DonChambitasTema.tipografia.cuerpoFuerte, color = Carbon)
            Text(
                text = stringResource(R.string.perfil_publico_ubicacion, perfil.municipio, perfil.estado),
                style = DonChambitasTema.tipografia.pie,
                color = Cafe
            )
            if (perfil.experienciaAnios > 0) {
                Text(
                    text = if (perfil.experienciaAnios == 1) {
                        stringResource(R.string.perfil_publico_experiencia_uno)
                    } else {
                        stringResource(R.string.perfil_publico_experiencia_varios, perfil.experienciaAnios)
                    },
                    style = DonChambitasTema.tipografia.pie,
                    color = Cafe
                )
            }
            if (perfil.disponible) {
                EtiquetaEstado(stringResource(R.string.perfil_publico_disponible), Exito, Blanco)
            } else {
                EtiquetaEstado(stringResource(R.string.perfil_publico_no_disponible), Cafe, Blanco)
            }
        }
    }
}

/** La foto, o la inicial del nombre sobre Mostaza si todavia no hay. */
@Composable
private fun Avatar(perfil: PerfilPublicoTrabajador) {
    val tamano = 72.dp
    if (perfil.fotoUrl != null) {
        AsyncImage(
            model = perfil.fotoUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(tamano).clip(FormaFotoPerfil)
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(tamano).clip(FormaFotoPerfil).background(Mostaza)
        ) {
            Text(
                text = perfil.nombre.take(1).uppercase(),
                style = DonChambitasTema.tipografia.titulo,
                color = Carbon
            )
        }
    }
}

@Composable
private fun Calificacion(perfil: PerfilPublicoTrabajador) {
    if (perfil.calificacion.totalResenas == 0) {
        Text(
            text = stringResource(R.string.perfil_publico_sin_calificacion),
            style = DonChambitasTema.tipografia.pie,
            color = Cafe
        )
    } else {
        Estrellas(
            calificacion = perfil.calificacion.promedio.toFloat(),
            mostrarNumero = true,
            totalResenas = perfil.calificacion.totalResenas
        )
    }
}

@Composable
private fun TituloSeccion(@StringRes titulo: Int) {
    Text(text = stringResource(titulo), style = DonChambitasTema.tipografia.subtitulo, color = Carbon)
}

@Composable
private fun Seccion(@StringRes titulo: Int, contenido: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp8)) {
        TituloSeccion(titulo)
        contenido()
    }
}

/**
 * Lo que sale en una seccion sin datos: dice que falta y donde se arregla, no una
 * lista en blanco (HU-08).
 */
@Composable
private fun Vacio(@StringRes texto: Int) {
    Text(
        text = stringResource(texto),
        style = DonChambitasTema.tipografia.cuerpo,
        color = Cafe,
        modifier = Modifier
            .fillMaxWidth()
            .clip(FormaTarjeta)
            .background(Arena)
            .border(BorderStroke(1.dp, Borde), FormaTarjeta)
            .padding(Espaciado.rellenoTarjeta)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Habilidades(habilidades: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Espaciado.dp8),
        verticalArrangement = Arrangement.spacedBy(Espaciado.dp8)
    ) {
        habilidades.forEach { habilidad ->
            Text(
                text = habilidad,
                style = DonChambitasTema.tipografia.pie,
                color = Carbon,
                modifier = Modifier
                    .clip(FormaChip)
                    .background(Arena)
                    .border(BorderStroke(1.dp, Borde), FormaChip)
                    .padding(horizontal = Espaciado.dp12, vertical = Espaciado.dp8)
            )
        }
    }
}

@Composable
private fun TarjetaDeServicio(servicio: ServicioPublico) {
    TarjetaServicio(
        titulo = servicio.titulo,
        categoria = servicio.categoria,
        precioTexto = textoPrecio(servicio),
        fotoUrl = servicio.fotos.firstOrNull()
    )
}

@Composable
private fun TarjetaDeResena(resena: ResenaPublica) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Espaciado.dp4),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Arena)
            .border(BorderStroke(1.dp, Borde), RoundedCornerShape(16.dp))
            .padding(Espaciado.rellenoTarjeta)
    ) {
        Estrellas(calificacion = resena.calificacion.toFloat())
        Text(text = resena.clienteNombre, style = DonChambitasTema.tipografia.cuerpoFuerte, color = Carbon)
        if (!resena.comentario.isNullOrBlank()) {
            Text(text = resena.comentario, style = DonChambitasTema.tipografia.cuerpo, color = Carbon)
        }
    }
}

/** El precio como lo pide la tabla 3.4 de DISENO-PERFIL-SERVICIOS.md; vacio si no hay. */
@Composable
private fun textoPrecio(servicio: ServicioPublico): String {
    val precio = when (val tipo = precioDeServicio(servicio.precioDesde, servicio.precioHasta)) {
        is PrecioServicio.Rango -> stringResource(R.string.precio_rango, tipo.desde, tipo.hasta)
        is PrecioServicio.Desde -> stringResource(R.string.precio_desde, tipo.desde)
        is PrecioServicio.Hasta -> stringResource(R.string.precio_hasta, tipo.hasta)
        null -> return ""
    }
    val unidad = servicio.unidadPrecio?.trim().orEmpty()
    return if (unidad.isEmpty()) precio else stringResource(R.string.precio_con_unidad, precio, unidad)
}
