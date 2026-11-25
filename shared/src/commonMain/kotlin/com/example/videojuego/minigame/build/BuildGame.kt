package com.example.videojuego.minigame.build

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/** Piezas de la fogata. Las "decoy" son trampas: no sirven y restan puntos si se usan. */
enum class Piece(val label: String, val decoy: Boolean = false) {
    YESCA("Yesca"),
    RAMAS("Ramas"),
    LENA("Leña"),
    PIEDRAS("Piedras"),
    HOJAS("Hojas"),
    CARBON("Carbón"),
    MOJADA("Leña mojada", decoy = true),
    MUSGO("Musgo húmedo", decoy = true);

    companion object {
        /** Piezas buenas, en el orden en que van apareciendo al subir la dificultad. */
        val useful: List<Piece> = values().filter { !it.decoy }
        val decoys: List<Piece> = values().filter { it.decoy }
    }
}

enum class BuildPhase { READY, PLAYING, ROUND_DONE, FINISHED }

/** Qué pasó con la última pieza. */
enum class StepResult { CORRECT, WRONG, DECOY, MISSED }

/**
 * Cómo es una ronda según su nivel de dificultad ([tier], desde 1). Cada ronda es más difícil que la anterior:
 * más pasos, más tipos de pieza, trampas, menos tiempo por pieza y la bandeja se reordena en cada paso.
 */
data class RoundSpec(
    val length: Int,
    val kinds: Int,
    val decoys: Int,
    val stepMs: Long,
    val reshuffle: Boolean,
) {
    val traySize: Int get() = kinds + decoys

    companion object {
        const val MAX_LENGTH = 7
        const val MAX_KINDS = 6
        const val MAX_DECOYS = 2
        const val MIN_STEP_MS = 2_400L

        fun forTier(tier: Int): RoundSpec {
            val t = tier.coerceAtLeast(1)
            return RoundSpec(
                length = min(2 + t, MAX_LENGTH),
                kinds = min(3 + t / 2, MAX_KINDS),
                decoys = when {
                    t < 3 -> 0
                    t < 5 -> 1
                    else -> MAX_DECOYS
                },
                stepMs = max(MIN_STEP_MS, 4_500L - (t - 1) * 700L),
                reshuffle = t >= 2,
            )
        }
    }
}

data class BuildState(
    val phase: BuildPhase = BuildPhase.READY,
    val remainingMs: Long,
    /** Número de ronda que ve el jugador (1, 2, 3...). */
    val round: Int = 1,
    /** Dificultad real de la ronda: sube con cada ronda y arranca más alta en noches difíciles. */
    val tier: Int = 1,
    /** Lo que Lucas va pidiendo, en orden. */
    val sequence: List<Piece> = emptyList(),
    /** Cuántas piezas de la secuencia ya se pusieron. */
    val step: Int = 0,
    val tray: List<Piece> = emptyList(),
    val stepRemainingMs: Long = 0L,
    val stepLimitMs: Long = 0L,
    val lastResult: StepResult? = null,
    val points: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val roundsCompleted: Int = 0,
    val correct: Int = 0,
    val wrong: Int = 0,
    val decoys: Int = 0,
    val missed: Int = 0,
) {
    /** Pieza que Lucas pide ahora, o null si no se está jugando. */
    val expected: Piece? get() = if (phase == BuildPhase.PLAYING) sequence.getOrNull(step) else null
}

/** Resumen de una partida. [score] va de 0 a 100 y es lo que se guarda como resultado del minijuego. */
data class BuildStats(
    val score: Int,
    val rounds: Int,
    val correct: Int,
    val wrong: Int,
    val decoys: Int,
    val missed: Int,
    val bestStreak: Int,
)

/**
 * Lógica pura de "Armar la fogata" (construir juntos), sin UI. El tiempo avanza solo con [update].
 *
 * Lucas pide una secuencia de piezas (leña, ramas, piedras...) y el jugador las coloca en orden tocando la bandeja.
 * Acertar da puntos y racha; equivocarse o usar una pieza trampa resta; si se acaba el tiempo de una pieza, Lucas la
 * coloca solo y no hay puntos. Al completar una ronda empieza otra más difícil, hasta que se acaba [durationMs].
 *
 * [level] (1 a 3) es la dificultad de la noche: con 1 se empieza en las rondas fáciles, con 3 ya con trampas.
 */
