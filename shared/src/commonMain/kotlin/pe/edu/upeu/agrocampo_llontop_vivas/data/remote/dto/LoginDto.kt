package pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto

import kotlinx.serialization.Serializable
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Sesion

@Serializable
data class LoginPeticionDto(
    val usuario: String,
    val contrasena: String
)

@Serializable
data class LoginRespuestaDto(
    val token: String,
    val usuario: String,
    val rol: String,
    val privilegios: Set<String>
)

fun LoginRespuestaDto.aDominio(): Sesion {
    return Sesion(
        token = token,
        usuario = usuario,
        rol = rol,
        privilegios = privilegios
    )
}