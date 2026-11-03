package com.francekim.empreintedelame.ui.tabs

import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.navigation.NavController
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.ui.language.AppLanguageViewModel
import com.francekim.empreintedelame.ui.onboarding.OnboardingViewModel
import com.francekim.empreintedelame.ui.theme.BlancNacre
import com.francekim.empreintedelame.ui.theme.Or
import com.francekim.empreintedelame.ui.theme.OrDore
import com.francekim.empreintedelame.ui.theme.VioletMoyen
import com.francekim.empreintedelame.ui.theme.VioletNuit

@Composable
fun MeScreen(navController: NavController, vm: OnboardingViewModel, initialSlot: Int = 1) {
    com.francekim.empreintedelame.ui.onboarding.MeScreen(navController = navController, vm = vm, initialSlot = initialSlot)
}

private const val WEBSITE_URL = "https://fkim532-a11y.github.io"
private const val KOFI_URL     = "https://ko-fi.com/franckim"
private const val PAYPAL_URL   = "https://paypal.me/franckim64"
private const val PRIVACY_URL  = "https://fkim532-a11y.github.io/tarots-legal/empreinte/privacy.html"

@Composable
fun MoreScreen(navController: NavController, languageVm: AppLanguageViewModel) {
    val context = LocalContext.current
    val language by languageVm.language.collectAsState()
    val currentLanguage = language ?: java.util.Locale.getDefault().language
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
            TextButton(
                onClick = {
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.padding(start = 0.dp, top = 4.dp)
            ) {
                Text("←", color = Or, style = MaterialTheme.typography.headlineMedium)
            }

            Text(
                text      = stringResource(R.string.more_title),
                style     = MaterialTheme.typography.titleMedium,
                color     = OrDore,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Spacer(Modifier.height(12.dp))

                SectionHeader(stringResource(R.string.more_language_section))
                Spacer(Modifier.height(4.dp))
                LanguageCard(
                    selected = currentLanguage,
                    onSelect = { lang -> languageVm.setLanguage(lang) }
                )

                Spacer(Modifier.height(40.dp))

                SectionHeader(stringResource(R.string.more_support_section))
                Spacer(Modifier.height(4.dp))
                SupportCard(
                    onKofiClick   = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(KOFI_URL)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) },
                    onPaypalClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PAYPAL_URL)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                )

                Spacer(Modifier.height(40.dp))

                SectionHeader(stringResource(R.string.more_links_section))
                Spacer(Modifier.height(4.dp))
                SupportButton(
                    label   = stringResource(R.string.more_website_label),
                    onClick = {
                        val url = if (currentLanguage == "en") "$WEBSITE_URL/en.html" else WEBSITE_URL
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                )

                Spacer(Modifier.height(40.dp))

                SectionHeader(stringResource(R.string.more_privacy_section))
                Spacer(Modifier.height(4.dp))
                SupportButton(
                    label   = stringResource(R.string.more_privacy_label),
                    onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_URL)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                )

                Spacer(Modifier.height(40.dp))

                Text(
                    text      = stringResource(
                        R.string.more_version,
                        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
                    ),
                    style     = MaterialTheme.typography.labelSmall,
                    color     = BlancNacre.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = OrDore.copy(alpha = 0.35f))
        Text(
            text  = "  ✦ $title ✦  ",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
            color = OrDore
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = OrDore.copy(alpha = 0.35f))
    }
}

@Composable
private fun LanguageCard(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(VioletNuit.copy(alpha = 0.45f))
            .border(1.dp, Or.copy(alpha = 0.20f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LanguageButton(
            label    = stringResource(R.string.language_fr),
            selected = selected == "fr",
            modifier = Modifier.weight(1f),
            onClick  = { onSelect("fr") }
        )
        LanguageButton(
            label    = stringResource(R.string.language_en),
            selected = selected == "en",
            modifier = Modifier.weight(1f),
            onClick  = { onSelect("en") }
        )
    }
}

@Composable
private fun LanguageButton(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) VioletMoyen.copy(alpha = 0.35f) else Color.Transparent)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) OrDore else OrDore.copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = label,
            color = if (selected) OrDore else BlancNacre.copy(alpha = 0.75f),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun SupportCard(onKofiClick: () -> Unit, onPaypalClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text      = stringResource(R.string.donation_description),
            style     = MaterialTheme.typography.bodyMedium,
            color     = BlancNacre.copy(alpha = 0.80f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        SupportButton(
            label   = stringResource(R.string.donation_button),
            flag    = "🇬🇧",
            onClick = onKofiClick
        )
        Spacer(Modifier.height(12.dp))
        SupportButton(
            label   = stringResource(R.string.donation_button_paypal),
            flag    = "🇫🇷",
            onClick = onPaypalClick
        )
    }
}

@Composable
private fun SupportButton(label: String, flag: String? = null, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(OrDore.copy(alpha = 0.10f))
            .border(1.dp, OrDore.copy(alpha = 0.55f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = if (flag != null) "$flag $label" else label,
            style = MaterialTheme.typography.bodyMedium,
            color = OrDore
        )
    }
}

