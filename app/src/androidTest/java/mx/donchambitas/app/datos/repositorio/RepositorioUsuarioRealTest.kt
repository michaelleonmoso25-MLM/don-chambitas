package mx.donchambitas.app.datos.repositorio

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.runBlocking
import mx.donchambitas.app.BuildConfig
import mx.donchambitas.app.dominio.modelo.Usuario
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
 * RepositorioUsuarioReal contra el proyecto de Supabase, con RLS y los
 * permisos por columna de verdad. Es la unica prueba de la suite que necesita
 * red: ninguna pantalla llama todavia a RepositorioUsuario (P-18 es S2-T11),
 * asi que no hay otra forma de ejecutarlo en el dispositivo.
 *
 * Se omite sola si faltan las llaves en local.properties. Usa la cuenta
 * ana.rls de basedatos/92_usuarios_prueba.sql y deja su ficha como estaba.
 */
@RunWith(AndroidJUnit4::class)
class RepositorioUsuarioRealTest {

    private lateinit var cliente: SupabaseClient
    private lateinit var auth: RepositorioAuthReal
    private lateinit var repositorio: RepositorioUsuarioReal

    @Before
    fun preparar() {
        assumeTrue(
            "Sin SUPABASE_URL y SUPABASE_ANON_KEY en local.properties",
            BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()
        )
        // La sesion en memoria: la prueba no toca la sesion guardada de la aplicacion.
        cliente = createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY) {
            install(Auth) { sessionManager = MemorySessionManager() }
            install(Postgrest)
        }
        auth = RepositorioAuthReal { cliente }
        repositorio = RepositorioUsuarioReal { cliente }
    }

    @After
    fun cerrar() {
        if (::cliente.isInitialized) runBlocking { auth.cerrarSesion() }
    }

    private suspend fun entrar(): Usuario {
        val sesion = auth.iniciarSesion(CORREO, CONTRASENA)
        assertTrue("No se pudo entrar con $CORREO: $sesion", sesion is Resultado.Exito)
        return (sesion as Resultado.Exito).dato.usuario
    }

    private fun exito(resultado: Resultado<Usuario>): Usuario {
        assertTrue(resultado.toString(), resultado is Resultado.Exito)
        return (resultado as Resultado.Exito).dato
    }

    private fun tipoDe(resultado: Resultado<Usuario>): TipoError {
        assertTrue(resultado.toString(), resultado is Resultado.Error)
        return (resultado as Resultado.Error).tipo
    }

    @Test
    fun debeDevolverLaFichaPropia_cuandoHaySesion() = runBlocking {
        val deLaSesion = entrar()

        val ficha = exito(repositorio.obtenerMiUsuario())

        assertEquals(deLaSesion.id, ficha.id)
        assertEquals(CORREO, ficha.correo)
        assertEquals(deLaSesion.rol, ficha.rol)
    }

    @Test
    fun debeGuardarYDevolverLosDatosNuevos_cuandoSeActualizaLaFicha() = runBlocking {
        val original = entrar()
        try {
            val devuelto = exito(repositorio.actualizarMiUsuario("Ana Prueba", "Cliente S2T12", "5512345678"))

            assertEquals("Ana Prueba", devuelto.nombre)
            assertEquals("Cliente S2T12", devuelto.apellidos)
            assertEquals("5512345678", devuelto.telefono)
            assertEquals(original.correo, devuelto.correo)
            assertEquals(original.rol, devuelto.rol)
            assertTrue(devuelto.actualizadoEn.isAfter(original.actualizadoEn))
            assertEquals(devuelto, exito(repositorio.obtenerMiUsuario()))
        } finally {
            exito(repositorio.actualizarMiUsuario(original.nombre, original.apellidos, original.telefono))
        }
    }

    @Test
    fun debeBorrarElTelefono_cuandoSeActualizaConNulo() = runBlocking {
        val original = entrar()
        try {
            val devuelto = exito(repositorio.actualizarMiUsuario(original.nombre, original.apellidos, null))

            assertNull(devuelto.telefono)
            assertNull(exito(repositorio.obtenerMiUsuario()).telefono)
        } finally {
            exito(repositorio.actualizarMiUsuario(original.nombre, original.apellidos, original.telefono))
        }
    }

    @Test
    fun debeSerValidacion_cuandoElNombreNoCabeEnLaColumna() = runBlocking {
        val original = entrar()

        val resultado = repositorio.actualizarMiUsuario("a".repeat(81), original.apellidos, original.telefono)

        assertEquals(TipoError.VALIDACION, tipoDe(resultado))
        assertEquals(original.nombre, exito(repositorio.obtenerMiUsuario()).nombre)
    }

    @Test
    fun debeSerAutenticacion_cuandoNoHaySesion() = runBlocking {
        assertEquals(TipoError.AUTENTICACION, tipoDe(repositorio.obtenerMiUsuario()))
        assertEquals(TipoError.AUTENTICACION, tipoDe(repositorio.actualizarMiUsuario("X", "Y", null)))
    }

    private companion object {
        const val CORREO = "ana.rls@prueba.donchambitas.mx"

        // La misma de basedatos/92_prueba_rls_anon.py: cuentas de prueba, sin datos reales.
        const val CONTRASENA = "prueba-S1T03-no-usar-en-produccion"
    }
}
