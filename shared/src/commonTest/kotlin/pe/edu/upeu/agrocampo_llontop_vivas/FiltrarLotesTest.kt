package pe.edu.upeu.agrocampo_llontop_vivas

import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Lote
import pe.edu.upeu.agrocampo_llontop_vivas.domain.usecase.FiltrarLotesUseCase
import pe.edu.upeu.agrocampo_llontop_vivas.domain.usecase.FiltrosLotes
import kotlin.test.Test
import kotlin.test.assertEquals

class FiltrarLotesTest {
    private val lotesLocales = listOf(
        Lote(1, "AG-001", "La Esperanza", 2.0, "Maíz", "Mantaro", true, 3.0),
        Lote(2, "AG-002", "Los Olivos", 3.0, "Papa", "Mantaro", false, 3.0),
        Lote(3, "AG-003", "El Progreso", 4.0, "Quinua", "Cañete", false, 3.0),
        Lote(4, "AG-004", "San Isidro", 5.0, "Maíz", "Mantaro", false, 3.0)
    )
    private val filtrar = FiltrarLotesUseCase()

    @Test
    fun combinaValleConCampaniasAbiertasSinUsarLaRed() {
        val resultado = filtrar(lotesLocales, FiltrosLotes(valle = "Mantaro", soloAbiertas = true))
        assertEquals(listOf("AG-002", "AG-004"), resultado.map { it.codigo })
    }

    @Test
    fun buscaPorNombreOCodigoSinDistinguirMayusculas() {
        assertEquals(
            listOf("AG-003"),
            filtrar(lotesLocales, FiltrosLotes(busqueda = "proGRe")).map { it.codigo }
        )
        assertEquals(
            listOf("AG-004"),
            filtrar(lotesLocales, FiltrosLotes(busqueda = "ag-004")).map { it.codigo }
        )
    }

    @Test
    fun mantieneTodosLosLotesCuandoNoHayFiltros() {
        assertEquals(4, filtrar(lotesLocales, FiltrosLotes()).size)
    }

    @Test
    fun combinaTresFiltrosAlMismoTiempo() {
        val resultado = filtrar(
            lotesLocales,
            FiltrosLotes(valle = "MANTARO", soloAbiertas = true, busqueda = "olivos")
        )
        assertEquals(listOf("AG-002"), resultado.map { it.codigo })
    }
}
