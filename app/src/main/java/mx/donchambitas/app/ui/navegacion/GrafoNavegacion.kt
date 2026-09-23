package mx.donchambitas.app.ui.navegacion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.componentes.BarraSuperior
import mx.donchambitas.app.ui.pantallas.IniciarSesionPantalla
import mx.donchambitas.app.ui.pantallas.RegistroPantalla
import mx.donchambitas.app.ui.pantallas.SplashPantalla
import mx.donchambitas.app.ui.componentes.BotonDestacado
import mx.donchambitas.app.ui.componentes.BotonPrincipal
import mx.donchambitas.app.ui.componentes.BotonSecundario
import mx.donchambitas.app.ui.componentes.BotonTexto
import mx.donchambitas.app.ui.tema.Arena
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.Cafe
import mx.donchambitas.app.ui.tema.Carbon
import mx.donchambitas.app.ui.tema.Crema
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Error
import mx.donchambitas.app.ui.tema.Exito
import mx.donchambitas.app.ui.tema.Mostaza
import mx.donchambitas.app.ui.tema.MostazaOscuro
import mx.donchambitas.app.ui.tema.Terracota
import mx.donchambitas.app.ui.tema.cuerpo
import mx.donchambitas.app.ui.tema.cuerpoFuerte
import mx.donchambitas.app.ui.tema.pie
import mx.donchambitas.app.ui.tema.secundario
import mx.donchambitas.app.ui.tema.subtitulo
import mx.donchambitas.app.ui.tema.titulo

/**
 * Composable principal de navegación de la aplicación Don Chambitas.
 * Gestiona el grafo completo con 3 subgrafos, 19 pantallas, barras inferiores
 * con 4 destinos cada una, botón flotante en P-05 y guardas por rol y sesión.
 */
@Composable
fun GrafoNavegacion(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val estadoSesion = MarcadorSesionTemporal.estado

    // Guarda global de navegación en tiempo real
    LaunchedEffect(rutaActual, estadoSesion) {
        if (rutaActual != null) {
            val redireccion = resolverGuarda(rutaActual, estadoSesion)
            if (redireccion != null && redireccion != rutaActual) {
                navController.navigate(redireccion) {
                    popUpTo(rutaActual) { inclusive = true }
                }
            }
        }
    }

    val rutaEncontrada = rutaActual?.let { encontrarRuta(it) }
    val tituloBarraSuperior = rutaEncontrada?.let { "${it.idPantalla} · ${it.titulo}" }
        ?: stringResource(R.string.app_name)

    val esDestinoRaiz = when (rutaActual) {
        Ruta.Splash.ruta,
        Ruta.IniciarSesion.ruta,
        Ruta.InicioCliente.ruta,
        Ruta.InicioTrabajador.ruta -> true
        else -> false
    }
    val puedeRegresar = navController.previousBackStackEntry != null && !esDestinoRaiz

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (rutaActual !in RUTAS_SIN_BARRA_DEL_ANDAMIO) {
                BarraSuperior(
                    titulo = tituloBarraSuperior,
                    alRegresar = if (puedeRegresar) {
                        { navController.popBackStack() }
                    } else null
                )
            }
        },
        bottomBar = {
            if (debeMostrarBarraInferior(rutaActual, estadoSesion)) {
                when (estadoSesion) {
                    EstadoSesionTemporal.CLIENTE -> {
                        BarraInferiorCliente(
                            rutaActual = rutaActual,
                            alSeleccionarRuta = { ruta ->
                                navController.navigate(ruta.ruta) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    EstadoSesionTemporal.TRABAJADOR -> {
                        BarraInferiorTrabajador(
                            rutaActual = rutaActual,
                            alSeleccionarRuta = { ruta ->
                                navController.navigate(ruta.ruta) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    EstadoSesionTemporal.SIN_SESION -> {
                        // Sin sesión no se muestra barra inferior
                    }
                }
            }
        },
        floatingActionButton = {
            if (rutaActual == Ruta.InicioCliente.ruta && estadoSesion == EstadoSesionTemporal.CLIENTE) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Ruta.PublicarSolicitud.ruta)
                    },
                    containerColor = Mostaza,
                    contentColor = Carbon
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.boton_publicar_solicitud)
                    )
                }
            }
        },
        containerColor = Crema
    ) { rellenoInterno ->
        // Sin barra del andamio, el relleno superior tiene que irse con ella: si
        // se conserva, la pantalla arranca debajo de la barra de estado y su
        // propia BarraSuperior se queda sin inset donde dibujar, con lo que el
        // reloj y los iconos quedan sobre el fondo Crema y no se leen.
        val direccion = LocalLayoutDirection.current
        val rellenoContenido = if (rutaActual in RUTAS_SIN_BARRA_DEL_ANDAMIO) {
            PaddingValues(
                start = rellenoInterno.calculateStartPadding(direccion),
                top = 0.dp,
                end = rellenoInterno.calculateEndPadding(direccion),
                bottom = rellenoInterno.calculateBottomPadding()
            )
        } else {
            rellenoInterno
        }

        NavHost(
            navController = navController,
            startDestination = Subgrafo.Autenticacion.ruta,
            modifier = Modifier
                .fillMaxSize()
                .padding(rellenoContenido)
        ) {
            subgrafoAutenticacion(navController)
            subgrafoCliente(navController)
            subgrafoTrabajador(navController)
            pantallasCompartidas(navController)
        }
    }
}

