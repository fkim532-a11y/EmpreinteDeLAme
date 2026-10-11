package com.francekim.empreintedelame.ui.archangelcard

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.data.model.ArchangelCard
import com.francekim.empreintedelame.ui.common.NoteField
import com.francekim.empreintedelame.ui.common.shareImage
import com.francekim.empreintedelame.ui.navigation.Screen
import com.francekim.empreintedelame.ui.theme.BlancNacre
import com.francekim.empreintedelame.ui.theme.Or
import com.francekim.empreintedelame.ui.theme.VioletMoyen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ArchangelCardScreen(navController: NavController) {
    val context = LocalContext.current
    val vm: ArchangelCardViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )
    val uiState by vm.uiState.collectAsState()

    var isCardFullscreen by remember { mutableStateOf(false) }
    var fullscreenWasShown by remember { mutableStateOf(false) }
    var cardFlipCenter by remember { mutableStateOf(Offset.Zero) }
    var cardFlipWidthPx by remember { mutableStateOf(0f) }

    LaunchedEffect(uiState.isFlipped) {
        if (uiState.isFlipped) {
            isCardFullscreen = true
            fullscreenWasShown = true
        } else {
            isCardFullscreen = false
            fullscreenWasShown = false
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
                    if (isCardFullscreen) {
                        isCardFullscreen = false
                    } else if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        navController.popBackStack()
                    }
                }) {
                    Text("←", color = Or, style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text      = stringResource(R.string.module_archangel_card),
                    style     = MaterialTheme.typography.titleLarge,
                    color     = Or,
                    textAlign = TextAlign.Center
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { navController.navigate(Screen.ArchangelCardHistory.route) }) {
                        Image(
                            painter = painterResource(R.drawable.ic_yiking_history),
                            contentDescription = stringResource(R.string.archangel_card_history_button_desc),
                            modifier = Modifier.height(40.dp)
                        )
                    }
                    if (uiState.isFlipped && uiState.selectedTheme != null && uiState.card != null) {
                        val card = uiState.card!!
                        val theme = uiState.selectedTheme!!
                        TextButton(onClick = {
                            val caption = buildArchangelCardShareText(context, card, theme, uiState.note)
                            val imageResId = context.resources.getIdentifier(card.image, "drawable", context.packageName)
                            val bitmap = BitmapFactory.decodeResource(context.resources, imageResId)
                            shareImage(context, bitmap, caption)
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

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Or)
                    }
                }
                uiState.card != null -> {
                    ArchangelCardContent(
                        uiState = uiState,
                        isCardFullscreen = isCardFullscreen,
                        fullscreenWasShown = fullscreenWasShown,
                        onThemeSelected = vm::selectTheme,
                        onNewDraw = vm::resetFlip,
                        onNoteChange = vm::updateNote,
                        onCardPositioned = { center, widthPx ->
                            cardFlipCenter = center
                            cardFlipWidthPx = widthPx
                        }
                    )
                }
            }
        }

        if (isCardFullscreen && uiState.card != null) {
            ArchangelCardFullscreenOverlay(
                card = uiState.card!!,
                cardFlipCenter = cardFlipCenter,
                cardFlipWidthPx = cardFlipWidthPx,
                onDismiss = { isCardFullscreen = false }
            )
        }
    }
}

