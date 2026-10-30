package pe.edu.upeu.agrocampo_llontop_vivas.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
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
                        cantidadJornales = fila.cantidadJornales.toInt(),
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
                    cantidadJornales = labor.cantidadJornales.toLong(),
                    observacion = labor.observacion,
                    pendiente = if (labor.pendiente) 1L else 0L
                )
            }
        }
    }
    /**
     * Identificador negativo exclusivo para registros aún no enviados al backend.
     * Nunca colisiona con los identificadores positivos del servidor.
     */
    suspend fun crearLaborPendiente(
        loteId: Long,
        tipo: TipoLabor,
        fecha: String,
        responsable: String,
        cantidad: Double,
        observacion: String,
        cantidadJornales: Int = 1
    ): Long = withContext(Dispatchers.Default) {
        var nuevoId = -1L
        base.transaction {
            val menorId = base.agroDbQueries.siguienteIdLocal().executeAsOne()
            nuevoId = siguienteIdTemporal(menorId)
            base.agroDbQueries.guardarLabor(
                id = nuevoId,
                loteId = loteId,
                tipo = tipo.name,
                fecha = fecha,
                responsable = responsable,
                cantidad = cantidad,
                cantidadJornales = cantidadJornales.toLong(),
                observacion = observacion,
                pendiente = 1L
            )
        }
        nuevoId
    }

    /**
     * Consulta el estado persistido actual antes de sincronizar. No vacía la cola.
     */
    suspend fun obtenerPendientes(): List<Labor> = withContext(Dispatchers.Default) {
        base.agroDbQueries.observarPendientes().executeAsList().map { fila ->
            Labor(
                id = fila.id,
                loteId = fila.loteId,
                tipo = TipoLabor.valueOf(fila.tipo),
                fecha = fila.fecha,
                responsable = fila.responsable,
                cantidad = fila.cantidad,
                cantidadJornales = fila.cantidadJornales.toInt(),
                observacion = fila.observacion,
                pendiente = true
            )
        }.sortedByDescending { it.id } // -1 antes de -2: orden de creación.
    }

    /** Elimina de la fuente observable solo después del 204 del backend. */
    suspend fun eliminarLabor(id: Long) = withContext(Dispatchers.Default) {
        base.agroDbQueries.eliminarLabor(id)
    }

    /** Conserva el último instante de sincronización confirmada. */
    fun observarUltimaSincronizacion(): Flow<String?> =
        base.agroDbQueries.observarUltimaSincronizacion()
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { fila -> fila?.ultimaSincronizacion }

    suspend fun guardarUltimaSincronizacion(instante: String) =
        withContext(Dispatchers.Default) {
            base.agroDbQueries.guardarUltimaSincronizacion(instante)
        }

    fun observarNumeroPendientes(): Flow<Int> =
        base.agroDbQueries.observarPendientes()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.size }

    /**
     * SC-D: emite el número de labores pendientes de cada lote cada vez
     * que SQLDelight observa un INSERT, UPDATE o DELETE sobre la tabla labor.
     */
    fun observarPendientesPorLote(): Flow<Map<Long, Int>> =
        base.agroDbQueries.contarPendientesPorLote()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { filas ->
                filas.associate { fila -> fila.loteId to fila.totalPendientes.toInt() }
            }

    /**
     * Transacción atómica: retira el ID temporal y guarda el ID definitivo
     * solamente después de recibir la confirmación del backend.
     */
    suspend fun confirmarLaborSincronizada(idTemporal: Long, confirmada: Labor) =
        withContext(Dispatchers.Default) {
            require(idTemporal < 0 && confirmada.id > 0) {
                "La confirmación debe tener un identificador real del servidor"
            }
            base.transaction {
                base.agroDbQueries.eliminarLabor(idTemporal)
                base.agroDbQueries.guardarLabor(
                    id = confirmada.id,
                    loteId = confirmada.loteId,
                    tipo = confirmada.tipo.name,
                    fecha = confirmada.fecha,
                    responsable = confirmada.responsable,
                    cantidad = confirmada.cantidad,
                    cantidadJornales = confirmada.cantidadJornales.toLong(),
                    observacion = confirmada.observacion,
                    pendiente = 0L
                )
            }
        }

}
