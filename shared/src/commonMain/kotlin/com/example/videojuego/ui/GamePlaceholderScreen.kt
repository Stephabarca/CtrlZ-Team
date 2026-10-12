package com.example.videojuego.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.videojuego.navigation.PlatformBackHandler
import com.example.videojuego.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Pantalla placeholder de Intro/Noches. Al pulsar atrás pide confirmación
 * antes de volver al menú.
 */
@Composable
fun GamePlaceholderScreen(
    title: String,
    isLast: Boolean,
    onNext: () -> Unit,
    onBackConfirmed: () -> Unit,
) {
    // rememberSaveable: el diálogo sobrevive a rotación y cambio de app.
    var showConfirm by rememberSaveable { mutableStateOf(false) }

    PlatformBackHandler(enabled = true) { showConfirm = true }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onNext) {
            Text(
                stringResource(
                    if (isLast) Res.string.action_finish else Res.string.action_next,
                ),
            )
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text(stringResource(Res.string.dialog_exit_title)) },
            text = { Text(stringResource(Res.string.dialog_exit_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    onBackConfirmed()
                }) { Text(stringResource(Res.string.dialog_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text(stringResource(Res.string.dialog_cancel))
                }
            },
        )
    }
}
