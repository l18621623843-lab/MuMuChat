# SRS 完整操作手册

## 目录
- [1. SRS 简介](#1-srs-简介)
- [2. 系统要求](#2-系统要求)
- [3. 部署方式](#3-部署方式)
- [4. 基础配置](#4-基础配置)
- [5. 应用场景配置](#5-应用场景配置)
- [6. 客户端对接](#6-客户端对接)
- [7. 安全配置](#7-安全配置)
- [8. 性能优化](#8-性能优化)
- [9. 监控和管理](#9-监控和管理)
- [10. 常见问题](#10-常见问题)

---

## 1. SRS 简介

### 1.1 什么是 SRS
SRS (Simple Realtime Server) 是一个开源的流媒体服务器，支持：
- RTMP/HLS/HTTP-FLV 直播
- WebRTC 实时通讯
- 低延迟直播
- 转码、录制、转推等功能

### 1.2 核心功能
- **直播推流**：RTMP、SRT、WebRTC 推流
- **播放协议**：RTMP、HLS、HTTP-FLV、WebRTC
- **WebRTC**：双向音视频通话、会议
- **集群**：源站、边缘站、负载均衡
- **录制**：FLV、MP4 格式录制
- **转码**：实时转码、水印
- **DVR**：直播时移、回看

---

## 2. 系统要求

### 2.1 硬件要求
- **CPU**：2核心以上（推荐4核）
- **内存**：2GB以上（推荐4GB）
- **磁盘**：根据录制需求，建议100GB+
- **网络**：公网IP、足够带宽（上行+下行）

### 2.2 软件要求
- **操作系统**：
  - Ubuntu 18.04/20.04/22.04
  - CentOS 7/8
  - macOS（开发测试）
- **依赖**：
  - GCC 4.8+
  - Make
  - Git（源码编译）

---

## 3. 部署方式

### 3.1 Docker 部署（推荐）

#### 3.1.1 快速启动
```bash
# 拉取镜像
docker pull ossrs/srs:5

# 启动容器
docker run -d \
  --name srs \
  -p 1935:1935 \
  -p 1985:1985 \
  -p 8081:8081 \
  -p 8000:8000/udp \
  ossrs/srs:5
```

#### 3.1.2 使用自定义配置
```bash
# 创建配置文件
mkdir -p /opt/srs/conf
cat > /opt/srs/conf/srs.conf << 'EOF'
listen              1935;
max_connections     1000;
daemon              off;
srs_log_tank        console;

http_server {
    enabled         on;
    listen          8081;
    dir             ./objs/nginx/html;
}

http_api {
    enabled         on;
    listen          1985;
}

stats {
    network         0;
}

vhost __defaultVhost__ {
}
EOF

# 启动容器挂载配置
docker run -d \
  --name srs \
  -p 1935:1935 \
  -p 1985:1985 \
  -p 8081:8081 \
  -p 8000:8000/udp \
  -v /opt/srs/conf/srs.conf:/usr/local/srs/conf/docker.conf \
  ossrs/srs:5
```

#### 3.1.3 Docker Compose 部署
```yaml
# docker-compose.yml
version: '3'
services:
  srs:
    image: ossrs/srs:5
    container_name: srs
    ports:
      - "1935:1935"      # RTMP
      - "1985:1985"      # HTTP API
      - "8081:8081"      # HTTP Server
      - "8000:8000/udp"  # WebRTC
    volumes:
      - ./conf/srs.conf:/usr/local/srs/conf/srs.conf
      - ./recordings:/usr/local/srs/objs/nginx/html/recordings
    restart: always
    environment:
      - CANDIDATE=${SERVER_IP}
```

```bash
# 启动
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止
docker-compose down
```

### 3.2 源码编译部署

#### 3.2.1 下载源码
```bash
# 下载最新版本
git clone -b 5.0release https://github.com/ossrs/srs.git
cd srs/trunk

# 或下载指定版本
wget https://github.com/ossrs/srs/archive/v5.0.tar.gz
tar -zxvf v5.0.tar.gz
cd srs-5.0/trunk
```

#### 3.2.2 编译安装
```bash
# 基础编译
./configure

# 启用所有功能
./configure \
  --with-ssl \
  --with-hls \
  --with-dvr \
  --with-transcode \
  --with-http-callback \
  --with-http-server \
  --with-http-api

# 编译
make

# 安装（可选）
sudo make install
```

#### 3.2.3 启动服务
```bash
# 启动
./objs/srs -c conf/srs.conf

# 后台运行
nohup ./objs/srs -c conf/srs.conf > /dev/null 2>&1 &

# 查看日志
tail -f ./objs/srs.log
```

#### 3.2.4 设置开机自启（systemd）
```bash
# 创建服务文件
sudo cat > /etc/systemd/system/srs.service << 'EOF'
[Unit]
Description=SRS Media Server
After=network.target

[Service]
Type=forking
ExecStart=/usr/local/srs/objs/srs -c /usr/local/srs/conf/srs.conf
ExecReload=/bin/kill -HUP $MAINPID
Restart=on-failure
User=root

[Install]
WantedBy=multi-user.target
EOF

# 启用服务
sudo systemctl daemon-reload
sudo systemctl enable srs
sudo systemctl start srs
sudo systemctl status srs
```

---

## 4. 基础配置

### 4.1 配置文件结构
```conf
# 全局配置
listen              1935;           # RTMP监听端口
max_connections     1000;           # 最大连接数
daemon              on;             # 后台运行
pid                 ./objs/srs.pid; # PID文件
srs_log_tank        file;           # 日志输出方式
srs_log_file        ./objs/srs.log; # 日志文件路径
srs_log_level       trace;          # 日志级别

# HTTP Server
http_server {
    enabled         on;
    listen          8081;
    dir             ./objs/nginx/html;
}

# HTTP API
http_api {
    enabled         on;
    listen          1985;
}

# 统计信息
stats {
    network         0;
    disk            sda vda;
}

# 虚拟主机
vhost __defaultVhost__ {
    # 各种功能配置
}
```

### 4.2 端口说明
| 端口 | 协议 | 用途 |
|------|------|------|
| 1935 | RTMP | RTMP推流和播放 |
| 1985 | HTTP | HTTP API接口 |
| 8081 | HTTP | HTTP-FLV播放、HLS播放 |
| 8000 | UDP | WebRTC媒体传输 |
| 8088 | HTTP | HTTPS API（可选）|

---

## 5. 应用场景配置

### 5.1 RTMP 直播

#### 5.1.1 基础配置
```conf
vhost __defaultVhost__ {
    # HLS配置
    hls {
        enabled         on;
        hls_path        ./objs/nginx/html;
        hls_fragment    10;
        hls_window      60;
    }
    
    # HTTP-FLV配置
    http_remux {
        enabled     on;
        mount       [vhost]/[app]/[stream].flv;
    }
}
```

#### 5.1.2 推流地址
```
rtmp://your-server-ip:1935/live/stream_name
```

#### 5.1.3 播放地址
```
# RTMP
rtmp://your-server-ip:1935/live/stream_name

# HTTP-FLV
http://your-server-ip:8081/live/stream_name.flv

# HLS
http://your-server-ip:8081/live/stream_name.m3u8
```

### 5.2 WebRTC 实时通讯

#### 5.2.1 WebRTC 配置
```conf
# RTC服务器配置
rtc_server {
    enabled on;
    listen 8000;
    
    # 候选地址（重要：填写服务器公网IP）
    candidate $CANDIDATE;
}

vhost __defaultVhost__ {
    # WebRTC配置
    rtc {
        enabled     on;
        
        # RTMP转WebRTC
        rtmp_to_rtc off;
        
        # WebRTC转RTMP
        rtc_to_rtmp off;
        
        # 仅WebRTC
        keep_bframe off;
    }
}
```

#### 5.2.2 设置候选地址
```bash
# 方式1：配置文件中设置
export CANDIDATE="your-server-public-ip"

# 方式2：Docker环境变量
docker run -d \
  -e CANDIDATE=your-server-public-ip \
  ...

# 方式3：动态获取
export CANDIDATE=$(curl -s http://checkip.amazonaws.com)
```

#### 5.2.3 WebRTC 推流
```javascript
// 使用 SRS WebRTC SDK
const sdk = new SrsRtcPublisherAsync();

// 推流地址
const url = 'webrtc://your-server-ip/live/stream_name';

// 推流
await sdk.publish(url);
```

#### 5.2.4 WebRTC 播放
```javascript
// 使用 SRS WebRTC SDK
const sdk = new SrsRtcPlayerAsync();

// 播放地址
const url = 'webrtc://your-server-ip/live/stream_name';

// 播放
await sdk.play(url);
```

### 5.3 双向音视频通话（1对1）

#### 5.3.1 配置
```conf
rtc_server {
    enabled on;
    listen 8000;
    candidate $CANDIDATE;
}

vhost __defaultVhost__ {
    rtc {
        enabled     on;
        rtmp_to_rtc off;
        rtc_to_rtmp off;
    }
}
```

#### 5.3.2 实现逻辑
```javascript
// 用户A推流
const publisherA = new SrsRtcPublisherAsync();
await publisherA.publish('webrtc://server/room/userA');

// 用户B推流
const publisherB = new SrsRtcPublisherAsync();
await publisherB.publish('webrtc://server/room/userB');

// 用户A播放用户B的流
const playerA = new SrsRtcPlayerAsync();
await playerA.play('webrtc://server/room/userB');

// 用户B播放用户A的流
const playerB = new SrsRtcPlayerAsync();
await playerB.play('webrtc://server/room/userA');
```

### 5.4 多人视频会议

#### 5.4.1 配置（MCU模式）
```conf
vhost conference {
    rtc {
        enabled     on;
        rtc_to_rtmp off;
        rtmp_to_rtc off;
        
        # 启用混流
        mix {
            enabled on;
        }
    }
}
```

#### 5.4.2 实现逻辑（SFU模式）
```javascript
// 会议室管理
class ConferenceRoom {
    constructor(roomId) {
        this.roomId = roomId;
        this.participants = new Map();
    }
    
    // 用户加入
    async join(userId) {
        // 推送自己的流
        const publisher = new SrsRtcPublisherAsync();
        await publisher.publish(`webrtc://server/conference/${this.roomId}/${userId}`);
        
        // 订阅其他人的流
        const players = [];
        for (let [id, _] of this.participants) {
            const player = new SrsRtcPlayerAsync();
            await player.play(`webrtc://server/conference/${this.roomId}/${id}`);
            players.push(player);
        }
        
        this.participants.set(userId, { publisher, players });
    }
    
    // 用户离开
    async leave(userId) {
        const participant = this.participants.get(userId);
        if (participant) {
            participant.publisher.close();
            participant.players.forEach(p => p.close());
            this.participants.delete(userId);
        }
    }
}
```

### 5.5 直播间（主播+观众）

#### 5.5.1 配置
```conf
vhost liveroom {
    # 启用HLS
    hls {
        enabled         on;
        hls_path        ./objs/nginx/html;
        hls_fragment    2;
        hls_window      10;
    }
    
    # 启用HTTP-FLV
    http_remux {
        enabled     on;
        mount       [vhost]/[app]/[stream].flv;
    }
    
    # 启用WebRTC
    rtc {
        enabled     on;
        rtc_to_rtmp on;  # WebRTC转RTMP
        rtmp_to_rtc on;  # RTMP转WebRTC
    }
    
    # 启用录制
    dvr {
        enabled      on;
        dvr_path     ./objs/nginx/html/recordings/[app]/[stream]/[timestamp].flv;
        dvr_plan     session;
    }
}
```

#### 5.5.2 应用架构
```
主播端（推流）:
  - OBS/FFmpeg: RTMP推流
  - 浏览器/APP: WebRTC推流

观众端（播放）:
  - 浏览器: HTTP-FLV / HLS / WebRTC
  - APP: RTMP / HTTP-FLV / WebRTC
  - 小程序: HLS
```

#### 5.5.3 完整配置示例
```conf
listen              1935;
max_connections     1000;
daemon              off;

http_server {
    enabled         on;
    listen          8081;
    dir             ./objs/nginx/html;
    
    # CORS支持
    crossdomain     on;
}

http_api {
    enabled         on;
    listen          1985;
    crossdomain     on;
}

rtc_server {
    enabled on;
    listen 8000;
    candidate $CANDIDATE;
}

stats {
    network         0;
}

vhost __defaultVhost__ {
    # HLS
    hls {
        enabled         on;
        hls_path        ./objs/nginx/html;
        hls_fragment    3;
        hls_window      20;
        hls_cleanup     on;
        hls_dispose     30;
    }
    
    # HTTP-FLV
    http_remux {
        enabled     on;
        mount       [vhost]/[app]/[stream].flv;
    }
    
    # WebRTC
    rtc {
        enabled     on;
        rtmp_to_rtc on;
        rtc_to_rtmp on;
    }
    
    # DVR录制
    dvr {
        enabled      on;
        dvr_path     ./objs/nginx/html/recordings/[app]/[stream]/[2006]/[01]/[02]/[15].[04].[05].[999].flv;
        dvr_plan     session;
        dvr_duration 30;
        dvr_wait_keyframe on;
    }
    
    # 转码（可选）
    transcode {
        enabled     off;
        ffmpeg      ./objs/ffmpeg/bin/ffmpeg;
        engine transcoded {
            enabled         on;
            vcodec          libx264;
            vbitrate        800;
            vfps            25;
            vwidth          1280;
            vheight         720;
            vthreads        4;
            vprofile        main;
            vpreset         medium;
            acodec          libfdk_aac;
            abitrate        96;
            asample_rate    44100;
            achannels       2;
            output          rtmp://127.0.0.1:[port]/[app]?vhost=[vhost]/[stream]_[engine];
        }
    }
}
```

### 5.6 低延迟直播

#### 5.6.1 配置优化
```conf
vhost lowlatency {
    # 关闭GOP缓存
    gop_cache       off;
    
    # 队列长度
    queue_length    3;
    
    # 最小延迟
    min_latency     on;
    
    # HLS低延迟配置
    hls {
        enabled         on;
        hls_fragment    1;    # 1秒切片
        hls_window      3;    # 3个切片
    }
    
    # WebRTC（延迟最低）
    rtc {
        enabled     on;
        rtmp_to_rtc on;
    }
}
```

---

## 6. 客户端对接

### 6.1 Web端集成

#### 6.1.1 HTTP-FLV 播放（推荐）
```html
<!DOCTYPE html>
<html>
<head>
    <title>HTTP-FLV播放</title>
    <script src="https://cdn.jsdelivr.net/npm/flv.js/dist/flv.min.js"></script>
</head>
<body>
    <video id="videoElement" controls width="640" height="480"></video>
    
    <script>
        if (flvjs.isSupported()) {
            var videoElement = document.getElementById('videoElement');
            var flvPlayer = flvjs.createPlayer({
                type: 'flv',
                url: 'http://your-server-ip:8081/live/stream_name.flv',
                isLive: true
            }, {
                enableWorker: true,
                enableStashBuffer: false,
                stashInitialSize: 128,
                lazyLoad: false
            });
            
            flvPlayer.attachMediaElement(videoElement);
            flvPlayer.load();
            flvPlayer.play();
        }
    </script>
</body>
</html>
```

#### 6.1.2 HLS 播放
```html
<!DOCTYPE html>
<html>
<head>
    <title>HLS播放</title>
    <script src="https://cdn.jsdelivr.net/npm/hls.js/dist/hls.min.js"></script>
</head>
<body>
    <video id="video" controls width="640" height="480"></video>
    
    <script>
        var video = document.getElementById('video');
        var videoSrc = 'http://your-server-ip:8081/live/stream_name.m3u8';
        
        if (Hls.isSupported()) {
            var hls = new Hls({
                enableWorker: true,
                lowLatencyMode: true
            });
            hls.loadSource(videoSrc);
            hls.attachMedia(video);
            hls.on(Hls.Events.MANIFEST_PARSED, function() {
                video.play();
            });
        } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
            video.src = videoSrc;
            video.addEventListener('loadedmetadata', function() {
                video.play();
            });
        }
    </script>
</body>
</html>
```

#### 6.1.3 WebRTC 推流
```html
<!DOCTYPE html>
<html>
<head>
    <title>WebRTC推流</title>
    <script src="https://cdn.jsdelivr.net/npm/srs.sdk.js@5/dist/srs.sdk.js"></script>
</head>
<body>
    <video id="localVideo" autoplay muted width="640" height="480"></video>
    <button onclick="startPublish()">开始推流</button>
    <button onclick="stopPublish()">停止推流</button>
    
    <script>
        let publisher = null;
        
        async function startPublish() {
            // 获取本地媒体流
            const stream = await navigator.mediaDevices.getUserMedia({
                video: true,
                audio: true
            });
            
            document.getElementById('localVideo').srcObject = stream;
            
            // 创建发布者
            publisher = new SrsRtcPublisherAsync();
            
            // 推流URL
            const url = 'webrtc://your-server-ip/live/stream_name';
            
            // 开始推流
            await publisher.publish(url, stream);
            
            console.log('推流成功');
        }
        
        async function stopPublish() {
            if (publisher) {
                await publisher.close();
                publisher = null;
                console.log('停止推流');
            }
        }
    </script>
</body>
</html>
```

#### 6.1.4 WebRTC 播放
```html
<!DOCTYPE html>
<html>
<head>
    <title>WebRTC播放</title>
    <script src="https://cdn.jsdelivr.net/npm/srs.sdk.js@5/dist/srs.sdk.js"></script>
</head>
<body>
    <video id="remoteVideo" autoplay controls width="640" height="480"></video>
    <button onclick="startPlay()">开始播放</button>
    <button onclick="stopPlay()">停止播放</button>
    
    <script>
        let player = null;
        
        async function startPlay() {
            // 创建播放器
            player = new SrsRtcPlayerAsync();
            
            // 播放URL
            const url = 'webrtc://your-server-ip/live/stream_name';
            
            // 开始播放
            const stream = await player.play(url);
            
            // 设置视频源
            document.getElementById('remoteVideo').srcObject = stream;
            
            console.log('播放成功');
        }
        
        async function stopPlay() {
            if (player) {
                await player.close();
                player = null;
                console.log('停止播放');
            }
        }
    </script>
</body>
</html>
```

### 6.2 iOS 端集成

#### 6.2.1 使用 IJKPlayer 播放 HTTP-FLV
```swift
import IJKMediaFramework

class ViewController: UIViewController {
    var player: IJKFFMoviePlayerController?
    
    func playStream() {
        let url = URL(string: "http://your-server-ip:8081/live/stream_name.flv")!
        
        let options = IJKFFOptions.byDefault()
        options?.setOptionIntValue(1, forKey: "analyzemaxduration", of: kIJKFFOptionCategoryFormat)
        options?.setOptionIntValue(1, forKey: "probesize", of: kIJKFFOptionCategoryFormat)
        options?.setOptionIntValue(0, forKey: "packet-buffering", of: kIJKFFOptionCategoryPlayer)
        
        player = IJKFFMoviePlayerController(contentURL: url, with: options)
        player?.view.frame = self.view.bounds
        self.view.addSubview(player!.view)
        player?.prepareToPlay()
        player?.play()
    }
}
```

#### 6.2.2 使用 WebRTC 推流
```swift
import WebRTC

class WebRTCPublisher {
    var peerConnection: RTCPeerConnection?
    var localStream: RTCMediaStream?
    
    func publish(streamUrl: String) {
        // 创建 PeerConnection
        let config = RTCConfiguration()
        config.iceServers = [RTCIceServer(urlStrings: ["stun:stun.l.google.com:19302"])]
        
        let constraints = RTCMediaConstraints(
            mandatoryConstraints: nil,
            optionalConstraints: ["DtlsSrtpKeyAgreement": "true"]
        )
        
        peerConnection = RTCPeerConnectionFactory().peerConnection(
            with: config,
            constraints: constraints,
            delegate: self
        )
        
        // 获取本地流
        localStream = createLocalStream()
        peerConnection?.add(localStream!)
        
        // 创建 Offer 并发送到 SRS
        // ... SDP交换逻辑
    }
    
    func createLocalStream() -> RTCMediaStream {
        let stream = RTCPeerConnectionFactory().mediaStream(withStreamId: "local")
        
        // 添加音频轨道
        let audioSource = RTCPeerConnectionFactory().audioSource(with: nil)
        let audioTrack = RTCPeerConnectionFactory().audioTrack(with: audioSource, trackId: "audio0")
        stream.addAudioTrack(audioTrack)
        
        // 添加视频轨道
        let videoSource = RTCPeerConnectionFactory().videoSource()
        let videoTrack = RTCPeerConnectionFactory().videoTrack(with: videoSource, trackId: "video0")
        stream.addVideoTrack(videoTrack)
        
        return stream
    }
}
```

### 6.3 Android 端集成

#### 6.3.1 使用 ijkplayer 播放
```kotlin
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class PlayerActivity : AppCompatActivity() {
    private var mediaPlayer: IjkMediaPlayer? = null
    
    fun playStream() {
        mediaPlayer = IjkMediaPlayer()
        
        // 配置选项
        mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 1L)
        mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024L)
        mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0L)
        mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L)
        
        // 设置数据源
        mediaPlayer?.dataSource = "http://your-server-ip:8081/live/stream_name.flv"
        
        // 设置Surface
        mediaPlayer?.setDisplay(surfaceView.holder)
        
        // 准备并播放
        mediaPlayer?.prepareAsync()
        mediaPlayer?.setOnPreparedListener {
            it.start()
        }
    }
}
```

#### 6.3.2 使用 WebRTC 推流
```kotlin
import org.webrtc.*

class WebRTCPublisher(context: Context) {
    private val peerConnectionFactory: PeerConnectionFactory
    private var peerConnection: PeerConnection? = null
    private var localStream: MediaStream? = null
    
    init {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context).createInitializationOptions()
        )
        
        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory()
    }
    
    fun publish() {
        // 创建本地流
        localStream = peerConnectionFactory.createLocalMediaStream("local")
        
        // 添加音频轨道
        val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        val audioTrack = peerConnectionFactory.createAudioTrack("audio0", audioSource)
        localStream?.addTrack(audioTrack)
        
        // 添加视频轨道
        val videoSource = peerConnectionFactory.createVideoSource(false)
        val videoTrack = peerConnectionFactory.createVideoTrack("video0", videoSource)
        localStream?.addTrack(videoTrack)
        
        // 创建 PeerConnection
        val rtcConfig = PeerConnection.RTCConfiguration(
            listOf(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
        )
        
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            // 实现回调方法
        })
        
        peerConnection?.addStream(localStream)
        
        // 创建Offer并发送到SRS
        // ... SDP交换逻辑
    }
}
```

### 6.4 小程序集成

#### 6.4.1 使用 live-player 组件
```xml
<!-- 播放器 -->
<live-player
  id="player"
  src="{{playUrl}}"
  mode="live"
  autoplay="{{true}}"
  muted="{{false}}"
  orientation="vertical"
  object-fit="contain"
  background-mute="{{false}}"
  min-cache="1"
  max-cache="3"
  bindstatechange="statechange"
  binderror="error"
