package com.francekim.empreintedelame.ui.portrait

import android.app.Application
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
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
import com.francekim.empreintedelame.domain.calculator.Arrow
import com.francekim.empreintedelame.domain.calculator.InclusionGrid
import com.francekim.empreintedelame.domain.calculator.PortraitProfile
import com.francekim.empreintedelame.domain.calculator.PythagoreanArrows
import com.francekim.empreintedelame.domain.model.ArrowInterpretation
import com.francekim.empreintedelame.domain.model.NumberInterpretation
import com.francekim.empreintedelame.ui.common.MasterNumberDisplay
import com.francekim.empreintedelame.ui.common.ModuleInfoButton
import com.francekim.empreintedelame.ui.common.shareText
import com.francekim.empreintedelame.ui.moduleintro.ModuleIntroIds
import com.francekim.empreintedelame.ui.navigation.Screen
import com.francekim.empreintedelame.ui.theme.*

// ─── Constantes de la grille de Pythagore ────────────────────────────────────

private val GRID_MATRIX = arrayOf(
    intArrayOf(3, 6, 9),
    intArrayOf(2, 5, 8),
    intArrayOf(1, 4, 7)
)

private val DIGIT_GRID_POS = mapOf(
    3 to Pair(0, 0), 6 to Pair(0, 1), 9 to Pair(0, 2),
    2 to Pair(1, 0), 5 to Pair(1, 1), 8 to Pair(1, 2),
    1 to Pair(2, 0), 4 to Pair(2, 1), 7 to Pair(2, 2)
)

private val ARROW_DIGITS_LIST = mapOf(
    Arrow.PRATICIEN    to listOf(1, 4, 7),
    Arrow.VOLONTE      to listOf(2, 5, 8),
    Arrow.INTELLECT    to listOf(3, 6, 9),
    Arrow.PENSEE       to listOf(1, 2, 3),
    Arrow.PERSEVERANCE to listOf(4, 5, 6),
    Arrow.SPIRITUALITE to listOf(7, 8, 9),
    Arrow.DIAGONAL_159 to listOf(1, 5, 9),
    Arrow.DIAGONAL_357 to listOf(3, 5, 7)
)

@StringRes
private fun arrowNameRes(arrow: Arrow): Int = when (arrow) {
    Arrow.PRATICIEN    -> R.string.arrow_praticien
    Arrow.VOLONTE      -> R.string.arrow_volonte
    Arrow.INTELLECT    -> R.string.arrow_intellect
    Arrow.PENSEE       -> R.string.arrow_pensee
    Arrow.PERSEVERANCE -> R.string.arrow_perseverance
    Arrow.SPIRITUALITE -> R.string.arrow_spiritualite
    Arrow.DIAGONAL_159 -> R.string.arrow_diagonal_159
    Arrow.DIAGONAL_357 -> R.string.arrow_diagonal_357
}

// ─── Écran principal ──────────────────────────────────────────────────────────

