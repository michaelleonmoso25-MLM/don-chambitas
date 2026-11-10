package mx.donchambitas.app.datos.repositorio

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.time.Instant
import kotlinx.coroutines.runBlocking
import mx.donchambitas.app.BuildConfig
import mx.donchambitas.app.dominio.modelo.PerfilTrabajador
import mx.donchambitas.app.dominio.modelo.Servicio
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * RepositorioTrabajadorReal, RepositorioServiciosReal y RepositorioCatalogosReal
 * contra el proyecto de Supabase, con RLS y la cubeta servicios de verdad
 * (S3-T09). Prueban lo de "Que falta verificar" de CONTRATOS-API.md.
 *
 * Se omiten solas si faltan las llaves en local.properties. Usan la cuenta
 * beto.rls de basedatos/92_usuarios_prueba.sql y la dejan como estaba: su
 * perfil y sus habilidades se restauran, y los servicios de prueba se borran.
 */
@RunWith(AndroidJUnit4::class)
class RepositorioTrabajadorServiciosRealTest {

    private lateinit var cliente: SupabaseClient
    private lateinit var auth: RepositorioAuthReal
    private lateinit var trabajador: RepositorioTrabajadorReal
    private lateinit var servicios: RepositorioServiciosReal
    private lateinit var catalogos: RepositorioCatalogosReal

    private lateinit var uid: String
    private var perfilOriginal: PerfilTrabajador? = null
    private var habilidadesOriginales: List<String> = emptyList()

    @Before
    fun preparar(): Unit = runBlocking {
        assumeTrue(
            "Sin SUPABASE_URL y SUPABASE_ANON_KEY en local.properties",
            BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()
        )
        cliente = createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY) {
            install(Auth) { sessionManager = MemorySessionManager() }
            install(Postgrest)
            install(Storage)
        }
        auth = RepositorioAuthReal { cliente }
        trabajador = RepositorioTrabajadorReal { cliente }
        servicios = RepositorioServiciosReal { cliente }
        catalogos = RepositorioCatalogosReal { cliente }

