package com.francekim.empreintedelame.ui.moduleintro

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.ui.navigation.Screen

/** Identifiants stables des modules ayant une page d'explication (persistés en DataStore). */
object ModuleIntroIds {
    const val LIFEPATH       = "lifepath"
    const val PORTRAIT       = "portrait"
    const val PREDICTIVE     = "predictive"
    const val IDENTITY       = "identity"
    const val HUMAN_DESIGN   = "human_design"
    const val HD_PREDICTIONS = "hd_predictions"
    const val BIORHYTHM      = "biorhythm"
    const val MIRROR_SOUL    = "mirror_soul"
}

data class ModuleIntroData(
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int,
    @DrawableRes val iconRes: Int,
    val targetRoute: String
)

val moduleIntroContents: Map<String, ModuleIntroData> = mapOf(
    ModuleIntroIds.LIFEPATH to ModuleIntroData(
        titleRes    = R.string.module_intro_lifepath_title,
        bodyRes     = R.string.module_intro_lifepath_body,
        iconRes     = R.drawable.ic_module_lifepath,
        targetRoute = Screen.NumerologyLifePath.route
    ),
    ModuleIntroIds.PORTRAIT to ModuleIntroData(
        titleRes    = R.string.module_intro_portrait_title,
        bodyRes     = R.string.module_intro_portrait_body,
        iconRes     = R.drawable.ic_module_portrait,
        targetRoute = Screen.NumerologyPortrait.route
    ),
    ModuleIntroIds.PREDICTIVE to ModuleIntroData(
        titleRes    = R.string.module_intro_predictive_title,
        bodyRes     = R.string.module_intro_predictive_body,
        iconRes     = R.drawable.ic_module_predictive,
        targetRoute = Screen.NumerologyPredictive.route
    ),
    ModuleIntroIds.IDENTITY to ModuleIntroData(
        titleRes    = R.string.module_intro_identity_title,
        bodyRes     = R.string.module_intro_identity_body,
        iconRes     = R.drawable.ic_module_identity,
        targetRoute = Screen.Identity.route
    ),
    ModuleIntroIds.HUMAN_DESIGN to ModuleIntroData(
        titleRes    = R.string.module_intro_human_design_title,
        bodyRes     = R.string.module_intro_human_design_body,
        iconRes     = R.drawable.ic_module_humandesign,
        targetRoute = Screen.HumanDesign.route
    ),
    ModuleIntroIds.HD_PREDICTIONS to ModuleIntroData(
        titleRes    = R.string.module_intro_hd_predictions_title,
        bodyRes     = R.string.module_intro_hd_predictions_body,
        iconRes     = R.drawable.ic_module_crystals,
        targetRoute = Screen.HdPredictions.route
    ),
    ModuleIntroIds.BIORHYTHM to ModuleIntroData(
        titleRes    = R.string.module_intro_biorhythm_title,
        bodyRes     = R.string.module_intro_biorhythm_body,
        iconRes     = R.drawable.ic_module_biorhythm,
        targetRoute = Screen.Biorhythm.route
    ),
    ModuleIntroIds.MIRROR_SOUL to ModuleIntroData(
        titleRes    = R.string.module_intro_mirror_soul_title,
        bodyRes     = R.string.module_intro_mirror_soul_body,
        iconRes     = R.drawable.ic_module_mirrorsoul,
        targetRoute = Screen.MirrorSoul.route
    )
)