@Composable
fun PortraitScreen(navController: NavController) {
    val context = LocalContext.current
    val vm: PortraitViewModel = viewModel(
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
                        text      = stringResource(R.string.module_portrait),
                        style     = MaterialTheme.typography.titleLarge,
                        color     = Or,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.width(6.dp))
                    ModuleInfoButton(onClick = {
                        navController.navigate(Screen.ModuleIntro.route(ModuleIntroIds.PORTRAIT, "revisit"))
                    })
                }
                Spacer(Modifier.weight(1f))
                if (state is PortraitUiState.Ready) {
                    val ready = state as PortraitUiState.Ready
                    TextButton(onClick = {
                        val text = buildPortraitShareText(
                            context         = context,
                            firstName       = ready.firstName,
                            profile         = ready.profile,
                            interpretations = ready.interpretations,
                            eightSpiritual  = ready.eightSpiritual,
                            arrowTexts      = ready.arrows
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
            if (state is PortraitUiState.Ready) {
                Text(
                    text      = stringResource(R.string.reading_screen_subtitle, (state as PortraitUiState.Ready).firstName.split(" ", "-").first()),
                    style     = MaterialTheme.typography.labelMedium,
                    color     = BlancNacre.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )
            }

            // ── Contenu selon l'état ───────────────────────────────────────────
            when (state) {
                is PortraitUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Or)
                    }
                }
                is PortraitUiState.NoProfile -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text       = stringResource(R.string.portrait_no_profile),
                            style      = MaterialTheme.typography.bodyLarge,
                            color      = BlancNacre.copy(alpha = 0.70f),
                            textAlign  = TextAlign.Center,
                            lineHeight = 28.sp
                        )
                    }
                }
                is PortraitUiState.Ready -> {
                    val ready = state as PortraitUiState.Ready
                    PortraitContent(
                        profile         = ready.profile,
                        interpretations = ready.interpretations,
                        eightSpiritual  = ready.eightSpiritual,
                        arrowTexts      = ready.arrows,
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
private fun PortraitContent(
    profile         : PortraitProfile,
    interpretations : Map<Int, NumberInterpretation>,
    eightSpiritual  : NumberInterpretation?,
    arrowTexts      : Map<Arrow, ArrowInterpretation>,
    modifier        : Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // ── Grille de l'Âme ──────────────────────────────────────────────────
        SectionTitle(
            text    = stringResource(R.string.portrait_inclusion_section),
            caption = stringResource(R.string.portrait_inclusion_info)
        )
        Spacer(Modifier.height(16.dp))
        InclusionGridView(profile.inclusionGrid)
        Spacer(Modifier.height(10.dp))
        GridLegend(profile.inclusionGrid)
        Spacer(Modifier.height(16.dp))
        InclusionInterpretations(profile.inclusionGrid, interpretations, profile.lifePathNumber, eightSpiritual)
        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        // ── Nombre de l'Inconscient ──────────────────────────────────────────
        SectionTitle(
            text    = stringResource(R.string.portrait_balance_section),
            caption = stringResource(R.string.portrait_balance_info)
        )
        Spacer(Modifier.height(12.dp))
        MasterNumberDisplay(number = profile.balanceNumber, fontSize = 80.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text  = stringResource(R.string.portrait_balance_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = BlancNacre.copy(alpha = 0.50f)
        )
        Spacer(Modifier.height(12.dp))
        if (profile.balanceNumber == 8 && eightSpiritual != null) {
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
            text  = interpretations[profile.balanceNumber]?.keyword ?: "",
            style = MaterialTheme.typography.titleMedium,
            color = Or.copy(alpha = 0.85f)
        )
        Text(
            text      = interpretations[profile.balanceNumber]?.unconscious ?: "",
            style     = MaterialTheme.typography.bodySmall,
            color     = BlancNacre.copy(alpha = 0.70f),
            textAlign = TextAlign.Center
        )
        if (profile.balanceNumber == 8 && eightSpiritual != null && eightSpiritual.unconscious.isNotBlank()) {
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
                text      = eightSpiritual.unconscious,
                style     = MaterialTheme.typography.bodySmall,
                color     = BlancNacre.copy(alpha = 0.70f),
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        // ── Flèches de Pythagore ─────────────────────────────────────────────
        SectionTitle(
            text    = stringResource(R.string.portrait_arrows_section),
            caption = stringResource(R.string.portrait_arrows_info)
        )
        Spacer(Modifier.height(16.dp))
        ArrowsGridView(profile.arrows)
        Spacer(Modifier.height(20.dp))
        ArrowsLegend(profile.arrows, arrowTexts)
        Spacer(Modifier.height(40.dp))
    }
}

// ─── Section 1 : Grille de l'Âme ─────────────────────────────────────────────

@Composable
private fun InclusionGridView(grid: InclusionGrid) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GRID_MATRIX.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { digit ->
                    GridCell(
                        digit        = digit,
                        count        = grid.occurrences[digit] ?: 0,
                        isMissing    = digit in grid.missingNumbers,
                        isOverloaded = digit in grid.overloadedNumbers,
                        modifier     = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GridCell(
    digit: Int, count: Int,
    isMissing: Boolean, isOverloaded: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isMissing    -> Color(0xFFE57373)
        isOverloaded -> Or
        else         -> Or.copy(alpha = 0.35f)
    }
    val borderWidth = if (isMissing || isOverloaded) 1.5.dp else 0.8.dp
    val textAlpha   = if (count == 0) 0.35f else 1f

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(
                Color.White.copy(alpha = if (count == 0) 0.03f else 0.08f),
                RoundedCornerShape(10.dp)
            )
            .border(borderWidth, borderColor, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text  = "$digit",
                style = MaterialTheme.typography.headlineMedium,
                color = Or.copy(alpha = textAlpha)
            )
            Text(
                text  = if (count == 0) "—" else "×$count",
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    isMissing    -> Color(0xFFE57373)
                    isOverloaded -> Or
                    else         -> BlancNacre.copy(alpha = 0.60f)
                }
            )
        }
    }
}

@Composable
private fun GridLegend(grid: InclusionGrid) {
    if (grid.missingNumbers.isEmpty() && grid.overloadedNumbers.isEmpty()) return
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (grid.missingNumbers.isNotEmpty()) {
            Text(
                text  = stringResource(R.string.portrait_missing_legend),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFE57373)
            )
        }
        if (grid.missingNumbers.isNotEmpty() && grid.overloadedNumbers.isNotEmpty()) {
            Spacer(Modifier.width(20.dp))
        }
        if (grid.overloadedNumbers.isNotEmpty()) {
            Text(
                text  = stringResource(R.string.portrait_overload_legend),
                style = MaterialTheme.typography.labelSmall,
                color = Or
            )
        }
    }
}

