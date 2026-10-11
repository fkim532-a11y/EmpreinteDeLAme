package com.francekim.empreintedelame.ui.humandesign

import androidx.annotation.StringRes
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.domain.calculator.humandesign.Center

// Nom affiché de chaque centre du Human Design, partagé entre l'écran Human Design
// et l'écran Compatibilité (les noms complets du contenu JSON, ex. "G (Identité)",
// sont trop longs pour ces deux usages).
@StringRes
fun centerLabelRes(center: Center): Int = when (center) {
    Center.HEAD         -> R.string.human_design_center_head
    Center.AJNA         -> R.string.human_design_center_ajna
    Center.THROAT       -> R.string.human_design_center_throat
    Center.G            -> R.string.human_design_center_g
    Center.HEART        -> R.string.human_design_center_heart
    Center.SACRAL       -> R.string.human_design_center_sacral
    Center.SOLAR_PLEXUS -> R.string.human_design_center_solar_plexus
    Center.SPLEEN       -> R.string.human_design_center_spleen
    Center.ROOT         -> R.string.human_design_center_root
}

// Même chose, en plus court là où la place est comptée : dans les losanges du
// schéma du Bodygraph. Seul le Plexus Solaire a besoin d'être raccourci.
@StringRes
fun centerShortLabelRes(center: Center): Int =
    if (center == Center.SOLAR_PLEXUS) R.string.human_design_center_solar_plexus_short
    else centerLabelRes(center)
