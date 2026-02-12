package com.kk.mumuchat.call

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kk.mumuchat.webrtc.WebRTCManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "CallViewModel"

class CallViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(CallState())
    val state: StateFlow<CallState> = _state.asStateFlow()

    private var webRTCManager: WebRTCManager? = null
    private var timerJob: Job? = null
    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun getWebRTCManager(): WebRTCManager? = webRTCManager

    fun startCall(callType: CallType, myPhone: String, targetPhone: String, targetName: String) {
        _state.update {
            it.copy(
                status = CallStatus.CONNECTING,
                callType = callType,
                myPhone = myPhone,
                targetPhone = targetPhone,
                targetName = targetName,
                durationSeconds = 0,
                isMuted = false,
                isSpeakerOn = callType == CallType.VIDEO,
                isCameraFront = true,
                errorMessage = null
            )
        }

        setupAudioMode(callType)

        val manager = WebRTCManager(getApplication())
        webRTCManager = manager
        manager.onConnected = {
            _state.update { it.copy(status = CallStatus.CONNECTED) }
            startTimer()
        }
        manager.onError = { error ->
            _state.update { it.copy(status = CallStatus.ENDED, errorMessage = error) }
        }

        manager.init(enableVideo = callType == CallType.VIDEO)

        // Publish local stream
        manager.publish(myPhone, targetPhone) { success, error ->
            if (success) {
                _state.update { it.copy(status = CallStatus.RINGING) }
                // Start playing remote stream after a short delay
                viewModelScope.launch {
                    delay(1000)
                    manager.play(myPhone, targetPhone) { playSuccess, playError ->
                        if (!playSuccess) {
                            Log.w(TAG, "Play failed: $playError (remote may not be streaming yet)")
                        }
                    }
                }
            } else {
                _state.update { it.copy(status = CallStatus.ENDED, errorMessage = error) }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.update { it.copy(durationSeconds = it.durationSeconds + 1) }
            }
        }
    }

    fun toggleMute() {
        val muted = webRTCManager?.toggleMute() ?: return
        _state.update { it.copy(isMuted = muted) }
    }

    fun toggleSpeaker() {
        val newSpeaker = !_state.value.isSpeakerOn
        _state.update { it.copy(isSpeakerOn = newSpeaker) }
        audioManager.isSpeakerphoneOn = newSpeaker
    }

    fun switchCamera() {
        webRTCManager?.switchCamera()
        _state.update { it.copy(isCameraFront = !it.isCameraFront) }
    }

    fun hangUp() {
        timerJob?.cancel()
        webRTCManager?.dispose()
        webRTCManager = null
        resetAudioMode()
        _state.update { it.copy(status = CallStatus.ENDED) }
    }

    private fun setupAudioMode(callType: CallType) {
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = callType == CallType.VIDEO
    }

    private fun resetAudioMode() {
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.isSpeakerphoneOn = false
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        webRTCManager?.dispose()
        resetAudioMode()
    }
}
