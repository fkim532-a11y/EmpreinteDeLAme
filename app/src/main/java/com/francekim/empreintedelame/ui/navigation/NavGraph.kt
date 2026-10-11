package com.francekim.empreintedelame.ui.navigation

import android.app.Application
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.ui.archangel.ArchangelScreen
import com.francekim.empreintedelame.ui.archangelcard.ArchangelCardHistoryScreen
import com.francekim.empreintedelame.ui.archangelcard.ArchangelCardScreen
import com.francekim.empreintedelame.ui.biorhythm.BiorhythmScreen
import com.francekim.empreintedelame.ui.compatibility.CompatibilityScreen
import com.francekim.empreintedelame.ui.mirrorsoul.MirrorSoulHistoryScreen
import com.francekim.empreintedelame.ui.mirrorsoul.MirrorSoulScreen
import com.francekim.empreintedelame.ui.destiny.DestinyScreen
import com.francekim.empreintedelame.ui.identity.IdentityScreen
import com.francekim.empreintedelame.ui.mirrorhours.MirrorHoursScreen
import com.francekim.empreintedelame.ui.portrait.PortraitScreen
import com.francekim.empreintedelame.ui.predictive.PredictiveScreen
import com.francekim.empreintedelame.ui.home.HomeScreen
import com.francekim.empreintedelame.ui.hdpredictions.HdPredictionsScreen
import com.francekim.empreintedelame.ui.humandesign.HumanDesignScreen
import com.francekim.empreintedelame.ui.yiking.YiKingScreen
import com.francekim.empreintedelame.ui.yiking.YiKingHistoryScreen
import com.francekim.empreintedelame.ui.intro.IntroScreen
import com.francekim.empreintedelame.ui.intro.IntroViewModel
import com.francekim.empreintedelame.ui.moduleintro.ModuleIntroScreen
import com.francekim.empreintedelame.ui.moduleintro.ModuleIntroViewModel
import com.francekim.empreintedelame.ui.moduleintro.moduleIntroContents
import com.francekim.empreintedelame.ui.common.LocalizedContent
import com.francekim.empreintedelame.ui.language.AppLanguageViewModel
import com.francekim.empreintedelame.ui.onboarding.OnboardingScreen
import com.francekim.empreintedelame.ui.onboarding.OnboardingViewModel
import com.francekim.empreintedelame.ui.practices.PracticesScreen
import com.francekim.empreintedelame.ui.tabs.MeScreen
import com.francekim.empreintedelame.ui.tabs.MoreScreen

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val vm: OnboardingViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )
    val introVm: IntroViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )
    val languageVm: AppLanguageViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )
    val profile by vm.profile.collectAsState()
    val isReady by vm.isReady.collectAsState()
    val introSeen by introVm.introSeen.collectAsState()
    val introIsReady by introVm.isReady.collectAsState()
    val language by languageVm.language.collectAsState()

    // Tant que Room / DataStore n'ont pas répondu, ne pas composer le NavHost : sinon le
    // startDestination choisi trop tôt (avant la lecture) provoque un flash
    // d'écran au démarrage quand la lecture révèle ensuite un profil existant.
    if (!isReady || !introIsReady) return

    val postIntroRoute = if (profile == null) Screen.Onboarding.route else Screen.Home.route
    val start = if (!introSeen) Screen.Intro.route else postIntroRoute

    LocalizedContent(languageCode = language) {
    NavHost(
        navController      = navController,
        startDestination   = start,
        enterTransition    = { fadeIn(animationSpec = tween(1000)) },
        exitTransition     = { fadeOut(animationSpec = tween(1000)) },
        popEnterTransition = { fadeIn(animationSpec = tween(1000)) },
        popExitTransition  = { fadeOut(animationSpec = tween(1000)) }
    ) {
        composable(Screen.Intro.route) {
            IntroScreen(onFinished = {
                introVm.markIntroSeen()
                navController.navigate(postIntroRoute) {
                    popUpTo(Screen.Intro.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController, vm = vm)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.NumerologyLifePath.route) {
            DestinyScreen(navController = navController)
        }
        composable(Screen.NumerologyPortrait.route) {
            PortraitScreen(navController = navController)
        }
        composable(Screen.NumerologyPredictive.route) {
            PredictiveScreen(navController = navController)
        }
        composable(Screen.Identity.route) {
            IdentityScreen(navController = navController)
        }
        composable(Screen.MirrorHours.route) {
            MirrorHoursScreen(navController = navController)
        }
        composable(Screen.Archangel.route) {
            ArchangelScreen(navController = navController)
        }
        composable(Screen.ArchangelCard.route) {
            ArchangelCardScreen(navController = navController)
        }
        composable(Screen.ArchangelCardHistory.route) {
            ArchangelCardHistoryScreen(navController = navController)
        }
        composable(Screen.HumanDesign.route) {
            HumanDesignScreen(navController = navController)
        }
        composable(Screen.HdPredictions.route) {
            HdPredictionsScreen(navController = navController)
        }
        composable(
            route     = Screen.ModuleIntro.route,
            arguments = listOf(
                navArgument("moduleId") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId")
            val mode     = backStackEntry.arguments?.getString("mode") ?: "first"
            val content  = moduleIntroContents[moduleId]
            if (content != null) {
                val moduleIntroVm: ModuleIntroViewModel = viewModel(
                    factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
                        .getInstance(context.applicationContext as Application)
                )
                ModuleIntroScreen(
                    title       = stringResource(content.titleRes),
                    body        = stringResource(content.bodyRes),
                    iconRes     = content.iconRes,
                    buttonLabel = stringResource(R.string.module_intro_continue_button),
                    onContinue  = {
                        if (mode == "first") {
                            moduleIntroVm.markSeen(moduleId!!)
                            navController.navigate(content.targetRoute) {
                                popUpTo(Screen.ModuleIntro.route) { inclusive = true }
                            }
                        } else {
                            navController.popBackStack()
                        }
                    }
                )
            }
        }
        composable(Screen.YiKing.route) {
            YiKingScreen(navController = navController)
        }
        composable(Screen.YiKingHistory.route) {
            YiKingHistoryScreen(navController = navController)
        }
        composable(Screen.Practices.route) {
            PracticesScreen(navController = navController)
        }
        composable(Screen.Compatibility.route) {
            CompatibilityScreen(navController = navController)
        }
        composable(Screen.Biorhythm.route) {
            BiorhythmScreen(navController = navController)
        }
        composable(Screen.MirrorSoul.route) {
            MirrorSoulScreen(navController = navController)
        }
        composable(Screen.MirrorSoulHistory.route) {
            MirrorSoulHistoryScreen(navController = navController)
        }
        composable(
            route     = Screen.Me.route,
            arguments = listOf(navArgument("slot") { type = NavType.IntType; defaultValue = 1 })
        ) { backStackEntry ->
            val slot = backStackEntry.arguments?.getInt("slot") ?: 1
            MeScreen(navController = navController, vm = vm, initialSlot = slot)
        }
        composable(Screen.More.route) {
            MoreScreen(navController = navController, languageVm = languageVm)
        }
    }
    }
}
