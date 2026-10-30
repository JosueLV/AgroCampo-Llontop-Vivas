package pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto

import kotlinx.serialization.Serializable
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Lote

@Serializable
data class LoteDto(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val hectareas: Double,
    val cultivo: String,
    val valle: String,
    val campaniaCerrada: Boolean,
    val dosisMaximaPorHectarea: Double
)

fun LoteDto.aDominio(): Lote = Lote(
    id = id,
    codigo = codigo,
    nombre = nombre,
    hectareas = hectareas,
    cultivo = cultivo,
    valle = valle,
    campaniaCerrada = campaniaCerrada,
    dosisMaximaPorHectarea = dosisMaximaPorHectarea
)
