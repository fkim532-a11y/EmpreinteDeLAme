package com.francekim.empreintedelame.ui.identity

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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.domain.calculator.IdentityProfile
import com.francekim.empreintedelame.domain.model.NumberInterpretation
import com.francekim.empreintedelame.ui.common.MasterNumberDisplay
import com.francekim.empreintedelame.ui.common.ModuleInfoButton
import com.francekim.empreintedelame.ui.common.shareText
import com.francekim.empreintedelame.ui.moduleintro.ModuleIntroIds
import com.francekim.empreintedelame.ui.navigation.Screen
import com.francekim.empreintedelame.ui.theme.*

// ─── Écran principal ──────────────────────────────────────────────────────────

@Composable
fun IdentityScreen(navController: NavController) {
    val context = LocalContext.current
    val vm: IdentityViewModel = viewModel(
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
                        text      = stringResource(R.string.module_identity),
                        style     = MaterialTheme.typography.titleLarge,
                        color     = Or,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.width(6.dp))
                    ModuleInfoButton(onClick = {
                        navController.navigate(Screen.ModuleIntro.route(ModuleIntroIds.IDENTITY, "revisit"))
                    })
                }
                Spacer(Modifier.weight(1f))
                if (state is IdentityUiState.Ready) {
                    val ready = state as IdentityUiState.Ready
                    TextButton(onClick = {
                        val text = buildIdentityShareText(
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
            if (state is IdentityUiState.Ready) {
                Text(
                    text      = stringResource(R.string.reading_screen_subtitle, (state as IdentityUiState.Ready).firstName.split(" ", "-").first()),
                    style     = MaterialTheme.typography.labelMedium,
                    color     = BlancNacre.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )
            }

            // ── Contenu selon l'état ───────────────────────────────────────────
            when (state) {
                is IdentityUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Or)
                    }
                }
                is IdentityUiState.NoProfile -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text       = stringResource(R.string.identity_no_profile),
                            style      = MaterialTheme.typography.bodyLarge,
                            color      = BlancNacre.copy(alpha = 0.70f),
                            textAlign  = TextAlign.Center,
                            lineHeight = 28.sp
                        )
                    }
                }
                is IdentityUiState.Ready -> {
                    val ready = state as IdentityUiState.Ready
                    IdentityContent(
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
private fun IdentityContent(
    profile         : IdentityProfile,
    interpretations : Map<Int, NumberInterpretation>,
    eightSpiritual  : NumberInterpretation?,
    modifier        : Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // ── Nombre d'Expression (héros) ───────────────────────────────────────
        SectionTitle(
            text    = stringResource(R.string.identity_expression_section),
            caption = stringResource(R.string.identity_expression_info)
        )
        Spacer(Modifier.height(8.dp))
        MasterNumberDisplay(number = profile.expressionNumber, fontSize = 80.sp)
        Text(
            text  = stringResource(R.string.identity_expression_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = BlancNacre.copy(alpha = 0.50f)
        )
        Spacer(Modifier.height(12.dp))
        if (profile.expressionNumber == 8 && eightSpiritual != null) {
            Text(
                text          = stringResource(R.string.eight_reading_material),
                style         = MaterialTheme.typography.labelSmall,
                color         = Or,
                letterSpacing = 1.5.sp,
                textAlign     = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
        }
        Text(
            text  = interpretations[profile.expressionNumber]?.keyword ?: "",
            style = MaterialTheme.typography.titleMedium,
            color = Or.copy(alpha = 0.85f)
        )
        Text(
            text      = interpretations[profile.expressionNumber]?.expression ?: "",
            style     = MaterialTheme.typography.bodySmall,
            color     = BlancNacre.copy(alpha = 0.70f),
            textAlign = TextAlign.Center
        )
        if (profile.expressionNumber == 8 && eightSpiritual != null && !eightSpiritual.expression.isBlank()) {
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
                text      = eightSpiritual.expression,
                style     = MaterialTheme.typography.bodySmall,
                color     = BlancNacre.copy(alpha = 0.70f),
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(24.dp))

        // ── Grille 2×2 : Élan Spirituel / Intime / Héritage / Action ─────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)
        ) {
            NumberCard(
                label    = stringResource(R.string.identity_grid_label_soul),
                number   = profile.soulNumber,
                subtitle = stringResource(R.string.identity_grid_subtitle_soul),
                keyword  = interpretations[profile.soulNumber]?.keyword,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            NumberCard(
                label    = stringResource(R.string.identity_grid_label_intimate),
                number   = profile.intimateNumber,
                subtitle = stringResource(R.string.identity_grid_subtitle_intimate),
                keyword  = interpretations[profile.intimateNumber]?.keyword,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)
        ) {
            NumberCard(
                label    = stringResource(R.string.identity_grid_label_heritage),
                number   = profile.heritageNumber,
                subtitle = stringResource(R.string.identity_grid_subtitle_heritage),
                keyword  = interpretations[profile.heritageNumber]?.keyword,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            NumberCard(
                label    = stringResource(R.string.identity_grid_label_action),
                number   = profile.actionNumber,
                subtitle = stringResource(R.string.identity_grid_subtitle_action),
                keyword  = interpretations[profile.actionNumber]?.keyword,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        // ── Détail : Élan Spirituel / Intime / Héritage / Action ─────────────
        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))
        IdentityDetailBlock(
            label          = stringResource(R.string.identity_grid_label_soul),
            value          = profile.soulNumber,
            keyword        = interpretations[profile.soulNumber]?.keyword,
            text           = interpretations[profile.soulNumber]?.soul,
            eightSpiritual = eightSpiritual,
            spiritualText  = eightSpiritual?.soul,
            caption        = stringResource(R.string.identity_soul_info)
        )
        Spacer(Modifier.height(24.dp))
        IdentityDetailBlock(
            label          = stringResource(R.string.identity_grid_label_intimate),
            value          = profile.intimateNumber,
            keyword        = interpretations[profile.intimateNumber]?.keyword,
            text           = interpretations[profile.intimateNumber]?.intimate,
            eightSpiritual = eightSpiritual,
            spiritualText  = eightSpiritual?.intimate,
            caption        = stringResource(R.string.identity_intimate_info)
        )
        Spacer(Modifier.height(24.dp))
        IdentityDetailBlock(
            label          = stringResource(R.string.identity_grid_label_heritage),
            value          = profile.heritageNumber,
            keyword        = interpretations[profile.heritageNumber]?.keyword,
            text           = interpretations[profile.heritageNumber]?.heritage,
            eightSpiritual = eightSpiritual,
            spiritualText  = eightSpiritual?.heritage,
            caption        = stringResource(R.string.identity_heritage_info)
        )
        Spacer(Modifier.height(24.dp))
        IdentityDetailBlock(
            label          = stringResource(R.string.identity_grid_label_action),
            value          = profile.actionNumber,
            keyword        = interpretations[profile.actionNumber]?.keyword,
            text           = interpretations[profile.actionNumber]?.action,
            eightSpiritual = eightSpiritual,
            spiritualText  = eightSpiritual?.action,
            caption        = stringResource(R.string.identity_action_info)
        )

        // ── Nombre Pont (si chemin de vie disponible) ─────────────────────────
        if (profile.bridgeNumber != null) {
            Spacer(Modifier.height(28.dp))
            SectionDivider()
            Spacer(Modifier.height(28.dp))
            SectionTitle(
                text    = stringResource(R.string.identity_bridge_section),
                caption = stringResource(R.string.identity_bridge_info)
            )
            Spacer(Modifier.height(8.dp))
            MasterNumberDisplay(
                number       = profile.bridgeNumber,
                fontSize     = 64.sp,
                defaultColor = VioletMoyen
            )
            Text(
                text  = stringResource(R.string.identity_bridge_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = BlancNacre.copy(alpha = 0.50f)
            )
            val isBridgeEight = profile.bridgeNumber == 8 && eightSpiritual != null
            if (isBridgeEight) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text          = stringResource(R.string.eight_reading_material),
                    style         = MaterialTheme.typography.labelSmall,
                    color         = Or,
                    letterSpacing = 1.5.sp,
                    textAlign     = TextAlign.Center
                )
            }
            val bridgeText = interpretations[profile.bridgeNumber]?.bridge
            if (!bridgeText.isNullOrBlank()) {
                Spacer(Modifier.height(if (isBridgeEight) 4.dp else 12.dp))
                Text(
                    text      = bridgeText,
                    style     = MaterialTheme.typography.bodySmall,
                    color     = BlancNacre.copy(alpha = 0.70f),
                    textAlign = TextAlign.Center
                )
            }
            val bridgeSpiritualText = eightSpiritual?.bridge
            if (isBridgeEight && !bridgeSpiritualText.isNullOrBlank()) {
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
                    text  = eightSpiritual!!.keyword,
                    style = MaterialTheme.typography.titleMedium,
                    color = VioletMoyen.copy(alpha = 0.85f)
                )
                Text(
                    text      = bridgeSpiritualText,
                    style     = MaterialTheme.typography.bodySmall,
                    color     = BlancNacre.copy(alpha = 0.70f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

// ─── Carte nombre ─────────────────────────────────────────────────────────────

@Composable
private fun NumberCard(
    label    : String,
    number   : Int,
    subtitle : String,
    keyword  : String? = null,
    modifier : Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(12.dp))
            .border(0.8.dp, Or.copy(alpha = 0.30f), RoundedCornerShape(12.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            // Label ancré en haut
            Text(
                text          = label.uppercase(),
                style         = MaterialTheme.typography.labelSmall,
                color         = BlancNacre.copy(alpha = 0.60f),
                letterSpacing = 1.5.sp,
                textAlign     = TextAlign.Center
            )
            // Nombre centré dans l'espace restant — aligne les chiffres horizontalement
            Box(
                modifier         = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                MasterNumberDisplay(number = number, fontSize = 48.sp)
            }
            // Sous-titre ancré en bas
            Text(
                text      = subtitle,
                style     = MaterialTheme.typography.labelSmall,
                color     = BlancNacre.copy(alpha = 0.45f),
                textAlign = TextAlign.Center
            )
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
}

// ─── Détail (nombre + nom + texte complet) ─────────────────────────────────────

@Composable
private fun IdentityDetailBlock(
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
        if (value == 8 && eightSpiritual != null) {
            Text(
                text          = stringResource(R.string.eight_reading_material),
                style         = MaterialTheme.typography.labelSmall,
                color         = Or,
                letterSpacing = 1.5.sp,
                textAlign     = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
        }
        if (!keyword.isNullOrBlank()) {
            Text(
                text  = keyword,
                style = MaterialTheme.typography.titleMedium,
                color = Or.copy(alpha = 0.85f)
            )
        }
        if (!text.isNullOrBlank()) {
            Text(
                text      = text,
                style     = MaterialTheme.typography.bodySmall,
                color     = BlancNacre.copy(alpha = 0.70f),
                textAlign = TextAlign.Center
            )
        }
        if (value == 8 && eightSpiritual != null && !spiritualText.isNullOrBlank()) {
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

private fun appendIdentityReading(
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

private fun buildIdentityShareText(
    context         : android.content.Context,
    firstName       : String,
    profile         : IdentityProfile,
    interpretations : Map<Int, NumberInterpretation>,
    eightSpiritual  : NumberInterpretation?
): String {
    val sb = StringBuilder()
    sb.appendLine(context.getString(R.string.share_identity_title, firstName.split(" ", "-").first()))
    sb.appendLine()

    sb.appendLine("🎭 " + context.getString(R.string.identity_expression_section).uppercase())
    appendIdentityReading(
        sb, context,
        label          = context.getString(R.string.identity_expression_section),
        displayValue   = profile.expressionNumber.toString(),
        lookupValue    = profile.expressionNumber,
        keyword        = interpretations[profile.expressionNumber]?.keyword,
        text           = interpretations[profile.expressionNumber]?.expression,
        eightSpiritual = eightSpiritual,
        spiritualText  = eightSpiritual?.expression
    )

    appendIdentityReading(
        sb, context,
        label          = "💫 " + context.getString(R.string.identity_grid_label_soul),
        displayValue   = profile.soulNumber.toString(),
        lookupValue    = profile.soulNumber,
        keyword        = interpretations[profile.soulNumber]?.keyword,
        text           = interpretations[profile.soulNumber]?.soul,
        eightSpiritual = eightSpiritual,
        spiritualText  = eightSpiritual?.soul
    )

    appendIdentityReading(
        sb, context,
        label          = "🔒 " + context.getString(R.string.identity_grid_label_intimate),
        displayValue   = profile.intimateNumber.toString(),
        lookupValue    = profile.intimateNumber,
        keyword        = interpretations[profile.intimateNumber]?.keyword,
        text           = interpretations[profile.intimateNumber]?.intimate,
        eightSpiritual = eightSpiritual,
        spiritualText  = eightSpiritual?.intimate
    )

    appendIdentityReading(
        sb, context,
        label          = "🌳 " + context.getString(R.string.identity_grid_label_heritage),
        displayValue   = profile.heritageNumber.toString(),
        lookupValue    = profile.heritageNumber,
        keyword        = interpretations[profile.heritageNumber]?.keyword,
        text           = interpretations[profile.heritageNumber]?.heritage,
        eightSpiritual = eightSpiritual,
        spiritualText  = eightSpiritual?.heritage
    )

    appendIdentityReading(
        sb, context,
        label          = "🔥 " + context.getString(R.string.identity_grid_label_action),
        displayValue   = profile.actionNumber.toString(),
        lookupValue    = profile.actionNumber,
        keyword        = interpretations[profile.actionNumber]?.keyword,
        text           = interpretations[profile.actionNumber]?.action,
        eightSpiritual = eightSpiritual,
        spiritualText  = eightSpiritual?.action
    )

    if (profile.bridgeNumber != null) {
        sb.appendLine("🌉 " + context.getString(R.string.identity_bridge_section).uppercase())
        appendIdentityReading(
            sb, context,
            label          = context.getString(R.string.identity_bridge_section),
            displayValue   = profile.bridgeNumber.toString(),
            lookupValue    = profile.bridgeNumber,
            keyword        = interpretations[profile.bridgeNumber]?.keyword,
            text           = interpretations[profile.bridgeNumber]?.bridge,
            eightSpiritual = eightSpiritual,
            spiritualText  = eightSpiritual?.bridge
        )
    }

    sb.appendLine(context.getString(R.string.share_destiny_footer, context.getString(R.string.app_name)))

    return sb.toString().trim()
}
