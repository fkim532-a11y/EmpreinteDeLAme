package com.francekim.empreintedelame.ui.practices

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francekim.empreintedelame.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PracticeMode { CHOICE, TIMER, FINISHED }
enum class Ambiance { EAU, OISEAUX, SILENCE }

data class PracticesUiState(
    val mode            : PracticeMode = PracticeMode.CHOICE,
    val selectedAmbiance: Ambiance = Ambiance.EAU,
    val customMusicUri  : Uri? = null,
    val customMusicName : String? = null,
    val durationMinutes : Int = 10,
    val timerProgress   : Float = 0f,
    val remainingSeconds: Int = 0
)

class PracticesViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PracticesUiState())
    val uiState: StateFlow<PracticesUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var ambiancePlayer: MediaPlayer? = null
    private var gongPlayer: MediaPlayer? = null

    fun setAmbiance(ambiance: Ambiance) {
        _uiState.update { it.copy(selectedAmbiance = ambiance, customMusicUri = null, customMusicName = null) }
    }

    fun setCustomMusic(uri: Uri, name: String) {
        _uiState.update { it.copy(customMusicUri = uri, customMusicName = name) }
    }

    fun clearCustomMusic() {
        _uiState.update { it.copy(customMusicUri = null, customMusicName = null) }
    }

    fun setDuration(minutes: Int) {
        _uiState.update { it.copy(durationMinutes = minutes) }
    }

    fun start() {
        val state = _uiState.value
        val totalSeconds = state.durationMinutes * 60
        _uiState.update {
            it.copy(mode = PracticeMode.TIMER, timerProgress = 0f, remainingSeconds = totalSeconds)
        }
        startAmbiance(state)
        startTimer(totalSeconds)
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
        stopAmbiance()
        _uiState.update { it.copy(mode = PracticeMode.CHOICE, timerProgress = 0f, remainingSeconds = 0) }
    }

    fun backToChoice() {
        _uiState.update { it.copy(mode = PracticeMode.CHOICE) }
    }

    private fun startAmbiance(state: PracticesUiState) {
        val context = getApplication<Application>()
        ambiancePlayer?.release()
        ambiancePlayer = null

        val customUri = state.customMusicUri
        val player = if (customUri != null) {
            try {
                MediaPlayer().apply {
                    setDataSource(context, customUri)
                    isLooping = true
                    prepare()
                }
            } catch (e: Exception) {
                null
            }
        } else {
            val resId = when (state.selectedAmbiance) {
                Ambiance.EAU      -> R.raw.bruit_eau
                Ambiance.OISEAUX  -> R.raw.chant_oiseau
                Ambiance.SILENCE  -> return
            }
            MediaPlayer.create(context, resId)?.apply { isLooping = true }
        }
        player?.start()
        ambiancePlayer = player
    }

    private fun stopAmbiance() {
        ambiancePlayer?.stop()
        ambiancePlayer?.release()
        ambiancePlayer = null
    }

    private fun startTimer(totalSeconds: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var elapsed = 0
            while (elapsed < totalSeconds) {
                delay(1000L)
                elapsed++
                _uiState.update {
                    it.copy(
                        timerProgress    = elapsed.toFloat() / totalSeconds,
                        remainingSeconds = (totalSeconds - elapsed).coerceAtLeast(0)
                    )
                }
            }
            stopAmbiance()
            _uiState.update { it.copy(mode = PracticeMode.FINISHED, timerProgress = 1f) }
            triggerVibration()
            playGong()
        }
    }

    private fun playGong() {
        val context = getApplication<Application>()
        gongPlayer?.release()
        gongPlayer = MediaPlayer.create(context, R.raw.gong)
        gongPlayer?.start()
        gongPlayer?.setOnCompletionListener { it.release(); gongPlayer = null }
    }

    @Suppress("DEPRECATION")
    private fun triggerVibration() {
        val context = getApplication<Application>()
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 150, 400, 150, 800), -1))
        } else {
            vibrator.vibrate(longArrayOf(0, 400, 150, 400, 150, 800), -1)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        stopAmbiance()
        gongPlayer?.release()
        gongPlayer = null
    }
}
