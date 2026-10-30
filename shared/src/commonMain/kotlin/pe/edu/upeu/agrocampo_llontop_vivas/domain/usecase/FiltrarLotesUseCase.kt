package pe.edu.upeu.agrocampo_llontop_vivas.domain.usecase

import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Lote

/**
 * Filtros aplicados a los lotes que vienen de SQLDelight.
 * Funciona sin red y nunca se ejecuta dentro de un Composable.
 */
data class FiltrosLotes(
    val valle: String = "",
    val soloAbiertas: Boolean = false,
    val busqueda: String = ""
)

class FiltrarLotesUseCase {
    operator fun invoke(lotesLocales: List<Lote>, filtros: FiltrosLotes): List<Lote> {
        val texto = filtros.busqueda.trim()
        val valle = filtros.valle.trim()

        return lotesLocales.filter { lote ->
            val coincideValle = valle.isEmpty() || lote.valle.equals(valle, ignoreCase = true)
            val coincideCampania = !filtros.soloAbiertas || !lote.campaniaCerrada
            val coincideTexto = texto.isEmpty() ||
                lote.nombre.contains(texto, ignoreCase = true) ||
                lote.codigo.contains(texto, ignoreCase = true)

            coincideValle && coincideCampania && coincideTexto
        }.sortedBy { it.codigo }
    }
}
