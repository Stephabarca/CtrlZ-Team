package com.example.videojuego.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.videojuego.resources.*
import com.example.videojuego.timer.QuestionTimerViewModel
import com.example.videojuego.timer.TimerStatus
import org.jetbrains.compose.resources.stringResource

private const val DEMO_SECONDS = 15

/**
 * Pantalla TEMPORAL para probar el temporizador de forma aislada.
 * Se elimina cuando exista la pantalla de preguntas (siguiente historia de EP-3).
 */
@Composable
fun TimerDemoScreen(onBack: () -> Unit) {
    val viewModel = viewModel { QuestionTimerViewModel(totalSeconds = DEMO_SECONDS) }
    val timer = viewModel.timer
    val state by timer.state.collectAsState()

    TimerLifecycleEffect(timer)

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.timer_demo_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        CountdownTimerView(state = state)
        if (state.status == TimerStatus.Finished) {
            Text(
                text = stringResource(Res.string.timer_time_up),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Button(onClick = {
            timer.reset()
            timer.start()
        }) { Text(stringResource(Res.string.timer_restart)) }
        Button(onClick = onBack) { Text(stringResource(Res.string.action_back)) }
    }
}
