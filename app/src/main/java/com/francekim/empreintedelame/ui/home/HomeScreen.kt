package com.francekim.empreintedelame.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.ui.moduleintro.ModuleIntroIds
import com.francekim.empreintedelame.ui.moduleintro.ModuleIntroViewModel
import com.francekim.empreintedelame.ui.navigation.Screen
import com.francekim.empreintedelame.ui.theme.*

// ─── Données ─────────────────────────────────────────────────────────────────

private data class ModuleCard(
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    val route: String,
    val overflowImage: Boolean = false,
    val imageScale: Float = 1f,
    val introId: String? = null
)

private val modules = listOf(
    ModuleCard(R.drawable.ic_module_lifepath,      R.string.module_lifepath,      Screen.NumerologyLifePath.route,   introId = ModuleIntroIds.LIFEPATH),
    ModuleCard(R.drawable.ic_module_portrait,      R.string.module_portrait,      Screen.NumerologyPortrait.route,   introId = ModuleIntroIds.PORTRAIT),
    ModuleCard(R.drawable.ic_module_predictive,    R.string.module_predictive,    Screen.NumerologyPredictive.route, introId = ModuleIntroIds.PREDICTIVE),
    ModuleCard(R.drawable.ic_module_identity,      R.string.module_identity,      Screen.Identity.route,             introId = ModuleIntroIds.IDENTITY),
    ModuleCard(R.drawable.ic_module_archangel,     R.string.module_archangel,     Screen.Archangel.route,    overflowImage = true),
    ModuleCard(R.drawable.ic_module_archangelcard, R.string.module_archangel_card,Screen.ArchangelCard.route,imageScale = 0.78f),
    ModuleCard(R.drawable.ic_module_mirrorhours,   R.string.module_mirror_hours_grid,  Screen.MirrorHours.route),
    ModuleCard(R.drawable.ic_module_humandesign,   R.string.module_human_design_grid,  Screen.HumanDesign.route,      introId = ModuleIntroIds.HUMAN_DESIGN),
    ModuleCard(R.drawable.ic_module_crystals,      R.string.module_hd_predictions_grid,Screen.HdPredictions.route,   introId = ModuleIntroIds.HD_PREDICTIONS),
)

// Modules cachés sous le pli, révélés uniquement en scrollant vers le haut depuis la grille 3x3.
private val hiddenModules = listOf(
    ModuleCard(R.drawable.ic_module_compatibility, R.string.module_compatibility, Screen.Compatibility.route, imageScale = 0.82f),
    ModuleCard(R.drawable.ic_module_biorhythm,     R.string.module_biorhythm,     Screen.Biorhythm.route,     imageScale = 0.82f, introId = ModuleIntroIds.BIORHYTHM),
    ModuleCard(R.drawable.ic_module_mirrorsoul,    R.string.module_mirror_soul,   Screen.MirrorSoul.route,    imageScale = 0.82f, introId = ModuleIntroIds.MIRROR_SOUL)
)

