package com.francekim.empreintedelame.ui.mirrorsoul

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.data.model.MirrorSoulAnswerEntity
import com.francekim.empreintedelame.ui.theme.BlancNacre
import com.francekim.empreintedelame.ui.theme.BleuNuitClair
import com.francekim.empreintedelame.ui.theme.Or
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MirrorSoulHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val vm: MirrorSoulViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )
    val history by vm.history.collectAsStateWithLifecycle(initialValue = emptyList())

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_galaxy),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
            ) {
                TextButton(onClick = {
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        navController.popBackStack()
                    }
                }) {
                    Text("←", color = Or, style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text      = stringResource(R.string.mirror_soul_history_title),
                    style     = MaterialTheme.typography.titleLarge,
                    color     = Or,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(64.dp))
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color    = Or.copy(alpha = 0.25f)
            )

            if (history.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text      = stringResource(R.string.mirror_soul_history_empty),
                        style     = MaterialTheme.typography.bodyLarge,
                        color     = BlancNacre.copy(alpha = 0.60f),
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    items(history) { entity ->
                        MirrorSoulHistoryRow(entity = entity)
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

@Composable
private fun MirrorSoulHistoryRow(entity: MirrorSoulAnswerEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BleuNuitClair.copy(alpha = 0.35f))
            .border(1.dp, Or.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(
            text  = dateFormat.format(java.util.Date(entity.timestampMillis)),
            style = MaterialTheme.typography.labelSmall,
            color = BlancNacre.copy(alpha = 0.55f)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text  = entity.texte,
            style = MaterialTheme.typography.titleSmall,
            color = Or
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text  = entity.reponse,
            style = MaterialTheme.typography.bodyMedium,
            color = BlancNacre.copy(alpha = 0.80f)
        )
    }
}
