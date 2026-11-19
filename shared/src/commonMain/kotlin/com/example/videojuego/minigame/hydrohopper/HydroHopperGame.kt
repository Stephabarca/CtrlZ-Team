package com.example.videojuego.minigame.hydrohopper

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

enum class HydroObstacleType(val width: Float, val penaltyScore: Int) {
    LOG(0.22f, 8),
    ROCK(0.09f, 6), // Botella flotante (pequeña)
    BUOY(0.14f, 8),
}

data class HydroObstacle(
    val id: String,
    val type: HydroObstacleType,
    val x: Float, // 0.15f a 0.85f
    var y: Float, // 0.16f (línea de horizonte) a 1.1f (abajo)
    val speed: Float = 0.4f, // velocidad de descenso por segundo
    var hit: Boolean = false,
    var dodged: Boolean = false,
)

enum class HydroPhase { READY, PLAYING, SHIP_RESCUE, GAME_OVER, FINISHED }

data class HydroHopperState(
    val phase: HydroPhase = HydroPhase.READY,
    val playerX: Float = 0.5f,
    val remainingMs: Long,
    val totalMs: Long,
    val distanceProgress: Float = 0.0f, // 0.0f a 1.0f
    val health: Int = 3,
    val maxHealth: Int = 3,
    val isJumping: Boolean = false,
    val jumpTimeMs: Long = 0L,
    val jumpHeight: Float = 0.0f, // 0.0f a 1.0f durante el salto
    val invincibleRemainingMs: Long = 0L, // Tiempo de gracia invencible tras sufrir un impacto
    val obstacles: List<HydroObstacle> = emptyList(),
    val score: Int = 100,
    val dodgedCount: Int = 0,
    val hitCount: Int = 0,
    val isShipVisible: Boolean = false,
    val lastHitMs: Long = 0L,
)

data class HydroHopperStats(
    val score: Int,
    val dodgedCount: Int,
    val hitCount: Int,
    val rescued: Boolean,
    val finalDistance: Float,
)

/**
 * Lógica pura del minijuego Hydro Hopper (Noche 3).
 * El jugador esquiva y salta obstáculos que surgen desde el horizonte marino hasta llegar al barco.
 */
