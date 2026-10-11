package com.francekim.empreintedelame.ui.hdpredictions

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.francekim.empreintedelame.ui.theme.OrDore
import com.francekim.empreintedelame.ui.theme.VioletClair

/**
 * Dessine un hexagramme du Yi-King : 6 traits empilés, du bas vers le haut.
 * [lines] est une chaîne de 6 caractères, position 0 = trait du bas,
 * '1' = trait plein (Yang), '0' = trait coupé (Yin).
 */
@Composable
fun YiKingHexagram(
    lines: String,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .width(120.dp)
            .aspectRatio(1f)
    ) {
        val w = size.width
        val h = size.height
        val lineCount = lines.length
        val rowHeight = h / lineCount
        val strokeWidth = rowHeight * 0.28f
        val gapWidth = w * 0.14f

        lines.forEachIndexed { index, char ->
            // index 0 = trait du bas → dessiné en dernier (en bas du Canvas)
            val rowIndex = lineCount - 1 - index
            val y = rowHeight * rowIndex + rowHeight / 2f

            if (char == '1') {
                drawLine(
                    color = OrDore,
                    start = Offset(0f, y),
                    end   = Offset(w, y),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            } else {
                drawLine(
                    color = VioletClair.copy(alpha = 0.85f),
                    start = Offset(0f, y),
                    end   = Offset(w / 2f - gapWidth / 2f, y),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = VioletClair.copy(alpha = 0.85f),
                    start = Offset(w / 2f + gapWidth / 2f, y),
                    end   = Offset(w, y),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
