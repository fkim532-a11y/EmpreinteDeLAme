package com.francekim.empreintedelame.ui.compatibility

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.francekim.empreintedelame.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

// Les deux illustrations sont dessinées à la même échelle physique (le visage occupe
// la même position en pixels absolus dans les deux fichiers sources) : on applique donc
// le même facteur d'échelle aux deux, puis on les aligne en haut, pour que les visages
// se retrouvent à la même hauteur au lieu d'être recentrés indépendamment.
private const val FEMME_WIDTH_PX = 824f
private const val FEMME_HEIGHT_PX = 818f
private const val HOMME_WIDTH_PX = 606f
private const val HOMME_HEIGHT_PX = 815f

private const val FEMME_HEIGHT_DP = 180f
private val HOMME_HEIGHT_DP = FEMME_HEIGHT_DP * HOMME_HEIGHT_PX / FEMME_HEIGHT_PX
private val FEMME_WIDTH_DP = FEMME_HEIGHT_DP * FEMME_WIDTH_PX / FEMME_HEIGHT_PX
private val HOMME_WIDTH_DP = FEMME_HEIGHT_DP * HOMME_WIDTH_PX / FEMME_HEIGHT_PX
private val BOX_HEIGHT_DP = maxOf(FEMME_HEIGHT_DP, HOMME_HEIGHT_DP)

// Écart (translation depuis le centre) mesuré pour que les deux visages se touchent
// tout juste ~67dp : on reste au-dessus pour qu'ils se rapprochent sans jamais se chevaucher.
private const val START_OFFSET_DP = 260f
private const val MAX_OFFSET_DP = 150f
private const val MIN_OFFSET_DP = 75f
private const val ARRIVAL_DURATION_MS = 2800

@Composable
fun CompatibilityArrivalAnimation(score: Int, onArrived: () -> Unit) {
    val targetOffset = MAX_OFFSET_DP - (MAX_OFFSET_DP - MIN_OFFSET_DP) * (score.coerceIn(0, 100) / 100f)
    val femmeOffset = remember { Animatable(START_OFFSET_DP) }
    val hommeOffset = remember { Animatable(START_OFFSET_DP) }
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        coroutineScope {
            launch { femmeOffset.animateTo(targetOffset, tween(ARRIVAL_DURATION_MS, easing = FastOutSlowInEasing)) }
            launch { hommeOffset.animateTo(targetOffset, tween(ARRIVAL_DURATION_MS, easing = FastOutSlowInEasing)) }
        }
        onArrived()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BOX_HEIGHT_DP.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Image(
            painter = painterResource(R.drawable.compat_profile_femme),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .height(FEMME_HEIGHT_DP.dp)
                .width(FEMME_WIDTH_DP.dp)
                .graphicsLayer { translationX = -femmeOffset.value * density.density }
        )
        Image(
            painter = painterResource(R.drawable.compat_profile_homme),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .height(HOMME_HEIGHT_DP.dp)
                .width(HOMME_WIDTH_DP.dp)
                .graphicsLayer { translationX = hommeOffset.value * density.density }
        )
    }
}