@Composable
private fun InclusionInterpretations(
    grid: InclusionGrid,
    interpretations: Map<Int, NumberInterpretation>,
    lifePathNumber: Int,
    eightSpiritual: NumberInterpretation?
) {
    val innateNumbers = grid.occurrences.keys
        .filter { it !in grid.missingNumbers && it !in grid.overloadedNumbers }
        .sorted()
    if (innateNumbers.isEmpty() && grid.missingNumbers.isEmpty() && grid.overloadedNumbers.isEmpty()) return
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        innateNumbers.forEach { digit ->
            if (digit == 8 && eightSpiritual != null) {
                DualReadingLine(
                    digit            = digit,
                    keyword          = interpretations[digit]?.keyword ?: "",
                    labelColor       = OrDore,
                    materialText     = interpretations[digit]?.innate ?: "",
                    spiritualKeyword = eightSpiritual.keyword,
                    spiritualText    = eightSpiritual.innate
                )
            } else {
                InterpretationLine(
                    digit      = digit,
                    keyword    = interpretations[digit]?.keyword ?: "",
                    text       = interpretations[digit]?.innate ?: "",
                    labelColor = OrDore
                )
            }
        }
        grid.missingNumbers.forEach { digit ->
            val isLifePath = digit == lifePathNumber
            if (isLifePath && digit == 8 && eightSpiritual != null) {
                DualReadingLine(
                    digit            = digit,
                    keyword          = interpretations[digit]?.keyword ?: "",
                    labelColor       = Color(0xFFE57373),
                    materialText     = interpretations[digit]?.missingLifePath ?: "",
                    spiritualKeyword = eightSpiritual.keyword,
                    spiritualText    = eightSpiritual.missingLifePath
                )
            } else {
                InterpretationLine(
                    digit      = digit,
                    keyword    = interpretations[digit]?.keyword ?: "",
                    text       = if (isLifePath) interpretations[digit]?.missingLifePath ?: ""
                                 else interpretations[digit]?.missing ?: "",
                    labelColor = Color(0xFFE57373)
                )
            }
        }
        grid.overloadedNumbers.forEach { digit ->
            InterpretationLine(
                digit      = digit,
                keyword    = interpretations[digit]?.keyword ?: "",
                text       = interpretations[digit]?.overload ?: "",
                labelColor = Or
            )
        }
    }
}

@Composable
private fun DualReadingLine(
    digit           : Int,
    keyword         : String,
    labelColor      : Color,
    materialText    : String,
    spiritualKeyword: String,
    spiritualText   : String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text  = "$digit — $keyword",
            style = MaterialTheme.typography.labelMedium,
            color = labelColor
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text          = stringResource(R.string.eight_reading_material),
            style         = MaterialTheme.typography.labelSmall,
            color         = Or,
            letterSpacing = 1.5.sp,
            textAlign     = TextAlign.Center
        )
        Text(
            text      = materialText,
            style     = MaterialTheme.typography.bodySmall,
            color     = BlancNacre.copy(alpha = 0.70f),
            textAlign = TextAlign.Center
        )
        if (spiritualText.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text          = stringResource(R.string.eight_reading_spiritual),
                style         = MaterialTheme.typography.labelSmall,
                color         = VioletMoyen,
                letterSpacing = 1.5.sp,
                textAlign     = TextAlign.Center
            )
            Text(
                text  = spiritualKeyword,
                style = MaterialTheme.typography.labelMedium,
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

@Composable
private fun InterpretationLine(
    digit: Int, keyword: String, text: String, labelColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text  = "$digit — $keyword",
            style = MaterialTheme.typography.labelMedium,
            color = labelColor
        )
        Text(
            text      = text,
            style     = MaterialTheme.typography.bodySmall,
            color     = BlancNacre.copy(alpha = 0.70f),
            textAlign = TextAlign.Center
        )
    }
}

