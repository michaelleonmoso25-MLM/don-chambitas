package mx.donchambitas.app.datos.falso

import kotlinx.coroutines.runBlocking
import mx.donchambitas.app.dominio.modelo.EstadoPostulacion
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.util.DatosPrueba
import mx.donchambitas.app.util.Resultado
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * RepositorioSolicitudesFalso cuenta las postulaciones sin revisar como lo hace
 * vw_mis_solicitudes (S4-T16, DEC-37, H-42): solo las que siguen en `enviada`.
 */
class SolicitudesSinRevisarFalsoTest {

    private lateinit var fuente: FuenteDatosFalsa
    private lateinit var repo: RepositorioSolicitudesFalso

    @Before
    fun preparar() {
        fuente = FuenteDatosFalsa()
        fuente.solicitudes.clear()
        fuente.postulaciones.clear()
        repo = RepositorioSolicitudesFalso(fuente).apply { retrasoMs = 0L }
        val cliente = DatosPrueba.crearUsuario(id = "cli-1")
        fuente.fijarSesionActiva(Sesion(cliente))
        fuente.solicitudes.add(DatosPrueba.crearSolicitud(id = "sol-a", clienteId = "cli-1"))
        fuente.solicitudes.add(DatosPrueba.crearSolicitud(id = "sol-b", clienteId = "cli-1"))
    }

    private fun postular(id: String, solicitudId: String, estatus: EstadoPostulacion) {
        fuente.postulaciones.add(
            DatosPrueba.crearPostulacion(id = id, solicitudId = solicitudId, estatus = estatus)
        )
    }

    private fun sinRevisar(solicitudId: String): Int = runBlocking {
        (repo.obtenerMisSolicitudes() as Resultado.Exito).dato.first { it.id == solicitudId }.postulacionesSinRevisar
    }

    @Test
    fun debeContarSoloLasEnviadas_cuandoHayPostulacionesEnVariosEstados() {
        postular("p1", "sol-a", EstadoPostulacion.ENVIADA)
        postular("p2", "sol-a", EstadoPostulacion.ENVIADA)
        postular("p3", "sol-a", EstadoPostulacion.RECHAZADA)
        postular("p4", "sol-a", EstadoPostulacion.RETIRADA)
        postular("p5", "sol-a", EstadoPostulacion.ACEPTADA)

        assertEquals(2, sinRevisar("sol-a"))
    }

    @Test
    fun debeDevolverCero_cuandoLaSolicitudNoTienePostulaciones() {
        assertEquals(0, sinRevisar("sol-b"))
    }

    @Test
    fun debeSepararElConteoPorSolicitud_cuandoHayPostulacionesEnVarias() {
        postular("p1", "sol-a", EstadoPostulacion.ENVIADA)
        postular("p2", "sol-b", EstadoPostulacion.ENVIADA)
        postular("p3", "sol-b", EstadoPostulacion.ENVIADA)

        assertEquals(1, sinRevisar("sol-a"))
        assertEquals(2, sinRevisar("sol-b"))
    }
}