// ─── Écran principal ──────────────────────────────────────────────────────────

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val moduleIntroVm: ModuleIntroViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )
    val seenModuleIntros by moduleIntroVm.seenModuleIntros.collectAsState()

    Scaffold(
        bottomBar = { EmpreinteBottomBar(navController) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Le fond (image + voile) apparaît en fondu, puis le contenu (constellation + grille) en fondu après lui
            var backgroundVisible by remember { mutableStateOf(false) }
            var contentVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                backgroundVisible = true
                kotlinx.coroutines.delay(900)
                contentVisible = true
            }

            AnimatedVisibility(
                visible = backgroundVisible,
                enter = fadeIn(animationSpec = tween(1200))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(R.drawable.bg_galaxy),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.22f))
                    )
                }
            }

            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(animationSpec = tween(1400))
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    // Hauteur de la "page" visible au chargement, identique à l'écran d'avant —
                    // le module caché est placé juste après, dans le même Column scrollable.
                    val pageHeight = maxHeight
                    val density = LocalDensity.current
                    val initialScrollPx = with(density) { pageHeight.toPx().toInt() }
                    val scrollState = rememberScrollState(initial = initialScrollPx)

                    // Au démarrage, l'écran est déjà positionné sur le module caché (aperçu de
                    // Compatibilité), puis redescend vers le menu d'accueil classique.
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(500)
                        scrollState.animateScrollTo(0, tween(1700, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(pageHeight)
                                .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())
                        ) {
                            // Header constellation + titre : zone réduite pour remonter constellation + grille
                            ConstellationTitle(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.8f)
                            )
                            // Grille 3x3 fixe : 2/3 de la hauteur disponible
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(2f)
                                    .padding(horizontal = 16.dp)
                            ) {
                                // Espace au-dessus de la 1ère rangée réduit (le reste va sous la dernière rangée)
                                Spacer(Modifier.weight(0.1f))
                                modules.chunked(3).forEachIndexed { index, rowModules ->
                                    if (index > 0) Spacer(Modifier.weight(1f))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        rowModules.forEach { module ->
                                            Box(modifier = Modifier.weight(1f)) {
                                                CircularModuleItem(module) {
                                                    if (module.introId != null && module.introId !in seenModuleIntros) {
                                                        navController.navigate(Screen.ModuleIntro.route(module.introId, "first"))
                                                    } else {
                                                        navController.navigate(module.route)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.weight(1.75f))
                            }
                        }

                        // Rangée cachée : uniquement visible en scrollant vers le haut.
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top    = 8.dp,
                                    bottom = padding.calculateBottomPadding() + 24.dp
                                ),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            hiddenModules.forEach { module ->
                                Box(modifier = Modifier.weight(1f)) {
                                    CircularModuleItem(module) {
                                        if (module.introId != null && module.introId !in seenModuleIntros) {
                                            navController.navigate(Screen.ModuleIntro.route(module.introId, "first"))
                                        } else {
                                            navController.navigate(module.route)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Header : constellation + titre ──────────────────────────────────────────

@Composable
private fun ConstellationTitle(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        // Constellation d'Orion — remplit tout le header, cy à 60% pour descendre vers les badges
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx        = size.width / 2f
            val scale     = 3.0f
            val cy        = size.height * 0.60f
            val starColor = Or.copy(alpha = 0.88f)
            val lineColor = Or.copy(alpha = 0.38f)
            val stroke    = 1.2.dp.toPx()

            val meissa     = Offset(cx + 2.dp.toPx()  * scale, cy - 33.dp.toPx() * scale)
            val betelgeuse = Offset(cx - 43.dp.toPx() * scale, cy - 25.dp.toPx() * scale)
            val bellatrix  = Offset(cx + 25.dp.toPx() * scale, cy - 22.dp.toPx() * scale)
            val alnitak    = Offset(cx - 10.dp.toPx() * scale, cy + 2.dp.toPx()  * scale)
            val alnilam    = Offset(cx,                         cy)
            val mintaka    = Offset(cx + 10.dp.toPx() * scale, cy - 5.dp.toPx()  * scale)
            val sword1     = Offset(cx + 2.dp.toPx()  * scale, cy + 11.dp.toPx() * scale)
            val sword2     = Offset(cx + 2.dp.toPx()  * scale, cy + 17.dp.toPx() * scale)
            val sword3     = Offset(cx + 2.dp.toPx()  * scale, cy + 22.dp.toPx() * scale)
            val saiph      = Offset(cx - 26.dp.toPx() * scale, cy + 26.dp.toPx() * scale)
            val rigel      = Offset(cx + 49.dp.toPx() * scale, cy + 21.dp.toPx() * scale)

            drawLine(lineColor, meissa,     betelgeuse, stroke)
            drawLine(lineColor, meissa,     bellatrix,  stroke)
            drawLine(lineColor, betelgeuse, bellatrix,  stroke)
            drawLine(lineColor, betelgeuse, alnitak,    stroke)
            drawLine(lineColor, bellatrix,  mintaka,    stroke)
            drawLine(lineColor, alnitak,    alnilam,    stroke)
            drawLine(lineColor, alnilam,    mintaka,    stroke)
            drawLine(lineColor, alnilam,    sword1,     stroke)
            drawLine(lineColor, sword1,     sword2,     stroke)
            drawLine(lineColor, sword2,     sword3,     stroke)
            drawLine(lineColor, alnitak,    saiph,      stroke)
            drawLine(lineColor, mintaka,    rigel,      stroke)

            listOf(meissa, bellatrix, alnitak, alnilam, mintaka,
                   sword1, sword2, sword3, saiph).forEach { p ->
                drawCircle(starColor, 2.5.dp.toPx(), p)
            }
            drawCircle(starColor, 3.5.dp.toPx(), rigel)

            // Betelgeuse — étoile à 8 branches
            val s = 13.dp.toPx()
            drawLine(starColor, Offset(betelgeuse.x - s, betelgeuse.y), Offset(betelgeuse.x + s, betelgeuse.y), stroke * 2f)
            drawLine(starColor, Offset(betelgeuse.x, betelgeuse.y - s), Offset(betelgeuse.x, betelgeuse.y + s), stroke * 2f)
            val d = s * 0.55f
            drawLine(starColor, Offset(betelgeuse.x - d, betelgeuse.y - d), Offset(betelgeuse.x + d, betelgeuse.y + d), stroke)
            drawLine(starColor, Offset(betelgeuse.x + d, betelgeuse.y - d), Offset(betelgeuse.x - d, betelgeuse.y + d), stroke)
            drawCircle(starColor, 3.dp.toPx(), betelgeuse)
        }

        // Texte descendu plus bas que la constellation
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 92.dp)
        ) {
            Text(
                text      = stringResource(R.string.app_name),
                style     = MaterialTheme.typography.displaySmall,
                color     = Or,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text          = stringResource(R.string.home_subtitle),
                style         = MaterialTheme.typography.labelMedium,
                color         = BlancNacre.copy(alpha = 0.50f),
                letterSpacing = 4.sp,
                textAlign     = TextAlign.Center
            )
        }
    }
}

// ─── Bouton circulaire ────────────────────────────────────────────────────────

@Composable
private fun CircularModuleItem(module: ModuleCard, onClick: () -> Unit) {
    val circleSize = 88.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (module.overflowImage) {
            // Archange — même taille que les autres badges (alignement préservé)
            // Image décalée vers le haut : tête dans le cercle, ailes qui dépassent au-dessus
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(circleSize + 18.dp)
                    .clickable { onClick() }
            ) {
                // 1. Bordure dorée EN DESSOUS de l'image (dessinée en premier)
                Box(
                    modifier = Modifier
                        .size(circleSize)
                        .border(1.5.dp, Or.copy(alpha = 0.70f), CircleShape)
                )
                // 2. Image au milieu — ailes passent PAR-DESSUS la bordure
                Image(
                    painter = painterResource(module.iconRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(circleSize * 1.15f)
                        .offset(y = (-18).dp)
                )
            }
        } else {
            // Badges standards : pas de fond, transparents
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(circleSize + 18.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(circleSize)
                        .clip(CircleShape)
                        .border(1.5.dp, Or.copy(alpha = 0.70f), CircleShape)
                        .clickable { onClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(module.iconRes),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(circleSize * module.imageScale)
                    )
                }
            }
        }

        Spacer(Modifier.height(2.dp))

        Text(
            text       = stringResource(module.labelRes),
            style      = MaterialTheme.typography.labelSmall,
            color      = Or.copy(alpha = 0.92f),
            textAlign  = TextAlign.Center,
            lineHeight = 15.sp
        )
    }
}

