package pe.edu.upeu.agrocampo_llontop_vivas

import pe.edu.upeu.agrocampo_llontop_vivas.data.local.siguienteIdTemporal
import kotlin.test.Test
import kotlin.test.assertEquals

class IdTemporalTest {
    @Test
    fun primeraLaborPendienteTieneIdNegativo() {
        assertEquals(-1L, siguienteIdTemporal(0L))
        assertEquals(-1L, siguienteIdTemporal(8L))
    }

    @Test
    fun nuevasLaboresNuncaRepitenIdTemporal() {
        assertEquals(-2L, siguienteIdTemporal(-1L))
        assertEquals(-7L, siguienteIdTemporal(-6L))
    }
}
