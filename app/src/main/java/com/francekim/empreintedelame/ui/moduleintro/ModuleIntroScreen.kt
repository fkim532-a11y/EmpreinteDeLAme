package com.francekim.empreintedelame.ui.moduleintro

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.ui.theme.BleuNuitSombre
import com.francekim.empreintedelame.ui.theme.BlancNacre
import com.francekim.empreintedelame.ui.theme.Or

@Composable
fun ModuleIntroScreen(
    title: String,
    body: String,
    @DrawableRes iconRes: Int,
    buttonLabel: String,
    onContinue: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        var backgroundVisible by remember { mutableStateOf(false) }
        var contentVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            backgroundVisible = true
            kotlinx.coroutines.delay(500)
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
            enter = fadeIn(animationSpec = tween(1600))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 32.dp)
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(110.dp)
                )
                Spacer(Modifier.height(28.dp))
                Text(
                    text      = title,
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
                    text       = body,
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = BlancNacre.copy(alpha = 0.85f),
                    textAlign  = TextAlign.Center,
                    lineHeight = 26.sp
                )
                Spacer(Modifier.height(48.dp))
                Button(
                    onClick  = onContinue,
                    colors   = ButtonDefaults.buttonColors(containerColor = Or, contentColor = BleuNuitSombre),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text(buttonLabel)
                }
            }
        }
    }
}
