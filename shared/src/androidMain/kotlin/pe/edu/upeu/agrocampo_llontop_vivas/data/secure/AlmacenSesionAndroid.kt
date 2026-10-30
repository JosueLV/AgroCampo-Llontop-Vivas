package pe.edu.upeu.agrocampo_llontop_vivas.data.secure

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.AlmacenSesion
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Sesion

/**
 * La clave AES nunca sale de AndroidKeyStore.
 * SharedPreferences solo contiene el IV y el texto cifrado AES-GCM.
 */
class AlmacenSesionAndroid(context: Context) : AlmacenSesion {
    private val preferencias = context.applicationContext.getSharedPreferences(
        "agrocampo_sesion_cifrada", Context.MODE_PRIVATE
    )

    override fun guardar(sesion: Sesion) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, obtenerClave())
        val cifrado = cipher.doFinal(Json.encodeToString(sesion).toByteArray(Charsets.UTF_8))
        val datos = ByteBuffer.allocate(1 + cipher.iv.size + cifrado.size)
            .put(cipher.iv.size.toByte())
            .put(cipher.iv)
            .put(cifrado)
            .array()
        val codificado = Base64.encodeToString(datos, Base64.NO_WRAP)
        check(preferencias.edit().putString("sesion", codificado).commit()) {
            "No se pudo guardar la sesión cifrada"
        }
    }

    override fun recuperar(): Sesion? {
        val guardada = preferencias.getString("sesion", null) ?: return null
        val sesion = runCatching {
            val datos = ByteBuffer.wrap(Base64.decode(guardada, Base64.DEFAULT))
            val longitudIv = datos.get().toInt() and 0xFF
            require(longitudIv in 12..16)
            val iv = ByteArray(longitudIv)
            datos.get(iv)
            val cifrado = ByteArray(datos.remaining())
            datos.get(cifrado)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, obtenerClave(), GCMParameterSpec(128, iv))
            Json.decodeFromString<Sesion>(
                String(cipher.doFinal(cifrado), Charsets.UTF_8)
            )
        }.getOrNull()

        if (sesion == null || !tokenVigente(sesion.token)) {
            borrar()
            return null
        }
        return sesion
    }

    override fun borrar() {
        preferencias.edit().remove("sesion").commit()
    }

    private fun tokenVigente(token: String): Boolean = runCatching {
        val partes = token.split(".")
        require(partes.size == 3)
        val decoded = Base64.decode(
            partes[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
        val expiracion = Json.parseToJsonElement(String(decoded, Charsets.UTF_8))
            .jsonObject["exp"]?.jsonPrimitive?.longOrNull ?: return false
        expiracion > System.currentTimeMillis() / 1000L + 5L
    }.getOrDefault(false)

    private fun obtenerClave(): SecretKey {
        val almacen = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existente = almacen.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existente != null) return existente.secretKey

        val generador = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        generador.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generador.generateKey()
    }

    private companion object {
        const val ALIAS = "agrocampo_jwt_llontop_vivas"
    }
}
