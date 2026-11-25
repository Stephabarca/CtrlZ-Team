package com.example.videojuego.ui.finale

/** Una línea del final. [speakerId] es null cuando es narración. */
data class FinaleLine(val speakerId: String?, val text: String)

/** Todos los textos de la escena final, en un solo lugar. */
object FinaleStrings {
    val names = mapOf("valentina" to "Valentina", "lucas" to "Lucas", "lila" to "Lila")

    const val TitleTop = "EL RESCATE"
    const val TitleBottom = "Cubierta del barco, al atardecer"
    const val Hint = "▼"

    val deck = listOf(
        FinaleLine(null, "La tripulación les echa mantas sobre los hombros y les da agua fresca. Por primera vez en días, nadie tiene miedo."),
        FinaleLine("valentina", "¡Nos vieron! ¡De verdad nos vieron! Díganme que no es un sueño."),
        FinaleLine("lucas", "Tres noches en esa isla y seguimos todos aquí. Yo mismo no lo creo."),
        FinaleLine("lila", "Quiero reír y llorar al mismo tiempo. ¿Eso es normal?"),
    )

    val sunset = listOf(
        FinaleLine(null, "El barco se aleja despacio. La isla se vuelve pequeña, y el sol empieza a caer sobre el mar."),
        FinaleLine(null, "Nadie habla. El cielo se pone naranja, luego rosa, luego morado."),
        FinaleLine(null, "Y entonces alguien se acerca a tu lado..."),
    )

    val rollCaptions = listOf(
        "Cada noche juntos..." to 0L,
        "Cada respuesta, cada regalo, cada ayuda..." to 1300L,
        "Tu corazón ya sabe la respuesta." to 2700L,
    )

    fun revealLines(partnerId: String, closeCall: Boolean, runnerUpId: String?): List<FinaleLine> {
        val partner = names.getValue(partnerId)
        val others = names.filterKeys { it != partnerId }.values.toList()
        return buildList {
            add(FinaleLine(null, "El corazón se detuvo en $partner."))
            addAll(partnerLines(partnerId))
            if (closeCall && runnerUpId != null) {
                add(FinaleLine(null, "Fue muy parejo. Por un momento, tu corazón dudó entre $partner y ${names.getValue(runnerUpId)}."))
            }
            add(FinaleLine(null, "${others[0]} y ${others[1]} sonríen con un poco de nostalgia y levantan la mano para despedirse."))
            add(FinaleLine(null, "$partner se queda a tu lado mientras el sol se hunde en el mar. Lo difícil ya pasó."))
        }
    }

    private fun partnerLines(id: String): List<FinaleLine> = when (id) {
        "valentina" -> listOf(
            FinaleLine("valentina", "Cuando todo parecía perdido, siempre fuiste tú el que se quedó a mi lado."),
            FinaleLine("valentina", "Cuando lleguemos a tierra... no quiero que esto se acabe."),
        )
        "lucas" -> listOf(
            FinaleLine("lucas", "No soy de muchas palabras. Pero si me dejas, me quedo contigo."),
            FinaleLine("lucas", "Y esta vez no voy a fingir que no me importa."),
        )
        else -> listOf(
            FinaleLine("lila", "Ya no necesito recordar nada de la isla para saber lo que quiero."),
            FinaleLine("lila", "Quedarme contigo. Eso sí lo tengo clarísimo."),
        )
    }

    const val EndTitle = "FIN"
    const val EndSubtitle = "Compatibilidad a lo largo del viaje"
    const val Continue = "CONTINUAR"
}
