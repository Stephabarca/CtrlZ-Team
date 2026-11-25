package com.example.videojuego.world

/** Objetos que se recogen en la isla y se pueden regalar. */
enum class ItemType(val label: String) {
    COCONUT("Coco"),
    COAL("Carbón"),
    /** Madera de los árboles del sendero. Solo se consigue desde el día 2. */
    WOOD("Madera"),
    /** Pescado de la pesca de la noche 1. Va a la mochila (y el sobrante al cofre). */
    FISH("Pescado"),
}

/** Cuántos objetos de cada tipo caben en la mochila. */
const val MAX_PER_ITEM = 9
