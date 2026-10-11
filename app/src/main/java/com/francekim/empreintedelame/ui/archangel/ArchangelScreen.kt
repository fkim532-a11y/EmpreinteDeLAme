package com.francekim.empreintedelame.ui.archangel

import android.app.Application
import android.graphics.BitmapFactory
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.francekim.empreintedelame.domain.calculator.Archangel
import com.francekim.empreintedelame.domain.model.ArchangelInfo
import com.francekim.empreintedelame.ui.common.shareImage
import com.francekim.empreintedelame.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ArchangelScreen(navController: NavController) {
    val context = LocalContext.current
    val vm: ArchangelViewModel = viewModel(
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
                Text(
                    text      = stringResource(R.string.module_archangel),
                    style     = MaterialTheme.typography.titleLarge,
                    color     = Or,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.weight(1f))
                if (state is ArchangelUiState.Ready) {
                    val ready = state as ArchangelUiState.Ready
                    TextButton(onClick = {
                        val caption = buildArchangelShareText(
                            context   = context,
                            firstName = ready.firstName,
                            info      = ready.info
                        )
                        val bitmap = BitmapFactory.decodeResource(context.resources, ready.archangel.imageRes)
                        shareImage(context, bitmap, caption)
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
            if (state is ArchangelUiState.Ready) {
                Text(
                    text      = stringResource(R.string.reading_screen_subtitle, (state as ArchangelUiState.Ready).firstName.split(" ", "-").first()),
                    style     = MaterialTheme.typography.labelMedium,
                    color     = BlancNacre.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )
            }

            // ── Contenu selon l'état ───────────────────────────────────────────
            when (state) {
                is ArchangelUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Or)
                    }
                }
                is ArchangelUiState.NoProfile -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text       = stringResource(R.string.archangel_no_profile),
                            style      = MaterialTheme.typography.bodyLarge,
                            color      = BlancNacre.copy(alpha = 0.70f),
                            textAlign  = TextAlign.Center,
                            lineHeight = 28.sp
                        )
                    }
                }
                is ArchangelUiState.Ready -> {
                    val ready = state as ArchangelUiState.Ready
                    ArchangelContent(
                        archangel = ready.archangel,
                        info = ready.info,
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
private fun ArchangelContent(
    archangel: Archangel,
    info     : ArchangelInfo?,
    modifier : Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text  = stringResource(R.string.archangel_subtitle),
            style = MaterialTheme.typography.labelMedium,
            color = BlancNacre.copy(alpha = 0.55f)
        )
        Spacer(Modifier.height(16.dp))

        ArchangelFlipCard(archangel = archangel)

        Spacer(Modifier.height(20.dp))
        Text(
            text      = info?.name ?: "",
            fontSize  = 44.sp,
            color     = Or,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text  = info?.keyword ?: "",
            style = MaterialTheme.typography.titleMedium,
            color = VioletMoyen.copy(alpha = 0.90f)
        )

        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        SectionTitle(
            text    = stringResource(R.string.archangel_domain_section),
            caption = stringResource(R.string.archangel_domain_info)
        )
        Spacer(Modifier.height(12.dp))
        BodyText(info?.domain)

        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        SectionTitle(
            text    = stringResource(R.string.archangel_message_section),
            caption = stringResource(R.string.archangel_message_info)
        )
        Spacer(Modifier.height(12.dp))
        BodyText(info?.description)

        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        SectionTitle(
            text    = stringResource(R.string.archangel_invocation_section),
            caption = stringResource(R.string.archangel_invocation_info)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text      = info?.invocation ?: "",
            style     = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color     = BlancNacre.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))
    }
}

// ─── Carte animée (dos → face) ─────────────────────────────────────────────────

@Composable
private fun ArchangelFlipCard(archangel: Archangel) {
    var isFlipped by remember(archangel) { mutableStateOf(false) }

    LaunchedEffect(archangel) {
        isFlipped = false
        delay(1500)
        isFlipped = true
    }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "archangelCardFlip"
    )

    Box(
        modifier = Modifier
            .width(240.dp)
            .height(391.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(16.dp))
    ) {
        if (rotation <= 90f) {
            Image(
                painter = painterResource(R.drawable.archangel_back),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(archangel.imageRes),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
            )
        }
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

private fun buildArchangelShareText(
    context   : android.content.Context,
    firstName : String,
    info      : ArchangelInfo?
): String {
    val sb = StringBuilder()
    sb.appendLine(context.getString(R.string.share_archangel_title, firstName.split(" ", "-").first()))
    sb.appendLine()

    if (!info?.name.isNullOrBlank()) {
        sb.appendLine("${info?.name} — ${info?.keyword}")
        sb.appendLine()
    }

    if (!info?.domain.isNullOrBlank()) {
        sb.appendLine("🕊️ " + context.getString(R.string.archangel_domain_section).uppercase())
        sb.appendLine(info?.domain)
        sb.appendLine()
    }

    if (!info?.description.isNullOrBlank()) {
        sb.appendLine("💬 " + context.getString(R.string.archangel_message_section).uppercase())
        sb.appendLine(info?.description)
        sb.appendLine()
    }

    if (!info?.invocation.isNullOrBlank()) {
        sb.appendLine("🙏 " + context.getString(R.string.archangel_invocation_section).uppercase())
        sb.appendLine(info?.invocation)
        sb.appendLine()
    }

    sb.appendLine(context.getString(R.string.share_destiny_footer, context.getString(R.string.app_name)))

    return sb.toString().trim()
}