@Composable
private fun ArchangelCardContent(
    uiState: ArchangelCardUiState,
    isCardFullscreen: Boolean,
    fullscreenWasShown: Boolean,
    onThemeSelected: (String) -> Unit,
    onNewDraw: () -> Unit,
    onNoteChange: (String) -> Unit,
    onCardPositioned: (Offset, Float) -> Unit
) {
    val card = uiState.card!!
    var shuffleDone by remember(uiState.shuffleKey) { mutableStateOf(uiState.isFlipped) }

    // Tout l'écran défile d'un bloc : la carte occupe une hauteur fixe de 380 dp,
    // donc clavier ouvert il ne resterait pas la place d'afficher le champ de notes
    // si seule la partie basse était défilante.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.archangel_card_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = BlancNacre.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier.fillMaxWidth().height(380.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!shuffleDone && !uiState.isFlipped) {
                ArchangelShuffleAnimation(onDone = { shuffleDone = true })
            } else {
                Box(
                    modifier = Modifier
                        .alpha(if (isCardFullscreen) 0f else 1f)
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInWindow()
                            onCardPositioned(
                                Offset(pos.x + coords.size.width / 2f, pos.y + coords.size.height / 2f),
                                coords.size.width.toFloat()
                            )
                        }
                ) {
                    ArchangelCardFlip(card = card, isFlipped = uiState.isFlipped)
                }
            }
        }

        AnimatedVisibility(
            visible = !uiState.isFlipped && shuffleDone,
            enter = fadeIn(animationSpec = tween(durationMillis = 1400)),
            exit = fadeOut(animationSpec = tween(durationMillis = 400))
        ) {
            ArchangelThemeSelector(onThemeSelected = onThemeSelected)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            AnimatedVisibility(
                visible = uiState.isFlipped && uiState.selectedTheme != null && !isCardFullscreen && fullscreenWasShown,
                enter = fadeIn(animationSpec = tween(durationMillis = 800)),
                exit = fadeOut(animationSpec = tween(durationMillis = 200))
            ) {
                Column {
                    ArchangelCardMessage(card = card, theme = uiState.selectedTheme ?: "spirituel")
                    if (uiState.castId != null) {
                        Spacer(Modifier.height(20.dp))
                        NoteField(initialNote = uiState.note, onSave = onNoteChange)
                    }
                    Spacer(Modifier.height(20.dp))
                    NewDrawButton(onClick = onNewDraw)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ArchangelCardFlip(card: ArchangelCard, isFlipped: Boolean) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "archangelCardFlip"
    )
    Box(
        modifier = Modifier
            .width(240.dp)
            .height(360.dp)
            .clip(RoundedCornerShape(16.dp))
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
    ) {
        if (rotation <= 90f) {
            ArchangelCardBack()
        } else {
            Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                ArchangelCardFront(card)
            }
        }
    }
}