// ─── Section 3 : Flèches de Pythagore ────────────────────────────────────────

@Composable
private fun ArrowsGridView(arrows: PythagoreanArrows) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        // Couche 1 : cellules avec chiffres (discrets)
        Column(modifier = Modifier.fillMaxSize()) {
            GRID_MATRIX.forEach { row ->
                Row(modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                ) {
                    row.forEach { digit ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .border(0.5.dp, BlancNacre.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text  = "$digit",
                                style = MaterialTheme.typography.headlineLarge,
                                color = BlancNacre.copy(alpha = 0.70f)
                            )
                        }
                    }
                }
            }
        }

        // Couche 2 : Canvas pour les flèches
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellW = size.width  / 3f
            val cellH = size.height / 3f

            fun center(digit: Int): Offset {
                val (row, col) = DIGIT_GRID_POS[digit] ?: return Offset.Zero
                return Offset(col * cellW + cellW / 2f, row * cellH + cellH / 2f)
            }

            // Flèches pleines — trait doré
            arrows.fullArrows.forEach { arrow ->
                val digits = ARROW_DIGITS_LIST[arrow] ?: return@forEach
                drawLine(
                    color       = Or,
                    start       = center(digits.first()),
                    end         = center(digits.last()),
                    strokeWidth = 5.dp.toPx(),
                    cap         = StrokeCap.Round
                )
            }

            // Flèches vides — trait violet en pointillés
            val dash = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
            arrows.emptyArrows.forEach { arrow ->
                val digits = ARROW_DIGITS_LIST[arrow] ?: return@forEach
                drawLine(
                    color       = VioletMoyen.copy(alpha = 0.55f),
                    start       = center(digits.first()),
                    end         = center(digits.last()),
                    strokeWidth = 2.5.dp.toPx(),
                    cap         = StrokeCap.Round,
                    pathEffect  = dash
                )
            }
        }
    }
}

