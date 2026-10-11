package com.francekim.empreintedelame.ui.mirrorhours

import android.app.Application
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.domain.calculator.MirrorHourCalculator
import com.francekim.empreintedelame.domain.model.MirrorHourInfo
import com.francekim.empreintedelame.ui.theme.*

@Composable
fun MirrorHoursScreen(navController: NavController) {
    val context = LocalContext.current
    val vm: MirrorHoursViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )

    var selectedHour by remember { mutableStateOf(vm.nearestHour) }

    // Au clic, le texte change tout de suite — le bloc est alors hors écran, la transition ne se
    // voit pas — puis on remonte doucement vers lui, qui est déjà en place à l'arrivée.
    val scrollState = rememberScrollState()
    val scope       = rememberCoroutineScope()
    val onHourSelected: (String) -> Unit = { hour ->
        selectedHour = hour
        scope.launch {
            scrollState.animateScrollTo(
                value         = 0,
                animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_galaxy),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
            ) {
                TextButton(onClick = {
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        navController.popBackStack()
                    }
                }) {
                    Text("←", color = Or, style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text      = stringResource(R.string.module_mirror_hours),
                    style     = MaterialTheme.typography.titleLarge,
                    color     = Or,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(64.dp))
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color    = Or.copy(alpha = 0.25f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text      = stringResource(R.string.mirror_hours_nearest_label),
                    style     = MaterialTheme.typography.labelMedium,
                    color     = BlancNacre.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                AnimatedContent(
                    targetState = selectedHour,
                    transitionSpec = {
                        (fadeIn(tween(600)) + scaleIn(tween(600), initialScale = 0.85f))
                            .togetherWith(fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.95f))
                            // Hauteur ajustée d'un coup au lieu du ressort par défaut : c'est lui
                            // qui faisait sursauter la grille pendant la remontée.
                            .using(SizeTransform { _, _ -> snap() })
                    },
                    label = "mirrorHourReveal"
                ) { hour ->
                    MirrorHourDetail(hour = hour, info = vm.hours[hour])
                }

                Spacer(Modifier.height(36.dp))
                HorizontalDivider(color = Or.copy(alpha = 0.20f))
                Spacer(Modifier.height(20.dp))

                MirrorHoursSection(
                    title        = stringResource(R.string.mirror_hours_all_section),
                    hours        = MirrorHourCalculator.doubleHours,
                    selectedHour = selectedHour,
                    onSelect     = onHourSelected
                )

                Spacer(Modifier.height(32.dp))

                MirrorHoursSection(
                    title        = stringResource(R.string.mirror_hours_reversed_section),
                    hours        = MirrorHourCalculator.reversedHours,
                    selectedHour = selectedHour,
                    onSelect     = onHourSelected
                )

                Spacer(Modifier.height(32.dp))

                MirrorHoursSection(
                    title        = stringResource(R.string.mirror_hours_triple_section),
                    hours        = MirrorHourCalculator.tripleHours,
                    selectedHour = selectedHour,
                    onSelect     = onHourSelected
                )

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// ─── Détail de l'heure mise en avant ───────────────────────────────────────────

@Composable
private fun MirrorHourDetail(hour: String, info: MirrorHourInfo?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text      = hour,
            fontSize  = 56.sp,
            color     = Or,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))
        SectionTitle(text = stringResource(R.string.mirror_hours_angel_section))
        Spacer(Modifier.height(8.dp))
        Text(
            text      = info?.angel ?: "",
            style     = MaterialTheme.typography.titleMedium,
            color     = VioletMoyen.copy(alpha = 0.90f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))
        SectionTitle(text = stringResource(R.string.mirror_hours_vibration_section))
        Spacer(Modifier.height(8.dp))
        BodyText(info?.vibration)

        Spacer(Modifier.height(24.dp))
        SectionTitle(text = stringResource(R.string.mirror_hours_message_section))
        Spacer(Modifier.height(8.dp))
        BodyText(info?.message)

        Spacer(Modifier.height(24.dp))
        SectionTitle(text = stringResource(R.string.mirror_hours_advice_section))
        Spacer(Modifier.height(8.dp))
        BodyText(info?.advice)
    }
}

@Composable
private fun BodyText(text: String?) {
    if (text.isNullOrBlank()) return
    Text(
        text      = text,
        style     = MaterialTheme.typography.bodySmall,
        color     = BlancNacre.copy(alpha = 0.70f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun SectionTitle(text: String, caption: String? = null) {
    Text(
        text          = text.uppercase(),
        style         = MaterialTheme.typography.labelMedium,
        color         = BlancNacre.copy(alpha = 0.50f),
        letterSpacing = 3.sp,
        textAlign     = TextAlign.Center
    )
    if (caption != null) {
        Spacer(Modifier.height(6.dp))
        Text(
            text      = caption,
            style     = MaterialTheme.typography.bodySmall,
            color     = Or.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 24.dp)
        )
    }
}

// ─── Section titrée (titre + grille) ────────────────────────────────────────────

@Composable
private fun MirrorHoursSection(
    title       : String,
    hours       : List<String>,
    selectedHour: String,
    onSelect    : (String) -> Unit
) {
    Text(
        text          = title,
        style         = MaterialTheme.typography.labelMedium,
        color         = BlancNacre.copy(alpha = 0.50f),
        letterSpacing = 2.sp,
        textAlign     = TextAlign.Center,
        modifier      = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(16.dp))

    MirrorHoursGrid(
        hours        = hours,
        selectedHour = selectedHour,
        onSelect     = onSelect
    )
}

// ─── Grille d'heures ────────────────────────────────────────────────────────────

@Composable
private fun MirrorHoursGrid(
    hours       : List<String>,
    selectedHour: String,
    onSelect    : (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        hours.chunked(4).forEach { rowHours ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowHours.forEach { hour ->
                    MirrorHourChip(
                        hour       = hour,
                        isSelected = hour == selectedHour,
                        onClick    = { onSelect(hour) },
                        modifier   = Modifier.weight(1f)
                    )
                }
                repeat(4 - rowHours.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MirrorHourChip(
    hour      : String,
    isSelected: Boolean,
    onClick   : () -> Unit,
    modifier  : Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) VioletMoyen.copy(alpha = 0.35f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (isSelected) Or else Or.copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Text(
            text  = hour,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) Or else BlancNacre.copy(alpha = 0.75f)
        )
    }
}
