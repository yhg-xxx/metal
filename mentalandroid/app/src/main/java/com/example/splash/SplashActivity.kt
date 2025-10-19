package com.example.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.R
import com.example.ui.screens.LoginActivity
import com.example.util.DatabaseHelper
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    private lateinit var dbHelper: DatabaseHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper(this)
        
        setContent {
            SplashScreen {        
                // 检查用户是否已登录
                val loggedInUser = dbHelper.getLoggedInUser()
                if (loggedInUser != null && loggedInUser.isLogin) {
                    // 已登录，直接跳转到主页
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                } else {
                    // 未登录，跳转到登录页
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                }
                finish()
            }
        }
    }
}

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    var countdown by remember { mutableIntStateOf(3) }
    // 添加状态变量防止重复触发跳转
    var isNavigated by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        repeat(3) {
            delay(1000)
            countdown--
        }
        if (!isNavigated) {
            isNavigated = true
            onFinish()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 显示开屏广告图片
        // 由于在res/photo目录下的图片无法直接通过R.photo.img访问
        // 这里假设图片已经被正确放置在drawable目录下
        // 如果需要从其他位置加载图片，可能需要使用其他方法
        Image(
            painter = painterResource(id = R.drawable.img),
            contentDescription = "开屏广告",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // 右上角的跳过按钮
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable {
                    if (!isNavigated) {
                        isNavigated = true
                        onFinish()
                    }
                }
                .padding(8.dp, 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "跳过",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = "$countdown",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}