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
import kotlinx.serialization.json.Json
import pe.edu.upeu.agrocampo_llontop_vivas.data.repository.LaboresRepositorioRemoto
import pe.edu.upeu.agrocampo_llontop_vivas.data.repository.LotesRepositorioRemoto
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.ErrorApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OperacionesCrudTest {

    private fun cliente(motor: MockEngine) = HttpClient(motor) {
        expectSuccess = false
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    @Test
    fun eliminarLabor204SinIntentarLeerUnCuerpo() = runTest {
        val motor = MockEngine { peticion ->
            assertEquals(HttpMethod.Delete, peticion.method)
            assertEquals("/api/v1/labores/31", peticion.url.encodedPath)
            respond(content = "", status = HttpStatusCode.NoContent)
        }
        val http = cliente(motor)
        try {
            LaboresRepositorioRemoto(http).eliminar(31)
        } finally {
            http.close()
        }
    }

    @Test
    fun eliminarSinPermisoConservaError403() = runTest {
        val motor = MockEngine {
            respond(
                content = """{"message":"Sin privilegio LABOR_ELIMINAR"}""",
                status = HttpStatusCode.Forbidden,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val http = cliente(motor)
        try {
            assertFailsWith<ErrorApi.SinPrivilegio> {
                LaboresRepositorioRemoto(http).eliminar(31)
            }
        } finally {
            http.close()
        }
    }

    @Test
    fun cerrarCampaniaActualizaLoteDevueltoPorServicio() = runTest {
        val motor = MockEngine { peticion ->
            assertEquals(HttpMethod.Post, peticion.method)
            assertEquals("/api/v1/lotes/3/cerrar-campania", peticion.url.encodedPath)
            respond(
                content = """
                    {
                        "id":3, "codigo":"AG-003", "nombre":"El Progreso",
                        "hectareas":6.0, "cultivo":"Quinua", "valle":"Mantaro",
                        "campaniaCerrada":true, "dosisMaximaPorHectarea":2.5
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val http = cliente(motor)
        try {
            val lote = LotesRepositorioRemoto(http).cerrarCampania(3)
            assertTrue(lote.campaniaCerrada)
            assertEquals("AG-003", lote.codigo)
        } finally {
            http.close()
        }
    }

    @Test
    fun cerrarCampaniaSinCosechaDevuelve409() = runTest {
        val motor = MockEngine {
            respond(
                content = """{"message":"Se necesita una cosecha para cerrar"}""",
                status = HttpStatusCode.Conflict,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val http = cliente(motor)
        try {
            assertFailsWith<ErrorApi.Conflicto> {
                LotesRepositorioRemoto(http).cerrarCampania(3)
            }
        } finally {
            http.close()
        }
    }
}
