package mx.donchambitas.app.datos.remoto.dto

import java.math.BigDecimal
import java.time.Instant
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ResumenTrabajadorDtoTest {

    /** Una fila de vw_busqueda_trabajadores como la devuelve PostgREST, con ubicacion y precio. */
    private val filaCompleta = """
        {
          "trabajador_id": "0b8f7c1e-2d3a-4b5c-8d9e-0f1a2b3c4d5e",
          "nombre": "Pedro",
          "apellidos": "Ramírez",
          "foto_url": "https://ejemplo.mx/pedro.jpg",
          "titulo": "Plomero",
          "disponible": true,
          "estado_id": 22,
          "municipio_id": 7,
          "estado": "Querétaro",
          "municipio": "Querétaro",
          "promedio": 4.5,
          "total_resenas": 12,
          "servicios_activos": 2,
          "precio_desde": 350.50,
          "categorias": [3, 5],
          "creado_en": "2026-10-01T15:30:00.123456+00:00"
        }
    """.trimIndent()

    /** Un trabajador recien registrado: sin foto, ubicacion ni servicios (H-48). */
    private val filaSinUbicacion = """
        {
          "trabajador_id": "11111111-2222-3333-4444-555555555555",
          "nombre": "Ana",
          "apellidos": "Lopez",
          "foto_url": null,
          "titulo": "Electricista",
          "disponible": false,
          "estado_id": null,
          "municipio_id": null,
          "estado": null,
          "municipio": null,
          "promedio": 0,
          "total_resenas": 0,
          "servicios_activos": 0,
          "precio_desde": null,
          "categorias": [],
          "creado_en": "2026-11-01T09:00:00+00:00"
        }
    """.trimIndent()

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun debeLeerLasColumnasConSuNombreReal_cuandoLaFilaTraeTodo() {
        val dto = json.decodeFromString<ResumenTrabajadorDto>(filaCompleta)

        assertEquals("0b8f7c1e-2d3a-4b5c-8d9e-0f1a2b3c4d5e", dto.trabajadorId)
        assertEquals(22, dto.estadoId)
        assertEquals(7, dto.municipioId)
        assertEquals(4.5, dto.promedio, 0.0)
        assertEquals(listOf(3, 5), dto.categorias)
    }

    @Test
    fun debeConservarElPrecioExacto_cuandoLlegaConDecimales() {
        val resumen = json.decodeFromString<ResumenTrabajadorDto>(filaCompleta).aResumenTrabajador()

        assertEquals(0, BigDecimal("350.50").compareTo(resumen.precioDesde))
    }

    @Test
    fun debeConvertirLaFechaAInstant_cuandoLlegaConZonaHoraria() {
        val resumen = json.decodeFromString<ResumenTrabajadorDto>(filaCompleta).aResumenTrabajador()

        assertEquals(Instant.parse("2026-10-01T15:30:00.123456Z"), resumen.creadoEn)
    }

    @Test
    fun debeLeerLaFila_cuandoElTrabajadorNoTieneUbicacionNiPrecioNiFoto() {
        // H-48: con campos no nulos, una sola fila asi rompia la pagina entera.
        val resumen = json.decodeFromString<ResumenTrabajadorDto>(filaSinUbicacion).aResumenTrabajador()

        assertNull(resumen.estadoId)
        assertNull(resumen.municipioId)
        assertNull(resumen.estado)
        assertNull(resumen.municipio)
        assertNull(resumen.precioDesde)
        assertNull(resumen.fotoUrl)
        assertEquals(emptyList<Int>(), resumen.categorias)
    }

    @Test
    fun debeConservarNoDisponible_cuandoLaFilaLoTrae() {
        // H-43: la vista no filtra disponible, y la tarjeta lo etiqueta.
        val resumen = json.decodeFromString<ResumenTrabajadorDto>(filaSinUbicacion).aResumenTrabajador()

        assertFalse(resumen.disponible)
    }

    @Test
    fun debeLeerUnPromedioEnteroComoDouble_cuandoNoHayResenas() {
        val resumen = json.decodeFromString<ResumenTrabajadorDto>(filaSinUbicacion).aResumenTrabajador()

        assertEquals(0.0, resumen.promedio, 0.0)
    }

    @Test
    fun debeIgnorarTextoBusqueda_cuandoLaVistaLoDevuelve() {
        val conTexto = filaCompleta.replace("\"titulo\"", "\"texto_busqueda\": \"plomero pedro ramirez\",\n  \"titulo\"")

        val dto = json.decodeFromString<ResumenTrabajadorDto>(conTexto)

        assertEquals("Plomero", dto.titulo)
    }
}
