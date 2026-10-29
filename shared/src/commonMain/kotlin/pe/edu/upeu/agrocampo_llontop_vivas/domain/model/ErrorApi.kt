package pe.edu.upeu.agrocampo_llontop_vivas.domain.model

sealed class ErrorApi(mensaje: String) : Exception(mensaje) {
    class Validacion : ErrorApi("Datos inválidos")
    class NoAutorizado : ErrorApi("Usuario o contraseña incorrectos")
    class SinPrivilegio : ErrorApi("No tienes permiso para esta operación")
    class NoEncontrado : ErrorApi("Recurso no encontrado")
    class Conflicto : ErrorApi("La operación no se puede realizar")
    class Servidor : ErrorApi("Error del servidor")
    class SinConexion : ErrorApi("No se pudo conectar con el servidor")
    class TiempoAgotado : ErrorApi("La conexión tardó demasiado")
}