/>
```

```javascript
Page({
  data: {
    playUrl: 'http://your-server-ip:8081/live/stream_name.m3u8' // 使用HLS
  },
  
  statechange(e) {
    console.log('live-player state changed:', e.detail.code)
  },
  
  error(e) {
    console.error('live-player error:', e.detail.errMsg)
  }
})
```

#### 6.4.2 使用 live-pusher 组件
```xml
<!-- 推流器 -->
<live-pusher
  id="pusher"
  url="{{pushUrl}}"
  mode="RTC"
  autopush="{{true}}"
  muted="{{false}}"
  enable-camera="{{true}}"
  beauty="5"
  whiteness="5"
  bindstatechange="statechange"
  binderror="error"
/>
```

```javascript
Page({
  data: {
    pushUrl: 'rtmp://your-server-ip:1935/live/stream_name'
  },
  
  statechange(e) {
    console.log('live-pusher state changed:', e.detail.code)
  },
  
  error(e) {
    console.error('live-pusher error:', e.detail.errMsg)
  }
})
```

### 6.5 推流工具

#### 6.5.1 OBS Studio 推流
```
服务器: rtmp://your-server-ip:1935/live
串流密钥: stream_name
```

#### 6.5.2 FFmpeg 推流
```bash
# RTMP推流
ffmpeg -re -i input.mp4 -c copy -f flv rtmp://your-server-ip:1935/live/stream_name

