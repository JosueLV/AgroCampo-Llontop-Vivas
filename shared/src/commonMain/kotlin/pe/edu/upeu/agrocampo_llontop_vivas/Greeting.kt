package pe.edu.upeu.agrocampo_llontop_vivas

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return sayHello(platform.name)
    }
}