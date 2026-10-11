package com.francekim.empreintedelame.ui.biorhythm

import android.app.Application
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.domain.calculator.BiorhythmCycle
import com.francekim.empreintedelame.domain.calculator.BiorhythmCycleResult
import com.francekim.empreintedelame.domain.calculator.BiorhythmState
import com.francekim.empreintedelame.ui.common.ModuleInfoButton
import com.francekim.empreintedelame.ui.common.sharePdf
import com.francekim.empreintedelame.ui.moduleintro.ModuleIntroIds
import com.francekim.empreintedelame.ui.navigation.Screen
import com.francekim.empreintedelame.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlinx.coroutines.launch

private val PhysicalColor     = Or
private val EmotionalColor    = Color(0xFFFF6B6B)
private val IntellectualColor = VioletClair

@Composable
fun BiorhythmScreen(navController: NavController) {
    val context = LocalContext.current
    val vm: BiorhythmViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val chartLayer = rememberGraphicsLayer()

    // Jour visé par le graphique : 0 = aujourd'hui, ± MAX_DAY_OFFSET par glissement du doigt.
    val today     = remember { LocalDate.now() }
    val dayOffset = remember { Animatable(0f) }

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text      = stringResource(R.string.module_biorhythm),
                        style     = MaterialTheme.typography.titleLarge,
                        color     = Or,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.width(6.dp))
                    ModuleInfoButton(onClick = {
                        navController.navigate(Screen.ModuleIntro.route(ModuleIntroIds.BIORHYTHM, "revisit"))
                    })
                }
                Spacer(Modifier.weight(1f))
                if (state is BiorhythmUiState.Ready) {
                    val ready = state as BiorhythmUiState.Ready
                    TextButton(onClick = {
                        val chartDate = today.plusDays(dayOffset.value.roundToLong())
                        val text = buildBiorhythmShareText(context, ready, chartDate)
                        scope.launch {
                            val bitmap = chartLayer.toImageBitmap().asAndroidBitmap()
                            sharePdf(context, "biorythmes", text, bitmap, maxImageHeight = 200f)
                        }
                    }) {
                        Image(
                            painter = painterResource(R.drawable.ic_share),
                            contentDescription = null,
                            modifier = Modifier.height(40.dp)
                        )
                    }
                } else {
                    Spacer(Modifier.width(64.dp))
                }
            }
            if (state is BiorhythmUiState.Ready) {
                Text(
                    text      = stringResource(R.string.reading_screen_subtitle, (state as BiorhythmUiState.Ready).firstName.split(" ", "-").first()),
                    style     = MaterialTheme.typography.labelMedium,
                    color     = BlancNacre.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )
            }

            when (state) {
                is BiorhythmUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Or)
                    }
                }
                is BiorhythmUiState.NoProfile -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text       = stringResource(R.string.biorhythm_no_profile),
                            style      = MaterialTheme.typography.bodyLarge,
                            color      = BlancNacre.copy(alpha = 0.70f),
                            textAlign  = TextAlign.Center,
                            lineHeight = 28.sp
                        )
                    }
                }
                is BiorhythmUiState.Ready -> {
                    val ready = state as BiorhythmUiState.Ready
                    BiorhythmContent(
                        ready      = ready,
                        chartLayer = chartLayer,
                        today      = today,
                        dayOffset  = dayOffset,
                        modifier   = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                    )
                }
            }
        }
    }
}

// ─── Contenu principal ─────────────────────────────────────────────────────────

@Composable
private fun BiorhythmContent(
    ready      : BiorhythmUiState.Ready,
    chartLayer : GraphicsLayer,
    today      : LocalDate,
    dayOffset  : Animatable<Float, *>,
    modifier   : Modifier = Modifier
) {
    val result = ready.result
    val synthesisText = ready.content.synthesisTexts[result.synthesisKey] ?: ""

    // Arrondi au jour : évite de recomposer le texte à chaque pixel du glissement.
    val shownDay by remember { derivedStateOf { dayOffset.value.roundToInt() } }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        SectionTitle(text = stringResource(R.string.biorhythm_synthesis_title))
        Spacer(Modifier.height(12.dp))
        BodyText(synthesisText)

        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(24.dp))

        Text(
            text      = if (shownDay == 0) stringResource(R.string.biorhythm_chart_today)
                        else formatChartDay(today.plusDays(shownDay.toLong())),
            style     = MaterialTheme.typography.labelMedium,
            color     = Or.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier.drawWithContent {
                chartLayer.record { this@drawWithContent.drawContent() }
                drawLayer(chartLayer)
            }
        ) {
            BiorhythmChart(
                daysElapsed = result.daysElapsed,
                dayOffset   = dayOffset,
                modifier    = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(BleuNuitClair.copy(alpha = 0.30f))
            )
        }
        Spacer(Modifier.height(16.dp))
        ChartLegend()

        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(24.dp))

        CycleBlock(
            title  = stringResource(R.string.biorhythm_cycle_physical),
            color  = PhysicalColor,
            result = result.physical,
            text   = ready.content.baseTexts["PHYSICAL_${result.physical.state}"]
        )
        Spacer(Modifier.height(24.dp))
        CycleBlock(
            title  = stringResource(R.string.biorhythm_cycle_emotional),
            color  = EmotionalColor,
            result = result.emotional,
            text   = ready.content.baseTexts["EMOTIONAL_${result.emotional.state}"]
        )
        Spacer(Modifier.height(24.dp))
        CycleBlock(
            title  = stringResource(R.string.biorhythm_cycle_intellectual),
            color  = IntellectualColor,
            result = result.intellectual,
            text   = ready.content.baseTexts["INTELLECTUAL_${result.intellectual.state}"]
        )

        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(20.dp))

        Text(
            text      = stringResource(R.string.biorhythm_disclaimer),
            style     = MaterialTheme.typography.labelSmall,
            color     = BlancNacre.copy(alpha = 0.40f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))
    }
}