/**
 * Rutas donde el andamio de S1-T12 no debe pintar barra superior, porque la
 * pantalla real ya resuelve su propia parte de arriba:
 *
 * - P-01 es de pantalla completa por diseño (S1-T15).
 * - P-02 y P-03 traen su propio Scaffold con BarraSuperior, que es lo que pide
 *   la regla 1.1 de DISENO-AUTENTICACION.md.
 *
 * Sin esto salen dos barras encimadas, con dos títulos y dos flechas de
 * regreso. El andamio nació cuando los 19 destinos eran MarcadorPantalla y su
 * título lleva el identificador delante ("P-02 · Iniciar sesión"), que es
 * andamio, no interfaz de producto.
 *
 * **Cada pantalla que sustituya a su marcador se agrega aquí.**
 */
private val RUTAS_SIN_BARRA_DEL_ANDAMIO = setOf(
    Ruta.Splash.ruta,
    Ruta.IniciarSesion.ruta,
    Ruta.Registro.ruta
)

/**
 * Subgrafo de autenticación (P-01 a P-04).
 */
private fun NavGraphBuilder.subgrafoAutenticacion(navController: NavHostController) {
    navigation(
        startDestination = Ruta.Splash.ruta,
        route = Subgrafo.Autenticacion.ruta
    ) {
        composable(Ruta.Splash.ruta) {
            SplashPantalla(
                alNavegarADestino = { destino ->
                    navController.navigate(destino.ruta) {
                        popUpTo(Ruta.Splash.ruta) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Ruta.IniciarSesion.ruta) {
            IniciarSesionPantalla(
                alNavegarADestino = { destino -> entrarConSesion(navController, destino) },
                alIrARegistro = { navController.navigate(Ruta.Registro.ruta) },
                alIrARecuperarContrasena = {
                    navController.navigate(Ruta.RecuperarContrasena.ruta)
                }
            )
        }

        composable(Ruta.Registro.ruta) {
            RegistroPantalla(
                alNavegarADestino = { destino -> entrarConSesion(navController, destino) },
                alRegresar = { navController.popBackStack() },
                alIrAIniciarSesion = { navController.popBackStack() }
            )
        }

        composable(Ruta.RecuperarContrasena.ruta) {
            MarcadorPantalla(
                ruta = Ruta.RecuperarContrasena,
                descripcion = "Se pide el correo, se avisa que se envió el enlace. Pantalla que se abre encima (sin barra).",
                navController = navController,
                acciones = listOf(
                    AccionNavegacion("Volver a Iniciar sesión (P-02)") {
                        navController.popBackStack()
                    }
                )
            )
        }
    }
}

/**
 * Entra al inicio del rol despues de iniciar sesion o registrarse, limpiando
 * la pila hasta el subgrafo de autenticacion (2.4 de DISENO-AUTENTICACION.md):
 * el boton atras ya no regresa al formulario.
 *
 * Sigue fijando el marcador temporal de S1-T12 porque P-01 lo lee para
 * decidir a donde ir; lo retira S2-T09 cuando la sesion salga del repositorio.
 */
private fun entrarConSesion(navController: NavHostController, destino: Ruta) {
    MarcadorSesionTemporal.estado = when (destino) {
        Ruta.InicioTrabajador -> EstadoSesionTemporal.TRABAJADOR
        else -> EstadoSesionTemporal.CLIENTE
    }
    navController.navigate(destino.ruta) {
        popUpTo(Subgrafo.Autenticacion.ruta) { inclusive = true }
    }
}

/**
 * Subgrafo de cliente (P-05 a P-09).
 */
private fun NavGraphBuilder.subgrafoCliente(navController: NavHostController) {
    navigation(
        startDestination = Ruta.InicioCliente.ruta,
        route = Subgrafo.Cliente.ruta
    ) {
        composable(Ruta.InicioCliente.ruta) {
            MarcadorPantalla(
                ruta = Ruta.InicioCliente,
                descripcion = "Buscador, cuadrícula de categorías y trabajadores mejor calificados. Botón flotante (+) lleva a P-08.",
                navController = navController,
                acciones = listOf(
                    AccionNavegacion("Ver resultados de búsqueda (P-06)") {
                        navController.navigate(Ruta.Resultados.ruta)
                    },
                    AccionNavegacion("Ver perfil público del trabajador (P-07)") {
                        navController.navigate(Ruta.PerfilTrabajador.crearRuta("trabajador-demo-01"))
                    },
                    AccionNavegacion("Publicar solicitud (P-08)") {
                        navController.navigate(Ruta.PublicarSolicitud.ruta)
                    },
                    AccionNavegacion("Mis solicitudes (P-09)") {
                        navController.navigate(Ruta.MisSolicitudes.ruta)
                    }
                )
            )
        }

        composable(Ruta.Resultados.ruta) {
            MarcadorPantalla(
                ruta = Ruta.Resultados,
                descripcion = "Lista de trabajadores con filtros de categoría, estado, municipio, precio y calificación. Pantalla encima (sin barra).",
                navController = navController,
                acciones = listOf(
                    AccionNavegacion("Ver perfil público de un trabajador (P-07)") {
                        navController.navigate(Ruta.PerfilTrabajador.crearRuta("trabajador-demo-02"))
                    }
                )
            )
        }

        composable(
            route = Ruta.PerfilTrabajador.ruta,
            arguments = Ruta.PerfilTrabajador.argumentos
        ) { backStackEntry ->
            val trabajadorId = backStackEntry.arguments?.getString(Ruta.PerfilTrabajador.ARG_TRABAJADOR_ID)
            MarcadorPantalla(
                ruta = Ruta.PerfilTrabajador,
                descripcion = "Datos, habilidades, servicios, reseñas y botón de contactar. Pantalla encima (sin barra).",
                argumentos = mapOf(Ruta.PerfilTrabajador.ARG_TRABAJADOR_ID to trabajadorId),
                navController = navController,
                acciones = listOf(
                    AccionNavegacion("Contactar trabajador -> Chat (P-16)") {
                        navController.navigate(Ruta.Chat.crearRuta("conv-demo-01"))
                    }
                )
            )
        }

        composable(Ruta.PublicarSolicitud.ruta) {
            MarcadorPantalla(
                ruta = Ruta.PublicarSolicitud,
                descripcion = "Título, descripción, categoría, presupuesto y ubicación. Botón de redactar con IA. Pantalla encima (sin barra).",
                navController = navController,
                acciones = listOf(
                    AccionNavegacion("Guardar e ir a Mis solicitudes (P-09)") {
                        navController.navigate(Ruta.MisSolicitudes.ruta)
                    }
                )
            )
        }

        composable(Ruta.MisSolicitudes.ruta) {
            MarcadorPantalla(
                ruta = Ruta.MisSolicitudes,
                descripcion = "Lista de las solicitudes del cliente con su estado.",
                navController = navController,
                acciones = listOf(
                    AccionNavegacion("Ver detalle de solicitud (P-19)") {
                        navController.navigate(Ruta.DetalleSolicitud.crearRuta("solicitud-demo-01"))
                    }
                )
            )
        }
    }
}

/**
 * Subgrafo de trabajador (P-10 a P-14).
 */
private fun NavGraphBuilder.subgrafoTrabajador(navController: NavHostController) {
    navigation(
        startDestination = Ruta.InicioTrabajador.ruta,
        route = Subgrafo.Trabajador.ruta
    ) {
        composable(Ruta.InicioTrabajador.ruta) {
            MarcadorPantalla(
                ruta = Ruta.InicioTrabajador,
                descripcion = "Solicitudes abiertas, filtrables por categoría.",
                navController = navController,
                acciones = listOf(
                    AccionNavegacion("Ver detalle de solicitud para postularme (P-19)") {
                        navController.navigate(Ruta.DetalleSolicitud.crearRuta("solicitud-demo-02"))
                    },
                    AccionNavegacion("Mis servicios (P-12)") {
                        navController.navigate(Ruta.MisServicios.ruta)
                    },
                    AccionNavegacion("Mi perfil (P-11)") {
                        navController.navigate(Ruta.MiPerfil.ruta)
                    },
                    AccionNavegacion("Mis postulaciones (P-14)") {
                        navController.navigate(Ruta.MisPostulaciones.ruta)
                    }
                )
            )
        }

        composable(Ruta.MiPerfil.ruta) {
            MarcadorPantalla(
                ruta = Ruta.MiPerfil,
                descripcion = "Ver y editar título, descripción, habilidades, experiencia, contacto y ubicación. Acceso desde P-18.",
                navController = navController,
                acciones = listOf(
                    AccionNavegacion("Volver a Mi cuenta (P-18)") {
                        navController.navigate(Ruta.MiCuenta.ruta)
                    }
                )
            )
        }

        composable(Ruta.MisServicios.ruta) {
            MarcadorPantalla(
                ruta = Ruta.MisServicios,
                descripcion = "Lista de servicios publicados con editar, pausar y eliminar.",
                navController = navController,
                acciones = listOf(
                    AccionNavegacion("Crear nuevo servicio (P-13)") {
                        navController.navigate(Ruta.CrearEditarServicio.crearRuta(null))
                    },
                    AccionNavegacion("Editar servicio existente (P-13)") {
                        navController.navigate(Ruta.CrearEditarServicio.crearRuta("servicio-demo-01"))
                    },
                    AccionNavegacion("Mis postulaciones (P-14)") {
                        navController.navigate(Ruta.MisPostulaciones.ruta)
                    }
                )
            )
        }

        composable(
            route = Ruta.CrearEditarServicio.ruta,
            arguments = Ruta.CrearEditarServicio.argumentos
        ) { backStackEntry ->
            val servicioId = backStackEntry.arguments?.getString(Ruta.CrearEditarServicio.ARG_SERVICIO_ID)
            MarcadorPantalla(
                ruta = Ruta.CrearEditarServicio,
                descripcion = "Título, descripción, categoría, precio, hasta 3 fotos. Botón de redactar con IA. Pantalla encima (sin barra).",
                argumentos = mapOf(Ruta.CrearEditarServicio.ARG_SERVICIO_ID to servicioId),
                navController = navController,
                acciones = listOf(
                    AccionNavegacion("Guardar y volver a Mis servicios (P-12)") {
                        navController.popBackStack()
                    }
                )
            )
        }

        composable(Ruta.MisPostulaciones.ruta) {
            MarcadorPantalla(
                ruta = Ruta.MisPostulaciones,
                descripcion = "Lista de postulaciones enviadas con su estado. Acceso desde P-12.",
                navController = navController,
                acciones = listOf(
                    AccionNavegacion("Ver detalle de solicitud (P-19)") {
                        navController.navigate(Ruta.DetalleSolicitud.crearRuta("solicitud-demo-03"))
                    }
                )
            )
        }
    }
}

/**
 * Pantallas compartidas entre cliente y trabajador (P-15 a P-19).
 */
private fun NavGraphBuilder.pantallasCompartidas(navController: NavHostController) {
    composable(Ruta.Conversaciones.ruta) {
        MarcadorPantalla(
            ruta = Ruta.Conversaciones,
            descripcion = "Bandeja de chats ordenada por mensaje más reciente. Presente en ambas barras inferiores.",
            navController = navController,
            acciones = listOf(
                AccionNavegacion("Abrir chat (P-16)") {
                    navController.navigate(Ruta.Chat.crearRuta("conv-demo-02"))
                }
            )
        )
    }

    composable(
        route = Ruta.Chat.ruta,
        arguments = Ruta.Chat.argumentos
    ) { backStackEntry ->
        val convId = backStackEntry.arguments?.getString(Ruta.Chat.ARG_CONVERSACION_ID)
        MarcadorPantalla(
            ruta = Ruta.Chat,
            descripcion = "Conversación de texto plano con un usuario. Pantalla encima (sin barra).",
            argumentos = mapOf(Ruta.Chat.ARG_CONVERSACION_ID to convId),
            navController = navController,
            acciones = listOf(
                AccionNavegacion("Volver a Conversaciones (P-15)") {
                    navController.popBackStack()
                }
            )
        )
    }

    composable(
        route = Ruta.DejarResena.ruta,
        arguments = Ruta.DejarResena.argumentos
    ) { backStackEntry ->
        val solicitudId = backStackEntry.arguments?.getString(Ruta.DejarResena.ARG_SOLICITUD_ID)
        MarcadorPantalla(
            ruta = Ruta.DejarResena,
            descripcion = "Calificación de 1 a 5 estrellas y comentario. Solo tras cerrar una solicitud. Pantalla encima (sin barra).",
            argumentos = mapOf(Ruta.DejarResena.ARG_SOLICITUD_ID to solicitudId),
            navController = navController,
            acciones = listOf(
                AccionNavegacion("Guardar reseña y volver") {
                    navController.popBackStack()
                }
            )
        )
    }

    composable(Ruta.MiCuenta.ruta) {
        MarcadorPantalla(
            ruta = Ruta.MiCuenta,
            descripcion = "Datos personales, foto, cambiar contraseña, cerrar sesión. Presente en ambas barras inferiores.",
            navController = navController,
            acciones = listOf(
                AccionNavegacion("Mi perfil (solo si Trabajador, P-11)") {
                    navController.navigate(Ruta.MiPerfil.ruta)
                },
                AccionNavegacion("Cerrar sesión (P-02)") {
                    MarcadorSesionTemporal.estado = EstadoSesionTemporal.SIN_SESION
                    navController.navigate(Ruta.IniciarSesion.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        )
    }

    composable(
        route = Ruta.DetalleSolicitud.ruta,
        arguments = Ruta.DetalleSolicitud.argumentos
    ) { backStackEntry ->
        val solicitudId = backStackEntry.arguments?.getString(Ruta.DetalleSolicitud.ARG_SOLICITUD_ID)
        MarcadorPantalla(
            ruta = Ruta.DetalleSolicitud,
            descripcion = "Datos de la solicitud, postulaciones recibidas y acciones según el rol. Pantalla encima (sin barra).",
            argumentos = mapOf(Ruta.DetalleSolicitud.ARG_SOLICITUD_ID to solicitudId),
            navController = navController,
            acciones = listOf(
                AccionNavegacion("Dejar reseña al cerrar (P-17)") {
                    navController.navigate(Ruta.DejarResena.crearRuta(solicitudId ?: "solicitud-demo-01"))
                },
                AccionNavegacion("Contactar por chat (P-16)") {
                    navController.navigate(Ruta.Chat.crearRuta("conv-demo-03"))
                }
            )
        )
    }
}

/**
 * Modelo para las acciones de navegación sugeridas en los marcadores.
 */
data class AccionNavegacion(
    val texto: String,
    val alHacerClic: () -> Unit
)

/**
 * Marcador visual provisional para cada una de las 19 pantallas según S1-T12.
 * Permite probar interactivamente las 19 rutas, sus argumentos y las guardas de rol.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarcadorPantalla(
    ruta: Ruta,
    descripcion: String,
    navController: NavHostController,
    argumentos: Map<String, String?> = emptyMap(),
    acciones: List<AccionNavegacion> = emptyList(),
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val estadoSesionActual = MarcadorSesionTemporal.estado

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tarjeta de información de la pantalla
        Surface(
            color = Arena,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Mostaza)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = ruta.idPantalla,
                            style = DonChambitasTema.tipografia.cuerpoFuerte,
                            color = Carbon
                        )
                    }

                    Text(
                        text = ruta.titulo,
                        style = DonChambitasTema.tipografia.titulo,
                        color = Carbon
                    )
                }

                Text(
                    text = descripcion,
                    style = DonChambitasTema.tipografia.cuerpo,
                    color = Cafe
                )

                if (argumentos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Argumentos recibidos:",
                        style = DonChambitasTema.tipografia.cuerpoFuerte,
                        color = Carbon
                    )
                    argumentos.forEach { (clave, valor) ->
                        Text(
                            text = "• $clave: ${valor ?: "(nulo)"}",
                            style = DonChambitasTema.tipografia.secundario,
                            color = Carbon
                        )
                    }
                }
            }
        }

        // Selector de rol de sesión temporal para pruebas de guardas
        Surface(
            color = Arena,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = Carbon,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Control de sesión temporal (guardas):",
                        style = DonChambitasTema.tipografia.cuerpoFuerte,
                        color = Carbon
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EstadoSesionTemporal.entries.forEach { rol ->
                        val activo = estadoSesionActual == rol
                        val fondo = if (activo) Mostaza else Crema
                        val textoColor = if (activo) Carbon else Cafe
                        val bordeColor = if (activo) MostazaOscuro else Borde

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(fondo)
                                .border(1.dp, bordeColor, RoundedCornerShape(12.dp))
                                .clickable {
                                    MarcadorSesionTemporal.estado = rol
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = rol.etiqueta,
                                style = if (activo) DonChambitasTema.tipografia.cuerpoFuerte else DonChambitasTema.tipografia.pie,
                                color = textoColor
                            )
                        }
                    }
                }
            }
        }

        // Acciones sugeridas de navegación para esta pantalla
        if (acciones.isNotEmpty()) {
            Surface(
                color = Arena,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Acciones en esta pantalla:",
                        style = DonChambitasTema.tipografia.cuerpoFuerte,
                        color = Carbon
                    )
                    acciones.forEach { accion ->
                        BotonPrincipal(
                            texto = accion.texto,
                            onClick = accion.alHacerClic,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Pruebas de guardas de rol prohibido
        Surface(
            color = Arena,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Terracota,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Pruebas de guardas:",
                        style = DonChambitasTema.tipografia.cuerpoFuerte,
                        color = Carbon
                    )
                }

                when (estadoSesionActual) {
                    EstadoSesionTemporal.SIN_SESION -> {
                        Text(
                            text = "Sin sesión activa: intentar ir a pantallas privadas debe redirigir a P-02.",
                            style = DonChambitasTema.tipografia.pie,
                            color = Cafe
                        )
                        BotonSecundario(
                            texto = "Intentar ir a Inicio Cliente (P-05)",
                            onClick = {
                                navController.navigate(Ruta.InicioCliente.ruta)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    EstadoSesionTemporal.CLIENTE -> {
                        Text(
                            text = "Como Cliente: intentar ir a rutas de trabajador debe redirigir a P-05.",
                            style = DonChambitasTema.tipografia.pie,
                            color = Cafe
                        )
                        BotonSecundario(
                            texto = "Intentar ir a Inicio Trabajador (P-10)",
                            onClick = {
                                navController.navigate(Ruta.InicioTrabajador.ruta)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    EstadoSesionTemporal.TRABAJADOR -> {
                        Text(
                            text = "Como Trabajador: intentar ir a rutas de cliente debe redirigir a P-10.",
                            style = DonChambitasTema.tipografia.pie,
                            color = Cafe
                        )
                        BotonSecundario(
                            texto = "Intentar ir a Inicio Cliente (P-05)",
                            onClick = {
                                navController.navigate(Ruta.InicioCliente.ruta)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Mapa completo de las 19 pantallas para recorrer la aplicación
        Surface(
            color = Arena,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Recorrer las 19 pantallas (Mapa completo):",
                    style = DonChambitasTema.tipografia.cuerpoFuerte,
                    color = Carbon
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TODAS_LAS_RUTAS.forEach { destino ->
                        val esActual = destino.idPantalla == ruta.idPantalla
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (esActual) Terracota else Crema)
                                .border(1.dp, if (esActual) Terracota else Borde, RoundedCornerShape(8.dp))
                                .clickable {
                                    val rutaDestino = when (destino) {
                                        Ruta.PerfilTrabajador -> Ruta.PerfilTrabajador.crearRuta("demo-trabajador")
                                        Ruta.CrearEditarServicio -> Ruta.CrearEditarServicio.crearRuta("demo-servicio")
                                        Ruta.Chat -> Ruta.Chat.crearRuta("demo-chat")
                                        Ruta.DejarResena -> Ruta.DejarResena.crearRuta("demo-solicitud")
                                        Ruta.DetalleSolicitud -> Ruta.DetalleSolicitud.crearRuta("demo-solicitud")
                                        else -> destino.ruta
                                    }
                                    navController.navigate(rutaDestino)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${destino.idPantalla} ${destino.titulo}",
                                style = DonChambitasTema.tipografia.pie,
                                color = if (esActual) Crema else Carbon
                            )
                        }
                    }
                }
            }
        }
    }
}
