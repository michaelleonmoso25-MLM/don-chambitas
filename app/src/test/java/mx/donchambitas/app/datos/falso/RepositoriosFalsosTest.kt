package mx.donchambitas.app.datos.falso

import java.math.BigDecimal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import mx.donchambitas.app.dominio.modelo.EstadoPostulacion
import mx.donchambitas.app.dominio.modelo.EstadoSolicitud
import mx.donchambitas.app.dominio.modelo.FiltrosBusquedaTrabajadores
import mx.donchambitas.app.dominio.modelo.FuncionIa
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.modelo.Servicio
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.dominio.modelo.Solicitud
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RepositoriosFalsosTest {

    private lateinit var fuente: FuenteDatosFalsa
    private lateinit var repoAuth: RepositorioAuthFalso
    private lateinit var repoUsuario: RepositorioUsuarioFalso
    private lateinit var repoTrabajador: RepositorioTrabajadorFalso
    private lateinit var repoServicios: RepositorioServiciosFalso
    private lateinit var repoSolicitudes: RepositorioSolicitudesFalso
    private lateinit var repoPostulaciones: RepositorioPostulacionesFalso
    private lateinit var repoChat: RepositorioChatFalso
    private lateinit var repoResenas: RepositorioResenasFalso
    private lateinit var repoCatalogos: RepositorioCatalogosFalso
    private lateinit var repoIa: RepositorioIaFalso

    @Before
    fun preparar() {
        fuente = FuenteDatosFalsa()
        repoAuth = RepositorioAuthFalso(fuente).apply { retrasoMs = 0L }
        repoUsuario = RepositorioUsuarioFalso(fuente).apply { retrasoMs = 0L }
        repoTrabajador = RepositorioTrabajadorFalso(fuente).apply { retrasoMs = 0L }
        repoServicios = RepositorioServiciosFalso(fuente).apply { retrasoMs = 0L }
        repoSolicitudes = RepositorioSolicitudesFalso(fuente).apply { retrasoMs = 0L }
        repoPostulaciones = RepositorioPostulacionesFalso(fuente).apply { retrasoMs = 0L }
        repoChat = RepositorioChatFalso(fuente).apply { retrasoMs = 0L }
        repoResenas = RepositorioResenasFalso(fuente).apply { retrasoMs = 0L }
        repoCatalogos = RepositorioCatalogosFalso(fuente).apply { retrasoMs = 0L }
        repoIa = RepositorioIaFalso().apply { retrasoMs = 0L }
    }

    @Test
    fun `1 RepositorioAuth registra nuevo usuario e inicia sesion correctamente`() = runBlocking {
        val resReg = repoAuth.registrar(
            correo = "nuevo@prueba.com",
            contrasena = "secret123",
            nombre = "Carlos",
            apellidos = "Perez",
            telefono = "5551234567",
            rol = RolUsuario.CLIENTE
        )
        assertTrue(resReg is Resultado.Exito)
        val sesion = (resReg as Resultado.Exito).dato
        assertEquals("nuevo@prueba.com", sesion.usuario.correo)
        // DEC-25: el registro deja la sesion abierta
        assertEquals(sesion, repoAuth.sesionActual().first())

        // Comprobar inicio de sesion con credenciales correctas
        val resLogin = repoAuth.iniciarSesion("nuevo@prueba.com", "secret123")
        assertTrue(resLogin is Resultado.Exito)

        // Login incorrecto
        val resMalo = repoAuth.iniciarSesion("inexistente@correo.com", "123")
        assertTrue(resMalo is Resultado.Error)
        assertEquals(TipoError.AUTENTICACION, (resMalo as Resultado.Error).tipo)

        // Forzar error
        repoAuth.errorForzado = TipoError.RED
        val resForzado = repoAuth.cerrarSesion()
        assertTrue(resForzado is Resultado.Error)
        assertEquals(TipoError.RED, (resForzado as Resultado.Error).tipo)
    }

    @Test
    fun `2 RepositorioUsuario obtiene y actualiza datos del usuario autenticado`() = runBlocking {
        val miUsuario = repoUsuario.obtenerMiUsuario()
        assertTrue(miUsuario is Resultado.Exito)
        val u = (miUsuario as Resultado.Exito).dato
        assertEquals("Juan", u.nombre)

        val resAct = repoUsuario.actualizarMiUsuario(
            nombre = "Juan Carlos",
            apellidos = "Perez H.",
            telefono = "2229998877"
        )
        assertTrue(resAct is Resultado.Exito)
        assertEquals("Juan Carlos", (resAct as Resultado.Exito).dato.nombre)

        val resFoto = repoUsuario.subirFotoPerfil(byteArrayOf(1, 2, 3))
        assertTrue(resFoto is Resultado.Exito)
        assertTrue((resFoto as Resultado.Exito).dato.contains("http"))
    }

    @Test
    fun `3 RepositorioTrabajador devuelve perfil publico completo para P-07`() = runBlocking {
        val res = repoTrabajador.obtenerPerfilPublico("usr-trab-1")
        assertTrue(res is Resultado.Exito)
        val perfil = (res as Resultado.Exito).dato
        assertEquals("Pedro", perfil.nombre)
        assertEquals("Puebla", perfil.estado)
        assertEquals("Cholula", perfil.municipio)
        assertTrue(perfil.habilidades.isNotEmpty())
        assertTrue(perfil.servicios.isNotEmpty())
        assertTrue(perfil.calificacion.promedio >= 0.0)
    }

    @Test
    fun `3b RepositorioTrabajador lee las habilidades propias que reemplazarHabilidades escribio`() = runBlocking {
        fuente.fijarSesionActiva(Sesion(fuente.usuarios.first { it.id == "usr-trab-1" }))

        val sembradas = (repoTrabajador.obtenerMisHabilidades() as Resultado.Exito).dato
        assertTrue(sembradas.isNotEmpty())
        assertEquals(sembradas.sorted(), sembradas)

        repoTrabajador.reemplazarHabilidades(listOf("Tinacos", "Boilers"))
        assertEquals(listOf("Boilers", "Tinacos"), (repoTrabajador.obtenerMisHabilidades() as Resultado.Exito).dato)

        fuente.fijarSesionActiva(null)
        val sinSesion = repoTrabajador.obtenerMisHabilidades()
        assertEquals(TipoError.AUTENTICACION, (sinSesion as Resultado.Error).tipo)
    }

    @Test
    fun `4 RepositorioServicios gestiona servicios y respeta tope de 3 fotos`() = runBlocking {
        // Fijar sesion como trabajador 1
        val trab1 = fuente.usuarios.first { it.id == "usr-trab-1" }
        fuente.fijarSesionActiva(Sesion(trab1))

        val misServicios = repoServicios.obtenerMisServicios()
        assertTrue(misServicios is Resultado.Exito)
        val lista = (misServicios as Resultado.Exito).dato
        assertTrue(lista.isNotEmpty())

        val servId = lista.first().id
        // El trabajador ya tiene 1 foto en la semilla. Agregamos 2 más para llegar a 3.
        val f2 = repoServicios.subirFoto(servId, byteArrayOf(1), 2)
        val f3 = repoServicios.subirFoto(servId, byteArrayOf(2), 3)
        assertTrue(f2 is Resultado.Exito)
        assertTrue(f3 is Resultado.Exito)

        // La cuarta foto debe fallar por tope de 3
        val f4 = repoServicios.subirFoto(servId, byteArrayOf(3), 4)
        assertTrue(f4 is Resultado.Error)
        assertEquals(TipoError.VALIDACION, (f4 as Resultado.Error).tipo)

        // H-18: obtenerMisServicios trae las tres, ordenadas por posicion, con su id.
        val conFotos = (repoServicios.obtenerMisServicios() as Resultado.Exito).dato.first { it.id == servId }
        assertEquals(listOf(1, 2, 3), conFotos.fotos.map { it.posicion })
        assertEquals((f2 as Resultado.Exito).dato, conFotos.fotos[1])

        // Pausar devuelve el servicio con sus fotos; actualizar no las duplica en la fuente.
        assertEquals(3, (repoServicios.pausar(servId, false) as Resultado.Exito).dato.fotos.size)
        val actualizado = (repoServicios.actualizar(conFotos.copy(titulo = "Nuevo")) as Resultado.Exito).dato
        assertEquals(3, actualizado.fotos.size)
        assertTrue(fuente.servicios.first { it.id == servId }.fotos.isEmpty())
    }

    @Test
    fun `5 RepositorioSolicitudes devuelve abiertas, detalle y valida cierre`() = runBlocking {
        val abiertas = repoSolicitudes.obtenerAbiertas()
        assertTrue(abiertas is Resultado.Exito)
        val lista = (abiertas as Resultado.Exito).dato
        assertTrue(lista.all { it.estatus == EstadoSolicitud.ABIERTA })

        // Detalle de solicitud con sus postulaciones
        val detalle = repoSolicitudes.obtenerDetalle("sol-1")
        assertTrue(detalle is Resultado.Exito)
        val d = (detalle as Resultado.Exito).dato
        assertEquals("sol-1", d.solicitud.id)
        assertTrue(d.postulaciones.isNotEmpty())

        // Intentar cerrar solicitud no asignada debe fallar
        val resCerrarInvalido = repoSolicitudes.cerrar("sol-1")
        assertTrue(resCerrarInvalido is Resultado.Error)

        // Cerrar solicitud asignada sol-3
        val resCerrarValido = repoSolicitudes.cerrar("sol-3")
        assertTrue(resCerrarValido is Resultado.Exito)
        assertEquals(EstadoSolicitud.CERRADA, (resCerrarValido as Resultado.Exito).dato.estatus)
        assertNotNull((resCerrarValido as Resultado.Exito).dato.cerradaEn)
    }

    @Test
    fun `6 RepositorioPostulaciones ejecuta aceptacion atomica de forma consistente`() = runBlocking {
        // Cambiar sesion a cliente 1 (propietario de sol-1)
        val cli1 = fuente.usuarios.first { it.id == "usr-cli-1" }
        fuente.fijarSesionActiva(Sesion(cli1))

        val resAceptar = repoPostulaciones.aceptar("post-1")
        assertTrue(resAceptar is Resultado.Exito)
        val postAceptada = (resAceptar as Resultado.Exito).dato
        assertEquals(EstadoPostulacion.ACEPTADA, postAceptada.estatus)

        // Comprobar que sol-1 paso a ASIGNADA con el trabajador asignado
        val solActual = fuente.solicitudes.first { it.id == "sol-1" }
        assertEquals(EstadoSolicitud.ASIGNADA, solActual.estatus)
        assertEquals("usr-trab-1", solActual.trabajadorId)
    }

    @Test
    fun `7 RepositorioChat envia mensajes y actualiza conversacion`() = runBlocking {
        val convs = repoChat.obtenerConversaciones()
        assertTrue(convs is Resultado.Exito)
        assertTrue((convs as Resultado.Exito).dato.isNotEmpty())

        val nuevoMensaje = repoChat.enviar("conv-1", "Mensaje de prueba unitaria")
        assertTrue(nuevoMensaje is Resultado.Exito)
        assertEquals("Mensaje de prueba unitaria", (nuevoMensaje as Resultado.Exito).dato.contenido)

        val msgs = repoChat.obtenerMensajes("conv-1")
        assertTrue(msgs is Resultado.Exito)
        assertTrue((msgs as Resultado.Exito).dato.any { it.contenido == "Mensaje de prueba unitaria" })
    }

    @Test
    fun `8 RepositorioResenas solo permite calificar solicitudes cerradas y en rango 1 a 5`() = runBlocking {
        // Cliente 2 califica sol-4 (que esta cerrada)
        val cli2 = fuente.usuarios.first { it.id == "usr-cli-2" }
        fuente.fijarSesionActiva(Sesion(cli2))

        // sol-4 ya tiene res-1 en la semilla, por lo que una segunda debe fallar
        val resSegunda = repoResenas.dejarResena("sol-4", 5, "Otra resena")
        assertTrue(resSegunda is Resultado.Error)

        // Calificacion fuera de rango debe fallar
        val resRango = repoResenas.dejarResena("sol-1", 6, "Invalida")
        assertTrue(resRango is Resultado.Error)
        assertEquals(TipoError.VALIDACION, (resRango as Resultado.Error).tipo)
    }

    @Test
    fun `9 RepositorioCatalogos retorna datos semilla exactos y filtra trabajadores`() = runBlocking {
        val cats = repoCatalogos.obtenerCategorias()
        assertTrue(cats is Resultado.Exito)
        assertEquals(16, (cats as Resultado.Exito).dato.size)

        val estados = repoCatalogos.obtenerEstados()
        assertTrue(estados is Resultado.Exito)
        assertEquals(32, (estados as Resultado.Exito).dato.size)

        val muns = repoCatalogos.obtenerMunicipios(9) // CMX
        assertTrue(muns is Resultado.Exito)
        assertEquals(11, (muns as Resultado.Exito).dato.size)

        // Busqueda por oficio "Plomeria"
        val busqueda = repoCatalogos.buscarTrabajadores(FiltrosBusquedaTrabajadores(texto = "Plomero"))
        assertTrue(busqueda is Resultado.Exito)
        val res = (busqueda as Resultado.Exito).dato
        assertTrue(res.any { it.titulo.contains("Plomero", ignoreCase = true) })
    }

    @Test
    fun `10 RepositorioIa genera respuestas segun funcion y detecta tope diario`() = runBlocking {
        val gen = repoIa.generar(FuncionIa.REDACTAR_PERFIL, "plomeria y gas")
        assertTrue(gen is Resultado.Exito)
        val resIa = (gen as Resultado.Exito).dato
        assertTrue(resIa.resultado.isNotBlank())
        assertTrue(resIa.llamadasRestantesHoy >= 0)

        // Agotar llamadas para verificar LIMITE_IA
        repoIa.llamadasRestantes = 0
        val resAgotado = repoIa.generar(FuncionIa.SUGERIR, "consulta")
        assertTrue(resAgotado is Resultado.Error)
        assertEquals(TipoError.LIMITE_IA, (resAgotado as Resultado.Error).tipo)
    }

    @Test
    fun `11 RepositorioIa no inventa garantias ni repite la entrada cruda (H-31)`() = runBlocking {
        val entrada = "Oficio: Plomero\nHabilidades: Boilers"
        for (funcion in listOf(FuncionIa.REDACTAR_PERFIL, FuncionIa.REDACTAR_SERVICIO)) {
            val texto = ((repoIa.generar(funcion, entrada)) as Resultado.Exito).dato.resultado

            val palabras = texto.trim().split(Regex("\\s+")).size
            assertTrue("$funcion: $palabras palabras", palabras in 40..80)
            assertFalse(texto.contains("Oficio:"))
            assertFalse(texto.contains("Plomero"))
            for (prohibida in listOf("garant", "primera calidad", "especializado", "presupuesto")) {
                assertFalse("$funcion dice \"$prohibida\"", texto.contains(prohibida, ignoreCase = true))
            }
            assertFalse(texto.any(Char::isDigit))
        }
    }
}
