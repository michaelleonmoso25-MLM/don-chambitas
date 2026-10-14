package mx.donchambitas.app.ui.navegacion

import androidx.navigation.NavType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para el sistema de navegación, rutas, barras inferiores y guardas de acceso.
 * Valida los criterios de aceptación de los tickets S1-T12 y S2-T15.
 */
class NavegacionTest {

    @Test
    fun las19RutasExistenYEstanDefinidas() {
        assertEquals("Deben existir exactamente 19 rutas", 19, TODAS_LAS_RUTAS.size)

        val idsEsperados = (1..19).map { "P-%02d".format(it) }.toSet()
        val idsPresentes = TODAS_LAS_RUTAS.map { it.idPantalla }.toSet()
        assertEquals("Todas las pantallas de P-01 a P-19 deben estar cubiertas", idsEsperados, idsPresentes)

        val rutasUnicas = TODAS_LAS_RUTAS.map { it.ruta }.toSet()
        assertEquals("Todas las rutas deben tener un identificador único", 19, rutasUnicas.size)

        TODAS_LAS_RUTAS.forEach { ruta ->
            assertTrue("El título no debe estar vacío: ${ruta.idPantalla}", ruta.titulo.isNotBlank())
            assertTrue("La ruta no debe estar vacía: ${ruta.idPantalla}", ruta.ruta.isNotBlank())
        }
    }

    @Test
    fun subgrafosEstanDefinidosCorrectamente() {
        val subgrafos = listOf(
            Subgrafo.Autenticacion,
            Subgrafo.Cliente,
            Subgrafo.Trabajador
        )
        assertEquals(3, subgrafos.size)
        subgrafos.forEach { subgrafo ->
            assertTrue(subgrafo.ruta.isNotBlank())
            assertTrue(subgrafo.nombre.isNotBlank())
        }
    }

    @Test
    fun rutasConArgumentosDeclaranTiposCorrectamente() {
        // P-07 PerfilTrabajador
        assertEquals(1, Ruta.PerfilTrabajador.argumentos.size)
        val argTrabajador = Ruta.PerfilTrabajador.argumentos.first()
        assertEquals(Ruta.PerfilTrabajador.ARG_TRABAJADOR_ID, argTrabajador.name)
        assertEquals(NavType.StringType, argTrabajador.argument.type)
        assertFalse(argTrabajador.argument.isNullable)

        // P-13 CrearEditarServicio
        assertEquals(1, Ruta.CrearEditarServicio.argumentos.size)
        val argServicio = Ruta.CrearEditarServicio.argumentos.first()
        assertEquals(Ruta.CrearEditarServicio.ARG_SERVICIO_ID, argServicio.name)
        assertEquals(NavType.StringType, argServicio.argument.type)
        assertTrue(argServicio.argument.isNullable)

        // P-16 Chat
        assertEquals(1, Ruta.Chat.argumentos.size)
        val argChat = Ruta.Chat.argumentos.first()
        assertEquals(Ruta.Chat.ARG_CONVERSACION_ID, argChat.name)
        assertEquals(NavType.StringType, argChat.argument.type)
        assertFalse(argChat.argument.isNullable)

        // P-17 DejarResena
        assertEquals(1, Ruta.DejarResena.argumentos.size)
        val argResena = Ruta.DejarResena.argumentos.first()
        assertEquals(Ruta.DejarResena.ARG_SOLICITUD_ID, argResena.name)
        assertEquals(NavType.StringType, argResena.argument.type)
        assertFalse(argResena.argument.isNullable)

        // P-19 DetalleSolicitud
        assertEquals(1, Ruta.DetalleSolicitud.argumentos.size)
        val argDetalle = Ruta.DetalleSolicitud.argumentos.first()
        assertEquals(Ruta.DetalleSolicitud.ARG_SOLICITUD_ID, argDetalle.name)
        assertEquals(NavType.StringType, argDetalle.argument.type)
        assertFalse(argDetalle.argument.isNullable)
    }