# 摄像头推流
ffmpeg -f avfoundation -i "0:0" -c:v libx264 -preset ultrafast -c:a aac -f flv rtmp://your-server-ip:1935/live/camera

# 屏幕推流
ffmpeg -f gdigrab -i desktop -c:v libx264 -preset ultrafast -c:a aac -f flv rtmp://your-server-ip:1935/live/screen

# 循环推流
ffmpeg -re -stream_loop -1 -i input.mp4 -c copy -f flv rtmp://your-server-ip:1935/live/stream_name
```

---

## 7. 安全配置

### 7.1 HTTP Callback 鉴权

#### 7.1.1 配置回调
```conf
vhost __defaultVhost__ {
    http_hooks {
        enabled         on;
        
        # 推流鉴权
        on_publish      http://your-auth-server/api/on_publish;
        
        # 播放鉴权
        on_play         http://your-auth-server/api/on_play;
        
        # 推流结束
        on_unpublish    http://your-auth-server/api/on_unpublish;
        
        # 播放结束
        on_stop         http://your-auth-server/api/on_stop;
        
        # DVR录制完成
        on_dvr          http://your-auth-server/api/on_dvr;
    }
}
```

#### 7.1.2 鉴权服务器示例（Node.js）
```javascript
const express = require('express');
const app = express();

