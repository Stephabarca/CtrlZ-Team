package com.example.videojuego.ui.build

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.videojuego.minigame.build.Piece

/** Dibujo de una pieza de la fogata (pixel art de 16x16, hecho con rectángulos). Se ajusta al tamaño que le des. */
@Composable
fun PieceIcon(piece: Piece, modifier: Modifier = Modifier) {
    Canvas(modifier) { drawPieceIcon(piece, Offset.Zero, size.minDimension) }
}

/** Dibuja el icono de [piece] en un cuadrado de lado [side] con la esquina en [topLeft]. */
fun DrawScope.drawPieceIcon(piece: Piece, topLeft: Offset, side: Float) {
    val u = side / 16f
    fun r(color: Long, x: Int, y: Int, w: Int, h: Int) {
        drawRect(Color(color), Offset(topLeft.x + x * u, topLeft.y + y * u), Size(w * u + 0.5f, h * u + 0.5f))
    }

    when (piece) {
        Piece.YESCA -> { // paja seca
            r(0xFFC9B06A, 2, 11, 12, 3)
            r(0xFFE8D28A, 3, 6, 1, 5)
            r(0xFFE8D28A, 5, 3, 1, 8)
            r(0xFFE8D28A, 7, 5, 1, 6)
            r(0xFFE8D28A, 9, 2, 1, 9)
            r(0xFFE8D28A, 11, 5, 1, 6)
            r(0xFFE8D28A, 13, 7, 1, 4)
            r(0xFFFFF2B8, 5, 3, 1, 2)
            r(0xFFFFF2B8, 9, 2, 1, 2)
            r(0xFF9C8240, 2, 13, 12, 1)
        }
        Piece.RAMAS -> { // dos ramitas cruzadas
            r(0xFF9C6B3C, 2, 11, 3, 2)
            r(0xFF9C6B3C, 4, 9, 3, 2)
            r(0xFF9C6B3C, 6, 7, 3, 2)
            r(0xFF9C6B3C, 8, 5, 3, 2)
            r(0xFF9C6B3C, 10, 3, 3, 2)
            r(0xFF7A5230, 2, 3, 3, 2)
            r(0xFF7A5230, 4, 5, 3, 2)
            r(0xFF7A5230, 8, 9, 3, 2)
            r(0xFF7A5230, 10, 11, 3, 2)
            r(0xFFC9955E, 12, 2, 2, 2)
        }
        Piece.LENA -> { // tronco
            r(0xFF6B4226, 2, 5, 12, 8)
            r(0xFF4F301B, 5, 7, 8, 1)
            r(0xFF4F301B, 4, 10, 9, 1)
            r(0xFF3F2A18, 2, 12, 12, 1)
            r(0xFFC9955E, 2, 5, 3, 8)
            r(0xFF8B5E3C, 3, 7, 1, 4)
            r(0xFF8B5E3C, 3, 8, 1, 2)
        }
        Piece.PIEDRAS -> { // tres piedras
            r(0xFF9AA0A6, 2, 8, 6, 5)
            r(0xFFC5CACF, 3, 9, 2, 1)
            r(0xFF6E7378, 2, 12, 6, 1)
            r(0xFF8A8D91, 8, 6, 6, 7)
            r(0xFFB3B6BA, 9, 7, 2, 1)
            r(0xFF5F6266, 8, 12, 6, 1)
            r(0xFFA9AEB3, 5, 4, 4, 4)
            r(0xFFD0D4D8, 6, 5, 1, 1)
        }
        Piece.HOJAS -> { // hoja verde
            r(0xFF6DAA4F, 6, 2, 4, 2)
            r(0xFF6DAA4F, 4, 4, 8, 6)
            r(0xFF6DAA4F, 6, 10, 4, 2)
            r(0xFF8FCB6B, 5, 5, 2, 2)
            r(0xFF4C8035, 7, 3, 2, 9)
            r(0xFF6B4226, 7, 12, 2, 3)
        }
        Piece.CARBON -> { // trozos de carbón con una brasa
            r(0xFF2B2B2B, 3, 7, 6, 5)
            r(0xFF3A3A3A, 8, 5, 5, 6)
            r(0xFF222222, 5, 4, 4, 4)
            r(0xFF5A5A5A, 4, 8, 2, 1)
            r(0xFF5A5A5A, 9, 6, 2, 1)
            r(0xFFF28C28, 10, 9, 1, 1)
            r(0xFF111111, 3, 12, 10, 1)
        }
        Piece.MOJADA -> { // trampa: tronco empapado con gotas y moho
            r(0xFF4A5A52, 2, 6, 12, 8)
            r(0xFF3A4840, 5, 8, 8, 1)
            r(0xFF3A4840, 4, 11, 9, 1)
            r(0xFF8FA39A, 2, 6, 3, 8)
            r(0xFF6F8A5E, 7, 7, 2, 1)
            r(0xFF6F8A5E, 10, 10, 2, 1)
            r(0xFF5BA9E0, 4, 1, 2, 3)
            r(0xFF5BA9E0, 10, 2, 2, 3)
            r(0xFF9AD0F2, 4, 1, 1, 1)
            r(0xFF9AD0F2, 10, 2, 1, 1)
        }
        Piece.MUSGO -> { // trampa: musgo húmedo con gotas
            r(0xFF7FA37A, 2, 9, 12, 4)
            r(0xFF5E8A5A, 4, 7, 8, 3)
            r(0xFFA5C8A0, 5, 8, 2, 1)
            r(0xFFA5C8A0, 9, 10, 2, 1)
            r(0xFF4A6E48, 2, 12, 12, 1)
            r(0xFF5BA9E0, 7, 2, 2, 3)
            r(0xFF9AD0F2, 7, 2, 1, 1)
        }
    }
}
