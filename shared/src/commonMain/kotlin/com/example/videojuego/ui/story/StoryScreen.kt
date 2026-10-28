package com.example.videojuego.ui.story

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.videojuego.history.AnswerHistory
import com.example.videojuego.navigation.PlatformBackHandler
import com.example.videojuego.story.CargaHistoria
import com.example.videojuego.story.StoryController
import com.example.videojuego.story.StoryLoader
import com.example.videojuego.story.model.Historia
import com.example.videojuego.ui.components.ActionButton
import com.example.videojuego.ui.components.DialogBox
import com.example.videojuego.ui.theme.IslaColors
import org.jetbrains.compose.resources.stringResource
import videojuego.shared.generated.resources.*
import kotlin.coroutines.cancellation.CancellationException

/**
 * Noche 1 jugable (EP-6): carga noche1.json, lo valida y lo recorre con diálogos y opciones.
 * Si el JSON no valida, NO se integra: se muestra el error y se puede continuar.
 */
@Composable
fun StoryScreen(
    onFinished: () -> Unit,
    onBackConfirmed: () -> Unit,
) {
    var showConfirm by rememberSaveable { mutableStateOf(false) }
    PlatformBackHandler(enabled = true) { showConfirm = true }

    val carga by produceState<CargaHistoria?>(initialValue = null) {
        value = try {
            StoryLoader.cargarValidada(Res.readBytes("files/noche1.json").decodeToString())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            CargaHistoria.Error("No se pudo leer noche1.json: ${e.message}", e)
        }
    }

    Box(Modifier.fillMaxSize().background(IslaColors.Primary)) {
        when (val c = carga) {
            null -> Text(
                text = stringResource(Res.string.story_loading),
                color = IslaColors.OnDark,
                modifier = Modifier.align(Alignment.Center),
            )
            is CargaHistoria.Error -> Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(Res.string.story_error), color = IslaColors.OnDark)
                Text(c.mensaje, color = IslaColors.OnDark)
                ActionButton(stringResource(Res.string.action_next), onFinished)
            }
            is CargaHistoria.Exito -> StoryContent(c.historia, onFinished)
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

@Composable
private fun StoryContent(historia: Historia, onFinished: () -> Unit) {
    val controller = remember(historia) { StoryController(historia, AnswerHistory()) }
    val state by controller.state.collectAsState()
    val nombres = remember(historia) { historia.personajes.associate { it.id to it.nombre } }

    val linea = state.linea
    val personajeId = linea.personajeId
    val (accent, onAccent) = coloresDe(personajeId)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        if (state.mostrarOpciones) {
            state.escena.opciones.forEach { opcion ->
                ActionButton(
                    text = opcion.texto,
                    onClick = { controller.elegir(opcion.id) },
                    modifier = Modifier.fillMaxWidth(),
                    container = IslaColors.Action,
                )
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(8.dp))
        }
        DialogBox(
            speakerName = personajeId?.let { nombres[it] }.orEmpty(),
            accent = accent,
            onAccent = onAccent,
            text = linea.texto,
        ) {
            if (!state.mostrarOpciones) {
                ActionButton(
                    text = stringResource(if (state.finalizada) Res.string.action_finish else Res.string.action_next),
                    onClick = { if (state.finalizada) onFinished() else controller.avanzar() },
                )
            }
        }
    }
}

/** Color de la pestaña del hablante (paleta del diseño) y color del texto encima. */
private fun coloresDe(personajeId: String?): Pair<Color, Color> = when (personajeId) {
    "valentina" -> IslaColors.Valentina to IslaColors.Ink
    "lucas" -> IslaColors.Lucas to IslaColors.Ink
    "lila" -> IslaColors.Lila to IslaColors.OnDark
    else -> IslaColors.Paper to IslaColors.Ink
}
