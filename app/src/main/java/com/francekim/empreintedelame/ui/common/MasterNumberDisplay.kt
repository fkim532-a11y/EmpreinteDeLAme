package com.francekim.empreintedelame.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.francekim.empreintedelame.ui.theme.BlancNacre
import com.francekim.empreintedelame.ui.theme.Or
import com.francekim.empreintedelame.ui.theme.VioletMoyen

// Retourne la réduction d'un nombre maître (11→2, 22→4, 33→6), null sinon.
fun masterReduction(n: Int): Int? = when (n) {
    11 -> 2
    22 -> 4
    33 -> 6
    else -> null
}

// Affiche un nombre : violet + "/X" si nombre maître, defaultColor sinon.
@Composable
fun MasterNumberDisplay(
    number       : Int,
    fontSize     : TextUnit,
    defaultColor : Color = Or
) {
    val reduction = masterReduction(number)
    val color     = if (reduction != null) VioletMoyen else defaultColor

    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text     = "$number",
            fontSize = fontSize,
            color    = color
        )
        if (reduction != null) {
            Text(
                text     = " /$reduction",
                fontSize = (fontSize.value * 0.42f).sp,
                color    = BlancNacre.copy(alpha = 0.55f),
                modifier = Modifier.padding(bottom = (fontSize.value * 0.08f).dp)
            )
        }
    }
}
