package com.example.network

import android.util.Base64
import com.example.util.IpAddressManager
import okhttp3.*
import org.json.JSONObject
import timber.log.Timber
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

class WebSocketManager private constructor() {
    companion object {
        private const val TAG = "WebSocketManager"
        private var instance: WebSocketManager? = null

        fun getInstance(): WebSocketManager {
            if (instance == null) {
                synchronized(WebSocketManager::class) {
                    if (instance == null) {
                        instance = WebSocketManager()
                    }
                }
            }
            return instance!!
        }
    }

    data class ChatMessage(
        val senderId: Long,
        val receiverId: Int,
        val senderType: String,
        val content: String,
        val timestamp: String = ""
    )

    private var webSocket: WebSocket? = null
    private var userId: Long = 0L
    private var counselorId: Int = 0
    private var messageListener: ((ChatMessage) -> Unit)? = null
    private var errorListener: ((String) -> Unit)? = null

    fun connect(
        userId: Long,
        counselorId: Int,
        onMessageReceived: (ChatMessage) -> Unit,
        onError: (String) -> Unit
    ) {
        Timber.tag(TAG).d("Attempting to connect to WebSocket for user: $userId, counselor: $counselorId")

        try {
            this.userId = userId
            this.counselorId = counselorId
            messageListener = onMessageReceived
            errorListener = onError

            val baseUrl = IpAddressManager.BASE_URL
            Timber.tag(TAG).d("Base URL: $baseUrl")

            val wsUrl = if (baseUrl.startsWith("https")) {
                baseUrl.replace("https", "wss") + "/ws-native"
            } else {
                baseUrl.replace("http", "ws") + "/ws-native"
            }

            Timber.tag(TAG).d("WebSocket URL: $wsUrl")

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()

            val request = Request.Builder()
                .url(wsUrl)
                .build()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Timber.tag(TAG).d("WebSocket connection established")
                    Timber.tag(TAG).d("Response: ${response.code} - ${response.message}")

                    sendStompConnectFrame()
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    Timber.tag(TAG).d("Received raw message: ${text.replace(Char(0), '␀').take(200)}...")
                    try {
                        if (text.startsWith("CONNECTED")) {
                            Timber.tag(TAG).d("STOMP protocol connected, subscribing to topics")
                            subscribeToTopics()
                            return
                        }

                        if (text.startsWith("ERROR")) {
                            val errorMessage = extractErrorMessage(text)
                            Timber.tag(TAG).e("STOMP protocol error: $errorMessage")
                            errorListener?.invoke("STOMP错误: $errorMessage")
                            return
                        }

                        val message = parseMessage(text)
                        messageListener?.invoke(message)
                    } catch (e: IllegalArgumentException) {
                        if (e.message?.contains("CONNECTED") == true) {
                            // 正常情况，忽略
                        } else if (e.message?.contains("STOMP Error") == true) {
                            // 已经在前面处理了
                        } else {
                            Timber.tag(TAG).e(e, "Failed to process received message")
                        }
                    } catch (e: Exception) {
                        Timber.tag(TAG).e(e, "Failed to process received message")
                        errorListener?.invoke("处理消息失败: ${e.message}")
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Timber.tag(TAG).d("WebSocket connection closed: $code - $reason")
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Timber.tag(TAG).e(t, "WebSocket connection failed")
                    var errorMsg = "WebSocket连接失败: ${t.message}"
                    if (response != null) {
                        errorMsg += " (HTTP ${response.code})"
                    }
                    errorListener?.invoke(errorMsg)
                }
            })
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to initialize WebSocket")
            errorListener?.invoke("WebSocket连接异常: ${e.message}")
        }
    }

    private fun sendStompConnectFrame() {
        val connectFrame = StringBuilder()
            .append("CONNECT\n")
            .append("accept-version:1.1,1.2\n")
            .append("heart-beat:10000,10000\n")
            .append("\n")
            .append('\u0000')
            .toString()

        webSocket?.send(connectFrame)
        Timber.tag(TAG).d("Sent STOMP CONNECT frame")
    }

    private fun subscribeToTopics() {
        val userMessagesSubId = "sub-user-${userId}-messages"
        val counselorMessagesSubId = "sub-counselor-${counselorId}-messages"
        val userErrorsSubId = "sub-user-${userId}-errors"
        val counselorErrorsSubId = "sub-counselor-${counselorId}-errors"

        sendStompSubscribeFrame("/queue/messages/user/$userId", userMessagesSubId)
        sendStompSubscribeFrame("/queue/messages/counselor/$counselorId", counselorMessagesSubId)
        sendStompSubscribeFrame("/queue/errors/user/$userId", userErrorsSubId)
        sendStompSubscribeFrame("/queue/errors/counselor/$counselorId", counselorErrorsSubId)
    }

    private fun sendStompSubscribeFrame(destination: String, subscriptionId: String) {
        val frame = StringBuilder()
            .append("SUBSCRIBE\n")
            .append("id:$subscriptionId\n")
            .append("destination:$destination\n")
            .append("\n")
            .append('\u0000')
            .toString()

        webSocket?.send(frame)
        Timber.tag(TAG).d("Sent STOMP SUBSCRIBE frame: $destination with id: $subscriptionId")
    }

    private fun sendStompFrame(command: String, destination: String, body: String = "") {
        val frame = StringBuilder()
            .append("$command\n")
            .append("destination:$destination\n")

        if (body.isNotEmpty()) {
            frame.append("content-type:application/json\n")
        }

        frame.append("\n")
            .append(body)
            .append('\u0000')

        val frameString = frame.toString()
        webSocket?.send(frameString)
        Timber.tag(TAG).d("Sent STOMP frame: $command to $destination, length: ${frameString.length}")
    }

    fun sendMessage(
        senderId: Long,
        receiverId: Int,
        senderType: String,
        content: String
    ) {
        try {
            val messageJson = JSONObject()
            messageJson.put("senderId", senderId)
            messageJson.put("receiverId", receiverId)
            messageJson.put("senderType", senderType)
            messageJson.put("content", content)

            val jsonString = messageJson.toString()
            sendStompFrame("SEND", "/app/chat.private", jsonString)
            Timber.tag(TAG).d("Message sent: $jsonString")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to send message")
        }
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "Normal closure")
            webSocket = null
            Timber.tag(TAG).d("WebSocket disconnected")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to disconnect")
        }
    }

    private fun parseMessage(text: String): ChatMessage {
        Timber.tag(TAG).d("Raw message received: ${text.replace(Char(0), '␀')}")

        if (text.startsWith("MESSAGE")) {
            val lines = text.split("\n")
            var bodyStartIndex = -1

            for (i in lines.indices) {
                if (lines[i].isEmpty()) {
                    bodyStartIndex = i + 1
                    break
                }
            }

            if (bodyStartIndex != -1 && bodyStartIndex < lines.size) {
                val messageBody = lines.subList(bodyStartIndex, lines.size)
                    .joinToString("\n")
                    .trim()
                    .removeSuffix("\u0000")

                if (messageBody.isNotEmpty()) {
                    try {
                        val json = JSONObject(messageBody)
                        return ChatMessage(
                            senderId = json.getLong("senderId"),
                            receiverId = json.getInt("receiverId"),
                            senderType = json.getString("senderType"),
                            content = json.getString("content"),
                            timestamp = json.optString("timestamp", "")
                        )
                    } catch (e: Exception) {
                        Timber.tag(TAG).e(e, "Failed to parse message body: $messageBody")
                        throw IllegalArgumentException("Invalid JSON in message body")
                    }
                }
            }
        }

        throw IllegalArgumentException("Unsupported STOMP message: ${text.take(100)}")
    }

    private fun extractErrorMessage(errorFrame: String): String {
        return try {
            val lines = errorFrame.split("\n")
            for (line in lines) {
                if (line.startsWith("message:")) {
                    return line.substringAfter("message:")
                }
            }
            "Unknown STOMP error"
        } catch (e: Exception) {
            "Failed to parse error message"
        }
    }

    private fun generateWebSocketKey(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}