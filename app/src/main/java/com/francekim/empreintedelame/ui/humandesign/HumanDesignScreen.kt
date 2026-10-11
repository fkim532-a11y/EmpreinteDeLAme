package com.francekim.empreintedelame.ui.humandesign

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.data.model.HumanDesignContent
import com.francekim.empreintedelame.domain.calculator.humandesign.Center
import com.francekim.empreintedelame.domain.calculator.humandesign.HumanDesignChart
import com.francekim.empreintedelame.ui.common.PDF_IMAGE_MARKER
import com.francekim.empreintedelame.ui.common.ModuleInfoButton
import com.francekim.empreintedelame.ui.common.sharePdf
import com.francekim.empreintedelame.ui.moduleintro.ModuleIntroIds
import com.francekim.empreintedelame.ui.navigation.Screen
import com.francekim.empreintedelame.ui.theme.*
import kotlinx.coroutines.launch

private val CENTER_ORDER = listOf(
    Center.HEAD, Center.AJNA, Center.THROAT, Center.G, Center.HEART,
    Center.SACRAL, Center.SOLAR_PLEXUS, Center.SPLEEN, Center.ROOT
)

@Composable
fun HumanDesignScreen(navController: NavController) {
    val context = LocalContext.current
    val vm: HumanDesignViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )
    val state by vm.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val bodygraphLayer = rememberGraphicsLayer()

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
                        text      = stringResource(R.string.module_human_design),
                        style     = MaterialTheme.typography.titleLarge,
                        color     = Or,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.width(6.dp))
                    ModuleInfoButton(onClick = {
                        navController.navigate(Screen.ModuleIntro.route(ModuleIntroIds.HUMAN_DESIGN, "revisit"))
                    })
                }
                Spacer(Modifier.weight(1f))
                if (state is HumanDesignUiState.Ready) {
                    val ready = state as HumanDesignUiState.Ready
                    TextButton(onClick = {
                        val text = buildHumanDesignShareText(
                            context   = context,
                            firstName = ready.firstName,
                            chart     = ready.chart,
                            content   = ready.content
                        )
                        scope.launch {
                            val bitmap = bodygraphLayer.toImageBitmap().asAndroidBitmap()
                            val centersHeaderLine = "✨ " + context.getString(R.string.human_design_centers_section).uppercase()
                            sharePdf(context, "human_design", text, bitmap, pageBreakBeforeLine = centersHeaderLine)
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
            if (state is HumanDesignUiState.Ready) {
                Text(
                    text      = stringResource(R.string.reading_screen_subtitle, (state as HumanDesignUiState.Ready).firstName.split(" ", "-").first()),
                    style     = MaterialTheme.typography.labelMedium,
                    color     = BlancNacre.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )
            }

            when (state) {
                is HumanDesignUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Or)
                    }
                }
                is HumanDesignUiState.NoProfile -> {
                    CenteredMessage(stringResource(R.string.human_design_no_profile))
                }
                is HumanDesignUiState.BirthTimeUnknown -> {
                    CenteredMessage(stringResource(R.string.human_design_birthtime_unknown))
                }
                is HumanDesignUiState.Ready -> {
                    val ready = state as HumanDesignUiState.Ready
                    HumanDesignContent(
                        chart          = ready.chart,
                        content        = ready.content,
                        bodygraphLayer = bodygraphLayer,
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

@Composable
private fun CenteredMessage(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text       = text,
            style      = MaterialTheme.typography.bodyLarge,
            color      = BlancNacre.copy(alpha = 0.70f),
            textAlign  = TextAlign.Center,
            lineHeight = 28.sp
        )
    }
}

// ─── Contenu principal ─────────────────────────────────────────────────────────

@Composable
private fun HumanDesignContent(
    chart          : HumanDesignChart,
    content        : HumanDesignContent,
    bodygraphLayer : GraphicsLayer,
    modifier       : Modifier = Modifier
) {
    val typeInfo       = content.types[chart.type.name]
    val authorityInfo  = content.authorities[chart.authority.name]
    val profileInfo    = content.profiles[chart.profile]
    val definitionInfo = content.definitions[chart.definition.name]

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text  = stringResource(R.string.human_design_subtitle),
            style = MaterialTheme.typography.labelMedium,
            color = BlancNacre.copy(alpha = 0.55f)
        )
        Spacer(Modifier.height(16.dp))

        Text(
            text      = typeInfo?.name ?: "",
            fontSize  = 40.sp,
            color     = Or,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text  = typeInfo?.keyword ?: "",
            style = MaterialTheme.typography.titleMedium,
            color = VioletMoyen.copy(alpha = 0.90f)
        )

        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        SectionTitle(
            text    = stringResource(R.string.human_design_strategy_section),
            caption = stringResource(R.string.human_design_strategy_info)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = typeInfo?.strategy ?: "",
            style     = MaterialTheme.typography.titleSmall,
            color     = VioletMoyen.copy(alpha = 0.90f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        BodyText(typeInfo?.description)

        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        SectionTitle(
            text    = stringResource(R.string.human_design_notself_section),
            caption = stringResource(R.string.human_design_notself_info)
        )
        Spacer(Modifier.height(12.dp))
        BodyText(typeInfo?.notSelfTheme)

        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        SectionTitle(
            text    = stringResource(R.string.human_design_authority_section),
            caption = stringResource(R.string.human_design_authority_info)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = authorityInfo?.name ?: "",
            style     = MaterialTheme.typography.titleMedium,
            color     = Or.copy(alpha = 0.90f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text      = authorityInfo?.keyword ?: "",
            style     = MaterialTheme.typography.labelMedium,
            color     = VioletMoyen.copy(alpha = 0.80f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        BodyText(authorityInfo?.description)

        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        SectionTitle(
            text    = stringResource(R.string.human_design_profile_section),
            caption = stringResource(R.string.human_design_profile_info)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = profileInfo?.name ?: "",
            style     = MaterialTheme.typography.titleMedium,
            color     = Or.copy(alpha = 0.90f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        BodyText(profileInfo?.description)

        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        SectionTitle(
            text    = stringResource(R.string.human_design_definition_section),
            caption = stringResource(R.string.human_design_definition_info)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = definitionInfo?.name ?: "",
            style     = MaterialTheme.typography.titleMedium,
            color     = Or.copy(alpha = 0.90f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        BodyText(definitionInfo?.description)

        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        Text(
            text          = stringResource(R.string.human_design_centers_section).uppercase(),
            style         = MaterialTheme.typography.labelMedium,
            color         = BlancNacre.copy(alpha = 0.85f),
            letterSpacing = 4.sp,
            textAlign     = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text      = stringResource(R.string.human_design_centers_info),
            style     = MaterialTheme.typography.bodySmall,
            color     = Or.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text      = stringResource(R.string.human_design_bodygraph_legend),
            style     = MaterialTheme.typography.labelSmall,
            color     = BlancNacre.copy(alpha = 0.50f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawWithContent {
                    bodygraphLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(bodygraphLayer)
                }
        ) {
            HumanDesignBodygraph(
                chart    = chart,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(20.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CENTER_ORDER.forEach { center ->
                val info = content.centers[center.name]
                val isDefined = center in chart.definedCenters
                CenterRow(
                    name        = info?.name ?: center.name,
                    isDefined   = isDefined,
                    description = if (isDefined) info?.definedDescription else info?.undefinedDescription
                )
            }
        }

        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        SectionTitle(
            text    = stringResource(R.string.human_design_channels_section),
            caption = stringResource(R.string.human_design_channels_info)
        )
        Spacer(Modifier.height(16.dp))
        if (chart.definedChannels.isEmpty()) {
            Text(
                text      = stringResource(R.string.human_design_no_channels),
                style     = MaterialTheme.typography.bodySmall,
                color     = BlancNacre.copy(alpha = 0.40f),
                textAlign = TextAlign.Center
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                chart.definedChannels.forEach { channel ->
                    val info = content.channels["${channel.gateA}-${channel.gateB}"]
                    NamedInterpretationBlock(
                        name        = info?.name ?: "${channel.gateA}-${channel.gateB}",
                        keyword     = info?.keyword,
                        description = info?.description
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        SectionDivider()
        Spacer(Modifier.height(28.dp))

        SectionTitle(
            text    = stringResource(R.string.human_design_gates_section),
            caption = stringResource(R.string.human_design_gates_info)
        )
        Spacer(Modifier.height(16.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            chart.activeGates.sorted().forEach { gate ->
                val info = content.gates[gate]
                NamedInterpretationBlock(
                    name        = "$gate — ${info?.name ?: ""}",
                    keyword     = info?.keyword,
                    description = info?.description
                )
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun CenterRow(name: String, isDefined: Boolean, description: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text  = name,
            style = MaterialTheme.typography.titleSmall,
            color = Or.copy(alpha = 0.90f)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text  = stringResource(
                if (isDefined) R.string.human_design_center_defined
                else R.string.human_design_center_undefined
            ),
            style = MaterialTheme.typography.labelSmall,
            color = if (isDefined) Or else VioletMoyen.copy(alpha = 0.80f)
        )
        Spacer(Modifier.height(6.dp))
        BodyText(description)
    }
}

@Composable
private fun NamedInterpretationBlock(name: String, keyword: String?, description: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text  = name,
            style = MaterialTheme.typography.titleSmall,
            color = Or.copy(alpha = 0.90f),
            textAlign = TextAlign.Center
        )
        if (!keyword.isNullOrBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text  = keyword,
                style = MaterialTheme.typography.labelSmall,
                color = VioletMoyen.copy(alpha = 0.80f),
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(6.dp))
        BodyText(description)
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

private fun buildHumanDesignShareText(
    context   : android.content.Context,
    firstName : String,
    chart     : HumanDesignChart,
    content   : HumanDesignContent
): String {
    val typeInfo       = content.types[chart.type.name]
    val authorityInfo  = content.authorities[chart.authority.name]
    val profileInfo    = content.profiles[chart.profile]
    val definitionInfo = content.definitions[chart.definition.name]

    val sb = StringBuilder()
    sb.appendLine(context.getString(R.string.share_human_design_title, firstName.split(" ", "-").first()))
    sb.appendLine()

    sb.appendLine("🔮 " + context.getString(R.string.human_design_strategy_section).uppercase())
    sb.appendLine(typeInfo?.name ?: "")
    if (!typeInfo?.strategy.isNullOrBlank()) sb.appendLine(typeInfo?.strategy)
    if (!typeInfo?.description.isNullOrBlank()) sb.appendLine(typeInfo?.description)
    sb.appendLine()

    sb.appendLine("⚡ " + context.getString(R.string.human_design_authority_section).uppercase())
    sb.appendLine(authorityInfo?.name ?: "")
    if (!authorityInfo?.description.isNullOrBlank()) sb.appendLine(authorityInfo?.description)
    sb.appendLine()

    sb.appendLine("👤 " + context.getString(R.string.human_design_profile_section).uppercase())
    sb.appendLine(profileInfo?.name ?: "")
    if (!profileInfo?.description.isNullOrBlank()) sb.appendLine(profileInfo?.description)
    sb.appendLine()

    sb.appendLine("🌐 " + context.getString(R.string.human_design_definition_section).uppercase())
    sb.appendLine(definitionInfo?.name ?: "")
    if (!definitionInfo?.description.isNullOrBlank()) sb.appendLine(definitionInfo?.description)
    sb.appendLine()

    sb.appendLine("✨ " + context.getString(R.string.human_design_centers_section).uppercase())
    sb.appendLine(context.getString(R.string.human_design_bodygraph_legend))
    sb.appendLine(PDF_IMAGE_MARKER)
    sb.appendLine()
    CENTER_ORDER.forEach { center ->
        val info = content.centers[center.name]
        val isDefined = center in chart.definedCenters
        val state = context.getString(
            if (isDefined) R.string.human_design_center_defined
            else R.string.human_design_center_undefined
        )
        val description = if (isDefined) info?.definedDescription else info?.undefinedDescription
        sb.appendLine("${info?.name ?: center.name} : $state")
        if (!description.isNullOrBlank()) sb.appendLine(description)
        sb.appendLine()
    }

    if (chart.definedChannels.isNotEmpty()) {
        sb.appendLine("🔗 " + context.getString(R.string.human_design_channels_section).uppercase())
        sb.appendLine()
        chart.definedChannels.forEach { channel ->
            val info = content.channels["${channel.gateA}-${channel.gateB}"]
            val name = info?.name ?: "${channel.gateA}-${channel.gateB}"
            sb.appendLine(if (!info?.keyword.isNullOrBlank()) "$name — ${info?.keyword}" else name)
            if (!info?.description.isNullOrBlank()) sb.appendLine(info?.description)
            sb.appendLine()
        }
    }

    sb.appendLine("🚪 " + context.getString(R.string.human_design_gates_section).uppercase())
    sb.appendLine()
    chart.activeGates.sorted().forEach { gate ->
        val info = content.gates[gate]
        val name = "$gate — ${info?.name ?: ""}"
        sb.appendLine(if (!info?.keyword.isNullOrBlank()) "$name — ${info?.keyword}" else name)
        if (!info?.description.isNullOrBlank()) sb.appendLine(info?.description)
        sb.appendLine()
    }

    sb.appendLine(context.getString(R.string.share_destiny_footer, context.getString(R.string.app_name)))

    return sb.toString().trim()
}
