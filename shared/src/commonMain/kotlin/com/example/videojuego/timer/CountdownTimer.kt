package com.example.videojuego.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Cuenta regresiva de [totalSeconds] segundos.
 *
 * - [state] expone el tiempo restante y el estado.
 * - [timeUps] emite una vez cuando el tiempo llega a cero.
 * - El tiempo se mide con [timeSource] (reloj virtual en pruebas), no sumando
 *   ticks, así que no acumula desfase.
 *
 * Debe usarse desde un solo hilo (el principal), como lo hace viewModelScope.
 */
class CountdownTimer(
    totalSeconds: Int,
    private val scope: CoroutineScope,
    private val timeSource: TimeSource = TimeSource.Monotonic,
    private val tick: Duration = 100.milliseconds,
) {
    init {
        require(totalSeconds > 0) { "totalSeconds debe ser mayor que 0" }
    }

    private val initialTotal: Duration = totalSeconds.seconds

    private val _state = MutableStateFlow(CountdownState(total = initialTotal))
    val state: StateFlow<CountdownState> = _state.asStateFlow()

    private val timeUpChannel = Channel<Unit>(Channel.CONFLATED)

    /** Evento "tiempo agotado": se entrega una sola vez a quien lo esté escuchando. */
    val timeUps: Flow<Unit> = timeUpChannel.receiveAsFlow()

    private var job: Job? = null
    private var mark: TimeMark? = null

    /** Inicia o reanuda la cuenta. No hace nada si ya corre o ya terminó. */
    fun start() {
        if (job?.isActive == true) return
        if (_state.value.status == TimerStatus.Finished) return

        _state.value = _state.value.copy(status = TimerStatus.Running)
        mark = timeSource.markNow()
        job = scope.launch {
            while (isActive) {
                delay(tick)
                if (!applyElapsed()) break
            }
        }
    }

    /** Pausa la cuenta conservando el tiempo restante. */
    fun pause() {
        if (_state.value.status != TimerStatus.Running) return
        job?.cancel()
        job = null

        val elapsed = mark?.elapsedNow() ?: Duration.ZERO
        mark = null
        val next = _state.value.advance(elapsed)
        if (next.status == TimerStatus.Finished) {
            _state.value = next
            timeUpChannel.trySend(Unit)
        } else {
            _state.value = next.copy(status = TimerStatus.Paused)
        }
    }

    /** Vuelve al tiempo completo y detiene la cuenta. */
    fun reset() {
        job?.cancel()
        job = null
        mark = null
        timeUpChannel.tryReceive() // descarta un evento pendiente
        _state.value = CountdownState(total = initialTotal)
    }

    /** Aplica el tiempo transcurrido. Devuelve false cuando la cuenta terminó. */
    private fun applyElapsed(): Boolean {
        val m = mark ?: return false
        val elapsed = m.elapsedNow()
        mark = m + elapsed // avanza la marca sin perder tiempo entre ticks
        val next = _state.value.advance(elapsed)
        _state.value = next
        if (next.status == TimerStatus.Finished) {
            timeUpChannel.trySend(Unit)
            return false
        }
        return true
    }
}
