package com.example.videojuego.ui.finale

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.finale.FinaleRules
import androidx.compose.ui.graphics.painter.Painter
import com.example.videojuego.ui.components.ActionButton
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily
import org.jetbrains.compose.resources.painterResource
import videojuego.shared.generated.resources.Res
import videojuego.shared.generated.resources.*

/** Galería de finales: los que ya viste se muestran con su personaje; los demás, en silueta. */
@Composable
fun EndingsScreen(seen: Set<String>, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val painters = mapOf(
        "valentina" to painterResource(Res.drawable.valentina_oliva_4),
        "lucas" to painterResource(Res.drawable.lucas_ambar_4),
        "lila" to painterResource(Res.drawable.lila_ariza_4),
    )
    Box(modifier.fillMaxSize().background(IslaColors.Primary).safeDrawingPadding()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(FinaleStrings.GalleryTitle, color = IslaColors.OnDark, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(FinaleStrings.GallerySubtitle, color = IslaColors.OnDark, fontFamily = PixelFontFamily, fontSize = 12.sp)
            Text(
                FinaleStrings.galleryCount(seen.size, FinaleRules.ALL_ENDINGS.size),
                color = IslaColors.OnDark, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp,
            )
            FinaleRules.ALL_ENDINGS.forEach { id ->
                val unlocked = id in seen
                Row(
                    Modifier
                        .fillMaxWidth()
                        .border(3.dp, IslaColors.Ink, RectangleShape)
                        .background(IslaColors.Paper, RectangleShape)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // El final de amistad muestra a los tres juntos, un poco encimados.
                    val faces: List<Painter> =
                        if (id == FinaleRules.FRIENDS_ID) FinaleRules.ORDER.map { painters.getValue(it) } else listOf(painters.getValue(id))
                    Row(horizontalArrangement = Arrangement.spacedBy((-26).dp)) {
                        faces.forEach { face ->
                            Image(
                                painter = face,
                                contentDescription = if (unlocked) FinaleStrings.endingTitle(id) else FinaleStrings.Locked,
                                modifier = Modifier.size(width = 60.dp, height = 88.dp),
                                // Sin ver el final, el personaje se ve como una silueta oscura.
                                colorFilter = if (unlocked) null else ColorFilter.tint(Color(0xFF1F2A2C), BlendMode.SrcIn),
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            if (unlocked) FinaleStrings.endingTitle(id) else FinaleStrings.Locked,
                            color = IslaColors.Action, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                        )
                        Text(
                            if (unlocked) FinaleStrings.blurb(id) else FinaleStrings.LockedHint,
                            color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 12.sp, lineHeight = 17.sp,
                        )
                    }
                }
            }
            ActionButton(text = FinaleStrings.Back, onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}
