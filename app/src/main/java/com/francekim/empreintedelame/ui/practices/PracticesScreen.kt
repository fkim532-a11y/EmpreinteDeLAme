package com.francekim.empreintedelame.ui.practices

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.ui.theme.BlancNacre
import com.francekim.empreintedelame.ui.theme.Or
import com.francekim.empreintedelame.ui.theme.VioletMoyen

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

private fun queryFileName(context: android.content.Context, uri: Uri): String {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) return cursor.getString(nameIndex)
    }
    return uri.lastPathSegment ?: ""
}

@Composable
fun PracticesScreen(navController: NavController) {
    val context = LocalContext.current
    val vm: PracticesViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )
    val state by vm.uiState.collectAsState()

    val musicPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            vm.setCustomMusic(uri, queryFileName(context, uri))
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
                .navigationBarsPadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
            ) {
                TextButton(onClick = {
                    if (state.mode == PracticeMode.TIMER) {
                        vm.stop()
                    } else if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        navController.popBackStack()
                    }
                }) {
                    Text("←", color = Or, style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text      = stringResource(R.string.practices_title),
                    style     = MaterialTheme.typography.titleLarge,
                    color     = Or,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(64.dp))
            }

            when (state.mode) {
                PracticeMode.CHOICE -> PracticesChoiceContent(
                    state         = state,
                    onSetAmbiance = vm::setAmbiance,
                    onPickMusic   = { musicPicker.launch(arrayOf("audio/*")) },
                    onClearMusic  = vm::clearCustomMusic,
                    onSetDuration = vm::setDuration,
                    onLaunch      = vm::start
                )
                PracticeMode.TIMER -> PracticesTimerContent(
                    state   = state,
                    onStop  = vm::stop
                )
                PracticeMode.FINISHED -> PracticesFinishedContent(
                    onBack = vm::backToChoice
                )
            }
        }
    }
}

// ─── Choix (ambiance + durée) ───────────────────────────────────────────────────

@Composable
private fun PracticesChoiceContent(
    state        : PracticesUiState,
    onSetAmbiance: (Ambiance) -> Unit,
    onPickMusic  : () -> Unit,
    onClearMusic : () -> Unit,
    onSetDuration: (Int) -> Unit,
    onLaunch     : () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 24.dp)
            .padding(bottom = 90.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.img_practices_dove),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.height(170.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text      = stringResource(R.string.practices_subtitle),
            style     = MaterialTheme.typography.bodyMedium,
            color     = BlancNacre.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        Text(
            text          = stringResource(R.string.practices_ambiance_label).uppercase(),
            style         = MaterialTheme.typography.labelMedium,
            color         = BlancNacre.copy(alpha = 0.50f),
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            AmbianceChip(
                label    = stringResource(R.string.practices_ambiance_eau),
                selected = state.customMusicUri == null && state.selectedAmbiance == Ambiance.EAU,
                onClick  = { onSetAmbiance(Ambiance.EAU) }
            )
            AmbianceChip(
                label    = stringResource(R.string.practices_ambiance_oiseaux),
                selected = state.customMusicUri == null && state.selectedAmbiance == Ambiance.OISEAUX,
                onClick  = { onSetAmbiance(Ambiance.OISEAUX) }
            )
            AmbianceChip(
                label    = stringResource(R.string.practices_ambiance_silence),
                selected = state.customMusicUri == null && state.selectedAmbiance == Ambiance.SILENCE,
                onClick  = { onSetAmbiance(Ambiance.SILENCE) }
            )
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .border(1.dp, Or.copy(alpha = 0.25f), RoundedCornerShape(50))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (state.customMusicUri != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text      = "🎵 ${state.customMusicName ?: ""}",
                        style     = MaterialTheme.typography.bodySmall,
                        color     = Or,
                        maxLines  = 1,
                        overflow  = TextOverflow.Ellipsis,
                        modifier  = Modifier.widthIn(max = 160.dp)
                    )
                    Text(
                        text     = stringResource(R.string.practices_custom_music_clear),
                        style    = MaterialTheme.typography.bodySmall,
                        color    = BlancNacre.copy(alpha = 0.55f),
                        modifier = Modifier.clickable(onClick = onClearMusic)
                    )
                }
            } else {
                Text(
                    text     = "🎵 " + stringResource(R.string.practices_custom_music_pick),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = BlancNacre.copy(alpha = 0.65f),
                    modifier = Modifier.clickable(onClick = onPickMusic)
                )
            }
        }

        Spacer(Modifier.height(36.dp))

        Text(
            text          = stringResource(R.string.practices_duration_label).uppercase(),
            style         = MaterialTheme.typography.labelMedium,
            color         = BlancNacre.copy(alpha = 0.50f),
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            listOf(5, 10, 15, 20, 30).forEach { minutes ->
                DurationChip(
                    minutes  = minutes,
                    selected = state.durationMinutes == minutes,
                    onClick  = { onSetDuration(minutes) }
                )
            }
        }

    }

    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
            .padding(bottom = 64.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.35f))
            .border(1.5.dp, Or.copy(alpha = 0.80f), RoundedCornerShape(50))
            .clickable(onClick = onLaunch)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = stringResource(R.string.practices_launch),
            style = MaterialTheme.typography.bodyMedium,
            color = Or
        )
    }
    }
}

@Composable
private fun AmbianceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) VioletMoyen.copy(alpha = 0.35f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) Or else Or.copy(alpha = 0.25f),
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) Or else BlancNacre.copy(alpha = 0.75f)
        )
    }
}

@Composable
private fun DurationChip(minutes: Int, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) VioletMoyen.copy(alpha = 0.35f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) Or else Or.copy(alpha = 0.25f),
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = stringResource(R.string.practices_duration_min, minutes),
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) Or else BlancNacre.copy(alpha = 0.75f)
        )
    }
}

// ─── Minuteur ────────────────────────────────────────────────────────────────

@Composable
private fun PracticesTimerContent(
    state : PracticesUiState,
    onStop: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue   = state.timerProgress,
        animationSpec = tween(950),
        label         = "practicesTimerProgress"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(220.dp)) {
                drawArc(
                    color      = Or.copy(alpha = 0.15f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter  = false,
                    style      = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color      = Or,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter  = false,
                    style      = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                text  = formatTime(state.remainingSeconds),
                style = MaterialTheme.typography.headlineLarge,
                color = Or
            )
        }

        Spacer(Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .border(1.dp, BlancNacre.copy(alpha = 0.25f), RoundedCornerShape(50))
                .clickable(onClick = onStop)
                .padding(horizontal = 32.dp, vertical = 10.dp)
        ) {
            Text(
                text  = stringResource(R.string.practices_stop),
                style = MaterialTheme.typography.bodyMedium,
                color = BlancNacre.copy(alpha = 0.55f)
            )
        }
    }
}

// ─── Fin de séance ──────────────────────────────────────────────────────────────

@Composable
private fun PracticesFinishedContent(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text  = "✦",
                style = MaterialTheme.typography.displayLarge,
                color = Or
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text  = stringResource(R.string.practices_finished_title),
                style = MaterialTheme.typography.headlineMedium,
                color = BlancNacre
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text      = stringResource(R.string.practices_finished_subtitle),
                style     = MaterialTheme.typography.bodyMedium,
                color     = BlancNacre.copy(alpha = 0.60f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .border(1.dp, Or.copy(alpha = 0.60f), RoundedCornerShape(50))
                    .clickable(onClick = onBack)
                    .padding(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Text(
                    text  = stringResource(R.string.practices_back),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Or
                )
            }
        }
    }
}
