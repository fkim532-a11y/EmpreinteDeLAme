package com.francekim.empreintedelame.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.ui.theme.Or

/** Petit bouton rond "i" pour revoir la page d'explication d'un module. */
@Composable
fun ModuleInfoButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .border(1.dp, Or.copy(alpha = 0.70f), CircleShape)
            .clickable(onClickLabel = stringResource(R.string.module_intro_button_content_description)) { onClick() }
    ) {
        Text(
            text  = "i",
            style = MaterialTheme.typography.labelMedium,
            color = Or.copy(alpha = 0.85f)
        )
    }
}
