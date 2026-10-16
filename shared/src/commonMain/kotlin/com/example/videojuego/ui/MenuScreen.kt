package com.example.videojuego.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.videojuego.resources.*
import org.jetbrains.compose.resources.stringResource

/** Menú principal con Jugar, Continuar, Ajustes y Salir. */
@Composable
fun MenuScreen(
    onPlay: () -> Unit,
    onContinue: () -> Unit,
    onSettings: () -> Unit,
    onTimerDemo: () -> Unit,
    onExit: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.menu_title),
            style = MaterialTheme.typography.headlineLarge,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onPlay) { Text(stringResource(Res.string.menu_play)) }
        // Deshabilitado hasta que exista el guardado de partida (EP-9).
        Button(onClick = onContinue, enabled = false) {
            Text(stringResource(Res.string.menu_continue))
        }
        Button(onClick = onSettings) { Text(stringResource(Res.string.menu_settings)) }
        // TEMPORAL: quitar cuando exista la pantalla de preguntas.
        Button(onClick = onTimerDemo) { Text(stringResource(Res.string.menu_timer_demo)) }
        Button(onClick = onExit) { Text(stringResource(Res.string.menu_exit)) }
    }
}
