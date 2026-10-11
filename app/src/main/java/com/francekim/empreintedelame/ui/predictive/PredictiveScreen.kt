package com.francekim.empreintedelame.ui.predictive

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.francekim.empreintedelame.domain.calculator.LetterTransit
import com.francekim.empreintedelame.domain.calculator.PredictiveProfile
import com.francekim.empreintedelame.domain.model.NumberInterpretation
import com.francekim.empreintedelame.ui.common.MasterNumberDisplay
import com.francekim.empreintedelame.ui.common.ModuleInfoButton
import com.francekim.empreintedelame.ui.common.shareText
import com.francekim.empreintedelame.ui.moduleintro.ModuleIntroIds
import com.francekim.empreintedelame.ui.navigation.Screen
import com.francekim.empreintedelame.ui.theme.*

// ─── Écran principal ──────────────────────────────────────────────────────────

@Composable
fun PredictiveScreen(navController: NavController) {
    val context = LocalContext.current
    val vm: PredictiveViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

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
                        text      = stringResource(R.string.module_predictive),
                        style     = MaterialTheme.typography.titleLarge,
                        color     = Or,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.width(6.dp))
                    ModuleInfoButton(onClick = {
                        navController.navigate(Screen.ModuleIntro.route(ModuleIntroIds.PREDICTIVE, "revisit"))
                    })
                }
                Spacer(Modifier.weight(1f))
                if (state is PredictiveUiState.Ready) {
                    val ready = state as PredictiveUiState.Ready
                    TextButton(onClick = {
                        val text = buildPredictiveShareText(
                            context         = context,
                            firstName       = ready.firstName,
                            profile         = ready.profile,
                            interpretations = ready.interpretations,
                            letterTransits  = ready.letterTransits,
                            eightSpiritual  = ready.eightSpiritual
                        )
                        shareText(context, text)
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
            if (state is PredictiveUiState.Ready) {
                Text(
                    text      = stringResource(R.string.reading_screen_subtitle, (state as PredictiveUiState.Ready).firstName.split(" ", "-").first()),
                    style     = MaterialTheme.typography.labelMedium,
                    color     = BlancNacre.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )
            }

            when (state) {
                is PredictiveUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Or)
                    }
                }
                is PredictiveUiState.NoProfile -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text       = stringResource(R.string.predictive_no_profile),
                            style      = MaterialTheme.typography.bodyLarge,
                            color      = BlancNacre.copy(alpha = 0.70f),
                            textAlign  = TextAlign.Center,
                            lineHeight = 28.sp
                        )
                    }
                }
                is PredictiveUiState.Ready -> {
                    val ready = state as PredictiveUiState.Ready
                    PredictiveContent(
                        profile         = ready.profile,
                        interpretations = ready.interpretations,
                        letterTransits  = ready.letterTransits,
                        eightSpiritual  = ready.eightSpiritual,
                        modifier        = Modifier
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
private fun PredictiveContent(
    profile         : PredictiveProfile,
    interpretations : Map<Int, NumberInterpretation>,
    letterTransits  : Map<Char, String>,
    eightSpiritual  : NumberInterpretation?,
    modifier        : Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // ── 1. Année Personnelle (héros) ──────────────────────────────────────
        SectionTitle(
            text    = stringResource(R.string.predictive_personal_year_section),
            caption = stringResource(R.string.destiny_personal_info)
        )
        Spacer(Modifier.height(8.dp))
        MasterNumberDisplay(number = profile.personalYear, fontSize = 80.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text  = interpretations[profile.personalYear]?.keyword ?: "",
            style = MaterialTheme.typography.titleMedium,
            color = Or.copy(alpha = 0.85f)
        )
        Text(
            text      = interpretations[profile.personalYear]?.year ?: "",
            style     = MaterialTheme.typography.bodySmall,
            color     = BlancNacre.copy(alpha = 0.50f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        // ── 2. Les 3 Trimestres ───────────────────────────────────────────────
        SectionTitle(
            text    = stringResource(R.string.predictive_trimestres_section),
            caption = stringResource(R.string.predictive_trimestres_info)
        )
        Spacer(Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)
        ) {
            TrimestreCard(
                label    = "T1",
                months   = stringResource(R.string.predictive_t1_months),
                number   = profile.trimestre1,
                keyword  = interpretations[profile.trimestre1]?.keyword ?: "",
                isActive = profile.currentTrimestre == 1,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            TrimestreCard(
                label    = "T2",
                months   = stringResource(R.string.predictive_t2_months),
                number   = profile.trimestre2,
                keyword  = interpretations[profile.trimestre2]?.keyword ?: "",
                isActive = profile.currentTrimestre == 2,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            TrimestreCard(
                label    = "T3",
                months   = stringResource(R.string.predictive_t3_months),
                number   = profile.trimestre3,
                keyword  = interpretations[profile.trimestre3]?.keyword ?: "",
                isActive = profile.currentTrimestre == 3,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
        Spacer(Modifier.height(16.dp))
        TrimestreText("T1", interpretations[profile.trimestre1]?.trimestre1)
        Spacer(Modifier.height(12.dp))
        TrimestreText("T2", interpretations[profile.trimestre2]?.trimestre2)
        Spacer(Modifier.height(12.dp))
        TrimestreText("T3", interpretations[profile.trimestre3]?.trimestre3)
        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        // ── 3. Mois Personnel ─────────────────────────────────────────────────
        SectionTitle(
            text    = stringResource(R.string.predictive_personal_month_section),
            caption = stringResource(R.string.predictive_personal_month_info)
        )
        Spacer(Modifier.height(8.dp))
        MasterNumberDisplay(number = profile.personalMonth, fontSize = 56.sp)
        Spacer(Modifier.height(4.dp))
        EightMaterialLabel(profile.personalMonth, eightSpiritual)
        Text(
            text  = interpretations[profile.personalMonth]?.keyword ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = Or.copy(alpha = 0.85f)
        )
        Text(
            text      = interpretations[profile.personalMonth]?.month ?: "",
            style     = MaterialTheme.typography.bodySmall,
            color     = BlancNacre.copy(alpha = 0.50f),
            textAlign = TextAlign.Center
        )
        EightSpiritualBlock(profile.personalMonth, eightSpiritual, eightSpiritual?.month)
        Spacer(Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)
        ) {
            CompanionCard(
                label    = stringResource(R.string.predictive_realization_label),
                subtitle = stringResource(R.string.predictive_realization_subtitle),
                number   = profile.monthlyRealization,
                keyword  = interpretations[profile.monthlyRealization]?.keyword ?: "",
                caption  = stringResource(R.string.predictive_realization_info),
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CompanionCard(
                label    = stringResource(R.string.predictive_challenge_label),
                subtitle = stringResource(R.string.predictive_challenge_subtitle),
                number   = profile.monthChallenge,
                keyword  = interpretations[profile.monthChallenge]?.keyword ?: "",
                caption  = stringResource(R.string.predictive_challenge_info),
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
        Spacer(Modifier.height(16.dp))
        EightMaterialLabel(profile.monthlyRealization, eightSpiritual)
        YearInterpretationText(interpretations[profile.monthlyRealization]?.realization)
        EightSpiritualBlock(profile.monthlyRealization, eightSpiritual, eightSpiritual?.realization)
        Spacer(Modifier.height(12.dp))
        EightMaterialLabel(profile.monthChallenge, eightSpiritual)
        YearInterpretationText(interpretations[profile.monthChallenge]?.monthChallenge)
        EightSpiritualBlock(profile.monthChallenge, eightSpiritual, eightSpiritual?.monthChallenge)
        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        // ── 4. Jour Personnel ─────────────────────────────────────────────────
        SectionTitle(
            text    = stringResource(R.string.predictive_personal_day_section),
            caption = stringResource(R.string.predictive_personal_day_info)
        )
        Spacer(Modifier.height(8.dp))
        MasterNumberDisplay(number = profile.personalDay, fontSize = 56.sp)
        Spacer(Modifier.height(4.dp))
        EightMaterialLabel(profile.personalDay, eightSpiritual)
        Text(
            text  = interpretations[profile.personalDay]?.keyword ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = Or.copy(alpha = 0.85f)
        )
        Text(
            text      = interpretations[profile.personalDay]?.day ?: "",
            style     = MaterialTheme.typography.bodySmall,
            color     = BlancNacre.copy(alpha = 0.50f),
            textAlign = TextAlign.Center
        )
        EightSpiritualBlock(profile.personalDay, eightSpiritual, eightSpiritual?.day)
        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        // ── 5. Transit de la Lettre ───────────────────────────────────────────
        LetterTransitSection(profile.letterTransit, letterTransits[profile.letterTransit.letter])
        Spacer(Modifier.height(24.dp))

        // ── Heures Miroirs ────────────────────────────────────────────────────
        Text(
            text          = stringResource(R.string.predictive_mirror_section).uppercase(),
            style         = MaterialTheme.typography.labelSmall,
            color         = BlancNacre.copy(alpha = 0.55f),
            letterSpacing = 3.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text      = stringResource(R.string.predictive_mirror_info),
            style     = MaterialTheme.typography.bodySmall,
            color     = Or.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(10.dp))
        if (profile.favoriteMirrorHours.isEmpty()) {
            Text(
                text  = stringResource(R.string.predictive_mirror_none),
                style = MaterialTheme.typography.bodySmall,
                color = BlancNacre.copy(alpha = 0.40f)
            )
        } else {
            MirrorHoursRow(profile.favoriteMirrorHours)
        }

        Spacer(Modifier.height(40.dp))
    }
}

// ─── Carte Trimestre ───────────────────────────────────────────────────────────

@Composable
private fun TrimestreCard(
    label    : String,
    months   : String,
    number   : Int,
    keyword  : String,
    isActive : Boolean,
    modifier : Modifier = Modifier
) {
    val borderColor = if (isActive) Or else Or.copy(alpha = 0.22f)
    val borderWidth = if (isActive) 1.2.dp else 0.6.dp
    val bgAlpha     = if (isActive) 0.12f else 0.05f

    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = bgAlpha), RoundedCornerShape(12.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 6.dp)
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isActive) Or else BlancNacre.copy(alpha = 0.50f)
            )
            Text(
                text      = months,
                style     = MaterialTheme.typography.labelSmall,
                color     = BlancNacre.copy(alpha = 0.40f),
                textAlign = TextAlign.Center
            )
            Box(
                modifier         = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                MasterNumberDisplay(number = number, fontSize = 36.sp)
            }
            Text(
                text      = keyword,
                style     = MaterialTheme.typography.labelSmall,
                color     = if (isActive) BlancNacre.copy(alpha = 0.75f) else BlancNacre.copy(alpha = 0.40f),
                textAlign = TextAlign.Center
            )
            if (isActive) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text          = stringResource(R.string.predictive_cycle_active),
                    style         = MaterialTheme.typography.labelSmall,
                    color         = Or,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ─── Carte Réalisation / Défi ─────────────────────────────────────────────────

@Composable
private fun CompanionCard(
    label    : String,
    subtitle : String,
    number   : Int,
    keyword  : String,
    caption  : String? = null,
    modifier : Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(12.dp))
            .border(0.8.dp, Or.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 14.dp, horizontal = 8.dp)
        ) {
            Text(
                text          = label.uppercase(),
                style         = MaterialTheme.typography.labelSmall,
                color         = BlancNacre.copy(alpha = 0.60f),
                letterSpacing = 1.5.sp,
                textAlign     = TextAlign.Center
            )
            if (caption != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text      = caption,
                    style     = MaterialTheme.typography.labelSmall,
                    color     = Or.copy(alpha = 0.70f),
                    textAlign = TextAlign.Center
                )
            }
            Box(
                modifier         = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                MasterNumberDisplay(number = number, fontSize = 40.sp)
            }
            Text(
                text      = keyword,
                style     = MaterialTheme.typography.labelSmall,
                color     = Or.copy(alpha = 0.70f),
                textAlign = TextAlign.Center
            )
            Text(
                text      = subtitle,
                style     = MaterialTheme.typography.labelSmall,
                color     = BlancNacre.copy(alpha = 0.40f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Textes complets ───────────────────────────────────────────────────────────

@Composable
private fun TrimestreText(label: String, text: String?) {
    if (text.isNullOrBlank()) return
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = Or.copy(alpha = 0.70f)
        )
        Text(
            text      = text,
            style     = MaterialTheme.typography.bodySmall,
            color     = BlancNacre.copy(alpha = 0.70f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun YearInterpretationText(text: String?) {
    if (text.isNullOrBlank()) return
    Text(
        text      = text,
        style     = MaterialTheme.typography.bodySmall,
        color     = BlancNacre.copy(alpha = 0.70f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun EightMaterialLabel(value: Int, eightSpiritual: NumberInterpretation?) {
    if (value != 8 || eightSpiritual == null) return
    Text(
        text          = stringResource(R.string.eight_reading_material),
        style         = MaterialTheme.typography.labelSmall,
        color         = Or,
        letterSpacing = 1.5.sp,
        textAlign     = TextAlign.Center
    )
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun EightSpiritualBlock(value: Int, eightSpiritual: NumberInterpretation?, spiritualText: String?) {
    if (value != 8 || eightSpiritual == null || spiritualText.isNullOrBlank()) return
    Spacer(Modifier.height(16.dp))
    Text(
        text          = stringResource(R.string.eight_reading_spiritual),
        style         = MaterialTheme.typography.labelSmall,
        color         = VioletMoyen,
        letterSpacing = 1.5.sp,
        textAlign     = TextAlign.Center
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text  = eightSpiritual.keyword,
        style = MaterialTheme.typography.titleMedium,
        color = VioletMoyen.copy(alpha = 0.85f)
    )
    Text(
        text      = spiritualText,
        style     = MaterialTheme.typography.bodySmall,
        color     = BlancNacre.copy(alpha = 0.70f),
        textAlign = TextAlign.Center
    )
}

// ─── Transit de la Lettre ─────────────────────────────────────────────────────

@Composable
private fun LetterTransitSection(transit: LetterTransit, transitText: String?) {
    SectionTitle(
        text    = stringResource(R.string.predictive_transit_section),
        caption = stringResource(R.string.predictive_transit_info)
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text      = stringResource(R.string.predictive_transit_subtitle),
        style     = MaterialTheme.typography.bodySmall,
        color     = BlancNacre.copy(alpha = 0.50f),
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(16.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(14.dp))
            .border(0.8.dp, Or.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
            .padding(vertical = 20.dp, horizontal = 24.dp)
    ) {
        Text(
            text     = transit.letter.toString(),
            fontSize = 72.sp,
            color    = Or
        )
        Spacer(Modifier.width(24.dp))
        Column {
            Text(
                text  = stringResource(R.string.predictive_transit_value, transit.letterValue),
                style = MaterialTheme.typography.bodyMedium,
                color = BlancNacre.copy(alpha = 0.75f)
            )
        }
    }
    if (!transitText.isNullOrBlank()) {
        Spacer(Modifier.height(16.dp))
        Text(
            text      = transitText,
            style     = MaterialTheme.typography.bodySmall,
            color     = BlancNacre.copy(alpha = 0.70f),
            textAlign = TextAlign.Center
        )
    }
}

// ─── Heures Miroirs ────────────────────────────────────────────────────────────

@Composable
private fun MirrorHoursRow(hours: List<Int>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        hours.forEach { h ->
            Box(
                modifier = Modifier
                    .background(VioletMoyen.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                    .border(0.8.dp, VioletMoyen.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = "%02d:xx".format(h),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VioletMoyen
                )
            }
        }
    }
}

// ─── Composants communs ───────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String, caption: String? = null) {
    Text(
        text          = text.uppercase(),
        style         = MaterialTheme.typography.labelMedium,
        color         = BlancNacre,
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

private fun appendPredictiveReading(
    sb             : StringBuilder,
    context        : android.content.Context,
    label          : String,
    displayValue   : String,
    lookupValue    : Int,
    keyword        : String?,
    text           : String?,
    eightSpiritual : NumberInterpretation?,
    spiritualText  : String?
) {
    if (lookupValue == 8 && eightSpiritual != null) {
        sb.appendLine(context.getString(R.string.eight_reading_material))
    }
    sb.appendLine(
        if (!keyword.isNullOrBlank()) "$label : $displayValue — $keyword" else "$label : $displayValue"
    )
    if (!text.isNullOrBlank()) sb.appendLine(text)
    if (lookupValue == 8 && eightSpiritual != null && !spiritualText.isNullOrBlank()) {
        sb.appendLine("${context.getString(R.string.eight_reading_spiritual)} : ${eightSpiritual.keyword}")
        sb.appendLine(spiritualText)
    }
    sb.appendLine()
}

private fun buildPredictiveShareText(
    context         : android.content.Context,
    firstName       : String,
    profile         : PredictiveProfile,
    interpretations : Map<Int, NumberInterpretation>,
    letterTransits  : Map<Char, String>,
    eightSpiritual  : NumberInterpretation?
): String {
    val sb = StringBuilder()
    sb.appendLine(context.getString(R.string.share_predictive_title, firstName.split(" ", "-").first()))
    sb.appendLine()

    sb.appendLine("🌙 " + context.getString(R.string.predictive_personal_year_section).uppercase())
    appendPredictiveReading(
        sb, context,
        label          = context.getString(R.string.predictive_personal_year_section),
        displayValue   = profile.personalYear.toString(),
        lookupValue    = profile.personalYear,
        keyword        = interpretations[profile.personalYear]?.keyword,
        text           = interpretations[profile.personalYear]?.year,
        eightSpiritual = eightSpiritual,
        spiritualText  = eightSpiritual?.year
    )

    sb.appendLine("📅 " + context.getString(R.string.predictive_trimestres_section).uppercase())
    val trimestres = listOf(
        Triple(1, profile.trimestre1, interpretations[profile.trimestre1]?.trimestre1),
        Triple(2, profile.trimestre2, interpretations[profile.trimestre2]?.trimestre2),
        Triple(3, profile.trimestre3, interpretations[profile.trimestre3]?.trimestre3)
    ).filter { (index, _, _) -> index >= profile.currentTrimestre }
    trimestres.forEach { (index, value, _) ->
        val keyword = interpretations[value]?.keyword
        sb.appendLine(if (!keyword.isNullOrBlank()) "T$index : $value — $keyword" else "T$index : $value")
    }
    sb.appendLine()
    trimestres.forEach { (index, _, text) ->
        if (!text.isNullOrBlank()) { sb.appendLine("T$index"); sb.appendLine(text); sb.appendLine() }
    }

    sb.appendLine("🌗 " + context.getString(R.string.predictive_personal_month_section).uppercase())
    appendPredictiveReading(
        sb, context,
        label          = context.getString(R.string.predictive_personal_month_section),
        displayValue   = profile.personalMonth.toString(),
        lookupValue    = profile.personalMonth,
        keyword        = interpretations[profile.personalMonth]?.keyword,
        text           = interpretations[profile.personalMonth]?.month,
        eightSpiritual = eightSpiritual,
        spiritualText  = eightSpiritual?.month
    )
    appendPredictiveReading(
        sb, context,
        label          = "🛠️ " + context.getString(R.string.predictive_realization_label),
        displayValue   = profile.monthlyRealization.toString(),
        lookupValue    = profile.monthlyRealization,
        keyword        = interpretations[profile.monthlyRealization]?.keyword,
        text           = interpretations[profile.monthlyRealization]?.realization,
        eightSpiritual = eightSpiritual,
        spiritualText  = eightSpiritual?.realization
    )
    appendPredictiveReading(
        sb, context,
        label          = "⚡ " + context.getString(R.string.predictive_challenge_label),
        displayValue   = profile.monthChallenge.toString(),
        lookupValue    = profile.monthChallenge,
        keyword        = interpretations[profile.monthChallenge]?.keyword,
        text           = interpretations[profile.monthChallenge]?.monthChallenge,
        eightSpiritual = eightSpiritual,
        spiritualText  = eightSpiritual?.monthChallenge
    )

    sb.appendLine("☀️ " + context.getString(R.string.predictive_personal_day_section).uppercase())
    appendPredictiveReading(
        sb, context,
        label          = context.getString(R.string.predictive_personal_day_section),
        displayValue   = profile.personalDay.toString(),
        lookupValue    = profile.personalDay,
        keyword        = interpretations[profile.personalDay]?.keyword,
        text           = interpretations[profile.personalDay]?.day,
        eightSpiritual = eightSpiritual,
        spiritualText  = eightSpiritual?.day
    )

    sb.appendLine("🔤 " + context.getString(R.string.predictive_transit_section).uppercase())
    val transit = profile.letterTransit
    sb.appendLine("${transit.letter} — " + context.getString(R.string.predictive_transit_value, transit.letterValue))
    letterTransits[transit.letter]?.let { if (it.isNotBlank()) sb.appendLine(it) }
    sb.appendLine()

    sb.appendLine("🪞 " + context.getString(R.string.predictive_mirror_section).uppercase())
    if (profile.favoriteMirrorHours.isEmpty()) {
        sb.appendLine(context.getString(R.string.predictive_mirror_none))
    } else {
        sb.appendLine(profile.favoriteMirrorHours.joinToString(", ") { "%02d:xx".format(it) })
    }
    sb.appendLine()

    sb.appendLine(context.getString(R.string.share_destiny_footer, context.getString(R.string.app_name)))

    return sb.toString().trim()
}
