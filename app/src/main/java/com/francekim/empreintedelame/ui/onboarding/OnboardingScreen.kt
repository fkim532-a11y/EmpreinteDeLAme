package com.francekim.empreintedelame.ui.onboarding

import android.content.Context
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.data.model.UserProfile
import com.francekim.empreintedelame.domain.calculator.FranceUtcOffsetCalculator
import com.francekim.empreintedelame.ui.common.PageIndicator
import com.francekim.empreintedelame.ui.navigation.Screen
import com.francekim.empreintedelame.ui.theme.*
import kotlinx.coroutines.launch

// ─── Onboarding (1ère ouverture) ─────────────────────────────────────────────

@Composable
fun OnboardingScreen(
    navController: NavController,
    vm: OnboardingViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_galaxy),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp)
        ) {
            Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall, color = Or, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(
                text          = stringResource(R.string.home_subtitle),
                style         = MaterialTheme.typography.labelMedium,
                color         = BlancNacre.copy(alpha = 0.50f),
                letterSpacing = 4.sp,
                textAlign     = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            HorizontalDivider(modifier = Modifier.width(48.dp), thickness = 1.dp, color = Or.copy(alpha = 0.35f))
            Spacer(Modifier.height(28.dp))

            ProfileFieldsForm(
                existingProfile = null,
                buttonLabel     = stringResource(R.string.onboarding_start),
                onSave = { profile ->
                    scope.launch {
                        vm.save(
                            firstName             = profile.firstName,
                            lastName               = profile.lastName,
                            birthDay               = profile.birthDay,
                            birthMonth             = profile.birthMonth,
                            birthYear              = profile.birthYear,
                            birthHour              = profile.birthHour,
                            birthMinute            = profile.birthMinute,
                            birthCity              = profile.birthCity,
                            birthUtcOffsetMinutes  = profile.birthUtcOffsetMinutes
                        )
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}

// ─── Page Profils (jusqu'à 3 profils, swipe pour changer, un seul actif) ──────

@Composable
fun MeScreen(navController: NavController, vm: OnboardingViewModel = viewModel(), initialSlot: Int = 1) {
    val profiles by vm.allProfiles.collectAsState()
    val activeSlot by vm.activeSlot.collectAsState()
    val pagerState = rememberPagerState(initialPage = (initialSlot - 1).coerceIn(0, 2), pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_galaxy),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))

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
                    text      = stringResource(R.string.nav_me),
                    style     = MaterialTheme.typography.titleLarge,
                    color     = Or,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(64.dp))
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val slot = page + 1
                val existing = profiles.getOrNull(page)
                var saved by remember(slot) { mutableStateOf(false) }
                var showDeleteConfirm by remember(slot) { mutableStateOf(false) }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 28.dp, vertical = 20.dp)
                ) {
                    Text(
                        text      = existing?.firstName?.takeIf { it.isNotBlank() }
                            ?: when (slot) {
                                2 -> stringResource(R.string.profile_second_slot_label)
                                3 -> stringResource(R.string.profile_third_slot_label)
                                else -> stringResource(R.string.profile_slot_label, slot)
                            },
                        style     = MaterialTheme.typography.displaySmall,
                        color     = Or,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { vm.setActiveSlot(slot) }
                    ) {
                        Checkbox(
                            checked = activeSlot == slot,
                            onCheckedChange = { checked -> if (checked) vm.setActiveSlot(slot) },
                            colors = CheckboxDefaults.colors(checkedColor = Or, checkmarkColor = BleuNuitSombre)
                        )
                        Text(
                            text  = stringResource(R.string.profile_active_label),
                            style = MaterialTheme.typography.bodyMedium,
                            color = BlancNacre.copy(alpha = 0.80f)
                        )
                    }
                    Spacer(Modifier.height(20.dp))

                    ProfileFieldsForm(
                        existingProfile = existing,
                        buttonLabel     = stringResource(R.string.profile_save),
                        successMessage  = if (saved) stringResource(R.string.profile_saved) else "",
                        onFieldChanged  = { if (activeSlot != slot) vm.setActiveSlot(slot) },
                        onSave = { profile ->
                            scope.launch {
                                vm.save(
                                    firstName              = profile.firstName,
                                    lastName                = profile.lastName,
                                    birthDay                = profile.birthDay,
                                    birthMonth              = profile.birthMonth,
                                    birthYear               = profile.birthYear,
                                    birthHour               = profile.birthHour,
                                    birthMinute             = profile.birthMinute,
                                    birthCity               = profile.birthCity,
                                    birthUtcOffsetMinutes   = profile.birthUtcOffsetMinutes,
                                    slot                    = slot
                                )
                                saved = true
                                if (slot < 3) {
                                    pagerState.animateScrollToPage(
                                        page = slot,
                                        animationSpec = tween(durationMillis = 1600)
                                    )
                                }
                            }
                        }
                    )

                    if (existing != null) {
                        Spacer(Modifier.height(16.dp))
                        TextButton(onClick = { showDeleteConfirm = true }) {
                            Text(
                                text  = stringResource(R.string.profile_delete_button),
                                color = Color(0xFFFF6B6B),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text(stringResource(R.string.profile_delete_confirm_title)) },
                            text  = { Text(stringResource(R.string.profile_delete_confirm_message)) },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDeleteConfirm = false
                                    vm.deleteProfile(slot)
                                }) {
                                    Text(stringResource(R.string.profile_delete_confirm_button), color = Color(0xFFFF6B6B))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirm = false }) {
                                    Text(stringResource(R.string.profile_delete_cancel_button), color = BlancNacre)
                                }
                            }
                        )
                    }
                }
            }

            PageIndicator(
                pageCount   = 3,
                currentPage = pagerState.currentPage,
                modifier    = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }
    }
}