class BuildGame(
    private val durationMs: Long = 45_000L,
    level: Int = 1,
    private val random: Random = Random.Default,
) {
    init {
        require(durationMs > 0) { "La duración debe ser positiva" }
    }

    private val startTier = level.coerceIn(1, MAX_LEVEL)
    private val _state = MutableStateFlow(BuildState(remainingMs = durationMs, tier = startTier))
    val state: StateFlow<BuildState> = _state.asStateFlow()

    private var pauseLeftMs = 0L

    /** Empieza la partida (desde la pantalla de instrucciones). */
    fun start() {
        if (_state.value.phase != BuildPhase.READY) return
        beginRound(_state.value, round = 1)
    }

    /** El jugador toca una pieza de la bandeja. */
    fun tap(piece: Piece) {
        val s = _state.value
        if (s.phase != BuildPhase.PLAYING || piece !in s.tray) return
        if (piece == s.sequence[s.step]) correctStep(s) else wrongStep(s, piece)
    }

    /** Toca la pieza de la posición [index] de la bandeja (para el teclado: teclas 1 a 8). */
    fun tapIndex(index: Int) {
        _state.value.tray.getOrNull(index)?.let { tap(it) }
    }

    /** Avanza el juego [deltaMs] milisegundos. */
    fun update(deltaMs: Long) {
        var s = _state.value
        if (s.phase == BuildPhase.READY || s.phase == BuildPhase.FINISHED) return
        var left = min(deltaMs.coerceAtLeast(0L), s.remainingMs)
        _state.value = s.copy(remainingMs = s.remainingMs - left)

        while (left > 0L) {
            s = _state.value
            when (s.phase) {
                BuildPhase.PLAYING -> {
                    val used = min(left, s.stepRemainingMs)
                    left -= used
                    val rest = s.stepRemainingMs - used
                    if (rest == 0L) missStep(s) else _state.value = s.copy(stepRemainingMs = rest)
                }
                BuildPhase.ROUND_DONE -> {
                    val used = min(left, pauseLeftMs)
                    pauseLeftMs -= used
                    left -= used
                    if (pauseLeftMs == 0L) beginRound(s, s.round + 1)
                }
                else -> left = 0L
            }
        }
        if (_state.value.remainingMs == 0L) _state.value = _state.value.copy(phase = BuildPhase.FINISHED)
    }

    fun stats(): BuildStats = statsOf(_state.value)

    // ---------- Reglas ----------

    private fun correctStep(s: BuildState) {
        val gained = CORRECT_POINTS + STREAK_BONUS * min(s.streak, MAX_STREAK_BONUS_STEPS)
        val streak = s.streak + 1
        advance(
            s.copy(
                points = s.points + gained,
                streak = streak,
                bestStreak = max(s.bestStreak, streak),
                correct = s.correct + 1,
                lastResult = StepResult.CORRECT,
            ),
        )
    }

    private fun wrongStep(s: BuildState, piece: Piece) {
        val trap = piece.decoy
        val penalty = if (trap) DECOY_PENALTY else WRONG_PENALTY
        _state.value = s.copy(
            points = (s.points - penalty).coerceAtLeast(0),
            streak = 0,
            wrong = if (trap) s.wrong else s.wrong + 1,
            decoys = if (trap) s.decoys + 1 else s.decoys,
            lastResult = if (trap) StepResult.DECOY else StepResult.WRONG,
        )
    }

    private fun missStep(s: BuildState) {
        advance(s.copy(streak = 0, missed = s.missed + 1, lastResult = StepResult.MISSED), byPlayer = false)
    }

    /**
     * Pasa a la siguiente pieza, o cierra la ronda si era la última. La ronda solo cuenta (y da bonus) si la cerró el
     * jugador con un acierto, no si se acabó el tiempo y la fogata se terminó sola.
     */
    private fun advance(s: BuildState, byPlayer: Boolean = true) {
        val next = s.step + 1
        if (next >= s.sequence.size) {
            pauseLeftMs = ROUND_PAUSE_MS
            _state.value = s.copy(
                phase = BuildPhase.ROUND_DONE,
                step = next,
                stepRemainingMs = 0L,
                roundsCompleted = if (byPlayer) s.roundsCompleted + 1 else s.roundsCompleted,
                points = if (byPlayer) s.points + ROUND_BONUS + ROUND_BONUS_PER_ROUND * s.round else s.points,
            )
        } else {
            val spec = RoundSpec.forTier(s.tier)
            _state.value = s.copy(
                step = next,
                stepRemainingMs = spec.stepMs,
                tray = if (spec.reshuffle) s.tray.shuffled(random) else s.tray,
            )
        }
    }

    private fun beginRound(s: BuildState, round: Int) {
        val tier = startTier + round - 1
        val spec = RoundSpec.forTier(tier)
        val kinds = Piece.useful.take(spec.kinds)
        var previous: Piece? = null
        val sequence = List(spec.length) {
            val pick = kinds.filter { it != previous }.random(random)
            previous = pick
            pick
        }
        val tray = (kinds + Piece.decoys.take(spec.decoys)).shuffled(random)
        _state.value = s.copy(
            phase = BuildPhase.PLAYING,
            round = round,
            tier = tier,
            sequence = sequence,
            step = 0,
            tray = tray,
            stepRemainingMs = spec.stepMs,
            stepLimitMs = spec.stepMs,
        )
    }

    companion object {
        const val MAX_LEVEL = 3
        const val CORRECT_POINTS = 10
        const val STREAK_BONUS = 2
        const val MAX_STREAK_BONUS_STEPS = 5
        const val WRONG_PENALTY = 4
        const val DECOY_PENALTY = 8
        const val ROUND_BONUS = 15
        const val ROUND_BONUS_PER_ROUND = 5
        const val ROUND_PAUSE_MS = 1_000L

        /** Con estos puntos se logra el 100 %. Se puede ajustar si el minijuego resulta muy fácil o difícil. */
        const val TARGET_POINTS = 550

        /** Dificultad de la noche según el campo "difficulty" de minigames.json. */
        fun levelFor(difficulty: String): Int = when (difficulty.trim().lowercase()) {
            "alta" -> 3
            "media" -> 2
            else -> 1
        }

        fun statsOf(s: BuildState) = BuildStats(
            score = (s.points * 100 / TARGET_POINTS).coerceIn(0, 100),
            rounds = s.roundsCompleted,
            correct = s.correct,
            wrong = s.wrong,
            decoys = s.decoys,
            missed = s.missed,
            bestStreak = s.bestStreak,
        )
    }
}