app.use(express.json());

// 推流鉴权
app.post('/api/on_publish', (req, res) => {
    const { app, stream, param } = req.body;
    
    // 验证token
    const token = new URLSearchParams(param).get('token');
    
    if (isValidToken(token, stream)) {
        res.json({ code: 0 }); // 允许推流
    } else {
        res.json({ code: 1 }); // 拒绝推流
    }
});

// 播放鉴权
app.post('/api/on_play', (req, res) => {
    const { app, stream, param } = req.body;
    
    // 验证token或其他逻辑
    const token = new URLSearchParams(param).get('token');
    
    if (isValidToken(token, stream)) {
        res.json({ code: 0 }); // 允许播放
    } else {
        res.json({ code: 1 }); // 拒绝播放
    }
});

function isValidToken(token, stream) {
    // 实现你的token验证逻辑
    return token === 'valid_token';
}

app.listen(3000, () => {
    console.log('Auth server running on port 3000');
});
```

### 7.2 Token 鉴权

#### 7.2.1 生成带token的URL
```javascript
// 推流URL
function generatePublishUrl(stream, secret) {
    const timestamp = Math.floor(Date.now() / 1000) + 3600; // 1小时有效期
    const token = md5(`${stream}-${timestamp}-${secret}`);
    return `rtmp://server:1935/live/${stream}?timestamp=${timestamp}&token=${token}`;
}