// ─── Barre de navigation ──────────────────────────────────────────────────────

private data class NavItem(
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    val route: String,
    val navigateRoute: String = route,
    val iconSize: Int = 60,
    val iconOffsetY: Int = 0,
    val labelOffsetY: Int = 0
)

private val navItems = listOf(
    NavItem(R.drawable.ic_module_yiking, R.string.module_yi_king, Screen.YiKing.route,   iconSize = 60, iconOffsetY = 7),
    NavItem(R.drawable.ic_nav_pratique, R.string.nav_practices, Screen.Practices.route),
    NavItem(R.drawable.ic_nav_moi,      R.string.nav_me,        Screen.Me.route,        navigateRoute = Screen.Me.route(1)),
    NavItem(R.drawable.ic_nav_plus,     R.string.nav_more,      Screen.More.route,      iconSize = 76, iconOffsetY = 10, labelOffsetY = -9)
)

@Composable
private fun EmpreinteBottomBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        containerColor = BleuNuitSombre.copy(alpha = 0.25f),
        tonalElevation = 0.dp,
        modifier = Modifier.drawBehind {
            drawLine(
                color       = VioletMoyen.copy(alpha = 0.35f),
                start       = Offset(0f, 0f),
                end         = Offset(size.width, 0f),
                strokeWidth = 0.8.dp.toPx()
            )
        }
    ) {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick  = {
                    navController.navigate(item.navigateRoute) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Image(
                        painter = painterResource(item.iconRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(item.iconSize.dp)
                            .offset(y = item.iconOffsetY.dp)
                    )
                },
                label = {
                    Text(
                        text  = stringResource(item.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(y = item.labelOffsetY.dp)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Or,
                    selectedTextColor   = Or,
                    indicatorColor      = Color.Transparent,
                    unselectedIconColor = BlancNacre.copy(alpha = 0.42f),
                    unselectedTextColor = BlancNacre.copy(alpha = 0.42f)
                )
            )
        }
    }
}