    @Test
    fun barrasInferioresTienenExactamenteCuatroDestinosCadaUna() {
        assertEquals("Barra inferior de Cliente debe tener 4 destinos", 4, DESTINOS_BARRA_CLIENTE.size)
        val rutasCliente = DESTINOS_BARRA_CLIENTE.map { it.ruta }
        assertTrue(rutasCliente.contains(Ruta.InicioCliente))
        assertTrue(rutasCliente.contains(Ruta.MisSolicitudes))
        assertTrue(rutasCliente.contains(Ruta.Conversaciones))
        assertTrue(rutasCliente.contains(Ruta.MiCuenta))

        assertEquals("Barra inferior de Trabajador debe tener 4 destinos", 4, DESTINOS_BARRA_TRABAJADOR.size)
        val rutasTrabajador = DESTINOS_BARRA_TRABAJADOR.map { it.ruta }
        assertTrue(rutasTrabajador.contains(Ruta.InicioTrabajador))
        assertTrue(rutasTrabajador.contains(Ruta.MisServicios))
        assertTrue(rutasTrabajador.contains(Ruta.Conversaciones))
        assertTrue(rutasTrabajador.contains(Ruta.MiCuenta))
    }

    @Test
    fun las8PantallasQueVanEncimaNoMuestranBarraInferior() {
        assertEquals("Deben ser exactamente 8 pantallas que van encima", 8, PANTALLAS_ENCIMA.size)

        val idsEncimaEsperados = setOf("P-04", "P-06", "P-07", "P-08", "P-13", "P-16", "P-17", "P-19")
        val idsEncimaPresentes = PANTALLAS_ENCIMA.map { it.idPantalla }.toSet()
        assertEquals(idsEncimaEsperados, idsEncimaPresentes)

        // Ninguna de las 8 debe mostrar barra inferior para cliente ni para trabajador
        PANTALLAS_ENCIMA.forEach { ruta ->
            assertFalse(
                "La pantalla encima ${ruta.idPantalla} no debe mostrar barra para Cliente",
                debeMostrarBarraInferior(ruta.ruta, EstadoSesion.CLIENTE)
            )
            assertFalse(
                "La pantalla encima ${ruta.idPantalla} no debe mostrar barra para Trabajador",
                debeMostrarBarraInferior(ruta.ruta, EstadoSesion.TRABAJADOR)
            )
        }

        // Pantallas con barra inferior para Cliente
        assertTrue(debeMostrarBarraInferior(Ruta.InicioCliente.ruta, EstadoSesion.CLIENTE))
        assertTrue(debeMostrarBarraInferior(Ruta.MisSolicitudes.ruta, EstadoSesion.CLIENTE))
        assertTrue(debeMostrarBarraInferior(Ruta.Conversaciones.ruta, EstadoSesion.CLIENTE))
        assertTrue(debeMostrarBarraInferior(Ruta.MiCuenta.ruta, EstadoSesion.CLIENTE))

        // Pantallas con barra inferior para Trabajador
        assertTrue(debeMostrarBarraInferior(Ruta.InicioTrabajador.ruta, EstadoSesion.TRABAJADOR))
        assertTrue(debeMostrarBarraInferior(Ruta.MisServicios.ruta, EstadoSesion.TRABAJADOR))
        assertTrue(debeMostrarBarraInferior(Ruta.Conversaciones.ruta, EstadoSesion.TRABAJADOR))
        assertTrue(debeMostrarBarraInferior(Ruta.MiCuenta.ruta, EstadoSesion.TRABAJADOR))
    }

    @Test
    fun sinSesionCualquierRutaProtegidaLlevaAIniciarSesion() {
        val rutasProtegidas = RUTAS_CLIENTE + RUTAS_TRABAJADOR + RUTAS_COMPARTIDAS
        rutasProtegidas.forEach { ruta ->
            val redireccion = resolverGuarda(ruta.ruta, EstadoSesion.SIN_SESION)
            assertEquals(
                "Sin sesión, ${ruta.idPantalla} debe redirigir a Iniciar sesión",
                Ruta.IniciarSesion.ruta,
                redireccion
            )
        }

        // Rutas de autenticación están permitidas sin sesión
        RUTAS_AUTENTICACION.forEach { ruta ->
            val redireccion = resolverGuarda(ruta.ruta, EstadoSesion.SIN_SESION)
            assertNull("Ruta de autenticación ${ruta.idPantalla} debe estar permitida sin sesión", redireccion)
        }
    }

