package pe.edu.upeu.agrocampo_llontop_vivas.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.CancellationException
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.ApiConfig
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.LoteDto
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.PaginaDto
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.aDominio
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.ErrorApi
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Lote

class LotesRepositorioRemoto(private val cliente: HttpClient) {

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
                    400 -> throw ErrorApi.Validacion()
                    401 -> throw ErrorApi.NoAutorizado()
                    403 -> throw ErrorApi.SinPrivilegio()
                    404 -> throw ErrorApi.NoEncontrado()
                    409 -> throw ErrorApi.Conflicto()
                    else -> throw ErrorApi.Servidor()
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
