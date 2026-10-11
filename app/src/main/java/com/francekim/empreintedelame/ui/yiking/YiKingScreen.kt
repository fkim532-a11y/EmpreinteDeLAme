package com.francekim.empreintedelame.ui.yiking

import android.app.Application
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.data.model.YiKingHexagram
import com.francekim.empreintedelame.domain.calculator.YiKingCastResult
import com.francekim.empreintedelame.domain.calculator.YiKingLine
import com.francekim.empreintedelame.domain.calculator.YiKingTables
import com.francekim.empreintedelame.ui.common.NoteField
import com.francekim.empreintedelame.ui.common.shareImage
import com.francekim.empreintedelame.ui.navigation.Screen
import com.francekim.empreintedelame.ui.theme.BlancNacre
import com.francekim.empreintedelame.ui.theme.Or
import com.francekim.empreintedelame.ui.theme.VioletMoyen
import kotlinx.coroutines.launch

@Composable
fun YiKingScreen(navController: NavController) {
    val context = LocalContext.current
    val vm: YiKingViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )
    val state by vm.uiState.collectAsState()
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
            ) {
                TextButton(
                    onClick = {
                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Text("←", color = Or, style = MaterialTheme.typography.headlineMedium)
                }
                Text(
                    text      = stringResource(R.string.module_yi_king),
                    style     = MaterialTheme.typography.titleLarge,
                    color     = Or,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.align(Alignment.Center)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    if (state is YiKingUiState.ThemeSelect || state is YiKingUiState.Result) {
                        TextButton(onClick = { navController.navigate(Screen.YiKingHistory.route) }) {
                            Image(
                                painter = painterResource(R.drawable.ic_yiking_history),
                                contentDescription = null,
                                modifier = Modifier.height(40.dp)
                            )
                        }
                    }
                    if (state is YiKingUiState.Result) {
                        val ready = state as YiKingUiState.Result
                        TextButton(onClick = {
                            val caption = buildYiKingShareText(context, ready.firstName, ready.theme, ready.cast, ready.primary, ready.secondary, ready.note)
                            scope.launch {
                                val bitmap = hexagramLayer.toImageBitmap().asAndroidBitmap()
                                shareImage(context, bitmap, caption)
                            }
                        }) {
                            Image(
                                painter = painterResource(R.drawable.ic_share),
                                contentDescription = null,
                                modifier = Modifier.height(40.dp)
                            )
                        }
                    }
                }
            }

            val firstName = when (val s = state) {
                is YiKingUiState.ThemeSelect -> s.firstName
                is YiKingUiState.Casting     -> s.firstName
                is YiKingUiState.Result      -> s.firstName
                else -> null
            }
            if (firstName != null) {
                Text(
                    text      = stringResource(R.string.reading_screen_subtitle, firstName.split(" ", "-").first()),
                    style     = MaterialTheme.typography.labelMedium,
                    color     = BlancNacre.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )
            }

            AnimatedContent(
                targetState = state,
                contentKey = { it::class },
                transitionSpec = {
                    fadeIn(tween(900)).togetherWith(fadeOut(tween(900)))
                },
                label = "yiKingState"
            ) { s ->
                when (s) {
                    is YiKingUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Or)
                        }
                    }
                    is YiKingUiState.NoProfile -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text       = stringResource(R.string.yiking_no_profile),
                                style      = MaterialTheme.typography.bodyLarge,
                                color      = BlancNacre.copy(alpha = 0.70f),
                                textAlign  = TextAlign.Center,
                                lineHeight = 28.sp,
                                modifier   = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                    is YiKingUiState.ThemeSelect -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text      = stringResource(R.string.yiking_subtitle),
                                style     = MaterialTheme.typography.bodyMedium,
                                color     = BlancNacre.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(24.dp))
                            YiKingThemeSelector(onThemeSelected = vm::selectTheme)
                        }
                    }
                    is YiKingUiState.Casting -> {
                        CastingContent(state = s, onToss = { line -> vm.commitLine(line) }, vm = vm)
                    }
                    is YiKingUiState.Result -> {
                        YiKingResultContent(
                            theme         = s.theme,
                            cast          = s.cast,
                            primary       = s.primary,
                            secondary     = s.secondary,
                            hexagramLayer = hexagramLayer,
                            note          = s.note,
                            onNoteChange  = if (s.castId != null) vm::updateNote else null,
                            modifier      = Modifier
                                .fillMaxSize()
                                .imePadding()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp, vertical = 20.dp),
                            trailing      = { NewCastButton(onClick = vm::newCast) }
                        )
                    }
                }
            }
        }
    }
}

