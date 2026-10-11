package com.francekim.empreintedelame.ui.yiking

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.ui.common.shareImage
import com.francekim.empreintedelame.ui.theme.BlancNacre
import com.francekim.empreintedelame.ui.theme.BleuNuitClair
import com.francekim.empreintedelame.ui.theme.Or
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun YiKingHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val vm: YiKingHistoryViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )
    val history by vm.history.collectAsState()
    val firstName by vm.firstName.collectAsState()
    // On retient l'identifiant, pas l'entrée elle-même : une entrée capturée resterait
    // figée sur son ancienne note après un enregistrement, et le partage l'emporterait.
    var selectedId by remember { mutableStateOf<Long?>(null) }
    val selected = history.find { it.entity.id == selectedId }
    val scope = rememberCoroutineScope()
    val hexagramLayer = rememberGraphicsLayer()

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
                    if (selected != null) {
                        selectedId = null
                    } else if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        navController.popBackStack()
                    }
                }) {
                    Text("←", color = Or, style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text      = stringResource(R.string.yiking_history_title),
                    style     = MaterialTheme.typography.titleLarge,
                    color     = Or,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.weight(1f))
                // Partage de l'entrée ouverte (rien à partager depuis la liste) ;
                // sinon un vide de même largeur, pour garder le titre centré.
                val openItem = selected
                if (openItem != null) {
                    TextButton(onClick = {
                        val caption = buildYiKingShareText(
                            context   = context,
                            firstName = firstName,
                            theme     = openItem.entity.theme,
                            cast      = openItem.cast,
                            primary   = openItem.primary,
                            secondary = openItem.secondary,
                            note      = openItem.entity.note ?: ""
                        )
                        scope.launch {
                            val bitmap = hexagramLayer.toImageBitmap().asAndroidBitmap()
                            shareImage(context, bitmap, caption)
                        }
                    }) {
                        Image(
                            painter = painterResource(R.drawable.ic_share),
                            contentDescription = null,
                            modifier = Modifier.height(40.dp)
                        )
                    }
                } else {
                    Spacer(Modifier.width(64.dp))
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color    = Or.copy(alpha = 0.25f)
            )

            val item = selected
            when {
                item != null -> {
                    YiKingResultContent(
                        theme         = item.entity.theme,
                        cast          = item.cast,
                        primary       = item.primary,
                        secondary     = item.secondary,
                        hexagramLayer = hexagramLayer,
                        note          = item.entity.note ?: "",
                        onNoteChange  = { text -> vm.updateNote(item.entity.id, text) },
                        modifier      = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                    )
                }
                history.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text      = stringResource(R.string.yiking_history_empty),
                            style     = MaterialTheme.typography.bodyLarge,
                            color     = BlancNacre.copy(alpha = 0.60f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        items(history) { historyItem ->
                            YiKingHistoryRow(item = historyItem, onClick = { selectedId = historyItem.entity.id })
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

@Composable
private fun YiKingHistoryRow(item: YiKingHistoryItem, onClick: () -> Unit) {
    val hasMutant = item.cast.lines.any { it.isMutant }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BleuNuitClair.copy(alpha = 0.35f))
            .border(1.dp, Or.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        YiKingHexagramCanvas(lines = item.cast.lines, size = 46.dp)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = item.primary?.nom ?: "",
                style = MaterialTheme.typography.titleSmall,
                color = Or
            )
            Text(
                text  = dateFormat.format(java.util.Date(item.entity.timestampMillis)),
                style = MaterialTheme.typography.labelSmall,
                color = BlancNacre.copy(alpha = 0.55f)
            )
        }
        if (hasMutant) {
            Text("✨", fontSize = 16.sp)
        }
    }
}
