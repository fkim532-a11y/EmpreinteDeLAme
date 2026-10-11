package com.francekim.empreintedelame.ui.compatibility

import android.app.Application
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.data.model.UserProfile
import com.francekim.empreintedelame.domain.calculator.CompatibilityCenterResult
import com.francekim.empreintedelame.domain.model.CompatibilityEntry
import com.francekim.empreintedelame.domain.model.CompatibilityNature
import com.francekim.empreintedelame.ui.common.MasterNumberDisplay
import com.francekim.empreintedelame.ui.common.PDF_SECTION_MARKER
import com.francekim.empreintedelame.ui.common.sharePdf
import com.francekim.empreintedelame.ui.humandesign.centerLabelRes
import com.francekim.empreintedelame.ui.navigation.Screen
import com.francekim.empreintedelame.ui.theme.BlancNacre
import com.francekim.empreintedelame.ui.theme.BleuNuitClair
import com.francekim.empreintedelame.ui.theme.BleuNuitSombre
import com.francekim.empreintedelame.ui.theme.Or
import com.francekim.empreintedelame.ui.theme.VioletMoyen

private enum class CompatTab(val labelRes: Int) {
    LOVE(R.string.compatibility_tab_love),
    FRIENDSHIP(R.string.compatibility_tab_friendship),
    FAMILY(R.string.compatibility_tab_family),
    WORK(R.string.compatibility_tab_work),
    CHEMISTRY(R.string.compatibility_tab_chemistry),
    HUMAN_DESIGN(R.string.compatibility_tab_human_design)
}

// Ne garde que le premier prénom (le champ peut contenir plusieurs prénoms séparés par un espace),
// sans casser un prénom composé par un tiret (ex. "Jean-Pierre" reste entier).
private fun displayFirstName(firstName: String): String =
    firstName.trim().split(" ").firstOrNull { it.isNotBlank() } ?: firstName

private fun scoreTextRes(score: Int): Int = when {
    score >= 85 -> R.string.compatibility_score_text_tier1
    score >= 70 -> R.string.compatibility_score_text_tier2
    score >= 50 -> R.string.compatibility_score_text_tier3
    score >= 30 -> R.string.compatibility_score_text_tier4
    else         -> R.string.compatibility_score_text_tier5
}

@Composable
fun CompatibilityScreen(navController: NavController) {
    val context = LocalContext.current
    val vm: CompatibilityViewModel = viewModel(
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
            ) {
                TextButton(onClick = {
                    val s = state
                    if (s is CompatibilityUiState.Ready && s.canChangeSelection) {
                        vm.backToSelection()
                    } else if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        navController.popBackStack()
                    }
                }) {
                    Text("←", color = Or, style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text      = stringResource(R.string.module_compatibility),
                    style     = MaterialTheme.typography.titleLarge,
                    color     = Or,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.weight(1f))
                val readyState = state
                if (readyState is CompatibilityUiState.Ready) {
                    TextButton(onClick = {
                        val text = buildCompatibilityShareText(context, readyState)
                        sharePdf(context, "compatibilite", text)
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

            when (val s = state) {
                is CompatibilityUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Or)
                    }
                }
                is CompatibilityUiState.NeedProfiles -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_module_compatibility),
                            contentDescription = null,
                            modifier = Modifier.size(140.dp)
                        )
                        Spacer(Modifier.height(28.dp))
                        Text(
                            text       = stringResource(
                                if (s.availableCount == 0) R.string.compatibility_need_profiles_zero
                                else R.string.compatibility_need_profiles_one
                            ),
                            style      = MaterialTheme.typography.bodyLarge,
                            color      = BlancNacre.copy(alpha = 0.70f),
                            textAlign  = TextAlign.Center,
                            lineHeight = 28.sp,
                            modifier   = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(Modifier.height(24.dp))
                        Image(
                            painter = painterResource(R.drawable.ic_nav_moi),
                            contentDescription = stringResource(R.string.nav_me),
                            modifier = Modifier
                                .size(60.dp)
                                .clickable {
                                    navController.navigate(Screen.Me.route(s.availableCount + 1))
                                }
                        )
                    }
                }
                is CompatibilityUiState.NeedSelection -> {
                    ProfileSelectionContent(profiles = s.profiles, onPairSelected = vm::selectPair)
                }
                is CompatibilityUiState.Ready -> {
                    CompatibilityResultContent(state = s)
                }
            }
        }
    }
}

