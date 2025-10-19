package com.example.ui.features

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Message
import com.example.network.RetrofitClient
import com.example.network.WebSocketManager
import com.example.util.DatabaseHelper
import com.example.util.IpAddressManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ChatDetailActivity : ComponentActivity() {
    companion object {
        private const val EXTRA_USER_ID = "userId"
        private const val EXTRA_COUNSELOR_ID = "counselorId"
        private const val EXTRA_COUNSELOR_NAME = "counselorName"
        private const val EXTRA_COUNSELOR_AVATAR = "counselorAvatar"
        private const val EXTRA_USER_AVATAR = "userAvatar"
        
        fun start(context: Context, userId: Long, counselor: Any) {
            val intent = Intent(context, ChatDetailActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
                try {
                    val counselorIdField = counselor.javaClass.getDeclaredField("counselorId")
                    counselorIdField.isAccessible = true
                    putExtra(EXTRA_COUNSELOR_ID, counselorIdField.getInt(counselor))
                    
                    val nameField = counselor.javaClass.getDeclaredField("realName")
                    nameField.isAccessible = true
                    putExtra(EXTRA_COUNSELOR_NAME, nameField.get(counselor) as String)
                    
                    val avatarField = counselor.javaClass.getDeclaredField("photoUrl")
                    avatarField.isAccessible = true
                    putExtra(EXTRA_COUNSELOR_AVATAR, avatarField.get(counselor) as String?)
                } catch (e: Exception) {
                    // 处理异常
                }
            }
            context.startActivity(intent)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 键盘弹出行为已在AndroidManifest.xml中通过windowSoftInputMode="stateHidden|adjustResize"配置
        
        val counselorId = intent.getIntExtra(EXTRA_COUNSELOR_ID, 0)
        val counselorName = intent.getStringExtra(EXTRA_COUNSELOR_NAME) ?: ""
        val counselorAvatar = intent.getStringExtra(EXTRA_COUNSELOR_AVATAR)
        
        // 从本地数据库获取已登录用户的ID
        val dbHelper = DatabaseHelper(this)
        val loggedInUser = dbHelper.getLoggedInUser()
        val userId = loggedInUser?.id?.toLong() ?: 0L
        val userAvatar = loggedInUser?.avatarUrl
        
        setContent {
            ChatDetailScreen(
                initialMessages = emptyList(),
                userAvatar = userAvatar,
                counselorAvatar = counselorAvatar,
                counselorName = counselorName,
                userId = userId,
                counselorId = counselorId,
                isConnected = false, // 初始状态为未连接
                onBackPress = { finish() },
                errorMessage = null,
                isLoadingHistory = false
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    initialMessages: List<Message> = emptyList(),
    userAvatar: String?,
    counselorAvatar: String?,
    counselorName: String,
    userId: Long,
    counselorId: Int,
    isConnected: Boolean,
    onBackPress: () -> Unit,
    errorMessage: String? = null,
    isLoadingHistory: Boolean = false
) {
    val messageText = remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val messages = remember { mutableStateOf(initialMessages) }
    val loadingState = remember { mutableStateOf(isLoadingHistory) }
    val errorState = remember { mutableStateOf(errorMessage) }
    val connectedState = remember { mutableStateOf(isConnected) }
    
    // 获取已登录用户的头像和ID
    val context = LocalContext.current
    val loggedInUser = remember {
        val dbHelper = DatabaseHelper(context)
        dbHelper.getLoggedInUser()
    }
    val loggedInUserId = loggedInUser?.id?.toLong() ?: userId
    val loggedInUserAvatar = loggedInUser?.avatarUrl ?: userAvatar
    
    // 组件销毁时断开WebSocket连接
    DisposableEffect(Unit) {
        onDispose {
            try {
                WebSocketManager.getInstance().disconnect()
            } catch (e: Exception) {
                // 忽略断开连接时的异常
            }
        }
    }
    
    // 建立WebSocket连接
    LaunchedEffect(loggedInUserId, counselorId) {
        if (loggedInUserId > 0 && counselorId > 0) {
            try {
                WebSocketManager.getInstance().connect(
                    userId = loggedInUserId,
                    counselorId = counselorId,
                    onMessageReceived = { chatMessage ->
                        // 处理接收到的消息
                        // 确保时间戳是ISO格式
                        val timestamp = chatMessage.timestamp.ifEmpty { 
                            // 使用UTC时区生成ISO格式时间戳，与服务器保持一致
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }.format(Date()) 
                        }
                        // 转换可能的非ISO格式时间戳
                        val formattedTimestamp = if (timestamp.contains('T')) timestamp else timestamp.replace(' ', 'T')
                        
                        val newMessage = Message(
                            id = System.currentTimeMillis(),
                            appointmentId = null,
                            senderType = chatMessage.senderType,
                            messageType = "TEXT",
                            content = chatMessage.content,
                            mediaUrl = null,
                            durationSeconds = null,
                            sentTime = formattedTimestamp,
                            readStatus = false,
                            userId = loggedInUserId,
                            counselorId = counselorId.toLong(),
                            conversationType = "PRIVATE"
                        )
                        
                        // 加强消息去重逻辑：仅基于内容和发送者类型判断，忽略时间戳差异
                        val isDuplicate = messages.value.any { existingMessage ->
                            existingMessage.content == newMessage.content &&
                            existingMessage.senderType == newMessage.senderType &&
                            // 检查消息是否是最近5秒内添加的，避免真正的重复消息
                            System.currentTimeMillis() - existingMessage.id < 5000
                        }
                        
                        // 只有不是重复消息时才添加到列表
                        if (!isDuplicate) {
                            messages.value = messages.value + listOf(newMessage)
                        }
                    },
                    onError = { errorMsg ->
                        errorState.value = errorMsg
                        connectedState.value = false
                    }
                )
                connectedState.value = true
            } catch (e: Exception) {
                errorState.value = "WebSocket连接失败：${e.message}"
                connectedState.value = false
            }
        }
    }
    
    // 加载历史消息
    LaunchedEffect(Unit) {
        if (messages.value.isEmpty()) {
            loadingState.value = true
            errorState.value = null
            try {
                val historyMessages = withContext(Dispatchers.IO) {
                    val apiService = RetrofitClient.apiService
                    return@withContext apiService.getConversationMessages(
                        userId = loggedInUserId,
                        counselorId = counselorId.toLong()
                    )
                }
                // 按时间正序排列，最早的消息在顶部
                messages.value = historyMessages.sortedBy { it.sentTime }
            } catch (e: Exception) {
                errorState.value = "获取聊天记录失败：${e.message}"
            } finally {
                loadingState.value = false
            }
        }
    }
    
    LaunchedEffect(messages.value.size) {
        if (messages.value.isNotEmpty()) {
            listState.animateScrollToItem(messages.value.size - 1)
        }
    }

    fun formatTimestamp(timestamp: String): String {
        try {
            // 对于ISO格式时间戳 - 先解析为Date对象再格式化为北京时间（UTC+8）
            if (timestamp.contains('T')) {
                // 创建UTC时区的解析格式
                val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }

                // 创建北京时间（UTC+8）的显示格式
                val beijingFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("GMT+8") // 明确设置为北京时间（UTC+8）
                }

                try {
                    // 尝试解析完整的时间格式（包含毫秒）
                    val date = if (timestamp.contains('.')) {
                        // 处理带毫秒的时间戳
                        val millisFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                        millisFormat.parse(timestamp)
                    } else {
                        utcFormat.parse(timestamp)
                    }

                    // 将UTC时间转换为北京时间显示
                    return if (date != null) {
                        beijingFormat.format(date)
                    } else {
                        // 解析失败，尝试直接截取
                        timestamp.substringAfter('T').substring(0, 5)
                    }
                } catch (e: ParseException) {
                    // 如果解析失败，回退到直接截取的方法
                    return timestamp.substringAfter('T').substring(0, 5)
                }
            } else if (timestamp.contains(' ')) {
                // 对于常规格式时间戳 (2025-10-19 12:41:58) - 直接提取小时分钟
                val timePart = timestamp.substringAfter(' ')
                val hourMinute = timePart.substring(0, 5) // 提取HH:mm部分
                return hourMinute
            }

            // 如果格式不匹配，返回默认时间或空字符串
            return "--:--"
        } catch (e: Exception) {
            // 如果解析失败，返回默认格式
            return "--:--"
        }
    }
    
    fun sendMessage(text: String) {
        if (text.isNotBlank() && loggedInUserId > 0 && counselorId > 0) {
            // 创建新消息对象
            val newMessage = Message(
                id = System.currentTimeMillis(), // 使用时间戳作为临时ID
                appointmentId = null,
                senderType = "USER",
                messageType = "TEXT",
                content = text,
                mediaUrl = null,
                durationSeconds = null,
                // 使用UTC时区生成ISO格式时间戳，与服务器保持一致
                sentTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(Date()),
                readStatus = false,
                userId = loggedInUserId,
                counselorId = counselorId.toLong(),
                conversationType = "PRIVATE"
            )
            
            // 立即更新UI，添加新消息到列表底部前进行去重检查
            val isDuplicate = messages.value.any { existingMessage ->
                existingMessage.content == newMessage.content &&
                existingMessage.senderType == newMessage.senderType &&
                System.currentTimeMillis() - existingMessage.id < 5000
            }
            
            if (!isDuplicate) {
                messages.value = messages.value + listOf(newMessage)
            }
            
            // 发送消息到服务器
            try {
                WebSocketManager.getInstance().sendMessage(
                    senderId = loggedInUserId,
                    receiverId = counselorId,
                    senderType = "USER",
                    content = text
                )
            } catch (e: Exception) {
                errorState.value = "发送消息失败：${e.message}"
            }
        }
    }
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (counselorAvatar != null) {
                            val processedAvatarUrl = IpAddressManager.processImageUrl(counselorAvatar)
                            AsyncImage(
                                model = processedAvatarUrl,
                                contentDescription = "咨询师头像",
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(18.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Text(
                            text = counselorName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* 更多操作 */ }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "更多",
                            tint = Color.Black
                        )
                    }
                },
                modifier = Modifier.background(Color.White)
            )
        },
        content = { paddingValues ->
            // 使用Column作为根布局，确保布局结构正确
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(paddingValues)
            ) {
                if (errorState.value != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEF5350))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = errorState.value!!,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    state = listState,
                    // 调整底部内边距，确保当键盘弹出时，最后一条消息不会被遮挡
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 80.dp)
                ) {
                    if (loadingState.value && messages.value.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "加载历史消息中...",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else if (messages.value.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "暂无聊天记录",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        items(messages.value) { message ->
                            val isUser = message.senderType == "USER"
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                                verticalAlignment = Alignment.Top
                            ) {
                                if (!isUser) {
                                    if (counselorAvatar != null) {
                                        val processedAvatarUrl = IpAddressManager.processImageUrl(counselorAvatar)
                                        AsyncImage(
                                            model = processedAvatarUrl,
                                            contentDescription = "咨询师头像",
                                            modifier = Modifier
                                                .width(36.dp)
                                                .height(36.dp)
                                                .clip(RoundedCornerShape(18.dp))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                }
                                
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (isUser) Color(0xFF5A67D8) else Color.White,
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = message.content,
                                            color = if (isUser) Color.White else Color.Black,
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp
                                        )
                                    }
                                    
                                    if (message.sentTime.isNotEmpty()) {
                                        Text(
                                            text = formatTimestamp(message.sentTime),
                                            color = Color.Gray,
                                            fontSize = 12.sp,
                                            modifier = Modifier
                                                .padding(top = 4.dp)
                                                .align(if (isUser) Alignment.End else Alignment.Start)
                                        )
                                    }
                                }
                                
                                if (isUser) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // 使用本地数据库中已登录用户的头像
                                    if (loggedInUserAvatar != null) {
                                        val processedUserAvatarUrl = IpAddressManager.processImageUrl(loggedInUserAvatar)
                                        AsyncImage(
                                            model = processedUserAvatarUrl,
                                            contentDescription = "用户头像",
                                            modifier = Modifier
                                                .width(36.dp)
                                                .height(36.dp)
                                                .clip(RoundedCornerShape(18.dp))
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .width(36.dp)
                                                .height(36.dp)
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(Color(0xFFE0E0E0))
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
                
                // 输入框部分，使用imePadding确保键盘弹出时输入框不会被遮挡
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(12.dp)
                        .imePadding(), // 添加imePadding以确保输入框在键盘弹出时可见
                    verticalAlignment = Alignment.Bottom
                ) {
                    TextField(
                        value = messageText.value,
                        onValueChange = { messageText.value = it },
                        placeholder = { Text(text = "输入消息...", color = Color.Gray) },
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentHeight(),
                        textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = false,
                        maxLines = 5
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            sendMessage(messageText.value)
                            messageText.value = ""
                        },
                        enabled = messageText.value.isNotBlank() && connectedState.value
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "发送",
                            tint = if (messageText.value.isNotBlank() && isConnected) Color(0xFF5A67D8) else Color.Gray
                        )
                    }
                }
            }
        }
    )
}