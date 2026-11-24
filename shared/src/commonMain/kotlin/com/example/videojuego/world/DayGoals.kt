package com.example.videojuego.world

/** Un objetivo del día: ayudar a un personaje con algo que necesita. No es obligatorio y no tiene prisa. */
data class DayGoal(
    val day: Int,
    val npcId: String,
    val item: ItemType,
    val amount: Int,
    /** Texto corto para la pantalla. */
    val title: String,
    /** Lo que dice el personaje al explicar qué necesita. */
    val askLine: String,
    /** Lo que dice al recibirlo. */
    val doneLine: String,
)

/** Resultado de entregar un objetivo: afinidad que suma (se aplica en el sistema de afinidad) y la respuesta. */
data class GoalResult(
    val npcId: String,
    val affinityDelta: Int,
    val line: String,
)

/**
 * Objetivos por día. Ayudar es una forma sana de acercarse, pero como los regalos suma poco
 * ([GiftRules.MAX_GIFT_DELTA]) y no se pide nada a cambio: el pedido es siempre opcional.
 */
object DayGoals {
    val all: List<DayGoal> = listOf(
        DayGoal(
            day = 2,
            npcId = "lucas",
            item = ItemType.WOOD,
            amount = 3,
            title = "Madera para Lucas",
            askLine = "Necesito 3 de madera para arreglar el techo. La sacas de los árboles con un tronco en la base. " +
                "Sin prisa: si hoy no puedes, no pasa nada.",
            doneLine = "Con esto el techo aguanta. Gracias por ayudarme. Me gusta que no pidieras nada a cambio.",
        ),
    )

    fun forDay(day: Int): DayGoal? = all.firstOrNull { it.day == day }
}