// ─── Phase de tirage ────────────────────────────────────────────────────────

@Composable
private fun CastingContent(
    state: YiKingUiState.Casting,
    onToss: (YiKingLine) -> Unit,
    vm: YiKingViewModel
) {
    var pendingLine by remember { mutableStateOf<YiKingLine?>(null) }
    var tossKey by remember { mutableStateOf(0) }
    var isAnimating by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text      = stringResource(R.string.yiking_toss_instructions),
            style     = MaterialTheme.typography.bodyMedium,
            color     = BlancNacre.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.weight(1f))

        YiKingHexagramCanvas(lines = state.lines)

        Spacer(Modifier.weight(1f))

        Box(modifier = Modifier.height(120.dp), contentAlignment = Alignment.Center) {
            YiKingCoinsDisplay(
                isTossing    = isAnimating,
                pendingCoins = pendingLine?.coins,
                tossKey      = tossKey,
                coinSize     = 90.dp,
                onLanded     = {
                    isAnimating = false
                    pendingLine?.let(onToss)
                    pendingLine = null
                }
            )
        }

        Spacer(Modifier.height(32.dp))

        val nextIndex = state.lines.size + 1
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(VioletMoyen.copy(alpha = if (isAnimating) 0.3f else 0.6f))
                .border(1.dp, Or.copy(alpha = 0.5f), RoundedCornerShape(50))
                .clickable(enabled = !isAnimating) {
                    val line = vm.prepareTossPreview()
                    pendingLine = line
                    tossKey++
                    isAnimating = true
                }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = stringResource(R.string.yiking_toss_button, nextIndex),
                style = MaterialTheme.typography.bodyLarge,
                color = Or
            )
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text  = stringResource(R.string.yiking_coin_method_title),
            style = MaterialTheme.typography.labelSmall,
            color = BlancNacre.copy(alpha = 0.55f)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text      = stringResource(R.string.yiking_coin_method_info),
            style     = MaterialTheme.typography.labelSmall,
            color     = Or.copy(alpha = 0.70f),
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.weight(1f))
    }
}

// ─── Résultat (tirage frais ou historique) ─────────────────────────────────

@Composable
fun YiKingResultContent(
    theme: String,
    cast: YiKingCastResult,
    primary: YiKingHexagram?,
    secondary: YiKingHexagram?,
    hexagramLayer: GraphicsLayer? = null,
    note: String = "",
    onNoteChange: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = if (hexagramLayer != null) {
                Modifier.drawWithContent {
                    hexagramLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(hexagramLayer)
                }
            } else {
                Modifier
            }
        ) {
            YiKingHexagramCanvas(lines = cast.lines, size = 150.dp)
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text      = primary?.nom ?: "",
            style     = MaterialTheme.typography.headlineMedium,
            color     = Or,
            textAlign = TextAlign.Center
        )
        Text(
            text  = primary?.nomChinois ?: "",
            style = MaterialTheme.typography.titleMedium,
            color = VioletMoyen.copy(alpha = 0.90f)
        )

        Spacer(Modifier.height(24.dp))
        SectionDivider()
        Spacer(Modifier.height(24.dp))

        SectionTitle(text = stringResource(R.string.yiking_judgement_title))
        Spacer(Modifier.height(10.dp))
        BodyText(primary?.jugementGeneral)

        Spacer(Modifier.height(24.dp))
        SectionDivider()
        Spacer(Modifier.height(24.dp))

        val themeLabelRes = yiKingThemeOptions.find { it.key == theme }?.labelRes
        SectionTitle(text = themeLabelRes?.let { stringResource(it) } ?: theme)
        Spacer(Modifier.height(10.dp))
        BodyText(primary?.themes?.forKey(theme))

        Spacer(Modifier.height(24.dp))
        SectionDivider()
        Spacer(Modifier.height(24.dp))

        SectionTitle(text = stringResource(R.string.yiking_image_title))
        Spacer(Modifier.height(10.dp))
        Text(
            text      = primary?.image ?: "",
            style     = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            color     = BlancNacre.copy(alpha = 0.70f),
            textAlign = TextAlign.Center
        )

        val mutantIndexes = cast.lines.mapIndexedNotNull { index, line -> if (line.isMutant) index + 1 else null }
        if (mutantIndexes.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            SectionDivider()
            Spacer(Modifier.height(24.dp))

            SectionTitle(
                text    = stringResource(R.string.yiking_mutant_traits_title),
                caption = stringResource(R.string.yiking_mutant_traits_info)
            )
            Spacer(Modifier.height(12.dp))
            mutantIndexes.forEach { traitIndex ->
                Text(
                    text  = stringResource(R.string.yiking_trait_label, traitIndex),
                    style = MaterialTheme.typography.titleSmall,
                    color = Or.copy(alpha = 0.85f)
                )
                Spacer(Modifier.height(4.dp))
                BodyText(primary?.traits?.get(traitIndex))
                Spacer(Modifier.height(14.dp))
            }
        }

        if (secondary != null) {
            Spacer(Modifier.height(10.dp))
            SectionDivider()
            Spacer(Modifier.height(24.dp))

            SectionTitle(
                text    = stringResource(R.string.yiking_transformation_title),
                caption = stringResource(R.string.yiking_transformation_info)
            )
            Spacer(Modifier.height(16.dp))
            YiKingHexagramCanvas(
                lines = stableLinesFromPattern(YiKingTables.HEXAGRAM_LINES.getValue(secondary.numero)),
                size  = 110.dp
            )
            Spacer(Modifier.height(12.dp))
            Text(text = secondary.nom, style = MaterialTheme.typography.titleLarge, color = Or, textAlign = TextAlign.Center)
            Text(text = secondary.nomChinois, style = MaterialTheme.typography.bodyMedium, color = VioletMoyen.copy(alpha = 0.90f))
            Spacer(Modifier.height(10.dp))
            BodyText(secondary.jugementGeneral)
        }

        if (onNoteChange != null) {
            Spacer(Modifier.height(28.dp))
            SectionDivider()
            Spacer(Modifier.height(24.dp))
            NoteField(initialNote = note, onSave = onNoteChange)
        }

        if (trailing != null) {
            Spacer(Modifier.height(32.dp))
            trailing()
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun NewCastButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(VioletMoyen.copy(alpha = 0.6f))
            .border(1.dp, Or.copy(alpha = 0.5f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = stringResource(R.string.archangel_card_new_draw),
            style = MaterialTheme.typography.bodyLarge,
            color = Or
        )
    }
}

