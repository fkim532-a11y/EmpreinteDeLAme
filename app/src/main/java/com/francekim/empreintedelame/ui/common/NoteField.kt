package com.francekim.empreintedelame.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.ui.theme.BlancNacre
import com.francekim.empreintedelame.ui.theme.Or
import kotlinx.coroutines.delay

/**
 * Zone de prise de note personnelle. Sauvegarde automatique quelques secondes après la frappe,
 * et bouton "Enregistrer" pour une confirmation immédiate et explicite.
 * `initialNote` change quand on regarde un tirage différent (nouveau tirage ou historique) :
 * le remember(initialNote) réinitialise alors le buffer local sur la bonne valeur.
 */
@Composable
fun NoteField(
    initialNote: String,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.note_field_label),
    placeholder: String = stringResource(R.string.note_field_placeholder)
) {
    var text by remember(initialNote) { mutableStateOf(initialNote) }
    var justSaved by remember(initialNote) { mutableStateOf(false) }

    LaunchedEffect(text) {
        if (text == initialNote) return@LaunchedEffect
        delay(600)
        onSave(text)
    }
    DisposableEffect(initialNote) {
        onDispose { if (text != initialNote) onSave(text) }
    }
    LaunchedEffect(justSaved) {
        if (!justSaved) return@LaunchedEffect
        delay(1500)
        justSaved = false
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text          = label,
            style         = MaterialTheme.typography.labelMedium,
            color         = BlancNacre.copy(alpha = 0.50f),
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = {
                Text(
                    text  = placeholder,
                    color = BlancNacre.copy(alpha = 0.30f)
                )
            },
            minLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor        = BlancNacre,
                unfocusedTextColor      = BlancNacre,
                focusedBorderColor      = Or.copy(alpha = 0.70f),
                unfocusedBorderColor    = BlancNacre.copy(alpha = 0.25f),
                cursorColor             = Or,
                focusedContainerColor   = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
            ),
            shape    = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            AnimatedVisibility(visible = justSaved, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    text     = stringResource(R.string.note_field_saved),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = Or.copy(alpha = 0.80f),
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
            OutlinedButton(
                onClick = {
                    onSave(text)
                    justSaved = true
                },
                colors  = ButtonDefaults.outlinedButtonColors(contentColor = Or),
                border  = androidx.compose.foundation.BorderStroke(1.dp, Or.copy(alpha = 0.60f))
            ) {
                Text(stringResource(R.string.note_field_save_button))
            }
        }
    }
}
