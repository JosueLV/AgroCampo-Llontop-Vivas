package pe.edu.upeu.agrocampo_llontop_vivas.di

import app.cash.sqldelight.db.SqlDriver
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import pe.edu.upeu.agrocampo_llontop_vivas.data.local.LotesLocal
import pe.edu.upeu.agrocampo_llontop_vivas.data.repository.LotesRepositorioRemoto
import pe.edu.upeu.agrocampo_llontop_vivas.db.AgroDb
import pe.edu.upeu.agrocampo_llontop_vivas.presentation.lotes.LotesViewModel

fun crearModuloLotes(driver: SqlDriver) = module {
    single { AgroDb(driver) }
    single { LotesLocal(get()) }
    single { LotesRepositorioRemoto(get()) }
    viewModelOf(::LotesViewModel)
}