    @Test
    fun unClienteNoAlcanzaRutaDeTrabajadorYRedirigeAInicioCliente() {
        RUTAS_TRABAJADOR.forEach { ruta ->
            val redireccion = resolverGuarda(ruta.ruta, EstadoSesion.CLIENTE)
            assertEquals(
                "Cliente intentando acceder a ${ruta.idPantalla} debe redirigir a InicioCliente",
                Ruta.InicioCliente.ruta,
                redireccion
            )
        }

        // Cliente sí puede acceder a sus rutas y compartidas
        (RUTAS_CLIENTE + RUTAS_COMPARTIDAS).forEach { ruta ->
            val redireccion = resolverGuarda(ruta.ruta, EstadoSesion.CLIENTE)
            assertNull("Cliente debe poder acceder a ${ruta.idPantalla}", redireccion)
        }
    }

    @Test
    fun unTrabajadorNoAlcanzaRutaDeClienteYRedirigeAInicioTrabajador() {
        RUTAS_CLIENTE.forEach { ruta ->
            val redireccion = resolverGuarda(ruta.ruta, EstadoSesion.TRABAJADOR)
            assertEquals(
                "Trabajador intentando acceder a ${ruta.idPantalla} debe redirigir a InicioTrabajador",
                Ruta.InicioTrabajador.ruta,
                redireccion
            )
        }

        // Trabajador sí puede acceder a sus rutas y compartidas
        (RUTAS_TRABAJADOR + RUTAS_COMPARTIDAS).forEach { ruta ->
            val redireccion = resolverGuarda(ruta.ruta, EstadoSesion.TRABAJADOR)
            assertNull("Trabajador debe poder acceder a ${ruta.idPantalla}", redireccion)
        }
    }

    @Test
    fun conSesionLosFormulariosDeEntradaLlevanAlInicioDelRol() {
        val formularios = RUTAS_AUTENTICACION - Ruta.Splash
        formularios.forEach { ruta ->
            assertEquals(
                "Con sesión de cliente, ${ruta.idPantalla} debe llevar a InicioCliente",
                Ruta.InicioCliente.ruta,
                resolverGuarda(ruta.ruta, EstadoSesion.CLIENTE)
            )
            assertEquals(
                "Con sesión de trabajador, ${ruta.idPantalla} debe llevar a InicioTrabajador",
                Ruta.InicioTrabajador.ruta,
                resolverGuarda(ruta.ruta, EstadoSesion.TRABAJADOR)
            )
        }

        // P-01 decide el destino, así que siempre pasa
        assertNull(resolverGuarda(Ruta.Splash.ruta, EstadoSesion.CLIENTE))
        assertNull(resolverGuarda(Ruta.Splash.ruta, EstadoSesion.TRABAJADOR))
    }

    @Test
    fun mientrasSeVerificaLaSesionNingunaGuardaRedirigeNiHayBarra() {
        TODAS_LAS_RUTAS.forEach { ruta ->
            assertNull(
                "Verificando, ${ruta.idPantalla} no debe redirigir",
                resolverGuarda(ruta.ruta, EstadoSesion.VERIFICANDO)
            )
            assertFalse(
                "Verificando, ${ruta.idPantalla} no debe mostrar barra",
                debeMostrarBarraInferior(ruta.ruta, EstadoSesion.VERIFICANDO)
            )
        }
    }

    @Test
    fun encontrarRutaFuncionaConRutasInstanciadasYConParametros() {
        val rutaPerfil = encontrarRuta(Ruta.PerfilTrabajador.crearRuta("uuid-trabajador-123"))
        assertNotNull(rutaPerfil)
        assertEquals(Ruta.PerfilTrabajador, rutaPerfil)

        val rutaServicio = encontrarRuta(Ruta.CrearEditarServicio.crearRuta("uuid-servicio-456"))
        assertNotNull(rutaServicio)
        assertEquals(Ruta.CrearEditarServicio, rutaServicio)

        val rutaChat = encontrarRuta(Ruta.Chat.crearRuta("uuid-chat-789"))
        assertNotNull(rutaChat)
        assertEquals(Ruta.Chat, rutaChat)

        val rutaResena = encontrarRuta(Ruta.DejarResena.crearRuta("uuid-solicitud-101"))
        assertNotNull(rutaResena)
        assertEquals(Ruta.DejarResena, rutaResena)

        val rutaDetalle = encontrarRuta(Ruta.DetalleSolicitud.crearRuta("uuid-solicitud-202"))
        assertNotNull(rutaDetalle)
        assertEquals(Ruta.DetalleSolicitud, rutaDetalle)
    }
}
