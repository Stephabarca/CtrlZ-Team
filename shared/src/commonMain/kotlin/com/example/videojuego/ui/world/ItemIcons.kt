package com.example.videojuego.ui.world

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.videojuego.world.ItemType

/** Dibujo de un objeto de la mochila (pixel art de 16x16 hecho con rectángulos). Se ajusta al tamaño que le des. */
@Composable
fun ItemIcon(item: ItemType, modifier: Modifier = Modifier) {
    Canvas(modifier) { drawItemIcon(item, Offset.Zero, size.minDimension) }
}

/** Dibuja el icono de [item] en un cuadrado de lado [side] con la esquina en [topLeft]. */
fun DrawScope.drawItemIcon(item: ItemType, topLeft: Offset, side: Float) {
    val u = side / 16f
    fun r(color: Long, x: Int, y: Int, w: Int, h: Int) {
        drawRect(Color(color), Offset(topLeft.x + x * u, topLeft.y + y * u), Size(w * u + 0.5f, h * u + 0.5f))
    }

    when (item) {
        ItemType.COCONUT -> {
            r(0xFF6B4A2B, 5, 2, 6, 1)
            r(0xFF6B4A2B, 4, 3, 8, 1)
            r(0xFF6B4A2B, 3, 4, 10, 8)
            r(0xFF6B4A2B, 4, 12, 8, 1)
            r(0xFF6B4A2B, 5, 13, 6, 1)
            r(0xFF4F331B, 12, 5, 1, 7)
            r(0xFF4F331B, 5, 13, 6, 1)
            r(0xFF9C7A52, 5, 4, 2, 2)
            r(0xFF2B1B0E, 6, 7, 1, 1)
            r(0xFF2B1B0E, 9, 7, 1, 1)
            r(0xFF2B1B0E, 7, 10, 2, 1)
        }
        ItemType.COAL -> {
            r(0xFF2B2B2B, 3, 7, 6, 5)
            r(0xFF3A3A3A, 8, 5, 5, 6)
            r(0xFF222222, 5, 4, 4, 4)
            r(0xFF5A5A5A, 4, 8, 2, 1)
            r(0xFF5A5A5A, 9, 6, 2, 1)
            r(0xFFF28C28, 10, 9, 1, 1)
            r(0xFF111111, 3, 12, 10, 1)
        }
        ItemType.WOOD -> {
            r(0xFF6B4226, 2, 5, 12, 8)
            r(0xFF4F301B, 5, 7, 8, 1)
            r(0xFF4F301B, 4, 10, 9, 1)
            r(0xFF3F2A18, 2, 12, 12, 1)
            r(0xFFC9955E, 2, 5, 3, 8)
            r(0xFF8B5E3C, 3, 7, 1, 4)
        }
        ItemType.FISH -> {
            r(0xFF3E7399, 6, 3, 3, 1)
            r(0xFF4F8FB8, 5, 4, 6, 1)
            r(0xFF7FB6D9, 3, 5, 10, 4)
            r(0xFFD6EAF5, 4, 9, 8, 1)
            r(0xFFD6EAF5, 5, 10, 6, 1)
            r(0xFF4F8FB8, 1, 5, 2, 4)
            r(0xFF4F8FB8, 0, 4, 1, 1)
            r(0xFF4F8FB8, 0, 9, 1, 1)
            r(0xFF3E7399, 8, 5, 1, 4)
            r(0xFFD6EAF5, 5, 6, 1, 1)
            r(0xFFD6EAF5, 6, 7, 1, 1)
            r(0xFF111111, 11, 6, 1, 1)
        }
    }
}
