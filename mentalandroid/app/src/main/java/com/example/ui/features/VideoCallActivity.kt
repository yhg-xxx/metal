package com.example.ui.features

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import android.Manifest
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
        const val PERMISSION_REQUEST_CODE = 1001

        fun start(context: Context, userId: Long, counselorId: Int, callId: String,
                  isIncomingCall: Boolean = false, callerName: String? = null,
                  callerAvatar: String? = null) {
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
    private var eglBase: EglBase? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    // SurfaceViewRenderers
    private lateinit var localVideoView: SurfaceViewRenderer
    private lateinit var remoteVideoView: SurfaceViewRenderer

    // 状态变量
    private var currentCallId: String = ""
    private var currentUserId: Long = 0L
    private var currentCounselorId: Int = 0
    private var isIncomingCall: Boolean = false
    private var isCallActive: Boolean = false
    private var isCallEnded: Boolean = false

    // 权限相关
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    // 权限请求启动器已替换为直接调用requestPermissions方法

    // WebSocket监听器
    private val webRTCSignalListener = { signalMessage: WebSocketManager.WebRTCSignalMessage ->
        runOnUiThread {
            handleWebRTCSignal(signalMessage)
        }
    }

    private val webRTCStatusListener = { statusMessage: WebSocketManager.WebRTCStatusMessage ->
        runOnUiThread {
            handleWebRTCStatus(statusMessage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化参数
        currentUserId = intent.getLongExtra(EXTRA_USER_ID, 0L)
        currentCounselorId = intent.getIntExtra(EXTRA_COUNSELOR_ID, 0)
        currentCallId = intent.getStringExtra(EXTRA_CALL_ID) ?: ""
        isIncomingCall = intent.getBooleanExtra(EXTRA_INCOMING_CALL, false)

        Timber.i("VideoCallActivity启动: userId=$currentUserId, counselorId=$currentCounselorId, callId=$currentCallId")

        // 注册WebSocket监听器
        WebSocketManager.getInstance().addWebRTCSignalListener(webRTCSignalListener)
        WebSocketManager.getInstance().addWebRTCStatusListener(webRTCStatusListener)

        // 请求必要的权限
        requestPermissionsIfNeeded()

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
                    onBackPress = { finish() },
                    onAcceptCall = { acceptCall() },
                    onRejectCall = { rejectCall() },
                    onEndCall = { endCall() },
                    onToggleVideo = { toggleVideo() },
                    onToggleMic = { toggleMic() }
                )
            }
        }

        // 如果是去电（主动呼叫），延迟自动接受通话
        if (!isIncomingCall) {
            Handler(Looper.getMainLooper()).postDelayed({
                acceptCall()
            }, 1000)
        }
    }

    // 处理收到的WebRTC信令消息
    private fun handleWebRTCSignal(signalMessage: WebSocketManager.WebRTCSignalMessage) {
        // 确保callId匹配
        if (signalMessage.callId != currentCallId) {
            Timber.w("CallId不匹配, 期望: $currentCallId, 实际: ${signalMessage.callId}")
            return
        }

        when (signalMessage.type) {
            "offer" -> handleRemoteOffer(signalMessage.data)
            "answer" -> handleRemoteAnswer(signalMessage.data)
            "ice-candidate" -> handleRemoteIceCandidate(signalMessage.data)
        }
    }

    private fun handleWebRTCStatus(statusMessage: WebSocketManager.WebRTCStatusMessage) {
        // 确保callId匹配
        if (statusMessage.callId != currentCallId) {
            Timber.w("CallId不匹配, 期望: $currentCallId, 实际: ${statusMessage.callId}")
            return
        }

        when (statusMessage.status) {
            "accepted" -> isCallActive = true
            "rejected" -> {
                runOnUiThread {
                    Toast.makeText(this, "通话被对方拒绝", Toast.LENGTH_SHORT).show()
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    cleanup()
                    finish()
                }, 2000)
            }
            "ended" -> {
                runOnUiThread {
                    Toast.makeText(this, "通话已结束", Toast.LENGTH_SHORT).show()
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    cleanup()
                    finish()
                }, 2000)
            }
            "missed" -> {
                runOnUiThread {
                    Toast.makeText(this, "通话未接听", Toast.LENGTH_SHORT).show()
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    cleanup()
                    finish()
                }, 2000)
            }
        }
    }

    private fun initializeWebRTC() {
        if (::peerConnectionFactory.isInitialized) {
            return
        }

        try {
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

            // 初始化EGL
            eglBase = EglBase.create()

            // 初始化视频视图
            localVideoView = SurfaceViewRenderer(this).apply {
                init(eglBase?.eglBaseContext, null)
                setMirror(true)
                setEnableHardwareScaler(true)
                setZOrderMediaOverlay(true)
            }

            remoteVideoView = SurfaceViewRenderer(this).apply {
                init(eglBase?.eglBaseContext, null)
                setEnableHardwareScaler(true)
            }

        } catch (e: Exception) {
            Timber.e(e, "WebRTC初始化失败")
            runOnUiThread {
                Toast.makeText(this, "视频功能初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createPeerConnection() {
        if (peerConnection != null) {
            Timber.d("PeerConnection已经存在")
            return
        }

        try {
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
            }

            peerConnection = peerConnectionFactory.createPeerConnection(
                rtcConfig,
                object : PeerConnection.Observer {
                    override fun onIceCandidate(candidate: IceCandidate) {
                        sendIceCandidate(candidate)
                    }

                    override fun onAddStream(stream: MediaStream) {
                        runOnUiThread {
                            stream.videoTracks.firstOrNull()?.let { remoteVideoTrack ->
                                remoteVideoTrack.addSink(remoteVideoView)
                                remoteVideoTrack.setEnabled(true)
                            }
                        }
                    }

                    override fun onAddTrack(receiver: RtpReceiver, streams: Array<out MediaStream>) {
                        runOnUiThread {
                            val track = receiver.track()
                            if (track is VideoTrack) {
                                track.addSink(remoteVideoView)
                                track.setEnabled(true)
                            }
                        }
                    }

                    override fun onTrack(transceiver: RtpTransceiver) {
                        runOnUiThread {
                            if (transceiver.mediaType == MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO) {
                                val track = transceiver.receiver.track()
                                if (track is VideoTrack) {
                                    track.addSink(remoteVideoView)
                                    track.setEnabled(true)
                                }
                            }
                        }
                    }

                    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
                        runOnUiThread {
                            when (newState) {
                                PeerConnection.PeerConnectionState.CONNECTED -> {
                                    isCallActive = true
                                    Toast.makeText(this@VideoCallActivity, "视频通话连接已建立", Toast.LENGTH_SHORT).show()
                                }
                                PeerConnection.PeerConnectionState.FAILED -> {
                                    Toast.makeText(this@VideoCallActivity, "视频连接失败", Toast.LENGTH_SHORT).show()
                                }
                                PeerConnection.PeerConnectionState.DISCONNECTED -> {
                                    Toast.makeText(this@VideoCallActivity, "视频连接断开", Toast.LENGTH_SHORT).show()
                                }
                                PeerConnection.PeerConnectionState.CLOSED -> {
                                    Toast.makeText(this@VideoCallActivity, "视频连接已关闭", Toast.LENGTH_SHORT).show()
                                }
                                else -> {}
                            }
                        }
                    }

                    override fun onSignalingChange(state: PeerConnection.SignalingState?) {}

                    override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
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



        } catch (e: Exception) {
            Timber.e(e, "创建PeerConnection失败")
            runOnUiThread {
                Toast.makeText(this, "创建连接失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startLocalVideo() {
        try {
            // 停止当前视频流
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
            videoCapturer = null
            localVideoTrack?.dispose()
            localVideoTrack = null
            localAudioTrack?.dispose()
            localAudioTrack = null

            // 创建新的视频源
            val videoSource = peerConnectionFactory.createVideoSource(false)

            // 创建新的摄像头捕获器
            videoCapturer = createCameraCapturer()
            if (videoCapturer != null && surfaceTextureHelper != null) {
                videoCapturer?.initialize(surfaceTextureHelper, applicationContext, videoSource.capturerObserver)
                try {
                    videoCapturer?.startCapture(1280, 720, 30)
                } catch (e: Exception) {
                    Timber.e(e, "启动相机捕获失败，尝试降低分辨率")
                    // 尝试降低分辨率
                    videoCapturer?.startCapture(640, 480, 30)
                }
            }

            // 创建新的视频轨道
            localVideoTrack = peerConnectionFactory.createVideoTrack("local_video", videoSource)
            localVideoTrack?.addSink(localVideoView)
            localVideoTrack?.setEnabled(true)

            // 创建新的音频源和轨道
            val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
            localAudioTrack = peerConnectionFactory.createAudioTrack("local_audio", audioSource)
            localAudioTrack?.setEnabled(true)

            // 添加轨道到PeerConnection
            localVideoTrack?.let { videoTrack ->
                peerConnection?.addTrack(videoTrack, listOf("local_stream"))
            }

            localAudioTrack?.let { audioTrack ->
                peerConnection?.addTrack(audioTrack, listOf("local_stream"))
            }

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
            Timber.e(e, "发送ICE候选失败")
        }
    }

    private fun requestPermissionsIfNeeded() {
        if (!hasRequiredPermissions()) {
            requestPermissions(requiredPermissions, PERMISSION_REQUEST_CODE)
        } else {
            initializeCameraIfReady()
        }
    }

    private fun initializeCameraIfReady() {
        // 在有需要时才初始化摄像头
        // 这里不立即启动，等待用户接受通话或主动发起通话时再启动
    }

    private fun hasRequiredPermissions(): Boolean {
        return requiredPermissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
    }

    fun acceptCall() {
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

            // 启动本地视频
            startLocalVideo()

            // 发送接受通话状态
            WebSocketManager.getInstance().sendWebRTCStatus(
                senderId = currentUserId,
                receiverId = currentCounselorId,
                senderType = "user",
                status = "accepted",
                callId = currentCallId
            )

            // 更新UI状态
            runOnUiThread {
                Toast.makeText(this, "正在建立视频连接...", Toast.LENGTH_SHORT).show()
            }

            isCallActive = true

        } catch (e: Exception) {
            Timber.e(e, "接受通话失败")
            runOnUiThread {
                Toast.makeText(this, "接受通话失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // createAndSendOffer方法已被合并到acceptCall方法中

    fun handleRemoteOffer(offerData: String) {
        try {
            // 在去电场景下，本地已发送offer，忽略远程offer以避免状态冲突
            if (!isIncomingCall) {
                Timber.w("去电场景下忽略远程offer，避免状态冲突")
                return
            }
            
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
                    // 只在调试时保留，生产环境可以移除
                    // Timber.d("Remote offer设置成功")

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
                                    // 只在调试时保留，生产环境可以移除
                                    // Timber.d("Answer发送成功")
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
                            Timber.e("创建answer失败: $error")
                        }
                        override fun onSetFailure(error: String) {
                            Timber.e("设置answer失败: $error")
                        }
                    }, mediaConstraints)
                }
                override fun onSetFailure(error: String) {
                    Timber.e("设置远程描述失败: $error")
                }
                override fun onCreateSuccess(p0: SessionDescription?) {}
                override fun onCreateFailure(error: String) {
                    Timber.e("创建远程描述失败: $error")
                }
            }, sdp)

        } catch (e: Exception) {
            Timber.e(e, "处理远程offer失败")
        }
    }

    fun handleRemoteAnswer(answerData: String) {
        try {
            // Timber.d("处理远程answer: $answerData")
            val answerJson = JSONObject(answerData)
            val sdp = SessionDescription(
                SessionDescription.Type.fromCanonicalForm(answerJson.getString("type")),
                answerJson.getString("sdp")
            )

            peerConnection?.setRemoteDescription(object : SdpObserver {
                override fun onSetSuccess() {
                    // 只在调试时保留，生产环境可以移除
                    // Timber.d("Remote answer设置成功")
                }
                override fun onSetFailure(error: String) {
                    Timber.e("设置远程answer失败: $error")
                }
                override fun onCreateSuccess(p0: SessionDescription?) {}
                override fun onCreateFailure(error: String) {
                    Timber.e("创建远程answer失败: $error")
                }
            }, sdp)
        } catch (e: Exception) {
            Timber.e(e, "处理远程answer失败")
        }
    }

    fun handleRemoteIceCandidate(candidateData: String) {
        try {
            // Timber.d("处理远程ICE candidate: $candidateData")
            val candidateJson = JSONObject(candidateData)
            val iceCandidate = IceCandidate(
                candidateJson.getString("sdpMid"),
                candidateJson.getInt("sdpMLineIndex"),
                candidateJson.getString("candidate")
            )

            peerConnection?.addIceCandidate(iceCandidate)
        } catch (e: Exception) {
            Timber.e(e, "处理远程ICE candidate失败")
        }
    }

    fun rejectCall() {
        // Timber.d("拒绝通话: $currentCallId")

        try {
            WebSocketManager.getInstance().sendWebRTCStatus(
                senderId = currentUserId,
                receiverId = currentCounselorId,
                senderType = "user",
                status = "rejected",
                callId = currentCallId
            )
        } catch (e: Exception) {
            Timber.e(e, "发送拒绝通话状态失败")
        }

        cleanup()
        finish()
    }

    fun endCall() {
        if (isCallEnded) return

        // Timber.d("结束通话: $currentCallId")
        isCallEnded = true

        try {
            if (isCallActive) {
                WebSocketManager.getInstance().sendWebRTCStatus(
                    senderId = currentUserId,
                    receiverId = currentCounselorId,
                    senderType = "user",
                    status = "ended",
                    callId = currentCallId
                )
            } else {
                WebSocketManager.getInstance().sendWebRTCStatus(
                    senderId = currentUserId,
                    receiverId = currentCounselorId,
                    senderType = "user",
                    status = "missed",
                    callId = currentCallId
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "发送结束通话状态失败")
        }

        cleanup()
        finish()
    }

    fun toggleVideo() {
        localVideoTrack?.let {
            val newState = !it.enabled()
            it.setEnabled(newState)
            // Timber.d("视频状态切换: $newState")
        }
    }

    fun toggleMic() {
        localAudioTrack?.let {
            val newState = !it.enabled()
            it.setEnabled(newState)
            // Timber.d("麦克风状态切换: $newState")
        }
    }

    private fun cleanup() {
        // Timber.d("开始清理WebRTC资源")

        try {
            // 停止视频捕获
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
            videoCapturer = null

            // 停止SurfaceTextureHelper
            surfaceTextureHelper?.stopListening()
            surfaceTextureHelper = null

            // 释放视频轨道
            localVideoTrack?.dispose()
            localVideoTrack = null

            // 释放音频轨道
            localAudioTrack?.dispose()
            localAudioTrack = null

            // 关闭PeerConnection
            peerConnection?.close()
            peerConnection?.dispose()
            peerConnection = null

            // 释放视频视图
            try {
                localVideoView.release()
                remoteVideoView.release()
            } catch (e: Exception) {
                Timber.e(e, "释放视频视图失败")
            }

            // 释放EGL
            eglBase?.release()
            eglBase = null

            // 移除WebSocket监听器
            WebSocketManager.getInstance().removeWebRTCSignalListener(webRTCSignalListener)
            WebSocketManager.getInstance().removeWebRTCStatusListener(webRTCStatusListener)

            // Timber.d("WebRTC资源清理完成")

        } catch (e: Exception) {
            Timber.e(e, "清理WebRTC资源时出错")
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // 如果通话尚未结束，自动结束
        if (!isCallEnded) {
            endCall()
        } else {
            cleanup()
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
    onBackPress: () -> Unit,
    onAcceptCall: () -> Unit,
    onRejectCall: () -> Unit,
    onEndCall: () -> Unit,
    onToggleVideo: () -> Unit,
    onToggleMic: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 通话状态
    val callState = remember { mutableStateOf(
        if (isIncomingCall) CallState.RINGING
        else CallState.CONNECTING
    ) }

    // 本地视频开关
    val isVideoEnabled = remember { mutableStateOf(true) }
    // 本地麦克风开关
    val isMicEnabled = remember { mutableStateOf(true) }
    // 通话时间计时器
    val callTimer = rememberCallTimer()

    // 初始化WebRTC视图
    val localVideoView = remember {
        SurfaceViewRenderer(context).apply {
            // 视图会在Activity中初始化
        }
    }

    val remoteVideoView = remember {
        SurfaceViewRenderer(context).apply {
            // 视图会在Activity中初始化
        }
    }

    // 监听Activity生命周期
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // Activity恢复时重新连接
                    Timber.d("Activity恢复，重新检查WebRTC连接")
                }
                Lifecycle.Event.ON_PAUSE -> {
                    // Activity暂停时暂停视频
                    Timber.d("Activity暂停")
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            callTimer.stop()
        }
    }

    // 监听WebSocket状态
    DisposableEffect(Unit) {
        val webSocketManager = WebSocketManager.getInstance()

        // 确保WebSocket已连接
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            delay(500) // 等待Activity初始化完成

            try {
                // 检查WebSocket连接状态
                if (!webSocketManager.isConnected()) {
                    Timber.d("WebSocket未连接，尝试重新连接")
                    webSocketManager.reconnect()
                }

                Timber.d("WebSocket连接状态: ${webSocketManager.isConnected()}")
            } catch (e: Exception) {
                Timber.e(e, "检查WebSocket连接失败")
            }
        }

        onDispose {
            scope.cancel()
            callTimer.stop()
            Timber.d("VideoCallScreen销毁")
        }
    }

    // 主界面布局
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 远程视频（大窗口）
        if (callState.value == CallState.ACTIVE) {
            AndroidView(
                factory = { remoteVideoView },
                modifier = Modifier.fillMaxSize()
            )
        }

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
                    onAccept = {
                        callState.value = CallState.CONNECTING
                        onAcceptCall()
                        callTimer.start()
                    },
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
    private val callDuration = mutableIntStateOf(0)
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    val duration: MutableState<Int> get() = callDuration

    fun start() {
        // 先清理可能存在的计时器
        stop()

        runnable = object : Runnable {
            override fun run() {
                callDuration.intValue = callDuration.intValue + 1
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
        callDuration.intValue = 0
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
            imageVector = Icons.AutoMirrored.Filled.CallMissed,
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