// ─── Formulaire partagé (champs + validation + bouton) ────────────────────────

@Composable
private fun ProfileFieldsForm(
    existingProfile: UserProfile?,
    buttonLabel: String,
    onSave: (UserProfile) -> Unit,
    successMessage: String = "",
    onFieldChanged: () -> Unit = {}
) {
    val context = LocalContext.current

    var firstName  by remember(existingProfile) { mutableStateOf(existingProfile?.firstName  ?: "") }
    var lastName   by remember(existingProfile) { mutableStateOf(existingProfile?.lastName   ?: "") }
    var birthDay   by remember(existingProfile) { mutableStateOf(existingProfile?.birthDay?.toString()   ?: "") }
    var birthMonth by remember(existingProfile) { mutableStateOf(existingProfile?.birthMonth?.toString() ?: "") }
    var birthYear  by remember(existingProfile) { mutableStateOf(existingProfile?.birthYear?.toString()  ?: "") }
    var birthHour  by remember(existingProfile) { mutableStateOf(if ((existingProfile?.birthHour ?: 0) > 0) existingProfile!!.birthHour.toString() else "") }
    var birthMin   by remember(existingProfile) { mutableStateOf(if ((existingProfile?.birthMinute ?: 0) > 0) existingProfile!!.birthMinute.toString() else "") }
    var birthCity  by remember(existingProfile) { mutableStateOf(existingProfile?.birthCity  ?: "") }
    var utcOffset  by remember(existingProfile) { mutableStateOf(existingProfile?.let { formatUtcOffset(it.birthUtcOffsetMinutes) } ?: "") }
    var error      by remember { mutableStateOf("") }

    ProfileField(stringResource(R.string.profile_field_firstname), firstName, KeyboardCapitalization.Words) { firstName = it; onFieldChanged() }
    Spacer(Modifier.height(14.dp))
    ProfileField(stringResource(R.string.profile_field_lastname), lastName, KeyboardCapitalization.Words) { lastName = it; onFieldChanged() }
    Spacer(Modifier.height(24.dp))

    SectionLabel(stringResource(R.string.profile_section_birthdate))
    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ProfileFieldSmall(
            stringResource(R.string.profile_field_day),
            birthDay, stringResource(R.string.profile_ph_day), 2
        ) { birthDay = it.filter(Char::isDigit); onFieldChanged() }
        ProfileFieldSmall(
            stringResource(R.string.profile_field_month),
            birthMonth, stringResource(R.string.profile_ph_month), 2
        ) { birthMonth = it.filter(Char::isDigit); onFieldChanged() }
        ProfileFieldSmall(
            stringResource(R.string.profile_field_year),
            birthYear, stringResource(R.string.profile_ph_year), 4,
            Modifier.weight(1.5f)
        ) { birthYear = it.filter(Char::isDigit); onFieldChanged() }
    }
    Spacer(Modifier.height(24.dp))

    SectionLabel(stringResource(R.string.profile_section_birthtime))
    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ProfileFieldSmall(
            stringResource(R.string.profile_field_hour),
            birthHour, stringResource(R.string.profile_ph_hour), 2,
            Modifier.weight(1f)
        ) { birthHour = it.filter(Char::isDigit); onFieldChanged() }
        ProfileFieldSmall(
            stringResource(R.string.profile_field_minutes),
            birthMin, stringResource(R.string.profile_ph_month), 2,
            Modifier.weight(1f)
        ) { birthMin = it.filter(Char::isDigit); onFieldChanged() }
    }
    Spacer(Modifier.height(24.dp))

    ProfileField(stringResource(R.string.profile_field_birthcity), birthCity, KeyboardCapitalization.Words) { birthCity = it; onFieldChanged() }
    Spacer(Modifier.height(14.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = utcOffset,
            onValueChange = { utcOffset = it; onFieldChanged() },
            label = { Text(stringResource(R.string.profile_field_utc_offset), color = BlancNacre.copy(alpha = 0.60f)) },
            placeholder = { Text(stringResource(R.string.profile_ph_utc_offset), color = BlancNacre.copy(alpha = 0.30f)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = profileFieldColors(),
            shape  = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = {
            val d = birthDay.toIntOrNull()
            val m = birthMonth.toIntOrNull()
            val y = birthYear.toIntOrNull()
            if (d == null || m == null || y == null) {
                error = context.getString(R.string.profile_err_utc_offset_auto)
            } else {
                val h  = birthHour.toIntOrNull() ?: 0
                val mn = birthMin.toIntOrNull()  ?: 0
                utcOffset = formatUtcOffset(FranceUtcOffsetCalculator.calculateOffsetMinutes(y, m, d, h, mn))
                error = ""
                onFieldChanged()
            }
        }) {
            Text(stringResource(R.string.profile_utc_offset_auto_button), color = Or, style = MaterialTheme.typography.labelMedium)
        }
    }
    Spacer(Modifier.height(6.dp))
    Text(
        text  = stringResource(R.string.profile_utc_offset_help),
        style = MaterialTheme.typography.bodySmall,
        color = BlancNacre.copy(alpha = 0.60f)
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text  = stringResource(R.string.profile_utc_offset_help_other_countries),
        style = MaterialTheme.typography.bodySmall,
        color = BlancNacre.copy(alpha = 0.60f)
    )
    Spacer(Modifier.height(32.dp))

    if (error.isNotEmpty()) {
        Text(error, color = Color(0xFFFF6B6B), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
    }
    if (successMessage.isNotEmpty()) {
        Text(successMessage, color = Color(0xFF81C784), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
    }

    Button(
        onClick = {
            error = validateProfile(context, firstName, lastName, birthDay, birthMonth, birthYear, birthHour, birthMin, utcOffset)
            if (error.isEmpty()) {
                onSave(
                    UserProfile(
                        firstName             = firstName.trim(),
                        lastName               = lastName.trim(),
                        birthDay               = birthDay.toInt(),
                        birthMonth             = birthMonth.toInt(),
                        birthYear              = birthYear.toInt(),
                        birthHour              = birthHour.toIntOrNull() ?: 0,
                        birthMinute            = birthMin.toIntOrNull()  ?: 0,
                        birthCity              = birthCity.trim(),
                        birthUtcOffsetMinutes  = parseUtcOffsetMinutes(utcOffset)
                    )
                )
            }
        },
        colors   = ButtonDefaults.buttonColors(containerColor = Or),
        shape    = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) {
        Text(buttonLabel, color = BleuNuitSombre, style = MaterialTheme.typography.titleMedium)
    }
}

// ─── Composants internes ──────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.labelLarge,
        color    = Or.copy(alpha = 0.80f),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ProfileField(
    label: String, value: String,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label, color = BlancNacre.copy(alpha = 0.60f)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = capitalization),
        colors = profileFieldColors(),
        shape  = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun RowScope.ProfileFieldSmall(
    label: String, value: String, placeholder: String, maxLength: Int,
    modifier: Modifier = Modifier.weight(1f),
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value, onValueChange = { if (it.length <= maxLength) onChange(it) },
        label       = { Text(label, color = BlancNacre.copy(alpha = 0.60f)) },
        placeholder = { Text(placeholder, color = BlancNacre.copy(alpha = 0.30f)) },
        singleLine  = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors   = profileFieldColors(),
        shape    = RoundedCornerShape(10.dp),
        modifier = modifier
    )
}

@Composable
private fun profileFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor        = BlancNacre,
    unfocusedTextColor      = BlancNacre,
    focusedBorderColor      = Or.copy(alpha = 0.70f),
    unfocusedBorderColor    = BlancNacre.copy(alpha = 0.25f),
    cursorColor             = Or,
    focusedContainerColor   = Color.White.copy(alpha = 0.05f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
)

private fun validateProfile(
    context: Context,
    firstName: String, lastName: String,
    day: String, month: String, year: String,
    hour: String, min: String,
    utcOffset: String
): String {
    if (firstName.isBlank()) return context.getString(R.string.profile_err_firstname)
    if (lastName.isBlank())  return context.getString(R.string.profile_err_lastname)
    val d = day.toIntOrNull()   ?: return context.getString(R.string.profile_err_day)
    val m = month.toIntOrNull() ?: return context.getString(R.string.profile_err_month)
    val y = year.toIntOrNull()  ?: return context.getString(R.string.profile_err_year)
    if (d !in 1..31)      return context.getString(R.string.profile_err_day_range)
    if (m !in 1..12)      return context.getString(R.string.profile_err_month_range)
    if (y !in 1900..2100) return context.getString(R.string.profile_err_year)
    if (hour.isNotEmpty()) {
        val h = hour.toIntOrNull() ?: return context.getString(R.string.profile_err_hour)
        if (h !in 0..23) return context.getString(R.string.profile_err_hour_range)
    }
    if (min.isNotEmpty()) {
        val mn = min.toIntOrNull() ?: return context.getString(R.string.profile_err_minutes)
        if (mn !in 0..59) return context.getString(R.string.profile_err_minutes_range)
    }
    if (utcOffset.isNotEmpty()) {
        val offset = utcOffset.replace(",", ".").toDoubleOrNull()
            ?: return context.getString(R.string.profile_err_utc_offset)
        if (offset !in -12.0..14.0) return context.getString(R.string.profile_err_utc_offset)
    }
    return ""
}

// ─── Décalage UTC de naissance ────────────────────────────────────────────────
// Saisi librement en heures décimales (ex. "+1", "-5", "+5.5") plutôt qu'un
// nom de fuseau IANA, faute de géocodage disponible dans ce projet hors-ligne.

private fun parseUtcOffsetMinutes(text: String): Int {
    val hours = text.replace(",", ".").toDoubleOrNull() ?: return 0
    return Math.round(hours * 60.0).toInt()
}

private fun formatUtcOffset(minutes: Int): String {
    if (minutes == 0) return ""
    val hours = minutes / 60.0
    val rounded = Math.round(hours * 100.0) / 100.0
    return if (rounded == Math.floor(rounded)) {
        "%+d".format(rounded.toInt())
    } else {
        "%+.2f".format(rounded).trimEnd('0').trimEnd('.')
    }
}
