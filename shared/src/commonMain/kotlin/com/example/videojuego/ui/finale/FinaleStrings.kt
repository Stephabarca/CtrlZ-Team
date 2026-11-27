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

    private val sunsetStart = listOf(
        FinaleLine(null, "El barco se aleja despacio. La isla se vuelve pequeña, y el sol empieza a caer sobre el mar."),
        FinaleLine(null, "Nadie habla. El cielo se pone naranja, luego rosa, luego morado."),
    )

    /** Las últimas líneas antes de la revelación. Con el final de amistad no hay ruleta, así que cambian. */
    fun sunset(friendship: Boolean): List<FinaleLine> = sunsetStart + if (friendship) {
        FinaleLine(null, "Y entonces los tres se acercan a ti, uno por uno, sin decir nada...")
    } else {
        FinaleLine(null, "Y entonces alguien se acerca a tu lado...")
    }

    val rollCaptions = listOf(
        "Cada noche juntos..." to 0L,
        "Cada respuesta, cada regalo, cada ayuda..." to 1300L,
        "Tu corazón ya sabe la respuesta." to 2700L,
    )

    /** Revelación del final de amistad: no hay pareja, pero los cuatro se quedan juntos. */
    val friendshipLines = listOf(
        FinaleLine(null, "El corazón no se detiene en nadie en particular. Esta vez no hace falta."),
        FinaleLine("valentina", "Qué raro: no sé qué somos, pero sé que no quiero que esto se acabe."),
        FinaleLine("lucas", "Yo tampoco. Y no lo digo mucho."),
        FinaleLine("lila", "Seamos honestos: nos salvamos entre todos. Eso no se olvida."),
        FinaleLine(null, "Los cuatro se quedan juntos, viendo cómo el sol se hunde en el mar."),
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
            FinaleLine("valentina", "Cuando todo parecía perdido, siempre fuiste tú quien se quedó a mi lado."),
            FinaleLine("valentina", "Cuando lleguemos a tierra... no quiero que esto se acabe."),
            FinaleLine("valentina", "¿Sabes? Llené el cuaderno que traía. Casi todo son cosas mías, por primera vez."),
        )
        "lucas" -> listOf(
            FinaleLine("lucas", "No soy de muchas palabras. Pero si me dejas, me quedo contigo."),
            FinaleLine("lucas", "Y esta vez no voy a fingir que no me importa."),
            FinaleLine("lucas", "Antes de viajar le escribí a una amiga: «Me voy unos días». Fue fácil, después de todo."),
        )
        else -> listOf(
            FinaleLine("lila", "Ya no necesito recordar nada de la isla para saber lo que quiero."),
            FinaleLine("lila", "Quedarme contigo. Eso sí lo tengo clarísimo."),
            FinaleLine("lila", "Y apenas lleguemos, mando esa carta. Ahora sí sé qué voy a cambiar."),
        )
    }

    fun endingTitle(id: String) =
        if (id == "amistad") "FINAL DE AMISTAD" else "FINAL DE ${names.getValue(id).uppercase()}"

    /** Dos o tres líneas de lo que pasó después, según con quién quedó. */
    fun epilogue(partnerId: String): List<FinaleLine> = when (partnerId) {
        "amistad" -> listOf(
            FinaleLine(null, "Meses después, los cuatro siguen escribiéndose. Valentina organiza las cenas y Lucas siempre llega primero."),
            FinaleLine(null, "Lila les dice lo que piensa, pero ahora lo dice con cuidado. Tú aprendiste a decir lo tuyo sin miedo."),
            FinaleLine(null, "No todos los vínculos terminan en pareja. Los que se construyen con respeto duran igual."),
        )
        "valentina" -> listOf(
            FinaleLine(null, "Meses después, la isla es solo un recuerdo. Valentina y tú comparten un apartamento pequeño con vista al mar."),
            FinaleLine(null, "Ella sigue diciendo lo que siente sin rodeos. Tú aprendiste a escucharla sin interrumpir."),
            FinaleLine(null, "Cada atardecer, antes de cenar, salen a caminar. Algunos días en silencio. Siempre juntos."),
        )
        "lucas" -> listOf(
            FinaleLine(null, "Lucas armó una mesa con la madera que guardó de la isla. Dice que es la mejor que ha hecho."),
            FinaleLine(null, "Habla poco, pero nunca falta: llega a tiempo, avisa si se retrasa y siempre pregunta cómo estuviste."),
            FinaleLine(null, "La fogata de la isla se volvió costumbre. Cada noche, ustedes dos y una conversación honesta."),
        )
        else -> listOf(
            FinaleLine(null, "Lila escribió todo lo que recordaba de la isla, hasta el último detalle. Dice que así nada se pierde."),
            FinaleLine(null, "Siguen diciéndose la verdad, aunque incomode. Aprendieron que la confianza se construye así."),
            FinaleLine(null, "Y de vez en cuando, Lila te mira y sonríe, como aquella noche en la cubierta del barco."),
        )
    }

    /** Frase corta de cada final para la galería. */
    fun blurb(partnerId: String) = when (partnerId) {
        "amistad" -> "Cuatro personas que se cuidan."
        "valentina" -> "Hablar claro y escuchar."
        "lucas" -> "Constancia y hechos, más que palabras."
        else -> "La verdad, aunque incomode."
    }

    const val GalleryTitle = "FINALES"
    const val FriendsLabel = "Amistad"
    const val GallerySubtitle = "Cada decisión te acerca a un final distinto."
    const val Locked = "???"
    const val LockedHint = "Aún no lo has visto."
    const val Back = "VOLVER"
    fun galleryCount(seen: Int, total: Int) = "Finales vistos: $seen de $total"

    const val EndTitle = "FIN"
    const val EndSubtitle = "Compatibilidad a lo largo del viaje"
    const val Continue = "CONTINUAR"
}
