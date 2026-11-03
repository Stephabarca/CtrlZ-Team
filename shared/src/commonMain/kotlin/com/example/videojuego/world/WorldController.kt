package com.example.videojuego.world

import com.example.videojuego.data.save.WorldSave
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max

/**
 * Lógica pura del mapa (sin UI): movimiento, colisiones, detección de personajes, reloj del día (5 min),
 * recolección de cocos y carbón, regalos, comienzo de la noche al tocar la fogata y la cabaña (entrar, cama, cofre).
 * Se prueba en commonTest.
 *
 * Todas las posiciones están en casillas: la casilla (3, 2) ocupa de x=3 a 4 y de y=2 a 3.
 * Dentro de la cabaña el reloj del día se detiene.
 */
class WorldController(
    private val map: WorldMap,
    private val npcs: List<NpcData>,
    private val startX: Float,
    private val startY: Float,
    private val dayLengthMs: Long = DAY_LENGTH_MS,
    private val totalDays: Int = TOTAL_DAYS,
    private val indoorMap: WorldMap? = null,
    private val indoorStartX: Float = 0f,
    private val indoorStartY: Float = 0f,
) {
    private val _state = MutableStateFlow(
        WorldState(
            playerX = startX,
            playerY = startY,
            nearbyNpcId = nearbyNpc(startX, startY, Location.OUTSIDE),
            nearTile = tileNear(startX, startY, Location.OUTSIDE),
            dayRemainingMs = dayLengthMs,
        ),
    )
    val state: StateFlow<WorldState> = _state.asStateFlow()

    private val chatCounters = mutableMapOf<String, Int>()

    /** Dónde estaba el jugador al entrar a la cabaña, para devolverlo al mismo sitio al salir. */
    private var returnX = startX
    private var returnY = startY

    /** El mapa de [location]. Dentro de la cabaña es el interior; si no hay interior, el de la isla. */
    fun mapFor(location: Location): WorldMap = if (location == Location.CABIN) indoorMap ?: map else map

    /** Los personajes que están en [location]: en la cabaña no hay ninguno. */
    fun npcsFor(location: Location): List<NpcData> = if (location == Location.OUTSIDE) npcs else emptyList()

    /**
     * Avanza la simulación.
     * @param dirX,dirY dirección de entrada de -1 a 1 (joystick o teclado). Una entrada pequeña camina más lento.
     * @param deltaMs tiempo transcurrido desde la actualización anterior.
     * @param clockPaused si es true el reloj del día no avanza (por ejemplo, mientras hay una conversación abierta).
     */
    fun update(dirX: Float, dirY: Float, deltaMs: Long, clockPaused: Boolean = false) {
        val current = _state.value
        val dt = deltaMs.coerceAtLeast(0L)
        val time = current.timeMs + dt

        var base = current.copy(timeMs = time)
        if (!clockPaused && base.location == Location.OUTSIDE && base.phase == DayPhase.DAY && dt > 0L) {
            val remaining = (base.dayRemainingMs - dt).coerceAtLeast(0L)
            base = base.copy(
                dayRemainingMs = remaining,
                phase = if (remaining == 0L) DayPhase.NIGHT else DayPhase.DAY,
            )
        }

        val length = hypot(dirX, dirY)
        if (length < DEADZONE || dt == 0L) {
            _state.value = settle(base.copy(moving = false), base.playerX, base.playerY)
            return
        }

        // La velocidad máxima es la misma en diagonal; una entrada parcial camina más lento.
        val scale = if (length > 1f) 1f / length else 1f
        val vx = dirX * scale
        val vy = dirY * scale
        val step = SPEED_TILES_PER_SEC * dt / 1000f

        // Se mueve por eje para poder deslizarse a lo largo de una pared.
        val location = base.location
        var x = base.playerX
        var y = base.playerY
        val nx = x + vx * step
        if (canStand(nx, y, location)) x = nx
        val ny = y + vy * step
        if (canStand(x, ny, location)) y = ny

        val facing = if (abs(vx) >= abs(vy)) {
            if (vx > 0f) Direction.RIGHT else Direction.LEFT
        } else {
            if (vy > 0f) Direction.DOWN else Direction.UP
        }
        _state.value = settle(
            base.copy(
                playerX = x,
                playerY = y,
                facing = facing,
                moving = x != current.playerX || y != current.playerY,
            ),
            x,
            y,
        )
    }

    /** Id del personaje con el que se puede hablar ahora, o null si no hay ninguno cerca. */
    fun interact(): String? = _state.value.nearbyNpcId

    /** Objeto (puerta, cama, cofre, maleta) con el que se puede interactuar ahora, o null. */
    fun interactTile(): TileRef? = _state.value.nearTile

    /** Siguiente frase de la conversación corta con [npcId]; da la vuelta al llegar al final. */
    fun talk(npcId: String): String? {
        val npc = npcs.firstOrNull { it.id == npcId } ?: return null
        if (npc.chatLines.isEmpty()) return npc.idleLine
        val index = chatCounters.getOrElse(npcId) { 0 }
        chatCounters[npcId] = index + 1
        return npc.chatLines[index % npc.chatLines.size]
    }

    // ---------- Regalos ----------

    /** Se puede regalar [item] a [npcId] ahora mismo (de día, cerca, con el objeto y sin regalo previo hoy). */
    fun canGift(npcId: String, item: ItemType): Boolean {
        val s = _state.value
        return s.phase == DayPhase.DAY &&
            s.nearbyNpcId == npcId &&
            npcId !in s.giftedNpcIds &&
            s.count(item) > 0
    }

    /**
     * Entrega un regalo. Devuelve null si no se puede (ver [canGift]); si no, gasta el objeto y devuelve
     * cuánta afinidad suma y qué responde el personaje. Aplicar la afinidad es responsabilidad de quien llama.
     */
    fun giveGift(npcId: String, item: ItemType): GiftResult? {
        if (!canGift(npcId, item)) return null
        val s = _state.value
        _state.value = s.copy(
            inventory = s.inventory + (item to s.count(item) - 1),
            giftedNpcIds = s.giftedNpcIds + npcId,
        )
        return GiftRules.gift(npcId, item)
    }

    // ---------- Cabaña ----------

    /** Entra a la cabaña si hay interior y el jugador está junto a la puerta. */
    fun enterCabin(): Boolean {
        val s = _state.value
        if (indoorMap == null || s.location != Location.OUTSIDE || s.nearTile?.type != TileType.HUT_DOOR) return false
        returnX = s.playerX
        returnY = s.playerY
        _state.value = settle(
            s.copy(location = Location.CABIN, playerX = indoorStartX, playerY = indoorStartY, moving = false),
            indoorStartX,
            indoorStartY,
        )
        return true
    }

    /** Sale de la cabaña si el jugador está junto a la puerta de salida. */
    fun exitCabin(): Boolean {
        val s = _state.value
        if (s.location != Location.CABIN || s.nearTile?.type != TileType.EXIT_DOOR) return false
        _state.value = settle(
            s.copy(location = Location.OUTSIDE, playerX = returnX, playerY = returnY, moving = false),
            returnX,
            returnY,
        )
        return true
    }

    /** Si ahora se puede dormir en la cama. */
    fun sleepCheck(): SleepCheck {
        val s = _state.value
        return when {
            s.location != Location.CABIN || s.nearTile?.type != TileType.BED -> SleepCheck.NOT_NEAR_BED
            s.phase == DayPhase.NIGHT -> SleepCheck.NIGHT
            else -> SleepCheck.OK
        }
    }

    /** Se puede guardar [item] en el cofre: junto al cofre y con al menos uno en la mochila. */
    fun canDeposit(item: ItemType): Boolean {
        val s = _state.value
        return nearChest(s) && s.count(item) > 0 && s.inChest(item) < CHEST_MAX_PER_ITEM
    }

    /** Se puede sacar [item] del cofre: junto al cofre, con uno guardado y espacio en la mochila. */
    fun canWithdraw(item: ItemType): Boolean {
        val s = _state.value
        return nearChest(s) && s.inChest(item) > 0 && s.count(item) < MAX_PER_ITEM
    }

    fun deposit(item: ItemType): Boolean {
        if (!canDeposit(item)) return false
        val s = _state.value
        _state.value = s.copy(
            inventory = s.inventory + (item to s.count(item) - 1),
            chest = s.chest + (item to s.inChest(item) + 1),
        )
        return true
    }

    fun withdraw(item: ItemType): Boolean {
        if (!canWithdraw(item)) return false
        val s = _state.value
        _state.value = s.copy(
            inventory = s.inventory + (item to s.count(item) + 1),
            chest = s.chest + (item to s.inChest(item) - 1),
        )
        return true
    }

    /** Guarda todo lo de la mochila en el cofre (hasta donde quepa). Devuelve cuántos objetos se guardaron. */
    fun depositAll(): Int {
        var moved = 0
        for (item in ItemType.entries) {
            while (deposit(item)) moved++
        }
        return moved
    }

    private fun nearChest(s: WorldState) = s.location == Location.CABIN && s.nearTile?.type == TileType.CHEST

    // ---------- Guardado ----------

    /** Lo del mapa que se guarda al dormir. */
    fun toWorldSave(): WorldSave {
        val s = _state.value
        return WorldSave(
            day = s.day,
            bag = s.inventory.filterValues { it > 0 }.mapKeys { it.key.name },
            chest = s.chest.filterValues { it > 0 }.mapKeys { it.key.name },
            giftedNpcIds = s.giftedNpcIds.toList(),
        )
    }

    /** Vuelve al estado guardado: el día empieza de nuevo en la playa, con la mochila y el cofre guardados. */
    fun restore(save: WorldSave) {
        fun items(source: Map<String, Int>, cap: Int): Map<ItemType, Int> =
            ItemType.entries.mapNotNull { item ->
                source[item.name]?.coerceIn(0, cap)?.takeIf { it > 0 }?.let { item to it }
            }.toMap()

        _state.value = WorldState(
            playerX = startX,
            playerY = startY,
            nearbyNpcId = nearbyNpc(startX, startY, Location.OUTSIDE),
            nearTile = tileNear(startX, startY, Location.OUTSIDE),
            timeMs = _state.value.timeMs,
            day = save.day.coerceIn(1, totalDays),
            dayRemainingMs = dayLengthMs,
            inventory = items(save.bag, MAX_PER_ITEM),
            chest = items(save.chest, CHEST_MAX_PER_ITEM),
            giftedNpcIds = save.giftedNpcIds.toSet(),
        )
    }

    // ---------- Noche y días ----------

    /** La UI ya inició la noche indicada: se limpia el aviso para que no se repita. */
    fun acknowledgeNight() {
        _state.value = _state.value.copy(nightTrigger = null, nightStarted = true)
    }

    /** Se salió de la noche sin terminarla (botón atrás): la fogata vuelve a poder iniciarla. */
    fun cancelNight() {
        _state.value = _state.value.copy(nightTrigger = null, nightStarted = false)
    }

    /**
     * Pasa al día siguiente: reloj a 5:00, se renuevan palmeras y rocas, se pueden volver a hacer regalos
     * y el jugador vuelve a la playa. Devuelve false si ya se jugaron todos los días.
     */
    fun startNextDay(): Boolean {
        val s = _state.value
        if (s.day >= totalDays) return false
        _state.value = s.copy(
            location = Location.OUTSIDE,
            playerX = startX,
            playerY = startY,
            moving = false,
            nearbyNpcId = nearbyNpc(startX, startY, Location.OUTSIDE),
            nearTile = tileNear(startX, startY, Location.OUTSIDE),
            day = s.day + 1,
            phase = DayPhase.DAY,
            dayRemainingMs = dayLengthMs,
            harvested = emptySet(),
            giftedNpcIds = emptySet(),
            nearFire = false,
            nightTrigger = null,
            nightStarted = false,
            lastHarvest = null,
        )
        return true
    }

    /** Coloca al jugador en una posición (por ejemplo, al volver de una conversación). */
    fun placeAt(x: Float, y: Float) {
        if (!canStand(x, y, _state.value.location)) return
        _state.value = settle(_state.value.copy(playerX = x, playerY = y), x, y)
    }

    /** Recalcula lo que depende de la posición: personaje cercano, objetos, recolección y fogata. */
    private fun settle(s: WorldState, x: Float, y: Float): WorldState {
        val location = s.location
        var result = s.copy(
            nearbyNpcId = nearbyNpc(x, y, location),
            nearTile = tileNear(x, y, location),
        )
        if (location != Location.OUTSIDE) return result.copy(nearFire = false)

        if (result.phase == DayPhase.DAY) {
            val harvested = result.harvested.toMutableSet()
            var inventory = result.inventory
            var seq = result.harvestSeq
            var last = result.lastHarvest
            for (ty in floor(y).toInt() - 1..floor(y).toInt() + 1) {
                for (tx in floor(x).toInt() - 1..floor(x).toInt() + 1) {
                    val item = when (map.tileAt(tx, ty)) {
                        TileType.TREE -> ItemType.COCONUT
                        TileType.ROCK -> ItemType.COAL
                        else -> continue
                    }
                    if ((tx to ty) in harvested) continue
                    if (distanceToTile(x, y, tx, ty) > TOUCH_DISTANCE) continue
                    val owned = inventory[item] ?: 0
                    if (owned >= MAX_PER_ITEM) continue
                    inventory = inventory + (item to owned + 1)
                    harvested += (tx to ty)
                    seq++
                    last = item
                }
            }
            result = result.copy(inventory = inventory, harvested = harvested, harvestSeq = seq, lastHarvest = last)
        }

        val nearFire = touchesFire(x, y)
        result = result.copy(nearFire = nearFire)
        if (result.phase == DayPhase.NIGHT && nearFire && result.nightTrigger == null && !result.nightStarted && result.day <= totalDays) {
            result = result.copy(nightTrigger = nightIdForDay(result.day))
        }
        return result
    }

    private fun touchesFire(x: Float, y: Float): Boolean {
        for (ty in floor(y).toInt() - 1..floor(y).toInt() + 1) {
            for (tx in floor(x).toInt() - 1..floor(x).toInt() + 1) {
                if (map.tileAt(tx, ty) == TileType.FIRE && distanceToTile(x, y, tx, ty) <= TOUCH_DISTANCE) return true
            }
        }
        return false
    }

    /** El objeto interactuable más cercano dentro del alcance, o null. */
    private fun tileNear(x: Float, y: Float, location: Location): TileRef? {
        val tiles = mapFor(location)
        var best: TileRef? = null
        var bestDistance = Float.MAX_VALUE
        for (ty in floor(y).toInt() - 1..floor(y).toInt() + 1) {
            for (tx in floor(x).toInt() - 1..floor(x).toInt() + 1) {
                val type = tiles.tileAt(tx, ty) ?: continue
                if (!type.interactable) continue
                val distance = distanceToTile(x, y, tx, ty)
                if (distance <= TILE_REACH && distance < bestDistance) {
                    best = TileRef(type, tx, ty)
                    bestDistance = distance
                }
            }
        }
        return best
    }

    /** Distancia del punto (x, y) al borde de la casilla (tx, ty); 0 si está dentro. */
    private fun distanceToTile(x: Float, y: Float, tx: Int, ty: Int): Float {
        val dx = max(max(tx - x, x - (tx + 1)), 0f)
        val dy = max(max(ty - y, y - (ty + 1)), 0f)
        return hypot(dx, dy)
    }

    private fun canStand(x: Float, y: Float, location: Location): Boolean {
        val tiles = mapFor(location)
        val corners = listOf(
            x - PLAYER_RADIUS to y - PLAYER_RADIUS,
            x + PLAYER_RADIUS to y - PLAYER_RADIUS,
            x - PLAYER_RADIUS to y + PLAYER_RADIUS,
            x + PLAYER_RADIUS to y + PLAYER_RADIUS,
        )
        if (corners.any { (cx, cy) -> !tiles.isWalkable(floor(cx).toInt(), floor(cy).toInt()) }) return false
        return npcsFor(location).none { hypot(x - it.centerX, y - it.centerY) < PLAYER_RADIUS + NPC_RADIUS }
    }

    private fun nearbyNpc(x: Float, y: Float, location: Location): String? =
        npcsFor(location).map { it to hypot(x - it.centerX, y - it.centerY) }
            .filter { (_, distance) -> distance <= INTERACT_DISTANCE }
            .minByOrNull { (_, distance) -> distance }
            ?.first?.id

    companion object {
        const val PLAYER_RADIUS = 0.3f
        const val NPC_RADIUS = 0.35f
        const val SPEED_TILES_PER_SEC = 3.5f
        const val INTERACT_DISTANCE = 1.4f
        const val DEADZONE = 0.15f

        /** Distancia máxima al borde de una palmera, roca o la fogata para "tocarla". */
        const val TOUCH_DISTANCE = 0.45f

        /** Distancia máxima al borde de la puerta, cama, cofre o maleta para interactuar. */
        const val TILE_REACH = 0.55f

        /** Cuántos objetos de cada tipo caben en el cofre. */
        const val CHEST_MAX_PER_ITEM = 99
    }
}

/** Resultado de intentar dormir en la cama. */
enum class SleepCheck { OK, NOT_NEAR_BED, NIGHT }

/**
 * Esquina izquierda/superior de la cámara en un eje. Sigue al jugador sin salirse del mapa;
 * si el mapa es más chico que la vista, lo centra.
 */
fun cameraOrigin(player: Float, viewSize: Float, mapSize: Int): Float =
    if (mapSize <= viewSize) {
        (mapSize - viewSize) / 2f
    } else {
        (player - viewSize / 2f).coerceIn(0f, mapSize - viewSize)
    }
