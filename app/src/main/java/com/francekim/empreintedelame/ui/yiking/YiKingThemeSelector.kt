package com.francekim.empreintedelame.ui.yiking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.ui.theme.BlancNacre
import com.francekim.empreintedelame.ui.theme.BleuNuitClair
import com.francekim.empreintedelame.ui.theme.Or

data class YiKingThemeOption(val key: String, val iconRes: Int, val labelRes: Int)

val yiKingThemeOptions = listOf(
    YiKingThemeOption("spirituel", R.drawable.theme_spirituel, R.string.theme_spirituel),
    YiKingThemeOption("amour",     R.drawable.theme_amour,     R.string.theme_amour),
    YiKingThemeOption("finance",   R.drawable.theme_travail,   R.string.theme_finance),
    YiKingThemeOption("sante",     R.drawable.theme_sante,     R.string.theme_sante),
    YiKingThemeOption("famille",   R.drawable.theme_famille,   R.string.theme_famille)
)

@Composable
fun YiKingThemeSelector(onThemeSelected: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(R.drawable.yiking_theme_header),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(R.string.archangel_card_choose_theme),
            style = MaterialTheme.typography.titleMedium,
            color = BlancNacre,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            YiKingThemeButton(modifier = Modifier.weight(1f), theme = yiKingThemeOptions[0], onClick = { onThemeSelected(yiKingThemeOptions[0].key) })
            YiKingThemeButton(modifier = Modifier.weight(1f), theme = yiKingThemeOptions[1], onClick = { onThemeSelected(yiKingThemeOptions[1].key) })
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            YiKingThemeButton(modifier = Modifier.weight(1f), theme = yiKingThemeOptions[2], onClick = { onThemeSelected(yiKingThemeOptions[2].key) })
            YiKingThemeButton(modifier = Modifier.weight(1f), theme = yiKingThemeOptions[3], onClick = { onThemeSelected(yiKingThemeOptions[3].key) })
        }
        YiKingThemeButton(
            modifier = Modifier.fillMaxWidth(0.5f),
            theme = yiKingThemeOptions[4],
            onClick = { onThemeSelected(yiKingThemeOptions[4].key) }
        )
    }
}

@Composable
private fun YiKingThemeButton(modifier: Modifier = Modifier, theme: YiKingThemeOption, onClick: () -> Unit) {
    val buttonHeight = 42.dp
    val iconSize = 82.dp
    Box(
        modifier = modifier.height(iconSize),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight)
                .clip(RoundedCornerShape(50))
                .background(BleuNuitClair.copy(alpha = 0.35f))
                .border(1.5.dp, Or.copy(alpha = 0.8f), RoundedCornerShape(50))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(theme.labelRes),
                style = MaterialTheme.typography.bodyLarge,
                color = BlancNacre,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 58.dp, end = 8.dp)
            )
        }
        Image(
            painter = painterResource(theme.iconRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(iconSize)
                .align(Alignment.CenterStart)
        )
    }
}