class HydroHopperGame(
    private val durationMs: Long = 30_000L,
    private val random: Random = Random.Default,
) {
    init {
        require(durationMs > 0) { "La duración debe ser positiva" }
    }

    private val _state = MutableStateFlow(HydroHopperState(remainingMs = durationMs, totalMs = durationMs))
    val state: StateFlow<HydroHopperState> = _state.asStateFlow()

    private var spawnTimerMs: Long = 0L
    private var obstacleIdCounter: Long = 0L

    fun start() {
        if (_state.value.phase != HydroPhase.READY) return
        _state.value = _state.value.copy(phase = HydroPhase.PLAYING)
    }

    fun restart() {
        spawnTimerMs = 0L
        obstacleIdCounter = 0L
        _state.value = HydroHopperState(
            remainingMs = durationMs,
            totalMs = durationMs,
            phase = HydroPhase.PLAYING,
        )
    }

    fun jump() {
        val s = _state.value
        if (s.phase != HydroPhase.PLAYING || s.isJumping) return
        _state.value = s.copy(isJumping = true, jumpTimeMs = 0L, jumpHeight = 0.0f)
    }

    fun movePlayer(deltaX: Float) {
        val current = _state.value
        if (current.phase != HydroPhase.PLAYING) return
        val newX = (current.playerX + deltaX).coerceIn(0.12f, 0.88f)
        _state.value = current.copy(playerX = newX)
    }

    fun setPlayerX(targetX: Float) {
        val current = _state.value
        if (current.phase != HydroPhase.PLAYING) return
        val newX = targetX.coerceIn(0.12f, 0.88f)
        _state.value = current.copy(playerX = newX)
    }

    fun update(deltaMs: Long) {
        val s = _state.value
        if (s.phase == HydroPhase.READY || s.phase == HydroPhase.FINISHED || s.phase == HydroPhase.GAME_OVER) return

        val deltaSec = deltaMs.toFloat() / 1000f
        val remaining = (s.remainingMs - deltaMs).coerceAtLeast(0L)
        val progress = ((s.totalMs - remaining).toFloat() / s.totalMs.toFloat()).coerceIn(0.0f, 1.0f)
        val isShipVisible = progress >= 0.80f

        // Manejar la parábola del salto
        var isJumping = s.isJumping
        var jumpTime = s.jumpTimeMs
        var jumpHeight = s.jumpHeight

        if (isJumping) {
            jumpTime += deltaMs
            val jumpDuration = 650L
            if (jumpTime >= jumpDuration) {
                isJumping = false
                jumpTime = 0L
                jumpHeight = 0.0f
            } else {
                val jumpProgress = jumpTime.toFloat() / jumpDuration.toFloat()
                jumpHeight = sin(PI * jumpProgress).toFloat()
            }
        }

        // Si llegó al barco de rescate
        if (progress >= 0.98f && s.phase == HydroPhase.PLAYING) {
            _state.value = s.copy(
                phase = HydroPhase.SHIP_RESCUE,
                distanceProgress = 1.0f,
                remainingMs = 2500L,
                isShipVisible = true,
                obstacles = emptyList(),
                isJumping = false,
                jumpHeight = 0.0f,
            )
            return
        }

        if (s.phase == HydroPhase.SHIP_RESCUE) {
            val remainingCelebration = s.remainingMs - deltaMs
            if (remainingCelebration <= 0L) {
                _state.value = s.copy(phase = HydroPhase.FINISHED, remainingMs = 0L)
            } else {
                _state.value = s.copy(remainingMs = remainingCelebration)
            }
            return
        }

        // Generar nuevos obstáculos DESDE EL HORIZONTE (y = 0.16f)
        spawnTimerMs += deltaMs
        val currentObstacles = s.obstacles.toMutableList()
        val spawnInterval = when {
            progress < 0.3f -> 1500L
            progress < 0.7f -> 1100L
            else -> 850L
        }

        val horizonY = 0.16f

        if (spawnTimerMs >= spawnInterval && progress < 0.80f) {
            spawnTimerMs = 0L
            obstacleIdCounter++
            val type = HydroObstacleType.entries[random.nextInt(HydroObstacleType.entries.size)]
            val spawnX = random.nextFloat() * 0.7f + 0.15f
            currentObstacles.add(
                HydroObstacle(
                    id = "obs_$obstacleIdCounter",
                    type = type,
                    x = spawnX,
                    y = horizonY,
                    speed = 0.35f + (progress * 0.25f),
                )
            )
        }

        // Actualizar temporizador de invencibilidad tras golpe
        var newInvincibleMs = (s.invincibleRemainingMs - deltaMs).coerceAtLeast(0L)

        // Actualizar posición de obstáculos y detectar colisiones
        var newScore = s.score
        var newDodged = s.dodgedCount
        var newHit = s.hitCount
        var newHealth = s.health
        var lastHit = s.lastHitMs

        val updatedObstacles = mutableListOf<HydroObstacle>()

        for (obs in currentObstacles) {
            obs.y += obs.speed * deltaSec

            // Detectar colisión justa con la balsa del jugador (playerY = 0.82f)
            val playerY = 0.82f
            val isCollidingY = abs(obs.y - playerY) < 0.045f
            val isCollidingX = abs(obs.x - s.playerX) < (obs.type.width * 0.35f)
            val isAirborne = isJumping && jumpHeight > 0.08f // Cualquier salto activo esquiva el agua limpiamente
            val isInvincible = newInvincibleMs > 0L

            if (isCollidingY && isCollidingX && !obs.hit && !isAirborne && !isInvincible) {
                obs.hit = true
                newHit++
                newScore = (newScore - obs.type.penaltyScore).coerceAtLeast(0)
                newHealth = s.health - 1
                lastHit = s.totalMs - remaining
                newInvincibleMs = 1200L // 1.2s de gracia invencible para evitar golpes dobles inmediatos

                if (newHealth <= 0) {
                    _state.value = s.copy(
                        phase = HydroPhase.GAME_OVER,
                        health = 0,
                        score = newScore,
                        hitCount = newHit,
                        obstacles = emptyList(),
                        isJumping = false,
                        jumpHeight = 0.0f,
                        invincibleRemainingMs = 0L,
                    )
                    return
                }
            } else if (obs.y > 1.0f && !obs.hit && !obs.dodged) {
                obs.dodged = true
                newDodged++
            }

            if (obs.y <= 1.15f) {
                updatedObstacles.add(obs)
            }
        }

        _state.value = s.copy(
            remainingMs = remaining,
            distanceProgress = progress,
            playerX = s.playerX,
            isJumping = isJumping,
            jumpTimeMs = jumpTime,
            jumpHeight = jumpHeight,
            invincibleRemainingMs = newInvincibleMs,
            obstacles = updatedObstacles,
            score = newScore,
            health = newHealth,
            dodgedCount = newDodged,
            hitCount = newHit,
            isShipVisible = isShipVisible,
            lastHitMs = lastHit,
        )
    }

    fun finishNow() {
        _state.value = _state.value.copy(phase = HydroPhase.FINISHED)
    }

    fun stats(): HydroHopperStats {
        val s = _state.value
        return HydroHopperStats(
            score = s.score,
            dodgedCount = s.dodgedCount,
            hitCount = s.hitCount,
            rescued = s.distanceProgress >= 0.95f,
            finalDistance = s.distanceProgress,
        )
    }
}
