package com.example.videojuego.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.videojuego.resources.*
import com.example.videojuego.timer.CountdownState
import org.jetbrains.compose.resources.stringResource

/** Muestra los segundos restantes y una barra de progreso. Rojo en los últimos 5 s. */
@Composable
fun CountdownTimerView(state: CountdownState, modifier: Modifier = Modifier) {
    val color = if (state.remainingSeconds <= 5) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(Res.string.timer_remaining, state.remainingSeconds),
            style = MaterialTheme.typography.displayMedium,
            color = color,
        )
        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier.fillMaxWidth(),
            color = color,
        )
    }
}
