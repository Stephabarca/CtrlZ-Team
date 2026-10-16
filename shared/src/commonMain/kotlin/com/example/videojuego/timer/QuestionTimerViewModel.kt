package com.example.videojuego.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

/**
 * Guarda el temporizador fuera de la UI: el ViewModel sobrevive a rotaciones y
 * recreación de la actividad, así que el tiempo restante se conserva.
 */
class QuestionTimerViewModel(totalSeconds: Int) : ViewModel() {
    val timer = CountdownTimer(totalSeconds = totalSeconds, scope = viewModelScope)
}
