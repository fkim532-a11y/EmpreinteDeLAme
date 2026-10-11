package com.francekim.empreintedelame.ui.destiny

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.domain.calculator.DestinyProfile
import com.francekim.empreintedelame.domain.model.NumberInterpretation
import com.francekim.empreintedelame.ui.common.MasterNumberDisplay
import com.francekim.empreintedelame.ui.common.ModuleInfoButton
import com.francekim.empreintedelame.ui.common.masterReduction
import com.francekim.empreintedelame.ui.common.shareText
import com.francekim.empreintedelame.ui.moduleintro.ModuleIntroIds
import com.francekim.empreintedelame.ui.navigation.Screen
import com.francekim.empreintedelame.ui.theme.*

@Composable
fun DestinyScreen(navController: NavController) {
    val context = LocalContext.current
    val vm: DestinyViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )
    val state by vm.uiState.collectAsState()

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
            // ── Barre du haut ──────────────────────────────────────────────────
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
                        text      = stringResource(R.string.module_lifepath),
                        style     = MaterialTheme.typography.titleLarge,
                        color     = Or,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.width(6.dp))
                    ModuleInfoButton(onClick = {
                        navController.navigate(Screen.ModuleIntro.route(ModuleIntroIds.LIFEPATH, "revisit"))
                    })
                }
                Spacer(Modifier.weight(1f))
                if (state is DestinyUiState.Ready) {
                    val ready = state as DestinyUiState.Ready
                    TextButton(onClick = {
                        val text = buildDestinyShareText(
                            context         = context,
                            firstName       = ready.firstName,
                            profile         = ready.profile,
                            interpretations = ready.interpretations,
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
            if (state is DestinyUiState.Ready) {
                Text(
                    text      = stringResource(R.string.reading_screen_subtitle, (state as DestinyUiState.Ready).firstName.split(" ", "-").first()),
                    style     = MaterialTheme.typography.labelMedium,
                    color     = BlancNacre.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )
            }

            // ── Contenu selon l'état ───────────────────────────────────────────
            when (state) {
                is DestinyUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Or)
                    }
                }
                is DestinyUiState.NoProfile -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text       = stringResource(R.string.destiny_no_profile),
                            style      = MaterialTheme.typography.bodyLarge,
                            color      = BlancNacre.copy(alpha = 0.70f),
                            textAlign  = TextAlign.Center,
                            lineHeight = 28.sp
                        )
                    }
                }
                is DestinyUiState.Ready -> {
                    val ready = state as DestinyUiState.Ready
                    DestinyContent(
                        profile         = ready.profile,
                        interpretations = ready.interpretations,
                        eightSpiritual  = ready.eightSpiritual,
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
private fun DestinyContent(
    profile         : DestinyProfile,
    interpretations : Map<Int, NumberInterpretation>,
    eightSpiritual  : NumberInterpretation?,
    modifier        : Modifier = Modifier
) {
    val sommets = listOf(
        Triple(
            stringResource(R.string.destiny_summit_label, 1),
            profile.summit1,
            stringResource(R.string.destiny_summit_until, profile.ageSummit1)
        ),
        Triple(
            stringResource(R.string.destiny_summit_label, 2),
            profile.summit2,
            stringResource(R.string.destiny_summit_range, profile.ageSummit1 + 1, profile.ageSummit2)
        ),
        Triple(
            stringResource(R.string.destiny_summit_label, 3),
            profile.summit3,
            stringResource(R.string.destiny_summit_range, profile.ageSummit2 + 1, profile.ageSummit3)
        ),
        Triple(
            stringResource(R.string.destiny_summit_label, 4),
            profile.summit4,
            stringResource(R.string.destiny_summit_from, profile.ageSummit3 + 1)
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // ── Chemin de Vie ────────────────────────────────────────────────────
        SectionTitle(
            text    = stringResource(R.string.destiny_life_path_section),
            caption = stringResource(R.string.destiny_life_path_info)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text      = profile.lifePathDisplay,
            fontSize  = 80.sp,
            color     = if (masterReduction(profile.lifePathReduced) != null) VioletMoyen else Or,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text  = stringResource(R.string.destiny_life_path_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = BlancNacre.copy(alpha = 0.50f)
        )
        Spacer(Modifier.height(12.dp))
        EightMaterialLabel(profile.lifePathReduced, eightSpiritual)
        Text(
            text  = interpretations[profile.lifePathReduced]?.keyword ?: "",
            style = MaterialTheme.typography.titleMedium,
            color = Or.copy(alpha = 0.85f)
        )
        Text(
            text      = interpretations[profile.lifePathReduced]?.lifePath ?: "",
            style     = MaterialTheme.typography.bodySmall,
            color     = BlancNacre.copy(alpha = 0.70f),
            textAlign = TextAlign.Center
        )
        EightSpiritualBlock(profile.lifePathReduced, eightSpiritual, eightSpiritual?.lifePath)
        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        // ── Nombre de l'Ombre (+ Maturité si disponible) ─────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NumberCard(
                label   = stringResource(R.string.destiny_shadow_number),
                value   = profile.shadowNumber,
                keyword = interpretations[profile.shadowNumber]?.keyword,
                caption = stringResource(R.string.destiny_shadow_number_info)
            )
            if (profile.maturityNumber != null) {
                NumberCard(
                    label   = stringResource(R.string.destiny_maturity_number),
                    value   = profile.maturityNumber,
                    keyword = interpretations[profile.maturityNumber]?.keyword,
                    caption = stringResource(R.string.destiny_maturity_number_info)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        EightMaterialLabel(profile.shadowNumber, eightSpiritual)
        YearInterpretationText(interpretations[profile.shadowNumber]?.shadow)
        EightSpiritualBlock(profile.shadowNumber, eightSpiritual, eightSpiritual?.shadow)
        if (profile.maturityNumber != null) {
            Spacer(Modifier.height(12.dp))
            EightMaterialLabel(profile.maturityNumber, eightSpiritual)
            YearInterpretationText(interpretations[profile.maturityNumber]?.maturity)
            EightSpiritualBlock(profile.maturityNumber, eightSpiritual, eightSpiritual?.maturity)
        }

        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        // ── 4 Sommets ────────────────────────────────────────────────────────
        SectionTitle(
            text    = stringResource(R.string.destiny_summits_section),
            caption = stringResource(R.string.destiny_summits_info)
        )
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            sommets.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { (label, value, age) ->
                        SommetCard(
                            label          = label,
                            value          = value,
                            age            = age,
                            keyword        = interpretations[value]?.keyword,
                            eightSpiritual = eightSpiritual,
                            modifier       = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        // ── Année Personnelle ────────────────────────────────────────────────
        SectionTitle(stringResource(R.string.destiny_personal_year_section))
        Spacer(Modifier.height(20.dp))
        YearBlock(
            label          = stringResource(R.string.destiny_personal),
            value          = profile.personalYear,
            keyword        = interpretations[profile.personalYear]?.keyword,
            text           = interpretations[profile.personalYear]?.year,
            eightSpiritual = eightSpiritual,
            spiritualText  = eightSpiritual?.year,
            caption        = stringResource(R.string.destiny_personal_info)
        )
        Spacer(Modifier.height(24.dp))
        YearBlock(
            label          = stringResource(R.string.destiny_challenge),
            value          = profile.yearChallenge,
            keyword        = interpretations[profile.yearChallenge]?.keyword,
            text           = interpretations[profile.yearChallenge]?.yearChallenge,
            eightSpiritual = eightSpiritual,
            spiritualText  = eightSpiritual?.yearChallenge,
            caption        = stringResource(R.string.destiny_challenge_info)
        )

        Spacer(Modifier.height(40.dp))
    }
}

// ─── Composants internes ───────────────────────────────────────────────────────

@Composable
private fun SommetCard(
    label: String, value: Int, age: String, keyword: String?,
    eightSpiritual: NumberInterpretation?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(14.dp))
            .padding(vertical = 18.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Or.copy(alpha = 0.65f))
            Spacer(Modifier.height(6.dp))
            MasterNumberDisplay(number = value, fontSize = 32.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text       = age,
                style      = MaterialTheme.typography.labelSmall,
                color      = BlancNacre.copy(alpha = 0.50f),
                textAlign  = TextAlign.Center,
                lineHeight = 16.sp
            )
            if (!keyword.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text      = keyword,
                    style     = MaterialTheme.typography.labelSmall,
                    color     = Or.copy(alpha = 0.70f),
                    textAlign = TextAlign.Center
                )
            }
            if (value == 8 && eightSpiritual != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text       = "✨ ${eightSpiritual.keyword}",
                    fontSize   = 12.sp,
                    lineHeight = 15.sp,
                    color      = Color(0xFF6A1B9A),
                    textAlign  = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun YearBlock(
    label          : String,
    value          : Int,
    keyword        : String?,
    text           : String?,
    eightSpiritual : NumberInterpretation?,
    spiritualText  : String?,
    caption        : String? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text          = label.uppercase(),
            style         = MaterialTheme.typography.labelSmall,
            color         = BlancNacre.copy(alpha = 0.55f),
            letterSpacing = 1.5.sp,
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
        Spacer(Modifier.height(6.dp))
        MasterNumberDisplay(number = value, fontSize = 48.sp)
        Spacer(Modifier.height(8.dp))
        EightMaterialLabel(value, eightSpiritual)
        if (!keyword.isNullOrBlank()) {
            Text(
                text  = keyword,
                style = MaterialTheme.typography.titleMedium,
                color = Or.copy(alpha = 0.85f)
            )
        }
        YearInterpretationText(text)
        EightSpiritualBlock(value, eightSpiritual, spiritualText)
    }
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
private fun EightSpiritualBlock(
    value          : Int,
    eightSpiritual : NumberInterpretation?,
    spiritualText  : String?
) {
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
private fun NumberCard(
    label   : String,
    value   : Int,
    keyword : String? = null,
    caption : String? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        MasterNumberDisplay(number = value, fontSize = 36.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text      = label,
            style     = MaterialTheme.typography.labelSmall,
            color     = BlancNacre.copy(alpha = 0.55f),
            textAlign = TextAlign.Center
        )
        if (caption != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text      = caption,
                style     = MaterialTheme.typography.labelSmall,
                color     = Or.copy(alpha = 0.70f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(horizontal = 4.dp)
            )
        }
        if (!keyword.isNullOrBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text      = keyword,
                style     = MaterialTheme.typography.labelSmall,
                color     = Or.copy(alpha = 0.70f),
                textAlign = TextAlign.Center
            )
        }
    }
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

private fun appendReading(
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

private fun buildDestinyShareText(
    context         : android.content.Context,
    firstName       : String,
    profile         : DestinyProfile,
    interpretations : Map<Int, NumberInterpretation>,
    eightSpiritual  : NumberInterpretation?
): String {
    val sb = StringBuilder()
    sb.appendLine(context.getString(R.string.share_destiny_title, firstName.split(" ", "-").first()))
    sb.appendLine()

    sb.appendLine("🔮 " + context.getString(R.string.destiny_life_path_section).uppercase())
    appendReading(
        sb, context,
        label          = context.getString(R.string.destiny_life_path_section),
        displayValue   = profile.lifePathDisplay,
        lookupValue    = profile.lifePathReduced,
        keyword        = interpretations[profile.lifePathReduced]?.keyword,
        text           = interpretations[profile.lifePathReduced]?.lifePath,
        eightSpiritual = eightSpiritual,
        spiritualText  = eightSpiritual?.lifePath
    )

    appendReading(
        sb, context,
        label          = "🕳️ " + context.getString(R.string.destiny_shadow_number),
        displayValue   = profile.shadowNumber.toString(),
        lookupValue    = profile.shadowNumber,
        keyword        = interpretations[profile.shadowNumber]?.keyword,
        text           = interpretations[profile.shadowNumber]?.shadow,
        eightSpiritual = eightSpiritual,
        spiritualText  = eightSpiritual?.shadow
    )

    if (profile.maturityNumber != null) {
        appendReading(
            sb, context,
            label          = context.getString(R.string.destiny_maturity_number),
            displayValue   = profile.maturityNumber.toString(),
            lookupValue    = profile.maturityNumber,
            keyword        = interpretations[profile.maturityNumber]?.keyword,
            text           = interpretations[profile.maturityNumber]?.maturity,
            eightSpiritual = eightSpiritual,
            spiritualText  = eightSpiritual?.maturity
        )
    }

    sb.appendLine("⛰️ " + context.getString(R.string.destiny_summits_section).uppercase())
    val summits = listOf(
        Triple(profile.summit1, context.getString(R.string.destiny_summit_until, profile.ageSummit1), 1),
        Triple(profile.summit2, context.getString(R.string.destiny_summit_range, profile.ageSummit1 + 1, profile.ageSummit2), 2),
        Triple(profile.summit3, context.getString(R.string.destiny_summit_range, profile.ageSummit2 + 1, profile.ageSummit3), 3),
        Triple(profile.summit4, context.getString(R.string.destiny_summit_from, profile.ageSummit3 + 1), 4)
    )
    summits.forEach { (value, age, index) ->
        val label   = context.getString(R.string.destiny_summit_label, index)
        val keyword = interpretations[value]?.keyword
        sb.appendLine(
            if (!keyword.isNullOrBlank()) "$label ($age) : $value — $keyword" else "$label ($age) : $value"
        )
    }
    sb.appendLine()

    sb.appendLine("🌙 " + context.getString(R.string.destiny_personal_year_section).uppercase())
    appendReading(
        sb, context,
        label          = context.getString(R.string.destiny_personal),
        displayValue   = profile.personalYear.toString(),
        lookupValue    = profile.personalYear,
        keyword        = interpretations[profile.personalYear]?.keyword,
        text           = interpretations[profile.personalYear]?.year,
        eightSpiritual = eightSpiritual,
        spiritualText  = eightSpiritual?.year
    )

    appendReading(
        sb, context,
        label          = "⚡ " + context.getString(R.string.destiny_challenge),
        displayValue   = profile.yearChallenge.toString(),
        lookupValue    = profile.yearChallenge,
        keyword        = interpretations[profile.yearChallenge]?.keyword,
        text           = interpretations[profile.yearChallenge]?.yearChallenge,
        eightSpiritual = eightSpiritual,
        spiritualText  = eightSpiritual?.yearChallenge
    )

    sb.appendLine(context.getString(R.string.share_destiny_footer, context.getString(R.string.app_name)))

    return sb.toString().trim()
}
