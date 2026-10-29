package pe.edu.upeu.agrocampo_llontop_vivas.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.request.parameter
import kotlinx.coroutines.CancellationException
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.ApiConfig
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.CrearLaborDto
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.EditarLaborDto
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.LaborDto
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.PaginaDto
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.aDominio
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.ErrorApi
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Labor

class LaboresRepositorioRemoto(private val cliente: HttpClient) {
    /**
     * Envía una corrección al backend. Las reglas de autoría y plazo de 24 horas
     * se validan en AgroServicio, nunca con el nombre del rol en esta clase.
     */
    suspend fun editar(labor: Labor): Labor {
        if (labor.id <= 0L) throw ErrorApi.Validacion()
        try {
            val respuesta = cliente.put("${ApiConfig.BASE_URL}/labores/${labor.id}") {
                contentType(ContentType.Application.Json)
                setBody(
                    EditarLaborDto(
                        tipo = labor.tipo.name,
                        fecha = labor.fecha,
                        cantidad = labor.cantidad,
                        observacion = labor.observacion,
                        cantidadJornales = labor.cantidadJornales,
                        cantidadJornales = labor.cantidadJornales
                    )
                )
            }
            return when (respuesta.status.value) {
                in 200..299 -> respuesta.body<LaborDto>().aDominio()
                400 -> throw ErrorApi.Validacion()
                401 -> throw ErrorApi.NoAutorizado()
                403 -> throw ErrorApi.SinPrivilegio()
                404 -> throw ErrorApi.NoEncontrado()
                409 -> throw ErrorApi.Conflicto()
                else -> throw ErrorApi.Servidor()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: ErrorApi) {
            throw e
        } catch (e: HttpRequestTimeoutException) {
            throw ErrorApi.TiempoAgotado()
        } catch (e: Exception) {
            throw ErrorApi.SinConexion()
        }
    }

    /**
     * Registra una labor almacenada sin conexión. El JWT se agrega en ClienteHttp.
     * Solo la respuesta 2xx permite retirar la labor de la cola local.
     */
    suspend fun crear(labor: Labor): Labor {
        try {
            val respuesta = cliente.post("${ApiConfig.BASE_URL}/labores") {
                contentType(ContentType.Application.Json)
                setBody(
                    CrearLaborDto(
                        loteId = labor.loteId,
                        tipo = labor.tipo.name,
                        fecha = labor.fecha,
                        cantidad = labor.cantidad,
                        observacion = labor.observacion
                    )
                )
            }
            return when (respuesta.status.value) {
                in 200..299 -> respuesta.body<LaborDto>().aDominio()
                400 -> throw ErrorApi.Validacion()
                401 -> throw ErrorApi.NoAutorizado()
                403 -> throw ErrorApi.SinPrivilegio()
                404 -> throw ErrorApi.NoEncontrado()
                409 -> throw ErrorApi.Conflicto()
                else -> throw ErrorApi.Servidor()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: ErrorApi) {
            throw e
        } catch (e: HttpRequestTimeoutException) {
            throw ErrorApi.TiempoAgotado()
        } catch (e: Exception) {
            throw ErrorApi.SinConexion()
        }
    }

    suspend fun listarPorLote(loteId: Long): List<Labor> {
        try {
            val resultado = mutableListOf<Labor>()
            var pagina = 0
            var ultima = false
            while (!ultima && pagina < 100) {
                val respuesta = cliente.get("${ApiConfig.BASE_URL}/lotes/$loteId/labores") {
                    parameter("pagina", pagina)
                    parameter("tamanio", 20)
                }
                when (respuesta.status.value) {
                    in 200..299 -> {
                        val datos = respuesta.body<PaginaDto<LaborDto>>()
                        resultado += datos.contenido.map { it.aDominio() }
                        ultima = datos.ultima || pagina + 1 >= datos.totalPaginas
                        pagina++
                    }
                    400 -> throw ErrorApi.Validacion()
                    401 -> throw ErrorApi.NoAutorizado()
                    403 -> throw ErrorApi.SinPrivilegio()
                    404 -> throw ErrorApi.NoEncontrado()
                    409 -> throw ErrorApi.Conflicto()
                    else -> throw ErrorApi.Servidor()
                }
            }
            return resultado
        } catch (e: CancellationException) {
            throw e
        } catch (e: ErrorApi) {
            throw e
        } catch (e: HttpRequestTimeoutException) {
            throw ErrorApi.TiempoAgotado()
        } catch (e: Exception) {
            throw ErrorApi.SinConexion()
        }
    }
}
