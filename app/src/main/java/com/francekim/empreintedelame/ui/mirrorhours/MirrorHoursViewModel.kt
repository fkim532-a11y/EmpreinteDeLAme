package com.francekim.empreintedelame.ui.mirrorhours

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.francekim.empreintedelame.data.repository.MirrorHourRepository
import com.francekim.empreintedelame.domain.calculator.MirrorHourCalculator
import com.francekim.empreintedelame.domain.model.MirrorHourInfo

class MirrorHoursViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = MirrorHourRepository(getApplication())

    val hours: Map<String, MirrorHourInfo> by lazy { repo.load() }

    val nearestHour: String by lazy { MirrorHourCalculator.nearestHour() }
}
