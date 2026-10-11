package com.francekim.empreintedelame.ui.common

import android.content.res.Configuration
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Force la langue de l'appli (fr/en) indépendamment de celle du téléphone.
 * [languageCode] à null = suit la langue du téléphone (comportement par défaut).
 */
@Composable
fun LocalizedContent(languageCode: String?, content: @Composable () -> Unit) {
    val baseContext = LocalContext.current
    val configuration = LocalConfiguration.current
    // Capturé avant le remplacement de LocalContext ci-dessous : le contexte reconfiguré
    // n'est plus un ContextWrapper, donc rememberLauncherForActivityResult() ne peut plus
    // retrouver l'Activity tout seul (findOwner() casse) → on le réinjecte explicitement.
    val activityResultRegistryOwner = LocalActivityResultRegistryOwner.current

    // Les dépôts de contenu (interprétations, Yi-King, etc.) lisent Locale.getDefault()
    // plutôt que ce Context reconfiguré — on l'aligne ici pour qu'ils suivent eux aussi
    // le sélecteur de langue in-app, pas seulement les textes d'interface.
    LaunchedEffect(languageCode) {
        if (languageCode != null) {
            Locale.setDefault(Locale(languageCode))
        }
    }

    val localizedContext = remember(languageCode, configuration) {
        if (languageCode == null) {
            baseContext
        } else {
            val locale = Locale(languageCode)
            val newConfig = Configuration(configuration)
            newConfig.setLocale(locale)
            baseContext.createConfigurationContext(newConfig)
        }
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalActivityResultRegistryOwner provides requireNotNull(activityResultRegistryOwner)
    ) {
        content()
    }
}
