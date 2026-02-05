package com.github.damontecres.wholphin.screensaver

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.damontecres.wholphin.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class InteractionTrackerViewModel
@Inject
constructor(
    private val appPreferences: DataStore<AppPreferences>,
) : ViewModel() {
    private val _isScreensaverVisible = MutableStateFlow(false)
    val isScreensaverVisible: StateFlow<Boolean> = _isScreensaverVisible

    private var inactivityJob: Job? = null

    init {
        viewModelScope.launch {
            appPreferences.data.collect {
                notifyInteraction(it.interfacePreferences.screensaverDelayMs)
            }
        }
    }

    fun notifyInteraction() {
        viewModelScope.launch {
            notifyInteraction(appPreferences.data.first().interfacePreferences.screensaverDelayMs)
        }
    }

    private fun notifyInteraction(screensaverDelayMs: Long) {
        _isScreensaverVisible.value = false
        inactivityJob?.cancel()
        if (screensaverDelayMs > 0) {
            inactivityJob =
                viewModelScope.launch {
                    delay(screensaverDelayMs)
                    _isScreensaverVisible.value = true
                }
        }
    }
}
