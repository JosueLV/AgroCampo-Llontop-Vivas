package pe.edu.upeu.agrocampo_llontop_vivas.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pe.edu.upeu.agrocampo_llontop_vivas.db.AgroDb
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Lote
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Labor
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.TipoLabor

/**
 * Fuente local observable. La interfaz usa este flujo como fuente de verdad.
 */
class LotesLocal(private val base: AgroDb) {

    fun observarLotes(): Flow<List<Lote>> =
        base.agroDbQueries.observarLotes()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { filas ->
                filas.map { fila ->
                    Lote(
                        id = fila.id,
                        codigo = fila.codigo,
                        nombre = fila.nombre,
                        hectareas = fila.hectareas,
                        cultivo = fila.cultivo,
                        valle = fila.valle,
                        campaniaCerrada = fila.campaniaCerrada != 0L,
                        dosisMaximaPorHectarea = fila.dosisMaxima
                    )
                }
            }

    suspend fun guardarLotes(lotes: List<Lote>) = withContext(Dispatchers.Default) {
        base.transaction {
            lotes.forEach { lote ->
                base.agroDbQueries.guardarLote(
                    id = lote.id,
                    codigo = lote.codigo,
                    nombre = lote.nombre,
                    hectareas = lote.hectareas,
                    cultivo = lote.cultivo,
                    valle = lote.valle,
                    campaniaCerrada = if (lote.campaniaCerrada) 1L else 0L,
                    dosisMaxima = lote.dosisMaximaPorHectarea
                )
            }
        }
    }

    fun observarLabores(loteId: Long): Flow<List<Labor>> =
        base.agroDbQueries.observarLaboresPorLote(loteId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { filas ->
                filas.map { fila ->
                    Labor(
                        id = fila.id,
                        loteId = fila.loteId,
                        tipo = TipoLabor.valueOf(fila.tipo),
                        fecha = fila.fecha,
                        responsable = fila.responsable,
                        cantidad = fila.cantidad,
                        observacion = fila.observacion,
                        pendiente = fila.pendiente != 0L
                    )
                }
            }

    suspend fun guardarLabores(labores: List<Labor>) = withContext(Dispatchers.Default) {
        base.transaction {
            labores.forEach { labor ->
                base.agroDbQueries.guardarLabor(
                    id = labor.id,
                    loteId = labor.loteId,
                    tipo = labor.tipo.name,
                    fecha = labor.fecha,
                    responsable = labor.responsable,
                    cantidad = labor.cantidad,
                    observacion = labor.observacion,
                    pendiente = if (labor.pendiente) 1L else 0L
                )
            }
        }
    }
}
