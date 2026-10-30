package pe.edu.upeu.agrocampo_llontop_vivas.data.remote

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.ErrorApi

@Serializable
private data class ErrorRespuestaDto(
    val message: String? = null,
    val validationErrors: Map<String, String> = emptyMap()
)

/** Traduce los estados HTTP sin perder la razón o el campo devuelto por AgroAPI. */
suspend fun HttpResponse.lanzarErrorApi(): Nothing {
    val detalle = try {
        Json { ignoreUnknownKeys = true }
            .decodeFromString<ErrorRespuestaDto>(bodyAsText())
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }
    when (status.value) {
        400 -> throw ErrorApi.Validacion(detalle?.validationErrors.orEmpty(), detalle?.message)
        401 -> throw ErrorApi.NoAutorizado()
        403 -> throw ErrorApi.SinPrivilegio(detalle?.message)
        404 -> throw ErrorApi.NoEncontrado(detalle?.message)
        409 -> throw ErrorApi.Conflicto(detalle?.message)
        else -> throw ErrorApi.Servidor()
    }
}
