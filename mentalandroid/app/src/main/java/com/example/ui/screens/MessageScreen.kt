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
import com.example.network.RetrofitClient
import com.example.ui.features.ChatDetailActivity
import com.example.ui.features.IconDisplayActivity
import com.example.ui.theme.MentalTheme
import com.example.util.DatabaseHelper
import com.example.util.IpAddressManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

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
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // 获取匹配的咨询师列表
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            // 获取当前登录用户
            val loggedInUser = dbHelper.getLoggedInUser()
            if (loggedInUser != null && loggedInUser.id > 0) {
                withContext(Dispatchers.IO) {
                    // 调用API获取匹配的咨询师
                    val response = RetrofitClient.apiService.getMatchedCounselors(loggedInUser.id.toLong())
                    if (response.code == 200 && response.data != null) {
                        counselors = response.data
                    } else {
                        error = "获取咨询师列表失败: ${response.msg}"
                    }
                }
            } else {
                error = "用户未登录"
            }
        } catch (e: Exception) {
            error = "网络请求失败: ${e.message}"
            Timber.e(e, "Failed to fetch matched counselors")
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
                        MessageListItem(counselor = it)
                    }
                }
            }
        }
    }
}

/**
 * 消息列表项组件
 */
@Composable
private fun MessageListItem(counselor: Counselor) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = counselor.realName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "您好，请问有什么可以帮助您的吗？",
                fontSize = 13.sp,
                color = Color.Gray,
                maxLines = 1
            )
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