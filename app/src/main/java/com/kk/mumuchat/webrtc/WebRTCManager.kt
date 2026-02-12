package com.kk.mumuchat.webrtc

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.webrtc.*
import java.util.concurrent.TimeUnit

private const val TAG = "WebRTCManager"
private const val SRS_HOST = "192.168.1.11"
private const val SRS_API = "http://$SRS_HOST:1985"

data class SrsRequest(
    val sdp: String,
    val streamurl: String
)

data class SrsResponse(
    val code: Int,
    val sdp: String?,
    val sessionid: String?
)

class WebRTCManager(private val context: Context) {

    companion object {
        private var initialized = false

        fun initializeWebRTC(context: Context) {
            if (initialized) return
            val options = PeerConnectionFactory.InitializationOptions.builder(context.applicationContext)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(options)
            initialized = true
            Log.d(TAG, "WebRTC initialized")
        }
    }

    private val gson = Gson()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val eglBase = EglBase.create()
    private var factory: PeerConnectionFactory? = null
    private var publishPc: PeerConnection? = null
    private var playPc: PeerConnection? = null

    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var videoSource: VideoSource? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    var remoteVideoTrack: VideoTrack? = null
        private set

    var onRemoteVideoTrack: ((VideoTrack) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onConnected: (() -> Unit)? = null

    private var isMuted = false
    private var isFrontCamera = true

    fun getEglBase(): EglBase = eglBase

    fun init(enableVideo: Boolean) {
        val encoderFactory = DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

        factory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()

        // Audio track
        val audioConstraints = MediaConstraints()
        val audioSource = factory!!.createAudioSource(audioConstraints)
        localAudioTrack = factory!!.createAudioTrack("audio0", audioSource)

        // Video track (if needed)
        if (enableVideo) {
            videoCapturer = createCameraCapturer()
            videoSource = factory!!.createVideoSource(videoCapturer!!.isScreencast)
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
            videoCapturer!!.initialize(surfaceTextureHelper, context, videoSource!!.capturerObserver)
            videoCapturer!!.startCapture(640, 480, 30)
            localVideoTrack = factory!!.createVideoTrack("video0", videoSource)
        }
    }

    fun getLocalVideoTrack(): VideoTrack? = localVideoTrack

    fun publish(myPhone: String, targetPhone: String, callback: (Boolean, String?) -> Unit) {
        val streamUrl = "webrtc://$SRS_HOST/live/call_${myPhone}_${targetPhone}"
        publishPc = createPeerConnection(object : PeerConnectionObserverAdapter() {
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                Log.d(TAG, "Publish ICE state: $state")
                if (state == PeerConnection.IceConnectionState.CONNECTED) {
                    onConnected?.invoke()
                }
            }
        })

        localAudioTrack?.let { publishPc?.addTrack(it, listOf("stream0")) }
        localVideoTrack?.let { publishPc?.addTrack(it, listOf("stream0")) }

        val offerConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        }

        publishPc?.createOffer(object : SdpObserverAdapter() {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp ?: return
                publishPc?.setLocalDescription(SdpObserverAdapter(), sdp)
                postSdpToSrs("$SRS_API/rtc/v1/publish/", sdp.description, streamUrl) { success, answerSdp, error ->
                    if (success && answerSdp != null) {
                        val answer = SessionDescription(SessionDescription.Type.ANSWER, answerSdp)
                        publishPc?.setRemoteDescription(SdpObserverAdapter(), answer)
                        callback(true, null)
                    } else {
                        callback(false, error)
                    }
                }
            }
            override fun onCreateFailure(error: String?) {
                callback(false, error)
            }
        }, offerConstraints)
    }

    fun play(myPhone: String, targetPhone: String, callback: (Boolean, String?) -> Unit) {
        val streamUrl = "webrtc://$SRS_HOST/live/call_${targetPhone}_${myPhone}"

        // 清理上一次的 playPc
        playPc?.close()
        playPc?.dispose()
        playPc = null

        playPc = createPeerConnection(object : PeerConnectionObserverAdapter() {
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                Log.d(TAG, "Play ICE state: $state")
            }
            override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {
                val track = receiver?.track()
                if (track is VideoTrack) {
                    remoteVideoTrack = track
                    onRemoteVideoTrack?.invoke(track)
                }
            }
        })

        val offerConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }

        playPc?.createOffer(object : SdpObserverAdapter() {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp ?: return
                playPc?.setLocalDescription(SdpObserverAdapter(), sdp)
                postSdpToSrs("$SRS_API/rtc/v1/play/", sdp.description, streamUrl) { success, answerSdp, error ->
                    if (success && answerSdp != null) {
                        val answer = SessionDescription(SessionDescription.Type.ANSWER, answerSdp)
                        playPc?.setRemoteDescription(SdpObserverAdapter(), answer)
                        callback(true, null)
                    } else {
                        callback(false, error)
                    }
                }
            }
            override fun onCreateFailure(error: String?) {
                callback(false, error)
            }
        }, offerConstraints)
    }

    fun toggleMute(): Boolean {
        isMuted = !isMuted
        localAudioTrack?.setEnabled(!isMuted)
        return isMuted
    }

    fun switchCamera() {
        videoCapturer?.switchCamera(null)
        isFrontCamera = !isFrontCamera
    }

    fun isFrontCamera(): Boolean = isFrontCamera

    fun dispose() {
        try {
            videoCapturer?.stopCapture()
        } catch (_: Exception) {}
        videoCapturer?.dispose()
        surfaceTextureHelper?.dispose()
        localAudioTrack?.dispose()
        localVideoTrack?.dispose()
        videoSource?.dispose()
        publishPc?.close()
        playPc?.close()
        publishPc?.dispose()
        playPc?.dispose()
        factory?.dispose()
        eglBase.release()
        publishPc = null
        playPc = null
        factory = null
    }

    private fun createPeerConnection(observer: PeerConnection.Observer): PeerConnection? {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
        val config = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }
        return factory?.createPeerConnection(config, observer)
    }

    private fun createCameraCapturer(): CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        for (name in enumerator.deviceNames) {
            if (enumerator.isFrontFacing(name)) {
                return enumerator.createCapturer(name, null)
            }
        }
        for (name in enumerator.deviceNames) {
            if (!enumerator.isFrontFacing(name)) {
                return enumerator.createCapturer(name, null)
            }
        }
        return null
    }

    private fun postSdpToSrs(
        url: String,
        sdp: String,
        streamUrl: String,
        callback: (Boolean, String?, String?) -> Unit
    ) {
        Thread {
            try {
                val body = gson.toJson(SrsRequest(sdp = sdp, streamurl = streamUrl))
                val request = Request.Builder()
                    .url(url)
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()
                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val srsResp = gson.fromJson(responseBody, SrsResponse::class.java)
                    if (srsResp.code == 0 && srsResp.sdp != null) {
                        callback(true, srsResp.sdp, null)
                    } else {
                        callback(false, null, "SRS error code: ${srsResp.code}")
                    }
                } else {
                    callback(false, null, "HTTP ${response.code}: $responseBody")
                }
            } catch (e: Exception) {
                Log.e(TAG, "SRS API error", e)
                callback(false, null, e.message)
            }
        }.start()
    }
}
