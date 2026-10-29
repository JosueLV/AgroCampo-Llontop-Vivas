package pe.edu.upeu.agrocampo_llontop_vivas.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.client.request.header
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.SesionActual
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApiConfig {
    const val BASE_URL = "http://127.0.0.1:8080/api/v1"
}

fun crearClienteHttp(sesionActual: SesionActual): HttpClient {
    return HttpClient {
        expectSuccess = false

        defaultRequest {
            sesionActual.token?.let { token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 10000
            connectTimeoutMillis = 5000
            socketTimeoutMillis = 10000
        }
    }
}