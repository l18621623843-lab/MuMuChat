package com.kk.mumuchat

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import com.kk.mumuchat.webrtc.WebRTCManager

class MuMuChatApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        WebRTCManager.initializeWebRTC(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}
