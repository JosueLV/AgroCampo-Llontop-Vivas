package pe.edu.upeu.agrocampo_llontop_vivas

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import pe.edu.upeu.agrocampo_llontop_vivas.data.repository.LaboresRepositorioRemoto
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.ErrorApi
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.TipoLabor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LaboresRemotoTest {

    private fun crearCliente(motor: MockEngine) = HttpClient(motor) {
        expectSuccess = false
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Test
    fun descargaLaboresDelLoteSeleccionado() = runTest {
        val motor = MockEngine { peticion ->
            assertEquals("/api/v1/lotes/3/labores", peticion.url.encodedPath)
            respond(
                content = """{"contenido":[{"id":15,"loteId":3,"tipo":"RIEGO","fecha":"2026-10-27","responsable":"tecnico1","cantidad":2.0,"observacion":"Riego de prueba","pendiente":false}],"pagina":0,"tamanio":20,"totalElementos":1,"totalPaginas":1,"ultima":true}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val cliente = crearCliente(motor)
        try {
            val labores = LaboresRepositorioRemoto(cliente).listarPorLote(3)
            assertEquals(1, labores.size)
            assertEquals(TipoLabor.RIEGO, labores[0].tipo)
            assertEquals("tecnico1", labores[0].responsable)
        } finally {
            cliente.close()
        }
    }

    @Test
    fun loteNoEncontradoDevuelveError404() = runTest {
        val motor = MockEngine {
            respond(
                content = """{"message":"Lote no encontrado"}""",
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val cliente = crearCliente(motor)
        try {
            assertFailsWith<ErrorApi.NoEncontrado> {
                LaboresRepositorioRemoto(cliente).listarPorLote(9999)
            }
        } finally {
            cliente.close()
        }
    }
}
