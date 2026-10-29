package pe.edu.upeu.agrocampo_llontop_vivas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import pe.edu.upeu.agrocampo_llontop_vivas.db.AgroDb

class MainActivity : ComponentActivity() {
    private val driver by lazy {
        AndroidSqliteDriver(AgroDb.Schema, applicationContext, "agrocampo.db")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App(driver)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) driver.close()
    }
}