// ─── Graphique des 3 courbes ────────────────────────────────────────────────────

private const val CHART_RANGE_DAYS = 15
private const val CHART_STEPS      = 120
private const val MAX_DAY_OFFSET   = 30f

@Composable
private fun BiorhythmChart(
    daysElapsed : Long,
    dayOffset   : Animatable<Float, *>,
    modifier    : Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    // Chevrons d'invitation au glissement : ils s'effacent au premier geste.
    var hintVisible by remember { mutableStateOf(true) }
    val hintAlpha = animateFloatAsState(
        targetValue   = if (hintVisible) 1f else 0f,
        animationSpec = tween(400),
        label         = "chartHintAlpha"
    )

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            // Un glissement de droite à gauche avance vers les jours à venir, l'inverse
            // remonte vers les jours passés ; au relâcher, on se cale sur le jour entier.
            detectHorizontalDragGestures(
                onDragEnd = {
                    scope.launch { dayOffset.animateTo(dayOffset.value.roundToInt().toFloat()) }
                }
            ) { change, dragAmount ->
                change.consume()
                hintVisible = false
                val pxPerDay = size.width / (2f * CHART_RANGE_DAYS)
                if (pxPerDay > 0f) {
                    val target = (dayOffset.value - dragAmount / pxPerDay)
                        .coerceIn(-MAX_DAY_OFFSET, MAX_DAY_OFFSET)
                    scope.launch { dayOffset.snapTo(target) }
                }
            }
        }
    ) {
        val width     = size.width
        val height    = size.height
        val centerY   = height / 2f
        val amplitude = height / 2f * 0.82f
        val todayX    = width / 2f
        val offset    = dayOffset.value

        drawLine(
            color       = BlancNacre.copy(alpha = 0.25f),
            start       = Offset(0f, centerY),
            end         = Offset(width, centerY),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color       = Or.copy(alpha = 0.45f),
            start       = Offset(todayX, 0f),
            end         = Offset(todayX, height),
            strokeWidth = 1.dp.toPx(),
            pathEffect  = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
        )

        // Repère discret d'aujourd'hui, visible seulement après s'en être éloigné.
        val todayMarkerX = todayX - offset * (width / (2f * CHART_RANGE_DAYS))
        if (abs(offset) >= 0.5f && todayMarkerX in 0f..width) {
            drawLine(
                color       = BlancNacre.copy(alpha = 0.30f),
                start       = Offset(todayMarkerX, 0f),
                end         = Offset(todayMarkerX, height),
                strokeWidth = 1.dp.toPx()
            )
        }

        fun drawCycle(cycle: BiorhythmCycle, color: Color) {
            val path = androidx.compose.ui.graphics.Path()
            for (i in 0..CHART_STEPS) {
                val offsetDays = -CHART_RANGE_DAYS + (2 * CHART_RANGE_DAYS * i / CHART_STEPS.toFloat())
                val value = sin(2 * PI * (daysElapsed + offset + offsetDays) / cycle.periodDays).toFloat()
                val x = width * i / CHART_STEPS.toFloat()
                val y = centerY - value * amplitude
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = color, style = Stroke(width = 2.5.dp.toPx()))
        }

        drawCycle(BiorhythmCycle.PHYSICAL, PhysicalColor)
        drawCycle(BiorhythmCycle.EMOTIONAL, EmotionalColor)
        drawCycle(BiorhythmCycle.INTELLECTUAL, IntellectualColor)

        listOf(
            BiorhythmCycle.PHYSICAL to PhysicalColor,
            BiorhythmCycle.EMOTIONAL to EmotionalColor,
            BiorhythmCycle.INTELLECTUAL to IntellectualColor
        ).forEach { (cycle, color) ->
            val value = sin(2 * PI * (daysElapsed + offset) / cycle.periodDays).toFloat()
            val y = centerY - value * amplitude
            drawCircle(color, radius = 4.dp.toPx(), center = Offset(todayX, y))
        }

        if (hintAlpha.value > 0.01f) {
            val hintColor  = Or.copy(alpha = 0.45f * hintAlpha.value)
            val hintStroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            val margin     = 10.dp.toPx()
            val armX       = 5.dp.toPx()
            val armY       = 7.dp.toPx()

            fun chevron(tipX: Float, armDirection: Float) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(tipX + armX * armDirection, centerY - armY)
                    lineTo(tipX, centerY)
                    lineTo(tipX + armX * armDirection, centerY + armY)
                }
                drawPath(path, color = hintColor, style = hintStroke)
            }

            chevron(tipX = margin, armDirection = 1f)
            chevron(tipX = width - margin, armDirection = -1f)
        }
    }
}

