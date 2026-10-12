package com.example.videojuego.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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

/** Placeholder de Ajustes; el contenido real llega con EP-9. */
@Composable
fun SettingsPlaceholderScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.screen_settings),
            style = MaterialTheme.typography.headlineMedium,
        )
        Button(onClick = onBack) { Text(stringResource(Res.string.action_back)) }
    }
}
