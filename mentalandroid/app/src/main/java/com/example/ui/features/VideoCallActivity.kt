package com.example.ui.features

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.network.WebSocketManager
import com.example.ui.theme.MentalTheme
import org.webrtc.*
import org.webrtc.Camera2Enumerator
import timber.log.Timber
import org.json.JSONObject
import kotlinx.coroutines.*
import android.os.Handler
import android.os.Looper
import androidx.compose.material.icons.automirrored.filled.CallMissed


class VideoCallActivity : ComponentActivity() {
    companion object {
        const val EXTRA_USER_ID = "userId"
        const val EXTRA_COUNSELOR_ID = "counselorId"
        const val EXTRA_CALL_ID = "callId"
        const val EXTRA_INCOMING_CALL = "incomingCall"
        const val EXTRA_CALLER_NAME = "callerName"
        const val EXTRA_CALLER_AVATAR = "callerAvatar"

        fun start(context: Context, userId: Long, counselorId: Int, callId: String, isIncomingCall: Boolean = false, callerName: String? = null, callerAvatar: String? = null) {
            val intent = Intent(context, VideoCallActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
                putExtra(EXTRA_COUNSELOR_ID, counselorId)
                putExtra(EXTRA_CALL_ID, callId)
                putExtra(EXTRA_INCOMING_CALL, isIncomingCall)
                callerName?.let { putExtra(EXTRA_CALLER_NAME, it) }
                callerAvatar?.let { putExtra(EXTRA_CALLER_AVATAR, it) }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    // WebRTC相关变量
    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private var peerConnection: PeerConnection? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private val eglBase = EglBase.create()

    // SurfaceViewRenderers
    private lateinit var localVideoView: SurfaceViewRenderer
    private lateinit var remoteVideoView: SurfaceViewRenderer

    // 状态变量
    private var currentCallId: String = ""
    private var currentUserId: Long = 0L
    private var currentCounselorId: Int = 0
    private var isIncomingCall: Boolean = false
    
    // 权限相关
    private val CAMERA_PERMISSION_CODE = 1001
    private val AUDIO_PERMISSION_CODE = 1002
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化参数
        currentUserId = intent.getLongExtra(EXTRA_USER_ID, 0L)
        currentCounselorId = intent.getIntExtra(EXTRA_COUNSELOR_ID, 0)
        currentCallId = intent.getStringExtra(EXTRA_CALL_ID) ?: ""
        isIncomingCall = intent.getBooleanExtra(EXTRA_INCOMING_CALL, false)
        
        // 请求必要的权限
        requestPermissionsIfNeeded()

        // 初始化WebRTC
        initializeWebRTC()

        val callerName = intent.getStringExtra(EXTRA_CALLER_NAME)
        val callerAvatar = intent.getStringExtra(EXTRA_CALLER_AVATAR)

        setContent {
            MentalTheme {
                VideoCallScreen(
                    userId = currentUserId,
                    counselorId = currentCounselorId,
                    callId = currentCallId,
                    isIncomingCall = isIncomingCall,
                    callerName = callerName,
                    callerAvatar = callerAvatar,
                    localVideoView = localVideoView,
                    remoteVideoView = remoteVideoView,
                    onBackPress = { finish() },
                    onAcceptCall = { acceptCall() },
                    onRejectCall = { rejectCall() },
                    onEndCall = { endCall() },
                    onToggleVideo = { toggleVideo() },
                    onToggleMic = { toggleMic() }
                )
            }
        }
        // 如果是去电（主动呼叫），自动接受通话
        if (!isIncomingCall) {
            Handler(Looper.getMainLooper()).postDelayed({
                acceptCall()
            }, 1000)
        }
    }

    private fun initializeWebRTC() {
        // 初始化PeerConnectionFactory
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions
                .builder(this)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )

        val options = PeerConnectionFactory.Options()
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .createPeerConnectionFactory()

        // 初始化视频视图
        localVideoView = SurfaceViewRenderer(this).apply {
            init(eglBase.eglBaseContext, null)
            setMirror(true)
            setEnableHardwareScaler(true)
            setZOrderMediaOverlay(true)
        }

        remoteVideoView = SurfaceViewRenderer(this).apply {
            init(eglBase.eglBaseContext, null)
            setEnableHardwareScaler(true)
        }

        Timber.d("WebRTC initialized successfully")
    }

    private fun createPeerConnection() {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer()
        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            keyType = PeerConnection.KeyType.ECDSA
            // 启用更强的兼容性设置
            var enableDtlsSrtp = true
        }

        peerConnection = peerConnectionFactory.createPeerConnection(
            rtcConfig,
            object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate) {
                    Timber.d("生成ICE候选: ${candidate.sdpMid} - ${candidate.sdpMLineIndex}")
                    sendIceCandidate(candidate)
                }

                override fun onAddStream(stream: MediaStream) {
                    Timber.d("收到远程流: , 视频轨道: ${stream.videoTracks.size}, 音频轨道: ${stream.audioTracks.size}")
                    runOnUiThread {
                        val videoTracks = stream.videoTracks
                        if (videoTracks.isNotEmpty()) {
                            val remoteVideoTrack = videoTracks[0]
                            remoteVideoTrack.addSink(remoteVideoView)
                            remoteVideoTrack.setEnabled(true)
                            Timber.d("远程视频轨道已设置: ${remoteVideoTrack.id()}")

                            // 检查轨道状态
                            Timber.d("远程视频轨道状态: enabled=${remoteVideoTrack.enabled()}")
                        }
                    }
                }

                override fun onAddTrack(receiver: RtpReceiver, streams: Array<out MediaStream>) {
                    Timber.d("通过onAddTrack收到远程轨道: ${receiver.track()?.id()}")
                    runOnUiThread {
                        val track = receiver.track()
                        if (track is VideoTrack) {
                            track.addSink(remoteVideoView)
                            track.setEnabled(true)
                            Timber.d("视频轨道已通过onAddTrack设置: ${track.id()}")
                        }
                    }
                }

                override fun onTrack(transceiver: RtpTransceiver) {
                    Timber.d("通过onTrack收到媒体: ${transceiver.mediaType}")
                    runOnUiThread {
                        if (transceiver.mediaType == MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO) {
                            val track = transceiver.receiver.track()
                            if (track is VideoTrack) {
                                track.addSink(remoteVideoView)
                                track.setEnabled(true)
                                Timber.d("视频轨道已通过onTrack设置: ${track.id()}")
                            }
                        }
                    }
                }

                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
                    Timber.d("PeerConnection状态改变: $newState")
                    runOnUiThread {
                        when (newState) {
                            PeerConnection.PeerConnectionState.CONNECTED -> {
                                Toast.makeText(this@VideoCallActivity, "视频通话连接已建立", Toast.LENGTH_SHORT).show()
                                checkMediaTracks()
                            }
                            PeerConnection.PeerConnectionState.FAILED -> {
                                Toast.makeText(this@VideoCallActivity, "视频连接失败", Toast.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                    }
                }

                override fun onSignalingChange(state: PeerConnection.SignalingState?) {
                    Timber.d("Signaling state: $state")
                }

                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                    Timber.d("ICE connection state: $state")
                    runOnUiThread {
                        when (state) {
                            PeerConnection.IceConnectionState.CONNECTED -> {
                                Toast.makeText(this@VideoCallActivity, "视频通话已连接", Toast.LENGTH_SHORT).show()
                            }
                            PeerConnection.IceConnectionState.DISCONNECTED -> {
                                Toast.makeText(this@VideoCallActivity, "视频连接断开", Toast.LENGTH_SHORT).show()
                            }
                            PeerConnection.IceConnectionState.FAILED -> {
                                Toast.makeText(this@VideoCallActivity, "视频连接失败", Toast.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                    }
                }

                override fun onIceConnectionReceivingChange(receiving: Boolean) {}
                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
                override fun onRemoveStream(stream: MediaStream?) {}
                override fun onDataChannel(channel: DataChannel?) {}
                override fun onRenegotiationNeeded() {}
            }
        )

        Timber.d("PeerConnection created: ${peerConnection != null}")
    }

    // 改进的 startLocalVideo 方法
    private fun startLocalVideo() {
        try {
            if (!hasRequiredPermissions()) {
                Timber.e("缺少相机或麦克风权限")
                return
            }

            // 创建视频源
            val videoSource = peerConnectionFactory.createVideoSource(false)
            val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)

            // 创建摄像头捕获器
            videoCapturer = createCameraCapturer()
            videoCapturer?.initialize(surfaceTextureHelper, applicationContext, videoSource.capturerObserver)
            videoCapturer?.startCapture(1280, 720, 30)

            // 创建视频轨道
            localVideoTrack = peerConnectionFactory.createVideoTrack("local_video", videoSource)
            localVideoTrack?.addSink(localVideoView)
            localVideoTrack?.setEnabled(true)

            // 创建音频源和轨道
            val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
            localAudioTrack = peerConnectionFactory.createAudioTrack("local_audio", audioSource)
            localAudioTrack?.setEnabled(true)

            // 重要：在PeerConnection创建后立即添加轨道
            if (peerConnection != null) {
                localVideoTrack?.let { videoTrack ->
                    val sender = peerConnection?.addTrack(videoTrack, listOf("local_stream"))
                    Timber.d("本地视频轨道已添加到PeerConnection, sender: ${sender != null}")
                }
                localAudioTrack?.let { audioTrack ->
                    val sender = peerConnection?.addTrack(audioTrack, listOf("local_stream"))
                    Timber.d("本地音频轨道已添加到PeerConnection, sender: ${sender != null}")
                }
            }

            Timber.d("本地视频启动完成")

        } catch (e: Exception) {
            Timber.e(e, "启动本地视频失败")
            runOnUiThread {
                Toast.makeText(this, "启动摄像头失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createCameraCapturer(): CameraVideoCapturer? {
        return try {
            val enumerator = Camera2Enumerator(this)
            val deviceNames = enumerator.deviceNames

            // 优先选择前置摄像头
            for (deviceName in deviceNames) {
                if (enumerator.isFrontFacing(deviceName)) {
                    return enumerator.createCapturer(deviceName, null)
                }
            }

            // 如果没有前置摄像头，选择第一个可用的
            if (deviceNames.isNotEmpty()) {
                enumerator.createCapturer(deviceNames[0], null)
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to create camera capturer")
            null
        }
    }

    private fun sendIceCandidate(candidate: IceCandidate) {
        try {
            val candidateJson = JSONObject().apply {
                put("sdpMid", candidate.sdpMid)
                put("sdpMLineIndex", candidate.sdpMLineIndex)
                put("candidate", candidate.sdp)
            }

            WebSocketManager.getInstance().sendWebRTCSignal(
                senderId = currentUserId,
                receiverId = currentCounselorId,
                senderType = "user",
                type = "ice-candidate",
                data = candidateJson.toString(),
                callId = currentCallId
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to send ICE candidate")
        }
    }

    private fun requestPermissionsIfNeeded() {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                CAMERA_PERMISSION_CODE
            )
        } else {
            // 如果已经有权限，立即初始化摄像头
            initializeCameraIfReady()
        }
    }

    private fun initializeCameraIfReady() {
        if (hasRequiredPermissions() && ::peerConnectionFactory.isInitialized) {
            // 延迟一点确保UI已经准备好
            Handler(Looper.getMainLooper()).postDelayed({
                startLocalVideo()
            }, 500)
        }
    }
    
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            CAMERA_PERMISSION_CODE, AUDIO_PERMISSION_CODE -> {
                val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                if (!allGranted) {
                    Toast.makeText(
                        this,
                        "无法获取相机或麦克风权限，视频通话功能可能无法正常使用",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    private fun hasRequiredPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun acceptCall() {
        Timber.d("Accepting call: $currentCallId")

        try {
            // 检查权限
            if (!hasRequiredPermissions()) {
                Toast.makeText(this, "请先授予相机和麦克风权限", Toast.LENGTH_SHORT).show()
                requestPermissionsIfNeeded()
                return
            }

            // 确保WebRTC已初始化
            if (!::peerConnectionFactory.isInitialized) {
                initializeWebRTC()
            }

            // 创建PeerConnection
            createPeerConnection()

            // 启动本地视频（这会创建并添加媒体轨道）
            startLocalVideo()

            // 发送接受通话状态
            WebSocketManager.getInstance().sendWebRTCStatus(
                senderId = currentUserId,
                receiverId = currentCounselorId,
                senderType = "user",
                status = "accepted",
                callId = currentCallId
            )

            // 如果是来电，需要等待对方的offer
            if (isIncomingCall) {
                Timber.d("Waiting for remote offer...")
                // 这里会在收到offer时处理
            } else {
                // 如果是去电，创建并发送offer
                createAndSendOffer()
            }

            // 更新UI状态
            runOnUiThread {
                Toast.makeText(this, "正在建立视频连接...", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Timber.e(e, "Failed to accept call")
            runOnUiThread {
                Toast.makeText(this, "接受通话失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // 改进的 createAndSendOffer 方法
    private fun createAndSendOffer() {
        val mediaConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription) {
                Timber.d("Offer创建成功: ${desc.type}")

                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        Timber.d("本地描述设置成功")

                        val offerJson = JSONObject().apply {
                            put("type", desc.type.canonicalForm())
                            put("sdp", desc.description)
                        }

                        WebSocketManager.getInstance().sendWebRTCSignal(
                            senderId = currentUserId,
                            receiverId = currentCounselorId,
                            senderType = "user",
                            type = "offer",
                            data = offerJson.toString(),
                            callId = currentCallId
                        )
                        Timber.d("Offer发送完成")
                    }
                    override fun onSetFailure(error: String) {
                        Timber.e("设置本地描述失败: $error")
                    }
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onCreateFailure(error: String) {
                        Timber.e("创建本地描述失败: $error")
                    }
                }, desc)
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String) {
                Timber.e("创建offer失败: $error")
            }
            override fun onSetFailure(error: String) {
                Timber.e("设置offer失败: $error")
            }
        }, mediaConstraints)
    }


    fun handleRemoteOffer(offerData: String) {
        try {
            Timber.d("Handling remote offer: $offerData")
            val offerJson = JSONObject(offerData)
            val sdp = SessionDescription(
                SessionDescription.Type.fromCanonicalForm(offerJson.getString("type")),
                offerJson.getString("sdp")
            )

            if (peerConnection == null) {
                createPeerConnection()
                startLocalVideo()
            }

            peerConnection?.setRemoteDescription(object : SdpObserver {
                override fun onSetSuccess() {
                    Timber.d("Remote offer set successfully")

                    // 创建answer时也使用正确的媒体约束
                    val mediaConstraints = MediaConstraints().apply {
                        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
                        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                    }

                    peerConnection?.createAnswer(object : SdpObserver {
                        override fun onCreateSuccess(desc: SessionDescription) {
                            peerConnection?.setLocalDescription(object : SdpObserver {
                                override fun onSetSuccess() {
                                    val answerJson = JSONObject().apply {
                                        put("type", desc.type.canonicalForm())
                                        put("sdp", desc.description)
                                    }

                                    WebSocketManager.getInstance().sendWebRTCSignal(
                                        senderId = currentUserId,
                                        receiverId = currentCounselorId,
                                        senderType = "user",
                                        type = "answer",
                                        data = answerJson.toString(),
                                        callId = currentCallId
                                    )
                                    Timber.d("Answer sent successfully")
                                }
                                override fun onSetFailure(error: String) {
                                    Timber.e("Failed to set local description for answer: $error")
                                }
                                override fun onCreateSuccess(p0: SessionDescription?) {}
                                override fun onCreateFailure(error: String) {
                                    Timber.e("Failed to create local description for answer: $error")
                                }
                            }, desc)
                        }
                        override fun onSetSuccess() {}
                        override fun onCreateFailure(error: String) {
                            Timber.e("Failed to create answer: $error")
                        }
                        override fun onSetFailure(error: String) {
                            Timber.e("Failed to set answer: $error")
                        }
                    }, mediaConstraints)
                }
                override fun onSetFailure(error: String) {
                    Timber.e("Failed to set remote description: $error")
                }
                override fun onCreateSuccess(p0: SessionDescription?) {}
                override fun onCreateFailure(error: String) {
                    Timber.e("Failed to create remote description: $error")
                }
            }, sdp)

        } catch (e: Exception) {
            Timber.e(e, "Failed to handle remote offer")
        }
    }

    fun handleRemoteAnswer(answerData: String) {
        try {
            Timber.d("Handling remote answer: $answerData")
            val answerJson = JSONObject(answerData)
            val sdp = SessionDescription(
                SessionDescription.Type.fromCanonicalForm(answerJson.getString("type")),
                answerJson.getString("sdp")
            )

            peerConnection?.setRemoteDescription(object : SdpObserver {
                override fun onSetSuccess() {
                    Timber.d("Remote answer set successfully")
                    // 连接建立后检查媒体轨道状态
                    checkMediaTracks()
                }
                override fun onSetFailure(error: String) {
                    Timber.e("Failed to set remote answer: $error")
                }
                override fun onCreateSuccess(p0: SessionDescription?) {}
                override fun onCreateFailure(error: String) {
                    Timber.e("Failed to create remote answer: $error")
                }
            }, sdp)
        } catch (e: Exception) {
            Timber.e(e, "Failed to handle remote answer")
        }
    }

    // 添加媒体轨道状态检查方法
    // 添加媒体轨道状态检查
    private fun checkMediaTracks() {
        runOnUiThread {
            peerConnection?.let { pc ->
                val receivers = pc.receivers
                val senders = pc.senders

                Timber.d("媒体轨道状态检查:")
                Timber.d("发送器数量: ${senders.size}")
                Timber.d("接收器数量: ${receivers.size}")

                receivers.forEachIndexed { index, receiver ->
                    val track = receiver.track()
                    if (track != null) {
                        Timber.d("接收器 $index: ${track.kind()} - ${track.enabled()}")
                    }
                }

                senders.forEachIndexed { index, sender ->
                    val track = sender.track()
                    if (track != null) {
                        Timber.d("发送器 $index: ${track.kind()} - ${track.enabled()}")
                    }
                }
            }
        }
    }

    fun handleRemoteIceCandidate(candidateData: String) {
        try {
            Timber.d("Handling remote ICE candidate: $candidateData")
            val candidateJson = JSONObject(candidateData)
            val iceCandidate = IceCandidate(
                candidateJson.getString("sdpMid"),
                candidateJson.getInt("sdpMLineIndex"),
                candidateJson.getString("candidate")
            )

            peerConnection?.addIceCandidate(iceCandidate)
        } catch (e: Exception) {
            Timber.e(e, "Failed to handle remote ICE candidate")
        }
    }

    fun rejectCall() {
        Timber.d("Rejecting call: $currentCallId")

        try {
            WebSocketManager.getInstance().sendWebRTCStatus(
                senderId = currentUserId,
                receiverId = currentCounselorId,
                senderType = "user",
                status = "rejected",
                callId = currentCallId
            )
            finish()
        } catch (e: Exception) {
            Timber.e(e, "Failed to reject call")
        }
    }

    fun endCall() {
        Timber.d("Ending call: $currentCallId")

        try {
            WebSocketManager.getInstance().sendWebRTCStatus(
                senderId = currentUserId,
                receiverId = currentCounselorId,
                senderType = "user",
                status = "ended",
                callId = currentCallId
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to send end call status")
        }

        cleanup()
        finish()
    }

    fun toggleVideo() {
        localVideoTrack?.setEnabled(localVideoTrack?.enabled() != true)
        Timber.d("Video toggled: ${localVideoTrack?.enabled()}")
    }

    fun toggleMic() {
        localAudioTrack?.setEnabled(localAudioTrack?.enabled() != true)
        Timber.d("Mic toggled: ${localAudioTrack?.enabled()}")
    }

    private fun cleanup() {
        peerConnection?.close()
        peerConnection = null

        videoCapturer?.stopCapture()
        videoCapturer?.dispose()
        videoCapturer = null

        localVideoView.release()
        remoteVideoView.release()

        peerConnectionFactory.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 安全地释放WebRTC资源
        try {
            // 停止本地视频捕获
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
            
            // 停止音频轨道
            try {
                localAudioTrack?.setEnabled(false)
                localAudioTrack?.dispose()
            } catch (e: Exception) {
                Timber.e(e, "Error stopping audio track")
            }
            
            // 停止视频轨道
            try {
                localVideoTrack?.setEnabled(false)
                localVideoTrack?.dispose()
            } catch (e: Exception) {
                Timber.e(e, "Error stopping video track")
            }
            
            // 关闭并释放PeerConnection
            peerConnection?.close()
            peerConnection?.dispose()
            
            // 释放工厂和EGL资源
            peerConnectionFactory.dispose()
            eglBase?.release()
            
            // 释放视频视图
            localVideoView.release()
            remoteVideoView.release()
            
            Timber.d("WebRTC resources released successfully")
        } catch (e: Exception) {
            Timber.e(e, "Error releasing WebRTC resources")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCallScreen(
    userId: Long,
    counselorId: Int,
    callId: String,
    isIncomingCall: Boolean,
    callerName: String?,
    callerAvatar: String?,
    localVideoView: SurfaceViewRenderer,
    remoteVideoView: SurfaceViewRenderer,
    onBackPress: () -> Unit,
    onAcceptCall: () -> Unit,
    onRejectCall: () -> Unit,
    onEndCall: () -> Unit,
    onToggleVideo: () -> Unit,
    onToggleMic: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? VideoCallActivity

    // 通话状态
    val callState = remember {
        mutableStateOf(
            if (isIncomingCall) CallState.RINGING
            else CallState.CONNECTING
        )
    }

    // 本地视频开关
    val isVideoEnabled = remember { mutableStateOf(true) }
    // 本地麦克风开关
    val isMicEnabled = remember { mutableStateOf(true) }

    // 通话时间计时器
    val callTimer = rememberCallTimer()

    // 监听WebRTC信令和状态
    DisposableEffect(Unit) {
        val webSocketManager = WebSocketManager.getInstance()

        try {
            // 安全地连接WebSocket，避免重复连接
            // 检查是否需要设置自定义的消息处理
            webSocketManager.connect(
                userId = userId,
                counselorId = counselorId,
                onMessageReceived = {}, // 不处理普通消息
                onError = { errorMsg ->
                    Timber.e("WebSocket error: $errorMsg")
                },
                onWebRTCSignalReceived = { signalMessage ->
                    Timber.d("Received WebRTC signal: ${signalMessage.type}")

                    when (signalMessage.type) {
                        "offer" -> {
                            if (callState.value == CallState.RINGING && signalMessage.callId == callId) {
                                activity?.handleRemoteOffer(signalMessage.data)
                                callState.value = CallState.ACTIVE
                                callTimer.start()
                            }
                        }
                        "answer" -> {
                            if (callState.value == CallState.CONNECTING && signalMessage.callId == callId) {
                                activity?.handleRemoteAnswer(signalMessage.data)
                                callState.value = CallState.ACTIVE
                                callTimer.start()
                            }
                        }
                        "ice-candidate" -> {
                            activity?.handleRemoteIceCandidate(signalMessage.data)
                        }
                    }
                },
                onWebRTCStatusReceived = { statusMessage ->
                    Timber.d("Received WebRTC status: ${statusMessage.status}")

                    when (statusMessage.status) {
                        "accepted" -> {
                            if (statusMessage.callId == callId) {
                                callState.value = CallState.ACTIVE
                                callTimer.start()
                            }
                        }
                        "rejected" -> {
                            if (statusMessage.callId == callId) {
                                callState.value = CallState.REJECTED
                                callTimer.stop()
                            }
                        }
                        "ended" -> {
                            if (statusMessage.callId == callId) {
                                callState.value = CallState.ENDED
                                callTimer.stop()
                            }
                        }
                        else -> {}
                    }
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to setup WebRTC listeners")
        }

        onDispose {
            callTimer.stop()
            
            if (callState.value == CallState.ACTIVE) {
                try {
                    webSocketManager.sendWebRTCStatus(
                        senderId = userId,
                        receiverId = counselorId,
                        senderType = "user",
                        status = "ended",
                        callId = callId
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Failed to send end call status")
                }
            }
            
            // 注意：不要在通话结束时断开WebSocket连接，让上层管理连接生命周期
        }
    }

    // 主界面布局
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 远程视频（大窗口）
        AndroidView(
            factory = { remoteVideoView },
            modifier = Modifier.fillMaxSize()
        )

        // 本地视频（小窗口）
        if (callState.value == CallState.ACTIVE) {
            AndroidView(
                factory = { localVideoView },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(120.dp)
                    .padding(16.dp)
            )
        }

        // 通话状态覆盖层
        when (callState.value) {
            CallState.RINGING -> {
                IncomingCallView(
                    callerName = callerName ?: "未知用户",
                    callerAvatar = callerAvatar,
                    onAccept = onAcceptCall,
                    onReject = onRejectCall
                )
            }
            CallState.CONNECTING -> {
                ConnectingCallView(
                    calleeName = callerName ?: "",
                    onEndCall = onEndCall
                )
            }
            CallState.ACTIVE -> {
                ActiveCallControls(
                    isVideoEnabled = isVideoEnabled.value,
                    isMicEnabled = isMicEnabled.value,
                    onToggleVideo = {
                        onToggleVideo()
                        isVideoEnabled.value = !isVideoEnabled.value
                    },
                    onToggleMic = {
                        onToggleMic()
                        isMicEnabled.value = !isMicEnabled.value
                    },
                    onEndCall = onEndCall
                )

                // 通话时间
                Text(
                    text = formatCallDuration(callTimer.duration.value),
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 50.dp)
                )
            }
            CallState.REJECTED -> {
                CallEndedView(reason = "通话被拒绝", onConfirm = onBackPress)
            }
            CallState.ENDED -> {
                CallEndedView(reason = "通话已结束", onConfirm = onBackPress)
            }
        }
    }
}

// 通话计时器助手类
class CallTimer {
    private val callDuration = mutableStateOf(0)
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    
    val duration: MutableState<Int> get() = callDuration
    
    fun start() {
        // 先清理可能存在的计时器
        stop()
        
        runnable = object : Runnable {
            override fun run() {
                callDuration.value = callDuration.value + 1
                handler.postDelayed(this, 1000)
            }
        }
        
        runnable?.let { handler.post(it) }
    }
    
    fun stop() {
        runnable?.let { 
            handler.removeCallbacks(it)
            runnable = null
        }
    }
}

// 在Composable中使用的remember函数
@Composable
fun rememberCallTimer(): CallTimer {
    val callTimer = remember { CallTimer() }
    
    DisposableEffect(Unit) {
        // 确保组件销毁时停止计时器
        onDispose {
            callTimer.stop()
        }
    }
    
    return callTimer
}

// 通话状态枚举
enum class CallState {
    RINGING,
    CONNECTING,
    ACTIVE,
    REJECTED,
    ENDED
}

// 来电界面
@Composable
fun IncomingCallView(callerName: String, callerAvatar: String?, onAccept: () -> Unit, onReject: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 头像
        if (callerAvatar != null) {
            AsyncImage(
                model = callerAvatar,
                contentDescription = "来电者头像",
                modifier = Modifier
                    .size(120.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.Gray)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 来电者名称
        Text(
            text = callerName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 来电状态
        Text(
            text = "来电中...",
            fontSize = 18.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.weight(1f))

        // 控制按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 拒绝按钮
            IconButton(
                onClick = onReject,
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Red, shape = MaterialTheme.shapes.extraLarge)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.CallMissed,
                    contentDescription = "拒绝通话",
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
            }

            // 接听按钮
            IconButton(
                onClick = onAccept,
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Green, shape = MaterialTheme.shapes.extraLarge)
            ) {
                Icon(
                    imageVector = Icons.Filled.Videocam,
                    contentDescription = "接听通话",
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
            }
        }
    }
}

// 拨出电话连接中界面
@Composable
fun ConnectingCallView(calleeName: String, onEndCall: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Videocam,
            contentDescription = "通话中",
            modifier = Modifier.size(64.dp),
            tint = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "正在连接...",
            fontSize = 24.sp,
            color = Color.White
        )

        if (calleeName.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = calleeName,
                fontSize = 18.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        IconButton(
            onClick = onEndCall,
            modifier = Modifier
                .size(72.dp)
                .background(Color.Red, shape = MaterialTheme.shapes.extraLarge)
        ) {
            Icon(
                imageVector = Icons.Filled.CallEnd,
                contentDescription = "结束通话",
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }
    }
}

// 活动通话控制界面
@Composable
fun ActiveCallControls(
    isVideoEnabled: Boolean,
    isMicEnabled: Boolean,
    onToggleVideo: () -> Unit,
    onToggleMic: () -> Unit,
    onEndCall: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = onToggleVideo,
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        if (isVideoEnabled) Color.Gray.copy(alpha = 0.7f) else Color.Red.copy(alpha = 0.7f),
                        shape = MaterialTheme.shapes.extraLarge
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Videocam,
                    contentDescription = if (isVideoEnabled) "关闭视频" else "开启视频",
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }

            IconButton(
                onClick = onToggleMic,
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        if (isMicEnabled) Color.Gray.copy(alpha = 0.7f) else Color.Red.copy(alpha = 0.7f),
                        shape = MaterialTheme.shapes.extraLarge
                    )
            ) {
                Icon(
                    imageVector = if (isMicEnabled) Icons.Filled.Mic else Icons.Filled.MicOff,
                    contentDescription = if (isMicEnabled) "静音" else "取消静音",
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }

            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Red, shape = MaterialTheme.shapes.extraLarge)
            ) {
                Icon(
                    imageVector = Icons.Filled.CallEnd,
                    contentDescription = "结束通话",
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }
        }
    }
}

// 通话结束界面
@Composable
fun CallEndedView(reason: String, onConfirm: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.CallMissed,
            contentDescription = "通话结束",
            modifier = Modifier.size(64.dp),
            tint = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = reason,
            fontSize = 24.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .padding(horizontal = 32.dp)
        ) {
            Text(
                text = "确定",
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

// 格式化通话时长
@SuppressLint("DefaultLocale")
fun formatCallDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%02d:%02d", minutes, secs)
    }
}