@Composable
private fun ArrowsLegend(arrows: PythagoreanArrows, arrowTexts: Map<Arrow, ArrowInterpretation>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Flèches pleines
        Text(
            text      = stringResource(R.string.portrait_full_arrows),
            style     = MaterialTheme.typography.labelSmall,
            color     = Or.copy(alpha = 0.80f),
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth()
        )
        if (arrows.fullArrows.isEmpty()) {
            Text(
                text      = stringResource(R.string.portrait_no_arrows),
                style     = MaterialTheme.typography.bodySmall,
                color     = BlancNacre.copy(alpha = 0.40f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )
        } else {
            arrows.fullArrows.forEach { arrow ->
                ArrowInterpretationBlock(
                    name  = stringResource(arrowNameRes(arrow)),
                    text  = arrowTexts[arrow]?.full,
                    color = Or
                )
            }
        }

        // Flèches vides
        Text(
            text      = stringResource(R.string.portrait_empty_arrows),
            style     = MaterialTheme.typography.labelSmall,
            color     = VioletMoyen.copy(alpha = 0.80f),
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth()
        )
        if (arrows.emptyArrows.isEmpty()) {
            Text(
                text      = stringResource(R.string.portrait_no_arrows),
                style     = MaterialTheme.typography.bodySmall,
                color     = BlancNacre.copy(alpha = 0.40f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )
        } else {
            arrows.emptyArrows.forEach { arrow ->
                ArrowInterpretationBlock(
                    name  = stringResource(arrowNameRes(arrow)),
                    text  = arrowTexts[arrow]?.empty,
                    color = VioletMoyen
                )
            }
        }
    }
}

@Composable
private fun ArrowInterpretationBlock(name: String, text: String?, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text  = name,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
        if (!text.isNullOrBlank()) {
            Text(
                text      = text,
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

private fun appendPortraitReading(
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

private fun buildPortraitShareText(
    context         : android.content.Context,
    firstName       : String,
    profile         : PortraitProfile,
    interpretations : Map<Int, NumberInterpretation>,
    eightSpiritual  : NumberInterpretation?,
    arrowTexts      : Map<Arrow, ArrowInterpretation>
): String {
    val sb = StringBuilder()
    sb.appendLine(context.getString(R.string.share_portrait_title, firstName.split(" ", "-").first()))
    sb.appendLine()

    // ── Grille de l'Âme ──────────────────────────────────────────────────
    val grid = profile.inclusionGrid
    val innateNumbers = grid.occurrences.keys
        .filter { it !in grid.missingNumbers && it !in grid.overloadedNumbers }
        .sorted()
    if (innateNumbers.isNotEmpty() || grid.missingNumbers.isNotEmpty() || grid.overloadedNumbers.isNotEmpty()) {
        sb.appendLine("🔢 " + context.getString(R.string.portrait_inclusion_section).uppercase())
        innateNumbers.forEach { digit ->
            sb.appendLine("${context.getString(R.string.portrait_innate_label)} $digit — ${interpretations[digit]?.keyword ?: ""}")
            if (digit == 8 && eightSpiritual != null) {
                sb.appendLine(context.getString(R.string.eight_reading_material))
                interpretations[digit]?.innate?.let { if (it.isNotBlank()) sb.appendLine(it) }
                if (eightSpiritual.innate.isNotBlank()) {
                    sb.appendLine("${context.getString(R.string.eight_reading_spiritual)} : ${eightSpiritual.keyword}")
                    sb.appendLine(eightSpiritual.innate)
                }
            } else {
                interpretations[digit]?.innate?.let { if (it.isNotBlank()) sb.appendLine(it) }
            }
            sb.appendLine()
        }
        grid.missingNumbers.forEach { digit ->
            val isLifePath = digit == profile.lifePathNumber
            sb.appendLine("${context.getString(R.string.portrait_missing_label)} $digit — ${interpretations[digit]?.keyword ?: ""}")
            when {
                isLifePath && digit == 8 && eightSpiritual != null -> {
                    sb.appendLine(context.getString(R.string.eight_reading_material))
                    interpretations[digit]?.missingLifePath?.let { if (it.isNotBlank()) sb.appendLine(it) }
                    if (eightSpiritual.missingLifePath.isNotBlank()) {
                        sb.appendLine("${context.getString(R.string.eight_reading_spiritual)} : ${eightSpiritual.keyword}")
                        sb.appendLine(eightSpiritual.missingLifePath)
                    }
                }
                isLifePath -> interpretations[digit]?.missingLifePath?.let { if (it.isNotBlank()) sb.appendLine(it) }
                else       -> interpretations[digit]?.missing?.let { if (it.isNotBlank()) sb.appendLine(it) }
            }
            sb.appendLine()
        }
        grid.overloadedNumbers.forEach { digit ->
            sb.appendLine("${context.getString(R.string.portrait_overload_label)} $digit — ${interpretations[digit]?.keyword ?: ""}")
            interpretations[digit]?.overload?.let { if (it.isNotBlank()) sb.appendLine(it) }
            sb.appendLine()
        }
    }

    // ── Nombre de l'Inconscient ──────────────────────────────────────────
    sb.appendLine("🌙 " + context.getString(R.string.portrait_balance_section).uppercase())
    appendPortraitReading(
        sb, context,
        label          = context.getString(R.string.portrait_balance_section),
        displayValue   = profile.balanceNumber.toString(),
        lookupValue    = profile.balanceNumber,
        keyword        = interpretations[profile.balanceNumber]?.keyword,
        text           = interpretations[profile.balanceNumber]?.unconscious,
        eightSpiritual = eightSpiritual,
        spiritualText  = eightSpiritual?.unconscious
    )

    // ── Flèches de Pythagore ─────────────────────────────────────────────
    sb.appendLine("🏹 " + context.getString(R.string.portrait_arrows_section).uppercase())
    sb.appendLine(context.getString(R.string.portrait_full_arrows))
    if (profile.arrows.fullArrows.isEmpty()) {
        sb.appendLine(context.getString(R.string.portrait_no_arrows))
    } else {
        profile.arrows.fullArrows.forEach { arrow ->
            sb.appendLine(context.getString(arrowNameRes(arrow)))
            arrowTexts[arrow]?.full?.let { if (it.isNotBlank()) sb.appendLine(it) }
        }
    }
    sb.appendLine()
    sb.appendLine(context.getString(R.string.portrait_empty_arrows))
    if (profile.arrows.emptyArrows.isEmpty()) {
        sb.appendLine(context.getString(R.string.portrait_no_arrows))
    } else {
        profile.arrows.emptyArrows.forEach { arrow ->
            sb.appendLine(context.getString(arrowNameRes(arrow)))
            arrowTexts[arrow]?.empty?.let { if (it.isNotBlank()) sb.appendLine(it) }
        }
    }
    sb.appendLine()

    sb.appendLine(context.getString(R.string.share_destiny_footer, context.getString(R.string.app_name)))

    return sb.toString().trim()
}
