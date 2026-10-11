package com.francekim.empreintedelame.ui.navigation

sealed class Screen(val route: String) {
    object Intro               : Screen("intro")
    object Onboarding          : Screen("onboarding")
    object Home                : Screen("home")
    object NumerologyLifePath  : Screen("numerology_lifepath")
    object NumerologyPortrait  : Screen("numerology_portrait")
    object NumerologyPredictive: Screen("numerology_predictive")
    object MirrorHours         : Screen("mirror_hours")
    object Identity            : Screen("identity")
    object Archangel           : Screen("archangel")
    object ArchangelCard       : Screen("archangel_card")
    object ArchangelCardHistory: Screen("archangel_card_history")
    object HumanDesign         : Screen("human_design")
    object HdPredictions       : Screen("hd_predictions")
    object ModuleIntro         : Screen("module_intro/{moduleId}/{mode}") {
        fun route(moduleId: String, mode: String) = "module_intro/$moduleId/$mode"
    }
    object YiKing              : Screen("yi_king")
    object YiKingHistory       : Screen("yi_king_history")
    object Practices           : Screen("practices")
    object Compatibility       : Screen("compatibility")
    object Biorhythm           : Screen("biorhythm")
    object MirrorSoul          : Screen("mirror_soul")
    object MirrorSoulHistory   : Screen("mirror_soul_history")
    object Me                  : Screen("me?slot={slot}") {
        fun route(slot: Int) = "me?slot=$slot"
    }
    object More                : Screen("more")
}
