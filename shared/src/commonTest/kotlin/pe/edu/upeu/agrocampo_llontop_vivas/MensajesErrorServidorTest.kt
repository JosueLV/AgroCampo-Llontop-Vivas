package pe.edu.upeu.agrocampo_llontop_vivas

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.agrocampo_llontop_vivas.data.repository.LaboresRepositorioRemoto
import pe.edu.upeu.agrocampo_llontop_vivas.data.repository.LotesRepositorioRemoto
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.ErrorApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MensajesErrorServidorTest {
    @Test
    fun mensaje400ConservaCampoCantidad() = runTest {
        val motor = MockEngine {
            respond(
                content = """{"message":"Dosis de fumigación excedida",
                    "validationErrors":{"cantidad":"Máximo permitido: 2.0"}}""",
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val http = HttpClient(motor) { expectSuccess = false }
        try {
            val labor = pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Labor(
                id = 31, loteId = 4,
                tipo = pe.edu.upeu.agrocampo_llontop_vivas.domain.model.TipoLabor.FUMIGACION,
                fecha = "2026-10-27", responsable = "tecnico1",
                cantidad = 100.0, cantidadJornales = 2, observacion = "", pendiente = false
            )
            val error = assertFailsWith<ErrorApi.Validacion> {
                LaboresRepositorioRemoto(http).editar(labor)
            }
            assertEquals("Máximo permitido: 2.0", error.erroresPorCampo["cantidad"])
            assertEquals("Dosis de fumigación excedida", error.message)
        } finally { http.close() }
    }

    @Test
    fun mensaje409MuestraMotivoRealDelServicio() = runTest {
        val motor = MockEngine {
            respond(
                content = """{"message":"Se necesita una cosecha para cerrar"}""",
                status = HttpStatusCode.Conflict,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val http = HttpClient(motor) { expectSuccess = false }
        try {
            val error = assertFailsWith<ErrorApi.Conflicto> {
                LotesRepositorioRemoto(http).cerrarCampania(4)
            }
            assertEquals("Se necesita una cosecha para cerrar", error.message)
        } finally { http.close() }
    }
}
