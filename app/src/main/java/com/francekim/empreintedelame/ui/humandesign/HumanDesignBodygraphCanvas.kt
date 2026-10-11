package com.francekim.empreintedelame.ui.humandesign

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.francekim.empreintedelame.domain.calculator.humandesign.Center
import com.francekim.empreintedelame.domain.calculator.humandesign.HumanDesignChart
import com.francekim.empreintedelame.domain.calculator.humandesign.HumanDesignTables
import com.francekim.empreintedelame.ui.theme.BlancNacre
import com.francekim.empreintedelame.ui.theme.BleuNuit
import com.francekim.empreintedelame.ui.theme.OrClair
import com.francekim.empreintedelame.ui.theme.OrDore
import com.francekim.empreintedelame.ui.theme.VioletClair

// Position et taille (en fraction de la largeur/hauteur du schéma) de chaque centre,
// disposition standard du Bodygraph simplifiée pour rester lisible sur mobile.
private data class CenterSpot(val center: Center, val xFrac: Float, val yFrac: Float, val sizeFrac: Float)

private val CENTER_LAYOUT = listOf(
    CenterSpot(Center.HEAD,         0.50f, 0.06f, 0.11f),
    CenterSpot(Center.AJNA,         0.50f, 0.18f, 0.13f),
    CenterSpot(Center.THROAT,       0.50f, 0.31f, 0.15f),
    CenterSpot(Center.G,            0.50f, 0.46f, 0.15f),
    CenterSpot(Center.HEART,        0.73f, 0.41f, 0.11f),
    CenterSpot(Center.SPLEEN,       0.25f, 0.59f, 0.13f),
    CenterSpot(Center.SACRAL,       0.50f, 0.63f, 0.16f),
    CenterSpot(Center.SOLAR_PLEXUS, 0.75f, 0.59f, 0.15f),
    CenterSpot(Center.ROOT,         0.50f, 0.87f, 0.13f)
)

// Chaque paire de centres reliés par au moins un canal, sans doublon
// (plusieurs canaux du Human Design relient parfois les 2 mêmes centres).
private val CENTER_PAIRS: List<Pair<Center, Center>> =
    HumanDesignTables.CHANNELS
        .map { it.centerA to it.centerB }
        .distinctBy { setOf(it.first, it.second) }

@Composable
fun HumanDesignBodygraph(
    chart: HumanDesignChart,
    modifier: Modifier = Modifier
) {
    val spotByCenter = CENTER_LAYOUT.associateBy { it.center }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.62f)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            CENTER_PAIRS.forEach { (a, b) ->
                val from = spotByCenter[a] ?: return@forEach
                val to = spotByCenter[b] ?: return@forEach
                val isDefined = chart.definedChannels.any { it.touches(a) && it.touches(b) }

                drawLine(
                    color = if (isDefined) OrDore else VioletClair.copy(alpha = 0.45f),
                    start = Offset(from.xFrac * w, from.yFrac * h),
                    end   = Offset(to.xFrac * w, to.yFrac * h),
                    strokeWidth = if (isDefined) 5f else 3f
                )
            }

            CENTER_LAYOUT.forEach { spot ->
                val isDefined = spot.center in chart.definedCenters
                val pos = Offset(spot.xFrac * w, spot.yFrac * h)
                val half = spot.sizeFrac * w / 2f

                // Halo lumineux derrière les centres définis.
                if (isDefined) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(OrClair.copy(alpha = 0.45f), Color.Transparent),
                            center = pos,
                            radius = half * 2.4f
                        ),
                        radius = half * 2.4f,
                        center = pos
                    )
                }

                val path = Path().apply {
                    moveTo(pos.x,        pos.y - half)
                    lineTo(pos.x + half, pos.y)
                    lineTo(pos.x,        pos.y + half)
                    lineTo(pos.x - half, pos.y)
                    close()
                }

                if (isDefined) {
                    drawPath(
                        path  = path,
                        brush = Brush.radialGradient(
                            colors = listOf(OrClair, OrDore),
                            center = pos,
                            radius = half * 1.2f
                        )
                    )
                } else {
                    drawPath(path, color = BleuNuit)
                }
                drawPath(
                    path  = path,
                    color = if (isDefined) OrDore else VioletClair.copy(alpha = 0.85f),
                    style = Stroke(width = if (isDefined) 3f else 2f)
                )
            }
        }

        // Libellés centrés sur chaque losange, dessinés après le Canvas donc
        // toujours au-dessus. Même couleur blanc/gris que le texte au-dessus
        // du schéma (titre "Centres" et légende), pour tous les losanges.
        CENTER_LAYOUT.forEach { spot ->
            val label = stringResource(centerShortLabelRes(spot.center))
            val halfHeight = maxWidth * (spot.sizeFrac / 2f)
            val boxWidth = maxWidth * (spot.sizeFrac * 1.3f)
            val liftFactor = if (spot.center == Center.HEAD) 2.2f else 1.75f

            Box(
                modifier = Modifier
                    .width(boxWidth)
                    .offset(
                        x = maxWidth * spot.xFrac - boxWidth / 2,
                        y = maxHeight * spot.yFrac - halfHeight * liftFactor
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = label,
                    color      = BlancNacre.copy(alpha = 0.75f),
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    maxLines   = 1
                )
            }
        }
    }
}
