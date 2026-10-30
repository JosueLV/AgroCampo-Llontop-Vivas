package pe.edu.upeu.agrocampo_llontop_vivas.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.parameter
import kotlinx.coroutines.CancellationException
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.ApiConfig
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.lanzarErrorApi
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.LoteDto
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.PaginaDto
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.aDominio
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.ErrorApi
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Lote

class LotesRepositorioRemoto(private val cliente: HttpClient) {

    /** El backend exige CAMPANIA_CERRAR y al menos una cosecha. */
    suspend fun cerrarCampania(loteId: Long): Lote {
        if (loteId <= 0L) throw ErrorApi.Validacion()
        try {
            val respuesta = cliente.post("${ApiConfig.BASE_URL}/lotes/$loteId/cerrar-campania")
            return when (respuesta.status.value) {
                in 200..299 -> respuesta.body<LoteDto>().aDominio()
                else -> respuesta.lanzarErrorApi()
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

    suspend fun listarTodos(): List<Lote> {
        try {
            val lotes = mutableListOf<Lote>()
            var pagina = 0
            var ultima = false

            // Recuperar las páginas del servidor sin depender de datos de muestra.
            while (!ultima && pagina < 100) {
                val respuesta = cliente.get("${ApiConfig.BASE_URL}/lotes") {
                    parameter("pagina", pagina)
                    parameter("tamanio", 20)
                }

                when (respuesta.status.value) {
                    in 200..299 -> {
                        val datos = respuesta.body<PaginaDto<LoteDto>>()
                        lotes += datos.contenido.map { it.aDominio() }
                        ultima = datos.ultima || pagina + 1 >= datos.totalPaginas
                        pagina++
                    }
                    else -> respuesta.lanzarErrorApi()
                }
            }
            return lotes
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
