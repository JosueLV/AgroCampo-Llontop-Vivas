package pe.edu.upeu.agrocampo_llontop_vivas.domain.model

/** Los detalles del servidor se conservan hasta la pantalla que originó la petición. */
sealed class ErrorApi(mensaje: String) : Exception(mensaje) {
    class Validacion(
        val erroresPorCampo: Map<String, String> = emptyMap(),
        mensaje: String? = null
    ) : ErrorApi(mensaje ?: "Datos inválidos")
    class NoAutorizado : ErrorApi("Usuario o contraseña incorrectos")
    class SinPrivilegio(mensaje: String? = null) :
        ErrorApi(mensaje ?: "No tienes permiso para esta operación")
    class NoEncontrado(mensaje: String? = null) :
        ErrorApi(mensaje ?: "Recurso no encontrado")
    class Conflicto(mensaje: String? = null) :
        ErrorApi(mensaje ?: "La operación no se puede realizar")
    class Servidor : ErrorApi("Error del servidor")
    class SinConexion : ErrorApi("No se pudo conectar con el servidor")
    class TiempoAgotado : ErrorApi("La conexión tardó demasiado")
}
