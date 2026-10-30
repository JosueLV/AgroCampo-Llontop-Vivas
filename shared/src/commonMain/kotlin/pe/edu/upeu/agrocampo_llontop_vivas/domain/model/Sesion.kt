package pe.edu.upeu.agrocampo_llontop_vivas.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Sesion(
    val token: String,
    val usuario: String,
    val rol: String,
    val privilegios: Set<String>
)