@Composable
private fun ChartLegend() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendDot(PhysicalColor, stringResource(R.string.biorhythm_cycle_physical))
        LegendDot(EmotionalColor, stringResource(R.string.biorhythm_cycle_emotional))
        LegendDot(IntellectualColor, stringResource(R.string.biorhythm_cycle_intellectual))
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = BlancNacre.copy(alpha = 0.70f)
        )
    }
}

@Composable
private fun CycleBlock(
    title  : String,
    color  : Color,
    result : BiorhythmCycleResult,
    text   : String?
) {
    val stateRes = when (result.state) {
        BiorhythmState.HIGH     -> R.string.biorhythm_state_high
        BiorhythmState.LOW      -> R.string.biorhythm_state_low
        BiorhythmState.CRITICAL -> R.string.biorhythm_state_critical
    }
    val percent = (result.value * 100).toInt()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text      = title.uppercase(),
                style     = MaterialTheme.typography.labelMedium,
                color     = BlancNacre.copy(alpha = 0.50f),
                letterSpacing = 3.sp
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text      = "${if (percent >= 0) "+" else ""}$percent%  •  ${stringResource(stateRes)}",
            style     = MaterialTheme.typography.titleMedium,
            color     = Or,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        BodyText(text)
    }
}

// ─── Composants internes ───────────────────────────────────────────────────────

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
private fun SectionTitle(text: String) {
    Text(
        text          = text.uppercase(),
        style         = MaterialTheme.typography.labelMedium,
        color         = BlancNacre.copy(alpha = 0.50f),
        letterSpacing = 4.sp,
        textAlign     = TextAlign.Center
    )
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 32.dp),
        color    = Or.copy(alpha = 0.20f)
    )
}

// ─── Dates ─────────────────────────────────────────────────────────────────────

/** Jour visé par le graphique, ex. « Vendredi 14 août ». */
private fun formatChartDay(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.getDefault()))
        .replaceFirstChar { it.uppercase() }

/** Même jour avec l'année, pour le PDF partagé. */
private fun formatChartDayFull(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.getDefault()))

// ─── Texte de partage ───────────────────────────────────────────────────────────

private fun buildBiorhythmShareText(
    context   : android.content.Context,
    ready     : BiorhythmUiState.Ready,
    chartDate : LocalDate
): String {
    val result = ready.result
    val sb = StringBuilder()
    sb.appendLine(context.getString(R.string.share_biorhythm_title, ready.firstName.split(" ", "-").first()))
    sb.appendLine()

    sb.appendLine(context.getString(R.string.biorhythm_synthesis_title).uppercase())
    sb.appendLine(ready.content.synthesisTexts[result.synthesisKey] ?: "")
    sb.appendLine()
    sb.appendLine(context.getString(R.string.share_biorhythm_chart_date, formatChartDayFull(chartDate)))
    sb.appendLine(com.francekim.empreintedelame.ui.common.PDF_IMAGE_MARKER)
    sb.appendLine()

    fun appendCycle(titleRes: Int, key: String, cycleResult: BiorhythmCycleResult, stateRes: Int) {
        val percent = (cycleResult.value * 100).toInt()
        sb.appendLine(context.getString(titleRes).uppercase())
        sb.appendLine("${if (percent >= 0) "+" else ""}$percent% — ${context.getString(stateRes)}")
        sb.appendLine(ready.content.baseTexts[key] ?: "")
        sb.appendLine()
    }

    appendCycle(R.string.biorhythm_cycle_physical, "PHYSICAL_${result.physical.state}", result.physical, stateResFor(result.physical.state))
    appendCycle(R.string.biorhythm_cycle_emotional, "EMOTIONAL_${result.emotional.state}", result.emotional, stateResFor(result.emotional.state))
    appendCycle(R.string.biorhythm_cycle_intellectual, "INTELLECTUAL_${result.intellectual.state}", result.intellectual, stateResFor(result.intellectual.state))

    sb.appendLine(context.getString(R.string.share_destiny_footer, context.getString(R.string.app_name)))

    return sb.toString().trim()
}

private fun stateResFor(state: BiorhythmState): Int = when (state) {
    BiorhythmState.HIGH     -> R.string.biorhythm_state_high
    BiorhythmState.LOW      -> R.string.biorhythm_state_low
    BiorhythmState.CRITICAL -> R.string.biorhythm_state_critical
}
