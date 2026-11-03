package com.example.videojuego.world

/** Resultado de regalar un objeto: cuánta afinidad suma (se aplica en el sistema de afinidad) y qué responde. */
data class GiftResult(
    val npcId: String,
    val item: ItemType,
    val affinityDelta: Int,
    val reaction: String,
)

/**
 * Reglas de los regalos. Son detalles pequeños: como máximo [MAX_GIFT_DELTA] puntos y solo un regalo
 * por personaje por día, para que regalar no reemplace a las conversaciones (relación sana: nadie
 * se "compra" con objetos).
 */
object GiftRules {
    const val MAX_GIFT_DELTA = 3

    private data class Taste(val liked: ItemType, val likedLine: String, val otherLine: String)

    private val tastes = mapOf(
        "valentina" to Taste(
            ItemType.COCONUT,
            "¡Un coco! Qué detalle, gracias. Lo compartimos más tarde.",
            "Gracias por pensar en mí. No tenías que hacerlo, pero me alegra.",
        ),
        "lucas" to Taste(
            ItemType.COAL,
            "Carbón... justo lo que necesitaba para la fogata. Gracias.",
            "Gracias. Lo guardo, ¿sí? Me gusta que no me pidas nada a cambio.",
        ),
        "lila" to Taste(
            ItemType.COCONUT,
            "Mm, un coco. Es un buen detalle... y no voy a fingir que no me gustó.",
            "Gracias. Lo agradezco, aunque prefiero que me lo digas con palabras.",
        ),
    )

    fun affinityDelta(npcId: String, item: ItemType): Int = when {
        tastes[npcId] == null -> 1
        tastes.getValue(npcId).liked == item -> MAX_GIFT_DELTA
        else -> 1
    }

    fun reaction(npcId: String, item: ItemType): String {
        val taste = tastes[npcId] ?: return "Gracias por el regalo."
        return if (taste.liked == item) taste.likedLine else taste.otherLine
    }

    fun gift(npcId: String, item: ItemType) =
        GiftResult(npcId, item, affinityDelta(npcId, item), reaction(npcId, item))
}