// ─── Composants internes ───────────────────────────────────────────────────

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

// ─── Texte de partage ───────────────────────────────────────────────────────

// `note` n'apparaît dans le texte partagé que si elle a été enregistrée
// (bouton « Enregistrer » du champ de notes).
internal fun buildYiKingShareText(
    context   : android.content.Context,
    firstName : String,
    theme     : String,
    cast      : YiKingCastResult,
    primary   : YiKingHexagram?,
    secondary : YiKingHexagram?,
    note      : String = ""
): String {
    val sb = StringBuilder()
    sb.appendLine(context.getString(R.string.share_yiking_title, firstName.split(" ", "-").first()))
    sb.appendLine()

    if (!primary?.nom.isNullOrBlank()) {
        sb.appendLine("${primary?.nom} — ${primary?.nomChinois}")
        sb.appendLine()
    }

    if (!primary?.jugementGeneral.isNullOrBlank()) {
        sb.appendLine("🔮 " + context.getString(R.string.yiking_judgement_title).uppercase())
        sb.appendLine(primary?.jugementGeneral)
        sb.appendLine()
    }

    val themeLabelRes = yiKingThemeOptions.find { it.key == theme }?.labelRes
    val themeText = primary?.themes?.forKey(theme)
    if (!themeText.isNullOrBlank()) {
        sb.appendLine("💫 " + (themeLabelRes?.let { context.getString(it) } ?: theme).uppercase())
        sb.appendLine(themeText)
        sb.appendLine()
    }

    val mutantIndexes = cast.lines.mapIndexedNotNull { index, line -> if (line.isMutant) index + 1 else null }
    if (mutantIndexes.isNotEmpty()) {
        sb.appendLine("✨ " + context.getString(R.string.yiking_mutant_traits_title).uppercase())
        mutantIndexes.forEach { traitIndex ->
            sb.appendLine(context.getString(R.string.yiking_trait_label, traitIndex) + " — " + primary?.traits?.get(traitIndex))
        }
        sb.appendLine()
    }

    if (secondary != null) {
        sb.appendLine("🌗 " + context.getString(R.string.yiking_transformation_title).uppercase())
        sb.appendLine("${secondary.nom} — ${secondary.nomChinois}")
        sb.appendLine(secondary.jugementGeneral)
        sb.appendLine()
    }

    if (note.isNotBlank()) {
        sb.appendLine("📝 " + context.getString(R.string.note_field_label).uppercase())
        sb.appendLine(note)
        sb.appendLine()
    }

    sb.appendLine(context.getString(R.string.share_destiny_footer, context.getString(R.string.app_name)))

    return sb.toString().trim()
}
