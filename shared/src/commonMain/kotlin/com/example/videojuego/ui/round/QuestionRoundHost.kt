package com.example.videojuego.ui.round

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.data.GameServices
import com.example.videojuego.data.save.SaveGame
import com.example.videojuego.round.RoundLoadResult
import com.example.videojuego.round.RoundLoader
import com.example.videojuego.round.RoundResult
import com.example.videojuego.round.RoundSnapshot
import com.example.videojuego.round.RoundSnapshotCodec
import com.example.videojuego.ui.components.ActionButton
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily

/**
 * Punto de entrada de la ronda: carga el banco de preguntas desde los JSON y muestra
 * la ronda, o un error controlado si el banco está vacío o dañado.
 *
 * Si la pantalla se rota o la actividad se recrea, la ronda continúa en la misma pregunta
 * y con el tiempo que le quedaba (el estado se guarda en [rememberSaveable]).
 *
 * @param save progreso actual; si es null se empieza una partida nueva.
 */
@Composable
fun QuestionRoundHost(
    nightId: String,
    onFinished: (RoundResult) -> Unit,
    modifier: Modifier = Modifier,
    save: SaveGame? = null,
    onBack: () -> Unit = {},
) {
    // Sobrevive a rotaciones y a que Android recree la actividad.
    var snapshot by rememberSaveable(nightId) { mutableStateOf<String?>(null) }

    var result by remember(nightId) { mutableStateOf<RoundLoadResult?>(null) }
    var restoredRemainingMs by remember(nightId) { mutableStateOf<Long?>(null) }

    LaunchedEffect(nightId) {
        val restored = snapshot?.let { RoundSnapshotCodec.decode(it) }
        restoredRemainingMs = restored?.remainingMs
        result = RoundLoader.load(GameServices.content, nightId, restored?.save ?: save)
    }

    when (val r = result) {
        null -> CenteredMessage(RoundStrings.Loading, modifier)
        is RoundLoadResult.Error -> RoundErrorScreen(r.message, onBack, modifier)
        is RoundLoadResult.Ready -> QuestionRoundScreen(
            controller = r.controller,
            onFinished = onFinished,
            modifier = modifier,
            initialRemainingMs = restoredRemainingMs,
            onProgress = { GameServices.saves.save(it) },
            onSnapshot = { progress, remainingMs ->
                snapshot = RoundSnapshotCodec.encode(RoundSnapshot(progress, remainingMs))
            },
        )
    }
}

@Composable
private fun CenteredMessage(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(IslaColors.Primary), contentAlignment = Alignment.Center) {
        Text(text, color = IslaColors.OnDark, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold)
    }
}

/** Error controlado: explica qué pasó y permite volver, sin cerrar la app. */
@Composable
fun RoundErrorScreen(message: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.fillMaxSize().background(IslaColors.Primary).safeDrawingPadding().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .border(3.dp, IslaColors.Ink, RectangleShape)
                .background(IslaColors.Paper, RectangleShape)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                RoundStrings.ErrorTitle,
                color = IslaColors.Action,
                fontFamily = PixelFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            Text(message, color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 14.sp, lineHeight = 20.sp)
            ActionButton(text = RoundStrings.Back, onClick = onBack)
        }
    }
}
