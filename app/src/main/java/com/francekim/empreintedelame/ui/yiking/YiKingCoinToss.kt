package com.francekim.empreintedelame.ui.yiking

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.francekim.empreintedelame.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Les 3 pièces sont les mêmes éléments animés en continu du repos jusqu'au résultat, d'un lancer
// à l'autre : jamais de composant qui apparaît/disparaît, donc jamais de saut visuel. Chaque pièce
// tourne toujours d'au moins un tour complet à partir de sa position actuelle (flourish), puis
// s'arrête sur la face tirée — la face affichée est déterminée par l'angle courant modulo 360°,
// pas par un seuil à sens unique, pour rester correcte même après plusieurs lancers successifs.
@Composable
fun YiKingCoinsDisplay(
    isTossing: Boolean,
    pendingCoins: List<Boolean>?,
    tossKey: Int,
    onLanded: () -> Unit,
    coinSize: Dp = 64.dp
) {
    // Repos initial : une pièce sur le verso pour montrer les deux côtés.
    val rotations = remember { List(3) { i -> Animatable(if (i == 1) 180f else 0f) } }
    val bounces = remember { List(3) { Animatable(0f) } }

    LaunchedEffect(tossKey) {
        if (!isTossing || pendingCoins == null) return@LaunchedEffect
        coroutineScope {
            pendingCoins.forEachIndexed { i, isYang ->
                launch {
                    delay(i * 180L)
                    val current = rotations[i].value
                    val currentFace = ((current % 360f) + 360f) % 360f
                    val targetFace = if (isYang) 0f else 180f
                    val delta = ((targetFace - currentFace) + 360f) % 360f
                    val target = current + 360f + delta
                    launch {
                        bounces[i].animateTo(-46f, tween(400, easing = FastOutSlowInEasing))
                        bounces[i].animateTo(0f, tween(700, easing = FastOutSlowInEasing))
                    }
                    rotations[i].animateTo(target, tween(1400, easing = FastOutSlowInEasing))
                }
            }
        }
        // Laisser le résultat visible un instant avant de passer au trait suivant.
        delay(500)
        onLanded()
    }

    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        repeat(3) { i ->
            YiKingCoinFace(rotation = rotations[i].value, bounce = bounces[i].value, size = coinSize)
        }
    }
}

@Composable
private fun YiKingCoinFace(rotation: Float, bounce: Float, size: Dp) {
    val normalized = ((rotation % 360f) + 360f) % 360f
    val showBack = normalized > 90f && normalized < 270f
    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                rotationY = rotation
                translationY = bounce
                cameraDistance = 16f * density
            }
    ) {
        if (!showBack) {
            Image(
                painter = painterResource(R.drawable.yiking_coin_face),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(size)
            )
        } else {
            Image(
                painter = painterResource(R.drawable.yiking_coin_back),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(size)
                    .graphicsLayer { rotationY = 180f }
            )
        }
    }
}
