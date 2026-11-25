package com.example.videojuego.ui.credits

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.offset
import com.example.videojuego.credits.CreditsData
import com.example.videojuego.credits.CreditsRoll
import com.example.videojuego.ui.components.ActionButton
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import videojuego.shared.generated.resources.Res
import videojuego.shared.generated.resources.lila_ariza_1
import videojuego.shared.generated.resources.lucas_ambar_1
import videojuego.shared.generated.resources.valentina_oliva_1

private val SKY = Brush.verticalGradient(listOf(Color(0xFF0B1033), Color(0xFF1B1F4B), Color(0xFF2D2159)))
private val TITLE_COLOR = Color(0xFFFFE9B8)
private val ROLE_COLOR = Color(0xFFFFC46B)
private val TEXT_COLOR = Color(0xFFF4F1FF)

private fun portrait(id: String): DrawableResource? = when (id) {
    "valentina" -> Res.drawable.valentina_oliva_1
    "lucas" -> Res.drawable.lucas_ambar_1
    "lila" -> Res.drawable.lila_ariza_1
    else -> null
}

/**
 * Créditos tipo película: los textos suben solos desde abajo mientras suena «Donde termina el mar» (la pone
 * App con MusicRules). Al final queda "GRACIAS POR JUGAR" en el centro y se vuelve al menú.
 * El botón SALTAR está desde el primer momento (también Esc, Enter o Espacio). El contenido se edita en CreditsData.
 * @param endingTitle si viene de terminar el juego, el final obtenido (por ejemplo "FINAL DE LUCAS"): sale bajo el título.
 */
@Composable
fun CreditsScreen(onBack: () -> Unit, modifier: Modifier = Modifier, endingTitle: String? = null) {
    val back by rememberUpdatedState(onBack)
    var left by remember { mutableStateOf(false) }
    val leave = {
        if (!left) {
            left = true
            back()
        }
    }

    var viewportPx by remember { mutableStateOf(0f) }
    var contentPx by remember { mutableStateOf(0f) }
    val speedPx = with(LocalDensity.current) { CreditsRoll.SPEED_DP_PER_S.dp.toPx() }

    // El reloj de los créditos empieza cuando ya se midió la pantalla, para que el primer cuadro no salte.
    var elapsed by remember { mutableStateOf(0L) }
    LaunchedEffect(viewportPx > 0f) {
        if (viewportPx <= 0f) return@LaunchedEffect
        val t0 = withFrameMillis { it }
        while (true) {
            elapsed = withFrameMillis { it } - t0
            if (CreditsRoll.finished(elapsed, speedPx, viewportPx, contentPx)) {
                leave()
                break
            }
        }
    }

    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    Box(
        modifier
            .fillMaxSize()
            .background(SKY)
            .clipToBounds()
            .onSizeChanged { viewportPx = it.height.toFloat() }
            .focusRequester(focus)
            .focusable()
            .onKeyEvent {
                val skip = it.key == Key.Escape || it.key == Key.Enter || it.key == Key.Spacebar
                if (it.type == KeyEventType.KeyDown && skip) {
                    leave(); true
                } else false
            },
    ) {
        val top = if (viewportPx > 0f) CreditsRoll.topAt(elapsed, speedPx, viewportPx, contentPx) else 100_000f // fuera de la pantalla hasta medirla
        Column(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.Top, unbounded = true)
                .offset { IntOffset(0, top.toInt()) }
                .onSizeChanged { contentPx = it.height.toFloat() }
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                CreditsData.GAME,
                color = TITLE_COLOR,
                fontFamily = PixelFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            CreditLine(CreditsData.PRESENTED_BY)
            if (endingTitle != null) {
                Spacer(Modifier.height(28.dp))
                Text(
                    endingTitle,
                    color = ROLE_COLOR,
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(64.dp))

            RoleTitle("REPARTO")
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp), verticalAlignment = Alignment.Bottom) {
                CreditsData.cast.forEach { member ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        portrait(member.id)?.let {
                            Image(
                                painterResource(it),
                                contentDescription = member.name,
                                modifier = Modifier.size(84.dp),
                                contentScale = ContentScale.Fit,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        CreditLine(member.name)
                    }
                }
            }
            Spacer(Modifier.height(56.dp))

            CreditsData.sections.forEach { section ->
                RoleTitle(section.title)
                section.lines.forEach { CreditLine(it) }
                Spacer(Modifier.height(48.dp))
            }

            Spacer(Modifier.height(40.dp))
            Text(
                CreditsData.THE_END,
                color = TITLE_COLOR,
                fontFamily = PixelFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
            )
        }

        // Saltar: visible desde el primer momento.
        ActionButton(
            text = "SALTAR ▶▶",
            onClick = leave,
            modifier = Modifier.align(Alignment.TopEnd).safeDrawingPadding().padding(12.dp),
            container = IslaColors.Action,
        )
    }
}

@Composable
private fun RoleTitle(text: String) {
    Text(
        text,
        modifier = Modifier.padding(bottom = 10.dp),
        color = ROLE_COLOR,
        fontFamily = PixelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun CreditLine(text: String) {
    Text(
        text,
        modifier = Modifier.widthIn(max = 520.dp).padding(vertical = 3.dp),
        color = TEXT_COLOR,
        fontFamily = PixelFontFamily,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        textAlign = TextAlign.Center,
    )
}
