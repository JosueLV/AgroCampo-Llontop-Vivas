package pe.edu.upeu.agrocampo_llontop_vivas

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform