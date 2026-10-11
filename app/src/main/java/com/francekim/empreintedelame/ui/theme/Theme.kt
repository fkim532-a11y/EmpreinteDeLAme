package com.francekim.empreintedelame.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EmpreinteColorScheme = darkColorScheme(
    primary              = VioletMoyen,
    onPrimary            = BlancNacre,
    primaryContainer     = VioletProfond,
    onPrimaryContainer   = VioletClair,

    secondary            = Or,
    onSecondary          = BleuNuit,
    secondaryContainer   = Color(0xFF3D3000),
    onSecondaryContainer = OrClair,

    tertiary             = VioletClair,
    onTertiary           = VioletProfond,

    background           = BleuNuit,
    onBackground         = BlancNacre,

    surface              = BleuNuitClair,
    onSurface            = BlancNacre,
    surfaceVariant       = VioletNuit,
    onSurfaceVariant     = VioletClair,

    outline              = VioletMoyen,
    outlineVariant       = Color(0xFF4A3F6B),

    error                = Color(0xFFCF6679),
    onError              = BleuNuit,
)

@Composable
fun EmpreinteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EmpreinteColorScheme,
        typography  = Typography,
        content     = content
    )
}
