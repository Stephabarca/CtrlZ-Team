package com.example.videojuego.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily

/** Botón de acción con bordes cuadrados (estilo pixel art). */
@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = IslaColors.Primary,
    content: Color = IslaColors.OnDark,
) {
    Box(
        modifier = modifier
            .border(3.dp, IslaColors.Ink, RectangleShape)
            .background(container, RectangleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = content,
            fontFamily = PixelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
    }
}

/**
 * Caja de diálogo: pestaña con el nombre (color del personaje), texto y un espacio para el botón.
 * Si [speakerName] está vacío (narrador) no se dibuja la pestaña.
 */
@Composable
fun DialogBox(
    speakerName: String,
    accent: Color,
    onAccent: Color,
    text: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (speakerName.isNotBlank()) {
            Box(
                modifier = Modifier
                    .border(3.dp, IslaColors.Ink, RectangleShape)
                    .background(accent, RectangleShape)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                Text(
                    text = speakerName.uppercase(),
                    color = onAccent,
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 130.dp)
                .border(3.dp, IslaColors.Ink, RectangleShape)
                .background(IslaColors.Paper, RectangleShape)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = text,
                color = IslaColors.Ink,
                fontFamily = PixelFontFamily,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            )
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { action() }
        }
    }
}
