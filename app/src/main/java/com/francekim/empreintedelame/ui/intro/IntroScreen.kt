package com.francekim.empreintedelame.ui.intro

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.ui.common.PageIndicator
import com.francekim.empreintedelame.ui.theme.*
import kotlinx.coroutines.launch

private data class IntroPage(
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int,
    @DrawableRes val imageRes: Int
)

private val introPages = listOf(
    IntroPage(R.string.intro_page1_title, R.string.intro_page1_body, R.drawable.intro_geometry_moon),
    IntroPage(R.string.intro_page2_title, R.string.intro_page2_body, R.drawable.intro_geometry_lotus),
    IntroPage(R.string.intro_page3_title, R.string.intro_page3_body, R.drawable.intro_geometry_heart)
)

@Composable
fun IntroScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { introPages.size })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
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
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
            }
        }

        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(animationSpec = tween(2200))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    IntroPageContent(introPages[page])
                }

                PageIndicator(
                    pageCount    = introPages.size,
                    currentPage  = pagerState.currentPage,
                    modifier     = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )

                val isLastPage = pagerState.currentPage == introPages.lastIndex
                if (isLastPage) {
                    Button(
                        onClick  = onFinished,
                        colors   = ButtonDefaults.buttonColors(containerColor = Or, contentColor = BleuNuitSombre),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                    ) {
                        Text(stringResource(R.string.onboarding_start))
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                    ) {
                        TextButton(onClick = onFinished) {
                            Text(stringResource(R.string.intro_skip), color = BlancNacre.copy(alpha = 0.60f))
                        }
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Or, contentColor = BleuNuitSombre)
                        ) {
                            Text(stringResource(R.string.intro_next))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IntroPageContent(page: IntroPage) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text      = stringResource(page.titleRes),
            style     = MaterialTheme.typography.headlineSmall,
            color     = Or,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(
            modifier  = Modifier.width(48.dp),
            thickness = 1.dp,
            color     = Or.copy(alpha = 0.35f)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text       = stringResource(page.bodyRes),
            style      = MaterialTheme.typography.bodyLarge,
            color      = BlancNacre.copy(alpha = 0.80f),
            textAlign  = TextAlign.Center,
            lineHeight = 26.sp
        )
        Spacer(Modifier.height(48.dp))
        Image(
            painter = painterResource(page.imageRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(150.dp)
        )
    }
}
