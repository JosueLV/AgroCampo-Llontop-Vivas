package pe.edu.upeu.agrocampo_llontop_vivas

import kotlinx.serialization.json.Json
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.LoteDto
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.PaginaDto
import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.dto.aDominio
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoteDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun conviertePaginaDeLotesAlDominio() {
        val cuerpo = """
            {
              "contenido": [
                {
                  "id": 3,
                  "codigo": "AG-003",
                  "nombre": "El Progreso",
                  "hectareas": 6.0,
                  "cultivo": "Quinua",
                  "valle": "Mantaro",
                  "campaniaCerrada": false,
                  "dosisMaximaPorHectarea": 2.5,
                  "campoAdicional": "ignorado"
                }
              ],
              "pagina": 0,
              "tamanio": 20,
              "totalElementos": 8,
              "totalPaginas": 1,
              "ultima": true
            }
        """.trimIndent()

        val pagina = json.decodeFromString<PaginaDto<LoteDto>>(cuerpo)
        val lote = pagina.contenido.single().aDominio()

        assertEquals(3L, lote.id)
        assertEquals("AG-003", lote.codigo)
        assertEquals("El Progreso", lote.nombre)
        assertEquals("Mantaro", lote.valle)
        assertEquals(6.0, lote.hectareas)
        assertEquals(2.5, lote.dosisMaximaPorHectarea)
        assertFalse(lote.campaniaCerrada)
        assertEquals(8L, pagina.totalElementos)
        assertTrue(pagina.ultima)
    }

    @Test
    fun permitePaginaVaciaSinInventarRegistros() {
        val cuerpo = """
            {
              "contenido": [],
              "pagina": 0,
              "tamanio": 20,
              "totalElementos": 0,
              "totalPaginas": 0,
              "ultima": true
            }
        """.trimIndent()

        val pagina = json.decodeFromString<PaginaDto<LoteDto>>(cuerpo)

        assertTrue(pagina.contenido.isEmpty())
        assertEquals(0L, pagina.totalElementos)
    }
}
