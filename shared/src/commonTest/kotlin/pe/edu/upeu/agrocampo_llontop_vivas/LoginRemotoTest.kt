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

import pe.edu.upeu.agrocampo_llontop_vivas.data.repository.SesionRepositorioRemoto
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.ErrorApi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LoginRemotoTest {

    @Test
    fun loginCorrectoDevuelveSesion() = runTest {

        val motor = MockEngine {
            respond(
                content = """
                    {
                        "token": "jwt-de-prueba",
                        "usuario": "tecnico1",
                        "rol": "TECNICO",
                        "privilegios": [
                            "LOTE_LEER",
                            "LABOR_CREAR"
                        ],
                        "nombre": "Tecnico de campo 1"
                    }
                """.trimIndent(),

                status = HttpStatusCode.OK,

                headers = headersOf(
                    HttpHeaders.ContentType,
                    "application/json"
                )
            )
        }

        val cliente = HttpClient(motor) {

            expectSuccess = false

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }

        try {

            val repositorio = SesionRepositorioRemoto(cliente)

            val sesion = repositorio.iniciarSesion(
                "tecnico1",
                "Agro2026*"
            )

            assertEquals(
                "tecnico1",
                sesion.usuario
            )

            assertEquals(
                "TECNICO",
                sesion.rol
            )

            assertEquals(
                "jwt-de-prueba",
                sesion.token
            )

        } finally {
            cliente.close()
        }
    }

    @Test
    fun loginIncorrectoTraduceError401() = runTest {

        val motor = MockEngine {
            respond(
                content = """
                    {
                        "message": "Credenciales incorrectas"
                    }
                """.trimIndent(),

                status = HttpStatusCode.Unauthorized,

                headers = headersOf(
                    HttpHeaders.ContentType,
                    "application/json"
                )
            )
        }

        val cliente = HttpClient(motor) {

            expectSuccess = false

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }

        try {

            val repositorio = SesionRepositorioRemoto(cliente)

            assertFailsWith<ErrorApi.NoAutorizado> {

                repositorio.iniciarSesion(
                    "tecnico1",
                    "incorrecta"
                )
            }

        } finally {
            cliente.close()
        }
    }
}