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
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.ErrorApi
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Labor
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
    private fun laborPendiente() = Labor(
        id = -1L,
        loteId = 3L,
        tipo = TipoLabor.RIEGO,
        fecha = "2026-10-29",
        responsable = "tecnico1",
        cantidad = 2.0,
        observacion = "Riego pendiente",
        pendiente = true
    )

    @Test
    fun enviaLaborPendienteConPostYRecibeIdDefinitivo() = runTest {
        val motor = MockEngine { peticion ->
            assertEquals(HttpMethod.Post, peticion.method)
            assertEquals("/api/v1/labores", peticion.url.encodedPath)
            respond(
                content = """{"id":51,"loteId":3,"tipo":"RIEGO","fecha":"2026-10-29","responsable":"tecnico1","cantidad":2.0,"observacion":"Riego pendiente","pendiente":false}""",
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val cliente = crearCliente(motor)
        try {
            val creada = LaboresRepositorioRemoto(cliente).crear(laborPendiente())
            assertEquals(51L, creada.id)
            assertEquals(false, creada.pendiente)
        } finally {
            cliente.close()
        }
    }

    @Test
    fun registrarLaborSinPermisosConservaError403() = runTest {
        val motor = MockEngine {
            respond(
                content = """{"message":"No autorizado"}""",
                status = HttpStatusCode.Forbidden,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val cliente = crearCliente(motor)
        try {
            assertFailsWith<ErrorApi.SinPrivilegio> {
                LaboresRepositorioRemoto(cliente).crear(laborPendiente())
            }
        } finally {
            cliente.close()
        }
    }


    @Test
    fun supervisorCorrigeLaborAjenaPorPut() = runTest {
        val motor = MockEngine { peticion ->
            assertEquals(HttpMethod.Put, peticion.method)
            assertEquals("/api/v1/labores/8", peticion.url.encodedPath)
            respond(
                content = """{"id":8,"loteId":3,"tipo":"RIEGO","fecha":"2026-10-29","responsable":"Jefe de campo 1","cantidad":2.0,"observacion":"Control de calidad","pendiente":false}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val cliente = crearCliente(motor)
        try {
            val original = laborPendiente().copy(id = 8L, pendiente = false)
            val respuesta = LaboresRepositorioRemoto(cliente).editar(original)
            assertEquals(8L, respuesta.id)
            assertEquals("Control de calidad", respuesta.observacion)
        } finally {
            cliente.close()
        }
    }

    @Test
    fun servidorNiegaEdicionSinPrivilegioCon403() = runTest {
        val motor = MockEngine {
            respond(
                content = """{"message":"No tienes el privilegio requerido"}""",
                status = HttpStatusCode.Forbidden,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val cliente = crearCliente(motor)
        try {
            assertFailsWith<ErrorApi.SinPrivilegio> {
                LaboresRepositorioRemoto(cliente).editar(laborPendiente().copy(id = 8L))
            }
        } finally {
            cliente.close()
        }
    }

    @Test
    fun backendRechazaEdicionDeCampaniaCerradaCon409() = runTest {
        val motor = MockEngine {
            respond(
                content = """{"message":"Campaña cerrada"}""",
                status = HttpStatusCode.Conflict,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val cliente = crearCliente(motor)
        try {
            assertFailsWith<ErrorApi.Conflicto> {
                LaboresRepositorioRemoto(cliente).editar(laborPendiente().copy(id = 8L))
            }
        } finally {
            cliente.close()
        }
    }
}
