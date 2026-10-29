package pe.edu.upeu.agrocampo_llontop_vivas.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.ApiConfig
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.LoginPeticionDto
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.LoginRespuestaDto
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.aDominio
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.ErrorApi
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Sesion
import pe.edu.upeu.agrocampo_llontop_vivas.domain.repository.SesionRepositorio

class SesionRepositorioRemoto(
    private val cliente: HttpClient
) : SesionRepositorio {

    override suspend fun iniciarSesion(
        usuario: String,
        contrasena: String
    ): Sesion {
        try {
            val respuesta = cliente.post(
                "${ApiConfig.BASE_URL}/auth/login"
            ) {
                contentType(ContentType.Application.Json)
                setBody(LoginPeticionDto(usuario, contrasena))
            }

            return when (respuesta.status.value) {
                in 200..299 ->
                    respuesta.body<LoginRespuestaDto>().aDominio()

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
}