@Composable
private fun ProfileSelectionContent(profiles: List<UserProfile>, onPairSelected: (UserProfile, UserProfile) -> Unit) {
    var selected by remember { mutableStateOf<List<UserProfile>>(emptyList()) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text      = stringResource(R.string.compatibility_choose_pair),
            style     = MaterialTheme.typography.bodyLarge,
            color     = BlancNacre.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        profiles.forEach { profile ->
            val isSelected = profile in selected
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) Or.copy(alpha = 0.85f) else BleuNuitClair.copy(alpha = 0.35f))
                    .border(1.5.dp, Or.copy(alpha = 0.70f), RoundedCornerShape(50))
                    .clickable {
                        selected = when {
                            isSelected -> selected - profile
                            selected.size < 2 -> selected + profile
                            else -> listOf(selected[1], profile)
                        }
                        if (selected.size == 2) {
                            onPairSelected(selected[0], selected[1])
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = displayFirstName(profile.firstName),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) BleuNuitSombre else Or
                )
            }
        }
    }
}

@Composable
private fun CompatibilityResultContent(state: CompatibilityUiState.Ready) {
    var currentTab by remember { mutableStateOf(CompatTab.LOVE) }
    var scoreRevealed by remember { mutableStateOf(false) }
    val result = state.result
    val scoreAlpha by animateFloatAsState(
        targetValue = if (scoreRevealed) 1f else 0f,
        animationSpec = tween(2200),
        label = "compatibilityScoreAlpha"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text      = "${displayFirstName(state.profileA.firstName)} & ${displayFirstName(state.profileB.firstName)}",
            style     = MaterialTheme.typography.titleMedium,
            color     = BlancNacre.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        CompatibilityArrivalAnimation(score = result.globalScore) { scoreRevealed = true }
        Spacer(Modifier.height(8.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().alpha(scoreAlpha)
        ) {
            MasterNumberDisplay(number = result.globalScore, fontSize = 56.sp)
            Text(
                text  = stringResource(R.string.compatibility_global_score_label),
                style = MaterialTheme.typography.labelMedium,
                color = BlancNacre.copy(alpha = 0.55f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text      = stringResource(scoreTextRes(result.globalScore)),
                style     = MaterialTheme.typography.bodySmall,
                color     = BlancNacre.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(horizontal = 32.dp)
            )
        }
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CompatTab.entries.forEach { tab ->
                val isSelected = tab == currentTab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) Or else Color.Transparent)
                        .border(1.dp, Or.copy(alpha = 0.60f), RoundedCornerShape(50))
                        .clickable { currentTab = tab }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text  = stringResource(tab.labelRes),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) BleuNuitSombre else Or
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            when (currentTab) {
                CompatTab.HUMAN_DESIGN -> HumanDesignDetailTab(result.hdTypeEntry, result.centerResults, result.hasHumanDesign)
                else                    -> DomainTabContent(currentTab, result.lifePathEntry, result.hdTypeEntry, result.hasHumanDesign)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DomainTabContent(
    tab: CompatTab,
    lifePathEntry: CompatibilityEntry?,
    hdTypeEntry: CompatibilityEntry?,
    hasHumanDesign: Boolean
) {
    fun textFor(entry: CompatibilityEntry?): String? = when (tab) {
        CompatTab.LOVE       -> entry?.love
        CompatTab.FRIENDSHIP -> entry?.friendship
        CompatTab.FAMILY     -> entry?.family
        CompatTab.WORK       -> entry?.work
        CompatTab.CHEMISTRY  -> entry?.chemistry
        else                  -> null
    }

    Spacer(Modifier.height(8.dp))
    SectionLabel(stringResource(R.string.compatibility_lifepath_section))
    Spacer(Modifier.height(8.dp))
    BodyText(textFor(lifePathEntry))

    Spacer(Modifier.height(24.dp))
    SectionDivider()
    Spacer(Modifier.height(24.dp))

    SectionLabel(stringResource(R.string.compatibility_hd_type_section))
    Spacer(Modifier.height(8.dp))
    if (hasHumanDesign) {
        BodyText(textFor(hdTypeEntry))
    } else {
        BodyText(stringResource(R.string.compatibility_no_hd_message))
    }
}

@Composable
private fun HumanDesignDetailTab(
    typeEntry: CompatibilityEntry?,
    centerResults: List<CompatibilityCenterResult>,
    hasHumanDesign: Boolean
) {
    if (!hasHumanDesign) {
        Spacer(Modifier.height(8.dp))
        BodyText(stringResource(R.string.compatibility_no_hd_message))
        return
    }
    Spacer(Modifier.height(8.dp))
    if (typeEntry != null) {
        NatureBadge(typeEntry.nature, typeEntry.score)
        Spacer(Modifier.height(20.dp))
        LabeledText(stringResource(R.string.compatibility_tab_love), typeEntry.love)
        LabeledText(stringResource(R.string.compatibility_tab_friendship), typeEntry.friendship)
        LabeledText(stringResource(R.string.compatibility_tab_family), typeEntry.family)
        LabeledText(stringResource(R.string.compatibility_tab_work), typeEntry.work)
        LabeledText(stringResource(R.string.compatibility_tab_chemistry), typeEntry.chemistry)
    }

    Spacer(Modifier.height(24.dp))
    SectionDivider()
    Spacer(Modifier.height(24.dp))
    SectionLabel(stringResource(R.string.compatibility_hd_centers_section))
    Spacer(Modifier.height(12.dp))
    centerResults.forEach { centerResult ->
        CenterRow(centerResult)
        Spacer(Modifier.height(14.dp))
    }
}

@Composable
private fun CenterRow(result: CompatibilityCenterResult) {
    val definedLabel = stringResource(R.string.human_design_center_defined)
    val undefinedLabel = stringResource(R.string.human_design_center_undefined)
    val labelA = if (result.definedA) definedLabel else undefinedLabel
    val labelB = if (result.definedB) definedLabel else undefinedLabel

    Column {
        Text(
            text  = stringResource(centerLabelRes(result.center)),
            style = MaterialTheme.typography.titleSmall,
            color = Or.copy(alpha = 0.85f)
        )
        Text(
            text  = "$labelA · $labelB",
            style = MaterialTheme.typography.labelSmall,
            color = BlancNacre.copy(alpha = 0.55f)
        )
        if (!result.text.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text      = result.text,
                style     = MaterialTheme.typography.bodySmall,
                color     = BlancNacre.copy(alpha = 0.75f),
                textAlign = TextAlign.Justify
            )
        }
    }
}

@Composable
private fun NatureBadge(nature: CompatibilityNature, score: Int) {
    val labelRes = when (nature) {
        CompatibilityNature.HARMONIEUX     -> R.string.compatibility_nature_harmonieux
        CompatibilityNature.COMPLEMENTAIRE -> R.string.compatibility_nature_complementaire
        CompatibilityNature.TENSION        -> R.string.compatibility_nature_tension
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text  = "$score",
            style = MaterialTheme.typography.titleLarge,
            color = Or
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text  = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = VioletMoyen.copy(alpha = 0.90f)
        )
    }
}

@Composable
private fun LabeledText(label: String, text: String) {
    if (text.isBlank()) return
    Column(modifier = Modifier.padding(bottom = 18.dp)) {
        Text(
            text          = label.uppercase(),
            style         = MaterialTheme.typography.labelSmall,
            color         = BlancNacre.copy(alpha = 0.50f),
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text      = text,
            style     = MaterialTheme.typography.bodyMedium,
            color     = BlancNacre.copy(alpha = 0.85f),
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
private fun BodyText(text: String?) {
    if (text.isNullOrBlank()) return
    Text(
        text      = text,
        style     = MaterialTheme.typography.bodyMedium,
        color     = BlancNacre.copy(alpha = 0.85f),
        textAlign = TextAlign.Justify
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text          = text.uppercase(),
        style         = MaterialTheme.typography.labelMedium,
        color         = BlancNacre.copy(alpha = 0.50f),
        letterSpacing = 3.sp
    )
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        color = Or.copy(alpha = 0.20f)
    )
}

// ─── Texte de partage ───────────────────────────────────────────────────────────

private fun buildCompatibilityShareText(
    context: android.content.Context,
    state: CompatibilityUiState.Ready
): String {
    val result = state.result
    val nameA  = displayFirstName(state.profileA.firstName)
    val nameB  = displayFirstName(state.profileB.firstName)

    val sb = StringBuilder()
    sb.appendLine(context.getString(R.string.share_compatibility_title, nameA, nameB))
    sb.appendLine()

    sb.appendLine("${result.globalScore} — " + context.getString(R.string.compatibility_global_score_label))
    sb.appendLine(context.getString(scoreTextRes(result.globalScore)))
    sb.appendLine()

    val lifePathTexts = mapOf(
        CompatTab.LOVE       to result.lifePathEntry?.love,
        CompatTab.FRIENDSHIP to result.lifePathEntry?.friendship,
        CompatTab.FAMILY     to result.lifePathEntry?.family,
        CompatTab.WORK       to result.lifePathEntry?.work,
        CompatTab.CHEMISTRY  to result.lifePathEntry?.chemistry
    )
    val hdTypeTexts = mapOf(
        CompatTab.LOVE       to result.hdTypeEntry?.love,
        CompatTab.FRIENDSHIP to result.hdTypeEntry?.friendship,
        CompatTab.FAMILY     to result.hdTypeEntry?.family,
        CompatTab.WORK       to result.hdTypeEntry?.work,
        CompatTab.CHEMISTRY  to result.hdTypeEntry?.chemistry
    )

    listOf(CompatTab.LOVE, CompatTab.FRIENDSHIP, CompatTab.FAMILY, CompatTab.WORK, CompatTab.CHEMISTRY).forEach { tab ->
        sb.appendLine(PDF_SECTION_MARKER + context.getString(tab.labelRes).uppercase())
        sb.appendLine()
        sb.appendLine(context.getString(R.string.compatibility_lifepath_section).uppercase())
        lifePathTexts[tab]?.takeIf { it.isNotBlank() }?.let { sb.appendLine(it) }
        sb.appendLine()
        sb.appendLine(context.getString(R.string.compatibility_hd_type_section).uppercase())
        if (result.hasHumanDesign) {
            hdTypeTexts[tab]?.takeIf { it.isNotBlank() }?.let { sb.appendLine(it) }
        } else {
            sb.appendLine(context.getString(R.string.compatibility_no_hd_message))
        }
        sb.appendLine()
    }

    if (result.hasHumanDesign) {
        sb.appendLine(context.getString(R.string.compatibility_hd_centers_section).uppercase())
        sb.appendLine()
        result.centerResults.forEach { center ->
            val labelA = context.getString(if (center.definedA) R.string.human_design_center_defined else R.string.human_design_center_undefined)
            val labelB = context.getString(if (center.definedB) R.string.human_design_center_defined else R.string.human_design_center_undefined)
            sb.appendLine(context.getString(centerLabelRes(center.center)))
            sb.appendLine("$labelA · $labelB")
            if (!center.text.isNullOrBlank()) sb.appendLine(center.text)
            sb.appendLine()
        }
    }

    sb.appendLine(context.getString(R.string.share_destiny_footer, context.getString(R.string.app_name)))

    return sb.toString().trim()
}