// 播放URL
function generatePlayUrl(stream, secret) {
    const timestamp = Math.floor(Date.now() / 1000) + 3600;
    const token = md5(`${stream}-${timestamp}-${secret}`);
    return `http://server:8081/live/${stream}.flv?timestamp=${timestamp}&token=${token}`;
}
```

### 7.3 HTTPS/WSS 配置

#### 7.3.1 使用 Nginx 反向代理
```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    
    # HTTP-FLV
    location /live/ {
        proxy_pass http://127.0.0.1:8081/live/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }
    
    # HTTP API
    location /api/ {
        proxy_pass http://127.0.0.1:1985/api/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
    }
    
    # WebRTC
    location /rtc/ {
        proxy_pass http://127.0.0.1:1985/rtc/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
    }
}
```

### 7.4 防盗链

#### 7.4.1 Referer 防盗链
```conf
vhost __defaultVhost__ {
    refer {
        enabled         on;
        all             deny;
        allow           your-domain.com *.your-domain.com;
    }
}
```

---

## 8. 性能优化

### 8.1 系统优化

#### 8.1.1 系统参数优化
```bash
# 修改 /etc/sysctl.conf
net.core.rmem_max = 134217728
net.core.wmem_max = 134217728
net.ipv4.tcp_rmem = 4096 87380 67108864
net.ipv4.tcp_wmem = 4096 65536 67108864
net.ipv4.tcp_mem = 94500000 915000000 927000000
net.core.netdev_max_backlog = 5000
net.ipv4.tcp_max_syn_backlog = 8192
fs.file-max = 1000000

