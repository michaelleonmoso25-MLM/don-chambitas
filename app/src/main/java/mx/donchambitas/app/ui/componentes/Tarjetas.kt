package mx.donchambitas.app.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.EstadoSolicitud
import mx.donchambitas.app.ui.tema.Arena
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.Cafe
import mx.donchambitas.app.ui.tema.Carbon
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.FormaFotoPerfil
import mx.donchambitas.app.ui.tema.FormaTarjeta
import mx.donchambitas.app.ui.tema.Terracota
import mx.donchambitas.app.ui.tema.cuerpoFuerte
import mx.donchambitas.app.ui.tema.pie
import mx.donchambitas.app.ui.tema.secundario

private val TamanoFotoTrabajador = 56.dp
private val TamanoFotoServicio = 72.dp
private val FormaFotoServicio = RoundedCornerShape(12.dp)
private const val OPACIDAD_SERVICIO_PAUSADO = 0.6f
private val TamanoIconoUbicacion = 14.dp

/**
 * Tarjeta para mostrar el resumen del perfil de un trabajador segun DISENO.md.
 * Incluye foto, nombre, titulo/oficio, estrellas, municipio, el precio inicial
 * y, si no toma trabajos, la etiqueta "No disponible" (H-43, DEC-36).
 *
 * @param municipio Nulo o vacio cuando el trabajador no puso su ubicacion: el renglon no se pinta (H-48).
 * @param precioDesdeTexto Nulo cuando no tiene servicios con precio: el renglon no se pinta.
 * @param disponible Falso lo marca con la etiqueta; no se oculta ni se atenua la tarjeta.
 */
