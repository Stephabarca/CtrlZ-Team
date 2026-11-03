package com.example.videojuego.world

/** Objetos que se recogen en la isla y se pueden regalar. */
enum class ItemType(val label: String) {
    COCONUT("Coco"),
    COAL("Carbón"),
}

/** Cuántos objetos de cada tipo caben en la mochila. */
const val MAX_PER_ITEM = 9
