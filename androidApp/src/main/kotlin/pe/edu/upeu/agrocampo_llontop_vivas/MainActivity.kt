package pe.edu.upeu.agrocampo_llontop_vivas

import android.os.Bundle
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import pe.edu.upeu.agrocampo_llontop_vivas.data.secure.AlmacenSesionAndroid
import pe.edu.upeu.agrocampo_llontop_vivas.db.AgroDb

class MainActivity : ComponentActivity() {
    private val driver by lazy {
        AndroidSqliteDriver(AgroDb.Schema, applicationContext, "agrocampo.db")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            show(WindowInsetsCompat.Type.statusBars())
        }

        setContent {
            App(
                driver,
                remember { AlmacenSesionAndroid(applicationContext) },
                remember { eventosDeRed() }
            )
        }
    }

    /** Emite eventos reales cuando Android recupera una red. */
    private fun eventosDeRed(): Flow<Unit> = callbackFlow {
        val conexion = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager
        val escucha = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(Unit)
            }
        }
        conexion.registerDefaultNetworkCallback(escucha)
        awaitClose { conexion.unregisterNetworkCallback(escucha) }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) driver.close()
    }
}
