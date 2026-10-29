package pe.edu.upeu.agrocampo_llontop_vivas

import androidx.compose.ui.window.ComposeUIViewController
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import pe.edu.upeu.agrocampo_llontop_vivas.db.AgroDb
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    val driver = NativeSqliteDriver(AgroDb.Schema, "agrocampo.db")
    return ComposeUIViewController { App(driver) }
}