@Composable
fun TarjetaTrabajador(
    nombre: String,
    oficio: String,
    calificacion: Float,
    municipio: String?,
    fotoUrl: String? = null,
    totalResenas: Int? = null,
    precioDesdeTexto: String? = null,
    disponible: Boolean = true,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = FormaTarjeta,
        colors = CardDefaults.cardColors(
            containerColor = Arena,
            contentColor = Carbon
        ),
        border = BorderStroke(1.dp, Borde),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Espaciado.rellenoTarjeta)
        ) {
            if (!fotoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = fotoUrl,
                    contentDescription = stringResource(R.string.foto_perfil),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(TamanoFotoTrabajador)
                        .clip(FormaFotoPerfil)
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(TamanoFotoTrabajador)
                        .clip(FormaFotoPerfil)
                        .background(Borde)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = stringResource(R.string.foto_perfil),
                        tint = Cafe,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(Espaciado.dp12))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Espaciado.dp4)
            ) {
                Text(
                    text = nombre,
                    style = DonChambitasTema.tipografia.cuerpoFuerte,
                    color = Carbon
                )

                Text(
                    text = oficio,
                    style = DonChambitasTema.tipografia.secundario,
                    color = Cafe
                )

                Estrellas(
                    calificacion = calificacion,
                    mostrarNumero = true,
                    totalResenas = totalResenas
                )

                if (!municipio.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Espaciado.dp4)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = Cafe,
                            modifier = Modifier.size(TamanoIconoUbicacion)
                        )
                        Text(
                            text = municipio,
                            style = DonChambitasTema.tipografia.pie,
                            color = Cafe
                        )
                    }
                }

                if (precioDesdeTexto != null || !disponible) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Espaciado.dp12),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (precioDesdeTexto != null) {
                            Text(
                                text = precioDesdeTexto,
                                style = DonChambitasTema.tipografia.secundario,
                                color = Carbon
                            )
                        }
                        if (!disponible) {
                            Text(
                                text = stringResource(R.string.perfil_publico_no_disponible),
                                style = DonChambitasTema.tipografia.pie,
                                color = Cafe
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta para mostrar un servicio publicado segun DISENO.md.
 * Incluye foto, titulo, categoria y rango de precio.
 *
 * @param precioTexto Vacio cuando el servicio no tiene precio: el renglon no se pinta (HU-09).
 * @param pausado El servicio no sale en la busqueda: la tarjeta se atenua y lo dice (H-21).
 */
@Composable
fun TarjetaServicio(
    titulo: String,
    categoria: String,
    precioTexto: String,
    fotoUrl: String? = null,
    pausado: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = FormaTarjeta,
        colors = CardDefaults.cardColors(
            containerColor = Arena,
            contentColor = Carbon
        ),
        border = BorderStroke(1.dp, Borde),
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (pausado) OPACIDAD_SERVICIO_PAUSADO else 1f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Espaciado.rellenoTarjeta)
        ) {
            if (!fotoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = fotoUrl,
                    contentDescription = stringResource(R.string.foto_servicio),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(TamanoFotoServicio)
                        .clip(FormaFotoServicio)
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(TamanoFotoServicio)
                        .clip(FormaFotoServicio)
                        .background(Borde)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Build,
                        contentDescription = stringResource(R.string.foto_servicio),
                        tint = Cafe,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(Espaciado.dp12))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Espaciado.dp4)
            ) {
                Text(
                    text = titulo,
                    style = DonChambitasTema.tipografia.cuerpoFuerte,
                    color = Carbon
                )

                Text(
                    text = if (pausado) {
                        stringResource(R.string.tarjeta_servicio_categoria_pausado, categoria)
                    } else {
                        categoria
                    },
                    style = DonChambitasTema.tipografia.secundario,
                    color = Cafe
                )

                if (precioTexto.isNotBlank()) {
                    Text(
                        text = precioTexto,
                        style = DonChambitasTema.tipografia.cuerpoFuerte,
                        color = Terracota
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta para mostrar una solicitud de trabajo segun DISENO.md.
 * Incluye titulo, categoria, presupuesto, estado y fecha.
 */
@Composable
fun TarjetaSolicitud(
    titulo: String,
    categoria: String,
    presupuesto: String,
    estado: EstadoSolicitud,
    fecha: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = FormaTarjeta,
        colors = CardDefaults.cardColors(
            containerColor = Arena,
            contentColor = Carbon
        ),
        border = BorderStroke(1.dp, Borde),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Espaciado.rellenoTarjeta),
            verticalArrangement = Arrangement.spacedBy(Espaciado.dp8)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoria,
                    style = DonChambitasTema.tipografia.secundario,
                    color = Cafe
                )
                EtiquetaEstado(estado = estado)
            }

            Text(
                text = titulo,
                style = DonChambitasTema.tipografia.cuerpoFuerte,
                color = Carbon
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = presupuesto,
                    style = DonChambitasTema.tipografia.cuerpoFuerte,
                    color = Carbon
                )

                Text(
                    text = fecha,
                    style = DonChambitasTema.tipografia.pie,
                    color = Cafe
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFDF8)
@Composable
private fun PreviaTarjetaTrabajador() {
    DonChambitasTema {
        TarjetaTrabajador(
            nombre = "Mario González",
            oficio = "Plomero y electricista",
            calificacion = 4.8f,
            totalResenas = 34,
            municipio = "Monterrey, N.L.",
            precioDesdeTexto = "Desde $350"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFDF8)
@Composable
private fun PreviaTarjetaTrabajadorSinUbicacionNiDisponibilidad() {
    DonChambitasTema {
        TarjetaTrabajador(
            nombre = "Mario González",
            oficio = "Plomero y electricista",
            calificacion = 0f,
            municipio = null,
            disponible = false
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFDF8)
@Composable
private fun PreviaTarjetaServicio() {
    DonChambitasTema {
        TarjetaServicio(
            titulo = "Instalación y reparación de tuberías de cobre",
            categoria = "Plomería",
            precioTexto = "$350 - $800 MXN"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFDF8)
@Composable
private fun PreviaTarjetaServicioPausado() {
    DonChambitasTema {
        TarjetaServicio(
            titulo = "Destape de drenaje",
            categoria = "Plomería",
            precioTexto = "",
            pausado = true
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFDF8)
@Composable
private fun PreviaTarjetaSolicitud() {
    DonChambitasTema {
        TarjetaSolicitud(
            titulo = "Reparación de fuga en baño principal",
            categoria = "Plomería",
            presupuesto = "$500 MXN",
            estado = EstadoSolicitud.ABIERTA,
            fecha = "Hoy, 14:30"
        )
    }
}
