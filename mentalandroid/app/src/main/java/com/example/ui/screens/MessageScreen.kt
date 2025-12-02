package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Counselor
import com.example.model.Message
import com.example.network.RetrofitClient
import com.example.ui.features.ChatDetailActivity
import com.example.ui.features.IconDisplayActivity
import com.example.ui.theme.MentalTheme
import com.example.util.DatabaseHelper
import com.example.util.IpAddressManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 消息屏幕组件
 * 包含消息列表和会话详情的基本UI结构
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    var counselors by remember { mutableStateOf<List<Counselor>?>(null) }
    var latestMessages by remember { mutableStateOf<List<Message>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // 获取匹配的咨询师列表和最新消息
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            // 获取当前登录用户
            val loggedInUser = dbHelper.getLoggedInUser()
            if (loggedInUser != null && loggedInUser.id > 0) {
                withContext(Dispatchers.IO) {
                    try {
                        // 调用API获取用户对话过的咨询师
                        val counselorsResponse = RetrofitClient.apiService.getUserConversatedCounselors(loggedInUser.id.toLong())
                        if (counselorsResponse.code == 200 && counselorsResponse.data != null) {
                            counselors = counselorsResponse.data
                            Timber.d("成功获取咨询师列表，共 ${counselors?.size ?: 0} 条数据")
                        } else {
                            val errorMessage = counselorsResponse.message
                            error = "获取咨询师列表失败: $errorMessage"
                            Timber.e("获取咨询师列表失败: $errorMessage")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "获取咨询师列表时发生异常")
                        // 即使获取咨询师列表失败，也继续获取消息数据
                    }
                    
                    try {
                        // 调用API获取最新消息，注意：现在直接返回List<Message>而不是ApiResponse包装
                        val messagesData = RetrofitClient.apiService.getUserLatestMessagesWithCounselors(loggedInUser.id.toLong())
                        latestMessages = messagesData
                        Timber.d("成功获取最新消息，共 ${latestMessages?.size ?: 0} 条数据")
                    } catch (e: Exception) {
                        Timber.e(e, "获取最新消息时发生异常")
                    }
                }
            } else {
                error = "用户未登录"
                Timber.w("用户未登录，无法获取消息")
            }
        } catch (e: Exception) {
            error = "网络请求失败: ${e.message}"
            Timber.e(e, "消息页面数据加载发生严重错误")
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "消息",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { /* 新增消息按钮点击事件 */ }) {
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = "新增消息",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { 
                        // 跳转到图标展示页面
                        navigateToIconDisplay(context)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "查看图标",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            // 加载状态
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } 
            // 错误状态
            else if (error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = error ?: "加载失败", color = Color.Red)
                }
            }
            // 空状态
            else if (counselors.isNullOrEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "暂无消息", color = Color.Gray)
                }
            }
            // 消息列表
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
                ) {
                    items(counselors!!) {
                        MessageListItem(counselor = it, latestMessages = latestMessages)
                    }
                }
            }
        }
    }
}

/**
 * 格式化消息时间显示，增加时区处理和安全性检查
 */
private fun formatMessageTime(timeString: String): String {
    return try {
        if (timeString.isBlank()) return ""
        
        // 设置东八区时区并使用更安全的时间处理方式
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
        
        val date = dateFormat.parse(timeString)
        val now = Date()
        
        if (date != null) {
            val diffInMillis = now.time - date.time
            val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)
            
            // 创建专门用于显示的格式化器
            val displaySdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            displaySdf.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
            
            when {
                diffInDays < 0 -> {
                    // 未来时间（理论上不应该发生），返回"未知时间"
                    "未知时间"
                }
                diffInDays == 0L -> {
                    // 今天，显示时间
                    displaySdf.format(date)
                }
                diffInDays == 1L -> {
                    // 昨天，增加显示时间
                    "昨天 ${displaySdf.format(date)}"
                }
                diffInDays < 7 -> {
                    // 一周内，显示星期和时间
                    val weekDays = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"))
                    calendar.time = date
                    "${weekDays[calendar.get(Calendar.DAY_OF_WEEK) - 1]} ${displaySdf.format(date)}"
                }
                else -> {
                    // 超过一周，显示日期和时间
                    val dateSdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                    dateSdf.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
                    dateSdf.format(date)
                }
            }
        } else {
            ""
        }
    } catch (e: Exception) {
        Timber.e(e, "格式化消息时间失败: $timeString")
        ""
    }
}

/**
 * 消息列表项组件
 */
@Composable
private fun MessageListItem(counselor: Counselor, latestMessages: List<Message>? = null) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    
    // 查找当前咨询师的最新消息，添加健壮的数据类型转换和空值检查
    val latestMessage = latestMessages?.find { message ->
        try {
            // 安全地转换counselorId类型并比较
            message.counselorId == counselor.counselorId.toLong()
        } catch (e: Exception) {
            Timber.w(e, "Error matching message to counselor: ${counselor.counselorId}")
            false
        }
    }
    
    // 安全获取消息内容，处理可能的null值
    val messageContent = when {
        latestMessage?.content.isNullOrEmpty() -> "您好，请问有什么可以帮助您的吗？"
        else -> latestMessage.content
    }
    
    // 安全格式化消息时间
    val messageTime = try {
        if (!latestMessage?.sentTime.isNullOrEmpty()) {
            formatMessageTime(latestMessage.sentTime)
        } else {
            ""
        }
    } catch (e: Exception) {
        Timber.w(e, "Error formatting message time")
        ""
    }
    
    // 安全检查未读状态
    val isUnread = try {
        latestMessage?.readStatus == false && latestMessage.senderType == "COUNSELOR"
    } catch (e: Exception) {
        false
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(12.dp)
            .clickable {
                // 获取当前登录用户
                val loggedInUser = dbHelper.getLoggedInUser()
                if (loggedInUser != null && loggedInUser.id > 0) {
                    // 跳转到聊天详情页面
                    ChatDetailActivity.start(context, loggedInUser.id.toLong(), counselor)
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 咨询师头像
        val processedAvatarUrl = IpAddressManager.processImageUrl(counselor.photoUrl)
        AsyncImage(
            model = processedAvatarUrl,
            contentDescription = "咨询师头像",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),

        )
        
        // 消息内容
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = counselor.realName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = messageTime,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = messageContent,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                if (isUnread) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Red, CircleShape)
                    )
                }
            }
        }
    }
    
    // 分割线
    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color(0xFFEEEEEE)))
}

@Preview(showBackground = true)
@Composable
fun MessageScreenPreview() {
    MentalTheme {
        MessageScreen()
    }
}

/**
 * 跳转到图标展示页面
 */
fun navigateToIconDisplay(context: Context) {
    val intent = Intent(context, IconDisplayActivity::class.java)
    context.startActivity(intent)
}