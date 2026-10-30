package pe.edu.upeu.agrocampo_llontop_vivas

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.CrearLaborDto
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.EditarLaborDto
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.LaborDto
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.aDominio
import pe.edu.upeu.agrocampo_llontop_vivas.data.repository.LaboresRepositorioRemoto
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Labor
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.TipoLabor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Pruebas SC-C: el número de jornales viaja y vuelve por el contrato JSON. */
class CantidadJornalesTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val respuestaConJornales = """
        {
          "id": 51,
          "loteId": 3,
          "tipo": "RIEGO",
          "fecha": "2026-10-29",
          "responsable": "tecnico1",
          "cantidad": 2.0,
          "observacion": "Riego de prueba",
          "pendiente": false,
          "cantidadJornales": 3
        }
    """.trimIndent()

    @Test
    fun postIncluyeCantidadJornalesEnJson() {
        val peticion = CrearLaborDto(
            loteId = 3L,
            tipo = "RIEGO",
            fecha = "2026-10-29",
            cantidad = 2.0,
            observacion = "Riego de prueba",
            cantidadJornales = 3
        )
        val cuerpo = json.encodeToString(peticion)
        assertTrue(cuerpo.contains("\"cantidadJornales\":3"))
    }

    @Test
    fun putIncluyeCantidadJornalesEnJson() {
        val peticion = EditarLaborDto(
            tipo = "RIEGO",
            fecha = "2026-10-29",
            cantidad = 2.0,
            observacion = "Riego de prueba",
            cantidadJornales = 4
        )
        val cuerpo = json.encodeToString(peticion)
        assertTrue(cuerpo.contains("\"cantidadJornales\":4"))
    }

    @Test
    fun respuestaJsonConservaCantidadJornalesEnDominio() {
        val labor = json.decodeFromString<LaborDto>(respuestaConJornales).aDominio()
        assertEquals(3, labor.cantidadJornales)
    }

    @Test
    fun crearLaborRemotaConservaJornalesDevueltos() = runTest {
        val motor = MockEngine { peticion ->
            assertEquals(HttpMethod.Post, peticion.method)
            assertEquals("/api/v1/labores", peticion.url.encodedPath)
            respond(
                content = respuestaConJornales,
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val cliente = HttpClient(motor) {
            expectSuccess = false
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        try {
            val laborPendiente = Labor(
                id = -1L,
                loteId = 3L,
                tipo = TipoLabor.RIEGO,
                fecha = "2026-10-29",
                responsable = "tecnico1",
                cantidad = 2.0,
                observacion = "Riego de prueba",
                pendiente = true,
                cantidadJornales = 3
            )
            val creada = LaboresRepositorioRemoto(cliente).crear(laborPendiente)
            assertEquals(51L, creada.id)
            assertEquals(3, creada.cantidadJornales)
            assertEquals(false, creada.pendiente)
        } finally {
            cliente.close()
        }
    }
}