# 应用配置
sudo sysctl -p
```

#### 8.1.2 文件句柄限制
```bash
# 修改 /etc/security/limits.conf
* soft nofile 1000000
* hard nofile 1000000
* soft nproc 1000000
* hard nproc 1000000
```

### 8.2 SRS 配置优化

#### 8.2.1 连接优化
```conf
listen              1935;
max_connections     10000;      # 增加最大连接数

# 工作进程数（CPU核心数）
workers             4;

# 缓冲区优化
tcp_nodelay         on;
send_min_size       131072;
```

#### 8.2.2 GOP 缓存优化
```conf
vhost __defaultVhost__ {
    # 开启GOP缓存（降低首屏时间）
    gop_cache       on;
    
    # 队列长度
    queue_length    10;
}
```

### 8.3 集群部署

#### 8.3.1 源站配置
```conf
# origin.conf
listen              1935;
max_connections     1000;

vhost __defaultVhost__ {
    # 集群模式：源站
    cluster {
        mode            local;
    }
    
    # 允许边缘站回源
    origin {
        enabled         on;
    }
}
```

#### 8.3.2 边缘站配置
```conf
# edge.conf
listen              1935;
max_connections     10000;

vhost __defaultVhost__ {
    # 集群模式：边缘
    cluster {
        mode            remote;
        origin          origin-server-ip:1935;
        token_traverse  on;
    }
}
```

---

## 9. 监控和管理

### 9.1 HTTP API

#### 9.1.1 获取服务器信息
```bash
# 获取版本信息
curl http://your-server-ip:1985/api/v1/versions

