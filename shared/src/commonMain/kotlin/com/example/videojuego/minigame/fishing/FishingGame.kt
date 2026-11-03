package com.example.videojuego.minigame.fishing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random

/** Cómo salió un intento de pesca. */
enum class CatchResult(val points: Int) {
    /** Se jaló justo en el centro de la zona. */
    PERFECT(20),
    /** Se jaló dentro de la zona verde. */
    GOOD(12),
    /** El pez picó pero se jaló fuera de la zona: sale uno pequeño. */
    WEAK(4),
    /** Se dejó pasar la picada y el pez se fue. */
    LOST(0),
    /** Se jaló antes de que picara y se asustaron los peces. */
    EARLY(-3),
}

enum class FishingPhase { READY, WAITING, BITING, RESULT, FINISHED }

data class FishingState(
    val phase: FishingPhase = FishingPhase.READY,
    val remainingMs: Long,
    /** Posición de la marca en la barra, de 0 a 1 (el centro es 0.5). Solo se mueve mientras pica. */
    val cursor: Float = 0f,
    /** Cuánto tiempo lleva picando el pez; sirve para animar el corcho. */
    val biteElapsedMs: Long = 0L,
    val lastResult: CatchResult? = null,
    val results: List<CatchResult> = emptyList(),
    val points: Int = 0,
)

/** Resumen de una partida de pesca. [score] va de 0 a 100 y es lo que se guarda como resultado del minijuego. */
data class FishingStats(
    val score: Int,
    val perfect: Int,
    val good: Int,
    val weak: Int,
    val lost: Int,
    val early: Int,
) {
    val fishCaught: Int get() = perfect + good + weak
    val attempts: Int get() = fishCaught + lost + early
}

/**
 * Lógica pura de la pesca (tap_timing), sin UI. El tiempo avanza solo con [update], así que se prueba con reloj virtual.
 *
 * Ciclo: se espera a que pique (si se jala antes, se asustan los peces) -> pica y aparece la barra con una marca que
 * va y viene -> se jala cuando la marca está en el centro -> resultado -> se vuelve a esperar. Dura [durationMs].
 */
class FishingGame(
    private val durationMs: Long = 30_000L,
    private val random: Random = Random.Default,
) {
    init {
        require(durationMs > 0) { "La duración debe ser positiva" }
    }

    private val _state = MutableStateFlow(FishingState(remainingMs = durationMs))
    val state: StateFlow<FishingState> = _state.asStateFlow()

    private var waitLeftMs = 0L
    private var resultLeftMs = 0L

    /** Empieza la partida (desde la pantalla de instrucciones). */
    fun start() {
        if (_state.value.phase != FishingPhase.READY) return
        beginWaiting(_state.value)
    }

    /** El jugador toca JALAR. */
    fun tap() {
        val s = _state.value
        when (s.phase) {
            FishingPhase.WAITING -> record(s, CatchResult.EARLY)
            FishingPhase.BITING -> record(s, judge(s.cursor))
            else -> Unit
        }
    }

    /** Avanza el juego [deltaMs] milisegundos. */
    fun update(deltaMs: Long) {
        var s = _state.value
        if (s.phase == FishingPhase.READY || s.phase == FishingPhase.FINISHED) return
        var left = min(deltaMs.coerceAtLeast(0L), s.remainingMs)
        val remaining = s.remainingMs - left
        _state.value = s.copy(remainingMs = remaining)

        while (left > 0L) {
            s = _state.value
            when (s.phase) {
                FishingPhase.WAITING -> {
                    val used = min(left, waitLeftMs)
                    waitLeftMs -= used
                    left -= used
                    if (waitLeftMs == 0L) _state.value = s.copy(phase = FishingPhase.BITING, biteElapsedMs = 0L, cursor = 0f)
                }
                FishingPhase.BITING -> {
                    val used = min(left, BITE_WINDOW_MS - s.biteElapsedMs)
                    val elapsed = s.biteElapsedMs + used
                    left -= used
                    if (elapsed >= BITE_WINDOW_MS) {
                        record(s.copy(biteElapsedMs = elapsed), CatchResult.LOST)
                    } else {
                        _state.value = s.copy(biteElapsedMs = elapsed, cursor = cursorAt(elapsed))
                    }
                }
                FishingPhase.RESULT -> {
                    val used = min(left, resultLeftMs)
                    resultLeftMs -= used
                    left -= used
                    if (resultLeftMs == 0L) beginWaiting(s)
                }
                else -> left = 0L
            }
        }
        if (remaining == 0L) _state.value = _state.value.copy(phase = FishingPhase.FINISHED)
    }

    fun stats(): FishingStats = statsOf(_state.value.results)

    private fun beginWaiting(s: FishingState) {
        waitLeftMs = random.nextLong(WAIT_MIN_MS, WAIT_MAX_MS + 1)
        _state.value = s.copy(phase = FishingPhase.WAITING, cursor = 0f, biteElapsedMs = 0L)
    }

    private fun record(s: FishingState, result: CatchResult) {
        resultLeftMs = if (result == CatchResult.EARLY) EARLY_PAUSE_MS else RESULT_PAUSE_MS
        _state.value = s.copy(
            phase = FishingPhase.RESULT,
            lastResult = result,
            results = s.results + result,
            points = (s.points + result.points).coerceAtLeast(0),
        )
    }

    companion object {
        const val WAIT_MIN_MS = 1_200L
        const val WAIT_MAX_MS = 2_800L
        /** Tiempo que el pez está picando antes de irse. */
        const val BITE_WINDOW_MS = 2_400L
        /** Tiempo que tarda la marca en ir y volver. */
        const val CURSOR_PERIOD_MS = 1_400L
        const val RESULT_PAUSE_MS = 900L
        const val EARLY_PAUSE_MS = 1_200L
        const val PERFECT_HALF_WIDTH = 0.06f
        const val GOOD_HALF_WIDTH = 0.16f
        /** Con esta cantidad de capturas perfectas se logra el 100 %. */
        const val TARGET_PERFECT_CATCHES = 6

        /** Marca en vaivén: 0 -> 1 -> 0 en cada periodo. */
        fun cursorAt(biteElapsedMs: Long): Float {
            val p = (biteElapsedMs % CURSOR_PERIOD_MS).toFloat() / CURSOR_PERIOD_MS
            return if (p < 0.5f) p * 2f else 2f - p * 2f
        }

        fun judge(cursor: Float): CatchResult {
            val distance = abs(cursor - 0.5f)
            return when {
                distance <= PERFECT_HALF_WIDTH -> CatchResult.PERFECT
                distance <= GOOD_HALF_WIDTH -> CatchResult.GOOD
                else -> CatchResult.WEAK
            }
        }

        fun statsOf(results: List<CatchResult>): FishingStats {
            // Los puntos nunca bajan de 0, igual que en la partida.
            val points = results.fold(0) { acc, r -> (acc + r.points).coerceAtLeast(0) }
            val score = (points * 100 / (TARGET_PERFECT_CATCHES * CatchResult.PERFECT.points)).coerceIn(0, 100)
            return FishingStats(
                score = score,
                perfect = results.count { it == CatchResult.PERFECT },
                good = results.count { it == CatchResult.GOOD },
                weak = results.count { it == CatchResult.WEAK },
                lost = results.count { it == CatchResult.LOST },
                early = results.count { it == CatchResult.EARLY },
            )
        }
    }
}
