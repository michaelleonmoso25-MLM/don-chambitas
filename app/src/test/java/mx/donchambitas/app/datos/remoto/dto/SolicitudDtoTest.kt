package mx.donchambitas.app.datos.remoto.dto

import java.math.BigDecimal
import java.time.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mx.donchambitas.app.dominio.modelo.EstadoPostulacion
import mx.donchambitas.app.dominio.modelo.EstadoSolicitud
import mx.donchambitas.app.dominio.modelo.RolUsuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SolicitudDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    /** Una fila de public.solicitudes como la devuelve PostgREST. */
    private val filaDeLaTabla = """
        {
          "id": "11111111-1111-4111-8111-111111111111",
          "cliente_id": "aaaaaaaa-0000-4000-8000-000000000001",
          "rol_cliente": "cliente",
          "categoria_id": 1,
          "titulo": "Fuga en el baño",
          "descripcion": "Se necesita plomero urgente.",
          "presupuesto": 450.50,
          "estado_id": 9,
          "municipio_id": 7,
          "estatus": "abierta",
          "trabajador_id": null,
          "categorizada_por_ia": false,
          "creado_en": "2026-11-09T18:30:00.123456+00:00",
          "actualizado_en": "2026-11-09T18:30:00.123456+00:00",
          "cerrada_en": null
        }
    """.trimIndent()

    @Test
    fun debeLeerLasColumnasConSuNombreReal_cuandoLaFilaVieneDeLaTabla() {
        val solicitud = json.decodeFromString<SolicitudDto>(filaDeLaTabla).aSolicitud()

        assertEquals("11111111-1111-4111-8111-111111111111", solicitud.id)
        assertEquals(RolUsuario.CLIENTE, solicitud.rolCliente)
        assertEquals(EstadoSolicitud.ABIERTA, solicitud.estatus)
        assertEquals(9, solicitud.estadoId)
        assertEquals(7, solicitud.municipioId)
        assertNull(solicitud.trabajadorId)
        assertNull(solicitud.cerradaEn)
    }

    @Test
    fun debeConservarElPresupuestoExacto_cuandoLlegaConDecimales() {
        val solicitud = json.decodeFromString<SolicitudDto>(filaDeLaTabla).aSolicitud()

        assertEquals(0, BigDecimal("450.50").compareTo(solicitud.presupuesto))
    }

    @Test
    fun debeConvertirLasFechasAInstant_cuandoLleganConZonaHoraria() {
        val solicitud = json.decodeFromString<SolicitudDto>(filaDeLaTabla).aSolicitud()

        assertEquals(Instant.parse("2026-11-09T18:30:00.123456Z"), solicitud.creadoEn)
    }

    @Test
    fun debeLeerUnPresupuestoNulo_cuandoElClienteNoPusoUno() {
        val sinPresupuesto = filaDeLaTabla.replace("\"presupuesto\": 450.50", "\"presupuesto\": null")

        assertNull(json.decodeFromString<SolicitudDto>(sinPresupuesto).aSolicitud().presupuesto)
    }

    @Test
    fun debeLeerLaFechaDeCierre_cuandoLaSolicitudEstaCerrada() {
        val cerrada = filaDeLaTabla
            .replace("\"estatus\": \"abierta\"", "\"estatus\": \"cerrada\"")
            .replace("\"cerrada_en\": null", "\"cerrada_en\": \"2026-11-10T09:00:00+00:00\"")

        val solicitud = json.decodeFromString<SolicitudDto>(cerrada).aSolicitud()

        assertEquals(EstadoSolicitud.CERRADA, solicitud.estatus)
        assertEquals(Instant.parse("2026-11-10T09:00:00Z"), solicitud.cerradaEn)
    }

    @Test
    fun debeDejarElConteoEnCero_cuandoLaFilaNoViene_deLaVista() {
        val solicitud = json.decodeFromString<SolicitudDto>(filaDeLaTabla).aSolicitud()

        assertEquals(0, solicitud.postulacionesSinRevisar)
    }

    @Test
    fun debeLeerLasPostulacionesSinRevisar_cuandoLaFilaVieneDeLaVista() {
        // vw_mis_solicitudes agrega esta columna (DEC-37, H-42).
        val deLaVista = filaDeLaTabla.replace(
            "\"cerrada_en\": null",
            "\"cerrada_en\": null, \"postulaciones_sin_revisar\": 3"
        )

        assertEquals(3, json.decodeFromString<SolicitudDto>(deLaVista).aSolicitud().postulacionesSinRevisar)
    }

    @Test
    fun debeLeerLasPostulacionesEmbebidas_cuandoSePidioElDetalle() {
        val conPostulaciones = filaDeLaTabla.replace(
            "\"cerrada_en\": null",
            """
            "cerrada_en": null,
            "postulaciones": [
              {
                "id": "22222222-2222-4222-8222-222222222222",
                "solicitud_id": "11111111-1111-4111-8111-111111111111",
                "trabajador_id": "bbbbbbbb-0000-4000-8000-000000000001",
                "mensaje": "Puedo ir hoy",
                "precio_propuesto": 400.00,
                "estatus": "enviada",
                "creado_en": "2026-11-09T19:00:00+00:00",
                "actualizado_en": "2026-11-09T19:00:00+00:00"
              },
              {
                "id": "33333333-3333-4333-8333-333333333333",
                "solicitud_id": "11111111-1111-4111-8111-111111111111",
                "trabajador_id": "bbbbbbbb-0000-4000-8000-000000000002",
                "mensaje": null,
                "precio_propuesto": null,
                "estatus": "rechazada",
                "creado_en": "2026-11-09T19:30:00+00:00",
                "actualizado_en": "2026-11-09T20:00:00+00:00"
              }
            ]
            """.trimIndent()
        )

        val dto = json.decodeFromString<SolicitudDto>(conPostulaciones)
        val postulaciones = dto.postulaciones.map { it.aPostulacion() }

        assertEquals(2, postulaciones.size)
        assertEquals(EstadoPostulacion.ENVIADA, postulaciones[0].estatus)
        assertEquals(0, BigDecimal("400.00").compareTo(postulaciones[0].precioPropuesto))
        assertNull(postulaciones[1].mensaje)
        assertNull(postulaciones[1].precioPropuesto)
        assertEquals(EstadoPostulacion.RECHAZADA, postulaciones[1].estatus)
    }

    @Test
    fun debeDejarLasPostulacionesVacias_cuandoNoSePidioElEmbebido() {
        assertTrue(json.decodeFromString<SolicitudDto>(filaDeLaTabla).postulaciones.isEmpty())
    }

    @Test
    fun debeFallarConUnMensajeClaro_cuandoElEstatusEsDesconocido() {
        val rara = filaDeLaTabla.replace("\"estatus\": \"abierta\"", "\"estatus\": \"suspendida\"")

        val error = assertThrows(IllegalArgumentException::class.java) {
            json.decodeFromString<SolicitudDto>(rara).aSolicitud()
        }

        assertTrue(error.message.orEmpty().contains("suspendida"))
    }

    // ---- SolicitudEscrituraDto --------------------------------------------

    @Test
    fun debeEscribirSoloLasSieteColumnasDeCrear_sinIdNiEstatusNiTrabajador() {
        val dto = SolicitudEscrituraDto(
            clienteId = "aaaaaaaa-0000-4000-8000-000000000001",
            categoriaId = 1,
            titulo = "Fuga",
            descripcion = "Urgente",
            presupuesto = BigDecimal("450.50"),
            estadoId = 9,
            municipioId = 7
        )

        val claves = Json.parseToJsonElement(Json.encodeToString(dto)).let {
            (it as kotlinx.serialization.json.JsonObject).keys
        }

        assertEquals(
            setOf("cliente_id", "categoria_id", "titulo", "descripcion", "presupuesto", "estado_id", "municipio_id"),
            claves
        )
    }

    @Test
    fun debeEscribirNuloEnElPresupuesto_cuandoElClienteNoPusoUno() {
        // Sin valores por omision: kotlinx no escribe una propiedad que vale su valor por omision.
        val dto = SolicitudEscrituraDto(
            clienteId = "aaaaaaaa-0000-4000-8000-000000000001",
            categoriaId = 1,
            titulo = "Fuga",
            descripcion = "Urgente",
            presupuesto = null,
            estadoId = 9,
            municipioId = 7
        )

        val texto = Json.encodeToString(dto)

        assertTrue(texto.contains("\"presupuesto\":null"))
    }
}