# 获取服务器统计
curl http://your-server-ip:1985/api/v1/summaries
```

#### 9.1.2 流管理
```bash
# 获取所有流
curl http://your-server-ip:1985/api/v1/streams

# 获取指定流信息
curl http://your-server-ip:1985/api/v1/streams/live/stream_name

# 踢掉指定客户端
curl -X DELETE http://your-server-ip:1985/api/v1/clients/CLIENT_ID
```

#### 9.1.3 录制管理
```bash
# 开始录制
curl -X POST http://your-server-ip:1985/api/v1/dvrs \
  -H "Content-Type: application/json" \
  -d '{"vhost":"__defaultVhost__","app":"live","stream":"stream_name"}'

# 停止录制
curl -X DELETE http://your-server-ip:1985/api/v1/dvrs/DVR_ID
```

### 9.2 控制台

访问内置控制台：
```
http://your-server-ip:8081/console/
```

功能包括：
- 实时流列表
- 客户端连接
- 带宽统计
- 服务器状态

### 9.3 Prometheus 监控

#### 9.3.1 启用 Exporter
```conf
exporter {
    enabled     on;
    listen      9972;
    label       app="srs";
}
```

#### 9.3.2 Prometheus 配置
```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'srs'
    static_configs:
      - targets: ['your-server-ip:9972']