        val sesion = auth.iniciarSesion(CORREO, CONTRASENA)
        assertTrue("No se pudo entrar con $CORREO: $sesion", sesion is Resultado.Exito)
        uid = (sesion as Resultado.Exito).dato.usuario.id
        perfilOriginal = exito(trabajador.obtenerMiPerfil())
        habilidadesOriginales = exito(trabajador.obtenerMisHabilidades())
        // Los servicios cuelgan del perfil: sin el, crear falla con 409 (H-27).
        exito(trabajador.guardarMiPerfil(perfilDePrueba()))
    }

    @After
    fun cerrar(): Unit = runBlocking {
        if (!::uid.isInitialized) return@runBlocking
        // Primero los servicios de prueba, con sus archivos; luego el perfil como estaba.
        exito(servicios.obtenerMisServicios())
            .filter { it.titulo.startsWith(PREFIJO) }
            .forEach { servicios.eliminar(it.id) }
        val original = perfilOriginal
        if (original == null) {
            cliente.from("perfiles_trabajador").delete { filter { eq("usuario_id", uid) } }
        } else {
            exito(trabajador.guardarMiPerfil(original))
            exito(trabajador.reemplazarHabilidades(habilidadesOriginales))
        }
        auth.cerrarSesion()
    }

    @Test
    fun catalogos_traenLosIdsDeLaBase(): Unit = runBlocking {
        val categorias = exito(catalogos.obtenerCategorias())
        assertEquals(16, categorias.size)
        assertEquals(categorias.sortedBy { it.orden }, categorias)

        val puebla = exito(catalogos.obtenerEstados()).single { it.clave == "PUE" }
        val municipios = exito(catalogos.obtenerMunicipios(puebla.id))
        assertEquals(setOf("Atlixco", "Cholula", "Puebla"), municipios.map { it.nombre }.toSet())
        assertTrue(municipios.all { it.estadoId == puebla.id })
    }

    @Test
    fun perfil_guardaElMunicipioElegidoYBorraConNulo(): Unit = runBlocking {
        val (estadoId, cholula) = estadoYCholula()

        val guardado = exito(
            trabajador.guardarMiPerfil(perfilDePrueba().copy(estadoId = estadoId, municipioId = cholula, descripcion = null))
        )

        assertEquals(uid, guardado.usuarioId)
        val leido = exito(trabajador.obtenerMiPerfil())!!
        assertEquals(cholula, leido.municipioId)
        assertEquals(estadoId, leido.estadoId)
        // Verificacion 2: el null viaja y borra la descripcion.
        assertNull(leido.descripcion)
    }

    @Test
    fun perfil_rechazaExperienciaFueraDeRango(): Unit = runBlocking {
        val resultado = trabajador.guardarMiPerfil(perfilDePrueba().copy(experienciaAnios = 71))
        // Verificacion 3: un CHECK es 400 y va a VALIDACION.
        assertEquals(TipoError.VALIDACION, tipoDe(resultado))
    }

    @Test
    fun habilidades_seReemplazanYSeLeenOrdenadas(): Unit = runBlocking {
        exito(trabajador.reemplazarHabilidades(listOf("Tinacos", "Boilers")))
        assertEquals(listOf("Boilers", "Tinacos"), exito(trabajador.obtenerMisHabilidades()))

        // Una repetida rompe la llave primaria: 409, VALIDACION.
        assertEquals(TipoError.VALIDACION, tipoDe(trabajador.reemplazarHabilidades(listOf("Cobre", "Cobre"))))
    }

    @Test
    fun perfilPublico_traeElPropioConSuUbicacion(): Unit = runBlocking {
        val (estadoId, cholula) = estadoYCholula()
        exito(trabajador.guardarMiPerfil(perfilDePrueba().copy(estadoId = estadoId, municipioId = cholula)))

        val publico = exito(trabajador.obtenerPerfilPublico(uid))

        assertEquals("Cholula", publico.municipio)
        assertEquals(TipoError.VALIDACION, tipoDe(trabajador.obtenerPerfilPublico(UID_INEXISTENTE)))
    }

    @Test
    fun servicio_seCreaActualizaPausaYElimina(): Unit = runBlocking {
        val creado = exito(servicios.crear(servicioDePrueba(precioDesde = BigDecimal("350.50"))))
        assertEquals(BigDecimal("350.50"), creado.precioDesde)
        assertNull(creado.precioHasta)
        assertTrue(creado.activo)
        assertTrue(creado.fotos.isEmpty())

        // Verificacion 1: el update devuelve el servicio con sus fotos embebidas.
        exito(servicios.subirFoto(creado.id, pngDePrueba(), 1))
        val actualizado = exito(servicios.actualizar(creado.copy(titulo = "$PREFIJO cambiado", precioDesde = null)))
        assertEquals("$PREFIJO cambiado", actualizado.titulo)
        assertNull(actualizado.precioDesde)
        assertEquals(1, actualizado.fotos.size)

        val pausado = exito(servicios.pausar(creado.id, false))
        assertEquals(false, pausado.activo)
        assertEquals(1, pausado.fotos.size)

        exito(servicios.eliminar(creado.id))
        assertTrue(exito(servicios.obtenerMisServicios()).none { it.id == creado.id })
        assertTrue(archivosDe(creado.id).isEmpty())
        assertEquals(TipoError.VALIDACION, tipoDe(servicios.actualizar(creado)))
        assertEquals(TipoError.VALIDACION, tipoDe(servicios.eliminar(creado.id)))
    }

    @Test
    fun servicio_rechazaHastaMenorQueDesde(): Unit = runBlocking {
        val resultado = servicios.crear(servicioDePrueba(BigDecimal("800"), BigDecimal("300")))
        assertEquals(TipoError.VALIDACION, tipoDe(resultado))
    }

    @Test
    fun fotos_seSubenSeEmbebenYSeQuitanConSuArchivo(): Unit = runBlocking {
        val servicio = exito(servicios.crear(servicioDePrueba()))

        val primera = exito(servicios.subirFoto(servicio.id, pngDePrueba(Color.RED), 1))
        val segunda = exito(servicios.subirFoto(servicio.id, pngDePrueba(Color.BLUE), 2))
        assertEquals(2, archivosDe(servicio.id).size)

        val leido = exito(servicios.obtenerMisServicios()).single { it.id == servicio.id }
        assertEquals(listOf(primera, segunda), leido.fotos)

        // La posicion ocupada falla en la fila, y el archivo recien subido se borra.
        assertEquals(TipoError.VALIDACION, tipoDe(servicios.subirFoto(servicio.id, pngDePrueba(), 1)))
        assertEquals(2, archivosDe(servicio.id).size)

        exito(servicios.eliminarFoto(primera.id))
        assertEquals(1, archivosDe(servicio.id).size)
        assertEquals(TipoError.VALIDACION, tipoDe(servicios.eliminarFoto(primera.id)))
    }

    @Test
    fun foto_deTipoNoAdmitidoNoSeSube(): Unit = runBlocking {
        val servicio = exito(servicios.crear(servicioDePrueba()))

        val resultado = servicios.subirFoto(servicio.id, byteArrayOf(1, 2, 3, 4), 1)

        assertEquals(TipoError.VALIDACION, tipoDe(resultado))
        assertTrue(archivosDe(servicio.id).isEmpty())
    }

    private suspend fun estadoYCholula(): Pair<Int, Int> {
        val puebla = exito(catalogos.obtenerEstados()).single { it.clave == "PUE" }
        val cholula = exito(catalogos.obtenerMunicipios(puebla.id)).single { it.nombre == "Cholula" }
        return puebla.id to cholula.id
    }

    private suspend fun archivosDe(servicioId: String): List<String> =
        cliente.storage.from("servicios").list("$uid/$servicioId").map { it.name }

    private fun perfilDePrueba() = PerfilTrabajador(
        usuarioId = "ignorado: sale de la sesion",
        titulo = "Plomero de prueba S3-T09",
        descripcion = "Perfil de prueba",
        experienciaAnios = 5,
        telefonoContacto = "2225550101",
        creadoEn = Instant.EPOCH,
        actualizadoEn = Instant.EPOCH
    )

    private fun servicioDePrueba(precioDesde: BigDecimal? = null, precioHasta: BigDecimal? = null) = Servicio(
        id = "",
        perfilId = "",
        categoriaId = 1,
        titulo = "$PREFIJO servicio",
        descripcion = "Servicio de prueba",
        precioDesde = precioDesde,
        precioHasta = precioHasta,
        creadoEn = Instant.EPOCH,
        actualizadoEn = Instant.EPOCH
    )

    /** Un PNG pequeño de un solo color: la cubeta admite JPG, PNG y WebP. */
    private fun pngDePrueba(color: Int = Color.GRAY): ByteArray {
        val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
        return ByteArrayOutputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            it.toByteArray()
        }
    }

    private fun <T> exito(resultado: Resultado<T>): T {
        assertTrue(resultado.toString(), resultado is Resultado.Exito)
        return (resultado as Resultado.Exito).dato
    }

    private fun tipoDe(resultado: Resultado<*>): TipoError {
        assertTrue(resultado.toString(), resultado is Resultado.Error)
        return (resultado as Resultado.Error).tipo
    }

    private companion object {
        const val CORREO = "beto.rls@prueba.donchambitas.mx"

        // La misma de basedatos/92_prueba_rls_anon.py: cuentas de prueba, sin datos reales.
        const val CONTRASENA = "prueba-S1T03-no-usar-en-produccion"

        /** Todo lo que crean estas pruebas empieza asi, para borrarlo al final. */
        const val PREFIJO = "Prueba S3-T09"
        const val UID_INEXISTENTE = "00000000-0000-4000-8000-000000000000"
    }
}
