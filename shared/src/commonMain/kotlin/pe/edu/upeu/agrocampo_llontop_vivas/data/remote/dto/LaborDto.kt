package pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto

import kotlinx.serialization.Serializable
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Labor
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.TipoLabor

@Serializable
data class LaborDto(
    val id: Long,
    val loteId: Long,
    val tipo: String,
    val fecha: String,
    val responsable: String,
    val cantidad: Double,
    val observacion: String,
    val pendiente: Boolean = false
)

@Serializable
data class CrearLaborDto(
    val loteId: Long,
    val tipo: String,
    val fecha: String,
    val cantidad: Double,
    val observacion: String
)

@Serializable
data class EditarLaborDto(
    val tipo: String,
    val fecha: String,
    val cantidad: Double,
    val observacion: String
)

fun LaborDto.aDominio(): Labor = Labor(
    id = id,
    loteId = loteId,
    tipo = TipoLabor.valueOf(tipo),
    fecha = fecha,
    responsable = responsable,
    cantidad = cantidad,
    observacion = observacion,
    pendiente = pendiente
)
