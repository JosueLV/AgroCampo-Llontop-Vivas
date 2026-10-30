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
import pe.edu.upeu.agrocampo_llontop_vivas.data.repository.LotesRepositorioRemoto
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.ErrorApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LotesRemotoTest {

    private fun clienteConMotor(motor: MockEngine) = HttpClient(motor) {
        expectSuccess = false
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Test
    fun obtieneTodasLasPaginasReales() = runTest {
        val motor = MockEngine { peticion ->
            val pagina = peticion.url.parameters["pagina"]
            val contenido = if (pagina == "0") {
                """{"contenido":[{"id":1,"codigo":"AG-001","nombre":"Lote Norte","hectareas":3.0,"cultivo":"Palta","valle":"Cañete","campaniaCerrada":false,"dosisMaximaPorHectarea":1.5}],"pagina":0,"tamanio":20,"totalElementos":2,"totalPaginas":2,"ultima":false}"""
            } else {
                """{"contenido":[{"id":2,"codigo":"AG-002","nombre":"Lote Sur","hectareas":4.0,"cultivo":"Maíz","valle":"Majes","campaniaCerrada":true,"dosisMaximaPorHectarea":2.5}],"pagina":1,"tamanio":20,"totalElementos":2,"totalPaginas":2,"ultima":true}"""
            }
            respond(
                content = contenido,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val cliente = clienteConMotor(motor)
        try {
            val lotes = LotesRepositorioRemoto(cliente).listarTodos()
            assertEquals(2, lotes.size)
            assertEquals("AG-001", lotes[0].codigo)
            assertEquals("AG-002", lotes[1].codigo)
            assertEquals(true, lotes[1].campaniaCerrada)
        } finally {
            cliente.close()
        }
    }

    @Test
    fun error403SeTraduceASinPrivilegio() = runTest {
        val motor = MockEngine {
            respond(
                content = """{"mensaje":"Sin permisos"}""",
                status = HttpStatusCode.Forbidden,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val cliente = clienteConMotor(motor)
        try {
            assertFailsWith<ErrorApi.SinPrivilegio> {
                LotesRepositorioRemoto(cliente).listarTodos()
            }
        } finally {
            cliente.close()
        }
    }
}
