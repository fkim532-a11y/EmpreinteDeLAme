package com.francekim.empreintedelame.ui.hdpredictions

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
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
import com.francekim.empreintedelame.data.model.HdLifeCycleInfo
import com.francekim.empreintedelame.domain.calculator.humandesign.HdLifeCycle
import com.francekim.empreintedelame.ui.common.PDF_IMAGE_MARKER
import com.francekim.empreintedelame.ui.common.ModuleInfoButton
import com.francekim.empreintedelame.ui.common.sharePdf
import com.francekim.empreintedelame.ui.moduleintro.ModuleIntroIds
import com.francekim.empreintedelame.ui.navigation.Screen
import com.francekim.empreintedelame.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HdPredictionsScreen(navController: NavController) {
    val context = LocalContext.current
    val vm: HdPredictionsViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val hexagramLayer = rememberGraphicsLayer()

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
                        text      = stringResource(R.string.module_hd_predictions),
                        style     = MaterialTheme.typography.titleLarge,
                        color     = Or,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.width(6.dp))
                    ModuleInfoButton(onClick = {
                        navController.navigate(Screen.ModuleIntro.route(ModuleIntroIds.HD_PREDICTIONS, "revisit"))
                    })
                }
                Spacer(Modifier.weight(1f))
                if (state is HdPredictionsUiState.Ready) {
                    val ready = state as HdPredictionsUiState.Ready
                    TextButton(onClick = {
                        val text = buildHdPredictionsShareText(
                            context   = context,
                            firstName = ready.firstName,
                            ready     = ready
                        )
                        scope.launch {
                            val bitmap = hexagramLayer.toImageBitmap().asAndroidBitmap()
                            sharePdf(context, "hd_predictions", text, bitmap, maxImageHeight = 160f)
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
            if (state is HdPredictionsUiState.Ready) {
                Text(
                    text      = stringResource(R.string.reading_screen_subtitle, (state as HdPredictionsUiState.Ready).firstName.split(" ", "-").first()),
                    style     = MaterialTheme.typography.labelMedium,
                    color     = BlancNacre.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )
            }

            when (state) {
                is HdPredictionsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Or)
                    }
                }
                is HdPredictionsUiState.NoProfile -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text       = stringResource(R.string.hd_predictions_no_profile),
                            style      = MaterialTheme.typography.bodyLarge,
                            color      = BlancNacre.copy(alpha = 0.70f),
                            textAlign  = TextAlign.Center,
                            lineHeight = 28.sp
                        )
                    }
                }
                is HdPredictionsUiState.Ready -> {
                    val ready = state as HdPredictionsUiState.Ready
                    HdPredictionsContent(
                        ready         = ready,
                        hexagramLayer = hexagramLayer,
                        modifier = Modifier
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
private fun HdPredictionsContent(
    ready         : HdPredictionsUiState.Ready,
    hexagramLayer : GraphicsLayer,
    modifier      : Modifier = Modifier
) {
    val gateReading = ready.content.dailyGates[ready.dailyGate.gate]
    val gateName    = ready.gateInfo[ready.dailyGate.gate]?.name ?: ""
    val yearInfo    = ready.content.personalYears[ready.personalYear]
    val monthInfo   = ready.content.personalMonths[ready.personalMonth]

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text  = stringResource(R.string.hd_predictions_subtitle),
            style = MaterialTheme.typography.labelMedium,
            color = BlancNacre.copy(alpha = 0.55f)
        )
        Spacer(Modifier.height(28.dp))

        // ── Porte du Jour ────────────────────────────────────────────────────
        SectionTitle(
            text    = stringResource(R.string.hd_predictions_gate_section),
            caption = stringResource(R.string.hd_predictions_gate_info)
        )
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier.drawWithContent {
                hexagramLayer.record { this@drawWithContent.drawContent() }
                drawLayer(hexagramLayer)
            }
        ) {
            YiKingHexagram(lines = ready.dailyGate.hexagramLines)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text      = "${ready.dailyGate.gate} — $gateName",
            style     = MaterialTheme.typography.titleMedium,
            color     = Or,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text      = gateReading?.yiKingName ?: "",
            style     = MaterialTheme.typography.labelMedium,
            color     = VioletMoyen.copy(alpha = 0.90f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text      = stringResource(R.string.hd_predictions_gate_line, ready.dailyGate.line),
            style     = MaterialTheme.typography.labelSmall,
            color     = BlancNacre.copy(alpha = 0.50f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text      = stringResource(R.string.hd_predictions_gate_line_info),
            style     = MaterialTheme.typography.labelSmall,
            color     = Or.copy(alpha = 0.70f),
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(12.dp))
        BodyText(gateReading?.description)
        Spacer(Modifier.height(8.dp))
        BodyText(gateReading?.invitation)

        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        // ── Mois HD Personnel ────────────────────────────────────────────────
        SectionTitle(
            text    = stringResource(R.string.hd_predictions_month_section),
            caption = stringResource(R.string.hd_predictions_month_info)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = "${ready.personalMonth} — ${monthInfo?.theme ?: ""}",
            style     = MaterialTheme.typography.titleMedium,
            color     = Or,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text      = monthInfo?.hdFocus ?: "",
            style     = MaterialTheme.typography.labelMedium,
            color     = VioletMoyen.copy(alpha = 0.80f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        BodyText(monthInfo?.description)
        Spacer(Modifier.height(8.dp))
        BodyText(monthInfo?.practicalTip)

        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        // ── Année HD Personnelle ─────────────────────────────────────────────
        SectionTitle(
            text    = stringResource(R.string.hd_predictions_year_section),
            caption = stringResource(R.string.hd_predictions_year_info)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = "${ready.personalYear} — ${yearInfo?.theme ?: ""}",
            style     = MaterialTheme.typography.titleMedium,
            color     = Or,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text      = yearInfo?.hdTheme ?: "",
            style     = MaterialTheme.typography.labelMedium,
            color     = VioletMoyen.copy(alpha = 0.80f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        BodyText(yearInfo?.description)
        Spacer(Modifier.height(8.dp))
        BodyText(yearInfo?.strategy)
        Spacer(Modifier.height(8.dp))
        BodyText(yearInfo?.affirmation)

        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        // ── Cycles de Vie ─────────────────────────────────────────────────────
        SectionTitle(
            text    = stringResource(R.string.hd_predictions_cycles_section),
            caption = stringResource(R.string.hd_predictions_cycles_info)
        )
        Spacer(Modifier.height(16.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            (ready.activeCycles + listOfNotNull(ready.nextCycle)).forEach { cycle ->
                LifeCycleBlock(cycle = cycle, content = ready.content.cycles[cycle.id.name])
            }
        }

        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(20.dp))

        Text(
            text      = stringResource(R.string.hd_predictions_disclaimer),
            style     = MaterialTheme.typography.labelSmall,
            color     = BlancNacre.copy(alpha = 0.40f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun LifeCycleBlock(
    cycle  : HdLifeCycle,
    content: HdLifeCycleInfo?
) {
    val statusRes = when {
        cycle.isActive -> R.string.hd_predictions_cycle_active
        cycle.isPast   -> R.string.hd_predictions_cycle_past
        else           -> R.string.hd_predictions_cycle_upcoming
    }
    val statusColor = if (cycle.isActive) Or else VioletMoyen.copy(alpha = 0.80f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text      = content?.name ?: "",
            style     = MaterialTheme.typography.titleSmall,
            color     = Or.copy(alpha = 0.90f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text      = stringResource(R.string.hd_predictions_age_range, cycle.ageMin, cycle.ageMax) +
                        "  •  " + stringResource(statusRes),
            style     = MaterialTheme.typography.labelSmall,
            color     = statusColor,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        BodyText(content?.theme)
        Spacer(Modifier.height(6.dp))
        BodyText(content?.description)
        Spacer(Modifier.height(6.dp))
        BodyText(content?.invitation)
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
private fun SectionTitle(text: String, caption: String? = null) {
    Text(
        text          = text.uppercase(),
        style         = MaterialTheme.typography.labelMedium,
        color         = BlancNacre.copy(alpha = 0.50f),
        letterSpacing = 4.sp,
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

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 32.dp),
        color    = Or.copy(alpha = 0.20f)
    )
}

// ─── Texte de partage ───────────────────────────────────────────────────────────

private fun buildHdPredictionsShareText(
    context   : android.content.Context,
    firstName : String,
    ready     : HdPredictionsUiState.Ready
): String {
    val gateReading = ready.content.dailyGates[ready.dailyGate.gate]
    val gateName    = ready.gateInfo[ready.dailyGate.gate]?.name ?: ""
    val yearInfo    = ready.content.personalYears[ready.personalYear]
    val monthInfo   = ready.content.personalMonths[ready.personalMonth]

    val sb = StringBuilder()
    sb.appendLine(context.getString(R.string.share_hd_predictions_title, firstName.split(" ", "-").first()))
    sb.appendLine()

    sb.appendLine("🔮 " + context.getString(R.string.hd_predictions_gate_section).uppercase())
    sb.appendLine(PDF_IMAGE_MARKER)
    sb.appendLine("${ready.dailyGate.gate} — $gateName")
    if (!gateReading?.yiKingName.isNullOrBlank()) sb.appendLine(gateReading?.yiKingName)
    sb.appendLine(context.getString(R.string.hd_predictions_gate_line, ready.dailyGate.line))
    if (!gateReading?.description.isNullOrBlank()) sb.appendLine(gateReading?.description)
    if (!gateReading?.invitation.isNullOrBlank()) sb.appendLine(gateReading?.invitation)
    sb.appendLine()

    sb.appendLine("🌙 " + context.getString(R.string.hd_predictions_month_section).uppercase())
    sb.appendLine("${ready.personalMonth} — ${monthInfo?.theme ?: ""}")
    if (!monthInfo?.hdFocus.isNullOrBlank()) sb.appendLine(monthInfo?.hdFocus)
    if (!monthInfo?.description.isNullOrBlank()) sb.appendLine(monthInfo?.description)
    if (!monthInfo?.practicalTip.isNullOrBlank()) sb.appendLine(monthInfo?.practicalTip)
    sb.appendLine()

    sb.appendLine("⭐ " + context.getString(R.string.hd_predictions_year_section).uppercase())
    sb.appendLine("${ready.personalYear} — ${yearInfo?.theme ?: ""}")
    if (!yearInfo?.hdTheme.isNullOrBlank()) sb.appendLine(yearInfo?.hdTheme)
    if (!yearInfo?.description.isNullOrBlank()) sb.appendLine(yearInfo?.description)
    if (!yearInfo?.strategy.isNullOrBlank()) sb.appendLine(yearInfo?.strategy)
    if (!yearInfo?.affirmation.isNullOrBlank()) sb.appendLine(yearInfo?.affirmation)
    sb.appendLine()

    sb.appendLine("🔄 " + context.getString(R.string.hd_predictions_cycles_section).uppercase())
    sb.appendLine()
    (ready.activeCycles + listOfNotNull(ready.nextCycle)).forEach { cycle ->
        val info = ready.content.cycles[cycle.id.name]
        val statusRes = when {
            cycle.isActive -> R.string.hd_predictions_cycle_active
            cycle.isPast   -> R.string.hd_predictions_cycle_past
            else           -> R.string.hd_predictions_cycle_upcoming
        }
        val ageRange = context.getString(R.string.hd_predictions_age_range, cycle.ageMin, cycle.ageMax)
        sb.appendLine("${info?.name ?: ""} ($ageRange • ${context.getString(statusRes)})")
        if (!info?.theme.isNullOrBlank()) sb.appendLine(info?.theme)
        if (!info?.description.isNullOrBlank()) sb.appendLine(info?.description)
        if (!info?.invitation.isNullOrBlank()) sb.appendLine(info?.invitation)
        sb.appendLine()
    }

    sb.appendLine(context.getString(R.string.share_destiny_footer, context.getString(R.string.app_name)))

    return sb.toString().trim()
}
