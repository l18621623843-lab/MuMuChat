package com.kk.mumuchat.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kk.mumuchat.call.CallStatus
import com.kk.mumuchat.call.CallType
import com.kk.mumuchat.call.CallViewModel
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer

@Composable
fun CallScreen(
    callViewModel: CallViewModel,
    onBack: () -> Unit
) {
    val state by callViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Auto-navigate back when call ends
    LaunchedEffect(state.status) {
        if (state.status == CallStatus.ENDED) {
            kotlinx.coroutines.delay(1500)
            onBack()
        }
    }

    val bgBrush = if (state.callType == CallType.VIDEO) {
        Brush.verticalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF2C3E50), Color(0xFF3498DB)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
            .statusBarsPadding()
    ) {
        if (state.callType == CallType.VIDEO) {
            VideoCallContent(callViewModel, state, onBack)
        } else {
            VoiceCallContent(state, callViewModel, onBack)
        }
    }
}

@Composable
private fun VoiceCallContent(
    state: com.kk.mumuchat.call.CallState,
    callViewModel: CallViewModel,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(80.dp))

        // Avatar
        Box(contentAlignment = Alignment.Center) {
            if (state.status == CallStatus.CONNECTING || state.status == CallStatus.RINGING) {
                PulseAnimation()
            }
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, "头像", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(56.dp))
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(state.targetName.ifEmpty { state.targetPhone }, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        // Status text
        val statusText = when (state.status) {
            CallStatus.CONNECTING -> "正在连接..."
            CallStatus.RINGING -> "正在呼叫..."
            CallStatus.CONNECTED -> formatDuration(state.durationSeconds)
            CallStatus.ENDED -> state.errorMessage ?: "通话已结束"
            CallStatus.IDLE -> ""
        }
        Text(statusText, color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)

        Spacer(Modifier.weight(1f))

        // Bottom buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 60.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CallActionButton(
                icon = if (state.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                label = if (state.isMuted) "已静音" else "静音",
                bgColor = if (state.isMuted) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.15f),
                onClick = { callViewModel.toggleMute() }
            )
            CallActionButton(
                icon = Icons.Default.CallEnd,
                label = "挂断",
                bgColor = Color(0xFFE53935),
                iconColor = Color.White,
                size = 68.dp,
                onClick = {
                    callViewModel.hangUp()
                    onBack()
                }
            )
            CallActionButton(
                icon = if (state.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                label = if (state.isSpeakerOn) "已开启" else "免提",
                bgColor = if (state.isSpeakerOn) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.15f),
                onClick = { callViewModel.toggleSpeaker() }
            )
        }
    }
}

@Composable
private fun VideoCallContent(
    callViewModel: CallViewModel,
    state: com.kk.mumuchat.call.CallState,
    onBack: () -> Unit
) {
    val manager = callViewModel.getWebRTCManager()

    Box(modifier = Modifier.fillMaxSize()) {
        // Remote video (full screen)
        if (manager != null) {
            AndroidView(
                factory = { ctx ->
                    SurfaceViewRenderer(ctx).apply {
                        init(manager.getEglBase().eglBaseContext, null)
                        setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                        setEnableHardwareScaler(true)
                        manager.onRemoteVideoTrack = { track ->
                            track.addSink(this)
                        }
                        manager.remoteVideoTrack?.addSink(this)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Local video (small window, top-right)
        if (manager != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
                    .size(width = 120.dp, height = 160.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        SurfaceViewRenderer(ctx).apply {
                            init(manager.getEglBase().eglBaseContext, null)
                            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                            setMirror(true)
                            setEnableHardwareScaler(true)
                            manager.getLocalVideoTrack()?.addSink(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Status overlay
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                state.targetName.ifEmpty { state.targetPhone },
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            val statusText = when (state.status) {
                CallStatus.CONNECTING -> "正在连接..."
                CallStatus.RINGING -> "正在呼叫..."
                CallStatus.CONNECTED -> formatDuration(state.durationSeconds)
                CallStatus.ENDED -> state.errorMessage ?: "通话已结束"
                CallStatus.IDLE -> ""
            }
            Text(statusText, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        }

        // Bottom buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 60.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CallActionButton(
                icon = if (state.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                label = if (state.isMuted) "已静音" else "静音",
                bgColor = if (state.isMuted) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.15f),
                onClick = { callViewModel.toggleMute() }
            )
            CallActionButton(
                icon = Icons.Default.CallEnd,
                label = "挂断",
                bgColor = Color(0xFFE53935),
                iconColor = Color.White,
                size = 68.dp,
                onClick = {
                    callViewModel.hangUp()
                    onBack()
                }
            )
            CallActionButton(
                icon = Icons.Default.Cameraswitch,
                label = "翻转",
                bgColor = Color.White.copy(alpha = 0.15f),
                onClick = { callViewModel.switchCamera() }
            )
        }
    }
}

@Composable
private fun CallActionButton(
    icon: ImageVector,
    label: String,
    bgColor: Color,
    iconColor: Color = Color.White,
    size: androidx.compose.ui.unit.Dp = 56.dp,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(bgColor)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, label, tint = iconColor, modifier = Modifier.size(size * 0.45f))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
    }
}

@Composable
private fun PulseAnimation() {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "scale"
    )
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "alpha"
    )
    Box(
        modifier = Modifier
            .size((100 * scale).dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = alpha))
    )
}

private fun formatDuration(seconds: Int): String {
    val min = seconds / 60
    val sec = seconds % 60
    return "${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}"
}
