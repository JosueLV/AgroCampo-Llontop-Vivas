package pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PaginaDto<T>(
    val contenido: List<T>,
    val pagina: Int,
    val tamanio: Int,
    val totalElementos: Long,
    val totalPaginas: Int,
    val ultima: Boolean
)