```

---

## 10. 常见问题

### 10.1 推流相关

#### Q1: RTMP 推流失败？
```
检查项：
1. 端口1935是否开放
2. 防火墙规则
3. 推流地址是否正确
4. 检查SRS日志：tail -f objs/srs.log
```

#### Q2: WebRTC 推流无声音？
```
检查项：
1. 浏览器麦克风权限
2. HTTPS环境（生产环境必须）
3. 检查候选地址配置：CANDIDATE变量
```

### 10.2 播放相关

#### Q3: HTTP-FLV 播放卡顿？
```
优化方案：
1. 关闭GOP缓存：gop_cache off
2. 调整队列长度：queue_length 3
3. 启用最小延迟：min_latency on
4. 客户端配置：enableStashBuffer: false
```

#### Q4: HLS 延迟太高？
```
优化配置：
hls_fragment    1;    # 切片时长1秒
hls_window      3;    # 保留3个切片
```

### 10.3 WebRTC 相关

#### Q5: WebRTC 连接失败？
```
检查项：
1. UDP端口8000是否开放
2. CANDIDATE是否设置为公网IP
3. STUN/TURN服务器配置
4. 浏览器控制台错误信息
```

#### Q6: WebRTC 画面黑屏？
```
检查项：
1. 摄像头/麦克风权限
2. HTTPS环境
3. getUserMedia是否成功
4. SDP协商是否正常
```

### 10.4 性能相关

#### Q7: CPU占用过高？
```
优化方案：
1. 关闭转码功能
2. 调整GOP缓存
3. 使用硬件加速
4. 集群部署分担负载
```

#### Q8: 带宽不足？
```
解决方案：
1. 使用CDN分发
2. 多码率转码
3. 集群边缘节点
4. 降低视频码率
```

### 10.5 录制相关

#### Q9: DVR录制失败？
```
检查项：
1. 录制路径权限
2. 磁盘空间
3. dvr配置是否启用
4. 检查日志错误信息
```

### 10.6 鉴权相关

#### Q10: HTTP Callback 不生效？
```
检查项：
1. 回调服务器地址是否可访问
2. 回调服务器返回格式正确
3. SRS日志中的回调请求
4. 防火墙规则
```

---

## 附录

### A. 完整配置示例

```conf
# 完整的生产环境配置示例
listen              1935;
max_connections     10000;
daemon              on;
pid                 ./objs/srs.pid;
srs_log_tank        file;
srs_log_file        ./objs/srs.log;
srs_log_level       warn;
ff_log_dir          ./objs;

# HTTP服务器
http_server {
    enabled         on;
    listen          8081;
    dir             ./objs/nginx/html;
    crossdomain     on;
}

# HTTP API
http_api {
    enabled         on;
    listen          1985;
    crossdomain     on;
    raw_api {
        enabled             on;
        allow_reload        on;
        allow_query         on;
        allow_update        on;
    }
}

# WebRTC服务器
rtc_server {
    enabled on;
    listen 8000;
    candidate $CANDIDATE;
}

# 统计信息
stats {
    network         0;
    disk            sda sdb;
}

# 心跳检测
heartbeat {
    enabled         off;
    interval        9.3;
    url             http://your-server/api/v1/servers;
}

# 默认虚拟主机
vhost __defaultVhost__ {
    # HLS配置
    hls {
        enabled         on;
        hls_path        ./objs/nginx/html;
        hls_fragment    3;
        hls_window      20;
        hls_cleanup     on;
        hls_dispose     30;
        hls_m3u8_file   [app]/[stream].m3u8;
        hls_ts_file     [app]/[stream]-[seq].ts;
    }
    
    # HTTP-FLV配置
    http_remux {
        enabled     on;
        mount       [vhost]/[app]/[stream].flv;
    }
    
    # WebRTC配置
    rtc {
        enabled     on;
        rtmp_to_rtc on;
        rtc_to_rtmp on;
        keep_bframe off;
    }
    
    # DVR录制
    dvr {
        enabled      on;
        dvr_path     ./objs/nginx/html/recordings/[app]/[stream]/[2006]/[01]/[02]/[15].[04].[05].[999].flv;
        dvr_plan     session;
        dvr_duration 30;
        dvr_wait_keyframe on;
        time_jitter             full;
    }
    
    # HTTP回调
    http_hooks {
        enabled         on;
        on_publish      http://your-auth-server/api/on_publish;
        on_unpublish    http://your-auth-server/api/on_unpublish;
        on_play         http://your-auth-server/api/on_play;
        on_stop         http://your-auth-server/api/on_stop;
        on_dvr          http://your-auth-server/api/on_dvr;
    }
    
    # 安全配置
    security {
        enabled         on;
        seo_switch      on;
    }
    
    # GOP缓存
    gop_cache       on;
    queue_length    10;
    
    # 防盗链
    refer {
        enabled         on;
        all             deny;
        allow           your-domain.com *.your-domain.com;
    }
}
```

### B. 常用命令

```bash
# 启动
./objs/srs -c conf/srs.conf

# 停止
killall srs

# 重载配置（平滑重启）
killall -1 srs

# 查看版本
./objs/srs -v

# 检查配置
./objs/srs -t -c conf/srs.conf

# 查看进程
ps aux | grep srs

# 查看端口
netstat -nltp | grep srs
```

### C. 参考资源

- **官方文档**: https://ossrs.io/
- **GitHub**: https://github.com/ossrs/srs
- **Wiki**: https://github.com/ossrs/srs/wiki
- **Docker Hub**: https://hub.docker.com/r/ossrs/srs
- **讨论区**: https://github.com/ossrs/srs/discussions

---

**文档版本**: 1.0  
**最后更新**: 2025年2月  
**适用版本**: SRS 5.x
