package com.example.videojuego.world

import com.example.videojuego.narrative.Speaker

enum class TileType(val walkable: Boolean, val interactable: Boolean = false) {
    WATER(false),
    SAND(true),
    GRASS(true),
    TREE(false),
    ROCK(false),
    FIRE(false),
    HUT(false),
    /** Puerta de la cabaña, por fuera: se puede entrar. */
    HUT_DOOR(false, interactable = true),

    // Interior de la cabaña.
    FLOOR(true),
    WALL(false),
    BED(false, interactable = true),
    CHEST(false, interactable = true),
    SUITCASE(false, interactable = true),
    EXIT_DOOR(false, interactable = true),
}

/** Dónde está el jugador: en la isla o dentro de la cabaña. */
enum class Location { OUTSIDE, CABIN }

/** Una casilla concreta del mapa (para saber con qué se puede interactuar). */
data class TileRef(val type: TileType, val x: Int, val y: Int)

enum class Direction { UP, DOWN, LEFT, RIGHT }

/** Mapa de casillas. Se define con texto (una fila por línea) para editarlo fácil. */
class WorldMap(rows: List<String>) {
    val height: Int = rows.size
    val width: Int = rows.firstOrNull()?.length ?: 0

    private val tiles: List<List<TileType>>

    init {
        require(rows.isNotEmpty() && width > 0) { "El mapa no puede estar vacío" }
        require(rows.all { it.length == width }) { "Todas las filas del mapa deben medir lo mismo" }
        tiles = rows.map { row -> row.map { parse(it) } }
    }

    fun tileAt(x: Int, y: Int): TileType? = tiles.getOrNull(y)?.getOrNull(x)

    fun isWalkable(x: Int, y: Int): Boolean = tileAt(x, y)?.walkable == true

    companion object {
        /**
         * Leyenda: ~ agua, . arena, g pasto, T palmera, r roca, F fogata, H cabaña, P puerta de la cabaña.
         * Interior: f piso, # pared, B cama, C cofre, M maleta, E puerta de salida.
         */
        fun parse(char: Char): TileType = when (char) {
            '~' -> TileType.WATER
            '.' -> TileType.SAND
            'g' -> TileType.GRASS
            'T' -> TileType.TREE
            'r' -> TileType.ROCK
            'F' -> TileType.FIRE
            'H' -> TileType.HUT
            'P' -> TileType.HUT_DOOR
            'f' -> TileType.FLOOR
            '#' -> TileType.WALL
            'B' -> TileType.BED
            'C' -> TileType.CHEST
            'M' -> TileType.SUITCASE
            'E' -> TileType.EXIT_DOOR
            else -> throw IllegalArgumentException("Carácter de mapa desconocido: '$char'")
        }
    }
}

/**
 * Personaje que está en el mapa. [id] coincide con el id del personaje en characters.json
 * y [nightId] con la noche que se juega al hablar con él.
 */
data class NpcData(
    val id: String,
    val name: String,
    val speaker: Speaker,
    val tileX: Int,
    val tileY: Int,
    val nightId: String,
    /** Lo que dice si el jugador le habla cuando todavía no le toca su noche. */
    val idleLine: String,
    /** Conversaciones muy cortas de día; se muestran una tras otra. Si está vacía se usa [idleLine]. */
    val chatLines: List<String> = emptyList(),
) {
    /** Centro del personaje, en coordenadas de casilla. */
    val centerX: Float get() = tileX + 0.5f
    val centerY: Float get() = tileY + 0.5f
}

data class WorldState(
    /** Centro del jugador en coordenadas de casilla (3.5 = mitad de la casilla 3). */
    val playerX: Float,
    val playerY: Float,
    val facing: Direction = Direction.DOWN,
    val moving: Boolean = false,
    /** Personaje al alcance para hablar, o null. */
    val nearbyNpcId: String? = null,
    /** Tiempo acumulado, para animaciones (agua, fogata). */
    val timeMs: Long = 0L,
    /** Día actual, desde 1. */
    val day: Int = 1,
    val phase: DayPhase = DayPhase.DAY,
    /** Tiempo que le queda al día. Llega a 0 y se hace de noche. */
    val dayRemainingMs: Long = DAY_LENGTH_MS,
    val inventory: Map<ItemType, Int> = emptyMap(),
    /** Casillas de palmeras/rocas ya recogidas hoy (se renuevan al día siguiente). */
    val harvested: Set<Pair<Int, Int>> = emptySet(),
    /** Personajes a los que ya se les regaló algo hoy. */
    val giftedNpcIds: Set<String> = emptySet(),
    /** Aumenta cada vez que se recoge algo; la UI lo usa para mostrar "+1 Coco". */
    val harvestSeq: Int = 0,
    val lastHarvest: ItemType? = null,
    /** Cerca de la fogata (para mostrar el aviso de la noche). */
    val nearFire: Boolean = false,
    /** Noche que debe empezar ahora (por tocar la fogata de noche), o null. La UI la consume con acknowledgeNight(). */
    val nightTrigger: String? = null,
    /** La noche de este día ya empezó (se tocó la fogata); no se vuelve a disparar hasta el día siguiente. */
    val nightStarted: Boolean = false,
    val location: Location = Location.OUTSIDE,
    /** Lo guardado en el cofre de la cabaña. */
    val chest: Map<ItemType, Int> = emptyMap(),
    /** Objeto interactuable al alcance (puerta, cama, cofre, maleta), o null. */
    val nearTile: TileRef? = null,
) {
    val isNight: Boolean get() = phase == DayPhase.NIGHT
    fun count(item: ItemType): Int = inventory[item] ?: 0
    fun inChest(item: ItemType): Int = chest[item] ?: 0
}
