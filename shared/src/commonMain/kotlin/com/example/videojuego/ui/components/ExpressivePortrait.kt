package com.example.videojuego.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.videojuego.ui.theme.IslaColors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/** Las expresiones que usan los JSON de las noches. */
enum class Expression {
    NEUTRAL, SERIA, SONRIENDO, TRISTE;

    companion object {
        /** Convierte el texto del JSON. Si no se reconoce, se toma como neutral. */
        fun from(text: String?): Expression = when (text?.trim()?.lowercase()) {
            "sonriendo", "feliz", "contenta", "contento" -> SONRIENDO
            "triste", "preocupada", "preocupado" -> TRISTE
            "seria", "serio", "molesta", "molesto" -> SERIA
            else -> NEUTRAL
        }

        /** Expresión que muestra el personaje al reaccionar a una respuesta según cuánta afinidad dio. */
        fun forDelta(delta: Int): Expression = when {
            delta > 0 -> SONRIENDO
            delta < 0 -> TRISTE
            else -> SERIA
        }
    }
}

/**
 * Personaje grande que reacciona a su expresión: salta contento, se encoge un poco si está triste, se queda firme
 * si está serio y respira suave si está neutral. Además muestra una carita en un globo, que "rebota" al cambiar.
 * (Cuando haya arte con caras distintas, se cambia el [painter] según la expresión y el resto sigue igual.)
 */
@Composable
fun ExpressivePortrait(
    painter: Painter,
    name: String,
    expression: Expression,
    modifier: Modifier = Modifier,
    width: Dp = 150.dp,
    height: Dp = 220.dp,
) {
    val infinite = rememberInfiniteTransition(label = "portrait")
    val t by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart),
        label = "portraitT",
    )
    val wave = sin(t * 2f * PI.toFloat())
    val hop = abs(sin(t * 2f * PI.toFloat() * 1.5f))

    val (lift, squash, tint) = when (expression) {
        Expression.SONRIENDO -> Triple(-10f * hop, 1f, Color(0xFFFFD84D).copy(alpha = 0.10f))
        Expression.TRISTE -> Triple(3f, 0.975f + 0.006f * wave, Color(0xFF4A63B0).copy(alpha = 0.26f))
        Expression.SERIA -> Triple(0f, 1f, Color(0xFF1F2A2C).copy(alpha = 0.14f))
        Expression.NEUTRAL -> Triple(0f, 1f + 0.012f * wave, Color.Transparent)
    }

    val pop = remember { Animatable(0.4f) }
    LaunchedEffect(expression) {
        pop.snapTo(0.4f)
        pop.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium))
    }

    Box(modifier.size(width = width, height = height + 8.dp)) {
        Image(
            painter = painter,
            contentDescription = name,
            modifier = Modifier
                .size(width = width, height = height)
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    translationY = lift
                    scaleY = squash
                    transformOrigin = TransformOrigin(0.5f, 1f)
                },
            colorFilter = if (tint == Color.Transparent) null else ColorFilter.tint(tint, BlendMode.SrcAtop),
        )
        Canvas(
            Modifier
                .size(40.dp)
                .align(Alignment.TopEnd)
                .graphicsLayer { scaleX = pop.value; scaleY = pop.value },
        ) {
            drawExpressionBubble(expression)
        }
    }
}

/** Globo con una carita dibujada con rectángulos y arcos. */
private fun DrawScope.drawExpressionBubble(expression: Expression) {
    val s = size.minDimension
    val ink = IslaColors.Ink
    val face = when (expression) {
        Expression.SONRIENDO -> Color(0xFFFFD84D)
        Expression.TRISTE -> Color(0xFF9DB4F0)
        Expression.SERIA -> Color(0xFFC9C3D6)
        Expression.NEUTRAL -> Color(0xFFF5F0E6)
    }
    drawCircle(ink, s / 2f, Offset(s / 2f, s / 2f))
    drawCircle(face, s / 2f - 3f, Offset(s / 2f, s / 2f))
    val eyeY = s * 0.40f
    val eye = s * 0.07f
    drawCircle(ink, eye, Offset(s * 0.35f, eyeY))
    drawCircle(ink, eye, Offset(s * 0.65f, eyeY))
    when (expression) {
        Expression.SONRIENDO -> drawArc(
            ink, 20f, 140f, false, Offset(s * 0.28f, s * 0.40f), Size(s * 0.44f, s * 0.34f), style = Stroke(s * 0.07f),
        )
        Expression.TRISTE -> {
            drawArc(ink, 200f, 140f, false, Offset(s * 0.30f, s * 0.58f), Size(s * 0.40f, s * 0.28f), style = Stroke(s * 0.07f))
            drawRect(Color(0xFF3E78D6), Offset(s * 0.30f, eyeY + eye * 1.4f), Size(s * 0.05f, s * 0.12f))
        }
        Expression.SERIA -> {
            drawRect(ink, Offset(s * 0.33f, s * 0.64f), Size(s * 0.34f, s * 0.06f))
            drawRect(ink, Offset(s * 0.27f, s * 0.27f), Size(s * 0.16f, s * 0.04f))
            drawRect(ink, Offset(s * 0.57f, s * 0.27f), Size(s * 0.16f, s * 0.04f))
        }
        Expression.NEUTRAL -> drawRect(ink, Offset(s * 0.40f, s * 0.64f), Size(s * 0.20f, s * 0.05f))
    }
}
