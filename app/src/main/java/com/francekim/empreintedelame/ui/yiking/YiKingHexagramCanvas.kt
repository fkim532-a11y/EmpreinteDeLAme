package com.francekim.empreintedelame.ui.yiking

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.domain.calculator.YiKingLine
import com.francekim.empreintedelame.ui.theme.BlancNacre

// Dessine un hexagramme en construction ou complet : [lines] contient 0 à 6 traits déjà
// connus (index 0 = trait du bas, comme le tirage traditionnel). Les positions manquantes
// sont dessinées en trait fantôme. Les traits mutants reçoivent un petit repère central.
// Utilise les visuels de barres dorées fournis (mêmes assets que les pièces/l'icône du
// module) plutôt qu'un tracé vectoriel plat.
@Composable
fun YiKingHexagramCanvas(
    lines: List<YiKingLine>,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val barHeight = size * 0.10f
    val gap = size * 0.045f

    Column(
        modifier = modifier.width(size),
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        for (index in 5 downTo 0) {
            YiKingTraitRow(line = lines.getOrNull(index), height = barHeight)
        }
    }
}

@Composable
private fun YiKingTraitRow(line: YiKingLine?, height: Dp) {
    Box(
        modifier = Modifier.fillMaxWidth().height(height),
        contentAlignment = Alignment.Center
    ) {
        // Le trait fraîchement tiré (null → valeur) apparaît en fondu plutôt que d'un coup.
        Crossfade(
            targetState = line,
            animationSpec = tween(700)
        ) { currentLine ->
            Box(
                modifier = Modifier.fillMaxWidth().height(height),
                contentAlignment = Alignment.Center
            ) {
                when {
                    currentLine == null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(height * 0.32f)
                                .clip(RoundedCornerShape(50))
                                .background(BlancNacre.copy(alpha = 0.12f))
                        )
                    }
                    currentLine.isYang -> {
                        Image(
                            painter = painterResource(R.drawable.yiking_trait_full),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxWidth().height(height)
                        )
                    }
                    else -> {
                        Image(
                            painter = painterResource(R.drawable.yiking_trait_broken),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxWidth().height(height)
                        )
                    }
                }

                if (currentLine?.isMutant == true) {
                    Box(
                        modifier = Modifier
                            .size(height * 0.55f)
                            .clip(CircleShape)
                            .background(BlancNacre)
                    )
                }
            }
        }
    }
}

// Représentation "stable" (sans mutation) d'un hexagramme à partir de son motif binaire —
// utilisée pour afficher l'hexagramme de transformation, qui n'a plus de traits mutants.
fun stableLinesFromPattern(pattern: String): List<YiKingLine> =
    pattern.map { char -> YiKingLine.fromValue(if (char == '1') 7 else 8) }
