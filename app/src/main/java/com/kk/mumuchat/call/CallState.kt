package com.kk.mumuchat.call

enum class CallType { VOICE, VIDEO }

enum class CallStatus { IDLE, CONNECTING, RINGING, CONNECTED, ENDED }

data class CallState(
    val status: CallStatus = CallStatus.IDLE,
    val callType: CallType = CallType.VOICE,
    val myPhone: String = "",
    val targetPhone: String = "",
    val targetName: String = "",
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isCameraFront: Boolean = true,
    val errorMessage: String? = null
)