@Composable
private fun ArchangelCardBack() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Or.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .background(com.francekim.empreintedelame.ui.theme.BleuNuitSombre)
    ) {
        Image(
            painter = painterResource(R.drawable.archangel_back),
            contentDescription = stringResource(R.string.archangel_card_back_desc),
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@SuppressLint("DiscouragedApi")
@Composable
internal fun ArchangelCardFront(card: ArchangelCard) {
    val context = LocalContext.current
    val imageResId = remember(card.image) {
        context.resources.getIdentifier(card.image, "drawable", context.packageName)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Or.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
    ) {
        Image(
            painter = painterResource(imageResId),
            contentDescription = card.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
internal fun ArchangelCardMessage(card: ArchangelCard, theme: String) {
    val themeContent = card.themeFor(theme)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = card.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Or,
                textAlign = TextAlign.Center
            )
            Text(
                text = card.keyword,
                style = MaterialTheme.typography.bodyMedium,
                color = BlancNacre.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        HorizontalDivider(color = Or.copy(alpha = 0.3f))

        Text(
            text = themeContent.situation,
            style = MaterialTheme.typography.bodyLarge,
            color = BlancNacre,
            textAlign = TextAlign.Justify
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = VioletMoyen.copy(alpha = 0.3f)
        ) {
            Text(
                text = themeContent.conseil,
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                color = BlancNacre,
                textAlign = TextAlign.Justify,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// ─── Texte de partage ───────────────────────────────────────────────────────────

// `note` n'apparaît dans le texte partagé que si elle a été enregistrée
// (bouton « Enregistrer » du champ de notes).
internal fun buildArchangelCardShareText(
    context : android.content.Context,
    card    : ArchangelCard,
    theme   : String,
    note    : String = ""
): String {
    val themeLabelRes = archangelThemeOptions.find { it.key == theme }?.labelRes
    val themeContent = card.themeFor(theme)

    val sb = StringBuilder()
    sb.appendLine(context.getString(R.string.share_archangelcard_title, card.name))
    sb.appendLine(card.keyword)
    sb.appendLine()

    if (themeLabelRes != null) {
        sb.appendLine("🔮 " + context.getString(themeLabelRes).uppercase())
    }
    if (themeContent.situation.isNotBlank()) {
        sb.appendLine(themeContent.situation)
        sb.appendLine()
    }
    if (themeContent.conseil.isNotBlank()) {
        sb.appendLine("💡 " + context.getString(R.string.archangel_message_section).uppercase())
        sb.appendLine(themeContent.conseil)
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

@Composable
private fun NewDrawButton(onClick: () -> Unit) {
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
            text = stringResource(R.string.archangel_card_new_draw),
            style = MaterialTheme.typography.bodyLarge,
            color = Or
        )
    }
}

@SuppressLint("DiscouragedApi")
@Composable
private fun ArchangelCardFullscreenOverlay(
    card: ArchangelCard,
    cardFlipCenter: Offset,
    cardFlipWidthPx: Float,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val imageResId = remember(card.image) {
        context.resources.getIdentifier(card.image, "drawable", context.packageName)
    }
    val configuration = LocalConfiguration.current
    val localDensity = LocalDensity.current
    val screenWidthDp = configuration.screenWidthDp
    val screenWidthPx = with(localDensity) { screenWidthDp.dp.toPx() }
    val screenHeightPx = with(localDensity) { configuration.screenHeightDp.dp.toPx() }

    val startScale = 240f / (screenWidthDp * 0.9f)

    val initialTx = if (cardFlipCenter != Offset.Zero) cardFlipCenter.x - screenWidthPx / 2f else 0f
    val initialTy = if (cardFlipCenter != Offset.Zero) cardFlipCenter.y - screenHeightPx / 2f else 0f

    val progress = remember { Animatable(0f) }
    val bgAlpha = remember { Animatable(0f) }
    val entryTranslationX = remember { Animatable(initialTx) }
    val entryTranslationY = remember { Animatable(initialTy) }
    var isDismissing by remember { mutableStateOf(false) }
    val dismissScale = remember { Animatable(1f) }
    val dismissTranslationX = remember { Animatable(0f) }
    val dismissTranslationY = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { bgAlpha.animateTo(1f, tween(1000, easing = FastOutSlowInEasing)) }
        launch { entryTranslationX.animateTo(0f, tween(800, easing = FastOutSlowInEasing)) }
        launch { entryTranslationY.animateTo(0f, tween(800, easing = FastOutSlowInEasing)) }
        delay(1600)
        progress.animateTo(1f, tween(2800, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(isDismissing) {
        if (isDismissing) {
            val overlayCardWidthPx = screenWidthPx * 0.9f
            val targetScale = if (cardFlipWidthPx > 0f) cardFlipWidthPx / overlayCardWidthPx else startScale
            val targetTx = if (cardFlipWidthPx > 0f) cardFlipCenter.x - screenWidthPx / 2f else 0f
            val targetTy = if (cardFlipWidthPx > 0f) cardFlipCenter.y - screenHeightPx / 2f else 0f

            launch { bgAlpha.animateTo(0f, tween(450, easing = FastOutSlowInEasing)) }
            launch { dismissTranslationX.animateTo(targetTx, tween(450, easing = FastOutSlowInEasing)) }
            launch { dismissTranslationY.animateTo(targetTy, tween(450, easing = FastOutSlowInEasing)) }
            dismissScale.animateTo(targetScale, tween(800, easing = FastOutSlowInEasing))
            onDismiss()
        }
    }

    val showFront = progress.value > 0.5f
    val animDone = progress.value >= 1f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(if (animDone && !isDismissing) Modifier.clickable(onClick = { isDismissing = true }) else Modifier)
    ) {
        Image(
            painter = painterResource(R.drawable.bg_galaxy),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(bgAlpha.value)
        )
        Box(modifier = Modifier.fillMaxSize().alpha(bgAlpha.value).background(Color.Black.copy(alpha = 0.45f)))

        val textAlpha = if (isDismissing) 0f else ((progress.value - 0.55f) * 3f).coerceIn(0f, 1f)
        val cardHeightDp = screenWidthDp * 0.9f * 1.5f
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = (configuration.screenHeightDp / 2f + cardHeightDp / 2f + 16f).dp)
                .alpha(textAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = card.name,
                style = MaterialTheme.typography.titleMedium,
                color = Or,
                textAlign = TextAlign.Center
            )
            Text(
                text = card.keyword,
                style = MaterialTheme.typography.bodyMedium,
                color = BlancNacre.copy(alpha = 0.70f),
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(2f / 3f)
                .align(Alignment.Center)
                .graphicsLayer {
                    translationX = if (isDismissing) dismissTranslationX.value else entryTranslationX.value
                    translationY = if (isDismissing) dismissTranslationY.value else entryTranslationY.value
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = if (isDismissing) dismissScale.value else startScale + (1f - startScale) * progress.value
                        scaleY = if (isDismissing) dismissScale.value else startScale + (1f - startScale) * progress.value
                        rotationY = progress.value * 180f
                        cameraDistance = 12f * density
                        shape = RoundedCornerShape(20.dp)
                        clip = true
                    }
            ) {
                if (!showFront) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(com.francekim.empreintedelame.ui.theme.BleuNuitSombre)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.archangel_back),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                        Image(
                            painter = painterResource(imageResId),
                            contentDescription = card.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
        if (animDone && !isDismissing) {
            Text(
                text = stringResource(R.string.archangel_card_reveal_hint),
                style = MaterialTheme.typography.bodySmall,
                color = BlancNacre.copy(alpha = 0.45f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
            )
        }
    }
}
