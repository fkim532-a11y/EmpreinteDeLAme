package com.francekim.empreintedelame.ui.archangelcard

import android.app.Application
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.ui.common.NoteField
import com.francekim.empreintedelame.ui.common.shareImage
import com.francekim.empreintedelame.ui.theme.BlancNacre
import com.francekim.empreintedelame.ui.theme.BleuNuitClair
import com.francekim.empreintedelame.ui.theme.Or
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ArchangelCardHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val vm: ArchangelCardHistoryViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )
    val history by vm.history.collectAsState()
    // On retient l'identifiant, pas l'entrée elle-même : une entrée capturée resterait
    // figée sur son ancienne note après un enregistrement, et le partage l'emporterait.
    var selectedId by remember { mutableStateOf<Long?>(null) }
    val selected = history.find { it.entity.id == selectedId }

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
                    text      = stringResource(R.string.archangel_card_history_title),
                    style     = MaterialTheme.typography.titleLarge,
                    color     = Or,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.weight(1f))
                // Partage de l'entrée ouverte (rien à partager depuis la liste) ;
                // sinon un vide de même largeur, pour garder le titre centré.
                val openCard = selected?.card
                if (openCard != null) {
                    TextButton(onClick = {
                        val caption = buildArchangelCardShareText(
                            context = context,
                            card    = openCard,
                            theme   = selected.entity.theme,
                            note    = selected.entity.note ?: ""
                        )
                        val imageResId = context.resources.getIdentifier(openCard.image, "drawable", context.packageName)
                        val bitmap = BitmapFactory.decodeResource(context.resources, imageResId)
                        shareImage(context, bitmap, caption)
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
                item?.card != null -> {
                    ArchangelCardHistoryDetail(
                        item     = item,
                        onNoteChange = { text -> vm.updateNote(item.entity.id, text) },
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                    )
                }
                history.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text      = stringResource(R.string.archangel_card_history_empty),
                            style     = MaterialTheme.typography.bodyLarge,
                            color     = BlancNacre.copy(alpha = 0.60f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        items(history) { historyItem ->
                            ArchangelCardHistoryRow(item = historyItem, onClick = { selectedId = historyItem.entity.id })
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
private fun ArchangelCardHistoryRow(item: ArchangelCardHistoryItem, onClick: () -> Unit) {
    val card = item.card
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
        if (card != null) {
            Box(modifier = Modifier.width(40.dp).aspectRatio(2f / 3f)) {
                ArchangelCardFront(card)
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = card?.name ?: "",
                style = MaterialTheme.typography.titleSmall,
                color = Or
            )
            Text(
                text  = dateFormat.format(java.util.Date(item.entity.timestampMillis)),
                style = MaterialTheme.typography.labelSmall,
                color = BlancNacre.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun ArchangelCardHistoryDetail(
    item: ArchangelCardHistoryItem,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val card = item.card ?: return
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(modifier = Modifier.width(200.dp).aspectRatio(2f / 3f)) {
            ArchangelCardFront(card)
        }
        Spacer(Modifier.height(20.dp))
        ArchangelCardMessage(card = card, theme = item.entity.theme)
        Spacer(Modifier.height(20.dp))
        NoteField(initialNote = item.entity.note ?: "", onSave = onNoteChange)
        Spacer(Modifier.height(32.dp))
    }
}
