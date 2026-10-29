package pe.edu.upeu.agrocampo_llontop_vivas.data.local

/**
 * Si no hay labores locales, se usa -1. Después: -2, -3, ...
 */
fun siguienteIdTemporal(menorIdActual: Long): Long =
    if (menorIdActual >= 0L) -1L else menorIdActual - 1L
