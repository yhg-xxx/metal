package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.example.splash.SplashActivity
import com.example.util.DatabaseHelper
import com.example.util.IpAddressManager
import com.example.ui.features.ProfileEditActivity
import com.example.ui.theme.MentalTheme

/**
 * 个人主页屏幕组件
 * 简化版：只显示头像、用户名、查看修改个人信息和退出登录功能
 */
@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    var loggedInUser by remember { mutableStateOf(dbHelper.getLoggedInUser()) }
    var username by remember { mutableStateOf(loggedInUser?.username ?: "用户${(10000..99999).random()}") }
    
    LaunchedEffect(key1 = loggedInUser) {
        username = loggedInUser?.username ?: "用户${(10000..99999).random()}"
    }
    
    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF7F7F7))) {
        // 顶部标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF5A67D8))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "我的",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { /* 设置按钮点击事件 */ }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "设置",
                    tint = Color.White
                )
            }
        }
        
        // 用户信息卡片
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 用户头像
                val currentUser = loggedInUser
                val avatarUrl = currentUser?.avatarUrl
                val processedAvatarUrl = IpAddressManager.processImageUrl(avatarUrl)
                AsyncImage(
                    model = processedAvatarUrl,
                    contentDescription = "用户头像",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )
                
                // 用户信息
                Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    Text(
                        text = username,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // 移除"点击登录/注册"文字
                }
                
                // 进入个人资料按钮
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .background(Color(0xFFF0F0F0))
                        .clip(RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp)
                        .clickable { 
                            if (loggedInUser == null) {
                                // 如果未登录，跳转到登录页
                                val intent = Intent(context, LoginActivity::class.java)
                                context.startActivity(intent)
                            } else {
                                // 已登录，进入个人资料详情
                                val intent = Intent(context, ProfileEditActivity::class.java)
                                context.startActivity(intent)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "查看/修改",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
        
        // 退出登录按钮
        if (loggedInUser != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color.White)
                    .padding(horizontal = 16.dp)
                    .clickable {
                        // 执行退出登录操作
                        dbHelper.logout()
                        loggedInUser = null
                        // 重新启动应用，跳转到启动页
                        val intent = Intent(context, SplashActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "退出登录",
                    fontSize = 16.sp,
                    color = Color.Red
                )
            }
        }
        
        // 底部间距
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MentalTheme {
        ProfileScreen()
    }
}